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
public class Echeadetails {

    private Timestamp datereglement;
    private String typeoperation;
    private String idbancon;
    private BigDecimal nbtotoperemis;
    private BigDecimal idecheancier;
    private String mnttotoperemis;
    private BigDecimal nbtotoperecus;
    private String mnttotoperecus;

    public Echeadetails() {
    }

    public Echeadetails(Timestamp datereglement) {
        this.datereglement = datereglement;
    }

    public Echeadetails(Timestamp datereglement, String typeoperation, String idbancon) {
        this.datereglement = datereglement;
        this.typeoperation = typeoperation;
        this.idbancon = idbancon;
    }

    public Timestamp getDatereglement() {
        return datereglement;
    }

    public void setDatereglement(Timestamp datereglement) {
        this.datereglement = datereglement;
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

    public BigDecimal getNbtotoperecus() {
        return nbtotoperecus;
    }

    public void setNbtotoperecus(BigDecimal nbtotoperecus) {
        this.nbtotoperecus = nbtotoperecus;
    }

    public String getMnttotoperecus() {
        return mnttotoperecus;
    }

    public void setMnttotoperecus(String mnttotoperecus) {
        this.mnttotoperecus = mnttotoperecus;
    }

    public String toString() {
        return "clearing.table.Echeadetails[datereglement=" + datereglement + "]";
    }

    public BigDecimal getIdecheancier() {
        return idecheancier;
    }

    public void setIdecheancier(BigDecimal idecheancier) {
        this.idecheancier = idecheancier;
    }
}
