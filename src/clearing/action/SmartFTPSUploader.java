/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action;

/**
 *
 * @author Patrick Augou
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.utils.Utility;

public class SmartFTPSUploader extends FlatFileReader {

    FTPSClient ftp = new FTPSClient(true);

    public SmartFTPSUploader() {
    }

    public SmartFTPSUploader(String host, String user, String pwd) throws Exception {
        int SFTPPORT = Integer.parseInt(Utility.getParam("SFTPPORT"));
        ftp.setConnectTimeout(5000);
        ftp.connect(host, SFTPPORT);
        ftp.login(user, pwd);
        ftp.enterLocalPassiveMode();
        //  ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        //test for local commit

        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(user, pwd);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        //ftp.enterLocalPassiveMode();
    }

    public void uploadFile(String localFileFullName, String fileName, String hostDir)
            throws Exception {
        InputStream input = new FileInputStream(new File(localFileFullName));
        ftp.storeFile(hostDir + fileName, input);
        System.out.println("hostDir + fileName =" + hostDir + fileName);
        input.close();
    }

    public void disconnect() {
        if (this.ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
            } catch (IOException f) {
                // do nothing as file is already saved to server
            }
        }
    }

    @Override
    public void setRepertoire(Repertoires repertoire) {
        setWaitFolder(Utility.getParam("TempCFT"));
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) {
        try {
            String SFTPHOST = Utility.getParam("SFTPSERVER");

            String SFTPUSER = Utility.getParam("SFTPUSER");
            String SFTPPASS = Utility.getParam("SFTPPASSWORD");

            System.out.println("Traitement de " + aFile.getAbsolutePath());
            SmartFTPSUploader ftpUploader = new SmartFTPSUploader(SFTPHOST, SFTPUSER, SFTPPASS);

            //FTP server path is relative. So if FTP account HOME directory is "/home/pankaj/public_html/" and you need to upload
            // files to "/home/pankaj/public_html/wp-content/uploads/image2/", you should pass directory parameter as "/wp-content/uploads/image2/"
            ftpUploader.uploadFile(aFile.getAbsolutePath(), aFile.getName(), repertoire.getPartenaire());
            System.out.println(aFile.getAbsolutePath() + " " + aFile.getName() + " " + repertoire.getPartenaire());
            ftpUploader.disconnect();
            System.out.println("Traitement terminé de " + aFile.getAbsolutePath());
            String archFolder = Utility.getParam("SIB_IN_FOLDER") + File.separator + "archives" + File.separator + Utility.convertDateToString(new Date(), "yyyyMMddHHmmss");
            File waitFolder = new File(archFolder);
            if (Utility.createFolderIfItsnt(waitFolder, null)) {

                File archf = new File(archFolder + File.separator + aFile.getName());
                FileUtils.copyFile(aFile, archf);
                if (archf.exists()) {
                    if (!aFile.delete()) {
                        FileUtils.forceDelete(aFile);
                    }
                }
            }
            return null;
            /*           }
            else {
            Utility.getCurrentTray().showTrayErrorBalloon(ResLoader.getMessages("TitreApp"),"Impossible de renommer en fichier d'envoi");
            logEvent("ERROR", "Impossible de renommer en fichier d'envoi");
            return null;
            }*/
        } catch (Exception ex) {
            Logger.getLogger(SmartFTPSUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

}
