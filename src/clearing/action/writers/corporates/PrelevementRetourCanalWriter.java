/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.corporates;

import clearing.model.CMPUtility;
import clearing.table.Banques;
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
public class PrelevementRetourCanalWriter extends FlatFileWriter {

    public PrelevementRetourCanalWriter() {
        setDescription("Envoi des retour de prélèvements vers Canal");
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
        String sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ", " + Utility.getParam("CETAOPEREJRETENVSIB") + ", " + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ") AND LOTSIB=1 AND ETABLISSEMENT='CANAL' ORDER BY REMISE";
        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
        int j = 0;
        long montantTotalFichier = 0;

        Prelevements[] prelevementsComp = null;
        Fichiers[] fichiers = null;
        Prelevements aPrelevement = null;

        if (prelevements != null && 0 < prelevements.length) {

            StringBuffer line;
            for (int i = 0; i < prelevements.length; i += j) {

                long montantTotal = j = 0;


                //Tous les prelevements compensables validés 
                sql = "SELECT * FROM PRELEVEMENTS WHERE REMISE=" + prelevements[i].getRemise() + "AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + ", " + Utility.getParam("CETAOPEREJRETENVSIB") + ", " + Utility.getParam("CETAOPEVALSURCAIENVSIBORG") + ")  AND LOTSIB=NULL ORDER BY REFERENCE_EMETTEUR";
                prelevementsComp = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());

                if (prelevementsComp != null && 0 < prelevementsComp.length) {
                    compteur = Utility.bourrageGauche("" + prelevements[i].getRemise(), 4, "0");
                    String canalFileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + CMPUtility.getCodeBanqueSica3() +"_CANAL_"+ CMPUtility.getDate()+"_"+compteur + Utility.getParam("SIB_FILE_EXTENSION");
                    setOut(createFlatFile(canalFileName));

                    long sumRemiseComp = 0;
                    long sumCompte = 0;
                    j += prelevementsComp.length;

                    String strLine = "";
                    strLine += "03100001";
                    strLine += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                    strLine += Utility.getParam("CODE_BANQUE_SICA3").substring(2);
                    strLine += "01001";
                    strLine += Utility.bourrageGauche(prelevements[0].getNumerocompte_Beneficiaire(), 12, "0");
                    strLine += Utility.bourrageDroite(prelevements[0].getNom_Beneficiaire(), 23, " ");
                    strLine += Utility.bourrageGauche(prelevements[0].getZoneinterbancaire_Beneficiaire(), 5, "0");
                    strLine += "0";
                    strLine += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                    strLine += createBlancs(59, " ");
                    writeln(strLine);

                    sumCompte = Long.parseLong(prelevements[0].getNumerocompte_Beneficiaire());
                    for (int x = 0; x < prelevementsComp.length; x++) {
                        sumRemiseComp += Long.parseLong(prelevementsComp[x].getMontantprelevement());
                        sumCompte += Long.parseLong(prelevementsComp[x].getNumerocompte_Tire());
                        line = new StringBuffer();
                        line.append("032");
                        line.append(Utility.bourrageGZero("" + (x + 2), 5));
                        line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
                        line.append(prelevementsComp[x].getBanque().substring(2));
                        line.append(prelevementsComp[x].getAgence());
                        line.append(Utility.bourrageDroite(prelevementsComp[x].getNumerocompte_Tire(), 12, "0"));
                        line.append(Utility.bourrageDroite(prelevementsComp[x].getNom_Tire(), 23, " "));
                        sql = "SELECT * FROM BANQUES WHERE CODEBANQUE='" + prelevementsComp[x].getBanque() + "'";
                        Banques banques[] = (Banques[]) db.retrieveRowAsObject(sql, new Banques());
                        if (banques != null && banques.length > 0) {
                            line.append(Utility.bourrageDroite(banques[0].getLibellebanque(), 17, " "));
                        } else {
                            line.append(Utility.bourrageDroite("BANQUE", 24, " "));
                        }

                        line.append(Utility.bourrageDroite(prelevementsComp[x].getLibelle(), 30, " "));
                        line.append(Utility.bourrageGZero(prelevementsComp[x].getMontantprelevement(), 12));
                        line.append(prelevementsComp[x].getMotifrejet() == null ? "00" : Utility.getParamOfType(prelevementsComp[x].getMotifrejet(), "CODE_REJET"));
                        line.append(createBlancs(10, " "));

                        writeln(line.toString());


                        prelevementsComp[x].setLotsib(new BigDecimal(2));
                        db.updateRowByObjectByQuery(prelevementsComp[x], "PRELEVEMENTS", "IDPRELEVEMENT=" + prelevementsComp[x].getIdprelevement());


                    }

                    montantTotal += sumRemiseComp;

                    line = new StringBuffer();
                    line.append("033");
                    line.append(Utility.bourrageGZero("" + (prelevementsComp.length + 2), 5));
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
                    line.append(createBlancs(8, " "));
                    line.append(Utility.bourrageGauche("" + sumCompte, 10, " "));
                    line.append(createBlancs(72, " "));
                    line.append(Utility.bourrageGZero("" + sumRemiseComp, 12));
                    line.append(createBlancs(12, " "));
                    writeln(line.toString());
                    closeFile();

                }
                montantTotalFichier += montantTotal;
            }


            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Prelevement= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));
            logEvent("INFO", "Nombre de Prelevement= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));


        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
