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
public class Banques {
    private String codepays;
    private String libellebanque;
    private BigDecimal algorithmedecontrolespecifique;
    private String codebanque;

    public Banques() {
    }

    public Banques(String codebanque) {
        this.codebanque = codebanque;
    }

    public String getCodepays() {
        return codepays;
    }

    public Banques(String codepays, String libellebanque, BigDecimal algorithmedecontrolespecifique, String codebanque) {
        this.codepays = codepays;
        this.libellebanque = libellebanque;
        
        this.codebanque = codebanque;
    }

    public BigDecimal getAlgorithmedecontrolespecifique() {
        return algorithmedecontrolespecifique;
    }

    public void setAlgorithmedecontrolespecifique(BigDecimal algorithmedecontrolespecifique) {
        this.algorithmedecontrolespecifique = algorithmedecontrolespecifique;
    }


    public void setCodepays(String codepays) {
        this.codepays = codepays;
    }

    public String getLibellebanque() {
        return libellebanque;
    }

    public void setLibellebanque(String libellebanque) {
        this.libellebanque = libellebanque;
    }

   

    public String getCodebanque() {
        return codebanque;
    }

    public void setCodebanque(String codebanque) {
        this.codebanque = codebanque;
    }



    public boolean equals(Object object) {

        if (!(object instanceof Banques)) {
            return false;
        }
        Banques other = (Banques) object;
        if ((this.codebanque == null && other.codebanque != null) || (this.codebanque != null && !this.codebanque.equals(other.codebanque))) {
            return false;
        }
        return true;
    }


    public String toString() {
        return "clearing.table.Banques[codebanque=" + codebanque + "]";
    }

}
