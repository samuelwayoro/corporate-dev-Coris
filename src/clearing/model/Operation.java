/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.model;

import java.math.BigDecimal;
import java.sql.Date;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class Operation {

    private RIO rio;
    private BigDecimal idObjetOrigine;
    private String typeOperation;
    private String refOperation;
    private String idAgeRem;
    private RIO rioOperInitial;
    private String motifRejet;
    private String flagIBANDeb;
    private String pfxIBANDeb;
    private String ribDebiteur;
    private String flagIBANCre;
    private String pfxIBANCre;
    private String ribCrediteur;
    private String montant;
    private String nomDebiteur;
    private String adrDebiteur;
    private String nomCrediteur;
    private String adrCrediteur;
    private String numIntOrdre;
    private Date dateOrdreClient;
    private String libelle;
    private String idBanCre;
    private String idAgeCre;
    private String refEmetteur;
    private String iPac;
    private String numCheque;
    private String idBanDeb;
    private String idAgeDeb;
    private String numCptDeb;
    private String cleRibDeb;
    private String cleRibCre;
    private String codeCertif;
    private Date dateEcheance;
    private String codeFrais;
    private String montantFrais;
    private String montantBrut;
    private String nomSouscripteur;
    private String adrSouscripteur;
    private String codeAcceptation;
    private String refSouscripteur;
    private String codeEndossement;
    private String numCedant;
    private String codeAval;
    private String codeRejet;

    private String protet;
    private String blancs;

    public String getProtet() {
        return protet;
    }

    public void setProtet(String protet) {
        this.protet = protet;
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

    public String getRefOperation() {
        return refOperation;
    }

    public void setRefOperation(String refOperation) {
        this.refOperation = Utility.bourrageGZero(refOperation, 8);
    }

    public String getIdAgeRem() {
        return idAgeRem;
    }

    public void setIdAgeRem(String idAgeRem) {
        this.idAgeRem = Utility.bourrageGZero(idAgeRem, 5);
    }

    public RIO getRioOperInitial() {
        return rioOperInitial;
    }

    public void setRioOperInitial(RIO rioOperInitial) {
        this.rioOperInitial = rioOperInitial;
    }

    public String getMotifRejet() {
        return motifRejet;
    }

    public void setMotifRejet(String motifRejet) {
        this.motifRejet = Utility.bourrageGZero(motifRejet, 3);
    }

    public String toString() {
        return " RIO =" + rio + " TypeOper =" + typeOperation + " RefOper =" + refOperation + " AgceRem =" + idAgeRem + " RIOInitial =" + rioOperInitial + " MotRej  =" + motifRejet + " DateOrdreClient =" + dateOrdreClient + " CodeRej  =" + codeRejet;

    }

    public String getFlagIBANDeb() {
        return flagIBANDeb;
    }

    public void setFlagIBANDeb(String flagIBANDeb) {
        this.flagIBANDeb = flagIBANDeb;
    }

    public String getPfxIBANDeb() {
        return pfxIBANDeb;
    }

    public void setPfxIBANDeb(String pfxIBANDeb) {
        this.pfxIBANDeb = Utility.bourrageGauche(pfxIBANDeb, 4, " ");
    }

    public String getRibDebiteur() {
        return ribDebiteur;
    }

    public void setRibDebiteur(String ribDebiteur) {
        this.ribDebiteur = Utility.bourrageGauche(ribDebiteur, 24, " ");
    }

    public String getFlagIBANCre() {
        return flagIBANCre;
    }

    public void setFlagIBANCre(String flagIBANCre) {
        this.flagIBANCre = flagIBANCre;
    }

    public String getPfxIBANCre() {
        return pfxIBANCre;
    }

    public void setPfxIBANCre(String pfxIBANCre) {
        this.pfxIBANCre = pfxIBANCre;
    }

    public String getRibCrediteur() {
        return ribCrediteur;
    }

    public void setRibCrediteur(String ribCrediteur) {
        this.ribCrediteur = Utility.bourrageGauche(ribCrediteur, 24, " ");
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = Utility.bourrageGZero(montant, 16);
    }

    public String getNomDebiteur() {
        return nomDebiteur;
    }

    public void setNomDebiteur(String nomDebiteur) {
        
        this.nomDebiteur = Utility.bourrageDroite(Utility.removeAccent(nomDebiteur), 35, " ");
        
    }

    public String getAdrDebiteur() {
        return adrDebiteur;
    }

    public void setAdrDebiteur(String adrDebiteur) {
        this.adrDebiteur = Utility.bourrageDroite(Utility.removeAccent(adrDebiteur), 50, " ");
    }

    public String getNomCrediteur() {
        return nomCrediteur;
    }

    public void setNomCrediteur(String nomCrediteur) {

        this.nomCrediteur = Utility.bourrageDroite(Utility.removeAccent(nomCrediteur), 35, " ");

    }

    public String getAdrCrediteur() {
        return adrCrediteur;
    }

    public void setAdrCrediteur(String adrCrediteur) {
        this.adrCrediteur = Utility.bourrageDroite(Utility.removeAccent(adrCrediteur), 50, " ");
    }

    public String getNumIntOrdre() {
        return numIntOrdre;
    }

    public void setNumIntOrdre(String numIntOrdre) {
        this.numIntOrdre = Utility.bourrageGZero(numIntOrdre, 10);
    }

    public Date getDateOrdreClient() {
        return dateOrdreClient;
    }

    public void setDateOrdreClient(Date dateOrdreClient) {
        this.dateOrdreClient = dateOrdreClient;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {

        this.libelle = Utility.bourrageDroite(libelle, 70, " ");

    }

    public String getBlancs() {
        return blancs;
    }

    public void setBlancs(String blancs) {
        this.blancs = blancs;
    }

    public String getIdBanCre() {
        return idBanCre;
    }

    public void setIdBanCre(String idBanCre) {
        this.idBanCre = idBanCre;
    }

    public String getIdAgeCre() {
        return idAgeCre;
    }

    public void setIdAgeCre(String idAgeCre) {
        this.idAgeCre = Utility.bourrageGZero(idAgeCre, 5);
    }

    public String getRefEmetteur() {
        return refEmetteur;
    }

    public void setRefEmetteur(String refEmetteur) {
        this.refEmetteur = refEmetteur;
    }

    public String getIPac() {
        return iPac;
    }

    public void setIPac(String iPac) {
        this.iPac = Utility.bourrageGZero(iPac, 2);
    }

    public String getNumCheque() {
        return numCheque;
    }

    public void setNumCheque(String numCheque) {
        this.numCheque = Utility.bourrageGZero(numCheque, 7);
    }

    public String getIdBanDeb() {
        return idBanDeb;
    }

    public void setIdBanDeb(String idBanDeb) {
        this.idBanDeb = idBanDeb;
    }

    public String getIdAgeDeb() {
        return idAgeDeb;
    }

    public void setIdAgeDeb(String idAgeDeb) {
        this.idAgeDeb = Utility.bourrageGZero(idAgeDeb, 5);
    }

    public String getNumCptDeb() {
        return numCptDeb;
    }

    public void setNumCptDeb(String numCptDeb) {
        this.numCptDeb = Utility.bourrageGZero(numCptDeb, 12);
    }

    public String getCleRibDeb() {
        return cleRibDeb;
    }

    public String getCleRibCre() {
        return cleRibCre;
    }

    public void setCleRibDeb(String cleRibDeb) {
        this.cleRibDeb = Utility.bourrageGZero(cleRibDeb, 2);
    }

    public void setCleRibCre(String cleRibCre) {
        this.cleRibCre = Utility.bourrageGZero(cleRibCre, 2);
    }

    public String getCodeCertif() {

        return codeCertif;
    }

    public void setCodeCertif(String codeCertif) {
        this.codeCertif = Utility.bourrageDroite(codeCertif, 1, " ");
    }

    public Date getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(Date dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public String getCodeFrais() {
        return Utility.bourrageGauche(codeFrais, 2, " ");
    }

    public void setCodeFrais(String codeFrais) {
        this.codeFrais = codeFrais;
    }

    public String getMontantFrais() {
        return montantFrais;
    }

    public void setMontantFrais(String montantFrais) {
        this.montantFrais = Utility.bourrageGZero(montantFrais, 16);
    }

    public String getMontantBrut() {
        return montantBrut;
    }

    public void setMontantBrut(String montantBrut) {
        this.montantBrut = Utility.bourrageGZero(montantBrut, 16);
    }

    public String getNomSouscripteur() {
        return nomSouscripteur;
    }

    public void setNomSouscripteur(String nomSouscripteur) {
        this.nomSouscripteur = Utility.bourrageDroite(Utility.removeAccent(nomSouscripteur), 35, " ");
    }

    public String getAdrSouscripteur() {
        return adrSouscripteur;
    }

    public void setAdrSouscripteur(String adrSouscripteur) {
        this.adrSouscripteur = Utility.bourrageDroite(Utility.removeAccent(adrSouscripteur), 50, " ");
    }

    public String getCodeAcceptation() {
        return codeAcceptation;
    }

    public void setCodeAcceptation(String codeAcceptation) {
        this.codeAcceptation = Utility.bourrageDroite(codeAcceptation, 1, " ");
    }

    public String getRefSouscripteur() {
        return refSouscripteur;
    }

    public void setRefSouscripteur(String refSouscripteur) {
        this.refSouscripteur = Utility.bourrageDroite(refSouscripteur, 12, " ");
    }

    public String getCodeEndossement() {
        return codeEndossement;
    }

    public void setCodeEndossement(String codeEndossement) {
        this.codeEndossement = Utility.bourrageDroite(codeEndossement, 1, " ");
    }

    public String getNumCedant() {
        return numCedant;
    }

    public void setNumCedant(String numCedant) {
        this.numCedant = Utility.bourrageGZero(numCedant, 7);
    }

    public String getCodeAval() {
        return codeAval;
    }

    public void setCodeAval(String codeAval) {
        this.codeAval = Utility.bourrageDroite(codeAval, 1, " ");
    }

    public String getCodeRejet() {
        return codeRejet;
    }

    public void setCodeRejet(String codeRejet) {
        this.codeRejet = codeRejet;
    }

    public BigDecimal getIdObjetOrigine() {
        return idObjetOrigine;
    }

    public void setIdObjetOrigine(BigDecimal idObjetOrigine) {
        this.idObjetOrigine = idObjetOrigine;
    }
}
