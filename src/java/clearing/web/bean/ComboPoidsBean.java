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
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboPoidsBean extends ComboBean{
 private String requete = "select p1.* from Params p1,Profils p2 where p1.type='CODE_POIDS' and "
                           + ResLoader.getMessages("trimFunction")+"(p1.nom)="+ ResLoader.getMessages("trimFunction") +"(p2.nomprofil) and p2.etat="+ Utility.getParam("CETAUTIACT")+" order by valeur";

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
            Params[] params = (Params[]) db.retrieveRowAsObject(requete, new Params());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (params != null && params.length > 0) {
                labels = new String[params.length ];
                values = new String[params.length ];
                for (int i = 0; i < params.length; i++) {
                    labels[i] = params[i].getValeur() + "-" + params[i].getLibelle();
                    values[i] = "A"+params[i].getValeur();
                }
                
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboPoidsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
  }
 

 
 

    public ComboPoidsBean() {
    }

}
