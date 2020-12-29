/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.readers.flexcube;

import clearing.table.Comptes;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CompteEBEReader extends FlatFileReader  {

    public CompteEBEReader() {

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
               //getChamp(1);
               compte.setAgence(getChamp(5));
               //getChamp(1);
               compte.setNom(getChamp(25));
               //getChamp(1);
               compte.setAdresse1(getChamp(25));
               compte.setNumcptex(getChamp(16));
               //getChamp(1);
               compte.setSignature1(getChamp(6));
               if(action.equalsIgnoreCase("C")){
                    db.insertObjectAsRowByQuery(compte, "COMPTES");
               }
               if(action.equalsIgnoreCase("M")){
                   db.updateRowByObjectByQuery(compte, "COMPTES","NUMERO='"+ compte.getNumero() +"'");
               }
               if(action.equalsIgnoreCase("A")){
                   compte.setEtat(new BigDecimal(10));
                   db.updateRowByObjectByQuery(compte, "COMPTES","NUMERO='"+ compte.getNumero() +"'");
               }
                     
            }
  db.close();
     return aFile;
     
    }

}
