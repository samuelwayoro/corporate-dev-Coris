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
public class ChequeAllerSurCaisseDebitFlexCubeSQLWriter extends FlatFileWriter {

    public ChequeAllerSurCaisseDebitFlexCubeSQLWriter() {
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
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_FILE_ROOTNAME") + Utility.convertDateToString(new Date(), "ddMMyyyy")+"_"+numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND LOTSIB IS NULL ORDER BY REMISE";

        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        long montantTotal = 0;
        int numLigne = 1;
        setOut(createFlatFile(fileName));

        createEnteteSQL();
        if (cheques != null && 0 < cheques.length) {

            for (int i = 0; i < cheques.length; i++) {
                if (!isValidLine(cheques[i])) {
                } else {
                    Cheques cheque = cheques[i];
                    //Deroule le schema comptable
                    numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + cheque.getIdcheque(), cheque.getMontantcheque(),
                            Utility.convertDateToString(new Date(), "ddMMyyyy"),
                            cheque, "Paiement CHQ N°:" + Utility.bourrageGauche(Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " ") +" du Bordereau N°"+cheque.getRefremise()+ " Faveur " + cheque.getNombeneficiaire()); //Utility.trimLeadingZero(cheque.getNumerocheque()), 7, " "

                    if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPESUPVALSURCAI"))) {
                        cheque.setLotsib(new BigDecimal("1"));

                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }

                    montantTotal += Long.parseLong(cheque.getMontantcheque());
                }

            }

            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Creation du fichier Echec
        //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESUPVALSURCAI") + " AND LOTSIB IS NULL ORDER BY REMISE";
        fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQCAISSE_IN_ERR_FILE_ROOTNAME") + Utility.convertDateToString(new Date(), "ddMMyyyy")+"_"+numeroBatch + Utility.getParam("SIB_FILE_EXTENSION");
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        long montantEchec = 0;
        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            for (Cheques cheque : cheques) {
                StringBuilder line = new StringBuilder();
                line.append(cheque.getAgenceremettant());
                line.append(";");
                line.append(cheque.getCompteremettant());
                line.append(";");
                line.append(cheque.getMontantcheque());
                line.append(";");
                line.append(cheque.getAgence());
                line.append(";");
                line.append(cheque.getNumerocompte());

                writeln(line.toString());
                montantEchec += Long.parseLong(cheque.getMontantcheque());
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPESUPVALSURCAI"))) {
                    cheque.setLotsib(new BigDecimal("1"));

                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }

            }
            closeFile();
            setDescription("<br>Echec des rejets de chèques Aller Sur Caisse - Debit des Clients");
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque Sur Caisse - Debit des Clients en echec = " + cheques.length + " - Montant Total en echec = " + Utility.formatNumber("" + montantEchec) + " - Nom de Fichier Echec =  <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileName.replace(Utility.getParam("SIB_IN_FOLDER")+"\\", "") +"\">"+ fileName.replace(Utility.getParam("SIB_IN_FOLDER")+"\\", "") + "</a>" +  "<br>");
            logEvent("INFO", "Nombre de Chèque Sur Caisse - Debit des Clients en echec = " + cheques.length + " - Montant Total rejete en echec = " + Utility.formatNumber("" + montantEchec));
        }

        db.close();
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
        //Verification des manager cheques
        if (cheque.getNumerocompte().equals(Utility.getParam("CPTFLEXMCACCOUNT"))) {
            return false;
        }

        return true;
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
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,INSTRUMENT_NO)\n"
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
//  + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
//                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,INSTRUMENT_NO)\n"
        /**
         * Format SQL LOADER
         */
        //Ligne 1 (Debit du Donneur d'ordre)
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
        line.append((Utility.getParam("CPTATTCHQCAIFLEX").length() == 16) ? Utility.getParam("CPTATTCHQCAIFLEX").substring(6, 14) : "99999999"); //rel_cust // usually ==> line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust '0017201000019901' 
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQCAIFLEX")); // Related Account
        line.append(separateur);
        line.append(aCheque.getNumerocheque()); // Instrument No
        writeln(line.toString());

        //Ligne de CREDIT du compte interne
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
        line.append(Utility.getParam("CPTATTCHQCAIFLEX")); //Compte Client ou attente
        line.append(separateur);
        line.append("F03");
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
        line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(numCptEx); // Utility.getParam("CPTATTCHQCAIFLEX")
        line.append(separateur);
        line.append(aCheque.getNumerocheque()); // Instrument No
        writeln(line.toString());

        return numLigne;
    }

}
