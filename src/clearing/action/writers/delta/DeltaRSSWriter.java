/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.Operation;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Lotcom;
import clearing.table.Remcom;
import clearing.table.Virements;
import clearing.utils.StaticValues;
import java.io.File;
import java.sql.SQLException;
import java.util.Vector;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class DeltaRSSWriter extends FlatFileWriter {

    private String sql = "";
    private int refLot = 0;
    private Vector<EnteteLot> vEnteteLots = new Vector<EnteteLot>();
    
    
    Cheques cheques[] = null;
    Cheques cheques35[] = null;
    Virements virements10[] = null;
    Virements virements15[] = null;
    Virements virements17[] = null;
    Virements virements11[] = null;
    Virements virements12[] = null;
    Effets effets40[] = null;
    Effets effets41[] = null;
    Effets effets42[] = null;
    Effets effets43[] = null;
    Effets effets45[] = null;
    Effets effets46[] = null;
    Remcom remcom = null;
    Lotcom lotcom = null;
    private boolean remiseHasRejetsCheques = false;
    private boolean remiseHasRejetsEffets = false;
    private boolean remiseHasCheques = false;
    private boolean remiseHasEffets = false;
    private boolean remiseHasVirements = false;

    public DeltaRSSWriter() {
        setDescription("Envoi de Lot d'opération rejet aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String  whereClause = " ETAT IN (" +  Utility.getParam("CETAOPEALLICOM2") +  ") ";
         //Recuperation des rejets cheques 130
                sql = "SELECT * FROM CHEQUES WHERE "+ whereClause + " AND TYPE_CHEQUE ='030'" ;
                cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                prepareRejetCheques(cheques);


                //Recuperation des cheques 135
                sql = "SELECT * FROM CHEQUES WHERE "+ whereClause + " AND TYPE_CHEQUE ='035'";
                cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                prepareRejetCheques(cheques35);
//        //Recuperation des effets d'une Banque
//        sql = "SELECT * FROM EFFETS WHERE   "+ whereClause + " AND TYPE_EFFET ='040' ";
//        effets40 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
//        prepareRejetEffets(effets40);
//
//        //Recuperation des effets d'une Banque
//        sql = "SELECT * FROM EFFETS WHERE  "+ whereClause + " AND TYPE_EFFET ='045'";
//        effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
//        prepareRejetEffets(effets45);
//
//        //Recuperation des effets d'une Banque
//        sql = "SELECT * FROM EFFETS WHERE  "+ whereClause + " AND TYPE_EFFET ='041' ";
//        effets41 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
//        prepareRejetEffets(effets41);
//
//        //Recuperation des effets d'une Banque
//        sql = "SELECT * FROM EFFETS WHERE  "+ whereClause + " AND TYPE_EFFET ='046'";
//        effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
//        prepareRejetEffets(effets46);
//
//        //Recuperation des effets d'une Banque
//        sql = "SELECT * FROM EFFETS  "+ whereClause + " AND TYPE_EFFET ='042' ";
//        effets42 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
//        prepareRejetEffets(effets42);
//
//        //Recuperation des effets d'une Banque
//        sql = "SELECT * FROM EFFETS WHERE  "+ whereClause + " AND TYPE_EFFET ='043' ";
//        effets43 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
//        prepareRejetEffets(effets43);
//

        if (refLot > 0) {
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques35.length + " - Montant Total= " + Utility.formatNumber(vEnteteLots.get(0).getMontantTotal()));
            logEvent("INFO", "Nombre de Chèque= " + cheques35.length + " - Montant Total= " + Utility.formatNumber("" + vEnteteLots.get(0).getMontantTotal()));

            updateEtatOperations(db);
        } else {
            logEvent("WARNING", "Il n'y a aucun élément disponible");
        }

        db.close();


    }

  


private void prepareRejetCheques(Cheques[] cheques) throws  Exception {

        if (cheques != null && cheques.length > 0) {
            remiseHasRejetsCheques = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ERRS");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(cheques[0].getType_Cheque().replaceFirst("0", "1"));
            enteteLot.setIdBanRem(cheques[0].getBanque());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + cheques.length);
            Operation[] operations = new Operation[cheques.length];

            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];
                operations[j] = new Operation();

                
                operations[j].setTypeOperation(cheques[0].getType_Cheque().replaceFirst("0", "1"));
                operations[j].setRefOperation(cheque.getReference_Operation_Interne().trim());
                operations[j].setIdAgeRem(cheque.getAgenceremettant());
                operations[j].setRioOperInitial(new RIO(cheque.getRio()));
                operations[j].setMotifRejet(cheque.getMotifrejet());
                operations[j].setBlancs(createBlancs(10, " "));
                operations[j].setIdObjetOrigine(cheque.getIdcheque());
            }

            enteteLot.setMontantTotal(createBlancs(16, " "));
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
            printLotRejets(enteteLot);
        }
    }

    private void prepareRejetEffets(Effets[] effets) throws  Exception {
        if (effets != null && effets.length > 0) {
            remiseHasRejetsEffets = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ERRS");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(effets[0].getType_Effet().replaceFirst("0", "1"));
            enteteLot.setIdBanRem(effets[0].getBanque());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + effets.length);
            Operation[] operations = new Operation[effets.length];

            for (int j = 0; j < effets.length; j++) {
                Effets effet = effets[j];
                operations[j] = new Operation();

               // operations[j].setRio(new RIO(effet.getRio_Rejet()));
                operations[j].setTypeOperation(effets[0].getType_Effet().replaceFirst("0", "1"));
                operations[j].setRefOperation(effet.getReference_Operation_Rejet().trim());
                operations[j].setIdAgeRem(effet.getAgenceremettant());

                operations[j].setRioOperInitial(new RIO(effet.getRio()));
                operations[j].setMotifRejet(effet.getMotifrejet());
                operations[j].setBlancs(createBlancs(10, " "));
                operations[j].setIdObjetOrigine(effet.getIdeffet());
            }

            enteteLot.setMontantTotal(createBlancs(16, " "));
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
            printLotRejets(enteteLot);
        }
    }


    private String printLotRejets( EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + enteteLot.getIdBanRem() + ".000." + enteteLot.getRefLot() + "."+ enteteLot.operations[0].getTypeOperation() + "."+ CMPUtility.getDevise() + ".RRS";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {

        line = new String(enteteLot.operations[j].getTypeOperation() +
                enteteLot.operations[j].getRefOperation() +
                enteteLot.operations[j].getIdAgeRem() +
                enteteLot.operations[j].getRioOperInitial() +
                enteteLot.operations[j].getMotifRejet() +
                enteteLot.operations[j].getBlancs());
        writeln(line);
        }
        closeFile();



        return line;
    }






    public void printLotVirement(Virements[] virements, EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + virements[0].getBanque() + ".000." + enteteLot.getRefLot() + "."+ virements[0].getType_Virement() + "."+ CMPUtility.getDevise() + ".CRO."+CMPUtility.getDate()+".done";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                  case StaticValues.VIR_CLIENT:
                         {
                            line = printVirements10(enteteLot, j);
                            writeln(line);

                        }
                        ;
                        break;
                    case StaticValues.VIR_CLIENT_SICA3:
                         {
                            line = printVirements15(enteteLot, j);
                            writeln(line);

                        }
                        ;
                        break;
                    case StaticValues.VIR_BANQUE:
                         {
                            line = printVirements11(enteteLot, j);
                            writeln(line);

                        }
                        ;
                        break;
                    case StaticValues.VIR_DISPOSITION:
                         {
                            line = printVirements12(enteteLot, j);
                            writeln(line);

                        }
                        ;
                        break;
                    case StaticValues.VIR_DISPOSITION_SICA3:
                         {
                            line = printVirements17(enteteLot, j);
                            writeln(line);

                        }
                        ;
                        break;


            }
        }
        closeFile();
    }
    public void printLotCheque(Cheques[] cheques, EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + cheques[0].getBanque() + ".000." + enteteLot.getRefLot() + "."+ cheques[0].getType_Cheque() + "."+ CMPUtility.getDevise() + ".CRO."+CMPUtility.getDate()+".done";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                case StaticValues.CHQ_SCAN:
                    ;
                case StaticValues.CHQ_PAP:
                    {
                        line = printCheques(enteteLot, j);
                        writeln(line);
                    }
                    ;
                    break;
                case StaticValues.CHQ_SCAN_SICA3:
                    {
                        line = printCheques35(enteteLot, j);
                        writeln(line);
                    }
                    ;
                    break;
            }
        }
        closeFile();
    }
    public void printLotEffet(Effets[] effets, EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + effets[0].getBanque() + ".000." + enteteLot.getRefLot() + "."+ effets[0].getType_Effet() + "."+ CMPUtility.getDevise() + ".CRO."+CMPUtility.getDate()+".done";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                 case StaticValues.BLT_ORD_SCAN:
                        ;
                    case StaticValues.BLT_ORD_PAP:
                         {
                            line = printEffets4042(enteteLot, j);
                            writeln(line);

                        }
                        ;
                        break;
                    case StaticValues.BLT_ORD_SCAN_SICA3:
                         {
                            line = printEffets45(enteteLot, j);
                            writeln(line);

                        }
                        ;
                        break;
                    case StaticValues.LTR_CHG_SCAN:
                        ;
                    case StaticValues.LTR_CHG_PAP:
                         {
                            line = printEffets4143(enteteLot, j);
                            writeln(line);

                        }
                        ;
                        break;
                    case StaticValues.LTR_CHG_SCAN_SICA3:
                         {
                            line = printEffets46(enteteLot, j);
                            writeln(line);

                        }
                        ;
                        break;

            }
        }
        closeFile();
    }


    private String printCheques(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() + enteteLot.operations[j].getIPac() + enteteLot.operations[j].getFlagIBANCre() + enteteLot.operations[j].getPfxIBANCre() + enteteLot.operations[j].getNumCheque() + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") + enteteLot.operations[j].getIdBanDeb() + enteteLot.operations[j].getIdAgeDeb() + enteteLot.operations[j].getNumCptDeb() + enteteLot.operations[j].getCleRibDeb() + enteteLot.operations[j].getMontant() + enteteLot.operations[j].getNomCrediteur() + enteteLot.operations[j].getCodeCertif() + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printCheques35(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() + enteteLot.operations[j].getIPac() + enteteLot.operations[j].getNomCrediteur() + enteteLot.operations[j].getAdrCrediteur() + enteteLot.operations[j].getNumCheque() + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") + enteteLot.operations[j].getIdBanDeb() + enteteLot.operations[j].getIdAgeDeb() + enteteLot.operations[j].getNumCptDeb() + enteteLot.operations[j].getCleRibDeb() + enteteLot.operations[j].getMontant() + enteteLot.operations[j].getNomCrediteur() + enteteLot.operations[j].getCodeCertif() + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printVirements10(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() +
                enteteLot.operations[j].getRefOperation() +
                enteteLot.operations[j].getFlagIBANDeb() +
                enteteLot.operations[j].getPfxIBANDeb() +
                enteteLot.operations[j].getRibDebiteur() +
                enteteLot.operations[j].getFlagIBANCre() +
                enteteLot.operations[j].getPfxIBANCre() +
                enteteLot.operations[j].getRibCrediteur() +
                enteteLot.operations[j].getMontant() +
                enteteLot.operations[j].getNomDebiteur() +
                enteteLot.operations[j].getAdrDebiteur() +
                enteteLot.operations[j].getNomCrediteur() +
                enteteLot.operations[j].getAdrCrediteur() +
                enteteLot.operations[j].getNumIntOrdre() +
                Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
                enteteLot.operations[j].getLibelle() +
                enteteLot.operations[j].getBlancs());

        return line;
    }

    private String printVirements15(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() +
                enteteLot.operations[j].getRefOperation() +
                enteteLot.operations[j].getFlagIBANDeb() +
                enteteLot.operations[j].getRibDebiteur() +
                enteteLot.operations[j].getFlagIBANCre() +
                enteteLot.operations[j].getRibCrediteur() +
                enteteLot.operations[j].getMontant() +
                enteteLot.operations[j].getNomDebiteur() +
                enteteLot.operations[j].getAdrDebiteur() +
                enteteLot.operations[j].getNomCrediteur() +
                enteteLot.operations[j].getAdrCrediteur() +
                enteteLot.operations[j].getNumIntOrdre() +
                Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
                enteteLot.operations[j].getLibelle() +
                enteteLot.operations[j].getBlancs());

        return line;
    }

    private String printVirements11(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() +
                enteteLot.operations[j].getIdBanCre() +
                enteteLot.operations[j].getIdAgeCre() +
                enteteLot.operations[j].getMontant() +
                enteteLot.operations[j].getNomDebiteur() +
                enteteLot.operations[j].getNomCrediteur() +
                enteteLot.operations[j].getRefEmetteur() +
                Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
                enteteLot.operations[j].getLibelle() + enteteLot.operations[j].getBlancs());


        return line;
    }

    private String printVirements12(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getPfxIBANDeb() +
                enteteLot.operations[j].getRibDebiteur() +
                enteteLot.operations[j].getIdBanCre() +
                enteteLot.operations[j].getIdAgeCre() +
                enteteLot.operations[j].getMontant() +
                enteteLot.operations[j].getNomDebiteur() +
                enteteLot.operations[j].getAdrDebiteur() +
                enteteLot.operations[j].getNomCrediteur() +
                enteteLot.operations[j].getAdrCrediteur() +
                enteteLot.operations[j].getRefEmetteur() +
                Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
                enteteLot.operations[j].getLibelle() +
                enteteLot.operations[j].getBlancs());


        return line;
    }

    private String printVirements17(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() +
                enteteLot.operations[j].getRefOperation() +
                enteteLot.operations[j].getRibDebiteur() +
                enteteLot.operations[j].getIdBanCre() +
                enteteLot.operations[j].getIdAgeCre() +
                enteteLot.operations[j].getMontant() +
                enteteLot.operations[j].getNomDebiteur() +
                enteteLot.operations[j].getAdrDebiteur() +
                enteteLot.operations[j].getNomCrediteur() +
                enteteLot.operations[j].getAdrCrediteur() +
                enteteLot.operations[j].getRefEmetteur() +
                Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
                enteteLot.operations[j].getLibelle() +
                enteteLot.operations[j].getBlancs());


        return line;
    }

    private String printEffets4042(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() +
                enteteLot.operations[j].getRefOperation() +
                enteteLot.operations[j].getIdAgeRem() +
                enteteLot.operations[j].getIPac() +
                enteteLot.operations[j].getNumIntOrdre() +
                Utility.convertDateToString(enteteLot.operations[j].getDateEcheance(), "yyyyMMdd") +
                enteteLot.operations[j].getFlagIBANDeb() +
                enteteLot.operations[j].getPfxIBANDeb() +
                enteteLot.operations[j].getRibDebiteur() +
                enteteLot.operations[j].getMontant() +
                enteteLot.operations[j].getCodeFrais() +
                enteteLot.operations[j].getMontantFrais() +
                enteteLot.operations[j].getMontantBrut() +
                enteteLot.operations[j].getNomSouscripteur() +
                enteteLot.operations[j].getAdrSouscripteur() +
                enteteLot.operations[j].getNomCrediteur() +
                enteteLot.operations[j].getAdrCrediteur() +
                Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
                enteteLot.operations[j].getCodeAcceptation() +
                enteteLot.operations[j].getRefSouscripteur() +
                enteteLot.operations[j].getCodeEndossement() +
                enteteLot.operations[j].getNumCedant() +
                enteteLot.operations[j].getCodeAval() +
                enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printEffets45(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() +
                enteteLot.operations[j].getIPac() +
                enteteLot.operations[j].getNumIntOrdre() +
                Utility.convertDateToString(enteteLot.operations[j].getDateEcheance(), "yyyyMMdd") +
                enteteLot.operations[j].getMontant() +
                enteteLot.operations[j].getCodeFrais() +
                enteteLot.operations[j].getMontantFrais() +
                enteteLot.operations[j].getMontantBrut() +
                enteteLot.operations[j].getNomSouscripteur() +
                enteteLot.operations[j].getAdrSouscripteur() +
                enteteLot.operations[j].getNomCrediteur() +
                enteteLot.operations[j].getAdrCrediteur() +
                Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
                enteteLot.operations[j].getCodeAcceptation() +
                enteteLot.operations[j].getRefSouscripteur() +
                enteteLot.operations[j].getCodeEndossement() +
                enteteLot.operations[j].getProtet() +
                enteteLot.operations[j].getCodeAval() + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printEffets4143(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() +
                enteteLot.operations[j].getIPac() +
                enteteLot.operations[j].getNumIntOrdre() +
                Utility.convertDateToString(enteteLot.operations[j].getDateEcheance(), "yyyyMMdd") +
                enteteLot.operations[j].getFlagIBANDeb() +
                enteteLot.operations[j].getPfxIBANDeb() +
                enteteLot.operations[j].getRibDebiteur() +
                enteteLot.operations[j].getMontant() +
                enteteLot.operations[j].getCodeFrais() +
                enteteLot.operations[j].getMontantFrais() +
                enteteLot.operations[j].getMontantBrut() +
                enteteLot.operations[j].getNomSouscripteur() +
                enteteLot.operations[j].getAdrSouscripteur() +
                enteteLot.operations[j].getNomCrediteur() +
                enteteLot.operations[j].getAdrCrediteur() +
                Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
                enteteLot.operations[j].getCodeAcceptation() +
                enteteLot.operations[j].getRefSouscripteur() +
                enteteLot.operations[j].getCodeEndossement() +
                enteteLot.operations[j].getNumCedant() +
                enteteLot.operations[j].getCodeAval() + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printEffets46(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() +
                enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() +
                enteteLot.operations[j].getIPac() +
                enteteLot.operations[j].getNumIntOrdre() +
                Utility.convertDateToString(enteteLot.operations[j].getDateEcheance(), "yyyyMMdd") +
                enteteLot.operations[j].getRibDebiteur() +
                enteteLot.operations[j].getMontant() +
                enteteLot.operations[j].getCodeFrais() +
                enteteLot.operations[j].getMontantFrais() +
                enteteLot.operations[j].getMontantBrut() +
                enteteLot.operations[j].getNomSouscripteur() +
                enteteLot.operations[j].getAdrSouscripteur() +
                enteteLot.operations[j].getNomCrediteur() +
                enteteLot.operations[j].getAdrCrediteur() +
                Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") +
                enteteLot.operations[j].getCodeAcceptation() +
                enteteLot.operations[j].getRefSouscripteur() +
                enteteLot.operations[j].getCodeEndossement() +
                enteteLot.operations[j].getProtet() +
                enteteLot.operations[j].getCodeAval() +
                enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printEnteteLot(EnteteLot enteteLot) throws Exception {
        String line;
        //lotcom = CMPUtility.insertLotcom(enteteLot, remcom);
        line = new String(enteteLot.getIdEntete() +
                enteteLot.getIdBanRem() +
                enteteLot.getTypeOperation() +
                enteteLot.getRefLot() +
                CMPUtility.getDevise() +
                enteteLot.getNbOperations() +
                enteteLot.getMontantTotal() +
                enteteLot.getBlancs());
        return line;
    }

    

    private void updateEtatOperations(DataBase db) throws SQLException {
        String l_sql = "";


        if (remiseHasRejetsCheques) {
            l_sql = "UPDATE CHEQUES SET LOTSIB=" + "2" + " WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM2") ;
            db.executeUpdate(l_sql);

             
        }

//         if (remiseHasRejetsEffets) {
//            l_sql = "UPDATE EFFETS SET ETAT="+Utility.getParam("CETAOPEREJRETENVSIB") +",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPEREJRET") ;
//            db.executeUpdate(l_sql);
//
//        }

    }
}
