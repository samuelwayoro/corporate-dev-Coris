/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table.delta;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 *
 * @author Patrick Augou
 */

public class Icsibtc1 implements Serializable {
  
    private String typecpt;
  
    private BigDecimal actions;
   
    private String libelle;

    public Icsibtc1() {
    }

    public Icsibtc1(String typecpt) {
        this.typecpt = typecpt;
    }

    public String getTypecpt() {
        return typecpt;
    }

    public void setTypecpt(String typecpt) {
        this.typecpt = typecpt;
    }

    public BigDecimal getActions() {
        return actions;
    }

    public void setActions(BigDecimal actions) {
        this.actions = actions;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (typecpt != null ? typecpt.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Icsibtc1)) {
            return false;
        }
        Icsibtc1 other = (Icsibtc1) object;
        if ((this.typecpt == null && other.typecpt != null) || (this.typecpt != null && !this.typecpt.equals(other.typecpt))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clearing.table.delta.Icsibtc1[typecpt=" + typecpt + "]";
    }

}
