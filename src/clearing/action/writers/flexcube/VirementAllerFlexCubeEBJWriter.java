/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Virements;
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
public class VirementAllerFlexCubeEBJWriter extends FlatFileWriter {

    public VirementAllerFlexCubeEBJWriter() {
        setDescription("Envoi des virements Aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTVIRALL", "VIRALL"), 4, "0");
            isEcobankStandard = false;
        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "v" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTVIRALL", "VIRALL"), 3, "0");
            isEcobankStandard = false;
        } else {

            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTVIRALL", "VIRALL"), 4, "0");
            isEcobankStandard = true;
        }

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);
        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");

        String sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        int j = 0;
        long montantTotal = 0;
        if (virements != null && 0 < virements.length) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            if (isEcobankStandard) {
                line.append(createBlancs(76, " "));
            }
            writeln(line.toString());


            for (int i = 0; i < virements.length; i++) {
                Virements virement = virements[i];
                //Tous les virements retour - ligne de credit montant sur cpt
                //Ligne 1 
                line = new StringBuffer();
                line.append(Utility.getParam("FLEXMAINBRANCH"));
                line.append(Utility.bourrageDroite(Utility.getParam("CPTREGBCEAO"), 16, " "));
                line.append(createBlancs(4, " "));
                line.append("D");
                line.append(Utility.bourrageGauche(virement.getMontantvirement(), 16, " "));
                line.append("F03");
                line.append(dateValeur);
                line.append(createBlancs(8, " "));
                //line.append(Utility.bourrageDroite(Utility.getParam("LIBVIRRETFLEX1"), 18, " "));
                line.append(Utility.bourrageDroite(virement.getLibelle(), 200, " "));
                //line.append(createBlancs(7, " "));
                line.append(virement.getType_Virement().equals("015") ? "010" : "011");
                line.append(Utility.bourrageGZero(virement.getIdvirement().toPlainString(), 8));
                writeln(line.toString());

                if (virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM1ACC"))) {
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                }

                montantTotal += Long.parseLong(virement.getMontantvirement());
            }
// Gestion cpt globalisation
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTATTCHQALEFLEX"), 9, " "));
            line.append(createBlancs(11, " "));
            line.append("C");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, " "));
            line.append("F57");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBVIRALEFLEX1"));
            line.append(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyy/mm/dd"), "dd/mm/yy"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());



            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Virements= " + virements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Virement= " + virements.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
