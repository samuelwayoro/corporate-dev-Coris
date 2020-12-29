/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube12;

import clearing.model.CMPUtility;
import clearing.table.Agences;
import clearing.table.Banques;
import clearing.table.Virements;
import clearing.table.flexcube.VW_VIRBCEAO;
import clearing.utils.StaticValues;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.math.NumberUtils;
import org.patware.action.file.FlatFileWriter;
import org.patware.bean.table.Params;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class VirementAllerFlexCube12Writer extends FlatFileWriter {

    public VirementAllerFlexCube12Writer() {
        setDescription("Recuperation des Virements Aller de FlexCube ");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        long montantTotal = 0l;
        Long montantTotalGood = 0l;
          Long montantTotalNC= 0l;
        Long montantTotalBad = 0l;
        //select * from ebjuser.VW_VIRBCEAO where datetransaction='12-december-2018' and filiale='EBJ'  7
        String virIntervalDate = Utility.getParam("VIR_INTERVAL_DATE");
        if (virIntervalDate == null || virIntervalDate.isEmpty()) {
            virIntervalDate = "7";
        }
        System.out.println("JDBCXmlReader.getUser() " + JDBCXmlReader.getUser());
        List<Virements> goodVirs = new ArrayList<>();
        List<Virements> nonComp = new ArrayList<>();
        List<VW_VIRBCEAO> listVirements = new ArrayList<>();
        List<Virements> badVirs = new ArrayList<>();
        Params paramVir = new Params();
        paramVir.setNom("RECUP_VIR");
        paramVir.setValeur("0");
        paramVir.setType("CODE_PARAMS");
        paramVir.setLibelle("RECUPERATION VIREMENTS F12");

        if (Utility.getParam("RECUP_VIR") == null || Utility.getParam("RECUP_VIR").equals("0")) {
            if (Utility.getParam("RECUP_VIR") == null) {

                db.insertObjectAsRowByQuery(paramVir, "Params");
                System.out.println("Parametre initialisé");
                Utility.clearParamsCache();
            }

            String sql = " SELECT *  FROM " + JDBCXmlReader.getUser() + ". VW_VIRBCEAO  WHERE ( DATETRANSACTION  > = sysdate - " + virIntervalDate + "  ) and UPPER(filiale)= '" + Utility.getParam("FILIALE").toUpperCase() + "'"
                    + "     AND REFTRANSACTION IS NOT NULL AND REFTRANSACTION NOT IN "
                    + " ( SELECT DISTINCT(REFERENCE_OPERATION_INTERNE) FROM  VIREMENTS where REFERENCE_OPERATION_INTERNE is not null )  ";
            VW_VIRBCEAO[] virsBCEAO = (VW_VIRBCEAO[]) db.retrieveRowAsObject(sql, new VW_VIRBCEAO());

            if (virsBCEAO != null && virsBCEAO.length > 0) {
                System.out.println("virsBCEAO.length  " + virsBCEAO.length);
                if (Utility.getParam("RECUP_VIR") != null) {
                    db.executeUpdate("UPDATE PARAMS SET VALEUR='1' WHERE NOM='RECUP_VIR'");
                    Utility.clearParamsCache();
                }

                for (VW_VIRBCEAO vw_virbceao : virsBCEAO) {
                    if (vw_virbceao != null && vw_virbceao.getREFTRANSACTION() != null && !vw_virbceao.getREFTRANSACTION().isEmpty()) {

                        Virements virement = new Virements();
                        virement.setDateordre(Utility.convertDateToString(vw_virbceao.getDATETRANSACTION() != null ? vw_virbceao.getDATETRANSACTION() : new Date(System.currentTimeMillis()), Utility.getParam("DATE_FORMAT_VIR_ALLER")));
                        virement.setValideur(vw_virbceao.getVALIDERPAR() != null ? vw_virbceao.getVALIDERPAR() : "");
                        virement.setCodeUtilisateur(vw_virbceao.getSAISIPAR() != null ? vw_virbceao.getSAISIPAR() : "");
                        virement.setType_Virement(vw_virbceao.getTYPEVIREMENT());

                        if (vw_virbceao.getMONTANTTRANSACTION() != null) {
                            System.out.println("test pour voir la nullite du montant");
                            if (NumberUtils.isNumber(vw_virbceao.getMONTANTTRANSACTION().toString().replaceAll("\\p{javaWhitespace}+", "").trim())) {

                                virement.setMontantvirement(String.valueOf(Long.parseLong(vw_virbceao.getMONTANTTRANSACTION().toString().replaceAll("\\p{javaWhitespace}+", "").trim())));

                            }
                        } else {
                            virement.setMontantvirement("000000000");
                        }

                        if (vw_virbceao.getREFTRANSACTION() != null) {
                            virement.setReference_Operation_Interne(vw_virbceao.getREFTRANSACTION());
                        }

                        virement.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEALLICOM1"))));
                        virement.setDevise(CMPUtility.getDevise());
                        virement.setLibelle(Utility.removeAccent(vw_virbceao.getNARRATIVE() != null ? vw_virbceao.getNARRATIVE() : "VIR FAVEUR "));
                        if (Utility.getParam("VERSION_SICA").equals("2")) {
                            virement.setEtablissement(CMPUtility.getCodeBanque());
                        } else {
                            virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
                        }
                        virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                        virement.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HHmmss"));

                        virement.setBanqueremettant((vw_virbceao.getRIBDONNEURDORDRE() != null && !vw_virbceao.getRIBDONNEURDORDRE().isEmpty() && vw_virbceao.getRIBDONNEURDORDRE().trim().length() >= 5) ? vw_virbceao.getRIBDONNEURDORDRE().substring(0, 5) : Utility.getParam("CODE_BANQUE_SICA3"));//CODE_BANQUE_SICA3                                 
                        if (vw_virbceao.getRIBDONNEURDORDRE() != null && !vw_virbceao.getRIBDONNEURDORDRE().isEmpty() && vw_virbceao.getRIBDONNEURDORDRE().trim().length() >= 10) {

                            virement.setAgenceremettant(vw_virbceao.getRIBDONNEURDORDRE().substring(5, 10));
                        } else {
                            virement.setAgenceremettant("01001");
                            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        }

                        String ribCrediteur = vw_virbceao.getRIBBENEFICIAIRE();
                        String ribDebiteur = vw_virbceao.getRIBDONNEURDORDRE();
                        virement.setNom_Tire(Utility.removeAccent(vw_virbceao.getNOMDONNEURDORDRE() != null ? Utility.bourrageDroite(vw_virbceao.getNOMDONNEURDORDRE(), 35, " ") : "ECOBANK"));
                        virement.setAdresse_Tire(" ");
                        virement.setNom_Beneficiaire(Utility.removeAccent(vw_virbceao.getNOMBENEFICIAIRE() != null ? Utility.bourrageDroite(vw_virbceao.getNOMBENEFICIAIRE(), 35, " ") : "AUTRE BANQUE"));
                        virement.setAdresse_Beneficiaire(" ");
                        switch (Integer.parseInt(vw_virbceao.getTYPEVIREMENT().substring(1))) {
                            case StaticValues.VIR_CLIENT_SICA3: {
                                if (ribCrediteur != null && ribCrediteur.length() == 24) {
                                    virement.setBanque(ribCrediteur.substring(0, 5));
                                    virement.setAgence(ribCrediteur.substring(5, 10));
                                    virement.setNumerocompte_Beneficiaire(ribCrediteur.substring(10, 22));
                                    if (!(NumberUtils.isNumber(virement.getNumerocompte_Beneficiaire().replaceAll("\\p{javaWhitespace}+", "")))) {
                                        logEvent("ERREUR", "NUMEREO DE COMPTE BENEFICIAIRE INCORRECT" + virement.getBanque().trim());
                                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                    }

                                    sql = "SELECT * FROM BANQUES WHERE trim(CODEBANQUE)='" + virement.getBanque().trim() + "'";
                                    Banques[] banques = (Banques[]) db.retrieveRowAsObject(sql, new Banques());
                                    if (banques == null || banques.length == 0) {
                                        logEvent("ERREUR", "BANQUE BENEFICIAIRE INEXISTANTE DANS LA BASE CLEARING " + virement.getBanque().trim());
                                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                    }
//                                    sql = "SELECT * FROM AGENCES WHERE trim(CODEBANQUE)='" + virement.getBanque().trim() + "' AND trim(CODEAGENCE)='" + virement.getAgence().trim() + "'  ";
//                                    Agences[] agences = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
//                                    if (agences == null || agences.length == 0) {
//                                        logEvent("ERREUR", "AGENCE BENEFICIAIRE " + virement.getAgence().trim() + " POUR LA BANQUE " + virement.getBanque().trim() + " INEXISTANTE DANS LA BASE CLEARING  ");
//                                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
//                                    }

                                } else {
                                    logEvent("ERREUR", "RIB BENEFICIAIRE INCORRECT " + ribCrediteur);
                                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));

                                }
                                if (ribDebiteur != null && ribDebiteur.length() == 24) {
                                    virement.setNumerocompte_Tire(vw_virbceao.getRIBDONNEURDORDRE().substring(10, 22));
                                } else {
                                    logEvent("ERREUR", "RIB DONNEUR D'ORDRE INCORRECT " + ribDebiteur);
                                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));

                                }
                                //  virement.setReference_Emetteur((vw_virbceao.getCONTRACT_REF_NO() != null && vw_virbceao.getCONTRACT_REF_NO().length() > 15) ? vw_virbceao.getCONTRACT_REF_NO().substring(8, 16) : " ");

                                //   virement.setNumerovirement(vw_virbceao.getCONTRACT_REF_NO().substring(8, 16));
                            }
                            break;
                            case StaticValues.VIR_BANQUE: {
                                if (ribCrediteur != null && ribCrediteur.length() >= 10) {
                                    virement.setBanque(ribCrediteur.substring(0, 5));
                                    virement.setAgence(ribCrediteur.substring(5, 10));
                                } else {
                                    logEvent("ERREUR", "RIB BENEFICIAIRE INCORRECT " + ribCrediteur);
                                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));

                                }
                                //  virement.setReference_Emetteur(vw_virbceao.getCONTRACT_REF_NO().substring(8, 16));
                            }
                            break;
                            case StaticValues.VIR_DISPOSITION_SICA3: {
                                if (ribDebiteur != null && ribDebiteur.length() == 24) {
                                    virement.setNumerocompte_Tire(vw_virbceao.getRIBDONNEURDORDRE().substring(10, 22));
                                } else {
                                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                                    logEvent("ERREUR", "RIB DONNEUR D'ORDRE INCORRECT " + ribDebiteur);

                                }
                                if (ribCrediteur != null && ribCrediteur.length() >= 10) {
                                    virement.setBanque(ribCrediteur.substring(0, 5));
                                    virement.setAgence(ribCrediteur.substring(5, 10));
                                } else {
                                    logEvent("ERREUR", "RIB BENEFICIAIRE INCORRECT " + ribCrediteur);
                                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));

                                }
                                //  virement.setReference_Emetteur(vw_virbceao.getCONTRACT_REF_NO().substring(8, 16));
                            }
                            break;
                        }

                        virement.setIdvirement(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDVIREMENT", "VIREMENTS"))));
                        //Checking pour lunicité du virement
                        sql = "SELECT * FROM VIREMENTS WHERE REFERENCE_OPERATION_INTERNE='" + virement.getReference_Operation_Interne().trim() + "'";
                        Virements[] virs = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                        if (virs != null && virs.length > 0) {
                            logEvent("ERREUR", "REFERENCE OPERATION INTERNE EXISTANTE DANS LA BASE CLEARING " + virement.getReference_Operation_Interne().trim());
                            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEANO")));
                        } else {
                           
                            if (virement.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")))) {
                                if (virement.getBanque().trim().equals(virement.getBanqueremettant())) {
                                    nonComp.add(virement);
                                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPESUPVALSURCAI")));
                                } else {
                                    //nonComp
                                    goodVirs.add(virement);
                                }

                            } else {
                                badVirs.add(virement);
                            }
                             db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                        }

                    }

                }

                if (Utility.getParam("RECUP_VIR") != null) {
                    db.executeUpdate("UPDATE PARAMS SET VALEUR='0' WHERE NOM='RECUP_VIR'");
                    Utility.clearParamsCache();
                }
                for (Virements goodVir : goodVirs) {
                    montantTotalGood += Long.parseLong(goodVir.getMontantvirement());
                }
                 for (Virements nonCom : nonComp) {
                    montantTotalNC += Long.parseLong(nonCom.getMontantvirement());
                }
                for (Virements badVir : badVirs) {
                    montantTotalBad += Long.parseLong(badVir.getMontantvirement());
                }
                setDescription(getDescription() + " exécuté avec succès:\n Nombre de Virements= " + goodVirs.size() + " - Montant Total= " + Utility.formatNumber("" + montantTotalGood));
                if (!badVirs.isEmpty()) {
                    setDescription(getDescription() + " \n Nombre de Mauvais Virements = " + badVirs.size() + " - Montant Total= " + Utility.formatNumber("" + montantTotalBad));
                }
                   if (!nonComp.isEmpty()) {
                    setDescription(getDescription() + " \n Nombre de Virements  Non Compensables= " + nonComp.size() + " - Montant Total= " + Utility.formatNumber("" + montantTotalNC));
                }
                System.out.println("Fin Execution VirementAllerFlexCube12Writer");
                logEvent("INFO", "Nombre de Virements= " + goodVirs.size() + " - Montant Total= " + Utility.formatNumber("" + montantTotalGood));
            } else {
                setDescription(getDescription() + ": Il n'y a aucun element disponible");
                logEvent("WARNING", "Il n'y a aucun element disponible");
                System.out.println("VirementAllerFlexCube12 Vide");
            }
        } else {
            logEvent("WARNING", " Un traitement de recuperation est en cours, Veuillez patienter");
            setDescription(getDescription() + " - WARNING: Un traitement de recuperation est en cours, Veuillez patienter");

        }

        db.close();

    }

}
