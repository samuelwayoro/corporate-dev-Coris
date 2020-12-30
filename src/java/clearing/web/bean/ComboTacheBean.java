/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.bean.table.Tache;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboTacheBean extends ComboBean {

    private String requete = "select * from tache ";

    public String getTacheMenu() {
        requete = requete + "where typetache='MENU'";
        return getComboDatas();

    }

    public String getComboDatas() {
        try {

            
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Tache[] tache = (Tache[]) db.retrieveRowAsObject(requete, new Tache());
            requete = "select * from tache ";
            String[] labels = null;
            String[] values = null;
            if (tache != null && tache.length > 0) {
                labels = new String[tache.length];
                values = new String[tache.length];
                for (int i = 0; i < tache.length; i++) {
                    labels[i] = tache[i].getIdtache().trim() + "-" + tache[i].getLibelle();
                    values[i] = tache[i].getIdtache().trim();
                }

                 return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboAgenceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ComboTacheBean() {
    }
}
