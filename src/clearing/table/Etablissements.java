/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table;

import java.io.Serializable;
import java.math.BigDecimal;



/**
 *
 * @author AUGOU Patrick
 */

public class Etablissements implements Serializable {
    
    private String codeetablissement;
    
    private String libelleetablissement;
    
    private BigDecimal etat;
    
    private BigDecimal algo;
    
    private String admin;
    
    private String datecreation;
    
    private String usercreation;
    
    private String datemaj;
    
    private String usermaj;

    public Etablissements() {
    }

    public Etablissements(String codeetablissement) {
        this.codeetablissement = codeetablissement;
    }

    public String getCodeetablissement() {
        return codeetablissement;
    }

    public void setCodeetablissement(String codeetablissement) {
        this.codeetablissement = codeetablissement;
    }

    public String getLibelleetablissement() {
        return libelleetablissement;
    }

    public void setLibelleetablissement(String libelleetablissement) {
        this.libelleetablissement = libelleetablissement;
    }

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }

    public BigDecimal getAlgo() {
        return algo;
    }

    public void setAlgo(BigDecimal algo) {
        this.algo = algo;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getDatecreation() {
        return datecreation;
    }

    public void setDatecreation(String datecreation) {
        this.datecreation = datecreation;
    }

    public String getUsercreation() {
        return usercreation;
    }

    public void setUsercreation(String usercreation) {
        this.usercreation = usercreation;
    }

    public String getDatemaj() {
        return datemaj;
    }

    public void setDatemaj(String datemaj) {
        this.datemaj = datemaj;
    }

    public String getUsermaj() {
        return usermaj;
    }

    public void setUsermaj(String usermaj) {
        this.usermaj = usermaj;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codeetablissement != null ? codeetablissement.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Etablissements)) {
            return false;
        }
        Etablissements other = (Etablissements) object;
        if ((this.codeetablissement == null && other.codeetablissement != null) || (this.codeetablissement != null && !this.codeetablissement.equals(other.codeetablissement))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clearing.table.Etablissements[codeetablissement=" + codeetablissement + "]";
    }

}
