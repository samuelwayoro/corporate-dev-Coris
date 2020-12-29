/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.acpach;

import clearing.model.EnteteLot;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import org.patware.bean.table.Repertoires;
import org.patware.utils.MD5;

/**
 *
 * @author Patrick
 */
public class VirementAllerReader extends FlatFileReader {

    public VirementAllerReader() {
        setHasNormalExtension(false);
        setExtensionType(END_EXT);
        setCheckIfAlreadyTreated(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;

        BufferedReader is = openFile(aFile);
        EnteteLot[] enteteLots = new EnteteLot[1];

        int cptLot = -1;
        int cptOper = -1;
        MD5 md5 = new MD5();

        System.out.println("repertoire " + repertoire.getPartenaire());
        return null;
    }
}
