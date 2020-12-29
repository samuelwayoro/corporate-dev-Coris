/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.corporates;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import java.io.File;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.patware.action.file.BinFileWriter;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ChequesExportXLSWriter extends BinFileWriter {

    public ChequesExportXLSWriter() {
        setDescription("Export des Cheques Aller transmis à la banque");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String dateTraitement = "";
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateTraitement = param1[0];
        }
        dateTraitement = Utility.convertDateToString(Utility.convertStringToDate(dateTraitement, "yyyyMMdd"), "yyyy/MM/dd");

        System.out.println("Date Traitement = " + dateTraitement);
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("CHQ_OUT_FILE_ROOTNAME") + "_" + CMPUtility.getLibelleBanque(CMPUtility.getCodeBanque()) + "_" + CMPUtility.getDateHeure() + ".xls";
        setOut(createBinFile(fileName));

        // créer un classeur
        Workbook wb = new HSSFWorkbook();
        // créer une feuille
        Sheet mySheet = wb.createSheet();

        String sql = "SELECT * FROM CHEQUES WHERE ETAT IN (" + Utility.getParam("CETAOPEALLICOM1") + ") AND DATETRAITEMENT='" + dateTraitement + "' ORDER BY BANQUE";
        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
        long montantTotal = 0;

        if (cheques != null && 0 < cheques.length) {

            mySheet.createRow(0);
            mySheet.createRow(1);
            mySheet.createRow(2);
            Row myRow = mySheet.createRow(3);

            // Ajouter des données dans les cellules
            myRow.createCell(0).setCellValue("");
            myRow.createCell(1).setCellValue("");
            myRow.createCell(2).setCellValue("");
            myRow.createCell(3).setCellValue("");
            myRow.createCell(4).setCellValue("");
            myRow.createCell(5).setCellValue("Chèques remis à Ecobank le " + dateTraitement);
            int firstRowNum = myRow.getRowNum();

            myRow = mySheet.createRow(5);
            myRow.createCell(0).setCellValue("Numéro de chèque");
            myRow.createCell(1).setCellValue("Compte Tiré");
            myRow.createCell(2).setCellValue("Nom Tiré");
            myRow.createCell(3).setCellValue("Date Emission");
            myRow.createCell(4).setCellValue("Montant");
            myRow.createCell(5).setCellValue("Point d'accès");
            myRow.createCell(6).setCellValue("Utilisateur");
            mySheet.createRow(6);

            int j = 0;
              int lastRow = 7;
            for (int i = 0; i < cheques.length; i += j) {
                //Tous les cheques  validés d'une banque
                sql = "SELECT * FROM CHEQUES WHERE BANQUE='" + cheques[i].getBanque() + "' AND ETAT IN (" + Utility.getParam("CETAOPEALLICOM1") + ") AND DATETRAITEMENT='" + dateTraitement + "' ORDER BY BANQUE";
                Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                if (chequesVal != null && 0 < chequesVal.length) {
                    long sumRemise = 0;
                  
                 

                    myRow = mySheet.createRow(lastRow++);
                    myRow.createCell(0).setCellValue("Banque: ");
                    myRow.createCell(1).setCellValue(cheques[i].getBanque());
                    myRow.createCell(2).setCellValue(CMPUtility.getLibelleBanque(cheques[i].getBanque()));
                    mySheet.createRow(lastRow++);

                    for (int x = 0; x < chequesVal.length; x++) {
                        myRow = mySheet.createRow(lastRow++);
                        System.out.println("row in:"+lastRow);
                        // Ajouter des données dans les cellules
                        myRow.createCell(0).setCellValue(chequesVal[x].getNumerocheque());
                        myRow.createCell(1).setCellValue(chequesVal[x].getNumerocompte());
                        myRow.createCell(2).setCellValue(chequesVal[x].getNomemetteur());
                        myRow.createCell(3).setCellValue(chequesVal[x].getDatetraitement());
                        myRow.createCell(4).setCellValue(chequesVal[x].getMontantcheque());
                        myRow.createCell(5).setCellValue(chequesVal[x].getEtablissement());
                        myRow.createCell(6).setCellValue(chequesVal[x].getCodeutilisateur());
                       
                        sumRemise += Long.parseLong(chequesVal[x].getMontantcheque());
                       
                    }
                    myRow = mySheet.createRow(lastRow++);
                    myRow.createCell(0).setCellValue("");
                    myRow.createCell(1).setCellValue("");
                    myRow.createCell(2).setCellValue("");
                    myRow.createCell(3).setCellValue("Total:");
                    myRow.createCell(4).setCellValue(sumRemise);
                    montantTotal += sumRemise;
                    mySheet.createRow(lastRow++);
                     
                       j = chequesVal.length;
                }

                
            }
            // créer une ligne à  la fin dans la feuille Excel

            myRow = mySheet.getRow(firstRowNum);
            // Ajouter des données dans les cellules
            myRow.createCell(6).setCellValue("Montant Total:");
            myRow.createCell(7).setCellValue(montantTotal);
           
            wb.write(getOut());
        }
        getOut().close();
        db.close();
        logEndOfTask();
    }
}
