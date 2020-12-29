/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.corporates;

import clearing.table.Prelevements;
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
public class RejetSicaPrelevementCorporatesWriter extends FlatFileWriter {

    public RejetSicaPrelevementCorporatesWriter() {
    }

    @Override
    public void execute() throws Exception {
       

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT = " + Utility.getParam("CETAOPEREJRET") + " AND TRIM(ETABLISSEMENT) IN (SELECT trim(CODEETABLISSEMENT) FROM ETABLISSEMENTS) ORDER BY ETABLISSEMENT";
        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());

        String line = "";
        if (prelevements != null && prelevements.length > 0) {

            String dateFichier = Utility.convertDateToString(new Date(), "ddMMyyyy");
            String heureFichier = Utility.convertDateToString(new Date(), "hhmmss");

            int j = 0;
            for (int i = 0; i < prelevements.length; i += j) {

                sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT = " + Utility.getParam("CETAOPEREJRET")
                        + "  AND ETABLISSEMENT ='" + prelevements[i].getEtablissement() + "'";
                Prelevements[] prelevementsEtab = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());

                String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + prelevements[i].getEtablissement().trim() + "-" + dateFichier
                        + "-" + heureFichier + ".REJALL";
                setOut(createFlatFile(fileName));
                System.out.println("fileName Creation de  " + fileName);

                for (int k = 0; k < prelevementsEtab.length; k++) {
                    line = "";
                    line += Utility.bourrageGauche("" + prelevementsEtab[k].getOrigine(), 10, "0");
                    line += prelevementsEtab[k].getNumeroprelevement();
                    line += prelevementsEtab[k].getMotifrejet();
                    writeln(line);

                }

                closeFile();
                j = prelevementsEtab.length;
                System.out.println("fileName Fin de  " + fileName);

            }

        }
        
        db.close();
    }

}
