/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.readers;

import clearing.table.Comptes;
import java.io.BufferedReader;
import java.io.File;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CompteReader extends FlatFileReader  {

    public CompteReader() {

        setTattooProcessDate(true);
    }

    
    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;
        String action="";
       
        Comptes compte = new Comptes();
        
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is =  openFile(aFile);
            while ((line = is.readLine()) != null) {
        

               setCurrentLine(line);
               action = getChamp(1);
               compte.setNumero(getChamp(12));
               getChamp(1);
               compte.setAgence(getChamp(5));
               getChamp(1);
               compte.setNom(getChamp(24));
               getChamp(1);
               compte.setAdresse1(getChamp(25));
               
               if(action.equalsIgnoreCase("C")){
                    db.insertObjectAsRowByQuery(compte, "COMPTES");
               }
               if(action.equalsIgnoreCase("M")){
                   db.updateRowByObjectByQuery(compte, "COMPTES","NUMERO='"+ compte.getNumero() +"'");
               }
               if(action.equalsIgnoreCase("A")){
                   db.executeUpdate("DELETE FROM COMPTES WHERE NUMERO='"+ compte.getNumero() +"'");
               }
                     
            }
  db.close();
     return aFile;
     
    }

}
