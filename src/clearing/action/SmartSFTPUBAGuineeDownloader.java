/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action;

/**
 *
 * @author Patrick Augou
 */
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.io.OutputStream;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.patware.action.file.Executable;
import org.patware.utils.Utility;

public class SmartSFTPUBAGuineeDownloader extends Executable {

    public SmartSFTPUBAGuineeDownloader() {

    }

    @Override
    public void execute() throws Exception {
        super.execute();

        Utility.clearParamsCache();
        String SFTPHOST = Utility.getParam("ADTSFTPSERVER"); //                                     
        int SFTPPORT = Integer.parseInt(Utility.getParam("ADTSFTPPORT")); //ADTSFTPPASSWORD                                   
        String SFTPUSER = Utility.getParam("ADTSFTPUSER");
        String SFTPPASS = Utility.getParamOfType("ADTSFTPPASSWORD", "CODE_CRYPTED"); //ADTSFTPPASSWORD                                   
        //            
        byte[] decoded;
        if (SFTPPASS != null) {
            decoded = Base64.decodeBase64(Utility.getParam("ADTSFTPPASSWORD"));
            SFTPPASS = new String(decoded);
        } else {
            SFTPPASS = Utility.getParam("ADTSFTPPASSWORD");
        }
        downloadRemoteFiles(SFTPUSER, SFTPPASS, SFTPHOST, SFTPPORT);
        Thread.sleep(Long.parseLong(Utility.getParam("SFTPHEARTBEAT")));

    }

    public void downloadRemoteFiles(String user, String password, String addr, int port) {

        ChannelSftp channelSftp = null;
        Channel channel = null;
        Session session = null;

        try {
            //    JSch.setLogger(new JSCHLogger());
            Properties config = new Properties();
            config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
            config.put("StrictHostKeyChecking", "no");

            session = getSession(user, password, addr, port);

            if (session != null) {
                session.setConfig(config);
                channel = session.openChannel("sftp");
                channel.connect();
                channelSftp = (ChannelSftp) channel;
                System.out.println("SFTPWORKINGDIR " + getRepertoire().getChemin());
                channelSftp.cd(getRepertoire().getChemin());
                Vector<ChannelSftp.LsEntry> list = (Vector<LsEntry>) channelSftp.ls("*." + getRepertoire().getExtension()); //sftpChannel.lpwd()
                for (ChannelSftp.LsEntry entry : list) {
                    System.out.println(entry.getFilename());
                    byte[] buffer = new byte[1024];

                    BufferedInputStream bis = new BufferedInputStream(channelSftp.get(entry.getFilename()));
                    File newFile = new File(getRepertoire().getPartenaire() + entry.getFilename());
                    OutputStream os = new FileOutputStream(newFile);
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    int readCount;

                    while ((readCount = bis.read(buffer)) > 0) {

                        bos.write(buffer, 0, readCount);
                    }
                    bis.close();
                    bos.close();

                    System.out.println("Writing File: " + entry.getFilename());
                    if (entry.getFilename().toUpperCase().endsWith(".PAK")) {
                        buffer = new byte[1024];
                        String catFile = entry.getFilename().replace("PAK", "CAT");

                        bis = new BufferedInputStream(channelSftp.get(catFile));
                        newFile = new File(getRepertoire().getPartenaire() + catFile);
                        os = new FileOutputStream(newFile);
                        bos = new BufferedOutputStream(os);

                        while ((readCount = bis.read(buffer)) > 0) {
                            System.out.println("Writing File Cat: " + catFile);
                            bos.write(buffer, 0, readCount);
                        }
                        bis.close();
                        bos.close();

                    }

                }
                for (ChannelSftp.LsEntry entry : list) {
                    System.out.println(entry.getFilename());
                    channelSftp.rm(entry.getFilename());
                    if (entry.getFilename().toUpperCase().endsWith(".PAK")) {
                        String catFile = entry.getFilename().replace("PAK", "CAT");
                        channelSftp.rm(catFile);
                    }
                }

            }

        } catch (Exception ex) {
            Logger.getLogger(SmartSFTPUBAGuineeDownloader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            if (channelSftp != null) {
                channelSftp.exit();
                System.out.println("sftp Channel exited.");
            }
            if (channel != null) {
                channel.disconnect();
                System.out.println("Channel disconnected.");
            }
            if (session != null) {
                session.disconnect();
                System.out.println("Host Session disconnected.");
            }
        }

    }

    @SuppressWarnings("empty-statement")
    private Session getSession(String user, String password, String addr, int port) {
        Session session = null;

        System.out.println("preparing the host information for sftp.");
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, addr, port);
            session.setPassword(password);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            System.out.println("Host connected.");
        } catch (JSchException e) {
            e.printStackTrace();;
        }
        return session;
    }

}
