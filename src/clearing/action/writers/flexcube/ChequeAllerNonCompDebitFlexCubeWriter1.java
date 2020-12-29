package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

public class ChequeAllerNonCompDebitFlexCubeWriter1
        extends FlatFileWriter {

    public ChequeAllerNonCompDebitFlexCubeWriter1() {
        setDescription("Envoi des cheques vers le SIB");
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
        int j = 0;
        long montantTotal = 0L;
        Cheques[] chequesVal = null;
        Remises[] remises = null;
        Cheques aCheque = null;

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));
        StringBuffer line = new StringBuffer();
        String sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        if ((cheques != null) && (0 < cheques.length)) {
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
                            RIO rio = new RIO(CMPUtility.getCodeBanqueSica3() + CMPUtility.getPacSCMPSICA3() + CMPUtility.getDevise() + "001" + Utility.getParam("DATECOMPENS_NAT") + "00001" + Utility.bourrageGZero(new StringBuilder().append("").append(aCheque.getIdcheque()).toString(), 8));
                            aCheque.setRio(rio.getRio());

                            line = new StringBuffer();
                            line.append(aCheque.getBanqueremettant());
                            line.append(aCheque.getBanque());
                            line.append("Q06 ");
                            line.append(Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " "));
                            line.append(Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), ResLoader.getMessages("patternDate")), "yyyyMMdd"));

                            line.append(dateValeur);
                            line.append(Utility.bourrageGauche(aCheque.getMontantcheque(), 16, " "));
                            line.append(createBlancs(3, " ") + "UAP");
                            line.append(aCheque.getRio());
                            line.append(CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(), "0"));
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getNumerocompte(), aCheque.getAgence()));
                            writeln(line.toString());
                            aCheque.setRio("");
                            aCheque.setLotsib(new BigDecimal(1));

                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }
                    }
                }
            }
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
