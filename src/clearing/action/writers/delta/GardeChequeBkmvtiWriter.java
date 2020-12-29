/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

import org.patware.action.file.FlatFileWriter;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick Augou
 */
public class GardeChequeBkmvtiWriter extends FlatFileWriter {


    private EntreeGardeChequesBkmvtiWriter egcbw;
    private SortieGardeChequesBkmvtiWriter sgcbw;
 
  
    public GardeChequeBkmvtiWriter() {
        setDescription("Generation des fichiers Garde Cheque vers le SIB");

       
       egcbw = new EntreeGardeChequesBkmvtiWriter();
       sgcbw = new SortieGardeChequesBkmvtiWriter();
    }

    @Override
    public void execute() throws Exception {
        super.execute();
   
        egcbw.setParametersMap(getParametersMap());
        egcbw.setDescription("Generation Fichier BKMVTI Entree Garde Cheque");
        egcbw.execute();
        setDescription(getDescription() + "\n" + egcbw.getDescription());

        sgcbw.setParametersMap(getParametersMap());
        sgcbw.setDescription("Generation Fichier BKMVTI Sortie Garde Cheque");
        sgcbw.execute();
        setDescription(getDescription() + "\n" + sgcbw.getDescription());

  
    }
}
