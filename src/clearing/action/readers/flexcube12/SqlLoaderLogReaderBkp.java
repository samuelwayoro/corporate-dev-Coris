/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.flexcube12;

import clearing.action.writers.flexcube12.esn.DetbUploadMasterWriter;
import com.jramoyo.io.IndexedFileReader;
import java.io.File;
import java.util.Date;
import java.util.SortedMap;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.utils.Utility;

/**
 *
 * @author BOUIKS
 */
public class SqlLoaderLogReaderBkp extends FlatFileReader {

    public SqlLoaderLogReaderBkp() {
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        System.out.println("aFile" + aFile);
        setFile(aFile);
        System.out.println("SqlLoaderLogReader New");

        File file = new File(Utility.getParam("SIB_IN_FOLDER") + File.separator + aFile.getName().replaceAll("#SqlLoaderLogReader", " "));
        System.out.println("file chargé " + file);
        try (IndexedFileReader reader = new IndexedFileReader(file)) {
            SortedMap<Integer, String> lines = reader.readLines(60, 65);
            String[] split;
            int result = 1;
            String numeroBatch = "";
            String libelle = "";
            int indexOf = aFile.getName().indexOf("_");
            System.out.println("indexOf " + indexOf);
            System.out.println("aFile.getName() " + aFile.getName());
            String fichierBatch = aFile.getName().replaceAll("#SqlLoaderLogReader", "");
            if (!aFile.getName().toUpperCase().contains("OLD") || !aFile.getName().toUpperCase().contains("OLD")) {
                numeroBatch = aFile.getName().substring(indexOf + 1, indexOf + 5);
            } else {
                numeroBatch = fichierBatch.substring(7, 11);
            }

            System.out.println("numeroBatch " + numeroBatch);

            System.out.println("fichierBatch " + fichierBatch);

            split = lines.get(63).split(":");
            if (split[0].trim().contains("Nombre total d'enregistrements logiques r")) {
                result = Integer.parseInt(split[1].trim());
            } else {
                split = lines.get(64).split(":");
                if (split[0].trim().contains("Nombre total d'enregistrements logiques r")) {
                    result = Integer.parseInt(split[1].trim());
                }

            }
            if (aFile.getName().toUpperCase().contains("VIR") || aFile.getName().toUpperCase().contains("EFF")) {
                if (aFile.getName().toUpperCase().contains("VIR")) {
                    libelle = "VIREMENTS RETOUR DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                } else {
                    libelle = "EFFETS RETOUR DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                }

//                split = lines.get(62).split(":");
//                split = (aFile.getName().toUpperCase().contains("EFF")) ? lines.get(64).split(":") : lines.get(62).split(":");
//                result = Integer.parseInt(split[1].trim());
                if (result == 0) {
                    //Lancer le Writer de Master
                    DetbUploadMasterWriter detbUploadMasterWriter = new DetbUploadMasterWriter(numeroBatch, libelle, fichierBatch);
                    detbUploadMasterWriter.execute();
                }
            } else if (aFile.getName().toUpperCase().contains("AC") || aFile.getName().toUpperCase().contains("LCD") || aFile.getName().contains("CR") || aFile.getName().toUpperCase().contains("RJ")) {
                //RJ Cheques

                if (aFile.getName().toUpperCase().contains("AC") || aFile.getName().toUpperCase().contains("CR") || aFile.getName().toUpperCase().contains("RJ")) {
                    libelle = "ALLER COMPENSE DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                } else {
                    libelle = "CHEQUES NON COMPENSABLE DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                }
//                split = (aFile.getName().toUpperCase().contains("RJ") || aFile.getName().toUpperCase().contains("LCDA")) ? lines.get(64).split(":") : lines.get(63).split(":");
//                result = Integer.parseInt(split[1].trim());
                if (result == 0) {
                    //Lancer le Writer de Master
                    DetbUploadMasterWriter detbUploadMasterWriter = new DetbUploadMasterWriter(numeroBatch, libelle, fichierBatch);
                    detbUploadMasterWriter.execute();
                }
            } else {
                if (fichierBatch.toUpperCase().contains("OLD") &&fichierBatch.toUpperCase().contains("NEW")  ) {
                    libelle = " COMPENSE DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                    if (result == 0) {
                        //Lancer le Writer de Master
                        DetbUploadMasterWriter detbUploadMasterWriter = new DetbUploadMasterWriter(numeroBatch, libelle, fichierBatch);
                        detbUploadMasterWriter.execute();
                    }
                }

            }
        }

        return aFile;
    }

}
