/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.nsia;

import clearing.model.CMPUtility;
import clearing.table.Comptes;
import clearing.table.Utilisateurs;
import clearing.table.Virements;
import clearing.table.flexcube.DETB_UPLOAD_DETAIL;
import clearing.table.flexcube.STTM_BRANCH;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
public class VirementRetourFlexCubeNSIAWriter extends FlatFileWriter {

    public VirementRetourFlexCubeNSIAWriter() {
        setDescription("Envoi des virements Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        StringBuilder line = new StringBuilder();
        long montantTotal = 0; //mnt total recu de la compense
        long montant = 0; //mnt total Vir dans le Fichier SQL LOADER 
        long montantEchec = 0; //mnt total des virements en Echec 

        String dateValeur = Utility.getParam("DATEVALEUR_RETOUR");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        dateValeur = Utility.convertDateToString(new Date(), "yyyyMMdd");
        /**
         * F12
         */
        DETB_UPLOAD_DETAIL[] detbUploadDetailArray;

        List<DETB_UPLOAD_DETAIL> detbUploadDetails = new ArrayList<>();
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
        param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }

        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

        String userLogin = ((Utilisateurs) getParametersMap().get("user")).getLogin().trim();
        Fichiers fichier = new Fichiers();
        fichier.setUserUpload(userLogin);
        fichier.setNomFichier(new File(fileName).getName());
        fichier.setDateReception(Utility.convertDateToString(new Date(), "yyyy/MM/dd"));
        fichier.setEtat(new BigDecimal(30));
        fichier.setIdFichier(new BigDecimal(Utility.computeCompteur("IDFICHIERS", "FICHIERS")));
        db.insertObjectAsRowByQuery(fichier, "FICHIERS");
        /**
         * FIN PARAM F12
         */
//        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");

        int ligne = 0;
        sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        int j = 0;

        if (virements != null && 0 < virements.length) {
            setOut(createFlatFile(fileName));

            for (int i = 0; i < virements.length; i++) {
                Virements virement = virements[i];
                //Tous les virements retour - ligne de credit montant sur cpt

                /**
                 * ligne F12
                 */
                Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(virement.getNumerocompte_Beneficiaire(), virement.getAgence());
                if (compteFlexCube == null || compteFlexCube.getNumcptex().isEmpty()) {
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETMAN")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());

                } else {
                    ligne++;
                    DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

                    detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + virement.getAgence().substring(3));//agence du compte
                    detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + virement.getNumerocompte_Beneficiaire());//REM ACCOUNT
                    detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(virement.getMontantvirement())); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBVIRRETFLEX1").trim() + " " +  " D/O " + virement.getNom_Tire().trim() + " POUR " + virement.getLibelle().trim(), 150, " "));
                    detbUploadDetail.setREL_CUST("");
                    detbUploadDetail.setEXTERNAL_REF_NO("");
                    detbUploadDetail.setRELATED_ACCOUNT("");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetails.add(detbUploadDetail);

                    if (virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                        db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                    }

                    montantTotal += Long.parseLong(virement.getMontantvirement());
                }

            }
// Gestion cpt globalisation

            /**
             * ligne F12
             */
            DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTDEBVIRRET"));//REM ACCOUNT
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setADDL_TEXT( Utility.bourrageDroite(Utility.getParam("LIBVIRRETFLEX2") +"  "+ virements[0].getDatecompensation(), 100, " "));
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);
            /**
             * ECRITURE DANS LE FICHIER
             */
            detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
            detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
            writeFile(detbUploadDetailArray);
            closeFile();
            String fileNameEchec = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_ECH_FILE_ROOTNAME") + Utility.convertDateToString(new Date(), "ddMMyyyy") + "_" + numeroBatch + Utility.getParam("SIB_FILE_EXTENSION");
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

    private void createEnteteSQL() {
        StringBuilder line = new StringBuilder();

        //entete
        line.append("LOAD DATA\n"
                + "INFILE *\n"
                + "APPEND\n"
                + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + ". DETB_UPLOAD_DETAIL\n"
                + " Fields terminated by '~'\n"
                + " Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE  DATE \"" + Utility.getParam("DATE_FORMAT") + "\" ,ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE  DATE \"" + Utility.getParam("DATE_FORMAT") + "\" ,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,TXT_FILE_NAME,INSTRUMENT_NO)\n"
                + "BEGINDATA");
        writeln(line.toString());
    }

    public String getSep() {
        return sep;
    }

    public void setSep(String sep) {
        this.sep = sep;
    }

    private String sep(String value) {
        return value + sep;
    }

    private int writeFile(DETB_UPLOAD_DETAIL[] detbUploadDetailList) {
        System.out.println("writeFile DETB_UPLOAD_DETAIL File");
        int numLigne = 1;
        createEnteteSQL();

        for (DETB_UPLOAD_DETAIL detbUploadDetail : detbUploadDetailList) {
            StringBuilder line = new StringBuilder();
            line.append(sep(detbUploadDetail.getFIN_CYCLE()));
            line.append(sep(detbUploadDetail.getPERIOD_CODE()));
            line.append(sep(Utility.convertDateToString(detbUploadDetail.getVALUE_DATE(), Utility.getParam("DATE_FORMAT"))));
            line.append(sep(detbUploadDetail.getADDL_TEXT()));
            line.append(sep(detbUploadDetail.getBRANCH_CODE()));
            line.append(sep(detbUploadDetail.getSOURCE_CODE()));
            line.append(sep(detbUploadDetail.getACCOUNT_BRANCH()));
            line.append(sep(detbUploadDetail.getACCOUNT()));
            line.append(sep(detbUploadDetail.getTXN_CODE()));
            line.append(sep(detbUploadDetail.getBATCH_NO()));
            line.append(sep(String.valueOf(numLigne++)));
            line.append(sep(detbUploadDetail.getAMOUNT().toString()));
            line.append(sep(detbUploadDetail.getDR_CR()));
            line.append(sep(detbUploadDetail.getUPLOAD_STAT()));
            line.append(sep(detbUploadDetail.getCCY_CD()));
            line.append(sep(Utility.convertDateToString(detbUploadDetail.getINITIATION_DATE(), Utility.getParam("DATE_FORMAT"))));
            line.append(sep(detbUploadDetail.getAMOUNT().toString()));
            line.append(sep(detbUploadDetail.getEXCH_RATE().toString()));
            line.append(sep(detbUploadDetail.getREL_CUST()));
            line.append(sep(detbUploadDetail.getEXTERNAL_REF_NO()));
            line.append(sep(detbUploadDetail.getRELATED_ACCOUNT()));
            line.append(sep(detbUploadDetail.getTXT_FILE_NAME()));
            line.append(sep(detbUploadDetail.getINSTRUMENT_NO()));
            writeln(line.toString());

        }

        return numLigne;
    }
    private String sep = "~";

}
