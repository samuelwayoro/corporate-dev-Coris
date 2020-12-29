/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Comptes;
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
public class VirementETGReader extends FlatFileReader {

    public VirementETGReader() {

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
            virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
            virement.setLibelle(getChamp(50));
            String compte = getChamp(16);
            Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("SELECT * FROM COMPTES where NUMCPTEX  ='" + compte + "'", new Comptes());
            if (comptes != null && comptes.length > 0) {
                virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                virement.setNumerocompte_Tire(comptes[0].getNumero());
                virement.setAgenceremettant(comptes[0].getAgence());
            }else{
                System.out.println("Numero de compte non trouvé = "+ compte );
                logEvent("WARNING", "Numero de compte non trouvé = "+ compte);
                
                continue;
            }
            virement.setBanque(getChamp(5));
            virement.setAgence(getChamp(5));
            virement.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
            virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(16))));
            virement.setNom_Tire(getChamp(35));
            virement.setNom_Beneficiaire(getChamp(35));
            
            virement.setDevise("XOF");
            virement.setDateordre(virement.getDatetraitement());
            virement.setNumerovirement(Utility.bourrageGZero("", 10));
            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));

            //virement.setDatecompensation(Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd"));
            virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
            virement.setType_Virement("015");
            virement.setVille("01");
            virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));
            db.insertObjectAsRowByQuery(virement, "VIREMENTS");

        }
        db.close();
        return aFile;

    }
}
