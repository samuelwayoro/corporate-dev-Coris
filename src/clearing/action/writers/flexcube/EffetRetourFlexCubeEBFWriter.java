/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Effets;
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
public class EffetRetourFlexCubeEBFWriter extends FlatFileWriter {

    public EffetRetourFlexCubeEBFWriter() {
        setDescription("Envoi des effets Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTEFFRET", "EFFRET"), 4, "0");
            isEcobankStandard = false;
        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "f" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTEFFRET", "EFFRET"), 3, "0");
            isEcobankStandard = false;
        } else {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTEFFRET", "EFFRET"), 4, "0");
            isEcobankStandard = true;
        }

        String dateValeur = Utility.getParam("DATEVALEUR_RETOUR");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");


        String sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ") ";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        int j = 0;
        long montantTotal = 0;
        if (effets != null && 0 < effets.length) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            if (isEcobankStandard) {
                line.append(createBlancs(76, " "));
            }
            writeln(line.toString());
            for (int i = 0; i < effets.length; i++) {
                Effets effet = effets[i];
                //Tous les effets retour - ligne de credit montant sur cpt
                //Ligne 1 
                line = new StringBuffer();
                line.append(CMPUtility.getNumCptExAgence(effet.getNumerocompte_Tire(), effet.getAgence()));
                line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(effet.getNumerocompte_Tire(), effet.getAgence(),"0"), 16," ") );
                line.append(createBlancs(4, " "));
                line.append("D");
                line.append(Utility.bourrageGauche(effet.getMontant_Effet(), 16, " "));
                line.append("F03");
                line.append(dateValeur);
                line.append(createBlancs(8, " "));
                //line.append(Utility.bourrageDroite(Utility.getParam("LIBEFFRETFLEX1"), 18, " "));
                line.append(Utility.bourrageDroite(effet.getDate_Echeance() +" "+ effet.getNom_Beneficiaire(), 100, " "));
                line.append(createBlancs(7, " "));
                line.append("043");
            //    line.append(Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 8));
                writeln(line.toString());

                if (effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }

                montantTotal += Long.parseLong(effet.getMontant_Effet());
            }
// Gestion cpt globalisation
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTCREEFFRET"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("C");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, " "));
            line.append("F03");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());



            setDescription(getDescription() + " exécuté avec succès:\n Nombre d'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre d'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
