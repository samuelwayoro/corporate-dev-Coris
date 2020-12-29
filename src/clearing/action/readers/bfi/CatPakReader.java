/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.bfi;

import clearing.model.CatClass;
import clearing.model.EnteteRemise;
import clearing.model.ImageIndex;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Effets;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
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
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CatPakReader extends BinFileReader {

    private byte[] bentete = new byte[65];
    private byte[] bindex = new byte[400];
    private String sentete = "";
    private String sindex = "";
    private EnteteRemise enteteRemise = new EnteteRemise();
    private ImageIndex imageIndex = new ImageIndex();
    private String mailoExtFolder = Utility.getParam("MAILO_EXT_FOLDER");
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

    private void readEntete(BufferedInputStream bis) throws IOException {
        if (bis.read(bentete) != -1) {

            sentete = new String(bentete);
            setCurrentLine(sentete);
            enteteRemise.setIdEntete(getChamp(4));
            enteteRemise.setIdRecepeteur(getChamp(5));
            enteteRemise.setRefRemise(getChamp(3));
            enteteRemise.setDatePresentation(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
            enteteRemise.setIdEmetteur(getChamp(5));
            enteteRemise.setDevise(getChamp(3));
            enteteRemise.setTypeRemise(getChamp(3));
            enteteRemise.setIdDestinataire(getChamp(5));
            enteteRemise.setCodeLieu(getChamp(2));
            enteteRemise.setNbTotOperVal(getChamp(4));

            bis.read(new byte[1]);

        }
    }

    private void readImageIndex(BufferedInputStream bis) throws IOException {
        if (bis.read(bindex) != -1) {
            sindex = new String(bindex);
            setCurrentLine(sindex);
            imageIndex.setTypeOperation(getChamp(3));
            imageIndex.setNumCheque(getChamp(7));
            imageIndex.setIdBanTire(getChamp(5));
            imageIndex.setIdAgeTire(getChamp(5));
            imageIndex.setNumCptTire(getChamp(12));
            imageIndex.setCleRibDeb(getChamp(2));
            imageIndex.setIdBanRem(getChamp(5));
            imageIndex.setIdAgeRem(getChamp(5));
            imageIndex.setRio(new RIO(getChamp(35)));
            imageIndex.setDateOrdreClient(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
            imageIndex.setLongRecto(getChamp(9));
            imageIndex.setLongVerso(getChamp(9));
            imageIndex.setMontant(getChamp(16));
            imageIndex.setCodeCmc7(getChamp(1));
            imageIndex.setBlancs(getChamp(278));
            bis.read(new byte[2]);
        }
    }

    private void writeImageRecto(String afile, byte[] image, CatClass catPak) throws Exception {
//        System.out.println("writeImageRecto");

//
        imagePath = getImagePath(catPak);
        imageName = getImageName(catPak);

        if (!new File(imagePath).exists()) {
            if (!new File(imagePath).mkdirs()) {
                logEvent("ERREUR", "Impossible de créer " + imagePath);
            }
        }

        // convert byte array back to BufferedImage
        InputStream in = new ByteArrayInputStream(image);
        BufferedImage bImageFromConvert = ImageIO.read(in);

        ImageIO.write(bImageFromConvert, "jpg", new File(imagePath
                + imageName
                + "f.jpg"));

    }

    private void writeImageVerso(String afile, byte[] verso, CatClass catPak) throws Exception {

        imagePath = getImagePath(catPak);
        imageName = getImageName(catPak);

        // convert byte array back to BufferedImage
        InputStream in = new ByteArrayInputStream(verso);
        BufferedImage bImageFromConvert = ImageIO.read(in);

        ImageIO.write(bImageFromConvert, "jpg", new File(imagePath
                + imageName
                + "r.jpg"));

    }

    private void linkImageIntoDB(DataBase db, CatClass catClass) throws SQLException {

        if (catClass.getCodeValeur().equals("30") || catClass.getCodeValeur().equals("33")) {
            Cheques aCheque = new Cheques();
            aCheque.setType_Cheque(catClass.getCodeValeur());
            aCheque.setNumerocheque(catClass.getIdentifiantDocument().substring(0, 7));

            aCheque.setBanque(catClass.getIdentifiantDocument().substring(7, 12));  //le tiré ==> ECM
            aCheque.setAgence(catClass.getIdentifiantDocument().substring(12, 17)); //le tiré ==> ECM
            aCheque.setNumerocompte(catClass.getIdentifiantDocument().substring(17, 28)); //le tiré ==> ECM
            aCheque.setRibcompte(catClass.getIdentifiantDocument().substring(28, 30));
            //    imageIndex.getCleRibDeb();
            aCheque.setBanqueremettant(catClass.getBanqueRemettante()); //le remettant ==> Autre Banque
            aCheque.setAgenceremettant(catClass.getAgenceRemettante()); //le remettant ==> Autre Banque

            aCheque.setMontantcheque(String.valueOf(Long.parseLong(catClass.getMontant())));
            //imageIndex.getCodeCmc7();
            if (imagePath != null && imageName != null) {

                aCheque.setPathimage(imagePath);
                aCheque.setFichierimage(imageName);
            }

//MAJ ou INSERTION
            String sql = "SELECT * FROM CHEQUES WHERE BANQUE ='" + aCheque.getBanque()
                    + "' AND NUMEROCHEQUE ='" + aCheque.getNumerocheque()
                    + "' AND NUMEROCOMPTE ='" + aCheque.getNumerocompte()
                    + "' AND MONTANTCHEQUE ='" + aCheque.getMontantcheque() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

            if (cheques != null && cheques.length > 0) {
                // aCheque.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));

                sql = "UPDATE CHEQUES SET PATHIMAGE='" + imagePath + "',FICHIERIMAGE='" + imageName + "'  "
                        + ", ETAT=" + Utility.getParam("CETAOPERET") + " " //CETAOPERETREC 152
                        + " WHERE IDCHEQUE =" + cheques[0].getIdcheque();
                db.executeUpdate(sql);
                // db.updateRowByObjectByQuery(aCheque, "CHEQUES", sql);

            } else {
                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERET")));
                db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
            }

        } else {
            Effets aEffet = new Effets();
            aEffet.setType_Effet(catClass.getCodeValeur());
            aEffet.setNumeroeffet(imageIndex.getNumEffet());
            aEffet.setIban_Tire(imageIndex.getPfxIBANDeb()); //le tiré ==> ECM
            aEffet.setBanque(imageIndex.getIdBanTire()); //le tiré ==> ECM
            aEffet.setAgence(imageIndex.getIdAgeTire()); //le tiré ==> ECM
            aEffet.setNumerocompte_Tire(imageIndex.getNumCptTire()); //le tiré ==> ECM

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
            Path path = Paths.get(aFile.toURI());
            // Load as binary:
            byte[] bytes = Files.readAllBytes(path);
            String asText = new String(bytes);//, StandardCharsets.UTF_8
//            System.out.println("asText" + asText);
            // Load as text, with some Charset:
//            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            setFile(aFile);
            //   System.out.println("aFile getAbsolutePath " + aFile.getAbsolutePath() + " afile" + aFile.getName());
            //   BufferedInputStream is = openFile(aFile);

            byte[] contents = new byte[10024];
            int bytesRead = 0;
            String strFileContents;
//            while ((bytesRead = is.read(contents)) != -1) {
            strFileContents = new String(contents, 0, bytesRead);
            // System.out.print(strFileContents);
            String[] split = asText.split("          ");
            System.out.println(" Nbre d'ordres: " + split.length + " devant etre dans  le .PAK ");
            if (split.length > 0) {
                for (int i = 0; i < split.length; i++) {
                    String string = split[i];
                    System.out.println("Ordre  " + i + " ");
                    CatClass catClass = new CatClass();
                    setCurrentLine(string);
                    catClass.setIdentifiantDocument(getChamp(30));
                    catClass.setCodePays(getChamp(2));
                    catClass.setBanqueRemettante(getChamp(5));
                    catClass.setAgenceRemettante(getChamp(5));
                    catClass.setDateCapture(getChamp(8));
                    catClass.setLongueurRecto(getChamp(9));
                    catClass.setPositionDebutRecto(getChamp(10));
                    catClass.setLongueurVerso(getChamp(9));
                    catClass.setPositionDebutVerso(getChamp(10));
                    catClass.setSignature(getChamp(128));
                    catClass.setReferenceClef(getChamp(16));
                    catClass.setMontant(getChamp(15));
                    catClass.setCodeValeur(getChamp(2));
                    catClass.setCodeCorrection(getChamp(1));

                    //Aller chercher le fichier .PAK et composer l'image
                    String absolutePath = aFile.getAbsolutePath();
                    int lastIndexOf = aFile.getAbsolutePath().lastIndexOf(".");

                    String fileWithoutExtension = absolutePath.substring(0, lastIndexOf);
                    String pakFile = fileWithoutExtension + ".PAK";
//                    System.out.println("pakFile " + pakFile);
//                    System.out.println(" catClass " + catClass.toString());

                    //Ouvrir le fichier pak pour les images
                    //RECTO
                    byte[] rectoImage = readFromFile(pakFile, new Integer(catClass.getPositionDebutRecto()), new Integer(catClass.getLongueurRecto()));
                    //ECRIRE L'IMAGE RECTO
                    writeImageRecto(pakFile, rectoImage, catClass);

                    //VERSO
                    byte[] versoImage = readFromFile(pakFile, new Integer(catClass.getPositionDebutVerso()), new Integer(catClass.getLongueurVerso()));
//                    System.out.println("versoImage" + versoImage.length);
                    //ECRIRE L'IMAGE RECTO
                    writeImageVerso(pakFile, versoImage, catClass);
                    linkImageIntoDB(db, catClass);

                    //Renommer le fichier .PAK
                    System.out.println("treatFile a renommer " + pakFile);
                    if (i+1 == split.length) {
                        File file = new File(pakFile + "#CatPakReader#TERMINE");
                        File treatFile = new File(pakFile);
                        if (treatFile.exists()) {
                            System.out.println("file exist treatFile");
                            System.out.println("file" + file);
                            FileUtils.moveFile(treatFile, new File(treatFile + "#CatPakReader#TERMINE"));

                        }
                    }

                }

            }

            db.close();

        } catch (Exception ex) {
            Logger.getLogger(CatPakReader.class.getName()).log(Level.INFO, null, ex);
            db.close();
        }
        return aFile;
    }

    public CatPakReader() {
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

        String image = catPak.getIdentifiantDocument()
                //    + catPak.getCleRibDeb()
                //                + ((catPak.getCodeValeur().equals("30") || (catPak.getCodeValeur().equals("33"))) ? catPak.getIdentifiantDocument() : catPak.getIdentifiantDocument())
                + new Long(catPak.getMontant());
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
                logEvent("ERREUR", "Impossible de créer " + fileName);
            }
        }
        OutputStream output = null;
        try {
            InputStream input = new ByteArrayInputStream(bytes);
            output = new FileOutputStream(fileName);
            IOUtils.copy(input, output);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CatPakReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CatPakReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(CatPakReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
