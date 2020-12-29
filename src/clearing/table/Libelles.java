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
public class Libelles {
    private BigDecimal idlibelle;
    
    private String typelibelle;
    
    private String code;
    
    private String libelle1;
    
    private String libelle2;
    
    private BigDecimal laversion;
    
    
    private Timestamp datemaj;
    
    private String libelle3;

    public Libelles() {
    }

    public Libelles(BigDecimal idlibelle) {
        this.idlibelle = idlibelle;
    }

    public BigDecimal getIdlibelle() {
        return idlibelle;
    }

    public void setIdlibelle(BigDecimal idlibelle) {
        this.idlibelle = idlibelle;
    }

    public String getTypelibelle() {
        return typelibelle;
    }

    public void setTypelibelle(String typelibelle) {
        this.typelibelle = typelibelle;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle1() {
        return libelle1;
    }

    public void setLibelle1(String libelle1) {
        this.libelle1 = libelle1;
    }

    public String getLibelle2() {
        return libelle2;
    }

    public void setLibelle2(String libelle2) {
        this.libelle2 = libelle2;
    }

    public BigDecimal getLaversion() {
        return laversion;
    }

    public void setLaversion(BigDecimal laversion) {
        this.laversion = laversion;
    }

    public Timestamp getDatemaj() {
        return datemaj;
    }

    public void setDatemaj(Timestamp datemaj) {
        this.datemaj = datemaj;
    }

    public String getLibelle3() {
        return libelle3;
    }

    public void setLibelle3(String libelle3) {
        this.libelle3 = libelle3;
    }



    public String toString() {
        return "clearing.table.Libelles[idlibelle=" + idlibelle + "]";
    }

}
