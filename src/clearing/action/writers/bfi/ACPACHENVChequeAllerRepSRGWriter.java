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
 * -	Remises de représentation d'opérations : Code enregistrement 21 ; CHEQUES
 * 33
 *
 * @author Patrick
 */
public class ACPACHENVChequeAllerRepSRGWriter extends FlatFileWriter {

    public ACPACHENVChequeAllerRepSRGWriter() {
        setDescription("Envoi des Cheques Aller Representés Nationaux vers ACP/ACH");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        Utility.clearParamsCache();
//        String dateCompensation = Utility.getParam("DATECOMPENS_NAT");
        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
        String dateFichier = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String heureFichier = Utility.convertDateToString(new Date(), "HHmmss");
//        String numRemise = Utility.bourrageGauche(Utility.computeCompteur("ENV_CHQ", dateFichier), 4, "0");
//        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-CM-" + Utility.getParam("CODE_BANQUE") + "-" + dateFichier
//                + "-" + heureFichier + "-33-21-950.ENV";
        String numRemise = Utility.bourrageGauche(Utility.computeCompteur("ENV_CHQ", dateFichier), 7, "0");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "02-GN-" + Utility.getParam("CODE_BANQUE") + "-" + dateFichier
                + "-" + heureFichier + "-33-21-324.ENV";

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1REP") + "," + Utility.getParam("CETAOPEALLICOM1") + ") "
                + " AND BANQUE<>BANQUEREMETTANT AND TYPE_CHEQUE='33' ORDER BY BANQUE, AGENCE, IDCHEQUE "; //requete de selection de bank regionale
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));

            long sumCheques = 0;

            for (int i = 0; i < cheques.length; i++) {
                sumCheques += Long.parseLong(cheques[i].getMontantcheque());

            }

            String line = "102GN";
            line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
            line += heureFichier;
            line += "33";
            line += Utility.getParam("CODE_BANQUE");
            line += dateCompensation + dateCompensation;
            line += numRemise;
            line += "11" + "324";
            line += Utility.bourrageGauche(String.valueOf(sumCheques), 15, "0");
            line += Utility.bourrageGauche(String.valueOf(cheques.length), 10, "0")
                  + createBlancs(233, " ");

            wwriteln(line);

            for (Cheques cheque : cheques) {
                //Tous les cheques aller
                //Detail
                line = "102GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "33" + Utility.getParam("CODE_BANQUE");
                line += dateCompensation + dateCompensation;
                line += numRemise;
                line += "21" + "324" +  Integer.parseInt(cheque.getCalcul()); //Nature du chèque
                line += Utility.bourrageGauche(cheque.getMontantcheque(), 15, "0"); //Montant du chèque
                line += Utility.bourrageGauche(cheque.getIdcheque().toPlainString(), 8, "0"); //Numéro du chèque
                line += Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");

                line += Utility.bourrageGauche(cheque.getBanque(), 3, "0"); //compte du tireur==celui qu'on debite 18 (code banque, code agence, numéro de compte, clé de contrÃ´le compte)
                line += Utility.bourrageGauche(cheque.getAgence(), 3, "0");
                line += Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanque(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgence(), 3, "0"),
                        Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNomemetteur() == null ? "BANQUE" : cheque.getNomemetteur(), 30, " ");
                line += Utility.bourrageDroite(cheque.getBanque() == null ? "ADRESSE BANQUE" : cheque.getBanque(), 30, " "); //Adresse du Tireur
                line += Utility.bourrageGauche(cheque.getBanque(), 3, "0") + "GN";

                line += Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"); //Compte du bénéficiaire 
                line += Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0");
                line += Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 30, " ");//Nom ou raison sociale du bénéficiaire
                line += Utility.bourrageDroite(cheque.getBanqueremettant() == null ? "ADRESSE BANQUE" : cheque.getBanqueremettant(), 30, " ");//Adresse du bénéficiaire
                line += Utility.bourrageDroite(cheque.getDateemission(), 8, "") ; //Date dâ€™émission du chèque 

                line += createBlancs(15, " "); //Motif de représentation
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy"); //Date de règlement
                line += createBlancs(8, "0"); //MOTIF REJET
                line += createBlancs(23, " ");

                wwriteln(line);
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM1REP"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1REPACC")));
                    cheque.setHeuretraitement(heureFichier);
                    cheque.setRemise(new BigDecimal(numRemise));
                    cheque.setDatecompensation(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "ddMMyyyy"), ResLoader.getMessages("patternDate")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM1"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACC")));
                    cheque.setHeuretraitement(heureFichier);
                    cheque.setRemcom(new BigDecimal(numRemise));
                    cheque.setDatecompensation(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "ddMMyyyy"), ResLoader.getMessages("patternDate")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
            }

            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Cheques Aller Representés Nationaux= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Cheques Aller Representés Nationaux= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques));
            closeFile();
        } else {

            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
