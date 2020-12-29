/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.finacle;

import clearing.Main;
import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.RobotTask;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerBFIFinacleWriterBackup extends FlatFileWriter {

    public ChequeAllerBFIFinacleWriterBackup() {
        setDescription("Envoi des chèques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String ftpFolder = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            ftpFolder = param1[0];
        }
        String sftpIdRepertoire = "";
        String[] param2 = (String[]) getParametersMap().get("sftpIdRepertoire");
        if (param2 != null && param2.length > 0) {
            sftpIdRepertoire = param2[0];
        }
        System.out.println("sftpIdRepertoire = " + sftpIdRepertoire);

        RobotTask[] robotTasks = Main.getRobotTasks();

        if (robotTasks != null && param2 != null) {

            for (int i = 0; i < robotTasks.length; i++) {
                if (robotTasks[i].getRepertoire().getIdRepertoire().equals(new BigDecimal(sftpIdRepertoire))) {
                    RobotTask robotTask = robotTasks[i];
                    robotTask.getRepertoire().setPartenaire(robotTask.getRepertoire().getPartenaire() + "/" + ftpFolder + "/");

                }
            }
        }

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND  (MONTANTCHEQUE IS NULL OR TRIM(MONTANTCHEQUE)='')";
        db.executeUpdate(sql);
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        Cheques aCheque = null;
        String line = null;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i++) {

                //Tous les chq de la remise
                aCheque = cheques[i];
                line = "6" + aCheque.getAgenceremettant() + Utility.bourrageGauche(aCheque.getCompteremettant(), 10, "0")
                        + createBlancs(2, " ")
                        + "GNF"
                        + Utility.bourrageGauche(aCheque.getMontantcheque(), 17, " ") + " "
                        + Utility.bourrageDroite(aCheque.getNomemetteur(), 30, " ")
                        + Utility.bourrageGauche(aCheque.getNumerocheque(), 16, " ")
                        + Utility.bourrageGauche(aCheque.getMontantcheque(), 17, " ")
                        + createBlancs(3, " ")
                        + // Utility.bourrageGZero(CMPUtility.getAgencePrincipale(aCheque.getBanque()).substring(0, 2), 3)+ aCheque.getBanque().substring(2) + CMPUtility.getAgencePrincipale(aCheque.getBanque()).substring(2)+
                        "004" + aCheque.getBanque() + aCheque.getAgence()
                        + //createBlancs(9, " ")+
                        Utility.bourrageDroite(aCheque.getAgence(), 6, " ")
                        + Utility.bourrageDroite(aCheque.getBanque(), 6, " ")
                        + "YY" + createBlancs(433, " ");

                writeln(line);
                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                montantTotal += Long.parseLong(cheques[i].getMontantcheque());

            }

            setDescription(getDescription() + " exécuté avec succès: Nombre de Chèque= " + cheques.length + " - Montant Total= " + montantTotal);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + montantTotal);

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }
}
