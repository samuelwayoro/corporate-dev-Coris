/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.corporates;

import clearing.table.Cheques;
import clearing.table.Remises;
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
public class RejetChequeCorporatesReader extends FlatFileReader {

    public RejetChequeCorporatesReader() {
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

            aCheque.setIdcheque(new BigDecimal(getChamp(10)));
            aCheque.setNumerocheque(getChamp(7));
            aCheque.setMotifrejet(getChamp(3));

            sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPEANO") + ", MOTIFREJET=" + aCheque.getMotifrejet()
                    + " WHERE IDCHEQUE=" + aCheque.getIdcheque() + " AND NUMEROCHEQUE='" + aCheque.getNumerocheque() + "'";

            db.executeUpdate(sql);
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject("SELECT * FROM CHEQUES WHERE IDCHEQUE =" + aCheque.getIdcheque(), new Cheques());
            Remises[] remises = (Remises[]) db.retrieveRowAsObject("SELECT * FROM REMISES WHERE IDREMISE =" + cheques[0].getRemise(), new Remises());
            if (db.executeUpdate("INSERT INTO CHEQUES_REJETES SELECT * FROM CHEQUES WHERE IDCHEQUE=" + aCheque.getIdcheque()) == 1) {
                db.executeUpdate("DELETE FROM CHEQUES WHERE IDCHEQUE=" + aCheque.getIdcheque());
            }
            remises[0].setNbOperation(remises[0].getNbOperation().subtract(BigDecimal.ONE));
            long montantRemise = Long.parseLong(remises[0].getMontant()) - Long.parseLong(cheques[0].getMontantcheque());
            remises[0].setMontant("" + montantRemise);

            if (montantRemise == 0) {
                remises[0].setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
            }

            db.updateRowByObjectByQuery(remises[0], "REMISES", "IDREMISE=" + remises[0].getIdremise());
        }

        db.close();
        return aFile;
    }

}
