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
import static org.patware.action.file.FlatFileWriter.createBlancs;

import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class VirementAllerAmplitudeWriter extends FlatFileWriter{

    public VirementAllerAmplitudeWriter() {
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        
        BigDecimal idFichierRemise = new BigDecimal(Utility.computeCompteur("IDFICHIER", "FICHIERS"));
        String sql ="UPDATE VIREMENTS SET REMISE="+ idFichierRemise +" WHERE REMISE IS NULL AND ETAT ="+  Utility.getParam("CETAOPEVALDELTA");
        db.executeUpdate(sql);
       

       
        
        String dateFichier = Utility.convertDateToString(new Date(),"ddMMyyyy");
        String heureFichier =  Utility.convertDateToString(new Date(),"hhmmss");
        
        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + "VIRMULT_KKASH_"+ idFichierRemise +"_"+ dateFichier
                          + "-" + heureFichier +".TXT";
        setOut(createFlatFile(fileName));
        
        

        sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" +  Utility.getParam("CETAOPEVALDELTA")  + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        

        if (virements != null && 0 < virements.length) {
        long sumVirements = 0;

            for (int i = 0; i < virements.length; i ++) {
                sumVirements+= Long.parseLong(virements[i].getMontantvirement());

            }
         String line = "1";
                //line += "KK"+Utility.bourrageDroite(""+idFichierRemise, 6, " ");
                line += "000100  ";
                line += Utility.convertDateToString(new Date(),"ddMMyy");;
                line += Utility.bourrageDroite("KASHKASH", 36, " ");
                line += Utility.getParam("CODE_BANQUE_SICA3");
                line += virements[0].getAgenceremettant();
                line += Utility.bourrageDroite(virements[0].getNumerocompte_Tire(), 16, " ");
                line += Utility.computeCleRIB(Utility.getParam("CODE_BANQUE_SICA3"),virements[0].getAgenceremettant(),virements[0].getNumerocompte_Tire());
                line += Utility.bourrageGZero(""+virements.length, 6);
                line += Utility.bourrageGZero(""+sumVirements, 20);
                line += Utility.bourrageDroite("VIREMENTS KK "+idFichierRemise, 30, " ");
                line += createBlancs(6, " ");
                line +="952";
                
                writeln(line);


            for (int i = 0; i < virements.length; i ++) {
                Virements virement = virements[i];
                //Tous les virements aller
                line = "2";
                line +=  Utility.bourrageDroite(virement.getNumerovirement(),12," ");
                line += Utility.bourrageDroite(virement.getNom_Beneficiaire(),36," ");
                line += createBlancs(24, " ");
                line += Utility.bourrageGauche(virement.getBanque(), 5, "0");
                line += Utility.bourrageGauche(virement.getAgence(), 5, "0");
                line += Utility.bourrageDroite(virement.getNumerocompte_Beneficiaire(), 16, " ");
                line += Utility.computeCleRIB(virement.getBanque(),virement.getAgence(),virement.getNumerocompte_Beneficiaire());
                line += Utility.bourrageGauche(virement.getMontantvirement(), 20,"0");
                line +=  Utility.bourrageDroite(virement.getLibelle(),30," ");
                
                
                writeln(line);
                if(virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPEVALDELTA"))){
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM1")));
                   
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
