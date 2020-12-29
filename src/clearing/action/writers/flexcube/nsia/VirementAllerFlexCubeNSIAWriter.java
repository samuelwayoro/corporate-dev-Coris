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
import java.util.HashMap;
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
public class VirementAllerFlexCubeNSIAWriter extends FlatFileWriter {

    public VirementAllerFlexCubeNSIAWriter() {
        setDescription("Envoi des virements Aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTVIRALL", "VIRALL"), 4, "0");
            isEcobankStandard = false;
        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "v" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTVIRALL", "VIRALL"), 3, "0");
            isEcobankStandard = false;
        } else {

            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTVIRALL", "VIRALL"), 4, "0");
            isEcobankStandard = true;
        }

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateCompensation = param1[0];
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
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_IN_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

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

        System.out.println("Date Compensation = " + dateCompensation);

         db.executeUpdate("UPDATE VIREMENTS SET ETAT="+Utility.getParam("CETAOPEALLICOM1")+" WHERE TYPE_VIREMENT='011' AND ETAT="+Utility.getParam("CETAOPESTO"));
         
        sql = "SELECT * FROM VIREMENTS WHERE TYPE_VIREMENT='015' AND ETAT IN (" + Utility.getParam("CETAOPESTO") + ") ORDER BY REMISE, NUMEROCOMPTE_TIRE ";

        Virements[] virementsTous = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        int j = 0;
        long montantTotalDesVirements = 0;
        if (virementsTous != null && 0 < virementsTous.length) {
            setOut(createFlatFile(fileName));

            for (int i = 0; i < virementsTous.length; i += j) {
                //distinct numero compte tiré

                //Tous les virements validés d'une remise
                sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPESTO") + ") AND REMISE= " + virementsTous[i].getRemise() + " AND NUMEROCOMPTE_TIRE='" + virementsTous[i].getNumerocompte_Tire() + "' ";
                Virements[] virementsSameCompteTireFichier = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                if (virementsSameCompteTireFichier != null && virementsSameCompteTireFichier.length > 0) {
                    j = virementsSameCompteTireFichier.length;
                    System.out.println("taille ::" + virementsSameCompteTireFichier.length);
                    Virements virement = virementsTous[i];

                    long montantTotalSameCompteTireFichier = 0;

                    HashMap<String, String> hashBanque = new HashMap();
                    //Montant TOTAL des virements par remise et compte tire
                    for (Virements virementSameCompteTire : virementsSameCompteTireFichier) {
                        montantTotalSameCompteTireFichier += Long.parseLong(virementSameCompteTire.getMontantvirement());
                        if (!virementSameCompteTire.getBanque().equals(CMPUtility.getCodeBanqueSica3())) {
                            hashBanque.put(virementSameCompteTire.getBanque(), virementSameCompteTire.getBanque());

                        }
                        if (virementSameCompteTire.getEtat().toPlainString().equals(Utility.getParam("CETAOPESTO"))) {
                            virementSameCompteTire.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                            db.updateRowByObjectByQuery(virementSameCompteTire, "VIREMENTS", "IDVIREMENT=" + virementSameCompteTire.getIdvirement());
                        }

                    }
                    montantTotalDesVirements += montantTotalSameCompteTireFichier;

                    int nbrBanque = hashBanque.keySet().size();

                    //DEBIT DONNEUR D'ORDRE
                    /**
                     * ligne F12
                     */
                    Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(virement.getNumerocompte_Tire(), virement.getAgenceremettant());

                    DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                    detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + virement.getAgence().substring(3));//agence du compte
                    detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + virement.getNumerocompte_Tire());//REM ACCOUNT
                    detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalSameCompteTireFichier)); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBVIRALLFLEX1").trim() + " " + virement.getLibelle(), 194, " "));
                    detbUploadDetail.setREL_CUST("");
                    detbUploadDetail.setEXTERNAL_REF_NO("");
                    detbUploadDetail.setRELATED_ACCOUNT("");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetails.add(detbUploadDetail);

                    if (Utility.getParam("FRAIS_VIB") != null && Utility.getParam("FRAIS_VIB").equalsIgnoreCase("TRUE")) {
                        for (String banque : hashBanque.keySet()) {
                            detbUploadDetail = new DETB_UPLOAD_DETAIL();
                            detbUploadDetail.setBATCH_NO(numeroBatch);
                            detbUploadDetail.setFIN_CYCLE(current_cycle);
                            detbUploadDetail.setPERIOD_CODE(current_period);
                            detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                            detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + virement.getAgence().substring(3));//agence du compte
                            detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + virement.getNumerocompte_Tire());//REM ACCOUNT
                            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                            detbUploadDetail.setAMOUNT(new BigDecimal(Utility.getParam("COMDEBVIRALEFLEX"))); //AMOUNT
                            detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBVIRALLFLEX2").trim() + " " + banque + " " + virement.getLibelle(), 194, " "));
                            detbUploadDetail.setREL_CUST("");
                            detbUploadDetail.setEXTERNAL_REF_NO("");
                            detbUploadDetail.setRELATED_ACCOUNT("");
                            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                            detbUploadDetails.add(detbUploadDetail);
                            
                            detbUploadDetail = new DETB_UPLOAD_DETAIL();
                            detbUploadDetail.setBATCH_NO(numeroBatch);
                            detbUploadDetail.setFIN_CYCLE(current_cycle);
                            detbUploadDetail.setPERIOD_CODE(current_period);
                            detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                            detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + virement.getAgence().substring(3));//agence du compte
                            detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + virement.getNumerocompte_Tire());//REM ACCOUNT
                            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
                            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                            detbUploadDetail.setAMOUNT(new BigDecimal(Utility.getParam("COMDEBVIRALEFLEX1"))); //AMOUNT
                            detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBVIRALLFLEX3").trim() + " " + banque + " " + virement.getLibelle(), 194, " "));
                            detbUploadDetail.setREL_CUST("");
                            detbUploadDetail.setEXTERNAL_REF_NO("");
                            detbUploadDetail.setRELATED_ACCOUNT("");
                            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                            detbUploadDetails.add(detbUploadDetail);

                        }
                    }

                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                    detbUploadDetail.setBATCH_NO(numeroBatch);
                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                    detbUploadDetail.setPERIOD_CODE(current_period);
                    detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                    detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
                    detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTVIRALEFLEX"));//REM ACCOUNT
                    detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                    detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                    detbUploadDetail.setAMOUNT(new BigDecimal(montantTotalSameCompteTireFichier)); //AMOUNT
                    detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(virement.getLibelle(), 100, " "));
                    detbUploadDetail.setREL_CUST("");
                    detbUploadDetail.setEXTERNAL_REF_NO("");
                    detbUploadDetail.setRELATED_ACCOUNT("");
                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                    detbUploadDetails.add(detbUploadDetail);
                    if (Utility.getParam("FRAIS_VIB") != null && Utility.getParam("FRAIS_VIB").equalsIgnoreCase("TRUE")) {
                        if (nbrBanque != 0) {
                            detbUploadDetail = new DETB_UPLOAD_DETAIL();
                            detbUploadDetail.setBATCH_NO(numeroBatch);
                            detbUploadDetail.setFIN_CYCLE(current_cycle);
                            detbUploadDetail.setPERIOD_CODE(current_period);
                            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
                            detbUploadDetail.setACCOUNT(Utility.getParam("CPTCRECOMVIRALE1"));//REM ACCOUNT
                            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                            detbUploadDetail.setAMOUNT(new BigDecimal((Long.parseLong(Utility.getParam("COMCREVIRALEFLEX1")) * nbrBanque))); //AMOUNT
                            detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBVIRALLFLEX4").trim(), 100, " "));
                            detbUploadDetail.setREL_CUST("");
                            detbUploadDetail.setEXTERNAL_REF_NO("");
                            detbUploadDetail.setRELATED_ACCOUNT("");
                            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                            detbUploadDetails.add(detbUploadDetail);

                            detbUploadDetail = new DETB_UPLOAD_DETAIL();
                            detbUploadDetail.setBATCH_NO(numeroBatch);
                            detbUploadDetail.setFIN_CYCLE(current_cycle);
                            detbUploadDetail.setPERIOD_CODE(current_period);
                            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
                            detbUploadDetail.setACCOUNT(Utility.getParam("CPTCRECOMVIRALE2"));//REM ACCOUNT
                            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                            detbUploadDetail.setAMOUNT(new BigDecimal((Long.parseLong(Utility.getParam("COMCREVIRALEFLEX2")) * nbrBanque))); //AMOUNT
                            detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBVIRALLFLEX5").trim(), 100, " "));
                            detbUploadDetail.setREL_CUST("");
                            detbUploadDetail.setEXTERNAL_REF_NO("");
                            detbUploadDetail.setRELATED_ACCOUNT("");
                            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                            detbUploadDetails.add(detbUploadDetail);
                        }
                    }

                }

            }
            /**
             * ECRITURE DANS LE FICHIER
             */
            detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
            detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
            writeFile(detbUploadDetailArray);
           
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Virements= " + virementsTous.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalDesVirements) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Virement= " + virementsTous.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalDesVirements));
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
                + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + ". DETB_UPLOAD_DETAIL\n"
                + " Fields terminated by '~'\n"
                + " Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\",ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\" ,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,TXT_FILE_NAME,INSTRUMENT_NO)\n"
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
