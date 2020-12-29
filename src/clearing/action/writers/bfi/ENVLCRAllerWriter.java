/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package clearing.action.writers.bfi;

import clearing.table.Effets;
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
public class ENVLCRAllerWriter extends FlatFileWriter{

    public ENVLCRAllerWriter() {
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());


        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
        String dateFichier = Utility.convertDateToString(new Date(),"ddMMyyyy");
        String heureFichier =  Utility.convertDateToString(new Date(),"hhmmss");
        String numRemise =  Utility.bourrageGauche(Utility.computeCompteur("ENV_EFF", dateFichier), 7, "0");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-GN-"+ Utility.getParam("CODE_BANQUE")+"-"+ dateFichier
                          + "-" + heureFichier +"-40-21-324.ENV";
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM EFFETS WHERE ETAT IN (" +  Utility.getParam("CETAOPEALLICOM1")  + ") ";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        

        if (effets != null && 0 < effets.length) {
        long sumEffets = 0;

            for (int i = 0; i < effets.length; i ++) {
                sumEffets+= Long.parseLong(effets[i].getMontant_Effet());

            }
         String line = "101GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "40"+ Utility.getParam("CODE_BANQUE");
                line += dateCompensation+dateCompensation;
                line +=numRemise;
                line += "11"+ "324"+"00";
                line += Utility.bourrageGauche(String.valueOf(sumEffets), 15, "0");
                line += Utility.bourrageGauche(String.valueOf(effets.length), 10, "0") +
                        createBlancs(401, " ");
                
                wwriteln(line);


            for (int i = 0; i < effets.length; i ++) {
                Effets effet = effets[i];
                //Tous les effets aller
                line = "101GN";
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += heureFichier;
                line += "40"+ Utility.getParam("CODE_BANQUE");
                line += dateCompensation+dateCompensation;
                line += numRemise;
                line += "21"+"324"+"00"+"1";
                line += Utility.bourrageGauche(effet.getMontant_Effet(), 15,"0");
                line += Utility.bourrageGauche(effet.getMontant_Effet(), 15,"0");
                line += Utility.bourrageGauche(effet.getMontant_Frais(), 15,"0");
                line += Utility.bourrageGauche(effet.getIdeffet().toPlainString(), 12,"0");
                line += Utility.bourrageGauche(effet.getAgenceremettant(), 3, "0");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += Utility.bourrageGauche(effet.getBanqueremettant(),3,"0");
                line += Utility.bourrageGauche(effet.getAgenceremettant(),3,"0");
                line += Utility.bourrageGauche(effet.getNumerocompte_Tire(), 10, "0");
                //line += Utility.computeCleRIBACPACH(effet.getBanqueremettant(), effet.getAgenceremettant(),Utility.bourrageGauche(effet.getNumerocompte_Tire(), 10, "0"));
                line +="00";
                line += Utility.bourrageGauche(effet.getBanqueremettant(),3,"0");
                line += Utility.bourrageGauche(effet.getAgenceremettant(),3,"0");
                line += Utility.bourrageGauche(effet.getNumerocompte_Tire(), 10, "0");
                line +="00";
                //line += Utility.computeCleRIBACPACH(effet.getBanqueremettant(), effet.getAgenceremettant(),Utility.bourrageGauche(effet.getNumerocompte_Tire(), 10, "0"));

                line += Utility.bourrageGauche(effet.getBanque(),3,"0")+"GM";
                line += Utility.bourrageGauche(effet.getBanque(),3,"0");
                line += Utility.bourrageGauche(effet.getAgence(),3,"0");
                line += Utility.bourrageGauche(effet.getNumerocompte_Beneficiaire(), 10, "0");
                //line += Utility.computeCleRIBACPACH(effet.getBanque(), effet.getAgence(),Utility.bourrageGauche(effet.getNumerocompte_Beneficiaire(), 10, "0"));
                line +="00";
                line += Utility.bourrageDroite(effet.getNom_Beneficiaire(),30," ");
                line += Utility.bourrageDroite(effet.getAdresse_Beneficiaire(),30," ");
                line += Utility.bourrageDroite(effet.getNom_Tire()==null?"BANQUE":effet.getNom_Tire(),30," ");
                line += Utility.bourrageDroite(effet.getAdresse_Tire()==null?"ADRESSE BANQUE":effet.getAdresse_Tire(),30," ");

                line += createBlancs(30," ");
                line += "00"+Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Echeance(),"yyyy/MM/dd" ), "ddMMyyyy");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Echeance(), "yyyy/MM/dd"), "ddMMyyyy");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Creation(), "yyyy/MM/dd"), "ddMMyyyy");
                line += createBlancs(75," ");
                line += Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
                line += createBlancs(8, "0");
                line += createBlancs(31, " ");

                wwriteln(line);
                if(effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPEALLICOM1"))){
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1ACCENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }
            }
            
        }
        closeFile();
        db.close();
    }
}
