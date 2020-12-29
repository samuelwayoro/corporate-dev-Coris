/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.uap;

import clearing.model.CMPUtility;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class IMGToMAILIReader extends FlatFileReader {

    public IMGToMAILIReader() {
        setTattooProcessDate(true);
    }



    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        String fileNamePart[] = aFile.getName().split("\\p{Punct}");
       
        String codeBanqueDestinataire = "";
        String sequence ="";
        String mailiFileName ="";

        if(fileNamePart!= null){
           
           codeBanqueDestinataire = fileNamePart[6];
        }
        
        if(codeBanqueDestinataire.substring(0, 2).equalsIgnoreCase(Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2))){
            sequence = Utility.bourrageGZero(Utility.computeCompteur("MAILI_NAT", Utility.getParam("DATECOMPENS_NAT")), 3);    
            mailiFileName = CMPUtility.getMailiNatFileName(codeBanqueDestinataire, CMPUtility.getPacSCMPSICA3(), sequence, "IMC", "MAILI");
        }else{
            sequence = Utility.bourrageGZero(Utility.computeCompteur("MAILI_SRG", Utility.getParam("DATECOMPENS_SRG")), 3);    
            mailiFileName = CMPUtility.getMailiSrgFileName(codeBanqueDestinataire, CMPUtility.getPacSCSRSICA3(), sequence, "IMC", "MAILI");
        }


        
        renameFile(aFile, mailiFileName);
       
        return null;

    }

    public File renameFile(File aFile, String newFname) throws Exception {
        System.out.println("Tentative de renommage de " + aFile.getAbsolutePath() + " en " + newFname);
        if (aFile.renameTo(new File(newFname))) {
            return new File(newFname);
        } else {
            if(Utility.getCurrentTray() != null){
                Utility.getCurrentTray().showTrayErrorBalloon(ResLoader.getMessages("AppTitle"), "Impossible de renommer en " + newFname);
            }
            throw new Exception("Impossible de renommer en " + newFname);
        }

    }
}
