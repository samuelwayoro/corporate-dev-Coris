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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.math.NumberUtils;
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
 * @author Bouiks
 */
public class VirementNSIAXLSReader extends BinFileReader {

    private long montantTotalCal = 0;
    private long montantTotal = 0;
    private int nbVirements;
    private int nbVirementsEnErreur;
    private String resultat = null;
    private StringBuilder resultatBuilder = new StringBuilder();
    private Virements[] virements;
    private Virements[] virementsEnErreur;
    private List<Virements> listVirements = new ArrayList<Virements>();
    private List<Virements> listVirementsEnErreur = new ArrayList<Virements>();

    public VirementNSIAXLSReader() {

        setTattooProcessDate(true);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);
        Virements virement = new Virements();
        System.out.println("VirementNSIAXLSReader treatFile");

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        setFile(aFile);
        BufferedInputStream is = openFile(aFile);

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
                System.out.println("typeVirement :" + typeVirement);
                if (typeVirement.equalsIgnoreCase("015")) {
                    virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    System.out.println("############################################ typeVirement.equalsIgnoreCase(\"015\") ########################################################################");
                    virement.setReference_Emetteur(processToString(row.getCell(1)));
                    String compteTire = processToString(row.getCell(2));

                    String numCpt = "";
                    Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numcptex  ='" + compteTire + "' ", new Comptes());
                    virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                    String ribBenef = processToString(row.getCell(3));
                    System.out.println(ribBenef);
                    if (ribBenef != null && ribBenef.trim().length() == 24) {
                        setCurrentLine(ribBenef);
                        virement.setBanque(getChamp(5));
                        virement.setAgence(getChamp(5));
                        virement.setNumerocompte_Beneficiaire(Utility.bourrageGZero(getChamp(12), 12));
                    } else {

                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        System.out.println("longueur du chap rib benef 015 incorrecte");
                        logEvent("ERROR", "Longueur du RIB incorrecte " + ribBenef);
                    }

                    getChamp(2);
                    virement.setNom_Beneficiaire(Utility.bourrageDroite(processToString(row.getCell(4)), 30, " "));
                    String mntVirement = processToString(row.getCell(5));
                    System.out.println("015 mntVirement " + mntVirement);
                    if (mntVirement != null & mntVirement.replaceAll("\\p{javaWhitespace}+", "").trim().length() > 0) {
                        mntVirement = mntVirement.replaceAll("\\p{javaWhitespace}+", "");
                        if (NumberUtils.toInt(mntVirement) < 50000000 && NumberUtils.toInt(mntVirement) > 0) {
                            virement.setMontantvirement(String.valueOf(Long.parseLong(mntVirement)));

                        } else {
                            System.out.println("");
                            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                            logEvent("ERROR", "Montant superieur a  la limite ");
                        }

                    } else {
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        logEvent("ERROR", "Montant null ");

                    }

                    virement.setLibelle(Utility.bourrageDroite(processToString(row.getCell(6)), 50, " "));
                    if (comptes != null && comptes.length > 0) {
                        numCpt = comptes[0].getNumero().trim();
                        virement.setNumerocompte_Tire(numCpt);
                        virement.setAgenceremettant(comptes[0].getAgence().trim());
                        virement.setNom_Tire(Utility.bourrageDroite(comptes[0].getNom(), 30, " "));
//                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                    } else {
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        System.out.println("Compte " + compteTire + "introuvable dans la base");
                        logEvent("WARNING", "Compte " + compteTire + "introuvable dans la base");

                    }
                    virement.setDevise("XOF");
                    virement.setDateordre(virement.getDatetraitement());
                    //virement.setNumerovirement(Utility.bourrageGZero(Utility.computeCompteur("NUMVIR", "VIREMENTS"), 10));
                    virement.setNumerovirement(virement.getReference_Emetteur());
                    virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
                    virement.setType_Virement(typeVirement.trim());
                    virement.setVille("01");
                    virement.setCodeUtilisateur("FICHIER");
                    virement.setOrigine(new BigDecimal(2));
                    virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));
                    if (virement.getEtat() == null) {
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                        montantTotal = montantTotal + NumberUtils.toLong(mntVirement);
                        virement.setReference_Operation_Interne(virement.getIdvirement().toPlainString());
                    }

                    System.out.println("virement.getEtat() " + virement.getEtat());
                    //   db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                    if (virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPESTO").trim())) {

                        listVirements.add(virement);
                        System.out.println(" listVirements.size" + listVirements.size());
                    } else {

                        listVirementsEnErreur.add(virement);
                        System.out.println(" listVirementsEnErreur.size" + listVirementsEnErreur.size());
                    }
                    virement = new Virements();
                    System.out.println("############################################ Fin  typeVirement.equalsIgnoreCase(\"015\") ########################################################################");
                }
                if (typeVirement.equalsIgnoreCase("011")) {
                    System.out.println("############################################ typeVirement.equalsIgnoreCase(\"011\") ########################################################################");
                    virement.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                    virement.setReference_Emetteur(processToString(row.getCell(1)));
                    String compteTire = processToString(row.getCell(2));
                    String numCpt = "";
                    //   Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numcptex  ='" + compteTire + "' ", new Comptes());
                    virement.setBanqueremettant(CMPUtility.getCodeBanqueSica3());
                    virement.setAgenceremettant("01001"); //agence principale
                    String ribBenef = processToString(row.getCell(3));
                    if (ribBenef != null && ribBenef.trim().length() == 10) {
                        setCurrentLine(ribBenef);
                        virement.setBanque(getChamp(5));
                        virement.setAgence(getChamp(5));
                    } else {
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        System.out.println("longueur du chap rib benef 011 incorrecte");
                        logEvent("ERROR", "Longueur du RIB incorrecte " + ribBenef);
                    }

                    virement.setNom_Beneficiaire(Utility.bourrageDroite(processToString(row.getCell(4)), 35, " "));
//                    virement.setMontantvirement(new String(String.valueOf(Long.parseLong(processToString(row.getCell(5))))).replaceAll("\\p{javaWhitespace}+", ""));
                    String mntVirement = processToString(row.getCell(5));
                    System.out.println("011 mntVirement " + mntVirement);
                    if (mntVirement != null & mntVirement.replaceAll("\\p{javaWhitespace}+", "").trim().length() > 0) {
                        mntVirement = mntVirement.replaceAll("\\p{javaWhitespace}+", "");
                        if (NumberUtils.toInt(mntVirement) < 50000000 && NumberUtils.toInt(mntVirement) > 0) {
                            virement.setMontantvirement(String.valueOf(Long.parseLong(mntVirement)));

                        } else {
                            System.out.println("");
                            virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                            logEvent("ERROR", "Montant superieur a  la limite ");
                        }

                    } else {
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
                        logEvent("ERROR", "Montant null ");

                    }

                    virement.setLibelle(Utility.bourrageDroite(processToString(row.getCell(6)), 70, " "));
////                    if (comptes != null && comptes.length > 0) {
////                        numCpt = comptes[0].getNumero().trim();
////                        virement.setAgenceremettant(comptes[0].getAgence().trim());
////                       
//////                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
////                    } else {
////                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEERR")));
////
////                        System.out.println("Compte " + compteTire + "introuvable dans la base");
////                        logEvent("WARNING", "Compte " + compteTire + "introuvable dans la base");
////
////                    }
                    virement.setDevise("XOF");
                    virement.setDateordre(virement.getDatetraitement());
                    //virement.setNumerovirement(Utility.bourrageGZero(Utility.computeCompteur("NUMVIR", "VIREMENTS"), 10));
                    virement.setNumerovirement(virement.getReference_Emetteur());
                    virement.setEtablissement(CMPUtility.getCodeBanqueSica3());
                    virement.setType_Virement(typeVirement.trim());
                    virement.setVille("01");
                    virement.setCodeUtilisateur("FICHIER");
                    virement.setOrigine(new BigDecimal(2));
                    virement.setIdvirement(new BigDecimal(Utility.computeCompteur("IDVIREMENT", "VIREMENTS")));

                    if (virement.getEtat() == null) {
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPESTO")));
                        montantTotal = montantTotal + NumberUtils.toLong(mntVirement);
                        virement.setReference_Operation_Interne(virement.getIdvirement().toPlainString());
                    }

                    //   db.insertObjectAsRowByQuery(virement, "VIREMENTS");
                    if (virement.getEtat().toPlainString().equals(Utility.getParam("CETAOPESTO").trim())) {

                        listVirements.add(virement);
                        System.out.println(" listVirements.size" + listVirements.size());
                    } else {

                        listVirementsEnErreur.add(virement);
                        System.out.println(" listVirementsEnErreur.size" + listVirementsEnErreur.size());
                    }
                    virement = new Virements();

                }

            }
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("");
        if (listVirements.isEmpty()) {
            resultat = "Structure du Fichier de virement incorrecte, Veuillez vérifier.";

        } else {
            virements = listVirements.toArray(new Virements[listVirements.size()]);//conversion liste to array
            virementsEnErreur = listVirementsEnErreur.toArray(new Virements[listVirementsEnErreur.size()]);//conversion liste to array
            System.out.println("listVirements" + listVirements.size());
            nbVirements = listVirements.size();
            nbVirementsEnErreur = listVirementsEnErreur.size();
            System.out.println("nbVirementsEnErreur :" + nbVirementsEnErreur + "nbVirements: " + nbVirements);
        }
        db.close();
        System.out.println("Fin VirementEBFXLSReader treatFile");
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

    public long getMontantTotalCal() {
        return montantTotalCal;
    }

    public void setMontantTotalCal(long montantTotalCal) {
        this.montantTotalCal = montantTotalCal;
    }

    public long getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(long montantTotal) {
        this.montantTotal = montantTotal;
    }

    public int getNbVirements() {
        return nbVirements;
    }

    public void setNbVirements(int nbVirements) {
        this.nbVirements = nbVirements;
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    public Virements[] getVirements() {
        return virements;
    }

    public void setVirements(Virements[] virements) {
        this.virements = virements;
    }

    public StringBuilder getResultatBuilder() {
        return resultatBuilder;
    }

    public void setResultatBuilder(StringBuilder resultatBuilder) {
        this.resultatBuilder = resultatBuilder;
    }

    public List<Virements> getListVirements() {
        return listVirements;
    }

    public void setListVirements(List<Virements> listVirements) {
        this.listVirements = listVirements;
    }

    public int getNbVirementsEnErreur() {
        return nbVirementsEnErreur;
    }

    public void setNbVirementsEnErreur(int nbVirementsEnErreur) {
        this.nbVirementsEnErreur = nbVirementsEnErreur;
    }

    public List<Virements> getListVirementsEnErreur() {
        return listVirementsEnErreur;
    }

    public void setListVirementsEnErreur(List<Virements> listVirementsEnErreur) {
        this.listVirementsEnErreur = listVirementsEnErreur;
    }

    public Virements[] getVirementsEnErreur() {
        return virementsEnErreur;
    }

    public void setVirementsEnErreur(Virements[] virementsEnErreur) {
        this.virementsEnErreur = virementsEnErreur;
    }
}
