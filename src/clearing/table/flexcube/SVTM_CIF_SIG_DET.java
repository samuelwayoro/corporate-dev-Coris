/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table.flexcube;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;

/**
 *
 * @author Patrick Augou
 */
public class SVTM_CIF_SIG_DET {

private String CIF_ID;
private String CIF_SIG_ID;
private BigDecimal SPECIMEN_NO;
private BigDecimal SPECIMEN_SEQ_NO;
private String SIGNATURE;
private String SIGNAME;
private String RECORD_STAT;
private Clob SIG_TEXT;
private Blob SIGN_IMG;

    public SVTM_CIF_SIG_DET() {
    }

    public String getSIGNAME() {
        return SIGNAME;
    }

    public void setSIGNAME(String SIGNAME) {
        this.SIGNAME = SIGNAME;
    }

    
    public String getCIF_ID() {
        return CIF_ID;
    }

    public void setCIF_ID(String CIF_ID) {
        this.CIF_ID = CIF_ID;
    }

    public String getCIF_SIG_ID() {
        return CIF_SIG_ID;
    }

    public void setCIF_SIG_ID(String CIF_SIG_ID) {
        this.CIF_SIG_ID = CIF_SIG_ID;
    }

    public String getRECORD_STAT() {
        return RECORD_STAT;
    }

    public void setRECORD_STAT(String RECORD_STAT) {
        this.RECORD_STAT = RECORD_STAT;
    }

    public String getSIGNATURE() {
        return SIGNATURE;
    }

    public void setSIGNATURE(String SIGNATURE) {
        this.SIGNATURE = SIGNATURE;
    }

    public Blob getSIGN_IMG() {
        return SIGN_IMG;
    }

    public void setSIGN_IMG(Blob SIGN_IMG) {
        this.SIGN_IMG = SIGN_IMG;
    }

    public Clob getSIG_TEXT() {
        return SIG_TEXT;
    }

    public void setSIG_TEXT(Clob SIG_TEXT) {
        this.SIG_TEXT = SIG_TEXT;
    }

    public BigDecimal getSPECIMEN_NO() {
        return SPECIMEN_NO;
    }

    public void setSPECIMEN_NO(BigDecimal SPECIMEN_NO) {
        this.SPECIMEN_NO = SPECIMEN_NO;
    }

    public BigDecimal getSPECIMEN_SEQ_NO() {
        return SPECIMEN_SEQ_NO;
    }

    public void setSPECIMEN_SEQ_NO(BigDecimal SPECIMEN_SEQ_NO) {
        this.SPECIMEN_SEQ_NO = SPECIMEN_SEQ_NO;
    }

    @Override
    public String toString() {
        return "SVTM_CIF_SIG_DET{" + "CIF_ID=" + CIF_ID + ", CIF_SIG_ID=" + CIF_SIG_ID + ", SPECIMEN_NO=" + SPECIMEN_NO + ", SPECIMEN_SEQ_NO=" + SPECIMEN_SEQ_NO + ", SIGNATURE=" + SIGNATURE + ", SIGNAME=" + SIGNAME + ", RECORD_STAT=" + RECORD_STAT + ", SIG_TEXT=" + SIG_TEXT + ", SIGN_IMG=" + SIGN_IMG + '}';
    }

    

}
