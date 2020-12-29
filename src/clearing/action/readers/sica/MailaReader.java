/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.sica;

import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class MailaReader extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);
        String name = Utility.removeFileNameSuffixe(aFile.getName(), "MAILA");
        String sql;
        if (name.contains("IMC"))
        {
           sql = "UPDATE CHEQUES SET ETATIMAGE="+Utility.getParam("CETAIMAENVREC") + " WHERE FICHIERMAILI LIKE '%"+ name +"%'";
          
        }
        else 
        {  
           sql = "UPDATE EFFETS SET ETATIMAGE="+Utility.getParam("CETAIMAENVREC") + " WHERE FICHIERMAILI LIKE '%"+ name +"%'";
        
        }
        
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        db.executeUpdate(sql);
        db.close();


        return aFile;

    }
}
