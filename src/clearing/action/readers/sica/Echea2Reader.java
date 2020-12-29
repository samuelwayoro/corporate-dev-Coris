/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.sica;

import clearing.model.CMPUtility;
import org.patware.action.file.FlatFileReader;
import clearing.model.Enreg;
import clearing.model.EnteteBloc;
import clearing.model.EnteteRemise;
import clearing.table.Echeadetails;
import clearing.table.Echeancier;
import clearing.table.Remcom;
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
public class Echea2Reader extends FlatFileReader  {

    public Echea2Reader() {
        setCopyOriginalFile(true);
    }


    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;
        BufferedReader is = openFile(aFile);

        EnteteRemise enteteRemise = new EnteteRemise();


        int cptLot = -1;
        int cptEnreg = -1;
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
                enteteRemise.setNbLots(getChamp(2));
                enteteRemise.setSeance(getChamp(1));
                enteteRemise.setFlagInversion(getChamp(1));
                getChamp(27);
                enteteRemise.enteteBlocs = new EnteteBloc[Integer.parseInt(enteteRemise.getNbLots())];
            } else if (line.startsWith("BECH")) {
                cptEnreg = -1;
                enteteRemise.enteteBlocs[++cptLot] = new EnteteBloc();
                enteteRemise.enteteBlocs[cptLot].setIdEntete(getChamp(4));
                enteteRemise.enteteBlocs[cptLot].setDateReglement(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                enteteRemise.enteteBlocs[cptLot].setNbEnreg(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].setSolde(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].setSigne(getChamp(1));
                getChamp(19);
                enteteRemise.enteteBlocs[cptLot].enregs = new Enreg[Integer.parseInt(enteteRemise.enteteBlocs[cptLot].getNbEnreg())];
            } else if (line.startsWith("FECH")) {

            } else /* Lecture enregistrement detail*/ {
                //Commun

                enteteRemise.enteteBlocs[cptLot].enregs[++cptEnreg] = new Enreg();
                enteteRemise.enteteBlocs[cptLot].enregs[cptEnreg].setTypeOperation(getChamp(3));
                enteteRemise.enteteBlocs[cptLot].enregs[cptEnreg].setIdBanCon(getChamp(5));
                enteteRemise.enteteBlocs[cptLot].enregs[cptEnreg].setDateReglement(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"));
                enteteRemise.enteteBlocs[cptLot].enregs[cptEnreg].setNbTotOperEmis(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].enregs[cptEnreg].setMntTotOperEmis(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].enregs[cptEnreg].setNbTotOperRecus(getChamp(16));
                enteteRemise.enteteBlocs[cptLot].enregs[cptEnreg].setMntTotOperRecus(getChamp(16));
                getChamp(20);

            }

        }

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
       // System.out.println(enteteRemise.toString());
        Remcom remcom = CMPUtility.insertRemcom(enteteRemise, 0);
        Echeancier echeancier = new Echeancier();
        Echeadetails echeadetails = new Echeadetails();

        for (int i = 0; i < enteteRemise.enteteBlocs.length; i++) {
            EnteteBloc enteteLot1 = enteteRemise.enteteBlocs[i];
            
            echeancier.setIdecheancier(new BigDecimal(Long.parseLong(Utility.computeCompteur("IDECHEANCIER", "ECHEANCIER"))));
            echeancier.setDatereglement(new java.sql.Timestamp(enteteLot1.getDateReglement().getTime()));
            echeancier.setIdremcom(remcom.getIdremcom());
            echeancier.setSigne(enteteLot1.getSigne());
            echeancier.setSolde(""+Long.parseLong(enteteLot1.getSolde()));
            db.insertObjectAsRowByQuery(echeancier, "ECHEANCIER");
            for (int j = 0; j < enteteLot1.enregs.length; j++) {
                
                echeadetails.setDatereglement(new java.sql.Timestamp(enteteLot1.enregs[j].getDateReglement().getTime()));
                echeadetails.setIdbancon(enteteLot1.enregs[j].getIdBanCon());
                echeadetails.setTypeoperation(enteteLot1.enregs[j].getTypeOperation());
                echeadetails.setMnttotoperecus(""+Long.parseLong(enteteLot1.enregs[j].getMntTotOperRecus()));
                echeadetails.setMnttotoperemis(""+Long.parseLong(enteteLot1.enregs[j].getMntTotOperEmis()));
                echeadetails.setNbtotoperecus(new BigDecimal(Integer.parseInt(enteteLot1.enregs[j].getNbTotOperRecus())));
                echeadetails.setNbtotoperemis(new BigDecimal(Integer.parseInt(enteteLot1.enregs[j].getNbTotOperEmis())));
                
                echeadetails.setIdecheancier(echeancier.getIdecheancier());
                
                db.insertObjectAsRowByQuery(echeadetails, "ECHEADETAILS");
                


            }

        }

        db.close();
        return aFile;
    }
}
