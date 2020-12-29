/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.sica;

import clearing.model.CMPUtility;
import clearing.model.EnteteRemise;
import java.sql.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class InitrSrgWriter extends FlatFileWriter{

    
    public InitrSrgWriter(){
         setDescription("Envoi INITR Sous-regional vers BCEAO");
    }
    
    public void execute() throws Exception{
       super.execute();
        EnteteRemise enteteRemise = new EnteteRemise();
        String sequence = Utility.bourrageGZero(Utility.computeCompteur("INITR_SRG",Utility.convertDateToString(new Date(System.currentTimeMillis()),"yyyyMMdd")),3);
        enteteRemise.setIdEntete("EINI");
        if (Utility.getParam("VERSION_SICA").equals("2")){
            enteteRemise.setIdEmetteur("KSCSR");
        }else{
            enteteRemise.setIdEmetteur("SNSSR");
        }
        enteteRemise.setRefRemise(sequence);
        enteteRemise.setDatePresentation(new Date(System.currentTimeMillis()));
        if (Utility.getParam("VERSION_SICA").equals("2")){
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanque());
        }else{
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanqueSica3());
        }
        enteteRemise.setDevise(CMPUtility.getDevise());
        enteteRemise.setTypeRemise("INITR");
        enteteRemise.setRefRemRelatif("000");
        enteteRemise.setCodeRejet("00");
        enteteRemise.setBlancs(createBlancs(12," "));

     String fileName = CMPUtility.getSrgFileName(sequence, enteteRemise.getTypeRemise());
     setOut(createFlatFile(fileName));
     String line = new String(enteteRemise.getIdEntete()
                  +enteteRemise.getIdEmetteur()
                  +enteteRemise.getRefRemise()
                  +createBlancs(8, "0")
                  +enteteRemise.getIdRecepeteur()
                  +enteteRemise.getDevise()
                  +enteteRemise.getTypeRemise()
                  +enteteRemise.getRefRemRelatif()
                  +enteteRemise.getCodeRejet()
                  +enteteRemise.getBlancs());
     writeln(line);    
     writeEOF("FINI",createBlancs(28," "));
     
    }
    
   
    
    
}
