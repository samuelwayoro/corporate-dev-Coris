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

public class Icsibtc2 implements Serializable {

 
    private String statucpt;

    private BigDecimal actionsb;
 
    private BigDecimal actionst;

    private String libelle;

    public Icsibtc2() {
    }

    public Icsibtc2(String statucpt) {
        this.statucpt = statucpt;
    }

    public String getStatucpt() {
        return statucpt;
    }

    public void setStatucpt(String statucpt) {
        this.statucpt = statucpt;
    }

    public BigDecimal getActionsb() {
        return actionsb;
    }

    public void setActionsb(BigDecimal actionsb) {
        this.actionsb = actionsb;
    }

    public BigDecimal getActionst() {
        return actionst;
    }

    public void setActionst(BigDecimal actionst) {
        this.actionst = actionst;
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
        hash += (statucpt != null ? statucpt.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Icsibtc2)) {
            return false;
        }
        Icsibtc2 other = (Icsibtc2) object;
        if ((this.statucpt == null && other.statucpt != null) || (this.statucpt != null && !this.statucpt.equals(other.statucpt))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clearing.table.delta.Icsibtc2[statucpt=" + statucpt + "]";
    }

}
