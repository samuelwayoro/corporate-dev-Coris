/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.corporates;

import clearing.table.Cheques;
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
public class RejetSicaPrelevementCorporatesReader extends FlatFileReader {

    public RejetSicaPrelevementCorporatesReader() {
        setHasNormalExtension(false);
        setExtensionType(END_EXT);
    }

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

            aPrelevement.setIdprelevement(new BigDecimal(getChamp(10)));
            aPrelevement.setNumeroprelevement(getChamp(7));
            aPrelevement.setMotifrejet(getChamp(3));

            sql = "UPDATE PRELEVEMENTS SET ETAT = " + Utility.getParam("CETAOPEREJRETENVSIB") + ", MOTIFREJET=" + aPrelevement.getMotifrejet()
                    + " WHERE IDPRELEVEMENT=" + aPrelevement.getIdprelevement() + " AND NUMEROPRELEVEMENT='" + aPrelevement.getNumeroprelevement()+ "'";

            db.executeUpdate(sql);
            
        }

        db.close();
        return aFile;
    }

}
