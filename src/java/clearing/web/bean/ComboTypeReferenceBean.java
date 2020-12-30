/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.bean.table.Params;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboTypeReferenceBean extends ComboBean{
 private String requete = "select * from params where type='CODE_REFERENCE' ";

 
   public String getComboDatas() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Params[] params = (Params[]) db.retrieveRowAsObject(requete, new Params());
            String[] labels = null;
            String[] values = null;
            if (params != null && params.length > 0) {
                labels = new String[params.length];
                values = new String[params.length];
                for (int i = 0; i < params.length; i++) {
                    //labels[i] = params[i].getCode() + "-" + params[i].getLibelle2();
                    labels[i] = params[i].getValeur().trim();
                    values[i] = params[i].getNom().trim();
                }
               
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboAgenceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
  }

    public ComboTypeReferenceBean() {
    }

}
