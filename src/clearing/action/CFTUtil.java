/*
 * CFTUtil.java
 *
 * Created on 15 juillet 2007, 16:31
 */

package clearing.action;


import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;

/**
 * Classe CFTUtil
 * Cette classe permet de communiquer avec CFT
 *
 * @author Patrick
 */
public class CFTUtil extends FlatFileReader{
    private String part;
    private String idf;
    private File aFile;
    /** Creates a new instance of CFTUtil */
    public CFTUtil() {
    }

     /** Constructeur qui utilise le partenaire
      * et un identifiant de fichier
      * @param part
      * Partenaire CFT
      * @param idf
      * Identifiant de fichier
      */
    public CFTUtil(String part, String idf) {
    setPart(part);
    setIdf(idf);
    }

     /** Send a file by CFT
      * @param fname
      * The file name of the file to send
      */
    public void send(String fname){
        send(getPart(),getIdf(),fname);
    }
     /** Send a file by CFT
      * @param part
      * Partenaire CFT
      * @param idf
      * Identifiant de fichier
      * @param fname
      * The file name of the file to send
      */
    public void send(String part, String idf, String fname){
       try{
        String cmdLine = "CFTUTIL send PART="+part+",IDF="+idf+",FNAME='"+fname+"'";
        System.out.println("Commande = "+ cmdLine);
        Process p = Utility.execute(cmdLine);
        cmdLine = "EXIT";
        System.out.println("Commande = "+ cmdLine);
        Utility.executeLineInProcess(p,cmdLine);

        }catch(Exception e){
            if(Utility.getCurrentTray() != null){
                Utility.getCurrentTray().showTrayErrorBalloon(ResLoader.getMessages("TitreApp"), "Erreur en accédant a CFTUTIL");
            }
            logEvent("ERROR", "Erreur en accédant a CFTUTIL");
            e.printStackTrace();
        }

    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getIdf() {
        return idf;
    }

    public void setIdf(String idf) {
        this.idf = idf;
    }

    @Override
    public void setRepertoire(Repertoires repertoire) {
        setWaitFolder(Utility.getParam("TempCFT"));
    }


    @Override
    public File treatFile(File aFile, Repertoires repertoire) {
        this.aFile = aFile;


        //String fname = ResLoader.getMessages("Temp") + Utility.removeFileSuffixe(aFile, "WSuffixe");
        //System.out.println("Tentative de renommage de "+ aFile.getAbsolutePath()+ " en " +fname);
       // if(aFile.renameTo(new File(fname))){
                      System.out.println("Traitement de " + aFile.getAbsolutePath());
                      send(repertoire.getPartenaire(),repertoire.getId(),aFile.getAbsolutePath());
                      System.out.println("Traitement terminé de " + aFile.getAbsolutePath());
                      return null;
       /*           }
        else {
            Utility.getCurrentTray().showTrayErrorBalloon(ResLoader.getMessages("TitreApp"),"Impossible de renommer en fichier d'envoi");
            logEvent("ERROR", "Impossible de renommer en fichier d'envoi");
            return null;
        }*/

    }





}
