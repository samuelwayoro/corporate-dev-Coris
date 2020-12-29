/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.readers;

import clearing.action.writers.borne.ComptesBorneWriter;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;


/**
 *
 * @author Patrick Augou
 */
public class ComptesBorneReader extends FlatFileReader {

/**
     * Creates a new instance of OUTGOToCROReader
     */
    public ComptesBorneReader()
    {

    }

 @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
      
       File fileTreated =  new ComptesBNPReader().treatFile(aFile, repertoire);

       new ComptesBorneWriter().execute();
     
       return fileTreated;
    }


}
