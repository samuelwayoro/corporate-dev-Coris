/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.orion;

import clearing.model.CMPUtility;
import clearing.table.Prelevements;
import java.io.File;
import java.math.BigDecimal;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class PrelevementRetourSitWriter extends FlatFileWriter{

    public PrelevementRetourSitWriter() {
        setDescription("Envoi des Prelevements retour vers le SIB");
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("PREL_OUT_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT IN (" +  Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
        int j = 0;
        long montantTotal = 0;
        if (prelevements != null && 0 < prelevements.length) {

            for (int i = 0; i < prelevements.length; i ++) {
                Prelevements prelevement = prelevements[i];
                //Tous les prelevements retours
                String line = "<>0320021801";
                line += prelevement.getBanqueremettant();
                line += createBlancs(2," ")+"1";
                line += prelevement.getBanque();
                line += createBlancs(2," ");
                line += prelevement.getAgence()+"XOF2";
                
                line += Utility.bourrageGZero(prelevement.getMontantprelevement(), 16);
                line += Utility.convertDateToString(Utility.convertStringToDate(prelevement.getDatecompensation(), ResLoader.getMessages("patternDate")),"yyMMdd");
                line += "0 "+createBlancs(15,"0")+createBlancs(4," ")+"F";
                line += createBlancs(4," ")+Utility.bourrageGZero(prelevement.getIdprelevement().toPlainString(), 16);
                line += Utility.bourrageGZero(prelevement.getNumeroprelevement(), 10);
                line += prelevement.getAgenceremettant();
                //
                line += prelevement.getNumerocompte_Beneficiaire().substring(1);
                line += prelevement.getNumerocompte_Tire().substring(1);
                line += createBlancs(2, " ");
                line += Utility.bourrageDroite(prelevement.getNom_Beneficiaire(),24," ");
                line += Utility.convertDateToString(Utility.convertStringToDate(prelevement.getDatetraitement(), ResLoader.getMessages("patternDate")),"yyMMdd");
                line += createBlancs(4," ");
                line += createBlancs(18," ");
                line += Utility.bourrageDroite(prelevement.getNom_Tire(),24," ");
                line += createBlancs(24," ");
                line += createBlancs(2," ");
                line += createBlancs(14," ");
                line += Utility.bourrageDroite(prelevement.getLibelle(),64," ");
                
                writeln(line);
                if(prelevement.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))){
                    prelevement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(prelevement, "PRELEVEMENTS", "IDPRELEVEMENT=" + prelevement.getIdprelevement());
                }else{
                    prelevement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(prelevement, "PRELEVEMENTS", "IDPRELEVEMENT=" + prelevement.getIdprelevement());
                }
                 montantTotal += Long.parseLong(prelevement.getMontantprelevement());
            }
            
        setDescription(getDescription()+" exécuté avec succès: Nombre de prelevements= "+prelevements.length +" - Montant Total= "+montantTotal);
            logEvent("INFO", "Nombre de prelevements= "+prelevements.length +" - Montant Total= "+montantTotal);
        }else {
            setDescription(getDescription()+": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }
        closeFile();
        db.close();
    }
}
