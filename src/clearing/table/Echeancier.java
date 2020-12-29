/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table;


import java.math.BigDecimal;
import java.sql.Timestamp;
/**
 *
 * @author Patrick
 */

public class Echeancier  {
    
    private BigDecimal idecheancier;
    private Timestamp datereglement;
    private String solde;
    private String signe;
    private BigDecimal idremcom;

    public Echeancier() {
    }

    public Echeancier(BigDecimal idecheancier) {
        this.idecheancier = idecheancier;
    }

    public BigDecimal getIdecheancier() {
        return idecheancier;
    }

    public void setIdecheancier(BigDecimal idecheancier) {
        this.idecheancier = idecheancier;
    }

    public Timestamp getDatereglement() {
        return datereglement;
    }

    public void setDatereglement(Timestamp datereglement) {
        this.datereglement = datereglement;
    }

    public String getSolde() {
        return solde;
    }

    public void setSolde(String solde) {
        this.solde = solde;
    }

    public String getSigne() {
        return signe;
    }

    public void setSigne(String signe) {
        this.signe = signe;
    }

    public BigDecimal getIdremcom() {
        return idremcom;
    }

    public void setIdremcom(BigDecimal idremcom) {
        this.idremcom = idremcom;
    }

 
    @Override
    public String toString() {
        return "clearing.table.Echeancier[idecheancier=" + idecheancier + "]";
    }

}
