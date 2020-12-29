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
public class Profils {
    private String libelleprofil;
    private String nomprofil;
    private BigDecimal idprofil;
    private String regtache;
    private String ucreation;
    private String umodification;
    private String dcreation;
    private String dmodification;
    private BigDecimal etat;

    public Profils() {
    }

    
    public BigDecimal getIdprofil() {
        return idprofil;
    }

    public void setIdprofil(BigDecimal idprofil) {
        this.idprofil = idprofil;
    }

    public String getLibelleprofil() {
        return libelleprofil;
    }

    public void setLibelleprofil(String libelleprofil) {
        this.libelleprofil = libelleprofil;
    }

    public String getNomprofil() {
        return nomprofil;
    }

    public void setNomprofil(String nomprofil) {
        this.nomprofil = nomprofil;
    }

    public String getRegtache() {
        return regtache;
    }

    public void setRegtache(String regtache) {
        this.regtache = regtache;
    }

    public String getUcreation() {
        return ucreation;
    }

    public void setUcreation(String ucreation) {
        this.ucreation = ucreation;
    }

    public String getUmodification() {
        return umodification;
    }

    public void setUmodification(String umodification) {
        this.umodification = umodification;
    }

    public String getDcreation() {
        return dcreation;
    }

    public void setDcreation(String dcreation) {
        this.dcreation = dcreation;
    }

    public String getDmodification() {
        return dmodification;
    }

    public void setDmodification(String dmodification) {
        this.dmodification = dmodification;
    }

    public BigDecimal getEtat() {
        return etat;
    }

    public void setEtat(BigDecimal etat) {
        this.etat = etat;
    }

}
