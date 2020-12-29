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
import com.jcraft.jsch.Session;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.io.OutputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.patware.action.file.Executable;
import org.patware.utils.Utility;

public class SmartSFTPDownloader extends Executable {

    public SmartSFTPDownloader() {

    }

    @Override
    public void execute() throws Exception {
        super.execute();

        Utility.clearParamsCache();
        String SFTPHOST = Utility.getParam("SFTPHOST");
        int SFTPPORT = Integer.parseInt(Utility.getParam("SFTPPORT"));
        String SFTPUSER = Utility.getParam("SFTPUSER");
        String SFTPPASS = Utility.getParamOfType("SFTPPASSWORD", "CODE_CRYPTED");
        //            
        byte[] decoded;
        if (SFTPPASS != null) {
            decoded = Base64.decodeBase64(Utility.getParam("SFTPPASSWORD"));
            SFTPPASS = new String(decoded);
        } else {
            SFTPPASS = Utility.getParam("SFTPPASSWORD");
        }
        downloadRemoteFiles(SFTPUSER, SFTPPASS, SFTPHOST, SFTPPORT);
       Thread.sleep(Long.parseLong(Utility.getParam("SFTPHEARTBEAT")));

    }

    public void downloadRemoteFiles(String user, String password, String addr, int port) {

        ChannelSftp channelSftp = null;
        Channel channel = null;
        Session session = null;

        try {
            session = getSession(user, password, addr, port);
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            System.out.println("SFTPWORKINGDIR " + getRepertoire().getChemin());
            channelSftp.cd(getRepertoire().getChemin());
            Vector<ChannelSftp.LsEntry> list = (Vector<LsEntry>) channelSftp.ls("*."+getRepertoire().getExtension()); //sftpChannel.lpwd()
            for (ChannelSftp.LsEntry entry : list) {
                System.out.println(entry.getFilename());
                byte[] buffer = new byte[1024];
                BufferedInputStream bis = new BufferedInputStream(channelSftp.get(entry.getFilename()));
                File newFile = new File(getRepertoire().getPartenaire() + entry.getFilename());
                OutputStream os = new FileOutputStream(newFile);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                int readCount;
//System.out.println("Getting: " + theLine);
                while ((readCount = bis.read(buffer)) > 0) {
                    System.out.println("Writing: " + entry.getFilename());
                    bos.write(buffer, 0, readCount);
                }
                bis.close();
                bos.close();
            }
            for (ChannelSftp.LsEntry entry : list) {
                System.out.println(entry.getFilename());
                channelSftp.rm(entry.getFilename());
            }
        } catch (Exception ex) {
            Logger.getLogger(SmartSFTPDownloader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            channelSftp.exit();
            System.out.println("sftp Channel exited.");
            channel.disconnect();
            System.out.println("Channel disconnected.");
            session.disconnect();
            System.out.println("Host Session disconnected.");
        }

    }

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
        } catch (Exception e) {
            e.printStackTrace();;
        }
        return session;
    }

}
