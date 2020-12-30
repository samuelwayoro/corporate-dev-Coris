/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import clearing.table.Utilisateurs;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboUtilisateurBean extends ComboBean {

    private String requete = "select * from Utilisateurs";
    

    public String getComboDatas() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Utilisateurs[] utilisateurs = (Utilisateurs[]) db.retrieveRowAsObject(requete, new Utilisateurs());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (utilisateurs != null && utilisateurs.length > 0) {
                labels = new String[utilisateurs.length + 1/*Tous*/];
                values = new String[utilisateurs.length + 1];
                for (int i = 0; i < utilisateurs.length; i++) {
                    labels[i] = (utilisateurs[i].getNom()+"_"+utilisateurs[i].getPrenom()).replaceAll("\\p{Punct}", " ");
                    values[i] = utilisateurs[i].getLogin() ;
                }
                labels[utilisateurs.length] = "Tous";
                values[utilisateurs.length] = "%";
                
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboAgenceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getComboUserofBranch(String agence){
        this.requete = requete + " where adresse ='"+agence+"'";
        return getComboDatas();
    }

    
    public String getRequete() {
        return requete;
    }

   
    public void setRequete(String requete) {
        this.requete = requete;
    }
    
    public ComboUtilisateurBean() {
    }
}
