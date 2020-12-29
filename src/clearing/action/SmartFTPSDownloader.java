/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPSClient;
import org.patware.action.file.Executable;
import org.patware.utils.Utility;

/**
 *
 * @author BOUIKS
 */
public class SmartFTPSDownloader extends Executable {

    @Override
    public void execute() throws Exception {
        super.execute();
        String SFTPHOST = Utility.getParam("SFTPSERVER");
        int SFTPPORT = Integer.parseInt(Utility.getParam("SFTPPORT"));
        String SFTPUSER = Utility.getParam("SFTPUSER");
        String SFTPPASS = Utility.getParam("SFTPPASSWORD");
        downloadRemoteFiles(SFTPUSER, SFTPPASS, SFTPHOST, SFTPPORT);
        Thread.sleep(Long.parseLong(Utility.getParam("SFTPHEARTBEAT")));
    }

    public void downloadRemoteFiles(String user, String password, String addr, int port) {
        FTPSClient ftpClient = new FTPSClient(true);
        ftpClient.setConnectTimeout(5000);
        try {
            ftpClient.connect(addr, port);
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
            String dirToSearch = getRepertoire().getChemin(); //getRepertoire().getChemin()
            FTPFileFilter filter = new FTPFileFilter() {
                @Override
                public boolean accept(FTPFile ftpFile) {
                    return (ftpFile.isFile() && ftpFile.getName().contains(getRepertoire().getExtension().trim()));
                }
            };

            FTPFile[] result = ftpClient.listFiles(dirToSearch, filter);

            if (result != null && result.length > 0) {

                for (FTPFile aFile : result) {
                    System.out.println(aFile.getName());
                    // APPROACH #1: using retrieveFile(String, OutputStream)
                    String remoteFile = getRepertoire().getChemin() + "/" + aFile.getName();
                    System.out.println("remoteFile " + remoteFile);
                    File downloadFile = new File(getRepertoire().getPartenaire() + File.separator + aFile.getName());
                    boolean success;
                    try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile))) {
                        success = ftpClient.retrieveFile(remoteFile, outputStream);
                    }

                    if (success) {
                        System.out.println("File " + remoteFile + " has been successfully downloaded.");

                    }
                    System.out.println("remoteFile to be deleted"+remoteFile);
                    boolean deleteFile = ftpClient.deleteFile(remoteFile);System.out.println("deleteFile "+deleteFile);
                    boolean exist = ftpClient.deleteFile(remoteFile);
                    if (exist) {
                        System.out.println("File " + remoteFile + " has been successfully deleted.");
                    }
                }

              

            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

}
