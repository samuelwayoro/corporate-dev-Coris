/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.uap;

import clearing.table.Cheques;
import java.io.File;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.utils.Utility;

/**
 *
 * @author BOUIKS
 */
public class OrdFileWriter extends FlatFileWriter {

    private Cheques cheques35[];
    private String sequence;

    public OrdFileWriter() {
        setDescription("Envoi de Fichier ORD ");
    }

    public OrdFileWriter(Cheques cheques35[], String sequence) {
        this.cheques35 = cheques35;
        this.sequence = sequence;
    }

    @Override
    public void execute() throws Exception {
        System.out.println("Execution ordFileWriter");
        //        //ORD008IMAGE000003035NXOFSN012000038    

        System.out.println("sequence " + sequence);
        String fileName = Utility.getParam("ORD_FOLDER") + File.separator
                + cheques35[0].getBanqueremettant() + "." + Utility.bourrageGauche("" + sequence, 3, "0") + "."
                + Utility.convertDateToString(new Date(), "yyyyMMddHHmmss") + ".ORD";
        setOut(createFlatFile(fileName));
        StringBuilder line = new StringBuilder();
        line.append("ORD");
        line.append(Utility.bourrageGauche("" + sequence, 3, "0"));
        line.append("IMAGE"); //000
        line.append("000");
        line.append(Utility.bourrageGauche("" + sequence, 3, "0"));
        line.append("035"); //NXOF
        line.append((cheques35[0].getBanque().substring(0, 2).equals(Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2))?"N":"Z"  ) ); //CODE_BANQUE_SICA3                                 
        line.append("XOF");
        line.append(cheques35[0].getBanque());
        line.append("00");
        line.append(Utility.bourrageGauche("" + cheques35.length, 4, "0"));
        line.append(createBlancs(29, " "));
        wwriteln(line.toString());
        closeFile();
    }

    public File[] listOfFiles(String path) {
        System.out.println("listOfFiles path" + path);
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return listOfFiles;
    }

    public void listFilesForFolder(File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                System.out.println(fileEntry.getName());
            }
        }
    }

    public Cheques[] getCheques35() {
        return cheques35;
    }

    public void setCheques35(Cheques[] cheques35) {
        this.cheques35 = cheques35;
    }

}
