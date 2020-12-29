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
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeAllerNonCompDebitFlexCubeEBJWriter extends FlatFileWriter {

    public ChequeAllerNonCompDebitFlexCubeEBJWriter() {
        setDescription("Envoi des chèques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        String userLogin = ((Utilisateurs) getParametersMap().get("user")).getLogin().trim();
        String numeroBatch = "";
        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if ((param1 != null) && (param1.length > 0)) {
            dateValeur = param1[0];
        }
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);

        int j = 0;
        long montantTotal = 0L;
        Cheques[] chequesVal = null;
        Remises[] remises = null;
        Cheques aCheque = null;

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME1")
                + Utility.convertDateToString(new Date(), "ddMMyyyy") + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

        String sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        if ((cheques != null) && (0 < cheques.length)) {
            setOut(createFlatFile(fileName));
            StringBuilder lineBuilder = new StringBuilder();
//
//            //entete
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
            writeln(lineBuilder.toString());
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
                            lineBuilder = new StringBuilder();
                            aCheque = chequesVal[x];
                            Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(aCheque.getNumerocompte(), aCheque.getAgence());

                            RIO rio = new RIO(CMPUtility.getCodeBanqueSica3() + CMPUtility.getPacSCMPSICA3() + CMPUtility.getDevise() + "001" + Utility.getParam("DATECOMPENS_NAT") + "00001" + Utility.bourrageGZero(new StringBuilder().append("").append(aCheque.getIdcheque()).toString(), 8));
                            aCheque.setRio(rio.getRio());

//////                            line = new StringBuffer();
//////                            line.append(aCheque.getBanqueremettant());
//////                            line.append(aCheque.getBanque());
//////                            line.append("Q06 ");
//////                            line.append(Utility.bourrageGauche(Utility.trimLeadingZero(aCheque.getNumerocheque()), 7, " "));
//////                            line.append(Utility.convertDateToString(Utility.convertStringToDate(aCheque.getDatetraitement(), ResLoader.getMessages("patternDate")), "yyyyMMdd"));
//////
//////                            line.append(dateValeur);
//////                            line.append(Utility.bourrageGauche(aCheque.getMontantcheque(), 16, " "));
//////                            line.append(createBlancs(3, " ") + "UAP");
//////                            line.append(aCheque.getRio());
//////
//////                            line.append(CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(), "0"));
//////                            line.append(CMPUtility.getNumCptExAgence(aCheque.getNumerocompte(), aCheque.getAgence()));
                            /**
                             * F12 CHEQUE DEBIT
                             */
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
                            writeln(lineBuilder.toString());
                            aCheque.setRio("");
                            aCheque.setLotsib(new BigDecimal(1));

                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }
                    }
                }
            }
            closeFile();

            setDescription(getDescription() + " executé avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber(new StringBuilder().append("").append(montantTotal).toString()) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber(new StringBuilder().append("").append(montantTotal).toString()));
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        db.close();
    }
}
