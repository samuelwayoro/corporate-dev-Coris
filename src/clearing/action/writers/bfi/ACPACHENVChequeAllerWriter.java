/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.bfi;

import clearing.table.Cheques;
import clearing.table.Remises;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.patware.action.file.FlatFileWriter;
import org.patware.action.impl.ExecutableImpl;

import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ACPACHENVChequeAllerWriter extends FlatFileWriter {

    public ACPACHENVChequeAllerWriter() {
        setDescription("Envoi des Cheques Aller Nationaux vers ACP/ACH");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
        String dateFichier = Utility.convertDateToString(new Date(), "ddMMyyyy");
        String heureFichier = Utility.convertDateToString(new Date(), "HHmmss");
        String numRemise = Utility.bourrageGauche(Utility.computeCompteur("ENV_CHQ", dateFichier), 7, "0");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-GN-" + Utility.getParam("CODE_BANQUE") + "-" + dateFichier
                + "-" + heureFichier + "-30-21-324.ENV";

        String sql = "SELECT * FROM CHEQUES  WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1") + ") AND BANQUE<>BANQUEREMETTANT ORDER BY REMISE, BANQUE, AGENCE, IDCHEQUE "; //Cheques presentes
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

        if (cheques != null && 0 < cheques.length) {

//            setOut(createFlatFile(fileName));
            long sumCheques = 0;

            HashSet<BigDecimal> hashSet = new HashSet();
            HashSet<Remises> hashSetRemises = new HashSet();
            ArrayList<String> fileCheques = new ArrayList<>();

            for (int i = 0; i < cheques.length; i++) {
                sumCheques += Long.parseLong(cheques[i].getMontantcheque());
                hashSet.add(cheques[i].getRemise());
                fileCheques.add(cheques[i].getDateecheance());

            }
            Iterator<BigDecimal> iterator = hashSet.iterator();

            while (iterator.hasNext()) {
                sql = "SELECT * FROM REMISES  WHERE IDREMISE=" + iterator.next();
                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                if (remises != null && 0 < remises.length) {
                    for (Remises remise : remises) {
                        hashSetRemises.add(remise);
                    }

                }
            }
            //enregistrement global
            String line = "101GN";
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

//            wwriteln(line);
            //enregistrement detail
            for (Cheques cheque : cheques) {
                //Tous les cheques aller
                line = "101GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "30" + Utility.getParam("CODE_BANQUE");
                line += dateCompensation + dateCompensation;
                line += numRemise;
                line += "21" + "324" + Integer.parseInt(cheque.getCalcul()); //Nature du cheque
                line += Utility.bourrageGauche(cheque.getMontantcheque(), 15, "0"); //Montant du cheque
                line += Utility.bourrageGauche(cheque.getNumerocheque(), 8, "0"); //Numero du cheque
                line += Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");

                line += Utility.bourrageGauche(cheque.getBanque(), 3, "0"); //compte du tireur==celui qu'on debite 18 (code banque, code agence, numero de compte, cle de controle compte)
                line += Utility.bourrageGauche(cheque.getAgence(), 3, "0");
                line += Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanque(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgence(), 3, "0"),
                        Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNomemetteur() == null ? "BANQUE" : cheque.getNomemetteur(), 30, " ");
                line += Utility.bourrageDroite(cheque.getBanque() == null ? "ADRESSE BANQUE" : cheque.getBanque(), 30, " "); //Adresse du Tireur
                line += Utility.bourrageGauche(cheque.getBanque(), 3, "0") + "GN";

                line += Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"); //Compte du beneficiaire 
                line += Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0");
                line += Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0"),
                        Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0"));
                line += Utility.bourrageDroite(cheque.getNombeneficiaire(), 30, " ");//Nom ou raison sociale du beneficiaire
                line += Utility.bourrageDroite(cheque.getBanqueremettant() == null ? "ADRESSE BANQUE" : cheque.getBanqueremettant(), 30, " ");//Adresse du beneficiaire
                line += Utility.bourrageDroite(cheque.getDateemission(), 8, ""); //Date d'emission du cheque 

                line += createBlancs(15, " "); //Motif de representation
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy"); //Date de reglement
                line += createBlancs(8, "0"); //MOTIF REJET
                line += createBlancs(23, " ");

//                wwriteln(line);
                if (cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM1"))) {
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));  // CETAOPEALLICOM1ACC

                    cheque.setHeuretraitement(heureFichier);
                    cheque.setRemcom(new BigDecimal(numRemise));
                    cheque.setDatecompensation(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "ddMMyyyy"), ResLoader.getMessages("patternDate")));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
            }
            setDescription(getDescription() + " execute avec succes:\n Nombre de Cheques  Aller Nationaux Presentes= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques)); // + " - Nom de Fichier = " + fileName
            logEvent("INFO", "Nombre de Cheques Aller Nationaux Presentes= " + cheques.length + " - Montant Total= " + Utility.formatNumber("" + sumCheques));
//            closeFile();

            Iterator<Remises> iteratorRemises = hashSetRemises.iterator();

            ArrayList<String> listRemise = new ArrayList<>(); //fileCheques
            while (iteratorRemises.hasNext()) {
                //constituer la liste des fichiers
                listRemise.add(iteratorRemises.next().getCdscpta());

            }
            listRemise.addAll(fileCheques);

            for (String fichier : listRemise) {
                System.out.println("fichier de la liste "+fichier);

                FileUtils.copyFile(new File(fichier), new File(Utility.getParam("SIB_IN_FOLDER") + File.separator + new File(fichier).getName()));
                
                logEvent("INFO", "Fichier de Donnees");
                if (fichier.endsWith(".CAT")) {
                    System.out.println("fichier" + fichier.toString());
                    File catFile = new File(fichier);
                    String catFileName = catFile.getName();
                    String pakFichier = catFile.getParent()+File.separator+  catFileName.replaceAll(".CAT", ".PAK");
                    System.out.println("Source Pak "+pakFichier);

                    FileUtils.copyFile(new File(pakFichier), new File(Utility.getParam("SIB_IN_FOLDER") + File.separator + new File(pakFichier).getName()));
                }

            }

//////////////            //      Ecrire le fichier Cat des images
//////////////            ExecutableImpl catWriter;
//////////////            catWriter = (ExecutableImpl) new CatAcpAchWriter(cheques, fileName.substring(0, fileName.indexOf(".")));
//////////////            catWriter.execute();
//////////////            System.out.println("#############################Fin execution CAT#################################");
//////////////
//////////////            //Ecrire le fichier Pak des images
//////////////            ExecutableImpl pakWriter;
//////////////            pakWriter = (ExecutableImpl) new PakAcpAchWriter(cheques, fileName.substring(0, fileName.indexOf(".")));
//////////////            pakWriter.execute();
//////////////            System.out.println("#############################Fin execution PAK #################################");
        } else {
            logEvent("WARNING", "Aucun element disponible");
            setDescription(getDescription() + " - : Aucun element disponible");
        }

        db.close();
    }
}
