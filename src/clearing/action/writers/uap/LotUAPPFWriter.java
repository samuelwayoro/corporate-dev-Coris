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
import clearing.table.Remises;
import clearing.table.Virements;
import clearing.utils.StaticValues;
import java.io.File;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class LotUAPPFWriter extends FlatFileWriter {

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
    private boolean remiseHasCheques = false;
    private boolean remiseHasEffets = false;
    private boolean remiseHasVirements = false;

    public LotUAPPFWriter() {
        setDescription("Envoi de Lot d'op�ration aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String whereClause = " ETAT=" + Utility.getParam("CETAOPEVAL") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";
        String whereClause2 = " ETAT=" + Utility.getParam("CETAOPEVAL2") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";


        //Recuperation des cheques 030
//                sql = "SELECT * FROM CHEQUES WHERE  " +whereClause+ " AND TYPE_CHEQUE ='030'" ;
//                cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
//                prepareCheques(cheques);
//            

        //Recuperation des cheques 035 
        sql = "SELECT * FROM CHEQUES WHERE  " + whereClause + "  AND TYPE_CHEQUE ='035'";
        cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        prepareCheques(cheques35);


        //Recuperation des virements CLIENTELE 
        //sql = "SELECT * FROM VIREMENTS WHERE  ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_VIREMENT ='010' ";
        //virements10 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        //prepareVirements(virements10);


        //Recuperation des virements CLIENTELE 015 
        sql = "SELECT * FROM VIREMENTS WHERE " + whereClause2 + " AND TYPE_VIREMENT ='015' ORDER BY NUMEROCOMPTE_TIRE";
        virements15 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        if (virements15 != null && virements15.length > 0) {
            int j = 0;
            for (int i = 0; i < virements15.length; i += j) {
                sql = "SELECT * FROM VIREMENTS WHERE " + whereClause2 + " AND NUMEROCOMPTE_TIRE='" + virements15[i].getNumerocompte_Tire() + "' ";
                Virements[] virementsSameCompteTireFichier = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                if (virementsSameCompteTireFichier != null && virementsSameCompteTireFichier.length > 0) {
                    j = virementsSameCompteTireFichier.length;

                    prepareVirements(virementsSameCompteTireFichier);
                }

            }

        }



        //Recuperation des virements BANQUE A BANQUE d'une Banque
        sql = "SELECT * FROM VIREMENTS WHERE " + whereClause2 + " AND TYPE_VIREMENT ='011' ";
        virements11 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements11);

        //Recuperation des virements MISE A DISPOSITION d'une Banque
        //sql = "SELECT * FROM VIREMENTS WHERE ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_VIREMENT ='012' ";
        //virements12 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        //prepareVirements(virements12);

        //Recuperation des virements MISE A DISPOSITION 017 d'une Banque
        sql = "SELECT * FROM VIREMENTS WHERE " + whereClause2 + " AND TYPE_VIREMENT ='017' ";
        virements17 = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        prepareVirements(virements17);

        /*
         //Recuperation des effets d'une Banque
         sql = "SELECT * FROM EFFETS WHERE  ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='040' ";
         effets40 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
         prepareEffets(effets40);

         //Recuperation des effets d'une Banque
         sql = "SELECT * FROM EFFETS WHERE ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='045'";
         effets45 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
         prepareEffets(effets45);

         //Recuperation des effets d'une Banque
         sql = "SELECT * FROM EFFETS WHERE ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='041' ";
         effets41 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
         prepareEffets(effets41);

         //Recuperation des effets d'une Banque
         sql = "SELECT * FROM EFFETS WHERE ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='046'";
         effets46 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
         prepareEffets(effets46);

         //Recuperation des effets d'une Banque
         sql = "SELECT * FROM EFFETS WHERE ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='042' ";
         effets42 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
         prepareEffets(effets42);

         //Recuperation des effets d'une Banque
         sql = "SELECT * FROM EFFETS WHERE ETAT BETWEEN " + Utility.getParam("CETAOPEALLICOM1ENV") + " AND " + Utility.getParam("CETAOPEALLICOM1ACC") + " AND LOTSIB IS NULL" + " AND TYPE_EFFET ='043' ";
         effets43 = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
         prepareEffets(effets43);*/

        if (refLot > 0) {
            if (remiseHasCheques) {

                setDescription(getDescription() + " ex�cut� avec succ�s:\n Nombre de Ch�ques= " + cheques35.length + " - Montant Total= " + Utility.formatNumber(vEnteteLots.get(0).getMontantTotal()));
                logEvent("INFO", "Nombre de Ch�que= " + cheques35.length + " - Montant Total= " + Utility.formatNumber("" + vEnteteLots.get(0).getMontantTotal()));
            }

            if (remiseHasVirements) {

                setDescription("\n" + getDescription() + " ex�cut� avec succ�s:\n Nombre de Virements = " + virements15.length);
                logEvent("INFO", "Nombre de Virements = " + virements15.length);

            }
        } else {
            logEvent("WARNING", "Il n'y a aucun �l�ment disponible");
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
        operations[j].setRefEmetteur(Utility.bourrageGauche(cheque.getCompteremettant(), 11, "0"));
        operations[j].setCodeCertif(cheque.getCodecertification());
        operations[j].setBlancs(createBlancs(286, " "));
    }

    private void feedOperationWithCheque35(int j, Cheques cheque, Operation[] operations) {
        operations[j].setTypeOperation(cheque.getType_Cheque());
        operations[j].setRefOperation("" + cheque.getIdcheque());
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        try {
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        } catch (Exception ex) {
            Logger.getLogger(LotUAPPFWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        String l_sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheque.getRemise();
        Remises[] remises = (Remises[]) db.retrieveRowAsObject(l_sql, new Remises());
        db.close();
        if (remises != null && remises.length > 0) {
            operations[j].setIdAgeRem(remises[0].getAgenceDepot());
        } else {
            operations[j].setIdAgeRem(cheque.getMachinescan().substring(0, 5));
        }

        operations[j].setIdAgeCre(cheque.getAgenceremettant());
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
        operations[j].setNumCedant("" + cheque.getRemise());
        operations[j].setRefEmetteur(Utility.bourrageGauche(cheque.getCompteremettant(), 12, "0"));
        operations[j].setCodeAval((cheque.getEscompte().intValue() == 0) ? "E" : "C");
        operations[j].setCodeCertif("0");
        operations[j].setAdrSouscripteur(Utility.bourrageDroite(remises[0].getReference(), 50, " "));

        operations[j].setNomSouscripteur(Utility.bourrageDroite(cheque.getCodeutilisateur(), 4, " "));
        operations[j].setBlancs(createBlancs(286, " "));
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

    private void prepareCheques(Cheques[] cheques) throws Exception {

        if (cheques != null && cheques.length > 0) {

            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefLot(Utility.computeCompteur("REFLOT", CMPUtility.getDate()));
            enteteLot.setRefBancaire(enteteLot.getRefLot());
            enteteLot.setTypeOperation(cheques[0].getType_Cheque());
            enteteLot.setIdBanRem(cheques[0].getBanqueremettant());
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
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot(Utility.bourrageGZero("" + ++refLot, 3));
            enteteLot.setRefBancaire(Utility.bourrageGZero(enteteLot.getRefLot(), 5));
            enteteLot.setTypeOperation(virements[0].getType_Virement());
            enteteLot.setIdBanRem(virements[0].getBanqueremettant());
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

    public void printLotVirement(Virements[] virements, EnteteLot enteteLot) throws Exception {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String l_sql = "";

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + virements[0].getBanqueremettant() + ".000." + enteteLot.getRefLot() + "." + virements[0].getType_Virement() + "." + CMPUtility.getDevise() + ".LOT";
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
            l_sql = "UPDATE VIREMENTS SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + ", LOTSIB=1 WHERE  IDVIREMENT=" + virements[j].getIdvirement() + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";
            db.executeUpdate(l_sql);
        }
        db.close();
        closeFile();
    }

    public void printLotCheque(Cheques[] cheques, EnteteLot enteteLot) throws Exception {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String l_sql = "";
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + cheques[0].getBanqueremettant() + ".000." + enteteLot.getRefLot() + "." + cheques[0].getType_Cheque() + "." + CMPUtility.getDevise() + ".LOT";
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
                    line = printCheques(enteteLot, j);
                    writeln(line);
                }
                ;
                break;
            }
            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + ", LOTSIB=1 WHERE  IDCHEQUE=" + cheques[j].getIdcheque() + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";
            db.executeUpdate(l_sql);


        }
        db.close();
        closeFile();
    }

    public void printLotEffet(Effets[] effets, EnteteLot enteteLot) throws Exception {
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + effets[0].getBanqueremettant() + ".001." + enteteLot.getRefLot() + "." + effets[0].getType_Effet() + "." + CMPUtility.getDevise() + ".LOT";
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

    private String printCheques(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getTypeOperation()
                + enteteLot.operations[j].getRefOperation()
                + enteteLot.operations[j].getIdAgeRem()
                + enteteLot.operations[j].getIPac()
                + enteteLot.operations[j].getFlagIBANCre()
                + enteteLot.operations[j].getPfxIBANCre()
                + enteteLot.operations[j].getNumCheque()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getIdBanDeb()
                + enteteLot.operations[j].getIdAgeDeb()
                + enteteLot.operations[j].getNumCptDeb()
                + enteteLot.operations[j].getCleRibDeb()
                + enteteLot.operations[j].getMontant()
                + enteteLot.operations[j].getNumCedant()
                + Utility.convertDateToString(enteteLot.operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteLot.operations[j].getRefEmetteur()
                + enteteLot.operations[j].getCodeAval()
                + enteteLot.operations[j].getIdAgeCre()
                + createBlancs(2, " ")
                + enteteLot.operations[j].getCodeCertif()
                + enteteLot.operations[j].getAdrSouscripteur()
                + createBlancs(50, " ")
                + enteteLot.operations[j].getNomSouscripteur()
                + enteteLot.operations[j].getBlancs());
        return line;
    }

    private String printVirements10(EnteteLot enteteLot, int j) {
        String line;

        line = new String(enteteLot.operations[j].getTypeOperation()
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

        line = new String(enteteLot.operations[j].getTypeOperation()
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

        line = new String(enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem()
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

        line = new String(enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getPfxIBANDeb()
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

        line = new String(enteteLot.operations[j].getTypeOperation()
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

        line = new String(enteteLot.operations[j].getTypeOperation()
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

        line = new String(enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem()
                + enteteLot.operations[j].getIPac()
                + enteteLot.operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteLot.operations[j].getDateEcheance(), "yyyyMMdd")
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

        line = new String(enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem()
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

        line = new String(enteteLot.operations[j].getTypeOperation() + enteteLot.operations[j].getRefOperation() + enteteLot.operations[j].getIdAgeRem()
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
        line = enteteLot.getIdEntete()
     + enteteLot.getIdBanRem()
     + "000"
     + enteteLot.getRefLot()
     + CMPUtility.getDevise()
     + enteteLot.getNbOperations()
     + enteteLot.getMontantTotal()
     + (enteteLot.getTypeOperation().equals("015")?"N":"")
     + enteteLot.getBlancs();
        return line;
    }

    private void updateEtatOperations(DataBase db) throws SQLException {
        String l_sql = "";

        String whereClause = " ETAT=" + Utility.getParam("CETAOPEVAL") + " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";

        if (remiseHasCheques) {
            l_sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + ", LOTSIB=1 WHERE  " + whereClause;
            db.executeUpdate(l_sql);
        }

        if (remiseHasVirements) {
            l_sql = "UPDATE VIREMENTS SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + ", LOTSIB=1 WHERE  " + whereClause;
            db.executeUpdate(l_sql);
        }

        if (remiseHasEffets) {
            l_sql = "UPDATE EFFETS SET SET ETAT=" + Utility.getParam("CETAOPEALLICOM1") + ", LOTSIB=1 WHERE  " + whereClause;
            db.executeUpdate(l_sql);
        }

    }
}