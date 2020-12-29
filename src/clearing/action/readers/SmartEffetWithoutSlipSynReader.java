package clearing.action.readers;

import clearing.model.CMPUtility;
import clearing.table.Effets;
import clearing.table.SequencesEffets;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.patware.action.file.FlatFileReader;
import org.patware.action.file.FlatFileWriter;
import org.patware.bean.table.Params;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.MyFileFilter;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

public class SmartEffetWithoutSlipSynReader
  extends FlatFileReader
{
  private BigDecimal lastIdRemise;
  private static Hashtable<String, String> codeSica2Cache = new Hashtable();
  private static Hashtable<String, String> codeSica3Cache = new Hashtable();
  
  public void setRepertoire(Repertoires repertoire)
  {
    setWaitFolder(repertoire.getChemin() + File.separator + repertoire.getPartenaire() + File.separator);
  }
  
  public SmartEffetWithoutSlipSynReader()
  {
    setHasNormalExtension(false);
    setExtensionType(1);
  }
  
  public static String getAlphaSicaCode(String numericCode, String type)
  {
    String result = null;
    
    result = (String)codeSica2Cache.get(numericCode);
    if (result != null) {
      return result;
    }
    result = (String)codeSica3Cache.get(numericCode);
    if (result != null) {
      return result;
    }
    try
    {
      DataBase db = new DataBase(JDBCXmlReader.getDriver());
      db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
      
      String sql = "SELECT * FROM PARAMS WHERE VALEUR='" + numericCode + "'AND TYPE ='" + type + "'";
      Params[] params = (Params[])db.retrieveRowAsObject(sql, new Params());
      if ((params != null) && (params.length > 0))
      {
        if (type.equalsIgnoreCase("CODE_SICA2"))
        {
          if ("23".contains(numericCode))
          {
            for (Params param : params) {
              if (param.getNom().trim().equalsIgnoreCase(Utility.getParam("CODE_BANQUE").substring(0, 1)))
              {
                codeSica2Cache.put(param.getValeur().trim(), param.getNom().trim());
                return (String)codeSica2Cache.get(numericCode);
              }
            }
            return "_";
          }
          codeSica2Cache.put(params[0].getValeur().trim(), params[0].getNom().trim());
          return (String)codeSica2Cache.get(numericCode);
        }
        codeSica3Cache.put(params[0].getValeur().trim(), params[0].getNom().trim());
        return (String)codeSica3Cache.get(numericCode);
      }
      System.out.println("Il n'existe pas de Parametre de type = " + type + " et de valeur = " + numericCode);
      
      db.close();
    }
    catch (Exception ex)
    {
      Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
    }
    return result;
  }
  
  private String getAlphaNumericCodeBanque(String numeric)
  {
    String codeBanque = numeric;
    String alpha = getAlphaSicaCode(numeric.substring(0, 2), "CODE_SICA3");
    if (alpha != null)
    {
      codeBanque = numeric.replaceFirst(numeric.substring(0, 2), alpha);
      return codeBanque;
    }
    alpha = getAlphaSicaCode(numeric.substring(0, 1), "CODE_SICA2");
    if (alpha != null)
    {
      codeBanque = numeric.replaceFirst(numeric.substring(0, 1), alpha);
      return codeBanque;
    }
    logEvent("ERREUR", "Verifier le parametrage des codes sica ");
    
    codeBanque = "_____";
    return codeBanque;
  }
  
  public File treatFile(File aFile, Repertoires repertoire)
    throws Exception
  {
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
    
    String srvChequeFilePath = srvRootFilePath + File.separator + "EFFETS";
    String pathimage = "";
    String fichierimage = "";
    
    int i = fileName.lastIndexOf('.');
    if ((i > 0) && (i < fileName.length() - 1))
    {
      sequence = fileName.substring(i + 1, fileName.length() - 1).toLowerCase();
      fileRoot = fileName.substring(0, i);
      for (int j = 0; j < sequence.length(); j++) {
        if (Character.isLetter(sequence.charAt(j)))
        {
          if (aFile.delete()) {
            System.out.println("Suppression reussie du fichier Parasite " + fileName);
          } else {
            System.out.println("Echec de suppression du fichier Parasite " + fileName);
          }
          return null;
        }
      }
      myFileFilter.setInternContent("." + sequence);
      boolean bTripletComplet = false;
      while (!bTripletComplet)
      {
        File[] files = monitoredDirectory.listFiles(myFileFilter);
        if (repertoire.getExtension().equalsIgnoreCase("R")) {
          for (int j = 0; j < files.length; j++)
          {
            if (files[j].getName().endsWith("V"))
            {
              versoFileName = files[j].getName();
              versoFile = files[j];
            }
            if (files[j].getName().endsWith("D"))
            {
              dataFileName = files[j].getName();
              dataFile = files[j];
            }
            rectoFileName = aFile.getName();
            rectoFile = aFile;
          }
        }
        if (repertoire.getExtension().equalsIgnoreCase("V")) {
          for (int j = 0; j < files.length; j++)
          {
            if (files[j].getName().endsWith("R"))
            {
              rectoFileName = files[j].getName();
              rectoFile = files[j];
            }
            if (files[j].getName().endsWith("D"))
            {
              dataFileName = files[j].getName();
              dataFile = files[j];
            }
            versoFileName = aFile.getName();
            versoFile = aFile;
          }
        }
        if (repertoire.getExtension().equalsIgnoreCase("D")) {
          for (int j = 0; j < files.length; j++)
          {
            if (files[j].getName().endsWith("V"))
            {
              versoFileName = files[j].getName();
              versoFile = files[j];
            }
            if (files[j].getName().endsWith("R"))
            {
              rectoFileName = files[j].getName();
              rectoFile = files[j];
            }
            dataFileName = aFile.getName();
            dataFile = aFile;
          }
        }
        if ((!versoFileName.trim().isEmpty()) && (!dataFileName.trim().isEmpty()) && (!rectoFileName.trim().isEmpty()) && (versoFile != null) && (dataFile != null) && (rectoFile != null) && (versoFile.exists()) && (dataFile.exists()) && (rectoFile.exists()) && (versoFile.canRead()) && (dataFile.canRead()) && (rectoFile.canRead())) {
          bTripletComplet = true;
        } else {
          bTripletComplet = false;
        }
      }
    }
    System.out.println("Sequence = " + sequence);
    System.out.println("RectoFile = " + rectoFileName);
    System.out.println("VersoFile = " + versoFileName);
    System.out.println("DataFile = " + dataFileName);
    if (sequence != null) {
      if ((!versoFileName.trim().isEmpty()) && (!dataFileName.trim().isEmpty()) && (!rectoFileName.trim().isEmpty()) && (versoFile.exists()) && (dataFile.exists()) && (rectoFile.exists()))
      {
        System.out.println("Triplet Complet pour la sequence = " + sequence);
        
        SequencesEffets sequences = new SequencesEffets();
        Effets aEffet = new Effets();
        
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        
        String line = null;
        
        BufferedReader is = openFile(dataFile);
        line = is.readLine();
        sequences.setIdsequence(new BigDecimal(Utility.computeCompteur("IDSEQUENCEEFFETS", "SEQUENCES_EFFETS")));
        sequences.setCodeline(line);
        sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
        sequences.setMachinescan(repertoire.getPartenaire());
        if (line == null) {
          line = FlatFileWriter.createBlancs(35, "_");
        }
        if (line.length() == 35) {
          line = line.substring(0, 1).concat(line.substring(1).replace("+", " +").replace("#", "# "));
        }
        setCurrentLine(line);
        if (line.startsWith("+"))
        {
          getChamp(1);
          aEffet.setNumeroeffet(getChamp(7).trim().replaceAll("\\p{Punct}", "_"));
        }
        else
        {
          aEffet.setNumeroeffet(getChamp(7).trim().replaceAll("\\p{Punct}", "_"));
        }
        getChamp(2);
        aEffet.setBanque(getAlphaNumericCodeBanque(Utility.bourrageGauche(getChamp(5).trim().replaceAll("\\p{Punct}", "_"), 5, "_")));
        aEffet.setAgence(getChamp(5).trim().replaceAll("\\p{Punct}", "_"));
        aEffet.setRibcompte(getChamp(2).trim().replaceAll("\\p{Punct}", "_"));
        getChamp(2);
        aEffet.setNumerocompte_Tire(getChamp(12).trim().replaceAll("\\p{Punct}", "_"));
        is.close();
        
        int renameSucceeded = 0;
        pathimage = srvChequeFilePath + File.separator + aEffet.getBanque() + File.separator + aEffet.getAgence() + File.separator;
        
        File waitFolder = new File(pathimage + File.separator);
        if ((!waitFolder.exists()) && 
          (!waitFolder.mkdirs())) {
          logEvent("ERREUR", "Impossible de cr?er " + pathimage);
        }
        //BigDecimal compteurEffets = new BigDecimal(Utility.computeCompteur("IDEFFETS", "EFFETS"));
        BigDecimal compteurEffets = new BigDecimal(Utility.computeCompteur("IDEFFET", "EFFETS"));
        fichierimage = aEffet.getNumerocompte_Tire() + aEffet.getRibcompte() + aEffet.getNumeroeffet() + "_" + CMPUtility.getDateHeure() + "_" + compteurEffets;
        
        FileUtils.copyFile(rectoFile, new File(pathimage + fichierimage + "f.jpg"));
        if ((rectoFile.exists()) && (new File(pathimage + fichierimage + "f.jpg").exists()))
        {
          if (!rectoFile.delete()) {
            FileUtils.forceDelete(rectoFile);
          }
          System.out.println("D?placement Recto Cheque OK " + sequence);
          renameSucceeded++;
        }
        else
        {
          System.out.println("D?placement Recto Cheque NON OK " + sequence);
          logEvent("ERREUR", "D?placement Recto Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "f.jpg");
        }
        FileUtils.copyFile(versoFile, new File(pathimage + fichierimage + "r.jpg"));
        if ((versoFile.exists()) && (new File(pathimage + fichierimage + "r.jpg").exists()))
        {
          if (!versoFile.delete()) {
            FileUtils.forceDelete(versoFile);
          }
          System.out.println("D?placement Verso Cheque OK " + sequence);
          renameSucceeded++;
        }
        else
        {
          System.out.println("D?placement Verso Cheque NON OK " + sequence);
          logEvent("ERREUR", "D?placement Verso Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "r.jpg");
        }
        FileUtils.copyFile(dataFile, new File(pathimage + fichierimage + "d.txt"));
        if ((dataFile.exists()) && (new File(pathimage + fichierimage + "d.txt").exists()))
        {
          if (!dataFile.delete()) {
            FileUtils.forceDelete(dataFile);
          }
          System.out.println("D?placement CMC7 Cheque OK " + sequence);
          renameSucceeded++;
        }
        else
        {
          System.out.println("D?placement CMC7 Cheque NON OK " + sequence);
          logEvent("ERREUR", "D?placement CMC7 Cheque NON OK " + sequence + "| vers " + pathimage + fichierimage + "d.jpg");
        }
        if (renameSucceeded == 3)
        {
          sequences.setTypedocument("EFFETS");
          db.insertObjectAsRowByQuery(sequences, "SEQUENCES_EFFETS");
          aEffet.setPathimage(pathimage);
          
          aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
          aEffet.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
          aEffet.setSequence(sequences.getIdsequence());
          aEffet.setMachinescan(repertoire.getPartenaire());
          aEffet.setIdeffet(compteurEffets);
          aEffet.setFichierimage(fichierimage);
          db.insertObjectAsRowByQuery(aEffet, "EFFETS");
        }
        db.close();
      }
      else
      {
        System.out.println("Triplet non complet pour la sequence " + sequence);
        logEvent("ERREUR", "Triplet non complet pour la sequence " + sequence);
      }
    }
    return null;
  }
}
