/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author BOUIKS
 */
public class Defi implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fldnam;

    private java.math.BigDecimal indfld;

    private Date valdat;

    private String valstr;

    private java.math.BigDecimal vallon;

    private BigDecimal valcur;

    private String flddes;

    private Date datsai;

    private java.math.BigDecimal usesai;

    private Date datmod;

    private java.math.BigDecimal usemod;

    private Date datanu;

    private java.math.BigDecimal useanu;

    public Defi() {
    }

    public Defi(String fldnam) {
        this.fldnam = fldnam;
    }

    public String getFldnam() {
        return fldnam;
    }

    public void setFldnam(String fldnam) {
        this.fldnam = fldnam;
    }
 
    public Date getValdat() {
        return valdat;
    }

    public void setValdat(Date valdat) {
        this.valdat = valdat;
    }

    public String getValstr() {
        return valstr;
    }

    public void setValstr(String valstr) {
        this.valstr = valstr;
    }

   

    public BigDecimal getValcur() {
        return valcur;
    }

    public void setValcur(BigDecimal valcur) {
        this.valcur = valcur;
    }

    public String getFlddes() {
        return flddes;
    }

    public void setFlddes(String flddes) {
        this.flddes = flddes;
    }

    public Date getDatsai() {
        return datsai;
    }

    public void setDatsai(Date datsai) {
        this.datsai = datsai;
    }

   

    public Date getDatmod() {
        return datmod;
    }

    public void setDatmod(Date datmod) {
        this.datmod = datmod;
    }

   

    public Date getDatanu() {
        return datanu;
    }

    public void setDatanu(Date datanu) {
        this.datanu = datanu;
    }

    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (fldnam != null ? fldnam.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Defi)) {
            return false;
        }
        Defi other = (Defi) object;
        if ((this.fldnam == null && other.fldnam != null) || (this.fldnam != null && !this.fldnam.equals(other.fldnam))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entites.Defi[ fldnam=" + fldnam + " ]";
    }

    public java.math.BigDecimal getIndfld() {
        return indfld;
    }

    public void setIndfld(java.math.BigDecimal indfld) {
        this.indfld = indfld;
    }

    public java.math.BigDecimal getVallon() {
        return vallon;
    }

    public void setVallon(java.math.BigDecimal vallon) {
        this.vallon = vallon;
    }

    public java.math.BigDecimal getUseanu() {
        return useanu;
    }

    public void setUseanu(java.math.BigDecimal useanu) {
        this.useanu = useanu;
    }

    public java.math.BigDecimal getUsesai() {
        return usesai;
    }

    public void setUsesai(java.math.BigDecimal usesai) {
        this.usesai = usesai;
    }

    public java.math.BigDecimal getUsemod() {
        return usemod;
    }

    public void setUsemod(java.math.BigDecimal usemod) {
        this.usemod = usemod;
    }

}
