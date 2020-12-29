/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.model;

import java.sql.Date;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class ImageIndex {

    private RIO rio;
    private String typeOperation;
    private String idBanRem;
    private String idAgeRem;
    private String flagIBANDeb;
    private String pfxIBANDeb;
    private String ribDebiteur;
    private String montant;
    private Date dateOrdreClient;
    private String numEffet;
    private String numCheque;
    private String idBanTire;
    private String idAgeTire;
    private String numCptTire;
    private String cleRibDeb;
    private String codeCmc7;
    private String longRecto;
    private String longVerso;
    private String blancs;
    //Corporates
    private String etablissement;
    private String compteRemettant;
    private String nomBeneficiaire;
    private String escompte;
    private String villeRemettant;
    private String codeUtilisateur;
    private String machineScan;
    private String referenceRemise;
    private String referenceExterne;
    private String nbrOperRemise;
    private String montantRemise;


    public String getMontantRemise() {
        return montantRemise;
    }

    public void setMontantRemise(String montantRemise) {
        this.montantRemise = Utility.bourrageGZero(montantRemise, 16);
    }

    public String getNbrOperRemise() {
        return nbrOperRemise;
    }

    public void setNbrOperRemise(String nbrOperRemise) {
        this.nbrOperRemise = Utility.bourrageGZero(nbrOperRemise, 4);
    }

    public String getCodeUtilisateur() {
        return codeUtilisateur;
    }

    public void setCodeUtilisateur(String codeUtilisateur) {
        this.codeUtilisateur = Utility.bourrageDroite(codeUtilisateur, 10, " ");
    }

    public String getCompteRemettant() {
        return compteRemettant;
    }

    public void setCompteRemettant(String compteRemettant) {
        this.compteRemettant = compteRemettant;
    }

    public String getEscompte() {
        return escompte;
    }

    public void setEscompte(String escompte) {
        this.escompte = escompte;
    }

    public String getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(String etablissement) {
        this.etablissement = Utility.bourrageDroite(etablissement, 10, " ");
    }

    public String getMachineScan() {
        return machineScan;
    }

    public void setMachineScan(String machineScan) {
        this.machineScan = Utility.bourrageDroite(machineScan, 15, " ");
    }

    public String getNomBeneficiaire() {
        return nomBeneficiaire;
    }

    public void setNomBeneficiaire(String nomBeneficiaire) {
        this.nomBeneficiaire = Utility.bourrageDroite(nomBeneficiaire, 35, " ");
    }

    public String getReferenceExterne() {
        return referenceExterne;
    }

    public void setReferenceExterne(String referenceExterne) {
        this.referenceExterne = Utility.bourrageDroite(referenceExterne, 25," ");
    }

    public String getReferenceRemise() {
        return referenceRemise;
    }

    public void setReferenceRemise(String referenceRemise) {
        this.referenceRemise = Utility.bourrageDroite(referenceRemise, 10," ");
    }

    public String getVilleRemettant() {
        return villeRemettant;
    }

    public void setVilleRemettant(String villeRemettant) {
        this.villeRemettant = villeRemettant;
    }

    


    public String getLongRecto() {
        return Utility.bourrageGZero(longRecto, 9);
    }

    public void setLongRecto(String longRecto) {
        this.longRecto = longRecto;
    }

    public String getLongVerso() {
        return Utility.bourrageGZero(longVerso, 9);
    }

    public void setLongVerso(String longVerso) {
        this.longVerso = longVerso;
    }

    public String getCodeCmc7() {
        return codeCmc7;
    }

    public void setCodeCmc7(String codeCmc7) {
        this.codeCmc7 = codeCmc7;
    }

    public String getIdAgeTire() {
        return idAgeTire;
    }

    public void setIdAgeTire(String idAgeTire) {
        this.idAgeTire = idAgeTire;
    }

    public String getIdBanRem() {
        return idBanRem;
    }

    public void setIdBanRem(String idBanRem) {
        this.idBanRem = idBanRem;
    }

    public String getIdBanTire() {
        return idBanTire;
    }

    public void setIdBanTire(String idBanTire) {
        this.idBanTire = idBanTire;
    }

    public String getNumCptTire() {
        return numCptTire;
    }

    public void setNumCptTire(String numCptTire) {
        this.numCptTire = numCptTire;
    }

    public String getNumEffet() {
        //return Utility.bourrageGZero(numEffet, 10);
        return Utility.bourrageGZero(numEffet, 7);
    }

    public void setNumEffet(String numEffet) {
        this.numEffet = numEffet;
    }

    public RIO getRio() {
        return rio;
    }

    public void setRio(RIO rio) {
        this.rio = rio;
    }

    public String getTypeOperation() {
        return typeOperation;
    }

    public void setTypeOperation(String typeOperation) {
        this.typeOperation = Utility.bourrageGZero(typeOperation, 3);
    }

    public String getIdAgeRem() {
        return idAgeRem;
    }

    public void setIdAgeRem(String idAgeRem) {
        this.idAgeRem = Utility.bourrageGZero(idAgeRem, 5);
    }

    public String getFlagIBANDeb() {
        return flagIBANDeb;
    }

    public void setFlagIBANDeb(String flagIBANDeb) {
        this.flagIBANDeb = flagIBANDeb;
    }

    public String getPfxIBANDeb() {
       return Utility.bourrageGauche(pfxIBANDeb, 4," ");
    }

    public void setPfxIBANDeb(String pfxIBANDeb) {
        this.pfxIBANDeb = pfxIBANDeb;
    }

    public String getRibDebiteur() {
        return ribDebiteur;
    }

    public void setRibDebiteur(String ribDebiteur) {
        this.ribDebiteur = Utility.bourrageGZero(ribDebiteur, 24);
    }

    public String getMontant() {
        return Utility.bourrageGZero(montant, 16);
    }

    public void setMontant(String montant) {
        this.montant = Utility.bourrageGZero(montant, 16);
    }

    public Date getDateOrdreClient() {
        return dateOrdreClient;
    }

    public void setDateOrdreClient(Date dateOrdreClient) {
        this.dateOrdreClient = dateOrdreClient;
    }

    public String getBlancs() {
        return blancs;
    }

    public void setBlancs(String blancs) {
        this.blancs = blancs;
    }

    public String getNumCheque() {
        return numCheque;
    }

    public void setNumCheque(String numCheque) {
        this.numCheque = Utility.bourrageGZero(numCheque, 7);
    }

    public String getCleRibDeb() {
        return cleRibDeb;
    }

    public void setCleRibDeb(String cleRibDeb) {
        this.cleRibDeb = Utility.bourrageGZero(cleRibDeb, 2);
    }

    public String toString() {
        return (" LongRecto:" + getLongRecto() +
                " LongVerso:" + getLongVerso() +
                " Type Oper:" + getTypeOperation() +
                " Bantire :" + getIdBanTire() +
                " Agetire :" + getIdAgeTire() +
                " Ncpttire :" + getNumCptTire() +
                " Ribdeb :" + getCleRibDeb() +
                " Banrem :" + getIdBanRem() +
                " Agerem :" + getIdAgeRem() +
                " Montant :" + String.valueOf(Long.parseLong(getMontant())));
    }
}
