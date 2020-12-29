/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package clearing.action.writers.bfi;

import clearing.table.Prelevements;
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
public class ENVPrelevementAllerRejWriter extends FlatFileWriter{

    public ENVPrelevementAllerRejWriter() {
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
                          + "-" + heureFichier +"-20-22-324.ENV";
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT IN (" +  Utility.getParam("CETAOPEALLICOM2")  + ") ";
        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());


        if (prelevements != null && 0 < prelevements.length) {
        long sumVirements = 0;

            for (int i = 0; i < prelevements.length; i ++) {
                sumVirements+= Long.parseLong(prelevements[i].getMontantprelevement());

            }
         String line = "101GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "20";
                line += Utility.getParam("CODE_BANQUE");
                line += dateCompensation+dateCompensation;
                line += numRemise;
                line += "22"+ "324"+"00";
                line += Utility.bourrageGauche(String.valueOf(sumVirements)+"00", 15, "0");
                line += Utility.bourrageGauche(String.valueOf(prelevements.length), 10, "0") +
                        createBlancs(301, " ");

                wwriteln(line);


            for (int i = 0; i < prelevements.length; i ++) {
                Prelevements prelevement = prelevements[i];
                //Tous les prelevements aller
                line = "101GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "22"+ Utility.getParam("CODE_BANQUE");
                line += dateCompensation+dateCompensation;
                line += numRemise;
                line += "22"+"324"+"00"+"0";
                line += Utility.bourrageGauche(prelevement.getMontantprelevement()+"00", 15,"0");
                line += Utility.bourrageGauche(prelevement.getNumeroprelevement(), 7,"0");
                line += Utility.bourrageGauche(prelevement.getAgence(), 3, "0");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");


                line += Utility.bourrageGauche(prelevement.getBanqueremettant(),3,"0");
                line += Utility.bourrageGauche(prelevement.getAgenceremettant(), 3, "0");
                line += Utility.bourrageGauche(prelevement.getNumerocompte_Tire(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(prelevement.getBanqueremettant(), 3, "0"), Utility.bourrageGauche(prelevement.getAgenceremettant(), 3, "0"),Utility.bourrageGauche(prelevement.getNumerocompte_Tire(), 10, "0"));
                line += Utility.bourrageDroite(prelevement.getNom_Tire()==null?"BANQUE":prelevement.getNom_Tire(),30," ");
                line += Utility.bourrageDroite(prelevement.getAdresse_Tire()==null?"ADRESSE BANQUE":prelevement.getAdresse_Tire(),30," ");


                //line +="00";
                line += Utility.bourrageGauche(prelevement.getBanqueremettant(), 3, "0")+"GN";
                line += Utility.bourrageGauche(prelevement.getBanque(), 3, "0");
                line += Utility.bourrageGauche(prelevement.getAgence(), 3, "0");
                line += Utility.bourrageGauche(prelevement.getNumerocompte_Beneficiaire(), 10, "0");
                line += Utility.computeCleRIBACPACH(Utility.bourrageGauche(prelevement.getBanque(), 3, "0"), Utility.bourrageGauche(prelevement.getAgence(), 3, "0"),Utility.bourrageGauche(prelevement.getNumerocompte_Beneficiaire(), 10, "0"));
                line += Utility.bourrageDroite(prelevement.getNom_Beneficiaire(),30," ");
                line += Utility.bourrageDroite(prelevement.getAdresse_Beneficiaire(),30," ");

                line += Utility.bourrageDroite(prelevement.getReference_Emetteur(),20," ");
                line += "00"+ Utility.bourrageDroite(prelevement.getLibelle(),45," ");
                line += createBlancs(15," ");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += Utility.bourrageGauche(prelevement.getMotifrejet(), 8, "0");
                line += createBlancs(33, " ");

                wwriteln(line);
                if(prelevement.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM2"))){
                    prelevement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM2ACC")));
                    prelevement.setHeuretraitement(heureFichier);
                    prelevement.setRemise(new BigDecimal(numRemise));
                    prelevement.setDatecompensation(dateCompensation);
                    db.updateRowByObjectByQuery(prelevement, "VIREMENTS", "IDVIREMENT=" + prelevement.getIdprelevement());
                }
            }

        }
        closeFile();
        db.close();
    }
}
