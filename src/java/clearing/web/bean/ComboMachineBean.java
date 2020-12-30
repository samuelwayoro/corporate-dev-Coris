/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import clearing.table.Machines;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboMachineBean extends ComboBean {

    private String requete = "select distinct machine from macuti";

    public String getComboDatas() {
        try {


            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            Machines[] machines = (Machines[]) db.retrieveRowAsObject(requete, new Machines());
            db.close();
            String[] labels = null;
            String[] values = null;
            if (machines != null && machines.length > 0) {
                labels = new String[machines.length + 1/*Tous*/];
                values = new String[machines.length + 1];
                for (int i = 0; i < machines.length; i++) {
                    labels[i] = machines[i].getMachine().replaceAll("\\p{Punct}", " ");
                    values[i] = machines[i].getMachine();
                }
                labels[machines.length] = "Tous";
                values[machines.length] = "%";

                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboAgenceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void setRequete(String requete) {
        this.requete = requete;
    }

    public String getRequete() {
        return requete;
    }

    public ComboMachineBean() {
    }
}
