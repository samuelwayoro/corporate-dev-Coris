/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

import org.patware.action.file.FlatFileWriter;

/**
 *
 * @author Patrick Augou
 */
public class FichierAllerDeltaBDKWriter extends FlatFileWriter {


    private EntreeGardeChequesBkmvtiWriter egcbw;
    private SortieGardeChequesBkmvtiWriter sgcbw;
    
    private PrelevementSortsPayesBkmvtiWriter pspbw;
    private PrelevementSortsImpayesBkmvtiWriter psibw;
    
    private LotDeltaBDKWriter ldbdkw;
 
  
    public FichierAllerDeltaBDKWriter() {
        setDescription("Generation des fichiers Garde Cheque vers le SIB");

       ldbdkw = new LotDeltaBDKWriter();
       egcbw = new EntreeGardeChequesBkmvtiWriter();
       sgcbw = new SortieGardeChequesBkmvtiWriter();
       
       pspbw = new PrelevementSortsPayesBkmvtiWriter();
       psibw = new PrelevementSortsImpayesBkmvtiWriter();
    }

    @Override
    public void execute() throws Exception {
        super.execute();
   
        ldbdkw.setParametersMap(getParametersMap());
        ldbdkw.setDescription("Generation Fichier LOT Aller pour Delta");
        ldbdkw.execute();
        setDescription(getDescription() + "\n" + ldbdkw.getDescription());

        
        egcbw.setParametersMap(getParametersMap());
        egcbw.setDescription("Generation Fichier BKMVTI Entree Garde Cheque");
        egcbw.execute();
        setDescription(getDescription() + "\n" + egcbw.getDescription());

        sgcbw.setParametersMap(getParametersMap());
        sgcbw.setDescription("Generation Fichier BKMVTI Sortie Garde Cheque");
        sgcbw.execute();
        setDescription(getDescription() + "\n" + sgcbw.getDescription());
        
        pspbw.setParametersMap(getParametersMap());
        pspbw.setDescription("Generation Fichier BKMVTI Sort Paye");
        pspbw.execute();
        setDescription(getDescription() + "\n" + pspbw.getDescription());

        psibw.setParametersMap(getParametersMap());
        psibw.setDescription("Generation Fichier BKMVTI Sort Impaye");
        psibw.execute();
        setDescription(getDescription() + "\n" + psibw.getDescription());

  
    }
}
