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
public class RIO {

    private String idEmetteur;
    private String pac;
    private String devise;
    private String refRemise;
    private Date datePresentation;
    private String refBancaireLot;
    private String refOperation;
    private String currentLine;
    private String rio;

    public RIO(){
        
    }
    
    
    public RIO(String line) {
        rio = currentLine = line;
     
        setIdEmetteur(getChamp(5));
        setPac(getChamp(3));
        setDevise(getChamp(3));
        setRefRemise(getChamp(3));
        setDatePresentation(Utility.convertStringToDate(getChamp(8),"yyyyMMdd"));
        setRefBancaireLot(getChamp(5));
        setRefOperation(getChamp(8));
    }
    
    public String getChamp(int longueur){
        String result = currentLine.substring(0, longueur);
        currentLine = currentLine.substring(longueur);
        return result;
        
    }
    public String getIdEmetteur() {
        return idEmetteur;
    }

    public void setIdEmetteur(String idEmetteur) {
        this.idEmetteur = idEmetteur;
    }

    public String getPac() {
        return pac;
    }

    public void setPac(String pac) {
        this.pac = pac;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
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

    public String getRefBancaireLot() {
        return refBancaireLot;
    }

    public void setRefBancaireLot(String refBancaireLot) {
        this.refBancaireLot = Utility.bourrageGZero(refBancaireLot, 5);
    }

    public String getRefOperation() {
        return refOperation;
    }

    public void setRefOperation(String refOperation) {
        this.refOperation = Utility.bourrageGZero(refOperation, 8);
    }

    public String getRio() {
        return rio;
    }
    
    @Override
    public String toString(){
        return rio;
    }
}
