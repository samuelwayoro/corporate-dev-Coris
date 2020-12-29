/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table.ibus;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 *
 * @author Patrick Augou
 */

public class IBUS_OPE_GUI implements Serializable {
 
    private BigDecimal no_Ex;

    private String cd_Scpta;

    private BigDecimal id_Op;

    private BigDecimal mt_Op;

    private String type_Inter;
 
    private String nm_Inter;

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


    public IBUS_OPE_GUI() {
    }

    public IBUS_OPE_GUI(BigDecimal no_Ex) {
        this.no_Ex = no_Ex;
    }

    public IBUS_OPE_GUI(BigDecimal no_Ex, String cd_Scpta, BigDecimal id_Op, BigDecimal mt_Op, String type_Inter, String nm_Inter, String app_src, String app_dest, String flux) {
        this.no_Ex = no_Ex;
        this.cd_Scpta = cd_Scpta;
        this.id_Op = id_Op;
        this.mt_Op = mt_Op;
        this.type_Inter = type_Inter;
        this.nm_Inter = nm_Inter;
        this.app_src = app_src;
        this.app_dest = app_dest;
        this.flux = flux;
    }

    
    public BigDecimal getNo_Ex() {
        return no_Ex;
    }

    public void setNo_Ex(BigDecimal no_Ex) {
        this.no_Ex = no_Ex;
    }

    public String getCd_Scpta() {
        return cd_Scpta;
    }

    public void setCd_Scpta(String cd_Scpta) {
        this.cd_Scpta = cd_Scpta;
    }

    public BigDecimal getId_Op() {
        return id_Op;
    }

    public void setId_Op(BigDecimal id_Op) {
        this.id_Op = id_Op;
    }

    public BigDecimal getMt_Op() {
        return mt_Op;
    }

    public void setMt_Op(BigDecimal mt_Op) {
        this.mt_Op = mt_Op;
    }

    public String getType_Inter() {
        return type_Inter;
    }

    public void setType_Inter(String type_Inter) {
        this.type_Inter = type_Inter;
    }

    public String getNm_Inter() {
        return nm_Inter;
    }

    public void setNm_Inter(String nm_Inter) {
        this.nm_Inter = nm_Inter;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (no_Ex != null ? no_Ex.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IBUS_OPE_GUI)) {
            return false;
        }
        IBUS_OPE_GUI other = (IBUS_OPE_GUI) object;
        if ((this.no_Ex == null && other.no_Ex != null) || (this.no_Ex != null && !this.no_Ex.equals(other.no_Ex))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clearing.table.ibus.Fluopegui[noEx=" + no_Ex + "]";
    }

}
