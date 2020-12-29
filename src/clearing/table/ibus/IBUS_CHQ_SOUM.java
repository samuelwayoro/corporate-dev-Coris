/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table.ibus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 *
 * @author Patrick Augou
 */

public class IBUS_CHQ_SOUM implements Serializable {
    
   
    private BigDecimal no_Ex;

    private String cd_Scpta;

    private BigDecimal id_Op;

    private BigDecimal mt_Op;

    private String ref_Cheque;
 
    private String cd_Bq;

    private String cd_Ag;

    private String nm_ivext_Emis;
 
    private Date dt_Emis;
  
    private Date dt_Recu;

    private BigDecimal mt_Cheq;

    private String no_Cpt;

    private BigDecimal idcompense;

    private String app_src;

    private String app_dest;

    private String flux;



    public String getNm_ivext_Emis() {
        return nm_ivext_Emis;
    }

    public void setNm_ivext_Emis(String nm_ivext_Emis) {
        this.nm_ivext_Emis = nm_ivext_Emis;
    }
    
    public String getApp_dest() {
        return app_dest;
    }

    public void setApp_dest(String app_dest) {
        this.app_dest = app_dest;
    }

    public String getApp_src() {
        return app_src;
    }

    public void setApp_src(String app_src) {
        this.app_src = app_src;
    }

    public String getFlux() {
        return flux;
    }

    public void setFlux(String flux) {
        this.flux = flux;
    }


    public IBUS_CHQ_SOUM() {
    }

    public String getCd_Ag() {
        return cd_Ag;
    }

    public void setCd_Ag(String cd_Ag) {
        this.cd_Ag = cd_Ag;
    }

    public String getCd_Bq() {
        return cd_Bq;
    }

    public void setCd_Bq(String cd_Bq) {
        this.cd_Bq = cd_Bq;
    }

    public String getCd_Scpta() {
        return cd_Scpta;
    }

    public void setCd_Scpta(String cd_Scpta) {
        this.cd_Scpta = cd_Scpta;
    }

    public Date getDt_Emis() {
        return dt_Emis;
    }

    public void setDt_Emis(Date dt_Emis) {
        this.dt_Emis = dt_Emis;
    }

    public Date getDt_Recu() {
        return dt_Recu;
    }

    public void setDt_Recu(Date dt_Recu) {
        this.dt_Recu = dt_Recu;
    }

    public BigDecimal getId_Op() {
        return id_Op;
    }

    public void setId_Op(BigDecimal id_Op) {
        this.id_Op = id_Op;
    }

    public BigDecimal getIdcompense() {
        return idcompense;
    }

    public void setIdcompense(BigDecimal idcompense) {
        this.idcompense = idcompense;
    }

    public BigDecimal getMt_Cheq() {
        return mt_Cheq;
    }

    public void setMt_Cheq(BigDecimal mt_Cheq) {
        this.mt_Cheq = mt_Cheq;
    }

    public BigDecimal getMt_Op() {
        return mt_Op;
    }

    public void setMt_Op(BigDecimal mt_Op) {
        this.mt_Op = mt_Op;
    }

    public String getNo_Cpt() {
        return no_Cpt;
    }

    public void setNo_Cpt(String no_Cpt) {
        this.no_Cpt = no_Cpt;
    }

    public BigDecimal getNo_Ex() {
        return no_Ex;
    }

    public void setNo_Ex(BigDecimal no_Ex) {
        this.no_Ex = no_Ex;
    }

    public String getRef_Cheque() {
        return ref_Cheque;
    }

    public void setRef_Cheque(String ref_Cheque) {
        this.ref_Cheque = ref_Cheque;
    }

    public IBUS_CHQ_SOUM(BigDecimal no_Ex, String cd_Scpta, BigDecimal id_Op, BigDecimal mt_Op, String ref_Cheque, String cd_Bq, String cd_Ag,String nm_emis, Date dt_Emis, Date dt_Recu, BigDecimal mt_Cheq, String no_Cpt, BigDecimal idcompense) {
        this.no_Ex = no_Ex;
        this.cd_Scpta = cd_Scpta;
        this.id_Op = id_Op;
        this.mt_Op = mt_Op;
        this.ref_Cheque = ref_Cheque;
        this.cd_Bq = cd_Bq;
        this.cd_Ag = cd_Ag;
        this.nm_ivext_Emis = nm_emis;
        this.dt_Emis = dt_Emis;
        this.dt_Recu = dt_Recu;
        this.mt_Cheq = mt_Cheq;
        this.no_Cpt = no_Cpt;
        this.idcompense = idcompense;
    }

    public IBUS_CHQ_SOUM(BigDecimal no_Ex, String cd_Scpta, BigDecimal id_Op, BigDecimal mt_Op, String ref_Cheque, String cd_Bq, String cd_Ag, String nm_emis, Date dt_Emis, Date dt_Recu, BigDecimal mt_Cheq, String no_Cpt, BigDecimal idcompense, String app_src, String app_dest, String flux) {
        this.no_Ex = no_Ex;
        this.cd_Scpta = cd_Scpta;
        this.id_Op = id_Op;
        this.mt_Op = mt_Op;
        this.ref_Cheque = ref_Cheque;
        this.cd_Bq = cd_Bq;
        this.cd_Ag = cd_Ag;
        this.nm_ivext_Emis = nm_emis;
        this.dt_Emis = dt_Emis;
        this.dt_Recu = dt_Recu;
        this.mt_Cheq = mt_Cheq;
        this.no_Cpt = no_Cpt;
        this.idcompense = idcompense;
        this.app_src = app_src;
        this.app_dest = app_dest;
        this.flux = flux;
    }

    


   
}
