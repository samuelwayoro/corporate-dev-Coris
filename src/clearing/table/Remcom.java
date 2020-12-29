/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 *
 * @author Patrick
 */
public class Remcom  {
    private String refremise;
    private String refremrelatif;
    private String coderejet;
    private Timestamp datepresentation;
    private String idrecepteur;
    private String idemetteur;
    private String devise;
    private String typeremise;
    private String nblots;
    private String seance;
    private String flaginversion;
    private BigDecimal idremcom;
    private BigDecimal nbtotremallenv;
    private BigDecimal nbtotremallacctot;
    private BigDecimal nbtotremallaccpar;
    private BigDecimal nbtotremallrejtot;
    private BigDecimal nbtotremallann;
    private BigDecimal nbtotoperval;
    private String idDestinataire;
private BigDecimal etat;

    public Remcom() {
    }

    public Remcom(BigDecimal idremcom) {
        this.idremcom = idremcom;
    }

    public String getIdDestinataire() {
        return idDestinataire;
    }

    public void setIdDestinataire(String idDestinataire) {
        this.idDestinataire = idDestinataire;
    }

    
    public String getRefremise() {
        return refremise;
    }

    public void setRefremise(String refremise) {
        this.refremise = refremise;
    }

    public String getRefremrelatif() {
        return refremrelatif;
    }

    public void setRefremrelatif(String refremrelatif) {
        this.refremrelatif = refremrelatif;
    }

    public String getCoderejet() {
        return coderejet;
    }

    public void setCoderejet(String coderejet) {
        this.coderejet = coderejet;
    }

    public Timestamp getDatepresentation() {
        return datepresentation;
    }

    public void setDatepresentation(Timestamp datepresentation) {
        this.datepresentation = datepresentation;
    }

    public String getIdrecepteur() {
        return idrecepteur;
    }

    public void setIdrecepteur(String idrecepteur) {
        this.idrecepteur = idrecepteur;
    }

    public String getIdemetteur() {
        return idemetteur;
    }

    public void setIdemetteur(String idemetteur) {
        this.idemetteur = idemetteur;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getTyperemise() {
        return typeremise;
    }

    public void setTyperemise(String typeremise) {
        this.typeremise = typeremise;
    }

    public String getNblots() {
        return nblots;
    }

    public void setNblots(String nblots) {
        this.nblots = nblots;
    }

    public String getSeance() {
        return seance;
    }

    public void setSeance(String seance) {
        this.seance = seance;
    }

    public String getFlaginversion() {
        return flaginversion;
    }

    public void setFlaginversion(String flaginversion) {
        this.flaginversion = flaginversion;
    }

    public BigDecimal getIdremcom() {
        return idremcom;
    }

    public void setIdremcom(BigDecimal idremcom) {
        this.idremcom = idremcom;
    }

    public BigDecimal getNbtotremallenv() {
        return nbtotremallenv;
    }

    public void setNbtotremallenv(BigDecimal nbtotremallenv) {
        this.nbtotremallenv = nbtotremallenv;
    }

    public BigDecimal getNbtotremallacctot() {
        return nbtotremallacctot;
    }

    public void setNbtotremallacctot(BigDecimal nbtotremallacctot) {
        this.nbtotremallacctot = nbtotremallacctot;
    }

    public BigDecimal getNbtotremallaccpar() {
        return nbtotremallaccpar;
    }

    public void setNbtotremallaccpar(BigDecimal nbtotremallaccpar) {
        this.nbtotremallaccpar = nbtotremallaccpar;
    }

    public BigDecimal getNbtotremallrejtot() {
        return nbtotremallrejtot;
    }

    public void setNbtotremallrejtot(BigDecimal nbtotremallrejtot) {
        this.nbtotremallrejtot = nbtotremallrejtot;
    }

    public BigDecimal getNbtotremallann() {
        return nbtotremallann;
    }

    public void setNbtotremallann(BigDecimal nbtotremallann) {
        this.nbtotremallann = nbtotremallann;
    }

    public BigDecimal getNbtotoperval() {
        return nbtotoperval;
    }

    public void setNbtotoperval(BigDecimal nbtotoperval) {
        this.nbtotoperval = nbtotoperval;
    }

    public String toString() {
        return "clearing.table.Remcom[idremcom=" + idremcom + "]";
    }

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }

}
