/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.readers.uap;

import clearing.action.readers.sica.OutgoReader;
import clearing.action.writers.uap.CroUAPTPCIWriter;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;


/**
 *
 * @author Patrick Augou
 */
public class OUTGOToCROTPCIReader extends FlatFileReader {

/**
     * Creates a new instance of OUTGOToCROReader
     */
    public OUTGOToCROTPCIReader()
    {

    }

 @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
      
       File fileTreated =  new OutgoReader().treatFile(aFile, repertoire);

       new CroUAPTPCIWriter().execute();
     
       return fileTreated;
    }


}
