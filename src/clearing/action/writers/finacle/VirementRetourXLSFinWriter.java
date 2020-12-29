/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.finacle;

import clearing.model.CMPUtility;
import clearing.table.Virements;
import java.io.File;
import java.math.BigDecimal;
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
public class VirementRetourXLSFinWriter extends BinFileWriter {

    public VirementRetourXLSFinWriter() {
        setDescription("Envoi des Virements retour vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_OUT_FILE_ROOTNAME") + CMPUtility.getDateHeure() + ".xls";
        setOut(createBinFile(fileName));

        // créer un classeur
        Workbook wb = new HSSFWorkbook();
        // créer une feuille
        Sheet mySheet = wb.createSheet();

        String sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPERETREC") + "," + Utility.getParam("CETAOPERET") + ") ";
        Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        long sumVirements = 0;
        if (virements != null && 0 < virements.length) {

            Row myRow = mySheet.createRow(0);

            // Ajouter des données dans les cellules
            myRow.createCell(0).setCellValue("NAME");
            myRow.createCell(1).setCellValue("ACCOUNT NUMBER");
            myRow.createCell(2).setCellValue("AMOUNT");
            myRow.createCell(3).setCellValue("NARRATION");
            myRow.createCell(4).setCellValue("PIN");
            myRow.createCell(5).setCellValue("TRANTYPE");

            for (int i = 0; i < virements.length; i++) {
                Virements virement = virements[i];
                sumVirements += Double.parseDouble(virement.getMontantvirement());
                //Tous les virements retours
                // créer une ligne de Ã  l'index i+1 dans la feuille Excel
                myRow = mySheet.createRow(i + 1);

                // Ajouter des données dans les cellules
                myRow.createCell(0).setCellValue(Utility.bourrageDroite(virement.getNom_Beneficiaire(), 30, " "));
                myRow.createCell(1).setCellValue(virement.getNumerocompte_Beneficiaire());
                myRow.createCell(2).setCellValue(Double.parseDouble(virement.getMontantvirement()));
                myRow.createCell(3).setCellValue(Utility.bourrageDroite(virement.getLibelle(), 30, " "));
                myRow.createCell(4).setCellValue(" ");
                myRow.createCell(5).setCellValue("C");


                if (virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPERET"))) {
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETENVSIB")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                } else {
                    virement.setEtat(new BigDecimal(Utility.getParam("CETAOPERETRECENVSIB")));
                    db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                }
            }
            // créer une ligne Ã  la fin dans la feuille Excel

            myRow = mySheet.createRow(virements.length + 1);
            // Ajouter des données dans les cellules
            myRow.createCell(0).setCellValue(Utility.bourrageDroite(Utility.getParam("UBA_CR_NAME"), 30, " "));
            myRow.createCell(1).setCellValue(Utility.getParam("UBA_CR_ACCOUNT"));
            myRow.createCell(2).setCellValue(sumVirements);
            myRow.createCell(3).setCellValue(Utility.bourrageDroite(Utility.getParam("UBA_LBL_CMP"), 30, " "));
            myRow.createCell(4).setCellValue(" ");
            myRow.createCell(5).setCellValue("D");
            wb.write(getOut());
        }
        getOut().close();
        db.close();
        logEndOfTask();
    }
}
