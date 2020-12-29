/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table.delta;

/**
 *
 * @author AUGOU Patrick
 */
public class Bkcom {

    private String agence;
    private String compte;
    private String nom;
    private String clerib;
    private String typecpt;
    private String service;
    


    public Bkcom() {
    }

    

    
    public String getAgence() {
        return agence;
    }

    
    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

   

    public String getClerib() {
        return clerib;
    }

    public void setClerib(String clerib) {
        this.clerib = clerib;
    }

    public String getCompte() {
        return compte;
    }

    public void setCompte(String compte) {
        this.compte = compte;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTypecpt() {
        return typecpt;
    }

    public void setTypecpt(String typecpt) {
        this.typecpt = typecpt;
    }



}
