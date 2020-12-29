/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table.delta;

import java.math.BigDecimal;
import java.sql.Blob;

/**
 *
 * @author AUGOU Patrick
 */
public class BksigImageValideCompte {
    private BigDecimal IDENT_SIG;
    private BigDecimal IDENT_BNK;
    private BigDecimal IDENT_CARTON;
    private BigDecimal NO_CARTON;
    private String CREAT_TIME;
    private String CREAT_CUTI;
    private String CREAT_VALID_DATE;
    private String CREAT_VALID;
    private String CREAT_VALID_TIME;
    private String COMMENTAIRE;
    private BigDecimal IDENT_NCP;
    private String AGE;
    private String NCP;
    private String SUF;
    private String DEV;
    private BigDecimal NO_FORMAT;
    private BigDecimal CHEMIN;
    private BigDecimal TYPE_DE_FICHIER;
    private BigDecimal NB_BITS_PIXEL;
    private BigDecimal WIDTH;
    private BigDecimal HEIGHT;
    private Blob IMAGE;
    private BigDecimal TAILLE;

    public String getAGE() {
        return AGE;
    }

    public void setAGE(String AGE) {
        this.AGE = AGE;
    }

    public BigDecimal getCHEMIN() {
        return CHEMIN;
    }

    public void setCHEMIN(BigDecimal CHEMIN) {
        this.CHEMIN = CHEMIN;
    }

    public String getCOMMENTAIRE() {
        return COMMENTAIRE;
    }

    public void setCOMMENTAIRE(String COMMENTAIRE) {
        this.COMMENTAIRE = COMMENTAIRE;
    }

    public String getCREAT_CUTI() {
        return CREAT_CUTI;
    }

    public void setCREAT_CUTI(String CREAT_CUTI) {
        this.CREAT_CUTI = CREAT_CUTI;
    }

    public String getCREAT_TIME() {
        return CREAT_TIME;
    }

    public void setCREAT_TIME(String CREAT_TIME) {
        this.CREAT_TIME = CREAT_TIME;
    }

    public String getCREAT_VALID() {
        return CREAT_VALID;
    }

    public void setCREAT_VALID(String CREAT_VALID) {
        this.CREAT_VALID = CREAT_VALID;
    }

    public String getCREAT_VALID_DATE() {
        return CREAT_VALID_DATE;
    }

    public void setCREAT_VALID_DATE(String CREAT_VALID_DATE) {
        this.CREAT_VALID_DATE = CREAT_VALID_DATE;
    }

    public String getCREAT_VALID_TIME() {
        return CREAT_VALID_TIME;
    }

    public void setCREAT_VALID_TIME(String CREAT_VALID_TIME) {
        this.CREAT_VALID_TIME = CREAT_VALID_TIME;
    }

    public String getDEV() {
        return DEV;
    }

    public void setDEV(String DEV) {
        this.DEV = DEV;
    }

    public BigDecimal getHEIGHT() {
        return HEIGHT;
    }

    public void setHEIGHT(BigDecimal HEIGHT) {
        this.HEIGHT = HEIGHT;
    }

    public BigDecimal getIDENT_BNK() {
        return IDENT_BNK;
    }

    public void setIDENT_BNK(BigDecimal IDENT_BNK) {
        this.IDENT_BNK = IDENT_BNK;
    }

    public BigDecimal getIDENT_CARTON() {
        return IDENT_CARTON;
    }

    public void setIDENT_CARTON(BigDecimal IDENT_CARTON) {
        this.IDENT_CARTON = IDENT_CARTON;
    }

    public BigDecimal getIDENT_NCP() {
        return IDENT_NCP;
    }

    public void setIDENT_NCP(BigDecimal IDENT_NCP) {
        this.IDENT_NCP = IDENT_NCP;
    }

    public BigDecimal getIDENT_SIG() {
        return IDENT_SIG;
    }

    public void setIDENT_SIG(BigDecimal IDENT_SIG) {
        this.IDENT_SIG = IDENT_SIG;
    }

    public Blob getIMAGE() {
        return IMAGE;
    }

    public void setIMAGE(Blob IMAGE) {
        this.IMAGE = IMAGE;
    }

    public BigDecimal getNB_BITS_PIXEL() {
        return NB_BITS_PIXEL;
    }

    public void setNB_BITS_PIXEL(BigDecimal NB_BITS_PIXEL) {
        this.NB_BITS_PIXEL = NB_BITS_PIXEL;
    }

    public String getNCP() {
        return NCP;
    }

    public void setNCP(String NCP) {
        this.NCP = NCP;
    }

    public BigDecimal getNO_CARTON() {
        return NO_CARTON;
    }

    public void setNO_CARTON(BigDecimal NO_CARTON) {
        this.NO_CARTON = NO_CARTON;
    }

    public BigDecimal getNO_FORMAT() {
        return NO_FORMAT;
    }

    public void setNO_FORMAT(BigDecimal NO_FORMAT) {
        this.NO_FORMAT = NO_FORMAT;
    }

    public String getSUF() {
        return SUF;
    }

    public void setSUF(String SUF) {
        this.SUF = SUF;
    }

    public BigDecimal getTAILLE() {
        return TAILLE;
    }

    public void setTAILLE(BigDecimal TAILLE) {
        this.TAILLE = TAILLE;
    }

    public BigDecimal getTYPE_DE_FICHIER() {
        return TYPE_DE_FICHIER;
    }

    public void setTYPE_DE_FICHIER(BigDecimal TYPE_DE_FICHIER) {
        this.TYPE_DE_FICHIER = TYPE_DE_FICHIER;
    }

    public BigDecimal getWIDTH() {
        return WIDTH;
    }

    public void setWIDTH(BigDecimal WIDTH) {
        this.WIDTH = WIDTH;
    }



}
