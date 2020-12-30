/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.utils.Utility;

/**
 *
 * @author Patrick Augou
 */
public class CompteMessageBean {

    private String action;
    private String actionStatut;
    private String nomClient;
    private String numeroCompte;
    private String agenceCompte[];
    private String agence;
    private String adresseCompte;
    private String escompte;
    private String cleRib;
    private String messageResult;
    private String messageStatutResult;

    public CompteMessageBean() {
    }

    public CompteMessageBean(String action, String nomClient, String numeroCompte, String[] agenceCompte, String adresseCompte, String message) {
        this.agenceCompte = new String[1];
        this.action = action;
        this.nomClient = nomClient;
        this.numeroCompte = numeroCompte;
        this.agenceCompte = agenceCompte;
        this.adresseCompte = adresseCompte;
        this.messageResult = message;

    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getCleRib() {
        return cleRib;
    }

    public void setCleRib(String cleRib) {
        this.cleRib = cleRib;
    }

    public String getMessageStatutResult() {
        return messageStatutResult;
    }

    public void setMessageStatutResult(String messageStatutResult) {
        this.messageStatutResult = messageStatutResult;
    }

    public String getActionStatut() {
        return actionStatut;
    }

    public void setActionStatut(String actionStatut) {
        this.actionStatut = actionStatut;
    }

    public String getEscompte() {
        return escompte;
    }

    public void setEscompte(String escompte) {
        if (escompte == null) {
            escompte = "0";
        }
        this.escompte = escompte;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAdresseCompte() {
        return adresseCompte;
    }

    public void setAdresseCompte(String adresseCompte) {
        this.adresseCompte = adresseCompte;
    }

    public String[] getAgenceCompte() {
        return agenceCompte;
    }

    public void setAgenceCompte(String[] agenceCompte) {
        this.agenceCompte = agenceCompte;
    }

    public String getMessageResult() {
        return messageResult;
    }

    public void setMessageResult(String messageResult) {
        this.messageResult = messageResult;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        //rajouter pour prendre en compte ETG et ses comptes sur 09 pos
        if (numeroCompte.length() == 9) {
            this.numeroCompte = Utility.bourrageGZero(numeroCompte, 9);
        } else {
            this.numeroCompte = Utility.bourrageGZero(numeroCompte, 12);
        }

    }

}
