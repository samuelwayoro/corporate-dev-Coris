/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.bfi;

import clearing.table.Cheques;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 * ACPACHENVChequeAllerRejeteAnnWriter Remises d'annulations d'opérations
 * rejetées : Code enregistrement 24 ; icom3 de icom2 CHEQUES
 *
 * @author Patrick
 */
public class ACPACHENVChequeAllerRejeteAnnWriter extends FlatFileWriter {

    public ACPACHENVChequeAllerRejeteAnnWriter() {
        setDescription("Envoi des Cheques   Aller Rejetés Annulés.  vers ACP/ACH");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        Utility.clearParamsCache();
        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
        String dateFichier = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String heureFichier = Utility.convertDateToString(new Date(), "HHmmss");

        String numRemise = Utility.bourrageGauche(Utility.computeCompteur("ENV_CHQ", dateFichier), 7, "0");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-GN-" + Utility.getParam("CODE_BANQUE") + "-" + dateFichier
                + "-" + heureFichier + "-30-24-324.ENV";

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ANN") + ") "
                + "  AND BANQUE<>BANQUEREMETTANT    AND TYPE_CHEQUE='30'  ";
        Cheques[] cheques30 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        if (cheques30 != null && 0 < cheques30.length) {
            setOut(createFlatFile(fileName));
            long sumCheques = 0;

            for (int i = 0; i < cheques30.length; i++) {
                sumCheques += Long.parseLong(cheques30[i].getMontantcheque());

            }
            String line = "101GN";
            line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
            line += heureFichier;
            line += "30" + Utility.getParam("CODE_BANQUE");
            line += dateCompensation + dateCompensation;
            line += numRemise + "14" + "324";
            line += Utility.bourrageGauche(String.valueOf(sumCheques), 15, "0");
            line += Utility.bourrageGauche(String.valueOf(cheques30.length), 10, "0")
                    + createBlancs(233, " ");

            wwriteln(line);

            for (Cheques cheque : cheques30) {
                //Tous les cheques aller
                line = "101GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "30" + Utility.getParam("CODE_BANQUE");
                line += dateCompensation + dateCompensation;
                line += numRemise;
                line += "24" + "324" + Integer.parseInt(cheque.getCalcul()); //Nature du chèque
                  line += Utility.bourrageGauche(cheque.getMontantcheque(), 15, "0"); //Montant du chèque
                line += Utility.bourrageGauche(cheque.getNumerocheque(), 8, "0"); //Numéro du chèque
                line += Utility.bourrageGauche(cheque.getAgence(), 3, "0"); //inversé ici
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");

                line += Utility.bourrageGauche(cheque.getBanque(), 3, "0"); //compte du tireur==celui qu'on debite 18 (code banque, code agence, numéro de compte, clé de controle compte)
                line += Utility.bourrageGauche(cheque.getAgence(), 3, "0");
                line += Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanque(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgence(), 3, "0"),
                        Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNomemetteur()== null ? "BANQUE" : cheque.getNomemetteur(), 30, " ");
                line += Utility.bourrageDroite(cheque.getBanque() == null ? "ADRESSE BANQUE" : cheque.getBanque(), 30, " "); //Adresse du Tireur
                line += Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0") + "GN";

                line += Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"); //Compte du bénéficiaire 
                line += Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0");
                line += Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 30, " ");//Nom ou raison sociale du bénéficiaire
                line += Utility.bourrageDroite(cheque.getBanqueremettant() == null ? "ADRESSE BANQUE" : cheque.getBanqueremettant(), 30, " ");//Adresse du bénéficiaire
                line += Utility.bourrageDroite(Utility.convertDateToString(Utility.convertStringToDate(cheque.getDateemission(), "yyyy/MM/dd"), "ddMMyyyy"), 8, ""); //Date d'émission du chèque 

                line += createBlancs(15, " "); //Motif de représentation
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy"); //Date de règlement
                line += createBlancs(8, "0"); //MOTIF REJET
                line += createBlancs(23, " ");

                wwriteln(line);
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ANN"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ANNACC")));
                    cheque.setDatecompensation(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "ddMMyyyy"), ResLoader.getMessages("patternDate")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
            }
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Cheques Rejetes Annulés= " + cheques30.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Cheques= " + cheques30.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques));
            closeFile();
        } else {

            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        heureFichier = Utility.convertDateToString(new Date(), "HHmmss");

        numRemise = Utility.bourrageGauche(Utility.computeCompteur("ENV_CHQ", dateFichier), 7, "0");
        fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-GN-" + Utility.getParam("CODE_BANQUE") + "-" + dateFichier
                + "-" + heureFichier + "-33-24-324.ENV";

        sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM2ANN") + ") "
                + "  AND BANQUE<>BANQUEREMETTANT    AND TYPE_CHEQUE='33'  ";
        Cheques[] cheques33 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        if (cheques33 != null && 0 < cheques33.length) {
            setOut(createFlatFile(fileName));
            long sumCheques = 0;

            for (int i = 0; i < cheques33.length; i++) {
                sumCheques += Long.parseLong(cheques33[i].getMontantcheque());

            }
            String line = "101GN";
            line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
            line += heureFichier;
            line += "33" + Utility.getParam("CODE_BANQUE");
            line += dateCompensation + dateCompensation;
            line += numRemise + "14" + "324";
            line += Utility.bourrageGauche(String.valueOf(sumCheques), 15, "0");
            line += Utility.bourrageGauche(String.valueOf(cheques33.length), 10, "0")
                    + createBlancs(233, " ");

            wwriteln(line);

            for (int i = 0; i < cheques33.length; i++) {
                Cheques cheque = cheques33[i];
                //Tous les cheques aller
                line = "101GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "33" + Utility.getParam("CODE_BANQUE");
                line += dateCompensation + dateCompensation;
                line += numRemise;
                line += "24" + "324" + Integer.parseInt(cheque.getCalcul()); //Nature du chèque
                line += Utility.bourrageGauche(cheque.getMontantcheque(), 15, "0"); //Montant du chèque
                line += Utility.bourrageGauche(cheque.getNumerocheque(), 8, "0"); //Numéro du chèque
                line += Utility.bourrageGauche(cheque.getAgence(), 3, "0"); //inversé ici
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");

                line += Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"); //compte du tireur==celui qu'on debite 18 (code banque, code agence, numéro de compte, clé de controle compte)
                line += Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0");
                line += Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNombeneficiaire() == null ? "BANQUE" : cheque.getNombeneficiaire(), 30, " ");
                line += Utility.bourrageDroite(cheque.getBanqueremettant() == null ? "ADRESSE BANQUE" : cheque.getBanque(), 30, " "); //Adresse du Tireur
                line += Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0") + "GN";

                line += Utility.bourrageGauche(cheque.getBanque(), 3, "0"); //Compte du bénéficiaire 
                line += Utility.bourrageGauche(cheque.getAgence(), 3, "0");
                line += Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanque(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgence(), 3, "0"),
                        Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNomemetteur(), 30, " ");//Nom ou raison sociale du bénéficiaire
                line += Utility.bourrageDroite(cheque.getBanque() == null ? "ADRESSE BANQUE" : cheque.getBanque(), 30, " ");//Adresse du bénéficiaire
                line += Utility.bourrageDroite(Utility.convertDateToString(Utility.convertStringToDate(cheque.getDateemission(), "yyyy/MM/dd"), "ddMMyyyy"), 8, ""); //Date d'émission du chèque 

                line += createBlancs(15, " "); //Motif de représentation
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy"); //Date de règlement
                line += createBlancs(8, "0"); //MOTIF REJET
                line += createBlancs(23, " ");

                wwriteln(line);
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2ANN"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ANNACC")));
                    cheque.setDatecompensation(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "ddMMyyyy"), ResLoader.getMessages("patternDate")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
            }
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Cheques Rep Rejetes Annulés= " + cheques33.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Cheques Rep Rejetes Annulés= " + cheques33.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques));
            closeFile();
        } else {

            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
