/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube12.all;

import clearing.model.CMPUtility;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Remises;
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
public class ChequeAllerNonCompFlexCubeWriter extends FlatFileWriter {

    public ChequeAllerNonCompFlexCubeWriter() {
        setDescription("Envoi des ch�ques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        String dateValeurDebit = Utility.getParam("DATEVALEUR_ALLER");
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateValeurDebit = param1[0];
        }
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

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
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

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

        setOut(createFlatFile(fileName));

// Population
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        Cheques[] chequesVal = null;
        Remises[] remises = null;
        Cheques aCheque = null;

        if (cheques != null && 0 < cheques.length) {
            StringBuffer line = new StringBuffer("HUAP");

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques valid�s

                //Tous les cheques non compensables valid�s d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT =" + Utility.getParam("CETAOPESUPVALSURCAI");
                chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                //La remise en question
                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                //Tous les cheques de la remise (compensables et non) 
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise();
                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;

                if ((remises != null && 0 < remises.length)
                        && (allChequesVal.length == remises[0].getNbOperation().intValue())) {
                    if (chequesVal != null && 0 < chequesVal.length) {
                        long sumRemise = 0;

                        for (int x = 0; x < chequesVal.length; x++) {
                            sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;
//Creation ligne de ch�que

                        for (int x = 0; x < chequesVal.length; x++) {
                            aCheque = chequesVal[x];

                            line = new StringBuffer();
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()));
                            line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(), "0"), 16, " s"));
                            line.append(createBlancs(4, " "));
                            line.append("C");
                            line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                            line.append("Q13");
                            line.append(dateValeur);
                            line.append(" ");
                            line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                            line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 18, " "));
                            line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
                            line.append("030");
                            line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));

                            /**
                             * ligne F12
                             */
                            Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(aCheque.getCompteremettant(), aCheque.getAgenceremettant());
                            Comptes compteFlexCubeDO = CMPUtility.getCompteESNFlexCube(aCheque.getNumerocompte(), aCheque.getAgence());
                            
                             DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                            detbUploadDetail.setBATCH_NO(numeroBatch);
                            detbUploadDetail.setFIN_CYCLE(current_cycle);
                            detbUploadDetail.setPERIOD_CODE(current_period);
                            detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                            
                            
                            detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim()
                                    : Utility.getParam("TXN_BRANCH").charAt(0) + aCheque.getAgenceremettant().substring(3));//agence du compte  cc
                            detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + aCheque.getCompteremettant());//REM ACCOUNT
                            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                            detbUploadDetail.setTXN_CODE("Q13");//Code transaction TXN_CODE
                            detbUploadDetail.setAMOUNT(new BigDecimal(aCheque.getMontantcheque())); //AMOUNT
                            detbUploadDetail.setADDL_TEXT(dateValeur + Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0") + Utility.getParam("LIBCHQALEFLEX1") + " " + Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
                            detbUploadDetail.setREL_CUST(compteFlexCubeDO != null ? compteFlexCubeDO.getNumcptex().substring(0, 9) : "");
                            detbUploadDetail.setEXTERNAL_REF_NO(aCheque.getIdcheque() + detbUploadDetail.getBATCH_NO());
                            detbUploadDetail.setRELATED_ACCOUNT(compteFlexCubeDO != null ? compteFlexCubeDO.getNumcptex() : "");
                            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());

                            detbUploadDetails.add(detbUploadDetail);
                            //aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                            //db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }

                        //db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }

                } else {

                    db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }

            }
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTATTCHQCAIFLEX"), 16, " "));
            line.append(createBlancs(11, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, "0"));
            line.append("Q13");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            /**
             * ligne F12
             */
             DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                            detbUploadDetail.setBATCH_NO(numeroBatch);
                            detbUploadDetail.setFIN_CYCLE(current_cycle);
                            detbUploadDetail.setPERIOD_CODE(current_period);
                            detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                            
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQCAIFLEX"));//REM ACCOUNT
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("Q13");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setVALUE_DATE(Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd"));
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX2"));
            detbUploadDetail.setREL_CUST((Utility.getParam("CPTATTCHQCAIFLEX").length() == 16) ? Utility.getParam("CPTATTCHQCAIFLEX").substring(6, 14) : "999999992");
            detbUploadDetail.setEXTERNAL_REF_NO(detbUploadDetail.getBATCH_NO());
            detbUploadDetail.setRELATED_ACCOUNT(Utility.getParam("CPTATTCHQCAIFLEX"));
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);
            /**
             * ECRITURE DANS LE FICHIER
             */
            detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
            detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
            writeFile(detbUploadDetailArray);

            closeFile();
            montantTotal = 0;
            //    fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
            fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");
            setOut(createFlatFile(fileName));
            StringBuilder lineBuilder = new StringBuilder();
//
//            //entete
            lineBuilder.append("LOAD DATA\n"
                    + "INFILE *\n"
                    + "APPEND\n"
                    + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + " .CSTB_IW_CLEARING_MASTER\n"
                    + "Fields terminated by '~'\n"
                    + "Trailing Nullcols\n"
                    + " (DIRECTION,PRODUCT_CODE,TXN_BRANCH,END_POINT,REM_ACCOUNT,ACC_BRANCH,"
                    + " ACC_CCY,INSTRUMENT_CCY,INSTRUMENT_AMT,INSTRUMENT_NO_1,"
                    + "STATUS,TXN_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\"  ,INSTRUMENT_DATE  DATE \"" + Utility.getParam("DATE_FORMAT") + "\" ," //\"DD-MM-YYYY\"
                    + "ROUTING_NO,RECORD_STAT,AUTH_STAT,MAKER_ID,"
                    + "MAKER_DT_STAMP  DATE \"" + Utility.getParam("DATE_FORMAT") + "\"  ,XREF,SCODE,EVENT_SEQ_NO,"
                    + "MODULE_CODE,MOD_NO,REMARKS,INSTRUMENT_TYPE,CHEQUE_ISSUE_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\"  ,BATCH_NO,BANK_CODE\n, FORCE_POSTING "
                    + "  )\n"
                    + "BEGINDATA");
            writeln(lineBuilder.toString());

            line = new StringBuffer();
            detbUploadDetails = new ArrayList<>();
            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques valid�s

                //Tous les cheques non compensables valid�s d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT =" + Utility.getParam("CETAOPESUPVALSURCAI");
                chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                //La remise en question
                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                //Tous les cheques de la remise (compensables et non)
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise();
                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;

                if ((remises != null && 0 < remises.length)
                        && (allChequesVal.length == remises[0].getNbOperation().intValue())) {
                    if (chequesVal != null && 0 < chequesVal.length) {
                        long sumRemise = 0;

                        for (int x = 0; x < chequesVal.length; x++) {
                            sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;
//Creation ligne de ch�que

                        for (int x = 0; x < chequesVal.length; x++) {
                            aCheque = chequesVal[x];
                            RIO rio = new RIO(CMPUtility.getCodeBanqueSica3() + CMPUtility.getPacSCMPSICA3() + CMPUtility.getDevise() + "001" + Utility.getParam("DATECOMPENS_NAT") + "00001" + Utility.bourrageGZero("" + aCheque.getIdcheque(), 8));
                            aCheque.setRio(rio.getRio());

                            line = new StringBuffer();
                            line.append(aCheque.getBanqueremettant());
                            line.append(aCheque.getBanque());
                            line.append("Q13 ");
                            line.append(Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " "));
                            line.append(Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), ResLoader.getMessages("patternDate")), "yyyyMMdd"));
                            //line += CMPUtility.getDate();
                            line.append(dateValeurDebit);
                            line.append(Utility.bourrageGauche(aCheque.getMontantcheque(), 16, " "));
                            line.append(createBlancs(3, " ") + "UAP");
                            line.append(aCheque.getRio());

                            line.append(CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(), "0"));
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getNumerocompte(), aCheque.getAgence()));

                            /**
                             * F12
                             */
                            lineBuilder = new StringBuilder();
                            Cheques cheque = cheques[i];
                            Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(cheque.getNumerocompte(), cheque.getAgence());
                            lineBuilder.append("I");//DIRECTION
                            lineBuilder.append("~");
                            lineBuilder.append("INCL");//PRODUCT_CODE
                            lineBuilder.append("~");
                            lineBuilder.append(Utility.getParam("TXN_BRANCH")); //TXN_BRANCH
                            lineBuilder.append("~");
                            lineBuilder.append("BCEAOEP");//END_POINT

                            lineBuilder.append("~");
                            lineBuilder.append(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + cheque.getNumerocompte()); //REM_ACCOUNT

                            lineBuilder.append("~");
                            lineBuilder.append(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : ""
                                    + Utility.getParam("TXN_BRANCH").charAt(0) + cheque.getAgence().substring(3));//ACC_BRANCH
                            lineBuilder.append("~");
                            lineBuilder.append("XOF"); // ACC_CCY
                            lineBuilder.append("~");
                            lineBuilder.append("XOF"); //INSTRUMENT_CCY
                            lineBuilder.append("~");
                            lineBuilder.append(new BigDecimal(cheque.getMontantcheque())); //INSTRUMENT_AMT
                            lineBuilder.append("~");
                            lineBuilder.append(cheque.getNumerocheque()); //INSTRUMENT_NO_1
                            lineBuilder.append("~");
                            lineBuilder.append("UNPR"); //STATUS
                            lineBuilder.append("~");
                            lineBuilder.append( Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), ResLoader.getMessages("patternDate")),  Utility.getParam("DATE_FORMAT"))); //TXN_DATE 
                            lineBuilder.append("~");
                            lineBuilder.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT")));//INSTRUMENT_DATE

                            lineBuilder.append("~");
                            lineBuilder.append("0051");//ROUTING_NO

                            lineBuilder.append("~");
                            lineBuilder.append("O");//RECORD_STAT

                            lineBuilder.append("~");
                            lineBuilder.append("A");//AUTH_STAT

                            lineBuilder.append("~");
                            lineBuilder.append(userLogin);//MAKER_ID

                            lineBuilder.append("~");
                            //MAKER_DT_STAMP
                            lineBuilder.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT"))); //MAKER_DT_STAMP
                            lineBuilder.append("~");
                            lineBuilder.append(cheque.getIdcheque()); //XREF
                            lineBuilder.append("~");
                            lineBuilder.append("FCRH");//SCODE
                            lineBuilder.append("~");

                            lineBuilder.append(1);//  //EVENT_SEQ_NO
                            lineBuilder.append("~");
                            lineBuilder.append("CG");//MODULE_CODE
                            lineBuilder.append("~");
                            lineBuilder.append(1); //MOD_NO
                            lineBuilder.append("~");
                            lineBuilder.append("UAP" + cheque.getRio());
                            lineBuilder.append("~");
                            lineBuilder.append("CHQ");
                            lineBuilder.append("~");
                            lineBuilder.append(cheque.getDateemission() != null ? (Utility.convertDateToString(Utility.convertStringToDate(cheque.getDateemission(), "yyyy/MM/dd"), Utility.getParam("DATE_FORMAT"))) : Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT"))); //CHEQUE_ISSUE_DATE //2018/10/10 
                            lineBuilder.append("~");
                            lineBuilder.append(numeroBatch);
                            lineBuilder.append("~");
                            lineBuilder.append(cheque.getBanqueremettant());
                            lineBuilder.append("~");
                            lineBuilder.append("Y");
                            lineBuilder.append("~");

                            writeln(lineBuilder.toString());

                            aCheque.setRio("");

                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }

                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }

                } else {

                    // db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    // db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }

            }

            closeFile();

            setDescription(getDescription() + " ex�cut� avec succ�s:\n Nombre de Ch�que= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Ch�que= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

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
                + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + ". DETB_UPLOAD_DETAIL\n"
                + " Fields terminated by '~'\n"
                + " Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\",ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE  DATE \"" + Utility.getParam("DATE_FORMAT") + "\",LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,TXT_FILE_NAME,INSTRUMENT_NO)\n"
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
