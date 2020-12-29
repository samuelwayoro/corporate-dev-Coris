/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.delta;

import clearing.model.EnteteLot;
import clearing.table.Cheques;
import clearing.table.Lotcom;
import clearing.table.Remcom;
import java.io.File;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.patware.action.file.FlatFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ImageChequeDeltaWriter extends FlatFileWriter {

    private String sql = "";
    private int refLot = 0;
    private Vector<EnteteLot> vEnteteLots = new Vector<EnteteLot>();
    Cheques cheques[] = null;
    Cheques cheques35[] = null;
    Remcom remcom = null;
    Lotcom lotcom = null;

    public ImageChequeDeltaWriter() {
        setDescription(getDescription()+"\nEnvoi d'image chèque aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        //CETAOPEVALDELTA  CETAOPEVAL2
        String whereClause = " ETAT =" + Utility.getParam("CETAOPEVALDELTA") + " AND LOTSIB=1 AND ETATIMAGE="+ Utility.getParam("CETAIMASTO"); //+ " AND BANQUE NOT IN ('" + CMPUtility.getCodeBanque() + "','" + CMPUtility.getCodeBanqueSica3() + "') ";

        //Recuperation des cheques 035 
        sql = "SELECT * FROM CHEQUES WHERE  " + whereClause + "  AND TYPE_CHEQUE ='035'";
        cheques35 = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        copyCheques(cheques35);

        if (refLot > 0) {
            db.executeUpdate("UPDATE CHEQUES SET ETATIMAGE="+Utility.getParam("CETAIMAENV") + " WHERE "+ whereClause);
            
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Chèque= " + cheques35.length );
            logEvent("INFO", "Nombre de Chèque= " + cheques35.length );

        } else {
            setDescription(getDescription() + "\n Il n'y a aucun élément disponible ");
            logEvent("WARNING", "Il n'y a aucun élément disponible");
        }

        db.close();

    }

   

    private void copyCheques(Cheques[] cheques) throws Exception {

        if (cheques != null && cheques.length > 0) {

            String ampPath = Utility.getParam("AMP_IMG_FOLDER");
            String ampFileName;
            String wdbFileName;
          
            for (Cheques cheque : cheques) {
                refLot++;
                wdbFileName = cheque.getPathimage()+cheque.getFichierimage();
                ampFileName = ampPath+cheque.getBanque()+"-"+cheque.getAgence()+"-"+cheque.getNumerocompte()+"-"+cheque.getNumerocheque();
                
                FileUtils.copyFile(new File(wdbFileName+"f.jpg"),new File(ampFileName+"-recto-GS.jpg"));
                FileUtils.copyFile(new File(wdbFileName+"r.jpg"),new File(ampFileName+"-verso-GS.jpg"));
            }

            
            
        }
    }

    }
