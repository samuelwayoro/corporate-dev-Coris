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
public class ENVVirementAllerAnnWriter extends FlatFileWriter{

    public ENVVirementAllerAnnWriter() {
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
        String dateFichier = Utility.convertDateToString(new Date(),"ddMMyyyy");
        String heureFichier =  Utility.convertDateToString(new Date(),"hhmmss");
        String numRemise =  Utility.bourrageGauche(Utility.computeCompteur("ENV_VIR", dateFichier), 7, "0");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-GN-"+ Utility.getParam("CODE_BANQUE")+"-"+ dateFichier
                          + "-" + heureFichier +"-10-23-324.ENV";
        setOut(createFlatFile(fileName));


        String sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" +  Utility.getParam("CETAOPEALLICOM3")  + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        

        if (virements != null && 0 < virements.length) {
        long sumVirements = 0;

        
            for (int i = 0; i < virements.length; i ++) {
                sumVirements+= Long.parseLong(virements[i].getMontantvirement());

            }
         String line = "101GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "10";
                line += Utility.getParam("CODE_BANQUE");
                line += dateCompensation+dateCompensation;
                line += numRemise;
                line += "13"+ "324"+"00";
                line += Utility.bourrageGauche(String.valueOf(sumVirements), 15, "0");
                line += Utility.bourrageGauche(String.valueOf(virements.length), 10, "0") +
                        createBlancs(301, " ");
                
                wwriteln(line);


            for (int i = 0; i < virements.length; i ++) {
                Virements virement = virements[i];
                //Tous les virements aller
                line = "101GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += virements[i].getHeuretraitement().trim();
                line += "10"+ Utility.getParam("CODE_BANQUE");
                line += virements[i].getDatecompensation().trim()+virements[i].getDatecompensation().trim();
                line += Utility.bourrageGauche(virements[i].getRemise().toPlainString(), 7, "0");
                line += "23"+"324"+"00"+"0";
                line += Utility.bourrageGauche(virement.getMontantvirement(), 15,"0");
                line += Utility.bourrageGauche(virement.getIdvirement().toPlainString(), 7,"0");
                line += Utility.bourrageGauche(virement.getAgenceremettant(), 3, "0");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += Utility.bourrageGauche(virement.getBanqueremettant(),3,"0");
                line += Utility.bourrageGauche(virement.getAgenceremettant(), 3, "0");
                line += Utility.bourrageGauche(virement.getNumerocompte_Tire(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(virement.getBanqueremettant(), 3, "0"), Utility.bourrageGauche(virement.getAgenceremettant(), 3, "0"),Utility.bourrageGauche(virement.getNumerocompte_Tire(), 10, "0"));
                //line +="00";
                line += Utility.bourrageDroite(virement.getNom_Tire()==null?"BANQUE":virement.getNom_Tire(),30," ");
                line += Utility.bourrageDroite(virement.getAdresse_Tire()==null?"ADRESSE BANQUE":virement.getAdresse_Tire(),30," ");
                line += Utility.bourrageGauche(virement.getBanque(), 3, "0")+"GN";
                line += Utility.bourrageGauche(virement.getBanque(), 3, "0");
                line += Utility.bourrageGauche(virement.getAgence(), 3, "0");
                line += Utility.bourrageGauche(virement.getNumerocompte_Beneficiaire(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(virement.getBanque(), 3, "0"), Utility.bourrageGauche(virement.getAgence(), 3, "0"),Utility.bourrageGauche(virement.getNumerocompte_Beneficiaire(), 10, "0"));
                //line +="00";
                line += Utility.bourrageDroite(virement.getNom_Beneficiaire(),30," ");
                line += Utility.bourrageDroite(virement.getAdresse_Beneficiaire(),30," ");
                line += Utility.bourrageDroite(virement.getReference_Emetteur(),20," ");
                line += "00"+ Utility.bourrageDroite(virement.getLibelle(),45," ");
                line += createBlancs(15," ");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += createBlancs(8, "0");
                line += createBlancs(33, " ");

                wwriteln(line);
                if(virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM3"))){
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM3ACC")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                }
            }
            
        }
        closeFile();
        db.close();
    }
}
