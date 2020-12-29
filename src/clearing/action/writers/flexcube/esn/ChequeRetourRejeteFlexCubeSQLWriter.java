/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.esn;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.flexcube.STTM_BRANCH;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequeRetourRejeteFlexCubeSQLWriter extends FlatFileWriter {

    public ChequeRetourRejeteFlexCubeSQLWriter() {
        setDescription("Envoi des rejets de chèques Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
        String sql = "select  CURRENT_CYCLE,CURRENT_PERIOD from "+Utility.getParam("FLEXSCHEMA") +".STTM_BRANCH where branch_code='001' ";
        STTM_BRANCH[] sttm_branch = (STTM_BRANCH[]) dbExt.retrieveRowAsObject(sql, new STTM_BRANCH());
        String current_cycle = "";
        String current_period = "";
        if (sttm_branch != null && sttm_branch.length > 0) {
            current_cycle = sttm_branch[0].getCurrent_cycle();
            current_period = sttm_branch[0].getCurrent_period();
        }
        dbExt.close();

        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        String dateValeur = "";
        param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        dateValeur = Utility.convertDateToString(Utility.convertStringToDate(dateValeur, "yyyyMMdd"), "ddMMyyyy");
        System.out.println("Date Valeur = " + dateValeur);

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_REJ_RET_FILE_ROOTNAME") + Utility.convertDateToString(new Date(), "ddMMyyyy") +"_"+numeroBatch+ Utility.getParam("SIB_FILE_SQL_EXTENSION");

        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ACC") + ") ";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        long montantTotal = 0;
        int numLigne = 1;

        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));

            createEnteteSQL();
            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Deroule le schema comptable

                numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + cheque.getIdcheque(), cheque.getMontantcheque(),
                        dateValeur,
                        cheque, "Rejet Cheque N°:" + Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ") + " Pour " + ((cheque.getIban()==null)?"":cheque.getIban()) +":"+ Utility.getParamLabel(cheque.getMotifrejet())); //Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " "

                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ACC"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }

                montantTotal += Long.parseLong(cheque.getMontantcheque());
            }

            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }

    private void createEnteteSQL() {
        StringBuilder line = new StringBuilder();

        //entete
        line.append("LOAD DATA\n"
                + "INFILE *\n"
                + "APPEND\n"
                + "INTO TABLE DETB_UPLOAD_DETAIL\n"
                + "Fields terminated by '~'\n"
                + "Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account)\n"
                + "BEGINDATA");
        writeln(line.toString());
    }

    /**
     *
     * @param current_cycle Annee Fiscale
     * @param current_period Mois
     * @param numeroBatch numero de Batch
     * @param numLigne Numero incrementiel de la ligne
     * @param separateur Separateur ~
     * @param referenceRelative Reference relative (IDREMISE ou IDCHEQUE)
     * @param montantLigne Montant de la Ligne inseree
     * @param dateValeur Date Valeur
     * @param aCheque le Cheque
     * @param libelle Libelle
     * @param montantFrais Montant des Frais
     * @throws Exception
     */
    private int createLinesSQL(String current_cycle, String current_period, String numeroBatch, int numLigne, String separateur, String referenceRelative,
            String montant, String dateValeur, Cheques aCheque, String libelle) throws Exception {
        StringBuilder line;
        String numCptEx = CMPUtility.getNumCptEx(aCheque.getNumerocompte(), aCheque.getAgence(),"1");
        if (numCptEx == null) {
            numCptEx = aCheque.getAgence().substring(2) + "0" + aCheque.getNumerocompte();
        }
        System.out.println("dateValeeur  createLinesSQL " + dateValeur);

        /**
         * Format SQL LOADER
         */
        //Ligne 1 (Extourne du montant préalablement débité)
        line = new StringBuilder();
        line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
        line.append(separateur);
        line.append(current_period); //PERIOD_CODE a recuperer par une requete
        line.append(separateur);
        line.append(dateValeur); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append("001"); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append(numCptEx.substring(0, 3)); //agence du compte
        line.append(separateur);
        line.append(numCptEx); //Compte Client ou attente
        line.append(separateur);
        line.append("F57");
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montant);
        line.append(separateur);
        line.append("C");
        line.append(separateur);
        line.append("U");
        line.append(separateur);
        line.append("XOF");
        line.append(separateur);
        line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
        line.append(separateur);
        line.append(montant);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append((Utility.getParam("CPTDEBREJCHQRET").length() == 16) ? Utility.getParam("CPTDEBREJCHQRET").substring(6, 14) : "99999999"); //rel_cust (Utility.getParam("CPTDEBREJCHQRET").length() == 16) ? Utility.getParam("CPTDEBREJCHQRET").substring(6, 14) : "99999999"
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTDEBREJCHQRET")); // 
        writeln(line.toString());

        //Ligne de DEBIT compte d'attente
        line = new StringBuilder();
        line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
        line.append(separateur);
        line.append(current_period); //PERIOD_CODE a recuperer par une requete
        line.append(separateur);
        line.append(dateValeur); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append("001"); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append("ECOSOURCE");
        line.append(separateur);
        line.append("001");  //agence du compte
        line.append(separateur);
        line.append(Utility.getParam("CPTDEBREJCHQRET")); //Compte Client ou attente
        line.append(separateur);
        line.append("F03");
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montant);
        line.append(separateur);
        line.append("D");
        line.append(separateur);
        line.append("U");
        line.append(separateur);
        line.append("XOF");
        line.append(separateur);
        line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
        line.append(separateur);
        line.append(montant);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(numCptEx); // 
        writeln(line.toString());
        System.out.println("dateValeur  createLinesSQL " + dateValeur);

        //Prise de commissions sur les motifs Rejet 201 & 202
        if (aCheque.getMotifrejet().equalsIgnoreCase("201") || aCheque.getMotifrejet().equalsIgnoreCase("202")) {

            //Debit Client du montant total des frais
            // libelle = "Nos Frais Rejet Cheque:" + aCheque.getNumerocheque();
            libelle = "DEBIT NOS FRAIS REJET + TOB COMPTE CLIENT :" + aCheque.getNumerocheque() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet());
            line = new StringBuilder();
            System.out.println("dateValeur Param pour le rejet : " + dateValeur);
            line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
            line.append(separateur);
            line.append(current_period); //PERIOD_CODE a recuperer par une requete
            line.append(separateur);
            line.append(dateValeur); //VALUE_DATE a recuperer par une requete
            line.append(separateur);
            line.append(libelle);
            line.append(separateur);
            line.append("001"); //Agence ou le chargement est effectue
            line.append(separateur);
            line.append("ECOSOURCE");
            line.append(separateur);
            line.append(numCptEx.substring(0, 3)); //agence du compte
            line.append(separateur);
            line.append(numCptEx); //Compte Client ou attente
            line.append(separateur);
            line.append("C59");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(Utility.getParam("COMDEBREJCHQRET"));
            line.append(separateur);
            line.append("D");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParam("COMDEBREJCHQRET")); //montant 
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append((Utility.getParam("CPTCRECOMREJCHQRET1").length() == 16) ? Utility.getParam("CPTCRECOMREJCHQRET1").substring(6, 14) : "99999999"); //rel_cust (Utility.getParam("CPTDEBREJCHQRET").length() == 16) ? Utility.getParam("CPTDEBREJCHQRET").substring(6, 14) : "99999999"
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(Utility.getParam("CPTCRECOMREJCHQRET1")); // 
            writeln(line.toString());

            //Credit du compte de commission
            libelle = "CREDIT COMPTE NOS FRAIS REJET " + aCheque.getNumerocheque() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet());
            line = new StringBuilder();
            line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
            line.append(separateur);
            line.append(current_period); //PERIOD_CODE a recuperer par une requete
            line.append(separateur);
            line.append(dateValeur); //VALUE_DATE a recuperer par une requete
            line.append(separateur);
            line.append(libelle);
            line.append(separateur);
            line.append("001"); //Agence ou le chargement est effectue
            line.append(separateur);
            line.append("ECOSOURCE");
            line.append(separateur);
            line.append("001");
            line.append(separateur);
            line.append(Utility.getParam("CPTCRECOMREJCHQRET1")); //Compte Client ou attente
            line.append(separateur);
            line.append("C59");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(Utility.getParam("COMCREREJCHQRETFLEX1")); //COMCREREJCHQRET
            line.append(separateur);
            line.append("C");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParam("COMCREREJCHQRETFLEX1"));
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptEx); // 
            writeln(line.toString());

            //Credit du compte des frais
            line = new StringBuilder();
            libelle = "CREDIT COMPTE TOB " + aCheque.getNumerocheque() + " Motif Rejet :" + Utility.getParamLabel(aCheque.getMotifrejet());
            line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
            line.append(separateur);
            line.append(current_period); //PERIOD_CODE a recuperer par une requete
            line.append(separateur);
            line.append(dateValeur); //VALUE_DATE a recuperer par une requete
            line.append(separateur);
            line.append(libelle);
            line.append(separateur);
            line.append("001"); //Agence ou le chargement est effectue
            line.append(separateur);
            line.append("ECOSOURCE");
            line.append(separateur);
            line.append("001");
            line.append(separateur);
            line.append(Utility.getParam("CPTCRECOMREJCHQRET2"));   //Compte Client ou attente
            line.append(separateur);
            line.append("T03");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(Utility.getParam("COMCREREJCHQRETFLEX2"));
            line.append(separateur);
            line.append("C");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParam("COMCREREJCHQRETFLEX2"));
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust (Utility.getParam("CPTATTCHQCAIFLEX").length() == 16) ? Utility.getParam("CPTATTCHQCAIFLEX").substring(6, 14) : "99999999"
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(numCptEx); // 
            writeln(line.toString());
        }
        return numLigne;
    }

}
