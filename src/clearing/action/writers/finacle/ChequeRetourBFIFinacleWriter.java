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
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeRetourBFIFinacleWriter extends FlatFileWriter {

    public ChequeRetourBFIFinacleWriter() {
        setDescription("Envoi des chèques retour vers le SIB");
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

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ORDER BY IDCHEQUE  ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") ORDER BY IDCHEQUE ";
        Cheques[] chequesRejetes = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        String chekrej = " ";
        int j = 0;
        long montantTotal = 0;
        int x = 0;
        long montantTotalChequesRejetes = 0;
        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques retours
                String line = "";
                //line += Utility.bourrageGZero(cheque.getAgenceremettant().substring(0, 2), 3)+ cheque.getBanqueremettant().substring(2) + cheque.getAgenceremettant().substring(2); //sort code remettant
                line += "004" + cheque.getBanqueremettant() + cheque.getAgenceremettant();
                //line += Utility.bourrageGZero(CMPUtility.getAgencePrincipale(cheque.getBanqueremettant()).substring(0, 2), 3)+ cheque.getBanqueremettant().substring(2) + CMPUtility.getAgencePrincipale(cheque.getBanqueremettant()).substring(2); //sort code remettant
                line += Utility.getParam("SORTCODE");
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "ddMMyy");
                line += Utility.bourrageGZero(cheque.getMontantcheque(), 13);
                line += Utility.bourrageGauche(cheque.getNumerocheque(), 8, "0");//
                line += Utility.bourrageGZero(cheque.getIdcheque().toPlainString(), 10);
                line += getTypeChequeCode(cheque);

                if (getTypeCheque(cheque).equalsIgnoreCase("DDS")) {
                    line += "GNF0";
                } else {
                    line += "6" + cheque.getAgence();
                }
                line += cheque.getNumerocompte();
                line += " " + getTypeCheque(cheque);
                line += createBlancs(8, " ");
                line += Utility.bourrageDroite(cheque.getAgenceremettant(), 6, " ");
                line += Utility.bourrageDroite(cheque.getBanqueremettant(), 6, " ");
                line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 50, " ");
                line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 50, " ");
                line += Utility.bourrageGauche(cheque.getNumerocheque(), 8, "0");//
                line += createBlancs(42, " ");
                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "ddMMyyyy");

                writeln(line);
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) { // //CETAOPERETRECENVSIB      170 // CETAOPERETENVSIB  172   
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                } else {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }
            if (chequesRejetes != null && 0 < chequesRejetes.length) {
                System.out.println("chequesRejetes "+chequesRejetes.length);
                for (int i = 0; i < chequesRejetes.length; i++) {
                    Cheques chequeRjt = chequesRejetes[i];
                    //Tous les cheques retours rejetes
                    String line = "";
                    //line += Utility.bourrageGZero(cheque.getAgenceremettant().substring(0, 2), 3)+ cheque.getBanqueremettant().substring(2) + cheque.getAgenceremettant().substring(2); //sort code remettant
                    line += "004" + chequeRjt.getBanqueremettant() + chequeRjt.getAgenceremettant();
                    //line += Utility.bourrageGZero(CMPUtility.getAgencePrincipale(cheque.getBanqueremettant()).substring(0, 2), 3)+ cheque.getBanqueremettant().substring(2) + CMPUtility.getAgencePrincipale(cheque.getBanqueremettant()).substring(2); //sort code remettant
                    line += Utility.getParam("SORTCODE");
                    line += Utility.convertDateToString(Utility.convertStringToDate(chequeRjt.getDatecompensation(), ResLoader.getMessages("patternDate")), "ddMMyy");
                    line += Utility.bourrageGZero(chequeRjt.getMontantcheque(), 13);
                    line += Utility.bourrageGauche(chequeRjt.getNumerocheque(), 8, "0");//
                    line += Utility.bourrageGZero(chequeRjt.getIdcheque().toPlainString(), 10);
                    line += getTypeChequeCode(chequeRjt);

                    if (getTypeCheque(chequeRjt).equalsIgnoreCase("DDS")) {
                        line += "GNF0";
                    } else {
                        line += "6" + chequeRjt.getAgence();
                    }
                    line += chequeRjt.getCompteremettant();
                    line += " " + getTypeCheque(chequeRjt);
                    line += createBlancs(8, " ");
                    line += Utility.bourrageDroite(chequeRjt.getAgenceremettant(), 6, " ");
                    line += Utility.bourrageDroite(chequeRjt.getBanqueremettant(), 6, " ");
                    line += Utility.bourrageDroite(chequeRjt.getNombeneficiaire(), 50, " ");
                    line += Utility.bourrageDroite(chequeRjt.getNombeneficiaire(), 50, " ");
                    line += Utility.bourrageGauche(chequeRjt.getNumerocheque(), 8, "0");//
                    line += createBlancs(42, " ");
                    line += Utility.convertDateToString(Utility.convertStringToDate(chequeRjt.getDatecompensation(), ResLoader.getMessages("patternDate")), "ddMMyyyy");

                    writeln(line);

                    if (chequeRjt.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))) {
                        chequeRjt.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                        db.updateRowByObjectByQuery(chequeRjt, "CHEQUES", "IDCHEQUE=" + chequeRjt.getIdcheque());
                    }
                    montantTotalChequesRejetes += Long.parseLong(chequeRjt.getMontantcheque());
                }
                // String chekrej=" Nombre de cheques Rejetés par les Confreres ="+chequesRejetes.length+ " - Montant Total= " + montantTotalChequesRejetes"";

                if (montantTotalChequesRejetes > 0) {
                    chekrej = " Nombre de cheques Rejetés par les Confreres =" + chequesRejetes.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalChequesRejetes)  ;
                }
                System.out.println("Fin des cheques rejetes");
            }

            setDescription(getDescription() + " exécuté avec succès: Nombre de Chèque Retour= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal)  + "" + (chekrej.trim().isEmpty() ? "" : "\r\n" + chekrej) +" Nom de Fichier :"+fileName);
            logEvent("INFO", "Nombre de Chèque Retour= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + "" + (chekrej.trim().isEmpty() ? "" : "\r\n" + chekrej)+" Nom de Fichier :"+fileName);

            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

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
