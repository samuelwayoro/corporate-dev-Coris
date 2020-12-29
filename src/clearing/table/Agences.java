/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.table;

import java.io.Serializable;

/**
 *
 * @author Patrick
 */
public class Agences implements Serializable {

    
    private String codeville;
    private String codevillecompense;
    private String libelleagence;
    private String codeagence;
    private String codebanque;

    
    public Agences() {
    }

    

    public String getCodeville() {
        return codeville;
    }

    public void setCodeville(String codeville) {
        this.codeville = codeville;
    }

    public String getCodevillecompense() {
        return codevillecompense;
    }

    public void setCodevillecompense(String codevillecompense) {
        this.codevillecompense = codevillecompense;
    }

    public String getLibelleagence() {
        return libelleagence;
    }

    public void setLibelleagence(String libelleagence) {
        this.libelleagence = libelleagence;
    }

    public String getCodeagence() {
        return codeagence;
    }

    public void setCodeagence(String codeagence) {
        this.codeagence = codeagence;
    }

    public String getCodebanque() {
        return codebanque;
    }

    public void setCodebanque(String codebanque) {
        this.codebanque = codebanque;
    }
}
