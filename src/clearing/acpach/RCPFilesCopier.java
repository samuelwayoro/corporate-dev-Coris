/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.acpach;

import clearing.action.readers.*;
import java.io.File;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.patware.action.file.FileCopier;
import org.patware.bean.table.Repertoires;
import org.patware.utils.Utility;

/**
 *
 * @author BOUIKS
 */
public class RCPFilesCopier extends FileCopier {

    public RCPFilesCopier() {
        super();
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        
        System.out.println("Copie des rcp");
        if ((repertoire.getPartenaire() != null) && !(repertoire.getPartenaire().equals("null"))) {
            String dateCopy = Utility.convertDateToString(new Date(), "ddMMyyyy");

            String extension = aFile.getName().substring(aFile.getName().lastIndexOf(".") + 1);  //data ou cat pak
            System.out.println("extension"+extension);
            System.out.println("dateCopy"+dateCopy);
            
            //01-GN-015-11062018-095006-30-21-324.RCP

            if (extension.toUpperCase().equalsIgnoreCase("RCP") ) {
                //Copie fichier data Elementaire 30000003901500132405062018103332.Data
                if (aFile.getName().substring(10, 19).equals(dateCopy)) {
                    FileUtils.copyFile(aFile, new File(repertoire.getPartenaire() + File.separator + aFile.getName()));
//                    FileUtils.copyFile(aFile,new File(repertoire.getPartenaire() +  File.separator +  aFile.getName()));
                

                   
                }
            }
 
        }
        
        
        closeFile();
        return null;
    }

}
