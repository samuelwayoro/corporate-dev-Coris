/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.migration;

import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Virements;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import org.patware.jdbc.DataBase;
import org.patware.log.SmartFileLogger;
import org.patware.utils.Utility;
import org.patware.xml.MigrationXmlReader;

/**
 *
 * @author Patrick
 */
public class Migrator {

    private DataBase db1;
    private DataBase db2;
    private ResultSet rs1;
    private ResultSet rs2;
    private ResultSetMetaData rsmd1;
    private ResultSetMetaData rsmd2;
    private int[] MigratorResult;
    private String[] id;
    private String[] relations;
    private String currentId;
    private String currentNbTables;
    private String[] currentTables;
    private String sqlMaxId;
    private String sqlMinId;
    private int nbChamps;
    private int nbTables;
    private String currentDriver1;
    private String currentUser1;
    private String currentPassword1;
    private String currentUrl1;
    private String currentSql1;
    private String currentDriver2;
    private String currentUser2;
    private String currentPassword2;
    private String currentUrl2;
    private String table;
    private String currentLibelle;
    private boolean execution = true;
    private String IdExt;
   
    public Migrator(String definitionId) {
        getCurrentDefinition(definitionId);
    }

    public void doMigration() throws Exception {

        Object objectToInsert = null;
        db1 = new DataBase(currentDriver1);
        System.out.println("Opening "+currentUrl1 );
        db1.open(currentUrl1, currentUser1, currentPassword1);

        db2 = new DataBase(currentDriver2);
        System.out.println("Opening "+currentUrl2 );
        db2.open(currentUrl2, currentUser2, currentPassword2);

        //Recuperation des limites.
        System.out.println("Recuperation des limites pour "+currentUrl1 );
        long minID = new Double(db1.getResultOfSQLFunction(sqlMinId)).longValue();
        long maxID = new Double(db1.getResultOfSQLFunction(sqlMaxId)).longValue();

        //Calcul de la fin de la requete
        boolean semicolon = false;
        boolean where = false;
        String append = "";
        String requete1 = currentSql1.trim();
        if (requete1.endsWith(";")) {
            requete1 = requete1.substring(0, requete1.length());
            semicolon = true;
        }
        if (requete1.toUpperCase().contains("WHERE")) {
            where = true;
        } else {
            append = " WHERE ";
        }
        if (where) {
            append += " AND " + IdExt + " >= ( ? ) AND " + IdExt + " <= ( ? ) ";
        } else {
            append += " " + IdExt + " >= ( ? ) AND " + IdExt + " <= ( ? ) ";
            where = true;
        }

        requete1 += append;
        if (semicolon) {
            requete1 += ";";
        }

        //Quelle table?
        if (table.contains("CHEQUES")) {
            objectToInsert = new Cheques();

            PreparedStatement preparedStatement = db1.getPrepareStatement(requete1);
            long minima;
            long maxima;

            for (long j = minID; j < maxID + 1; j += 500) {
                minima = j;
                maxima = j + 500;
                preparedStatement.setObject(1, minima);
                preparedStatement.setObject(2, maxima);


                if (j + 500 > maxID) {
                    j = j - (j + 500 - maxID);
                    maxima = j + 500;
                    preparedStatement.setObject(2, maxima);
                    Cheques[] cheques = (Cheques[]) db1.retrieveRowAsObject(preparedStatement, objectToInsert);
                    for (int i = 0; i < cheques.length; i++) {
                        cheques[i].setIdcheque(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDCHEQUE", "CHEQUES"))));
                        db2.insertObjectAsRowByQuery(cheques[i], table);
                    }
                    break;
                }

                Cheques[] cheques = (Cheques[]) db1.retrieveRowAsObject(preparedStatement, objectToInsert);
                for (int i = 0; i < cheques.length; i++) {
                    cheques[i].setIdcheque(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDCHEQUE", "CHEQUES"))));
                    db2.insertObjectAsRowByQuery(cheques[i], table);
                }




            }





        } else if (table.equalsIgnoreCase("effets")) {
            objectToInsert = new Effets();
        }
        if (table.equalsIgnoreCase("Virements")) {
            objectToInsert = new Virements();
        }

    //Envoi de la requete pour recuperation

db1.close();
db2.close();


    }

    public String getIdExt() {
        return IdExt;
    }

    public void setIdExt(String IdExt) {
        this.IdExt = IdExt;
    }

    public int[] getMigratorResult() {
        return MigratorResult;
    }

    public void setMigratorResult(int[] MigratorResult) {
        this.MigratorResult = MigratorResult;
    }

    public String getCurrentDriver1() {
        return currentDriver1;
    }

    public void setCurrentDriver1(String currentDriver1) {
        this.currentDriver1 = currentDriver1;
    }

    public String getCurrentDriver2() {
        return currentDriver2;
    }

    public void setCurrentDriver2(String currentDriver2) {
        this.currentDriver2 = currentDriver2;
    }

    public String getCurrentId() {
        return currentId;
    }

    public void setCurrentId(String currentId) {
        this.currentId = currentId;
    }

    public String getCurrentLibelle() {
        return currentLibelle;
    }

    public void setCurrentLibelle(String currentLibelle) {
        this.currentLibelle = currentLibelle;
    }

    public String getCurrentNbTables() {
        return currentNbTables;
    }

    public void setCurrentNbTables(String currentNbTables) {
        this.currentNbTables = currentNbTables;
    }

    public String getCurrentPassword1() {
        return currentPassword1;
    }

    public void setCurrentPassword1(String currentPassword1) {
        this.currentPassword1 = currentPassword1;
    }

    public String getCurrentPassword2() {
        return currentPassword2;
    }

    public void setCurrentPassword2(String currentPassword2) {
        this.currentPassword2 = currentPassword2;
    }

    public String getCurrentSql1() {
        return currentSql1;
    }

    public void setCurrentSql1(String currentSql1) {
        this.currentSql1 = currentSql1;
    }

    public String[] getCurrentTables() {
        return currentTables;
    }

    public void setCurrentTables(String[] currentTables) {
        this.currentTables = currentTables;
    }

    public String getCurrentUrl1() {
        return currentUrl1;
    }

    public void setCurrentUrl1(String currentUrl1) {
        this.currentUrl1 = currentUrl1;
    }

    public String getCurrentUrl2() {
        return currentUrl2;
    }

    public void setCurrentUrl2(String currentUrl2) {
        this.currentUrl2 = currentUrl2;
    }

    public String getCurrentUser1() {
        return currentUser1;
    }

    public void setCurrentUser1(String currentUser1) {
        this.currentUser1 = currentUser1;
    }

    public String getCurrentUser2() {
        return currentUser2;
    }

    public void setCurrentUser2(String currentUser2) {
        this.currentUser2 = currentUser2;
    }

    public DataBase getDb1() {
        return db1;
    }

    public void setDb1(DataBase db1) {
        this.db1 = db1;
    }

    public DataBase getDb2() {
        return db2;
    }

    public void setDb2(DataBase db2) {
        this.db2 = db2;
    }

    public boolean isExecution() {
        return execution;
    }

    public void setExecution(boolean execution) {
        this.execution = execution;
    }

    public String[] getId() {
        return id;
    }

    public void setId(String[] id) {
        this.id = id;
    }

    

    public int getNbChamps() {
        return nbChamps;
    }

    public void setNbChamps(int nbChamps) {
        this.nbChamps = nbChamps;
    }

    public int getNbTables() {
        return nbTables;
    }

    public void setNbTables(int nbTables) {
        this.nbTables = nbTables;
    }

    public String[] getRelations() {
        return relations;
    }

    public void setRelations(String[] relations) {
        this.relations = relations;
    }

    public ResultSet getRs1() {
        return rs1;
    }

    public void setRs1(ResultSet rs1) {
        this.rs1 = rs1;
    }

    public ResultSet getRs2() {
        return rs2;
    }

    public void setRs2(ResultSet rs2) {
        this.rs2 = rs2;
    }

    public ResultSetMetaData getRsmd1() {
        return rsmd1;
    }

    public void setRsmd1(ResultSetMetaData rsmd1) {
        this.rsmd1 = rsmd1;
    }

    public ResultSetMetaData getRsmd2() {
        return rsmd2;
    }

    public void setRsmd2(ResultSetMetaData rsmd2) {
        this.rsmd2 = rsmd2;
    }

    public String getSqlMaxId() {
        return sqlMaxId;
    }

    public void setSqlMaxId(String sqlMaxId) {
        this.sqlMaxId = sqlMaxId;
    }

    public String getSqlMinId() {
        return sqlMinId;
    }

    public void setSqlMinId(String sqlMinId) {
        this.sqlMinId = sqlMinId;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

 public void processSqlMaitre(String definitionId, String filtre) {
        try {
            

            String sql = this.getCurrentSql1();
            DataBase db = new DataBase(this.getCurrentDriver1());
            db.open(this.getCurrentUrl1(), this.getCurrentUser1(), this.getCurrentPassword1());

           
            if (sql != null && !sql.trim().equals("")) {


                sql = (sql.toUpperCase() + " ").replaceAll("\\p{javaWhitespace}+", " ");

                //Verification de la fin de la requete
                if (filtre != null && !filtre.trim().equals("")) {
                    boolean semicolon = false;
                    boolean where = false;
                    String append = "";

                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length());
                        semicolon = true;
                    }
                    if (sql.toUpperCase().contains("WHERE")) {
                        where = true;
                    } else {
                        append = " WHERE ";
                    }
                    if (where) {
                        append += " AND " + filtre;
                    } else {
                        append += " " + filtre;
                        where = true;
                    }


                    sql += append;


                    if (semicolon) {
                        sql += ";";
                    }
                }
                this.setCurrentSql1(sql);
                this.doMigration();
                
                
            }
            
        } catch (Exception ex) {
            
        }

        return ;
    }

    public void stopMigrator() {
        this.setExecution(false);
    }

    private void getCurrentDefinition(String definitionId) {
        id = MigrationXmlReader.getId();
        for (int i = 0; i < id.length; i++) {
            if (id[i].equalsIgnoreCase(definitionId)) {
                currentId = MigrationXmlReader.getId()[i];
                currentLibelle = MigrationXmlReader.getLibelle()[i];
                currentDriver1 = MigrationXmlReader.getDriver1()[i];
                currentUser1 = MigrationXmlReader.getUser1()[i];
                currentPassword1 = MigrationXmlReader.getPassword1()[i];
                currentUrl1 = MigrationXmlReader.getUrl1()[i];
                currentSql1 = MigrationXmlReader.getSql1()[i];
                currentDriver2 = MigrationXmlReader.getDriver2()[i];
                currentUser2 = MigrationXmlReader.getUser2()[i];
                currentPassword2 = MigrationXmlReader.getPassword2()[i];
                currentUrl2 = MigrationXmlReader.getUrl2()[i];
                table = MigrationXmlReader.getTable()[i];
                IdExt = MigrationXmlReader.getIDEXT()[i];
                sqlMaxId = MigrationXmlReader.getSQLMAXID()[i];
                sqlMinId = MigrationXmlReader.getSQLMINID()[i];
                currentNbTables = MigrationXmlReader.getNbTables()[i];

                if (currentNbTables != null) {
                    nbTables = Integer.parseInt(currentNbTables);

                    currentTables = new String[nbTables];
                    for (int j = 1; j <= nbTables; j++) {
                        currentTables[j - 1] = MigrationXmlReader.getTableFiltres(i, j);
                    }

                }
            }
        }


    }
}
