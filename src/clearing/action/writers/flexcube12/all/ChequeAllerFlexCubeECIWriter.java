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

public class ChequeAllerFlexCubeECIWriter
        extends FlatFileWriter {

    public ChequeAllerFlexCubeECIWriter() {
        setDescription("Envoi des cheques vers le SIB");
    }

    public void execute()
            throws Exception {
        super.execute();

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if ((param1 != null) && (param1.length > 0)) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur = " + dateValeur);

        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if ((param1 != null) && (param1.length > 0)) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);
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
        System.out.println("current_cycle " + current_cycle + " current_period" + current_period + " numeroBatch" + numeroBatch);

        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

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

        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");

        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") " + " AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";

        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0L;
        if ((cheques != null) && (0 < cheques.length)) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("");

            for (int i = 0; i < cheques.length; i += j) {
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;
                if ((chequesVal != null) && (0 < chequesVal.length)) {
                    long sumRemise = 0L;

                    Cheques aCheque = chequesVal[0];
                    for (int x = 0; x < chequesVal.length; x++) {
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                    }
                    montantTotal += sumRemise;
                    for (int x = 0; x < chequesVal.length; x++) {
                        aCheque = chequesVal[x];

                        line = new StringBuffer();
                        line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()));
                        line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(), "0"), 16, " "));
                        line.append(createBlancs(4, " "));
                        line.append("C");
                        line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                        line.append("Q13");
                        line.append(dateValeur);
                        line.append(" ");
                        line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                        line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 18, " "));
                       // line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
                        
                         //reference remise en lieu et place de numero de cheques
                         line.append(Utility.bourrageGauche(aCheque.getRefremise() + "", 7, " "));
                         
                        line.append("030");
                        line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));

                        /**
                         * ligne F12
                         */
                        Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(aCheque.getCompteremettant(), aCheque.getAgenceremettant());
                        DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                        detbUploadDetail.setBATCH_NO(numeroBatch);
                        detbUploadDetail.setFIN_CYCLE(current_cycle);
                        detbUploadDetail.setPERIOD_CODE(current_period);
                        detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

                        detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + aCheque.getAgenceremettant().substring(3));//agence du compte
                        detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + aCheque.getCompteremettant());//REM ACCOUNT
                        detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                        detbUploadDetail.setTXN_CODE("Q13");//Code transaction TXN_CODE
                        detbUploadDetail.setAMOUNT(new BigDecimal(aCheque.getMontantcheque())); //AMOUNT
                       // detbUploadDetail.setADDL_TEXT(dateValeur + Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0") + " " + Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 18, " ") + Utility.bourrageGauche(aCheque.getNumerocheque()+ "", 7, "0"));
                        detbUploadDetail.setADDL_TEXT( Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 18, " ") + Utility.bourrageGauche(aCheque.getNumerocheque()+ "", 7, "0"));

                        detbUploadDetail.setREL_CUST(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : "");
                        detbUploadDetail.setEXTERNAL_REF_NO(aCheque.getIdcheque() + detbUploadDetail.getBATCH_NO());
                        detbUploadDetail.setRELATED_ACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "");
                        detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                        detbUploadDetails.add(detbUploadDetail);

                        if (aCheque.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACC")))) {
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }
                    }
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                }
            }
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCHGL"));
            line.append(Utility.getParam("CPTATTCHQALEFLEX"));
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
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCHGL"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX"));//REM ACCOUNT
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("Q13");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());
            detbUploadDetail.setADDL_TEXT(CMPUtility.getDate() + Utility.getParam("LIBCHQALEFLEX2"));
            detbUploadDetail.setREL_CUST((Utility.getParam("CPTATTCHQALEFLEX").length() == 16) ? Utility.getParam("CPTATTCHQALEFLEX").substring(6, 14) : "999999992");
            detbUploadDetail.setEXTERNAL_REF_NO(detbUploadDetail.getBATCH_NO());
            detbUploadDetail.setRELATED_ACCOUNT(Utility.getParam("CPTATTCHQALEFLEX"));
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);
            /**
             * ECRITURE DANS LE FICHIER
             */

            detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
            detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
            writeFile(detbUploadDetailArray);

            setDescription(getDescription() + " execute avec succes:\n Nombre de Cheques= " + cheques.length + " - Montant Total= " + Utility.formatNumber(new StringBuilder().append("").append(montantTotal).toString()) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Cheque= " + cheques.length + " - Montant Total= " + Utility.formatNumber(new StringBuilder().append("").append(montantTotal).toString()));
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
                + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + ". DETB_UPLOAD_DETAIL\n"
                + " Fields terminated by '~'\n"
                + " Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\",ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE   DATE \"" + Utility.getParam("DATE_FORMAT") + "\",LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,TXT_FILE_NAME,INSTRUMENT_NO)\n"
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
            line.append(sep(Utility.convertDateToString(detbUploadDetail.getVALUE_DATE(), Utility.getParam("DATE_FORMAT") )));
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
            line.append(sep(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT"))));
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
