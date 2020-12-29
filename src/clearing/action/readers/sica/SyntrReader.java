/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.readers.sica;

import clearing.model.CMPUtility;
import org.patware.action.file.FlatFileReader;
import clearing.model.Enreg;
import clearing.model.EnteteRemise;
import java.io.BufferedReader;
import java.io.File;
import org.patware.bean.table.Repertoires;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class SyntrReader extends FlatFileReader {

    public SyntrReader() {
        setCopyOriginalFile(true);
    }

    

    @Override
    public File treatFile(File aFile, Repertoires repertoire)throws Exception {
        setFile(aFile);
    
        String line = null;
        
           
        BufferedReader is =  openFile(aFile);
            EnteteRemise enteteRemise = new EnteteRemise();
           
           
            int cptLot = -1;
            int cptEnreg = -1;
            while ((line = is.readLine()) != null) {
                setCurrentLine(line);    
                if(line.startsWith("ESYR")){
                    enteteRemise.setIdEntete(getChamp(4));
                    enteteRemise.setIdEmetteur(getChamp(5));
                    enteteRemise.setRefRemise(getChamp(3));
                    enteteRemise.setDatePresentation(Utility.convertStringToDate(getChamp(8),"yyyyMMdd"));
                    enteteRemise.setIdRecepeteur(getChamp(5));
                    enteteRemise.setDevise(getChamp(3));
                    enteteRemise.setTypeRemise(getChamp(5));
                    getChamp(8);
                    enteteRemise.setNbLots(getChamp(3));
                    enteteRemise.setSolde(""+Long.parseLong(getChamp(16)));
                    enteteRemise.setSigne(getChamp(1));
                    enteteRemise.setFlagInversion(getChamp(1));
                    getChamp(2);
                    enteteRemise.enregs = new Enreg[Integer.parseInt(enteteRemise.getNbLots())];
                }else if(line.startsWith("ERGS")){
                      cptEnreg = -1;
                      enteteRemise.enregs[++cptLot]= new Enreg();
                      getChamp(4);
                      enteteRemise.enregs[cptLot].setTypeOperation(getChamp(3));
                      enteteRemise.enregs[cptLot].setIdBanCon(getChamp(5));
                      enteteRemise.enregs[cptLot].setSolde(""+Long.parseLong(getChamp(16)));
                      enteteRemise.enregs[cptLot].setSigne(getChamp(1));
                      getChamp(35);
                      
                }else if(line.startsWith("FSYO")){
                    
                }
            }
            
              CMPUtility.insertSynthese(enteteRemise);

        
        return aFile;   
    }

}
