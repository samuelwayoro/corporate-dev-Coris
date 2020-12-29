/*
 * CFTUtil.java
 *
 * Created on 15 juillet 2007, 16:31
 */
package clearing.action;

import java.io.File;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;

/**
 * Classe SQLLoader Cette classe permet de faire un SQLLoader sous Oracle
 *
 * @author Patrick
 */
public class OracleSQLLoader extends FlatFileReader {

    private String dataFileName;
    private String username;
    private String password;
    private String instance;
    private File dataFile;

    /**
     * Creates a new instance of CFTUtil
     */
    public OracleSQLLoader() {
    }

    /**
     * Constructeur qui utilise les paramètres
     *
     * @param username Utilisateur BD
     * @param password Password de l'utilisateur BD
     * @param instance DB instance
     * @param dataFileName The file name of the file to send
     */
    public OracleSQLLoader(String dataFileName, String username, String password, String instance) {
        this.dataFileName = dataFileName;
        this.username = username;
        this.password = password;
        this.instance = instance;

    }

    /**
     * Send a file by CFT
     *
     * @param dataFileName The file name of the file to send
     */
    public void send(String dataFileName) {
        send(getUsername(), getPassword(), getInstance(), dataFileName);
    }

    /**
     * Send a file by SQLLoader
     *
     * @param username DB User
     * @param password DB User Password
     * @param instance DB instance
     * @param dataFileName The file name of the file to send
     */
    public void send(String username, String password, String instance, String dataFileName) {
        try {
            String archFolder = Utility.getParam("SIB_IN_FOLDER");
            //            String archFolder = Utility.getParam("SIB_IN_FOLDER") + File.separator + "archives" + File.separator + CMPUtility.getDate();
            File waitFolder = new File(archFolder);
            Utility.createFolderIfItsnt(waitFolder, null);

            String cmdLine = "SQLLDR USERID=" + username + "/" + password + "@" + instance + " CONTROL=" + dataFileName + " LOG=" + archFolder + File.separator + new File(dataFileName).getName() + ".log";
            System.out.println("Commande = " + cmdLine);
            Process p = Utility.execute(cmdLine);
            cmdLine = "EXIT";
            System.out.println("Commande = " + cmdLine);
            Utility.executeLineInProcess(p, cmdLine);

            if (Utility.createFolderIfItsnt(waitFolder, null)) {
                File f = new File(dataFileName);
                System.out.println("Fichier traité dans OracleSQLLoader :" + dataFileName);
                if (!dataFileName.contains("MASTER")) {
                    System.out.println("!dataFileName contains (\"MASTER\")");
                } else {
                    System.out.println("dataFileName  contient MASTER");
                }
                if (!dataFileName.contains("MASTER") && !dataFileName.contains("DEBIT")) {
                    File archf = new File(archFolder + File.separator + "DAT" + File.separator + f.getName());
                    FileUtils.copyFile(f, archf);
                    if (archf.exists()) {
                        if (!f.delete()) {
                            FileUtils.forceDelete(f);
                        }
                    }
                } else {
                    if (dataFileName.toUpperCase().contains("DEBIT")) {
                        File archf = new File(archFolder + File.separator + "DEBIT" + File.separator + f.getName());
                        Utility.createFolderIfItsnt(archf.getParentFile(), null);
                        FileUtils.copyFile(f, archf);
                        if (archf.exists()) {
                            if (!f.delete()) {
                                FileUtils.forceDelete(f);
                            }
                        }

                    } else {
                        File archf = new File(archFolder + File.separator + "MASTER_BKP" + File.separator + f.getName());
                        FileUtils.copyFile(f, archf);
                        if (archf.exists()) {
                            if (!f.delete()) {
                                FileUtils.forceDelete(f);
                            }
                        }
                    }

                }
//////
            }

        } catch (Exception e) {
            if (Utility.getCurrentTray() != null) {
                Utility.getCurrentTray().showTrayErrorBalloon(ResLoader.getMessages("TitreApp"), "Erreur en accédant a SQLLDR");
            }
            logEvent("ERROR", "Erreur en accédant a SQLLDR");
            e.printStackTrace();
        }

    }

    @Override
    public void setRepertoire(Repertoires repertoire) {
        setWaitFolder(Utility.getParam("TempCFT"));
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) {
        Utility.clearParamsCache();
        this.dataFile = aFile;
        this.instance = Utility.getParam("ORACLEINSTANCE");
        this.username = Utility.getParam("ORACLEUSER");
        this.password = Utility.getParamOfType("ORACLEPASSWORD", "CODE_CRYPTED");
        byte[] decoded;
        if (password != null) {
            decoded = Base64.decodeBase64(Utility.getParam("ORACLEPASSWORD"));
            password = new String(decoded);
        } else {
            password = Utility.getParam("ORACLEPASSWORD");
        }
        System.out.println("Traitement de " + aFile.getAbsolutePath());
        send(username, password, instance, dataFile.getAbsolutePath());
        System.out.println("Traitement terminé de " + aFile.getAbsolutePath());
        return null;

    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public File getDataFile() {
        return dataFile;
    }

    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }

}
