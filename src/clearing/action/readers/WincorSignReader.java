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
public class WincorSignReader extends FlatFileReader  {

    private UnZipper unZipper = new UnZipper();
    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        String fileRoot = aFile.getName().substring(0, aFile.getName().lastIndexOf("."));
         fileRoot = fileRoot.substring(fileRoot.lastIndexOf("#")+1);
         File zipFile = new File(aFile.getParent()+ File.separator + fileRoot +".zip");
         //System.out.println(""+zipFile.getAbsolutePath());
         String atm = fileRoot.substring(0, fileRoot.lastIndexOf("_"));
         String chemin = repertoire.getPartenaire()+ File.separator + atm + File.separator;
         Utility.createFolderIfItsnt(new File(chemin), this);
         unZipper.unZip(zipFile,chemin);
         return aFile;


    }
}
