/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.sica;

import clearing.action.writers.uap.CraUAPWriter;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;

/**
 *
 * @author AUGOU Patrick
 */
public class IcomaToCraReader extends FlatFileReader {
    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        
        IcomaReader icomaReader = new IcomaReader();
        
        File fileTreated = icomaReader.treatFile(aFile, repertoire);

        CraUAPWriter craUAPWriter = new CraUAPWriter();
        
        craUAPWriter.setRemcom(icomaReader.getRemcom());
        craUAPWriter.execute();
                
        return fileTreated;
    }
    
}
