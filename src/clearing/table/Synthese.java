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
public class Synthese {

    private BigDecimal idremcom;
    private String type;
    private Timestamp datepresentation;
    private String typeoperation;
    private String idbancon;
    private BigDecimal nbtotoperemis;
    private String mnttotoperemis;
    private BigDecimal nbtotoperrecus;
    private String mnttotoperrecus;
    private String solde;
    private String signe;

    public Synthese() {
    }

    public Synthese(BigDecimal idremcom, String type, Timestamp datepresentation, String typeoperation, String idbancon, BigDecimal nbtotoperemis, String mnttotoperemis, BigDecimal nbtotoperrecus, String mnttotoperrecus, String solde, String signe) {
        this.idremcom = idremcom;
        this.type = type;
        this.datepresentation = datepresentation;
        this.typeoperation = typeoperation;
        this.idbancon = idbancon;
        this.nbtotoperemis = nbtotoperemis;
        this.mnttotoperemis = mnttotoperemis;
        this.nbtotoperrecus = nbtotoperrecus;
        this.mnttotoperrecus = mnttotoperrecus;
        this.solde = solde;
        this.signe = signe;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getDatepresentation() {
        return datepresentation;
    }

    public void setDatepresentation(Timestamp datepresentation) {
        this.datepresentation = datepresentation;
    }

    public String getTypeoperation() {
        return typeoperation;
    }

    public void setTypeoperation(String typeoperation) {
        this.typeoperation = typeoperation;
    }

    public String getIdbancon() {
        return idbancon;
    }

    public void setIdbancon(String idbancon) {
        this.idbancon = idbancon;
    }

    public BigDecimal getNbtotoperemis() {
        return nbtotoperemis;
    }

    public void setNbtotoperemis(BigDecimal nbtotoperemis) {
        this.nbtotoperemis = nbtotoperemis;
    }

    public String getMnttotoperemis() {
        return mnttotoperemis;
    }

    public void setMnttotoperemis(String mnttotoperemis) {
        this.mnttotoperemis = mnttotoperemis;
    }

    public BigDecimal getNbtotoperrecus() {
        return nbtotoperrecus;
    }

    public void setNbtotoperrecus(BigDecimal nbtotoperrecus) {
        this.nbtotoperrecus = nbtotoperrecus;
    }

    public String getMnttotoperrecus() {
        return mnttotoperrecus;
    }

    public void setMnttotoperrecus(String mnttotoperrecus) {
        this.mnttotoperrecus = mnttotoperrecus;
    }

    public String getSolde() {
        return solde;
    }

    public void setSolde(String solde) {
        this.solde = solde;
    }

    public String getSigne() {
        return signe;
    }

    public void setSigne(String signe) {
        this.signe = signe;
    }

   

    public BigDecimal getIdremcom() {
        return idremcom;
    }

    public void setIdremcom(BigDecimal idremcom) {
        this.idremcom = idremcom;
    }
}
