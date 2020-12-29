/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.bfi;

import clearing.table.Cheques;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.patware.action.file.FlatFileWriter;
import org.patware.action.impl.ExecutableImpl;

import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CatAcpAchWriterSignature extends FlatFileWriter {

    private Cheques[] cheques = null;
    private String fileName;

    public CatAcpAchWriterSignature() {
//        setDescription("Envoi des Cheques Aller Nationaux vers ACP/ACH");
        setDescription("Envoi CAT vers ADT");
        System.setProperty("file.encoding", "ISO-8859-1");
    }
//Method to retrieve the Public Key from a file

    public PublicKey getPublic(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private boolean verifySignature(byte[] data, byte[] signature, String keyFile) throws Exception {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(getPublic(keyFile));
        sig.update(data);

        return sig.verify(signature);
    }

    public CatAcpAchWriterSignature(Cheques[] cheques, String fileName) {
        this.cheques = cheques;
        this.fileName = fileName;
    }

    public static void writeToFile(String filename, byte[] signatureBytes) throws FileNotFoundException, IOException {
        File f = new File(filename);
        f.getParentFile().mkdirs();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(signatureBytes);
//        out.write(signatureBytes);
        out.close();
        System.out.println("Your file is ready.");
    }

    public static KeyPair readKeyPair(File privateKey, char[] keyPassword) throws IOException {
        FileReader fileReader = new FileReader(privateKey);
        PEMReader r = new PEMReader(fileReader, new CatAcpAchWriterSignature.DefaultPasswordFinder(keyPassword));
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

    private static byte[] readBytesFromFile(File filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            bytesArray = new byte[(int) filePath.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(filePath);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        ///////
        Security.addProvider(new BouncyCastleProvider());
        File privateKey = new File("C:\\SSL\\key.pem"); //new File("KeyPair/text.txt")  //C:\Users\BOUIKS\Documents\Patrick-DEV-WBC\Webclearing\KeyPair\key.pem
        KeyPair keyPair = readKeyPair(privateKey, "1fqsrv1111g4s".toCharArray()); //1fqsrv1111g4s
        Signature signature = Signature.getInstance("SHA256WithRSAEncryption"); //SHA1withRSA SHA256WithRSAEncryption
        signature.initSign(keyPair.getPrivate());

        setOut(createFlatFile(fileName + ".CAT"));
        System.out.println("Cheques " + cheques.length + " Fichier" + fileName + ".CAT");
        Long positionRecto = 0l;
        Long positionVerso = 0l;
        File aRectoFile = null;
        File aVersoFile = null;

        for (Cheques cheque : cheques) {

            aRectoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "f.jpg");
            aVersoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "r.jpg");
            //imageIndex.setLongRecto(String.valueOf(aRectoFile.length()));
            StringBuilder secondLineBuilder = new StringBuilder();
            secondLineBuilder.append(cheque.getDateecheance().substring(0, 118));
            secondLineBuilder.append(Utility.bourrageGauche(String.valueOf(aRectoFile.length()), 9, "0")); //09
            secondLineBuilder.append(Utility.bourrageGauche("" + positionRecto, 10, "0")); //10	Debut Longueur recto .on commence a 0; on fait plus la taille de limage recto
            secondLineBuilder.append(Utility.bourrageGauche(String.valueOf(aVersoFile.length()), 9, "0")); //09	Longueur verso //Taille image Recto en int
            positionVerso = positionRecto + aRectoFile.length();
            secondLineBuilder.append(Utility.bourrageGauche("" + positionVerso, 10, "0")); //10
            secondLineBuilder.append(cheque.getDateecheance().substring(156, 300));
            System.out.println("cheque.getDateecheance().substring(156, 300) "+cheque.getDateecheance().substring(156, 300));
            System.out.println("cheque.getDateecheance() length "+cheque.getDateecheance().length());
            System.out.println("secondLineBuilder.toString() "+secondLineBuilder.toString().length());
            write(secondLineBuilder.toString());
            positionRecto = positionVerso + aVersoFile.length();

         
            
////////////////////            StringBuilder line = new StringBuilder();
////////////////////            line.append(Utility.bourrageDroite(cheque.getType_Cheque().trim(), 2, "0")); //02 Code valeur
////////////////////            line.append(Utility.bourrageGauche(cheque.getNumerocheque(), 12, "0")); //12	Numero Cheque valeur
////////////////////            line.append(Utility.bourrageGauche(cheque.getBanque(), 3, "0"))
////////////////////                    .append(Utility.bourrageGauche(cheque.getAgence(), 3, "0"))
////////////////////                    .append(Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0"))
////////////////////                    .append(Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanque(), 3, "0"), Utility.bourrageGauche(cheque.getAgence(), 3, "0"), Utility.bourrageGauche(cheque.getNumerocompte(), 10, "0"))); //18 	Compte tiré      
////////////////////            line.append(Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"))
////////////////////                    .append(Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0"))
////////////////////                    .append(Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0"))
////////////////////                    .append(Utility.computeCleRIBACPACH(Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0"), Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0"), Utility.bourrageGauche(cheque.getCompteremettant(), 10, "0"))); //18	Compte bénéficiaire
////////////////////            line.append(Integer.parseInt(cheque.getCalcul())); //01	Nature valeur
////////////////////            line.append("GN"); //02   GN = Guinea
////////////////////            line.append(Utility.bourrageGauche(cheque.getBanqueremettant(), 3, "0")); //03	Code banque remettante
////////////////////            line.append(Utility.bourrageGauche(cheque.getAgenceremettant(), 3, "0")); //03	Code agence remettante
////////////////////            line.append(Utility.convertDateToString(Utility.convertStringToDate(cheque.getDatesaisie(), "yyyy/MM/dd"), "ddMMyyyy")); //08	Date de scenarisation ddMMyyyy
////////////////////            line.append("324"); //03	270 (GM) /694 (SL) / 324 (GN)
////////////////////            line.append(Utility.bourrageGauche(cheque.getMontantcheque(), 15, "0")); //15	Montant
////////////////////            line.append("0"); //01	Code de correction de la ligne CMC7/MICR
////////////////////            line.append(cheque.getCalcul()); //02	code Transaction 
////////////////////            line.append(Utility.bourrageGauche(cheque.getGarde(), 2, "0")); //02	Code etat
////////////////////            line.append(Utility.bourrageGauche(cheque.getCodeVignetteUV(), 2, "0")); //02	Clef de contole ligne CMC7/MICR
////////////////////            line.append(createBlancs(26, " ")); //26	Filler 22 dans la spec mais 26 en realite
////////////////////
////////////////////            line.append(Utility.bourrageGauche(String.valueOf(aRectoFile.length()), 9, "0")); //09	Longueur recto //Taille image Recto en int
////////////////////            line.append(Utility.bourrageGauche("" + positionRecto, 10, "0")); //10	Debut Longueur recto .on commence a 0; on fait plus la taille de limage recto
////////////////////            line.append(Utility.bourrageGauche(String.valueOf(aVersoFile.length()), 9, "0")); //09	Longueur verso //Taille image Recto en int
////////////////////            positionVerso = positionRecto + aRectoFile.length();
////////////////////            line.append(Utility.bourrageGauche("" + positionVerso, 10, "0")); //10	Debut Longueur verso; on commence a 0+tailleimagerecto.
////////////////////            line.append("GN00015160420181"); //16	Reference de la clef // createBlancs(16, " ")
//////////////////////            line.append(createBlancs(128, " ")); //128	Signature
////////////////////            //Prochaines Positions d'images
////////////////////            //Enlever le \n a la fin du fichier
////////////////////
////////////////////            //Signatures 
//////////////////////            signature.update(line.toString().getBytes());
////////////////////////            signature.update(cheque.getBanque().getBytes());
////////////////////////            signature.update(cheque.getAgence().getBytes());
////////////////////////            signature.update(cheque.getNumerocompte().getBytes());
////////////////////////            signature.update(cheque.getNumerocheque().getBytes());
////////////////////////            signature.update(cheque.getBanqueremettant().getBytes());
////////////////////////            signature.update(cheque.getAgenceremettant().getBytes());
////////////////////////            signature.update(cheque.getCompteremettant().getBytes());
////////////////////////            signature.update(cheque.getMontantcheque().getBytes());
////////////////////            //Informations sur la valeur
//////////////////////            line.append(new String(signatureBytes, Charset.defaultCharset())); //128	Signature
////////////////////            write(line.toString());
////////////////////            positionRecto = positionVerso + aVersoFile.length();
        }

        closeFile();

        db.close();
    }

    public static void signatureFile(byte[] bytesToBeSigned) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        File privateKey = new File("KeyPair/key.pem"); //new File("KeyPair/text.txt")
        KeyPair keyPair = readKeyPair(privateKey, "1fqsrv1111g4s".toCharArray()); //1fqsrv1111g4s
        Signature signature = Signature.getInstance("SHA256WithRSAEncryption");
        signature.initSign(keyPair.getPrivate());
        signature.update(bytesToBeSigned);
        byte[] signatureBytes = signature.sign();
        System.out.println("Fin signature");
        writeToFile("C:\\SSL\\MyData\\SignedDataCAT.txt", signatureBytes);
    }

    public Cheques[] getCheques() {
        return cheques;
    }

    public void setCheques(Cheques[] cheques) {
        this.cheques = cheques;
    }

}
