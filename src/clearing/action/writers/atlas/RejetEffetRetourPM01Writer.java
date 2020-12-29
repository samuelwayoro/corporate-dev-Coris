/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.atlas;

import clearing.model.CMPUtility;

import clearing.table.Effets;
import java.io.File;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class RejetEffetRetourPM01Writer extends FlatFileWriter {

    public RejetEffetRetourPM01Writer() {
        setDescription("Envoi des rejets effet retour vers le SIB");
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

        compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTEFFRETREJ", "EFFRET"), 4, "0");
        String pm01FileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_REJ_RET_FILE_ROOTNAME") + compteur + ".prn";

// Population
        String sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ACC") + ")  ORDER BY AGENCE";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

        long sumRemiseComp = 0;

        if (effets != null && 0 < effets.length) {
            for (Effets effet : effets) {
                sumRemiseComp += Long.parseLong(effet.getMontant_Effet());
            }
            setOut(createFlatFile(pm01FileName));

            writeln(Utility.getParam("ENTPM01EFFRETREJ")); //  117WR

            //Ligne de credit du Total du compte interne CNPP
            String cptComptable;
            String codeMouvement;
            String refDenotage;
            String libelleComptable;
            String type;

            for (Effets effet : effets) {
                //Ligne de debit du compte client

                //Ligne de credit du compte client
                cptComptable = CMPUtility.getNumCpt(effet.getNumerocompte_Tire().substring(1), effet.getAgence());
                if (cptComptable == null) {
                    cptComptable = Utility.getParam("CPTINTDEBEFF");
                }
                codeMouvement = Utility.getParam("CODOPECREPM01");
                refDenotage = Utility.bourrageGauche("" + effet.getIdeffet(), 8, "0");
                String dateEcheance = Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Echeance(), "yyyy/MM/dd"), "ddMMyyyy");
                libelleComptable = Utility.bourrageDroite("IMP EFF " + Utility.bourrageDroite(effet.getNom_Beneficiaire(), 17, " ") + " " + dateEcheance, 34, " ");
                type = "0";
                if (!createLinePM01(cptComptable, Long.parseLong(effet.getMontant_Effet()), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                    return;
                }

                //Ligne de DEBIT du compte CNPP
                cptComptable = Utility.getParam("CPTGLOBALEDNCREJ");
                codeMouvement = Utility.getParam("CODOPEDEBPM01");
                refDenotage = Utility.bourrageGauche("" + effet.getIdeffet(), 8, "0");
                libelleComptable = Utility.bourrageDroite("IMP EFF " + Utility.bourrageDroite(effet.getNom_Beneficiaire(), 17, " ") + " " + dateEcheance, 34, " ");
                type = "0";
                if (!createLinePM01(cptComptable, Long.parseLong(effet.getMontant_Effet()), dateValeur, codeMouvement, refDenotage, libelleComptable, type)) {
                    return;
                }

//                effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
//                db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
            }

            setDescription(getDescription() + " execute avec succes:\n Nombre d'effets = " + effets.length + " - Montant Total= " + Utility.formatNumber("" + sumRemiseComp));
            logEvent("INFO", "Nombre d'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + sumRemiseComp));
            db.executeUpdate("UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEALLICOM2ACCENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPEALLICOM2ACC"));
            closeFile();

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }

    private boolean createLinePM01(String cptComptable, long montantLigne, String dateValeur, String codeMouvement, String refDenotage, String libelleComptable, String type) {
        StringBuffer line;
        line = new StringBuffer();

        line.append(Utility.bourrageGauche(cptComptable.substring(0, cptComptable.length() - 2), 14, "0"));
        line.append("99XOF");
        line.append(Utility.bourrageGauche("" + montantLigne, 11, "0"));
        line.append(codeMouvement);
        line.append(refDenotage);
        line.append(dateValeur);
        line.append(libelleComptable);
        line.append(type);

        writeln(line.toString());
        return true;
    }

    public static boolean priseCommission(Effets effet) {
        if (effet.getMotifrejet().equals("201")
                || effet.getMotifrejet().equals("202")) {
            return true;
        } else {
            return false;
        }

    }
}
