/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.finacle;

import clearing.action.writers.finacle.PrelevementRetourFinacleWriter;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.utils.Utility;

/**
 *
 * @author AUGOU Patrick
 */
public class PrelevementRetourReader extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        setFile(aFile);
        PrelevementRetourFinacleWriter prWriter = new PrelevementRetourFinacleWriter(Utility.getParam("CETAOPERET"));
        prWriter.execute();
        return aFile;

    }

}
