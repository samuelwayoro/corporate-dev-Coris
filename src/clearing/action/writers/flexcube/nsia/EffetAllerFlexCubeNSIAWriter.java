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
public class EffetAllerFlexCubeNSIAWriter extends FlatFileWriter {

    public EffetAllerFlexCubeNSIAWriter() {
        setDescription("Envoi des Effets vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;

        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);

        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);

        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");

        String dateValeur = "";
        param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur J2 = " + dateValeur + " yyyyMMdd ");

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteur;

//        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
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

//     String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_IN_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

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

        int j = 0;
        long montantTotal = 0;
        long montantTotalEchec = 0;

        // Population
        sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") "
                + "AND DATECOMPENSATION='" + dateCompensation+"'";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

        j = 0;
        montantTotal = 0;

        if (effets != null && 0 < effets.length) {
            setOut(createFlatFile(fileName));

            for (Effets effet : effets) {
                //Tous les effets validés
                montantTotal += Long.parseLong(effet.getMontant_Effet());
                /**
                 * ligne F12
                 */
                Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(effet.getNumerocompte_Beneficiaire(), effet.getAgenceremettant());
                DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                detbUploadDetail.setBATCH_NO(numeroBatch);
                detbUploadDetail.setFIN_CYCLE(current_cycle);
                detbUploadDetail.setPERIOD_CODE(current_period);
                detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + effet.getAgenceremettant().substring(3));//agence du compte  cc
                detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Beneficiaire());//REM ACCOUNT
                detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                detbUploadDetail.setAMOUNT(new BigDecimal(effet.getMontant_Effet())); //AMOUNT
                //detbUploadDetail.setADDL_TEXT( Utility.bourrageDroite(nomEmetteur.trim() + " " + Utility.getParam("LIBCHQALEFLEX1") + " " + CMPUtility.getLibelleBanque(aCheque.getBanqueremettant()) + " N° " + Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"), 100, " "));
                detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBEFFALLFLEX10") + " " + effet.getNom_Tire() , 100, " "));
                detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                detbUploadDetail.setINSTRUMENT_NO("");
                detbUploadDetail.setREL_CUST("");
                detbUploadDetail.setEXTERNAL_REF_NO("");
                detbUploadDetail.setRELATED_ACCOUNT("");
                detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                detbUploadDetails.add(detbUploadDetail);
                if (effet.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACC")))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                        db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }
               
            } /*} else {
            db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
            }*/
        

        /**
         * ligne F12
         */
        DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
        detbUploadDetail.setBATCH_NO(numeroBatch);
        detbUploadDetail.setFIN_CYCLE(current_cycle);
        detbUploadDetail.setPERIOD_CODE(current_period);
        detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
        detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
        detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX"));//REM ACCOUNT 371300000
        detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
        detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
        detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
        detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX1"));//Paiement client
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
        detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX"));//REM ACCOUNT 371300000
        detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
        detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
        detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
        detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX2"));//Regularisation pour paiements clients
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
        detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTBCEAOFLEX"));//REM ACCOUNT 0100401490059989025
        detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
        detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
        detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
        detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEBCEAOFLEX") + " " + Utility.convertDateToString(Utility.convertStringToDate(effets[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Règlement BCEAO compense aller du 05 06 2019
        detbUploadDetail.setREL_CUST("");
        detbUploadDetail.setEXTERNAL_REF_NO("");
        detbUploadDetail.setRELATED_ACCOUNT("");
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
        detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX6"));//REM ACCOUNT 961200000
        detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
        detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
        detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
        detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX3"));//Annulation de la réception
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
        detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX5"));//REM ACCOUNT 961100000
        detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
        detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
        detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX3"));
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
        detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX3"));//REM ACCOUNT 373100000
        detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
        detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
        detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX4"));//Annulation envoi en recouvrement
        detbUploadDetail.setREL_CUST("");
        detbUploadDetail.setEXTERNAL_REF_NO("");
        detbUploadDetail.setRELATED_ACCOUNT("");
        detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
        detbUploadDetails.add(detbUploadDetail);

        /**
         * ligne F12 Fin annulation
         */
        detbUploadDetail = new DETB_UPLOAD_DETAIL();
        detbUploadDetail.setBATCH_NO(numeroBatch);
        detbUploadDetail.setFIN_CYCLE(current_cycle);
        detbUploadDetail.setPERIOD_CODE(current_period);
        detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
        detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
        detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
        detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX4"));//REM ACCOUNT 372100000
        detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
        detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
        detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX4"));
        detbUploadDetail.setREL_CUST("");
        detbUploadDetail.setEXTERNAL_REF_NO("");
        detbUploadDetail.setRELATED_ACCOUNT("");
        detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
        detbUploadDetails.add(detbUploadDetail);

        detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
        detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
        writeFile(detbUploadDetailArray);

        setDescription(getDescription() + " exécuté avec succès:\n Nombre d'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
        logEvent("INFO", "Nombre d'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
        closeFile();
    } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
        logEvent("WARNING", "Il n'y a aucun element disponible");
    }

    db.close ();
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
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\"  ,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,TXT_FILE_NAME,INSTRUMENT_NO)\n"
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
            line.append(sep(Utility.convertDateToString(detbUploadDetail.getINITIATION_DATE(), "ddMMyyyy")));
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

   

    private boolean isValidLine(Cheques cheque) throws Exception {
        //Verification de l'existence du compte
        Comptes numCptEx = CMPUtility.getCompteESNFlexCube(cheque.getCompteremettant(), cheque.getAgenceremettant());
        if (numCptEx == null) {
            return false;
        }
        //Verification du compte de scan

        return !cheque.getCompteremettant().equals(Utility.getParam("CPTATTSCANCHQ"));
    }
    private String sep = "~";

}
