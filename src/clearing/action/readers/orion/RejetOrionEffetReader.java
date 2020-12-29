/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.readers.orion;

import clearing.table.Effets;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class RejetOrionEffetReader extends FlatFileReader {
    
    @Override
    public File treatFile(File aFile, Repertoires repertoire)throws Exception {
        setFile(aFile);
    
        String line =null, sql = null;
        
        Effets aEffet = new Effets();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
         db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

       
        BufferedReader is =  openFile(aFile);
            while ((line = is.readLine()) != null) {
                setCurrentLine(line);
                //aCheque.setDatetraitement(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                getChamp(8);
                if(getChamp(3).equalsIgnoreCase("460")){

                getChamp(71);
                aEffet.setMotifrejet(getChamp(3));
                getChamp(129);
                aEffet.setIdeffet(new BigDecimal(getChamp(5)));

                sql= "UPDATE EFFETS SET ETAT = "+Utility.getParam("CETAOPEALLICOM2")+
                     " MOTIFREJET="+ aEffet.getMotifrejet()+ " WHERE IDEFFET="+ aEffet.getIdeffet();

                db.executeUpdate(sql);
                }
                
                }

        db.close();
        return aFile;

    }

}
