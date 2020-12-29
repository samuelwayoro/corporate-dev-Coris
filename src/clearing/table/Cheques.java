/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table;

import java.math.BigDecimal;


/**
 *
 * @author Patrick
 */

public class Cheques  {
    private String etablissement;
    private BigDecimal idcheque = null;
    private BigDecimal origine;
    private String numerocheque;
    private String ribcompte;
    private String numerocompte;
    private String compteremettant;
    private String montantcheque;
    private String devise;
    private String nombeneficiaire;
    private String pathimage;
    private String fichierimage;
    private String dateemission;
    private String dateescompte;
    private BigDecimal etat;
    private String datesaisie;
    private String heuresaisie;
    private String rio;
    private String iban;
    private String motifrejet;
    private String reference_Operation_Interne;
    private BigDecimal remise;
    private BigDecimal lotcom;
    private BigDecimal remcom;
    private String banqueremettant;
    private String agenceremettant;
    private String villeremettant;
    private String datetraitement;
    private String heuretraitement;
    private BigDecimal indicateurmodificationcmc7;
    private String banque;
    private String agence;
    private String ville;
    private String codecertification;
    private BigDecimal etatimage;
    private String reference_Operation_Rejet;
    private String datecompensation;
    private String rio_Rejet;
    private String type_Cheque;
    private String codeutilisateur;
    private BigDecimal sequence;
    private String machinescan;
    private String nomemetteur;
    private String fichiermaili;
    private String refremise;
    private BigDecimal escompte;
    private BigDecimal coderepresentation;
    private BigDecimal lotsib;
    private String codeVignetteUV;
    private String numeroEndos;
    private String garde;
    private String calcul;
    private String dateecheance;
//    private String fichierCat;
 

    public Cheques() {
    }

     public Cheques(Cheques_his cheque) {
        this.idcheque = cheque.getIdcheque();
        this.etablissement = cheque.getEtablissement();
        this.origine = cheque.getOrigine();
        this.numerocheque = cheque.getNumerocheque();
        this.ribcompte = cheque.getRibcompte();
        this.numerocompte = cheque.getNumerocompte();
        this.compteremettant = cheque.getCompteremettant();
        this.montantcheque = cheque.getMontantcheque();
        this.devise = cheque.getDevise();
        this.nombeneficiaire = cheque.getNombeneficiaire();
        this.pathimage = cheque.getPathimage();
        this.fichierimage = cheque.getFichierimage();
        this.dateemission = cheque.getDateemission();
        this.dateescompte = cheque.getDateescompte();
        this.etat = cheque.getEtat();
        this.datesaisie = cheque.getDatesaisie();
        this.heuresaisie = cheque.getHeuresaisie();
        this.rio = cheque.getRio();
        this.iban = cheque.getIban();
        this.motifrejet = cheque.getMotifrejet();
        this.reference_Operation_Interne = cheque.getReference_Operation_Interne();
        this.remise = cheque.getRemise();
        this.lotcom = cheque.getLotcom();
        this.remcom = cheque.getRemcom();
        this.banqueremettant = cheque.getBanqueremettant();
        this.agenceremettant = cheque.getAgenceremettant();
        this.datetraitement = cheque.getDatetraitement();
        this.heuretraitement = cheque.getHeuretraitement();
        this.indicateurmodificationcmc7 = cheque.getIndicateurmodificationcmc7();
        this.banque = cheque.getBanque();
        this.agence = cheque.getAgence();
        this.ville = cheque.getVille();
        this.codecertification = cheque.getCodecertification();
        this.etatimage = cheque.getEtatimage();
        this.reference_Operation_Rejet = cheque.getReference_Operation_Rejet();
        this.datecompensation = cheque.getDatecompensation();
        this.rio_Rejet = cheque.getRio_Rejet();
        this.type_Cheque = cheque.getType_Cheque();
        this.codeutilisateur = cheque.getCodeutilisateur();
        this.sequence = cheque.getSequence();
        this.machinescan = cheque.getMachinescan();
        this.nomemetteur = cheque.getNomemetteur();
        this.fichiermaili = cheque.getFichiermaili();
        this.refremise = cheque.getRefremise();
        this.escompte = cheque.getEscompte();
        this.coderepresentation = cheque.getCoderepresentation();
        this.lotsib = cheque.getLotsib();
        this.codeVignetteUV = cheque.getCodeVignetteUV();
        this.numeroEndos = cheque.getNumeroEndos();
        this.garde = cheque.getGarde();
        this.calcul = cheque.getCalcul();
        this.dateecheance = cheque.getDateecheance();
    }

    public String getGarde() {
        return garde;
    }

    public void setGarde(String garde) {
        this.garde = garde;
    }

    public String getCalcul() {
        return calcul;
    }

    public void setCalcul(String calcul) {
        this.calcul = calcul;
    }

    public String getDateecheance() {
        return dateecheance;
    }

    public void setDateecheance(String dateecheance) {
        this.dateecheance = dateecheance;
    }

    
    public String getCodeVignetteUV() {
        return codeVignetteUV;
    }

    public void setCodeVignetteUV(String codeVignetteUV) {
        this.codeVignetteUV = codeVignetteUV;
    }

    public String getNumeroEndos() {
        return numeroEndos;
    }

    public void setNumeroEndos(String numeroEndos) {
        this.numeroEndos = numeroEndos;
    }

    
    public BigDecimal getLotsib() {
        return lotsib;
    }

    public void setLotsib(BigDecimal lotsib) {
        this.lotsib = lotsib;
    }

    
    public BigDecimal getCoderepresentation() {
        return coderepresentation;
    }

    public void setCoderepresentation(BigDecimal coderepresentation) {
        this.coderepresentation = coderepresentation;
    }

    
    public BigDecimal getEscompte() {
        return escompte;
    }

    public void setEscompte(BigDecimal escompte) {
        this.escompte = escompte;
    }

    public String getRefremise() {
        return refremise;
    }

    public void setRefremise(String refremise) {
        this.refremise = refremise;
    }

    
    public String getFichiermaili() {
        return fichiermaili;
    }

    public void setFichiermaili(String fichiermaili) {
        this.fichiermaili = fichiermaili;
    }

    
    public String getNomemetteur() {
        return nomemetteur;
    }

    public void setNomemetteur(String nomemetteur) {
        this.nomemetteur = nomemetteur;
    }

    
    public String getMachinescan() {
        return machinescan;
    }

    public void setMachinescan(String machinescan) {
        this.machinescan = machinescan;
    }

    public BigDecimal getSequence() {
        return sequence;
    }

    public void setSequence(BigDecimal sequence) {
        this.sequence = sequence;
    }

    public String getCompteremettant() {
        return compteremettant;
    }

    public void setCompteremettant(String compteremettant) {
        this.compteremettant = compteremettant;
    }

    
    public String getCodeutilisateur() {
        return codeutilisateur;
    }

    public void setCodeutilisateur(String codeutilisateur) {
        this.codeutilisateur = codeutilisateur;
    }

    public Cheques(BigDecimal idcheque) {
        this.idcheque = idcheque;
    }

    public String getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(String etablissement) {
        this.etablissement = etablissement;
    }

    public BigDecimal getIdcheque() {
        return idcheque;
    }

    public void setIdcheque(BigDecimal idcheque) {
        this.idcheque = idcheque;
    }

    public BigDecimal getOrigine() {
        return origine;
    }

    public void setOrigine(BigDecimal origine) {
        this.origine = origine;
    }

    public String getNumerocheque() {
        return numerocheque;
    }

    public void setNumerocheque(String numerocheque) {
        this.numerocheque = numerocheque;
    }

    public String getRibcompte() {
        return ribcompte;
    }

    public void setRibcompte(String zoneinterbancaire) {
        this.ribcompte = zoneinterbancaire;
    }

    public String getNumerocompte() {
        return numerocompte;
    }

    public void setNumerocompte(String numerocompte) {
        this.numerocompte = numerocompte;
    }

    public String getMontantcheque() {
        return montantcheque;
    }

    public void setMontantcheque(String montantcheque) {
        this.montantcheque = montantcheque;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getNombeneficiaire() {
        if(nombeneficiaire == null) nombeneficiaire =""; 
        return nombeneficiaire;
    }

    public void setNombeneficiaire(String nombeneficiaire) {
        this.nombeneficiaire = nombeneficiaire;
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

    public String getDateemission() {
        return dateemission;
    }

    public void setDateemission(String dateemission) {
        this.dateemission = dateemission;
    }

    public String getDateescompte() {
        return dateescompte;
    }

    public void setDateescompte(String dateescompte) {
        this.dateescompte = dateescompte;
    }

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
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

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
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

    public void setReference_Operation_Interne(String referenceOperationInterne) {
        this.reference_Operation_Interne = referenceOperationInterne;
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

    public String getVilleremettant() {
        return villeremettant;
    }

    public void setVilleremettant(String villeremettant) {
        this.villeremettant = villeremettant;
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

    public BigDecimal getIndicateurmodificationcmc7() {
        return indicateurmodificationcmc7;
    }

    public void setIndicateurmodificationcmc7(BigDecimal indicateurmodificationcmc7) {
        this.indicateurmodificationcmc7 = indicateurmodificationcmc7;
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
        if(ville!= null && !ville.isEmpty()) return ville;
        return "01";
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCodecertification() {
        return codecertification;
    }

    public void setCodecertification(String codecertification) {
        this.codecertification = codecertification;
    }

    public BigDecimal getEtatimage() {
        return etatimage;
    }

    public void setEtatimage(BigDecimal etatimage) {
        this.etatimage = etatimage;
    }

    public String getReference_Operation_Rejet() {
        return reference_Operation_Rejet;
    }

    public void setReference_Operation_Rejet(String referenceOperationRejet) {
        this.reference_Operation_Rejet = referenceOperationRejet;
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

    public void setRio_Rejet(String rioRejet) {
        this.rio_Rejet = rioRejet;
    }

    public String getType_Cheque() {
        return type_Cheque;
    }

    public void setType_Cheque(String typeCheque) {
        this.type_Cheque = typeCheque;
    }

    @Override
    public String toString() {
        return "clearing.table.Cheques[idcheque=" + idcheque + "]";
    }

    

}
