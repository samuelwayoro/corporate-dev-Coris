/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.finacle;

import clearing.Main;
import clearing.model.CMPUtility;
import clearing.table.Virements;
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
public class VirementRetourBFIFinacleWriter extends FlatFileWriter {

    public VirementRetourBFIFinacleWriter() {
        setDescription("Envoi des virements retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

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

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");

        String sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        int j = 0;
        long sumVirements = 0;
        if (virements != null && 0 < virements.length) { //+" Nom de Fichier :"+fileName
            setOut(createFlatFile(fileName));

            for (int i = 0; i < virements.length; i++) {
                Virements virement = virements[i];
                //Tous les virements retours
                String line = "";
                line += Utility.bourrageDroite("6" + virement.getAgence() + virement.getNumerocompte_Beneficiaire(), 16, " ")
                        + virement.getDevise()
                        + Utility.bourrageDroite(Utility.getParam("FINACLE_BRANCH"), 8, " ") + "C";
                line += Utility.bourrageGauche(virement.getMontantvirement(), 17, " ");
                line += Utility.bourrageDroite(virement.getLibelle(), 30, " ") + createBlancs(63, " ");
                line += Utility.bourrageGauche(virement.getMontantvirement(), 17, " ") + virement.getDevise();
                sumVirements += Long.parseLong(virement.getMontantvirement());
                writeln(line);
                if (virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                } else {
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                }
            }

            String line = "";
            line += Utility.bourrageDroite(Utility.getParam("FINACLE_CPTATTVIR"), 16, " ")
                    + CMPUtility.getDevise()
                    + Utility.bourrageDroite(Utility.getParam("FINACLE_BRANCH"), 8, " ") + "D";
            line += Utility.bourrageGauche("" + sumVirements, 17, " ");
            line += Utility.bourrageDroite(Utility.getParamLabel("FINACLE_CPTATTVIR"), 30, " ") + createBlancs(63, " ");
            line += Utility.bourrageGauche("" + sumVirements, 17, " ") + CMPUtility.getDevise();
            writeln(line);
            closeFile();
            setDescription(getDescription() + " exécuté avec succès: Nombre de Virements= " + virements.length + " - Montant Total= " + sumVirements + " Nom de Fichier :" + fileName);
            logEvent("INFO", "Nombre de Virements= " + virements.length + " - Montant Total= " + sumVirements+ " Nom de Fichier :" + fileName);
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        db.close();
    }
}
