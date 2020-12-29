/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.nsia;

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
public class ChequeAllerFlexCubeSQLWriter extends FlatFileWriter {

    public ChequeAllerFlexCubeSQLWriter() {

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
        String dateCompensation = "("
                + "	CASE WHEN SUBSTR(trim(BANQUE),0,2)=SUBSTR(trim(BANQUEREMETTANT),0,2) THEN "
                + "			( SELECT MAX(DATECOMPENSATION) FROM CHEQUES WHERE SUBSTR(trim(BANQUE),0,2)=SUBSTR(trim(BANQUEREMETTANT),0,2) AND DATECOMPENSATION "
                + "                 <(SELECT MAX(DATECOMPENSATION) FROM CHEQUES   where SUBSTR(trim(BANQUE),0,2)=SUBSTR(trim(BANQUEREMETTANT),0,2) ) )"
                + "		 WHEN SUBSTR(trim(BANQUE),0,2)<>SUBSTR(trim(BANQUEREMETTANT),0,2) THEN "
                + "			( SELECT MAX(DATECOMPENSATION) FROM CHEQUES WHERE SUBSTR(trim(BANQUE),0,2)<>SUBSTR(trim(BANQUEREMETTANT),0,2)  AND DATECOMPENSATION "
                + "                 < (SELECT MAX(DATECOMPENSATION) FROM CHEQUES where SUBSTR(trim(BANQUE),0,2)<>SUBSTR(trim(BANQUEREMETTANT),0,2)) )      "
                + "				"
                + "			   END )";

        //  String dateCompensation = "(SELECT MAX(DATECOMPENSATION) FROM CHEQUES WHERE DATECOMPENSATION < (SELECT MAX(DATECOMPENSATION) FROM CHEQUES))";
        System.out.println("Date Compensation = " + dateCompensation);

        String dateValeurJ2 = "";
        param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeurJ2 = param1[0];
        }
        System.out.println("Date Valeur J2 = " + dateValeurJ2 + " yyyyMMdd ");
        dateValeurJ2 = Utility.convertDateToString(Utility.convertStringToDate(dateValeurJ2, "yyyyMMdd"), "ddMMyyyy");
        System.out.println("Date Valeur J2 Formated = " + dateValeurJ2);

        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
//        String sql = "select to_char(next_working_day,'YYYY/MM/DD') as next_working_day from boesn.STTM_AEOD_DATES where branch_code ='001'";
//        STTM_AEOD_DATES[] sttm_aeod_dates = (STTM_AEOD_DATES[]) dbExt.retrieveRowAsObject(sql, new STTM_AEOD_DATES());
//        String dateValeurJ2 = "";
//        if (sttm_aeod_dates != null && sttm_aeod_dates.length > 0) {
//            dateValeurJ2 = sttm_aeod_dates[0].getNext_working_day();
//        }
        String sql = "select  CURRENT_CYCLE,CURRENT_PERIOD from " + Utility.getParam("FLEXSCHEMA") + ".STTM_BRANCH where branch_code='001' ";
        STTM_BRANCH[] sttm_branch = (STTM_BRANCH[]) dbExt.retrieveRowAsObject(sql, new STTM_BRANCH());
        String current_cycle = "";
        String current_period = "";
        if (sttm_branch != null && sttm_branch.length > 0) {
            current_cycle = sttm_branch[0].getCurrent_cycle();
            current_period = sttm_branch[0].getCurrent_period();
        }
        dbExt.close();

        long montantTotalRejete = 0;
        long montantTotalEchec = 0;
        int numLigne = 1;

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");
        setOut(createFlatFile(fileName));
        createEnteteSQL();
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                if (!isValidLine(cheque)) {
                } else {

                    numLigne = createLinesRejetSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + cheque.getIdcheque(),
                            cheque.getMontantcheque(), Utility.convertDateToString(new java.util.Date(System.currentTimeMillis()), "ddMMyyyy"), cheque, "");
                    //  createLinesRejetFlex(cheque);
                    if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))) {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }
                    montantTotalRejete += Long.parseLong(cheque.getMontantcheque());
                }

            }
            setDescription("Envoi des rejets de chèques Aller vers le SIB");
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque rejete= " + cheques.length + " - Montant Total rejete= " + Utility.formatNumber("" + montantTotalRejete) + " - Nom de Fichier = " + fileName + "<br>");
            logEvent("INFO", "Nombre de Chèque rejete = " + cheques.length + " - Montant Total rejete = " + Utility.formatNumber("" + montantTotalRejete));
        }

        // Population des cheques en credit immediat
        sql = "SELECT * FROM CHEQUES WHERE "
                + " ETAT IN  (" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") "
                + " AND ESCOMPTE=1 "
                + "AND  DATECOMPENSATION=" + dateCompensation + " ORDER BY REMISE";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        montantTotalEchec = 0;

        if (cheques != null && 0 < cheques.length) {
            for (int i = 0; i < cheques.length; i += j) {

                {
                    //Tous les cheques validés

                    //Tous les cheques compensables validés d'une remise
                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND "
                            + " ETAT IN  (" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") "
                            + " AND ESCOMPTE=1 "
                            + " AND DATECOMPENSATION=" + dateCompensation + " ORDER BY REMISE";
                    Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                    if (remises != null && remises.length > 0) {

                        if (chequesVal != null && 0 < chequesVal.length) {
                            j = chequesVal.length;

                            long sumRemise = 0;

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

                                dateValeur = dateValeurJ2;

                                numLigne = createLinesCRISQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getRemise(), "" + sumRemise,
                                        dateValeur, libelle);

                            } else if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("C"))) {

                                libelle = "VRS CH" + aCheque.getNumerocheque() + "_" + remises[0].getReference();
                                //Credit cheque par cheque sur le compte du gros remettant

                                dateValeur = dateValeurJ2;
                                for (int x = 0; x < chequesVal.length; x++) {

                                    aCheque = chequesVal[x];
//                                   
                                    numLigne = createLinesCRISQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getIdcheque(),
                                            aCheque.getMontantcheque(), dateValeur, libelle);

                                }

                            }

                        }
                    }
                }

            }

            setDescription(getDescription() + "<br>Liquidation des credits immédiats sur les comptes internes:");
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        }
// Population
        sql = "SELECT * FROM CHEQUES WHERE "
                + " ETAT IN  (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") "
                + "  AND ESCOMPTE<>1 "
                + " AND DATECOMPENSATION=" + dateCompensation + " ORDER BY REMISE";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        j = 0;
        montantTotal = 0;
        montantTotalEchec = 0;

        if (cheques != null && 0 < cheques.length) {
            for (int i = 0; i < cheques.length; i += j) {

                {
                    //Tous les cheques validés

                    //Tous les cheques compensables validés d'une remise
                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND "
                            + " ETAT IN  (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") "
                            + " AND ESCOMPTE<> 1"
                            + " AND DATECOMPENSATION=" + dateCompensation + " ORDER BY REMISE";
                    Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                    if (remises != null && remises.length > 0) {

                        if (chequesVal != null && 0 < chequesVal.length) {
                            j = chequesVal.length;
                            if (!isValidLine(cheques[i])) {

                            } else {

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

                                    dateValeur = dateValeurJ2;
                                    //Credit du bordereau remise sur le compte du gros remettant
                                    //  createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);
                                    numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getRemise(), "" + sumRemise,
                                            dateValeur, aCheque, libelle, montantFrais);

                                } else if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("C"))) {

                                    //Credit cheque par cheque sur le compte du gros remettant
                                    dateValeur = dateValeurJ2;
                                    for (int x = 0; x < chequesVal.length; x++) {

                                        montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                                        aCheque = chequesVal[x];
                                        libelle = "VERSEMENT CHEQUE" + Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " ") + "_" + aCheque.getRefremise(); //Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ");

//                                    createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                                        numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + aCheque.getIdcheque(),
                                                aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);

                                    }

                                }

                                db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                                db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + " WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") AND  REMISE=" + aCheque.getRemise());
                            }
                        }
                    }
                }

            }

            setDescription(getDescription() + "<br>Envoi des chèques Aller vers le SIB");
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription(getDescription() + "<br>Envoi des chèques Aller vers le SIB");
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Creation du fichier d'Echec
        //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") ";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        dateTraitement = Utility.convertDateToString(new Date(), "ddMMyyyy");
        fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_ERR_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_EXTENSION");

        setOut(createFlatFile(fileName));
        if (cheques != null && 0 < cheques.length) {
            System.out.println(" ECHEC rejets");

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];

//                createLinesRejetFlex(cheque);
                createLinesRejetSQL(current_cycle, current_period, numeroBatch, i + 1, ";", numeroBatch + "" + cheque.getIdcheque(),
                        cheque.getMontantcheque(), Utility.convertDateToString(new Date(), "ddMMyyyy"), cheque, "");
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                montantTotalRejete += Long.parseLong(cheque.getMontantcheque());

            }
            setDescription(getDescription() + "<br>Echec des rejets de chèques Aller");
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque rejete en echec = " + cheques.length + " - Montant Total rejete en echec = " + Utility.formatNumber("" + montantTotalRejete) + " - Nom de Fichier Rejet Echec =  <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "\">" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "</a>" + "<br>");
            logEvent("INFO", "Nombre de Chèque rejete en echec = " + cheques.length + " - Montant Total rejete en echec = " + Utility.formatNumber("" + montantTotalRejete));
        }
// Population
        sql = "SELECT * FROM CHEQUES WHERE "
                + " ETAT IN  (" + Utility.getParam("CETAOPEALLICOM1ACC") + " ) "
                + " AND DATECOMPENSATION=" + dateCompensation + " ORDER BY REMISE";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        j = 0;
        montantTotal = 0;
        montantTotalEchec = 0;

        if (cheques != null && 0 < cheques.length) {
            System.out.println(" ECHEC CHEQUES CETAOPEALLICOM1ACC");
            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques compensables validés d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND "
                        + " ETAT IN  (" + Utility.getParam("CETAOPEALLICOM1ACC") + " ) "
                        + "  AND DATECOMPENSATION=" + dateCompensation + " ORDER BY REMISE";
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

                        libelle = "VRS CH" + aCheque.getNumerocheque() + "_" + aCheque.getRefremise();
                        //Credit cheque par cheque sur le compte du gros remettant

                        dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeurJ2, "yyyyMMdd"), "ddMMyyyy");
                        for (int x = 0; x < chequesVal.length; x++) {

                            montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                            aCheque = chequesVal[x];
                            //     createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                            numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, ";", numeroBatch + "" + aCheque.getIdcheque(),
                                    aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);

                        }

                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + " WHERE REMISE=" + aCheque.getRemise() + " AND ETAT=" + Utility.getParam("CETAOPEALLICOM1ACC"));
                    }
                }

            }

            setDescription(getDescription() + "<br>Fichier Echec des chèques Aller vers le SIB");
            setDescription(getDescription() + " :<br> Nombre de Chèque en echec = " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier  Echec =  <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "\">" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "</a>");
            logEvent("INFO", "Nombre de Chèque Aller en echec= " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotal));

        }
        closeFile();
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
    private int createLinesRejetSQL(String current_cycle, String current_period, String numeroBatch, int numLigne, String separateur, String referenceRelative,
            String montant, String dateValeur, Cheques aCheque, String libelle) throws Exception {
        StringBuilder line;
        String numCptEx = CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"1");
        if (numCptEx == null) {
            numCptEx = aCheque.getAgenceremettant().substring(2) + "0" + aCheque.getCompteremettant();
        }
        /**
         * Format SQL LOADER
         */
        //Ligne de debit du client
        //Ligne de debit
        line = new StringBuilder();
        line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
        line.append(separateur);
        line.append(current_period); //PERIOD_CODE a recuperer par une requete
        line.append(separateur);
        line.append(Utility.convertDateToString(Utility.convertStringToDate(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT")), "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(Utility.bourrageDroite("Rejet Cheque N°:" + aCheque.getNumerocheque() + " Remise " + aCheque.getRefremise() + " Pour Motif:" + Utility.getParamLabel(aCheque.getMotifrejet()), 100, " ").trim());
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append(numCptEx.substring(0, 3));
        line.append(separateur);
        line.append(numCptEx);
        line.append(separateur);
        line.append("F03");
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montant);
        line.append(separateur);
        line.append("D");
        line.append(separateur);
        line.append("U");
        line.append(separateur);
        line.append("XOF");
        line.append(separateur);
        line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
        line.append(separateur);
        line.append(montant);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append((Utility.getParam("CPTGLOCOMREJCHQALE").length() == 16) ? Utility.getParam("CPTGLOCOMREJCHQALE").substring(6, 14) : "99999999"); //rel_cust
        //line.append((Utility.getParam("CPTCOMCHQALEFLEX").length() == 16) ? Utility.getParam("CPTCOMCHQALEFLEX").substring(6, 14) : "99999999"); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTGLOCOMREJCHQALE"));// 
        writeln(line.toString());

        //Ligne de credit sur le compte dattente
        line = new StringBuilder();
        line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
        line.append(separateur);
        line.append(current_period); //PERIOD_CODE a recuperer par une requete
        line.append(separateur);
        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(Utility.bourrageDroite("Rejet Cheque N°:" + aCheque.getNumerocheque() + " Remise " + aCheque.getRefremise() + " Pour Motif:" + Utility.getParamLabel(aCheque.getMotifrejet()), 100, " ").trim());
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH"));  //agence du compte
        line.append(separateur);
        line.append(Utility.getParam("CPTGLOCOMREJCHQALE"));
        line.append(separateur);
        line.append("F57");
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montant);
        line.append(separateur);
        line.append("C");
        line.append(separateur);
        line.append("U");
        line.append(separateur);
        line.append("XOF");
        line.append(separateur);
        line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
        line.append(separateur);
        line.append(montant);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(numCptEx);// 
        writeln(line.toString());

        //Prise de commissions sur les motifs Rejet 201 & 202
        if (aCheque.getMotifrejet().equalsIgnoreCase("201") || aCheque.getMotifrejet().equalsIgnoreCase("202")) {

            //Debit Client du montant total des frais
            libelle = "Nos Frais De Rejet Cheque: " + aCheque.getNumerocheque(); //Utility.bourrageDroite("Frais Rej" + cheque.getNumerocheque() + ":" + cheque.getRemise(), 25, " ")
            line = new StringBuilder();
            line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
            line.append(separateur);
            line.append(current_period); //PERIOD_CODE a recuperer par une requete
            line.append(separateur);
            line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
            line.append(separateur);
            line.append(Utility.bourrageDroite("Frais Rejet Cheque: " + aCheque.getNumerocheque() + " Remise :" + aCheque.getRefremise(), 100, " ").trim());
            line.append(separateur);
            line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
            line.append(separateur);
            line.append("ECOSOURCE");
            line.append(separateur);
            line.append(numCptEx.substring(0, 3)); //agence du compte
            line.append(separateur);
            line.append(numCptEx); //Compte Client ou attente
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
            line.append((Utility.getParam("CPTGLOCOMREJCHQALE").length() == 16) ? Utility.getParam("CPTGLOCOMREJCHQALE").substring(6, 14) : "99999999"); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(Utility.getParam("CPTGLOCOMREJCHQALE"));//  //(Utility.getParam("CPTGLOCOMREJCHQALE").length() == 16) ? Utility.getParam("CPTGLOCOMREJCHQALE").substring(6, 14) : "99999999"
            writeln(line.toString());

            //Credit du compte de commission
            line = new StringBuilder();
            line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
            line.append(separateur);
            line.append(current_period); //PERIOD_CODE a recuperer par une requete
            line.append(separateur);
            line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
            line.append(separateur);
            line.append(Utility.bourrageDroite("Commission sur Rejet Cheque :" + aCheque.getNumerocheque() + " Remise :" + aCheque.getRefremise(), 100, " ").trim());
            line.append(separateur);
            line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
            line.append(separateur);
            line.append("ECOSOURCE");
            line.append(separateur);
            line.append(Utility.getParam("TXN_BRANCH"));
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
            line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptEx);// 
            writeln(line.toString());

            //Credit du compte des frais
            line = new StringBuilder();
            line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
            line.append(separateur);
            line.append(current_period); //PERIOD_CODE a recuperer par une requete
            line.append(separateur);
            line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
            line.append(separateur);
            line.append(Utility.bourrageDroite("TOB sur Rejet Cheque :" + aCheque.getNumerocheque() + " Remise :" + aCheque.getRefremise(), 100, " ").trim());
            line.append(separateur);
            line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
            line.append(separateur);
            line.append("ECOSOURCE");
            line.append(separateur);
            line.append(Utility.getParam("TXN_BRANCH"));
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
            line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptEx);// //Related Account
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
            String montantLigne, String dateValeur, Cheques aCheque, String libelle, long montantFrais) throws Exception {

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
        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH"));
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALEFLEX"));
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
        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
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
        line.append((Utility.getParam("CPTATTCHQALEFLEX").length() == 16) ? Utility.getParam("CPTATTCHQALEFLEX").substring(6, 14) : "99999999"); //rel_cust
        //  line.append((Utility.getParam("CPTCOMCHQALEFLEX").length() == 16) ? Utility.getParam("CPTCOMCHQALEFLEX").substring(6, 14) : "99999999"); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALEFLEX")); // //Related Account
        writeln(line.toString());

        libelle = "Frais sica Remise N°:" + aCheque.getRefremise();
        //Ligne de debit des frais sica sur le compte client
        line = new StringBuilder();
        line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
        line.append(separateur);
        line.append(current_period); //PERIOD_CODE a recuperer par une requete
        line.append(separateur);
        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append(numCptEx.substring(0, 3));  //agene du compte
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
        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH"));
        line.append(separateur);
        line.append(Utility.getParam("CPTCOMCHQALEFLEX"));
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
    private int createLinesCRISQL(String current_cycle, String current_period, String numeroBatch, int numLigne, String separateur, String referenceRelative,
            String montantLigne, String dateValeur, String libelle) throws Exception {

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
        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH"));
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALEFLEX"));
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
        line.append("99999999"); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALECRIFLEX")); // //Related Account
        writeln(line.toString());

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
        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "ddMMyyyy"), Utility.getParam("DATE_FORMAT"))); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH"));
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALECRIFLEX"));
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
        line.append("99999999"); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALEFLEX")); // //Related Account
        writeln(line.toString());
        return numLigne;

    }

}
