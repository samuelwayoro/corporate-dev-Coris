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
public class ChequeNonCompRejFlexCubeReader extends FlatFileReader {

    public ChequeNonCompRejFlexCubeReader() {
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
            String refNoToPlainString = new BigDecimal(refNo).toPlainString();
            aCheque.setIdcheque(new BigDecimal(refNoToPlainString.substring(4)));
            aCheque.setMotifrejet(getChamp(3));
            //aCheque.setIdcheque(new BigDecimal(getChamp(18)));

            sql = "SELECT * FROM CHEQUES WHERE IDCHEQUE=" + aCheque.getIdcheque();

            //Retour du Refer Est ce que tous ces cheques sont ceux initiaux qui ont été holdés? si oui comment discrimer
            //2 choses a faire ramener a letat initial 800,1 ou 822,2 pour rejet
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            if (cheques != null && cheques.length > 0) {
          
                    sql = "UPDATE CHEQUES SET ETAT = " + Utility.getParam("CETAOPENONCOMREJ") + ", LOTSIB=2, MOTIFREJET=" + aCheque.getMotifrejet() + " WHERE BANQUE=BANQUEREMETTANT"
                            + " AND IDCHEQUE=" + cheques[0].getIdcheque();
                    db.executeUpdate(sql);
               

            } 

        }

        db.close();
        return aFile;
    }

}
