/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.table.delta;

import java.io.Serializable;

/**
 *
 * @author Patrick Augou
 */
public class SibIccptNSIA implements Serializable {

    private String agence;
    private String service;
    private String ncp;
    private String compte;
    private String nom;
    private String clerib;
    private String typecpt;

    public SibIccptNSIA() {
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.agence != null ? this.agence.hashCode() : 0);
        hash = 97 * hash + (this.service != null ? this.service.hashCode() : 0);
        hash = 97 * hash + (this.ncp != null ? this.ncp.hashCode() : 0);
        hash = 97 * hash + (this.compte != null ? this.compte.hashCode() : 0);
        hash = 97 * hash + (this.nom != null ? this.nom.hashCode() : 0);
        hash = 97 * hash + (this.clerib != null ? this.clerib.hashCode() : 0);
        hash = 97 * hash + (this.typecpt != null ? this.typecpt.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SibIccptNSIA other = (SibIccptNSIA) obj;
        if ((this.agence == null) ? (other.agence != null) : !this.agence.equals(other.agence)) {
            return false;
        }
        if ((this.service == null) ? (other.service != null) : !this.service.equals(other.service)) {
            return false;
        }
        if ((this.ncp == null) ? (other.ncp != null) : !this.ncp.equals(other.ncp)) {
            return false;
        }
        if ((this.compte == null) ? (other.compte != null) : !this.compte.equals(other.compte)) {
            return false;
        }
        if ((this.nom == null) ? (other.nom != null) : !this.nom.equals(other.nom)) {
            return false;
        }
        if ((this.clerib == null) ? (other.clerib != null) : !this.clerib.equals(other.clerib)) {
            return false;
        }
        if ((this.typecpt == null) ? (other.typecpt != null) : !this.typecpt.equals(other.typecpt)) {
            return false;
        }
        return true;
    }
 
 

//    @Override
//    public String toString() {
//        return "clearing.table.delta.SibIccptNSIA[compte=" + compte + "]";
//    }

    @Override
    public String toString() {
        return "SibIccptNSIA{" + "agence=" + agence + ", service=" + service + ", ncp=" + ncp + ", compte=" + compte + ", nom=" + nom + ", clerib=" + clerib + ", typecpt=" + typecpt + '}';
    }

    
    
    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getNcp() {
        return ncp;
    }

    public void setNcp(String ncp) {
        this.ncp = ncp;
    }

    public String getCompte() {
        return compte;
    }

    public void setCompte(String compte) {
        this.compte = compte;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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

}
