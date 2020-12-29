/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 *
 * @author AUGOU Patrick
 */

public class Vignettes implements Serializable {
   
    private String numerosequence;
    
    private String numerocommande;
  
    private String datecommande;
  
    private String fournisseur;
  
    private String numerocheque;

    private String numeroendos;
 
    private String numeroserie;
   
    private String codebanque;
  
    private String codeguichet;
   
    private String numerocompte;
   
    private String clerib;
 
    private String payable1;
   
    private String payable2;

    private String infoclient1;
   
    private String infoclient2;
   
    private String codevignette;
  
    private String datecreation;
   
    private String datemaj;
   
    private BigDecimal etat;
   
    private String usermaj;
   
    private BigDecimal idvignette;
  
    private String datereception;
    
    private String producteur;
   
    private String machineproduction;
   
    private String dateproduction;
   
    private String editeur;
    
    private String dateedition;
   
    private String typeimprime;
  
    private String codebarrement;
  
    private String etatreprise;

    private String codereprise;
   
    private String libellebarrement;
  
    private String libelletypeimprime;
    
    private String libellereprise;

    private String libellecodereprise;
   
    private String nbrchqcarnet;
    
    private String libellefixe;
  
    private String numdernierchq;
  
    private String payable3;
   
    private String payable4;
   
    private String payable5;
   
    private String infoclient3;
  
    private String infoclient4;
   
    private String infoclient5;

    public Vignettes() {
    }

    public Vignettes(String codevignette) {
        this.codevignette = codevignette;
    }

    public Vignettes(String codevignette, BigDecimal idvignette) {
        this.codevignette = codevignette;
        this.idvignette = idvignette;
    }

    public String getNumerosequence() {
        return numerosequence;
    }

    public void setNumerosequence(String numerosequence) {
        this.numerosequence = numerosequence;
    }

    public String getNumerocommande() {
        return numerocommande;
    }

    public void setNumerocommande(String numerocommande) {
        this.numerocommande = numerocommande;
    }

    public String getDatecommande() {
        return datecommande;
    }

    public void setDatecommande(String datecommande) {
        this.datecommande = datecommande;
    }

    public String getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(String fournisseur) {
        this.fournisseur = fournisseur;
    }

    public String getNumerocheque() {
        return numerocheque;
    }

    public void setNumerocheque(String numerocheque) {
        this.numerocheque = numerocheque;
    }

    public String getNumeroendos() {
        return numeroendos;
    }

    public void setNumeroendos(String numeroendos) {
        this.numeroendos = numeroendos;
    }

    public String getNumeroserie() {
        return numeroserie;
    }

    public void setNumeroserie(String numeroserie) {
        this.numeroserie = numeroserie;
    }

    public String getCodebanque() {
        return codebanque;
    }

    public void setCodebanque(String codebanque) {
        this.codebanque = codebanque;
    }

    public String getCodeguichet() {
        return codeguichet;
    }

    public void setCodeguichet(String codeguichet) {
        this.codeguichet = codeguichet;
    }

    public String getNumerocompte() {
        return numerocompte;
    }

    public void setNumerocompte(String numerocompte) {
        this.numerocompte = numerocompte;
    }

    public String getClerib() {
        return clerib;
    }

    public void setClerib(String clerib) {
        this.clerib = clerib;
    }

    public String getPayable1() {
        return payable1;
    }

    public void setPayable1(String payable1) {
        this.payable1 = payable1;
    }

    public String getPayable2() {
        return payable2;
    }

    public void setPayable2(String payable2) {
        this.payable2 = payable2;
    }

    public String getInfoclient1() {
        return infoclient1;
    }

    public void setInfoclient1(String infoclient1) {
        this.infoclient1 = infoclient1;
    }

    public String getInfoclient2() {
        return infoclient2;
    }

    public void setInfoclient2(String infoclient2) {
        this.infoclient2 = infoclient2;
    }

    public String getCodevignette() {
        return codevignette;
    }

    public void setCodevignette(String codevignette) {
        this.codevignette = codevignette;
    }

    public String getDatecreation() {
        return datecreation;
    }

    public void setDatecreation(String datecreation) {
        this.datecreation = datecreation;
    }

    public String getDatemaj() {
        return datemaj;
    }

    public void setDatemaj(String datemaj) {
        this.datemaj = datemaj;
    }

    

    public String getUsermaj() {
        return usermaj;
    }

    public void setUsermaj(String usermaj) {
        this.usermaj = usermaj;
    }

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }

    public BigDecimal getIdvignette() {
        return idvignette;
    }

    public void setIdvignette(BigDecimal idvignette) {
        this.idvignette = idvignette;
    }

  

    public String getDatereception() {
        return datereception;
    }

    public void setDatereception(String datereception) {
        this.datereception = datereception;
    }

    public String getProducteur() {
        return producteur;
    }

    public void setProducteur(String producteur) {
        this.producteur = producteur;
    }

    public String getMachineproduction() {
        return machineproduction;
    }

    public void setMachineproduction(String machineproduction) {
        this.machineproduction = machineproduction;
    }

    public String getDateproduction() {
        return dateproduction;
    }

    public void setDateproduction(String dateproduction) {
        this.dateproduction = dateproduction;
    }

    public String getEditeur() {
        return editeur;
    }

    public void setEditeur(String editeur) {
        this.editeur = editeur;
    }

    public String getDateedition() {
        return dateedition;
    }

    public void setDateedition(String dateedition) {
        this.dateedition = dateedition;
    }

    public String getCodebarrement() {
        return codebarrement;
    }

    public void setCodebarrement(String codebarrement) {
        this.codebarrement = codebarrement;
    }

    public String getEtatreprise() {
        return etatreprise;
    }

    public void setEtatreprise(String etatreprise) {
        this.etatreprise = etatreprise;
    }

    public String getTypeimprime() {
        return typeimprime;
    }

    public void setTypeimprime(String typeimprime) {
        this.typeimprime = typeimprime;
    }



    public String getCodereprise() {
        return codereprise;
    }

    public void setCodereprise(String codereprise) {
        this.codereprise = codereprise;
    }

    public String getLibellebarrement() {
        return libellebarrement;
    }

    public void setLibellebarrement(String libellebarrement) {
        this.libellebarrement = libellebarrement;
    }

    public String getLibelletypeimprime() {
        return libelletypeimprime;
    }

    public void setLibelletypeimprime(String libelletypeimprime) {
        this.libelletypeimprime = libelletypeimprime;
    }

    public String getLibellereprise() {
        return libellereprise;
    }

    public void setLibellereprise(String libellereprise) {
        this.libellereprise = libellereprise;
    }

    public String getLibellecodereprise() {
        return libellecodereprise;
    }

    public void setLibellecodereprise(String libellecodereprise) {
        this.libellecodereprise = libellecodereprise;
    }

    public String getNbrchqcarnet() {
        return nbrchqcarnet;
    }

    public void setNbrchqcarnet(String nbrchqcarnet) {
        this.nbrchqcarnet = nbrchqcarnet;
    }

    public String getLibellefixe() {
        return libellefixe;
    }

    public void setLibellefixe(String libellefixe) {
        this.libellefixe = libellefixe;
    }

    public String getNumdernierchq() {
        return numdernierchq;
    }

    public void setNumdernierchq(String numdernierchq) {
        this.numdernierchq = numdernierchq;
    }

    public String getPayable3() {
        return payable3;
    }

    public void setPayable3(String payable3) {
        this.payable3 = payable3;
    }

    public String getPayable4() {
        return payable4;
    }

    public void setPayable4(String payable4) {
        this.payable4 = payable4;
    }

    public String getPayable5() {
        return payable5;
    }

    public void setPayable5(String payable5) {
        this.payable5 = payable5;
    }

    public String getInfoclient3() {
        return infoclient3;
    }

    public void setInfoclient3(String infoclient3) {
        this.infoclient3 = infoclient3;
    }

    public String getInfoclient4() {
        return infoclient4;
    }

    public void setInfoclient4(String infoclient4) {
        this.infoclient4 = infoclient4;
    }

    public String getInfoclient5() {
        return infoclient5;
    }

    public void setInfoclient5(String infoclient5) {
        this.infoclient5 = infoclient5;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codevignette != null ? codevignette.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vignettes)) {
            return false;
        }
        Vignettes other = (Vignettes) object;
        if ((this.codevignette == null && other.codevignette != null) || (this.codevignette != null && !this.codevignette.equals(other.codevignette))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clearing.model.Vignettes[codevignette=" + codevignette + "]";
    }

}
