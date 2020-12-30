/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import clearing.table.Etablissements;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;
    
/**
 *
 * @author Patrick
 */
public class ComboEtablissementBean extends ComboBean{
 private String requete = "select * from Etablissements";

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
            Etablissements[] etablissements = (Etablissements[]) db.retrieveRowAsObject(requete, new Etablissements());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (etablissements != null && etablissements.length > 0) {
                labels = new String[etablissements.length + 1];
                values = new String[etablissements.length + 1];
                for (int i = 0; i < etablissements.length; i++) {
                    labels[i] = etablissements[i].getCodeetablissement() + "-" + etablissements[i].getLibelleetablissement();
                    values[i] = etablissements[i].getCodeetablissement();
                }
                labels[etablissements.length] = "Tous";
                values[etablissements.length] = "%%";
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboEtablissementBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
  }
 public String getComboDatas2() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Etablissements[] etablissements = (Etablissements[]) db.retrieveRowAsObject(requete, new Etablissements());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (etablissements != null && etablissements.length > 0) {
                labels = new String[etablissements.length ];
                values = new String[etablissements.length ];
                for (int i = 0; i < etablissements.length; i++) {
                    labels[i] = etablissements[i].getCodeetablissement() + "-" + etablissements[i].getLibelleetablissement();
                    values[i] = etablissements[i].getCodeetablissement();
                }
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboEtablissementBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
  }
 public String getComboEtablissementData(){
     setRequete("select * from etablissements where etat = 10");
     return getComboDatas();
 }

 public String getComboEtablissementData(String etat){
     setRequete("select * from etablissements where etat = "+ etat );
     return getComboDatas2();
 }
 
 

    public ComboEtablissementBean() {
    }

}
