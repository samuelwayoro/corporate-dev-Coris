/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import clearing.model.CMPUtility;
import clearing.table.Agences;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboAgenceBean extends ComboBean{
 private String requete = "select * from Agences";
 
 

    public String getRequete() {
        return requete;
    }

    public void setRequete(String requete) {
        this.requete = requete;
    }

    
 public String getComboDatas() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Agences[] agences = (Agences[]) db.retrieveRowAsObject(requete, new Agences());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (agences != null && agences.length > 0) {
                labels = new String[agences.length + 1];
                values = new String[agences.length + 1];
                for (int i = 0; i < agences.length; i++) {
                    labels[i] = agences[i].getCodeagence() + "-" + agences[i].getLibelleagence();
                    values[i] = "A"+agences[i].getCodeagence();
                }
                labels[agences.length] = "Tous";
                values[agences.length] = "%%";
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboAgenceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
  }
 public String getComboDatas2() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Agences[] agences = (Agences[]) db.retrieveRowAsObject(requete, new Agences());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (agences != null && agences.length > 0) {
                labels = new String[agences.length ];
                values = new String[agences.length ];
                for (int i = 0; i < agences.length; i++) {
                    labels[i] = agences[i].getCodeagence() + "-" + agences[i].getLibelleagence();
                    values[i] = "A"+agences[i].getCodeagence();
                }
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboAgenceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
  }
 public String getComboBanqueData(){
     setRequete("select * from agences where codebanque = '"+ CMPUtility.getCodeBanqueSica3()+"'");
     return getComboDatas();
 }

 public String getComboBanqueData(String banque){
     setRequete("select * from agences where codebanque = '"+ banque +"'");
     return getComboDatas2();
 }
 
 

    public ComboAgenceBean() {
    }

}
