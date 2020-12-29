/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.orion;

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
public class EffetAllerRejeteSitWriter extends FlatFileWriter {

    public EffetAllerRejeteSitWriter() {
        setDescription("Envoi des rejets effet vers le SIB");
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("EFF_REJ_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM EFFETS WHERE ETAT IN (" +  Utility.getParam("CETAOPEREJRET")  + ") ";
        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
        int j = 0;
        long montantTotal = 0;
        if (effets != null && 0 < effets.length) {

            for (int i = 0; i < effets.length; i ++) {
                Effets effet = effets[i];
                //Tous les effets retours rejetes
                String line = "<>0448024602";
                line += effet.getAgence();
                line += createBlancs(2," ")+"2";
                line += effet.getBanqueremettant();
                line += createBlancs(7," ")+"XOF2";
                line += Utility.bourrageGZero(effet.getMontant_Effet(), 16);
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatesaisie(), ResLoader.getMessages("patternDate")),"yyMMdd");
                line += "0"+createBlancs(7," ")+"0"+createBlancs(2," ")+createBlancs(6,"0")+createBlancs(4, "b")+createBlancs(1," ");
                line += Utility.getParam(effet.getMotifrejet()).trim();
                line += createBlancs(2," ");
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 7);
                line += createBlancs(29," ");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatecompensation(), ResLoader.getMessages("patternDate")),"yyyyMMdd");
                line += Utility.bourrageGZero(effet.getNumeroeffet(), 7);
                line += createBlancs(48," ");
                line += Utility.bourrageGauche(effet.getNumerocompte_Tire(),11,"0");
                line += createBlancs(367," ");
                line += Utility.bourrageGZero(effet.getNumerocompte_Beneficiaire(),12);
                
                writeln(line);
                if(effet.getEtat().toPlainString().equals(Utility.getParam("CETAOPEREJRET"))){
                    effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEREJRETENVSIB")));
                    db.updateRowByObjectByQuery(effet, "effets", "IDEFFET=" + effet.getIdeffet());
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
