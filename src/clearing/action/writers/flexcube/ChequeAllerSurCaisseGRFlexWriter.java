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
public class ChequeAllerSurCaisseGRFlexWriter extends FlatFileWriter {

    public ChequeAllerSurCaisseGRFlexWriter() {
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
        System.out.println("Date Valeur = " + dateValeur);

        String dateValeurGRJ1 = "";
        String[] param2 = (String[]) getParametersMap().get("param2");
        if (param2 != null && param2.length > 0) {
            dateValeurGRJ1 = param2[0];
        }
        System.out.println("Date Valeur J+1 GR= " + dateValeurGRJ1);

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQCAI", "CHQCAI"), 4, "0");
        //Fichier LCDa
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

// Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        Cheques[] chequesVal = null;
        Remises[] remises = null;
        Cheques aCheque = null;

        if (cheques != null && 0 < cheques.length) {
            StringBuffer line = new StringBuffer("H001UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            line.append(createBlancs(76, " "));
            writeln(line.toString());

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques non compensables validés d'une remise
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

                        aCheque = chequesVal[0];
                        for (int x = 0; x < chequesVal.length; x++) {
                            sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;
//Creation ligne de chèque
                        if (aCheque.getEscompte().intValue() == 1) {

                            //Credit du bordereau remise sur le compte du gros remettant
                            line = new StringBuffer();
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()));
                            line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"0"), 16, " "));
                            line.append(createBlancs(4, " "));
                            line.append("C");
                            line.append(Utility.bourrageGauche("" + sumRemise, 16, " "));
                            line.append("Q13");
                            line.append(Utility.getParam("DATEVALEUR_ALLER"));
                            line.append(" ");
                            line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                            line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 18, " "));
                            line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
                            line.append("030");
                            line.append(createBlancs(8, "0"));
                            writeln(line.toString());

                            //Ligne de credit du detail de la remise sur le compte d'attente
                            for (int x = 0; x < chequesVal.length; x++) {
                                aCheque = chequesVal[x];
                                //Ligne de credit
                                line = new StringBuffer();
                                line.append("001");
                                line.append(Utility.bourrageDroite(Utility.getParam("CPTATTCHQCAIFLEX"), 16, " ") );
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

                            }

                            //Ligne de debit du montant du bordereau sur le compte d'attente
                            //Ligne de debit
                            line = new StringBuffer();
                            line.append("001");
                            line.append(Utility.bourrageDroite(Utility.getParam("CPTATTCHQCAIFLEX"), 16, " ")  );
                            line.append(createBlancs(4, " "));
                            line.append("D");
                            line.append(Utility.bourrageGauche("" + sumRemise, 16, " "));
                            line.append("Q13");
                            line.append(dateValeur);
                            line.append(createBlancs(8, " "));
                            line.append(Utility.getParam("LIBCHQALEFLEX1"));
                            line.append(createBlancs(24, " "));
                            writeln(line.toString());

                        } else {

                            for (int x = 0; x < chequesVal.length; x++) {
                                aCheque = chequesVal[x];

                                line = new StringBuffer();
                                line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()));
                                line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"0"), 16, " "));
                                line.append(createBlancs(4, " "));
                                line.append("C");
                                line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                                line.append("Q13");
                                line.append(dateValeurGRJ1);
                                line.append(" ");
                                line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                                line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 18, " "));
                                line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
                                line.append("030");
                                line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));
                                writeln(line.toString());

                            }

                        }

                    }

                } else {

                    db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }

            }
            line = new StringBuffer();
            line.append("001");
            line.append( Utility.bourrageDroite(Utility.getParam("CPTATTCHQCAIFLEX"), 16, " "));
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
            montantTotal = 0;

            //Fichier LCDb
            fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
            setOut(createFlatFile(fileName));
            line = new StringBuffer("H001UAP");
            line.append(Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQCAI", "CHQCAI"), 4, "0"));
            line.append(CMPUtility.getDate());
            line.append(createBlancs(76, " "));
            writeln(line.toString());

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques non compensables validés d'une remise
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
//Creation ligne de chèque

                        for (int x = 0; x < chequesVal.length; x++) {
                            aCheque = chequesVal[x];

                            line = new StringBuffer();
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getNumerocompte(),aCheque.getAgence() )   );
                            line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(),"0"), 16, " "));
                            line.append(createBlancs(4, " "));
                            line.append("D");
                            line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                            line.append("Q13");
                            line.append(Utility.getParam("DATEVALEUR_ALLER"));
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

                } else {

                    db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
                }

            }
            line = new StringBuffer();
            line.append("001");
            line.append(Utility.bourrageDroite(Utility.getParam("CPTATTCHQCAIFLEX"), 16, " ") );
            line.append(createBlancs(4, " "));
            line.append("C");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, " "));
            line.append("Q13");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());
            closeFile();

            setDescription(getDescription() + " exécuté avec succès: Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
