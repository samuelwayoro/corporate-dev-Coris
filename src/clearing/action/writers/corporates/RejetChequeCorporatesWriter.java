/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.corporates;

import clearing.table.Cheques;
import java.io.File;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author patri
 */
public class RejetChequeCorporatesWriter extends FlatFileWriter {

    public RejetChequeCorporatesWriter() {
    }

    @Override
    public void execute() throws Exception {

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String sql = "SELECT * FROM CHEQUES_REJETES WHERE ETAT = " + Utility.getParam("CETAOPEANO") + " AND LOTSIB IS NULL ORDER BY ETABLISSEMENT";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        String line = "";
        if (cheques != null && cheques.length > 0) {

            String dateFichier = Utility.convertDateToString(new Date(), "ddMMyyyy");
            String heureFichier = Utility.convertDateToString(new Date(), "hhmmss");

            int j = 0;
            for (int i = 0; i < cheques.length; i += j) {
               

                sql = "SELECT * FROM CHEQUES_REJETES WHERE ETAT = " + Utility.getParam("CETAOPEANO")
                        + " AND LOTSIB IS NULL AND ETABLISSEMENT ='" + cheques[i].getEtablissement() + "'";
                Cheques[] chequesEtab = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + cheques[i].getEtablissement().trim()+ "-" + dateFichier
                        + "-" + heureFichier + ".REJ";
                setOut(createFlatFile(fileName));

                for (int k = 0; k < chequesEtab.length; k++) {
                    line = "";
                    line += Utility.bourrageGauche("" + chequesEtab[k].getOrigine(), 10, "0");
                    line += chequesEtab[k].getNumerocheque();
                    line += chequesEtab[k].getIban();
                    writeln(line);
                    
                }
                db.executeUpdate("UPDATE CHEQUES_REJETES SET LOTSIB=1 WHERE REMISE="+cheques[i].getRemise());
                closeFile();
                j = chequesEtab.length;

            }

        }

        db.close();
    }

}
