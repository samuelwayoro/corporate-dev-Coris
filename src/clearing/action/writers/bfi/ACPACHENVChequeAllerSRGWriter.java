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

import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ACPACHENVChequeAllerSRGWriter extends FlatFileWriter {

    public ACPACHENVChequeAllerSRGWriter() {
        setDescription("Envoi des Cheques Aller Regionaux vers ACP/ACH");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
//        String dateCompensation = Utility.getParam("DATECOMPENS_NAT");
        String dateFichier = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String heureFichier = Utility.convertDateToString(new Date(), "HHmmss");
        String numRemise = Utility.bourrageGauche(Utility.computeCompteur("ENV_CHQ", dateFichier), 7, "0");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "02-GN-" + Utility.getParam("CODE_BANQUE") + "-" + dateFichier
                + "-" + heureFichier + "-30-21-324.ENV";

        String sql = "SELECT * FROM CHEQUES  WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1") + ") AND TYPE_CHEQUE='30'  ORDER BY BANQUE, AGENCE, IDCHEQUE "; //requete de selection de bank srg
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        if (cheques != null && 0 < cheques.length) {
            setOut(createFlatFile(fileName));
            long sumCheques = 0;

            for (int i = 0; i < cheques.length; i++) {
                sumCheques += Long.parseLong(cheques[i].getMontantcheque());

            }
            //enregistrement global
            String line = "102GN";
            line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
            line += heureFichier;
            line += "30";
            line += Utility.getParam("CODE_BANQUE");
            line += dateCompensation + dateCompensation;
            line += numRemise;
            line += "11" + "324";
            line += Utility.bourrageGauche(String.valueOf(sumCheques), 15, "0");
            line += Utility.bourrageGauche(String.valueOf(cheques.length), 10, "0")
                    + createBlancs(233, " ");

            wwriteln(line);

            //enregistrement detail
            for (int i = 0; i < cheques.length; i++) {
                Cheques cheque = cheques[i];
                //Tous les cheques aller
                line = "102GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "30" + Utility.getParam("CODE_BANQUE");
                line += dateCompensation + dateCompensation;
                line += numRemise;
                line += "21" + "324" + Integer.parseInt(cheque.getCalcul()); //Nature du ch�que
                line += Utility.bourrageGauche(cheque.getMontantcheque(), 15, "0"); //Montant du ch�que
                line += Utility.bourrageGauche(cheque.getIdcheque().toPlainString(), 8, "0"); //Num�ro du ch�que
                line += Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");

                line += Utility.bourrageGauche(cheque.getBanque(), 3, "0"); //compte du tireur==celui qu'on debite 18 (code banque, code agence, num�ro de compte, cl� de contrôle compte)
                line += Utility.bourrageGauche(cheque.getAgence(), 3, "0");
                line += Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanque(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgence(), 3, "0"),
                        Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNomemetteur() == null ? "BANQUE" : cheque.getNomemetteur(), 30, " ");
                line += Utility.bourrageDroite(cheque.getBanque() == null ? "ADRESSE BANQUE" : cheque.getBanque(), 30, " "); //Adresse du Tireur
                line += Utility.bourrageGauche(cheque.getBanque(), 3, "0") + "GN";

                line += Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"); //Compte du b�n�ficiaire 
                line += Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0");
                line += Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 30, " ");//Nom ou raison sociale du b�n�ficiaire
                line += Utility.bourrageDroite(cheque.getBanqueremettant() == null ? "ADRESSE BANQUE" : cheque.getBanqueremettant(), 30, " ");//Adresse du b�n�ficiaire
                line += Utility.bourrageDroite(cheque.getDateemission(), 8, ""); //Date d’�mission du ch�que  

                line += createBlancs(15, " "); //Motif de repr�sentation
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy"); //Date de r�glement
                line += createBlancs(8, "0"); //MOTIF REJET
                line += createBlancs(23, " ");

                wwriteln(line);
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM1"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACC")));

                    cheque.setHeuretraitement(heureFichier);
                    cheque.setRemcom(new BigDecimal(numRemise));
                    cheque.setDatecompensation(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "ddMMyyyy"), ResLoader.getMessages("patternDate")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
            }
            setDescription(getDescription() + " ex�cut� avec succ�s:\n Nombre de Cheques Regionaux Aller= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Cheques Regionaux Aller= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques));
            closeFile();
        } else {

            logEvent("WARNING", "Aucun element disponible");
            setDescription(getDescription() + " - : Aucun element disponible");
        }

        db.close();
    }
}
