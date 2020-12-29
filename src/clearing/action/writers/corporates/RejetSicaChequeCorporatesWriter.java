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
public class RejetSicaChequeCorporatesWriter extends FlatFileWriter {

    public RejetSicaChequeCorporatesWriter() {
    }

    @Override
    public void execute() throws Exception {
       

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEREJRET") + " AND TRIM(ETABLISSEMENT) IN (SELECT trim(CODEETABLISSEMENT) FROM ETABLISSEMENTS) ORDER BY ETABLISSEMENT";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        String line = "";
        if (cheques != null && cheques.length > 0) {

            String dateFichier = Utility.convertDateToString(new Date(), "ddMMyyyy");
            String heureFichier = Utility.convertDateToString(new Date(), "hhmmss");

            int j = 0;
            for (int i = 0; i < cheques.length; i += j) {

                sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEREJRET") //liste des cheques rejetés en retours compense 
                        + "  AND ETABLISSEMENT ='" + cheques[i].getEtablissement() + "'";
                Cheques[] chequesEtab = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + cheques[i].getEtablissement().trim() + "-" + dateFichier
                        + "-" + heureFichier + ".REJALL";
                setOut(createFlatFile(fileName));
                System.out.println("fileName Creation de  " + fileName);

                for (int k = 0; k < chequesEtab.length; k++) {
                    line = "";
                    line += Utility.bourrageGauche("" + chequesEtab[k].getOrigine(), 10, "0");
                    line += chequesEtab[k].getNumerocheque();
                    line += chequesEtab[k].getMotifrejet();
                    writeln(line);

                }

                closeFile();
                j = chequesEtab.length;
                System.out.println("fileName Fin de  " + fileName);

            }

        }
        
        db.close();
    }

}
