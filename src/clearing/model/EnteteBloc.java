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
public class EnteteBloc {

    private String idEntete;
    private Date dateReglement;
    private String nbEnreg;
    private String solde;
    private String signe;
    private String blancs;
    public  Enreg enregs[];

    public String getIdEntete() {
        return idEntete;
    }

    public void setIdEntete(String idEntete) {
        this.idEntete = idEntete;
    }

    public Date getDateReglement() {
        return dateReglement;
    }

    public void setDateReglement(Date dateReglement) {
        this.dateReglement = dateReglement;
    }

    public String getNbEnreg() {
        return nbEnreg;
    }

    public void setNbEnreg(String nbEnreg) {
        this.nbEnreg = nbEnreg;
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
}
