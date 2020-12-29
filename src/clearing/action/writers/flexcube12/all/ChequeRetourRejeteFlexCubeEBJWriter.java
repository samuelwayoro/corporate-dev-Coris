/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube12.all;

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
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeRetourRejeteFlexCubeEBJWriter extends FlatFileWriter {

    public ChequeRetourRejeteFlexCubeEBJWriter() {
        setDescription("Envoi des rejets de chèques Retour vers le SIB");
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
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_RET_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

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

        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ACC") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques retour rejetes - ligne de credit montant sur cpt
                //Ligne 1 (Extourne du montant préalablement débité)
                line = new StringBuffer();
                line.append(CMPUtility.getNumCptExAgence(cheque.getNumerocompte(), cheque.getAgence()));
                line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(), "0"), 16, " "));
                line.append(createBlancs(4, " "));
                line.append("C");
                line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 16, " "));
                line.append("F57");
                line.append(dateValeur);
                line.append(Utility.bourrageGauche(cheque.getNumerocheque(), 8, " "));
                line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX1"), 200, " "));
                line.append("030");
                line.append(Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8));
                line.append(cheque.getMotifrejet());
                /**
                 * ligne F12
                 */
                Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(cheque.getNumerocompte(), cheque.getAgence());
                DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                detbUploadDetail.setBATCH_NO(numeroBatch);
                detbUploadDetail.setFIN_CYCLE(current_cycle);
                detbUploadDetail.setPERIOD_CODE(current_period);
                detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

                detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + cheque.getAgence().substring(3));//agence du compte
                detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + cheque.getNumerocompte());//REM ACCOUNT
                detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                detbUploadDetail.setTXN_CODE("Q17");//Code transaction TXN_CODE
                detbUploadDetail.setAMOUNT(new BigDecimal("-" + cheque.getMontantcheque())); //AMOUNT
                detbUploadDetail.setADDL_TEXT(dateValeur + Utility.bourrageGauche(cheque.getNumerocheque(), 8, " ") + Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8) + " " + Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX1"), 18, " ") + "030" + cheque.getMotifrejet());
                detbUploadDetail.setREL_CUST(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : "");
                detbUploadDetail.setEXTERNAL_REF_NO(cheque.getIdcheque() + detbUploadDetail.getBATCH_NO());
                detbUploadDetail.setRELATED_ACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "");
                detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                detbUploadDetail.setINSTRUMENT_NO(cheque.getNumerocheque());
                detbUploadDetails.add(detbUploadDetail);

                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ACC"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }

                if (cheque.getMotifrejet().equalsIgnoreCase("201") || cheque.getMotifrejet().equalsIgnoreCase("202")) {
                    //Ligne de debit commission
                    String pers = ent(CMPUtility.getAcctClass(cheque.getNumerocompte(), cheque.getAgence()));
                    line = new StringBuffer();
                    line.append(CMPUtility.getNumCptExAgence(cheque.getNumerocompte(), cheque.getAgence()));

                    line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(cheque.getNumerocompte(), cheque.getAgence(), "0"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("D");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMDEBREJCHQRET" + pers + "FLEX", "CODE_COMMISSION"), 16, " "));
                    line.append("F03");
                    line.append(dateValeur);
                    line.append(Utility.bourrageGauche(cheque.getNumerocheque(), 8, " "));
                    line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX1"), 200, " "));
                    line.append("030");
                    line.append(Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8));
                    line.append(cheque.getMotifrejet());
                    /**
                     * ligne F12
                     */
                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

                    detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + cheque.getAgence().substring(3));//agence du compte
                    detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + cheque.getNumerocompte());//REM ACCOUNT
                    detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("Q17");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(Utility.bourrageGauche(Utility.getParamOfType("COMDEBREJCHQRET" + pers + "FLEX", "CODE_COMMISSION").trim(), 16, " ").trim())); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(dateValeur + Utility.bourrageGauche(cheque.getNumerocheque(), 8, " ") + Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 8) + " " + Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX1"), 18, " ") + "030" + cheque.getMotifrejet());
                    detbUploadDetail.setREL_CUST(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : "");
                    detbUploadDetail.setEXTERNAL_REF_NO(cheque.getIdcheque() + detbUploadDetail.getBATCH_NO());
                    detbUploadDetail.setRELATED_ACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetails.add(detbUploadDetail);

                    //Ligne de credit commission
                    line = new StringBuffer();
                    line.append(Utility.getParam("FLEXMAINBRANCH"));
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQRET1"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRET" + pers + "FLEX1", "CODE_COMMISSION"), 16, " "));
                    line.append("F57");
                    line.append(dateValeur);
                    line.append(createBlancs(8, " "));
                    line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX2"), 200, " "));
                    line.append(createBlancs(14, " "));
                    /**
                     * * F12
                     */
                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

                    detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
                    detbUploadDetail.setACCOUNT(Utility.getParam("CPTCRECOMREJCHQRET1"));//REM ACCOUNT
                    detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("K10");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRET" + pers + "FLEX1", "CODE_COMMISSION").trim(), 16, " ").trim())); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(dateValeur + Utility.bourrageDroite(Utility.getParam("LIBREJCHQALEFLEX2"), 25, " "));
                    detbUploadDetail.setREL_CUST(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : "");
                    detbUploadDetail.setEXTERNAL_REF_NO(detbUploadDetail.getBATCH_NO());
                    detbUploadDetail.setRELATED_ACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetails.add(detbUploadDetail);

                    //Ligne de credit com2
                    line = new StringBuffer();
                    line.append(Utility.getParam("FLEXMAINBRANCH"));
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQRET2"), 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("C");
                    line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRET" + pers + "FLEX2", "CODE_COMMISSION"), 16, " "));
                    line.append("F57");
                    line.append(dateValeur);
                    line.append(createBlancs(8, " "));
                    line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX3"), 200, " "));
                    line.append(createBlancs(14, " "));
                    /**
                     * * F12
                     */
                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

                    detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
                    detbUploadDetail.setACCOUNT(Utility.getParam("CPTCRECOMREJCHQRET2"));//REM ACCOUNT
                    detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("K10");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQRET" + pers + "FLEX2", "CODE_COMMISSION").trim(), 16, " ").trim())); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(dateValeur + Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX3"), 25, " "));
                    detbUploadDetail.setREL_CUST(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : "");
                    detbUploadDetail.setEXTERNAL_REF_NO(detbUploadDetail.getBATCH_NO());
                    detbUploadDetail.setRELATED_ACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetails.add(detbUploadDetail);
                }
                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }
// Gestion cpt globalisation
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTDEBREJCHQRET"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, " "));
            line.append("Q11");
            line.append(dateValeur);
            line.append(createBlancs(8, " "));
            line.append(Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX4"), 200, " "));
            line.append(createBlancs(14, " "));
            /**
             * * F12
             */
            DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTDEBREJCHQRET"));//REM ACCOUNT
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("Q11");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(dateValeur + Utility.bourrageDroite(Utility.getParam("LIBREJCHQRETFLEX4"), 25, " "));
            detbUploadDetail.setREL_CUST((Utility.getParam("CPTDEBREJCHQRET").length() == 16) ? Utility.getParam("CPTDEBREJCHQRET").substring(6, 14) : "999999996");
            detbUploadDetail.setEXTERNAL_REF_NO(detbUploadDetail.getBATCH_NO());
            detbUploadDetail.setRELATED_ACCOUNT(Utility.getParam("CPTDEBREJCHQRET"));
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            /**
             * ECRITURE DANS LE FICHIER
             */
            detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
            detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
            writeFile(detbUploadDetailArray);
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
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
