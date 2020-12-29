/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.finacle;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.Operation;
import clearing.table.Virements;
import clearing.utils.StaticValues;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import org.apache.commons.lang.math.NumberUtils;
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
public class VirementAllerReader extends FlatFileReader {

    public VirementAllerReader() {
        setHasNormalExtension(false);
        setExtensionType(END_EXT);
    }

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
            if (line.startsWith("ELOT")) {
                md5.update(getCurrentLine());
                cptOper = -1;
                enteteLots[++cptLot] = new EnteteLot();
                enteteLots[cptLot].setIdEntete(getChamp(4));
                enteteLots[cptLot].setIdBanRem(getChamp(5));
                getChamp(3);
                enteteLots[cptLot].setRefLot(getChamp(3));
                getChamp(3);
                enteteLots[cptLot].setNbOperations(getChamp(4));
                enteteLots[cptLot].setMontantTotal(getChamp(16));
                enteteLots[cptLot].setBlancs(getChamp(26));
                enteteLots[cptLot].operations = new Operation[Integer.parseInt(enteteLots[cptLot].getNbOperations())];
            } else /* Lecture ordre*/ {
                //Commun

                enteteLots[cptLot].operations[++cptOper] = new Operation();

                enteteLots[cptLot].operations[cptOper].setTypeOperation(getChamp(3));
                enteteLots[cptLot].operations[cptOper].setRefOperation(getChamp(8));

                //Particulier
                switch (Integer.parseInt(enteteLots[cptLot].operations[cptOper].getTypeOperation())) {
                    case StaticValues.VIR_CLIENT: {
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
                        System.out.println(" Valeur du type operation :: " + StaticValues.VIR_CLIENT);

                    }

                    break;

                    default: {
                        System.out.println("Type d'operation de virement inconnu");
                    }

                }
            }
        }

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        // if (cptLot != enteteLots.length) System.out.println("WARNING: Nbre de Lot réel différent du nbr de lot marqué dans l'entÃªte de remise.");
        EnteteLot enteteLot1 = enteteLots[0];
        System.out.println(enteteLot1.toString());
        boolean result = true;

        for (Operation operation1 : enteteLot1.operations) {
            Virements virement = new Virements();

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
            virement.setHeuretraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), "HH:mm:ss"));

            switch (Integer.parseInt(operation1.getTypeOperation())) {

                case StaticValues.VIR_CLIENT: {
                    String ribDebiteur = Utility.computeCleRIBACPACH(
                            operation1.getRibDebiteur().substring(0, 3),
                            operation1.getRibDebiteur().substring(3, 6),
                            operation1.getRibDebiteur().substring(6, 16));
                    if (!ribDebiteur.equals(operation1.getRibDebiteur().substring(16, 18))) {
                        result = false;
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        logEvent("ERROR", "CLEF RIB DEBITEUR INCORRRECTE POUR ["
                                + operation1.getRibDebiteur() + "]. CLEF CALCULEE "
                                + ribDebiteur + " CLEF DANS LE FICHIER :"
                                + operation1.getRibDebiteur().substring(16, 18));
                    }

                    String ribCrediteur = Utility.computeCleRIBACPACH(
                            operation1.getRibCrediteur().substring(0, 3),
                            operation1.getRibCrediteur().substring(3, 6),
                            operation1.getRibCrediteur().substring(6, 16));
                    if (!ribCrediteur.equals(operation1.getRibCrediteur().substring(16, 18))) {
                        result = false;
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        logEvent("ERROR", "CLEF RIB CREDITEUR INCORRRECTE POUR ["
                                + operation1.getRibCrediteur() + "]. CLEF CALCULEE "
                                + ribCrediteur + " CLEF DANS LE FICHIER :"
                                + operation1.getRibCrediteur().substring(16, 18));
                    }

                    virement.setBanqueremettant(operation1.getRibDebiteur().substring(0, 3));
                    virement.setAgenceremettant(operation1.getRibDebiteur().substring(3, 6));
                    virement.setNumerocompte_Tire(operation1.getRibDebiteur().substring(6, 16));

                    virement.setBanque(operation1.getRibCrediteur().substring(0, 3));
                    virement.setAgence(operation1.getRibCrediteur().substring(3, 6));
                    virement.setNumerocompte_Beneficiaire(operation1.getRibCrediteur().substring(6, 16));
                    String mntVirement;
                    if (operation1.getMontant() != null & operation1.getMontant().replaceAll("\\p{javaWhitespace}+", "").trim().length() > 0) {
                        mntVirement = operation1.getMontant().replaceAll("\\p{javaWhitespace}+", "");
                        if (NumberUtils.toInt(mntVirement) > 0) {
                            virement.setMontantvirement(String.valueOf(Long.parseLong(mntVirement)));

                        } else {
                            result = false;
                            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                            logEvent("ERROR", "Montant du virement Egal 0 ");
                        }

                    } else {
                        result = false;
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        logEvent("ERROR", "Montant null ");

                    }

                    virement.setNom_Tire(Utility.removeAccent(operation1.getNomDebiteur()));
                    virement.setAdresse_Tire(Utility.removeAccent(operation1.getAdrDebiteur()));
                    virement.setNom_Beneficiaire(Utility.removeAccent(operation1.getNomCrediteur()));
                    virement.setAdresse_Beneficiaire(Utility.removeAccent(operation1.getAdrCrediteur()));
                    virement.setNumerovirement(operation1.getNumIntOrdre());
                    virement.setDateordre(Utility.convertDateToString(operation1.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
                    virement.setLibelle(operation1.getLibelle());
                    virement.setIdvirement(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDVIREMENT", "VIREMENTS"))));

                    db.insertObjectAsRowByQuery(virement, "VIREMENTS");

                }

                break;
                default: {
                    System.out.println("Type d'operation de virement inconnu");
                }

            }
        }
        db.close();
        return aFile;
    }
}
