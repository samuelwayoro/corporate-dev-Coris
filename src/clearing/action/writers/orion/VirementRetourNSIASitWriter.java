/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clearing.action.writers.orion;

import clearing.model.CMPUtility;
import clearing.table.Virements;
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
public class VirementRetourNSIASitWriter extends FlatFileWriter{

    public VirementRetourNSIASitWriter() {
         setDescription("Envoi Virements retour vers SIB");
    }

    @Override
       public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_FILE_ROOTNAME") + CMPUtility.getDateHeure() + Utility.getParam("SIB_FILE_EXTENSION");
        setOut(createFlatFile(fileName));

        String sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" +  Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        int j = 0;
        if (virements != null && 0 < virements.length) {

            for (int i = 0; i < virements.length; i ++) {
                Virements virement = virements[i];
                //Tous les virements retours
                String line = "<>0320021201";
                line += "00"+virement.getBanqueremettant();
                line += createBlancs(2," ")+"1";
                line += "00"+virement.getBanque();
                line += createBlancs(2," ");
                line += CMPUtility.getAgenceACP(virement.getBanque(),virement.getAgence())+ virement.getDevise()+"2";
                line += Utility.bourrageGZero(virement.getMontantvirement(), 16);
                line += Utility.convertDateToString(Utility.convertStringToDate(virement.getDatecompensation(), ResLoader.getMessages("patternDate")),"yyMMdd");
                line += "0 "+createBlancs(15,"0")+createBlancs(4," ")+"F";
                line += Utility.bourrageGZero(virement.getIdvirement().toPlainString(), 9);
                line += createBlancs(21," ");
                //line += Utility.bourrageGZero(virement.getNumerovirement(), 10);
                //line += virement.getAgence();
                line += Utility.bourrageGauche(virement.getNumerocompte_Tire(), 12, "0");
                line += createBlancs(4," ");
                line += Utility.bourrageGauche(virement.getNumerocompte_Beneficiaire(), 12, "0");
                line += "  ";
                line += Utility.bourrageDroite(virement.getNom_Tire(),24," ");
                line += CMPUtility.getAgenceACP(virement.getBanqueremettant(), virement.getAgenceremettant());
                line += createBlancs(23," ");
                
                line += Utility.bourrageDroite(virement.getNom_Beneficiaire(),24," ");
                line += createBlancs(40," ");
                line += Utility.bourrageDroite(virement.getLibelle(),32," ");
                line += Utility.bourrageDroite(virement.getLibelle(),32," ");
               
                writeln(line);
                if(virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))){
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                }else{
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                }
            }
            
        }
        closeFile();
        db.close();
    }
}
