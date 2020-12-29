/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.flexcube12;

import clearing.action.writers.flexcube12.esn.DetbUploadMasterWriter;
import clearing.model.CMPUtility;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.utils.Utility;

/**
 *
 * @author BOUIKS
 */
public class SqlLoaderLogReader extends FlatFileReader {

    public SqlLoaderLogReader() {
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        System.out.println("aFile" + aFile);
        setFile(aFile);
        System.out.println("SqlLoaderLogReader Version for Bad File"); //SIB_IN_FOLDER
        String archFolder = Utility.getParam("SIB_IN_FOLDER") + File.separator + "archives" + File.separator + CMPUtility.getDate();
        String fichierBatch = aFile.getName().replaceAll("#SqlLoaderLogReader", "");

        int lastIndexOf = fichierBatch.lastIndexOf(".");

        String fileWithoutExtension = fichierBatch.substring(0, lastIndexOf);
        String badFile = fileWithoutExtension + ".BAD";

        File file = new File(archFolder + File.separator + badFile);
        /**
         * File rcpToBeTreated = new File(fileWithoutExtension + ".RCP");
         */
        boolean badFileExist = file.exists(); //file   //true
        int giveUpVar = 0;
        while (!badFileExist && giveUpVar <= 4) {
            try {
                Thread.sleep(5000);
                giveUpVar = giveUpVar + 1;

                badFileExist = Files.exists(Paths.get(file.toURI()));
            } catch (InterruptedException ex) {
                Logger.getLogger(SqlLoaderLogReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (giveUpVar == 5) {
            System.out.println("Giving up, Je vais tester voir si le fichier BAD existe");

        }
        if (!badFileExist) {
            System.out.println("Le fichier BAD :[" + file + "] n'existe pas, On peut inserer dans la table master ");

            int result = 1;
            String numeroBatch = "";
            String libelle = "";
            int indexOf = aFile.getName().indexOf("_");

            // String fichierBatch = aFile.getName().replaceAll("#SqlLoaderLogReader", "");
            if (!aFile.getName().toUpperCase().contains("OLD") || !aFile.getName().toUpperCase().contains("OLD")) {
                numeroBatch = aFile.getName().substring(indexOf + 1, indexOf + 5);
            } else {
                numeroBatch = fichierBatch.substring(7, 11);
            }

            System.out.println("numeroBatch " + numeroBatch);
            System.out.println("fichierBatch " + fichierBatch);

            if (aFile.getName().toUpperCase().contains("VIR") || aFile.getName().toUpperCase().contains("EFF")) {
                if (aFile.getName().toUpperCase().contains("VIR")) {
                    libelle = "VIREMENTS RETOUR DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                } else {
                    libelle = "EFFETS RETOUR DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                }

                //Lancer le Writer de Master
                DetbUploadMasterWriter detbUploadMasterWriter = new DetbUploadMasterWriter(numeroBatch, libelle, fichierBatch);
                detbUploadMasterWriter.execute();

            } else if (aFile.getName().toUpperCase().contains("AC")
                    || aFile.getName().toUpperCase().contains("LCD")
                    || aFile.getName().contains("RC")
                    || aFile.getName().toUpperCase().contains("DAY")
                    || aFile.getName().contains("CR")
                    || aFile.getName().toUpperCase().contains("RJ")) {
                //RJ Cheques

                if (aFile.getName().toUpperCase().contains("AC") || aFile.getName().toUpperCase().contains("CR") || aFile.getName().toUpperCase().contains("RJ") || aFile.getName().toUpperCase().contains("DAY")) {
                    libelle = "ALLER COMPENSE DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                } else if (aFile.getName().toUpperCase().contains("LCD")) {
                    libelle = "CHEQUES NON COMPENSABLE DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                } else if (aFile.getName().contains("RC")) {
                    libelle = "RETOUR COMPENSE DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                }

//                split = (aFile.getName().toUpperCase().contains("RJ") || aFile.getName().toUpperCase().contains("LCDA")) ? lines.get(64).split(":") : lines.get(63).split(":");
//                result = Integer.parseInt(split[1].trim());
                //Lancer le Writer de Master
                DetbUploadMasterWriter detbUploadMasterWriter = new DetbUploadMasterWriter(numeroBatch, libelle, fichierBatch);
                detbUploadMasterWriter.execute();

            } else {
                if (fichierBatch.toUpperCase().contains("OLD") && fichierBatch.toUpperCase().contains("NEW")) {
                    libelle = " COMPENSE DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                    //Lancer le Writer de Master
                    DetbUploadMasterWriter detbUploadMasterWriter = new DetbUploadMasterWriter(numeroBatch, libelle, fichierBatch);
                    detbUploadMasterWriter.execute();

                } else {
                    libelle = " COMPENSE DU " + Utility.convertDateToString(new Date(), "dd-MM-yyyy");
                    //Lancer le Writer de Master
                    DetbUploadMasterWriter detbUploadMasterWriter = new DetbUploadMasterWriter(numeroBatch, libelle, fichierBatch);
                    detbUploadMasterWriter.execute();
                }

            }

        } else {
            System.out.println("ERR : Fichier BAD :[" + file + "] existe ");
        }

        return aFile;
    }

}
