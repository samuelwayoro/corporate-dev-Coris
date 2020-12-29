/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.readers.flexcube.esn;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class RejetChequeFlexCubeReader extends FlatFileReader {

    public RejetChequeFlexCubeReader() {
        setHasNormalExtension(false);
        setExtensionType(END_EXT);
    }


    @Override
    public File treatFile(File aFile, Repertoires repertoire)throws Exception {
        setFile(aFile);
    
        String line =null, sql = null;
        
        Cheques aCheque = new Cheques();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
         db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

       
        BufferedReader is =  openFile(aFile);
            while ((line = is.readLine()) != null) {
                setCurrentLine(line);
                //aCheque.setDatetraitement(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                aCheque.setMontantcheque(getChamp(16));
                getChamp(16);
                aCheque.setReference_Operation_Interne(getChamp(8));
                aCheque.setMotifrejet(getChamp(3));
                //aCheque.setIdcheque(new BigDecimal(getChamp(18)));

                sql= "UPDATE CHEQUES SET ETAT = "+Utility.getParam("CETAOPEALLICOM2")+
                     ", MOTIFREJET="+ aCheque.getMotifrejet()+ " WHERE Reference_Operation_Interne like '"+ aCheque.getReference_Operation_Interne()+"' AND MONTANTCHEQUE='"+Utility.trimLeadingZero(aCheque.getMontantcheque())
                        +"' AND ETAT IN("+ Utility.getParam("CETAOPERETENVSIB")+","+Utility.getParam("CETAOPERETRECENVSIB")+","+Utility.getParam("CETAOPERETRECENVSIBVER")+","+Utility.getParam("CETAOPEALLICOM2")+")";
        
                db.executeUpdate(sql);
                }

        db.close();
        return aFile;
    }

}
