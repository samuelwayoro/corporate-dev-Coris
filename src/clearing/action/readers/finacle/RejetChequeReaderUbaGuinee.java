/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.finacle; //clearing.action.readers.finacle.RejetChequeReader

import clearing.table.Cheques;
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
public class RejetChequeReaderUbaGuinee extends FlatFileReader {

    public RejetChequeReaderUbaGuinee() {
        setHasNormalExtension(false);
        setExtensionType(END_EXT);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null, sql = null;

        Cheques aCheque = new Cheques();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            getChamp(3);
            aCheque.setMotifrejet(Utility.getParamNameOfType(getChamp(2), "CODE_REJET"));
            aCheque.setIdcheque(new BigDecimal(getChamp(18)));

            sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPEALLICOM2")
                    + ", MOTIFREJET=" + aCheque.getMotifrejet() + " WHERE IDCHEQUE=" + aCheque.getIdcheque();

            db.executeUpdate(sql);
        }
        is.close();
        db.close();
        return aFile;
    }

}
