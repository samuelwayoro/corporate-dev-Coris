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
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboEtatBean extends ComboBean{
 private String requete = "select * from Params where nom like 'CETAOPE%'";

  
 public String getComboDatas() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Params[] params = (Params[]) db.retrieveRowAsObject(requete, new Params());
            db.close();
            String[] labels = null;
            String[] values = null;
            String allValues = "0";
            if (params != null && params.length > 0) {
                labels = new String[params.length + 6];
                values = new String[params.length + 6];
                for (int i = 0; i < params.length; i++) {
                    labels[i] = params[i].getValeur().trim() + "-" + params[i].getLibelle().trim();
                    values[i] = params[i].getValeur().trim();
                    allValues += ","+values[i];
                }
                labels[params.length] = "Tous";
                values[params.length] = allValues;
                labels[params.length + 1] =  Utility.getParam("CETAOPERETREC") + "-" + Utility.getParam("CETAOPERET")+ "-TOUT RETOUR";
                values[params.length + 1] =  Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET");
                labels[params.length + 2] =  Utility.getParam("CETAOPERETRECENVSIB") + "-" + Utility.getParam("CETAOPERETENVSIB")+ "-TOUT RETOUR ENV.SIB";
                values[params.length + 2] =  Utility.getParam("CETAOPERETRECENVSIB") + "," + Utility.getParam("CETAOPERETENVSIB");
                labels[params.length + 3] =  Utility.getParam("CETAOPERETRECENVSIBVER") + "-" + Utility.getParam("CETAOPERETENVSIB")+ "," + Utility.getParam("CETAOPERETRECENVSIB")+ "-TOUT RETOUR ENV.SIB VERIFIE";
                values[params.length + 3] =  Utility.getParam("CETAOPERETRECENVSIBVER") + "," + Utility.getParam("CETAOPERETENVSIB")+ "," + Utility.getParam("CETAOPERETRECENVSIB");
                labels[params.length + 4] =  "COMPENSE RETOUR";
                values[params.length + 4] =  Utility.getParam("CETAOPERETRECENVSIBVER") + "," + Utility.getParam("CETAOPERETENVSIB")+ "," + Utility.getParam("CETAOPERETRECENVSIB")+ "," + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET");
                labels[params.length + 5] =  "COMPENSE ALLER";
                values[params.length + 5] =  Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEALLICOM1ACCENVSIB")+ "," + Utility.getParam("CETAOPEREJRET")+ "," + Utility.getParam("CETAOPEREJRETENVSIB") + "," + Utility.getParam("CETAOPERET");
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboAgenceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
  }
    public ComboEtatBean() {
    }

}
