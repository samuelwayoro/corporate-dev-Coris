/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
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
public class ChequeAllerGRFlexCubeWriter1 extends FlatFileWriter {

    public ChequeAllerGRFlexCubeWriter1() {
        setDescription("Envoi des chèques vers le SIB");
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

        String dateValeurGRJ1 = "";
        String[] param3 = (String[]) getParametersMap().get("param3");
        if (param3 != null && param3.length > 0) {
            dateValeurGRJ1 = param3[0];
        }
        System.out.println("Date Valeur J+1 GR= " + dateValeurGRJ1);

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQALE", "CHQALE"), 4, "0");
            isEcobankStandard = false;
        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "a" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQALE", "CHQALE"), 3, "0");
            isEcobankStandard = false;
        } else {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQALE", "CHQALE"), 4, "0");
            isEcobankStandard = true;
        }

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");

        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");
// Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        long montantTotalEsc = 0;
        long montantTotalNonEsc = 0;

        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H001UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            if (isEcobankStandard) {
                line.append(createBlancs(76, " "));
            }
            writeln(line.toString());

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques compensables validés d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

//                //La remise en question
//                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
//                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
//
//                //Tous les cheques de la remise (compensables et non)
//                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise()  ;
//                Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                j = chequesVal.length;

                if (chequesVal != null && 0 < chequesVal.length) {
                    long sumRemise = 0;

                    Cheques aCheque = chequesVal[0];
                    for (int x = 0; x < chequesVal.length; x++) {
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                    }
                    montantTotal += sumRemise;

//Creation ligne de chèque
                    if (aCheque.getEscompte().intValue() == 1) {

                        Comptes cptGR = getInfoCompte(aCheque.getCompteremettant());

                        montantTotalEsc += sumRemise;
                        //Credit du bordereau remise sur le compte du gros remettant
                      
                        if (cptGR.getSignature1()!=null && cptGR.getSignature1().trim().equals("J")) {
                            
                            line = new StringBuffer();
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()));
                            line.append(Utility.bourrageGauche(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"0"), 16, "0"));
                            line.append(createBlancs(4, " "));
                            line.append("C");
                            line.append(Utility.bourrageGauche("" + sumRemise, 16, " "));
                            line.append("Q13");
                            line.append(Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), "yyyy/MM/dd"), "yyyyMMdd"));//Jour J
                            line.append(" ");
                            line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                            line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 100, " "));
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
                                line.append(Utility.getParam("CPTATTCHQALEFLEX"));
                                line.append(createBlancs(11, " "));
                                line.append("C");
                                line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                                line.append("Q13");
                                line.append(Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), "yyyy/MM/dd"), "yyyyMMdd"));//Jour J
                                line.append(" ");
                                line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                                line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 100, " "));
                                line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
                                line.append("030");
                                line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));
                                writeln(line.toString());
                                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                                db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                            }

                            //Ligne de debit du montant du bordereau sur le compte d'attente
                            //Ligne de debit
                            line = new StringBuffer();
                            line.append("001");
                            line.append(Utility.getParam("CPTATTCHQALEFLEX"));
                            line.append(createBlancs(11, " "));
                            line.append("D");
                            line.append(Utility.bourrageGauche("" + sumRemise, 16, " "));
                            line.append("Q13");
                            line.append(Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), "yyyy/MM/dd"), "yyyyMMdd"));//Jour J
                            line.append(createBlancs(8, " "));
                            line.append(Utility.getParam("LIBCHQALEFLEX1"));
                            line.append(createBlancs(24, " "));
                            writeln(line.toString());
                        }
                        
                        if (cptGR.getSignature1()!=null && cptGR.getSignature1().equals("J1")) {
                            line = new StringBuffer();
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()));
                            line.append(Utility.bourrageGauche(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"0"), 16, "0"));
                            line.append(createBlancs(4, " "));
                            line.append("C");
                            line.append(Utility.bourrageGauche("" + sumRemise, 16, " "));
                            line.append("Q13");
                            line.append(dateValeurGRJ1);//Jour J+1
                            line.append(" ");
                            line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                            line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 100, " "));
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
                                line.append(Utility.getParam("CPTATTCHQALEFLEX"));
                                line.append(createBlancs(11, " "));
                                line.append("C");
                                line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                                line.append("Q13");
                                line.append(dateValeurGRJ1);//Jour J+1
                                line.append(" ");
                                line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                                line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 100, " "));
                                line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
                                line.append("030");
                                line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));
                                writeln(line.toString());
                                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                                db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                            }

                            //Ligne de debit du montant du bordereau sur le compte d'attente
                            //Ligne de debit
                            line = new StringBuffer();
                            line.append("001");
                            line.append(Utility.getParam("CPTATTCHQALEFLEX"));
                            line.append(createBlancs(11, " "));
                            line.append("D");
                            line.append(Utility.bourrageGauche("" + sumRemise, 16, " "));
                            line.append("Q13");
                            line.append(dateValeurGRJ1);//Jour J+1
                            line.append(createBlancs(8, " "));
                            line.append(Utility.getParam("LIBCHQALEFLEX1"));
                            line.append(createBlancs(24, " "));
                            writeln(line.toString());
                        }

                    } else {
                        //Somme Totale des cheque non escomptes
                        montantTotalNonEsc += sumRemise;
                        //Tous les chq de la remise
                        for (int x = 0; x < chequesVal.length; x++) {
                            aCheque = chequesVal[x];
                            //Ligne de credit
                            line = new StringBuffer();
                            line.append(CMPUtility.getNumCptExAgence(aCheque.getCompteremettant(), aCheque.getAgenceremettant()));
                            line.append(Utility.bourrageGauche(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"0"), 16, "0"));
                            line.append(createBlancs(4, " "));
                            line.append("C");
                            line.append(Utility.bourrageGauche("" + aCheque.getMontantcheque(), 16, " "));
                            line.append("Q13");
                            line.append(dateValeur);
                            line.append(" ");
                            line.append(Utility.bourrageGauche(aCheque.getRemise() + "", 7, "0"));
                            line.append(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX1"), 100, " "));
                            line.append(Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"));
//                            line.append("030");
//                            line.append(Utility.bourrageGauche(aCheque.getIdcheque() + "", 8, "0"));
                            writeln(line.toString());
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }

                    }

                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                }

            }
            //Ligne de debit
            line = new StringBuffer();
            line.append("001");
            line.append(Utility.getParam("CPTATTCHQALEFLEX"));
            line.append(createBlancs(11, " "));
            line.append("D");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, "0"));
            line.append("Q13");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));
            writeln(line.toString());

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }

    public Comptes getInfoCompte(String numeroCompte) {
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero like '%" + numeroCompte + "%'", new Comptes());

            if (comptes != null && comptes.length > 0) {
                comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));
                if (comptes[0].getPrenom() != null) {
                    comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                }
                if (comptes[0].getAdresse1() != null) {
                    comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                }

                return comptes[0];
            }
            db.close();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
