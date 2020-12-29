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
import org.patware.bean.table.Fichiers;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.ExtJDBCXmlReader;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class EffetRetourFlexCubeSQLWriter extends FlatFileWriter {

    public EffetRetourFlexCubeSQLWriter() {
        setDescription("Envoi des effets Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
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

        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);

////        }
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME") + Utility.convertDateToString(new Date(), "ddMMyyyy") + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");
        sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ") ";
        Effets[] effets = (Effets[]) ((Effets[]) db.retrieveRowAsObject(sql, new Effets()));
        boolean j = false;
        long montantTotalSucces = 0L;

        long montantTotalEchec = 0;
        int numLigne = 1;
        String libelleComptable;
        if (effets != null && 0 < effets.length) {
            this.setOut(this.createFlatFile(fileName));

            String userLogin = ((Utilisateurs) getParametersMap().get("user")).getLogin().trim();
            Fichiers fichier = new Fichiers();
            fichier.setUserUpload(userLogin);
            fichier.setNomFichier(new File(fileName).getName());
            fichier.setDateReception(Utility.convertDateToString(new Date(), "yyyy/MM/dd"));
            fichier.setEtat(new BigDecimal(30));
            fichier.setIdFichier(new BigDecimal(Utility.computeCompteur("IDFICHIERS", "FICHIERS")));
            db.insertObjectAsRowByQuery(fichier, "FICHIERS");

            createEnteteSQL();
            for (int i = 0; i < effets.length; ++i) {
                Effets effet = effets[i];
                if (!isValidLine(effet)) {

                    montantTotalEchec += Long.parseLong(effet.getMontant_Effet());
                    if (effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                        effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETMAN")));
                        db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                    }

                } else {
                    libelleComptable = Utility.bourrageDroite("EFF " + Utility.bourrageDroite(effet.getNom_Beneficiaire(), 17, " ") + " " + effet.getDate_Echeance(), 34, " ");
                    numLigne = createLinesSQL(current_cycle, current_period, numeroBatch, numLigne, "~", numeroBatch + "" + effet.getIdeffet(),
                            "" + effet.getMontant_Effet(), Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT")), "D",
                            effet, libelleComptable, "F03", new File(fileName).getName());
                    montantTotalSucces += Long.parseLong(effet.getMontant_Effet());
                }

                if (effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }

            }
//            createEndSQL(current_cycle, current_period, numeroBatch, numLigne, "~", "" + numeroBatch,
//                    "" + montantTotalSucces, Utility.convertDateToString(new Date(), "ddMMyyyy"), "C",
//                    null, Utility.getParam("LIBEFFRETFLEX2"), "F03");

            this.setDescription(this.getDescription() + " exécuté avec succès:<br> Nombre d\'effets dans le fichier= " + effets.length + " - Montant Total = " + Utility.formatNumber("" + montantTotalSucces) + " - Nom de Fichier = " + fileName);
            this.logEvent("INFO", "Nombre d\'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalSucces));
            this.closeFile();

        } else {
            this.setDescription(this.getDescription() + ": Il n\'y a aucun element disponible");
            this.logEvent("WARNING", "Il n\'y a aucun element disponible");
        }
        fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_ERR_FILE_ROOTNAME") + Utility.convertDateToString(new Date(), "ddMMyyyy") + Utility.getParam("SIB_FILE_EXTENSION");
        sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPERETMAN") + ") ";
        effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

        if (effets != null && 0 < effets.length) {
            setOut(createFlatFile(fileName));
            for (int i = 0; i < effets.length; i++) {
                Effets effet = effets[i];
                StringBuilder line = new StringBuilder();
                line.append(effet.getBanqueremettant());
                line.append(";");
                line.append(effet.getAgenceremettant());
                line.append(";");
                line.append(effet.getNumerocompte_Beneficiaire());
                line.append(";");
                line.append(effet.getMontant_Effet());
                line.append(";");
                line.append(effet.getNom_Beneficiaire());
                line.append(";");
                line.append(effet.getNom_Tire());
                writeln(line.toString());
                montantTotalEchec += Long.parseLong(effet.getMontant_Effet());
            }
            closeFile();
            db.executeUpdate("UPDATE EFFETS SET LOTSIB=1, ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " WHERE ETAT=" + Utility.getParam("CETAOPERETMAN"));

            this.setDescription(this.getDescription() + " exécuté avec succès:<br> Nombre d\'effets en Echec dans le fichier= " + effets.length + " - Montant Total = " + Utility.formatNumber("" + montantTotalEchec) + " - Nom de Fichier Echec =  <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "\">" + fileName.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "</a>");
            this.logEvent("INFO", "Nombre d\'effets en Echec dans le fichier= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalEchec) + " - Nom de Fichier = " + fileName);

        }

        db.close();
    }

    private void createEnteteSQL() {
        StringBuilder line = new StringBuilder();

        //entete
        line.append("LOAD DATA\n"
                + "INFILE *\n"
                + "APPEND\n"
                + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + " . DETB_UPLOAD_DETAIL\n"
                + "Fields terminated by '~'\n"
                + "Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\" ,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,TXT_FILE_NAME)\n"
                + "BEGINDATA");
        writeln(line.toString());
    }

    /**
     *
     * @param current_cycle
     * @param current_period
     * @param numeroBatch
     * @param numLigne
     * @param separateur
     * @param referenceRelative
     * @param montantLigne
     * @param dateValeur
     * @param sens
     * @param effet
     * @param libelle
     * @throws Exception
     */
    private int createLinesSQL(String current_cycle, String current_period, String numeroBatch, int numLigne, String separateur, String referenceRelative,
            String montantLigne, String dateValeur, String sens, Effets effet, String libelle, String codeOp, String fileName) throws Exception {
        StringBuilder line;

        if (effet != null) {
            Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(effet.getNumerocompte_Tire(), effet.getAgence());
//            String numCptEx = CMPUtility.getNumCptEx(effet.getNumerocompte_Tire(), effet.getAgence());

            /**
             * Format SQL LOADER
             */
            // 
            //Ligne de debit du client
            line = new StringBuilder();
            line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
            line.append(separateur);
            line.append(current_period); //PERIOD_CODE a recuperer par une requete
            line.append(separateur);
            line.append(dateValeur); //VALUE_DATE a recuperer par une requete
            line.append(separateur);
            line.append(libelle);
            line.append(separateur);
            line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
            line.append(separateur);
            line.append(Utility.getParam("BATCH_TYPE"));
            line.append(separateur);
            line.append(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : "" + Utility.getParam("TXN_BRANCH").charAt(0) +  effet.getAgence().substring(3)); //Agence du compte

            line.append(separateur);
            if (compteFlexCube == null) {
                effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETMAN")));
            } else {

                line.append(compteFlexCube.getNumcptex());
            }

            line.append(separateur);
            line.append(codeOp);
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(montantLigne);
            line.append(separateur);
            line.append(sens);
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT")));   //INITIATION_DATE
            line.append(separateur);
            line.append(montantLigne);
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : ""); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);

            if (compteFlexCube != null && !compteFlexCube.getNumcptex().isEmpty()) {
                line.append(compteFlexCube.getNumcptex()); //
                line.append(separateur);
                line.append(fileName);
                line.append(separateur);
                writeln(line.toString());
            }

            //Ligne de credit du compte interne
            line = new StringBuilder();
            line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
            line.append(separateur);
            line.append(current_period); //PERIOD_CODE a recuperer par une requete
            line.append(separateur);
            line.append(dateValeur); //VALUE_DATE a recuperer par une requete
            line.append(separateur);
            line.append(Utility.getParam("LIBEFFRETFLEX2"));
            line.append(separateur);
            line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
            line.append(separateur);
            line.append(Utility.getParam("BATCH_TYPE"));
            line.append(separateur);
            line.append(Utility.getParam("TXN_BRANCH")); //Agence du compte
            line.append(separateur);
            line.append(Utility.getParam("CPTCREEFFRET"));
            line.append(separateur);
            line.append("F03");
            line.append(separateur);
            line.append(numeroBatch);
            line.append(separateur);
            line.append(numLigne++);
            line.append(separateur);
            line.append(montantLigne);
            line.append(separateur);
            line.append("C");
            line.append(separateur);
            line.append("U");
            line.append(separateur);
            line.append("XOF");
            line.append(separateur);
            line.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT")));   //INITIATION_DATE
            line.append(separateur);
            line.append(montantLigne);
            line.append(separateur);
            line.append("1"); //EXCH_RATE
            line.append(separateur);
            line.append("999999992"); //rel_cust
            line.append(separateur);
            line.append(referenceRelative); //external_ref_no
            line.append(separateur);
            if (compteFlexCube != null && !compteFlexCube.getNumcptex().isEmpty()) {
                line.append(Utility.getParam("CPTCREEFFRET")); //
                line.append(separateur);
                line.append(fileName);
                line.append(separateur);
                writeln(line.toString());
            }
        }
        return numLigne;

    }

    private void createEndSQL(String current_cycle, String current_period, String numeroBatch, int numLigne, String separateur, String referenceRelative,
            String montantLigne, String dateValeur, String sens, Effets effet, String libelle, String codeOp) throws Exception {
        StringBuilder line;
        //   + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
        //     + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account)\n"
        /**
         * Format SQL LOADER
         */
        // 
        //Ligne de debit du client
        line = new StringBuilder();
        line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
        line.append(separateur);
        line.append(current_period); //PERIOD_CODE a recuperer par une requete
        line.append(separateur);
        line.append(dateValeur); //VALUE_DATE a recuperer par une requete
        line.append(separateur);
        line.append(libelle);
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence ou le chargement est effectue
        line.append(separateur);
        line.append(Utility.getParam("BATCH_TYPE"));
        line.append(separateur);
        line.append(Utility.getParam("TXN_BRANCH")); //Agence du compte
        line.append(separateur);
        line.append(Utility.getParam("CPTCREEFFRET"));
        line.append(separateur);
        line.append(codeOp);
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montantLigne);
        line.append(separateur);
        line.append(sens);
        line.append(separateur);
        line.append("U");
        line.append(separateur);
        line.append("XOF");
        line.append(separateur);
        line.append(Utility.convertDateToString(new Date(), Utility.getParam("DATE_FORMAT")));   //INITIATION_DATE
        line.append(separateur);
        line.append(montantLigne);
        line.append(separateur);
        line.append("1"); //EXCH_RATE
        line.append(separateur);
        line.append("999999992"); //rel_cust
        line.append(separateur);
        line.append(referenceRelative); //external_ref_no
        line.append(separateur);
        line.append(Utility.getParam("CPTCREEFFRET")); //
        writeln(line.toString());

    }

    private boolean isValidLine(Effets effet) throws Exception {
        //Verification de l'existence du compte
        String numCptEx = CMPUtility.getNumCptEx(effet.getNumerocompte_Tire(), effet.getAgence(), "1");
        if (numCptEx == null) {
            return false;
        }

        //Verification des manager cheques
        if (effet.getNumerocompte_Tire().equals(Utility.getParam("CPTFLEXMCACCOUNT"))) {
            return false;
        }

        //Verification des comptes staff
        if ("051|085".contains(CMPUtility.getAcctClass(numCptEx))) {
            return false;
        }
        //Verification des Effets Avalisé
        if (effet.getCode_Aval().trim().equals("1")) {

            return false;
        }

        return true;
    }

}
