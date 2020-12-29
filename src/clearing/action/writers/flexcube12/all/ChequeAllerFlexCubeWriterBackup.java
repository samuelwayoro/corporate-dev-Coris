/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube12.all;

import clearing.action.writers.flexcube.*;
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
public class ChequeAllerFlexCubeWriterBackup extends FlatFileWriter {

    public ChequeAllerFlexCubeWriterBackup() {
        setDescription("Envoi des chèques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;
        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur = " + dateValeur);

        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQALE", "CHQALE"), 4, "0");
            isEcobankStandard = false;
        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "a" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQALE", "CHQALE"), 3, "0");
            isEcobankStandard = false;
        } else {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQALE", "CHQALE"), 4, "0");
            isEcobankStandard = true;
        }

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");

        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");
// Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ","
                + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") "
                + " AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;

        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            if (isEcobankStandard) {
                line.append(createBlancs(76, " "));
            }
            writeln(line.toString());

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques compensables validés d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;

                if (chequesVal != null && 0 < chequesVal.length) {
                    long sumRemise = 0;

                    Cheques aCheque = chequesVal[0];
                    for (int x = 0; x < chequesVal.length; x++) {
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                    }
                    montantTotal += sumRemise;

                    for (int x = 0; x < chequesVal.length; x++) {
                        aCheque = chequesVal[x];

                        line = new StringBuffer();
                        line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()));
                        line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"0"), 16, " "));
                        line.append(createBlancs(4, " "));
                        line.append("C");
                        line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                        line.append("Q13");
                        line.append(dateValeur);
                        line.append(" ");
                        line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                        line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 18, " "));
                        line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
                        line.append("030");
                        line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));
                        writeln(line.toString());
                        if (aCheque.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACC")))) {
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }
                    }

                    //  }
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                }

                /*} else {

            db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
            }*/
            }
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTATTCHQALEFLEX"), 16, " ")); //Utility.bourrageDroite(Utility.getParam("CPTATTCHQALEFLEX"), 16, " ")
            line.append(createBlancs(11, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, "0"));
            line.append("Q13");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
