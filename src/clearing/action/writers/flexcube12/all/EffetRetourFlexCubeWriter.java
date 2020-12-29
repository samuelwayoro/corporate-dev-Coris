/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube12.all;

import clearing.model.CMPUtility;
import clearing.table.Comptes;
import clearing.table.Effets;
import clearing.table.Utilisateurs;
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
public class EffetRetourFlexCubeWriter extends FlatFileWriter {

    public EffetRetourFlexCubeWriter() {
        setDescription("Envoi des effets Retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        boolean isEcobankStandard;
        String compteur;

        String dateValeur = Utility.getParam("DATEVALEUR_RETOUR");
        String[] param1 = (String[]) ((String[]) this.getParametersMap().get("param1"));
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
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
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

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
//          fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
        sql = "SELECT * FROM EFFETS WHERE ETAT IN (" + Utility.getParam("CETAOPERET") + ") ";
        Effets[] effets = (Effets[]) ((Effets[]) db.retrieveRowAsObject(sql, new Effets()));
        boolean j = false;
        long montantTotal = 0L;
        if (effets != null && 0 < effets.length) {
            this.setOut(this.createFlatFile(fileName));
            StringBuffer line = new StringBuffer("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");

            for (int i = 0; i < effets.length; ++i) {
                Effets effet = effets[i];
                line = new StringBuffer();
                line.append(CMPUtility.getNumCptExAgence(effet.getNumerocompte_Tire(), effet.getAgence()));
                line.append(Utility.bourrageGZero(CMPUtility.getNumCptEx(effet.getNumerocompte_Tire(), effet.getAgence(), "1"), 16));
                line.append(createBlancs(4, " "));
                line.append("D");
                line.append(Utility.bourrageGauche(effet.getMontant_Effet(), 16, " "));
                line.append("F03");
                line.append(dateValeur);
                line.append(createBlancs(8, " "));
                line.append(Utility.bourrageDroite(Utility.getParam("LIBEFFRETFLEX1"), 18, " "));
                line.append(createBlancs(7, " "));
                line.append("043");
                line.append(Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 8));

                /**
                 * ligne F12
                 */
                Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(effet.getNumerocompte_Tire(), effet.getAgence());
                DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                detbUploadDetail.setBATCH_NO(numeroBatch);
                detbUploadDetail.setFIN_CYCLE(current_cycle);
                detbUploadDetail.setPERIOD_CODE(current_period);
                detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

                detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + effet.getAgence().substring(3));//agence du compte
                detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + effet.getNumerocompte_Tire());//REM ACCOUNT
                detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                detbUploadDetail.setTXN_CODE("F03");//Code transaction TXN_CODE
                detbUploadDetail.setAMOUNT(new BigDecimal(effet.getMontant_Effet())); //AMOUNT
                detbUploadDetail.setADDL_TEXT(dateValeur + effet.getNom_Tire() + " " + Utility.bourrageDroite(Utility.getParam("LIBEFFRETFLEX1"), 18, " ") + "043" + Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 8));
                detbUploadDetail.setREL_CUST(compteFlexCube != null ? compteFlexCube.getNumcptex().substring(0, 9) : "");
                detbUploadDetail.setEXTERNAL_REF_NO(effet.getIdeffet() + detbUploadDetail.getBATCH_NO());
                detbUploadDetail.setRELATED_ACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "");
                detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                detbUploadDetails.add(detbUploadDetail);
                if (effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }

                montantTotal += Long.parseLong(effet.getMontant_Effet());
            }

            line = new StringBuffer();
            line.append(Utility.getParam("FLEXMAINBRANCH"));
            line.append(Utility.bourrageDroite(Utility.getParam("CPTCREEFFRET"), 16, " "));
            line.append(createBlancs(4, " "));
            line.append("C");
            line.append(Utility.bourrageGauche("" + montantTotal, 16, " "));
            line.append("F03");
            line.append(CMPUtility.getDate());
            line.append(createBlancs(8, " "));
            line.append(Utility.getParam("LIBCHQALEFLEX2"));
            line.append(createBlancs(24, " "));

            /**
             * ligne F12
             */
            DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete

            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTCREEFFRET"));//REM ACCOUNT
            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("F03");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(CMPUtility.getDate() + Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX2"), 25, " "));
            detbUploadDetail.setREL_CUST((Utility.getParam("CPTCREEFFRET").length() == 16) ? Utility.getParam("CPTCREEFFRET").substring(6, 14) : "999999992");
            detbUploadDetail.setEXTERNAL_REF_NO(detbUploadDetail.getBATCH_NO());
            detbUploadDetail.setRELATED_ACCOUNT(Utility.getParam("CPTCREEFFRET"));
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);
            /**
             * ECRITURE DANS LE FICHIER
             */
            detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
            detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
            writeFile(detbUploadDetailArray);

            this.setDescription(this.getDescription() + " exécuté avec succès:\n Nombre d\'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            this.logEvent("INFO", "Nombre d\'effets= " + effets.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));
            this.closeFile();
        } else {
            this.setDescription(this.getDescription() + ": Il n\'y a aucun element disponible");
            this.logEvent("WARNING", "Il n\'y a aucun element disponible");
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
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\",ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\",LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,TXT_FILE_NAME,INSTRUMENT_NO)\n"
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
            line.append(sep(Utility.convertDateToString(detbUploadDetail.getVALUE_DATE(), Utility.getParam("DATE_FORMAT") )));
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
            line.append(sep(Utility.convertDateToString(detbUploadDetail.getINITIATION_DATE(), Utility.getParam("DATE_FORMAT") )));
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
