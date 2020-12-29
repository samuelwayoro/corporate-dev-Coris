/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

import clearing.table.Prelevements;
import clearing.table.delta.Bkcom;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class PrelevementSortsImpayesBkmvtiWriter extends FlatFileWriter {

    public PrelevementSortsImpayesBkmvtiWriter() {
        setDescription("Envoi des prélèvements impayes vers le SIB");
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

        compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQALE", "CHQALE"), 4, "0");
        String bkmvtiFileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "bkmvti" + compteur + Utility.getParam("SIB_FILE_EXTENSION");



// Population
        String sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") "
                   + " AND DATECOMPENSATION='"+ Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "yyyyMMdd"), "yyyy/MM/dd")+"' "
                   + " AND DATEFINRECYCLAGE<='" + Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyy/MM/dd")+"' ORDER BY REMISE";
        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
        int j = 0;
        long montantTotalFichier = 0;

        Prelevements[] prelevementsComp = null;
        
        Prelevements aPrelevement = null;

        if (prelevements != null && 0 < prelevements.length) {
            PrintWriter outBkmvti = createFlatFile(bkmvtiFileName);
            StringBuffer line;
            for (int i = 0; i < prelevements.length; i += j) {
                long montantTotal = j = 0;


                //Tous les prelevements compensables validés 
                sql = "SELECT * FROM PRELEVEMENTS WHERE REMISE=" + prelevements[i].getRemise() //+ " AND BANQUE <> BANQUEREMETTANT AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") "
                        + "AND ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") "
                        + "AND DATECOMPENSATION='"+ Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "yyyyMMdd"), "yyyy/MM/dd")+"' "
                        + "AND DATEFINRECYCLAGE<='" + Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyy/MM/dd")+"' ORDER BY REMISE";
                
                prelevementsComp = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());

                if (prelevementsComp != null && 0 < prelevementsComp.length) {
                    long sumRemiseComp = 0;
                    j += prelevementsComp.length;
                    for (int x = 0; x < prelevementsComp.length; x++) {
                        sumRemiseComp += Long.parseLong(prelevementsComp[x].getMontantprelevement());
                        if (prelevementsComp[x].getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))) {
                            prelevementsComp[x].setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                            db.updateRowByObjectByQuery(prelevementsComp[x], "PRELEVEMENTS", "IDPRELEVEMENT=" + prelevementsComp[x].getIdprelevement());
                        }

                    }
                    String requete = "select x0.age AGENCE, x0.ser SERVICE,x0.ncp COMPTE,SUBSTR(x0.inti,1,30) NOM,x0.ribdec CLERIB,x0.CPRO TYPECPT from bkcom x0 where x0.cfe='N' and x0.ife='N' and x0.dev='952' ";
                    DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
                    dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
                    Bkcom[] bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete + " and ncp like '" + Utility.bourrageDroite(Utility.getParam("CPTATTPRELALLAMP"), 11, "0") + "'", new Bkcom());
                    montantTotal += sumRemiseComp;
                    setOut(outBkmvti);
                    //Ligne de credit du Total du compte organisme
                    line = new StringBuffer();
                    line.append("01001").append("|952|");
                    line.append("|");
                    line.append(Utility.bourrageDroite(Utility.getParam("CPTATTPRELALLAMP"), 11, " ")).append("|");
                    line.append(createBlancs(1, " ")).append("|");
                    line.append(Utility.getParam("CODOPEPRELALLIMPAYE")).append("|").append("|").append("|");
                    line.append("AUTO||");
                    line.append(Utility.computeCleRIB(Utility.getParam("CODE_BANQUE_SICA3"), "01001", Utility.bourrageGZero(Utility.getParam("CPTATTPRELALLAMP"), 12))).append("|");
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                     try{
                        line.append("|").append(bkcom[0].getService()).append("|");
                    }catch(NullPointerException ex){
                        setDescription(getDescription() +" Compte "+Utility.bourrageGauche(prelevements[i].getNumerocompte_Beneficiaire(), 11, "0")+" non trouvé dans bkcom");
                        logEvent("ERREUR", getDescription());
                        return;
                    }
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                    line.append(montantTotal);
                    line.append("|C|");
                    line.append(Utility.bourrageDroite("PREL"+"-"+prelevements[i].getNom_Beneficiaire()+"-"+Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM"), 30, " "));
                    line.append("|N|");
                    String numPiece = Utility.computeCompteur("CPTPIEPREL", Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                    line.append("PRL").append(numPiece).append("|PRL").append(numPiece).append("||||||||1,0|| |0,0|||N|N|N| |");
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                    line.append("|||||952|");
                    line.append(montantTotal);
                    line.append("|");
                     line.append(numPiece);
                    line.append("|");
                    line.append(" |001| ||| |||||| ||");
                    writeln(line.toString());

                    bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete + " and ncp like '" + Utility.bourrageGauche(prelevements[i].getNumerocompte_Beneficiaire(), 11, "0") + "'", new Bkcom());
                    line = new StringBuffer();
                    line.append(prelevements[i].getAgenceremettant()).append("|952|");
                    line.append("|");
                    line.append(Utility.bourrageDroite(prelevements[i].getNumerocompte_Beneficiaire().substring(1), 11, " ")).append("|");
                    line.append(createBlancs(1, " ")).append("|");
                    line.append(Utility.getParam("CODOPEPRELALLIMPAYE")).append("|").append("|").append("|");
                    line.append("AUTO||");
                    line.append(Utility.computeCleRIB(Utility.getParam("CODE_BANQUE_SICA3"), prelevements[i].getAgenceremettant(), prelevements[i].getNumerocompte_Beneficiaire())).append("|");
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                    try{
                        line.append("|").append(bkcom[0].getService()).append("|");
                    }catch(NullPointerException ex){
                        setDescription(getDescription() +" Compte "+Utility.bourrageGauche(prelevements[i].getNumerocompte_Beneficiaire(), 11, "0")+" non trouvé dans bkcom");
                        logEvent("ERREUR", getDescription());
                        return;
                    }
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy")).append("|");
                    line.append(montantTotal);
                    line.append("|D|");
                    line.append(Utility.bourrageDroite("IMPAYE "+prelevements[i].getLibelle(), 30, " "));
                    line.append("|N|");
                    line.append("PRL").append(numPiece).append("|PRL").append(numPiece).append("||||||||1,0|| |0,0|||N|N|N| |");
                    line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "yyyyMMdd"), "dd/MM/yyyy"));
                    line.append("|||||952|");
                    line.append(montantTotal);
                    line.append("|");
                    line.append(numPiece);
                    line.append("|");
                    line.append(" |001| ||| |||||| ||");
                    writeln(line.toString());

                }
                montantTotalFichier += montantTotal;
            }


            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Prelevement= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));
            logEvent("INFO", "Nombre de Prelevement= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));
            outBkmvti.close();
            //closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
