/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.flexcube;

import clearing.model.CMPUtility;
import clearing.table.Comptes;
import clearing.table.Virements;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class VirementAllerFlexCubeEBFWriter extends FlatFileWriter {

    public VirementAllerFlexCubeEBFWriter() {
        setDescription("Envoi des virements Aller vers le SIB");
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        boolean isEcobankStandard;
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        String compteur;
        if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("0")) {
            compteur = Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTVIRALL", "VIRALL"), 4, "0");
            isEcobankStandard = false;
        } else if (Utility.getParam("ECOBANK_STANDARD") != null && Utility.getParam("ECOBANK_STANDARD").equalsIgnoreCase("2")) {
            compteur = "v" + Utility.bourrageGauche(Utility.computeCompteurAlphaNum("CPTVIRALL", "VIRALL"), 3, "0");
            isEcobankStandard = false;
        } else {

            compteur = Utility.bourrageGauche(Utility.computeCompteur("CPTVIRALL", "VIRALL"), 4, "0");
            isEcobankStandard = true;
        }

        String dateValeur = Utility.getParam("DATEVALEUR_ALLER");
        String[] param1 = (String[]) getParametersMap().get("param1");
        if (param1 != null && param1.length > 0) {
            dateValeur = param1[0];
        }
        String dateCompensation = "";
        param1 = (String[]) getParametersMap().get("param2");
        if (param1 != null && param1.length > 0) {
            dateCompensation = param1[0];
        }
        System.out.println("Date Compensation = " + dateCompensation);
        dateCompensation = Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyyMMdd"), "yyyy/MM/dd");

        String fileName = Utility.getParam("SIB_IN_FOLDER") + File.separator + Utility.getParam("VIR_IN_FILE_ROOTNAME") + compteur + Utility.getParam("SIB_FILE_EXTENSION");

        String sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPESTO") + ") ORDER BY REMISE, NUMEROCOMPTE_TIRE ";

        Virements[] virementsTous = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
        int j = 0;
        long montantTotalDesVirements = 0;
        if (virementsTous != null && 0 < virementsTous.length) {
            setOut(createFlatFile(fileName));
            StringBuilder line = new StringBuilder("H" + Utility.getParam("FLEXBRANCHCODE") + "UAP");
            line.append(compteur.toLowerCase());
            line.append(CMPUtility.getDate());
            if (isEcobankStandard) {
                line.append(createBlancs(76, " "));
            }
            writeln(line.toString());

            for (int i = 0; i < virementsTous.length; i += j) {
                //distinct numero compte tiré
  
                //Tous les virements validés d'une remise
                sql = "SELECT * FROM VIREMENTS WHERE ETAT IN (" + Utility.getParam("CETAOPESTO") + ") AND REMISE= " + virementsTous[i].getRemise() + " AND NUMEROCOMPTE_TIRE='" + virementsTous[i].getNumerocompte_Tire() + "' ";
                Virements[] virementsSameCompteTireFichier = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                if (virementsSameCompteTireFichier != null && virementsSameCompteTireFichier.length > 0) {
                    j = virementsSameCompteTireFichier.length;
                    System.out.println("taille ::" + virementsSameCompteTireFichier.length);
                    Virements virement = virementsTous[i];

                    long montantTotalSameCompteTireFichier = 0;

                    HashMap<String, String> hashBanque = new HashMap();
                    //Montant TOTAL des virements par remise et compte tire
                    for (Virements virementSameCompteTire : virementsSameCompteTireFichier) {
                        montantTotalSameCompteTireFichier += Long.parseLong(virementSameCompteTire.getMontantvirement());
                        if (!virementSameCompteTire.getBanque().equals(CMPUtility.getCodeBanqueSica3())) {
                            hashBanque.put(virementSameCompteTire.getBanque(), virementSameCompteTire.getBanque());

                        }
                    }
                    montantTotalDesVirements += montantTotalSameCompteTireFichier;

                    int nbrBanque = hashBanque.keySet().size();

                    String numCptex = "";
                    Comptes[] comptes = (Comptes[]) db.retrieveRowAsObject("select * from comptes where numero  ='" + virementsSameCompteTireFichier[0].getNumerocompte_Tire() + "' and agence ='" + virementsSameCompteTireFichier[0].getAgenceremettant() + "'", new Comptes());
                    if (comptes != null && comptes.length > 0) {
                        numCptex = comptes[0].getNumcptex().trim();
                    }

                    //DEBIT DONNEUR D'ORDRE
                    line = new StringBuilder();
                    line.append(Utility.bourrageDroite(numCptex, 3, " "));
                    line.append(Utility.bourrageDroite(numCptex, 16, " "));
                    line.append(createBlancs(4, " "));
                    line.append("D");
                    line.append(Utility.bourrageGauche("" + montantTotalSameCompteTireFichier, 16, " "));
                    line.append("F03");
                    line.append(dateValeur);
                    line.append(createBlancs(8, " "));
                    line.append(Utility.bourrageDroite(virement.getLibelle(), 18, " "));
                    line.append(createBlancs(7, " "));
                    line.append(virement.getType_Virement().equals("015") ? "010" : "011");
                    line.append(Utility.bourrageGZero(virement.getIdvirement().toPlainString(), 8));
                    writeln(line.toString());

                    for (String banque : hashBanque.keySet()) {
                        //o	Débit : frais VIB par banque (compte client donneur dâ€™ordre) )-2.950 FCFA*banque 
                        //DEBIT DONNEUR D'ORDRE du montant des frais par banque <>  CMPUtility.getCodeBanqueSica3()
                        line = new StringBuilder();
                        line.append(Utility.bourrageDroite(numCptex, 3, " "));
                        line.append(Utility.bourrageDroite(numCptex, 16, " "));
                        line.append(createBlancs(4, " "));
                        line.append("D");
                        line.append(Utility.bourrageGauche(Utility.getParam("COMDEBVIRALEFLEX"), 16, " "));
                        line.append("F03");
                        line.append(dateValeur);
                        line.append(createBlancs(8, " "));
                        line.append(Utility.bourrageDroite("FRAIS VIB " + banque, 18, " "));//libelle
                        line.append(createBlancs(7, " "));
                        line.append(virementsSameCompteTireFichier[0].getType_Virement().equals("015") ? "010" : "011");
                        line.append(Utility.bourrageGZero(virementsSameCompteTireFichier[0].getIdvirement().toPlainString(), 8));
                        writeln(line.toString());

                    }

                    for (int x = 0; x < virementsSameCompteTireFichier.length; x++) {
                        Virements virement1 = virementsSameCompteTireFichier[x];
                        if (!virement1.getBanque().equals(CMPUtility.getCodeBanqueSica3())) {
                            line = new StringBuilder();
                            line.append(Utility.getParam("FLEXMAINBRANCH"));
                            line.append(Utility.bourrageDroite(Utility.getParam("CPTATTVIRALEFLEX"), 9, " "));
                            line.append(createBlancs(11, " "));
                            line.append("C");
                            line.append(Utility.bourrageGauche("" + virement1.getMontantvirement(), 16, " "));
                            line.append("F57");
                            line.append(CMPUtility.getDate());
                            line.append(createBlancs(8, " "));
                            line.append(Utility.bourrageDroite(virement1.getLibelle(), 18, " "));
                            line.append(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyy/mm/dd"), "dd/mm/yy"));
                            line.append(createBlancs(10, " "));
                            writeln(line.toString());
                        } else {
                            line = new StringBuilder();
                            line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(virement1.getNumerocompte_Beneficiaire(), virement1.getAgence(),"0"), 3, " "));
                            line.append(Utility.bourrageDroite(CMPUtility.getNumCptEx(virement1.getNumerocompte_Beneficiaire(), virement1.getAgence(),"0"), 16, " "));
                            line.append(createBlancs(11, " "));
                            line.append("C");
                            line.append(Utility.bourrageGauche("" + virement1.getMontantvirement(), 16, " "));
                            line.append("F57");
                            line.append(CMPUtility.getDate());
                            line.append(createBlancs(8, " "));
                            line.append(Utility.bourrageDroite(virement1.getLibelle(), 18, " "));
                            line.append(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyy/mm/dd"), "dd/mm/yy"));
                            line.append(createBlancs(10, " "));
                            writeln(line.toString());
                        }

                        if (virement1.getEtat().toPlainString().equals(Utility.getParam("CETAOPESTO"))) {
                            virement1.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                            db.updateRowByObjectByQuery(virement1, "VIREMENTS", "IDVIREMENT=" + virement1.getIdvirement());
                        }
                    }
                    if (nbrBanque != 0) {
                        line = new StringBuilder();
                        line.append(Utility.getParam("FLEXMAINBRANCH"));
                        line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMVIRALE1"), 9, " "));
                        line.append(createBlancs(11, " "));
                        line.append("C");
                        line.append(Utility.bourrageGauche("" + (Long.parseLong(Utility.getParam("COMCREVIRALEFLEX1")) * nbrBanque), 16, " "));
                        line.append("F57");
                        line.append(CMPUtility.getDate());
                        line.append(createBlancs(8, " "));
                        line.append(Utility.bourrageDroite(virement.getLibelle(), 18, " "));
                        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyy/mm/dd"), "dd/mm/yy"));
                        line.append(createBlancs(10, " "));
                        writeln(line.toString());

                        line = new StringBuilder();
                        line.append(Utility.getParam("FLEXMAINBRANCH"));
                        line.append(Utility.bourrageDroite(Utility.getParam("CPTCRECOMVIRALE2"), 9, " "));
                        line.append(createBlancs(11, " "));
                        line.append("C");
                        line.append(Utility.bourrageGauche("" + (Long.parseLong(Utility.getParam("COMCREVIRALEFLEX2")) * nbrBanque), 16, " "));
                        line.append("F57");
                        line.append(CMPUtility.getDate());
                        line.append(createBlancs(8, " "));
                        line.append(Utility.bourrageDroite(virement.getLibelle(), 18, " "));
                        line.append(Utility.convertDateToString(Utility.convertStringToDate(dateCompensation, "yyyy/mm/dd"), "dd/mm/yy"));
                        line.append(createBlancs(10, " "));
                        writeln(line.toString());

                    }
                    

                }
        
            }
            setDescription(getDescription() + " exécuté avec succès:\n Nombre de Virements= " + virementsTous.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalDesVirements) + " - Nom de Fichier = " + fileName);
            logEvent("INFO", "Nombre de Virement= " + virementsTous.length + " - Montant Total= " + Utility.formatNumber("" + montantTotalDesVirements));
            closeFile();
        } else {
            setDescription(getDescription() + ": Il n'y a aucun element disponible");
            logEvent("WARNING", "Il n'y a aucun element disponible");
        }

        db.close();
    }
}
