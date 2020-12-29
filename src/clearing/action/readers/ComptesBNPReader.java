/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers;

import clearing.table.Comptes;
import java.io.BufferedReader;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComptesBNPReader extends FlatFileReader {

    public ComptesBNPReader() {

        setTattooProcessDate(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;
        String action = "";

        Comptes compte = new Comptes();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

         db.execute("TRUNCATE TABLE COMPTES");
        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {


            setCurrentLine(line);

            compte.setNumero(getChamp(16));
            compte.setNumcptex(compte.getNumero());
            getChamp(4);
            compte.setAgence(getChamp(5));
            compte.setNom(getChamp(35));
            getChamp(18);
            action = getChamp(1);
           
            
            if (action.equalsIgnoreCase("O")) {
                db.insertObjectAsRowByQuery(compte, "COMPTES");
            }
            if (action.equalsIgnoreCase("N")) {
                db.executeUpdate("DELETE FROM COMPTES WHERE NUMERO='" + compte.getNumero() + "'");
            }


        }

        db.close();
        return aFile;

    }
}
