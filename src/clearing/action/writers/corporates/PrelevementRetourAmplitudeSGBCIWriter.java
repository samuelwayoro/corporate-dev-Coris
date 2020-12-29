/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.corporates;

import clearing.model.CMPUtility;
import clearing.table.Prelevements;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.bean.table.Fichiers;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class PrelevementRetourAmplitudeSGBCIWriter extends FlatFileWriter {

    public PrelevementRetourAmplitudeSGBCIWriter() {
        setDescription("Envoi de prélèvements retour vers NSIA");
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
        String compteur;





// Population
        String sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ", " + Utility.getParam("CETAOPERETREC") + ")  ORDER BY REFERENCE_EMETTEUR";
        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
        int j = 0;
        long montantTotalFichier = 0;

        Prelevements[] prelevementsComp = prelevements;
        Fichiers[] fichiers = null;
        Prelevements aPrelevement = null;

        if (prelevements != null && 0 < prelevements.length) {
            compteur = Utility.bourrageGauche("" + Utility.computeCompteur("NBFICPREL", Utility.getParam("DATECOMPENS_NAT")), 3, "0");
            String prelFileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + CMPUtility.getCodeBanqueSica3() + "__" + Utility.getParam("CODEMEPREL") + "__" + CMPUtility.getDate() + "_" + compteur + Utility.getParam("SIB_FILE_EXTENSION");
            setOut(createFlatFile(prelFileName));

            StringBuffer line;


            long montantTotal = j = 0;

            long sumRemiseComp = 0;
            long sumCompte = 0;

            String strLine = "";
            strLine += "03100001";
            strLine += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy");
            strLine += Utility.getParam("CODE_BANQUE_SICA3").substring(2);
            strLine += Utility.bourrageGauche(Utility.getParam("AGECREPREL"), 5, "0");
            strLine += Utility.bourrageGauche(Utility.getParam("COMCREPREL"), 9, "0");
            strLine += createBlancs(2, " ");
            strLine += Utility.bourrageDroite(prelevements[0].getNom_Beneficiaire(), 24, " ");
            strLine += Utility.bourrageGauche(Utility.getParam("CODEMEPREL"), 5, "0");
            strLine += " ";
            strLine += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy");
            strLine += createBlancs(59, " ");
            writeln(strLine);

            sumCompte = Long.parseLong(Utility.bourrageGauche(Utility.getParam("COMCREPREL"), 9, "0"));

            for (int x = 0; x < prelevements.length; x++) {
                sumRemiseComp += Long.parseLong(prelevementsComp[x].getMontantprelevement());
                sumCompte += Long.parseLong(Utility.bourrageDroite(prelevementsComp[x].getNumerocompte_Tire().substring(1),9,"0"));;
                line = new StringBuffer();
                line.append("032");
                String sequence = Utility.bourrageGZero("" + (x + 2), 5);
                line.append(sequence);
                line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
                line.append(prelevementsComp[x].getBanque().substring(2));
                line.append("00").append(prelevementsComp[x].getAgence().substring(2));
                line.append(Utility.bourrageDroite(prelevementsComp[x].getNumerocompte_Tire().substring(1),9,"0"));
                line.append(createBlancs(2, " "));
                line.append(Utility.bourrageDroite(prelevementsComp[x].getNom_Tire(), 24, " "));
//                sql = "SELECT * FROM BANQUES WHERE CODEBANQUE='" + prelevementsComp[x].getBanque() + "'";
//                Banques banques[] = (Banques[]) db.retrieveRowAsObject(sql, new Banques());
//                if (banques != null && banques.length > 0) {
//                    line.append(Utility.bourrageDroite(banques[0].getLibellebanque(), 17, " "));
//                } else {
//                    line.append(Utility.bourrageDroite("BANQUE", 17, " "));
//                }

                line.append(Utility.bourrageDroite(prelevementsComp[x].getBanqueremettant(), 17, " "));
                line.append(Utility.bourrageDroite(prelevementsComp[x].getLibelle(), 30, " "));
                line.append(Utility.bourrageGZero(prelevementsComp[x].getMontantprelevement(), 12));
                line.append(prelevementsComp[x].getMotifrejet() == null ? "00" : Utility.getParamOfType(prelevementsComp[x].getMotifrejet(), "CODE_REJET"));
                line.append(createBlancs(10, " "));

                writeln(line.toString());

                prelevementsComp[x].setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                prelevementsComp[x].setLotsib(new BigDecimal(2));
                prelevementsComp[x].setReference(sequence);
                db.updateRowByObjectByQuery(prelevementsComp[x], "PRELEVEMENTS", "IDPRELEVEMENT=" + prelevementsComp[x].getIdprelevement());

            }


            montantTotal += sumRemiseComp;

            line = new StringBuffer();
            line.append("039");
            line.append(Utility.bourrageGZero("" + (prelevementsComp.length + 2), 5));
            line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
            line.append(createBlancs(8, " "));
            line.append(Utility.bourrageGauche("" + sumCompte, 10, " "));
            line.append(createBlancs(71, " "));
            line.append(Utility.bourrageGZero("" + sumRemiseComp, 12));
            line.append(createBlancs(12, " "));
            writeln(line.toString());
            closeFile();


            montantTotalFichier += montantTotal;



            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Prelevement= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));
            logEvent("INFO", "Nombre de Prelevement= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));


        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
