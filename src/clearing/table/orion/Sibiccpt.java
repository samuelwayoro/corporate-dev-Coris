/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table.orion;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Patrick Augou
 */

public class Sibiccpt implements Serializable {
   
  
    private String banque;
    
    private String guichet;

    private String compte;
  
    private String clerib;

    private String typecpt;

    private String statucpt;

    private String titulair;
    
    private String nomabreg;
   
    private String prenom;
    
    private String email;
   
    private String devise;
   
    private String remaescp;
   
    private String ltremesc;
   
    private BigDecimal seuilsgn;
  
    private String datecpt;
 
    private BigDecimal seuilchq;
   
    private String banquev3;
   
    private String cleribv3;

    public Sibiccpt() {
    }

    public Sibiccpt(String compte) {
        this.compte = compte;
    }

    public String getBanque() {
        return banque;
    }

    public void setBanque(String banque) {
        this.banque = banque;
    }

    public String getGuichet() {
        return guichet;
    }

    public void setGuichet(String guichet) {
        this.guichet = guichet;
    }

    public String getCompte() {
        return compte;
    }

    public void setCompte(String compte) {
        this.compte = compte;
    }

    public String getClerib() {
        return clerib;
    }

    public void setClerib(String clerib) {
        this.clerib = clerib;
    }

    public String getTypecpt() {
        return typecpt;
    }

    public void setTypecpt(String typecpt) {
        this.typecpt = typecpt;
    }

    public String getStatucpt() {
        return statucpt;
    }

    public void setStatucpt(String statutcpt) {
        this.statucpt = statutcpt;
    }

    public String getTitulair() {
        return titulair;
    }

    public void setTitulair(String titulair) {
        this.titulair = titulair;
    }

    public String getNomabreg() {
        return nomabreg;
    }

    public void setNomabreg(String nomabreg) {
        this.nomabreg = nomabreg;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getRemaescp() {
        return remaescp;
    }

    public void setRemaescp(String remaescp) {
        this.remaescp = remaescp;
    }

    public String getLtremesc() {
        return ltremesc;
    }

    public void setLtremesc(String ltremesc) {
        this.ltremesc = ltremesc;
    }

    public BigDecimal getSeuilsgn() {
        return seuilsgn;
    }

    public void setSeuilsgn(BigDecimal seuilsgn) {
        this.seuilsgn = seuilsgn;
    }

    public String getDatecpt() {
        return datecpt;
    }

    public void setDatecpt(String datecpt) {
        this.datecpt = datecpt;
    }

    public BigDecimal getSeuilchq() {
        return seuilchq;
    }

    public void setSeuilchq(BigDecimal seuilchq) {
        this.seuilchq = seuilchq;
    }

    
    public String getBanquev3() {
        return banquev3;
    }

    public void setBanquev3(String banquev3) {
        this.banquev3 = banquev3;
    }

    public String getCleribv3() {
        return cleribv3;
    }

    public void setCleribv3(String cleribv3) {
        this.cleribv3 = cleribv3;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (compte != null ? compte.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Sibiccpt)) {
            return false;
        }
        Sibiccpt other = (Sibiccpt) object;
        if ((this.compte == null && other.compte != null) || (this.compte != null && !this.compte.equals(other.compte))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clearing.table.ibus.Sibiccpt[compte=" + compte + "]";
    }

}
