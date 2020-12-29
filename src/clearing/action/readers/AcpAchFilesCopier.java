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
public class AcpAchFilesCopier extends FileCopier {

    public AcpAchFilesCopier() {
        super();
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        if ((repertoire.getPartenaire() != null) && !(repertoire.getPartenaire().equals("null"))) {
            String dateCopy = Utility.convertDateToString(new Date(), "ddMMyyyy");

            String extension = aFile.getName().substring(aFile.getName().lastIndexOf(".") + 1);  //data ou cat pak

            if (extension.toUpperCase().equalsIgnoreCase("DATA") && aFile.getName().length() == 37) {  
                //Copie fichier data Elementaire 30000003901500132405062018103332.Data
                if (aFile.getName().substring(18, 26).equals(dateCopy)) {
                    FileUtils.copyFile(aFile, new File(repertoire.getPartenaire() + File.separator + aFile.getName()));
//                    FileUtils.copyFile(aFile,new File(repertoire.getPartenaire() +  File.separator +  aFile.getName()));

                }
            }
            if (extension.toUpperCase().equalsIgnoreCase("PAK") && aFile.getName().length() == 55) {
                //Copie fichier CAT/PAK Elementaire
                //000290300301700006380615000450023248805062018103620.PAK 
                if (aFile.getName().substring(37, 45).equals(dateCopy)) {
                    File pakFileDestination = new File(repertoire.getPartenaire() + File.separator + aFile.getName());
                    FileUtils.copyFile(aFile, pakFileDestination);

                    //Copie du PAK terminée
                    Path path = Paths.get(pakFileDestination.toURI());
                    // Load as binary:
                    byte[] pakBytes = Files.readAllBytes(path);
                    // String catFile = aFile.getName().replace("PAK", "CAT");
                    if (pakBytes.length > 0) {
                        File catFile = new File(aFile.getParent()+File.separator +  aFile.getName().replace("PAK", "CAT"));
                        System.out.println("catFile Name" + catFile.getName());
                   
                        FileUtils.copyFile(catFile, new File(repertoire.getPartenaire() + File.separator + catFile.getName()));
                    }

                }
            }
        }

        closeFile();
        return null;
    }

}
