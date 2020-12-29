/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube12.esn;

import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.patware.action.file.FlatFileWriter;
import org.patware.bean.table.Fichiers;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author BOUIKS
 */
public class DetbUploadMasterWriter extends FlatFileWriter {

    private String batchNo;
    private String libelle;
    private String fileName;

    public DetbUploadMasterWriter() {
    }

    public DetbUploadMasterWriter(String batchNo, String libelle, String fileName) {
        this.batchNo = batchNo;
        this.libelle = libelle;
        this.fileName = fileName;
        System.out.println("DetbUploadMasterWriter fileName" + fileName);
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        System.out.println("Execution DetbUploadMasterWriter ");
        File batchFile = new File(fileName);
     
        String fileMaster = Utility.getParam("SIB_IN_FOLDER") + File.separator + "MASTER" + File.separator + "MASTER_" + fileName;
        if (Utility.createFolderIfItsnt(new File(fileMaster).getParentFile(), null)) {

        }
           System.out.println("fileMaster "+fileMaster);
        setOut(createFlatFile(fileMaster));
        createEnteteSQL();
        System.out.println("execute DetbUploadMasterWriter" + fileName + " batchNo " + batchNo + " Libelle " + libelle);
        createLinesSQL(batchNo, libelle, batchFile);
        closeFile();

    }

    private String getUserInsession(String uploadFileName) {
        String userInSession = "";
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            String sql = "SELECT * FROM FICHIERS WHERE DATERECEPTION='" + Utility.convertDateToString(new Date(), "yyyy/MM/dd") + "' AND NOMFICHIER ='" + uploadFileName + "' ";
            Fichiers[] fichiers = (Fichiers[]) db.retrieveRowAsObject(sql, new Fichiers());
            if (fichiers != null && fichiers.length > 0) {
                userInSession = fichiers[0].getUserUpload();
            }
        } catch (Exception ex) {
            Logger.getLogger(DetbUploadMasterWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userInSession;
    }

    private void createLinesSQL(String batchNo, String batchDesc, File uploadFileName) {

        String userLogin = getUserInsession(uploadFileName.getName());
        if (userLogin == null || userLogin.isEmpty()) {
            userLogin = "USERTEST";
        }
        /**
         * (BRANCH_CODE, SOURCE_CODE, BATCH_NO, BALANCING, BATCH_DESC,
         * MIS_REQUIRED, AUTO_AUTH, GL_OFFSET_ENTRY_REQD, UDF_UPLOAD_REQD,
         * UPLOAD_STAT, JOBNO, SYSTEM_BATCH, MAKER_ID, AUTH_STAT, RECORD_STAT,
         * ONCE_AUTH, UPLOAD_DATE, UPLOAD_FILE_NAME) values ('L01', 'UAP',
         * '4120', 'Y', 'COMPENSE 19 NOV 2018', 'N', 'N', 'N', 'N', 'U', 1, 'N',
         * 'JNDOYE', 'U', 'O', 'Y', '19-NOV-2019', '4120.TXT');
         *
         *
         */

        StringBuilder line = new StringBuilder();
        line.append(Utility.getParam("TXN_BRANCH"));//   BRANCH_CODE
        line.append("~");
        line.append(Utility.getParam("BATCH_TYPE"));//SOURCE_CODE  
        line.append("~");
        line.append(batchNo);//BATCH_NO 
        line.append("~");
        line.append("Y");//BALANCING 
        line.append("~");
        line.append(batchDesc);//BATCH_DESC  
        line.append("~");
        line.append("N");//MIS_REQUIRED  
        line.append("~");
        line.append("N");//AUTO_AUTH  
        line.append("~");
        line.append("N");//GL_OFFSET_ENTRY_REQD  
        line.append("~");
        line.append("N");//UDF_UPLOAD_REQD  
        line.append("~");
        line.append("U");//UPLOAD_STAT  
        line.append("~");
        line.append("1");//JOBNO  
        line.append("~");
        line.append("N");//SYSTEM_BATCH  
        line.append("~");
        line.append(userLogin);//MAKER_ID 
        line.append("~");
        if (Utility.getParam("FLEXVERSION")!=null && Utility.getParam("FLEXVERSION").equals("7")){
            line.append(userLogin).append("~");//USER_ID 
        }
        line.append("U");//AUTH_STAT  
        line.append("~");
        line.append("O");//RECORD_STAT  
        line.append("~");
        line.append("Y");//ONCE_AUTH  
        line.append("~");
        line.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT"))); //UPLOAD_DATE
        line.append("~");
        line.append(uploadFileName.getName()); //UPLOAD_FILE_NAME
        writeln(line.toString());

    }

    private void createEnteteSQL() { //DETB_UPLOAD_MASTER 
        StringBuilder line = new StringBuilder();
        /**
         *    //entete lineBuilder.append("LOAD DATA\n" + "INFILE *\n" +
         * "APPEND\n" + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + "
         * .CSTB_IW_CLEARING_MASTER\n" + "Fields terminated by '~'\n" +
         * "Trailing Nullcols\n" + "
         * (DIRECTION,PRODUCT_CODE,TXN_BRANCH,END_POINT,REM_ACCOUNT,ACC_BRANCH,"
         * + " ACC_CCY,INSTRUMENT_CCY,INSTRUMENT_AMT,INSTRUMENT_NO_1," +
         * "STATUS,TXN_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\"
         * ,INSTRUMENT_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\" ,"
         * //\"DD-MM-YYYY\" + "ROUTING_NO,RECORD_STAT,AUTH_STAT,MAKER_ID," +
         * "MAKER_DT_STAMP DATE \"" + Utility.getParam("DATE_FORMAT") + "\"
         * ,XREF,SCODE,EVENT_SEQ_NO," +
         * "MODULE_CODE,MOD_NO,REMARKS,INSTRUMENT_TYPE,CHEQUE_ISSUE_DATE DATE
         * \"" + Utility.getParam("DATE_FORMAT") + "\" ,BATCH_NO,BANK_CODE\n,
         * FORCE_POSTING " + " )\n" + "BEGINDATA");
         */
        //entete
        /*
        BRANCH_CODE, SOURCE_CODE, BATCH_NO, BALANCING, BATCH_DESC, MIS_REQUIRED, AUTO_AUTH,
        GL_OFFSET_ENTRY_REQD , USER_ID, UPLOAD_STAT,  JOBNO, SYSTEM_BATCH,  MAKER_ID,
        AUTH_STAT, RECORD_STAT, ONCE_AUTH,  UPLOAD_DATE, UPLOAD_FILE_NAME
         */
        //entete

        /**
         * (BRANCH_CODE, SOURCE_CODE, BATCH_NO, BALANCING, BATCH_DESC,
         * MIS_REQUIRED, AUTO_AUTH, GL_OFFSET_ENTRY_REQD, UDF_UPLOAD_REQD,
         * UPLOAD_STAT, JOBNO, SYSTEM_BATCH, MAKER_ID, AUTH_STAT, RECORD_STAT,
         * ONCE_AUTH, UPLOAD_DATE, UPLOAD_FILE_NAME) values ('L01', 'UAP',
         * '4120', 'Y', 'COMPENSE 19 NOV 2018', 'N', 'N', 'N', 'N', 'U', 1, 'N',
         * 'JNDOYE', 'U', 'O', 'Y', '19-NOV-2019', '4120.TXT');
         *
         *
         */
        line.append("LOAD DATA\n"
                + "INFILE *\n"
                + "APPEND\n"
                + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + " .DETB_UPLOAD_MASTER\n"
                + "Fields terminated by '~'\n"
                + "Trailing Nullcols\n"
                + " (BRANCH_CODE, SOURCE_CODE, BATCH_NO, BALANCING, BATCH_DESC, MIS_REQUIRED, AUTO_AUTH, "
                + " GL_OFFSET_ENTRY_REQD, UDF_UPLOAD_REQD, UPLOAD_STAT,  JOBNO, SYSTEM_BATCH,  MAKER_ID,");
         
        if (Utility.getParam("FLEXVERSION")!=null && Utility.getParam("FLEXVERSION").equals("7")){
            line.append(" USER_ID,");//USER_ID 
        }
        
        line.append(" AUTH_STAT, RECORD_STAT, ONCE_AUTH, UPLOAD_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\" , UPLOAD_FILE_NAME  "
                + "  )\n"
                + "BEGINDATA");
        writeln(line.toString());
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
