/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import clearing.table.Comptes;
import clearing.table.Utilisateurs;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboCompteTPCICUBean extends ComboBean {

    private String requete = "select * from Comptes";

    private Utilisateurs user = new Utilisateurs();
    private String currentCompteRemettant;

    public String getCurrentCompteRemettant() {
        return currentCompteRemettant;
    }

    public void setCurrentCompteRemettant(String currentCompteRemettant) {
        this.currentCompteRemettant = currentCompteRemettant;
    }
    
    

    public String getComboDatas() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete+ " where adresse1 like '"+user.getAdresse().trim()+"'", new Comptes());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (comptes != null && comptes.length > 0) {
                labels = new String[comptes.length];
                values = new String[comptes.length];
                for (int i = 0; i < comptes.length; i++) {
                    labels[i] = comptes[i].getNumero();
                    values[i] = comptes[i].getNumero()+"-"+Utility.getParam("LCE_SBF").trim()+ "-" + comptes[i].getAgence() + "-" + Utility.removeAccent(comptes[i].getNom()) + "-" + Utility.removeAccent(comptes[i].getAdresse1());
                }

                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboCompteTPCICUBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public String getComboDatasWithSelected() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject(requete+ " where adresse1 like '"+user.getAdresse().trim()+"'", new Comptes());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (comptes != null && comptes.length > 0) {
                labels = new String[comptes.length];
                values = new String[comptes.length];
                for (int i = 0; i < comptes.length; i++) {
                    labels[i] = comptes[i].getNumero();
                    values[i] = comptes[i].getNumero()+"-"+Utility.getParam("LCE_SBF").trim()+ "-" + comptes[i].getAgence() + "-" + Utility.removeAccent(comptes[i].getNom()) + "-" + Utility.removeAccent(comptes[i].getAdresse1());
                }

                return getComboLiteralWithSelected(labels, values,getCurrentCompteRemettant()).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboCompteTPCICUBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


 public ComboCompteTPCICUBean(Utilisateurs user) {
        this.user = user;
    }

    public ComboCompteTPCICUBean() {
    }
}
