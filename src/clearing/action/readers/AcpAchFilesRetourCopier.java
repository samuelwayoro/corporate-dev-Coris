/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.patware.action.file.FileCopier;
import org.patware.bean.table.Repertoires;
import org.patware.utils.Utility;

/**
 *
 * @author BOUIKS
 */
public class AcpAchFilesRetourCopier extends FileCopier {

    
    public AcpAchFilesRetourCopier() {
        super();
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        System.out.println("AcpAchFilesRetourCopier ");
        if ((repertoire.getPartenaire() != null) && !(repertoire.getPartenaire().equals("null"))) {

 

            System.out.println("AcpAchFilesRetourCopier copie des fichiers" + aFile.getAbsolutePath());
            String dateCopy = Utility.convertDateToString(new Date(), "ddMMyyyy");

            String extension = aFile.getName().substring(aFile.getName().lastIndexOf(".") + 1);  //data ou cat pak
            String substring = aFile.getName().substring(10, 18);
            if (extension.toUpperCase().equalsIgnoreCase("RCP")) {

                if (substring.equals(dateCopy)) {
                    FileUtils.copyFile(aFile, new File(repertoire.getPartenaire() + File.separator + aFile.getName()));

                } else {

                }
            }
            if (extension.toUpperCase().equalsIgnoreCase("PAK")) {

                if (substring.equals(dateCopy)) {

                    File pakFileDestination = new File(repertoire.getPartenaire() + File.separator + aFile.getName());
                    FileUtils.copyFile(aFile, pakFileDestination);
                    System.out.println("Copie du fichier PAK terminée" + pakFileDestination.getAbsolutePath());

                    //Copie du PAK terminée
                    Path path = Paths.get(pakFileDestination.toURI());
                    // Load as binary:
                    byte[] pakBytes = Files.readAllBytes(path);
                    // String catFile = aFile.getName().replace("PAK", "CAT");
                    if (pakBytes.length > 0) {

                        File catFile = new File(aFile.getParent() + File.separator + aFile.getName().replace("PAK", "CAT"));
                        //   File catFile = new File(aFile.getName().replace("PAK", "CAT"));
                        System.out.println("catFile Name" + catFile.getName());

                        File catFileDestination = new File(repertoire.getPartenaire() + File.separator + catFile.getName());
                        FileUtils.copyFile(catFile, catFileDestination);
                        System.out.println("Copie du fichier CAT terminée" + catFileDestination.getAbsolutePath());
                    }

                } else {

                }
            }
        }

        closeFile();
        return null;
    }

}
