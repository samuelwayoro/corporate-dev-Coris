/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.esn;

import clearing.action.writers.flexcube.*;
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
public class EffetRetourFlexCubeWriter extends FlatFileWriter {

    public EffetRetourFlexCubeWriter() {
        setDescription("Envoi des effets Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        boolean isEcobankStandard;
        String compteur;
        if(Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTEFFRET", "EFFRET"), 4, "0");
            isEcobankStandard = false;
        } else if(Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "f" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTEFFRET", "EFFRET"), 3, "0");
            isEcobankStandard = false;
        } else {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTEFFRET", "EFFRET"), 4, "0");
            isEcobankStandard = true;
        }

        String dateValeur = Utility.getParam("DATEVALEUR_RETOUR");
        String[] param1 = (String[])((String[])this.getParametersMap().get("param1"));
        if(param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
        String sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ") ";
        Effets[] effets = (Effets[])((Effets[])db.retrieveRowAsObject(sql, new Effets()));
        boolean j = false;
        long montantTotal = 0L;
        if(effets != null && 0 < effets.length) {
            this.setOut(this.createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            if(isEcobankStandard) {
                line.append(createBlancs(76, " "));
            }

            this.writeln(line.toString());

            for(int i = 0; i < effets.length; ++i) {
                Effets effet = effets[i];
                line = new StringBuffer();
                line.append(Utility.bourrageGZero(CMPUtility.getNumCptEx(effet.getNumerocompte_Tire(), effet.getAgence(),"1"), 16).substring(0, 3));
                line.append(Utility.bourrageGZero(CMPUtility.getNumCptEx(effet.getNumerocompte_Tire(), effet.getAgence(),"1"), 16));
                line.append(createBlancs(4, " "));
                line.append("D");
                line.append(Utility.bourrageGauche(effet.getMontant_Effet(), 16, " "));
                line.append("F03");
                line.append(dateValeur);
                line.append(createBlancs(8, " "));
                line.append(Utility.bourrageDroite(Utility.getParam("LIBEFFRETFLEX1"), 18, " "));
                line.append(createBlancs(7, " "));
                line.append("043");
                line.append(Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 8));
                this.writeln(line.toString());
                if(effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }

                montantTotal += Long.parseLong(effet.getMontant_Effet());
            }

            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTCREEFFRET"), 9, " "));
            line.append(createBlancs(11, " "));
            line.append("C");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, " "));
            line.append("F03");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            this.writeln(line.toString());
            this.setDescription(this.getDescription() + " exécuté avec succès:\n Nombre d\'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            this.logEvent("INFO", "Nombre d\'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            this.closeFile();
        } else {
            this.setDescription(this.getDescription() + ": Il n\'y a aucun element disponible");
            this.logEvent("WARNING", "Il n\'y a aucun element disponible");
        }

        db.close();
    }
}
