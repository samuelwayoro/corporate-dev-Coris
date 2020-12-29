/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube12.all;

import clearing.action.writers.flexcube.*;
import clearing.model.CMPUtility;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerNonCompDebitFlexCubeWriterSignaturesETG extends FlatFileWriter {

    public ChequeAllerNonCompDebitFlexCubeWriterSignaturesETG() {
        setDescription("Envoi des chèques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQCAI", "CHQCAI"), 4, "0");

        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQCAI", "CHQCAI"), 4, "0");

        } else {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQCAI", "CHQCAI"), 4, "0");

        }

        int j = 0;
        long montantTotal = 0;
        Cheques[] chequesVal = null;
        Remises[] remises = null;
        Cheques aCheque = null;

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));
        StringBuffer line = new StringBuffer();
        //CI 
        //   String sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND VILLEREMETTANT='02' ORDER BY REMISE"; //mise Ã  jour pour la pri
        String sql = "SELECT * FROM CHEQUES WHERE (ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND LOTSIB=1) OR  "
                + "(ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND REMISE IN (SELECT IDREMISE FROM REMISES WHERE AGENCEDEPOT='" + Utility.getParam("CASH_MANAGEMENT") + "' )) ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        if (cheques != null && 0 < cheques.length) {
            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques non compensables validés d'une remise AND LOTSIB=1 
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + "    AND ETAT =" + Utility.getParam("CETAOPESUPVALSURCAI");
                chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                //La remise en question
                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                //Tous les cheques de la remise (compensables et non)
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise();
                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                j = chequesVal.length;

                if ((remises != null && 0 < remises.length)
                        && (allChequesVal.length == remises[0].getNbOperation().intValue())) {
                    if (chequesVal != null && 0 < chequesVal.length) {
                        long sumRemise = 0;

                        for (int x = 0; x < chequesVal.length; x++) {
                            sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;
//Creation ligne de chèque

                        for (int x = 0; x < chequesVal.length; x++) {
                            aCheque = chequesVal[x];
                            RIO rio = new RIO(CMPUtility.getCodeBanqueSica3() + CMPUtility.getPacSCMPSICA3() + CMPUtility.getDevise() + "001" + Utility.getParam("DATECOMPENS_NAT") + "00001" + Utility.bourrageGZero("" + aCheque.getIdcheque(), 8));
                            aCheque.setRio(rio.getRio());

                            line = new StringBuffer();
                            line.append(aCheque.getBanqueremettant());
                            line.append(aCheque.getBanque());
                            line.append("Q06 ");
                            line.append(Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " "));
                            line.append(Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), ResLoader.getMessages("patternDate")), "yyyyMMdd"));
                            //line += CMPUtility.getDate();
                            line.append(dateValeur);
                            line.append(Utility.bourrageGauche(aCheque.getMontantcheque(), 16, " "));
                            line.append(createBlancs(3, " ") + "UAP");
                            line.append(aCheque.getRio());
                            String numCptEx = CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(),"0");
                            
                            line.append(CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(),"0"));
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getNumerocompte(), aCheque.getAgence()));
                            writeln(line.toString());
                            aCheque.setRio("");
                            aCheque.setLotsib(new BigDecimal(2));

                            //aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }

                        //  db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }

                } else {

                    // db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    // db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }

            }

            closeFile();

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
