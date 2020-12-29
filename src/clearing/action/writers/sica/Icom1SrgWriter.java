/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.sica;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.EnteteRemise;
import clearing.model.Operation;
import clearing.model.RIO;
import clearing.table.Banques;
import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Lotcom;
import clearing.table.Prelevements;
import clearing.table.Remcom;
import clearing.table.Virements;
import clearing.utils.StaticValues;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Vector;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class Icom1SrgWriter extends FlatFileWriter {

    private String sql = "";
    private int refLot = 0;
    private Vector<EnteteLot> vEnteteLots = new Vector<EnteteLot>();
    private String sequence;
    EnteteRemise enteteRemise = new EnteteRemise();
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
    Prelevements prelevements[] = null;
    Remcom remcom = null;
    Lotcom lotcom = null;
    private boolean remiseHasCheques = false;
    private boolean remiseHasEffets = false;
    private boolean remiseHasVirements = false;
    private boolean remiseHasPrelevements = false;

    public Icom1SrgWriter() {

        setDescription("Envoi ICOM1 Sous Regional vers la BCEAO ");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        if (Utility.getParam("ENVOI_ICOM_SRG") == null || Utility.getParam("ENVOI_ICOM_SRG").equals("0")) {
            if (Utility.getParam("ATTENTE_ICOMA_SRG") == null || Utility.getParam("ATTENTE_ICOMA_SRG").equals("0")) {

                Banques banques[] = CMPUtility.getBanquesSousRegionales(db, " ETAT = " + Utility.getParam("CETAOPEALLICOM1"));

                if (banques != null) {
                    if (Utility.getParam("ENVOI_ICOM_SRG") != null) {
                        db.executeUpdate("UPDATE PARAMS SET VALEUR='1' WHERE NOM='ENVOI_ICOM_SRG'");
                        Utility.clearParamsCache();
                    }
                    for (int i = 0; i < banques.length; i++) {
                        Banques banque = banques[i];
                        //Recuperation des cheques d'une Banque
                        sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_CHEQUE ='" + Utility.getParam("CHEACCIMASCAANCNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        prepareCheques(cheques);
                        //Recuperation des cheques "+ Utility.getParam("CHEACCIMASCANOUNOR") +" d'une Banque
                        sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_CHEQUE ='" + Utility.getParam("CHEACCIMASCANOUNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        prepareCheques(cheques35);
                        //Recuperation des virements CLIENTELE d'une Banque
                        sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRSTACLIANCNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        virements10 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                        prepareVirements(virements10);
                        //Recuperation des virements CLIENTELE "+ Utility.getParam("VIRSTACLINOUNOR") +" d'une Banque
                        sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRSTACLINOUNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        virements15 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                        prepareVirements(virements15);
                        //Recuperation des virements BANQUE A BANQUE d'une Banque
                        sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRBANBANANCNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        virements11 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                        prepareVirements(virements11);
                        //Recuperation des virements MISE A DISPOSITION d'une Banque
                        sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRMISDISANCNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        virements12 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                        prepareVirements(virements12);
                        //Recuperation des virements MISE A DISPOSITION "+ Utility.getParam("VIRMISDISNOUNOR") +" d'une Banque
                        sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRMISDISNOUNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        virements17 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                        prepareVirements(virements17);
                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_EFFET ='" + Utility.getParam("BILORDACCIMASCA") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        effets40 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets40);
                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_EFFET ='" + Utility.getParam("BILORDNOUNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets45);

                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_EFFET ='" + Utility.getParam("LETCHAACCIMASCA") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        effets41 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets41);
                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_EFFET ='" + Utility.getParam("LETCHANOUNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets46);

                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_EFFET ='" + Utility.getParam("BILORDACCVALPAPANCNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        effets42 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets42);
                        //Recuperation des effets d'une Banque
                        sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_EFFET ='" + Utility.getParam("LETCHAACCVALPAPANCNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        effets43 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        prepareEffets(effets43);

                        //Recuperation des prelevements d'une Banque
                        sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM1") + " AND TYPE_PRELEVEMENT ='" + Utility.getParam("ORDPRENOUNOR") + "' AND BANQUE LIKE '" + banque.getCodebanque() + "'";
                        prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
                        preparePrelevements(prelevements);

                    }

                }

                if (refLot > 0) {
                    printRemise(db);
                    updateEtatOperations(db);
                    if (Utility.getParam("ENVOI_ICOM_SRG") != null) {
                        db.executeUpdate("UPDATE PARAMS SET VALEUR='0' WHERE NOM='ENVOI_ICOM_SRG'");
                        Utility.clearParamsCache();
                    }
                    if (Utility.getParam("ATTENTE_ICOMA_SRG") != null) {
                        db.executeUpdate("UPDATE PARAMS SET VALEUR='1' WHERE NOM='ATTENTE_ICOMA_SRG'");
                        Utility.clearParamsCache();
                    }

                } else {
                    if (Utility.getParam("ENVOI_ICOM_SRG") != null) {
                        db.executeUpdate("UPDATE PARAMS SET VALEUR='0' WHERE NOM='ENVOI_ICOM_SRG'");
                        Utility.clearParamsCache();
                    }
                    logEvent("WARNING", "Il n'y a aucun element disponible");
                    setDescription(getDescription() + " - WARNING: Il n'y a aucun element disponible");
                }

            } else {
                logEvent("WARNING", "Le Système est en attente d'un ICOMA|ATTENTE_ICOMA_SRG=1");
                setDescription(getDescription() + " - WARNING: Le Système est en attente d'un ICOMA|ATTENTE_ICOMA_SRG=1");
            }
        } else {
            logEvent("WARNING", "Un ICOM1 est en cours d'envoi|ENVOI_ICOM_SRG=1");
            setDescription(getDescription() + " - WARNING:Un ICOM1 est en cours d'envoi|ENVOI_ICOM_SRG=1");
        }

        db.close();

    }

    private void feedOperationWithPrelevement(int j, Operation[] operations, Prelevements prelevement) {
        operations[j].setIdAgeRem(prelevement.getAgenceremettant());
        operations[j].setIPac(prelevement.getVille());
        operations[j].setNumIntOrdre(String.valueOf(prelevement.getNumeroprelevement()));
        operations[j].setIdObjetOrigine(prelevement.getIdprelevement());

        operations[j].setRibDebiteur(prelevement.getBanque() + prelevement.getAgence() + Utility.bourrageGZero(prelevement.getNumerocompte_Tire(), 12) + Utility.computeCleRIB(prelevement.getBanque(), prelevement.getAgence(), Utility.bourrageGZero(prelevement.getNumerocompte_Tire(), 12)));
        operations[j].setMontant(prelevement.getMontantprelevement());

        operations[j].setNomSouscripteur(prelevement.getNom_Tire());
        operations[j].setAdrSouscripteur(prelevement.getAdresse_Tire());
        operations[j].setNomCrediteur(prelevement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(prelevement.getAdresse_Beneficiaire());
        operations[j].setRibCrediteur(prelevement.getBanqueremettant() + prelevement.getAgenceremettant() + Utility.bourrageGZero(prelevement.getNumerocompte_Beneficiaire(), 12) + Utility.computeCleRIB(prelevement.getBanqueremettant(), prelevement.getAgenceremettant(), Utility.bourrageGZero(prelevement.getNumerocompte_Beneficiaire(), 12)));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(prelevement.getDatetraitement(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(Utility.bourrageDroite(prelevement.getLibelle(), 70, " "));
        operations[j].setRefEmetteur(Utility.bourrageDroite(prelevement.getReference_Emetteur(), 24, " "));
        operations[j].setBlancs(createBlancs(149, " "));
    }

    private void feedOperationWithBltOrdre(int j, Operation[] operations, Effets effet) {
        operations[j].setIdAgeRem(effet.getAgenceremettant());
        operations[j].setIPac(effet.getVille());
        //operations[j].setNumIntOrdre(String.valueOf(effet.getIdeffet()));
        
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
       // operations[j].setNumIntOrdre(String.valueOf(effet.getIdeffet()));
        
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
        operations[j].setTypeOperation(cheque.getType_Cheque());
        operations[j].setRefOperation("" + cheque.getIdcheque());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setIPac(cheque.getVilleremettant());
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
        operations[j].setTypeOperation(cheque.getType_Cheque());
        operations[j].setRefOperation("" + ((cheque.getReference_Operation_Interne() != null) ? cheque.getReference_Operation_Interne() : cheque.getIdcheque()));
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setIPac(cheque.getVilleremettant());
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
        operations[j].setIdAgeRem(effet.getAgenceremettant());
        operations[j].setIPac(effet.getVille());
        //operations[j].setNumIntOrdre(String.valueOf(effet.getIdeffet()));
        
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
        //operations[j].setNumIntOrdre(String.valueOf(effet.getIdeffet()));
        
        operations[j].setNumIntOrdre(effet.getNumeroeffet());
        
        operations[j].setDateEcheance(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")));

        operations[j].setRibDebiteur(effet.getBanque() + effet.getAgence() + Utility.bourrageGZero(effet.getNumerocompte_Tire(), 12) + Utility.computeCleRIB(effet.getBanque(), effet.getAgence(), Utility.bourrageGZero(effet.getNumerocompte_Tire(), 12)));
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

    private void feedOperationWithVirement10(Virements virement, int j, Operation[] operations) {
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
        if (Character.isDigit(virement.getBanqueremettant().charAt(1))) {//Ancienne Norme
            operations[j].setFlagIBANDeb("1");
        } else {//Nouvelle Norme
            operations[j].setFlagIBANDeb("2");
        }

        operations[j].setRibDebiteur(virement.getBanqueremettant() + virement.getAgenceremettant() + Utility.bourrageGZero(virement.getNumerocompte_Tire(), 12) + Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(), Utility.bourrageGZero(virement.getNumerocompte_Tire(), 12)));

        if (Character.isDigit(virement.getBanque().charAt(1))) {//Ancienne Norme
            operations[j].setFlagIBANCre("1");
        } else {//Nouvelle Norme
            operations[j].setFlagIBANCre("2");
        }
        operations[j].setRibCrediteur(virement.getBanque() + virement.getAgence() + Utility.bourrageGZero(virement.getNumerocompte_Beneficiaire(), 12) + Utility.computeCleRIB(virement.getBanque(), virement.getAgence(), Utility.bourrageGZero(virement.getNumerocompte_Beneficiaire(), 12)));

        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setAdrDebiteur(virement.getAdresse_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(virement.getAdresse_Beneficiaire());
        operations[j].setNumIntOrdre(String.valueOf(virement.getIdvirement()));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(65, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void feedOperationWithVirement11(Operation[] operations, int j, Virements virement) {
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

    private void preparePrelevements(Prelevements[] prelevements) throws SQLException {
        if (prelevements != null && prelevements.length > 0) {
            remiseHasPrelevements = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
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
        }
    }

    private void prepareCheques(Cheques[] cheques) throws NumberFormatException, SQLException {

        if (cheques != null && cheques.length > 0) {
            remiseHasCheques = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(cheques[0].getType_Cheque());
            enteteLot.setIdBanRem(cheques[0].getBanque());
            enteteLot.setBlancs(createBlancs(24, " "));
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
        }
    }

    private void prepareEffets(Effets[] effets) throws SQLException {
        if (effets != null && effets.length > 0) {
            remiseHasEffets = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(effets[0].getType_Effet());
            enteteLot.setIdBanRem(effets[0].getBanque());
            enteteLot.setBlancs(createBlancs(24, " "));
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
        }
    }

    private void prepareVirements(Virements[] virements) throws SQLException {
        if (virements != null && virements.length > 0) {
            remiseHasVirements = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot(Utility.bourrageGZero("" + ++refLot, 3));
            enteteLot.setRefBancaire(Utility.bourrageGZero(enteteLot.getRefLot(), 5));
            enteteLot.setTypeOperation(virements[0].getType_Virement());
            enteteLot.setIdBanRem(virements[0].getBanque());
            enteteLot.setBlancs(createBlancs(24, " "));
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
        }
    }

    private String printPrelevements(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + "2"
                + enteteRemise.enteteLots[i].operations[j].getRibCrediteur()
                + "2"
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getRefEmetteur()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printCheques(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation() + enteteRemise.enteteLots[i].operations[j].getIdAgeRem() + enteteRemise.enteteLots[i].operations[j].getIPac() + enteteRemise.enteteLots[i].operations[j].getFlagIBANCre() + enteteRemise.enteteLots[i].operations[j].getPfxIBANCre() + enteteRemise.enteteLots[i].operations[j].getNumCheque() + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd") + enteteRemise.enteteLots[i].operations[j].getIdBanDeb() + enteteRemise.enteteLots[i].operations[j].getIdAgeDeb() + enteteRemise.enteteLots[i].operations[j].getNumCptDeb() + enteteRemise.enteteLots[i].operations[j].getCleRibDeb() + enteteRemise.enteteLots[i].operations[j].getMontant() + enteteRemise.enteteLots[i].operations[j].getNomCrediteur() + enteteRemise.enteteLots[i].operations[j].getCodeCertif() + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printCheques35(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIPac()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getNumCheque()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getIdBanDeb()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeDeb()
                + enteteRemise.enteteLots[i].operations[j].getNumCptDeb()
                + enteteRemise.enteteLots[i].operations[j].getCleRibDeb()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getCodeCertif()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printVirements10(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANCre()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANCre()
                + enteteRemise.enteteLots[i].operations[j].getRibCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printVirements15(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANCre()
                + enteteRemise.enteteLots[i].operations[j].getRibCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printVirements11(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation() + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIdBanCre()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeCre()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getRefEmetteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle() + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printVirements12(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getIdBanCre()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeCre()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getRefEmetteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printVirements17(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getIdBanCre()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeCre()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getRefEmetteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printEffets4042(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIPac()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getCodeFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantBrut()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getCodeAcceptation()
                + enteteRemise.enteteLots[i].operations[j].getRefSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getCodeEndossement()
                + enteteRemise.enteteLots[i].operations[j].getNumCedant()
                + enteteRemise.enteteLots[i].operations[j].getCodeAval()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printEffets45(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation() + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIPac()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getCodeFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantBrut()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getCodeAcceptation()
                + enteteRemise.enteteLots[i].operations[j].getRefSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getCodeEndossement()
                + enteteRemise.enteteLots[i].operations[j].getProtet()
                + enteteRemise.enteteLots[i].operations[j].getCodeAval() + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printEffets4143(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation() + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIPac()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getCodeFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantBrut()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getCodeAcceptation()
                + enteteRemise.enteteLots[i].operations[j].getRefSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getCodeEndossement()
                + enteteRemise.enteteLots[i].operations[j].getNumCedant()
                + enteteRemise.enteteLots[i].operations[j].getCodeAval() + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printEffets46(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation() + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIPac()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getCodeFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantBrut()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getCodeAcceptation()
                + enteteRemise.enteteLots[i].operations[j].getRefSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getCodeEndossement()
                + enteteRemise.enteteLots[i].operations[j].getProtet()
                + enteteRemise.enteteLots[i].operations[j].getCodeAval()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printEnteteLot(EnteteRemise enteteRemise, Remcom remcom, int i) throws Exception {
        String line;
        lotcom = CMPUtility.insertLotcom(enteteRemise.enteteLots[i], remcom);
        line = new String(enteteRemise.enteteLots[i].getIdEntete() + enteteRemise.enteteLots[i].getRefLot() + enteteRemise.enteteLots[i].getRefBancaire() + enteteRemise.enteteLots[i].getTypeOperation() + enteteRemise.enteteLots[i].getIdBanRem() + enteteRemise.enteteLots[i].getNbOperations() + enteteRemise.enteteLots[i].getMontantTotal() + enteteRemise.enteteLots[i].getBlancs());
        return line;
    }

    private String printEnteteRemise(EnteteRemise enteteRemise) throws Exception {
        String line;

        enteteRemise.enteteLots = vEnteteLots.toArray(new EnteteLot[vEnteteLots.size()]);

        enteteRemise.setIdEntete("EREM");
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            enteteRemise.setIdEmetteur("KSCSR");
        } else {
            enteteRemise.setIdEmetteur("SNSSR");
        }

        sequence = Utility.bourrageGZero(Utility.computeCompteur("ICOM_SRG", Utility.getParam("DATECOMPENS_SRG")), 3);
        enteteRemise.setRefRemise(sequence);
        enteteRemise.setDatePresentation(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_SRG"), "yyyyMMdd"));
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanque());
        } else {
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanqueSica3());
        }
        enteteRemise.setDevise(CMPUtility.getDevise());
        enteteRemise.setTypeRemise("ICOM1");
        enteteRemise.setRefRemRelatif("000");
        enteteRemise.setNbLots("" + refLot);
        enteteRemise.setBlancs(createBlancs(15, " "));

        String fileName = CMPUtility.getSrgFileName(sequence, enteteRemise.getTypeRemise());
        setOut(createFlatFile(fileName));
        line = new String(enteteRemise.getIdEntete() + enteteRemise.getIdEmetteur() + enteteRemise.getRefRemise() + Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd") + enteteRemise.getIdRecepeteur() + enteteRemise.getDevise() + enteteRemise.getTypeRemise() + enteteRemise.getRefRemRelatif() + enteteRemise.getNbLots() + enteteRemise.getBlancs());
        return line;
    }

    private void printRemise(DataBase db) throws Exception {
        String line = "";
        String l_sql = "";
        line = printEnteteRemise(enteteRemise);
        writeln(line);

        remcom = CMPUtility.insertRemcom(enteteRemise, Integer.parseInt(Utility.getParam("CETAOPEALLPREICOM1")));
        for (int i = 0; i < enteteRemise.enteteLots.length; i++) {
            line = printEnteteLot(enteteRemise, remcom, i);
            writeln(line);
            for (int j = 0; j < enteteRemise.enteteLots[i].operations.length; j++) {
                RIO rio;
                if (Utility.getParam("VERSION_SICA").equals("2")) {
                    rio = new RIO(CMPUtility.getCodeBanque() + CMPUtility.getPacSCSR() + CMPUtility.getDevise() + sequence + Utility.getParam("DATECOMPENS_SRG") + enteteRemise.enteteLots[i].getRefBancaire() + enteteRemise.enteteLots[i].operations[j].getRefOperation());
                } else {
                    rio = new RIO(CMPUtility.getCodeBanqueSica3() + CMPUtility.getPacSCSRSICA3() + CMPUtility.getDevise() + sequence + Utility.getParam("DATECOMPENS_SRG") + enteteRemise.enteteLots[i].getRefBancaire() + enteteRemise.enteteLots[i].operations[j].getRefOperation());
                }

                switch (Integer.parseInt(enteteRemise.enteteLots[i].getTypeOperation())) {
                    case StaticValues.VIR_CLIENT: {
                        line = printVirements10(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE VIREMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDVIREMENT =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);

                    }
                    ;
                    break;
                    case StaticValues.VIR_CLIENT_SICA3: {
                        line = printVirements15(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE VIREMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDVIREMENT =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);

                    }
                    ;
                    break;
                    case StaticValues.VIR_BANQUE: {
                        line = printVirements11(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE VIREMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDVIREMENT =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                    case StaticValues.VIR_DISPOSITION: {
                        line = printVirements12(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE VIREMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDVIREMENT =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                    case StaticValues.VIR_DISPOSITION_SICA3: {
                        line = printVirements17(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE VIREMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDVIREMENT =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                    case StaticValues.PRELEVEMENT: {
                    }
                    ;
                    break;
                    case StaticValues.CHQ_SCAN:
                        ;
                    case StaticValues.CHQ_PAP: {
                        line = printCheques(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE CHEQUES SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDCHEQUE =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                    case StaticValues.CHQ_SCAN_SICA3: {
                        line = printCheques35(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE CHEQUES SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDCHEQUE =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                    case StaticValues.BLT_ORD_SCAN:
                        ;
                    case StaticValues.BLT_ORD_PAP: {
                        line = printEffets4042(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE EFFETS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDEFFET =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                    case StaticValues.BLT_ORD_SCAN_SICA3: {
                        line = printEffets45(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE EFFETS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDEFFET =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                    case StaticValues.LTR_CHG_SCAN:
                        ;
                    case StaticValues.LTR_CHG_PAP: {
                        line = printEffets4143(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE EFFETS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDEFFET =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                    case StaticValues.LTR_CHG_SCAN_SICA3: {
                        line = printEffets46(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE EFFETS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDEFFET =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                    case StaticValues.PRELEVEMENT_SICA3: {
                        line = printPrelevements(enteteRemise, j, i);
                        writeln(line);
                        l_sql = "UPDATE PRELEVEMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ", RIO='" + rio.getRio() + "',ETAT=" + Utility.getParam("CETAOPEALLPREICOM1") + " WHERE IDPRELEVEMENT =" + enteteRemise.enteteLots[i].operations[j].getIdObjetOrigine();
                        db.executeUpdate(l_sql);
                    }
                    ;
                    break;
                }
            }
        }

        writeEOF("FREM", createBlancs(28, " "));
    }

    private void updateEtatOperations(DataBase db) throws SQLException {
        String l_sql = "";
        remcom.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPEALLICOM1ENV"))));
        db.updateRowByObjectByQuery(remcom, "REMCOM", "IDREMCOM=" + remcom.getIdremcom());
        l_sql = "UPDATE LOTCOM SET ETAT=" + Utility.getParam("CETAOPEALLICOM1ENV") + " WHERE IDREMCOM=" + remcom.getIdremcom();
        db.executeUpdate(l_sql);
        if (remiseHasCheques) {
            l_sql = "UPDATE CHEQUES SET DATECOMPENSATION='" + Utility.convertDateToString(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_SRG"), "yyyyMMdd"), ResLoader.getMessages("patternDate")) + "',ETAT=" + Utility.getParam("CETAOPEALLICOM1ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM1");
            db.executeUpdate(l_sql);
        }

        if (remiseHasVirements) {
            l_sql = "UPDATE VIREMENTS SET DATECOMPENSATION='" + Utility.convertDateToString(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_SRG"), "yyyyMMdd"), ResLoader.getMessages("patternDate")) + "',ETAT=" + Utility.getParam("CETAOPEALLICOM1ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM1");
            db.executeUpdate(l_sql);
        }

        if (remiseHasEffets) {
            l_sql = "UPDATE EFFETS SET DATECOMPENSATION='" + Utility.convertDateToString(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_SRG"), "yyyyMMdd"), ResLoader.getMessages("patternDate")) + "',ETAT=" + Utility.getParam("CETAOPEALLICOM1ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM1");
            db.executeUpdate(l_sql);
        }
        if (remiseHasPrelevements) {
            l_sql = "UPDATE PRELEVEMENTS SET DATECOMPENSATION='" + Utility.convertDateToString(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"), ResLoader.getMessages("patternDate")) + "',ETAT=" + Utility.getParam("CETAOPEALLICOM1ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM1");
            db.executeUpdate(l_sql);
        }

    }
}
