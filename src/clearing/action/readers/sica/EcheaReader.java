/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.sica;

import clearing.model.CMPUtility;
import org.patware.action.file.FlatFileReader;
import clearing.model.EnteteBloc;
import clearing.model.EnteteRemise;
import clearing.table.Echeancier;
import clearing.table.Remcom;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.log.SmartFileLogger;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class EcheaReader extends FlatFileReader  {

    public File treatFile(File aFile, Repertoires repertoire) throws Exception {

        setFile(aFile);

        String line = null;
        BufferedReader is = openFile(aFile);

        EnteteRemise enteteRemise = new EnteteRemise();


        int cptLot = -1;
       
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            if (line.startsWith("EECH")) {
                enteteRemise.setIdEntete(getChamp(4));
                enteteRemise.setIdEmetteur(getChamp(5));
                enteteRemise.setRefRemise(getChamp(3));
                enteteRemise.setDatePresentation(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                enteteRemise.setIdRecepeteur(getChamp(5));
                enteteRemise.setDevise(getChamp(3));
                enteteRemise.setTypeRemise(getChamp(5));

                enteteRemise.enteteBlocs = new EnteteBloc[5];
                enteteRemise.enteteBlocs[++cptLot] = new EnteteBloc();
                enteteRemise.enteteBlocs[cptLot].setDateReglement(enteteRemise.getDatePresentation());
                enteteRemise.enteteBlocs[cptLot].setSolde(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].setSigne(getChamp(1));
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(enteteRemise.getDatePresentation());
                cal.add(Calendar.DAY_OF_MONTH, 1);
                enteteRemise.enteteBlocs[++cptLot] = new EnteteBloc();
                enteteRemise.enteteBlocs[cptLot].setDateReglement(new Date(cal.getTimeInMillis()));
                enteteRemise.enteteBlocs[cptLot].setSolde(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].setSigne(getChamp(1));
                cal.setTime(enteteRemise.getDatePresentation());
                cal.add(Calendar.DAY_OF_MONTH, 2);
                enteteRemise.enteteBlocs[++cptLot] = new EnteteBloc();
                enteteRemise.enteteBlocs[cptLot].setDateReglement(new Date(cal.getTimeInMillis()));
                enteteRemise.enteteBlocs[cptLot].setSolde(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].setSigne(getChamp(1));
                cal.setTime(enteteRemise.getDatePresentation());
                cal.add(Calendar.DAY_OF_MONTH, 3);
                enteteRemise.enteteBlocs[++cptLot] = new EnteteBloc();
                enteteRemise.enteteBlocs[cptLot].setDateReglement(new Date(cal.getTimeInMillis()));
                enteteRemise.enteteBlocs[cptLot].setSolde(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].setSigne(getChamp(1));
                cal.setTime(enteteRemise.getDatePresentation());
                cal.add(Calendar.DAY_OF_MONTH, 4);
                enteteRemise.enteteBlocs[++cptLot] = new EnteteBloc();
                enteteRemise.enteteBlocs[cptLot].setDateReglement(new Date(cal.getTimeInMillis()));
                enteteRemise.enteteBlocs[cptLot].setSolde(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].setSigne(getChamp(1));
                enteteRemise.setSeance(getChamp(1));
                
             
            } else if (line.startsWith("FECH")) {

            } 

        }
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        System.out.println(enteteRemise.toString());
        Remcom remcom = CMPUtility.insertRemcom(enteteRemise, 0);
        Echeancier echeancier = new Echeancier();
       

        for (int i = 0; i < enteteRemise.enteteBlocs.length; i++) {
            EnteteBloc enteteLot1 = enteteRemise.enteteBlocs[i];
            System.out.println(enteteLot1.toString());
            echeancier.setIdecheancier(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDECHEANCIER", "ECHEANCIER"))));
            echeancier.setDatereglement(new java.sql.Timestamp(enteteLot1.getDateReglement().getTime()));
            echeancier.setIdremcom(remcom.getIdremcom());
            echeancier.setSigne(enteteLot1.getSigne());
            echeancier.setSolde(enteteLot1.getSolde());
            db.insertObjectAsRowByQuery(echeancier, "ECHEANCIER");
            
        }

        db.close();


        return aFile;
    }
}
