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

public class IBUS_CHQ_COMP implements Serializable {
    
   
    private BigDecimal idcompense;
  
    private String fg_Etat;
   
    private Date dt_Compense;
       private String app_src;

    private String app_dest;

    private String flux;

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


    public IBUS_CHQ_COMP() {
    }

    public IBUS_CHQ_COMP(BigDecimal idcompense) {
        this.idcompense = idcompense;
    }

    public IBUS_CHQ_COMP(BigDecimal idcompense, String fgEtat, Date dtCompense) {
        this.idcompense = idcompense;
        this.fg_Etat = fgEtat;
        this.dt_Compense = dtCompense;
    }

    public IBUS_CHQ_COMP(BigDecimal idcompense, String fg_Etat, Date dt_Compense, String app_src, String app_dest, String flux) {
        this.idcompense = idcompense;
        this.fg_Etat = fg_Etat;
        this.dt_Compense = dt_Compense;
        this.app_src = app_src;
        this.app_dest = app_dest;
        this.flux = flux;
    }

    
    public Date getDt_Compense() {
        return dt_Compense;
    }

    public void setDt_Compense(Date dt_Compense) {
        this.dt_Compense = dt_Compense;
    }

    public String getFg_Etat() {
        return fg_Etat;
    }

    public void setFg_Etat(String fg_Etat) {
        this.fg_Etat = fg_Etat;
    }

    public BigDecimal getIdcompense() {
        return idcompense;
    }

    public void setIdcompense(BigDecimal idcompense) {
        this.idcompense = idcompense;
    }

  
}
