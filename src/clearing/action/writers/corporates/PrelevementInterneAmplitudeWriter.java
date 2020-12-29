/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.corporates;

import clearing.table.Prelevements;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class PrelevementInterneAmplitudeWriter extends FlatFileWriter {

    public PrelevementInterneAmplitudeWriter() {
        setDescription("Envoi des prélèvements aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur = " + dateValeur);

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BigDecimal idFichierRemise = new BigDecimal(Utility.computeCompteur("IDFICHIER", "FICHIERS"));
        String sql = "UPDATE PRELEVEMENTS SET REMISE=" + idFichierRemise + " WHERE REMISE IS NULL AND ETAT=" + Utility.getParam("CETAOPEVALDELTA");
        db.executeUpdate(sql);
        String compteur;

        compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQALE", "CHQALE"), 4, "0");

// Population
        sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPEVALDELTA") + ") ";
        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());

        int j = 0;
        long montantTotalFichier = 0;
        Prelevements[] prelevementsCaisse = null;

        if (prelevements != null && 0 < prelevements.length) {

            StringBuffer line;
            for (int i = 0; i < prelevements.length; i += j) {
                long montantTotal = 0;
                //Tous les prelevements compensables et non compensables validés d'une remise
                sql = "SELECT * FROM PRELEVEMENTS WHERE Numerocompte_Beneficiaire=" + prelevements[i].getNumerocompte_Beneficiaire() + "  AND ETAT IN (" + Utility.getParam("CETAOPEVALDELTA") + ") ";
                prelevementsCaisse = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());

                if (prelevementsCaisse != null && 0 < prelevementsCaisse.length) {
                    long sumRemiseCaisse = 0;

                    j = prelevementsCaisse.length;
                    //Fichier de prelevement interne
                    String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.bourrageGauche(prelevements[i].getZoneinterbancaire_Beneficiaire(), 5, "0") + Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyHHmmss") + ".pint";
                    setOut(createFlatFile(fileName));
                    line = new StringBuffer();
                    line.append("PRL");
                    line.append(Utility.getParam("CODE_BANQUE_SICA3"));
                    line.append(Utility.bourrageDroite(prelevements[i].getBanqueremettant(), 5, "0"));
                    line.append(Utility.bourrageDroite(prelevements[i].getAgenceremettant(), 5, "0"));
                    line.append(Utility.bourrageGauche(prelevements[i].getNumerocompte_Beneficiaire(), 12, "0"));
                    line.append(Utility.computeCleRIB(prelevements[i].getBanqueremettant(), prelevements[i].getAgenceremettant(), prelevements[i].getNumerocompte_Beneficiaire()));
                    line.append(Utility.bourrageDroite(prelevements[i].getNom_Beneficiaire(), 35, " "));
                    line.append(Utility.bourrageGauche(prelevements[i].getZoneinterbancaire_Beneficiaire(), 10, "0"));
                    line.append("XOF");
                    line.append(Utility.bourrageGauche("" + prelevementsCaisse.length, 10, "0"));
                    for (int x = 0; x < prelevementsCaisse.length; x++) {
                        sumRemiseCaisse += Long.parseLong(prelevementsCaisse[x].getMontantprelevement());
                    }
                    line.append(Utility.bourrageGauche("" + sumRemiseCaisse, 16, "0"));

                    line.append(createBlancs(294, " "));

                    writeln(line.toString());
                    for (int x = 0; x < prelevementsCaisse.length; x++) {

                        //TODO Credit du compte client organisme au detail
                        line = new StringBuffer();
                        line.append(Utility.bourrageGZero(prelevementsCaisse[x].getMontantprelevement(), 16));
                        line.append(createBlancs(7, " "));
                        line.append(prelevementsCaisse[x].getBanque());
                        line.append(prelevementsCaisse[x].getAgence());
                        line.append(Utility.bourrageGauche(prelevementsCaisse[x].getNumerocompte_Tire(), 12, "0"));
                        line.append(Utility.computeCleRIB(prelevementsCaisse[x].getBanque(), prelevementsCaisse[x].getAgence(), prelevementsCaisse[x].getNumerocompte_Tire()));
                        line.append(Utility.bourrageDroite(prelevementsCaisse[x].getNom_Tire(), 50, " "));
                        //line.append(Utility.bourrageGZero("" + (x + 2), 10));
//                        line.append(Utility.bourrageGauche(prelevementsCaisse[x].getReference_Emetteur(), 10, "0"));
                        line.append(createBlancs(10, " "));
                        line.append(Utility.bourrageDroite(prelevementsCaisse[x].getReference_Emetteur(), 50, " "));
                        line.append(Utility.convertDateToString(Utility.convertStringToDate(prelevementsCaisse[x].getDateFinRecyclage(), "yyyyMMdd"), "dd/MM/yy"));
                        line.append(createBlancs(235, " "));

                        writeln(line.toString());
                        if (prelevementsCaisse[x].getEtat().toPlainString().equals(Utility.getParam("CETAOPEVALDELTA"))) {
                            prelevementsCaisse[x].setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                            prelevementsCaisse[x].setReference_Operation_Interne(prelevementsCaisse[x].getZoneinterbancaire_Tire());
                            db.updateRowByObjectByQuery(prelevementsCaisse[x], "PRELEVEMENTS", "IDPRELEVEMENT=" + prelevementsCaisse[x].getIdprelevement());
                        }

                    }
                    montantTotal += sumRemiseCaisse;
                    getOut().close();

                }
                montantTotalFichier += montantTotal;
            }

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Prelevement= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));
            logEvent("INFO", "Nombre de Prelevement= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));

            //closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
