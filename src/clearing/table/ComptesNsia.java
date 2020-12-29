/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.table;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Patrick
 */
public class ComptesNsia implements Serializable {

    private String numero;
    private String agence;
    private BigDecimal gestionnaire1;
    private BigDecimal gestionnaire2;
    private String nom;
    private String prenom;
    private String adresse1;
    private String adresse2;
    private String ville;
    private String pays;
    private String photo;
    private String signature1;
    private String signature2;
    private String signature3;
    private String signature4;
    private String numcptex;
    private BigDecimal etat; //escompte
    private String dateFinEscompte;
    private String dateDebutEscompte;

    public ComptesNsia() {
    }

    public ComptesNsia(String numero) {
        this.numero = numero;
    }

    public String getNumcptex() {
        return numcptex;
    }

    public void setNumcptex(String numcptex) {
        this.numcptex = numcptex;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public BigDecimal getGestionnaire1() {
        return gestionnaire1;
    }

    public void setGestionnaire1(BigDecimal gestionnaire1) {
        this.gestionnaire1 = gestionnaire1;
    }

    public BigDecimal getGestionnaire2() {
        return gestionnaire2;
    }

    public void setGestionnaire2(BigDecimal gestionnaire2) {
        this.gestionnaire2 = gestionnaire2;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAdresse1() {
        return adresse1;
    }

    public void setAdresse1(String adresse1) {
        this.adresse1 = adresse1;
    }

    public String getAdresse2() {
        return adresse2;
    }

    public void setAdresse2(String adresse2) {
        this.adresse2 = adresse2;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getSignature1() {
        return signature1;
    }

    public void setSignature1(String signature1) {
        this.signature1 = signature1;
    }

    public String getSignature2() {
        return signature2;
    }

    public void setSignature2(String signature2) {
        this.signature2 = signature2;
    }

    public String getSignature3() {
        return signature3;
    }

    public void setSignature3(String signature3) {
        this.signature3 = signature3;
    }

    public String getSignature4() {
        return signature4;
    }

    public void setSignature4(String signature4) {
        this.signature4 = signature4;
    }

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof ComptesNsia)) {
            return false;
        }
        ComptesNsia other = (ComptesNsia) object;
        if ((this.numero == null && other.numero != null) || (this.numero != null && !this.numero.equals(other.numero))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.numero != null ? this.numero.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "clearing.table.Comptes[numero=" + numero + "]";
    }

    public String getDateFinEscompte() {
        return dateFinEscompte;
    }

    public void setDateFinEscompte(String dateFinEscompte) {
        this.dateFinEscompte = dateFinEscompte;
    }

    public String getDateDebutEscompte() {
        return dateDebutEscompte;
    }

    public void setDateDebutEscompte(String dateDebutEscompte) {
        this.dateDebutEscompte = dateDebutEscompte;
    }

}
