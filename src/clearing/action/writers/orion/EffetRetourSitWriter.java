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
public class EffetRetourSitWriter extends FlatFileWriter{

    public EffetRetourSitWriter() {
        setDescription("Envoi des effets retour vers le SIB");
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
                String line = "<>0320020601";
                line += effet.getBanqueremettant();
                line += createBlancs(2," ")+"1";
                line += effet.getBanque();
                line += createBlancs(2," ");
                line += effet.getAgence()+"XOF2";
                
                line += Utility.bourrageGZero(effet.getMontant_Effet(), 16);
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatecompensation(), ResLoader.getMessages("patternDate")),"yyMMdd");
                line += "0 "+createBlancs(15,"0")+createBlancs(4," ")+"F";
                line += Utility.bourrageGZero(effet.getIdeffet().toPlainString(), 9);
                line += createBlancs(11," ");
                line += Utility.bourrageGZero(effet.getNumeroeffet(), 10);
                //
                line += createBlancs(16," ");//effet.getNumerocompte_Beneficiaire();
                line += effet.getNumerocompte_Tire();
                line += createBlancs(2, " ");
                line += Utility.bourrageDroite(effet.getNom_Tire(),24," ");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")),"yyMMdd");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDate_Creation(), ResLoader.getMessages("patternDate")),"yyMMdd");
                line += Utility.convertDateToString(Utility.convertStringToDate(effet.getDatetraitement(), ResLoader.getMessages("patternDate")),"yyMMdd");
                line += effet.getAgenceremettant();
                line += createBlancs(51," ");
                line += createBlancs(15,"0");
                line += createBlancs(67," ");
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
