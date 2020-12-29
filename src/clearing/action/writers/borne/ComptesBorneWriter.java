/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.borne;

import clearing.table.Comptes;
import java.io.File;
import org.patware.action.file.FlatFileWriter;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author AUGOU Patrick
 */
public class ComptesBorneWriter extends FlatFileWriter {

    public ComptesBorneWriter() {
        setDescription("Envoi des comptes vers les bornes");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteFolder;
        Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("SELECT * FROM COMPTES", new Comptes());
        if (comptes != null && comptes.length > 0) {
            Repertoires[] repertoires = (Repertoires[]) db.retrieveRowAsObject("SELECT * FROM REPERTOIRES WHERE TACHE='clearing.action.readers.IRISImageChequeReader' ", new Repertoires());
            if (repertoires != null && repertoires.length > 0) {
                for (int i = 0; i < repertoires.length; i++) {
                    Repertoires repertoires1 = repertoires[i];
                    compteFolder = repertoires1.getChemin().substring(0, repertoires1.getChemin().lastIndexOf("\\"));
                    compteFolder = compteFolder + File.separator + Utility.getParam("NOMDOSCPTBOR");

                    if (comptes != null && comptes.length > 0) {
                        String fileName = compteFolder + File.separator + Utility.getParam("NOMFICCPTBOR");
                        setOut(createFlatFile(fileName));

                        for (int j = 0; j < comptes.length; j++) {
                            Comptes comptes1 = comptes[j];
                            String line = comptes1.getNumero() + Utility.getParam("SEPFICCPTBOR") + comptes1.getNom();
                            wwriteln(line);

                        }
                        closeFile();
                    }
                }
            }


        }
        db.close();
    }
}
