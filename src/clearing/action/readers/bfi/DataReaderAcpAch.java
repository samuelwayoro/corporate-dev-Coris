/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.bfi;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
import clearing.table.Utilisateurs;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;

import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class DataReaderAcpAch extends FlatFileReader {

    public DataReaderAcpAch() {
        setCopyOriginalFile(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);
        Set<String> imgFilesName = new HashSet<>();
        String aFileName = aFile.getName().replaceAll("#DataReaderAcpAch", "");

        String parent = aFile.getParent() + File.separator + "TREATED";
        if (!new File(parent).exists()) {
            if (!new File(parent).mkdirs()) {
                logEvent("ERREUR", "Impossible de creer " + parent);
            }

        }
        File file = new File(parent + File.separator + aFileName);

        String line = null;

        Cheques aCheque = new Cheques();
        Remises aRemise = new Remises();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is = openFile(aFile);
        String fileName = aFile.getName();
        System.out.println("fileName" + fileName);
        int i = fileName.lastIndexOf(".");
        String ribBeneficiaire = "";
        String agence = fileName.substring(12, 15);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            String typeCheque = getChamp(2); //ty
            String typeEnreg = getChamp(1);
            String numeroRemise;

            String nomBeneficiaire;
            String adresseBeneficiaire;
            Integer nbreCheques;
            Long montantTotal = 0L;
            String dateRemise;
            String utilisateur;
            if (typeEnreg.equals("1")) {
                getChamp(3); //code devise
                numeroRemise = getChamp(7); //Numero remise 7
                ribBeneficiaire = getChamp(18);
                nomBeneficiaire = getChamp(30);
                adresseBeneficiaire = getChamp(30);
                nbreCheques = Integer.parseInt(getChamp(10));
                montantTotal = Long.parseLong(getChamp(15));
                getChamp(2);// reconciliation
                getChamp(30); //Information concernant la remise
                dateRemise = getChamp(8);
                utilisateur = getChamp(20);

                aRemise.setEtablissement(ribBeneficiaire.substring(0, 3));
                aRemise.setAgenceRemettant(agence);
                String sql = "SELECT * FROM UTILISATEURS WHERE NOM= 'CAPTURE' AND ADRESSE ='" + agence + "' AND TRIM(PASSWORD)='XXXXXXXXXX'  ";
                Utilisateurs[] userSaisie = (Utilisateurs[]) db.retrieveRowAsObject(sql, new Utilisateurs());
                if (userSaisie != null && userSaisie.length > 0) {
                    utilisateur = userSaisie[0].getLogin();
                }

                aRemise.setCompteRemettant(ribBeneficiaire.substring(6, 16));
                aRemise.setAgenceDepot(agence);
                aRemise.setNomClient(nomBeneficiaire.trim());
                aRemise.setNbOperation(new BigDecimal(nbreCheques));
                aRemise.setMontant(String.valueOf(montantTotal));
                aRemise.setNomUtilisateur(utilisateur.trim());
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                aRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(aFile.getName().substring(19, 27), "ddMMyyyy"), "yyyy/MM/dd")); //yyyy/MM/dd
                aRemise.setDevise(CMPUtility.getDevise());
                aRemise.setCdscpta(file.getPath());
                aRemise.setSequence(new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE")));
                aRemise.setIdremise(aRemise.getSequence());

                sql = "SELECT * FROM REMISES WHERE TRIM(AGENCEDEPOT) ='" + aRemise.getAgenceDepot() + "'"
                        + "   AND  TRIM(NBOPERATION) ='" + aRemise.getNbOperation() + "' "
                        + "   AND TRIM(DATESAISIE) ='" + aRemise.getDateSaisie().trim() + "' "
                        + "   AND TRIM(ETABLISSEMENT) ='" + aRemise.getEtablissement().trim() + "'  "
                        + "   AND TRIM(AGENCEREMETTANT) ='" + aRemise.getAgenceRemettant() + "' "
                        + "   AND TRIM(MONTANT) ='" + aRemise.getMontant().trim() + "'  "
                        + "   AND TRIM(NOMUTILISATEUR)='" + aRemise.getNomUtilisateur().trim() + "'         ";
                Remises[] remisesSaisies = (Remises[]) db.retrieveRowAsObject(sql, new Remises());

                if (remisesSaisies != null && remisesSaisies.length > 0) {
                    if (remisesSaisies[0].getEtat().toPlainString().equals(Utility.getParam("CETAOPEANO")) || remisesSaisies[0].getEtat().toPlainString().equals(Utility.getParam("CETAREMERR"))) {
                        db.insertObjectAsRowByQuery(aRemise, "REMISES");
                        System.out.println("Remises existe mais etat 20 ou 22");
                    } else {
                        System.out.println("Remises existe et pas annulé");
                    }

                } else {

                    db.insertObjectAsRowByQuery(aRemise, "REMISES");
                }

            }
            if (typeEnreg.equals("2")) {
                aCheque.setRio(getChamp(7));
                String ligneCMC7 = getChamp(37);
                imgFilesName.add(ligneCMC7);
                aCheque.setNumerocheque(ligneCMC7.substring(0, 8));
                aCheque.setBanque(ligneCMC7.substring(10, 13));
                aCheque.setGarde(ligneCMC7.substring(13, 15)); // Code état de la ligne CMC7  Code état	13 -15eme pos sur la ligne CMC7
                aCheque.setAgence(ligneCMC7.substring(15, 18));
                aCheque.setNumerocompte(ligneCMC7.substring(18, 28));
                aCheque.setRibcompte(ligneCMC7.substring(28, 30));
                aCheque.setCalcul(ligneCMC7.substring(30, 32)); //Code de la Transaction == Nature du chèque 1 : cheque personnel 02 : cheque d?entreprise 03 : Chèque de Banque
                aCheque.setCodeVignetteUV(ligneCMC7.substring(35, 37));

                aCheque.setDateemission(getChamp(8));
                aCheque.setMontantcheque(String.valueOf(Long.parseLong(getChamp(15)))); //montant cheque
                getChamp(2);
                aCheque.setType_Cheque(typeCheque);
                getChamp(8);
                getChamp(3);
                aCheque.setNomemetteur(getChamp(30).trim());
                getChamp(30);
                aCheque.setBanqueremettant(getChamp(3));
                getChamp(15);
                aCheque.setAgenceremettant(ribBeneficiaire.substring(3, 6));
                aCheque.setCompteremettant(aRemise.getCompteRemettant()); //015002108000331076 getChamp(10)
                aCheque.setNombeneficiaire(getChamp(30).trim());
                aCheque.setEtablissement(aCheque.getBanqueremettant());
                getChamp(30);
                aCheque.setCodeutilisateur(getChamp(20).trim());
                aCheque.setDevise(CMPUtility.getDevise());
                aCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                aCheque.setDatesaisie(aCheque.getDatetraitement());
                aCheque.setRemise(aRemise.getIdremise());
                aCheque.setOrigine(new BigDecimal(0));
                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                aCheque.setVilleremettant("01");

                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));

                //Test voir si le cheque existe en BD; si oui pas d'insertion.
                System.out.println("Test voir si le cheque existe en BD; si oui pas d'insertion");
                String sql = " SELECT * FROM CHEQUES WHERE NUMEROCHEQUE='" + aCheque.getNumerocheque() + "'"
                        + "   AND NUMEROCOMPTE='" + aCheque.getNumerocompte().trim() + "' "
                        + "   AND AGENCE='" + aCheque.getAgence().trim() + "' "
                        + "   AND BANQUE='" + aCheque.getBanque().trim() + "'  "
                        + "   AND TRIM(COMPTEREMETTANT)='" + aCheque.getCompteremettant().trim() + "'  "
                        + "   AND AGENCEREMETTANT='" + aCheque.getAgenceremettant().trim() + "' "
                        + "   AND DATETRAITEMENT='" + aCheque.getDatetraitement().trim() + "'  "
                        + "   AND DATESAISIE='" + aCheque.getDatesaisie().trim() + "'  "
                        + "   AND BANQUEREMETTANT='" + aCheque.getBanqueremettant().trim() + "'         ";

                Cheques[] chequesSaisis = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                //le cheque existe  a cette date de Saisie
                if (chequesSaisis != null && chequesSaisis.length > 0) {
                    System.out.println("CHEQUE EXISTE");
                    //Si le cheque existe a cette date de saisie, sil est a 20 ou 22 c bon pour insertion nouveau
                    if (chequesSaisis[0].getEtat().toPlainString().equals(Utility.getParam("CETAOPEANO")) || chequesSaisis[0].getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM3ACC")) || chequesSaisis[0].getEtat().toPlainString().equals(Utility.getParam("CETAREMERR"))) { //
                        aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
                        if (chequesSaisis[0].getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM3ACC"))) {
                            System.out.println("Cheque parti en compensation et Annulé depuis WBC " + chequesSaisis[0].getIdcheque() + " ");
                        }

                        aCheque.setRemise(aRemise.getIdremise());
                        db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
                    } else if (chequesSaisis[0].getEtat().toPlainString().equals(Utility.getParam("CETAIMASTO"))) {//CETAIMASTO
                        //  mise a jour du cheque
                        aCheque.setIdcheque(chequesSaisis[0].getIdcheque()); //cheques[0].getIdcheque()
                        aCheque.setPathimage(chequesSaisis[0].getPathimage());
                        aCheque.setFichierimage(chequesSaisis[0].getFichierimage());
                        aCheque.setDateecheance(chequesSaisis[0].getDateecheance());
                        aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
                        aCheque.setRemise(aRemise.getIdremise());

                        db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());
                    } else {
                        System.out.println("rien");
                    }

                } else {
                    //le cheque n'existe pas a cette date de Saisie
                    sql = "SELECT * FROM CHEQUES WHERE TRIM(BANQUE) ='" + aCheque.getBanque().trim() + "' "
                            + " AND TRIM(NUMEROCHEQUE) ='" + aCheque.getNumerocheque().trim() + "' "
                            + " AND TRIM(NUMEROCOMPTE) ='" + aCheque.getNumerocompte().trim() + "' "
                            + " AND TRIM(DATESAISIE)='" + aCheque.getDatesaisie().trim() + "'  " //ETATIMAGE=" + Utility.getParam("CETAIMASTO") + 
                            + " AND ETAT=" + Utility.getParam("CETAIMASTO") + ""
                            + " AND TRIM(BANQUEREMETTANT)='" + aCheque.getBanqueremettant().trim() + "'  "
                            + " AND TRIM(COMPTEREMETTANT)='" + aCheque.getCompteremettant().trim() + "'  "
                            //                            + " AND ETAT=" + Utility.getParam("CETAOPEVAL") + ""
                            + " AND TRIM(MONTANTCHEQUE) ='" + aCheque.getMontantcheque().trim() + "'";
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    if (cheques != null && cheques.length > 0) {
                        aCheque.setFichierimage(cheques[0].getFichierimage());
                        aCheque.setPathimage(cheques[0].getPathimage());
                        aCheque.setDateecheance(cheques[0].getDateecheance());
                        aCheque.setIdcheque(cheques[0].getIdcheque());
                        aCheque.setOrigine(new BigDecimal(0));
                        aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
                        aCheque.setRemise(aRemise.getIdremise());

//                        cheques = null;
                        db.updateRowByObjectByQuery(aCheque, "CHEQUES", "IDCHEQUE=" + aCheque.getIdcheque());

                    } else {
                        //insertion
                        aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));

                        aCheque.setRemise(aRemise.getIdremise());
                        db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
                    }
                }

//                if (aCheque.getRio().equals(aRemise.getAgenceDepot())) {
//                    aCheque.setRemise(aRemise.getIdremise());
//                }
//                db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
            }

        }
        db.close();
//        System.out.println("afile " + aFile + " aFile.getAbsolutePath() " + aFile.getParent() + " aFile.getName" + aFile.getName() + " aFile.getCanonicalPath" + aFile.getCanonicalPath() + "aFile.getPath() " + aFile.getPath());

        FileUtils.copyFile(aFile, file);

        //Charger les Fichiers CAT/PAK Correspondants
        /**
         * Iterator<String> iterator = fruits.iterator();
		while(iterator.hasNext()){
			System.out.println("Consuming fruit "+iterator.next());
		}
         */
//        if (true) {
//            File file = new File(aFile + "#CatPakAcpAchAllerReader#TERMINE");
//            File treatFile = new File(aFile.getAbsolutePath());
//            if (treatFile.exists()) {
//                FileUtils.moveFile(treatFile, new File(treatFile + "#CatPakAcpAchAllerReader#TERMINE"));
//
//            }
//        }
        return aFile;
    }

}
