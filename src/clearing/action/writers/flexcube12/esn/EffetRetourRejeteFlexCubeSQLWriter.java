/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube12.esn;

import clearing.model.CMPUtility;
import clearing.table.Comptes;
import clearing.table.Effets;
import clearing.table.Utilisateurs;
import clearing.table.flexcube.STTM_BRANCH;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.bean.table.Params;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class EffetRetourRejeteFlexCubeSQLWriter extends FlatFileWriter {

    public EffetRetourRejeteFlexCubeSQLWriter() {
        setDescription("Envoi des rejets de chèques Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
        String sql = "select  CURRENT_CYCLE,CURRENT_PERIOD from " + Utility.getParam("FLEXSCHEMA") + ".STTM_BRANCH where branch_code='" + Utility.getParam("TXN_BRANCH") + "' ";
        STTM_BRANCH[] sttm_branch = (STTM_BRANCH[]) dbExt.retrieveRowAsObject(sql, new STTM_BRANCH());
        String current_cycle = "";
        String current_period = "";
        if (sttm_branch != null && sttm_branch.length > 0) {
            current_cycle = sttm_branch[0].getCurrent_cycle();
            current_period = sttm_branch[0].getCurrent_period();
        }
        dbExt.close();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String userLogin = ((Utilisateurs) getParametersMap().get("user")).getLogin().trim();
        Params param = new Params();
        param.setNom("USER_IN_SESSION");
        param.setValeur(userLogin);
        String params = Utility.getParam(param.getNom());
        if (params != null) {
            sql = "UPDATE PARAMS SET VALEUR='" + param.getValeur() + "' WHERE NOM='" + param.getNom() + "'";
            db.executeUpdate(sql);
        } else {
            db.insertObjectAsRowByQuery(param, "PARAMS");
        }
        Utility.clearParamsCache();
        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_REJ_RET_FILE_ROOTNAME") + Utility.convertDateToString(new Date(), "ddMMyyyy")  + "_" + numeroBatch +  Utility.getParam("SIB_FILE_SQL_EXTENSION");

        sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ACC") + ") ";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

        long montantTotal = 0;
        int numLigne = 1;
        if (effets != null && 0 < effets.length) {
            setOut(createFlatFile(fileName));

            createEnteteSQL();
            for (int i = 0; i < effets.length; i++) {
                Effets effet = effets[i];
                //Deroule le schema comptable
                numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + effet.getIdeffet(), effet.getMontant_Effet(),
                        Utility.convertDateToString(new Date(), "ddMMyyyy"),
                        effet, "Rejet Effet N°:" + Utility.bourrageGauche(Utility.trimLeadingZero(effet.getNumeroeffet()), 7, " ") + " Pour " + Utility.getParamLabel(effet.getMotifrejet())); //Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " "

                if (effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ACC"))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACCENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }

                montantTotal += Long.parseLong(effet.getMontant_Effet());
            }

            setDescription(getDescription() + " exécuté avec succès:<br> Nombre d'Effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre d'Effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
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
            String montant, String dateValeur, Effets effet, String libelle) throws Exception {
        StringBuilder line;
        Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(effet.getNumerocompte_Tire(), effet.getAgence());
//        String numCptEx = CMPUtility.getNumCptEx(effet.getNumerocompte_Tire(), effet.getAgence());
//        if (numCptEx == null) {
//            numCptEx = effet.getAgence().substring(2) + "0" + effet.getNumerocompte_Tire();
//        }

        /**
         * Format SQL LOADER
         */
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
        line.append(Utility.getParam("BATCH_TYPE"));
        line.append(separateur);
        line.append("001");  //agence du compte
        line.append(separateur);
        line.append(Utility.getParam("CPTCREEFFRET")); //Compte attente
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
        line.append(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : ""); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(compteFlexCube != null ? compteFlexCube.getNumcptex() : ""); // 
        writeln(line.toString());

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
        line.append(Utility.getParam("BATCH_TYPE"));
        line.append(separateur);
        line.append(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : effet.getAgence().substring(2)); //agence du compte
        line.append(separateur);
        line.append(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Tire()); //Compte Client ou attente
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
        line.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT")));   //INITIATION_DATE
        line.append(separateur);
        line.append(montant);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append((Utility.getParam("CPTCREEFFRET").length() == 16) ? Utility.getParam("CPTCREEFFRET").substring(6, 14) : "999999992"); //rel_cust /(Utility.getParam("CPTDEBREJCHQRET").length() == 16) ? Utility.getParam("CPTDEBREJCHQRET").substring(6, 14) : "999999992"
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTCREEFFRET")); // 
        writeln(line.toString());

        //Prise de commissions sur les motifs Rejet 201 & 202
        if (effet.getMotifrejet().equalsIgnoreCase("201") || effet.getMotifrejet().equalsIgnoreCase("202")) {

            //Debit Client du montant total des frais
//            libelle = "Nos Frais Rej Chq:" + effet.getNumeroeffet();
            libelle = "DEBIT NOS FRAIS REJET + TOB COMPTE CLIENT :" + effet.getNumeroeffet() + " Motif Rejet :" + Utility.getParamLabel(effet.getMotifrejet());
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
            line.append(Utility.getParam("BATCH_TYPE"));
            line.append(separateur);
            line.append(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : effet.getAgence().substring(2)); //agence du compte
            line.append(separateur);
            line.append(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Tire()); //Compte Client ou attente
            line.append(separateur);
            line.append("K10");
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
            line.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT")));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParam("COMDEBREJCHQRET")); //montant 
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append((Utility.getParam("CPTCRECOMREJCHQRET1").length() == 16) ? Utility.getParam("CPTCRECOMREJCHQRET1").substring(6, 14) : "999999992"); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(Utility.getParam("CPTCRECOMREJCHQRET1")); // 
            writeln(line.toString());

            //Credit du compte de commission
            line = new StringBuilder();
            libelle = "CREDIT COMPTE NOS FRAIS REJET " + effet.getNumeroeffet() + " Motif Rejet :" + Utility.getParamLabel(effet.getMotifrejet());
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
            line.append(Utility.getParam("BATCH_TYPE"));
            line.append(separateur);
            line.append("001");
            line.append(separateur);
            line.append(Utility.getParam("CPTCRECOMREJCHQRET1")); //Compte Client ou attente
            line.append(separateur);
            line.append("K10");
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
            line.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT")));   //INITIATION_DATE
            line.append(separateur);
            line.append(Utility.getParam("COMCREREJCHQRETFLEX1"));
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Tire()); // 
            writeln(line.toString());

            //Credit du compte des frais
            line = new StringBuilder();
            libelle = "CREDIT COMPTE TOB " + effet.getNumeroeffet() + " Motif Rejet :" + Utility.getParamLabel(effet.getMotifrejet());
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
            line.append(Utility.getParam("BATCH_TYPE"));
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
            line.append(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            line.append(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Tire()); // 
            writeln(line.toString());
        }
        return numLigne;
    }

}
