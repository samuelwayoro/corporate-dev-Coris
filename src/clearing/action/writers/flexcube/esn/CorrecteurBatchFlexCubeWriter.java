/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.esn;

import clearing.table.flexcube.DETB_UPLOAD_DETAIL;
import java.io.File; 
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;

/**
 *
 * @author patri
 */
public class CorrecteurBatchFlexCubeWriter extends FlatFileWriter {

    public CorrecteurBatchFlexCubeWriter() {
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String oldNumeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            oldNumeroBatch = param1[0];
        }
        System.out.println("Ancien Numéro de Batch = " + oldNumeroBatch);

        String newNumeroBatch = "";
        param1 = (String[]) getParametersMap().get("textParam2");
        if (param1 != null && param1.length > 0) {
            newNumeroBatch = param1[0];
        }
        System.out.println("Nouveau Numéro de Batch = " + newNumeroBatch);

        String dateValeur = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur = " + dateValeur);

        dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "yyyyMMdd"), "ddMMyyyy");
        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

        // distinct Liste des Ref_no a probleme 
        String sql = "select * from " + Utility.getParam("FLEXSCHEMA") + ".DETB_UPLOAD_DETAIL where INITIATION_DATE = TO_DATE('" + dateValeur + "','ddmmyyyy') and BATCH_NO ='" + oldNumeroBatch + "' " + " and upload_stat ='E'";
        DETB_UPLOAD_DETAIL[] detbUploadDetailsBad = (DETB_UPLOAD_DETAIL[]) dbExt.retrieveRowAsObject(sql, new DETB_UPLOAD_DETAIL());

        if (detbUploadDetailsBad != null && detbUploadDetailsBad.length > 0) {
            Set<String> refnoSet = new HashSet();
            long sumTransactionsCredit = 0;
            long sumTransactionsDebit = 0;
            for (int i = 0; i < detbUploadDetailsBad.length; i++) {

                if (detbUploadDetailsBad[i].getDR_CR().equalsIgnoreCase("C")) {
                    sumTransactionsCredit += detbUploadDetailsBad[i].getAMOUNT().longValue();
                }
                if (detbUploadDetailsBad[i].getDR_CR().equalsIgnoreCase("D")) {
                    sumTransactionsDebit += detbUploadDetailsBad[i].getAMOUNT().longValue();
                }

                refnoSet.add(detbUploadDetailsBad[i].getEXTERNAL_REF_NO());
            }
            long sumTransactions = Math.abs(sumTransactionsCredit - sumTransactionsDebit);
            String refnoList = "";
            for (Iterator iterator = refnoSet.iterator(); iterator.hasNext();) {
                refnoList += "'" + (String) iterator.next() + "',";
            }

            refnoList = "(" + refnoList.substring(0, refnoList.length() - 1) + ")";
            sql = "select * from " + Utility.getParam("FLEXSCHEMA") + ".DETB_UPLOAD_DETAIL where INITIATION_DATE = TO_DATE('" + dateValeur + "','ddmmyyyy') and BATCH_NO ='" + oldNumeroBatch + "' " + " and external_ref_no  in " + refnoList;
            detbUploadDetailsBad = (DETB_UPLOAD_DETAIL[]) dbExt.retrieveRowAsObject(sql, new DETB_UPLOAD_DETAIL());

            String badFileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "old" + oldNumeroBatch + dateValeur + ".bad";
            setOut(createFlatFile(badFileName));
            setSep(";");
            writeBadFile(detbUploadDetailsBad);
            closeFile();
            setSep("~");

            //Fichier Good
            sql = "select * from " + Utility.getParam("FLEXSCHEMA") + ".DETB_UPLOAD_DETAIL where INITIATION_DATE = TO_DATE('" + dateValeur + "','ddmmyyyy') and BATCH_NO ='" + oldNumeroBatch + "' " + " and external_ref_no not in " + refnoList + " order by curr_no";
            DETB_UPLOAD_DETAIL[] detbUploadDetailsGood = (DETB_UPLOAD_DETAIL[]) dbExt.retrieveRowAsObject(sql, new DETB_UPLOAD_DETAIL());

            if (detbUploadDetailsGood != null && detbUploadDetailsGood.length > 0) {
                for (int i = 0; i < detbUploadDetailsGood.length; i++) {

                    detbUploadDetailsGood[i].setBATCH_NO(newNumeroBatch);
                    detbUploadDetailsGood[i].setUPLOAD_STAT("U");
                    detbUploadDetailsGood[i].setEXTERNAL_REF_NO(detbUploadDetailsGood[i].getEXTERNAL_REF_NO().replaceFirst(oldNumeroBatch, newNumeroBatch));
                    if (detbUploadDetailsGood[i].getEXTERNAL_REF_NO().equalsIgnoreCase(newNumeroBatch)) {
                        detbUploadDetailsGood[i].setAMOUNT(new BigDecimal(detbUploadDetailsGood[i].getAMOUNT().longValue() - sumTransactions));
                        detbUploadDetailsGood[i].setLCY_EQUIVALENT(detbUploadDetailsGood[i].getAMOUNT());

                    }

                }
                String newFileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + oldNumeroBatch + "Old" + newNumeroBatch + "New" + dateValeur + Utility.getParam("SIB_FILE_SQL_EXTENSION");
                setOut(createFlatFile(newFileName));
                writeFile(detbUploadDetailsGood);
                closeFile();

                setDescription("<BR>Correcteur de Batch executé avec succès");
                setDescription(getDescription() + " :<BR> -Nom de fichier bad = <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + badFileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "\">" + badFileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "</a>" + " <BR> - Nom du nouveau Fichier Batch = " + newFileName);
                logEvent("INFO", " Nom de fichier bad = " + badFileName + " - Nom du nouveau Fichier Batch = " + newFileName);
            }

        } else {

            setDescription(getDescription() + " :\n Il n'y aucun élément disponible");
            logEvent("INFO", "Il n'y aucun élément disponible");

        }
        dbExt.close();

    }

    private void writeFile(DETB_UPLOAD_DETAIL[] detbUploadDetailList) {

        createEnteteSQL();
        for (DETB_UPLOAD_DETAIL detbUploadDetail : detbUploadDetailList) {
            String line = sep(detbUploadDetail.getFIN_CYCLE())
                    + sep(detbUploadDetail.getPERIOD_CODE())
                    + sep(Utility.convertDateToString(detbUploadDetail.getVALUE_DATE(), "ddMMyyyy"))
                    + sep(detbUploadDetail.getADDL_TEXT())
                    + sep(detbUploadDetail.getBRANCH_CODE())
                    + sep(detbUploadDetail.getSOURCE_CODE())
                    + sep(detbUploadDetail.getACCOUNT_BRANCH())
                    + sep(detbUploadDetail.getACCOUNT())
                    + sep(detbUploadDetail.getTXN_CODE())
                    + sep(detbUploadDetail.getBATCH_NO())
                    + sep(String.valueOf(detbUploadDetail.getCURR_NO()))
                    + sep(detbUploadDetail.getAMOUNT().toString())
                    + sep(detbUploadDetail.getDR_CR())
                    + sep(detbUploadDetail.getUPLOAD_STAT())
                    + sep(detbUploadDetail.getCCY_CD())
                    + sep(Utility.convertDateToString(detbUploadDetail.getINITIATION_DATE(), "ddMMyyyy"))
                    + sep(detbUploadDetail.getLCY_EQUIVALENT().toString())
                    + sep(detbUploadDetail.getEXCH_RATE().toString())
                    + sep(detbUploadDetail.getREL_CUST())
                    + sep(detbUploadDetail.getEXTERNAL_REF_NO())
                    + detbUploadDetail.getRELATED_ACCOUNT();
            writeln(line);
        }

    }

    private void writeBadFile(DETB_UPLOAD_DETAIL[] detbUploadDetailList) {

        for (DETB_UPLOAD_DETAIL detbUploadDetail : detbUploadDetailList) {
            String line
                    = sep(Utility.convertDateToString(detbUploadDetail.getVALUE_DATE(), "ddMMyyyy"))
                    + sep(detbUploadDetail.getADDL_TEXT())
                    + sep(detbUploadDetail.getBRANCH_CODE())
                    + sep(detbUploadDetail.getACCOUNT())
                    + sep(detbUploadDetail.getAMOUNT().toString())
                    + sep(detbUploadDetail.getDR_CR())
                    + sep(Utility.convertDateToString(detbUploadDetail.getINITIATION_DATE(), "ddMMyyyy"))
                    + sep(detbUploadDetail.getLCY_EQUIVALENT().toString())
                    + sep(detbUploadDetail.getBATCH_NO());

            writeln(line);
        }

    }

    private String sep = "~";

    public String getSep() {
        return sep;
    }

    public void setSep(String sep) {
        this.sep = sep;
    }

    private String sep(String value) {
        return value + sep;
    }

    private void createEnteteSQL() {
        StringBuilder line = new StringBuilder();

        //entete
        line.append("LOAD DATA\n"
                + "INFILE *\n"
                + "APPEND\n"
                + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + ". DETB_UPLOAD_DETAIL\n"
                + "Fields terminated by '~'\n"
                + "Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account)\n"
                + "BEGINDATA");
        writeln(line.toString());
    }
}
