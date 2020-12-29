/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.sica;

import clearing.action.writers.uap.CraUAPWriter;
import clearing.model.EnteteLot;
import clearing.model.EnteteRemise;
import clearing.model.Operation;
import clearing.model.RIO;
import clearing.table.Lotcom;
import clearing.table.Remcom;
import clearing.utils.StaticValues;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class IcomaToCraReaderV1 extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;
        String sql = "";


        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());



        BufferedReader is = openFile(aFile);
        EnteteRemise enteteRemise = new EnteteRemise();


        int cptLot = -1;
        int cptOper = -1;
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            if (line.startsWith("ECRA")) {
                enteteRemise.setIdEntete(getChamp(4));
                enteteRemise.setIdEmetteur(getChamp(5));
                enteteRemise.setRefRemise(getChamp(3));
                enteteRemise.setDatePresentation(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                enteteRemise.setIdRecepeteur(getChamp(5));
                enteteRemise.setDevise(getChamp(3));
                enteteRemise.setTypeRemise(getChamp(5));
                enteteRemise.setRefRemRelatif(getChamp(3));
                enteteRemise.setNbLots(getChamp(4));
                enteteRemise.setCodeRejet(getChamp(2));
                enteteRemise.setSeance(getChamp(1));
                enteteRemise.setFlagInversion(getChamp(1));
                getChamp(20);
                enteteRemise.enteteLots = new EnteteLot[Integer.parseInt(enteteRemise.getNbLots())];
                if (Utility.getParam("ATTENTE_ICOMA_NAT") != null && enteteRemise.getIdEmetteur().contains("SCN")) {
                    db.executeUpdate("UPDATE PARAMS SET VALEUR='0' WHERE NOM='ATTENTE_ICOMA_NAT'");
                    Utility.clearParamsCache();
                }
                if (Utility.getParam("ATTENTE_ICOMA_SRG") != null && enteteRemise.getIdEmetteur().contains("SSR")) {
                    db.executeUpdate("UPDATE PARAMS SET VALEUR='0' WHERE NOM='ATTENTE_ICOMA_SRG'");
                    Utility.clearParamsCache();
                }
            } else if (line.startsWith("ELOT")) {
                cptOper = -1;
                enteteRemise.enteteLots[++cptLot] = new EnteteLot();
                enteteRemise.enteteLots[cptLot].setIdEntete(getChamp(4));
                enteteRemise.enteteLots[cptLot].setRefLot(getChamp(3));
                enteteRemise.enteteLots[cptLot].setRefBancaire(getChamp(5));
                enteteRemise.enteteLots[cptLot].setTypeOperation(getChamp(3));
                enteteRemise.enteteLots[cptLot].setIdBanRem(getChamp(5));
                enteteRemise.enteteLots[cptLot].setNbOperations(getChamp(4));
                enteteRemise.enteteLots[cptLot].setMontantTotal(getChamp(16));
                enteteRemise.enteteLots[cptLot].setNbTotOperAcc(getChamp(4));
                enteteRemise.enteteLots[cptLot].setMntTotOperAcc(getChamp(16));
                enteteRemise.enteteLots[cptLot].setCodeRejet(getChamp(2));
                getChamp(2);
                enteteRemise.enteteLots[cptLot].operations = new Operation[Integer.parseInt(enteteRemise.enteteLots[cptLot].getNbOperations())];
            } else if (line.startsWith("FCRA")) {
            } else /* Lecture ordre*/ {
                //Commun

                enteteRemise.enteteLots[cptLot].operations[++cptOper] = new Operation();
                enteteRemise.enteteLots[cptLot].operations[cptOper].setTypeOperation(getChamp(3));
                enteteRemise.enteteLots[cptLot].operations[cptOper].setRefOperation(getChamp(8));

                //Particulier
                switch (Integer.parseInt(enteteRemise.enteteLots[cptLot].getTypeOperation())) {
                    case StaticValues.VIR_CLIENT: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANCre(getChamp(4));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibCrediteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(54));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));

                    }
                    ;
                    break;
                    case StaticValues.VIR_CLIENT_SICA3: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));

                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));

                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANCre(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibCrediteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(62));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));

                    }
                    ;
                    break;
                    case StaticValues.VIR_BANQUE: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdBanCre(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeCre(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(200));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;
                    case StaticValues.VIR_DISPOSITION: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdBanCre(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeCre(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(76));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;
                    case StaticValues.VIR_DISPOSITION_SICA3: {

                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdBanCre(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeCre(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(81));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;
                    case StaticValues.PRELEVEMENT: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANCre(getChamp(4));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibCrediteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(141));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;
                    case StaticValues.PRELEVEMENT_SICA3: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));

                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibCrediteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));

                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRefEmetteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(149));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;
                    case StaticValues.CHQ_SCAN:
                        ;
                    case StaticValues.CHQ_PAP: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANCre(getChamp(4));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumCheque(getChamp(7));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdBanDeb(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeDeb(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumCptDeb(getChamp(12));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCleRibDeb(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeCertif(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(286));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;
                    case StaticValues.CHQ_SCAN_SICA3: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumCheque(getChamp(7));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdBanDeb(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeDeb(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumCptDeb(getChamp(12));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCleRibDeb(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));

                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeCertif(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(241));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;
                    case StaticValues.BLT_ORD_SCAN:
                        ;
                    case StaticValues.BLT_ORD_PAP: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateEcheance(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeFrais(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontantFrais(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontantBrut(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomSouscripteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrSouscripteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeAcceptation(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRefSouscripteur(getChamp(12));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeEndossement(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumCedant(getChamp(7));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeAval(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(85));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;
                    case StaticValues.BLT_ORD_SCAN_SICA3: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateEcheance(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));

                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeFrais(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontantFrais(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontantBrut(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomSouscripteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrSouscripteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeAcceptation(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRefSouscripteur(getChamp(12));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeEndossement(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setProtet(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeAval(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(96));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;
                    case StaticValues.LTR_CHG_SCAN:
                        ;
                    case StaticValues.LTR_CHG_PAP: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateEcheance(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeFrais(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontantFrais(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontantBrut(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeAcceptation(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRefSouscripteur(getChamp(12));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeEndossement(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumCedant(getChamp(7));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeAval(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(85));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    break;
                    case StaticValues.LTR_CHG_SCAN_SICA3: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateEcheance(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));

                        enteteRemise.enteteLots[cptLot].operations[cptOper].setPfxIBANDeb(getChamp(4));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeFrais(getChamp(2));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontantFrais(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMontantBrut(getChamp(16));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeAcceptation(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRefSouscripteur(getChamp(12));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeEndossement(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setProtet(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeAval(getChamp(1));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(96));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    break;
                    case StaticValues.RETRAIT_CB:
                        ;
                        break;
                    case StaticValues.PAIEMENT_CB_TPE:
                        ;
                        break;
                    case StaticValues.PAIEMENT_CB_FER:
                        ;
                        break;
                    case StaticValues.REJ_PRELEVEMENT:
                    case StaticValues.REJ_PRELEVEMENT_SICA3:
                        ;
                    case StaticValues.REJ_CHQ_SCAN:
                    case StaticValues.REJ_CHQ_SCAN_SICA3:
                        ;
                    case StaticValues.REJ_CHQ_PAP:
                        ;
                    case StaticValues.REJ_LTR_CHG_PAP:
                        ;
                    case StaticValues.REJ_LTR_CHG_SCAN:
                    case StaticValues.REJ_LTR_CHG_SCAN_SICA3:
                        ;
                    case StaticValues.REJ_BLT_ORD_SCAN:
                    case StaticValues.REJ_BLT_ORD_SCAN_SICA3:
                        ;
                    case StaticValues.REJ_BLT_ORD_PAP:
                        ;
                    case StaticValues.REJ_CB_FER: {
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setRioOperInitial(new RIO(getChamp(35)));
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setMotifRejet(getChamp(3));
                        getChamp(7);
                        enteteRemise.enteteLots[cptLot].operations[cptOper].setCodeRejet(getChamp(3));
                    }
                    ;
                    break;

                }
            }


        }
        //System.out.println(enteteRemise.toString());
        CraUAPWriter craUAPWriter = new CraUAPWriter();


        //Recuperation de la remise ENVOYEE
        sql = "SELECT * FROM REMCOM WHERE IDEMETTEUR = '" + enteteRemise.getIdEmetteur() + "' AND REFREMISE = '" + enteteRemise.getRefRemRelatif() + "' AND DATEPRESENTATION =" + ResLoader.getMessages("pfxTimestamp") + Utility.convertDateToString(enteteRemise.getDatePresentation(), ResLoader.getMessages("patternTimestamp")) + ResLoader.getMessages("sfxTimestamp") + " AND (ETAT = " + Utility.getParam("CETAOPEALLICOM1ENV") + " OR ETAT = " + Utility.getParam("CETAOPEALLICOM2ENV") + " OR ETAT = " + Utility.getParam("CETAOPEALLICOM3ENV") + ")";
        Remcom remcom[] = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());

        if (remcom != null && remcom.length > 0) {
            String type = remcom[0].getTyperemise().substring(remcom[0].getTyperemise().length() - 1);
            if (enteteRemise.getCodeRejet().equalsIgnoreCase("00")) {
                //Acceptation totale de la Remise
                updateRemise(remcom[0], Utility.getParam("CETAOPEALLICOM" + type + "ENV"), Utility.getParam("CETAOPEALLICOM" + type + "ACC"), "00");
            } else if (enteteRemise.getCodeRejet().equalsIgnoreCase("99")) {
                //Rejet Partiel de la remise

                for (int i = 0; i < enteteRemise.enteteLots.length; i++) {
                    EnteteLot enteteLot1 = enteteRemise.enteteLots[i];
                    //Recuperation du lot de la remise 
                    sql = "SELECT * FROM LOTCOM WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM" + type + "ENV") + " AND IDREMCOM =" + remcom[0].getIdremcom() + " AND TYPEOPERATION ='" + enteteLot1.getTypeOperation() + "' AND REFLOT='" + enteteLot1.getRefLot() + "' AND IDBANREM ='" + enteteLot1.getIdBanRem() + "' ";
                    Lotcom lotcom[] = (Lotcom[]) db.retrieveRowAsObject(sql, new Lotcom());
                    if (lotcom != null && lotcom.length > 0) {
                        if (enteteLot1.getCodeRejet().equalsIgnoreCase("00")) {
                            //Accept Total du lot

                            sql = "UPDATE LOTCOM SET ETAT= " + Utility.getParam("CETAOPEALLICOM" + type + "ACC") + ",CODEREJET='" + "00" + "' WHERE IDLOTCOM = " + lotcom[0].getIdlotcom() + "";
                            db.executeUpdate(sql);
                            updateOperation(enteteLot1, Utility.getParam("CETAOPEALLICOM" + type + "ACC"), "00", lotcom[0].getIdlotcom(), "");

                        } else if (enteteLot1.getCodeRejet().equalsIgnoreCase("99")) {
                            //Rejet partiel du lot
                            sql = "UPDATE LOTCOM SET ETAT=" + Utility.getParam("CETAOPEALLICOM" + type + "REJ") + ",CODEREJET='" + "99" + "' WHERE IDLOTCOM = " + lotcom[0].getIdlotcom() + "";
                            db.executeUpdate(sql);

                            for (int j = 0; j < enteteLot1.operations.length; j++) {
                                Operation operation1 = enteteLot1.operations[j];
                                updateOperation(enteteLot1, Utility.getParam("CETAOPEALLICOM" + type + "REJ"), operation1.getCodeRejet(), lotcom[0].getIdlotcom(), operation1.getRefOperation());
                            }

                        } else {
                            //Rejet total du lot

                            sql = "UPDATE LOTCOM SET ETAT=" + Utility.getParam("CETAOPEALLICOM" + type + "REJ") + " WHERE IDLOTCOM = " + lotcom[0].getIdlotcom() + "";
                            db.executeUpdate(sql);
                            updateOperation(enteteLot1, Utility.getParam("CETAOPEALLICOM" + type + "REJ"), enteteLot1.getCodeRejet(), lotcom[0].getIdlotcom(), "");

                        }

                    }
                }
                sql = "UPDATE REMCOM SET ETAT=" + Utility.getParam("CETAOPEALLICOM" + type + "REJ") + ",CODEREJET='" + "99" + "'WHERE IDREMCOM =" + remcom[0].getIdremcom() + "";
                db.executeUpdate(sql);
                //Acceptation du reste de la remise
                updateRemise(remcom[0], Utility.getParam("CETAOPEALLICOM" + type + "ENV"), Utility.getParam("CETAOPEALLICOM" + type + "ACC"), "00");
            } else {
                //Rejet Total de la remise
                updateRemise(remcom[0], Utility.getParam("CETAOPEALLICOM" + type + "ENV"), Utility.getParam("CETAOPEALLICOM" + type + "REJ"), enteteRemise.getCodeRejet());
            }
            craUAPWriter.setRemcom(remcom[0]);
            craUAPWriter.execute();

        } else {
            System.out.println("Remise Introuvable = " + enteteRemise.toString());
            logEvent("ERREUR", "Remise Introuvable = " + enteteRemise.toString());
        }

        db.close();

        return aFile;
    }

    private void updateOperation(EnteteLot enteteLot, String etat, String codeRejet, BigDecimal idlotcom, String refOperation) throws Exception {
        String sql = "";
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        switch (Integer.parseInt(enteteLot.getTypeOperation())) {
            case StaticValues.VIR_CLIENT:
            case StaticValues.VIR_CLIENT_SICA3:

            case StaticValues.VIR_BANQUE:

            case StaticValues.VIR_DISPOSITION:
            case StaticValues.VIR_DISPOSITION_SICA3: {
                sql = "UPDATE VIREMENTS SET ETAT=" + etat + ",MOTIFREJET='" + codeRejet + "'WHERE LOTCOM =" + idlotcom + (refOperation.equals("") ? "" : " AND IDVIREMENT = " + Long.parseLong(refOperation) + "");

                db.executeUpdate(sql);

            }
            break;
            case StaticValues.REJ_PRELEVEMENT:
                ;
            case StaticValues.REJ_PRELEVEMENT_SICA3: {
                sql = "UPDATE PRELEVEMENTS SET ETAT=" + etat + ",MOTIFREJET=" + (codeRejet.equals("00") ? "MOTIFREJET" : "'" + codeRejet + "'") + " WHERE LOTCOM =" + idlotcom + (refOperation.equals("") ? "" : " AND Reference_Operation_Interne LIKE '" + refOperation + "'");
                db.executeUpdate(sql);
            }
            break;
            case StaticValues.PRELEVEMENT: {
            }
            ;
            case StaticValues.PRELEVEMENT_SICA3: {
                sql = "UPDATE PRELEVEMENTS SET ETAT=" + etat + ",MOTIFREJET='" + codeRejet + "'WHERE LOTCOM =" + idlotcom + (refOperation.equals("") ? "" : " AND IDPRELEVEMENT LIKE '" + Long.parseLong(refOperation) + "'");
                db.executeUpdate(sql);
            }
            ;
            break;
            case StaticValues.CHQ_SCAN:
            case StaticValues.CHQ_SCAN_SICA3:
            case StaticValues.CHQ_PAP: {
                sql = "UPDATE CHEQUES SET ETAT=" + etat + ",MOTIFREJET='" + codeRejet + "'WHERE LOTCOM =" + idlotcom + (refOperation.equals("") ? "" : " AND IDCHEQUE LIKE '" + Long.parseLong(refOperation) + "'");
                db.executeUpdate(sql);

            }
            break;
            case StaticValues.REJ_CHQ_SCAN:
            case StaticValues.REJ_CHQ_SCAN_SICA3:
            case StaticValues.REJ_CHQ_PAP: {
                sql = "UPDATE CHEQUES SET ETAT=" + etat + ",MOTIFREJET=" + (codeRejet.equals("00") ? "MOTIFREJET" : "'" + codeRejet + "'") + " WHERE LOTCOM =" + idlotcom + (refOperation.equals("") ? "" : " AND Reference_Operation_Interne LIKE '" + refOperation + "'");
                db.executeUpdate(sql);
            }
            break;
            case StaticValues.REJ_LTR_CHG_PAP:
            case StaticValues.REJ_LTR_CHG_SCAN_SICA3:
                ;
            case StaticValues.REJ_LTR_CHG_SCAN:
                ;
            case StaticValues.REJ_BLT_ORD_SCAN:
            case StaticValues.REJ_BLT_ORD_SCAN_SICA3:
                ;
            case StaticValues.REJ_BLT_ORD_PAP: {
                sql = "UPDATE EFFETS SET ETAT=" + etat + ",MOTIFREJET=" + (codeRejet.equals("00") ? "MOTIFREJET" : "'" + codeRejet + "'") + " WHERE LOTCOM =" + idlotcom + (refOperation.equals("") ? "" : " AND Reference_Operation_rejet LIKE '" + refOperation + "'");
                db.executeUpdate(sql);

            }
            break;
            case StaticValues.BLT_ORD_SCAN:
            case StaticValues.BLT_ORD_SCAN_SICA3:
                ;
            case StaticValues.BLT_ORD_PAP:

            case StaticValues.LTR_CHG_SCAN:
            case StaticValues.LTR_CHG_SCAN_SICA3:
                ;
            case StaticValues.LTR_CHG_PAP: {
                sql = "UPDATE EFFETS SET ETAT=" + etat + ",MOTIFREJET='" + codeRejet + "'WHERE LOTCOM =" + idlotcom + (refOperation.equals("") ? "" : " AND IDEFFET LIKE '" + Long.parseLong(refOperation) + "'");
                db.executeUpdate(sql);

            }
            ;
            break;


        }
        db.close();
    }

    private void updateRemise(Remcom remcom, String etatAvant, String etat, String codeRejet) throws Exception {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String sql = "UPDATE CHEQUES SET ETAT=" + etat + " WHERE ETAT =" + etatAvant + " AND REMCOM =" + remcom.getIdremcom();
        db.executeUpdate(sql);
        sql = "UPDATE VIREMENTS SET ETAT=" + etat + " WHERE ETAT =" + etatAvant + " AND REMCOM =" + remcom.getIdremcom() + "";
        db.executeUpdate(sql);
        sql = "UPDATE EFFETS SET ETAT=" + etat + " WHERE ETAT =" + etatAvant + " AND REMCOM =" + remcom.getIdremcom() + "";
        db.executeUpdate(sql);
        sql = "UPDATE LOTCOM SET ETAT=" + etat + ",CODEREJET='" + codeRejet + "'WHERE ETAT =" + etatAvant + " AND IDREMCOM =" + remcom.getIdremcom() + "";
        db.executeUpdate(sql);
        sql = "UPDATE REMCOM SET ETAT=" + etat + ",CODEREJET='" + codeRejet + "'WHERE ETAT =" + etatAvant + " AND IDREMCOM =" + remcom.getIdremcom() + "";
        db.executeUpdate(sql);
        sql = "UPDATE PRELEVEMENTS SET ETAT=" + etat + " WHERE ETAT =" + etatAvant + " AND REMCOM =" + remcom.getIdremcom() + "";
        db.executeUpdate(sql);
        db.close();
    }
}
