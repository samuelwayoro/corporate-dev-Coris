/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.finacle;

import clearing.Main;
import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.RobotTask;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerRejeteFinacleWriter extends FlatFileWriter {

    public ChequeAllerRejeteFinacleWriter() {
        setDescription("Envoi des Rejets de Chèques Aller vers le SIB");
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


        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques retours
                String line = "";
                //line += Utility.bourrageGZero(cheque.getAgenceremettant().substring(0, 2), 3)+ cheque.getBanqueremettant().substring(2) + cheque.getAgenceremettant().substring(2); //sort code remettant
                line += Utility.bourrageGZero(CMPUtility.getAgencePrincipale(cheque.getBanqueremettant()).substring(0, 2), 3) + cheque.getBanqueremettant().substring(2) + CMPUtility.getAgencePrincipale(cheque.getBanqueremettant()).substring(2); //sort code remettant
                line += Utility.getParam("SORTCODE");
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "ddMMyy");
                line += Utility.bourrageGZero(cheque.getMontantcheque(), 13);
                line += Utility.bourrageGauche(cheque.getNumerocheque(), 8, "0");//
                line += Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 10);
                line += getTypeChequeCode(cheque);
                line += cheque.getCompteremettant();
                line += createBlancs(1, " ") + getTypeCheque(cheque);
                line += createBlancs(8, " ");
                line += Utility.bourrageDroite(cheque.getAgenceremettant(), 6, " ");
                line += Utility.bourrageDroite(cheque.getBanqueremettant(), 6, " ");
                line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 50, " ");
                line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 50, " ");
                line += Utility.bourrageGauche(cheque.getNumerocheque(), 8, "0");//
                line += createBlancs(42, " ");
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "ddMMyyyy");
                //Commenté nouvelle spec UBA 22/08/2016
//////
//////                //Ajouté selon la SPEC UBA produite
//////                line += "O";//RejType	1eposition	Toujours la lettre O 
//////                line += Utility.getParam(cheque.getMotifrejet()).trim();
//////                line += Utility.bourrageGauche("6299", 8, " ");

                writeln(line);

                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }

            setDescription(getDescription() + " exécuté avec succès: Nombre de Chèque(s)= " + cheques.length + " - Montant Total= " + montantTotal);
            logEvent("INFO", "Nombre de Chèque(s)= " + cheques.length + " - Montant Total= " + montantTotal);

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }

    public String getTypeCheque(Cheques cheque) {

        if (cheque.getCompteremettant().contains(Utility.getParam("SOLID").trim())) {
            return "DDS";
        } else {
            if ("567".contains("" + cheque.getCompteremettant().charAt(3))) {
                
                return "WS ";
            } else {
                return "CHQ";
            }



        }
    }

    public String getTypeChequeCode(Cheques cheque) {

        if (cheque.getCompteremettant().contains(Utility.getParam("SOLID").trim())) {
            return "03";
        } else {
            if ("567".contains("" + cheque.getCompteremettant().charAt(3))) {
                return "02";
            } else {
                return "01";
            }

        }
    }
}
