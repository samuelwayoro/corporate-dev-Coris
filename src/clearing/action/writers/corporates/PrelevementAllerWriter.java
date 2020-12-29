/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package clearing.action.writers.corporates;

import clearing.table.Prelevements;
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
public class PrelevementAllerWriter extends FlatFileWriter{

    public PrelevementAllerWriter() {
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        
        BigDecimal idFichierRemise = new BigDecimal(Utility.computeCompteur("IDFICHIER", "FICHIERS"));
        String sql ="UPDATE PRELEVEMENTS SET REMISE="+ idFichierRemise +" WHERE REMISE IS NULL AND ETAT="+  Utility.getParam("CETAOPEVALDELTA");
        db.executeUpdate(sql);
        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
        String dateFichier = Utility.convertDateToString(new Date(),"ddMMyyyy");
        String heureFichier =  Utility.convertDateToString(new Date(),"hhmmss");
        String numRemise =  Utility.bourrageGauche(Utility.computeCompteur("ENV_PREL", dateFichier), 7, "0");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-PREL-"+ Utility.getParam("CODE_BANQUE")+"-"+ dateFichier
                          + "-" + heureFichier +".PREL";
        setOut(createFlatFile(fileName));
        
        

        sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT IN (" +  Utility.getParam("CETAOPEVALDELTA")  + ") ";
        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
        

        if (prelevements != null && 0 < prelevements.length) {
        long sumPrelevements = 0;

            for (int i = 0; i < prelevements.length; i ++) {
                sumPrelevements+= Long.parseLong(prelevements[i].getMontantprelevement());

            }
         String line = "PREL";
                line += "025";
                line += Utility.getParam("CODE_BANQUE_SICA3");
                line += dateCompensation;
                line += Utility.bourrageGauche(String.valueOf(sumPrelevements), 16, "0");
                line += Utility.bourrageGauche(String.valueOf(prelevements.length), 10, "0") +
                        createBlancs(313, " ");
                
                writeln(line);


            for (int i = 0; i < prelevements.length; i ++) {
                Prelevements prelevement = prelevements[i];
                //Tous les prelevements aller
                line = Utility.bourrageGauche(prelevement.getRemise().toPlainString(), 7,"0");
                line += Utility.bourrageGauche(prelevement.getIdprelevement().toPlainString(), 7,"0");
                line += Utility.bourrageGauche(prelevement.getMontantprelevement(), 16,"0");
                line += Utility.bourrageGauche(prelevement.getEtablissement(), 5, " ");
                line += Utility.bourrageDroite(prelevement.getReference_Emetteur(),24," ");

                line += Utility.bourrageGauche(prelevement.getBanque(), 5, "0");
                line += Utility.bourrageGauche(prelevement.getAgence(), 5, "0");
                line += Utility.bourrageGauche(prelevement.getNumerocompte_Beneficiaire(), 12, "0");
                line += Utility.bourrageDroite(prelevement.getNom_Beneficiaire(),30," ");
                line += Utility.bourrageDroite(prelevement.getAdresse_Beneficiaire(),30," ");
                
                line += Utility.bourrageGauche(prelevement.getBanqueremettant(),5,"0");
                line += Utility.bourrageGauche(prelevement.getAgenceremettant(), 5, "0");
                line += Utility.bourrageGauche(prelevement.getNumerocompte_Tire(), 12, "0");
                line += Utility.bourrageDroite(prelevement.getNom_Tire()==null?"BANQUE":prelevement.getNom_Tire(),30," ");
                line += Utility.bourrageDroite(prelevement.getAdresse_Tire()==null?"ADRESSE BANQUE":prelevement.getAdresse_Tire(),30," ");
                
                line +=  Utility.bourrageDroite(prelevement.getLibelle(),45," ");
                line +=  Utility.bourrageDroite(prelevement.getZoneinterbancaire_Beneficiaire(),5,"0");
                line +=  Utility.bourrageDroite(prelevement.getDateFinRecyclage(),10,"0");
                line +=  Utility.bourrageDroite(prelevement.getNumeroprelevement(),10,"0");
                writeln(line);
                if(prelevement.getEtat().toPlainString().equals(Utility.getParam("CETAOPEVALDELTA"))){
                    prelevement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                    //prelevement.setHeuretraitement(heureFichier);
                    prelevement.setRemise(new BigDecimal(numRemise));
                    //prelevement.setDatecompensation(dateCompensation);
                    db.updateRowByObjectByQuery(prelevement, "PRELEVEMENTS", "IDPRELEVEMENT=" + prelevement.getIdprelevement());
                }
            }
          setDescription(getDescription() + " exécuté avec succès:\n Nombre de Prelevements= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + sumPrelevements) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Prelevements= " + prelevements.length + " - Montant Total= " + Utility.formatNumber("" + sumPrelevements));  
        }
        closeFile();
        db.close();
    }
}
