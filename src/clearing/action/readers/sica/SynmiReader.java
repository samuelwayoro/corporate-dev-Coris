/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.sica;

import clearing.model.CMPUtility;
import org.patware.action.file.FlatFileReader;
import clearing.model.Enreg;
import clearing.model.EnteteRemise;
import java.io.BufferedReader;
import java.io.File;
import org.patware.action.file.FlatFileWriter;
import org.patware.bean.table.Repertoires;
import org.patware.log.SmartFileLogger;
import org.patware.utils.MD5;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class SynmiReader extends FlatFileReader {

    public SynmiReader() {
    setCopyOriginalFile(true);
    }


    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;


        BufferedReader is = openFile(aFile);
        EnteteRemise enteteRemise = new EnteteRemise();


        int cptLot = -1;
        int cptEnreg = -1;
        MD5 md5 = new MD5();

        while ((line = is.readLine()) != null) {
            if (!line.startsWith("FSMI")) {
                md5.update(line);
                md5.update("\n");
            }
            setCurrentLine(line);
            if (line.startsWith("ESMI")) {
                enteteRemise.setIdEntete(getChamp(4));
                enteteRemise.setIdEmetteur(getChamp(5));
                enteteRemise.setRefRemise(getChamp(3));
                enteteRemise.setDatePresentation(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                enteteRemise.setIdRecepeteur(getChamp(5));
                enteteRemise.setDevise(getChamp(3));
                enteteRemise.setTypeRemise(getChamp(5));
                getChamp(8);
                enteteRemise.setNbTotRemAllEnv(getChamp(16));
                enteteRemise.setNbTotRemAllAccTot(getChamp(16));
                enteteRemise.setNbTotRemAllRejTot(getChamp(16));
                enteteRemise.setNbLots(getChamp(3));
                enteteRemise.setSeance(getChamp(1));
                enteteRemise.setFlagInversion(getChamp(1));
                getChamp(2);
                enteteRemise.enregs = new Enreg[Integer.parseInt(enteteRemise.getNbLots())];
            } else if (line.startsWith("ERMS")) {
                cptEnreg = -1;
                enteteRemise.enregs[++cptLot] = new Enreg();
                getChamp(4);
                enteteRemise.enregs[cptLot].setTypeOperation(getChamp(3));
                enteteRemise.enregs[cptLot].setIdBanCon(getChamp(5));
                enteteRemise.enregs[cptLot].setNbTotOperEmis(""+Integer.parseInt(getChamp(16)));
                getChamp(36);

            } else if (line.startsWith("FSMI")) {
                md5.update("FSMI" + FlatFileWriter.createBlancs(28, " "));
                System.out.println("Checksum = " + md5.digest());
            }
        }


        CMPUtility.insertSynthese(enteteRemise);



        return aFile;
    }
}
