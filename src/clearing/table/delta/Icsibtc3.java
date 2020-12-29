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

public class Icsibtc3 implements Serializable {
    

    private String statuchq;
    private String dsplage;
    private BigDecimal actions;
    private String libelle;

    public Icsibtc3() {
    }

    public Icsibtc3(String statuchq) {
        this.statuchq = statuchq;
    }

    public String getStatuchq() {
        return statuchq;
    }

    public void setStatuchq(String statuchq) {
        this.statuchq = statuchq;
    }

    public String getDsplage() {
        return dsplage;
    }

    public void setDsplage(String dsplage) {
        this.dsplage = dsplage;
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
        hash += (statuchq != null ? statuchq.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Icsibtc3)) {
            return false;
        }
        Icsibtc3 other = (Icsibtc3) object;
        if ((this.statuchq == null && other.statuchq != null) || (this.statuchq != null && !this.statuchq.equals(other.statuchq))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clearing.table.delta.Icsibtc3[statuchq=" + statuchq + "]";
    }

}
