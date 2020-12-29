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
public class Effets {
    
    private String etablissement;
    private BigDecimal ideffet = null;
    private String type_Effet;
    
    private BigDecimal origine;
    
    private String numeroeffet;
    
    private String date_Echeance;
    
    private String date_Creation;
    
    private String zoneinterbancaire_Tire;
    
    private String numerocompte_Tire;
    
    private String nom_Tire;
    
    private String adresse_Tire;
    
    private String identification_Tire;
    
    private String devise;
    
    private String montant_Effet;
   
    private String montant_Brut;
    
    private String code_Frais;
   
    private String montant_Frais;
    
    private String zoneinterbancaire_Beneficiaire;
    
    private String numerocompte_Beneficiaire;
    
    private String nom_Beneficiaire;
  
    private String adresse_Beneficiaire;
   
    private BigDecimal etat;
  
    private String code_Acceptation;
    
    private String code_Endossement;
    
    private String code_Aval;
    
    private String numero_Cedant;
    
    private String datesaisie;
    
    private String heuresaisie;
    
    private String rio;
    
    private String iban_Tire;
   
    private String iban_Beneficiaire;
    
    private String motifrejet;
    
    private String reference_Operation_Rejet;
    
    private BigDecimal remise;
    
    private BigDecimal lotcom;
        
    private BigDecimal remcom;
    
    private BigDecimal lotsib;
   
    private BigDecimal escompte;

   
    private String datetraitement;
    
    private String heuretraitement;
    
    private String banque;
    
    private String agence;
    
    private String ville;
    
    private BigDecimal etatimage;
    
    private String pathimage;
    
    private String fichierimage;
    
    private String banqueremettant;
    
    private String agenceremettant;
    
    private String machinescan;
    
    private BigDecimal indeximages;
    
    private String datecompensation;
    
    private String rio_Rejet;

    private String codeUtilisateur;

    private String valideur;
    
  
    
    private String protet;
    
    
    //eugene 
    private String ribcompte;
    private BigDecimal sequence;
    private String refremise;
    private BigDecimal indicateurmodificationcmc7;
    private BigDecimal coderepresentation;
    private String fichiermaili;

    public String getFichiermaili() {
        return fichiermaili;
    }

    public void setFichiermaili(String fichiermaili) {
        this.fichiermaili = fichiermaili;
    }
    
    

    public BigDecimal getCoderepresentation() {
        return coderepresentation;
    }

    public void setCoderepresentation(BigDecimal coderepresentation) {
        this.coderepresentation = coderepresentation;
    }
    
    

    public BigDecimal getIndicateurmodificationcmc7() {
        return indicateurmodificationcmc7;
    }

    public void setIndicateurmodificationcmc7(BigDecimal indicateurmodificationcmc7) {
        this.indicateurmodificationcmc7 = indicateurmodificationcmc7;
    }
     
     

    public String getRefremise() {
        return refremise;
    }

    public void setRefremise(String refremise) {
        this.refremise = refremise;
    }
    
    
    
    

    public Effets() {
    }

    public Effets(String etablissement, String type_Effet, BigDecimal origine, String numeroeffet, String date_Echeance, String date_Creation, String zoneinterbancaire_Tire, String numerocompte_Tire, String nom_Tire, String adresse_Tire, String identification_Tire, String devise, String montant_Effet, String montant_Brut, String code_Frais, String montant_Frais, String zoneinterbancaire_Beneficiaire, String numerocompte_Beneficiaire, String nom_Beneficiaire, String adresse_Beneficiaire, BigDecimal etat, String code_Acceptation, String code_Endossement, String code_Aval, String numero_Cedant, String datesaisie, String heuresaisie, String rio, String iban_Tire, String iban_Beneficiaire, String motifrejet, String reference_Operation_Rejet, BigDecimal remise, BigDecimal lotcom, BigDecimal remcom, BigDecimal lotsib, String datetraitement, String heuretraitement, String banque, String agence, String ville, BigDecimal etatimage, String pathimage, String fichierimage, String banqueremettant, String agenceremettant, String machinescan, BigDecimal indeximages, String datecompensation, String rio_Rejet, String codeUtilisateur, String valideur, String protet, String ribcompte, BigDecimal sequence, String refremise, BigDecimal indicateurmodificationcmc7, BigDecimal coderepresentation,String fichiermaili,BigDecimal escompte) {
        this.etablissement = etablissement;
        this.type_Effet = type_Effet;
        this.origine = origine;
        this.numeroeffet = numeroeffet;
        this.date_Echeance = date_Echeance;
        this.date_Creation = date_Creation;
        this.zoneinterbancaire_Tire = zoneinterbancaire_Tire;
        this.numerocompte_Tire = numerocompte_Tire;
        this.nom_Tire = nom_Tire;
        this.adresse_Tire = adresse_Tire;
        this.identification_Tire = identification_Tire;
        this.devise = devise;
        this.montant_Effet = montant_Effet;
        this.montant_Brut = montant_Brut;
        this.code_Frais = code_Frais;
        this.montant_Frais = montant_Frais;
        this.zoneinterbancaire_Beneficiaire = zoneinterbancaire_Beneficiaire;
        this.numerocompte_Beneficiaire = numerocompte_Beneficiaire;
        this.nom_Beneficiaire = nom_Beneficiaire;
        this.adresse_Beneficiaire = adresse_Beneficiaire;
        this.etat = etat;
        this.code_Acceptation = code_Acceptation;
        this.code_Endossement = code_Endossement;
        this.code_Aval = code_Aval;
        this.numero_Cedant = numero_Cedant;
        this.datesaisie = datesaisie;
        this.heuresaisie = heuresaisie;
        this.rio = rio;
        this.iban_Tire = iban_Tire;
        this.iban_Beneficiaire = iban_Beneficiaire;
        this.motifrejet = motifrejet;
        this.reference_Operation_Rejet = reference_Operation_Rejet;
        this.remise = remise;
        this.lotcom = lotcom;
        this.remcom = remcom;
        this.lotsib = lotsib;
        this.datetraitement = datetraitement;
        this.heuretraitement = heuretraitement;
        this.banque = banque;
        this.agence = agence;
        this.ville = ville;
        this.etatimage = etatimage;
        this.pathimage = pathimage;
        this.fichierimage = fichierimage;
        this.banqueremettant = banqueremettant;
        this.agenceremettant = agenceremettant;
        this.machinescan = machinescan;
        this.indeximages = indeximages;
        this.datecompensation = datecompensation;
        this.rio_Rejet = rio_Rejet;
        this.codeUtilisateur = codeUtilisateur;
        this.valideur = valideur;
        this.protet = protet;
        this.ribcompte = ribcompte;
        this.sequence = sequence;
        this.refremise = refremise;
        this.indicateurmodificationcmc7 = indicateurmodificationcmc7;
        this.coderepresentation = coderepresentation;
        this.fichiermaili  = fichiermaili;
        this.escompte = escompte;
    }
    
    
    

    public String getRibcompte() {
        return ribcompte;
    }

    public void setRibcompte(String ribcompte) {
        this.ribcompte = ribcompte;
    }

    public BigDecimal getSequence() {
        return sequence;
    }

    public void setSequence(BigDecimal sequence) {
        this.sequence = sequence;
    }
    
    
    public BigDecimal getEscompte() {
        return escompte;
    }

    public void setEscompte(BigDecimal escompte) {
        this.escompte = escompte;
    }
    

    public Effets(BigDecimal ideffet) {
        this.ideffet = ideffet;
    }

    public String getCodeUtilisateur() {
        return codeUtilisateur;
    }

    public void setCodeUtilisateur(String codeUtilisateur) {
        this.codeUtilisateur = codeUtilisateur;
    }

    public String getValideur() {
        return valideur;
    }

    public void setValideur(String valideur) {
        this.valideur = valideur;
    }

    
    public String getMachinescan() {
        return machinescan;
    }

    public void setMachinescan(String machinescan) {
        this.machinescan = machinescan;
    }


    
    public String getProtet() {
        return protet;
    }

    public void setProtet(String protet) {
        this.protet = protet;
    }


   
    @Override
    public String toString() {
        return "clearing.table.Effets[ideffet=" + getIdeffet() + "]";
    }

    public BigDecimal getIdeffet() {
        return ideffet;
    }

    public void setIdeffet(BigDecimal ideffet) {
        this.ideffet = ideffet;
    }

    public String getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(String etablissement) {
        this.etablissement = etablissement;
    }

    public String getType_Effet() {
        return type_Effet;
    }

    public void setType_Effet(String type_Effet) {
        this.type_Effet = type_Effet;
    }

    public BigDecimal getOrigine() {
        return origine;
    }

    public void setOrigine(BigDecimal origine) {
        this.origine = origine;
    }

    public String getNumeroeffet() {
        return numeroeffet;
    }

    public void setNumeroeffet(String numeroeffet) {
        //this.numeroeffet = Utility.bourrageGZero(numeroeffet, 10);
        this.numeroeffet = Utility.bourrageGZero(numeroeffet, 7);
    }

    public String getDate_Echeance() {
        return date_Echeance;
    }

    public void setDate_Echeance(String date_Echeance) {
        this.date_Echeance = date_Echeance;
    }

    public String getDate_Creation() {
        return date_Creation;
    }

    public void setDate_Creation(String date_Creation) {
        this.date_Creation = date_Creation;
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
        this.adresse_Tire = Utility.removeAccent(adresse_Tire);
    }

    public String getIdentification_Tire() {
        return identification_Tire;
    }

    public void setIdentification_Tire(String identification_Tire) {
        this.identification_Tire = Utility.removeAccent(identification_Tire);
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getMontant_Effet() {
        return montant_Effet;
    }

    public void setMontant_Effet(String montant_Effet) {
        this.montant_Effet = montant_Effet;
    }

    public String getMontant_Brut() {
        return montant_Brut;
    }

    public void setMontant_Brut(String montant_Brut) {
        this.montant_Brut = montant_Brut;
    }

    public String getCode_Frais() {
        return Utility.bourrageGauche(code_Frais, 2, " ");
    }

    public void setCode_Frais(String code_Frais) {
        this.code_Frais = code_Frais;
    }

    public String getMontant_Frais() {
        return montant_Frais;
    }

    public void setMontant_Frais(String montant_Frais) {
        this.montant_Frais = montant_Frais;
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
        this.numerocompte_Beneficiaire =Utility.bourrageGZero(numerocompte_Beneficiaire, 12);
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

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }

    public String getCode_Acceptation() {
        return code_Acceptation;
    }

    public void setCode_Acceptation(String code_Acceptation) {
        this.code_Acceptation = code_Acceptation;
    }

    public String getCode_Endossement() {
        return code_Endossement;
    }

    public void setCode_Endossement(String code_Endossement) {
        this.code_Endossement = code_Endossement;
    }

    public String getCode_Aval() {
        return code_Aval;
    }

    public void setCode_Aval(String code_Aval) {
        this.code_Aval = code_Aval;
    }

    public String getNumero_Cedant() {
        return numero_Cedant;
    }

    public void setNumero_Cedant(String numero_Cedant) {
        this.numero_Cedant = numero_Cedant;
    }

    public String getDatesaisie() {
        return datesaisie;
    }

    public void setDatesaisie(String datesaisie) {
        this.datesaisie = datesaisie;
    }

    public String getHeuresaisie() {
        return heuresaisie;
    }

    public void setHeuresaisie(String heuresaisie) {
        this.heuresaisie = heuresaisie;
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

    public String getReference_Operation_Rejet() {
        return reference_Operation_Rejet;
    }

    public void setReference_Operation_Rejet(String reference_Operation_Rejet) {
        this.reference_Operation_Rejet = reference_Operation_Rejet;
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


    public BigDecimal getLotsib() {
        return lotsib;
    }

    public void setLotsib(BigDecimal lotsib) {
        this.lotsib = lotsib;
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

    public String getBanque() {
        return banque;
    }

    public void setBanque(String banque) {
        this.banque = banque;
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

    public BigDecimal getEtatimage() {
        return etatimage;
    }

    public void setEtatimage(BigDecimal etatstockage) {
        this.etatimage = etatstockage;
    }

    public String getPathimage() {
        return pathimage;
    }

    public void setPathimage(String pathimage) {
        this.pathimage = pathimage;
    }

    public String getFichierimage() {
        return fichierimage;
    }

    public void setFichierimage(String fichierimage) {
        this.fichierimage = fichierimage;
    }

    public String getBanqueremettant() {
        return banqueremettant;
    }

    public void setBanqueremettant(String banqueremettant) {
        this.banqueremettant = banqueremettant;
    }

    public String getAgenceremettant() {
        return agenceremettant;
    }

    public void setAgenceremettant(String agenceremettant) {
        this.agenceremettant = agenceremettant;
    }

  

    public BigDecimal getIndeximages() {
        return indeximages;
    }

    public void setIndeximages(BigDecimal indeximages) {
        this.indeximages = indeximages;
    }

    public String getDatecompensation() {
        return datecompensation;
    }

    public void setDatecompensation(String datecompensation) {
        this.datecompensation = datecompensation;
    }

    public String getRio_Rejet() {
        return rio_Rejet;
    }

    public void setRio_Rejet(String rio_Rejet) {
        this.rio_Rejet = rio_Rejet;
    }

}
