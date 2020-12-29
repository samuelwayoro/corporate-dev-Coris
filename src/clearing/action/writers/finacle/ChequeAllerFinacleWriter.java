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
public class ChequeAllerFinacleWriter extends FlatFileWriter {

    public ChequeAllerFinacleWriter() {
        setDescription("Envoi des chèques vers le SIB");
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
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE ETAT=" + Utility.getParam("CETAOPEVAL2") + " AND  (MONTANTCHEQUE IS NULL OR TRIM(MONTANTCHEQUE)='')";
        db.executeUpdate(sql);
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPEVAL2") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques compensables validés d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT =" + Utility.getParam("CETAOPEVAL2") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "')";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                //La remise en question
                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                //Tous les cheques de la remise (compensables et non)
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT >=" + Utility.getParam("CETAOPEVAL2");
                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;

                if ((remises != null && 0 < remises.length) &&
                        (allChequesVal.length == remises[0].getNbOperation().intValue())) {
                    if (chequesVal != null && 0 < chequesVal.length) {
                        long sumRemise = 0;
                        String line;

                        //Creation Ligne remise a partir du premier chq
                        Cheques aCheque = chequesVal[0];
                        for (int x = 0; x < chequesVal.length; x++) {
                            sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;

                        //Creation ligne de chèque
                        for (int x = 0; x < chequesVal.length; x++) {
                            //Tous les chq de la remise
                            aCheque = chequesVal[x];
                            line = Utility.bourrageGauche(aCheque.getCompteremettant(), 12, "0") +
                                    createBlancs(4, " ") + "XOF" +
                                    Utility.bourrageGauche(aCheque.getMontantcheque(), 17, " ") + " " +
                                    Utility.bourrageDroite(aCheque.getNomemetteur(), 30, " ") +
                                    //Utility.bourrageDroite(aCheque.getNombeneficiaire(),20, " ")+" NC"+Utility.bourrageGauche(aCheque.getNumerocheque(), 7, "0")+
                                    Utility.bourrageGauche(aCheque.getNumerocheque(), 16, " ") +
                                    Utility.bourrageGauche(aCheque.getMontantcheque(), 17, " ") +
                                    createBlancs(3, " ") +
                                    Utility.bourrageGZero(aCheque.getAgence().substring(0, 2), 3) + aCheque.getBanque().substring(2) + aCheque.getAgence().substring(2) +
                                    aCheque.getAgence() + " " +
                                    aCheque.getBanque() + " " +
                                    ((x == chequesVal.length - 1) ? "Y" : "N") +
                                    ((i == cheques.length - 1) ? "Y" : "N") + createBlancs(433, " ");

                            writeln(line);
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());

                        }
                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }

                } else {

                    db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }


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
