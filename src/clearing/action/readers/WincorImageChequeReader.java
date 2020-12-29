/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers;

import clearing.model.CMPUtility;
import clearing.model.WincorCheque;
import clearing.table.Cheques;
import clearing.table.Remises;
import clearing.table.Sequences;
import java.awt.image.BufferedImage;
import org.patware.action.file.FlatFileReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.patware.bean.table.Repertoires;
import org.patware.bean.table.Params;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;
import org.patware.xml.WincorXmlReader;

/**
 *
 * @author Patrick
 */
public class WincorImageChequeReader extends FlatFileReader  {

    private BigDecimal lastIdRemise;
    private static Hashtable<String, String> codeSica2Cache = new Hashtable<String, String>();
    private static Hashtable<String, String> codeSica3Cache = new Hashtable<String, String>();
    private WincorXmlReader wincorXmlReader;
    private WincorCheque[] wincorCheque;
   

    public WincorImageChequeReader() {
         
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


        wincorXmlReader = new WincorXmlReader(aFile);

        String[] nums = wincorXmlReader.getNums();
        String[] cmc7s = wincorXmlReader.getCmc7s();
        String[] agences = wincorXmlReader.getAgences();
        String[] atms = wincorXmlReader.getAtms();
        String[] cmc7oks = wincorXmlReader.getCmc7oks();
        String[] compteRs = wincorXmlReader.getCompteRs();
        String[] dateOs = wincorXmlReader.getDateOs();
        String[] dteServers = wincorXmlReader.getDteServers();
        String[] images = wincorXmlReader.getImages();
        String[] numBRemises = wincorXmlReader.getNumBRemises();
        String[] remises = wincorXmlReader.getRemises();

        if (nums != null) {
            wincorCheque = new WincorCheque[nums.length];

            for (int i = 0; i < wincorCheque.length; i++) {
                wincorCheque[i] = new WincorCheque(nums[i], cmc7s[i], cmc7oks[i], dateOs[i], agences[i], atms[i], compteRs[i], remises[i], numBRemises[i], dteServers[i], images[i]);
            }
        }

        String imgAllerFolder = Utility.getParam("IMG_ALLER_FOLDER");

        String remiseFileName = "";
        String versoFileName = "";
        String rectoFileName = "";

        String srvRootFilePath = imgAllerFolder + File.separator + repertoire.getPartenaire() + File.separator + CMPUtility.getDate();
        String srvRemiseFilePath = srvRootFilePath + File.separator + "REMISES";
        String srvChequeFilePath = srvRootFilePath + File.separator + "CHEQUES";
        String pathimage = "";
        String fichierimage = "";

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String lastCompteR = "";
        String currentCompteR;
        Remises aRemise = new Remises();
        Sequences sequences = new Sequences();

        for (int i = 0; i < wincorCheque.length; i++) {
            currentCompteR = Utility.bourrageGZero(wincorCheque[i].getCompteR(), 12);
            if (!currentCompteR.equals(lastCompteR)) {


                remiseFileName = wincorCheque[i].getImage() + "r.jpg";
                File remiseFile = new File(repertoire.getChemin() + File.separator + remiseFileName);
                FileUtils.copyFile(remiseFile, new File(srvRemiseFilePath + File.separator + wincorCheque[i].getImage() + "f.jpg"));

                sequences.setIdsequence(new BigDecimal(Utility.computeCompteur("IDSEQUENCE", "SEQUENCES")));
                sequences.setTypedocument("REMISE");
                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                sequences.setMachinescan(repertoire.getPartenaire());
                db.insertObjectAsRowByQuery(sequences, "SEQUENCES");

                lastIdRemise = new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE"));
                aRemise.setIdremise(lastIdRemise);
                aRemise.setNomUtilisateur(repertoire.getId());
                
                aRemise.setEtat(new BigDecimal(Utility.getParam("CETAREMSTO")));
                aRemise.setSequence(sequences.getIdsequence());
                aRemise.setPathimage(srvRemiseFilePath + File.separator);
                aRemise.setFichierimage(wincorCheque[i].getImage());
                aRemise.setMachinescan(repertoire.getPartenaire());
                aRemise.setCompteRemettant(currentCompteR);
                aRemise.setMontant("100000000000");

                db.insertObjectAsRowByQuery(aRemise, "REMISES");

            }
            lastCompteR = currentCompteR;
            //------------------------------------------------------

            rectoFileName = wincorCheque[i].getImage() + "f.jpg";
            versoFileName = wincorCheque[i].getImage() + "r.jpg";

            File versoFile = new File(repertoire.getChemin() + File.separator + versoFileName);
            File rectoFile = new File(repertoire.getChemin() + File.separator + rectoFileName);
            if (versoFile.exists() && rectoFile.exists()) {


                Cheques aCheque = new Cheques();

                sequences.setIdsequence(new BigDecimal(Utility.computeCompteur("IDSEQUENCE", "SEQUENCES")));
                sequences.setCodeline(wincorCheque[i].getCmc7());
                sequences.setDatedescan(Utility.convertDateToString(new Date(System.currentTimeMillis()), "dd/MM/yyyy"));
                sequences.setMachinescan(repertoire.getPartenaire());

                //Lecture ligne CMC7
                setCurrentLine(wincorCheque[i].getCmc7());
                getChamp(1);
                aCheque.setNumerocheque(getChamp(7).trim().replaceAll("\\p{Punct}", "_"));
                getChamp(2);
                aCheque.setBanque(getAlphaNumericCodeBanque(Utility.bourrageGauche(getChamp(5).trim().replaceAll("\\p{Punct}", "_"), 5, "_")));
                aCheque.setAgence(getChamp(5).trim().replaceAll("\\p{Punct}", "_"));
                aCheque.setRibcompte(getChamp(2).trim().replaceAll("\\p{Punct}", "_"));
                getChamp(2);
                aCheque.setNumerocompte(getChamp(12).trim().replaceAll("\\p{Punct}", "_"));

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

                if (rectoFile.renameTo(new File(pathimage + fichierimage + "f.jpg"))) {
                    System.out.println("Déplacement Recto Cheque OK ");
                    renameSucceeded++;
                } else {
                    System.out.println("Déplacement Recto Cheque NON OK ");
                    logEvent("ERREUR", "Déplacement Recto Cheque NON OK  vers " + pathimage + fichierimage + "f.jpg");
                }
                File newVersoFile = new File(pathimage + fichierimage + "r.jpg");
                if (versoFile.exists()) {
                   System.out.println("Déplacement Verso Cheque en cours ");
                   BufferedImage bimg = ImageIO.read(versoFile);
                   Boolean mayTattoo = new Boolean(ResLoader.getMessages("TattooPic"));
                   if(mayTattoo.booleanValue()) Utility.tattooPicture(bimg, newVersoFile,"DATECOMPENS_ENDOS");
                   ImageIO.write(bimg, "jpeg", newVersoFile);
                   versoFile.delete();
                   renameSucceeded++;
                } else {
                    System.out.println("Déplacement Verso Cheque NON OK ");
                    logEvent("ERREUR", "Déplacement Verso Cheque NON OK vers " + pathimage + fichierimage + "r.jpg");
                }

                if (renameSucceeded == 2) {
                    sequences.setTypedocument("CHEQUE");
                    db.insertObjectAsRowByQuery(sequences, "SEQUENCES");
                    aCheque.setPathimage(pathimage);
                    aCheque.setFichierimage(fichierimage);
                    aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                    aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
                    aCheque.setRemise(lastIdRemise);
                    aCheque.setSequence(sequences.getIdsequence());
                    aCheque.setMachinescan(repertoire.getPartenaire());
                    aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                    db.insertObjectAsRowByQuery(aCheque, "CHEQUES");

                }




            } else {
                System.out.println("Images non encore disponible ");
                logEvent("ERREUR", "Images non encore disponible ");
            }

        }

        db.close();




        return aFile;
    }
}
