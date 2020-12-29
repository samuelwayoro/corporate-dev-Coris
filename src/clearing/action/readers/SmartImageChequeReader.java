/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
import clearing.table.Sequences;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.patware.bean.table.Repertoires;
import org.patware.bean.table.Params;
import org.patware.jdbc.DataBase;
import org.patware.utils.MyFileFilter;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class SmartImageChequeReader extends FlatFileReader {

    private BigDecimal lastIdRemise;
    private static Hashtable<String, String> codeSica2Cache = new Hashtable<String, String>();
    private static Hashtable<String, String> codeSica3Cache = new Hashtable<String, String>();

    @Override
    public void setRepertoire(Repertoires repertoire) {
        setWaitFolder(repertoire.getChemin() + File.separator + repertoire.getPartenaire() + File.separator);
    }

    public SmartImageChequeReader() {
        setHasNormalExtension(false);
        setExtensionType(END_EXT);
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

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        String imgAllerFolder = Utility.getParam("IMG_ALLER_FOLDER");
        String sequence = null;
        String fileName = aFile.getName();

        MyFileFilter myFileFilter = new MyFileFilter();
        File monitoredDirectory = new File(repertoire.getChemin());
        String fileRoot = "";


        String rectoFileName = "";
        String versoFileName = "";
        String dataFileName = "";
        File versoFile = null;
        File rectoFile = null;
        File dataFile = null;

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
            if (repertoire.getExtension().equalsIgnoreCase("R")) {
                for (int j = 0; j < files.length; j++) {
                    if (files[j].getName().endsWith("V")) {
                        versoFileName = files[j].getName();
                        versoFile = files[j];
                    }
                    if (files[j].getName().endsWith("D")) {
                        dataFileName = files[j].getName();
                        dataFile = files[j];
                    }
                    rectoFileName = aFile.getName();
                    rectoFile = aFile;
                }
            }
            if (repertoire.getExtension().equalsIgnoreCase("V")) {
                for (int j = 0; j < files.length; j++) {
                    if (files[j].getName().endsWith("R")) {
                        rectoFileName = files[j].getName();
                        rectoFile = files[j];
                    }
                    if (files[j].getName().endsWith("D")) {
                        dataFileName = files[j].getName();
                        dataFile = files[j];
                    }
                    versoFileName = aFile.getName();
                    versoFile = aFile;
                }
            }

            if (repertoire.getExtension().equalsIgnoreCase("D")) {
                for (int j = 0; j < files.length; j++) {
                    if (files[j].getName().endsWith("V")) {
                        versoFileName = files[j].getName();
                        versoFile = files[j];
                    }
                    if (files[j].getName().endsWith("R")) {
                        rectoFileName = files[j].getName();
                        rectoFile = files[j];
                    }
                    dataFileName = aFile.getName();
                    dataFile = aFile;
                }
            }



        }
        System.out.println("Sequence = " + sequence);
        System.out.println("RectoFile = " + rectoFileName);
        System.out.println("VersoFile = " + versoFileName);
        System.out.println("DataFile = " + dataFileName);

        if (sequence != null) {



            if (!versoFileName.trim().isEmpty() && !dataFileName.trim().isEmpty() && !rectoFileName.trim().isEmpty()
                    && versoFile.exists() && dataFile.exists() && rectoFile.exists()) {
                System.out.println("Triplet Complet pour la sequence = " + sequence);

                boolean rExist = isFileAlreadyScanned(rectoFile);
                boolean vExist = isFileAlreadyScanned(dataFile);
                boolean dExist = isFileAlreadyScanned(versoFile);

                if (rExist || vExist || dExist) {
                    System.out.println("Fichier déja traité = " + rectoFileName + "|" + versoFileName + "|" + dataFileName);
                    String param = Utility.getParam("RVDSTRONGCHECK");
                    if (param != null) {
                        if (param.equalsIgnoreCase("0"));
                        if (param.equalsIgnoreCase("1")) {
                            String junkFolderName = Utility.getParam("JUNK_FOLDER");
                            if (junkFolderName != null) {
                                File junkFolder = new File(junkFolderName);
                                Utility.createFolderIfItsnt(junkFolder, null);
                                File junkR = new File(junkFolderName + File.separator + rectoFile.getName());
                                if(junkR.exists()){
                                    junkR.delete();
                                }
                                rectoFile.renameTo(junkR);
                                File junkV = new File(junkFolderName + File.separator + versoFile.getName());
                                if(junkV.exists()){
                                    junkV.delete();
                                }
                                versoFile.renameTo(junkV);
                                
                                File junkD = new File(junkFolderName + File.separator + dataFile.getName());
                                if(junkD.exists()){
                                    junkD.delete();
                                }
                                dataFile.renameTo(junkD);
                            }

                        }
                        if (param.equalsIgnoreCase("2")) {
                            rectoFile.delete();
                            versoFile.delete();
                            dataFile.delete();
                        }

                    }
                } else {


                    Sequences sequences = new Sequences();
                    Cheques aCheque = new Cheques();
                    Remises aRemise = new Remises();

                    DataBase db = new DataBase(JDBCXmlReader.getDriver());
                    db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());


                    String line = null;

                    BufferedReader is = openFile(dataFile);
                    line = is.readLine();
                    sequences.setIdsequence(new BigDecimal(Utility.computeCompteur("IDSEQUENCE", "SEQUENCES")));
                    sequences.setCodeline(line);
                    sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy HH:mm:ss"));
                    sequences.setMachinescan(repertoire.getPartenaire());



                    if (dataFile.length() == 0 || (Utility.getParam("STRONG_CHECK_CMC7") != null && Utility.getParam("STRONG_CHECK_CMC7").equals("1") && line.length() < Integer.parseInt(Utility.getParam("CMC7_MIN_LENGTH")))) {
                        is.close();

                        //REMISE - Deplacement Images remise
                        File waitFolder = new File(srvRemiseFilePath + File.separator);
                        if (!waitFolder.exists()) {
                            if (!waitFolder.mkdirs()) {
                                logEvent("ERREUR", "Impossible de créer " + srvRemiseFilePath);

                            }
                        }


                        int renameSucceeded = 0;

                        FileUtils.copyFile(rectoFile, new File(srvRemiseFilePath + File.separator + fileRoot + "f.jpg"));

                        if (rectoFile.exists() && new File(srvRemiseFilePath + File.separator + fileRoot + "f.jpg").exists()) {
                            rectoFile.delete();
                            System.out.println("Déplacement Recto Remise OK " + sequence);
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement Recto Remise NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement Recto Remise NON OK " + sequence + "| vers " + srvRemiseFilePath + File.separator + fileRoot + "f.jpg");
                        }
                        FileUtils.copyFile(versoFile, new File(srvRemiseFilePath + File.separator + fileRoot + "r.jpg"));
                        if (versoFile.exists() && new File(srvRemiseFilePath + File.separator + fileRoot + "r.jpg").exists()) {
                            versoFile.delete();
                            System.out.println("Déplacement Verso Remise OK " + sequence);
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement Verso Remise NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement Verso Remise NON OK " + sequence + "| vers " + srvRemiseFilePath + File.separator + fileRoot + "r.jpg");
                        }
                        if (dataFile.delete()) {
                            System.out.println("Suppression CMC7 Remise OK " + sequence);
                        } else {
                            System.out.println("Suppression CMC7 Remise NON OK " + sequence);
                            logEvent("ERREUR", "Suppression CMC7 Remise NON OK  " + sequence + "| " + dataFile.getAbsolutePath());
                        }

                        if (renameSucceeded == 2) {
                            sequences.setTypedocument("REMISE");
                            db.insertObjectAsRowByQuery(sequences, "SEQUENCES");
                            lastIdRemise = new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE"));
                            aRemise.setIdremise(lastIdRemise);
                            aRemise.setNomUtilisateur(repertoire.getId());
                            aRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSTO")));
                            aRemise.setSequence(sequences.getIdsequence());
                            aRemise.setPathimage(srvRemiseFilePath + File.separator);
                            aRemise.setFichierimage(fileRoot);
                            aRemise.setMachinescan(repertoire.getPartenaire());
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
                        aCheque.setBanque(getAlphaNumericCodeBanque(Utility.bourrageGauche(getChamp(5).trim().replaceAll("\\p{Punct}", "_"), 5, "_")));
                        aCheque.setAgence(getChamp(5).trim().replaceAll("\\p{Punct}", "_"));
                        aCheque.setRibcompte(getChamp(2).trim().replaceAll("\\p{Punct}", "_"));
                        getChamp(2);
                        aCheque.setNumerocompte(getChamp(12).trim().replaceAll("\\p{Punct}", "_"));
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
                        fichierimage = aCheque.getNumerocompte() + aCheque.getRibcompte() + aCheque.getNumerocheque() + "_" + CMPUtility.getDateHeure();


                        //Utility.convertPicture("jpeg",aFile,pathimage + fichierimage + "f.jpg",0.5f);
                        //Utility.compressJpegFile(rectoFile,new File(pathimage + fichierimage + "f.jpg"),0.3f);
                        FileUtils.copyFile(rectoFile, new File(pathimage + fichierimage + "f.jpg"));
                        if (rectoFile.exists() && (new File(pathimage + fichierimage + "f.jpg")).exists()) {
                            if (!rectoFile.delete()) {
                                FileUtils.forceDelete(rectoFile);
                            }
                            System.out.println("Déplacement Recto Cheque OK " + sequence);
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement Recto Cheque NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement Recto Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "f.jpg");
                        }


                        //Utility.convertPicture("jpeg",versoFile,pathimage + fichierimage + "r.jpg",0.5f);
                        //Utility.compressJpegFile(versoFile,new File(pathimage + fichierimage + "r.jpg"),0.3f);
                        FileUtils.copyFile(versoFile, new File(pathimage + fichierimage + "r.jpg"));

                        if (versoFile.exists() && (new File(pathimage + fichierimage + "r.jpg")).exists()) {

                            if (!versoFile.delete()) {
                                FileUtils.forceDelete(versoFile);
                            }
                            System.out.println("Déplacement Verso Cheque OK " + sequence);
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement Verso Cheque NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement Verso Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "r.jpg");
                        }
                        FileUtils.copyFile(dataFile, new File(pathimage + fichierimage + "d.txt"));
                        if (dataFile.exists() && (new File(pathimage + fichierimage + "d.txt")).exists()) {
                            if (!dataFile.delete()) {
                                FileUtils.forceDelete(dataFile);
                            }
                            System.out.println("Déplacement CMC7 Cheque OK " + sequence);
                            renameSucceeded++;
                        } else {
                            System.out.println("Déplacement CMC7 Cheque NON OK " + sequence);
                            logEvent("ERREUR", "Déplacement CMC7 Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "d.jpg");
                        }
                        if (renameSucceeded == 3) {
                            sequences.setTypedocument("CHEQUE");
                            db.insertObjectAsRowByQuery(sequences, "SEQUENCES");
                            aCheque.setPathimage(pathimage);
                            aCheque.setFichierimage(fichierimage);
                            aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                            aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));

                            String sql = "SELECT * FROM REMISES WHERE MACHINESCAN='" + repertoire.getPartenaire() + "' AND ETAT IN (" + Utility.getParam("CETAREMSTO") + "," + Utility.getParam("CETAREMSAI") + ") ORDER BY IDREMISE DESC";
                            // String sql = "SELECT * FROM REMISES WHERE IDREMISE = SELECT MAX(IDREMISE) FROM REMISES WHERE MACHINESCAN='" + repertoire.getPartenaire();
                            Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                            if (remises != null && remises.length > 0) {
                                aCheque.setRemise(remises[0].getIdremise());
                            }

                            aCheque.setSequence(sequences.getIdsequence());
                            aCheque.setMachinescan(repertoire.getPartenaire());

                            aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                            db.insertObjectAsRowByQuery(aCheque, "CHEQUES");

                        }


                    }
                    db.close();
                }
            } else {
                System.out.println("Triplet non complet pour la sequence " + sequence);
                logEvent("ERREUR", "Triplet non complet pour la sequence " + sequence);
            }

        }




        return null;
    }
}
