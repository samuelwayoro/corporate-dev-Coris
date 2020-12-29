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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
public class CatPakAcpAchRetourReader extends BinFileReader {

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

    private void writeImage(String afile, byte[] verso, CatClass catPak, String extension, String imgName) throws Exception { //
        System.out.println("writeImageVerso");

        imagePath = getImagePath(catPak);
        imageName = getImageName(catPak, imgName);
        System.out.println("imagePath " + imagePath);
        System.out.println("imageName " + imageName);
        if (!new File(imagePath).exists()) {
            if (!new File(imagePath).mkdirs()) {
                logEvent("ERREUR", "Impossible de creer " + imagePath);
            }
        }
        // convert byte array back to BufferedImage
        InputStream in = new ByteArrayInputStream(verso);
        BufferedImage bImageFromConvert = ImageIO.read(in);

        ImageIO.write(bImageFromConvert, "jpg", new File(imagePath + imageName + extension)); //"r.jpg"
        System.out.println("Fin writeImageVerso");
    }

    private void linkImageIntoDB(DataBase db, CatClass catClass) throws SQLException {

        if (catClass.getCodeValeur().equals("30") || catClass.getCodeValeur().equals("33")) {
            Cheques aCheque = new Cheques();
            aCheque.setType_Cheque(catClass.getCodeValeur());
            aCheque.setNumerocheque(catClass.getNumeroValeur().substring(4)); //numeroValeur.substring(4)

            aCheque.setBanque(catClass.getCompteTire().substring(0, 3));  //le tiré ==> UBA GUINEE
            aCheque.setAgence(catClass.getCompteTire().substring(3, 6)); //le tiré ==> UBA GUINEE
            aCheque.setNumerocompte(catClass.getCompteTire().substring(6, 16)); //le tiré ==> UBA GUINEE
            aCheque.setRibcompte(catClass.getCompteTire().substring(16, 18));
            //    imageIndex.getCleRibDeb();
            aCheque.setBanqueremettant(catClass.getBanqueRemettante()); //le remettant ==> Autre Banque
            aCheque.setAgenceremettant(catClass.getCompteBeneficiaire().substring(3, 6)); //le remettant ==> Autre Banque
            aCheque.setCompteremettant(catClass.getCompteBeneficiaire().substring(6, 16));

            aCheque.setMontantcheque(String.valueOf(Long.parseLong(catClass.getMontant())));
            //imageIndex.getCodeCmc7();
            if (imagePath != null && imageName != null) {

                aCheque.setPathimage(imagePath);
                aCheque.setFichierimage(imageName);
            }

            //MAJ ou INSERTION
            System.out.println("CatPakAcpAchRetourReader requete ");
            String sql = "SELECT * FROM CHEQUES WHERE "
                    + " TRIM(BANQUE) ='" + aCheque.getBanque().trim() + "'"
                    + " AND TRIM(AGENCE) ='" + aCheque.getAgence().trim() + "'"
                    + " AND TRIM(NUMEROCOMPTE) ='" + aCheque.getNumerocompte().trim() + "'"
                    + " AND TRIM(NUMEROCHEQUE) ='" + aCheque.getNumerocheque().trim() + "'"
                    + " AND TRIM(BANQUEREMETTANT) ='" + aCheque.getBanqueremettant().trim() + "'"
                    + " AND TRIM(COMPTEREMETTANT) ='" + aCheque.getCompteremettant().trim() + "'"
                    + " AND TRIM(AGENCEREMETTANT) ='" + aCheque.getAgenceremettant().trim() + "'"
                    + " AND TRIM(MONTANTCHEQUE) ='" + aCheque.getMontantcheque().trim() + "'";
            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

            if (cheques != null && cheques.length > 0) {
                System.out.println("Le cheque existe deja Mise a jour etat recncilié");
                // aCheque.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));

                sql = "UPDATE CHEQUES SET PATHIMAGE='" + imagePath + "',FICHIERIMAGE='" + imageName + "'  "
                        + ", ETAT=" + Utility.getParam("CETAOPERETREC") + " " //CETAOPERETREC 152
                        + " WHERE IDCHEQUE =" + cheques[0].getIdcheque();
                db.executeUpdate(sql);
                // db.updateRowByObjectByQuery(aCheque, "CHEQUES", sql);

            } else {
                System.out.println("Le cheque n'existe pas en BD");
                aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                //aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERET")));
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
    @SuppressWarnings("SleepWhileInLoop")
    public File treatFile(File aFile, Repertoires repertoire) {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        List<File> files = new ArrayList<>();
        System.setProperty("file.encoding", "ISO-8859-1");
        //01-GN-015-11062018-094531-33-21-324
        String dateCopy = Utility.convertDateToString(new java.util.Date(), "ddMMyyyy");

        System.out.println("dateCopy " + dateCopy);
        String substring = aFile.getName().substring(10, 18);
        System.out.println("substring" + substring);

        try {
//            String date = Utility.getParam("DATECOPY");
//            if (substring.equals(dateCopy)) {
            Path path = Paths.get(aFile.toURI());
            // Load as binary:
            byte[] bytes = Files.readAllBytes(path);
            String asText = new String(bytes, StandardCharsets.ISO_8859_1);

            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            setFile(aFile);
            //   System.out.println("aFile getAbsolutePath " + aFile.getAbsolutePath() + " afile" + aFile.getName());
            //   BufferedInputStream is = openFile(aFile);
            System.out.println("asText length " + asText.length());
            String[] splitToNChar = splitToNChar(asText, 300);
            System.out.println("splitToNChar length" + splitToNChar.length);
            if (splitToNChar.length > 0) {
                for (int i = 0; i < splitToNChar.length; i++) {
                    String ligneCheque = splitToNChar[i];
                    System.out.println("ligneCheque length: " + ligneCheque.length());
                    // for (String ligneCheque : splitToNChar) {
                    //02+12+18+18+01+02+03+03+08+03+15+01+02+02+02+22+09+10+09+10+16+128
                    //Une ligne de cheques 
                    setCurrentLine(ligneCheque);
                    String codeValeur = getChamp(2); //Code valeur
                    String numeroValeur = getChamp(12); //Numero Valeur Numero Cheque
                    String compteTire = getChamp(18); //compte Tire
                    String compteBeneficiaire = getChamp(18); //compte Beneficiaire
                    String natureValeur = getChamp(1); //nature Valeur
                    String codePays = getChamp(2); //Code Pays
                    String banqueRemettante = getChamp(3); //banque Remettante
                    String agenceRemettante = getChamp(3); //agence Remettante
                    String dateDeScenarisation = getChamp(8); //Date de scénarisation
                    String devise = getChamp(3); //devise
                    String montant = getChamp(15); //montant
                    String codeCorrectionMICR = getChamp(1); //code Correction MICR
                    String codeTransaction = getChamp(2); //code Transaction
                    String codeEtat = getChamp(2); //Code Etat
                    String clefControle = getChamp(2); //clef Controle
                    getChamp(26); //filler 22 dans la doc mais je pense 26
                    String longueurRecto = getChamp(9); //longueur Recto
                    String debutLongueurRecto = getChamp(10); //debut Longueur Recto   
                    String longueurVerso = getChamp(9); //longueur Verso
                    String debutLongueurVerso = getChamp(10); //debut Longueur Verso
                    getChamp(16); //reference Clef
                    getChamp(128);  //Signature

                    CatClass imageCheque = new CatClass();
                    imageCheque.setAgenceRemettante(agenceRemettante);
                    imageCheque.setBanqueRemettante(banqueRemettante);
                    imageCheque.setCodeCorrection(codeCorrectionMICR);
                    imageCheque.setCodePays(codePays);
                    imageCheque.setCodeValeur(codeValeur);
                    imageCheque.setCompteBeneficiaire(compteBeneficiaire);
                    imageCheque.setCompteTire(compteTire);
                    imageCheque.setDateCapture(dateDeScenarisation);
                    imageCheque.setLongueurRecto(longueurRecto);
                    imageCheque.setLongueurVerso(longueurVerso);
                    imageCheque.setNumeroValeur(numeroValeur);
                    imageCheque.setMontant(montant);
                    imageCheque.setPositionDebutRecto(debutLongueurRecto);
                    imageCheque.setPositionDebutVerso(debutLongueurVerso);
                    System.out.println("imageCheque" + imageCheque);
                    //Aller chercher le fichier .PAK et composer l'image
                    String absolutePath = aFile.getAbsolutePath();
                    int lastIndexOf = aFile.getAbsolutePath().lastIndexOf(".");

                    String fileWithoutExtension = absolutePath.substring(0, lastIndexOf);
                    String pakFile = fileWithoutExtension + ".PAK";
                    //Ecrire l'image recto
                    //RECTO
                    byte[] rectoImage = readFromFile(pakFile, new Integer(imageCheque.getPositionDebutRecto()), new Integer(imageCheque.getLongueurRecto()));
                    String imgName = Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss");
                    //ECRIRE L'IMAGE RECTO
                    writeImage(pakFile, rectoImage, imageCheque, "f.jpg", imgName);
                    System.out.println("rectoImage" + rectoImage.length);

                    //VERSO
                    byte[] versoImage = readFromFile(pakFile, new Integer(imageCheque.getPositionDebutVerso()), new Integer(imageCheque.getLongueurVerso()));
                    System.out.println("versoImage" + versoImage.length);
                    //ECRIRE L'IMAGE RECTO
                    writeImage(pakFile, versoImage, imageCheque, "r.jpg", imgName);
                    linkImageIntoDB(db, imageCheque);
                    //Renommer le fichier .PAK
                    System.out.println("PAK FILE a renommer " + pakFile);
                    if (i + 1 == splitToNChar.length) {
                        File file = new File(pakFile + "#CatPakAcpAchRetourReader#TERMINE");
                        File treatFile = new File(pakFile);
                        if (treatFile.exists()) {
                            System.out.println("file exist treatFile");
                            System.out.println("file" + file);
                            FileUtils.moveFile(treatFile, new File(treatFile + "#CatPakAcpAchRetourReader#TERMINE"));

                        }
                        File rcpToBeTreated = new File(fileWithoutExtension + ".RCP");
                        System.out.println("rcpToBeTreated " + rcpToBeTreated);
                        while (!rcpToBeTreated.exists()) {
                            System.out.println("Le fichier RCP n'existe pas encore sur le disk");
                            Thread.sleep(2000);

                        }
                        System.out.println("Le fichier RCP 30-21 existe sur le disk");

                        if (rcpToBeTreated.exists()) {
                            RCPChequeRetourReader rcpReader = new RCPChequeRetourReader();
                            repertoire.setChemin(rcpToBeTreated.getParent());
                            System.out.println("rcpToBeTreated.getParent() : " + rcpToBeTreated.getParent());
                            repertoire.setExtension("RCP");
                            repertoire.setTache("clearing.action.readers.bfi.RCPChequeRetourReader");
                            //fichier
                            File rcpReaderTreater = rcpReader.treatFile(rcpToBeTreated, repertoire);
                            System.out.println("RCP FILE a renommer " + rcpReaderTreater);
                            FileUtils.copyFile(rcpToBeTreated, new File(rcpReaderTreater + "#RCPChequeRetourReader#TERMINE"));
                            files.add(rcpToBeTreated);
                            //  FileUtils.forceDelete(rcpToBeTreated);

                        }

                        //30-21-324.RCP
                        //treatFile a renommer    C:\Clearing\Interfaces\Entree\01-GN-015-12102018-112019-30-21-324.PAK|#]
                        //
                    }

                }

//                }
                db.close();

            }

        } catch (Exception ex) {
            Logger.getLogger(CatPakAcpAchRetourReader.class.getName()).log(Level.INFO, null, ex);
            db.close();
        }
        for (File file : files) {
            if (file.exists()) {
                if (!file.delete()) {
                    try {
                        FileUtils.forceDelete(file);
                    } catch (IOException ex) {
                        Logger.getLogger(CatPakAcpAchRetourReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        }
        System.out.println("aFile" + aFile);
        return aFile;
    }

    public CatPakAcpAchRetourReader() {
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

    public String getImageName(CatClass catPak, String imgName) {

        String image = catPak.getCompteBeneficiaire() + "_" + imgName;
        //  String image = catPak.getCompteBeneficiaire() + "_" + Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss");
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
            Logger.getLogger(CatPakAcpAchRetourReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CatPakAcpAchRetourReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }

            } catch (IOException ex) {
                Logger.getLogger(CatPakAcpAchRetourReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
