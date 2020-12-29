/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.flexcube12;

import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;

/**
 *
 * @author AUGOU Patrick
 */
public class SqlLoaderLogToDetbUploadMasterReader extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        SqlLoaderLogReader readerVirement = new SqlLoaderLogReader();
        repertoire = new Repertoires();
        repertoire.setChemin("");
        repertoire.setExtension("log");
        repertoire.setTache("clearing.action.readers.flexcube12.SqlLoaderLogReader");
        //  repertoire.setPartenaire(userInSession.getAdresse().trim());

        File treatFile = readerVirement.treatFile(aFile, repertoire);

        return treatFile;
    }

}
