/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.sica;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.EnteteRemise;
import clearing.model.Operation;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Lotcom;
import clearing.table.Remcom;
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
public class SmartOutgoReader extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;


        BufferedReader is = openFile(aFile);
        EnteteRemise enteteRemise = new EnteteRemise();


        int cptLot = -1;
        int cptOper = -1;
        MD5 md5 = new MD5();
        while ((line = is.readLine()) != null) {

            setCurrentLine(line);
            if (line.startsWith("ERET")) {
                md5.update(getCurrentLine());
                enteteRemise.setIdEntete(getChamp(4));

                enteteRemise.setIdEmetteur(getChamp(5));
                enteteRemise.setRefRemise(getChamp(3));
                enteteRemise.setDatePresentation(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                enteteRemise.setIdRecepeteur(getChamp(5));
                enteteRemise.setDevise(getChamp(3));
                enteteRemise.setTypeRemise(getChamp(5));
                getChamp(3);
                enteteRemise.setNbLots(getChamp(3));
                getChamp(25);
                enteteRemise.enteteLots = new EnteteLot[Integer.parseInt(enteteRemise.getNbLots())];
            } else if (line.startsWith("ELOT")) {
                md5.update(getCurrentLine());
                cptOper = -1;
                enteteRemise.enteteLots[++cptLot] = new EnteteLot();
                enteteRemise.enteteLots[cptLot].setIdEntete(getChamp(4));

                enteteRemise.enteteLots[cptLot].setRefLot(getChamp(3));
                enteteRemise.enteteLots[cptLot].setRefBancaire(getChamp(5));
                enteteRemise.enteteLots[cptLot].setTypeOperation(getChamp(3));
                enteteRemise.enteteLots[cptLot].setIdBanRem(getChamp(5));
                enteteRemise.enteteLots[cptLot].setNbOperations(getChamp(4));
                enteteRemise.enteteLots[cptLot].setMontantTotal(getChamp(16));
                enteteRemise.enteteLots[cptLot].setBlancs(getChamp(24));
                enteteRemise.enteteLots[cptLot].operations = new Operation[Integer.parseInt(enteteRemise.enteteLots[cptLot].getNbOperations())];
            } else if (line.startsWith("FRET")) {
                //md5.update("FRET");
                // System.out.println("Checksum = " + md5.digest());
            } else /* Lecture ordre*/ {
                //Commun
                //md5.update(getCurrentLine());
                enteteRemise.enteteLots[cptLot].operations[++cptOper] = new Operation();
                enteteRemise.enteteLots[cptLot].operations[cptOper].setRio(new RIO(getChamp(35)));
                enteteRemise.enteteLots[cptLot].operations[cptOper].setTypeOperation(getChamp(3));
                enteteRemise.enteteLots[cptLot].operations[cptOper].setRefOperation(getChamp(8));

                //Particulier
                switch (Integer.parseInt(enteteRemise.enteteLots[cptLot].getTypeOperation())) {
                    case StaticValues.VIR_CLIENT:
                         {
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
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(57));

                        }

                        break;

                    case StaticValues.VIR_CLIENT_SICA3:
                         {
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANDeb(getChamp(1));

                            enteteRemise.enteteLots[cptLot].operations[cptOper].setRibDebiteur(getChamp(24));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setFlagIBANCre(getChamp(1));

                            enteteRemise.enteteLots[cptLot].operations[cptOper].setRibCrediteur(getChamp(24));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setMontant(getChamp(16));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setNomDebiteur(getChamp(35));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrDebiteur(getChamp(50));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setNomCrediteur(getChamp(35));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setAdrCrediteur(getChamp(50));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setLibelle(getChamp(70));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setBlancs(getChamp(65));

                        }

                        break;
                    case StaticValues.VIR_BANQUE:
                         {
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
                        }

                        break;
                    case StaticValues.VIR_DISPOSITION:
                         {
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
                        }

                        break;
                    case StaticValues.VIR_DISPOSITION_SICA3:
                         {
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
                        }

                        break;
                    case StaticValues.PRELEVEMENT:
                         {
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
                        }

                        break;
                    case StaticValues.PRELEVEMENT_SICA3:
                         {
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
                        }

                        break;
                    case StaticValues.CHQ_SCAN:

                    case StaticValues.CHQ_PAP:
                         {
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
                        }

                        break;
                    case StaticValues.CHQ_SCAN_SICA3:
                         {
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
                        }

                        break;

                    case StaticValues.BLT_ORD_SCAN:

                    case StaticValues.BLT_ORD_PAP:
                         {
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
                        }

                        break;

                    case StaticValues.BLT_ORD_SCAN_SICA3:
                         {
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
                        }

                        break;
                    case StaticValues.LTR_CHG_SCAN:

                    case StaticValues.LTR_CHG_PAP:
                         {
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
                        }
                        break;

                    case StaticValues.LTR_CHG_SCAN_SICA3:
                         {
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setIPac(getChamp(2));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setNumIntOrdre(getChamp(10));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setDateEcheance(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));

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
                    case StaticValues.REJ_CB_FER:
                         {
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setIdAgeRem(getChamp(5));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setRioOperInitial(new RIO(getChamp(35)));
                            enteteRemise.enteteLots[cptLot].operations[cptOper].setMotifRejet(getChamp(3));

                        }

                        break;

                }
            }


        }
        // System.out.println(enteteRemise.toString());

        Remcom remcom = CMPUtility.insertRemcom(enteteRemise, Integer.parseInt(Utility.getParam("CETAOPERET")));

        if (cptLot != enteteRemise.enteteLots.length) {
            System.out.println("WARNING: Nbre de Lot réel différent du nbr de lot marqué dans l'entÃªte de remise.");
        }
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        for (int i = 0; i < enteteRemise.enteteLots.length; i++) {
            EnteteLot enteteLot1 = enteteRemise.enteteLots[i];
            Lotcom lotcom = CMPUtility.insertLotcom(enteteLot1, remcom);
            //       System.out.println(enteteLot1.toString());

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            for (int j = 0; j < enteteLot1.operations.length; j++) {
                Operation operation1 = enteteLot1.operations[j];
                Cheques cheque = new Cheques();
                Virements virement = new Virements();
                Effets effet = new Effets();

                effet.setRio(operation1.getRio().getRio());
                effet.setType_Effet(operation1.getTypeOperation());
                effet.setReference_Operation_Rejet(operation1.getRefOperation());
                effet.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPERET"))));
                effet.setLotcom(lotcom.getIdlotcom());
                effet.setRemcom(remcom.getIdremcom());
                effet.setDatecompensation(Utility.convertDateToString(new Date(remcom.getDatepresentation().getTime()), ResLoader.getMessages("patternDate")));
                effet.setDevise(remcom.getDevise());
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    effet.setEtablissement(CMPUtility.getCodeBanque());
                } else {
                    effet.setEtablissement(CMPUtility.getCodeBanqueSica3());
                }

                effet.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                effet.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HHmmss"));

                virement.setRio(operation1.getRio().getRio());
                virement.setType_Virement(operation1.getTypeOperation());
                virement.setReference_Operation_Interne(operation1.getRefOperation());
                virement.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPERET"))));
                virement.setLotcom(lotcom.getIdlotcom());
                virement.setRemcom(remcom.getIdremcom());
                virement.setDatecompensation(Utility.convertDateToString(new Date(remcom.getDatepresentation().getTime()), ResLoader.getMessages("patternDate")));
                virement.setDevise(remcom.getDevise());
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    virement.setEtablissement(CMPUtility.getCodeBanque());
                } else {
                    virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
                }
                virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                virement.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HHmmss"));

                cheque.setRio(operation1.getRio().getRio());
                cheque.setType_Cheque(operation1.getTypeOperation());
                cheque.setReference_Operation_Interne(operation1.getRefOperation());
                cheque.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPERET"))));
                cheque.setLotcom(lotcom.getIdlotcom());
                cheque.setRemcom(remcom.getIdremcom());
                cheque.setDatecompensation(Utility.convertDateToString(new Date(remcom.getDatepresentation().getTime()), ResLoader.getMessages("patternDate")));
                cheque.setDevise(remcom.getDevise());
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    cheque.setEtablissement(CMPUtility.getCodeBanque());
                } else {
                    cheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                }

                switch (Integer.parseInt(enteteLot1.getTypeOperation())) {
                    case StaticValues.VIR_CLIENT:
                         {
                            virement.setIban_Tire(operation1.getPfxIBANDeb());
                            virement.setBanqueremettant(operation1.getRio().getIdEmetteur());
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
                            db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                        }

                        break;
                    case StaticValues.VIR_CLIENT_SICA3:
                         {

                            virement.setBanqueremettant(operation1.getRio().getIdEmetteur());
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
                            db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                        }

                        break;
                    case StaticValues.VIR_BANQUE:
                         {
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
                            virement.setBanqueremettant(operation1.getRio().getIdEmetteur());
                            virement.setNumerovirement(operation1.getRefEmetteur());
                            db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                        }
                        ;
                        break;
                    case StaticValues.VIR_DISPOSITION:
                         {
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
                            db.insertObjectAsRowByQuery(virement, "VIREMENTS");

                        }

                        break;
                    case StaticValues.VIR_DISPOSITION_SICA3:
                         {

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
                            db.insertObjectAsRowByQuery(virement, "VIREMENTS");

                        }

                        break;
                    case StaticValues.PRELEVEMENT:
                        ;
                        break;
                    case StaticValues.CHQ_SCAN:
                    case StaticValues.CHQ_PAP:
                         {

                            cheque.setBanqueremettant(operation1.getRio().getIdEmetteur());
                            cheque.setRemcom(remcom.getIdremcom());
                            cheque.setDatecompensation(Utility.convertDateToString(new Date(remcom.getDatepresentation().getTime()), ResLoader.getMessages("patternDate")));
                            cheque.setDateemission(Utility.convertDateToString(operation1.getRio().getDatePresentation(), ResLoader.getMessages("patternDate")));
                            cheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                            cheque.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternTime")));
                            cheque.setLotcom(lotcom.getIdlotcom());
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

                            String sql = "SELECT * FROM ALL_CHEQUES WHERE BANQUE ='" + cheque.getBanque() +
                                    "' AND NUMEROCHEQUE ='" + cheque.getNumerocheque() +
                                    "' AND NUMEROCOMPTE ='" + cheque.getNumerocompte() +
                                    "'";
                            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                            boolean reconciliation = false;
                            boolean doublon = false;
                            if (cheques != null && cheques.length > 0) {
                                for (int k = 0; k < cheques.length; k++) {
                                    if (!reconciliation && cheques[k].getMontantcheque().equalsIgnoreCase(cheque.getMontantcheque()) &&
                                            cheques[k].getEtat().equals(new BigDecimal(Utility.getParam("CETAOPERETIMA")))) {

                                        cheque.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPERETREC"))));
                                        sql = " IDCHEQUE =" + cheques[k].getIdcheque();
                                        reconciliation = db.updateRowByObjectByQuery(cheque, "CHEQUES", sql);
                                        continue;
                                    }
                                    cheque.setCoderepresentation(new BigDecimal("1"));
                                    cheque.setIdcheque(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDCHEQUE", "CHEQUES"))));
                                    db.insertObjectAsRowByQuery(cheque, "CHEQUES");
                                    doublon = true;
                                }
//                                if(doublon){
//                                    for (int k = 0; k < cheques.length; k++) {
//                                    cheques[k].setCoderepresentation(new BigDecimal("1"));
//                                    db.updateRowByObjectByQuery(cheques[k], "CHEQUES", " IDCHEQUE =" + cheques[k].getIdcheque());
//                                    }
//                                }

                            } else {
                                cheque.setIdcheque(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDCHEQUE", "CHEQUES"))));
                                db.insertObjectAsRowByQuery(cheque, "CHEQUES");
                            }



                        }

                        break;
                    case StaticValues.CHQ_SCAN_SICA3:
                         {

                            cheque.setBanqueremettant(operation1.getRio().getIdEmetteur());
                            cheque.setRemcom(remcom.getIdremcom());
                            cheque.setDatecompensation(Utility.convertDateToString(new Date(remcom.getDatepresentation().getTime()), ResLoader.getMessages("patternDate")));
                            cheque.setDateemission(Utility.convertDateToString(operation1.getRio().getDatePresentation(), ResLoader.getMessages("patternDate")));
                            cheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                            cheque.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternTime")));
                            cheque.setLotcom(lotcom.getIdlotcom());
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
                            cheque.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternTime")));

                             String sql = "SELECT * FROM ALL_CHEQUES WHERE BANQUE ='" + cheque.getBanque() +
                                    "' AND NUMEROCHEQUE ='" + cheque.getNumerocheque() +
                                    "' AND NUMEROCOMPTE ='" + cheque.getNumerocompte() +
                                    "'";
                            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                            boolean reconciliation = false;
                            if (cheques != null && cheques.length > 0) {
                                for (int k = 0; k < cheques.length; k++) {
                                    if (!reconciliation && cheques[k].getMontantcheque().equalsIgnoreCase(cheque.getMontantcheque()) &&
                                            cheques[k].getEtat().equals(new BigDecimal(Utility.getParam("CETAOPERETIMA")))) {

                                        cheque.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPERETREC"))));
                                        sql = " IDCHEQUE =" + cheques[k].getIdcheque();
                                        reconciliation = db.updateRowByObjectByQuery(cheque, "CHEQUES", sql);
                                        continue;
                                    }
                                    cheque.setCoderepresentation(new BigDecimal("1"));
                                    cheque.setIdcheque(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDCHEQUE", "CHEQUES"))));
                                    db.insertObjectAsRowByQuery(cheque, "CHEQUES");
                                }

                            } else {
                                cheque.setIdcheque(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDCHEQUE", "CHEQUES"))));
                                db.insertObjectAsRowByQuery(cheque, "CHEQUES");
                            }

                        }

                        break;
                    case StaticValues.BLT_ORD_SCAN:
                        ;
                    case StaticValues.BLT_ORD_PAP:
                         {
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
                            db.insertObjectAsRowByQuery(effet, "EFFETS");

                        }

                        break;
                    case StaticValues.BLT_ORD_SCAN_SICA3:
                         {


                            effet.setAgenceremettant(operation1.getIdAgeRem());
                            effet.setVille(operation1.getIPac());
                            effet.setNumeroeffet(operation1.getNumIntOrdre());
                            effet.setDate_Echeance(Utility.convertDateToString(operation1.getDateEcheance(), ResLoader.getMessages("patternDate")));
                            effet.setBanqueremettant(operation1.getRio().getIdEmetteur());
                            effet.setBanque(enteteRemise.getIdRecepeteur());
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
                            String sql = "SELECT * FROM EFFETS WHERE BANQUE ='" + effet.getBanque() +
                                    "' AND NUMEROCHEQUE ='" + effet.getNumeroeffet() +
                                    "' AND NUMEROCOMPTE ='" + effet.getNumerocompte_Beneficiaire() +
                                    "' AND MONTANT_EFFET ='" + effet.getMontant_Effet() + "' AND ETAT =" + Utility.getParam("CETAOPERETIMA") +
                                    "";
                            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

                            if (effets != null && effets.length > 0) {
                                effet.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPERETREC"))));

                                sql = " IDEFFET =" + effets[0].getIdeffet();

                                db.updateRowByObjectByQuery(effet, "EFFETS", sql);
                            } else {
                                effet.setIdeffet(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDEFFET", "EFFETS"))));
                                db.insertObjectAsRowByQuery(effet, "EFFETS");
                            }


                        }

                        break;
                    case StaticValues.LTR_CHG_SCAN:
                    case StaticValues.LTR_CHG_PAP:
                         {
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
                            db.insertObjectAsRowByQuery(effet, "EFFETS");

                        }
                        break;
                    case StaticValues.LTR_CHG_SCAN_SICA3:
                         {
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
                            effet.setNom_Tire(operation1.getNomDebiteur());
                            effet.setAdresse_Tire(operation1.getAdrDebiteur());
                            effet.setNom_Beneficiaire(operation1.getNomCrediteur());
                            effet.setAdresse_Beneficiaire(operation1.getAdrCrediteur());
                            effet.setDate_Creation(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                            effet.setCode_Acceptation(operation1.getCodeAcceptation());
                            effet.setIdentification_Tire(operation1.getRefSouscripteur());
                            effet.setCode_Endossement(operation1.getCodeEndossement());
                            effet.setProtet(operation1.getProtet());
                            effet.setCode_Aval(operation1.getCodeAval());
                            String sql = "SELECT * FROM EFFETS WHERE BANQUE ='" + effet.getBanque() +
                                    "' AND NUMEROCHEQUE ='" + effet.getNumeroeffet() +
                                    "' AND NUMEROCOMPTE ='" + effet.getNumerocompte_Beneficiaire() +
                                    "' AND MONTANT_EFFET ='" + effet.getMontant_Effet() + "' AND ETAT =" + Utility.getParam("CETAOPERETIMA") +
                                    "";
                            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

                            if (effets != null && effets.length > 0) {
                                effet.setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPERETREC"))));

                                sql = " IDEFFET =" + effets[0].getIdeffet();

                                db.updateRowByObjectByQuery(effet, "EFFETS", sql);
                            } else {
                                effet.setIdeffet(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDEFFET", "EFFETS"))));
                                db.insertObjectAsRowByQuery(effet, "EFFETS");
                            }

                        }
                        break;
                    case StaticValues.REJ_PRELEVEMENT:
                        break;
                    case StaticValues.REJ_CHQ_SCAN:
                    case StaticValues.REJ_CHQ_SCAN_SICA3:
                    case StaticValues.REJ_CHQ_PAP:
                         {
                            String sql = "SELECT * FROM CHEQUES WHERE RIO ='" + operation1.getRioOperInitial().getRio() + "'";
                            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                            if (cheques != null && cheques.length > 0) {
                                cheques[0].setReference_Operation_Rejet(operation1.getRefOperation());
                                cheques[0].setRio_Rejet(operation1.getRio().getRio());
                                cheques[0].setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEREJRET"))));
                                cheques[0].setMotifrejet(operation1.getMotifRejet());
                                cheques[0].setRemcom(remcom.getIdremcom());
                                sql = " RIO ='" + operation1.getRioOperInitial().getRio() + "'";
                                db.updateRowByObjectByQuery(cheques[0], "CHEQUES", sql);
                            } else {
                                System.out.println("Ordre a rejeter introuvable : " + operation1.toString());
                                logEvent("WARNING", "Ordre a rejeter introuvable : " + operation1.getRioOperInitial().getRio());
                            }

                        }
                        break;
                    case StaticValues.REJ_BLT_ORD_SCAN:
                    case StaticValues.REJ_BLT_ORD_SCAN_SICA3:
                    case StaticValues.REJ_BLT_ORD_PAP:
                    case StaticValues.REJ_LTR_CHG_SCAN_SICA3:
                    case StaticValues.REJ_LTR_CHG_PAP:
                    case StaticValues.REJ_LTR_CHG_SCAN:
                         {
                            //    System.out.println(operation1.toString());
                            String sql = "SELECT * FROM EFFETS WHERE RIO ='" + operation1.getRioOperInitial().getRio() + "'";
                            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

                            if (effets != null && effets.length > 0) {
                                sql = "UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEREJRET") + ",REMCOM=" + remcom.getIdremcom() + ",RIO_REJET ='" + operation1.getRio().getRio() + "',REFERENCE_OPERATION_REJET =" + operation1.getRefOperation() + " ,MOTIFREJET='" + operation1.getMotifRejet() + "'WHERE RIO ='" + operation1.getRioOperInitial().getRio() + "'";
                                db.executeUpdate(sql);
                            } else {
                                //    System.out.println("Ordre a rejeter introuvable : " + operation1.toString());
                                logEvent("WARNING", "Ordre a rejeter introuvable : " + operation1.getRioOperInitial().getRio());
                            }
                        }
                        break;


                }

            // System.out.println(operation1.toString());

            }
            db.close();
        }





        return aFile;
    }
}

    
