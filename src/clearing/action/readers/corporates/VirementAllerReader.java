/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.corporates;

import clearing.table.Virements;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class VirementAllerReader extends FlatFileReader {

    public VirementAllerReader() {

        setTattooProcessDate(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;


        Virements virement = new Virements();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {


            setCurrentLine(line);
            
            
            if (line.startsWith("VIR015")) {
                //Debut
            }else{

                virement.setRemise(new BigDecimal(getChamp(7)));
                virement.setOrigine(new BigDecimal(getChamp(7)));
                virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(16))));
                virement.setEtablissement(getChamp(5));
                virement.setReference_Emetteur(getChamp(24));
                virement.setBanque(getChamp(5));
                virement.setAgence(getChamp(5));
                virement.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                virement.setNom_Beneficiaire(getChamp(30));
                virement.setAdresse_Beneficiaire(getChamp(30));
                virement.setBanqueremettant(getChamp(5));
                virement.setAgenceremettant(getChamp(5));
                virement.setNumerocompte_Tire(getChamp(12));
                virement.setNom_Tire(getChamp(30));
                virement.setAdresse_Tire(getChamp(30));
                virement.setLibelle(getChamp(45));
                virement.setZoneinterbancaire_Beneficiaire(getChamp(5));
                virement.setDateordre(getChamp(10));
                virement.setNumerovirement(getChamp(10));
                
                virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                virement.setDevise("XOF");
                virement.setDateordre(virement.getDatetraitement());
                
                virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                
                virement.setType_Virement("015");
                virement.setVille("01");
                virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));
                db.insertObjectAsRowByQuery(virement, "VIREMENTS");


            }
            
        }
        db.close();
        return aFile;

    }
}
