/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import clearing.table.Utilisateurs;
import clearing.table.ibus.IBUS_OPE_GUI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboBordereauBean extends ComboBean {

    private String requete = "select * from IBUS_OPE_GUI";
    private Utilisateurs user = new Utilisateurs();

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
            IBUS_OPE_GUI[] fluopegui = (IBUS_OPE_GUI[]) db.retrieveRowAsObject(requete +" where cd_scpta like '"+user.getAdresse().trim()+"'", new IBUS_OPE_GUI());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (fluopegui != null && fluopegui.length > 0) {
                labels = new String[fluopegui.length];
                values = new String[fluopegui.length];
                for (int i = 0; i < fluopegui.length; i++) {
                    labels[i] = fluopegui[i].getNo_Ex() + "-" + fluopegui[i].getId_Op() + "-" + fluopegui[i].getCd_Scpta();
                    values[i] = fluopegui[i].getNo_Ex() + "-" + fluopegui[i].getId_Op() + "-" + fluopegui[i].getCd_Scpta() + "-" + fluopegui[i].getMt_Op() + "-" + Utility.removeAccent(fluopegui[i].getType_Inter()) + "-" + Utility.removeAccent(fluopegui[i].getNm_Inter());
                }

                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboBordereauBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ComboBordereauBean(Utilisateurs user) {
        this.user = user;
    }

    public ComboBordereauBean() {
    }
}
