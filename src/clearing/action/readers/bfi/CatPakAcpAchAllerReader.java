/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.bfi;

import clearing.model.CMPUtility;
import clearing.model.CatClass;
import clearing.model.EnteteRemise;
import clearing.model.ImageIndex;
import clearing.table.Cheques;
import clearing.table.Effets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.patware.action.file.BinFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import static org.patware.utils.Utility.splitToNChar;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CatPakAcpAchAllerReader extends BinFileReader {

    private byte[] bentete = new byte[65];
    private byte[] bindex = new byte[400];
    private String sentete = "";
    private String sindex = "";
    private EnteteRemise enteteRemise = new EnteteRemise();
    private ImageIndex imageIndex = new ImageIndex();
    private String mailoExtFolder = Utility.getParam("IMG_ALLER_FOLDER");
    private String imagePath;
    private String imageName;
    private static final int MAXIMGSIZE = 1000000;

    private void insertImageIndexIntoDB(DataBase db) throws SQLException {

        if (imageIndex.getTypeOperation().equals("030") || imageIndex.getTypeOperation().equals("035")) {
            Cheques aCheque = new Cheques();
            aCheque.setType_Cheque(imageIndex.getTypeOperation());
            aCheque.setNumerocheque(imageIndex.getNumCheque());

            aCheque.setBanque(imageIndex.getIdBanTire());
            aCheque.setAgence(imageIndex.getIdAgeTire());
            aCheque.setNumerocompte(imageIndex.getNumCptTire());
            imageIndex.getCleRibDeb();
            aCheque.setBanqueremettant(imageIndex.getIdBanRem());
            aCheque.setAgenceremettant(imageIndex.getIdAgeRem());
            aCheque.setRio(imageIndex.getRio().getRio());
            aCheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            aCheque.setDatesaisie(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            //imageIndex.getLongRecto();
            //imageIndex.getLongVerso();
            aCheque.setMontantcheque(String.valueOf(Long.parseLong(imageIndex.getMontant())));
            //imageIndex.getCodeCmc7();
            aCheque.setPathimage(imagePath);
            aCheque.setFichierimage(imageName);
//MAJ ou INSERTION
            String sql = "SELECT * FROM CHEQUES WHERE BANQUE ='" + aCheque.getBanque()
                    + "' AND NUMEROCHEQUE ='" + aCheque.getNumerocheque()
                    + "' AND NUMEROCOMPTE ='" + aCheque.getNumerocompte()
                    + "' AND MONTANTCHEQUE ='" + aCheque.getMontantcheque() + "' AND ETAT =" + Utility.getParam("CETAOPERET")
                    + "";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

            if (cheques != null && cheques.length > 0) {
                // aCheque.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));

                sql = "UPDATE CHEQUES SET PATHIMAGE='" + imagePath + "',FICHIERIMAGE='" + imageName + "', ETAT=" + Utility.getParam("CETAOPERETREC") + " WHERE IDCHEQUE =" + cheques[0].getIdcheque();
                db.executeUpdate(sql);
                // db.updateRowByObjectByQuery(aCheque, "CHEQUES", sql);

            } else {
////                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
////                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETIMA")));
////                db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
            }

        } else {
            Effets aEffet = new Effets();
            aEffet.setType_Effet(imageIndex.getTypeOperation());
            aEffet.setNumeroeffet(imageIndex.getNumEffet());
            aEffet.setIban_Tire(imageIndex.getPfxIBANDeb());
            aEffet.setBanque(imageIndex.getIdBanTire());
            aEffet.setAgence(imageIndex.getIdAgeTire());
            aEffet.setNumerocompte_Tire(imageIndex.getNumCptTire());
            imageIndex.getCleRibDeb();
            aEffet.setBanqueremettant(imageIndex.getIdBanRem());
            aEffet.setAgenceremettant(imageIndex.getIdAgeRem());
            aEffet.setRio(imageIndex.getRio().getRio());
            aEffet.setDatetraitement(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            aEffet.setDatesaisie(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            imageIndex.getLongRecto();
            imageIndex.getLongVerso();
            aEffet.setMontant_Effet(String.valueOf(Long.parseLong(imageIndex.getMontant())));
            imageIndex.getCodeCmc7();
            aEffet.setPathimage(imagePath);
            aEffet.setFichierimage(imageName);
            //MAJ ou INSERTION

            String sql = "SELECT * FROM EFFETS WHERE BANQUE ='" + aEffet.getBanque()
                    + "' AND NUMEROEFFET ='" + aEffet.getNumeroeffet()
                    + "' AND NUMEROCOMPTE_TIRE ='" + aEffet.getNumerocompte_Tire()
                    + "' AND MONTANT_EFFET ='" + aEffet.getMontant_Effet() + "' AND ETAT =" + Utility.getParam("CETAOPERET")
                    + "";
            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

            if (effets != null && effets.length > 0) {
                aEffet.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));

                sql = " IDEFFET =" + effets[0].getIdeffet();

                db.updateRowByObjectByQuery(aEffet, "EFFETS", sql);
            } else {

                aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETIMA")));
                aEffet.setIdeffet(new BigDecimal(Utility.computeCompteur("IDEFFET", "EFFETS")));
                db.insertObjectAsRowByQuery(aEffet, "EFFETS");
            }

        }
    }

    private String writeImage(String afile, byte[] verso, CatClass catPak, String imgName, String extension) throws Exception {

        String imagePath = getImagePath(catPak);
        String imageName = imgName;

        if (!new File(imagePath).exists()) {
            if (!new File(imagePath).mkdirs()) {
                logEvent("ERREUR", "Impossible de creer " + imagePath);
            }
        }

        // convert byte array back to BufferedImage
        InputStream in = new ByteArrayInputStream(verso);
        BufferedImage bImageFromConvert = ImageIO.read(in);

        ImageIO.write(bImageFromConvert, "jpg", new File(imagePath + imageName + extension)); //"r.jpg"
        return imagePath + imageName + extension;

    }

    private void linkImageIntoDB(DataBase db, CatClass catClass) throws SQLException {

        if (catClass.getCodeValeur().equals("30") || catClass.getCodeValeur().equals("33")) {
            Cheques aCheque = new Cheques();
            aCheque.setType_Cheque(catClass.getCodeValeur());
            aCheque.setNumerocheque(catClass.getNumeroValeur().substring(4));

            aCheque.setBanque(catClass.getCompteTire().substring(0, 3));  //le tir ==> UBA GUINEE
            aCheque.setAgence(catClass.getCompteTire().substring(3, 6)); //le tir ==> UBA GUINEE
            aCheque.setNumerocompte(catClass.getCompteTire().substring(6, 16)); //le tir ==> UBA GUINEE
            aCheque.setRibcompte(catClass.getCompteTire().substring(16, 18));
            //    imageIndex.getCleRibDeb();
            aCheque.setBanqueremettant(catClass.getBanqueRemettante()); //le remettant ==> Autre Banque
            aCheque.setAgenceremettant(catClass.getCompteBeneficiaire().substring(3, 6)); //le remettant ==> Autre Banque
            aCheque.setCompteremettant(catClass.getCompteBeneficiaire().substring(6, 16));

            aCheque.setMontantcheque(String.valueOf(Long.parseLong(catClass.getMontant())));
            aCheque.setDateecheance(catClass.getSignature()); //toute la ligne du  .CAT
            aCheque.setCalcul(catClass.getCodeTransaction());
            aCheque.setGarde(catClass.getCodeEtat()); //Code état de la ligne CMC7  Champ Garde de cheque
            aCheque.setCodeVignetteUV(catClass.getClefControle());
            aCheque.setRio_Rejet(catClass.getFiller()); //filler dans  Rio_Rejet
            aCheque.setDatesaisie(Utility.convertDateToString(new java.util.Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            aCheque.setDatetraitement(aCheque.getDatesaisie());
            aCheque.setEtablissement(CMPUtility.getCodeBanque());
            aCheque.setDateecheance(catClass.getIdentifiantDocument());

            //imageIndex.getCodeCmc7();
            if (catClass.getImagePath() != null && catClass.getImageName() != null) {

                aCheque.setPathimage(catClass.getImagePath());
                aCheque.setFichierimage(catClass.getImageName());

            }

            //MAJ ou INSERTION
            String sql = "SELECT * FROM CHEQUES WHERE "
                    + " TRIM(BANQUE) ='" + aCheque.getBanque().trim() + "'"
                    + " AND TRIM(AGENCE) ='" + aCheque.getAgence().trim() + "'"
                    + " AND TRIM(NUMEROCHEQUE) ='" + aCheque.getNumerocheque().trim() + "'"
                    + " AND TRIM(NUMEROCOMPTE) ='" + aCheque.getNumerocompte().trim() + "'"
                    + " AND TRIM(DATETRAITEMENT) ='" + aCheque.getDatetraitement().trim() + "'"
                    + " AND TRIM(NUMEROCHEQUE) ='" + aCheque.getNumerocheque().trim() + "'"
                    + " AND TRIM(BANQUEREMETTANT) ='" + aCheque.getBanqueremettant().trim() + "'"
                    + " AND TRIM(COMPTEREMETTANT) ='" + aCheque.getCompteremettant().trim() + "'"
                    + " AND TRIM(AGENCEREMETTANT) ='" + aCheque.getAgenceremettant().trim() + "'"
                    + " AND TRIM(MONTANTCHEQUE) ='" + aCheque.getMontantcheque().trim() + "'  "
                    + " ORDER BY REMISE";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
            System.out.println("Requete pour voir si le cheque existe");
            int giveUpVar = 0;
            while (cheques.length == 0 && giveUpVar <= 7) {
                try {
                    Thread.sleep(5000);
                    giveUpVar = giveUpVar + 1;
                    System.out.println("Le cheque scanne sur Barberousse n'est pas encore en BD. on attend encore");
                    cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                } catch (InterruptedException ex) {
                    Logger.getLogger(CatPakAcpAchAllerReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (giveUpVar == 8) {
                System.out.println("Giving up, On insere le cheque à partir du fichier CAT/PAK. Pas de fichier Data deja inséré");

            }

            if (cheques != null && cheques.length > 0) {

                // aCheque.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));
                if (cheques[0].getEtat().toPlainString().equals(Utility.getParam("CETAOPEANO"))
                        || cheques[0].getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM3ACC"))
                        || cheques[0].getEtat().toPlainString().equals(Utility.getParam("CETAREMERR"))) {
                    System.out.println("Cheque Existe deja et est en erreur ou Annulé en compense");
                    if (cheques[0].getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM3ACC"))) {
                        System.out.println("Cheque parti en compensation et Annulé depuis WBC " + cheques[0].getIdcheque() + " ");
                    }
                    aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));

                    aCheque.setEtat(new BigDecimal(Utility.getParam("CETAIMASTO")));
                    db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
                } else if (cheques[0].getEtat().toPlainString().equals(Utility.getParam("CETAOPEVAL"))) {
                    System.out.println("Cheque Existe deja et est integré par le datareader" + cheques[0].getIdcheque());
                    System.out.println("Cheque integré par le datareader" + cheques[0].getIdcheque());
                    cheques[0].setFichierimage(aCheque.getFichierimage());
                    cheques[0].setPathimage(aCheque.getPathimage());
                    cheques[0].setDateecheance(aCheque.getDateecheance());

                    db.updateRowByObjectByQuery(cheques[0], "CHEQUES", "IDCHEQUE=" + cheques[0].getIdcheque());

//                    db.executeUpdate(sql);
                } else if (cheques[0].getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM3ACC"))) { //Cheque parti en compensation et Annulé depuis WBC
                    System.out.println("Cheque parti en compensation et Annulé depuis WBC " + cheques[0].getIdcheque() + " ");

                }

                // db.updateRowByObjectByQuery(aCheque, "CHEQUES", sql);
            } else {
                System.out.println("Cheque n'existe pas");
                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAIMASTO")));
                db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
            }

        } else {
            Effets aEffet = new Effets();
            aEffet.setType_Effet(catClass.getCodeValeur());
            aEffet.setNumeroeffet(imageIndex.getNumEffet());
            aEffet.setIban_Tire(imageIndex.getPfxIBANDeb()); //le tire==> ECM
            aEffet.setBanque(imageIndex.getIdBanTire()); //le tire ==> ECM
            aEffet.setAgence(imageIndex.getIdAgeTire()); //le tire ==> ECM
            aEffet.setNumerocompte_Tire(imageIndex.getNumCptTire()); //le tire ==> ECM

//            imageIndex.getCleRibDeb();
            aEffet.setBanqueremettant(imageIndex.getIdBanRem());  //le remettant ==> Autre Banque
            aEffet.setAgenceremettant(imageIndex.getIdAgeRem());  //le remettant ==> Autre Banque
//            aEffet.setRio(imageIndex.getRio().getRio()); //pas de RIO pour systac
            aEffet.setDatetraitement(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
            aEffet.setDatesaisie(Utility.convertDateToString(imageIndex.getDateOrdreClient(), ResLoader.getMessages("patternDate")));
//            imageIndex.getLongRecto();
//            imageIndex.getLongVerso();
            aEffet.setMontant_Effet(String.valueOf(Long.parseLong(imageIndex.getMontant())));
//            imageIndex.getCodeCmc7();
            aEffet.setPathimage(imagePath);
            aEffet.setFichierimage(imageName);
            //MAJ ou INSERTION

            String sql = "SELECT * FROM EFFETS WHERE BANQUE ='" + aEffet.getBanque()
                    + "' AND NUMEROEFFET ='" + aEffet.getNumeroeffet()
                    + "' AND NUMEROCOMPTE_TIRE ='" + aEffet.getNumerocompte_Tire()
                    + "' AND MONTANT_EFFET ='" + aEffet.getMontant_Effet() + "' AND ETAT =" + Utility.getParam("CETAOPERET")
                    + "";
            Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

            if (effets != null && effets.length > 0) {
                aEffet.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));

                sql = " IDEFFET =" + effets[0].getIdeffet();

                db.updateRowByObjectByQuery(aEffet, "EFFETS", sql);
            } else {

                aEffet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETIMA")));
                aEffet.setIdeffet(new BigDecimal(Utility.computeCompteur("IDEFFET", "EFFETS")));
                db.insertObjectAsRowByQuery(aEffet, "EFFETS");
            }

        }
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());

        try {
            String line = "";
            Path path = Paths.get(aFile.toURI());
            // Load as binary:
            byte[] bytes = Files.readAllBytes(path);
            String asText = new String(bytes, StandardCharsets.ISO_8859_1);//, StandardCharsets.UTF_8 ISO_8859_1
//            System.out.println("asText" + asText);
            // Load as text, with some Charset:
//            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            setFile(aFile);

            String aFileName = aFile.getName().replaceAll("#CatPakAcpAchAllerReader", "");

            String parent = aFile.getParent() + File.separator + "TREATED";
            if (!new File(parent).exists()) {
                if (!new File(parent).mkdirs()) {
                    logEvent("ERREUR", "Impossible de creer " + parent);
                }

            }
            File fileCheque = new File(parent + File.separator + aFileName);
            String fileChequeParent = fileCheque.getParent();
            String fileChequeName = fileCheque.getName();

//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(aFile), "ISO8859_6")); //"Cp1252" ISO-8859-5 //"ISO-2022-KR"  x-MacCyrillic  
            String[] splitToNChar = splitToNChar(asText, 300);
            if (splitToNChar.length > 0) {
                for (int i = 0; i < splitToNChar.length; i++) {
                    String ligneCheque = splitToNChar[i];

                    // for (String ligneCheque : splitToNChar) {
                    //02+12+18+18+01+02+03+03+08+03+15+01+02+02+02+22+09+10+09+10+16+128
                    //Une ligne de cheques 
                    setCurrentLine(ligneCheque);
                    System.out.println("taille de ligne " + ligneCheque.length());
                    String codeValeur = getChamp(2); //Code valeur
                    String numeroValeur = getChamp(12); //Numero Valeur Numero Cheque
                    String compteTire = getChamp(18); //compte Tire
                    String compteBeneficiaire = getChamp(18); //compte Beneficiaire
                    String natureValeur = getChamp(1); //nature Valeur ??
                    String codePays = getChamp(2); //Code Pays
                    String banqueRemettante = getChamp(3); //banque Remettante
                    String agenceRemettante = getChamp(3); //agence Remettante
                    String dateDeScenarisation = getChamp(8); //Date de scenarisation
                    String devise = getChamp(3); //devise
                    String montant = getChamp(15); //montant
                    String codeCorrectionMICR = getChamp(1); //code Correction MICR
                    String codeTransaction = getChamp(2); //code Transaction Code de la Transaction == Nature du chèque 1 : cheque personnel 02 : cheque d?entreprise 03 : Chèque de Banque
                    String codeEtat = getChamp(2); //Code Etat
                    String clefControle = getChamp(2); //clef Controle
                    String filler = getChamp(26); //filler 22 dans la doc mais je pense 26
                    String longueurRecto = getChamp(9); //longueur Recto
                    String debutLongueurRecto = getChamp(10); //debut Longueur Recto   
                    String longueurVerso = getChamp(9); //longueur Verso
                    String debutLongueurVerso = getChamp(10); //debut Longueur Verso
                    String referenceClef = getChamp(16); //reference Clef

                    CatClass imageCheque = new CatClass();
                    imageCheque.setAgenceRemettante(agenceRemettante);
                    imageCheque.setBanqueRemettante(banqueRemettante);
                    imageCheque.setCodeCorrection(codeCorrectionMICR);
                    imageCheque.setCodePays(codePays);
                    imageCheque.setCodeValeur(codeValeur);
                    imageCheque.setCompteBeneficiaire(compteBeneficiaire);
                    imageCheque.setCompteTire(compteTire);
                    imageCheque.setCodeTransaction(codeTransaction);
                    imageCheque.setNatureValeur(natureValeur);
                    imageCheque.setDateCapture(dateDeScenarisation);
                    imageCheque.setLongueurRecto(longueurRecto);
                    imageCheque.setLongueurVerso(longueurVerso);
                    imageCheque.setNumeroValeur(numeroValeur);
                    imageCheque.setMontant(montant);
                    imageCheque.setPositionDebutRecto(debutLongueurRecto);
                    imageCheque.setPositionDebutVerso(debutLongueurVerso);
                    imageCheque.setFiller(filler);

                    imageCheque.setReferenceClef(referenceClef);
                    imageCheque.setClefControle(clefControle);
                    imageCheque.setCodeEtat(codeEtat);

                    //Aller chercher le fichier .PAK et composer l'image
                    String absolutePath = aFile.getAbsolutePath();
                    int lastIndexOf = aFile.getAbsolutePath().lastIndexOf(".");

                    String fileWithoutExtension = absolutePath.substring(0, lastIndexOf);
                    String pakFile = fileWithoutExtension + ".PAK";
                    imageCheque.setIdentifiantDocument(fileChequeParent + File.separator + fileChequeName);

                    //Ecrire l'image recto
                    //RECTO
                    byte[] rectoImage = readFromFile(pakFile, new Integer(imageCheque.getPositionDebutRecto()), new Integer(imageCheque.getLongueurRecto()));
                    //ECRIRE L'IMAGE RECTO
                    writeImage(pakFile, rectoImage, imageCheque, fileWithoutExtension.substring(fileWithoutExtension.lastIndexOf("\\") + 1), "f.jpg"); //.lastIndexOf("\\")
                    imageCheque.setImagePath(getImagePath(imageCheque));
                    imageCheque.setImageName(fileWithoutExtension.substring(fileWithoutExtension.lastIndexOf("\\") + 1));

                    //VERSO
                    byte[] versoImage = readFromFile(pakFile, new Integer(imageCheque.getPositionDebutVerso()), new Integer(imageCheque.getLongueurVerso()));

                    //ECRIRE L'IMAGE RECTO
                    writeImage(pakFile, versoImage, imageCheque, fileWithoutExtension.substring(fileWithoutExtension.lastIndexOf("\\") + 1), "r.jpg");

                    linkImageIntoDB(db, imageCheque);
                    //Renommer le fichier .PAK

                    if (i + 1 == splitToNChar.length) {
                        File file = new File(pakFile + "#CatPakAcpAchAllerReader#TERMINE");
                        FileUtils.copyFile(new File(pakFile), file);
                        File treatFile = new File(pakFile);
                        if (treatFile.exists()) {

                            FileUtils.moveFile(treatFile, new File(fileCheque.getParent() + File.separator + treatFile.getName()));

                        }
                    }
                }

            }

//            String[] splitToNChar = splitToNChar(asText, 300);
            db.close();
            FileUtils.copyFile(aFile, fileCheque);

        } catch (Exception ex) {
            Logger.getLogger(CatPakAcpAchAllerReader.class.getName()).log(Level.INFO, null, ex);
            db.close();
        }

        return aFile;
    }

    public String escapeUnicode(String input) {
        StringBuilder b = new StringBuilder(input.length());
        Formatter f = new Formatter(b);
        for (char c : input.toCharArray()) {
            if (c < 128) {
                b.append(c);
            } else {
                f.format("\\u%04x", (int) c);
            }
        }
        return b.toString();
    }

    public CatPakAcpAchAllerReader() {
        setCopyOriginalFile(true);
    }

    private byte[] readFromFile(String filePath, int position, int size)
            throws IOException {

        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        file.seek(position);
        byte[] bytes = new byte[size];
        file.read(bytes);
        file.close();
        return bytes;

    }

    public String getImagePath(CatClass catPak) {

        String image = mailoExtFolder + File.separator
                + catPak.getDateCapture() + File.separator
                + catPak.getBanqueRemettante() + File.separator
                + catPak.getAgenceRemettante() + File.separator
                + catPak.getCodeValeur() + File.separator;
//        System.out.println("getImagePath " + image);
        return image;
    }

    public String getImageName(CatClass catPak) {

        String image = catPak.getCompteBeneficiaire() + "_" + catPak.getCodeValeur().substring(4) + Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss");
//        System.out.println("getImageName " + image);
        return image;
    }

    private void writeToFile(String filePath, String data, int position)
            throws IOException {

        RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        file.seek(position);
        file.write(data.getBytes());
        file.close();

    }

    public void writeImage(String fileName, byte[] bytes) {
        if (!new File(fileName).exists()) {
            if (!new File(fileName).mkdirs()) {
                logEvent("ERREUR", "Impossible de creer " + fileName);
            }
        }
        OutputStream output = null;
        try {
            InputStream input = new ByteArrayInputStream(bytes);
            output = new FileOutputStream(fileName);
            IOUtils.copy(input, output);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CatPakAcpAchAllerReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CatPakAcpAchAllerReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(CatPakAcpAchAllerReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
