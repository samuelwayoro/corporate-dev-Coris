/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

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
public class PrelevementRetourWriter extends FlatFileWriter {

    public PrelevementRetourWriter() {
        setDescription("Envoi des prélèvements retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

       


        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteur;

        compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTRERET", "PRERET"), 4, "0");
       


// Population
        String sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ")  ORDER BY NUMEROCOMPTE_BENEFICIAIRE";
        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
        int j = 0;
        long montantTotalFichier = 0;
        Prelevements[] prelevementsCaisse = null;
       

        Prelevements aPrelevement = null;

        if (prelevements != null && 0 < prelevements.length) {

            StringBuffer line;
            for (int i = 0; i < prelevements.length; i += j) {
                long montantTotal = j = 0;
                //Tous les prelevements non compensables validés d'une remise
                sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ") ";
                prelevementsCaisse = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());

                if (prelevementsCaisse != null && 0 < prelevementsCaisse.length) {
                    long sumRemiseCaisse = 0;

                    j = prelevementsCaisse.length;
                    //Fichier de prelevement interne
                    String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.bourrageGauche(Utility.getParam("CODORGPRERET"), 5, "0") + Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy")+".pint";
                    setOut(createFlatFile(fileName));
                    line = new StringBuffer();
                    line.append("04100001");
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
                    line.append(Utility.getParam("CODE_BANQUE_SICA3").substring(2));
                    line.append("99111");
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTORGPRERET"), 11, " "));
                    line.append(Utility.bourrageDroite("ORGANISME PRELEVEMENT RECU", 24, " "));

                    line.append(Utility.bourrageGauche(Utility.getParam("CODORGPRERET"), 5, "0"));
                    line.append(" ");
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
                    line.append(createBlancs(59, " "));
                    writeln(line.toString());
                    for (int x = 0; x < prelevementsCaisse.length; x++) {
                        sumRemiseCaisse += Long.parseLong(prelevementsCaisse[x].getMontantprelevement());
                        //TODO Credit du compte client organisme au detail
                        line = new StringBuffer();
                        line.append("042");
                        line.append(Utility.bourrageGZero("" + (x + 2), 5));
                        line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
                        line.append(prelevementsCaisse[x].getBanque().substring(2));
                        line.append(prelevementsCaisse[x].getAgence());
                        line.append(Utility.bourrageDroite(prelevementsCaisse[x].getNumerocompte_Tire().substring(1), 11, " "));
                        line.append(Utility.bourrageDroite(prelevementsCaisse[x].getNom_Tire(), 24, " "));
                        line.append(Utility.bourrageDroite("BANQUE", 24, " "));
                        line.append(Utility.bourrageDroite(prelevementsCaisse[x].getNom_Beneficiaire().trim() + " " + prelevementsCaisse[x].getLibelle(), 30, " "));
                        line.append(Utility.bourrageGZero(prelevementsCaisse[x].getMontantprelevement(), 12));
                        line.append(createBlancs(4, " "));
                        line.append("XOF");
                        writeln(line.toString());
                        if (prelevementsCaisse[x].getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                            prelevementsCaisse[x].setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                            prelevementsCaisse[x].setZoneinterbancaire_Tire(Utility.bourrageGZero("" + (x + 2), 5));
                            prelevementsCaisse[x].setReference_Emetteur(Utility.bourrageGZero("" + (x + 2), 5));
                            db.updateRowByObjectByQuery(prelevementsCaisse[x], "PRELEVEMENTS", "IDPRELEVEMENT=" + prelevementsCaisse[x].getIdprelevement());
                        }

                    }
                    montantTotal += sumRemiseCaisse;
                    getOut().close();
                }
                montantTotalFichier = montantTotal;
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
