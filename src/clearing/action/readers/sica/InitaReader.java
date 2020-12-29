/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.sica;

import clearing.model.EnteteRemise;
import org.patware.bean.table.Params;
import java.io.BufferedReader;
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
public class InitaReader extends FlatFileReader  {

    public InitaReader() {
        setCopyOriginalFile(true);
    }


    
    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;


        BufferedReader is = openFile(aFile);
        EnteteRemise enteteRemise = new EnteteRemise();


        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            if(Utility.getParam("VERSION_SICA").equals("2")){
            
                if (line.startsWith("EINI")) {
                enteteRemise.setIdEntete(getChamp(4));
                enteteRemise.setIdRecepeteur(getChamp(5));
                enteteRemise.setRefRemise(getChamp(3));
                enteteRemise.setDatePresentation(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                enteteRemise.setIdEmetteur(getChamp(5));
                enteteRemise.setDevise(getChamp(3));
                enteteRemise.setTypeRemise(getChamp(5));
                enteteRemise.setRefRemRelatif(getChamp(3));
                enteteRemise.setCodeRejet(getChamp(2));
                getChamp(12);

                } else if (line.startsWith("FINI")) {


                }
            }else{
                
                if (line.startsWith("EINI")) {
                enteteRemise.setIdEntete(getChamp(4));
                enteteRemise.setIdRecepeteur(getChamp(5));
                enteteRemise.setRefRemise(getChamp(3));
                enteteRemise.setDatePresentation(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                enteteRemise.setIdEmetteur(getChamp(5));
                enteteRemise.setDevise(getChamp(3));
                enteteRemise.setTypeRemise(getChamp(5));
                enteteRemise.setRefRemRelatif(getChamp(3));
                
                getChamp(18);

                } else if (line.startsWith("ESTA")) {
                   getChamp(4);
                   
                   DataBase db = new DataBase(JDBCXmlReader.getDriver());
                   db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
                   Params param = new Params();
                   param.setNom(getChamp(3));
                   param.setValeur(getChamp(1));
                   String params = Utility.getParam(param.getNom());
                   String sql = "";
                    if (params != null) {
                        sql = "UPDATE PARAMS SET VALEUR='" + param.getValeur() + "' WHERE NOM='" + param.getNom() + "'";
                        db.executeUpdate(sql);
                    } else {
                        db.insertObjectAsRowByQuery(param, "PARAMS");
                    } 

                   //Utility.clearParamsCache();
                   db.close();
                   
                }else if (line.startsWith("EIND")) {
                   getChamp(4);
                   
                   DataBase db = new DataBase(JDBCXmlReader.getDriver());
                   db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
                   Params param = new Params();
                   param.setNom(getChamp(3));
                   param.setValeur(getChamp(1));
                   String params = Utility.getParam(param.getNom());
                   String sql = "";
                    if (params != null) {
                        sql = "UPDATE PARAMS SET VALEUR='" + param.getValeur() + "' WHERE NOM='" + param.getNom() + "'";
                        db.executeUpdate(sql);
                    } else {
                        db.insertObjectAsRowByQuery(param, "PARAMS");
                    } 

                   //Utility.clearParamsCache();
                   db.close();
                   
                }
            }
            
        }

        System.out.println(enteteRemise.toString());

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        Params param = new Params();

        param.setValeur(Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd"));

        if (enteteRemise.getIdRecepeteur().equalsIgnoreCase("KSCSR") || enteteRemise.getIdRecepeteur().equalsIgnoreCase("SNSSR") ) {
            param.setNom("DATECOMPENS_SRG");
        } else {
            param.setNom("DATECOMPENS_NAT");
        }

        String params = Utility.getParam(param.getNom());
        String sql = "";
        if (params != null) {
            sql = "UPDATE PARAMS SET VALEUR='" + param.getValeur() + "' WHERE NOM='" + param.getNom() + "'";
            db.executeUpdate(sql);
        } else {
            db.insertObjectAsRowByQuery(param, "PARAMS");
        }
        Utility.clearParamsCache();
        db.close();

        return aFile;

    }
}
