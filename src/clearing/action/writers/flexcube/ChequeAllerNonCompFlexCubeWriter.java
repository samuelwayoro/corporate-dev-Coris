/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Remises;
import clearing.table.Utilisateurs;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerNonCompFlexCubeWriter extends FlatFileWriter {

    public ChequeAllerNonCompFlexCubeWriter() {
        setDescription("Envoi des chèques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        String userLogin = ((Utilisateurs) getParametersMap().get("user")).getLogin().trim();
        String numeroBatch = "";
        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        String dateValeurDebit = Utility.getParam("DATEVALEUR_ALLER");
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateValeurDebit = param1[0];
        }
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);
        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTCHQCAI", "CHQCAI"), 4, "0");

        } else {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTCHQCAI", "CHQCAI"), 4, "0");

        }

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
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXMAINBRANCH") + "UAP");
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

                        for (int x = 0; x < chequesVal.length; x++) {
                            sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                        }
                        montantTotal += sumRemise;
//Creation ligne de chèque

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
                            //aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALSURCAIENVSIB")));
                            //db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }

                        //db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                    }

                } else {

                    db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
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
            montantTotal = 0;
//            fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1") + compteur + Utility.getParam("SIB_FILE_EXTENSION");

            fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1")
                    + Utility.convertDateToString(new Date(), "ddMMyyyy") + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

            setOut(createFlatFile(fileName));
            StringBuilder lineBuilder = new StringBuilder();     //entete
            lineBuilder.append("LOAD DATA\n"
                    + "INFILE *\n"
                    + "APPEND\n"
                    + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + " .CSTB_IW_CLEARING_MASTER\n"
                    + "Fields terminated by '~'\n"
                    + "Trailing Nullcols\n"
                    + " (DIRECTION,PRODUCT_CODE,TXN_BRANCH,END_POINT,REM_ACCOUNT,ACC_BRANCH,"
                    + " ACC_CCY,INSTRUMENT_CCY,INSTRUMENT_AMT,INSTRUMENT_NO_1,"
                    + "STATUS,TXN_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\"  ,INSTRUMENT_DATE  DATE \"" + Utility.getParam("DATE_FORMAT") + "\" ," //\"DD-MM-YYYY\"
                    + "ROUTING_NO,RECORD_STAT,AUTH_STAT,MAKER_ID,"
                    + "MAKER_DT_STAMP  DATE \"" + Utility.getParam("DATE_FORMAT") + "\"  ,XREF,SCODE,EVENT_SEQ_NO,"
                    + "MODULE_CODE,MOD_NO,REMARKS,INSTRUMENT_TYPE,CHEQUE_ISSUE_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\"  ,BATCH_NO,BANK_CODE\n, FORCE_POSTING "
                    + "  )\n"
                    + "BEGINDATA");
            line = new StringBuffer();

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
                            Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(aCheque.getNumerocompte(), aCheque.getAgence());
                            RIO rio = new RIO(CMPUtility.getCodeBanqueSica3() + CMPUtility.getPacSCMPSICA3() + CMPUtility.getDevise() + "001" + Utility.getParam("DATECOMPENS_NAT") + "00001" + Utility.bourrageGZero("" + aCheque.getIdcheque(), 8));
                            aCheque.setRio(rio.getRio());

////                            line = new StringBuffer();
////                            line.append(aCheque.getBanqueremettant());
////                            line.append(aCheque.getBanque());
////                            line.append("Q13 ");
////                            line.append(Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " "));
////                            line.append(Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), ResLoader.getMessages("patternDate")), "yyyyMMdd"));
////                            //line += CMPUtility.getDate();
////                            line.append(dateValeurDebit);
////                            line.append(Utility.bourrageGauche(aCheque.getMontantcheque(), 16, " "));
////                            line.append(createBlancs(3, " ") + "UAP");
////                            line.append(aCheque.getRio());
////
////                            line.append(CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(), "0"));
////                            line.append(CMPUtility.getNumCptExAgence(aCheque.getNumerocompte(), aCheque.getAgence()));

                            
                            
                            lineBuilder.append("I");//DIRECTION
                            lineBuilder.append("~");
                            lineBuilder.append("INCL");//PRODUCT_CODE
                            lineBuilder.append("~");
                            lineBuilder.append(Utility.getParam("TXN_BRANCH")); //TXN_BRANCH
                            lineBuilder.append("~");
                            lineBuilder.append("BCEAOEP");//END_POINT

                            lineBuilder.append("~");
                            lineBuilder.append(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + aCheque.getNumerocompte()); //REM_ACCOUNT

                            lineBuilder.append("~");
                            lineBuilder.append(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : "" + Utility.getParam("TXN_BRANCH").charAt(0) + aCheque.getAgence().substring(3));//ACC_BRANCH
                            lineBuilder.append("~");
                            lineBuilder.append("XOF"); // ACC_CCY
                            lineBuilder.append("~");
                            lineBuilder.append("XOF"); //INSTRUMENT_CCY
                            lineBuilder.append("~");
                            lineBuilder.append(new BigDecimal(aCheque.getMontantcheque())); //INSTRUMENT_AMT
                            lineBuilder.append("~");
                            lineBuilder.append(aCheque.getNumerocheque()); //INSTRUMENT_NO_1
                            lineBuilder.append("~");
                            lineBuilder.append("UNPR"); //STATUS
                            lineBuilder.append("~");
                            lineBuilder.append(Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "yyyyMMdd"), Utility.getParam("DATE_FORMAT"))); //TXN_DATE 
                            lineBuilder.append("~");
                            lineBuilder.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT")));//INSTRUMENT_DATE

                            lineBuilder.append("~");
                            lineBuilder.append("0051");//ROUTING_NO

                            lineBuilder.append("~");
                            lineBuilder.append("O");//RECORD_STAT

                            lineBuilder.append("~");
                            lineBuilder.append("A");//AUTH_STAT

                            lineBuilder.append("~");
                            lineBuilder.append(userLogin);//MAKER_ID

                            lineBuilder.append("~");
                            //MAKER_DT_STAMP
                            lineBuilder.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT"))); //MAKER_DT_STAMP
                            lineBuilder.append("~");
                            lineBuilder.append(aCheque.getIdcheque()); //XREF
                            lineBuilder.append("~");
                            lineBuilder.append("FCRH");//SCODE
                            lineBuilder.append("~");

                            lineBuilder.append(1);//  //EVENT_SEQ_NO
                            lineBuilder.append("~");
                            lineBuilder.append("CG");//MODULE_CODE
                            lineBuilder.append("~");
                            lineBuilder.append(1); //MOD_NO
                            lineBuilder.append("~");
                            lineBuilder.append("UAP" + aCheque.getRio());
                            lineBuilder.append("~");
                            lineBuilder.append("CHQ");
                            lineBuilder.append("~");
                            lineBuilder.append(aCheque.getDateemission() != null ? (Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDateemission(), "yyyy/MM/dd"), Utility.getParam("DATE_FORMAT"))) : Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT"))); //CHEQUE_ISSUE_DATE //2018/10/10 
                            lineBuilder.append("~");
                            lineBuilder.append(numeroBatch);
                            lineBuilder.append("~");
                            lineBuilder.append(aCheque.getBanqueremettant());
                            lineBuilder.append("~");
                            lineBuilder.append("Y");
                            lineBuilder.append("~");

//                            writeln(line.toString());
                            aCheque.setRio("");

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

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
