/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube.nsia;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.Remises;
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
public class ChequeAllerFlexCubeNSIAWriter extends FlatFileWriter {

    public ChequeAllerFlexCubeNSIAWriter() {
        setDescription("Envoi des chèques vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;

        String numeroBatch = "";
        String[] param1 = (String[]) getParametersMap().get("textParam1");
        if (param1 != null && param1.length > 0) {
            numeroBatch = param1[0];
        }
        System.out.println("Numéro de Batch = " + numeroBatch);

        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);

        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");

        String dateValeur = "";
        param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur J2 = " + dateValeur + " yyyyMMdd ");

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String compteur;

//        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
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

//     String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");
        String dateTraitement = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_IN_FILE_ROOTNAME") + dateTraitement + "_" + numeroBatch + Utility.getParam("SIB_FILE_SQL_EXTENSION");

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

        // Population des cheques en credit immediat
        sql = "SELECT * FROM CHEQUES WHERE "
                + " ETAT IN  (" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") "
                + " AND ESCOMPTE=1 "
                + "AND  DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        int j = 0;
        long montantTotal = 0;
        long montantTotalCRI = 0;
        long montantTotalEchec = 0;

        if (cheques != null && 0 < cheques.length) {
            for (int i = 0; i < cheques.length; i += j) {

                {
                    //Tous les cheques validés

                    //Tous les cheques compensables validés d'une remise
                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND "
                            + " ETAT IN  (" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "," + Utility.getParam("CETAOPEREJRETENVSIB") + ") "
                            + " AND ESCOMPTE=1 "
                            + " AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
                    Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[i].getRemise();
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                    if (remises != null && remises.length > 0) {

                        if (chequesVal != null && 0 < chequesVal.length) {
                            j = chequesVal.length;
                            if (!isValidLine(cheques[i])) {

                            } else {
                                long sumRemise = 0;

                                Cheques aCheque = chequesVal[0];
                                for (int x = 0; x < chequesVal.length; x++) {
                                    sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                                }
                                montantTotal += sumRemise;

                                //Creation ligne de chèque
                                String libelle;

                                Comptes cptGR = CMPUtility.getInfoCompte(aCheque.getCompteremettant());
                                if (cptGR.getSignature2() == null || (cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("R"))) {

                                    if ((cptGR.getSignature3() != null && cptGR.getSignature3().trim().equals("O"))) {
                                        libelle = remises[0].getReference();
                                    } else {
                                        libelle = "Versement Remise N°" + remises[0].getReference();
                                    }

                                    DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                                    

                                    detbUploadDetail = new DETB_UPLOAD_DETAIL();
                                    detbUploadDetail.setBATCH_NO(numeroBatch);
                                    detbUploadDetail.setFIN_CYCLE(current_cycle);
                                    detbUploadDetail.setPERIOD_CODE(current_period);
                                    detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                                    detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXCRIBRANCH"));//agence du compte  cc
                                    detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALECRIFLEX"));//REM ACCOUNT
                                    detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                                    detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                                    detbUploadDetail.setAMOUNT(new BigDecimal(sumRemise)); //AMOUNT
                                    detbUploadDetail.setADDL_TEXT(libelle);
                                    detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                                    detbUploadDetail.setINSTRUMENT_NO("" + aCheque.getRemise());
                                    detbUploadDetail.setREL_CUST("");
                                    detbUploadDetail.setEXTERNAL_REF_NO("");
                                    detbUploadDetail.setRELATED_ACCOUNT("");
                                    detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                                    detbUploadDetails.add(detbUploadDetail);

                                } else if ((cptGR.getSignature2() != null && cptGR.getSignature2().trim().equals("C"))) {

                                    // libelle = Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX10") + " (" + CMPUtility.getLibelleBanque(aCheque.getBanque()) + ") NO " + Utility.bourrageGauche(aCheque.getNumerocheque() , 7, "0"), 100, " ");
                                     
                                    //eugene
                                        String nomEmetteur;
                                            if (aCheque.getNomemetteur() == null || aCheque.getNomemetteur().isEmpty()) {
                                                nomEmetteur = " ";
                                            } else {
                                                nomEmetteur = aCheque.getNomemetteur();
                                            }
                                     libelle = Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX10") + " (" + CMPUtility.getLibelleBanque(aCheque.getBanque()) + ") NO " + Utility.bourrageGauche(aCheque.getNumerocheque() , 7, "0")+" de " + nomEmetteur, 100, " ");
                                    //Credit cheque par cheque sur le compte du gros remettant

                                    for (int x = 0; x < chequesVal.length; x++) {

                                        aCheque = chequesVal[x];
                                        DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                                        

                                        detbUploadDetail = new DETB_UPLOAD_DETAIL();
                                        detbUploadDetail.setBATCH_NO(numeroBatch);
                                        detbUploadDetail.setFIN_CYCLE(current_cycle);
                                        detbUploadDetail.setPERIOD_CODE(current_period);
                                        detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                                        detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXCRIBRANCH"));//agence du compte  cc
                                        detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALECRIFLEX"));//REM ACCOUNT
                                        detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                                        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                                        detbUploadDetail.setAMOUNT(new BigDecimal(aCheque.getMontantcheque())); //AMOUNT
                                        detbUploadDetail.setADDL_TEXT(libelle);
                                        detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                                        detbUploadDetail.setINSTRUMENT_NO(aCheque.getNumerocheque());
                                        detbUploadDetail.setREL_CUST("");
                                        detbUploadDetail.setEXTERNAL_REF_NO("");
                                        detbUploadDetail.setRELATED_ACCOUNT("");
                                        detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                                        detbUploadDetails.add(detbUploadDetail);
//                                   

                                    }

                                }
                            }

                        }
                    }
                }

            }

            montantTotalCRI = montantTotal;
            
            setDescription(getDescription() + "<br>Liquidation des credits immédiats sur les comptes internes:");
            setDescription(getDescription() + " exécuté avec succès:<br> Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal));

        }
        // Population
        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") "
                + " AND ESCOMPTE<>1 "
                + "AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        j = 0;
        montantTotal = 0;

        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));

            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques validés

                //Tous les cheques compensables validés d'une remise
                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[i].getRemise() + " AND ETAT"
                        + " IN (" + Utility.getParam("CETAOPEALLICOM1ACC") + ") "
                        + " AND ESCOMPTE<>1 "
                        + " AND DATECOMPENSATION='" + dateCompensation + "' ORDER BY REMISE";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                System.out.println("chequesVal " + chequesVal.length + " Remise " + cheques[i].getRemise());

                j = chequesVal.length;

                if (chequesVal != null && 0 < chequesVal.length) {
                    long sumRemise = 0;

                    Cheques aCheque = chequesVal[0];
                    for (int x = 0; x < chequesVal.length; x++) {
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                    }
                    montantTotal += sumRemise;

                    for (int x = 0; x < chequesVal.length; x++) {
                        aCheque = chequesVal[x];

                        StringBuffer line = new StringBuffer();

                        String nomEmetteur;
                        if (aCheque.getNomemetteur() == null || aCheque.getNomemetteur().isEmpty()) {
                            nomEmetteur = " ";
                        } else {
                            nomEmetteur = aCheque.getNomemetteur();
                        }

                        /**
                         * ligne F12
                         */
                        Comptes compteFlexCube = CMPUtility.getCompteESNFlexCube(aCheque.getCompteremettant(), aCheque.getAgenceremettant());
                        DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
                        detbUploadDetail.setBATCH_NO(numeroBatch);
                        detbUploadDetail.setFIN_CYCLE(current_cycle);
                        detbUploadDetail.setPERIOD_CODE(current_period);
                        detbUploadDetail.setVALUE_DATE(dateValeur != null ? Utility.convertStringToDate(dateValeur, "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
                        detbUploadDetail.setACCOUNT_BRANCH(compteFlexCube != null ? compteFlexCube.getAdresse2().trim() : Utility.getParam("TXN_BRANCH").charAt(0) + aCheque.getAgenceremettant().substring(3));//agence du compte  cc
                        detbUploadDetail.setACCOUNT(compteFlexCube != null ? compteFlexCube.getNumcptex() : "" + aCheque.getCompteremettant());//REM ACCOUNT
                        detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
                        detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
                        detbUploadDetail.setAMOUNT(new BigDecimal(aCheque.getMontantcheque())); //AMOUNT
                        //detbUploadDetail.setADDL_TEXT( Utility.bourrageDroite(nomEmetteur.trim() + " " + Utility.getParam("LIBCHQALEFLEX1") + " " + CMPUtility.getLibelleBanque(aCheque.getBanqueremettant()) + " N° " + Utility.bourrageGauche(aCheque.getNumerocheque() + "", 7, "0"), 100, " "));
                        detbUploadDetail.setADDL_TEXT(Utility.bourrageDroite(Utility.getParam("LIBCHQALEFLEX10") + " " + CMPUtility.getLibelleBanque(aCheque.getBanque()) + " N° " + Utility.bourrageGauche(aCheque.getNumerocheque(), 7, "0")+" de "+nomEmetteur, 100, " "));
                        detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
                        detbUploadDetail.setINSTRUMENT_NO(aCheque.getNumerocheque());
                        detbUploadDetail.setREL_CUST("");
                        detbUploadDetail.setEXTERNAL_REF_NO("");
                        detbUploadDetail.setRELATED_ACCOUNT("");
                        detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
                        detbUploadDetails.add(detbUploadDetail);

                        if (aCheque.getEtat().equals(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACC")))) {
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                            db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                        }
                    }

                    //  }
                    db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAREMENVSIB") + " WHERE IDREMISE=" + aCheque.getRemise());
                }

                /*} else {

            db.executeUpdate("UPDATE CHEQUES SET ETAT =" + Utility.getParam("CETAOPEERR") + " WHERE REMISE=" + cheques[i].getRemise());
            db.executeUpdate("UPDATE REMISES SET ETAT=" + Utility.getParam("CETAOPEERR") + " WHERE IDREMISE=" + cheques[i].getRemise());
            }*/
            }

            /**
             * ligne F12
             */
            DETB_UPLOAD_DETAIL detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX"));//REM ACCOUNT 371300000
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal+montantTotalCRI)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX1"));//Paiement client
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
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX"));//REM ACCOUNT 371300000
            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal+montantTotalCRI)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX2"));//Regularisation pour paiements clients
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
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("TXN_BRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTBCEAOFLEX"));//REM ACCOUNT 0100401490059989025
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal+montantTotalCRI)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEBCEAOFLEX") + " " + Utility.convertDateToString(Utility.convertStringToDate(cheques[0].getDatecompensation(), "yyyy/MM/dd"), "dd-MM-yyyy"));//Règlement BCEAO compense aller du 05 06 2019
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            /**
             * ligne F12 debut Annulation
             */
            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX6"));//REM ACCOUNT 961200000
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal+montantTotalCRI)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX3"));//Annulation de la réception
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            /**
             * ligne F12
             */
            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX5"));//REM ACCOUNT 961100000
            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal+montantTotalCRI)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX3"));
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            /**
             * ligne F12
             */
            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX3"));//REM ACCOUNT 373100000
            detbUploadDetail.setDR_CR("D");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal+montantTotalCRI)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX4"));//Annulation envoi en recouvrement
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            /**
             * ligne F12 Fin annulation
             */
            detbUploadDetail = new DETB_UPLOAD_DETAIL();
            detbUploadDetail.setBATCH_NO(numeroBatch);
            detbUploadDetail.setFIN_CYCLE(current_cycle);
            detbUploadDetail.setPERIOD_CODE(current_period);
            detbUploadDetail.setVALUE_DATE(CMPUtility.getDate() != null ? Utility.convertStringToDate(CMPUtility.getDate(), "yyyyMMdd") : new Date());//VALUE_DATE a recuperer par une requete
            detbUploadDetail.setSOURCE_CODE(Utility.getParam("BATCH_TYPE"));
            detbUploadDetail.setACCOUNT_BRANCH(Utility.getParam("FLEXMAINBRANCH"));//agence du compte
            detbUploadDetail.setACCOUNT(Utility.getParam("CPTATTCHQALEFLEX4"));//REM ACCOUNT 372100000
            detbUploadDetail.setDR_CR("C");//DR_CR Sens D ou C
            detbUploadDetail.setTXN_CODE("ACT");//Code transaction TXN_CODE
            detbUploadDetail.setAMOUNT(new BigDecimal(montantTotal+montantTotalCRI)); //AMOUNT
            detbUploadDetail.setADDL_TEXT(Utility.getParam("LIBCHQALEFLEX4"));
            detbUploadDetail.setREL_CUST("");
            detbUploadDetail.setEXTERNAL_REF_NO("");
            detbUploadDetail.setRELATED_ACCOUNT("");
            detbUploadDetail.setTXT_FILE_NAME(new File(fileName).getName());
            detbUploadDetails.add(detbUploadDetail);

            detbUploadDetailArray = new DETB_UPLOAD_DETAIL[detbUploadDetails.size()];
            detbUploadDetailArray = detbUploadDetails.toArray(detbUploadDetailArray);
            writeFile(detbUploadDetailArray);

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + montantTotal) + " - Nom de Fichier = " + fileName);
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
                + "INTO TABLE " + Utility.getParam("FLEXSCHEMA") + ". DETB_UPLOAD_DETAIL\n"
                + " Fields terminated by '~'\n"
                + " Trailing Nullcols\n"
                + " (FIN_CYCLE,PERIOD_CODE,VALUE_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\",ADDL_TEXT,BRANCH_CODE,SOURCE_CODE,ACCOUNT_BRANCH,ACCOUNT,TXN_CODE,BATCH_NO, "
                + " CURR_NO,AMOUNT,DR_CR,UPLOAD_STAT,CCY_CD,INITIATION_DATE DATE \"" + Utility.getParam("DATE_FORMAT") + "\"  ,LCY_EQUIVALENT,EXCH_RATE,rel_cust,external_ref_no,related_account,TXT_FILE_NAME,INSTRUMENT_NO)\n"
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
            line.append(sep(Utility.convertDateToString(detbUploadDetail.getINITIATION_DATE(), "ddMMyyyy")));
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

    private int createLinesCRISQL(String current_cycle, String current_period, String numeroBatch, int numLigne, String separateur, String referenceRelative,
            String montantLigne, String dateValeur, String libelle, String fileName) throws Exception {

        /**
         * Format SQL LOADER
         */
        //Ligne de debit du montant du bordereau sur le compte d'attente
        //Ligne de debit
        StringBuilder line = new StringBuilder();
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
        line.append(Utility.getParam("TXN_BRANCH"));
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALEFLEX"));
        line.append(separateur);
        line.append("F03");
        line.append(separateur);
        line.append(numeroBatch);
        line.append(separateur);
        line.append(numLigne++);
        line.append(separateur);
        line.append(montantLigne);
        line.append(separateur);
        line.append("D");
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
        line.append(Utility.getParam("CPTATTCHQALECRIFLEX")); // //Related Account
        line.append(separateur);
        line.append(fileName);
        writeln(line.toString());

        /**
         * Format SQL LOADER
         */
        //Ligne de debit du montant du bordereau sur le compte d'attente
        //Ligne de debit
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
        line.append(Utility.getParam("TXN_BRANCH"));
        line.append(separateur);
        line.append(Utility.getParam("CPTATTCHQALECRIFLEX"));
        line.append(separateur);
        line.append("F57");
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
        line.append(Utility.getParam("CPTATTCHQALEFLEX")); // //Related Account
        line.append(separateur);
        line.append(fileName);
        writeln(line.toString());
        return numLigne;

    }

    private boolean isValidLine(Cheques cheque) throws Exception {
        //Verification de l'existence du compte
        Comptes numCptEx = CMPUtility.getCompteESNFlexCube(cheque.getCompteremettant(), cheque.getAgenceremettant());
        if (numCptEx == null) {
            return false;
        }
        //Verification du compte de scan

        return !cheque.getCompteremettant().equals(Utility.getParam("CPTATTSCANCHQ"));
    }
    private String sep = "~";

}
