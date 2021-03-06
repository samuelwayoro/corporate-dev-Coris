/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.io.File;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CPTENVFlexCubeWriter extends FlatFileWriter {

    public CPTENVFlexCubeWriter() {
        setDescription("Envoi du fichier CPTENVe vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;
        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur = " + dateValeur);

        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTENV", "CPTENV"), 4, "0");
            isEcobankStandard = false;
        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "e" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTENV", "CPTENV"), 3, "0");
            isEcobankStandard = false;
        } else {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTENV", "CPTENV"), 4, "0");
            isEcobankStandard = true;
        }


        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CPTENV_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");


        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");
// Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ")  ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;

        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            if (isEcobankStandard) {
                line.append(createBlancs(76, " "));
            }
            writeln(line.toString());

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques valid�s

                //Tous les cheques compensables valid�s d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") ORDER BY REMISE";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());


                j = chequesVal.length;

                if (chequesVal != null && 0 < chequesVal.length) {
                    long sumRemise = 0;

                    for (int x = 0; x < chequesVal.length; x++) {
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                    }
                    montantTotal += sumRemise;

                }


            }
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.getParam("CPTATTCHQALEFLEX5"));
            line.append(createBlancs(11, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, "0"));
            line.append("F03");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());

            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.getParam("CPTATTCHQALEFLEX6"));
            line.append(createBlancs(11, " "));
            line.append("C");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, "0"));
            line.append("F57");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());

            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.getParam("CPTATTCHQALEFLEX4"));
            line.append(createBlancs(11, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, "0"));
            line.append("F03");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());

            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.getParam("CPTATTCHQALEFLEX3"));
            line.append(createBlancs(11, " "));
            line.append("C");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, "0"));
            line.append("F57");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());

            setDescription(getDescription() + " ex�cut� avec succ�s:\n Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", " Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
