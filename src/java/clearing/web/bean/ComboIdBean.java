/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import java.util.logging.Level;
import java.util.logging.Logger;
import clearing.action.migration.Migrator;
import org.patware.jdbc.DataBase;
import org.patware.log.SmartFileLogger;
import org.patware.xml.MigrationXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboIdBean extends ComboBean {

    private Migrator migrator;

    public String getColonnes(String table) {
        try {
            DataBase db1 = new DataBase(getMigrator().getCurrentDriver1());
            db1.open(getMigrator().getCurrentUrl1(), getMigrator().getCurrentUser1(), getMigrator().getCurrentPassword1());
            String[] colonnes = db1.getColumnsNames(table);
            return getComboLiteral(colonnes, colonnes).toString();

        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getTableFiltres(String id) {
        try {

            setMigrator(new Migrator(id));

            String tables[] = getMigrator().getCurrentTables();
             return getComboLiteral(tables, tables).toString();
           
        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getComboDatas() {
        try {

            String id[] = MigrationXmlReader.getId();
            String libelle[] = MigrationXmlReader.getLibelle();
            System.out.println("Combo :"+getComboLiteral(libelle, id).toString());
             return getComboLiteral(libelle, id).toString();
        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ComboIdBean() {
    }

    public Migrator getMigrator() {
        return migrator;
    }

    public void setMigrator(Migrator migrator) {
        this.migrator = migrator;
    }
}
