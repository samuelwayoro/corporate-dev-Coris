/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.table.flexcube;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.utils.Utility;

/**
 *
 * @author patri
 */
public class DETB_UPLOAD_DETAIL implements Serializable {

    private String BATCH_NO;

    private String BRANCH_CODE;

    private String SOURCE_CODE;

    private BigDecimal CURR_NO;

    private String INSTRUMENT_NO;

    private String FIN_CYCLE;

    private String PERIOD_CODE;

    private String MIS_CODE;

    private String REL_CUST;

    private String ADDL_TEXT;

    private String MIS_GROUP;

    private String DW_AC_NO;

    private String ACCOUNT_NEW;

    private String TXN_MIS_1;

    private String TXN_MIS_2;

    private String TXN_MIS_3;

    private String TXN_MIS_4;

    private String TXN_MIS_5;

    private String TXN_MIS_6;

    private String TXN_MIS_7;

    private String TXN_MIS_8;

    private String TXN_MIS_9;

    private String TXN_MIS_10;

    private String COMP_MIS_1;

    private String COMP_MIS_2;

    private String COMP_MIS_3;

    private String COMP_MIS_4;

    private String COMP_MIS_5;

    private String COMP_MIS_6;

    private String COMP_MIS_7;

    private String COMP_MIS_8;

    private String COMP_MIS_9;

    private String COMP_MIS_10;

    private String COST_CODE1;

    private String COST_CODE2;

    private String COST_CODE3;

    private String COST_CODE4;

    private String COST_CODE5;

    private String MIS_HEAD;

    private String RELATED_ACCOUNT;

    private String RELATED_REF;

    private String POOL_CODE;

    private BigDecimal REF_RATE;

    private String CALC_METHOD;

    private String MIS_FLAG;

    private String MIS_GROUP_TXN;

    private String UPLOAD_STAT;

    private String CCY_CD;

    private Date INITIATION_DATE;

    private BigDecimal AMOUNT;

    private String ACCOUNT;

    private String ACCOUNT_BRANCH;

    private String TXN_CODE;

    private String DR_CR;

    private BigDecimal LCY_EQUIVALENT;

    private BigDecimal EXCH_RATE;

    private Date VALUE_DATE;

    private String EXTERNAL_REF_NO;

    private String RESERVED_FUNDS_REF;

    private String DELETE_STAT;

    private Date UPLOAD_DATE;

    private String TXT_FILE_NAME;

    public DETB_UPLOAD_DETAIL() {
        setDefault(this);
    }

    @Override
    public String toString() {
        return "DETB_UPLOAD_DETAIL{" + "BATCH_NO=" + BATCH_NO + ", BRANCH_CODE=" + BRANCH_CODE + ", SOURCE_CODE=" + SOURCE_CODE + ", CURR_NO=" + CURR_NO + ", INSTRUMENT_NO=" + INSTRUMENT_NO + ", FIN_CYCLE=" + FIN_CYCLE + ", PERIOD_CODE=" + PERIOD_CODE + ", MIS_CODE=" + MIS_CODE + ", REL_CUST=" + REL_CUST + ", ADDL_TEXT=" + ADDL_TEXT + ", MIS_GROUP=" + MIS_GROUP + ", DW_AC_NO=" + DW_AC_NO + ", ACCOUNT_NEW=" + ACCOUNT_NEW + ", TXN_MIS_1=" + TXN_MIS_1 + ", TXN_MIS_2=" + TXN_MIS_2 + ", TXN_MIS_3=" + TXN_MIS_3 + ", TXN_MIS_4=" + TXN_MIS_4 + ", TXN_MIS_5=" + TXN_MIS_5 + ", TXN_MIS_6=" + TXN_MIS_6 + ", TXN_MIS_7=" + TXN_MIS_7 + ", TXN_MIS_8=" + TXN_MIS_8 + ", TXN_MIS_9=" + TXN_MIS_9 + ", TXN_MIS_10=" + TXN_MIS_10 + ", COMP_MIS_1=" + COMP_MIS_1 + ", COMP_MIS_2=" + COMP_MIS_2 + ", COMP_MIS_3=" + COMP_MIS_3 + ", COMP_MIS_4=" + COMP_MIS_4 + ", COMP_MIS_5=" + COMP_MIS_5 + ", COMP_MIS_6=" + COMP_MIS_6 + ", COMP_MIS_7=" + COMP_MIS_7 + ", COMP_MIS_8=" + COMP_MIS_8 + ", COMP_MIS_9=" + COMP_MIS_9 + ", COMP_MIS_10=" + COMP_MIS_10 + ", COST_CODE1=" + COST_CODE1 + ", COST_CODE2=" + COST_CODE2 + ", COST_CODE3=" + COST_CODE3 + ", COST_CODE4=" + COST_CODE4 + ", COST_CODE5=" + COST_CODE5 + ", MIS_HEAD=" + MIS_HEAD + ", RELATED_ACCOUNT=" + RELATED_ACCOUNT + ", RELATED_REF=" + RELATED_REF + ", POOL_CODE=" + POOL_CODE + ", REF_RATE=" + REF_RATE + ", CALC_METHOD=" + CALC_METHOD + ", MIS_FLAG=" + MIS_FLAG + ", MIS_GROUP_TXN=" + MIS_GROUP_TXN + ", UPLOAD_STAT=" + UPLOAD_STAT + ", CCY_CD=" + CCY_CD + ", INITIATION_DATE=" + INITIATION_DATE + ", AMOUNT=" + AMOUNT + ", ACCOUNT=" + ACCOUNT + ", ACCOUNT_BRANCH=" + ACCOUNT_BRANCH + ", TXN_CODE=" + TXN_CODE + ", DR_CR=" + DR_CR + ", LCY_EQUIVALENT=" + LCY_EQUIVALENT + ", EXCH_RATE=" + EXCH_RATE + ", VALUE_DATE=" + VALUE_DATE + ", EXTERNAL_REF_NO=" + EXTERNAL_REF_NO + ", RESERVED_FUNDS_REF=" + RESERVED_FUNDS_REF + ", DELETE_STAT=" + DELETE_STAT + ", UPLOAD_DATE=" + UPLOAD_DATE + ", TXT_FILE_NAME=" + TXT_FILE_NAME + '}';
    }

    public String getBATCH_NO() {
        return BATCH_NO;
    }

    public void setBATCH_NO(String BATCH_NO) {
        this.BATCH_NO = BATCH_NO;
    }

    public String getBRANCH_CODE() {
        return BRANCH_CODE;
    }

    public void setBRANCH_CODE(String BRANCH_CODE) {
        this.BRANCH_CODE = BRANCH_CODE;
    }

    public String getSOURCE_CODE() {
        return SOURCE_CODE;
    }

    public void setSOURCE_CODE(String SOURCE_CODE) {
        this.SOURCE_CODE = SOURCE_CODE;
    }

    public String getINSTRUMENT_NO() {
        return INSTRUMENT_NO;
    }

    public void setINSTRUMENT_NO(String INSTRUMENT_NO) {
        this.INSTRUMENT_NO = INSTRUMENT_NO;
    }

    public String getFIN_CYCLE() {
        return FIN_CYCLE;
    }

    public void setFIN_CYCLE(String FIN_CYCLE) {
        this.FIN_CYCLE = FIN_CYCLE;
    }

    public String getPERIOD_CODE() {
        return PERIOD_CODE;
    }

    public void setPERIOD_CODE(String PERIOD_CODE) {
        this.PERIOD_CODE = PERIOD_CODE;
    }

    public String getMIS_CODE() {
        return MIS_CODE;
    }

    public void setMIS_CODE(String MIS_CODE) {
        this.MIS_CODE = MIS_CODE;
    }

    public String getREL_CUST() {
        return REL_CUST;
    }

    public void setREL_CUST(String REL_CUST) {
        this.REL_CUST = REL_CUST;
    }

    public String getADDL_TEXT() {
        return ADDL_TEXT;
    }

    public void setADDL_TEXT(String ADDL_TEXT) {
        this.ADDL_TEXT = ADDL_TEXT;
    }

    public String getMIS_GROUP() {
        return MIS_GROUP;
    }

    public void setMIS_GROUP(String MIS_GROUP) {
        this.MIS_GROUP = MIS_GROUP;
    }

    public String getDW_AC_NO() {
        return DW_AC_NO;
    }

    public void setDW_AC_NO(String DW_AC_NO) {
        this.DW_AC_NO = DW_AC_NO;
    }

    public String getACCOUNT_NEW() {
        return ACCOUNT_NEW;
    }

    public void setACCOUNT_NEW(String ACCOUNT_NEW) {
        this.ACCOUNT_NEW = ACCOUNT_NEW;
    }

    public String getTXN_MIS_1() {
        return TXN_MIS_1;
    }

    public void setTXN_MIS_1(String TXN_MIS_1) {
        this.TXN_MIS_1 = TXN_MIS_1;
    }

    public String getTXN_MIS_2() {
        return TXN_MIS_2;
    }

    public void setTXN_MIS_2(String TXN_MIS_2) {
        this.TXN_MIS_2 = TXN_MIS_2;
    }

    public String getTXN_MIS_3() {
        return TXN_MIS_3;
    }

    public void setTXN_MIS_3(String TXN_MIS_3) {
        this.TXN_MIS_3 = TXN_MIS_3;
    }

    public String getTXN_MIS_4() {
        return TXN_MIS_4;
    }

    public void setTXN_MIS_4(String TXN_MIS_4) {
        this.TXN_MIS_4 = TXN_MIS_4;
    }

    public String getTXN_MIS_5() {
        return TXN_MIS_5;
    }

    public void setTXN_MIS_5(String TXN_MIS_5) {
        this.TXN_MIS_5 = TXN_MIS_5;
    }

    public String getTXN_MIS_6() {
        return TXN_MIS_6;
    }

    public void setTXN_MIS_6(String TXN_MIS_6) {
        this.TXN_MIS_6 = TXN_MIS_6;
    }

    public String getTXN_MIS_7() {
        return TXN_MIS_7;
    }

    public void setTXN_MIS_7(String TXN_MIS_7) {
        this.TXN_MIS_7 = TXN_MIS_7;
    }

    public String getTXN_MIS_8() {
        return TXN_MIS_8;
    }

    public void setTXN_MIS_8(String TXN_MIS_8) {
        this.TXN_MIS_8 = TXN_MIS_8;
    }

    public String getTXN_MIS_9() {
        return TXN_MIS_9;
    }

    public void setTXN_MIS_9(String TXN_MIS_9) {
        this.TXN_MIS_9 = TXN_MIS_9;
    }

    public String getTXN_MIS_10() {
        return TXN_MIS_10;
    }

    public void setTXN_MIS_10(String TXN_MIS_10) {
        this.TXN_MIS_10 = TXN_MIS_10;
    }

    public String getCOMP_MIS_1() {
        return COMP_MIS_1;
    }

    public void setCOMP_MIS_1(String COMP_MIS_1) {
        this.COMP_MIS_1 = COMP_MIS_1;
    }

    public String getCOMP_MIS_2() {
        return COMP_MIS_2;
    }

    public void setCOMP_MIS_2(String COMP_MIS_2) {
        this.COMP_MIS_2 = COMP_MIS_2;
    }

    public String getCOMP_MIS_3() {
        return COMP_MIS_3;
    }

    public void setCOMP_MIS_3(String COMP_MIS_3) {
        this.COMP_MIS_3 = COMP_MIS_3;
    }

    public String getCOMP_MIS_4() {
        return COMP_MIS_4;
    }

    public void setCOMP_MIS_4(String COMP_MIS_4) {
        this.COMP_MIS_4 = COMP_MIS_4;
    }

    public String getCOMP_MIS_5() {
        return COMP_MIS_5;
    }

    public void setCOMP_MIS_5(String COMP_MIS_5) {
        this.COMP_MIS_5 = COMP_MIS_5;
    }

    public String getCOMP_MIS_6() {
        return COMP_MIS_6;
    }

    public void setCOMP_MIS_6(String COMP_MIS_6) {
        this.COMP_MIS_6 = COMP_MIS_6;
    }

    public String getCOMP_MIS_7() {
        return COMP_MIS_7;
    }

    public void setCOMP_MIS_7(String COMP_MIS_7) {
        this.COMP_MIS_7 = COMP_MIS_7;
    }

    public String getCOMP_MIS_8() {
        return COMP_MIS_8;
    }

    public void setCOMP_MIS_8(String COMP_MIS_8) {
        this.COMP_MIS_8 = COMP_MIS_8;
    }

    public String getCOMP_MIS_9() {
        return COMP_MIS_9;
    }

    public void setCOMP_MIS_9(String COMP_MIS_9) {
        this.COMP_MIS_9 = COMP_MIS_9;
    }

    public String getCOMP_MIS_10() {
        return COMP_MIS_10;
    }

    public void setCOMP_MIS_10(String COMP_MIS_10) {
        this.COMP_MIS_10 = COMP_MIS_10;
    }

    public String getCOST_CODE1() {
        return COST_CODE1;
    }

    public void setCOST_CODE1(String COST_CODE1) {
        this.COST_CODE1 = COST_CODE1;
    }

    public String getCOST_CODE2() {
        return COST_CODE2;
    }

    public void setCOST_CODE2(String COST_CODE2) {
        this.COST_CODE2 = COST_CODE2;
    }

    public String getCOST_CODE3() {
        return COST_CODE3;
    }

    public void setCOST_CODE3(String COST_CODE3) {
        this.COST_CODE3 = COST_CODE3;
    }

    public String getCOST_CODE4() {
        return COST_CODE4;
    }

    public void setCOST_CODE4(String COST_CODE4) {
        this.COST_CODE4 = COST_CODE4;
    }

    public String getCOST_CODE5() {
        return COST_CODE5;
    }

    public void setCOST_CODE5(String COST_CODE5) {
        this.COST_CODE5 = COST_CODE5;
    }

    public String getMIS_HEAD() {
        return MIS_HEAD;
    }

    public void setMIS_HEAD(String MIS_HEAD) {
        this.MIS_HEAD = MIS_HEAD;
    }

    public String getRELATED_ACCOUNT() {
        return RELATED_ACCOUNT;
    }

    public void setRELATED_ACCOUNT(String RELATED_ACCOUNT) {
        this.RELATED_ACCOUNT = RELATED_ACCOUNT;
    }

    public String getRELATED_REF() {
        return RELATED_REF;
    }

    public void setRELATED_REF(String RELATED_REF) {
        this.RELATED_REF = RELATED_REF;
    }

    public String getPOOL_CODE() {
        return POOL_CODE;
    }

    public void setPOOL_CODE(String POOL_CODE) {
        this.POOL_CODE = POOL_CODE;
    }

    public BigDecimal getREF_RATE() {
        return REF_RATE;
    }

    public void setREF_RATE(BigDecimal REF_RATE) {
        this.REF_RATE = REF_RATE;
    }

    public String getMIS_FLAG() {
        return MIS_FLAG;
    }

    public void setMIS_FLAG(String MIS_FLAG) {
        this.MIS_FLAG = MIS_FLAG;
    }

    public BigDecimal getCURR_NO() {
        return CURR_NO;
    }

    public void setCURR_NO(BigDecimal CURR_NO) {
        this.CURR_NO = CURR_NO;
    }

    public String getCALC_METHOD() {
        return CALC_METHOD;
    }

    public void setCALC_METHOD(String CALC_METHOD) {
        this.CALC_METHOD = CALC_METHOD;
    }

    public String getMIS_GROUP_TXN() {
        return MIS_GROUP_TXN;
    }

    public void setMIS_GROUP_TXN(String MIS_GROUP_TXN) {
        this.MIS_GROUP_TXN = MIS_GROUP_TXN;
    }

    public String getUPLOAD_STAT() {
        return UPLOAD_STAT;
    }

    public void setUPLOAD_STAT(String UPLOAD_STAT) {
        this.UPLOAD_STAT = UPLOAD_STAT;
    }

    public String getDR_CR() {
        return DR_CR;
    }

    public void setDR_CR(String DR_CR) {
        this.DR_CR = DR_CR;
    }

    public String getCCY_CD() {
        return CCY_CD;
    }

    public void setCCY_CD(String CCY_CD) {
        this.CCY_CD = CCY_CD;
    }

    public Date getINITIATION_DATE() {
        return INITIATION_DATE;
    }

    public void setINITIATION_DATE(Date INITIATION_DATE) {
        this.INITIATION_DATE = INITIATION_DATE;
    }

    public BigDecimal getAMOUNT() {
        return AMOUNT;
    }

    public void setAMOUNT(BigDecimal AMOUNT) {
        this.AMOUNT = AMOUNT;
    }

    public String getACCOUNT() {
        return ACCOUNT;
    }

    public void setACCOUNT(String ACCOUNT) {
        this.ACCOUNT = ACCOUNT;
    }

    public String getACCOUNT_BRANCH() {
        return ACCOUNT_BRANCH;
    }

    public void setACCOUNT_BRANCH(String ACCOUNT_BRANCH) {
        this.ACCOUNT_BRANCH = ACCOUNT_BRANCH;
    }

    public String getTXN_CODE() {
        return TXN_CODE;
    }

    public void setTXN_CODE(String TXN_CODE) {
        this.TXN_CODE = TXN_CODE;
    }

    public BigDecimal getLCY_EQUIVALENT() {
        return LCY_EQUIVALENT;
    }

    public void setLCY_EQUIVALENT(BigDecimal LCY_EQUIVALENT) {
        this.LCY_EQUIVALENT = LCY_EQUIVALENT;
    }

    public BigDecimal getEXCH_RATE() {
        return EXCH_RATE;
    }

    public void setEXCH_RATE(BigDecimal EXCH_RATE) {
        this.EXCH_RATE = EXCH_RATE;
    }

    public Date getVALUE_DATE() {
        return VALUE_DATE;
    }

    public void setVALUE_DATE(Date VALUE_DATE) {
        this.VALUE_DATE = VALUE_DATE;
    }

    public String getEXTERNAL_REF_NO() {
        return EXTERNAL_REF_NO;
    }

    public void setEXTERNAL_REF_NO(String EXTERNAL_REF_NO) {
        this.EXTERNAL_REF_NO = EXTERNAL_REF_NO;
    }

    public String getRESERVED_FUNDS_REF() {
        return RESERVED_FUNDS_REF;
    }

    public void setRESERVED_FUNDS_REF(String RESERVED_FUNDS_REF) {
        this.RESERVED_FUNDS_REF = RESERVED_FUNDS_REF;
    }

    public String getDELETE_STAT() {
        return DELETE_STAT;
    }

    public void setDELETE_STAT(String DELETE_STAT) {
        this.DELETE_STAT = DELETE_STAT;
    }

    public Date getUPLOAD_DATE() {
        return UPLOAD_DATE;
    }

    public void setUPLOAD_DATE(Date UPLOAD_DATE) {
        this.UPLOAD_DATE = UPLOAD_DATE;
    }

    public String getTXT_FILE_NAME() {
        return TXT_FILE_NAME;
    }

    public void setTXT_FILE_NAME(String TXT_FILE_NAME) {
        this.TXT_FILE_NAME = TXT_FILE_NAME;
    }

    public void setDefault(DETB_UPLOAD_DETAIL detbUploadDetails) {

        detbUploadDetails.setBRANCH_CODE(Utility.getParam("TXN_BRANCH"));  //Agence ou le chargement est effectue
        detbUploadDetails.setSOURCE_CODE(Utility.getParam("BATCH_TYPE")); //SOURCE_CODE
        detbUploadDetails.setUPLOAD_STAT("U");//UPLOAD_STAT
        detbUploadDetails.setEXCH_RATE(BigDecimal.ONE); //EXCH_RATE
        detbUploadDetails.setCCY_CD("XOF");//CCY_CD
        detbUploadDetails.setINSTRUMENT_NO((detbUploadDetails.getINSTRUMENT_NO() != null) ? detbUploadDetails.getINSTRUMENT_NO() : "");
        detbUploadDetails.setINITIATION_DATE(new Date(System.currentTimeMillis()));//INITIATION_DATE
        detbUploadDetails.setLCY_EQUIVALENT(detbUploadDetails.getAMOUNT()); //LCY_EQUIVALENT

    }

}
