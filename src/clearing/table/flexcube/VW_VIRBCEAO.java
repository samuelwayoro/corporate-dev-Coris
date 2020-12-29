/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.table.flexcube;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Patrick
 */
public class VW_VIRBCEAO implements Serializable {

    /**
     * VW_VIRBCEAO ( DATETRANSACTION DATE, REFTRANSACTION VARCHAR2(56 CHAR),
     * MONTANTTRANSACTION NUMBER(22,3), AGENCEDUCOMPTE VARCHAR2(3 CHAR), FILIALE
     * VARCHAR2(3 CHAR), REFERENCE VARCHAR2(16 CHAR), BANQUEREMETTANT
     * VARCHAR2(105 CHAR), RIBDONNEURDORDRE VARCHAR2(4000), NOMDONNEURDORDRE
     * VARCHAR2(4000 CHAR), RIBBENEFICIAIRE VARCHAR2(420), NOMBENEFICIAIRE
     * VARCHAR2(105 CHAR), NARRATIVE VARCHAR2(3060), STATUTTRANSACTION
     * VARCHAR2(4), SAISIPAR VARCHAR2(12 CHAR), VALIDERPAR VARCHAR2(12 CHAR),
     * TYPEVIREMENT VARCHAR2(3 CHAR))
     *
     */
    /**
     * DATETRANSACTION DATE REFTRANSACTION VARCHAR2(16 CHAR) MONTANTTRANSACTION
     * NUMBER(22,3) AGENCEDUCOMPTE VARCHAR2(3 CHAR) FILIALE VARCHAR2(3 CHAR)
     * CONTRACT_REF_NO NOT NULL VARCHAR2(16 CHAR) BANQUEREMETTANT VARCHAR2(20
     * CHAR) RIBDONNEURDORDRE VARCHAR2(4000) NOMDONNEURDORDRE VARCHAR2(105 CHAR)
     * RIBBENEFICIAIRE VARCHAR2(35 CHAR) CPTY_AC_NO VARCHAR2(35 CHAR) NOMDON
     * VARCHAR2(4000) DUP_RESOLUTION_LIST VARCHAR2(256 CHAR) NOMBENEFICIAIRE
     * VARCHAR2(105 CHAR) NARRATIVE VARCHAR2(765 CHAR) STATUTTRANSACTION
     * VARCHAR2(4) SAISIPAR VARCHAR2(12 CHAR) VALIDERPAR VARCHAR2(12 CHAR)
     * TYPEVIREMENT CHAR(3) *
     */
    private Date DATETRANSACTION;
    private String REFTRANSACTION;
    private BigDecimal MONTANTTRANSACTION;
    private String AGENCEDUCOMPTE;
    private String FILIALE;
    private String CONTRACT_REF_NO;
    private String CODESWIFTBANQUEREMETTANT;
 
    private String RIBDONNEURDORDRE;
    private String NOMDONNEURDORDRE;
    private String RIBBENEFICIAIRE;
    private String NOMBENEFICIAIRE;
    private String NARRATIVE;
    private String STATUTTRANSACTION;
    private String SAISIPAR;
    private String VALIDERPAR;
    private String TYPEVIREMENT;
    private BigDecimal SEQUENCETRANSCTION;

    @Override
    public String toString() {
        return "VW_VIRBCEAO{" + "DATETRANSACTION=" + DATETRANSACTION + ", REFTRANSACTION=" + REFTRANSACTION + ", MONTANTTRANSACTION=" + MONTANTTRANSACTION + ", AGENCEDUCOMPTE=" + AGENCEDUCOMPTE + ", FILIALE=" + FILIALE + ", CONTRACT_REF_NO=" + CONTRACT_REF_NO + ", CODESWIFTBANQUEREMETTANT=" + CODESWIFTBANQUEREMETTANT + ", RIBDONNEURDORDRE=" + RIBDONNEURDORDRE + ", NOMDONNEURDORDRE=" + NOMDONNEURDORDRE + ", RIBBENEFICIAIRE=" + RIBBENEFICIAIRE + ", NOMBENEFICIAIRE=" + NOMBENEFICIAIRE + ", NARRATIVE=" + NARRATIVE + ", STATUTTRANSACTION=" + STATUTTRANSACTION + ", SAISIPAR=" + SAISIPAR + ", VALIDERPAR=" + VALIDERPAR + ", TYPEVIREMENT=" + TYPEVIREMENT + ", SEQUENCETRANSCTION=" + SEQUENCETRANSCTION + '}';
    }

    public VW_VIRBCEAO() {
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.DATETRANSACTION);
        hash = 89 * hash + Objects.hashCode(this.REFTRANSACTION);
        hash = 89 * hash + Objects.hashCode(this.MONTANTTRANSACTION);
        hash = 89 * hash + Objects.hashCode(this.AGENCEDUCOMPTE);
        hash = 89 * hash + Objects.hashCode(this.FILIALE);

        hash = 89 * hash + Objects.hashCode(this.RIBDONNEURDORDRE);
        hash = 89 * hash + Objects.hashCode(this.NOMDONNEURDORDRE);
        hash = 89 * hash + Objects.hashCode(this.RIBBENEFICIAIRE);
        hash = 89 * hash + Objects.hashCode(this.NOMBENEFICIAIRE);
        hash = 89 * hash + Objects.hashCode(this.NARRATIVE);
        hash = 89 * hash + Objects.hashCode(this.STATUTTRANSACTION);
        hash = 89 * hash + Objects.hashCode(this.SAISIPAR);
        hash = 89 * hash + Objects.hashCode(this.VALIDERPAR);
        hash = 89 * hash + Objects.hashCode(this.TYPEVIREMENT);
        hash = 89 * hash + Objects.hashCode(this.SEQUENCETRANSCTION);
        hash = 89 * hash + Objects.hashCode(this.CODESWIFTBANQUEREMETTANT);
        hash = 89 * hash + Objects.hashCode(this.CONTRACT_REF_NO);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VW_VIRBCEAO other = (VW_VIRBCEAO) obj;
        if (!Objects.equals(this.REFTRANSACTION, other.REFTRANSACTION)) {
            return false;
        }
        if (!Objects.equals(this.AGENCEDUCOMPTE, other.AGENCEDUCOMPTE)) {
            return false;
        }
        if (!Objects.equals(this.FILIALE, other.FILIALE)) {
            return false;
        }

        if (!Objects.equals(this.RIBDONNEURDORDRE, other.RIBDONNEURDORDRE)) {
            return false;
        }
        if (!Objects.equals(this.NOMDONNEURDORDRE, other.NOMDONNEURDORDRE)) {
            return false;
        }
        if (!Objects.equals(this.RIBBENEFICIAIRE, other.RIBBENEFICIAIRE)) {
            return false;
        }
        if (!Objects.equals(this.NOMBENEFICIAIRE, other.NOMBENEFICIAIRE)) {
            return false;
        }
        if (!Objects.equals(this.NARRATIVE, other.NARRATIVE)) {
            return false;
        }
        if (!Objects.equals(this.STATUTTRANSACTION, other.STATUTTRANSACTION)) {
            return false;
        }
        if (!Objects.equals(this.SAISIPAR, other.SAISIPAR)) {
            return false;
        }
        if (!Objects.equals(this.VALIDERPAR, other.VALIDERPAR)) {
            return false;
        }
        if (!Objects.equals(this.TYPEVIREMENT, other.TYPEVIREMENT)) {
            return false;
        }
        if (!Objects.equals(this.CODESWIFTBANQUEREMETTANT, other.CODESWIFTBANQUEREMETTANT)) {
            return false;
        }
        if (!Objects.equals(this.CONTRACT_REF_NO, other.CONTRACT_REF_NO)) {
            return false;
        }
        if (!Objects.equals(this.DATETRANSACTION, other.DATETRANSACTION)) {
            return false;
        }
        if (!Objects.equals(this.MONTANTTRANSACTION, other.MONTANTTRANSACTION)) {
            return false;
        }
        if (!Objects.equals(this.SEQUENCETRANSCTION, other.SEQUENCETRANSCTION)) {
            return false;
        }
        return true;
    }

    public Date getDATETRANSACTION() {
        return DATETRANSACTION;
    }

    public void setDATETRANSACTION(Date DATETRANSACTION) {
        this.DATETRANSACTION = DATETRANSACTION;
    }

    public String getREFTRANSACTION() {
        return REFTRANSACTION;
    }

    public void setREFTRANSACTION(String REFTRANSACTION) {
        this.REFTRANSACTION = REFTRANSACTION;
    }

    public BigDecimal getMONTANTTRANSACTION() {
        return MONTANTTRANSACTION;
    }

    public void setMONTANTTRANSACTION(BigDecimal MONTANTTRANSACTION) {
        this.MONTANTTRANSACTION = MONTANTTRANSACTION;
    }

    public String getRIBBENEFICIAIRE() {
        return RIBBENEFICIAIRE;
    }

    public void setRIBBENEFICIAIRE(String RIBBENEFICIAIRE) {
        this.RIBBENEFICIAIRE = RIBBENEFICIAIRE;
    }

    public String getRIBDONNEURDORDRE() {
        return RIBDONNEURDORDRE;
    }

    public void setRIBDONNEURDORDRE(String RIBDONNEURDORDRE) {
        this.RIBDONNEURDORDRE = RIBDONNEURDORDRE;
    }

    public String getNOMDONNEURDORDRE() {
        return NOMDONNEURDORDRE;
    }

    public void setNOMDONNEURDORDRE(String NOMDONNEURDORDRE) {
        this.NOMDONNEURDORDRE = NOMDONNEURDORDRE;
    }

    public String getNOMBENEFICIAIRE() {
        return NOMBENEFICIAIRE;
    }

    public void setNOMBENEFICIAIRE(String NOMBENEFICIAIRE) {
        this.NOMBENEFICIAIRE = NOMBENEFICIAIRE;
    }

    public String getAGENCEDUCOMPTE() {
        return AGENCEDUCOMPTE;
    }

    public void setAGENCEDUCOMPTE(String AGENCEDUCOMPTE) {
        this.AGENCEDUCOMPTE = AGENCEDUCOMPTE;
    }

    public String getFILIALE() {
        return FILIALE;
    }

    public void setFILIALE(String FILIALE) {
        this.FILIALE = FILIALE;
    }

    public String getNARRATIVE() {
        return NARRATIVE;
    }

    public void setNARRATIVE(String NARRATIVE) {
        this.NARRATIVE = NARRATIVE;
    }

    public String getSTATUTTRANSACTION() {
        return STATUTTRANSACTION;
    }

    public void setSTATUTTRANSACTION(String STATUTTRANSACTION) {
        this.STATUTTRANSACTION = STATUTTRANSACTION;
    }

    public String getSAISIPAR() {
        return SAISIPAR;
    }

    public void setSAISIPAR(String SAISIPAR) {
        this.SAISIPAR = SAISIPAR;
    }

    public String getVALIDERPAR() {
        return VALIDERPAR;
    }

    public void setVALIDERPAR(String VALIDERPAR) {
        this.VALIDERPAR = VALIDERPAR;
    }

    public String getTYPEVIREMENT() {
        return TYPEVIREMENT;
    }

    public void setTYPEVIREMENT(String TYPEVIREMENT) {
        this.TYPEVIREMENT = TYPEVIREMENT;
    }

    public BigDecimal getSEQUENCETRANSCTION() {
        return SEQUENCETRANSCTION;
    }

    public void setSEQUENCETRANSCTION(BigDecimal SEQUENCETRANSCTION) {
        this.SEQUENCETRANSCTION = SEQUENCETRANSCTION;
    }

    public String getCODESWIFTBANQUEREMETTANT() {
        return CODESWIFTBANQUEREMETTANT;
    }

    public void setCODESWIFTBANQUEREMETTANT(String CODESWIFTBANQUEREMETTANT) {
        this.CODESWIFTBANQUEREMETTANT = CODESWIFTBANQUEREMETTANT;
    }

    public String getCONTRACT_REF_NO() {
        return CONTRACT_REF_NO;
    }

    public void setCONTRACT_REF_NO(String CONTRACT_REF_NO) {
        this.CONTRACT_REF_NO = CONTRACT_REF_NO;
    }

   
}
