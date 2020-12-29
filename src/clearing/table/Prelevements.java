/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table;

import java.math.BigDecimal;
import org.patware.utils.Utility;



/**
 *
 * @author Patrick
 */
public class Prelevements {
    private String etablissement;
    
    private String dateFinRecyclage;
    
    private BigDecimal idprelevement = null;
    
    private String type_prelevement;
    
    private BigDecimal etat;
    
    private BigDecimal origine;
    
    private String numeroprelevement;
    
    private String zoneinterbancaire_Tire;
    
    private String numerocompte_Tire;
    
    private String montantprelevement;
    
    private String nom_Tire;
    
    private String adresse_Tire;
    
    private String libelle;
    
    private String devise;
    
    private String zoneinterbancaire_Beneficiaire;
    
    private String numerocompte_Beneficiaire;
    
    private String nom_Beneficiaire;
    
    private String adresse_Beneficiaire;
   
    private String dateordre;
   
    private String rio;
    
    private String iban_Tire;
  
    private String iban_Beneficiaire;
    
    private String motifrejet;
    
    private String reference_Operation_Interne;
    
    private BigDecimal remise;
    
    private BigDecimal lotcom;
    
    private BigDecimal lotsib;


    private BigDecimal remcom;
    
    private String datetraitement;
    
    private String heuretraitement;
    
    private String reference_Operation_Rejet;
    
    private String reference;
    
    private String reference_Emetteur;

    private String banque;

    private String agence;

    private String ville;

    private String datecompensation;

    private String rio_Rejet;
    
    private String banqueremettant;
    
    private String agenceremettant;
    
    

    public Prelevements() {
    }

    public Prelevements(BigDecimal idprelevement) {
        this.idprelevement = idprelevement;
    }

    public void setIdprelevement(BigDecimal idprelevement) {
        this.idprelevement = idprelevement;
    }

    public String getDateFinRecyclage() {
        return dateFinRecyclage;
    }

    public void setDateFinRecyclage(String dateFinRecyclage) {
        this.dateFinRecyclage = dateFinRecyclage;
    }

    

    public String getMontantprelevement() {
        return montantprelevement;
    }

    public void setMontantprelevement(String montantprelevement) {
        this.montantprelevement = montantprelevement;
    }

    public String getNumeroprelevement() {
        return numeroprelevement;
    }

    public void setNumeroprelevement(String numeroprelevement) {
        this.numeroprelevement = numeroprelevement;
    }

    public String getType_prelevement() {
        return type_prelevement;
    }

    public void setType_prelevement(String type_prelevement) {
        this.type_prelevement = type_prelevement;
    }

    

     public BigDecimal getLotsib() {
        return lotsib;
    }

    public void setLotsib(BigDecimal lotsib) {
        this.lotsib = lotsib;
    }
    
    public String toString() {
        return "clearing.table.Virements[idvirement=" + getIdprelevement() + "]";
    }

    public BigDecimal getIdprelevement() {
        return idprelevement;
    }

   

    public String getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(String etablissement) {
        this.etablissement = etablissement;
    }

  
    public String getAgenceremettant() {
        return agenceremettant;
    }

    public void setAgenceremettant(String agenceremettant) {
        this.agenceremettant = agenceremettant;
    }

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }

    public BigDecimal getOrigine() {
        return origine;
    }

    public void setOrigine(BigDecimal origine) {
        this.origine = origine;
    }

  
    public String getZoneinterbancaire_Tire() {
        return zoneinterbancaire_Tire;
    }

    public void setZoneinterbancaire_Tire(String zoneinterbancaire_Tire) {
        this.zoneinterbancaire_Tire = zoneinterbancaire_Tire;
    }

    public String getNumerocompte_Tire() {
        return numerocompte_Tire;
    }

    public void setNumerocompte_Tire(String numerocompte_Tire) {
        if(numerocompte_Tire.length()<=10)
            this.numerocompte_Tire = Utility.bourrageGZero(numerocompte_Tire, 10);
        else
            this.numerocompte_Tire = Utility.bourrageGZero(numerocompte_Tire, 12);
    }

  

    public String getNom_Tire() {
        return nom_Tire;
    }

    public void setNom_Tire(String nom_Tire) {
        this.nom_Tire = Utility.removeAccent(nom_Tire);
    }

    public String getAdresse_Tire() {
        return adresse_Tire;
    }

    public void setAdresse_Tire(String adresse_Tire) {
        this.adresse_Tire = adresse_Tire;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = Utility.bourrageDroite(Utility.removeAccent(libelle), 50, " ");
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getZoneinterbancaire_Beneficiaire() {
        return zoneinterbancaire_Beneficiaire;
    }

    public void setZoneinterbancaire_Beneficiaire(String zoneinterbancaire_Beneficiaire) {
        this.zoneinterbancaire_Beneficiaire = zoneinterbancaire_Beneficiaire;
    }

    public String getNumerocompte_Beneficiaire() {
        return numerocompte_Beneficiaire;
    }

    public void setNumerocompte_Beneficiaire(String numerocompte_Beneficiaire) {
        if(numerocompte_Beneficiaire.length()<=10)
            this.numerocompte_Beneficiaire = Utility.bourrageGZero(numerocompte_Beneficiaire, 10);
        else
            this.numerocompte_Beneficiaire = Utility.bourrageGZero(numerocompte_Beneficiaire, 12);
    }

    public String getNom_Beneficiaire() {
        return nom_Beneficiaire;
    }

    public void setNom_Beneficiaire(String nom_Beneficiaire) {
        this.nom_Beneficiaire = Utility.removeAccent(nom_Beneficiaire);
    }

    public String getAdresse_Beneficiaire() {
        return adresse_Beneficiaire;
    }

    public void setAdresse_Beneficiaire(String adresse_Beneficiaire) {
        this.adresse_Beneficiaire = adresse_Beneficiaire;
    }

    public String getDateordre() {
        return dateordre;
    }

    public void setDateordre(String dateordre) {
        this.dateordre = dateordre;
    }

    public String getRio() {
        return rio;
    }

    public void setRio(String rio) {
        this.rio = rio;
    }

    public String getIban_Tire() {
        return iban_Tire;
    }

    public void setIban_Tire(String iban_Tire) {
        this.iban_Tire = iban_Tire;
    }

    public String getIban_Beneficiaire() {
        return iban_Beneficiaire;
    }

    public void setIban_Beneficiaire(String iban_Beneficiaire) {
        this.iban_Beneficiaire = iban_Beneficiaire;
    }

    public String getMotifrejet() {
        return motifrejet;
    }

    public void setMotifrejet(String motifrejet) {
        this.motifrejet = motifrejet;
    }

    public String getReference_Operation_Interne() {
        return reference_Operation_Interne;
    }

    public void setReference_Operation_Interne(String reference_Operation_Interne) {
        this.reference_Operation_Interne = reference_Operation_Interne;
    }

    public BigDecimal getRemise() {
        return remise;
    }

    public void setRemise(BigDecimal remise) {
        this.remise = remise;
    }

    public BigDecimal getLotcom() {
        return lotcom;
    }

    public void setLotcom(BigDecimal lotcom) {
        this.lotcom = lotcom;
    }

    public BigDecimal getRemcom() {
        return remcom;
    }

    public void setRemcom(BigDecimal remcom) {
        this.remcom = remcom;
    }

    public String getDatetraitement() {
        return datetraitement;
    }

    public void setDatetraitement(String datetraitement) {
        this.datetraitement = datetraitement;
    }

    public String getHeuretraitement() {
        return heuretraitement;
    }

    public void setHeuretraitement(String heuretraitement) {
        this.heuretraitement = heuretraitement;
    }

    public String getReference_Operation_Rejet() {
        return reference_Operation_Rejet;
    }

    public void setReference_Operation_Rejet(String reference_Operation_Rejet) {
        this.reference_Operation_Rejet = reference_Operation_Rejet;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getReference_Emetteur() {
        return reference_Emetteur;
    }

    public void setReference_Emetteur(String reference_Emetteur) {
        this.reference_Emetteur = reference_Emetteur;
    }

    public String getBanque() {
        return banque;
    }

    public void setBanque(String banque) {
        this.banque = banque.toUpperCase();
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getDatecompensation() {
        return datecompensation;
    }

    public void setDatecompensation(String datecompensation) {
        this.datecompensation = datecompensation;
    }

   

    public String getBanqueremettant() {
        return banqueremettant;
    }

    public void setBanqueremettant(String banqueremettant) {
        this.banqueremettant = banqueremettant.toUpperCase();
    }

    public String getRio_Rejet() {
        return rio_Rejet;
    }

    public void setRio_Rejet(String rio_Rejet) {
        this.rio_Rejet = rio_Rejet;
    }

}
