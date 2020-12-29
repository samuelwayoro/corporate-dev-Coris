/*
 * Utilisateur.java
 *
 * Created on 11 f�vrier 2006, 14:43
 */

package clearing.table;

import java.math.BigDecimal;
import java.sql.Timestamp;


/**
 *
 * @author Administrateur
 */
public class Utilisateurs {
    private BigDecimal etat;
    private BigDecimal poids;
    private String nom;
    private String prenom;
    private String courriel;
    private String adresse;
    private Timestamp date_inscrit;
    private String login;
    private String password;
    private String ucreation;
    private String umodification;
    private String dcreation;
    private String dmodification;
    /** Creates a new instance of Utilisateur */
    public Utilisateurs() {
    }

    public Utilisateurs(BigDecimal poids, String nom, String prenom, String courriel, String adresse, Timestamp date_inscrit, String login, String password) {
        this.poids = poids;
        this.nom = nom;
        this.prenom = prenom;
        this.courriel = courriel;
        this.adresse = adresse;
        this.date_inscrit = date_inscrit;
        this.login = login;
        this.password = password;
    }

    public String getUcreation() {
        return ucreation;
    }

    public void setUcreation(String ucreation) {
        this.ucreation = ucreation;
    }

    public String getUmodification() {
        return umodification;
    }

    public void setUmodification(String umodification) {
        this.umodification = umodification;
    }

    public String getDcreation() {
        return dcreation;
    }

    public void setDcreation(String dcreation) {
        this.dcreation = dcreation;
    }

    public String getDmodification() {
        return dmodification;
    }

    public void setDmodification(String dmodification) {
        this.dmodification = dmodification;
    }

    
    
    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }


    
    public BigDecimal getPoids() {
        return poids;
    }

    public void setPoids(BigDecimal poids) {
        this.poids = poids;
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

    public String getCourriel() {
        return courriel;
    }

    public void setCourriel(String courriel) {
        this.courriel = courriel;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Timestamp getDate_inscrit() {
        return date_inscrit;
    }

    public void setDate_inscrit(Timestamp date_inscrit) {
        this.date_inscrit = date_inscrit;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
}
