/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.orion;

import clearing.table.Prelevements;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class RejetOrionPrelevementReader extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null, sql = null;

        Prelevements aPrelevement = new Prelevements();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());


        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            //aCheque.setDatetraitement(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
            if (!getChamp(6).equals("<>0192")) {
                getChamp(82);
                aPrelevement.setMotifrejet(Utility.getParamNameOfType(getChamp(2), "CODE_REJET"));
                aPrelevement.setIdprelevement(new BigDecimal(getChamp(16)));

                sql = "UPDATE PRELEVEMENTS SET ETAT = " + Utility.getParam("CETAOPEALLICOM2")
                        + " MOTIFREJET=" + aPrelevement.getMotifrejet() + " WHERE IDPRELEVEMENT=" + aPrelevement.getIdprelevement();

                db.executeUpdate(sql);
            }

        }

        db.close();
        return aFile;

    }
}
