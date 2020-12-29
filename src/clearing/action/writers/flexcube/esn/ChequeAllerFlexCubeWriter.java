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
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerFlexCubeWriter extends FlatFileWriter {

    public ChequeAllerFlexCubeWriter() {

    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);

        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);

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

        long montantTotalRejete = 0;
        

        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyy");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + dateTraitement + Utility.getParam("SIB_FILE_EXTENSION");

        setOut(createFlatFile(fileName));
        if (cheques != null && 0 < cheques.length) {

            StringBuffer line = new StringBuffer("H001UAP");
            line.append(numeroBatch.toLowerCase());
            line.append(CMPUtility.getDate());
            writeln(line.toString());

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                if (!isValidLine(cheque)) {
                } else {
                    createLinesRejetFlex(cheque);
                    if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))) {
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }
                    montantTotalRejete += Long.parseLong(cheque.getMontantcheque());
                }

            }
            setDescription("Envoi des rejets de chèques Aller vers le SIB");
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque rejete= " + cheques.length + " - Montant Total rejete= " + Utility.formatNumber("" + montantTotalRejete) + " - Nom de Fichier = " + fileName + "\n");
            logEvent("INFO", "Nombre de Chèque rejete = " + cheques.length + " - Montant Total rejete = " + Utility.formatNumber("" + montantTotalRejete));
        }
// Population
        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN  (" + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEREJRET") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
      

        if (cheques != null && 0 < cheques.length) {
            for (int i = 0; i < cheques.length; i += j) {

                if (!isValidLine(cheques[i])) {
                } else {
                    //Tous les cheques validés

                    //Tous les cheques compensables validés d'une remise
                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ")  AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
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
                                } else {
                                    libelle = "Versement Remise N°" + aCheque.getRemise();
                                }

                                //Credit du bordereau remise sur le compte du gros remettant
                                dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeurJ2, "yyyy/MM/dd"), "yyyyMMdd");
                                createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);

                            } else if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("C"))) {

                                libelle = "VRS CH" + aCheque.getNumerocheque() + "_" + aCheque.getRemise();
                                //Credit cheque par cheque sur le compte du gros remettant

                                dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeurJ2, "yyyy/MM/dd"), "yyyyMMdd");
                                for (int x = 0; x < chequesVal.length; x++) {

                                    montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                                    aCheque = chequesVal[x];
                                    createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                                }

                            }

                            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                            db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + " WHERE REMISE=" + aCheque.getRemise());
                        }
                    }
                }

            }

            setDescription(getDescription() +"Envoi des chèques Aller vers le SIB");
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription("Envoi des chèques Aller vers le SIB");
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();

        //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Creation du fichier d'echec
        //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") ";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        dateTraitement = Utility.convertDateToString(new Date(), "ddMMyy");
        fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_ERR_FILE_ROOTNAME") + dateTraitement + Utility.getParam("SIB_FILE_EXTENSION");

        setOut(createFlatFile(fileName));
        if (cheques != null && 0 < cheques.length) {

            StringBuffer line = new StringBuffer("H001UAP");
            line.append(numeroBatch.toLowerCase());
            line.append(CMPUtility.getDate());
            writeln(line.toString());

            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];

                createLinesRejetFlex(cheque);
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                montantTotalRejete += Long.parseLong(cheque.getMontantcheque());

            }
            setDescription("\nEchec des rejets de chèques Aller");
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque rejete en echec = " + cheques.length + " - Montant Total rejete en echec = " + Utility.formatNumber("" + montantTotalRejete) + " - Nom de Fichier = " + fileName + "\n");
            logEvent("INFO", "Nombre de Chèque rejete en echec = " + cheques.length + " - Montant Total rejete en echec = " + Utility.formatNumber("" + montantTotalRejete));
        }
// Population
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        j = 0;
        montantTotal = 0;
       

        if (cheques != null && 0 < cheques.length) {
            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques compensables validés d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ")  AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
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
                        if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("R"))) {

                            if ((cptGR.getSignature3() != null && cptGR.getSignature3().trim().equals("O"))) {
                                libelle = remises[0].getReference();
                            } else {
                                libelle = "Versement Remise N°" + aCheque.getRemise();
                            }

                            //Credit du bordereau remise sur le compte du gros remettant
                            dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeurJ2, "yyyy/MM/dd"), "yyyyMMdd");
                            createLinesFlex("" + sumRemise, dateValeur, aCheque, libelle, montantFrais);

                        } else if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("C"))) {

                            libelle = "VRS CH" + aCheque.getNumerocheque() + "_" + aCheque.getRemise();
                            //Credit cheque par cheque sur le compte du gros remettant

                            dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeurJ2, "yyyy/MM/dd"), "yyyyMMdd");
                            for (int x = 0; x < chequesVal.length; x++) {

                                montantFrais = Long.parseLong(Utility.getParam("FRAIS_SICA"));
                                aCheque = chequesVal[x];
                                createLinesFlex(aCheque.getMontantcheque(), dateValeur, aCheque, libelle, montantFrais);
                            }

                        }

                        db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                        db.executeUpdate("UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + " WHERE REMISE=" + aCheque.getRemise());
                    }
                }

            }

            setDescription(getDescription() +"\nFichier Echec des chèques Aller vers le SIB");
            setDescription(getDescription() + " :\n Nombre de Chèque en echec = " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque Aller en echec= " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantTotal));

        }
        closeFile();
        db.close();
    }

    private void createLinesRejetFlex(Cheques cheque) throws Exception {
        
        String numCptEx = CMPUtility.getNumCptEx(cheque.getCompteremettant(), cheque.getAgenceremettant(),"1");
        if (numCptEx == null) {
                    numCptEx = cheque.getAgenceremettant().substring(2) + "0" + cheque.getCompteremettant();
                }
        StringBuffer line;
        //Tous les cheques Aller rejetes - ligne de debit montant sur cpt
        line = new StringBuffer();
        line.append(cheque.getAgenceremettant().substring(2));
        line.append(Utility.bourrageDroite(numCptEx, 16, " "));
        line.append(createBlancs(4, " "));
        line.append("D");
        line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 16, " "));
        line.append("F03");
        line.append(CMPUtility.getDate());
        line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
        line.append(Utility.bourrageDroite("Rej Chq N°" + cheque.getNumerocheque() + " Rem" + cheque.getRemise() + " Mtf:" + Utility.getParamLabel(cheque.getMotifrejet()), 25, " "));
        line.append("030");
        line.append(cheque.getMotifrejet());
        writeln(line.toString());
        //Ligne de credit montant sur compte d'attente
        line = new StringBuffer();
        line.append(Utility.getParam("FLEXMAINBRANCH"));
        line.append(Utility.bourrageDroite(Utility.getParam("CPTGLOCOMREJCHQALE"), 16, " "));
        line.append(createBlancs(4, " "));
        line.append("C");
        line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 16, " "));
        line.append("F57");
        line.append(CMPUtility.getDate());
        line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
        line.append(Utility.bourrageDroite("Rejet Chq" + cheque.getNumerocheque() + ":" + cheque.getRemise(), 25, " "));
        line.append("030");
        writeln(line.toString());

        if (cheque.getMotifrejet().equalsIgnoreCase("201") || cheque.getMotifrejet().equalsIgnoreCase("202")) {
            //Ligne de debit commission

            line = new StringBuffer();
            line.append((Utility.bourrageGZero(numCptEx, 16)).substring(0, 3));
            line.append(Utility.bourrageGZero(numCptEx, 16));
            line.append(createBlancs(4, " "));
            line.append("D");
            line.append(Utility.bourrageGauche(Utility.getParamOfType("COMDEBREJCHQALEFLEX", "CODE_COMMISSION"), 16, " "));
            line.append("C59");
            line.append(CMPUtility.getDate());
            line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));

            line.append(Utility.bourrageDroite("Frais Rej" + cheque.getNumerocheque() + ":" + cheque.getRemise(), 25, " "));
            line.append("030");
            writeln(line.toString());

            //Ligne de credit com1
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQALE1"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("C");
            line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQALEFLEX1", "CODE_COMMISSION"), 16, " "));
            line.append("F57");
            line.append(CMPUtility.getDate());
            line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
            line.append(Utility.bourrageDroite("Com Rej Chq" + cheque.getNumerocheque() + ":" + cheque.getRemise(), 25, " "));
            line.append("030");
            writeln(line.toString());

            //Ligne de credit com2
            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMREJCHQALE2"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("C");
            line.append(Utility.bourrageGauche(Utility.getParamOfType("COMCREREJCHQALEFLEX2", "CODE_COMMISSION"), 16, " "));
            line.append("T03");
            line.append(CMPUtility.getDate());
            line.append(Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 8, " "));
            line.append(Utility.bourrageDroite("Com Rej Chq" + cheque.getNumerocheque() + ":" + cheque.getRemise(), 25, " "));
            line.append("030");
            writeln(line.toString());
        }
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
        libelle = "Frais sica Rem N°" + aCheque.getRemise();
        //Ligne de debit des frais sica sur le compte client
        line = new StringBuffer();
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

    private boolean isValidLine(Cheques cheque) throws Exception {
        //Verification de l'existence du compte
        String numCptEx = CMPUtility.getNumCptEx(cheque.getCompteremettant(), cheque.getAgenceremettant(),"1");
        if (numCptEx == null) {
            return false;
        }

        //Verification du compte de scan
        if (cheque.getCompteremettant().equals(Utility.getParam("CPTATTSCANCHQ"))) {
            return false;
        }

        return true;
    }
}
