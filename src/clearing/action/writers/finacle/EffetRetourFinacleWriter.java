/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.finacle;

import clearing.Main;
import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Effets;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.RobotTask;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class EffetRetourFinacleWriter extends FlatFileWriter {

    public EffetRetourFinacleWriter() {
        setDescription("Envoi des effets retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();


        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        System.out.println("JDBCXmlReader.getUrl() : " + JDBCXmlReader.getUrl());
        String ftpFolder = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1"); //ramene le Map des parametres d'un Writer
        if (param1 != null && param1.length > 0) {
            ftpFolder = param1[0];
        }
        System.out.println("");
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



        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        int j = 0;
        long montantTotal = 0;
        if (effets != null && 0 < effets.length) {

            for (int i = 0; i < effets.length; i++) {
                Effets effet = effets[i];
                //Tous les effets retour
                String line = "";
                line += Utility.bourrageGZero(CMPUtility.getAgencePrincipale(effet.getBanqueremettant()).substring(0, 2), 3) + effet.getBanqueremettant().substring(2) + CMPUtility.getAgencePrincipale(effet.getBanqueremettant()).substring(2); //sort code remettant
                line += Utility.getParam("SORTCODE");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatecompensation(), ResLoader.getMessages("patternDate")), "ddMMyy");
                line += Utility.bourrageGZero(effet.getMontant_Effet(), 13);
                line += Utility.bourrageGauche(effet.getNumeroeffet(), 8, "0");
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 10);
                line += ("02");//Code de transaction  Constante obligatoire (02)
                line += effet.getNumerocompte_Tire();
                line += createBlancs(1, " ");
                line += Utility.bourrageDroite("WS", 3, " ");
                line += createBlancs(8, " ");//Zone SOLID	Optionnel (remplir avec des Blanc) 
                line += Utility.bourrageDroite(effet.getAgenceremettant(), 6, " ");
                line += Utility.bourrageDroite(effet.getBanqueremettant(), 6, " ");
                line += Utility.bourrageDroite(effet.getNom_Tire(), 50, " ");
                line += Utility.bourrageDroite(effet.getNom_Tire(), 50, " ");
                line += Utility.bourrageGauche(effet.getNumeroeffet(), 8, "0");
                line += createBlancs(42, " ");//
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatecompensation(), ResLoader.getMessages("patternDate")), "ddMMyyyy");

//                         line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatecompensation(), ResLoader.getMessages("patternDate")), "dd/MM/yyyy");
                //Commenté nouvelle spec UBA 22/08/2016
//                line += " ";//RejType	1eposition	a blanc
//                line += "  "; //RejectCode1 a blanc
//                line += "        "; //OutRejectZoneSolId a blanc
////               


                writeln(line);
                if (effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                } else if (effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPERETREC"))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                } else if (effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }
                montantTotal += Long.parseLong(effet.getMontant_Effet());
            }

            setDescription(getDescription() + " exécuté avec succès: Nombre d'Effets = " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            logEvent("INFO", "Nombre d'Effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }

    public String getTypeCheque(Cheques cheque) {

        if (cheque.getNumerocompte().contains(Utility.getParam("SOLID").trim())) {
            return "DDS";
        } else {
            return "CHQ";
        }
    }

    public String getTypeChequeCode(Cheques cheque) {

        if (cheque.getNumerocompte().contains(Utility.getParam("SOLID").trim())) {
            return "03";
        } else {
            return "01";
        }
    }
}
