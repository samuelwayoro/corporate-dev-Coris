/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers;

import clearing.model.CMPUtility;
import clearing.table.Agences;
import clearing.table.Cheques;
import clearing.table.Comptes;
import clearing.table.ComptesNsia;
import clearing.table.Remises;
import java.awt.image.BufferedImage;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.patware.bean.table.Repertoires;
import org.patware.bean.table.Params;
import org.patware.jdbc.DataBase;
import org.patware.utils.MyFileFilter;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class IRISImageChequeNSIAReader extends FlatFileReader {

    private BigDecimal lastIdRemise;
    private static Hashtable<String, String> codeSica2Cache = new Hashtable<String, String>();
    private static Hashtable<String, String> codeSica3Cache = new Hashtable<String, String>();

    public IRISImageChequeNSIAReader() {

        setHasNormalExtension(false);
        setExtensionType(END_EXT);
    }

    @Override
    public void setRepertoire(Repertoires repertoire) {
        setWaitFolder(repertoire.getChemin() + File.separator + repertoire.getPartenaire() + File.separator);
    }

    public static String getAlphaSicaCode(String numericCode, String type) {
        String result = null;

        result = codeSica2Cache.get(numericCode);
        if (result != null) {
            return result;
        }
        result = codeSica3Cache.get(numericCode);
        if (result != null) {
            return result;
        }

        try {

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            String sql = "SELECT * FROM PARAMS WHERE VALEUR='" + numericCode + "'AND TYPE ='" + type + "'";
            Params[] params = (Params[]) db.retrieveRowAsObject(sql, new Params());
            if (params != null && params.length > 0) {

                if (type.equalsIgnoreCase("CODE_SICA2")) {
                    if ("23".contains(numericCode)) {
                        for (Params param : params) {
                            if (param.getNom().trim().equalsIgnoreCase(Utility.getParam("CODE_BANQUE").substring(0, 1))) {
                                codeSica2Cache.put(param.getValeur().trim(), param.getNom().trim());
                                return codeSica2Cache.get(numericCode);
                            }
                        }
                        return "_";
                    } else {
                        codeSica2Cache.put(params[0].getValeur().trim(), params[0].getNom().trim());
                        return codeSica2Cache.get(numericCode);
                    }

                } else {
                    codeSica3Cache.put(params[0].getValeur().trim(), params[0].getNom().trim());
                    return codeSica3Cache.get(numericCode);
                }

            } else {
                System.out.println("Il n'existe pas de Parametre de type = " + type + " et de valeur = " + numericCode);

            }

            db.close();
        } catch (Exception ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private String getAlphaNumericCodeBanque(String numeric) {
        String codeBanque = numeric;
        String alpha = getAlphaSicaCode(numeric.substring(0, 2), "CODE_SICA3");

        if (alpha != null) {

            codeBanque = numeric.replaceFirst(numeric.substring(0, 2), alpha);
            return codeBanque;

        }

        alpha = getAlphaSicaCode(numeric.substring(0, 1), "CODE_SICA2");
        if (alpha != null) {

            codeBanque = numeric.replaceFirst(numeric.substring(0, 1), alpha);
            return codeBanque;
        }

        logEvent("ERREUR", "Verifier le parametrage des codes sica ");

        codeBanque = "_____";
        return codeBanque;

    }

    public Comptes getInfoCompte(String numeroCompte) {
        try {
            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero like '%" + numeroCompte + "%'", new Comptes());

            if (comptes != null && comptes.length > 0) {
                Date today = Utility.convertStringToDate(Utility.convertDateToString(new Date(), ResLoader.getMessages("patternDate")), ResLoader.getMessages("patternDate"));

                comptes[0].setNom(comptes[0].getNom().replaceAll("\\p{Punct}", " "));
                if (comptes[0].getPrenom() != null) {
                    comptes[0].setPrenom(comptes[0].getPrenom().replaceAll("\\p{Punct}", " "));
                }
                if (comptes[0].getAdresse1() != null) {
                    comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));
                }

                ComptesNsia[] comptesNsia = (ComptesNsia[]) db.retrieveRowAsObject("SELECT * from ComptesNSIA  where numero like '" + numeroCompte + "'", new ComptesNsia());

                if (comptesNsia != null && comptesNsia.length > 0) {
                    if (((Utility.convertStringToDate(comptesNsia[0].getDateDebutEscompte(), ResLoader.getMessages("patternDate")).before(today)
                            || Utility.convertStringToDate(comptesNsia[0].getDateDebutEscompte(), ResLoader.getMessages("patternDate")).equals(today))
                            && (Utility.convertStringToDate(comptesNsia[0].getDateFinEscompte(), ResLoader.getMessages("patternDate")).after(today)
                            || Utility.convertStringToDate(comptesNsia[0].getDateFinEscompte(), ResLoader.getMessages("patternDate")).equals(today)))
                            && comptesNsia[0].getEtat() != null && comptesNsia[0].getEtat().equals(new BigDecimal("" + Utility.getParam("LCE_SBF").trim()))) {

                        comptes[0].setEtat(comptesNsia[0].getEtat());

                    } else {
                        comptes[0].setEtat(new BigDecimal("0"));

                    }

                } else {
                    comptes[0].setEtat(new BigDecimal("0"));
                }

                return comptes[0];
            }
            db.close();
            return null;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        String imgAllerFolder = Utility.getParam("IMG_ALLER_FOLDER");
        String sequence = null;
        String fileName = aFile.getName();

        MyFileFilter myFileFilter = new MyFileFilter();
        File monitoredDirectory = new File(repertoire.getChemin());
        String fileRoot = "";

        String versoFileName = "";
        String dataFileName = "";

        String srvRootFilePath = imgAllerFolder + File.separator + repertoire.getPartenaire() + File.separator + CMPUtility.getDate();
        String srvRemiseFilePath = srvRootFilePath + File.separator + "REMISES";

        String srvChequeFilePath = srvRootFilePath + File.separator + "CHEQUES";
        String pathimage = "";
        String fichierimage = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            sequence = fileName.substring(i + 1, fileName.length() - 1).toLowerCase();
            fileRoot = fileName.substring(0, i);
            for (int j = 0; j < sequence.length(); j++) {
                if (Character.isLetter(sequence.charAt(j))) {
                    if (aFile.delete()) {
                        System.out.println("Suppression reussie du fichier Parasite " + fileName);
                    } else {
                        System.out.println("Echec de suppression du fichier Parasite " + fileName);
                    }
                    return null;
                }

            }

            myFileFilter.setInternContent("." + sequence);
            File[] files = monitoredDirectory.listFiles(myFileFilter);
            for (int j = 0; j < files.length; j++) {
                if (files[j].getName().endsWith("V")) {
                    versoFileName = files[j].getName();
                }
                if (files[j].getName().endsWith("D")) {
                    dataFileName = files[j].getName();
                }
            }

        }
        System.out.println("Sequence = " + sequence);
        System.out.println("FileRoot = " + fileRoot);
        if (sequence != null) {
            File versoFile = new File(repertoire.getChemin() + File.separator + versoFileName);
            File dataFile = new File(repertoire.getChemin() + File.separator + dataFileName);
            if (versoFile.exists() && dataFile.exists()) {
                System.out.println("Triplet Complet pour la sequence = " + sequence);

                Cheques aCheque = new Cheques();
                Remises aRemise = new Remises();

                DataBase db = new DataBase(JDBCXmlReader.getDriver());
                db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

                String line = null;

                BufferedReader is = openFile(dataFile);
                System.out.println("Fichier " + dataFile.toString());
                System.out.println("dataFileName  " + dataFileName.toString());
                line = is.readLine();
                //sequences.setIdsequence(new BigDecimal(Utility.computeCompteur("IDSEQUENCE", "SEQUENCES")));
                //sequences.setCodeline(line);
                //sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                // sequences.setMachinescan(repertoire.getPartenaire());

                if (line != null) {
                    if (line.startsWith("C")) {
                        setCurrentLine(line);
                        getChamp(1);
                        String compteRemettant = getChamp(Integer.parseInt(Utility.getParam("ACCOUNT_LENGTH")));
                        aRemise.setCompteRemettant(compteRemettant);
                        getChamp(1);
                        aRemise.setMontant(String.valueOf(Long.parseLong(getChamp(16))));
                        getChamp(1);
                        aRemise.setNbOperation(new BigDecimal(getChamp(3)));
                        aRemise.setDateSaisie(CMPUtility.getDate());
                        Comptes comptes = getInfoCompte(compteRemettant);
                        if (comptes != null) {
                            aRemise.setEscompte(comptes.getEtat());
                            aRemise.setAgenceRemettant(comptes.getAgence());
                            aRemise.setNomClient(comptes.getNom());
                            aRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSAIIRIS")));
                        } else {
                            aRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMERR")));
                        }

                        is.close();

                        //REMISE - Deplacement Images remise
                        File waitFolder = new File(srvRemiseFilePath + File.separator);
                        if (!waitFolder.exists()) {
                            if (!waitFolder.mkdirs()) {
                                logEvent("ERREUR", "Impossible de créer " + srvRemiseFilePath);

                            }
                        }

                        int renameSucceeded = 0;
                        if (aFile.renameTo(new File(srvRemiseFilePath + File.separator + fileRoot + "f.jpg"))) {
                            System.out.println("Déplacement Recto Remise OK " + sequence);
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement Recto Remise NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement Recto Remise NON OK " + sequence + "| vers " + srvRemiseFilePath + File.separator + fileRoot + "f.jpg");
                        }
                        /*if (versoFile.renameTo(new File(srvRemiseFilePath + File.separator + fileRoot + "r.jpg"))) {
                        System.out.println("Déplacement Verso Remise OK " + sequence);
                        renameSucceeded++;
                        } else {
                        System.out.println("Déplacement Verso Remise NON OK " + sequence);
                        logEvent("ERREUR", "Déplacement Verso Remise NON OK " + sequence + "| vers " + srvRemiseFilePath + File.separator + fileRoot + "r.jpg");
                        }*/
                        if (dataFile.renameTo(new File(srvRemiseFilePath + File.separator + fileRoot + "d.txt"))) {
                            System.out.println("Déplacement Data Remise OK " + sequence);
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement Data Remise NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement Data Remise NON OK " + sequence + "| vers " + srvRemiseFilePath + File.separator + fileRoot + "d.txt");
                        }

                        if (renameSucceeded == 2) {
                            //      sequences.setTypedocument("REMISE");
                            //    db.insertObjectAsRowByQuery(sequences, "SEQUENCES");
                            lastIdRemise = new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE"));
                            aRemise.setIdremise(lastIdRemise);
                            aRemise.setNomUtilisateur(repertoire.getId());

                            aRemise.setSequence(new BigDecimal(sequence));
                            aRemise.setPathimage(srvRemiseFilePath + File.separator);
                            aRemise.setFichierimage(fileRoot);
                            aRemise.setMachinescan(repertoire.getPartenaire());
                            aRemise.setDevise("XOF");
                            db.insertObjectAsRowByQuery(aRemise, "REMISES");
                            System.out.println("Gestion Triplet OK " + sequence);
                        } else {
                            System.out.println("Gestion Triplet NON OK " + sequence);
                        }

                    } else {
                        //CHEQUE

                        //Lecture ligne CMC7
                        setCurrentLine(line);
                        if (line.startsWith("+")) {
                            getChamp(1);
                            aCheque.setNumerocheque(getChamp(7).trim().replaceAll("\\p{Punct}", "_"));
                        } else {
                            aCheque.setNumerocheque(getChamp(7).trim().replaceAll("\\p{Punct}", "_"));
                        }
                        getChamp(2);
                        aCheque.setBanque(getAlphaNumericCodeBanque(getChamp(5).trim().replaceAll("\\p{Punct}", "_")));
                        aCheque.setAgence(getChamp(5).trim().replaceAll("\\p{Punct}", "_"));
                        aCheque.setRibcompte(getChamp(2).trim().replaceAll("\\p{Punct}", "_"));
                        getChamp(2);
                        aCheque.setNumerocompte(getChamp(12).trim().replaceAll("\\p{Punct}", "_"));
                        getChamp(1);
                        aCheque.setMontantcheque(String.valueOf(Long.parseLong(getChamp(16))));
                        aCheque.setDevise("XOF");
                        getChamp(1);
                        aCheque.setCompteremettant(getChamp(Integer.parseInt(Utility.getParam("ACCOUNT_LENGTH"))).trim().replaceAll("\\p{Punct}", "_"));
                        is.close();
                        //Deplacement images Cheque
                        int renameSucceeded = 0;
                        pathimage = srvChequeFilePath + File.separator + aCheque.getBanque() + File.separator + aCheque.getAgence() + File.separator;

                        File waitFolder = new File(pathimage + File.separator);
                        if (!waitFolder.exists()) {
                            if (!waitFolder.mkdirs()) {
                                logEvent("ERREUR", "Impossible de créer " + pathimage);

                            }
                        }
                        fichierimage = aCheque.getNumerocompte() + aCheque.getRibcompte() + aCheque.getNumerocheque() + aCheque.getMontantcheque() + "_" + CMPUtility.getDateHeure();

                        if (aFile.renameTo(new File(pathimage + fichierimage + "f.jpg"))) {
                            System.out.println("Déplacement Recto Cheque OK " + sequence);
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement Recto Cheque NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement Recto Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "f.jpg");
                        }
                        File newVersoFile = new File(pathimage + fichierimage + "r.jpg");
                        if (versoFile.exists()) {
                            System.out.println("Déplacement Verso Cheque en cours ");
                            BufferedImage bimg = ImageIO.read(versoFile);
                            Boolean mayTattoo = new Boolean(ResLoader.getMessages("TattooPic"));
                            if (mayTattoo.booleanValue()) {
                                Utility.tattooPicture(bimg, newVersoFile, "DATECOMPENS_ENDOS");
                            }
                            ImageIO.write(bimg, "jpeg", newVersoFile);
                            versoFile.delete();
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement Verso Cheque NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement Verso Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "r.jpg");
                        }
                        if (dataFile.renameTo(new File(pathimage + fichierimage + "d.txt"))) {
                            System.out.println("Déplacement CMC7 Cheque OK " + sequence);
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement CMC7 Cheque NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement CMC7 Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "d.txt");
                        }
                        if (renameSucceeded == 3) {
                            //sequences.setTypedocument("CHEQUE");
                            //db.insertObjectAsRowByQuery(sequences, "SEQUENCES");
                            aCheque.setPathimage(pathimage);
                            aCheque.setFichierimage(fichierimage);

                            aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
                            Comptes comptes = getInfoCompte(aCheque.getCompteremettant());
                            if (comptes != null) {
                                String sql = "SELECT * FROM REMISES WHERE COMPTEREMETTANT='" + comptes.getNumero() + "' AND MACHINESCAN='" + repertoire.getPartenaire() + "' AND ETAT IN (" + Utility.getParam("CETAREMERR") + "," + Utility.getParam("CETAREMSAIIRIS") + ") ORDER BY IDREMISE DESC";
                                // String sql = "SELECT * FROM REMISES WHERE IDREMISE = SELECT MAX(IDREMISE) FROM REMISES WHERE MACHINESCAN='" + repertoire.getPartenaire();
                                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                                if (remises != null && remises.length > 0) {
                                    aCheque.setRemise(remises[0].getIdremise());
                                    aCheque.setEtat(remises[0].getEtat());
                                    aCheque.setAgenceremettant(comptes.getAgence());
                                    aCheque.setRefremise(remises[0].getReference());
                                    aCheque.setEscompte(remises[0].getEscompte());
                                    aCheque.setDatesaisie(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                                    aCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                                    aCheque.setDateemission(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                                    aCheque.setDevise("XOF");
                                    aCheque.setNombeneficiaire(comptes.getNom());
                                }

                                aCheque.setSequence(new BigDecimal(sequence));
                                aCheque.setMachinescan(repertoire.getPartenaire());
                                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                                if (Character.isDigit(aCheque.getBanque().charAt(1))) {
                                    aCheque.setEtablissement(CMPUtility.getCodeBanque());
                                    aCheque.setBanqueremettant(CMPUtility.getCodeBanque());
                                    aCheque.setType_Cheque("030");
                                } else {
                                    aCheque.setEtablissement(CMPUtility.getCodeBanqueSica3());
                                    aCheque.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                                    aCheque.setType_Cheque("035");
                                }
                                sql = "SELECT * FROM AGENCES WHERE CODEBANQUE LIKE '" + aCheque.getBanque() + "' AND CODEAGENCE LIKE '" + aCheque.getAgence() + "'";
                                Agences agences[] = (Agences[]) db.retrieveRowAsObject(sql, new Agences());
                                if (agences != null && agences.length > 0) {
                                    aCheque.setVille(agences[0].getCodevillecompense());
                                } else {
                                    aCheque.setVille("01");
                                }
                                aCheque.setVilleremettant("01");
                                aCheque.setIndicateurmodificationcmc7(new BigDecimal(0));
                                aCheque.setCodeutilisateur(repertoire.getId());
                                sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAREMSAIIRIS") + "," + Utility.getParam("CETAOPEALLICOM1ACC") + "," + Utility.getParam("CETAOPEVAL") + ","
                                        + Utility.getParam("CETAOPEALLICOM1") + "," + Utility.getParam("CETAOPEALLICOM1ENV") + "," + Utility.getParam("CETAOPEALLPREICOM1") + ","
                                        + Utility.getParam("CETAOPEVALSURCAIENVSIB") + ") AND BANQUE='" + aCheque.getBanque() + "' AND AGENCE='" + aCheque.getAgence() + "'"
                                        + " AND NUMEROCHEQUE='" + aCheque.getNumerocheque() + "' AND NUMEROCOMPTE='" + aCheque.getNumerocompte() + "'";
                                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                                if (cheques != null && cheques.length > 0) {
                                    if (!cheques[0].getIdcheque().equals(aCheque.getIdcheque())) {
                                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                                    }
                                }
                                if (aCheque.getNumerocheque().contains("_") || aCheque.getBanque().contains("_") || aCheque.getAgence().contains("_") || aCheque.getNumerocompte().contains("_") || aCheque.getRibcompte().contains("_")) {
                                    aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                                }

                            } else {
                                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                            }
                            db.insertObjectAsRowByQuery(aCheque, "CHEQUES");

                        }

                    }
                }
                db.close();
            } else {
                System.out.println("Triplet non complet pour la sequence " + sequence);
                logEvent("ERREUR", "Triplet non complet pour la sequence " + sequence);
            }

        }

        return null;
    }
}
