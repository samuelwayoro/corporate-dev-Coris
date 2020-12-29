/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.atlas;

import clearing.table.Remises;
import java.io.File;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class FraisBceaoWriter extends FlatFileWriter {

    public FraisBceaoWriter() {
        setDescription("Frais Bceao Writer ");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
//select * from remises where idremise in (select remise from cheques where etat=50) and etat=50
        String sql = " SELECT * FROM REMISES WHERE ETAT='" + Utility.getParam("CETAREMENVSIB") + "' "
                + "  AND IDREMISE IN   ( SELECT REMISE FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEALLICOM1") + " ) ";

        Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
        db.close();
        if (remises != null & remises.length > 0) {
            String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("FRAIS_IN_FILE_ROOTNAME") + Utility.getParam("SIB_FILE_EXTENSION");
            setOut(createFlatFile(fileName));
            Long nbCheques = 0L;
            for (Remises remise : remises) {
                nbCheques += remise.getNbOperation().longValue();
            }
            for (Remises remise : remises) {
                StringBuilder line = new StringBuilder();
                line.append(Utility.bourrageDroite(remise.getCompteRemettant(), 20, " "));
                line.append(";");
                line.append(Utility.bourrageGauche(Utility.bourrageGauche(remise.getNbOperation().toPlainString().trim(), 3, "0"), 5, " "));
                line.append(";");
                line.append(Utility.convertDateToString(new Date(), "dd/MM/yy"));
                line.append(createBlancs(165, " "));
                wwriteln(line.toString());
            }
            closeFile();
            setDescription(getDescription() + " execute avec succes: Nombre de remises " + remises.length + " Nombre Total de Cheques :" + nbCheques);
            logEvent("INFO", "Nombre de remises " + remises.length + " Nombre Total de Cheques :" + nbCheques);
        }

    }

}
