package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

public class ChequeAllerNonCompCreditFlexCubeWriter
        extends FlatFileWriter {

    public ChequeAllerNonCompCreditFlexCubeWriter() {
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
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        if ((Utility.getParam("ECOBANK_STANDARD") != null) && (Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0"))) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQCAI", "CHQCAI"), 4, "0");
        } else {

            if ((Utility.getParam("ECOBANK_STANDARD") != null) && (Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2"))) {
                compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQCAI", "CHQCAI"), 4, "0");
            } else {
                compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQCAI", "CHQCAI"), 4, "0");
            }
        }
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + "AND LOTSIB=1 ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0L;
        Cheques[] chequesVal = null;
        Remises[] remises = null;
        Cheques aCheque = null;
        if ((cheques != null) && (0 < cheques.length)) {
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXMAINBRANCH") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            line.append(createBlancs(76, " "));
            writeln(line.toString());
            for (int i = 0; i < cheques.length; i += j) {
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT =" + Utility.getParam("CETAOPESUPVALSURCAI");
                chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise();
                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;
                if ((remises != null) && (0 < remises.length) && (allChequesVal.length == remises[0].getNbOperation().intValue())) {
                    if ((chequesVal != null) && (0 < chequesVal.length)) {
                        long sumRemise = 0L;
                        for (int x = 0; x < chequesVal.length; x++) {
                            sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;
                        for (int x = 0; x < chequesVal.length; x++) {
                            aCheque = chequesVal[x];

                            line = new StringBuffer();
                            line.append(aCheque.getAgenceremettant().substring(2));
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
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }
                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }
                }
            }
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTATTCHQCAIFLEX"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, "0"));
            line.append("Q13");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());
            closeFile();

            setDescription(getDescription() + " ex?cut? avec succ?s:\n Nombre de Ch?que= " + cheques.length + " - Montant Total= " + Utility.formatNumber(new StringBuilder().append("").append(montantTotal).toString()) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Ch?que= " + cheques.length + " - Montant Total= " + Utility.formatNumber(new StringBuilder().append("").append(montantTotal).toString()));
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        db.close();
    }
}
