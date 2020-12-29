/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube12;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
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
public class ChequeRetourFlexCubeESNWriter extends FlatFileWriter {

    public ChequeRetourFlexCubeESNWriter() {
        setDescription("Envoi des chèques retour vers le SIB ");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        String userLogin = ((Utilisateurs) getParametersMap().get("user")).getLogin().trim();
        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);
        String dateValeur = "";
        String[] param2 = (String[]) getParametersMap().get("param1");
        if (param2 != null && param2.length > 0) {
            dateValeur = param2[0];
        }

        System.out.println("Date Valeur  = " + dateValeur + " yyyyMMdd ");
        Utility.convertStringToDate(dateValeur, "yyyyMMdd");
        //  dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "yyyyMMdd"), "ddMMyyyy");
        System.out.println("Date Valeur  Formated = " + dateValeur + " Cheque Retour FlexCube Writer");

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_FILE_ROOTNAME")
                + Utility.convertDateToString(new Date(), "ddMMyyyy") + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

        //Population
        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        if (cheques != null && 0 < cheques.length) {
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
            for (int i = 0; i < cheques.length; i++) {
                lineBuilder = new StringBuilder();
                Cheques cheque = cheques[i];
                Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(cheque.getNumerocompte(), cheque.getAgence());
                lineBuilder.append("I");//DIRECTION
                lineBuilder.append("~");
                lineBuilder.append("INCL");//PRODUCT_CODE
                lineBuilder.append("~");
                lineBuilder.append(Utility.getParam("TXN_BRANCH")); //TXN_BRANCH
                lineBuilder.append("~");
                lineBuilder.append("BCEAOEP");//END_POINT

                lineBuilder.append("~");
                lineBuilder.append(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + cheque.getNumerocompte()); //REM_ACCOUNT

                lineBuilder.append("~");
                lineBuilder.append(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : "" + Utility.getParam("TXN_BRANCH").charAt(0) + cheque.getAgence().substring(3));//ACC_BRANCH
                lineBuilder.append("~");
                lineBuilder.append("XOF"); // ACC_CCY
                lineBuilder.append("~");
                lineBuilder.append("XOF"); //INSTRUMENT_CCY
                lineBuilder.append("~");
                lineBuilder.append(new BigDecimal(cheque.getMontantcheque())); //INSTRUMENT_AMT
                lineBuilder.append("~");
                lineBuilder.append(cheque.getNumerocheque()); //INSTRUMENT_NO_1
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
                lineBuilder.append(cheque.getIdcheque()); //XREF
                lineBuilder.append("~");
                lineBuilder.append("FCRH");//SCODE
                lineBuilder.append("~");

                lineBuilder.append(1);//  //EVENT_SEQ_NO
                lineBuilder.append("~");
                lineBuilder.append("CG");//MODULE_CODE
                lineBuilder.append("~");
                lineBuilder.append(1); //MOD_NO
                lineBuilder.append("~");
                lineBuilder.append(Utility.bourrageDroite(cheque.getNombeneficiaire(), 27, " ")); //REMARKS
                lineBuilder.append(cheque.getRio().substring(27)); //REMARKS
                lineBuilder.append("~");
                lineBuilder.append("CHQ");
                lineBuilder.append("~");
                lineBuilder.append(cheque.getDateemission() != null ? (Utility.convertDateToString(Utility.convertStringToDate(cheque.getDateemission(), "yyyy/MM/dd"), Utility.getParam("DATE_FORMAT"))) : Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT"))); //CHEQUE_ISSUE_DATE //2018/10/10 
                lineBuilder.append("~");
                lineBuilder.append(numeroBatch);
                lineBuilder.append("~");
                lineBuilder.append(cheque.getBanqueremettant());
                lineBuilder.append("~");
                lineBuilder.append("Y");
                lineBuilder.append("~");

//                //Tous les cheques retours
//                String line = "";
//                line += cheque.getBanqueremettant();
//                line += cheque.getBanque();
//                line += "011 ";
//                line += Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ");
//                line += Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatecompensation(), ResLoader.getMessages("patternDate")), "yyyyMMdd");
//                //line += CMPUtility.getDate();
//                line += dateValeur;
//                line += Utility.bourrageGauche(cheque.getMontantcheque(), 16, " ");
//                line += createBlancs(3, " ") + "UAP";
//                line += cheque.getRio();
//
//                if (numCptEx == null) {
//                    numCptEx = cheque.getAgence().substring(2) + "0" + cheque.getNumerocompte();
//                }
//                line += Utility.bourrageGZero(numCptEx, 16);
//                line += Utility.bourrageGZero(numCptEx, 16).substring(0, 3);
//                writeln(line);
                writeln(lineBuilder.toString());
                cheque.setLotsib(new BigDecimal(1));
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                } else {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        //MAJ DES CHEQUES SANS IMAGES AVEC MOTIF REJET 215
        sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM2") + " , MOTIFREJET='215' WHERE ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " AND BANQUEREMETTANT IN (SELECT CODEBANQUE FROM BANQUES WHERE ALGORITHMEDECONTROLESPECIFIQUE=1)";
        db.executeUpdate(sql);
        db.close();
    }
}
