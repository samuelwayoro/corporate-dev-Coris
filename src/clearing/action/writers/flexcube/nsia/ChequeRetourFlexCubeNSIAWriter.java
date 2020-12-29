/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.nsia;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Utilisateurs;
import clearing.table.flexcube.DETB_UPLOAD_DETAIL;
import clearing.table.flexcube.STTM_BRANCH;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.patware.action.file.FlatFileWriter;
import org.patware.bean.table.Fichiers;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeRetourFlexCubeNSIAWriter extends FlatFileWriter {

    public ChequeRetourFlexCubeNSIAWriter() {
        setDescription("Envoi des chèques retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String dateValeur = Utility.getParam("DATEVALEUR_RETOUR");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }

        /**
         * F12
         */
        DETB_UPLOAD_DETAIL[] detbUploadDetailArray;

        List<DETB_UPLOAD_DETAIL> detbUploadDetails = new ArrayList<>();
        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

        String sql = "select  CURRENT_CYCLE,CURRENT_PERIOD from " + Utility.getParam("FLEXSCHEMA") + ".STTM_BRANCH where branch_code='" + Utility.getParam("TXN_BRANCH") + "' ";
        STTM_BRANCH[] sttm_branch = (STTM_BRANCH[]) dbExt.retrieveRowAsObject(sql, new STTM_BRANCH());
        String current_cycle = "";
        String current_period = "";
        if (sttm_branch != null && sttm_branch.length > 0) {
            current_cycle = sttm_branch[0].getCurrent_cycle();
            current_period = sttm_branch[0].getCurrent_period();
        }
        dbExt.close();

        String numeroBatch = "";
        param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }

        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

        String userLogin = ((Utilisateurs) getParametersMap().get("user")).getLogin().trim();
        Fichiers fichier = new Fichiers();
        fichier.setUserUpload(userLogin);
        fichier.setNomFichier(new File(fileName).getName());
        fichier.setDateReception(Utility.convertDateToString(new Date(), "yyyy/MM/dd"));
        fichier.setEtat(new BigDecimal(30));
        fichier.setIdFichier(new BigDecimal(Utility.computeCompteur("IDFICHIERS", "FICHIERS")));
        db.insertObjectAsRowByQuery(fichier, "FICHIERS");
        /**
         * FIN PARAM F12
         */

        //Population
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        long montantTotalEchec = 0;
        long montantTotalMC = 0;

        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];

                Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(cheque.getNumerocompte(), cheque.getAgence());
                if (!isValidLine(cheque)) {

                    montantTotalEchec += Long.parseLong(cheque.getMontantcheque());
                    if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETMAN")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    } else {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECMAN")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }

                } else {
                    //Tous les cheques retours
                    /**
                     * ligne F12
                     */

                    DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

                    detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + cheque.getAgence().substring(3));//agence du compte
                    detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + cheque.getNumerocompte());//REM ACCOUNT
                    detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("ICP");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(cheque.getMontantcheque())); //AMOUNT
                    //detbUploadDetail.setADDL_TEXT(  Utility.bourrageGauche(cheque.getNumerocheque(), 8, " ") + Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8) + " " + Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX1"), 18, " ") + "030" + cheque.getMotifrejet());
                    detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBCHQRETFLEX10") + " " + CMPUtility.getLibelleBanque(cheque.getBanque()) + " No " + Utility.bourrageGauche(cheque.getNumerocheque() , 7, "0")+" CPSE DU "+cheque.getDatecompensation() +" Venant de "+ CMPUtility.getLibelleBanque(cheque.getBanqueremettant()), 200, " "));
                    detbUploadDetail.setREL_CUST("");
                    detbUploadDetail.setEXTERNAL_REF_NO("");
                    detbUploadDetail.setRELATED_ACCOUNT("");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetail.setINSTRUMENT_NO(cheque.getNumerocheque());
                    detbUploadDetails.add(detbUploadDetail);
                    cheque.setLotsib(new BigDecimal(1));

                    montantTotal += Long.parseLong(cheque.getMontantcheque());
                }

            }
            // Gestion cpt globalisation

            /**
             * ligne F12
             */
            DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTBCEAOFLEX"));//REM ACCOUNT 0100401490059989025
            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETBCEAOFLEX") + " " + Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Règlement BCEAO compense retour du 
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            detbUploadDetail = new DETB_UPLOAD_DETAIL();

            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETFLEX"));//REM ACCOUNT 371300000
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX1") + " " + Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Reglement Compens du 11/06/2019  
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETFLEX"));//REM ACCOUNT 371300000
            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ICP");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX1") + " " + Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Reglement Compens du 11/06/2019  
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setINSTRUMENT_NO(Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatecompensation(), "yyyy/MM/dd"), "ddMMyyyy"));
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            /**
             * ligne F12 debut Annulation
             */
            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETFLEX6"));//REM ACCOUNT 961400000
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX3"));//Annulation de la reception des chèques DBB
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            /**
             * ligne F12
             */
            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETFLEX5"));//REM ACCOUNT 961300000
            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX3"));
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            /**
             * ECRITURE DANS LE FICHIER
             */
            detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
            detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
            writeFile(detbUploadDetailArray);

            db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERET"));
            db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERETREC"));
            closeFile();

            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque accepte = " + cheques.length + " - Montant Total accepte = " + Utility.formatNumber("" + montantTotal) + " <br> - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total accepte = " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);

            numeroBatch = "";
            param1 = (String[]) getParametersMap().get("textParam2");
            if (param1 != null && param1.length > 0) {
                numeroBatch = param1[0];
            }
            fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_MC_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

            fichier = new Fichiers();
            fichier.setUserUpload(userLogin);
            fichier.setNomFichier(new File(fileName).getName());
            fichier.setDateReception(Utility.convertDateToString(new Date(), "yyyy/MM/dd"));
            fichier.setEtat(new BigDecimal(30));
            fichier.setIdFichier(new BigDecimal(Utility.computeCompteur("IDFICHIERS", "FICHIERS")));
            db.insertObjectAsRowByQuery(fichier, "FICHIERS");
            detbUploadDetails = new ArrayList<>();
            //Population
            sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECMAN") + "," + Utility.getParam("CETAOPERETMAN") + ") AND NUMEROCOMPTE LIKE '" + Utility.getParam("CPTFLEXMCACCOUNT") + "%' ";
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

            if (cheques != null && 0 < cheques.length) {
                setOut(createFlatFile(fileName));
                for (int i = 0; i < cheques.length; i++) {
                    Cheques cheque = cheques[i];
                    Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(cheque.getNumerocompte(), cheque.getAgence());

                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                    detbUploadDetail.setACCOUNT_BRANCH(cheque.getNumerocompte().substring(9));//agence du compte
                    detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTDEBMCFLEX"));//REM ACCOUNT
                    detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("ICP");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(cheque.getMontantcheque())); //AMOUNT
                    // detbUploadDetail.setADDL_TEXT( Utility.bourrageGauche(cheque.getNumerocheque(), 8, " ") + Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8) + " " + Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX1"), 18, " ") + "030" + cheque.getMotifrejet());
                    //detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBCHQRETFLEX10") + " No" + Utility.bourrageGauche(cheque.getNumerocheque() , 7, "0")+" CPSE DU ("+ cheque.getDatecompensation()+")", 200, " "));
                    detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBCHQRETFLEX10") + " "+(CMPUtility.getLibelleBanque(cheque.getBanque()))+ " No" + Utility.bourrageGauche(cheque.getNumerocheque() , 7, "0")+" CPSE DU ("+ cheque.getDatecompensation()+")", 200, " "));
                    detbUploadDetail.setREL_CUST("");
                    detbUploadDetail.setEXTERNAL_REF_NO("");
                    detbUploadDetail.setRELATED_ACCOUNT("");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetail.setINSTRUMENT_NO(cheque.getNumerocheque());
                    detbUploadDetails.add(detbUploadDetail);

                    montantTotalMC += Long.parseLong(cheque.getMontantcheque());

                    cheque.setLotsib(new BigDecimal(1));

                }

                // Gestion cpt globalisation
                /**
                 * ligne F12
                 */
                detbUploadDetail = new DETB_UPLOAD_DETAIL();
                detbUploadDetail.setBATCH_NO(numeroBatch);
                detbUploadDetail.setFIN_CYCLE(current_cycle);
                detbUploadDetail.setPERIOD_CODE(current_period);
                detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
                detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTBCEAOFLEX"));//REM ACCOUNT 0100401490059989025
                detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalMC)); //AMOUNT
                detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETBCEAOFLEX") + " " + Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Règlement BCEAO compense retour du 
                detbUploadDetail.setREL_CUST("");
                detbUploadDetail.setEXTERNAL_REF_NO("");
                detbUploadDetail.setRELATED_ACCOUNT("");
                detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                detbUploadDetails.add(detbUploadDetail);

                detbUploadDetail = new DETB_UPLOAD_DETAIL();
                detbUploadDetail.setBATCH_NO(numeroBatch);
                detbUploadDetail.setFIN_CYCLE(current_cycle);
                detbUploadDetail.setPERIOD_CODE(current_period);
                detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
                detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETFLEX"));//REM ACCOUNT 371300000
                detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalMC)); //AMOUNT
                detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX1") + " " + Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Reglement Compens du 11/06/2019  
                detbUploadDetail.setREL_CUST("");
                detbUploadDetail.setEXTERNAL_REF_NO("");
                detbUploadDetail.setRELATED_ACCOUNT("");
                detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                detbUploadDetails.add(detbUploadDetail);

                detbUploadDetail = new DETB_UPLOAD_DETAIL();
                detbUploadDetail.setBATCH_NO(numeroBatch);
                detbUploadDetail.setFIN_CYCLE(current_cycle);
                detbUploadDetail.setPERIOD_CODE(current_period);
                detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
                detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETFLEX"));//REM ACCOUNT 371300000
                detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                detbUploadDetail.setTXN_CODE("ICP");//Code transaction TXN_CODE
                detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalMC)); //AMOUNT
                detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX1") + " " + Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Reglement Compens du 11/06/2019  
                detbUploadDetail.setREL_CUST("");
                detbUploadDetail.setEXTERNAL_REF_NO("");
                detbUploadDetail.setRELATED_ACCOUNT("");
                detbUploadDetail.setINSTRUMENT_NO("99999");
                detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                detbUploadDetails.add(detbUploadDetail);

                /**
                 * ligne F12 debut Annulation
                 */
                detbUploadDetail = new DETB_UPLOAD_DETAIL();
                detbUploadDetail.setBATCH_NO(numeroBatch);
                detbUploadDetail.setFIN_CYCLE(current_cycle);
                detbUploadDetail.setPERIOD_CODE(current_period);
                detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
                detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETFLEX6"));//REM ACCOUNT 961400000
                detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalMC)); //AMOUNT
                detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX3"));//Annulation de la reception des chèques DBB
                detbUploadDetail.setREL_CUST("");
                detbUploadDetail.setEXTERNAL_REF_NO("");
                detbUploadDetail.setRELATED_ACCOUNT("");
                detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                detbUploadDetails.add(detbUploadDetail);

                /**
                 * ligne F12
                 */
                detbUploadDetail = new DETB_UPLOAD_DETAIL();
                detbUploadDetail.setBATCH_NO(numeroBatch);
                detbUploadDetail.setFIN_CYCLE(current_cycle);
                detbUploadDetail.setPERIOD_CODE(current_period);
                detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
                detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETFLEX5"));//REM ACCOUNT 961300000
                detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalMC)); //AMOUNT
                detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX3"));
                detbUploadDetail.setREL_CUST("");
                detbUploadDetail.setEXTERNAL_REF_NO("");
                detbUploadDetail.setRELATED_ACCOUNT("");
                detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                detbUploadDetails.add(detbUploadDetail);
                /**
                 * ECRITURE DANS LE FICHIER
                 */
                detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
                detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
                writeFile(detbUploadDetailArray);

                db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERETMAN") + " AND NUMEROCOMPTE LIKE '" + Utility.getParam("CPTFLEXMCACCOUNT") + "%' ");
                db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERETRECMAN") + " AND NUMEROCOMPTE LIKE '" + Utility.getParam("CPTFLEXMCACCOUNT") + "%' ");

                closeFile();
                setDescription(getDescription() + "<br> Nombre de Chèque MC= " + cheques.length + " - Montant Total de MC = " + Utility.formatNumber("" + montantTotalMC) + "<br> - Nom de Fichier MC =  <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "\">" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "</a>");
                logEvent("INFO", "Nombre de Chèque MC = " + cheques.length + " - Montant Total MC = " + Utility.formatNumber("" + montantTotalMC) + " - Nom de Fichier MC = " + fileName);

            }

            fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_ERR_FILE_ROOTNAME") + dateTraitement + Utility.getParam("SIB_FILE_EXTENSION");

            //Population
            sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETRECMAN") + "," + Utility.getParam("CETAOPERETMAN") + ")  ";
            cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

            if (cheques != null && 0 < cheques.length) {
                setOut(createFlatFile(fileName));
                for (int i = 0; i < cheques.length; i++) {
                    Cheques cheque = cheques[i];
                    String line = "";
                    line += cheque.getBanqueremettant() + ";";
                    line += cheque.getBanque() + ";";
                    line += Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ") + ";";
                    line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "yyyyMMdd") + ";";
                    line += Utility.bourrageGauche(cheque.getMontantcheque(), 16, "0") + ";";
                    line += cheque.getAgence() + ";";
                    line += cheque.getNumerocompte() + ";";
                    line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 27, " ") + ";";
                    line += cheque.getRio().substring(27);
                    writeln(line);
                    cheque.setLotsib(new BigDecimal(1));

                }

                db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERETMAN"));
                db.executeUpdate("UPDATE CHEQUES SET LOTSIB=1,ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERETRECMAN"));

                closeFile();
                setDescription(getDescription() + "<br> Nombre de Chèque en echec= " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotalEchec) + "<br> - Nom de Fichier Echec =  <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "\">" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "</a>");
                logEvent("INFO", "Nombre de Chèque en Echec = " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotalEchec) + " - Nom de Fichier Echec = " + fileName);

            }

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

//        //MAJ DES CHEQUES SANS IMAGES AVEC MOTIF REJET 215
//        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM2") + " , MOTIFREJET='215' WHERE ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " AND BANQUEREMETTANT IN (SELECT CODEBANQUE FROM BANQUES WHERE ALGORITHMEDECONTROLESPECIFIQUE=1)";
//        db.executeUpdate(sql);
        db.close();
    }

    private boolean isValidLine(Cheques cheque) throws Exception {
        //Verification de l'existence du compte
        Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(cheque.getNumerocompte(), cheque.getAgence());

        if (compteFlexCube == null) {
            return false;
        }

        //Verification des manager cheques
        if (cheque.getNumerocompte().contains(Utility.getParam("CPTFLEXMCACCOUNT"))) {
            return false;
        }

//        //Verification des comptes staff
//        if (Utility.getParam("ACCLASSERR").contains(CMPUtility.getAcctClass(numCptEx))) {
//            return false;
//        }
        return true;
    }

    private void createEnteteSQL() {
        StringBuilder line = new StringBuilder();

        //entete
        line.append("LOAD DATA\n"
                + "INFILE *\n"
                + "APPEND\n"
                + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + ". DETB_UPLOAD_DETAIL\n"
                + " Fields terminated by '~'\n"
                + " Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\",ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\",LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,TXT_FILE_NAME,INSTRUMENT_NO)\n"
                + "BEGINDATA");
        writeln(line.toString());
    }

    public String getSep() {
        return sep;
    }

    public void setSep(String sep) {
        this.sep = sep;
    }

    private String sep(String value) {
        return value + sep;
    }

    private int writeFile(DETB_UPLOAD_DETAIL[] detbUploadDetailList) {
        System.out.println("writeFile DETB_UPLOAD_DETAIL File");
        int numLigne = 1;
        createEnteteSQL();

        for (DETB_UPLOAD_DETAIL detbUploadDetail : detbUploadDetailList) {
            StringBuilder line = new StringBuilder();
            line.append(sep(detbUploadDetail.getFIN_CYCLE()));
            line.append(sep(detbUploadDetail.getPERIOD_CODE()));
            line.append(sep(Utility.convertDateToString(detbUploadDetail.getVALUE_DATE(), Utility.getParam("DATE_FORMAT"))));
            line.append(sep(detbUploadDetail.getADDL_TEXT()));
            line.append(sep(detbUploadDetail.getBRANCH_CODE()));
            line.append(sep(detbUploadDetail.getSOURCE_CODE()));
            line.append(sep(detbUploadDetail.getACCOUNT_BRANCH()));
            line.append(sep(detbUploadDetail.getACCOUNT()));
            line.append(sep(detbUploadDetail.getTXN_CODE()));
            line.append(sep(detbUploadDetail.getBATCH_NO()));
            line.append(sep(String.valueOf(numLigne++)));
            line.append(sep(detbUploadDetail.getAMOUNT().toString()));
            line.append(sep(detbUploadDetail.getDR_CR()));
            line.append(sep(detbUploadDetail.getUPLOAD_STAT()));
            line.append(sep(detbUploadDetail.getCCY_CD()));
            line.append(sep(Utility.convertDateToString(detbUploadDetail.getINITIATION_DATE(), Utility.getParam("DATE_FORMAT"))));
            line.append(sep(detbUploadDetail.getAMOUNT().toString()));
            line.append(sep(detbUploadDetail.getEXCH_RATE().toString()));
            line.append(sep(detbUploadDetail.getREL_CUST()));
            line.append(sep(detbUploadDetail.getEXTERNAL_REF_NO()));
            line.append(sep(detbUploadDetail.getRELATED_ACCOUNT()));
            line.append(sep(detbUploadDetail.getTXT_FILE_NAME()));
            line.append(sep(detbUploadDetail.getINSTRUMENT_NO()));
            writeln(line.toString());

        }

        return numLigne;
    }
    private String sep = "~";

}
