/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
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
import org.patware.action.file.FlatFileWriter;
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
public class SmartChequeWithoutSlipUBAGuineeReader extends FlatFileReader {
    
    private BigDecimal lastIdRemise;
    private static Hashtable<String, String> codeSica2Cache = new Hashtable<String, String>();
    private static Hashtable<String, String> codeSica3Cache = new Hashtable<String, String>();
    
    @Override
    public void setRepertoire(Repertoires repertoire) {
        setWaitFolder(repertoire.getChemin() + File.separator + repertoire.getPartenaire() + File.separator);
    }
    
    public SmartChequeWithoutSlipUBAGuineeReader() {
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
                
                Sequences sequences = new Sequences();
                Cheques aCheque = new Cheques();
                
                DataBase db = new DataBase(JDBCXmlReader.getDriver());
                db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
                
                String line = null;
                
                BufferedReader is = openFile(dataFile);
                line = is.readLine();
                sequences.setIdsequence(new BigDecimal(Utility.computeCompteur("IDSEQUENCE", "SEQUENCES")));
                sequences.setCodeline(line);
                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                sequences.setMachinescan(repertoire.getPartenaire());
                
                {
                    System.out.println("line" + line + " Length: " + line.length());
                    //line +01564459+0300200842#007601000127+0132416>  //Numero de cheque 8
                    //Length: 42
                    //CHEQUE
                    if (line == null) {
                        line = FlatFileWriter.createBlancs(35, "_");
                    }
                    
                    if (line.length() == 35) {
                        line = line.substring(0, 1).concat(line.substring(1).replace("+", " +").replace("#", "# "));
                        
                    }

                    //Lecture ligne CMC7
                    setCurrentLine(line);
                    if (line.startsWith("+")) {
                        getChamp(1); //Separateur
                        aCheque.setNumerocheque(getChamp(8).trim().replaceAll("\\p{Punct}", "_"));
                    } else {
                        aCheque.setNumerocheque(getChamp(8).trim().replaceAll("\\p{Punct}", "_"));
                    }
                    getChamp(1);   //Separateur
                    getChamp(2); //Code Pays tire 02pos 	01 (GM) /05 (SL) / 03 (GN)
                    aCheque.setBanque(getChamp(3).trim().replaceAll("\\p{Punct}", "_")); //Code Banque tire 03pos
                    aCheque.setGarde(getChamp(2));//Code etat 02 Champ Garde de cheque
                    aCheque.setAgence(getChamp(3).trim().replaceAll("\\p{Punct}", "_"));  //Code Agence tire 03
                    getChamp(1);   //Separateur
                    aCheque.setNumerocompte(getChamp(10).trim().replaceAll("\\p{Punct}", "_")); //Numero de Compte tire
                    aCheque.setRibcompte(getChamp(2).trim().replaceAll("\\p{Punct}", "_"));  //Cle Contrôle Compte tire
                    getChamp(1);  //Separateur
                    aCheque.setCalcul(getChamp(2));     //Code transaction 02Pos ==Champ Calcul du cheque
                    aCheque.setDevise("GNF");
                     aCheque.setCodeVignetteUV(getChamp(2));  //Cle de controle MICR 02pos CodeVignetteUV du cheque	Modulo97 inclue tous les caracteres de la ligne  MICR (35 caracteres)
                    is.close();
                    //Deplacement images Cheque
                    int renameSucceeded = 0;
                    pathimage = srvChequeFilePath + File.separator + aCheque.getBanque() + File.separator + aCheque.getAgence() + File.separator;
                    
                    File waitFolder = new File(pathimage + File.separator);
                    if (!waitFolder.exists()) {
                        if (!waitFolder.mkdirs()) {
                            logEvent("ERREUR", "Impossible de creer " + pathimage);
                            
                        }
                    }
                    fichierimage = aCheque.getNumerocompte() + aCheque.getRibcompte() + aCheque.getNumerocheque() + "_" + CMPUtility.getDateHeure();
                    
                    FileUtils.copyFile(rectoFile, new File(pathimage + fichierimage + "f.jpg"));
                    if (rectoFile.exists() && (new File(pathimage + fichierimage + "f.jpg")).exists()) {
                        if (!rectoFile.delete()) {
                            FileUtils.forceDelete(rectoFile);
                        }
                        System.out.println("Deplacement Recto Cheque OK " + sequence);
                        renameSucceeded++;
                    } else {
                        System.out.println("Deplacement Recto Cheque NON OK " + sequence);
                        logEvent("ERREUR", "Deplacement Recto Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "f.jpg");
                    }
                    
                    FileUtils.copyFile(versoFile, new File(pathimage + fichierimage + "r.jpg"));
                    if (versoFile.exists() && (new File(pathimage + fichierimage + "r.jpg")).exists()) {
                        if (!versoFile.delete()) {
                            FileUtils.forceDelete(versoFile);
                        }
                        System.out.println("Deplacement Verso Cheque OK " + sequence);
                        renameSucceeded++;
                    } else {
                        System.out.println("Deplacement Verso Cheque NON OK " + sequence);
                        logEvent("ERREUR", "Deplacement Verso Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "r.jpg");
                    }
                    FileUtils.copyFile(dataFile, new File(pathimage + fichierimage + "d.txt"));
                    if (dataFile.exists() && (new File(pathimage + fichierimage + "d.txt")).exists()) {
                        if (!dataFile.delete()) {
                            FileUtils.forceDelete(dataFile);
                        }
                        System.out.println("Deplacement CMC7 Cheque OK " + sequence);
                        renameSucceeded++;
                    } else {
                        System.out.println("Deplacement CMC7 Cheque NON OK " + sequence);
                        logEvent("ERREUR", "Deplacement CMC7 Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "d.jpg");
                    }
                    if (renameSucceeded == 3) {
                        sequences.setTypedocument("CHEQUE");
                        db.insertObjectAsRowByQuery(sequences, "SEQUENCES_CHEQUES");
                        aCheque.setPathimage(pathimage);
                        aCheque.setFichierimage(fichierimage);
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                        aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
                        aCheque.setSequence(sequences.getIdsequence());
                        aCheque.setMachinescan(repertoire.getPartenaire());
                        aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                        db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
                        
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
