/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.uap;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.Operation;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Lotcom;
import clearing.table.Prelevements;
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
public class CroUAPESNWriter extends FlatFileWriter {

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
    Prelevements prelevements[] = null;
    Effets effets40[] = null;
    Effets effets41[] = null;
    Effets effets42[] = null;
    Effets effets43[] = null;
    Effets effets45[] = null;
    Effets effets46[] = null;
    Remcom remcom = null;
    Lotcom lotcom = null;
    private boolean remiseHasRejetsCheques = false;
    private boolean remiseHasRejetsPrelevements = false;
    private boolean remiseHasRejetsEffets = false;
    private boolean remiseHasCheques = false;
    private boolean remiseHasEffets = false;
    private boolean remiseHasVirements = false;
    private boolean remiseHasPrelevements = false;

    public CroUAPESNWriter() {
        setDescription("Envoi de Lot d'op�ration retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String whereClause = " ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") AND BANQUEREMETTANT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' ";

        //Recuperation des prelevements 025
        sql = "SELECT * FROM PRELEVEMENTS WHERE " + whereClause + " AND TYPE_PRELEVEMENT ='025'";
        prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
        preparePrelevements(prelevements);

        //Recuperation des cheques 030
        sql = "SELECT * FROM CHEQUES WHERE " + whereClause + " AND TYPE_CHEQUE ='030'";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareCheques(cheques);

        //Recuperation des cheques 035
        sql = "SELECT * FROM CHEQUES WHERE " + whereClause + " AND TYPE_CHEQUE ='035'";
        cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareCheques(cheques35);

        //Recuperation des virements CLIENTELE
        sql = "SELECT * FROM VIREMENTS WHERE   " + whereClause + " AND TYPE_VIREMENT ='010' ";
        virements10 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements10);

        //Recuperation des virements CLIENTELE 015
        sql = "SELECT * FROM VIREMENTS WHERE  " + whereClause + " AND TYPE_VIREMENT ='015' ";
        virements15 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        if (virements15 != null && virements15.length > 9999) {
            Virements[] virements = new Virements[9999];
            System.arraycopy(virements15, 0, virements, 0, 9999);
            prepareVirements(virements);
            virements = new Virements[(virements15.length - 9999)];
            System.arraycopy(virements15, 9999, virements, 0, (virements15.length - 9999));
            prepareVirements(virements);
        } else {
            prepareVirements(virements15);
        }

        //Recuperation des virements BANQUE A BANQUE d'une Banque
        sql = "SELECT * FROM VIREMENTS WHERE  " + whereClause + " AND TYPE_VIREMENT ='011' ";
        virements11 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements11);

        //Recuperation des virements MISE A DISPOSITION d'une Banque
        sql = "SELECT * FROM VIREMENTS WHERE  " + whereClause + " AND TYPE_VIREMENT ='012' ";
        virements12 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements12);

        //Recuperation des virements MISE A DISPOSITION 017 d'une Banque
        sql = "SELECT * FROM VIREMENTS WHERE  " + whereClause + " AND TYPE_VIREMENT ='017' ";
        virements17 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements17);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE   " + whereClause + " AND TYPE_EFFET ='040' ";
        effets40 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets40);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='045'";
        effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets45);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='041' ";
        effets41 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets41);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='046'";
        effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets46);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS  WHERE " + whereClause + " AND TYPE_EFFET ='042' ";
        effets42 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets42);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='043' ";
        effets43 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets43);

        whereClause = " ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") AND BANQUE LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' ";
        //Recuperation des rejets cheques 130
        sql = "SELECT * FROM CHEQUES WHERE " + whereClause + " AND TYPE_CHEQUE ='030'";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareRejetCheques(cheques);

        //Recuperation des cheques 135
        sql = "SELECT * FROM CHEQUES WHERE " + whereClause + " AND TYPE_CHEQUE ='035'";
        cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareRejetCheques(cheques35);

        //Recuperation des prelevements 125
        sql = "SELECT * FROM PRELEVEMENTS WHERE " + whereClause + " AND TYPE_PRELEVEMENT ='025'";
        prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
        prepareRejetPrelevements(prelevements);
        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE   " + whereClause + " AND TYPE_EFFET ='040' ";
        effets40 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets40);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='045'";
        effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets45);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='041' ";
        effets41 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets41);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='046'";
        effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets46);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS  WHERE" + whereClause + " AND TYPE_EFFET ='042' ";
        effets42 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets42);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='043' ";
        effets43 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets43);

        whereClause = " ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") AND BANQUEREMETTANT NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' ";

        //Recuperation des cheques 030
        sql = "SELECT * FROM CHEQUES WHERE " + whereClause + " AND TYPE_CHEQUE ='030'";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareCheques(cheques);

        //Recuperation des cheques 035
        sql = "SELECT * FROM CHEQUES WHERE " + whereClause + " AND TYPE_CHEQUE ='035'";
        cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareCheques(cheques35);

        //Recuperation des virements CLIENTELE
        sql = "SELECT * FROM VIREMENTS WHERE   " + whereClause + " AND TYPE_VIREMENT ='010' ";
        virements10 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements10);

        //Recuperation des virements CLIENTELE 015
        sql = "SELECT * FROM VIREMENTS WHERE  " + whereClause + " AND TYPE_VIREMENT ='015' ";
        virements15 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        if (virements15 != null && virements15.length > 9999) {
            Virements[] virements = new Virements[9999];
            System.arraycopy(virements15, 0, virements, 0, 9999);
            prepareVirements(virements);
            virements = new Virements[(virements15.length - 9999)];
            System.arraycopy(virements15, 9999, virements, 0, (virements15.length - 9999));
            prepareVirements(virements);
        } else {
            prepareVirements(virements15);
        }

        //Recuperation des virements BANQUE A BANQUE d'une Banque
        sql = "SELECT * FROM VIREMENTS WHERE  " + whereClause + " AND TYPE_VIREMENT ='011' ";
        virements11 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements11);

        //Recuperation des virements MISE A DISPOSITION d'une Banque
        sql = "SELECT * FROM VIREMENTS WHERE  " + whereClause + " AND TYPE_VIREMENT ='012' ";
        virements12 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements12);

        //Recuperation des virements MISE A DISPOSITION 017 d'une Banque
        sql = "SELECT * FROM VIREMENTS WHERE  " + whereClause + " AND TYPE_VIREMENT ='017' ";
        virements17 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements17);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE   " + whereClause + " AND TYPE_EFFET ='040' ";
        effets40 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets40);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='045'";
        effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets45);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='041' ";
        effets41 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets41);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='046'";
        effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets46);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS  WHERE " + whereClause + " AND TYPE_EFFET ='042' ";
        effets42 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets42);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='043' ";
        effets43 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareEffets(effets43);

        whereClause = " ETAT IN (" + Utility.getParam("CETAOPEREJRET") + ") AND BANQUE NOT LIKE '" + Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + "%' ";
        //Recuperation des rejets cheques 130
        sql = "SELECT * FROM CHEQUES WHERE " + whereClause + " AND TYPE_CHEQUE ='030'";
        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareRejetCheques(cheques);

        //Recuperation des cheques 135
        sql = "SELECT * FROM CHEQUES WHERE " + whereClause + " AND TYPE_CHEQUE ='035'";
        cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareRejetCheques(cheques35);

        //Recuperation des prelevements 125
        sql = "SELECT * FROM PRELEVEMENTS WHERE " + whereClause + " AND TYPE_PRELEVEMENT ='025'";
        prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
        prepareRejetPrelevements(prelevements);
        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE   " + whereClause + " AND TYPE_EFFET ='040' ";
        effets40 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets40);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='045'";
        effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets45);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='041' ";
        effets41 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets41);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='046'";
        effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets46);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS  WHERE" + whereClause + " AND TYPE_EFFET ='042' ";
        effets42 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets42);

        //Recuperation des effets d'une Banque
        sql = "SELECT * FROM EFFETS WHERE  " + whereClause + " AND TYPE_EFFET ='043' ";
        effets43 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        prepareRejetEffets(effets43);
        if (refLot > 0) {

//            updateEtatOperations(db);
            setDescription(getDescription() + " ex�cut� avec succ�s:\n ");
            for (int i = 0; i < vEnteteLots.size(); i++) {
                logEvent("INFO", "Nombre d'op�rations " + vEnteteLots.get(i).getTypeOperation() + " -> " + vEnteteLots.get(i).getNbOperations());
            }

        } else {
            logEvent("WARNING", "Il n'y a aucun �l�ment disponible");
        }

        db.close();

    }

    private void feedOperationWithBltOrdre(int j, Operation[] operations, Effets effet) {
        operations[j].setRio(new RIO(effet.getRio()));
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
        operations[j].setRio(new RIO(effet.getRio()));
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

    private void feedOperationWithCheque(int j, Cheques cheque, Operation[] operations) {
        operations[j].setRio(new RIO(cheque.getRio()));
        operations[j].setTypeOperation(cheque.getType_Cheque());
        operations[j].setRefOperation("" + cheque.getIdcheque());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setIPac(cheque.getVille());
        operations[j].setFlagIBANCre("0");
        operations[j].setPfxIBANCre(createBlancs(4, " "));
        operations[j].setNumCheque(cheque.getNumerocheque());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")));
        operations[j].setIdBanDeb(cheque.getBanque());
        operations[j].setIdAgeDeb(cheque.getAgence());
        operations[j].setNumCptDeb(cheque.getNumerocompte());
        operations[j].setCleRibDeb(Utility.computeCleRIB(cheque.getBanque(), cheque.getAgence(), cheque.getNumerocompte()));
        operations[j].setMontant(cheque.getMontantcheque());
        operations[j].setNomCrediteur(cheque.getNombeneficiaire());
        operations[j].setCodeCertif(cheque.getCodecertification());
        operations[j].setBlancs(createBlancs(286, " "));
    }

    private void feedOperationWithCheque35(int j, Cheques cheque, Operation[] operations) {
        operations[j].setRio(new RIO(cheque.getRio()));
        operations[j].setTypeOperation(cheque.getType_Cheque());
        operations[j].setRefOperation("" + cheque.getIdcheque());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setIPac(cheque.getVille());
        operations[j].setNomCrediteur(cheque.getNombeneficiaire());
        operations[j].setAdrCrediteur(createBlancs(50, " "));
        operations[j].setNumCheque(cheque.getNumerocheque());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")));
        operations[j].setIdBanDeb(cheque.getBanque());
        operations[j].setIdAgeDeb(cheque.getAgence());
        operations[j].setNumCptDeb(cheque.getNumerocompte());
        operations[j].setCleRibDeb(Utility.computeCleRIB(cheque.getBanque(), cheque.getAgence(), cheque.getNumerocompte()));
        operations[j].setMontant(cheque.getMontantcheque());
        operations[j].setCodeCertif(cheque.getCodecertification());
        operations[j].setBlancs(createBlancs(241, " "));
    }

    private void feedOperationWithLtrChange(Operation[] operations, int j, Effets effet) {
        operations[j].setRio(new RIO(effet.getRio()));
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
        operations[j].setRio(new RIO(effet.getRio()));
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

    private void feedOperationWithPrelevement(int j, Operation[] operations, Prelevements prelevement) {
        operations[j].setIdAgeRem(prelevement.getAgenceremettant());
        operations[j].setIPac(prelevement.getVille());
        operations[j].setNumIntOrdre(String.valueOf(prelevement.getNumeroprelevement()));
        operations[j].setIdObjetOrigine(prelevement.getIdprelevement());
        operations[j].setRio(new RIO(prelevement.getRio()));

        operations[j].setRibDebiteur(prelevement.getBanque() + prelevement.getAgence() + Utility.bourrageGZero(prelevement.getNumerocompte_Tire(), 12) + Utility.computeCleRIB(prelevement.getBanque(), prelevement.getAgence(), Utility.bourrageGZero(prelevement.getNumerocompte_Tire(), 12)));
        operations[j].setMontant(prelevement.getMontantprelevement());

        operations[j].setNomSouscripteur(prelevement.getNom_Tire());
        operations[j].setAdrSouscripteur(prelevement.getAdresse_Tire());
        operations[j].setNomCrediteur(prelevement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(prelevement.getAdresse_Beneficiaire());
        operations[j].setRibCrediteur(prelevement.getBanqueremettant() + prelevement.getAgenceremettant() + Utility.bourrageGZero(prelevement.getNumerocompte_Beneficiaire(), 12) + Utility.computeCleRIB(prelevement.getBanqueremettant(), prelevement.getAgenceremettant(), Utility.bourrageGZero(prelevement.getNumerocompte_Beneficiaire(), 12)));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(prelevement.getDatetraitement(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(prelevement.getLibelle());
        operations[j].setBlancs(createBlancs(173, " "));
    }

    private void feedOperationWithVirement10(Virements virement, int j, Operation[] operations) {
        operations[j].setRio(new RIO(virement.getRio()));
        operations[j].setFlagIBANDeb("0");
        operations[j].setPfxIBANDeb(createBlancs(4, " "));
        operations[j].setRibDebiteur(virement.getBanqueremettant() + virement.getAgenceremettant() + virement.getNumerocompte_Tire() + Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(), virement.getNumerocompte_Tire()));
        operations[j].setFlagIBANCre("0");
        operations[j].setPfxIBANCre(createBlancs(4, " "));
        operations[j].setRibCrediteur(virement.getBanque() + virement.getAgence() + virement.getNumerocompte_Beneficiaire() + Utility.computeCleRIB(virement.getBanque(), virement.getAgence(), virement.getNumerocompte_Beneficiaire()));

        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setAdrDebiteur(virement.getAdresse_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(virement.getAdresse_Beneficiaire());
        operations[j].setNumIntOrdre(virement.getNumerovirement());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(57, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void feedOperationWithVirement15(Virements virement, int j, Operation[] operations) {
        operations[j].setRio(new RIO(virement.getRio()));
        if (Character.isDigit(virement.getBanqueremettant().charAt(1))) {//Ancienne Norme
            operations[j].setFlagIBANDeb("1");
        } else {//Nouvelle Norme
            operations[j].setFlagIBANDeb("2");
        }

        operations[j].setRibDebiteur(virement.getBanqueremettant() + virement.getAgenceremettant() + virement.getNumerocompte_Tire() + Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(), virement.getNumerocompte_Tire()));

        if (Character.isDigit(virement.getBanque().charAt(1))) {//Ancienne Norme
            operations[j].setFlagIBANCre("1");
        } else {//Nouvelle Norme
            operations[j].setFlagIBANCre("2");
        }
        operations[j].setRibCrediteur(virement.getBanque() + virement.getAgence() + virement.getNumerocompte_Beneficiaire() + Utility.computeCleRIB(virement.getBanque(), virement.getAgence(), virement.getNumerocompte_Beneficiaire()));

        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setAdrDebiteur(virement.getAdresse_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(virement.getAdresse_Beneficiaire());
        operations[j].setNumIntOrdre(virement.getNumerovirement());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(65, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void feedOperationWithVirement11(Operation[] operations, int j, Virements virement) {
        operations[j].setRio(new RIO(virement.getRio()));
        operations[j].setIdAgeRem(virement.getAgenceremettant());
        operations[j].setIdBanCre(virement.getBanque());
        operations[j].setIdAgeCre(virement.getAgence());
        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setRefEmetteur(virement.getReference_Emetteur());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(200, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void feedOperationWithVirement12(int j, Operation[] operations, Virements virement) {
        operations[j].setRio(new RIO(virement.getRio()));
        operations[j].setFlagIBANDeb("0");
        operations[j].setPfxIBANDeb(createBlancs(4, " "));
        operations[j].setRibDebiteur(virement.getBanqueremettant() + virement.getAgenceremettant() + virement.getNumerocompte_Tire() + Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(), virement.getNumerocompte_Tire()));
        operations[j].setIdBanCre(virement.getBanque());
        operations[j].setIdAgeCre(virement.getAgence());
        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setAdrDebiteur(virement.getAdresse_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(virement.getAdresse_Beneficiaire());
        operations[j].setRefEmetteur(virement.getReference_Emetteur());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(76, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void feedOperationWithVirement17(int j, Operation[] operations, Virements virement) {
        operations[j].setRio(new RIO(virement.getRio()));
        operations[j].setRibDebiteur(virement.getBanqueremettant() + virement.getAgenceremettant() + virement.getNumerocompte_Tire() + Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(), virement.getNumerocompte_Tire()));
        operations[j].setIdBanCre(virement.getBanque());
        operations[j].setIdAgeCre(virement.getAgence());
        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setAdrDebiteur(virement.getAdresse_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(virement.getAdresse_Beneficiaire());
        operations[j].setRefEmetteur(virement.getReference_Emetteur());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(81, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void preparePrelevements(Prelevements[] prelevements) throws Exception {
        if (prelevements != null && prelevements.length > 0) {
            remiseHasPrelevements = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ECRO");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(prelevements[0].getType_prelevement());
            enteteLot.setIdBanRem(prelevements[0].getBanque());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + prelevements.length);
            Operation[] operations = new Operation[prelevements.length];
            long montantLot = 0;
            for (int j = 0; j < prelevements.length; j++) {
                Prelevements prelevement = prelevements[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(prelevement.getMontantprelevement().trim());

                operations[j].setTypeOperation(prelevement.getType_prelevement());
                operations[j].setRefOperation("" + prelevement.getIdprelevement());
                operations[j].setIdObjetOrigine(prelevement.getIdprelevement());
                switch (Integer.parseInt(operations[j].getTypeOperation())) {
                    case StaticValues.PRELEVEMENT_SICA3: {
                        feedOperationWithPrelevement(j, operations, prelevement);
                    }
                    break;

                }

            }

            enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
            printLotPrelevement(prelevements, enteteLot);
        }
    }

    private void prepareRejetPrelevements(Prelevements[] prelevements) throws Exception {
        if (prelevements != null && prelevements.length > 0) {
            remiseHasRejetsPrelevements = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ECRO");
            ++refLot;
            enteteLot.setRefLot(Utility.computeCompteur("REFLOT", CMPUtility.getDate()));
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(prelevements[0].getType_prelevement().replaceFirst("0", "1"));
            enteteLot.setIdBanRem(prelevements[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + prelevements.length);
            Operation[] operations = new Operation[prelevements.length];

            for (int j = 0; j < prelevements.length; j++) {
                Prelevements prelevement = prelevements[j];
                operations[j] = new Operation();

                operations[j].setRio(new RIO(prelevement.getRio_Rejet()));
                operations[j].setTypeOperation(prelevements[0].getType_prelevement().replaceFirst("0", "1"));
                operations[j].setRefOperation(prelevement.getReference_Operation_Rejet().trim());
                operations[j].setIdAgeRem(prelevement.getAgenceremettant());
                operations[j].setRioOperInitial(new RIO(prelevement.getRio()));
                operations[j].setMotifRejet(prelevement.getMotifrejet());
                operations[j].setRefEmetteur(prelevement.getReference_Operation_Interne());
                operations[j].setBlancs(createBlancs(10, " "));
                operations[j].setIdObjetOrigine(prelevement.getIdprelevement());
            }

            enteteLot.setMontantTotal(createBlancs(16, " "));
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
            printLotRejets(enteteLot);
        }
    }

    private void prepareRejetCheques(Cheques[] cheques) throws Exception {

        if (cheques != null && cheques.length > 0) {
            remiseHasRejetsCheques = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ECRO");
            ++refLot;
            enteteLot.setRefLot(Utility.computeCompteur("REFLOT", CMPUtility.getDate()));
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(cheques[0].getType_Cheque().replaceFirst("0", "1"));
            enteteLot.setIdBanRem(cheques[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + cheques.length);
            Operation[] operations = new Operation[cheques.length];

            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];
                operations[j] = new Operation();

                operations[j].setRio(new RIO(cheque.getRio_Rejet()));
                operations[j].setTypeOperation(cheques[0].getType_Cheque().replaceFirst("0", "1"));
                operations[j].setRefOperation(cheque.getReference_Operation_Rejet().trim());
                operations[j].setIdAgeRem(cheque.getAgenceremettant());
                operations[j].setRioOperInitial(new RIO(cheque.getRio()));
                operations[j].setMotifRejet(cheque.getMotifrejet());
                operations[j].setRefEmetteur(cheque.getReference_Operation_Interne());
                operations[j].setBlancs(createBlancs(10, " "));
                operations[j].setIdObjetOrigine(cheque.getIdcheque());
            }

            enteteLot.setMontantTotal(createBlancs(16, " "));
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
            printLotRejets(enteteLot);
        }
    }

    private void prepareRejetEffets(Effets[] effets) throws Exception {
        if (effets != null && effets.length > 0) {
            remiseHasRejetsEffets = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ECRO");
            ++refLot;
            enteteLot.setRefLot(Utility.computeCompteur("REFLOT", CMPUtility.getDate()));
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(effets[0].getType_Effet().replaceFirst("0", "1"));
            enteteLot.setIdBanRem(effets[0].getBanqueremettant());
            enteteLot.setBlancs(createBlancs(24, " "));
            enteteLot.setNbOperations("" + effets.length);
            Operation[] operations = new Operation[effets.length];

            for (int j = 0; j < effets.length; j++) {
                Effets effet = effets[j];
                operations[j] = new Operation();

                operations[j].setRio(new RIO(effet.getRio_Rejet()));
                operations[j].setTypeOperation(effets[0].getType_Effet().replaceFirst("0", "1"));
              //  operations[j].setRefOperation(effet.getZoneinterbancaire_Beneficiaire().trim());
                operations[j].setRefOperation((effet.getZoneinterbancaire_Beneficiaire() != null) ? effet.getZoneinterbancaire_Beneficiaire().trim() : effet.getIdeffet().toPlainString());
                operations[j].setIdAgeRem(effet.getAgenceremettant());
                operations[j].setRefEmetteur(effet.getZoneinterbancaire_Beneficiaire());
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

    private void prepareCheques(Cheques[] cheques) throws Exception {

        if (cheques != null && cheques.length > 0) {
            remiseHasCheques = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ECRO");
            enteteLot.setIdBanRem(cheques[0].getBanque());
            enteteLot.setTypeOperation(cheques[0].getType_Cheque());
            ++refLot;
            enteteLot.setRefLot(Utility.computeCompteur("REFLOT", CMPUtility.getDate()));
            enteteLot.setBlancs(createBlancs(26, " "));
            enteteLot.setNbOperations("" + cheques.length);

            Operation[] operations = new Operation[cheques.length];
            long montantLot = 0;
            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(cheque.getMontantcheque().trim());
                operations[j].setIdObjetOrigine(cheque.getIdcheque());
                switch (Integer.parseInt(cheque.getType_Cheque())) {

                    case StaticValues.CHQ_SCAN: {
                        feedOperationWithCheque(j, cheque, operations);
                    }
                    break;
                    case StaticValues.CHQ_SCAN_SICA3: {
                        feedOperationWithCheque35(j, cheque, operations);
                    }
                    break;

                }

            }

            enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);

            printLotCheque(cheques, enteteLot);
        }
    }

    private void prepareEffets(Effets[] effets) throws Exception {
        if (effets != null && effets.length > 0) {
            remiseHasEffets = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ECRO");
            ++refLot;
            enteteLot.setRefLot(Utility.computeCompteur("REFLOT", CMPUtility.getDate()));
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(effets[0].getType_Effet());
            enteteLot.setIdBanRem(effets[0].getBanque());
            enteteLot.setBlancs(createBlancs(26, " "));
            enteteLot.setNbOperations("" + effets.length);
            Operation[] operations = new Operation[effets.length];
            long montantLot = 0;
            for (int j = 0; j < effets.length; j++) {
                Effets effet = effets[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(effet.getMontant_Effet().trim());

                operations[j].setTypeOperation(effet.getType_Effet());
                operations[j].setRefOperation("" + effet.getIdeffet().toPlainString());
                operations[j].setIdObjetOrigine(effet.getIdeffet());
                switch (Integer.parseInt(operations[j].getTypeOperation())) {
                    case StaticValues.BLT_ORD_PAP:
                    case StaticValues.BLT_ORD_SCAN: {
                        feedOperationWithBltOrdre(j, operations, effet);
                    }
                    break;
                    case StaticValues.BLT_ORD_SCAN_SICA3: {
                        feedOperationWithBltOrdre45(j, operations, effet);
                    }
                    break;
                    case StaticValues.LTR_CHG_SCAN:

                    case StaticValues.LTR_CHG_PAP: {
                        feedOperationWithLtrChange(operations, j, effet);
                    }
                    break;
                    case StaticValues.LTR_CHG_SCAN_SICA3: {
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

    private void prepareVirements(Virements[] virements) throws Exception {
        if (virements != null && virements.length > 0) {
            remiseHasVirements = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ECRO");
            ++refLot;
            enteteLot.setRefLot(Utility.bourrageGZero(Utility.computeCompteur("REFLOT", CMPUtility.getDate()), 3));
            enteteLot.setRefBancaire(Utility.bourrageGZero(enteteLot.getRefLot(), 5));
            enteteLot.setTypeOperation(virements[0].getType_Virement());
            enteteLot.setIdBanRem(virements[0].getBanque());
            enteteLot.setBlancs(createBlancs(26, " "));
            enteteLot.setNbOperations(Utility.bourrageGZero("" + virements.length, 4));
            Operation[] operations = new Operation[virements.length];
            long montantLot = 0;
            for (int j = 0; j < virements.length; j++) {
                Virements virement = virements[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(virement.getMontantvirement().trim());

                operations[j].setTypeOperation(virement.getType_Virement());
                operations[j].setRefOperation("" + virement.getIdvirement());
                operations[j].setIdObjetOrigine(virement.getIdvirement());
                switch (Integer.parseInt(operations[j].getTypeOperation())) {
                    case StaticValues.VIR_CLIENT: {
                        feedOperationWithVirement10(virement, j, operations);
                    }
                    break;
                    case StaticValues.VIR_BANQUE: {
                        feedOperationWithVirement11(operations, j, virement);
                    }
                    break;

                    case StaticValues.VIR_DISPOSITION: {
                        feedOperationWithVirement12(j, operations, virement);

                    }
                    break;
                    case StaticValues.VIR_CLIENT_SICA3: {
                        feedOperationWithVirement15(virement, j, operations);

                    }
                    break;
                    case StaticValues.VIR_DISPOSITION_SICA3: {
                        feedOperationWithVirement17(j, operations, virement);

                    }
                    break;
                }

            }

            enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
            printLotVirement(virements, enteteLot);
        }
    }

    public void printLotCheque(Cheques[] cheques, EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + cheques[0].getBanque() + ".000." + enteteLot.getRefLot() + "." + cheques[0].getType_Cheque() + "." + CMPUtility.getDevise() + ".CRO"; //+CMPUtility.getDate()+".done";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                case StaticValues.CHQ_SCAN:
                    ;
                case StaticValues.CHQ_PAP: {
                    line = printCheques(enteteLot, j);
                    writeln(line);
                }
                ;
                break;
                case StaticValues.CHQ_SCAN_SICA3: {
                    line = printCheques35(enteteLot, j);
                    writeln(line);
                }
                ;
                break;
            }
        }
        closeFile();
    }

    public void printLotVirement(Virements[] virements, EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + virements[0].getBanque() + ".000." + enteteLot.getRefLot() + "." + virements[0].getType_Virement() + "." + CMPUtility.getDevise() + ".CRO"; //+CMPUtility.getDate()+".done";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                case StaticValues.VIR_CLIENT: {
                    line = printVirements10(enteteLot, j);
                    writeln(line);

                }
                ;
                break;
                case StaticValues.VIR_CLIENT_SICA3: {
                    line = printVirements15(enteteLot, j);
                    writeln(line);

                }
                ;
                break;
                case StaticValues.VIR_BANQUE: {
                    line = printVirements11(enteteLot, j);
                    writeln(line);

                }
                ;
                break;
                case StaticValues.VIR_DISPOSITION: {
                    line = printVirements12(enteteLot, j);
                    writeln(line);

                }
                ;
                break;
                case StaticValues.VIR_DISPOSITION_SICA3: {
                    line = printVirements17(enteteLot, j);
                    writeln(line);

                }
                ;
                break;

            }
        }
        closeFile();
    }

    public void printLotEffet(Effets[] effets, EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + effets[0].getBanque() + ".000." + enteteLot.getRefLot() + "." + effets[0].getType_Effet() + "." + CMPUtility.getDevise() + ".CRO";//+CMPUtility.getDate()+".done";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                case StaticValues.BLT_ORD_SCAN:
                    ;
                case StaticValues.BLT_ORD_PAP: {
                    line = printEffets4042(enteteLot, j);
                    writeln(line);

                }
                ;
                break;
                case StaticValues.BLT_ORD_SCAN_SICA3: {
                    line = printEffets45(enteteLot, j);
                    writeln(line);

                }
                ;
                break;
                case StaticValues.LTR_CHG_SCAN:
                    ;
                case StaticValues.LTR_CHG_PAP: {
                    line = printEffets4143(enteteLot, j);
                    writeln(line);

                }
                ;
                break;
                case StaticValues.LTR_CHG_SCAN_SICA3: {
                    line = printEffets46(enteteLot, j);
                    writeln(line);

                }
                ;
                break;

            }
        }
        closeFile();
    }

    public void printLotPrelevement(Prelevements[] prelevements, EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + prelevements[0].getBanque() + ".000." + enteteLot.getRefLot() + "." + prelevements[0].getType_prelevement() + "." + CMPUtility.getDevise() + ".CRO";//+CMPUtility.getDate()+".done";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {
            switch (Integer.parseInt(enteteLot.getTypeOperation())) {
                case StaticValues.PRELEVEMENT_SICA3: {
                    line = printPrelevements(enteteLot, j);
                    writeln(line);

                }
                ;
                break;

            }
        }
        closeFile();
    }

    private String printLotRejets(EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + enteteLot.getIdBanRem() + ".000." + enteteLot.getRefLot() + "." + enteteLot.operations[0].getTypeOperation() + "." + CMPUtility.getDevise() + ".CRO"; //+CMPUtility.getDate()+".done";
        setOut(createFlatFile(fileName));
        String line = printEnteteLot(enteteLot);
        writeln(line);
        for (int j = 0; j < enteteLot.operations.length; j++) {

            line = new String(
                    enteteLot.operations[j].getRio()
                    + enteteLot.operations[j].getTypeOperation()
                    + enteteLot.operations[j].getRefOperation()
                    + enteteLot.operations[j].getIdAgeRem()
                    + //enteteLot.operations[j].getRioOperInitial()+
                    enteteLot.operations[j].getRioOperInitial().toString().replace(enteteLot.operations[j].getRioOperInitial().getRefOperation(), Utility.bourrageGZero(enteteLot.operations[j].getRefEmetteur(), 8))
                    + enteteLot.operations[j].getMotifRejet()
                    + enteteLot.operations[j].getBlancs());
            writeln(line);
        }
        closeFile();

        return line;
    }

    private String printCheques(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() + enteteLot.operations[j].getIPac() + enteteLot.operations[j].getFlagIBANCre() + enteteLot.operations[j].getPfxIBANCre() + enteteLot.operations[j].getNumCheque() + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") + enteteLot.operations[j].getIdBanDeb() + enteteLot.operations[j].getIdAgeDeb() + enteteLot.operations[j].getNumCptDeb() + enteteLot.operations[j].getCleRibDeb() + enteteLot.operations[j].getMontant() + enteteLot.operations[j].getNomCrediteur() + enteteLot.operations[j].getCodeCertif() + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printCheques35(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio() + enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem() + enteteLot.operations[j].getIPac() + enteteLot.operations[j].getNomCrediteur() + enteteLot.operations[j].getAdrCrediteur() + enteteLot.operations[j].getNumCheque() + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd") + enteteLot.operations[j].getIdBanDeb() + enteteLot.operations[j].getIdAgeDeb() + enteteLot.operations[j].getNumCptDeb() + enteteLot.operations[j].getCleRibDeb() + enteteLot.operations[j].getMontant() + enteteLot.operations[j].getNomCrediteur() + enteteLot.operations[j].getCodeCertif() + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printPrelevements(EnteteLot enteteLot, int j) {

        StringBuilder lineBuilder = new StringBuilder();

        lineBuilder.append(enteteLot.operations[j].getRio().getRio());
        lineBuilder.append(enteteLot.operations[j].getTypeOperation());
        lineBuilder.append(enteteLot.operations[j].getRefOperation());
        lineBuilder.append("2");
        lineBuilder.append(enteteLot.operations[j].getRibCrediteur());
        lineBuilder.append("2");
        lineBuilder.append(enteteLot.operations[j].getRibDebiteur());
        lineBuilder.append(enteteLot.operations[j].getMontant());
        lineBuilder.append(enteteLot.operations[j].getNomCrediteur());
        lineBuilder.append(enteteLot.operations[j].getNumIntOrdre());
        lineBuilder.append(enteteLot.operations[j].getNomSouscripteur());
        lineBuilder.append(enteteLot.operations[j].getLibelle());

        lineBuilder.append(enteteLot.operations[j].getBlancs());

        return lineBuilder.toString();
    }

    private String printVirements10(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation()
                + enteteLot.operations[j].getRefOperation()
                + enteteLot.operations[j].getFlagIBANDeb()
                + enteteLot.operations[j].getPfxIBANDeb()
                + enteteLot.operations[j].getRibDebiteur()
                + enteteLot.operations[j].getFlagIBANCre()
                + enteteLot.operations[j].getPfxIBANCre()
                + enteteLot.operations[j].getRibCrediteur()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getNomDebiteur()
                + enteteLot.operations[j].getAdrDebiteur()
                + enteteLot.operations[j].getNomCrediteur()
                + enteteLot.operations[j].getAdrCrediteur()
                + enteteLot.operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getLibelle()
                + enteteLot.operations[j].getBlancs());

        return line;
    }

    private String printVirements15(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation()
                + enteteLot.operations[j].getRefOperation()
                + enteteLot.operations[j].getFlagIBANDeb()
                + enteteLot.operations[j].getRibDebiteur()
                + enteteLot.operations[j].getFlagIBANCre()
                + enteteLot.operations[j].getRibCrediteur()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getNomDebiteur()
                + enteteLot.operations[j].getAdrDebiteur()
                + enteteLot.operations[j].getNomCrediteur()
                + enteteLot.operations[j].getAdrCrediteur()
                + enteteLot.operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getLibelle()
                + enteteLot.operations[j].getBlancs());

        return line;
    }

    private String printVirements11(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem()
                + enteteLot.operations[j].getIdBanCre()
                + enteteLot.operations[j].getIdAgeCre()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getNomDebiteur()
                + enteteLot.operations[j].getNomCrediteur()
                + enteteLot.operations[j].getRefEmetteur()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getLibelle() + enteteLot.operations[j].getBlancs());

        return line;
    }

    private String printVirements12(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getPfxIBANDeb()
                + enteteLot.operations[j].getRibDebiteur()
                + enteteLot.operations[j].getIdBanCre()
                + enteteLot.operations[j].getIdAgeCre()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getNomDebiteur()
                + enteteLot.operations[j].getAdrDebiteur()
                + enteteLot.operations[j].getNomCrediteur()
                + enteteLot.operations[j].getAdrCrediteur()
                + enteteLot.operations[j].getRefEmetteur()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getLibelle()
                + enteteLot.operations[j].getBlancs());

        return line;
    }

    private String printVirements17(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation()
                + enteteLot.operations[j].getRefOperation()
                + enteteLot.operations[j].getRibDebiteur()
                + enteteLot.operations[j].getIdBanCre()
                + enteteLot.operations[j].getIdAgeCre()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getNomDebiteur()
                + enteteLot.operations[j].getAdrDebiteur()
                + enteteLot.operations[j].getNomCrediteur()
                + enteteLot.operations[j].getAdrCrediteur()
                + enteteLot.operations[j].getRefEmetteur()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getLibelle()
                + enteteLot.operations[j].getBlancs());

        return line;
    }

    private String printEffets4042(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation()
                + enteteLot.operations[j].getRefOperation()
                + enteteLot.operations[j].getIdAgeRem()
                + enteteLot.operations[j].getIPac()
                + enteteLot.operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteLot.operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteLot.operations[j].getFlagIBANDeb()
                + enteteLot.operations[j].getPfxIBANDeb()
                + enteteLot.operations[j].getRibDebiteur()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getCodeFrais()
                + enteteLot.operations[j].getMontantFrais()
                + enteteLot.operations[j].getMontantBrut()
                + enteteLot.operations[j].getNomSouscripteur()
                + enteteLot.operations[j].getAdrSouscripteur()
                + enteteLot.operations[j].getNomCrediteur()
                + enteteLot.operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getCodeAcceptation()
                + enteteLot.operations[j].getRefSouscripteur()
                + enteteLot.operations[j].getCodeEndossement()
                + enteteLot.operations[j].getNumCedant()
                + enteteLot.operations[j].getCodeAval()
                + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printEffets45(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem()
                + enteteLot.operations[j].getIPac()
                + enteteLot.operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteLot.operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteLot.operations[j].getRibDebiteur()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getCodeFrais()
                + enteteLot.operations[j].getMontantFrais()
                + enteteLot.operations[j].getMontantBrut()
                + enteteLot.operations[j].getNomSouscripteur()
                + enteteLot.operations[j].getAdrSouscripteur()
                + enteteLot.operations[j].getNomCrediteur()
                + enteteLot.operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getCodeAcceptation()
                + enteteLot.operations[j].getRefSouscripteur()
                + enteteLot.operations[j].getCodeEndossement()
                + enteteLot.operations[j].getProtet()
                + enteteLot.operations[j].getCodeAval() + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printEffets4143(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem()
                + enteteLot.operations[j].getIPac()
                + enteteLot.operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteLot.operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteLot.operations[j].getFlagIBANDeb()
                + enteteLot.operations[j].getPfxIBANDeb()
                + enteteLot.operations[j].getRibDebiteur()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getCodeFrais()
                + enteteLot.operations[j].getMontantFrais()
                + enteteLot.operations[j].getMontantBrut()
                + enteteLot.operations[j].getNomSouscripteur()
                + enteteLot.operations[j].getAdrSouscripteur()
                + enteteLot.operations[j].getNomCrediteur()
                + enteteLot.operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getCodeAcceptation()
                + enteteLot.operations[j].getRefSouscripteur()
                + enteteLot.operations[j].getCodeEndossement()
                + enteteLot.operations[j].getNumCedant()
                + enteteLot.operations[j].getCodeAval() + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printEffets46(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getRio().getRio()
                + enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem()
                + enteteLot.operations[j].getIPac()
                + enteteLot.operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteLot.operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteLot.operations[j].getRibDebiteur()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getCodeFrais()
                + enteteLot.operations[j].getMontantFrais()
                + enteteLot.operations[j].getMontantBrut()
                + enteteLot.operations[j].getNomSouscripteur()
                + enteteLot.operations[j].getAdrSouscripteur()
                + enteteLot.operations[j].getNomCrediteur()
                + enteteLot.operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getCodeAcceptation()
                + enteteLot.operations[j].getRefSouscripteur()
                + enteteLot.operations[j].getCodeEndossement()
                + enteteLot.operations[j].getProtet()
                + enteteLot.operations[j].getCodeAval()
                + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printEnteteLot(EnteteLot enteteLot) throws Exception {
        String line;
        //lotcom = CMPUtility.insertLotcom(enteteLot, remcom);
        line = new String(enteteLot.getIdEntete()
                + enteteLot.getIdBanRem()
                + "000"
                + //                enteteLot.getTypeOperation() +
                enteteLot.getRefLot()
                + CMPUtility.getDevise()
                + enteteLot.getNbOperations()
                + enteteLot.getMontantTotal()
                + enteteLot.getBlancs());
        return line;
    }

    private void updateEtatOperations(DataBase db) throws SQLException {
        String l_sql = "";

        if (remiseHasCheques) {
//            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPERETENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERET");
//            db.executeUpdate(l_sql);
            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERETREC");
            db.executeUpdate(l_sql);
            //MAJ DES CHEQUES SANS IMAGES AVEC MOTIF REJET 215
            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM2") + ",LOTSIB=" + "1" + " , MOTIFREJET='215' WHERE ETAT=" + Utility.getParam("CETAOPERET") + " AND BANQUEREMETTANT IN  (SELECT CODEBANQUE FROM BANQUES B WHERE B.ALGORITHMEDECONTROLESPECIFIQUE =" + Utility.getParam("BANQUE_EIS") + " ) ";
            db.executeUpdate(l_sql);

        }

        if (remiseHasRejetsCheques) {
            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEREJRETENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPEREJRET");
            db.executeUpdate(l_sql);

        }
        if (remiseHasRejetsPrelevements) {
            l_sql = "UPDATE PRELEVEMENTS SET ETAT=" + Utility.getParam("CETAOPEREJRETENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPEREJRET");
            db.executeUpdate(l_sql);

        }

        if (remiseHasRejetsEffets) {
            l_sql = "UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEREJRETENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPEREJRET");
            db.executeUpdate(l_sql);

        }

        if (remiseHasVirements) {
            l_sql = "UPDATE VIREMENTS SET ETAT=" + Utility.getParam("CETAOPERETENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERET");
            db.executeUpdate(l_sql);
            l_sql = "UPDATE VIREMENTS SET ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERETREC");
            db.executeUpdate(l_sql);

        }

        if (remiseHasEffets) {
            l_sql = "UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPERETENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERET");
            db.executeUpdate(l_sql);
            l_sql = "UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERETREC");
            db.executeUpdate(l_sql);
        }

        if (remiseHasPrelevements) {
            l_sql = "UPDATE PRELEVEMENTS SET ETAT=" + Utility.getParam("CETAOPERETENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERET");
            db.executeUpdate(l_sql);
            l_sql = "UPDATE PRELEVEMENTS SET ETAT=" + Utility.getParam("CETAOPERETRECENVSIB") + ",LOTSIB=" + "1" + " WHERE ETAT = " + Utility.getParam("CETAOPERETREC");
            db.executeUpdate(l_sql);
        }

    }

}
