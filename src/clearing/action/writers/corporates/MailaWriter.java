/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.corporates;

import clearing.model.CMPUtility;
import clearing.model.EnteteRemise;
import java.io.File;
import java.sql.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;

/**
 *
 * @author Patrick
 */
public class MailaWriter extends FlatFileWriter {

    private String nomFichierMaila;

    public MailaWriter() {
        setDescription("Envoi MAILA vers Partenaire");
    }

    public MailaWriter(String name) {

        name = Utility.removeFileNameSuffixe(name, "MAILI");
        name = name +"MAILA";
        setNomFichierMaila(name);


    }

    public String getNomFichierMaila() {
        return nomFichierMaila;
    }

    
    public void setNomFichierMaila(String nomFichierMaila) {
        this.nomFichierMaila = nomFichierMaila;
    }



    @Override
    public void execute() throws Exception {
        super.execute();
        EnteteRemise enteteRemise = new EnteteRemise();
        
        enteteRemise.setIdEntete("ECMA");
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            enteteRemise.setIdEmetteur(CMPUtility.getCodeBanque().charAt(0) + "SCPM");
        } else {
            enteteRemise.setIdEmetteur(CMPUtility.getCodeBanqueSica3().substring(0, 2) + "SCN");
        }
        
        enteteRemise.setDatePresentation(new Date(System.currentTimeMillis()));
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanque());
        } else {
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanqueSica3());
        }
        
        enteteRemise.setTypeRemise("MAILA");
       
        enteteRemise.setBlancs(createBlancs(42, " "));

        String fileName = Utility.getParam("MAILA_NAT_FOLDER")+ File.separator + getNomFichierMaila();
        setOut(createFlatFile(fileName));
        String line = enteteRemise.getIdEntete() + enteteRemise.getIdEmetteur() + Utility.convertDateToString(enteteRemise.getDatePresentation(),ResLoader.getMessages("patternDate")) + enteteRemise.getTypeRemise() + enteteRemise.getBlancs();
        writeln(line);
        writeEOF("FCMA", createBlancs(28, " "));
       
    }
}
