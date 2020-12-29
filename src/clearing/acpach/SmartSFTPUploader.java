/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.acpach;

/**
 *
 * @author Patrick Augou
 */
import clearing.model.CMPUtility;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.utils.Utility;

public class SmartSFTPUploader extends FlatFileReader {

    public SmartSFTPUploader() {
        setStoreOriginalName(true);
        //  setHasNormalExtension(false);
        setExtensionType(NORMAL_EXT);
        setCheckIfAlreadyTreated(true);

    }

    public void send(String fileName, String adresseIp, String cheminDistant) {
        try {
            String SFTPHOST = Utility.getParam("ADTSFTPSERVER");
            int SFTPPORT = Integer.parseInt(Utility.getParam("ADTSFTPPORT"));
            String SFTPUSER = Utility.getParam("ADTSFTPUSER");
            byte[] decoded;
            Utility.clearParamsCache();
            String SFTPPASS = Utility.getParamOfType("ADTSFTPPASSWORD", "CODE_CRYPTED");
            //           

            if (SFTPPASS != null) {
                decoded = Base64.decodeBase64(Utility.getParam("ADTSFTPPASSWORD"));
                SFTPPASS = new String(decoded);

            } else {
                SFTPPASS = Utility.getParam("ADTSFTPPASSWORD");
            }
            String SFTPWORKINGDIR = cheminDistant;
            Session session = null;
            Channel channel = null;
            ChannelSftp channelSftp = null;
            System.out.println("preparing the host information for sftp.");
            try {
                JSch jsch = new JSch(); //classe permettant de se connecter en ssh a un serveur 
                session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
                session.setPassword(SFTPPASS);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();
                System.out.println("Host connected.");
                channel = session.openChannel("sftp");
                channel.connect();
                System.out.println("sftp channel opened and connected.");
                channelSftp = (ChannelSftp) channel;
                channelSftp.cd(SFTPWORKINGDIR);
                File f = new File(fileName);
                FileInputStream fInputStream = new FileInputStream(f);
                channelSftp.put(fInputStream, f.getName());
                fInputStream.close();
                System.out.println("File transfered successfully to host.");
            } catch (Exception ex) {
                System.out.println("Exception found while tranfer the response.");
                ex.printStackTrace();
            } finally {
                channelSftp.exit();
                System.out.println("sftp Channel exited.");
                channel.disconnect();
                System.out.println("Channel disconnected.");
                session.disconnect();
                System.out.println("Host Session disconnected.");
            }
            String archFolder = Utility.getParam("SIB_IN_FOLDER") + File.separator + "archives" + File.separator + CMPUtility.getDate();
            File waitFolder = new File(archFolder);
            if (Utility.createFolderIfItsnt(waitFolder, null)) {
                File f = new File(fileName);
                File archf = new File(archFolder + File.separator + f.getName());
                FileUtils.copyFile(f, archf);
                if (archf.exists()) {
                    if (!f.delete()) {
                        FileUtils.forceDelete(f);
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(SmartSFTPUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    @Override
//    public void setRepertoire(Repertoires repertoire) {
//        System.out.println("repertoire "+repertoire.getPartenaire());
//     //   setWaitFolder(Utility.getParam("TempCFT"));
//    }
    @Override
    public File treatFile(File aFile, Repertoires repertoire) {
        System.out.println("aFile" + aFile);
        try {
            setFile(aFile);

            String SFTPSERVER = Utility.getParam("FTPSERVER");
            send(aFile.getAbsolutePath(), SFTPSERVER, repertoire.getPartenaire());

            System.out.println("Traitement terminé de " + aFile.getAbsolutePath());
            return null;

        } catch (Exception ex) {
            Logger.getLogger(SmartSFTPUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }
}
