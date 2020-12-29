/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.esn;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Remises;
import clearing.table.flexcube.STTM_BRANCH;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerSurCaisseCreditFlexCubeSQLWriter extends FlatFileWriter {

    public ChequeAllerSurCaisseCreditFlexCubeSQLWriter() {
        setDescription("Envoi des chèques Non Compensable vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
        String sql = "select  CURRENT_CYCLE,CURRENT_PERIOD from " + Utility.getParam("FLEXSCHEMA") + ".STTM_BRANCH where branch_code='001' ";
        STTM_BRANCH[] sttm_branch = (STTM_BRANCH[]) dbExt.retrieveRowAsObject(sql, new STTM_BRANCH());
        String current_cycle = "";
        String current_period = "";
        if (sttm_branch != null && sttm_branch.length > 0) {
            current_cycle = sttm_branch[0].getCurrent_cycle();
            current_period = sttm_branch[0].getCurrent_period();
        }
        dbExt.close(); //CHQCAISSE_IN_FILE_ROOTNAME1                       
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1") + Utility.convertDateToString(new Date(), "ddMMyyyy") + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");
//        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND LOTSIB =1 ORDER BY REMISE";
//select c.* from cheques c , remises r  where

        sql = "SELECT C.* FROM CHEQUES C , REMISES R WHERE C.ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND C.LOTSIB =1 "
                + " AND C.REMISE=R.IDREMISE"
                + " and r.nboperation = (SELECT count(*) from cheques chq where chq.etat=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND C.LOTSIB =1  AND chq.REMISE=r.idremise   ) "
                + " ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        int a = 0;
        long montantTotal = 0;
        int numLigne = 1;
        Cheques[] chequesVal = null;
        Remises[] remises = null;

        if (cheques != null && 0 < cheques.length) {

            setOut(createFlatFile(fileName));
            createEnteteSQL();
            for (int i = 0; i < cheques.length; i += j) {

                //Tous les cheques non compensables validés d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND LOTSIB=1 AND ETAT =" + Utility.getParam("CETAOPESUPVALSURCAI");
                chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                //La remise en question
                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                //Tous les cheques de la remise (compensables et non) //controle de coherence sur le nombre de cheques de la remise
                sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND "
                        + " LOTSIB = 1 AND REMISE=" + cheques[i].getRemise();  //hold de la remise ici
                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

//                    //Tous les cheques de la remise (compensables et non) //controle de coherence sur le nombre de cheques de la remise
//                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise();
//                    Cheques[] allChequesValAPayer = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                if ((remises != null && 0 < remises.length)
                        && (allChequesVal.length == remises[0].getNbOperation().intValue())) {
                    if (chequesVal != null && 0 < chequesVal.length) {
                        long sumRemise = 0;
                        j = chequesVal.length;

                        Cheques aCheque = chequesVal[0];

                        //Creation ligne de chèque
                        if (!isValidLine(cheques[i])) {

                        } else {
                            for (int x = 0; x < chequesVal.length; x++) {
                                sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                            }

                            montantTotal += sumRemise;
                            a += chequesVal.length;
                            String libelle;
                            String dateValeur;
                            Comptes cptGR = CMPUtility.getInfoCompte(aCheque.getCompteremettant());
                            if (cptGR.getSignature2() == null || (cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("R"))) {

                                if ((cptGR.getSignature3() != null && cptGR.getSignature3().trim().equals("O"))) {
                                    libelle = remises[0].getReference();
                                } else {
                                    libelle = "Versement Remise N°" + remises[0].getReference();
                                }

                                //Credit du bordereau remise sur le compte du gros remettant
                                dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), "yyyy/MM/dd"), "ddMMyyyy");
                                //  createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);
                                numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getRemise(),
                                        "" + sumRemise,
                                        dateValeur, aCheque, libelle);

                            } else if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("C"))) {

                                libelle = "VRS CH" + aCheque.getNumerocheque() + "_" + aCheque.getRemise();
                                //Credit cheque par cheque sur le compte du gros remettant

                                dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), "yyyy/MM/dd"), "ddMMyyyy");
                                for (int x = 0; x < chequesVal.length; x++) {

                                    aCheque = chequesVal[x];
                                    libelle = "VERSEMENT CHEQUE" + Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " ") + "_" + aCheque.getRefremise();
//                                    createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                                    numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getIdcheque(),
                                            aCheque.getMontantcheque(), dateValeur, aCheque, libelle);

                                }

                            }
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + ""
                                    + "  AND LOTSIB=1 AND REMISE=" + aCheque.getRemise());
                        }

                    }

                }

                //CETAOPEVALSURCAIENVSIB                            
            }
            //  db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque= " + a + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + a + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

            closeFile();

            fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_ERR_FILE_ROOTNAME1") + Utility.convertDateToString(new Date(), "ddMMyyyy") + "_" + numeroBatch + Utility.getParam("SIB_FILE_EXTENSION");
            sql = "SELECT C.* FROM CHEQUES C , REMISES R WHERE C.ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND C.LOTSIB =1 "
                    + " AND C.REMISE=R.IDREMISE"
                    + " and r.nboperation = (SELECT count(*) from cheques chq where chq.etat=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND C.LOTSIB =1  AND chq.REMISE=r.idremise   ) "
                    + " ORDER BY REMISE";
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

            long montantTotalEchec = 0;
            if (cheques != null && 0 < cheques.length) {

                setOut(createFlatFile(fileName));
                for (int i = 0; i < cheques.length; i++) {
                    Cheques cheque = cheques[i];
                    if (!isValidLine(cheques[i])) {
                        StringBuilder line = new StringBuilder();

                        line.append(cheque.getAgenceremettant());
                        line.append(";");
                        line.append(cheque.getCompteremettant());
                        line.append(";");
                        line.append(cheque.getMontantcheque());
                        line.append(";");
                        line.append(cheque.getAgence());
                        line.append(";");
                        line.append(cheque.getNumerocompte());

                        writeln(line.toString());
                        montantTotalEchec += Long.parseLong(cheque.getMontantcheque());

                    }

                    if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPESUPVALSURCAI")) && cheque.getLotsib().equals(new BigDecimal("1"))) {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }

                }
                closeFile();
                setDescription(getDescription() + "<br>Echec des Cheques Sur Caisse - Credit des Clients");
                setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque Sur Caisse - Credit des Clients en echec = " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotalEchec) + " - Nom de Fichier Echec =  <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "\">" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "</a>" + "<br>");
                logEvent("INFO", "Nombre de Chèque Sur Caisse - Credit des Clients en echec = " + cheques.length + " - Montant Total  en echec = " + Utility.formatNumber("" + montantTotalEchec));
            }

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }

    private void createEnteteSQL() {
        StringBuilder line = new StringBuilder();

        //entete
        line.append("LOAD DATA\n"
                + "INFILE *\n"
                + "APPEND\n"
                + "INTO TABLE DETB_UPLOAD_DETAIL\n"
                + "Fields terminated by '~'\n"
                + "Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account)\n"
                + "BEGINDATA");
        writeln(line.toString());
    }

    private boolean isValidLine(Cheques cheque) throws Exception {
        //Verification de l'existence du compte
        String numCptEx = CMPUtility.getNumCptEx(cheque.getCompteremettant(), cheque.getAgenceremettant(),"1");
        if (numCptEx == null) {
            return false;
        }

        //Verification du compte de scan
        if (cheque.getCompteremettant().equals(Utility.getParam("CPTATTSCANCHQ"))) {
            return false;
        }
        //Verification des manager cheques
        if (cheque.getNumerocompte().equals(Utility.getParam("CPTFLEXMCACCOUNT"))) {
            return false;
        }
        return true;
    }

    /**
     * *
     *
     * @param current_cycle
     * @param current_period
     * @param numeroBatch
     * @param numLigne
     * @param separateur
     * @param referenceRelative
     * @param montantLigne
     * @param dateValeur
     * @param aCheque
     * @param libelle
     * @param montantFrais
     * @throws Exception
     */
    private int createLinesSQL(String current_cycle, String current_period, String numeroBatch, int numLigne, String separateur, String referenceRelative,
            String montantLigne, String dateValeur, Cheques aCheque, String libelle) throws Exception {
        //   + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
        //     + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account)\n"
        String numCptEx = CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"1");
        if (numCptEx == null) {
            numCptEx = aCheque.getAgenceremettant().substring(2) + "0" + aCheque.getCompteremettant();
        }

        /**
         * Format SQL LOADER
         */
        //Ligne de debit du montant du bordereau sur le compte d'attente
        //Ligne de debit
        StringBuilder line = new StringBuilder();
        line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
        line.append(separateur);
        line.append(current_period); //PERIOD_CODE a recuperer par une requete
        line.append(separateur);
        line.append(dateValeur); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append("001"); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append("001");
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQCAIFLEX"));
        line.append(separateur);
        line.append("F03");
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montantLigne);
        line.append(separateur);
        line.append("D");
        line.append(separateur);
        line.append("U");
        line.append(separateur);
        line.append("XOF");
        line.append(separateur);
        line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
        line.append(separateur);
        line.append(montantLigne);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(numCptEx); // related_account
        writeln(line.toString());

        //Ligne de credit sur le compte client
        line = new StringBuilder();
        line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
        line.append(separateur);
        line.append(current_period); //PERIOD_CODE a recuperer par une requete
        line.append(separateur);
        line.append(dateValeur); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append("001"); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append(numCptEx != null ? numCptEx.substring(0, 3) : "");  //agence du compte
        line.append(separateur);
        line.append(numCptEx);
        line.append(separateur);
        line.append("F57");
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montantLigne);
        line.append(separateur);
        line.append("C");
        line.append(separateur);
        line.append("U");
        line.append(separateur);
        line.append("XOF");
        line.append(separateur);
        line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
        line.append(separateur);
        line.append(montantLigne);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append((Utility.getParam("CPTATTCHQCAIFLEX").length() == 16) ? Utility.getParam("CPTATTCHQCAIFLEX").substring(6, 14) : "99999999"); //rel_cust // usually ==> line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust '0017201000019901' 
        //   line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQCAIFLEX")); //  // related_account
        writeln(line.toString());

        return numLigne;

    }

}
