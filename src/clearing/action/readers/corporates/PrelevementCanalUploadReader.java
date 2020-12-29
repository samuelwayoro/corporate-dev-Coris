/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.corporates;

import clearing.table.Prelevements;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.patware.action.file.FlatFileReader;
import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author DavyStephane
 */
public class PrelevementCanalUploadReader extends FlatFileReader {

    private long montantTotalCal = 0;
    private String montantTotal;
    private int nbPrelevements;
    private Prelevements[] prelevements;
    private List<Prelevements> listPrelevements = new ArrayList<Prelevements>();
    private String resultat = "";

    public PrelevementCanalUploadReader() {
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);
        String line;

        Prelevements prelevement;
        String compteClient = "";
        String agenceClient = "01001";
        String nomBeneficiaire = "";
        String codeOrganisme = "";




        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {

            setCurrentLine(line);
            String codeOperation = getChamp(3);
            if (codeOperation.equalsIgnoreCase("031")) { //entete du fichier

                getChamp(5);
                getChamp(6);
                getChamp(3);
                agenceClient = getChamp(5);
                compteClient = getChamp(12);

                nomBeneficiaire = getChamp(23);
                codeOrganisme = getChamp(5);
                getChamp(1);
                getChamp(6);
                getChamp(59);


            } else if (codeOperation.equalsIgnoreCase("032")) {  //lignes de prelevements
                prelevement = new Prelevements();

                prelevement.setReference_Emetteur(getChamp(5));//Matricule. //reference
                prelevement.setDatetraitement(Utility.convertDateToString(Utility.convertStringToDate(getChamp(6), "ddMMyy"), "yyyy/MM/dd"));
                prelevement.setBanque(Utility.getParam("CODE_BANQUE_SICA3").substring(0, 2) + getChamp(3));
                prelevement.setAgence(getChamp(5));
                prelevement.setNumerocompte_Tire(getChamp(12));
                prelevement.setNom_Tire(getChamp(23));
                getChamp(17); //libelle bank
                prelevement.setLibelle(getChamp(30));//libelle operation
                prelevement.setMontantprelevement("" + Integer.parseInt(getChamp(12)));

                prelevement.setType_prelevement(Utility.getParam("ORDPRENOUNOR"));
                prelevement.setIdprelevement(new BigDecimal(Utility.computeCompteur("IDPRELEVEMENT", "PRELEVEMENTS")));
                prelevement.setBanqueremettant(Utility.getParam("CODE_BANQUE_SICA3"));
                prelevement.setAgenceremettant(agenceClient);
                prelevement.setNumerocompte_Beneficiaire(compteClient);
                prelevement.setNom_Beneficiaire(nomBeneficiaire);
                prelevement.setZoneinterbancaire_Beneficiaire(codeOrganisme);

                prelevement.setDateordre(prelevement.getDatetraitement());
                prelevement.setEtablissement(repertoire.getPartenaire());
                prelevement.setDevise("XOF");
                prelevement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALDELTA")));

                montantTotalCal += Integer.parseInt(prelevement.getMontantprelevement());
                listPrelevements.add(prelevement);


            } else if (codeOperation.equalsIgnoreCase("033")) {//fin de fichier
                getChamp(5);
                getChamp(6);
                getChamp(8);
                getChamp(10);
                getChamp(72);

                montantTotal = "" + Integer.parseInt(getChamp(12));
                System.out.println("montantTotal :" + montantTotal);

            }

        }

        if (listPrelevements.isEmpty()) {
            resultat = "Structure du Fichier de prélèvement incorrect, Veuillez vérifier.";
        } else if (Long.parseLong(montantTotal) != montantTotalCal) {
            resultat = "Montant Total calculé différent du montant total dans la fin du fichier.";

        }

        prelevements = listPrelevements.toArray(new Prelevements[listPrelevements.size()]);//conversion liste to array
        nbPrelevements = listPrelevements.size();
        System.out.println(" Resultat :" + resultat);

        closeFile();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        for (int i = 0; i < prelevements.length; i++) {
            Prelevements prelevements1 = prelevements[i];
           
            db.insertObjectAsRow(prelevements1, "PRELEVEMENTS");
        }
        db.close();
        return aFile;

    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    public long getMontantTotalCal() {
        return montantTotalCal;
    }

    public void setMontantTotalCal(long montantInteger) {
        this.montantTotalCal = montantInteger;
    }

    public String getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(String montantTotal) {
        this.montantTotal = montantTotal;
    }

    public int getNbPrelevements() {
        return nbPrelevements;
    }

    public void setNbPrelevements(int nbPrelevements) {
        this.nbPrelevements = nbPrelevements;
    }

    public Prelevements[] getPrelevements() {
        return prelevements;
    }

    public void setPrelevements(Prelevements[] prelevements) {
        this.prelevements = prelevements;
    }

    public List<Prelevements> getListPrelevements() {
        return listPrelevements;
    }

    public void setListPrelevements(List<Prelevements> prel) {
        this.listPrelevements = prel;
    }
}
