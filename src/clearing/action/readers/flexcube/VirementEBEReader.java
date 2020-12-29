/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.flexcube;

import clearing.model.CMPUtility;
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
public class VirementEBEReader extends FlatFileReader {

    public VirementEBEReader() {

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
            String typeVirement = getChamp(3);
            if (typeVirement.equalsIgnoreCase("015")) {
                virement.setReference_Emetteur(getChamp(16));
                virement.setBanqueremettant(getChamp(5));
                virement.setAgenceremettant(getChamp(5));
                virement.setNumerocompte_Tire(getChamp(12));
                getChamp(2);
                virement.setBanque(getChamp(5));
                virement.setAgence(getChamp(5));
                virement.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                getChamp(2);
                virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(16))));
                virement.setNom_Tire(getChamp(35));
                virement.setNom_Beneficiaire(getChamp(35));
                virement.setLibelle(getChamp(70));

            }
            if (typeVirement.equalsIgnoreCase("011")) {
                virement.setReference_Emetteur(getChamp(16));
                virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                virement.setAgenceremettant(getChamp(5));

                virement.setBanque(getChamp(5));
                virement.setAgence(getChamp(5));

                virement.setMontantvirement(String.valueOf(Long.parseLong(getChamp(16))));
                virement.setNom_Tire(getChamp(35));
                virement.setNom_Beneficiaire(getChamp(35));
                virement.setLibelle(getChamp(70));

            }
            virement.setDevise("XOF");
            virement.setDateordre(virement.getDatetraitement());
            virement.setNumerovirement(Utility.bourrageGZero(Utility.computeCompteur("NUMVIR", "VIREMENTS"), 10));
            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
            virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
            virement.setType_Virement(typeVirement);
            virement.setVille("01");
            virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));
            db.insertObjectAsRowByQuery(virement, "VIREMENTS");

        }
        db.close();
        return aFile;

    }
}
