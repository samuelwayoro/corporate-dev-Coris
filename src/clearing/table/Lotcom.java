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
public class Lotcom {
    
    private BigDecimal etat;
       
    private String reflot;
   
    private String refbancaire;
   
    private String typeoperation;
   
    private String idbanrem;
   
    private String nboperations;
   
    private String montanttotal;
   
    private String nbtotoperacc;
   
    private String mnttotoperacc;
   
    private String coderejet;
    
    private BigDecimal idlotcom;
   
    private BigDecimal idremcom;

    public Lotcom() {
    }

    public Lotcom(BigDecimal idlotcom) {
        this.idlotcom = idlotcom;
    }

    public Lotcom(BigDecimal idlotcom, BigDecimal idremcom) {
        this.idlotcom = idlotcom;
        this.idremcom = idremcom;
    }

    public String getReflot() {
        return reflot;
    }

    public void setReflot(String reflot) {
        this.reflot = reflot;
    }

    public String getRefbancaire() {
        return refbancaire;
    }

    public void setRefbancaire(String refbancaire) {
        this.refbancaire = refbancaire;
    }

    public String getTypeoperation() {
        return typeoperation;
    }

    public void setTypeoperation(String typeoperation) {
        this.typeoperation = typeoperation;
    }

    public String getIdbanrem() {
        return idbanrem;
    }

    public void setIdbanrem(String idbanrem) {
        this.idbanrem = idbanrem;
    }

    public String getNboperations() {
        return nboperations;
    }

    public void setNboperations(String nboperations) {
        this.nboperations = nboperations;
    }

    public String getMontanttotal() {
        return montanttotal;
    }

    public void setMontanttotal(String montanttotal) {
        this.montanttotal = montanttotal;
    }

    public String getNbtotoperacc() {
        return nbtotoperacc;
    }

    public void setNbtotoperacc(String nbtotoperacc) {
        this.nbtotoperacc = nbtotoperacc;
    }

    public String getMnttotoperacc() {
        return mnttotoperacc;
    }

    public void setMnttotoperacc(String mnttotoperacc) {
        this.mnttotoperacc = mnttotoperacc;
    }

    public String getCoderejet() {
        return coderejet;
    }

    public void setCoderejet(String coderejet) {
        this.coderejet = coderejet;
    }

    public BigDecimal getIdlotcom() {
        return idlotcom;
    }

    public void setIdlotcom(BigDecimal idlotcom) {
        this.idlotcom = idlotcom;
    }

    public BigDecimal getIdremcom() {
        return idremcom;
    }

    public void setIdremcom(BigDecimal idremcom) {
        this.idremcom = idremcom;
    }

   
    public String toString() {
        return "clearing.table.Lotcom[idlotcom=" + idlotcom + "]";
    }

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }

}
