/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.uap;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.Operation;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Virements;
import clearing.utils.StaticValues;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.MD5;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CROUAPReader extends FlatFileReader {

    public CROUAPReader() {
        setCheckIfAlreadyTreated(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;

        BufferedReader is = openFile(aFile);
        EnteteLot[] enteteLots = new EnteteLot[1];

        int cptLot = -1;
        int cptOper = -1;
        MD5 md5 = new MD5();
        while ((line = is.readLine()) != null) {

            setCurrentLine(line);
            if (line.startsWith("ECRO")) {
                md5.update(getCurrentLine());
                cptOper = -1;
                enteteLots[++cptLot] = new EnteteLot();
                enteteLots[cptLot].setIdEntete(getChamp(4));
                enteteLots[cptLot].setIdBanRem(getChamp(5));
                getChamp(3);

                enteteLots[cptLot].setRefLot(getChamp(3));
                getChamp(3);//devise
                enteteLots[cptLot].setNbOperations(getChamp(4));
                enteteLots[cptLot].setMontantTotal(getChamp(16));
                enteteLots[cptLot].setCodeRejet(getChamp(2));
                enteteLots[cptLot].setBlancs(getChamp(24));
                enteteLots[cptLot].operations = new Operation[Integer.parseInt(enteteLots[cptLot].getNbOperations())];
            } else /* Lecture ordre*/ {
                //Commun
                //md5.update(getCurrentLine());
                enteteLots[cptLot].operations[++cptOper] = new Operation();
                enteteLots[cptLot].operations[cptOper].setRio(new RIO(getChamp(35)));
                enteteLots[cptLot].operations[cptOper].setTypeOperation(getChamp(3));
                enteteLots[cptLot].operations[cptOper].setRefOperation(getChamp(8));

                //Particulier
                switch (Integer.parseInt(enteteLots[cptLot].operations[cptOper].getTypeOperation())) {
                    case StaticValues.VIR_CLIENT: {
                        enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setPfxIBANCre(getChamp(4));
                        enteteLots[cptLot].operations[cptOper].setRibCrediteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(57));

                    }

                    break;

                    case StaticValues.VIR_CLIENT_SICA3: {
                        enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setRibCrediteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(65));

                    }

                    break;
                    case StaticValues.VIR_BANQUE: {
                        enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIdBanCre(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIdAgeCre(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(200));
                    }

                    break;
                    case StaticValues.VIR_DISPOSITION: {
                        enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setIdBanCre(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIdAgeCre(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(76));
                    }

                    break;
                    case StaticValues.VIR_DISPOSITION_SICA3: {
                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setIdBanCre(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIdAgeCre(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(81));
                    }

                    break;
                    case StaticValues.PRELEVEMENT: {
                        enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setPfxIBANCre(getChamp(4));
                        enteteLots[cptLot].operations[cptOper].setRibCrediteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(141));
                    }

                    break;
                    case StaticValues.PRELEVEMENT_SICA3: {
                        enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));

                        enteteLots[cptLot].operations[cptOper].setRibCrediteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));

                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(149));
                    }

                    break;
                    case StaticValues.CHQ_SCAN:

                    case StaticValues.CHQ_PAP: {
                        enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setPfxIBANCre(getChamp(4));
                        enteteLots[cptLot].operations[cptOper].setNumCheque(getChamp(7));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setIdBanDeb(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIdAgeDeb(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setNumCptDeb(getChamp(12));
                        enteteLots[cptLot].operations[cptOper].setCleRibDeb(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setCodeCertif(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(286));
                    }

                    break;
                    case StaticValues.CHQ_SCAN_SICA3: {
                        enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNumCheque(getChamp(7));
                        String dateOrdre = getChamp(8);
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient((dateOrdre.equals("00000000")) ? Utility.convertStringToDate(new Date(System.currentTimeMillis()).toString(), "yyyyMMdd") : Utility.convertStringToDate(dateOrdre, "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setIdBanDeb(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIdAgeDeb(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setNumCptDeb(getChamp(12));
                        enteteLots[cptLot].operations[cptOper].setCleRibDeb(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setCodeCertif(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(238));
                        enteteLots[cptLot].operations[cptOper].setMotifRejet(getChamp(3));
                    }

                    break;

                    case StaticValues.BLT_ORD_SCAN:

                    case StaticValues.BLT_ORD_PAP: {
                        enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setDateEcheance(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setCodeFrais(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setMontantFrais(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setMontantBrut(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomSouscripteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrSouscripteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setCodeAcceptation(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setRefSouscripteur(getChamp(12));
                        enteteLots[cptLot].operations[cptOper].setCodeEndossement(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setNumCedant(getChamp(7));
                        enteteLots[cptLot].operations[cptOper].setCodeAval(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(85));
                    }

                    break;

                    case StaticValues.BLT_ORD_SCAN_SICA3: {
                        enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setDateEcheance(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setCodeFrais(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setMontantFrais(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setMontantBrut(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomSouscripteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrSouscripteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setCodeAcceptation(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setRefSouscripteur(getChamp(12));
                        enteteLots[cptLot].operations[cptOper].setCodeEndossement(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setProtet(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setCodeAval(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(96));
                    }

                    break;
                    case StaticValues.LTR_CHG_SCAN:

                    case StaticValues.LTR_CHG_PAP: {
                        enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setDateEcheance(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setCodeFrais(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setMontantFrais(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setMontantBrut(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setCodeAcceptation(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setRefSouscripteur(getChamp(12));
                        enteteLots[cptLot].operations[cptOper].setCodeEndossement(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setNumCedant(getChamp(7));
                        enteteLots[cptLot].operations[cptOper].setCodeAval(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(85));
                    }
                    break;

                    case StaticValues.LTR_CHG_SCAN_SICA3: {
                        enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteLots[cptLot].operations[cptOper].setDateEcheance(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));

                        enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setCodeFrais(getChamp(2));
                        enteteLots[cptLot].operations[cptOper].setMontantFrais(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setMontantBrut(getChamp(16));
                        enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteLots[cptLot].operations[cptOper].setCodeAcceptation(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setRefSouscripteur(getChamp(12));
                        enteteLots[cptLot].operations[cptOper].setCodeEndossement(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setProtet(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setCodeAval(getChamp(1));
                        enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(96));
                    }
                    break;
                    case StaticValues.REJ_PRELEVEMENT:
                    case StaticValues.REJ_PRELEVEMENT_SICA3:
                    case StaticValues.REJ_CHQ_SCAN:
                    case StaticValues.REJ_CHQ_SCAN_SICA3:
                    case StaticValues.REJ_CHQ_PAP:
                    case StaticValues.REJ_BLT_ORD_SCAN:
                    case StaticValues.REJ_BLT_ORD_SCAN_SICA3:
                    case StaticValues.REJ_BLT_ORD_PAP:
                    case StaticValues.REJ_LTR_CHG_PAP:
                    case StaticValues.REJ_LTR_CHG_SCAN:
                    case StaticValues.REJ_LTR_CHG_SCAN_SICA3:
                    case StaticValues.REJ_CB_FER: {
                        enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteLots[cptLot].operations[cptOper].setRioOperInitial(new RIO(getChamp(35)));
                        enteteLots[cptLot].operations[cptOper].setMotifRejet(getChamp(3));

                    }

                    break;

                }
            }

        }

        is.close();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        // if (cptLot != enteteLots.length) System.out.println("WARNING: Nbre de Lot r�el diff�rent du nbr de lot marqu� dans l'entête de remise.");
        EnteteLot enteteLot1 = enteteLots[0];
        System.out.println(enteteLot1.toString());

        for (int j = 0; j < enteteLot1.operations.length; j++) {
            Operation operation1 = enteteLot1.operations[j];
            Cheques cheque = new Cheques();
            Virements virement = new Virements();
            Effets effet = new Effets();

            //effet.setRio(operation1.getRio().getRio());
            effet.setType_Effet(operation1.getTypeOperation());
            effet.setReference_Operation_Rejet(operation1.getRefOperation());
            effet.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEALLICOM1"))));

            effet.setDevise(CMPUtility.getDevise());
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                effet.setEtablissement(CMPUtility.getCodeBanque());
            } else {
                effet.setEtablissement(CMPUtility.getCodeBanqueSica3());
            }

            effet.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            effet.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HHmmss"));

            //virement.setRio(operation1.getRio().getRio());
            virement.setType_Virement(operation1.getTypeOperation());
            virement.setReference_Operation_Interne(operation1.getRefOperation());
            virement.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEALLICOM1"))));
            virement.setDevise(CMPUtility.getDevise());
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                virement.setEtablissement(CMPUtility.getCodeBanque());
            } else {
                virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
            }
            virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            virement.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HHmmss"));

            //cheque.setRio(operation1.getRio().getRio());
            cheque.setType_Cheque(operation1.getTypeOperation());
            cheque.setReference_Operation_Interne(operation1.getRefOperation());
            cheque.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEALLICOM1"))));

            cheque.setDevise(CMPUtility.getDevise());
//            if (Utility.getParam("VERSION_SICA").equals("2")) {
//                cheque.setEtablissement(CMPUtility.getCodeBanque());
//            } else {
//                cheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
//            }

            switch (Integer.parseInt(operation1.getTypeOperation())) {
                case StaticValues.VIR_CLIENT: {
                    virement.setIban_Tire(operation1.getPfxIBANDeb());
                    virement.setBanqueremettant(operation1.getRibDebiteur().substring(0, 5));
                    virement.setAgenceremettant(operation1.getRibDebiteur().substring(5, 10));
                    virement.setNumerocompte_Tire(operation1.getRibDebiteur().substring(10, 22));
                    virement.setIban_Beneficiaire(operation1.getPfxIBANCre());
                    virement.setBanque(operation1.getRibCrediteur().substring(0, 5));
                    virement.setAgence(operation1.getRibCrediteur().substring(5, 10));
                    virement.setNumerocompte_Beneficiaire(operation1.getRibCrediteur().substring(10, 22));
                    virement.setMontantvirement(String.valueOf(Long.parseLong(operation1.getMontant())));
                    virement.setNom_Tire(operation1.getNomDebiteur());
                    virement.setAdresse_Tire(operation1.getAdrDebiteur());
                    virement.setNom_Beneficiaire(operation1.getNomCrediteur());
                    virement.setAdresse_Beneficiaire(operation1.getAdrCrediteur());
                    virement.setNumerovirement(operation1.getNumIntOrdre());
                    virement.setDateordre(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    virement.setLibelle(operation1.getLibelle());
                    virement.setIdvirement(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDVIREMENT", "VIREMENTS"))));
//                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                }

                break;
                case StaticValues.VIR_CLIENT_SICA3: {

                    virement.setBanqueremettant(operation1.getRibDebiteur().substring(0, 5));
                    virement.setAgenceremettant(operation1.getRibDebiteur().substring(5, 10));
                    virement.setNumerocompte_Tire(operation1.getRibDebiteur().substring(10, 22));

                    virement.setBanque(operation1.getRibCrediteur().substring(0, 5));
                    virement.setAgence(operation1.getRibCrediteur().substring(5, 10));
                    virement.setNumerocompte_Beneficiaire(operation1.getRibCrediteur().substring(10, 22));
                    virement.setMontantvirement(String.valueOf(Long.parseLong(operation1.getMontant())));
                    virement.setNom_Tire(operation1.getNomDebiteur());
                    virement.setAdresse_Tire(operation1.getAdrDebiteur());
                    virement.setNom_Beneficiaire(operation1.getNomCrediteur());
                    virement.setAdresse_Beneficiaire(operation1.getAdrCrediteur());
                    virement.setNumerovirement(operation1.getNumIntOrdre());
                    virement.setDateordre(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    virement.setLibelle(operation1.getLibelle());
                    virement.setIdvirement(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDVIREMENT", "VIREMENTS"))));
////                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                }

                break;
                case StaticValues.VIR_BANQUE: {
                    virement.setAgenceremettant(operation1.getIdAgeRem());
                    virement.setBanque(operation1.getIdBanCre());
                    virement.setAgence(operation1.getIdAgeCre());
                    virement.setMontantvirement(String.valueOf(Long.parseLong(operation1.getMontant())));
                    virement.setNom_Tire(operation1.getNomDebiteur());
                    virement.setNom_Beneficiaire(operation1.getNomCrediteur());
                    virement.setReference_Emetteur(operation1.getRefEmetteur());
                    virement.setDateordre(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    virement.setLibelle(operation1.getLibelle());
                    virement.setIdvirement(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDVIREMENT", "VIREMENTS"))));
                    virement.setBanqueremettant(operation1.getRibDebiteur().substring(0, 5));
                    virement.setNumerovirement(operation1.getRefEmetteur());
//                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                }
                ;
                break;
                case StaticValues.VIR_DISPOSITION: {
                    virement.setIban_Tire(operation1.getPfxIBANDeb());
                    virement.setBanqueremettant(operation1.getRibDebiteur().substring(0, 5));
                    virement.setAgenceremettant(operation1.getRibDebiteur().substring(5, 10));
                    virement.setNumerocompte_Tire(operation1.getRibDebiteur().substring(10, 22));
                    virement.setBanque(operation1.getIdBanCre());
                    virement.setAgence(operation1.getIdAgeCre());
                    virement.setMontantvirement(String.valueOf(Long.parseLong(operation1.getMontant())));
                    virement.setNom_Tire(operation1.getNomDebiteur());
                    virement.setAdresse_Tire(operation1.getAdrDebiteur());
                    virement.setNom_Beneficiaire(operation1.getNomCrediteur());
                    virement.setAdresse_Beneficiaire(operation1.getAdrCrediteur());
                    virement.setReference_Emetteur(operation1.getRefEmetteur());
                    virement.setDateordre(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    virement.setLibelle(operation1.getLibelle());
                    virement.setIdvirement(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDVIREMENT", "VIREMENTS"))));
//                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");

                }

                break;
                case StaticValues.VIR_DISPOSITION_SICA3: {

                    virement.setBanqueremettant(operation1.getRibDebiteur().substring(0, 5));
                    virement.setAgenceremettant(operation1.getRibDebiteur().substring(5, 10));
                    virement.setNumerocompte_Tire(operation1.getRibDebiteur().substring(10, 22));
                    virement.setBanque(operation1.getIdBanCre());
                    virement.setAgence(operation1.getIdAgeCre());
                    virement.setMontantvirement(String.valueOf(Long.parseLong(operation1.getMontant())));
                    virement.setNom_Tire(operation1.getNomDebiteur());
                    virement.setAdresse_Tire(operation1.getAdrDebiteur());
                    virement.setNom_Beneficiaire(operation1.getNomCrediteur());
                    virement.setAdresse_Beneficiaire(operation1.getAdrCrediteur());
                    virement.setReference_Emetteur(operation1.getRefEmetteur());
                    virement.setDateordre(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    virement.setLibelle(operation1.getLibelle());
                    virement.setIdvirement(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDVIREMENT", "VIREMENTS"))));
//                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");

                }

                break;
                case StaticValues.PRELEVEMENT:
                        ;
                    break;
                case StaticValues.CHQ_SCAN:
                case StaticValues.CHQ_PAP: {

                    cheque.setBanqueremettant(operation1.getRio().getIdEmetteur());

                    cheque.setDateemission(Utility.convertDateToString(operation1.getRio().getDatePresentation(), ResLoader.getMessages("patternDate")));
                    cheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    cheque.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternTime")));

                    cheque.setVilleremettant(operation1.getRio().getPac().substring(1));
                    cheque.setAgenceremettant(operation1.getIdAgeRem());
                    cheque.setVille(operation1.getIPac());
                    cheque.setIban(operation1.getPfxIBANCre());
                    cheque.setNumerocheque(operation1.getNumCheque());
                    cheque.setDatesaisie(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    cheque.setBanque(operation1.getIdBanDeb());
                    cheque.setAgence(operation1.getIdAgeDeb());
                    cheque.setNumerocompte(operation1.getNumCptDeb());
                    cheque.setRibcompte(operation1.getCleRibDeb());
                    cheque.setMontantcheque(String.valueOf(Long.parseLong(operation1.getMontant())));
                    cheque.setNombeneficiaire(operation1.getNomCrediteur());
                    cheque.setCodecertification(operation1.getCodeCertif());
                    cheque.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternTime")));

////                    String sql = "SELECT * FROM CHEQUES WHERE BANQUE ='" + cheque.getBanque() + "'"
////                            + " AND NUMEROCHEQUE ='" + cheque.getNumerocheque() + "'"
////                            + " AND NUMEROCOMPTE ='" + cheque.getNumerocompte() + "'"
////                            + " AND MONTANTCHEQUE ='" + cheque.getMontantcheque() + "' "
////                            + " AND AGENCE ='" + cheque.getAgence()+ "' "
////                            + " AND ETAT =" + Utility.getParam("CETAOPERETIMA");
////                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
////
////                    if (cheques != null && cheques.length > 0) {
////                        cheque.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEALLICOM1ACC "))));
////
////                        sql = " IDCHEQUE =" + cheques[0].getIdcheque();
////                        db.updateRowByObjectByQuery(cheque, "CHEQUES", sql);
////                    } else {
////                        cheque.setIdcheque(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDCHEQUE", "CHEQUES"))));
////                        db.insertObjectAsRowByQuery(cheque, "CHEQUES");
////                    }
                }

                break;
                case StaticValues.CHQ_SCAN_SICA3: {

                    cheque.setBanqueremettant(operation1.getRio().getIdEmetteur());
                    cheque.setRio(operation1.getRio().getRio());
                    cheque.setDateemission(Utility.convertDateToString(operation1.getRio().getDatePresentation(), ResLoader.getMessages("patternDate")));
                    cheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    cheque.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternTime")));

                    cheque.setVilleremettant(operation1.getRio().getPac().substring(1));
                    cheque.setAgenceremettant(operation1.getIdAgeRem());
                    cheque.setVille(operation1.getIPac());

                    cheque.setNumerocheque(operation1.getNumCheque());
                    cheque.setDatesaisie(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    cheque.setBanque(operation1.getIdBanDeb());
                    cheque.setAgence(operation1.getIdAgeDeb());
                    cheque.setNumerocompte(operation1.getNumCptDeb());
                    cheque.setRibcompte(operation1.getCleRibDeb());
                    cheque.setMontantcheque(String.valueOf(Long.parseLong(operation1.getMontant())));
                    cheque.setNombeneficiaire(operation1.getNomCrediteur());
                    cheque.setCodecertification(operation1.getCodeCertif());
                    cheque.setMotifrejet(operation1.getMotifRejet());
                    System.out.println("Motif rejet " + cheque.getMotifrejet());

                    String sql = "SELECT * FROM CHEQUES WHERE "
                            + " trim(BANQUE) ='" + cheque.getBanque().trim() + "'"
                            + " AND trim(AGENCE) ='" + cheque.getAgence().trim() + "'"
                            + " AND trim(NUMEROCHEQUE) ='" + cheque.getNumerocheque().trim() + "' "
                            + " AND trim(NUMEROCOMPTE) ='" + cheque.getNumerocompte().trim() + "' "
                            + " AND trim(MONTANTCHEQUE) ='" + cheque.getMontantcheque().trim() + "' "
                            + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM1")
                            + "";
                    System.out.println("sql " + sql);
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                    if (cheques != null && cheques.length > 0) {

                        if (cheque.getMotifrejet() != null && cheque.getMotifrejet().equals("000")) {
                            cheque.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEALLICOM1ACCENVSIB"))));

                        } else {

                            System.out.println("Cheque pas accept� en compense");
                        }

                        sql = " IDCHEQUE =" + cheques[0].getIdcheque();

                        db.updateRowByObjectByQuery(cheque, "CHEQUES", sql);
                    } else {
//                        cheque.setIdcheque(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDCHEQUE", "CHEQUES"))));
//                        db.insertObjectAsRowByQuery(cheque, "CHEQUES");
                    }

                }

                break;
                case StaticValues.BLT_ORD_SCAN:
                        ;
                case StaticValues.BLT_ORD_PAP: {
                    effet.setBanque(operation1.getRibDebiteur().substring(0, 5));
                    effet.setAgence(operation1.getRibDebiteur().substring(5, 10));
                    effet.setNumerocompte_Tire(operation1.getRibDebiteur().substring(10, 22));
                    effet.setAgenceremettant(operation1.getIdAgeRem());
                    effet.setBanqueremettant(operation1.getRio().getIdEmetteur());
                    effet.setVille(operation1.getIPac());
                    effet.setNumeroeffet(operation1.getNumIntOrdre());
                    effet.setDate_Echeance(Utility.convertDateToString(operation1.getDateEcheance(), ResLoader.getMessages("patternDate")));
                    effet.setIban_Tire(operation1.getPfxIBANDeb());
                    effet.setMontant_Effet(String.valueOf(Long.parseLong(operation1.getMontant())));
                    effet.setCode_Frais(operation1.getCodeFrais());
                    effet.setMontant_Frais(String.valueOf(Long.parseLong(operation1.getMontantFrais())));
                    effet.setMontant_Brut(String.valueOf(Long.parseLong(operation1.getMontantBrut())));
                    effet.setNom_Tire(operation1.getNomSouscripteur());
                    effet.setAdresse_Tire(operation1.getAdrSouscripteur());
                    effet.setNom_Beneficiaire(operation1.getNomCrediteur());
                    effet.setAdresse_Beneficiaire(operation1.getAdrCrediteur());
                    effet.setDate_Creation(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    effet.setCode_Acceptation(operation1.getCodeAcceptation());
                    effet.setIdentification_Tire(operation1.getRefSouscripteur());
                    effet.setCode_Endossement(operation1.getCodeEndossement());
                    effet.setNumero_Cedant(operation1.getNumCedant());
                    effet.setCode_Aval(operation1.getCodeAval());
                    effet.setIdeffet(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDEFFET", "EFFETS"))));
//                    db.insertObjectAsRowByQuery(effet, "EFFETS");

                }

                break;
                case StaticValues.BLT_ORD_SCAN_SICA3: {
                    effet.setAgenceremettant(operation1.getIdAgeRem());
                    effet.setBanqueremettant(operation1.getRio().getIdEmetteur());

                    effet.setVille(operation1.getIPac());
                    effet.setNumeroeffet(operation1.getNumIntOrdre());
                    effet.setDate_Echeance(Utility.convertDateToString(operation1.getDateEcheance(), ResLoader.getMessages("patternDate")));
//                            effet.setBanque(enteteRemise.getIdRecepeteur());
                    effet.setMontant_Effet(String.valueOf(Long.parseLong(operation1.getMontant())));
                    effet.setCode_Frais(operation1.getCodeFrais());
                    effet.setMontant_Frais(String.valueOf(Long.parseLong(operation1.getMontantFrais())));
                    effet.setMontant_Brut(String.valueOf(Long.parseLong(operation1.getMontantBrut())));
                    effet.setNom_Tire(operation1.getNomSouscripteur());
                    effet.setAdresse_Tire(operation1.getAdrSouscripteur());
                    effet.setNom_Beneficiaire(operation1.getNomCrediteur());
                    effet.setAdresse_Beneficiaire(operation1.getAdrCrediteur());
                    effet.setDate_Creation(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    effet.setCode_Acceptation(operation1.getCodeAcceptation());
                    effet.setIdentification_Tire(operation1.getRefSouscripteur());
                    effet.setCode_Endossement(operation1.getCodeEndossement());
                    effet.setProtet(operation1.getProtet());
                    effet.setCode_Aval(operation1.getCodeAval());
//                    String sql = "SELECT * FROM EFFETS WHERE BANQUE ='" + effet.getBanque()
//                            + "' AND NUMEROCHEQUE ='" + effet.getNumeroeffet()
//                            + "' AND NUMEROCOMPTE ='" + effet.getNumerocompte_Beneficiaire()
//                            + "' AND MONTANTEFFET ='" + effet.getMontant_Effet() + "' AND ETAT =" + Utility.getParam("CETAOPERETIMA")
//                            + "";
//                    Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
//
//                    if (effets != null && effets.length > 0) {
//                        effet.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPERETREC"))));
//
//                        sql = " IDEFFET =" + effets[0].getIdeffet();
//
//                        db.updateRowByObjectByQuery(effet, "EFFETS", sql);
//                    } else {
//                        effet.setIdeffet(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDEFFET", "EFFETS"))));
//                        db.insertObjectAsRowByQuery(effet, "EFFETS");
//                    }

                }

                break;
                case StaticValues.LTR_CHG_SCAN:
                case StaticValues.LTR_CHG_PAP: {
                    effet.setBanqueremettant(operation1.getRio().getIdEmetteur());
                    effet.setAgenceremettant(operation1.getIdAgeRem());
                    effet.setVille(operation1.getIPac());
                    effet.setNumeroeffet(operation1.getNumIntOrdre());
                    effet.setDate_Echeance(Utility.convertDateToString(operation1.getDateEcheance(), ResLoader.getMessages("patternDate")));
                    effet.setIban_Tire(operation1.getPfxIBANDeb());
                    effet.setBanque(operation1.getRibDebiteur().substring(0, 5));
                    effet.setAgence(operation1.getRibDebiteur().substring(5, 10));
                    effet.setNumerocompte_Tire(operation1.getRibDebiteur().substring(10, 22));
                    effet.setMontant_Effet(String.valueOf(Long.parseLong(operation1.getMontant())));
                    effet.setCode_Frais(operation1.getCodeFrais());
                    effet.setMontant_Frais(String.valueOf(Long.parseLong(operation1.getMontantFrais())));
                    effet.setMontant_Brut(String.valueOf(Long.parseLong(operation1.getMontantBrut())));
                    effet.setNom_Tire(operation1.getNomDebiteur());
                    effet.setAdresse_Tire(operation1.getAdrDebiteur());
                    effet.setNom_Beneficiaire(operation1.getNomCrediteur());
                    effet.setAdresse_Beneficiaire(operation1.getAdrCrediteur());
                    effet.setDate_Creation(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    effet.setCode_Acceptation(operation1.getCodeAcceptation());
                    effet.setIdentification_Tire(operation1.getRefSouscripteur());
                    effet.setCode_Endossement(operation1.getCodeEndossement());
                    effet.setNumero_Cedant(operation1.getNumCedant());
                    effet.setCode_Aval(operation1.getCodeAval());
                    effet.setIdeffet(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDEFFET", "EFFETS"))));
//                    db.insertObjectAsRowByQuery(effet, "EFFETS");

                }
                break;
                case StaticValues.LTR_CHG_SCAN_SICA3: {
                    effet.setBanqueremettant(operation1.getRio().getIdEmetteur());
                    effet.setAgenceremettant(operation1.getIdAgeRem());
                    effet.setVille(operation1.getIPac());
                    effet.setNumeroeffet(operation1.getNumIntOrdre());
                    effet.setDate_Echeance(Utility.convertDateToString(operation1.getDateEcheance(), ResLoader.getMessages("patternDate")));

                    effet.setBanque(operation1.getRibDebiteur().substring(0, 5));
                    effet.setAgence(operation1.getRibDebiteur().substring(5, 10));
                    effet.setNumerocompte_Tire(operation1.getRibDebiteur().substring(10, 22));
                    effet.setMontant_Effet(String.valueOf(Long.parseLong(operation1.getMontant())));
                    effet.setCode_Frais(operation1.getCodeFrais());
                    effet.setMontant_Frais(String.valueOf(Long.parseLong(operation1.getMontantFrais())));
                    effet.setMontant_Brut(String.valueOf(Long.parseLong(operation1.getMontantBrut())));
                    effet.setNom_Tire(operation1.getNomSouscripteur());
                    effet.setAdresse_Tire(operation1.getAdrSouscripteur());
                    effet.setNom_Beneficiaire(operation1.getNomCrediteur());
                    effet.setAdresse_Beneficiaire(operation1.getAdrCrediteur());
                    effet.setDate_Creation(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    effet.setCode_Acceptation(operation1.getCodeAcceptation());
                    effet.setIdentification_Tire(operation1.getRefSouscripteur());
                    effet.setCode_Endossement(operation1.getCodeEndossement());
                    effet.setProtet(operation1.getProtet());
                    effet.setCode_Aval(operation1.getCodeAval());
//                    String sql = "SELECT * FROM EFFETS WHERE BANQUE ='" + effet.getBanque()
//                            + "' AND NUMEROCHEQUE ='" + effet.getNumeroeffet()
//                            + "' AND NUMEROCOMPTE ='" + effet.getNumerocompte_Beneficiaire()
//                            + "' AND MONTANTEFFET ='" + effet.getMontant_Effet() + "' AND ETAT =" + Utility.getParam("CETAOPERETIMA")
//                            + "";
//                    Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
//
//                    if (effets != null && effets.length > 0) {
//                        effet.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPERETREC"))));
//
//                        sql = " IDEFFET =" + effets[0].getIdeffet();
//
//                        db.updateRowByObjectByQuery(effet, "EFFETS", sql);
//                    } else {
//                        effet.setIdeffet(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDEFFET", "EFFETS"))));
//                        db.insertObjectAsRowByQuery(effet, "EFFETS");
//                    }

                }
                break;
                case StaticValues.REJ_PRELEVEMENT:
                    break;
                case StaticValues.REJ_CHQ_SCAN:
                case StaticValues.REJ_CHQ_SCAN_SICA3:
                case StaticValues.REJ_CHQ_PAP: {
                    String sql = "SELECT * FROM CHEQUES WHERE RIO ='" + operation1.getRioOperInitial().getRio() + "'";
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                    if (cheques != null && cheques.length > 0) {

                        cheques[0].setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEREJRET")))); ///CETAOPEALLICOM2ACCENVSIB CETAOPEREJRET                                     
                        cheques[0].setMotifrejet(operation1.getMotifRejet());

                        sql = " RIO ='" + operation1.getRioOperInitial().getRio() + "'";
                        db.updateRowByObjectByQuery(cheques[0], "CHEQUES", sql);
                    } else {
                        System.out.println("Ordre a rejeter introuvable : " + operation1.toString());
//                        logEvent("WARNING", "Ordre a rejeter introuvable : " + operation1.getRioOperInitial().getRio());
                    }

                }
                break;
                case StaticValues.REJ_BLT_ORD_SCAN:
                case StaticValues.REJ_BLT_ORD_SCAN_SICA3:
                case StaticValues.REJ_BLT_ORD_PAP:
                case StaticValues.REJ_LTR_CHG_SCAN_SICA3:
                case StaticValues.REJ_LTR_CHG_PAP:
                case StaticValues.REJ_LTR_CHG_SCAN: {
//                    System.out.println(operation1.toString());
//                    String sql = "SELECT * FROM EFFETS WHERE RIO ='" + operation1.getRioOperInitial().getRio() + "'";
//                    Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
//
//                    if (effets != null && effets.length > 0) {
//                        sql = "UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEALLICOM2") + " ,MOTIFREJET='" + operation1.getMotifRejet() + "'WHERE RIO ='" + operation1.getRioOperInitial().getRio() + "'";
//                        db.executeUpdate(sql);
//                    } else {
//                        System.out.println("Ordre a rejeter introuvable : " + operation1.toString());
//                        logEvent("WARNING", "Ordre a rejeter introuvable : " + operation1.getRioOperInitial().getRio());
//                    }
                }
                break;

            }

            System.out.println(operation1.toString());

        }

        db.close();

        return aFile;
    }
}
