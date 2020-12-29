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
public class EnteteRemise {

    private String idEntete;
    private String idEmetteur;
    private String idDestinataire;
    private String refRemise;
    private String refRemRelatif;
    private String codeRejet;
    private Date datePresentation;
    private String idRecepeteur;
    private String devise;
    private String typeRemise;
    private String nbLots;
    private String seance;
    private String flagInversion;
    private String nbTotRemAllEnv;
    private String nbTotRemAllAccTot;
    private String nbTotRemAllAccPar;
    private String nbTotRemAllRejTot;
    private String nbTotRemAllAnn;
   

    private String nbTotOperVal;
    private String solde;
    private String signe;
    private String blancs;
    private String codeLieu;
    public EnteteLot enteteLots[];
    public EnteteBloc enteteBlocs[];
    public Enreg enregs[];

  


    
    public String getIdEntete() {
        return idEntete;
    }

    public void setIdEntete(String idEntete) {
        this.idEntete = idEntete;
    }

    public String getIdDestinataire() {
        return idDestinataire;
    }

    public void setIdDestinataire(String idDestinataire) {
        this.idDestinataire = idDestinataire;
    }

    
    public String getIdEmetteur() {
        return idEmetteur;
    }

    public void setIdEmetteur(String idEmetteur) {
        this.idEmetteur = idEmetteur;
    }

    public String getRefRemise() {
        return refRemise;
    }

    public void setRefRemise(String refRemise) {
        
        this.refRemise = Utility.bourrageGZero(refRemise, 3);
    }

    public Date getDatePresentation() {
        return datePresentation;
    }

    public void setDatePresentation(Date datePresentation) {
        this.datePresentation = datePresentation;
    }

    public String getIdRecepeteur() {
        return idRecepeteur;
    }

    public void setIdRecepeteur(String idRecepeteur) {
        this.idRecepeteur = idRecepeteur;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getCodeLieu() {
        return codeLieu;
    }

    public void setCodeLieu(String codeLieu) {
        this.codeLieu = codeLieu;
    }
 
    
    public String getTypeRemise() {
        return typeRemise;
    }

    public void setTypeRemise(String typeRemise) {
        this.typeRemise = typeRemise;
    }

    public String getNbLots() {
        return nbLots;
    }

    public void setNbLots(String nbLots) {
        
        this.nbLots = Utility.bourrageGZero(nbLots, 3);
    }

    public String getBlancs() {
        return blancs;
    }

    public void setBlancs(String blancs) {
        this.blancs = blancs;
    }
    
    @Override
    public String toString(){
        return  " Entete ="+idEntete+" Emetteur ="+idEmetteur+" RefRemise ="+refRemise+" Date ="+datePresentation+" Recepteur ="+idRecepeteur+" Devise ="+devise+" TypeRemise ="+typeRemise+" NbLots ="+nbLots+" CodeRejet ="+codeRejet;
    }

    public String getSeance() {
        return seance;
    }

    public void setSeance(String seance) {
        this.seance = seance;
    }

    public String getFlagInversion() {
        return flagInversion;
    }

    public void setFlagInversion(String flagInversion) {
        this.flagInversion = flagInversion;
    }

    public String getNbTotRemAllEnv() {
        return nbTotRemAllEnv;
    }

    public void setNbTotRemAllEnv(String nbTotRemAllEnv) {
        this.nbTotRemAllEnv = nbTotRemAllEnv;
    }

    public String getNbTotRemAllAccTot() {
        return nbTotRemAllAccTot;
    }

    public void setNbTotRemAllAccTot(String nbTotRemAllAccTot) {
        this.nbTotRemAllAccTot = nbTotRemAllAccTot;
    }

    public String getNbTotRemAllAccPar() {
        return nbTotRemAllAccPar;
    }

    public void setNbTotRemAllAccPar(String nbTotRemAllAccPar) {
        this.nbTotRemAllAccPar = nbTotRemAllAccPar;
    }

    public String getNbTotRemAllRejTot() {
        return nbTotRemAllRejTot;
    }

    public void setNbTotRemAllRejTot(String nbTotRemAllRejTot) {
        this.nbTotRemAllRejTot = nbTotRemAllRejTot;
    }

    public String getNbTotRemAllAnn() {
        return nbTotRemAllAnn;
    }

    public void setNbTotRemAllAnn(String nbTotRemAllAnn) {
        this.nbTotRemAllAnn = nbTotRemAllAnn;
    }

    public String getNbTotOperVal() {
        return Utility.bourrageGZero(nbTotOperVal, 4);
    }

    public void setNbTotOperVal(String nbTotOperVal) {
        this.nbTotOperVal = nbTotOperVal;
    }

    public String getSolde() {
        return solde;
    }

    public void setSolde(String solde) {
        this.solde = solde;
    }

    public String getSigne() {
        return signe;
    }

    public void setSigne(String signe) {
        this.signe = signe;
    }

    public String getRefRemRelatif() {
        return refRemRelatif;
    }

    public void setRefRemRelatif(String refRemRelatif) {
        this.refRemRelatif = Utility.bourrageGZero(refRemRelatif, 3);
    }

    public String getCodeRejet() {
        return codeRejet;
    }

    public void setCodeRejet(String codeRejet) {
        this.codeRejet = codeRejet;
    }
}
