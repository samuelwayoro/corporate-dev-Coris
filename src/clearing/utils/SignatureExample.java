/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.Security;
import java.security.Signature;
import java.util.Arrays;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author BOUIKS
 */
public class SignatureExample {

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        byte[] rImageBytes;
        byte[] vImageBytes;
        String message = "hello world";
        File privateKey = new File("KeyPair/key.pem"); //new File("KeyPair/text.txt")
        KeyPair keyPair = readKeyPair(privateKey, "1fqsrv1111g4s".toCharArray()); //1fqsrv1111g4s

        File aRectoFile = null;
        File aVersoFile = null;
        aRectoFile = new File("c:\\image\\aller\\admin\\20180531\\CHEQUES\\002\\842\\" + "00034410013300232544_20180531224643" + "f.jpg");
        aVersoFile = new File("c:\\image\\aller\\admin\\20180531\\CHEQUES\\002\\842\\" + "00034410013300232544_20180531224643" + "r.jpg");
        rImageBytes = new byte[(int) aRectoFile.length()];
        vImageBytes = new byte[(int) aVersoFile.length()];
        Signature signature = Signature.getInstance("SHA256WithRSAEncryption");
        System.out.println("keyPair.getPrivate()" + keyPair.getPrivate().toString());
        signature.initSign(keyPair.getPrivate());
        signature.update(rImageBytes);
        signature.update(vImageBytes);
        byte[] signatureBytes = signature.sign();
        System.out.println(new String(signatureBytes, "UTF-8"));
        writeToFile("MyData/SignedDataCAT.txt", signatureBytes);
//        System.out.println("signatureBytes" + new String(Hex.encode(signatureBytes)));

        Signature verifier = Signature.getInstance("SHA256WithRSAEncryption");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(message.getBytes());
        if (verifier.verify(signatureBytes)) {
            System.out.println("Signature is valid");
        } else {
            System.out.println("Signature is invalid");
        }
    }
    //Method to write the List of byte[] to a file

    public static void writeToFile(String filename, byte[] signatureBytes) throws FileNotFoundException, IOException {
        File f = new File(filename);
        f.getParentFile().mkdirs();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
        out.write(signatureBytes);
//        out.writeObject(signatureBytes);
        out.close();
        System.out.println("Your file is ready.");
    }

    public static KeyPair readKeyPair(File privateKey, char[] keyPassword) throws IOException {
        FileReader fileReader = new FileReader(privateKey);
        PEMReader r = new PEMReader(fileReader, new DefaultPasswordFinder(keyPassword));
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

}
