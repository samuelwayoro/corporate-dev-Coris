/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.nsia;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Effets;
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
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class EffetRetourRejeteFlexCubeNSIAWriter extends FlatFileWriter {

    public EffetRetourRejeteFlexCubeNSIAWriter() {
        setDescription("Envoi des rejets de ch�ques Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;

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
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_REJ_RET_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

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

        sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ACC") + ") ";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        int j = 0;
        long montantTotal = 0;
        long montantTotalSP = 0;
        long montantTotalAutres = 0;

        if (effets != null && 0 < effets.length) {
            setOut(createFlatFile(fileName));

            for (int i = 0; i < effets.length; i++) {
                Effets effet = effets[i];
                //Tous les cheques retour rejetes - ligne de credit montant sur cpt
                //Ligne 1 (Extourne du montant pr�alablement d�bit�)

                /**
                 * ligne F12
                 */
                Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(effet.getNumerocompte_Tire(), effet.getAgence());

                if (effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ACC"))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }

                if (effet.getMotifrejet().equalsIgnoreCase("201") || effet.getMotifrejet().equalsIgnoreCase("202")) {

                    DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
//Ligne de debit du compte
                    if (Utility.getParam("NSIA_STANDARD") != null && Utility.getParam("NSIA_STANDARD").equalsIgnoreCase("1")) {

                    } else {
                        detbUploadDetail.setBATCH_NO(numeroBatch);
                        detbUploadDetail.setFIN_CYCLE(current_cycle);
                        detbUploadDetail.setPERIOD_CODE(current_period);
                        detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                        detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + effet.getAgence().substring(3));//agence du compte
                        detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Tire());//REM ACCOUNT
                        detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                        detbUploadDetail.setTXN_CODE("ICP");//Code transaction TXN_CODE
                        detbUploadDetail.setAMOUNT(new BigDecimal(effet.getMontant_Effet())); //AMOUNT
                        detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBEFFRETFLEX10") + " Benef: " + effet.getNom_Beneficiaire() + " CPSE DU " + effet.getDatecompensation());
                        detbUploadDetail.setREL_CUST("");
                        detbUploadDetail.setEXTERNAL_REF_NO("");
                        detbUploadDetail.setRELATED_ACCOUNT("");
                        detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                        detbUploadDetail.setINSTRUMENT_NO("99999");
                        detbUploadDetails.add(detbUploadDetail);
                    }

                    //Ligne de credit du compte                  
                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                    detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + effet.getAgence().substring(3));//agence du compte
                    detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Tire());//REM ACCOUNT
                    detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(effet.getMontant_Effet())); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBREJEFFRETREJFLEX1") + Utility.bourrageGauche(effet.getNumeroeffet(), 8, " ") + Utility.getParam("LIBREJEFFRETREJFLEX2") + " " + effet.getNom_Beneficiaire().trim() + " POUR " + Utility.getParamLabel(effet.getMotifrejet()), 120, " "));
                    detbUploadDetail.setREL_CUST("");
                    detbUploadDetail.setEXTERNAL_REF_NO("");
                    detbUploadDetail.setRELATED_ACCOUNT("");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetail.setINSTRUMENT_NO("");
                    detbUploadDetails.add(detbUploadDetail);

//Ligne de debit commission
                    String pers = ent(CMPUtility.getAcctClass(effet.getNumerocompte_Tire(), effet.getAgence()));

                    /**
                     * ligne F12
                     */
                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                    detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + effet.getAgence().substring(3));//agence du compte
                    detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Tire());//REM ACCOUNT
                    detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(Utility.bourrageGauche(Utility.getParamOfType("COMDEBREJEFFRET" + pers + "FLEX", "CODE_COMMISSION").trim(), 16, " ").trim())); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBEFFRETFLEX10") + "  " + effet.getNom_Beneficiaire() + " CPSE DU " + effet.getDatecompensation());
                    detbUploadDetail.setREL_CUST("");
                    detbUploadDetail.setEXTERNAL_REF_NO("");
                    detbUploadDetail.setRELATED_ACCOUNT("");
                    detbUploadDetail.setINSTRUMENT_NO("");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetails.add(detbUploadDetail);

                    //Ligne de credit commission
                    /**
                     * * F12
                     */
                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                    detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + effet.getAgence().substring(3));//agence du compte
                    detbUploadDetail.setACCOUNT(Utility.getParam("CPTCRECOMREJEFFRET1"));//REM ACCOUNT
                    detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJEFFRET" + pers + "FLEX1", "CODE_COMMISSION").trim(), 16, " ").trim())); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBREJEFFRETFLEX2"), 25, " "));
                    detbUploadDetail.setREL_CUST(compteFlexCube != null ? compteFlexCube.getVille().trim() : "9999999");
                    detbUploadDetail.setEXTERNAL_REF_NO("");
                    detbUploadDetail.setRELATED_ACCOUNT("");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetails.add(detbUploadDetail);

                    //Ligne de credit com2
                    /**
                     * * F12
                     */
                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                    detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + effet.getAgence().substring(3));//agence du compte
                    detbUploadDetail.setACCOUNT(Utility.getParam("CPTCRECOMREJEFFRET2"));//REM ACCOUNT
                    detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJEFFRET" + pers + "FLEX2", "CODE_COMMISSION").trim(), 16, " ").trim())); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBREJEFFRETFLEX3"), 25, " "));
                    detbUploadDetail.setREL_CUST("");
                    detbUploadDetail.setEXTERNAL_REF_NO("");
                    detbUploadDetail.setRELATED_ACCOUNT("");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetails.add(detbUploadDetail);
                    montantTotalSP += Long.parseLong(effet.getMontant_Effet());
                } else {
                    if (Utility.getParam("NSIA_STANDARD") != null && Utility.getParam("NSIA_STANDARD").equalsIgnoreCase("1")) {
                        //Ligne de credit du compte                  
                        DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                        detbUploadDetail.setBATCH_NO(numeroBatch);
                        detbUploadDetail.setFIN_CYCLE(current_cycle);
                        detbUploadDetail.setPERIOD_CODE(current_period);
                        detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                        detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + effet.getAgence().substring(3));//agence du compte
                        detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Tire());//REM ACCOUNT
                        detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                        detbUploadDetail.setAMOUNT(new BigDecimal(effet.getMontant_Effet())); //AMOUNT
                        detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBREJEFFRETREJFLEX1") + Utility.bourrageGauche(effet.getNumerocompte_Beneficiaire(), 8, " ") + Utility.getParam("LIBREJEFFRETREJFLEX2") + " " + effet.getNom_Beneficiaire().trim() + " POUR " + Utility.getParamLabel(effet.getMotifrejet()), 120, " "));
                        detbUploadDetail.setREL_CUST("");
                        detbUploadDetail.setEXTERNAL_REF_NO("");
                        detbUploadDetail.setRELATED_ACCOUNT("");
                        detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                        detbUploadDetail.setINSTRUMENT_NO("");
                        detbUploadDetails.add(detbUploadDetail);
                    }
                    montantTotalAutres += Long.parseLong(effet.getMontant_Effet());
                }
                montantTotal += Long.parseLong(effet.getMontant_Effet());
            }
// Gestion cpt globalisation

            /**
             * * F12
             */
            DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();

            if (Utility.getParam("NSIA_STANDARD") != null && Utility.getParam("NSIA_STANDARD").equalsIgnoreCase("1")) {
                detbUploadDetail = new DETB_UPLOAD_DETAIL();
                detbUploadDetail.setBATCH_NO(numeroBatch);
                detbUploadDetail.setFIN_CYCLE(current_cycle);
                detbUploadDetail.setPERIOD_CODE(current_period);
                detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
                detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTBCEAOFLEX"));//REM ACCOUNT 0100401490059989025
                detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
                detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQREJRETBCEAOFLEX") + " " + Utility.convertDateToString(Utility.convertStringToDate(effets[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//R�glement BCEAO compense retour du 
                detbUploadDetail.setREL_CUST("");
                detbUploadDetail.setEXTERNAL_REF_NO("");
                detbUploadDetail.setRELATED_ACCOUNT("");
                detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                detbUploadDetails.add(detbUploadDetail);
            }

            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETREJFLEX"));//REM ACCOUNT 371300000
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalSP)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX1") + " " + Utility.convertDateToString(Utility.convertStringToDate(effets[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Reglement Compens du 11/06/2019  
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setINSTRUMENT_NO("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETREJFLEX"));//REM ACCOUNT 371300000
            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ICP");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalSP)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX1") + " " + Utility.convertDateToString(Utility.convertStringToDate(effets[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Reglement Compens du 11/06/2019  
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
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalSP)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX3"));//Annulation de la reception des ch�ques DBB
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setINSTRUMENT_NO("");
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
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalSP)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX3"));
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setINSTRUMENT_NO("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            //Gestion des ch�ques rejetes pour autres motifs
            detbUploadDetail = new DETB_UPLOAD_DETAIL();

            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETREJFLEX"));//REM ACCOUNT 371300000
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalAutres)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX1") + " " + Utility.convertDateToString(Utility.convertStringToDate(effets[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Reglement Compens du 11/06/2019  
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setINSTRUMENT_NO("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQRETREJFLEX"));//REM ACCOUNT 371300000
            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalAutres)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX1") + " " + Utility.convertDateToString(Utility.convertStringToDate(effets[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Reglement Compens du 11/06/2019  
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setINSTRUMENT_NO("");
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
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalAutres)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX3"));//Annulation de la reception des ch�ques DBB
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setINSTRUMENT_NO("");
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
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalAutres)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQRETFLEX3"));
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setINSTRUMENT_NO("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            /**
             * ECRITURE DANS LE FICHIER
             */
            detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
            detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
            writeFile(detbUploadDetailArray);
            setDescription(getDescription() + " ex�cut� avec succ�s:\n Nombre d'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre d'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }

    private String ent(String acctClass) {
        if (Utility.getParam(acctClass) != null) {
            return Utility.getParam(acctClass).equalsIgnoreCase("PERSPHYS") ? "" : "ENT";
        } else {
            return "";
        }
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
