/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
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
public class ChequeAllerNonCompFlexCubeEBFWriter extends FlatFileWriter {

    public ChequeAllerNonCompFlexCubeEBFWriter() {
        setDescription("Envoi des ch�ques vers le SIB ChequeAllerNonCompFlexCubeEBFWriter");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        String dateValeurDebit = Utility.getParam("DATEVALEUR_RETOUR");
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateValeurDebit = param1[0];
        }
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQCAI", "CHQCAI"), 4, "0");

        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "l" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQCAI", "CHQCAI"), 3, "0");

        } else {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQCAI", "CHQCAI"), 4, "0");

        }

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");


// Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        Cheques[] chequesVal = null;
        Remises[] remises = null;
        Cheques aCheque = null;

        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            StringBuilder line = new StringBuilder("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            line.append(createBlancs(76, " "));
            writeln(line.toString());

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques valid�s

                //Tous les cheques non compensables valid�s d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT =" + Utility.getParam("CETAOPESUPVALSURCAI");
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
//Creation ligne de ch�que

                        for (int x = 0; x < chequesVal.length; x++) {
                            aCheque = chequesVal[x];

                            line = new StringBuilder();
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()) );
                            line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"0"), 16, " "));
                            line.append(createBlancs(4, " "));
                            line.append("C");
                            line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                            line.append("Q13");
                            line.append(dateValeur);
                            line.append(" ");
                            line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                            line.append(" ");
                            line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 18, " "));
                            line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
//                            line.append("030");
//                            line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));
                            writeln(line.toString());
                            //aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                            //db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }



                        //db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }

                } else {
                    //db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    //db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }


            }

            long montantTotalDebit = 0;

            line = new StringBuilder();


            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques valid�s

                //Tous les cheques non compensables valid�s d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT =" + Utility.getParam("CETAOPESUPVALSURCAI");
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
                        montantTotalDebit += sumRemise;
//Creation ligne de ch�que

                        for (int x = 0; x < chequesVal.length; x++) {
                            aCheque = chequesVal[x];

                            line = new StringBuilder();
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getNumerocompte(), aCheque.getAgence()));
                            line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(),"0"), 16, " "));
                            line.append(createBlancs(4, " "));
                            line.append("D");
                            line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                            line.append("Q13");
                            line.append(dateValeurDebit);
                            line.append(" ");
                            line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                              line.append(" ");
                            line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX3"), 18, " "));
                            line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
//                            line.append("030");
//                            line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));
                            writeln(line.toString());
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }



                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }

                } else {
                    // db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    // db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }


            }

            closeFile();

            setDescription(getDescription() + " ex�cut� avec succ�s:\n Nombre de Ch�que= " + cheques.length + " - Montant Total Credit= " + Utility.formatNumber("" + montantTotal)
                    + " - Montant Total Debit= " + Utility.formatNumber("" + montantTotalDebit) + " \n- Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Ch�que= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }



        db.close();
    }
}
