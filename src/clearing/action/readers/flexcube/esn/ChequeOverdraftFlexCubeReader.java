/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.flexcube.esn;

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
public class ChequeOverdraftFlexCubeReader extends FlatFileReader {

    public ChequeOverdraftFlexCubeReader() {
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
        while ((line = is.readLine().trim()) != null) {
            setCurrentLine(line);
            String refNo = getChamp(35);
            String refNoToPlainString = new BigDecimal(refNo).toPlainString(); //000000000
            aCheque.setIdcheque(new BigDecimal(refNoToPlainString.substring(4)));
            aCheque.setMotifrejet(getChamp(3));
            //aCheque.setIdcheque(new BigDecimal(getChamp(18)));

            sql = "SELECT * FROM CHEQUES WHERE IDCHEQUE=" + aCheque.getIdcheque();

            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
                sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPENONCOMREJ") + ", MOTIFREJET=" + aCheque.getMotifrejet() + " WHERE BANQUE=BANQUEREMETTANT"
                        + "  AND  IDCHEQUE=" + cheques[0].getIdcheque();
                //Hold du cheque, la remise entiere sera holdée dans le fichier de Credit

                db.executeUpdate(sql);

            } else {
            }

        }

        db.close();
        return aFile;
    }

}
