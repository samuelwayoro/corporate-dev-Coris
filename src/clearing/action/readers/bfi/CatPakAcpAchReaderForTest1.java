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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.Signature;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
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
public class CatPakAcpAchReaderForTest1 extends BinFileReader {

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

    public CatPakAcpAchReaderForTest1() {
        setCopyOriginalFile(true);

    }

    public static KeyPair readKeyPair(File privateKey, char[] keyPassword) throws IOException {
        FileReader fileReader = new FileReader(privateKey);
        PEMReader r = new PEMReader(fileReader, new CatPakAcpAchReaderForTest1.DefaultPasswordFinder(keyPassword));
        try {
            return (KeyPair) r.readObject();
        } catch (IOException ex) {
            throw new IOException("The private key could not be decrypted", ex);
        } finally {
            r.close();
            fileReader.close();
        }
    }

    public static class DefaultPasswordFinder implements PasswordFinder {

        private final char[] password;

        private DefaultPasswordFinder(char[] password) {
            this.password = password;
        }

        @Override
        public char[] getPassword() {
            return Arrays.copyOf(password, password.length);
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {

        File catFile = new File("C:\\CLEARING\\interfaces\\entree\\146395720301000001001001437297023242807062018124928.CAT"); //
//      
        CatPakAcpAchReaderForTest1 readerCat = new CatPakAcpAchReaderForTest1();
        Repertoires repertoire = new Repertoires();
        repertoire.setChemin("C:\\CLEARING\\interfaces\\entree\\CAT-PAK-ENV-UBA");
        repertoire.setExtension("CAT");
        repertoire.setTache("clearing.action.readers.bfi.CatPakAcpAchReaderForTest");

        File treatFile = readerCat.treatFile(catFile, repertoire);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        DataBase db = new DataBase(JDBCXmlReader.getDriver());

        Security.addProvider(new BouncyCastleProvider());
        File privateKey = new File("C:\\SSL\\key.pem"); //new File("KeyPair/text.txt")  //C:\Users\BOUIKS\Documents\Patrick-DEV-WBC\Webclearing\KeyPair\key.pem
        KeyPair keyPair = readKeyPair(privateKey, "1fqsrv1111g4s".toCharArray()); //1fqsrv1111g4s
//        Signature signature = Signature.getInstance("SHA256WithRSAEncryption");  //SHA256WithRSAEncryption SHA1withRSA  SHA256withRSA

        Signature signature = Signature.getInstance("SHA256withRSA"); //SHA1withRSA/ISO9796-2 SHA1withRSA

        signature.initSign(keyPair.getPrivate());

        try {
            Path path = Paths.get(aFile.toURI());
            // Load as binary:
            byte[] bytes = Files.readAllBytes(path);
            String asText = new String(bytes, StandardCharsets.ISO_8859_1);//, StandardCharsets.UTF_8 ISO-8859-1
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            setFile(aFile);

            String[] splitToNChar = splitToNChar(asText, 300);

            if (splitToNChar.length > 0) {

                for (int i = 0; i < splitToNChar.length; i++) {
                    String ligneCheque = splitToNChar[i];

                    // for (String ligneCheque : splitToNChar) {
                    //02+12+18+18+01+02+03+03+08+03+15+01+02+02+02+26+09+10+09+10+16+128
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
                    String filler = getChamp(26); //filler 22 dans la doc mais je pense 26
                    String longueurRecto = getChamp(9); //longueur Recto
                    String debutLongueurRecto = getChamp(10); //debut Longueur Recto   
                    String longueurVerso = getChamp(9); //longueur Verso
                    String debutLongueurVerso = getChamp(10); //debut Longueur Verso
                    String referenceClef = getChamp(16); //reference Clef
                    String signatureCat = getChamp(128);  //Signature
                    StringBuilder forTest = new StringBuilder();

                    forTest.append(codeValeur);
                    forTest.append(numeroValeur);
                    forTest.append(compteTire);
                    forTest.append(compteBeneficiaire);
                    forTest.append(natureValeur);
                    forTest.append(codePays);
                    forTest.append(banqueRemettante);
                    forTest.append(agenceRemettante);
                    forTest.append(dateDeScenarisation);
                    forTest.append(devise);
                    forTest.append(montant); //montant Integer.parseInt(montant)
                    forTest.append(codeCorrectionMICR);
                    forTest.append(codeTransaction);
                    forTest.append(codeEtat);
                    forTest.append(clefControle);
                    forTest.append(filler);
//                    System.out.println("filler" + filler);
//                    forTest.append(longueurRecto);
//                    forTest.append(debutLongueurRecto);
//                    forTest.append(longueurVerso);
//                    forTest.append(debutLongueurVerso);
//                    forTest.append(referenceClef);
//                    String ligneCMC7 = numeroValeur.substring(4) + "03" + compteTire.substring(0, 3) + codeEtat + compteTire.substring(3, 6) + compteTire.substring(6, 16) + compteTire.substring(16, 18) + codeTransaction + "324" + clefControle;
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
                    //    System.out.println("imageCheque" + imageCheque);

                    //Aller chercher le fichier .PAK et composer l'image
                    String absolutePath = aFile.getAbsolutePath();
                    int lastIndexOf = aFile.getAbsolutePath().lastIndexOf(".");

                    String fileWithoutExtension = absolutePath.substring(0, lastIndexOf);
                    String pakFile = fileWithoutExtension + ".PAK";
                    //Ecrire l'image recto
                    //RECTO
                    byte[] rectoImage = readFromFile(pakFile, new Integer(imageCheque.getPositionDebutRecto()), new Integer(imageCheque.getLongueurRecto()));
                    //ECRIRE L'IMAGE RECTO

                    //VERSO
                    byte[] versoImage = readFromFile(pakFile, new Integer(imageCheque.getPositionDebutVerso()), new Integer(imageCheque.getLongueurVerso()));

                    //ECRIRE L'IMAGE RECTO
                    //Signatures Test 
                    //Données de la valeur
//                    signature.update(forTest.toString().trim().getBytes());
                    //Images
                    signature.update(versoImage);
                    signature.update(forTest.toString().getBytes());
                    signature.update(rectoImage);

//////
                    byte[] signatureBytes = signature.sign();

//
                    writeToFile("C:\\SSL\\MyData\\SignedEssaiCAT_" + i + ".txt", signatureBytes);
                    writeToFile("C:\\SSL\\MyData\\SignedOrigCAT_" + i + ".txt", signatureCat.getBytes());
//////                    linkImageIntoDB(db, imageCheque);
                    //Renommer le fichier .PAK
//////                    System.out.println("treatFile a renommer " + pakFile);
//////                    if (i + 1 == splitToNChar.length) {
//////                        File file = new File(pakFile + "#CatPakReaderAcpAch#TERMINE");
//////                        File treatFile = new File(pakFile);
//////                        if (treatFile.exists()) {
//////                            System.out.println("file exist treatFile");
//////                            System.out.println("file" + file);
//////                            FileUtils.moveFile(treatFile, new File(treatFile + "#CatPakReaderAcpAch#TERMINE"));
//////
//////                        }
//////                    }
                }
            }

            db.close();

        } catch (Exception ex) {
            Logger.getLogger(CatPakAcpAchReaderForTest1.class.getName()).log(Level.INFO, null, ex);
            db.close();
        }
        return aFile;
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

        String image = catPak.getCompteBeneficiaire() + "_" + Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss");
//        System.out.println("getImageName " + image);
        return image;
    }

    public static void writeToFile(String filename, byte[] signatureBytes) throws FileNotFoundException, IOException {
        File f = new File(filename);
        f.getParentFile().mkdirs();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
//        out.writeObject(signatureBytes);

        out.write(signatureBytes);
        out.close();

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
            Logger.getLogger(CatPakAcpAchReaderForTest1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CatPakAcpAchReaderForTest1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(CatPakAcpAchReaderForTest1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
