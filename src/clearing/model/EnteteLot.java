/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.model;

import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class EnteteLot {

    private String idEntete;
    private String refLot;
    private String refBancaire;
    private String typeOperation;
    private String idBanRem;
    private String nbOperations;
    private String montantTotal;
    private String nbTotOperAcc;
    private String mntTotOperAcc;
    private String codeRejet;
    
    private String blancs;
    public Operation operations[];

   
    

    public String getIdEntete() {
        return idEntete;
    }

    public void setIdEntete(String idEntete) {
        this.idEntete = idEntete;
    }

    public String getRefLot() {
        return refLot;
    }

    public void setRefLot(String refLot) {
        this.refLot = Utility.bourrageGZero(refLot, 3);
    }

    public String getRefBancaire() {
        return refBancaire;
    }

    public void setRefBancaire(String refBancaire) {
        this.refBancaire = Utility.bourrageGZero(refBancaire, 5);
    }

    public String getTypeOperation() {
        return typeOperation;
    }

    public void setTypeOperation(String typeOperation) {
        this.typeOperation = Utility.bourrageGZero(typeOperation, 3);
    }

    public String getIdBanRem() {
        return idBanRem;
    }

    public void setIdBanRem(String idBanRem) {
        this.idBanRem = CMPUtility.getCodeBanqueDestinataire(idBanRem);

    }

    public String getNbOperations() {
        return nbOperations;
    }

    public void setNbOperations(String nbOperations) {
        this.nbOperations = Utility.bourrageGZero(nbOperations, 4);
    }

    public String getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(String montantTotal) {
        this.montantTotal = Utility.bourrageGZero(montantTotal, 16);
    }

    public String getBlancs() {
        return blancs;
    }

    public void setBlancs(String blancs) {
        this.blancs = blancs;
    }

    @Override
    public String toString() {
        return " Entete =" + idEntete + " RefLot =" + refLot + " RefBancaire =" + refBancaire + " TypeOper =" + typeOperation + " Idbanrem =" + idBanRem + " NbOper  =" + nbOperations + " MTT =" + montantTotal + " Blancs =" + blancs;
    }

    public String getNbTotOperAcc() {
        return nbTotOperAcc;
    }

    public void setNbTotOperAcc(String nbTotOperAcc) {
        this.nbTotOperAcc = nbTotOperAcc;
    }

    public String getMntTotOperAcc() {
        return mntTotOperAcc;
    }

    public void setMntTotOperAcc(String mntTotOperAcc) {
        this.mntTotOperAcc = mntTotOperAcc;
    }

    public String getCodeRejet() {
        return codeRejet;
    }

    public void setCodeRejet(String codeRejet) {
        this.codeRejet = codeRejet;
    }
}
