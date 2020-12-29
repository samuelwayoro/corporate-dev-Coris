/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers;

import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.action.file.UnZipper;
import org.patware.bean.table.Repertoires;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class SBSSynFileReader extends FlatFileReader {

    private UnZipper unZipper = new UnZipper();

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        String fileRoot = aFile.getName().substring(0, aFile.getName().lastIndexOf("."));
        fileRoot = fileRoot.substring(fileRoot.lastIndexOf("#") + 1);
        File zipFile = new File(aFile.getParent() + File.separator + fileRoot + ".zip");
        //System.out.println(""+zipFile.getAbsolutePath());

        String chemin = repertoire.getChemin() + File.separator + "workFolder" + File.separator;
        Utility.createFolderIfItsnt(new File(chemin), this);
        if (unZipper.unZip2(zipFile, chemin)) {

           String param = Utility.getParam("SAVEZIPFILE");
                    if (param != null) {
                        if (param.equalsIgnoreCase("0"));
                        if (param.equalsIgnoreCase("1")) {
                            String junkFolderName = repertoire.getChemin() + File.separator + "zipFolder" + File.separator;
                            if (junkFolderName != null) {
                                File junkFolder = new File(junkFolderName);
                                Utility.createFolderIfItsnt(junkFolder, null);
                                File zipToSave = new File(junkFolderName + File.separator + zipFile.getName());
                                zipFile.renameTo(zipToSave);
                               
                            }

                        }
                        if (param.equalsIgnoreCase("2")) {
                            zipFile.delete();
                          
                        }

                    }

        }
        return aFile;

    }
}
