/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package clearing.action.writers.corporates;

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
public class VirementAllerWriter extends FlatFileWriter{

    public VirementAllerWriter() {
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        
        BigDecimal idFichierRemise = new BigDecimal(Utility.computeCompteur("IDFICHIER", "FICHIERS"));
        String sql ="UPDATE VIREMENTS SET REMISE="+ idFichierRemise +" WHERE REMISE IS NULL AND ETAT ="+  Utility.getParam("CETAOPEVALDELTA");
        db.executeUpdate(sql);
       

       
        String dateCompensation = Utility.convertDateToString(new Date(System.currentTimeMillis()), "ddMMyyyy");
        String dateFichier = Utility.convertDateToString(new Date(),"ddMMyyyy");
        String heureFichier =  Utility.convertDateToString(new Date(),"hhmmss");
        String numRemise =  Utility.bourrageGauche(Utility.computeCompteur("ENV_VIR", dateFichier), 7, "0");
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "01-VIR-"+ Utility.getParam("CODE_BANQUE")+"-"+ dateFichier
                          + "-" + heureFichier +".VIR";
        setOut(createFlatFile(fileName));
        
        

        sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" +  Utility.getParam("CETAOPEVALDELTA")  + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        

        if (virements != null && 0 < virements.length) {
        long sumVirements = 0;

            for (int i = 0; i < virements.length; i ++) {
                sumVirements+= Long.parseLong(virements[i].getMontantvirement());

            }
         String line = "VIR";
                line += "015";
                line += Utility.getParam("CODE_BANQUE_SICA3");
                line += dateCompensation;
                line += Utility.bourrageGauche(String.valueOf(sumVirements), 16, "0");
                line += Utility.bourrageGauche(String.valueOf(virements.length), 10, "0") +
                        createBlancs(313, " ");
                
                writeln(line);


            for (int i = 0; i < virements.length; i ++) {
                Virements virement = virements[i];
                //Tous les virements aller
                line = Utility.bourrageGauche(virement.getRemise().toPlainString(), 7,"0");
                line += Utility.bourrageGauche(virement.getIdvirement().toPlainString(), 7,"0");
                line += Utility.bourrageGauche(virement.getMontantvirement(), 16,"0");
                line += Utility.bourrageGauche(virement.getEtablissement(), 5, " ");
                line += Utility.bourrageDroite(virement.getReference_Emetteur(),24," ");

                line += Utility.bourrageGauche(virement.getBanque(), 5, "0");
                line += Utility.bourrageGauche(virement.getAgence(), 5, "0");
                line += Utility.bourrageGauche(virement.getNumerocompte_Beneficiaire(), 12, "0");
                line += Utility.bourrageDroite(virement.getNom_Beneficiaire(),30," ");
                line += Utility.bourrageDroite(virement.getAdresse_Beneficiaire(),30," ");
                
                line += Utility.bourrageGauche(virement.getBanqueremettant(),5,"0");
                line += Utility.bourrageGauche(virement.getAgenceremettant(), 5, "0");
                line += Utility.bourrageGauche(virement.getNumerocompte_Tire(), 12, "0");
                line += Utility.bourrageDroite(virement.getNom_Tire()==null?"BANQUE":virement.getNom_Tire(),30," ");
                line += Utility.bourrageDroite(virement.getAdresse_Tire()==null?"ADRESSE BANQUE":virement.getAdresse_Tire(),30," ");
                
                line +=  Utility.bourrageDroite(virement.getLibelle(),45," ");
                line +=  Utility.bourrageDroite(virement.getZoneinterbancaire_Beneficiaire(),5,"0");
                line +=  Utility.bourrageDroite(virement.getDateordre(),10,"0");
                line +=  Utility.bourrageDroite(virement.getNumerovirement(),10,"0");
                writeln(line);
                if(virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPEVALDELTA"))){
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                    virement.setHeuretraitement(heureFichier);
                    virement.setRemise(new BigDecimal(numRemise));
                    virement.setDatecompensation(dateCompensation);
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                }
            }
          setDescription(getDescription() + " exécuté avec succès:\n Nombre de Virements= " + virements.length + " - Montant Total= " + Utility.formatNumber("" + sumVirements) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Virements= " + virements.length + " - Montant Total= " + Utility.formatNumber("" + sumVirements));  
        }
        closeFile();
        db.close();
    }
}
