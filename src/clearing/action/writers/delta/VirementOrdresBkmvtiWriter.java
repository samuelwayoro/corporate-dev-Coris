/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

import clearing.table.Virements;
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
public class VirementOrdresBkmvtiWriter extends FlatFileWriter {

    public VirementOrdresBkmvtiWriter() {
        setDescription("Envoi des virements aller vers le SIB");
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
        String sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPEVAL") + ")  ORDER BY REMISE";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        int j = 0;
        long montantTotalFichier = 0;
        Virements[] virementsCaisse = null;
        Virements[] virementsComp = null;

        Virements aVirement = null;

        if (virements != null && 0 < virements.length) {
            PrintWriter outBkmvti = createFlatFile(bkmvtiFileName);
            StringBuffer line;
            for (int i = 0; i < virements.length; i += j) {
                long montantTotal = j = 0;
                //Tous les virements non compensables validés d'une remise
                sql = "SELECT * FROM VIREMENTS WHERE BANQUE = BANQUEREMETTANT AND ETAT IN (" + Utility.getParam("CETAOPEVAL") + ") ";
                virementsCaisse = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                if (virementsCaisse != null && 0 < virementsCaisse.length) {
                    long sumRemiseCaisse = 0;

                    j = virementsCaisse.length;
                    //Fichier de virement interne
                    String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.bourrageGauche(virements[i].getZoneinterbancaire_Beneficiaire(), 5, "0") + Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy")+".vint";
                    setOut(createFlatFile(fileName));
                    line = new StringBuffer();
                    line.append("04100001");
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
                    line.append(Utility.getParam("CODE_BANQUE_SICA3").substring(2));
                    line.append(virements[i].getAgenceremettant());
                    line.append(Utility.bourrageDroite(virements[i].getNumerocompte_Beneficiaire().substring(1), 11, " "));
                    line.append(Utility.bourrageDroite(virements[i].getNom_Beneficiaire(), 24, " "));

                    line.append(Utility.bourrageGauche(virements[i].getZoneinterbancaire_Beneficiaire(), 5, "0"));
                    line.append(" ");
                    line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
                    line.append(createBlancs(59, " "));
                    writeln(line.toString());
                    for (int x = 0; x < virementsCaisse.length; x++) {
                        sumRemiseCaisse += Long.parseLong(virementsCaisse[x].getMontantvirement());
                        //TODO Credit du compte client organisme au detail
                        line = new StringBuffer();
                        line.append("042");
                        line.append(Utility.bourrageGZero("" + (x + 2), 5));
                        line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyy"));
                        line.append(virementsCaisse[x].getBanque().substring(2));
                        line.append(virementsCaisse[x].getAgence());
                        line.append(Utility.bourrageDroite(virementsCaisse[x].getNumerocompte_Tire().substring(1), 11, " "));
                        line.append(Utility.bourrageDroite(virementsCaisse[x].getNom_Tire(), 24, " "));
                        line.append(Utility.bourrageDroite(virementsCaisse[x].getReference_Emetteur(), 24, " "));
                        line.append(Utility.bourrageDroite(virementsCaisse[x].getLibelle(), 30, " "));
                        line.append(Utility.bourrageGZero(virementsCaisse[x].getMontantvirement(), 12));
                        line.append(createBlancs(4, " "));
                        line.append("XOF");
                        writeln(line.toString());
                        if (virementsCaisse[x].getEtat().toPlainString().equals(Utility.getParam("CETAOPEVAL"))) {
                            virementsCaisse[x].setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAI")));
                            virementsCaisse[x].setZoneinterbancaire_Tire(Utility.bourrageGZero("" + (x + 2), 5));
                            virementsCaisse[x].setReference_Operation_Interne(Utility.bourrageGZero("" + (x + 2), 5));
                            db.updateRowByObjectByQuery(virementsCaisse[x], "VIREMENTS", "IDVIREMENT=" + virementsCaisse[x].getIdvirement());
                        }

                    }
                    montantTotal += sumRemiseCaisse;
                    getOut().close();

                }

                //Fichier de virement externe
                //Tous les virements compensables validés 
                sql = "SELECT * FROM VIREMENTS WHERE BANQUE <> BANQUEREMETTANT AND ETAT IN (" + Utility.getParam("CETAOPEVAL") + ") ";
                virementsComp = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                if (virementsComp != null && 0 < virementsComp.length) {
                    long sumRemiseComp = 0;
                    j += virementsComp.length;
                    for (int x = 0; x < virementsComp.length; x++) {
                        sumRemiseComp += Long.parseLong(virementsComp[x].getMontantvirement());
                        if (virementsComp[x].getEtat().toPlainString().equals(Utility.getParam("CETAOPEVAL"))) {
                            virementsComp[x].setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                            db.updateRowByObjectByQuery(virementsComp[x], "VIREMENTS", "IDVIREMENT=" + virementsComp[x].getIdvirement());
                        }

                    }
                    String requete = "select x0.age AGENCE, x0.ser SERVICE,x0.ncp COMPTE,SUBSTR(x0.inti,1,30) NOM,x0.ribdec CLERIB,x0.CPRO TYPECPT from bkcom x0 where x0.cfe='N' and x0.ife='N' and x0.dev='952' ";
                    DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
                    dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
                    Bkcom[] bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete + " and ncp like '" + Utility.bourrageGauche(virements[i].getNumerocompte_Tire(), 11, "0") + "'", new Bkcom());

                    montantTotal += sumRemiseComp;
                    setOut(outBkmvti);
                    //Ligne de debit du Total du compte organisme
                    
                    String codeOperation = Utility.getParam("CODOPEVIRALL");
                    String sensComptable ="D";
                    String agenceComptable = virements[i].getAgenceremettant();
                    String cptComptable = virements[i].getNumerocompte_Tire().substring(1);
                    String libelleComptable = Utility.bourrageDroite("VIR" + "-" + virements[i].getNom_Tire() + "-" + Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM"), 30, " ");
                    String numPiece = Utility.computeCompteur("CPTPIEVIR", Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                    if (!createLineBkmvti(codeOperation, sensComptable, bkcom, dateValeur, sumRemiseComp, numPiece, agenceComptable, cptComptable, libelleComptable)) {
                        return;
                    }
                    
                    

                    
                    bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete + " and ncp like '" + Utility.bourrageGauche(Utility.getParam("CPTINTAGELIA"), 11, "0") + "'", new Bkcom());
                    codeOperation = Utility.getParam("CODOPEVIRALL");
                    sensComptable ="C";
                    agenceComptable = "01001";
                    cptComptable = Utility.getParam("CPTINTAGELIA");
                    libelleComptable = Utility.bourrageDroite("VIR" + "-" + virements[i].getNom_Tire() + "-" + Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM"), 30, " ");
                    if (!createLineBkmvti(codeOperation, sensComptable, bkcom, dateValeur, sumRemiseComp,  numPiece, agenceComptable, cptComptable, libelleComptable)) {
                        return;
                    }
                    
                    bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete + " and ncp like '" + Utility.bourrageGauche(Utility.getParam("CPTINTAGELIA"), 11, "0") + "'", new Bkcom());
                    codeOperation = Utility.getParam("CODOPEVIRALL");
                    sensComptable ="D";
                    agenceComptable = "99111";
                    cptComptable = Utility.getParam("CPTINTAGELIA");
                    libelleComptable = Utility.bourrageDroite("VIR" + "-" + virements[i].getNom_Tire() + "-" + Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM"), 30, " ");
                    if (!createLineBkmvti(codeOperation, sensComptable, bkcom, dateValeur, sumRemiseComp,  numPiece, agenceComptable, cptComptable, libelleComptable)) {
                        return;
                    }
                    
                    bkcom = (Bkcom[]) dbExt.retrieveRowAsObject(requete + " and ncp like '" + Utility.bourrageGauche(Utility.getParam("CPTINTVALIMP"), 11, "0") + "'", new Bkcom());
                    codeOperation = Utility.getParam("CPTINTVALIMP");
                    sensComptable ="C";
                    agenceComptable = "99111";
                    cptComptable = Utility.getParam("CPTINTVALIMP");
                    libelleComptable = Utility.bourrageDroite("VIR" + "-" + virements[i].getNom_Tire() + "-" + Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM"), 30, " ");
                    if (!createLineBkmvti(codeOperation, sensComptable, bkcom, dateValeur, sumRemiseComp,  numPiece, agenceComptable, cptComptable, libelleComptable)) {
                        return;
                    }
                    
                    codeOperation = Utility.getParam("CODOPEVIRALL");
                    sensComptable ="D";
                    agenceComptable = virements[i].getAgenceremettant();
                    cptComptable = virements[i].getNumerocompte_Tire().substring(1);
                    libelleComptable = "COMMISIONS VIREMENT";
                    sumRemiseComp = Long.parseLong(Utility.getParam("MNTCOMDEBKK"));
                    if (!createLineBkmvti(codeOperation, sensComptable, bkcom, dateValeur, sumRemiseComp, numPiece, agenceComptable, cptComptable, libelleComptable)) {
                        return;
                    }
                    
                    codeOperation = Utility.getParam("CODOPEVIRALL");
                    sensComptable ="C";
                    agenceComptable = "01001";
                    cptComptable = Utility.getParam("CPTTAFBDK");
                    libelleComptable = "TAF VIREMENT KASHKASH";
                    sumRemiseComp = Long.parseLong(Utility.getParam("MNTTAFVIRKK"));
                    if (!createLineBkmvti(codeOperation, sensComptable, bkcom, dateValeur, sumRemiseComp, numPiece, agenceComptable, cptComptable, libelleComptable)) {
                        return;
                    }
                    
                    codeOperation = Utility.getParam("CODOPEVIRALL");
                    sensComptable ="C";
                    agenceComptable = "01001";
                    cptComptable = Utility.getParam("CPTFRACOMBCEAO");
                    libelleComptable = "Frais Compense BCEAO KASHKASH";
                    sumRemiseComp = Long.parseLong(Utility.getParam("MNTFRACOMBCEAO"));
                    if (!createLineBkmvti(codeOperation, sensComptable, bkcom, dateValeur, sumRemiseComp, numPiece, agenceComptable, cptComptable, libelleComptable)) {
                        return;
                    }
                    
                    codeOperation = Utility.getParam("CODOPEVIRALL");
                    sensComptable ="C";
                    agenceComptable = "01001";
                    cptComptable = Utility.getParam("CPTCOMVIRBDK");
                    libelleComptable = "COMMISIONS VIREMENT";
                    sumRemiseComp = Long.parseLong(Utility.getParam("MNTCOMCREKK"));
                    if (!createLineBkmvti(codeOperation, sensComptable, bkcom, dateValeur, sumRemiseComp, numPiece, agenceComptable, cptComptable, libelleComptable)) {
                        return;
                    }
                    
                    
                    dbExt.close();

                }
                montantTotalFichier += montantTotal;
            }


            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Virements = " + virements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));
            logEvent("INFO", "Nombre de Virement= " + virements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalFichier));
            outBkmvti.close();
            //closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }

    private boolean createLineBkmvti(String codeOperation, String sensComptable, Bkcom[] bkcom, String dateValeur, long sumRemiseComp, String numPiece, String agenceComptable, String cptComptable, String libelleComptable) {
        StringBuffer line;
        line = new StringBuffer();
        line.append(agenceComptable).append("|952|");
        line.append("|");
        line.append(Utility.bourrageDroite(cptComptable, 11, " ")).append("|");
        line.append(createBlancs(1, " ")).append("|");
        line.append(codeOperation).append("|").append("|").append("|");
        line.append("AUTO||");
        line.append(Utility.computeCleRIB(Utility.getParam("CODE_BANQUE_SICA3"), agenceComptable, Utility.bourrageGZero(cptComptable, 12))).append("|");
        line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
        try {
            line.append("|").append(bkcom[0].getService()).append("|");
        } catch (NullPointerException ex) {
            setDescription(getDescription() + " Compte " + Utility.bourrageGauche(cptComptable, 11, "0") + " non trouvé dans bkcom");
            logEvent("ERREUR", getDescription());
            return false;
        }
        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "yyyyMMdd"), "dd/MM/yyyy")).append("|");
        line.append(sumRemiseComp);
        line.append("|"+sensComptable+"|");
        line.append(libelleComptable);
        line.append("|N|");
        line.append("VIR").append(numPiece).append("|VIR").append(numPiece).append("||||||||1,0|| |0,0|||N|N|N| |");
        line.append(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
        line.append("|||||952|");
        line.append(sumRemiseComp);
        line.append("|");
        line.append(numPiece);
        line.append("|");
        line.append(" |001| ||| |||||| ||");
        writeln(line.toString());
        return true;
    }
}
