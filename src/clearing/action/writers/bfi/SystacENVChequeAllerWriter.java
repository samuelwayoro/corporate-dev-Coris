/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.bfi;

import clearing.table.Cheques;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class SystacENVChequeAllerWriter extends FlatFileWriter{

    public SystacENVChequeAllerWriter() {
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
        String dateFichier = Utility.convertDateToString(new Date(),"ddMMyyyy");
        String heureFichier =  Utility.convertDateToString(new Date(),"hhmmss");
        String numRemise =  Utility.bourrageGauche(Utility.computeCompteur("ENV_CHQ", dateFichier), 4, "0");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-CM-"+ Utility.getParam("CODE_BANQUE")+"-"+ dateFichier
                          + "-" + heureFichier +"-30-21-950.ENV";
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" +  Utility.getParam("CETAOPEVAL")  + ") AND BANQUE LIKE '10%'";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        

        if (cheques != null && 0 < cheques.length) {
        long sumCheques = 0;

            for (int i = 0; i < cheques.length; i ++) {
                sumCheques+= Long.parseLong(cheques[i].getMontantcheque());

            }
         String line = "101CM";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
                line += heureFichier;
                line += "30"+ Utility.getParam("CODE_BANQUE");
                line += dateCompensation+dateCompensation;
                line += numRemise+"11"+ "950";
                line += Utility.bourrageGauche(String.valueOf(sumCheques), 15, "0");
                line += Utility.bourrageGauche(String.valueOf(cheques.length), 10, "0") +
                        createBlancs(114, " ");
                
                wwriteln(line);


            for (int i = 0; i < cheques.length; i ++) {
                Cheques cheque = cheques[i];
                //Tous les cheques aller
                line = "101CM";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
                line += heureFichier;
                line += "30"+ Utility.getParam("CODE_BANQUE");
                line += dateCompensation+dateCompensation;
                line += numRemise+"21"+"950";
                line += Utility.bourrageGauche(cheque.getMontantcheque(), 15,"0");
                line += Utility.bourrageGauche(cheque.getNumerocheque(), 7,"0");
                line += cheque.getBanque();
                line += cheque.getAgence();
                line += Utility.bourrageGauche(cheque.getNumerocompte(), 11, "0");
                line += Utility.computeCleRIBSYSTAC(cheque.getBanque(), cheque.getAgence(),Utility.bourrageGauche(cheque.getNumerocompte(), 11, "0"));
                line += cheque.getBanqueremettant();
                line += "CM";
                line += cheque.getBanqueremettant();
                line += cheque.getAgenceremettant();
                line += Utility.bourrageGauche(cheque.getCompteremettant(), 11, "0");
                line += Utility.computeCleRIBSYSTAC(cheque.getBanqueremettant(), cheque.getAgenceremettant(),Utility.bourrageGauche(cheque.getCompteremettant(), 11, "0"));
                line += Utility.bourrageGauche(cheque.getNombeneficiaire().trim().isEmpty()?"BANQUE":cheque.getNombeneficiaire(),30," ");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
                line += createBlancs(8, "0");
                line += createBlancs(9, " ");
                
                wwriteln(line);
                if(cheque.getEtat().toPlainString().equals(Utility.getParam("CETAOPEVAL"))){
                    cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACC")));
                    cheque.setDatecompensation(Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyy/MM/dd"));
                    db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                }
            }
            
        }
        closeFile();
        db.close();
    }
}
