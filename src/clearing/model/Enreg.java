/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.model;

import java.sql.Date;

/**
 *
 * @author Patrick
 */
public class Enreg {

    private String typeOperation;
    private String idBanCon;
    private Date dateReglement;
    private String nbTotOperEmis;
    private String mntTotOperEmis;
    private String nbTotOperRecus;
    private String mntTotOperRecus;
    private String signe;
    private String solde;
    private String blancs;

    public String getTypeOperation() {
        return typeOperation;
    }

    public void setTypeOperation(String typeOperation) {
        this.typeOperation = typeOperation;
    }

    public String getIdBanCon() {
        return idBanCon;
    }

    public void setIdBanCon(String idBanCon) {
        this.idBanCon = idBanCon;
    }

    public Date getDateReglement() {
        return dateReglement;
    }

    public void setDateReglement(Date dateReglement) {
        this.dateReglement = dateReglement;
    }

    public String getNbTotOperEmis() {
        return nbTotOperEmis;
    }

    public void setNbTotOperEmis(String nbTotOperEmis) {
        this.nbTotOperEmis = nbTotOperEmis;
    }

    public String getMntTotOperEmis() {
        return mntTotOperEmis;
    }

    public void setMntTotOperEmis(String mntTotOperEmis) {
        this.mntTotOperEmis = mntTotOperEmis;
    }

    public String getNbTotOperRecus() {
        return nbTotOperRecus;
    }

    public void setNbTotOperRecus(String nbTotOperRecus) {
        this.nbTotOperRecus = nbTotOperRecus;
    }

    public String getMntTotOperRecus() {
        return mntTotOperRecus;
    }

    public void setMntTotOperRecus(String mntTotOperRecus) {
        this.mntTotOperRecus = mntTotOperRecus;
    }
    
    public String getSigne() {
        return signe;
    }

    public void setSigne(String signe) {
        this.signe = signe;
    }

    public String getSolde() {
        return solde;
    }

    public void setSolde(String solde) {
        this.solde = solde;
    }
}
