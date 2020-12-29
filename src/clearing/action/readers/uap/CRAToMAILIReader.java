/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.uap;

import clearing.action.writers.uap.IMGNatWriter;
import clearing.action.writers.uap.IMGSrgWriter;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;

/**
 *
 * @author Patrick Augou
 */
public class CRAToMAILIReader extends FlatFileReader {

    /**
     * Creates a new instance of OUTGOToCROReader
     */
    public CRAToMAILIReader() {
 
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        File fileTreated = new CRAUAPReader().treatFile(aFile, repertoire);

        new IMGNatWriter().execute();
        new IMGSrgWriter().execute();

        return fileTreated;
    }

}
