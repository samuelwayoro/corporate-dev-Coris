/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.readers.uap;

import clearing.action.readers.sica.OutgoReader;
import clearing.action.writers.corporates.RejetSicaChequeCorporatesWriter;
import clearing.action.writers.uap.CroUAPBNPWriter;
import clearing.action.writers.uap.CroUAPESNWriter;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;


/**
 *
 * @author Patrick Augou
 */
public class OUTGOToCROBNPReader extends FlatFileReader {

/**
     * Creates a new instance of OUTGOToCROReader
     */
    public OUTGOToCROBNPReader()
    {

    }

 @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
      
       File fileTreated =  new OutgoReader().treatFile(aFile, repertoire);

       new CroUAPBNPWriter().execute();
       
     
       return fileTreated;
    }


}
