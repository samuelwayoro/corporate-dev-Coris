/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.finacle;

import clearing.model.CMPUtility;
import clearing.table.Effets;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class EffetRetourNewFinWriter extends FlatFileWriter{

    public EffetRetourNewFinWriter() {
        setDescription("Envoi des chèques retour vers le SIB");
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_OUT_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM EFFETS WHERE ETAT IN (" +  Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        int j = 0;
         long montantTotal = 0;
        if (effets != null && 0 < effets.length) {

            for (int i = 0; i < effets.length; i ++) {
                Effets effet = effets[i];
                //Tous les effets retours
                String line = "";
                //line += Utility.bourrageGZero(effet.getAgenceremettant().substring(0, 2), 3)+ effet.getBanqueremettant().substring(2) + effet.getAgenceremettant().substring(2); //sort code remettant
                line += Utility.bourrageGZero(CMPUtility.getAgencePrincipale(effet.getBanqueremettant()).substring(0, 2), 3)+ effet.getBanqueremettant().substring(2) + CMPUtility.getAgencePrincipale(effet.getBanqueremettant()).substring(2); //sort code remettant
                line += Utility.getParam("SORTCODE");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatecompensation(), ResLoader.getMessages("patternDate")),"ddMMyy");
                line += Utility.bourrageGZero(effet.getMontant_Effet(), 11)+"00";
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 8);//
                line += Utility.bourrageGZero(effet.getNumeroeffet(), 10);
                line += createBlancs(2," ");
                line += effet.getNumerocompte_Tire();
                line += createBlancs(1," ")+"  ";
                line += createBlancs(8," ");
                line += Utility.bourrageDroite(effet.getAgenceremettant(), 6, " ");
                line += Utility.bourrageDroite(effet.getBanqueremettant(), 6, " ");
                line += Utility.bourrageDroite(effet.getAgence(), 6, " ");
                line += Utility.bourrageDroite(effet.getBanque(), 6, " ");
                line += createBlancs(100," ");
                line += Utility.bourrageDroite(effet.getIdeffet().toPlainString(), 50," ");
                
                writeln(line);
                if(effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))){
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }else{
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                }
                montantTotal += Long.parseLong(effet.getMontant_Effet());
            }
            
        setDescription(getDescription()+" exécuté avec succès: Nombre d'effets= "+effets.length +" - Montant Total= "+montantTotal);
            logEvent("INFO", "Nombre d'effets= "+effets.length +" - Montant Total= "+montantTotal);
        }else {
            setDescription(getDescription()+": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }
}
