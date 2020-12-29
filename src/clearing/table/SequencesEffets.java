/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.table;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 *
 * @author Patrick
 */

public class SequencesEffets implements Serializable {
    
    private BigDecimal idsequence;
 
    private String codeline;
  
    private BigDecimal numerolot;
  
    private String typedocument;
  
    private String datedescan;
 
    private String machinescan;

    public SequencesEffets() {
    }

    public SequencesEffets(BigDecimal idsequence) {
        this.idsequence = idsequence;
    }

    public BigDecimal getIdsequence() {
        return idsequence;
    }

    public void setIdsequence(BigDecimal idsequence) {
        this.idsequence = idsequence;
    }

    public String getCodeline() {
        return codeline;
    }

    public void setCodeline(String codeline) {
        this.codeline = codeline;
    }

    public BigDecimal getNumerolot() {
        return numerolot;
    }

    public void setNumerolot(BigDecimal numerolot) {
        this.numerolot = numerolot;
    }

    public String getTypedocument() {
        return typedocument;
    }

    public void setTypedocument(String typedocument) {
        this.typedocument = typedocument;
    }

    public String getDatedescan() {
        return datedescan;
    }

    public void setDatedescan(String datedescan) {
        this.datedescan = datedescan;
    }

    public String getMachinescan() {
        return machinescan;
    }

    public void setMachinescan(String machinescan) {
        this.machinescan = machinescan;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idsequence != null ? idsequence.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SequencesEffets)) {
            return false;
        }
        SequencesEffets other = (SequencesEffets) object;
        if ((this.idsequence == null && other.idsequence != null) || (this.idsequence != null && !this.idsequence.equals(other.idsequence))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clearing.table.Sequences[idsequence=" + idsequence + "]";
    }

}
