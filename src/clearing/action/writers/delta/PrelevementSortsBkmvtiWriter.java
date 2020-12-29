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
public class PrelevementSortsBkmvtiWriter extends FlatFileWriter {


    private PrelevementSortsPayesBkmvtiWriter pspbw;
    private PrelevementSortsImpayesBkmvtiWriter psibw;
 
  
    public PrelevementSortsBkmvtiWriter() {
        setDescription("Generation des fichiers vers le SIB");

       
       pspbw = new PrelevementSortsPayesBkmvtiWriter();
       psibw = new PrelevementSortsImpayesBkmvtiWriter();
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        System.out.println("Date Valeur  = " + dateValeur);
        
        

        pspbw.setParametersMap(getParametersMap());
        pspbw.setDescription("Generation Fichier BKMVTI sort paye");
        pspbw.execute();
        setDescription(getDescription() + "\n" + pspbw.getDescription());

        psibw.setParametersMap(getParametersMap());
        psibw.setDescription("Generation Fichier BKMVTI sort impaye");
        psibw.execute();
        setDescription(getDescription() + "\n" + psibw.getDescription());

  
    }
}
