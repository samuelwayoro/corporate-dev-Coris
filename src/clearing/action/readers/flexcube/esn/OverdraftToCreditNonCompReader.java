/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.flexcube.esn;

import clearing.action.writers.flexcube.esn.ChequeAllerSurCaisseCreditFlexCubeSQLWriter;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;

/**
 *
 * @author AUGOU Patrick
 */
public class OverdraftToCreditNonCompReader extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        ChequeOverdraftFlexCubeReader overdraftReader = new ChequeOverdraftFlexCubeReader();
        File fileTreated = overdraftReader.treatFile(aFile, repertoire);

        ChequeAllerSurCaisseCreditFlexCubeSQLWriter creditNonCompWriter = new ChequeAllerSurCaisseCreditFlexCubeSQLWriter();
        creditNonCompWriter.execute();

        return fileTreated;
    }

}
