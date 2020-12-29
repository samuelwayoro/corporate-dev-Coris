/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.table.flexcube;

import java.math.BigDecimal;

/**
 *
 * @author Patrick Augou
 */
public class SVTM_ACC_SIG_DET {

    private String BRANCH;
    private String ACC_NO;
    private String SIG_MSG;
    private String SIG_TYPE;
    private BigDecimal APPROVAL_LIMIT;
    private BigDecimal SOLO_SUFFICIENT;
    private String RECORD_STAT;
    private String CIF_SIG_NAME;

    public SVTM_ACC_SIG_DET() {
    }

    public String getACC_NO() {
        return ACC_NO;
    }

    public void setACC_NO(String ACC_NO) {
        this.ACC_NO = ACC_NO;
    }

    public BigDecimal getAPPROVAL_LIMIT() {
        return APPROVAL_LIMIT;
    }

    public void setAPPROVAL_LIMIT(BigDecimal APPROVAL_LIMIT) {
        this.APPROVAL_LIMIT = APPROVAL_LIMIT;
    }

    public String getBRANCH() {
        return BRANCH;
    }

    public void setBRANCH(String BRANCH) {
        this.BRANCH = BRANCH;
    }

    public String getCIF_SIG_NAME() {
        return CIF_SIG_NAME;
    }

    public void setCIF_SIG_NAME(String CIF_SIG_NAME) {
        this.CIF_SIG_NAME = CIF_SIG_NAME;
    }

    public String getRECORD_STAT() {
        return RECORD_STAT;
    }

    public void setRECORD_STAT(String RECORD_STAT) {
        this.RECORD_STAT = RECORD_STAT;
    }

    public String getSIG_MSG() {
        return SIG_MSG;
    }

    public void setSIG_MSG(String SIG_MSG) {
        this.SIG_MSG = SIG_MSG;
    }

    public String getSIG_TYPE() {
        return SIG_TYPE;
    }

    public void setSIG_TYPE(String SIG_TYPE) {
        this.SIG_TYPE = SIG_TYPE;
    }

    public BigDecimal getSOLO_SUFFICIENT() {
        return SOLO_SUFFICIENT;
    }

    public void setSOLO_SUFFICIENT(BigDecimal SOLO_SUFFICIENT) {
        this.SOLO_SUFFICIENT = SOLO_SUFFICIENT;
    }



}
