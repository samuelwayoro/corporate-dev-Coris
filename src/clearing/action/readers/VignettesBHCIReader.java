/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers;

import clearing.model.CMPUtility;
import clearing.table.Vignettes;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class VignettesBHCIReader extends FlatFileReader {

    public VignettesBHCIReader() {

        setTattooProcessDate(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;
       
        Vignettes vignette = new Vignettes();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        int compteur = 0;
        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            compteur++;
            if (compteur != 1) {
                setCurrentLine(line);

               // vignette.setNumerosequence(getChamp(5));
               // vignette.setNumerocommande(getChamp(4));
               // vignette.setDatecommande(Utility.convertDateToString(Utility.convertStringToDate(getChamp(6), "ddMMyy"), "yyyyMMdd"));
               // vignette.setNumeroserie(getChamp(20));
//                vignette.setPayable1(getChamp(25));
//                vignette.setPayable2(getChamp(25));
//                vignette.setInfoclient1(getChamp(35));
//                vignette.setInfoclient2(getChamp(35));
//                vignette.setCodevignette(getChamp(21));
//
                vignette.setNumerocompte(getChamp(11));
                vignette.setCodeguichet(getChamp(5));
                vignette.setClerib(getChamp(2));
                vignette.setNumerocheque(getChamp(7));
               // vignette.setNumeroendos(getChamp(19));
                vignette.setCodevignette(getChamp(19));
                vignette.setFournisseur("BHCI-AIS");
                vignette.setCodebanque(CMPUtility.getCodeBanqueSica3());
                vignette.setDatecreation(Utility.convertDateToString(new Date(System.currentTimeMillis()),"yyyyMMdd"));
                vignette.setEtat(new BigDecimal(Utility.getParam("CETAVIGSTO")));
                vignette.setIdvignette(new BigDecimal(Utility.computeCompteur("IDVIGNETTE", "VIGNETTES")));
                db.insertObjectAsRowByQuery(vignette, "VIGNETTES");

            }

        }
        db.close();
        return aFile;

    }
}
