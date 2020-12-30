/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.web.bean;

/**
 *
 * @author Patrick
 */
public class RejetFormBean {

    private String objet; 
    private String nomIdObjet;
    private String idObjet;
    private String motifRejet;
    
    public RejetFormBean() {
    }

    public RejetFormBean(String objet, String nomIdObjet, String idObjet, String motifRejet) {
        this.objet = objet;
        this.nomIdObjet = nomIdObjet;
        this.idObjet = idObjet;
        this.motifRejet = motifRejet;
    }

    
    public String getIdObjet() {
        return idObjet;
    }

    public void setIdObjet(String idObjet) {
        this.idObjet = idObjet;
    }

    public String getMotifRejet() {
        return motifRejet;
    }

    public void setMotifRejet(String motifRejet) {
        this.motifRejet = motifRejet;
    }

    public String getNomIdObjet() {
        return nomIdObjet;
    }

    public void setNomIdObjet(String nomIdObjet) {
        this.nomIdObjet = nomIdObjet;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }
    

}
