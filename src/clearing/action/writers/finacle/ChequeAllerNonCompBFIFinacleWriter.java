/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.finacle;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerNonCompBFIFinacleWriter extends FlatFileWriter {

    public ChequeAllerNonCompBFIFinacleWriter() {
        setDescription("Envoi des chèques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");

        String sql = "UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE ETAT=" + Utility.getParam("CETAOPEVALSURCAI") + " AND  (MONTANTCHEQUE IS NULL OR TRIM(MONTANTCHEQUE)='')";
        db.executeUpdate(sql);
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVALSURCAI") + " AND BANQUE IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            for (Cheques cheque : cheques) {
                
                String line;

                line = Utility.bourrageGauche(cheque.getCompteremettant(), 12, "0")
                        + createBlancs(4, " ") + "GNF"
                        + Utility.bourrageGauche(cheque.getMontantcheque(), 17, "0") + " "
                        + Utility.bourrageDroite(cheque.getNombeneficiaire(), 30, " ")
                        + Utility.bourrageGauche(cheque.getNumerocheque(), 7, "0") + " "
                        + Utility.bourrageGauche(cheque.getMontantcheque(), 17, "0")
                        + createBlancs(12, " ")
                        + //aCheque.getAgence()+" "+
                        CMPUtility.getAgencePrincipale(cheque.getBanque()) + " "
                        + cheque.getBanque() + " "
                        + "Y" + createBlancs(16, " ");
                writeln(line);
                cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());

                //Creation Ligne remise a partir du premier chq
                montantTotal += Long.parseLong(cheque.getMontantcheque());

                db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + cheque.getRemise());
            }
            setDescription(getDescription() + " execute avec succes: Nombre de Cheque= " + cheques.length + " - Montant Total= " + montantTotal);
            logEvent("INFO", "Nombre de Cheque= " + cheques.length + " - Montant Total= " + montantTotal);
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
