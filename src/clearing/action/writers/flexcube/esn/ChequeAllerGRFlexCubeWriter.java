/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.esn;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Remises;
import clearing.table.flexcube.STTM_AEOD_DATES;
import java.io.File;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerGRFlexCubeWriter extends FlatFileWriter {

    public ChequeAllerGRFlexCubeWriter() {
        setDescription("Envoi des chèques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);

//        String dateCompensation = "";
//        param1 = (String[]) getParametersMap().get("param2");
//        if (param1 != null && param1.length > 0) {
//            dateCompensation = param1[0];
//        }
//        System.out.println("Date Compensation = " + dateCompensation);
        

        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
        String sql = "select to_char(next_working_day,'YYYY/MM/DD') as next_working_day from boesn.STTM_AEOD_DATES where branch_code ='001'";
        STTM_AEOD_DATES[] sttm_aeod_dates = (STTM_AEOD_DATES[]) dbExt.retrieveRowAsObject(sql, new STTM_AEOD_DATES());
        String dateValeurJ2 = "";
        if (sttm_aeod_dates != null && sttm_aeod_dates.length > 0) {
            dateValeurJ2 = sttm_aeod_dates[0].getNext_working_day();
        }
        dbExt.close();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

// Population
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") AND ESCOMPTE='1' ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;

        if (cheques != null && 0 < cheques.length) {
            
            String dateTraitement = Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatetraitement(), "yyyy/MM/dd"), "ddMMyy");
            String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_CRI_FILE_ROOTNAME") + dateTraitement + Utility.getParam("SIB_FILE_EXTENSION");

            setOut(createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H001UAP");
            line.append(numeroBatch.toLowerCase());
            line.append(CMPUtility.getDate());

            writeln(line.toString());

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques compensables validés d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") AND ESCOMPTE='1' ORDER BY REMISE";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                if (remises != null && remises.length > 0) {

                

                if (chequesVal != null && 0 < chequesVal.length) {
                    j = chequesVal.length;
                    long sumRemise = 0;
                    long montantFrais = j * Long.parseLong(Utility.getParam("FRAIS_SICA"));

                    Cheques aCheque = chequesVal[0];
                    for (int x = 0; x < chequesVal.length; x++) {
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                    }
                    montantTotal += sumRemise;

//Creation ligne de chèque
                    String libelle;
                    String dateValeur;
                    Comptes cptGR = CMPUtility.getInfoCompte(aCheque.getCompteremettant());
                    if (cptGR.getSignature2() == null || (cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("R"))) {

                        if ((cptGR.getSignature3() != null && cptGR.getSignature3().trim().equals("O"))) {
                            libelle = remises[0].getReference();
                        }else{
                            libelle = "Versement Remise N°" + aCheque.getRemise();
                        }
                        
                        //Credit du bordereau remise sur le compte du gros remettant

                        if ((cptGR.getSignature1() != null && cptGR.getSignature1().trim().equals("J"))) {

                            dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), "yyyy/MM/dd"), "yyyyMMdd");
                            createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);

                        }

                        if (cptGR.getSignature1() != null && cptGR.getSignature1().equals("J1")) {

                            dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatecompensation(), "yyyy/MM/dd"), "yyyyMMdd");
                            createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);
                        }
                        if (cptGR.getSignature1() != null && cptGR.getSignature1().equals("J2")) {

                            dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeurJ2, "yyyy/MM/dd"), "yyyyMMdd");
                            createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);
                        }

                    } else if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("C"))) {

                        libelle = "VRS CH" + aCheque.getNumerocheque() + "_" + aCheque.getRemise();
                        //Credit cheque par cheque sur le compte du gros remettant

                        if ((cptGR.getSignature1() != null && cptGR.getSignature1().trim().equals("J"))) {

                            dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), "yyyy/MM/dd"), "yyyyMMdd");
                            for (int x = 0; x < chequesVal.length; x++) {

                                montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                                aCheque = chequesVal[x];
                                createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                            }

                        }

                        if (cptGR.getSignature1() != null && cptGR.getSignature1().equals("J1")) {
                            dateValeur = Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatecompensation(), "yyyy/MM/dd"), "yyyyMMdd");
                            for (int x = 0; x < chequesVal.length; x++) {

                                montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                                aCheque = chequesVal[x];
                                createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                            }
                        }
                        if (cptGR.getSignature1() != null && cptGR.getSignature1().equals("J2")) {
                            dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeurJ2, "yyyy/MM/dd"), "yyyyMMdd");
                            for (int x = 0; x < chequesVal.length; x++) {

                                montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                                aCheque = chequesVal[x];
                                createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                            }
                        }

                    }

                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                    db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + " WHERE REMISE=" + aCheque.getRemise());
                }
            }

            }

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }

    private void createLinesFlex(String montantLigne, String dateValeur, Cheques aCheque, String libelle, long montantFrais) throws Exception {
        StringBuffer line;
        //Ligne de debit du montant du bordereau sur le compte d'attente
        //Ligne de debit
        line = new StringBuffer();
        line.append("001");
        line.append(Utility.getParam("CPTATTCHQALEFLEX"));
        line.append(createBlancs(11, " "));
        line.append("D");
        line.append(Utility.bourrageGauche(montantLigne, 16, " "));
        line.append("F03");
        line.append(dateValeur);//Jour J
        line.append(" ");
        line.append(Utility.bourrageDroite(aCheque.getRemise() + "", 7, " "));
        line.append(Utility.bourrageDroite(libelle, 25, " "));
        line.append("030");
        line.append(createBlancs(8, " "));
        writeln(line.toString());
        //Ligne de credit sur le compte client
        line = new StringBuffer();
        line.append((Utility.bourrageGauche(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"1"), 16, "0")).substring(0, 3));
        line.append(Utility.bourrageGauche(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"1"), 16, "0"));
        line.append(createBlancs(4, " "));
        line.append("C");
        line.append(Utility.bourrageGauche(montantLigne, 16, " "));
        line.append("F57");
        line.append(dateValeur);//Jour J
        line.append(" ");
        line.append(Utility.bourrageDroite(aCheque.getRemise() + "", 7, " "));
        line.append(Utility.bourrageDroite(libelle, 25, " "));
        line.append("030");
        line.append(createBlancs(8, " "));
        writeln(line.toString());
         
        //Ligne de debit des frais sica sur le compte client
        line = new StringBuffer();
        libelle = "Frais sica Rem N°" + aCheque.getRemise();
        line.append((Utility.bourrageGauche(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"1"), 16, "0")).substring(0, 3));
        line.append(Utility.bourrageGauche(CMPUtility.getNumCptEx(aCheque.getCompteremettant(), aCheque.getAgenceremettant(),"1"), 16, "0"));
        line.append(createBlancs(4, " "));
        line.append("D");
        line.append(Utility.bourrageGauche("" + montantFrais, 16, " "));
        line.append("C81");
        line.append(dateValeur);//Jour J
        line.append(" ");
        line.append(Utility.bourrageDroite(aCheque.getRemise() + "", 7, " "));
        line.append(Utility.bourrageDroite(libelle, 25, " "));
        line.append("030");
        line.append(createBlancs(8, " "));
        writeln(line.toString());
        
        //Ligne de credit des frais sica sur le compte de commission
        line = new StringBuffer();
        line.append("001");
        line.append(Utility.getParam("CPTCOMCHQALEFLEX"));
        line.append(createBlancs(11, " "));
        line.append("C");
        line.append(Utility.bourrageGauche("" + montantFrais, 16, " "));
        line.append("C81");
        line.append(dateValeur);//Jour J
        line.append(" ");
        line.append(Utility.bourrageDroite(aCheque.getRemise() + "", 7, " "));
        line.append(Utility.bourrageDroite(libelle, 25, " "));
        line.append("030");
        line.append(createBlancs(8, " "));
        writeln(line.toString());
    }

}
