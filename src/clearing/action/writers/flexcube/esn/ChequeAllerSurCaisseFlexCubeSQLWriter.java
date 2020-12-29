/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.esn;

import clearing.action.writers.corporates.RejetNonComChequeCorporatesWriter;
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
public class ChequeAllerSurCaisseFlexCubeSQLWriter extends FlatFileWriter {

    public ChequeAllerSurCaisseFlexCubeSQLWriter() {
        setDescription("Envoi des Cheques sur Caisse Debit/Credit");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        System.out.println("Envoi des Cheques sur Caisse Debit/Credit");

        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);

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
        dbExt.close();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        long montantTotalRejete = 0;
        long montantTotalEchec = 0;
        int numLigne = 1;
        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQEXTCAISSE_IN_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");
        setOut(createFlatFile(fileName));

        createEnteteSQL();

        // Population des Cheques à payer == tous les cheques de la remise non compensable
        sql = " SELECT * FROM CHEQUES WHERE ( "
                + "( ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND LOTSIB =1) " //800 et 1
                + " OR "
                + " ( ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =1) " //DEBITE DANS UN AUTRE BATCH ou ENCORE HOLDED QD LE REFER donne les sorts 822 et lotsib 1; il est payé
                + " OR "
                + " ( ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =2)   " //REJETE avec frais 822 et lotsib 2 (retour refer) 
                + " OR "
                + " ( ETAT =" + Utility.getParam("CETAOPEANO") + " AND LOTSIB =1 )" //REJETE sans frais 20 et lotsib 1
                + " ) "
                + " AND BANQUE=BANQUEREMETTANT "
                + " ORDER BY REMISE DESC";
//        sql = " SELECT * FROM CHEQUES WHERE ( "
//                + "( ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND LOTSIB =1)"
//                //////                + " OR " //payed
//                //////                + "( ETAT=" + Utility.getParam("CETAOPEANO") + " AND LOTSIB =3) OR " //hold
//                //////                + "( ETAT=" + Utility.getParam("CETAOPEANO") + " AND LOTSIB =1) OR " //hold
//                //////                + "( ETAT=" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =2) OR " //REJET
//                //////                + " (ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =1) OR  " //DEBITE DANS UN AUTRE BATCH ou ENCORE HOLDED QD LE REFER donne les sorts
//                //////                + "( ETAT=" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =3)  " //REJET
//                + " ) "
//                + " AND BANQUE=BANQUEREMETTANT "
//                + " ORDER BY REMISE ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        System.out.println("cheques a payer");
        int j = 0;
        long montantTotal = 0;
        montantTotalEchec = 0;

        if (cheques != null && 0 < cheques.length) {
            System.out.println("cheques a payer" + cheques.length + " Tableau");
            for (int i = 0; i < cheques.length; i += j) {

                {
                    //Tous les cheques validés

                    //Tous les cheques non compensables  d'une remise Validés ou Flagués parce que deja presentés et deja payé
                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + ""
                            ////                            + "  AND ( "
                            ////                            + "( ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND LOTSIB =1) OR " //payed
                            ////                            + "( ETAT=" + Utility.getParam("CETAOPEANO") + " AND LOTSIB =3) OR " //hold
                            ////                            + "( ETAT=" + Utility.getParam("CETAOPEANO") + " AND LOTSIB =1) OR " //hold
                            ////                            + "( ETAT=" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =2) OR " //REJET
                            ////                            + " (ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =1) OR  " //DEBITE DANS UN AUTRE BATCH ou ENCORE HOLDED QD LE REFER donne les sorts
                            ////                            + "( ETAT=" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =3)  " //REJET
                            ////                            + " ) "
                            + " AND BANQUE=BANQUEREMETTANT "
                            + " ORDER BY REMISE DESC";
                    Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    System.out.println("Remise du Cheque " + cheques[i].getRemise());
                    sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                    if (remises != null && remises.length > 0) {

                        if (chequesVal != null && 0 < chequesVal.length) {
                            j = chequesVal.length;
                            long sumRemise = 0;

                            if (!isValidLine(cheques[i])) {
                            } else {

                                Cheques aCheque = chequesVal[0];
                                for (int x = 0; x < chequesVal.length; x++) {
                                    System.out.println("on est dans la sommation de la remise aCheque" + chequesVal[x].getIdcheque() + " Remise " + chequesVal[x].getRemise());
                                    sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                                }
                                montantTotal += sumRemise;

                                //Creation ligne de chèque
                                String libelle;
                                String dateValeur;
                                Comptes cptGR = CMPUtility.getInfoCompte(aCheque.getCompteremettant());
                                System.out.println("cptGR " + cptGR.getNom());

                                if (cptGR.getSignature2() == null || (cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("R"))) {
                                    System.out.println("Credit par remise");

                                    if ((cptGR.getSignature3() != null && cptGR.getSignature3().trim().equals("O"))) {
                                        libelle = remises[0].getReference();
                                    } else {
                                        libelle = "Versement Remise N°" + remises[0].getReference();
                                    }

                                    //Credit du bordereau remise sur le compte du gros remettant
                                    dateValeur = dateTraitement;
                                    //  createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);
                                    numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getRemise(), "" + sumRemise,
                                            dateValeur, aCheque, libelle);
                                    System.out.println("Ecriture createLinesSQL");

                                } else if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("C"))) {
                                    System.out.println("Credit Par cheque");

                                    //Credit cheque par cheque sur le compte du gros remettant
                                    dateValeur = dateTraitement;
                                    for (int x = 0; x < chequesVal.length; x++) {
                                        aCheque = chequesVal[x];
                                        libelle = "Versement Cheque : " + Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " ") + "_" + aCheque.getRefremise(); //Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ");

                                        numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getIdcheque(),
                                                aCheque.getMontantcheque(), dateValeur, aCheque, libelle);
                                        System.out.println("Ecriture createLinesSQL pour le cheque");
                                    }

                                }

                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + ""
                                        + "  AND LOTSIB=1 AND REMISE=" + aCheque.getRemise());
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPENONCOMREJ") + ""
                                        + "  AND LOTSIB=1 AND REMISE=" + aCheque.getRemise());
//                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEANO") + ", LOTSIB=3 WHERE ETAT=" + Utility.getParam("CETAOPEANO") + ""
//                                    + "    AND REMISE=" + aCheque.getRemise());
                            }
                        }
                    }
                }

            }

            setDescription("<br>Envoi des chèques Aller Sur Caisse vers le SIB");
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque Sur Caisse= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {

            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        sql = "SELECT * FROM CHEQUES WHERE "
                + " (ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =2) OR  " //REJETE
                //     + " (ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =1) OR  " //DEBITE DANS UN AUTRE BATCH ou ENCORE HOLDED QD LE REFER donne les sorts
                + " ( ETAT =" + Utility.getParam("CETAOPEANO") + " AND LOTSIB =1 )"
                + " AND BANQUE=BANQUEREMETTANT "
                + " ORDER BY REMISE"; ///
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        if (cheques != null && 0 < cheques.length) {
            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                if (!isValidLine(cheque)) {
                } else {

                    numLigne = createLinesRjtSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + cheque.getIdcheque(),
                            cheque.getMontantcheque(), Utility.convertDateToString(new java.util.Date(System.currentTimeMillis()), "ddMMyyyy"), cheque, "Rejet Cheque");
                    //  createLinesRejetFlex(cheque);
                    if (cheque.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPENONCOMREJ")))) {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPENONCOMREJ")));
                        cheque.setLotsib(new BigDecimal(3)); //mise a jour du lotsib
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }
                    if (cheque.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEANO"))) && cheque.getLotsib().equals(new BigDecimal(1))) {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        cheque.setLotsib(new BigDecimal(3)); //mise a jour du lotsib
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }
                    montantTotalRejete += Long.parseLong(cheque.getMontantcheque());
                }

            }
            setDescription(getDescription() + "Envoi des rejets de chèques Non Compensables vers le SIB");
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque rejete= " + cheques.length + " - Montant Total rejete= " + Utility.formatNumber("" + montantTotalRejete) + " - Nom de Fichier = " + fileName + "<br>");
            logEvent("INFO", "Nombre de Chèque rejete = " + cheques.length + " - Montant Total rejete = " + Utility.formatNumber("" + montantTotalRejete));
        }

        closeFile();

        //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Creation du fichier d'Echec
        //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        sql = "SELECT * FROM CHEQUES WHERE "
                + " (ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =2) OR  " //REJETE
                //     + " (ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =1) OR  " //DEBITE DANS UN AUTRE BATCH ou ENCORE HOLDED QD LE REFER donne les sorts
                + " ( ETAT =" + Utility.getParam("CETAOPEANO") + " AND LOTSIB =1 )"
                + " AND BANQUE=BANQUEREMETTANT "
                + " ORDER BY REMISE"; ///
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        dateTraitement = Utility.convertDateToString(new Date(), "ddMMyyyy");
        fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQEXTCAISSE_IN_ERR_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_EXTENSION");

        setOut(createFlatFile(fileName));
        createEnteteSQL();
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];

//                createLinesRejetFlex(cheque);
                int createLinesRejetSQL = createLinesRjtSQL(current_cycle, current_period, numeroBatch, i + 1, "~", numeroBatch + "" + cheque.getIdcheque(),
                        cheque.getMontantcheque(), Utility.convertDateToString(new Date(), "ddMMyyyy"), cheque, "Rejet Cheque");

                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPENONCOMREJ"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPENONCOMREJ")));
                    cheque.setLotsib(new BigDecimal(3)); //mise a jour du lotsib
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                if (cheque.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEANO"))) && cheque.getLotsib().equals(new BigDecimal(1))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                    cheque.setLotsib(new BigDecimal(3)); //mise a jour du lotsib
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                montantTotalRejete += Long.parseLong(cheque.getMontantcheque());

            }
            setDescription(getDescription() + "<br>Echec des rejets de chèques non Compensables");
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque rejete en echec = " + cheques.length + " - Montant Total rejete en echec = " + Utility.formatNumber("" + montantTotalRejete) + " - Nom de Fichier = " + fileName + "<br>");
            logEvent("INFO", "Nombre de Chèque rejete en echec = " + cheques.length + " - Montant Total rejete en echec = " + Utility.formatNumber("" + montantTotalRejete));
        }

        // Population pour les cheques en Echec
        sql = " SELECT * FROM CHEQUES WHERE ( "
                + "( ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND LOTSIB =1) " //800 et 1
                + " OR " //payed

                + " ( ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =1)   " //DEBITE DANS UN AUTRE BATCH ou ENCORE HOLDED QD LE REFER donne les sorts 822 et 1
                //////                + "( ETAT=" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =3)  " //REJET
                + " ) "
                + " AND BANQUE=BANQUEREMETTANT "
                + " ORDER BY REMISE ";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        j = 0;
        montantTotal = 0;
        montantTotalEchec = 0;

        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques compensables validés d'une remise
                //Tous les cheques non compensables  d'une remise Validés ou Flagués parce que deja presentés et deja payé
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + ""
                        ////                            + "  AND ( "
                        ////                            + "( ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND LOTSIB =1) OR " //payed
                        ////                            + "( ETAT=" + Utility.getParam("CETAOPEANO") + " AND LOTSIB =3) OR " //hold
                        ////                            + "( ETAT=" + Utility.getParam("CETAOPEANO") + " AND LOTSIB =1) OR " //hold
                        ////                            + "( ETAT=" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =2) OR " //REJET
                        ////                            + " (ETAT =" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =1) OR  " //DEBITE DANS UN AUTRE BATCH ou ENCORE HOLDED QD LE REFER donne les sorts
                        ////                            + "( ETAT=" + Utility.getParam("CETAOPENONCOMREJ") + " AND LOTSIB =3)  " //REJET
                        ////                            + " ) "
                        + " AND BANQUE=BANQUEREMETTANT "
                        + " ORDER BY REMISE";
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

                        //Credit cheque par cheque sur le compte du gros remettant
                        dateValeur = dateTraitement;
                        for (int x = 0; x < chequesVal.length; x++) {

                            montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                            aCheque = chequesVal[x];
                            libelle = "Versement Cheque :" + aCheque.getNumerocheque() + "_" + aCheque.getRemise();
                            //     createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                            numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getIdcheque(),
                                    aCheque.getMontantcheque(), dateValeur, aCheque, libelle);

                        }

                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + ""
                                + "  AND LOTSIB=1 AND REMISE=" + aCheque.getRemise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEVALSURCAIENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPENONCOMREJ") + ""
                                + "  AND LOTSIB=1 AND REMISE=" + aCheque.getRemise());
                    }
                }

            }

            setDescription(getDescription() + "<br>Fichier Echec des chèques Aller vers le SIB");
            setDescription(getDescription() + " :<br> Nombre de Chèque en echec = " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotal) + " "
                    + " - Nom de Fichier Echec =  <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "\">" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "</a>");
            logEvent("INFO", "Nombre de Chèque Aller en echec= " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotal));

        }
        closeFile();

        System.out.println("Pour Corporates RejetNonComChequeCorporatesWriter");
        new RejetNonComChequeCorporatesWriter().execute();

        db.close();
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

    private void createEnteteSQL() {
        System.out.println("createEnteteSQL");
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
     * @throws Exception
     */
    private int createLinesRjtSQL(String current_cycle, String current_period, String numeroBatch, int numLigne, String separateur, String referenceRelative,
            String montantLigne, String dateValeur, Cheques aCheque, String libelle) throws Exception {
        //   + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
        //     + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account)\n"
        String numCptExDO = CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(),"1");
        if (numCptExDO == null) {
            numCptExDO = aCheque.getAgence().substring(2) + "0" + aCheque.getNumerocompte();
        }

        String numCptExBenef = CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"1");
        if (numCptExBenef == null) {
            numCptExBenef = aCheque.getAgenceremettant().substring(2) + "0" + aCheque.getCompteremettant();
        }

        if (!aCheque.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEANO")))) {
            //Cheque debité auparavant
            /**
             * Format SQL LOADER
             */
            //Ligne de debit du montant du cheque sur le compte d'attente
            //Ligne de debit compte d'attente pour credit le benef

            //Ligne de debit du Benef du montant du cheque rejeté
            StringBuilder line = new StringBuilder();
            libelle = "Rejet Cheque :" + aCheque.getNumerocheque() + " Remise :" + aCheque.getRefremise() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet()) + " Faveur " + aCheque.getNombeneficiaire().trim();
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
            line.append(numCptExBenef != null ? numCptExBenef.substring(0, 3) : "");  //agence du compte
            line.append(separateur);
            line.append(numCptExBenef);
            line.append(separateur);
            line.append("F57");
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
            line.append(numCptExDO != null ? numCptExDO.substring(6, 14) : ""); //rel_cust // usually ==> line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust '0017201000019901' 
            //      line.append(numCptExDO != null ? numCptExDO.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptExDO); //  // related_account
            writeln(line.toString());

            //Ligne de Credit du DO
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
            line.append(numCptExDO != null ? numCptExDO.substring(0, 3) : "");  //agence du compte
            line.append(separateur);
            line.append(numCptExDO);
            line.append(separateur);
            line.append("F03");
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
            line.append((numCptExBenef != null) ? numCptExBenef.substring(6, 14) : "99999999"); //rel_cust // usually ==> line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust '0017201000019901' 
            //      line.append(numCptExDO != null ? numCptExDO.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptExBenef); //  // related_account
            writeln(line.toString());
//(Utility.getParam("CPTATTCHQCAIFLEX").length() == 16) ? Utility.getParam("CPTATTCHQCAIFLEX").substring(6, 14) : "99999999"
            /**
             * //Prise de commission
             */
            //Ligne de Debit du donneur d'ordre
            libelle = "DEBIT NOS FRAIS REJET + TOB COMPTE CLIENT :" + aCheque.getNumerocheque() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet());
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
            line.append(numCptExDO != null ? numCptExDO.substring(0, 3) : "");  //agence du compte
            line.append(separateur);
            line.append(numCptExDO);
            line.append(separateur);
            line.append("C59");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(Utility.getParam("COMDEBREJCHQRET"));
            line.append(separateur);
            line.append("D");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParam("COMDEBREJCHQRET"));
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append((Utility.getParam("CPTCRECOMREJCHQRET1").length() == 16) ? Utility.getParam("CPTCRECOMREJCHQRET1").substring(6, 14) : "99999999"); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(Utility.getParam("CPTCRECOMREJCHQRET1")); //  // related_account
            writeln(line.toString());

            //Credit du compte de commission
            line = new StringBuilder();
            libelle = "CREDIT COMPTE NOS FRAIS REJET " + aCheque.getNumerocheque() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet());
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
            line.append(Utility.getParam("CPTCRECOMREJCHQRET1")); //Compte Client ou attente
            line.append(separateur);
            line.append("C59");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(Utility.getParam("COMCREREJCHQRETFLEX1")); //COMCREREJCHQRET
            line.append(separateur);
            line.append("C");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParam("COMCREREJCHQRETFLEX1"));
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append(numCptExDO != null ? numCptExDO.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptExDO); // 
            writeln(line.toString());

            //Credit du compte des frais
            libelle = "CREDIT COMPTE TOB " + aCheque.getNumerocheque() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet());
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
            line.append(Utility.getParam("CPTCRECOMREJCHQRET2"));   //Compte Client ou attente
            line.append(separateur);
            line.append("T03");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(Utility.getParam("COMCREREJCHQRETFLEX2"));
            line.append(separateur);
            line.append("C");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParam("COMCREREJCHQRETFLEX2"));
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append(numCptExDO != null ? numCptExDO.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptExDO); // // related_account
            writeln(line.toString());

            /**
             * Gestion du Benef du cheque
             */
            /**
             * Prise de comm sur le Benef
             */
            libelle = "DEBIT NOS FRAIS REJET + TOB COMPTE CLIENT :" + aCheque.getNumerocheque() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet());;
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
            line.append(numCptExBenef != null ? numCptExBenef.substring(0, 3) : ""); //agence du compte
            line.append(separateur);
            line.append(numCptExBenef); //Compte Client ou attente
            line.append(separateur);
            line.append("C59");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(Utility.getParamOfType("COMDEBREJCHQALEFLEX", "CODE_COMMISSION"));
            line.append(separateur);
            line.append("D");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParamOfType("COMDEBREJCHQALEFLEX", "CODE_COMMISSION")); //montant 
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append((Utility.getParam("CPTCRECOMREJCHQRET1").length() == 16) ? Utility.getParam("CPTCRECOMREJCHQRET1").substring(6, 14) : "99999999"); //rel_cust
            //(Utility.getParam("CPTCRECOMREJCHQRET1").length() == 16) ? Utility.getParam("CPTCRECOMREJCHQRET1").substring(6, 14) : "99999999"
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(Utility.getParam("CPTCRECOMREJCHQRET1"));// // related_account
            writeln(line.toString());

            //Credit du compte de commission
            libelle = "CREDIT COMPTE NOS FRAIS REJET " + aCheque.getNumerocheque() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet());
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
            line.append(Utility.getParam("CPTCRECOMREJCHQALE1")); //Compte Client ou attente
            line.append(separateur);
            line.append("C59");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(Utility.getParamOfType("COMCREREJCHQALEFLEX1", "CODE_COMMISSION")); //COMCREREJCHQRET
            line.append(separateur);
            line.append("C");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParamOfType("COMCREREJCHQALEFLEX1", "CODE_COMMISSION"));
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append(numCptExBenef != null ? numCptExBenef.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptExBenef);// // related_account
            writeln(line.toString());

            //Credit du compte des frais
            line = new StringBuilder();
            libelle = "CREDIT COMPTE TOB " + aCheque.getNumerocheque() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet());
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
            line.append(Utility.getParam("CPTCRECOMREJCHQALE2"));   //Compte Client ou attente
            line.append(separateur);
            line.append("T03");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(Utility.getParamOfType("COMCREREJCHQALEFLEX2", "CODE_COMMISSION"));
            line.append(separateur);
            line.append("C");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParamOfType("COMCREREJCHQALEFLEX2", "CODE_COMMISSION"));
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append(numCptExBenef != null ? numCptExBenef.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptExBenef);// // related_account
            writeln(line.toString());

        } else {
            /**
             * CHEQUE HOLDED
             */
            //
            //Ligne de Debit du Benef
            libelle = "Rejet Cheque " + aCheque.getNumerocheque() + " Motif: Presented Faveur :" + aCheque.getNombeneficiaire().trim();
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
            line.append(numCptExBenef != null ? numCptExBenef.substring(0, 3) : "");  //agence du compte
            line.append(separateur);
            line.append(numCptExBenef);
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
            //      line.append((Utility.getParam("CPTATTCHQCAIFLEX").length() == 16) ? Utility.getParam("CPTATTCHQCAIFLEX").substring(6, 14) : "99999999"); //rel_cust // usually ==> line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust '0017201000019901' 
            line.append((Utility.getParam("CPTATTCHQCAIFLEX").length() == 16) ? Utility.getParam("CPTATTCHQCAIFLEX").substring(6, 14) : "99999999"); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(Utility.getParam("CPTATTCHQCAIFLEX")); //  // related_account
            writeln(line.toString());

            //Credit Cpt Attente
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
            line.append(numCptExBenef != null ? numCptExBenef.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptExBenef); // related_account
            writeln(line.toString());

        }

        return numLigne;

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
        line.append(current_cycle);//recupere l'annee Fiscale FIN_CYCLE,  
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
        line.append(numCptEx); // 
        writeln(line.toString());

        //Ligne de credit sur le compte Beneficiaire
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
        line.append(numCptEx.substring(0, 3));  //agence du compte
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
        line.append((Utility.getParam("CPTATTCHQCAIFLEX").length() == 16) ? Utility.getParam("CPTATTCHQCAIFLEX").substring(6, 14) : "99999999"); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQCAIFLEX")); // 
        writeln(line.toString());
        return numLigne;

    }

}
