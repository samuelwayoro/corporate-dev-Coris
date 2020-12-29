/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.sica;

import clearing.action.writers.uap.CraUAPESNWriter;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;

/**
 *
 * @author AUGOU Patrick
 */
public class IcomaToCraESNReader extends FlatFileReader {
    @Override 
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        
        IcomaReader icomaReader = new IcomaReader();
        
        File fileTreated = icomaReader.treatFile(aFile, repertoire);

        CraUAPESNWriter craUAPWriter = new CraUAPESNWriter();
        
        craUAPWriter.setRemcom(icomaReader.getRemcom());
        craUAPWriter.execute();
                
        return fileTreated;
    }
    
}
