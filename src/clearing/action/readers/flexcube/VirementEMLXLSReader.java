/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Comptes;
import clearing.table.Virements;
import java.io.BufferedInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.patware.action.file.BinFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class VirementEMLXLSReader extends BinFileReader {

    public VirementEMLXLSReader() {

        setTattooProcessDate(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);

        Virements virement = new Virements();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        setFile(aFile);
        BufferedInputStream is = openFile(aFile);

        virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
        try {

            // Créer un objet classuer
            HSSFWorkbook classeur = new HSSFWorkbook(is);

            //Lire la première feuille de ce classeur
            HSSFSheet feuille = classeur.getSheetAt(0);

            // Créer un Itérateur sur la feuille
            Iterator<Row> rowIterator = feuille.iterator();
            int i = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (i++ == 0) {
                    continue;
                }
                String typeVirement = row.getCell(0).getStringCellValue();
                if (typeVirement.equalsIgnoreCase("015")) {
                    virement.setReference_Emetteur(processToString(row.getCell(1)));

                    String compteTire = processToString(row.getCell(2));
                    String numCpt = "";
                    Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numcptex  ='" + compteTire + "' ", new Comptes());
                    if (comptes != null && comptes.length > 0) {
                        numCpt = comptes[0].getNumero().trim();

                        virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                        virement.setAgenceremettant(comptes[0].getAgence().trim());
                        virement.setNumerocompte_Tire(numCpt);

                        String ribBenef = processToString(row.getCell(3));
                        System.out.println(ribBenef);
                        setCurrentLine(ribBenef);
                        virement.setBanque(getChamp(5));
                        virement.setAgence(getChamp(5));
                        virement.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                        getChamp(2);
                        virement.setNom_Beneficiaire(Utility.bourrageDroite(processToString(row.getCell(4)), 30, " "));
                        virement.setNom_Tire(Utility.bourrageDroite(comptes[0].getNom(), 30, " "));
                        virement.setMontantvirement(String.valueOf(Long.parseLong(processToString(row.getCell(5)))));
                        virement.setLibelle(Utility.bourrageDroite(processToString(row.getCell(6)), 50, " "));
                    } else {
                        System.out.println("Compte " + compteTire + "introuvable dans la base");
                        logEvent("WARNING", "Compte " + compteTire + "introuvable dans la base");
                        continue;

                    }

                }
                if (typeVirement.equalsIgnoreCase("011")) {
                    virement.setReference_Emetteur(processToString(row.getCell(1)));
                    String compteTire = processToString(row.getCell(2));
                    String numCpt = "";
                    Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numcptex  ='" + compteTire + "' ", new Comptes());
                    if (comptes != null && comptes.length > 0) {
                        numCpt = comptes[0].getNumero().trim();

                        virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                        virement.setAgenceremettant(comptes[0].getAgence().trim());

                        String ribBenef = processToString(row.getCell(3));
                        setCurrentLine(ribBenef);
                        virement.setBanque(getChamp(5));
                        virement.setAgence(getChamp(5));
                        virement.setNom_Beneficiaire(Utility.bourrageDroite(processToString(row.getCell(4)), 35, " "));
                        virement.setMontantvirement(String.valueOf(Long.parseLong(processToString(row.getCell(5)))));
                        virement.setNom_Tire(comptes[0].getNom());

                        virement.setLibelle(Utility.bourrageDroite(processToString(row.getCell(6)), 70, " "));
                    } else {
                        System.out.println("Compte " + compteTire + "introuvable dans la base");
                        logEvent("WARNING", "Compte " + compteTire + "introuvable dans la base");
                        continue;

                    }
                }
                virement.setDevise("XOF");
                virement.setDateordre(virement.getDatetraitement());
                //virement.setNumerovirement(Utility.bourrageGZero(Utility.computeCompteur("NUMVIR", "VIREMENTS"), 10));
                virement.setNumerovirement(virement.getReference_Emetteur());
                virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEFIC")));
                virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
                virement.setType_Virement(typeVirement.trim());
                virement.setVille("01");
                virement.setCodeUtilisateur("FICHIER");
                virement.setOrigine(new BigDecimal(2));
                virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));
                db.insertObjectAsRowByQuery(virement, "VIREMENTS");

                System.out.println("");
            }
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
        return aFile;

    }

    String processToString(Cell cell) {

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf(new Double(cell.getNumericCellValue()).longValue());

            case Cell.CELL_TYPE_STRING:
                return (cell.getStringCellValue());
        }

        return cell.getStringCellValue();
    }
}
