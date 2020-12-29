/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.atlas;

import org.patware.action.file.FlatFileWriter;

/**
 *
 * @author Patrick
 */
public class ExportAllerAtlasSNWriter extends FlatFileWriter {

    private final ExportAllerAtlasBFWriter exportAller;
    private final FraisBceaoWriter fraisBceao; //

    public ExportAllerAtlasSNWriter() {
        setDescription("Generation des fichiers Export et Frais vers le SIB");

        exportAller = new ExportAllerAtlasBFWriter();
        fraisBceao = new FraisBceaoWriter();

    }

    @Override
    public void execute() throws Exception {
        super.execute();
        exportAller.setDescription("Generation Fichier Export Aller");
        exportAller.execute();
        setDescription(getDescription() + "\n" + exportAller.getDescription());

        //
        fraisBceao.setDescription("Generation Fichier BCEAO");
        fraisBceao.execute();
        setDescription(getDescription() + "\n" + fraisBceao.getDescription());

    }
}
