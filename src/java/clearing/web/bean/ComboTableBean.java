 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboTableBean extends ComboBean {

    

    public String getColonnes(String table) {
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(),JDBCXmlReader.getUser(),JDBCXmlReader.getPassword());
            String[] colonnes = db.getColumnsNames(table);
            return getComboLiteral(colonnes, colonnes).toString();

        } catch (Exception ex) {
            Logger.getLogger(ComboTableBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getTables() {
        try {

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(),JDBCXmlReader.getUser(),JDBCXmlReader.getPassword());

            String tables[] = db.getTableNames(JDBCXmlReader.getUser());
             return getComboLiteral(tables, tables).toString();
           
        } catch (Exception ex) {
            Logger.getLogger(ComboTableBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getComboDatas() {
        try {

            
        } catch (Exception ex) {
            Logger.getLogger(ComboTableBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ComboTableBean() {
    }

}
