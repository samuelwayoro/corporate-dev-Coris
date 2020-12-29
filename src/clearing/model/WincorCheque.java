/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.model;

/**
 *
 * @author Patrick
 */
public class WincorCheque {
    private String num;
    private String cmc7;
    private String cmc7ok;
    private String dateO;
    private String agence;
    private String atm;
    private String compteR;
    private String remise;
    private String numBRemise   ;
    private String dteServer;
    private String image;

    public WincorCheque() {
    }

    public WincorCheque(String num, String cmc7, String cmc7ok, String dateO, String agence, String atm, String compteR, String remise, String numBRemise, String dteServer, String image) {
        this.num = num;
        this.cmc7 = cmc7;
        this.cmc7ok = cmc7ok;
        this.dateO = dateO;
        this.agence = agence;
        this.atm = atm;
        this.compteR = compteR;
        this.remise = remise;
        this.numBRemise = numBRemise;
        this.dteServer = dteServer;
        this.image = image;
    }


    
    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getAtm() {
        return atm;
    }

    public void setAtm(String atm) {
        this.atm = atm;
    }

    public String getCmc7() {
        return cmc7;
    }

    public void setCmc7(String cmc7) {
        this.cmc7 = cmc7;
    }

    public String getCmc7ok() {
        return cmc7ok;
    }

    public void setCmc7ok(String cmc7ok) {
        this.cmc7ok = cmc7ok;
    }

    public String getCompteR() {
        return compteR;
    }

    public void setCompteR(String compteR) {
        this.compteR = compteR;
    }

    public String getDateO() {
        return dateO;
    }

    public void setDateO(String dateO) {
        this.dateO = dateO;
    }

    public String getDteServer() {
        return dteServer;
    }

    public void setDteServer(String dteServer) {
        this.dteServer = dteServer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getNumBRemise() {
        return numBRemise;
    }

    public void setNumBRemise(String numBRemise) {
        this.numBRemise = numBRemise;
    }

    public String getRemise() {
        return remise;
    }

    public void setRemise(String remise) {
        this.remise = remise;
    }





}
