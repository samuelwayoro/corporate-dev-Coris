/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.flexcube;

import clearing.table.Comptes;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CompteNSIAReader extends FlatFileReader {

    public CompteNSIAReader() {

        setTattooProcessDate(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;
        String action = "";

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        if (repertoire.getPartenaire() != null && repertoire.getPartenaire().equalsIgnoreCase("TRUNCATE")) {
            db.executeUpdate("TRUNCATE TABLE COMPTES");
        }

        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {

            setCurrentLine(line);
            Comptes compte = new Comptes();
            action = getChamp(1);
            compte.setNumero(getChamp(12));

            compte.setAgence(getChamp(5));

            compte.setNom(getChamp(25));

            compte.setAdresse1(getChamp(25));
            
            compte.setAdresse2(getChamp(3));
            compte.setVille(getChamp(7));

            compte.setNumcptex(getChamp(19));

            switch (getChamp(1)) {

                case "0":
                    compte.setSignature1("J");
                    compte.setEtat(BigDecimal.ONE);
                    break;
                case "1":
                    compte.setSignature1("J1");
                    compte.setEtat(BigDecimal.ONE);
                    break;
                case "2":
                    compte.setSignature1("J2");
                    compte.setEtat(BigDecimal.ONE);
                    break;
            }

            switch (getChamp(1)) {
                case "0":
                    compte.setSignature2("R");
                    break;
                case "1":
                    compte.setSignature2("C");
                    break;

            }
            compte.setSignature3(getChamp(1));
            compte.setSignature4(getChamp(1));

            if (action.equalsIgnoreCase("C")) {
                db.insertObjectAsRowByQuery(compte, "COMPTES");
            }
            if (action.equalsIgnoreCase("M")) {
                db.updateRowByObjectByQuery(compte, "COMPTES", "NUMERO='" + compte.getNumero() + "'");
            }
            if (action.equalsIgnoreCase("A")) {
                compte.setEtat(new BigDecimal(10));
                db.updateRowByObjectByQuery(compte, "COMPTES", "NUMERO='" + compte.getNumero() + "'");
            }

        }
        db.close();
        return aFile;

    }

}
