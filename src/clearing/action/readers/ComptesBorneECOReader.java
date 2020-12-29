/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.readers;

import clearing.action.writers.borne.ComptesBorneWriter;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import clearing.action.readers.flexcube.CompteReader;

/**
 *
 * @author Patrick Augou
 */
public class ComptesBorneECOReader extends FlatFileReader {

/**
     * Creates a new instance of OUTGOToCROReader
     */
    public ComptesBorneECOReader()
    {

    }

 @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
      CompteReader compteReader = new CompteReader();
      
       File fileTreated =  compteReader.treatFile(aFile, repertoire);

       new ComptesBorneWriter().execute();
     
       return fileTreated;
    }


}
