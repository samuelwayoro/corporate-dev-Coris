/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.corporates;

import clearing.table.Prelevements;
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
public class PrelevementAllerReader extends FlatFileReader {

    public PrelevementAllerReader() {

        setTattooProcessDate(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;


        Prelevements prelevement = new Prelevements();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {


            setCurrentLine(line);
            
            
            if (line.startsWith("PREL025")) {
                //Debut
            }else{

                prelevement.setRemise(new BigDecimal(getChamp(7)));
                prelevement.setOrigine(new BigDecimal(getChamp(7)));
                prelevement.setMontantprelevement(String.valueOf(Long.parseLong(getChamp(16))));
                prelevement.setEtablissement(getChamp(5));
                prelevement.setReference_Emetteur(getChamp(24));
                prelevement.setBanque(getChamp(5));
                prelevement.setAgence(getChamp(5));
                prelevement.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                prelevement.setNom_Beneficiaire(getChamp(30));
                prelevement.setAdresse_Beneficiaire(getChamp(30));
                prelevement.setBanqueremettant(getChamp(5));
                prelevement.setAgenceremettant(getChamp(5));
                prelevement.setNumerocompte_Tire(getChamp(12));
                prelevement.setNom_Tire(getChamp(30));
                prelevement.setAdresse_Tire(getChamp(30));
                prelevement.setLibelle(getChamp(45));
                prelevement.setZoneinterbancaire_Beneficiaire(getChamp(5));
                prelevement.setDateFinRecyclage(getChamp(10));
                prelevement.setNumeroprelevement(getChamp(10));
                
                prelevement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                prelevement.setDevise("XOF");
                prelevement.setDateordre(prelevement.getDatetraitement());
                
                prelevement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                
                prelevement.setType_prelevement("025");
                prelevement.setVille("01");
                prelevement.setIdprelevement(new BigDecimal(Utility.computeCompteur("IDPRELEVEMENT", "PRELEVEMENTS")));
                db.insertObjectAsRowByQuery(prelevement, "PRELEVEMENTS");


            }
            
        }
        db.close();
        return aFile;

    }
}
