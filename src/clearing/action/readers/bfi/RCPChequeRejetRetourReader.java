/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.bfi;


import clearing.table.Cheques;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;

import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class RCPChequeRejetRetourReader extends FlatFileReader {

    public RCPChequeRejetRetourReader() {
         setHasNormalExtension(false);
          setExtensionType(END_EXT);
    }

  

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        String line = null;

        //Virements virement = new Virements();
        Cheques cheque = new Cheques();
        

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        int compteur = 0;
        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            compteur++;
            if(compteur != 1){
                getChamp(1);
                    getChamp(2);
                    getChamp(2);
                    getChamp(8);
                    getChamp(6);
                    cheque.setType_Cheque(getChamp(2));
                    cheque.setBanque(getChamp(5));
                    cheque.setDatecompensation(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    getChamp(8);
                    getChamp(4);
                    getChamp(2);
                    cheque.setDevise(getChamp(3));
                    cheque.setDevise("GNF");
                    //getChamp(2);
                    cheque.setMontantcheque(String.valueOf(Long.parseLong(getChamp(15))));
                    cheque.setNumerocheque(getChamp(7));
                    cheque.setBanque(getChamp(5));
                    cheque.setAgence(getChamp(5));
                    cheque.setNumerocompte(getChamp(11));
                    cheque.setRibcompte(getChamp(2));
                    cheque.setBanqueremettant(getChamp(5));
                    getChamp(2);//Code Pays
                    cheque.setBanqueremettant(getChamp(5));
                    cheque.setAgenceremettant(getChamp(5));
                    cheque.setCompteremettant(getChamp(11));
                    cheque.setVilleremettant(getChamp(2));
                    cheque.setNombeneficiaire(getChamp(30));
                    cheque.setDateemission(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    getChamp(8);//Date de valeur
                    getChamp(6);//Motif rejet
                    cheque.setMotifrejet(getChamp(2));
                    getChamp(2);
                    getChamp(10);
                    
                    
                    cheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    cheque.setOrigine(new BigDecimal(0));
                    cheque.setEtablissement(Utility.getParam("CODE_BANQUE"));


                    String sql = "SELECT * FROM CHEQUES WHERE DATEEMISSION='" +cheque.getDateemission()+
                            "' AND BANQUE ='" + cheque.getBanque() +
                            "' AND NUMEROCHEQUE ='" + cheque.getNumerocheque() +
                            "' AND NUMEROCOMPTE ='" + cheque.getNumerocompte() +
                            "' AND MONTANTCHEQUE ='" + cheque.getMontantcheque() + "' AND ETAT =" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") +
                            "";
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                    if (cheques != null && cheques.length > 0) {
                        cheques[0].setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEREJRET"))));
                        cheques[0].setMotifrejet(cheque.getMotifrejet());
                        sql = " IDCHEQUE =" + cheques[0].getIdcheque();
                        db.updateRowByObjectByQuery(cheques[0], "CHEQUES", sql);
                    }

                    

            }
                    
                }

            
        
        db.close();

        
        return aFile;
    }
}
