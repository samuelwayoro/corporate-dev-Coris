/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.esn;

import clearing.model.CMPUtility;
import clearing.table.Banques;
import clearing.table.Virements;
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
public class VirementRetourFlexCubeSQLWriter extends FlatFileWriter {

    public VirementRetourFlexCubeSQLWriter() {
        setDescription("Envoi des virements Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
        dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());
        String sql = "select  CURRENT_CYCLE,CURRENT_PERIOD from " + Utility.getParam("FLEXSCHEMA") + ".STTM_BRANCH where branch_code='001' ";
        STTM_BRANCH[] sttm_branch = (STTM_BRANCH[]) dbExt.retrieveRowAsObject(sql, new STTM_BRANCH());
        String current_cycle = null;
        String current_period = null;
        if (sttm_branch != null && sttm_branch.length > 0) {
            current_cycle = sttm_branch[0].getCurrent_cycle();
            current_period = sttm_branch[0].getCurrent_period();
        }
        dbExt.close();

        String numeroBatch = null;
        //Date Valeur a recuperer par
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        } //
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_FILE_ROOTNAME") + Utility.convertDateToString(new Date(), "ddMMyyyy") +"_"+numeroBatch+ Utility.getParam("SIB_FILE_SQL_EXTENSION");

        sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

        int j = 0;
        long montantTotal = 0; //mnt total recu de la compense
        long montant = 0; //mnt total Vir dans le Fichier SQL LOADER 
        long montantEchec = 0; //mnt total des virements en Echec 
        if (virements != null && 0 < virements.length) {
            setOut(createFlatFile(fileName));
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

            int ligne = 0;

            for (int i = 0; i < virements.length; i++) {
                Virements virement = virements[i];
                String numCptEx = CMPUtility.getNumCptEx(virement.getNumerocompte_Beneficiaire(), virement.getAgence(),"1");
                //Tous les virements retour - ligne de credit montant sur cpt
                //Ligne 1 
                line = new StringBuilder();
                line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
                line.append("~");
                line.append(current_period); //PERIOD_CODE a recuperer par une requete
                line.append("~");
                line.append(Utility.convertDateToString(Utility.convertStringToDate(virement.getDatetraitement(), "yyyy/MM/dd"), "ddMMyyyy")); //VALUE_DATE a recuperer par une requete
                line.append("~");
                line.append(" VRT RECU DE: ");
                sql = "SELECT * FROM BANQUES  WHERE trim(CODEBANQUE)  ='" + virement.getBanqueremettant().trim() + "'  ";
                Banques[] banques = (Banques[]) db.retrieveRowAsObject(sql, new Banques());
                if (banques != null && banques.length > 0) {
                    line.append(banques[0].getLibellebanque().trim());//libelle de la banque Remettante 
                }

                line.append(" : ");
                line.append(virement.getNom_Tire());//Donneur d'ordre
                line.append(virement.getLibelle().trim());
                line.append("~");
                line.append("001"); //Agence ou le chargement est effectue
                line.append("~");
                line.append("ECOSOURCE");
                line.append("~");
                line.append(numCptEx != null ? numCptEx.substring(0, 3) : virement.getAgence().substring(2)); //Agence du compte
                line.append("~");

                System.out.println("numCptEx " + numCptEx);
                if (numCptEx == null) {
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETMAN")));

                } else {
                    ligne++;
                    montant += Long.parseLong(virement.getMontantvirement());
                    line.append(Utility.bourrageDroite(numCptEx, 16, " "));
                }

                line.append("~");
                line.append("F64");
                line.append("~");
                line.append(numeroBatch);
                line.append("~");
                line.append(ligne);
                line.append("~");
                line.append(virement.getMontantvirement().trim());
                line.append("~");
                line.append("C");
                line.append("~");
                line.append("U");
                line.append("~");
                line.append("XOF");
                line.append("~");
                line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
                line.append("~");
                line.append(virement.getMontantvirement().trim());
                line.append("~");
                line.append("1"); //EXCH_RATE
                line.append("~");
                line.append(numCptEx != null ? numCptEx.substring(6, 14) : ""); //rel_cust
                line.append("~");
                line.append(numeroBatch + virement.getIdvirement()); //external_ref_no
                line.append("~");
                line.append(numCptEx != null ? numCptEx : ""); //
                if (numCptEx != null && !numCptEx.isEmpty()) {
                    writeln(line.toString());
                }

                if (virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                } else if (virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPERETMAN"))) {

                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                }

                montantTotal += Long.parseLong(virement.getMontantvirement());
            }
// Gestion cpt globalisation
            //   + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
            //     + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account)\n"
            line = new StringBuilder();
            line.append(current_cycle);//recupere l'annee FiscaleFIN_CYCLE,  
            line.append("~");
            line.append(current_period); //PERIOD_CODE a recuperer par une requete
            line.append("~");
            line.append(Utility.convertDateToString(Utility.convertStringToDate(virements[0].getDatetraitement(), "yyyy/MM/dd"), "ddMMyyyy")); //VALUE_DATE a recuperer par une requete
            line.append("~");
            line.append("VIREMENT ");
            line.append("~");
            line.append("001"); //Agence ou le chargement est effectue
            line.append("~");
            line.append("ECOSOURCE");
            line.append("~");
            line.append("001");
            line.append("~");
            line.append(Utility.getParam("CPTDEBVIRRET"));
            line.append("~");
            line.append("F57");
            line.append("~");
            line.append(numeroBatch);
            line.append("~");
            line.append(ligne + 1);
            line.append("~");
            line.append(montant);
            line.append("~");
            line.append("D");
            line.append("~");
            line.append("U");
            line.append("~");
            line.append("XOF");
            line.append("~");
            line.append(Utility.convertDateToString(new Date(), "ddMMyyyy"));   //INITIATION_DATE
            line.append("~");
            line.append(montant);
            line.append("~");
            line.append("1"); //EXCH_RATE
            line.append("~");
            line.append("99999999"); //rel_cust
            line.append("~");
            line.append(numeroBatch); //external_ref_no
            line.append("~");
            line.append(Utility.getParam("CPTDEBVIRRET")); //related account

            writeln(line.toString());
            logEvent("INFO", "Montant Total Recu de la Compense= " + Utility.formatNumber("" + montantTotal) + ""
                    + "Nombre de Virements dans le fichier Batch= " + ligne + " "
                    + " Nom de Fichier Batch = " + fileName
            );

            closeFile();

            String fileNameEchec = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_ECH_FILE_ROOTNAME") + Utility.convertDateToString(new Date(), "ddMMyyyy")+"_"+numeroBatch + Utility.getParam("SIB_FILE_EXTENSION");
            sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPERETMAN") + ") AND DATETRAITEMENT='" + virements[0].getDatetraitement() + "' ";

            Virements[] virementsEchec = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
            if (virementsEchec != null && virementsEchec.length > 0) {
                setOut(createFlatFile(fileNameEchec));
                for (Virements virement : virementsEchec) {
                    line = new StringBuilder();
                    line.append(virement.getType_Virement());
                    line.append(";");
                    line.append(virement.getBanqueremettant());
                    line.append(";");
                    line.append(virement.getAgenceremettant());
                    line.append(";");
                    line.append(virement.getBanque());
                    line.append(";");
                    line.append(virement.getAgence());
                    line.append(";");
                    line.append(virement.getNumerocompte_Beneficiaire());
                    line.append(";");
                    line.append(virement.getMontantvirement());
                    line.append(";");
                    line.append(virement.getNom_Beneficiaire());
                    line.append(";");
                    line.append(virement.getLibelle());
//virementsEchec virementsEchec.length
                    writeln(line.toString());
                    montantEchec += Long.parseLong(virement.getMontantvirement());
                }
                closeFile();
            }

            db.executeUpdate("UPDATE VIREMENTS SET LOTSIB=1, ETAT=" + Utility.getParam("CETAOPERETENVSIB") + " WHERE DATETRAITEMENT='" + virements[0].getDatetraitement() + "'  AND ETAT=" + Utility.getParam("CETAOPERETMAN"));
            String nbVirEchec = (virementsEchec != null) ? "" + virementsEchec.length : "0";
            String mntEchec = (virementsEchec != null) ? "" + montantEchec : "0";
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Virements Recu de la Compense = " + virements.length
                    + " <br>- Montant Total Recu de la Compense= " + Utility.formatNumber("" + montantTotal)
                    + " <br>- Nombre de Virements dans le fichier Batch= " + ligne
                    + " <br>- Montant Total des Virements dans le fichier Batch = " + Utility.formatNumber("" + montant)
                    + " <br>- Nom de Fichier Batch = " + fileName
                    + " <br>- Nom de Fichier Batch Echec Virements = <a onclick=\"NoPrompt()\" target=\"dynamic\" href=\"sortie/" + fileNameEchec.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "\">" + fileNameEchec.replace(Utility.getParam("SIB_IN_FOLDER") + "\\", "") + "</a>"
                    + " <br>- Montant Total des Virements en Echec = " + Utility.formatNumber("" + mntEchec)
                    + " <br>- Nombre Total de Virements en Echec= " + nbVirEchec
            );

            logEvent("INFO", "Montant Total des Virements en Echec= " + Utility.formatNumber("" + mntEchec) + ""
                    + " Nombre de Virements den Echec= " + nbVirEchec + " "
                    + " Nom de Fichier Batch Echec Virements = " + fileNameEchec
            );
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
