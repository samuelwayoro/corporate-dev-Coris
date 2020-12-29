/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.uap;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.Operation;
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
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class LotUAPEffetWriterSIB extends FlatFileWriter {

    private String sql = "";
    private int refLot = 0;
    private Vector<EnteteLot> vEnteteLots = new Vector<EnteteLot>();
    
    Effets effets45[] = null;
    Effets effets46[] = null;
    Remcom remcom = null;
    Lotcom lotcom = null;

    private boolean remiseHasEffets = false;

    public LotUAPEffetWriterSIB() {
        setDescription("Envoi de Lot d'opération aller effet vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String whereClause = " ETAT=" + Utility.getParam("CETAOPEVAL2") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";

      /*  //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='045'";
        effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets45);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='046'";
        effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets46);*/
        
         //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEVAL2") + " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='045'";
        effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets45);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEVAL2") +  " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='046'";
        effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets46);

        if (refLot > 0) {
           
            updateEtatOperations(db);
        } else {
            logEvent("WARNING", "Il n'y a aucun élément disponible");
        }

        db.close();


    }

    
    private void feedOperationWithBltOrdre(int j, Operation[] operations, Effets effet) {
        operations[j].setIdAgeRem(effet.getAgenceremettant());
        operations[j].setIPac(effet.getVille());
        operations[j].setNumIntOrdre(effet.getNumeroeffet());
        operations[j].setDateEcheance(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")));
        operations[j].setFlagIBANDeb("0");
        operations[j].setPfxIBANDeb(createBlancs(4, " "));
        operations[j].setRibDebiteur(effet.getBanque() + effet.getAgence() + effet.getNumerocompte_Tire() + Utility.computeCleRIB(effet.getBanque(), effet.getAgence(), effet.getNumerocompte_Tire()));
        operations[j].setMontant(effet.getMontant_Effet());
        operations[j].setCodeFrais(effet.getCode_Frais());
        operations[j].setMontantFrais(effet.getMontant_Frais());
        operations[j].setMontantBrut(effet.getMontant_Brut());
        operations[j].setNomSouscripteur(effet.getNom_Tire());
        operations[j].setAdrSouscripteur(effet.getAdresse_Tire());
        operations[j].setNomCrediteur(effet.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(effet.getAdresse_Beneficiaire());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(effet.getDate_Creation(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeAcceptation(effet.getCode_Acceptation());
        operations[j].setRefSouscripteur(effet.getIdentification_Tire());
        operations[j].setCodeEndossement(effet.getCode_Endossement());
        operations[j].setNumCedant(effet.getNumero_Cedant());
        operations[j].setCodeAval(effet.getCode_Aval());
        operations[j].setBlancs(createBlancs(85, " "));
    }

    private void feedOperationWithBltOrdre45(int j, Operation[] operations, Effets effet) {
        operations[j].setIdAgeRem(effet.getAgenceremettant());
        operations[j].setIPac(effet.getVille());
        operations[j].setNumIntOrdre(effet.getNumeroeffet());
        operations[j].setDateEcheance(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")));

        operations[j].setRibDebiteur(effet.getBanque() + effet.getAgence() + effet.getNumerocompte_Tire() + Utility.computeCleRIB(effet.getBanque(), effet.getAgence(), effet.getNumerocompte_Tire()));
        operations[j].setMontant(effet.getMontant_Effet());
        operations[j].setCodeFrais(effet.getCode_Frais());
        operations[j].setMontantFrais(effet.getMontant_Frais());
        operations[j].setMontantBrut(effet.getMontant_Brut());
        operations[j].setNomSouscripteur(effet.getNom_Tire());
        operations[j].setAdrSouscripteur(effet.getAdresse_Tire());
        operations[j].setNomCrediteur(effet.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(effet.getAdresse_Beneficiaire());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(effet.getDate_Creation(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeAcceptation(effet.getCode_Acceptation());
        operations[j].setRefSouscripteur(effet.getIdentification_Tire());
        operations[j].setCodeEndossement(effet.getCode_Endossement());
        operations[j].setProtet(effet.getProtet());
        operations[j].setCodeAval(effet.getCode_Aval());
        operations[j].setBlancs(createBlancs(96, " "));
    }

    private void feedOperationWithLtrChange(Operation[] operations, int j, Effets effet) {
        operations[j].setIdAgeRem(effet.getAgenceremettant());
        operations[j].setIPac(effet.getVille());
        operations[j].setNumIntOrdre(effet.getNumeroeffet());
        operations[j].setDateEcheance(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")));
        operations[j].setFlagIBANDeb("0");
        operations[j].setPfxIBANDeb(createBlancs(4, " "));
        operations[j].setRibDebiteur(effet.getBanque() + effet.getAgence() + effet.getNumerocompte_Tire() + Utility.computeCleRIB(effet.getBanque(), effet.getAgence(), effet.getNumerocompte_Tire()));
        operations[j].setMontant(effet.getMontant_Effet());
        operations[j].setCodeFrais(effet.getCode_Frais());
        operations[j].setMontantFrais(effet.getMontant_Frais());
        operations[j].setMontantBrut(effet.getMontant_Brut());
        operations[j].setNomSouscripteur(effet.getNom_Tire());
        operations[j].setAdrSouscripteur(effet.getAdresse_Tire());
        operations[j].setNomCrediteur(effet.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(effet.getAdresse_Beneficiaire());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(effet.getDate_Creation(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeAcceptation(effet.getCode_Acceptation());
        operations[j].setRefSouscripteur(effet.getIdentification_Tire());
        operations[j].setCodeEndossement(effet.getCode_Endossement());
        operations[j].setNumCedant(effet.getNumero_Cedant());
        operations[j].setCodeAval(effet.getCode_Aval());
        operations[j].setBlancs(createBlancs(85, " "));
    }

    private void feedOperationWithLtrChange46(Operation[] operations, int j, Effets effet) {
        operations[j].setIdAgeRem(effet.getAgenceremettant());
        operations[j].setIPac(effet.getVille());
        operations[j].setNumIntOrdre(effet.getNumeroeffet());
        operations[j].setDateEcheance(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")));

        operations[j].setRibDebiteur(effet.getBanque() + effet.getAgence() + effet.getNumerocompte_Tire() + Utility.computeCleRIB(effet.getBanque(), effet.getAgence(), effet.getNumerocompte_Tire()));
        operations[j].setMontant(effet.getMontant_Effet());
        operations[j].setCodeFrais(effet.getCode_Frais());
        operations[j].setMontantFrais(effet.getMontant_Frais());
        operations[j].setMontantBrut(effet.getMontant_Brut());
        operations[j].setNomSouscripteur(effet.getNom_Tire());
        operations[j].setAdrSouscripteur(effet.getAdresse_Tire());
        operations[j].setNomCrediteur(effet.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(effet.getAdresse_Beneficiaire());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(effet.getDate_Creation(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeAcceptation(effet.getCode_Acceptation());
        operations[j].setRefSouscripteur(effet.getIdentification_Tire());
        operations[j].setCodeEndossement(effet.getCode_Endossement());
        operations[j].setProtet(effet.getProtet());
        operations[j].setCodeAval(effet.getCode_Aval());
        operations[j].setBlancs(createBlancs(96, " "));
    }

    private void prepareEffets(Effets[] effets) throws  Exception {
        if (effets != null && effets.length > 0) {
            remiseHasEffets = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(effets[0].getType_Effet());
            enteteLot.setIdBanRem(effets[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(26, " "));
            enteteLot.setNbOperations("" + effets.length);
            Operation[] operations = new Operation[effets.length];
            long montantLot = 0;
            for (int j = 0; j < effets.length; j++) {
                Effets effet = effets[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(effet.getMontant_Effet().trim());

                operations[j].setTypeOperation(effet.getType_Effet());
                operations[j].setRefOperation("" + effet.getIdeffet());
                operations[j].setIdObjetOrigine(effet.getIdeffet());
                switch (Integer.parseInt(operations[j].getTypeOperation())) {
                    case StaticValues.BLT_ORD_PAP:
                    case StaticValues.BLT_ORD_SCAN:
                         {
                            feedOperationWithBltOrdre(j, operations, effet);
                        }
                        break;
                    case StaticValues.BLT_ORD_SCAN_SICA3:
                         {
                            feedOperationWithBltOrdre45(j, operations, effet);
                        }
                        break;
                    case StaticValues.LTR_CHG_SCAN:

                    case StaticValues.LTR_CHG_PAP:
                         {
                            feedOperationWithLtrChange(operations, j, effet);
                        }
                        break;
                    case StaticValues.LTR_CHG_SCAN_SICA3:
                         {
                            feedOperationWithLtrChange46(operations, j, effet);
                        }
                        break;
                }


            }

            enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
            printLotEffet(effets, enteteLot);
        }
    }

    public void printLotEffet(Effets[] effets, EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + effets[0].getBanqueremettant() + ".001." + enteteLot.getRefLot() + "."+ effets[0].getType_Effet() + "."+ CMPUtility.getDevise() + ".LOT";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                 case StaticValues.BLT_ORD_SCAN:
                        ;
                    case StaticValues.BLT_ORD_SCAN_SICA3:
                         {
                            line = printEffets45(enteteLot, j);
                            writeln(line);
                        }
                        ;
                        break;
                    case StaticValues.LTR_CHG_SCAN:
                        ;
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

    private String printEffets45(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() +
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

    private String printEffets46(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() +
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

        String whereClause = " ETAT=" + Utility.getParam("CETAOPEVAL2") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";

       /* if (remiseHasCheques) {
            l_sql = "UPDATE CHEQUES SET ETAT="+ Utility.getParam("CETAOPEALLICOM1") + ", LOTSIB=1 WHERE  " + whereClause ;
            db.executeUpdate(l_sql);
        }

        if (remiseHasVirements) {
            l_sql = "UPDATE VIREMENTS SET ETAT="+ Utility.getParam("CETAOPEALLICOM1") + ", LOTSIB=1 WHERE  " + whereClause ;
            db.executeUpdate(l_sql);
        }*/

        if (remiseHasEffets) {
            l_sql = "UPDATE EFFETS SET ETAT="+ Utility.getParam("CETAOPEALLICOM1") + ", LOTSIB=1 WHERE  " + whereClause ;
            db.executeUpdate(l_sql);
        }

    }
}
