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

public class Remises {
        
    private BigDecimal idremise;
    
    private String agenceRemettant;
    
    private String compteRemettant;
    
    private String agenceDepot;
    
    private BigDecimal nbOperation;
    
    private String montant;
    
    private String devise;
    
    private String nomClient;
    
    private BigDecimal lotAgence;
    
    private BigDecimal sequence;
    
    private BigDecimal etat;
    
    private String dateSaisie;

    private String heureSaisie;

    private String typeRemise;

    private String etablissement;
    
    private String pathimage;
    
    private String fichierimage;
    
    private String nomUtilisateur;

    private String machinescan;
    
    private String remarques;
   
    private BigDecimal noex;

    private BigDecimal idop;

    private String typeinter;

    private String cdscpta;

    private String reference;

    private BigDecimal escompte;

    private String valideur;

    public Remises() {
    }

    public Remises(BigDecimal idremise) {
        this.idremise = idremise;
    }

    public Remises(BigDecimal idremise, String agenceRemettant, String compteRemettant, String agenceDepot, BigDecimal nbOperation, String montant, String devise, String nomClient, BigDecimal lotAgence, BigDecimal sequence, BigDecimal etat, String dateSaisie, String heureSaisie, String typeRemise, String etablissement) {
        this.idremise = idremise;
        this.agenceRemettant = agenceRemettant;
        this.compteRemettant = compteRemettant;
        this.agenceDepot = agenceDepot;
        this.nbOperation = nbOperation;
        this.montant = montant;
        this.devise = devise;
        this.nomClient = nomClient;
        this.lotAgence = lotAgence;
        this.sequence = sequence;
        this.etat = etat;
        this.dateSaisie = dateSaisie;
        this.heureSaisie = heureSaisie;
        this.typeRemise = typeRemise;
        this.etablissement = etablissement;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }

    
    public BigDecimal getEscompte() {
        return escompte;
    }

    public void setEscompte(BigDecimal escompte) {
        this.escompte = escompte;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getValideur() {
        return valideur;
    }

    public void setValideur(String valideur) {
        this.valideur = valideur;
    }

    

     public String getCdscpta() {
        return cdscpta;
    }

    public void setCdscpta(String cdscpta) {
        this.cdscpta = cdscpta;
    }

    public BigDecimal getIdop() {
        return idop;
    }

    public void setIdop(BigDecimal idop) {
        this.idop = idop;
    }

    public BigDecimal getNoex() {
        return noex;
    }

    public void setNoex(BigDecimal noex) {
        this.noex = noex;
    }

    public String getTypeinter() {
        return typeinter;
    }

    public void setTypeinter(String typeinter) {
        this.typeinter = typeinter;
    }

    public String getMachinescan() {
        return machinescan;
    }

    public void setMachinescan(String machinescan) {
        this.machinescan = machinescan;
    }

    
    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    
    public String getFichierimage() {
        return fichierimage;
    }

    public void setFichierimage(String fichierimage) {
        this.fichierimage = fichierimage;
    }

    public String getPathimage() {
        return pathimage;
    }

    public void setPathimage(String pathimage) {
        this.pathimage = pathimage;
    }

    
    public BigDecimal getIdremise() {
        return idremise;
    }

    public void setIdremise(BigDecimal idremise) {
        this.idremise = idremise;
    }

    public String getAgenceRemettant() {
        return agenceRemettant;
    }

    public void setAgenceRemettant(String agenceRemettant) {
        this.agenceRemettant = agenceRemettant;
    }

    public String getCompteRemettant() {
        return compteRemettant;
    }

    public void setCompteRemettant(String compteRemettant) {
        this.compteRemettant = compteRemettant;
    }

    public String getAgenceDepot() {
        return agenceDepot;
    }

    public void setAgenceDepot(String agenceDepot) {
        this.agenceDepot = agenceDepot;
    }

    public BigDecimal getNbOperation() {
        return nbOperation;
    }

    public void setNbOperation(BigDecimal nbOperation) {
        this.nbOperation = nbOperation;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        
           this.nomClient = Utility.bourrageDroite(nomClient, 35, " ");
       
    }

    public BigDecimal getLotAgence() {
        return lotAgence;
    }

    public void setLotAgence(BigDecimal lotAgence) {
        this.lotAgence = lotAgence;
    }

    public BigDecimal getSequence() {
        return sequence;
    }

    public void setSequence(BigDecimal sequence) {
        this.sequence = sequence;
    }

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }

    public String getDateSaisie() {
        return dateSaisie;
    }

    public void setDateSaisie(String dateSaisie) {
        this.dateSaisie = dateSaisie;
    }

    public String getHeureSaisie() {
        return heureSaisie;
    }

    public void setHeureSaisie(String heureSaisie) {
        this.heureSaisie = heureSaisie;
    }

    public String getTypeRemise() {
        return typeRemise;
    }

    public void setTypeRemise(String typeRemise) {
        this.typeRemise = typeRemise;
    }

    public String getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(String etablissement) {
        this.etablissement = etablissement;
    }

   

}
