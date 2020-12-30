/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import clearing.model.CMPUtility;
import clearing.table.Banques;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.web.json.JSONConverter;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboBanqueBean extends ComboBean {

    private String requete = "select * from Banques";

    public String getComboDatas() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Banques[] banques = (Banques[]) db.retrieveRowAsObject(requete, new Banques());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (banques != null && banques.length > 0) {
                labels = new String[banques.length + 3/*Tous+National+SousRegional*/];
                values = new String[banques.length + 3];
                for (int i = 0; i < banques.length; i++) {
                    labels[i] = banques[i].getCodebanque() + "-" + banques[i].getLibellebanque();
                    values[i] = banques[i].getCodebanque();
                }
                labels[banques.length] = "Tous";
                values[banques.length] = "%";
                labels[banques.length + 1] = "National";
                values[banques.length + 1] = (Utility.getParam("VERSION_SICA").equals("2")?CMPUtility.getCodeBanque().charAt(0):CMPUtility.getCodeBanqueSica3().substring(0,2))+ "%%%";
                labels[banques.length + 2] = "Sous Régional";
                values[banques.length + 2] = (Utility.getParam("VERSION_SICA").equals("2")?CMPUtility.getCodeBanque().charAt(0):CMPUtility.getCodeBanqueSica3().substring(0,2))+ "%%";
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
            Banques[] banques = (Banques[]) db.retrieveRowAsObject(requete, new Banques());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (banques != null && banques.length > 0) {
                labels = new String[banques.length];
                values = new String[banques.length ];
                for (int i = 0; i < banques.length; i++) {
                    labels[i] = banques[i].getCodebanque() + "-" + banques[i].getLibellebanque();
                    values[i] = banques[i].getCodebanque();
                }
               
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboAgenceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
        public String getInfoBanque(String codeBanque) {

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(ComboBanqueBean.class.getName()).log(Level.SEVERE, null, ex);
        }
            Banques[] banques = (Banques[]) db.retrieveRowAsObject("SELECT * FROM BANQUES WHERE CODEBANQUE='"+ codeBanque +"'", new Banques());
            db.close();
              if (banques != null && banques.length > 0) {
                  JSONConverter jsonConverter = new JSONConverter();
            try {
                return jsonConverter.objectToJSONStringArray(banques[0]);
            } catch (JSONException ex) {
                Logger.getLogger(ComboBanqueBean.class.getName()).log(Level.SEVERE, null, ex);
            }
              }
            return "rien";

    }

        public String getComboBanques() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Banques[] banques = (Banques[]) db.retrieveRowAsObject(requete, new Banques());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (banques != null && banques.length > 0) {
                labels = new String[banques.length ];
                values = new String[banques.length ];
                for (int i = 0; i < banques.length; i++) {
                    labels[i] = banques[i].getCodebanque() + "-" + banques[i].getLibellebanque();
                    values[i] = banques[i].getCodebanque();
                }

                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboAgenceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public String getRequete() {
        return requete;
    }


    public void setRequete(String requete) {
        this.requete = requete;
    }
    
    public ComboBanqueBean() {
    }
}
