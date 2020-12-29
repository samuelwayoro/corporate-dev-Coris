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
public class ChequeAllerGRFlexCubeSQLWriter extends FlatFileWriter {

    public ChequeAllerGRFlexCubeSQLWriter() {
        setDescription("Envoi des chèques vers le SIB");
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

        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
//        String sql = "select to_char(next_working_day,'YYYY/MM/DD') as next_working_day from boesn.STTM_AEOD_DATES where branch_code ='001'";
//        STTM_AEOD_DATES[] sttm_aeod_dates = (STTM_AEOD_DATES[]) dbExt.retrieveRowAsObject(sql, new STTM_AEOD_DATES());
//        String dateValeurJ2 = "";
//        if (sttm_aeod_dates != null && sttm_aeod_dates.length > 0) {
//            dateValeurJ2 = sttm_aeod_dates[0].getNext_working_day();
//        }

        String dateValeurJ2 = "";
        param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeurJ2 = param1[0];
        }
        System.out.println("Date Valeur = " + dateValeurJ2);
        dateValeurJ2 = Utility.convertDateToString(Utility.convertStringToDate(dateValeurJ2, "yyyyMMdd"), "ddMMyyyy");

        String sql = "select  CURRENT_CYCLE,CURRENT_PERIOD from "+Utility.getParam("FLEXSCHEMA") +".STTM_BRANCH where branch_code='001' ";
        STTM_BRANCH[] sttm_branch = (STTM_BRANCH[]) dbExt.retrieveRowAsObject(sql, new STTM_BRANCH());
        String current_cycle = "";
        String current_period = "";
        if (sttm_branch != null && sttm_branch.length > 0) {
            current_cycle = sttm_branch[0].getCurrent_cycle();
            current_period = sttm_branch[0].getCurrent_period();
        }
        dbExt.close();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

// Population
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") AND ESCOMPTE='1' ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        int numLigne = 1;

        if (cheques != null && 0 < cheques.length) {
            cheques[0].getDatecompensation();
            String dateTraitement = Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatetraitement(), "yyyy/MM/dd"), "ddMMyyyy");
            String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_CRI_FILE_ROOTNAME") + dateTraitement +"_"+numeroBatch +Utility.getParam("SIB_FILE_SQL_EXTENSION");

            setOut(createFlatFile(fileName));
            createEnteteSQL();
            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques compensables validés d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") AND ESCOMPTE='1' ORDER BY REMISE";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                if (remises != null && remises.length > 0) {

                    if (chequesVal != null && 0 < chequesVal.length) {
                        j = chequesVal.length;
                        long sumRemise = 0;
                        long montantFrais = j * Long.parseLong(Utility.getParam("FRAIS_SICA"));

                        Cheques aCheque = chequesVal[0];
                        for (int x = 0; x < chequesVal.length; x++) {
                            sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;

                        //Creation ligne de chèque
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
                            if ((cptGR.getSignature1() != null && cptGR.getSignature1().trim().equals("J"))) {

                                dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), "yyyy/MM/dd"), "ddMMyyyy");
                                numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getRemise(), "" + sumRemise,
                                        dateValeur, aCheque, libelle, montantFrais);
//                                createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);

                            }

                            if (cptGR.getSignature1() != null && cptGR.getSignature1().equals("J1")) {

                                dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatecompensation(), "yyyy/MM/dd"), "ddMMyyyy");
//                                createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);
                                numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getRemise(), "" + sumRemise,
                                        dateValeur, aCheque, libelle, montantFrais);
                            }
                            if (cptGR.getSignature1() != null && cptGR.getSignature1().equals("J2")) {

                                dateValeur =dateValeurJ2 ;
//                                createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);
                                numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getRemise(), "" + sumRemise,
                                        dateValeur, aCheque, libelle, montantFrais);
                            }

                            System.out.println("numLigne R" + numLigne);
                        } else if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("C"))) {
                            System.out.println("numLigne pour les cheques " + numLigne);
                            
                            //Credit cheque par cheque sur le compte du gros remettant

                            if ((cptGR.getSignature1() != null && cptGR.getSignature1().trim().equals("J"))) {

                                dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), "yyyy/MM/dd"), "ddMMyyyy");
                                for (int x = 0; x < chequesVal.length; x++) {
                                    aCheque = chequesVal[x];
                                    libelle = "VERSEMENT CHEQUE" + Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " ") + "_" + aCheque.getRefremise(); //Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ");

                                    montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                                    
//                                    createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                                    numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getIdcheque(),
                                            aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);

                                }

                            }

                            if (cptGR.getSignature1() != null && cptGR.getSignature1().equals("J1")) {
                                dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatecompensation(), "yyyy/MM/dd"), "ddMMyyyy");
                                for (int x = 0; x < chequesVal.length; x++) {

                                    montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                                    aCheque = chequesVal[x];
                                    libelle = "VERSEMENT CHEQUE" + Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " ") + "_" + aCheque.getRefremise(); //Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ");

//                                    createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                                    numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getIdcheque(),
                                            aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);

                                }

                            }
                            if (cptGR.getSignature1() != null && cptGR.getSignature1().equals("J2")) {
                                dateValeur =dateValeurJ2 ;
                                for (int x = 0; x < chequesVal.length; x++) {

                                    montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                                    aCheque = chequesVal[x];
                                    libelle = "VERSEMENT CHEQUE" + Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " ") + "_" + aCheque.getRefremise(); //Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ");

//                                    createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                                    numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getIdcheque(),
                                            aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);

                                }

                            }
                             
                        }

                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + " WHERE REMISE=" + aCheque.getRemise());
                    }
                }

            }

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
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

    /**
     *
     * @param current_cycle Annee Fiscale
     * @param current_period Mois
     * @param numeroBatch numero de Batch
     * @param numLigne Numero incrementiel de la ligne
     * @param separateur Separateur ~
     * @param referenceRelative Reference relative (IDREMISE ou IDCHEQUE)
     * @param montantLigne Montant de la Ligne inseree
     * @param dateValeur Date Valeur
     * @param aCheque le Cheque
     * @param libelle Libelle
     * @param montantFrais Montant des Frais
     * @throws Exception
     */
    private int createLinesSQL(String current_cycle, String current_period, String numeroBatch, int numLigne, String separateur, String referenceRelative,
            String montantLigne, String dateValeur, Cheques aCheque, String libelle, long montantFrais) throws Exception {
        StringBuilder line;
        String numCptEx = CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"1");
        if (numCptEx == null) {
            numCptEx = aCheque.getAgenceremettant().substring(2) + "0" + aCheque.getCompteremettant();
        }

        /**
         * Format SQL LOADER
         */
        //Ligne de debit du montant du bordereau sur le compte d'attente
        //Ligne de debit
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
        line.append("001");
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALECRIFLEX"));
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
        line.append(numCptEx); // //Related Account
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
        line.append(numCptEx != null ? numCptEx.substring(0, 3) : aCheque.getAgenceremettant().substring(2));  //agence du compte
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
        line.append((Utility.getParam("CPTATTCHQALECRIFLEX").length() == 16) ? Utility.getParam("CPTATTCHQALECRIFLEX").substring(6, 14) : "99999999"); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALECRIFLEX")); // //Related Account
        writeln(line.toString());

        libelle = "Frais sica Remise N°" + aCheque.getRefremise();
        //Ligne de debit des frais sica sur le compte client
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
        line.append(numCptEx != null ? numCptEx.substring(0, 3) : aCheque.getAgenceremettant().substring(2));  //agence du compte
        line.append(separateur);
        line.append(numCptEx);
        line.append(separateur);
        line.append("C81");
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montantFrais);
        line.append(separateur);
        line.append("D");
        line.append(separateur);
        line.append("U");
        line.append(separateur);
        line.append("XOF");
        line.append(separateur);
        line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
        line.append(separateur);
        line.append(montantFrais);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append((Utility.getParam("CPTCOMCHQALEFLEX").length() == 16) ? Utility.getParam("CPTCOMCHQALEFLEX").substring(6, 14) : "99999999"); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTCOMCHQALEFLEX")); // //Related Account
        writeln(line.toString());

        //Credit du compte des frais
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
        line.append("001");
        line.append(separateur);
        line.append(Utility.getParam("CPTCOMCHQALEFLEX")); //numCptEx != null ? numCptEx.substring(6, 14) : ""
        line.append(separateur);
        line.append("C81");
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montantFrais);
        line.append(separateur);
        line.append("C");
        line.append(separateur);
        line.append("U");
        line.append(separateur);
        line.append("XOF");
        line.append(separateur);
        line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
        line.append(separateur);
        line.append(montantFrais);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(numCptEx); // //Related Account
        writeln(line.toString());

        return numLigne;
    }

}
