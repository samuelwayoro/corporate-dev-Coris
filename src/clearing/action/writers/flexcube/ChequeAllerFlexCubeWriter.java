package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashMap;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

public class ChequeAllerFlexCubeWriter
        extends FlatFileWriter {

    public ChequeAllerFlexCubeWriter() {
        setDescription("Envoi des ch?ques vers le SIB");
    }

    public void execute()
            throws Exception {
        super.execute();

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if ((param1 != null) && (param1.length > 0)) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur = " + dateValeur);

        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if ((param1 != null) && (param1.length > 0)) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        boolean isEcobankStandard;
        if ((Utility.getParam("ECOBANK_STANDARD") != null) && (Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0"))) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQALE", "CHQALE"), 4, "0");
            isEcobankStandard = false;
        } else {

            if ((Utility.getParam("ECOBANK_STANDARD") != null) && (Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2"))) {
                compteur = "a" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQALE", "CHQALE"), 3, "0");
                isEcobankStandard = false;
            } else {
                compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQALE", "CHQALE"), 4, "0");
                isEcobankStandard = true;
            }
        }
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");

        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") " + " AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";

        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0L;
        if ((cheques != null) && (0 < cheques.length)) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            if (isEcobankStandard) {
                line.append(createBlancs(76, " "));
            }
            writeln(line.toString());
            for (int i = 0; i < cheques.length; i += j) {
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;
                if ((chequesVal != null) && (0 < chequesVal.length)) {
                    long sumRemise = 0L;

                    Cheques aCheque = chequesVal[0];
                    for (int x = 0; x < chequesVal.length; x++) {
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                    }
                    montantTotal += sumRemise;
                    for (int x = 0; x < chequesVal.length; x++) {
                        aCheque = chequesVal[x];

                        line = new StringBuffer();
                        line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()));
                        line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(), "0"), 16, " "));
                        line.append(createBlancs(4, " "));
                        line.append("C");
                        line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                        line.append("Q13");
                        line.append(dateValeur);
                        line.append(" ");
                        line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                        line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 18, " "));
                        line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
                        line.append("030");
                        line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));
                        writeln(line.toString());
                        if (aCheque.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACC")))) {
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }
                    }
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                }
            }
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTATTCHQALEFLEX"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, "0"));
            line.append("Q13");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());

            setDescription(getDescription() + " execute avec succes:\n Nombre de Cheque= " + cheques.length + " - Montant Total= " + Utility.formatNumber(new StringBuilder().append("").append(montantTotal).toString()) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Cheque= " + cheques.length + " - Montant Total= " + Utility.formatNumber(new StringBuilder().append("").append(montantTotal).toString()));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        db.close();
    }
}
