/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.bfi;

import clearing.table.Virements;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class SystacENVVirementAllerWriter extends FlatFileWriter{

    public SystacENVVirementAllerWriter() {
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
        String dateFichier = Utility.convertDateToString(new Date(),"ddMMyyyy");
        String heureFichier =  Utility.convertDateToString(new Date(),"hhmmss");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-CM-"+ Utility.getParam("CODE_BANQUE")+"-"+ dateFichier
                          + "-" + heureFichier +"-10-21-950.ENV";
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" +  Utility.getParam("CETAOPEALLICOM1")  + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        

        if (virements != null && 0 < virements.length) {
        long sumVirements = 0;

            for (int i = 0; i < virements.length; i ++) {
                sumVirements+= Long.parseLong(virements[i].getMontantvirement());

            }
         String line = "101CM";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
                line += heureFichier;
                line += "10"+ Utility.getParam("CODE_BANQUE");
                line += dateCompensation+dateCompensation;
                line += "000111"+ "950"+"00";
                line += Utility.bourrageGauche(String.valueOf(sumVirements), 15, "0");
                line += Utility.bourrageGauche(String.valueOf(virements.length), 10, "0") +
                        createBlancs(202, " ");
                
                wwriteln(line);


            for (int i = 0; i < virements.length; i ++) {
                Virements virement = virements[i];
                //Tous les virements aller
                line = "101CM";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
                line += heureFichier;
                line += "10"+ Utility.getParam("CODE_BANQUE");
                line += dateCompensation+dateCompensation;
                line += "000121"+"950"+"00";
                line += Utility.bourrageGauche(virement.getMontantvirement(), 15,"0");
                line += Utility.bourrageGauche(virement.getIdvirement().toPlainString(), 7,"0");
                line += virement.getBanqueremettant();
                line += virement.getAgenceremettant();
                line += Utility.bourrageGauche(virement.getNumerocompte_Tire(), 11, "0");
                line += Utility.computeCleRIBSYSTAC(virement.getBanqueremettant(), virement.getAgenceremettant(),Utility.bourrageGauche(virement.getNumerocompte_Tire(), 11, "0"));
                line += Utility.bourrageGauche(virement.getNom_Tire().trim().isEmpty()?"BACM":virement.getNom_Tire(),30," ");
                line += virement.getBanque()+"CM";
                line += virement.getBanque();
                line += virement.getAgence();
                line += Utility.bourrageGauche(virement.getNumerocompte_Beneficiaire(), 11, "0");
                line += Utility.computeCleRIBSYSTAC(virement.getBanque(), virement.getAgence(),Utility.bourrageGauche(virement.getNumerocompte_Beneficiaire(), 11, "0"));
                line += Utility.bourrageDroite(virement.getNom_Beneficiaire(),30," ");
                line += Utility.bourrageDroite(virement.getReference_Emetteur(),20," ");
                line += "00"+ Utility.bourrageDroite(virement.getLibelle(),45," ");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyyMMdd");
                line += createBlancs(8, "0");
                line += createBlancs(9, " ");

                wwriteln(line);
                if(virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM1"))){
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                }
            }
            
        }
        closeFile();
        db.close();
    }
}
