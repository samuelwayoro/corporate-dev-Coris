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
import org.patware.utils.Utility;

/**
 *
 * @author DavyStephane
 */
public class PrelevementUploadReader extends FlatFileReader {

    private long montantTotalCal = 0;
    private String montantTotal;
    private int nbPrelevements;
    private Prelevements[] prelevements;
    private List<Prelevements> listPrelevements = new ArrayList<Prelevements>();
    private String resultat = null;

    public PrelevementUploadReader() {
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);
        String line;

        Prelevements prelevement;
        String compteClient = "";
        String agenceClient = "01001";
        String nomBeneficiaire = "";




        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {

            setCurrentLine(line);
            String codeOperation = getChamp(3);
            if (codeOperation.equalsIgnoreCase("021")) { //entete du fichier

                getChamp(5);
                getChamp(6);
                getChamp(5);
                agenceClient = getChamp(5);
                compteClient = getChamp(12);
                getChamp(2);
                nomBeneficiaire = getChamp(24);

            } else if (codeOperation.equalsIgnoreCase("022")) {  //lignes de prelevements
                prelevement = new Prelevements();

                getChamp(5);
                prelevement.setDatetraitement(Utility.convertDateToString(Utility.convertStringToDate(getChamp(6), "ddMMyy"), "yyyy/MM/dd"));
                prelevement.setBanque(getChamp(5));
                prelevement.setAgence(getChamp(5));
                prelevement.setNumerocompte_Tire(getChamp(12));
                String clerib = getChamp(2); //clef rib
                String cleribCal = Utility.computeCleRIB(prelevement.getBanque(), prelevement.getAgence(), prelevement.getNumerocompte_Tire());
                if(!cleribCal.equals(clerib)){
                    resultat += "<BR>Erreur de Cle RIB pour le compte :" +prelevement.getBanque()+" "+prelevement.getAgence()+" "+ prelevement.getNumerocompte_Tire()
                     +" Cle RIB du fichier : "+ clerib + " Cle RIB calculé :"+ cleribCal;
                }
                prelevement.setReference_Emetteur(getChamp(7));//Matricule. //reference
                prelevement.setNom_Tire(getChamp(24));
                getChamp(12); //libelle bank
                prelevement.setLibelle(getChamp(30));//libelle operation
                prelevement.setMontantprelevement("" + Integer.parseInt(getChamp(12)));

                prelevement.setType_prelevement(Utility.getParam("ORDPRENOUNOR"));
                prelevement.setIdprelevement(new BigDecimal(Utility.computeCompteur("IDPRELEVEMENT", "PRELEVEMENTS")));
                prelevement.setBanqueremettant(Utility.getParam("CODE_BANQUE_SICA3"));
                prelevement.setAgenceremettant(agenceClient);
                prelevement.setNumerocompte_Beneficiaire(compteClient);
                prelevement.setNom_Beneficiaire(nomBeneficiaire);

                prelevement.setDateordre(prelevement.getDatetraitement());
                prelevement.setEtablissement(repertoire.getPartenaire());
                prelevement.setDevise("XOF");
                prelevement.setEtat(new BigDecimal(Utility.getParam("CETAOPEVALDELTA")));

                montantTotalCal += Integer.parseInt(prelevement.getMontantprelevement());
                listPrelevements.add(prelevement);


            } else if (codeOperation.equalsIgnoreCase("029")) {//fin de fichier
                getChamp(5);
                getChamp(6);
                getChamp(8);
                getChamp(12);
                getChamp(77);

                montantTotal = "" + Integer.parseInt(getChamp(12));
                System.out.println("montantTotal :" + montantTotal);

            }

        }

        if (listPrelevements.isEmpty()) {
            resultat = "Structure du Fichier de prélèvement incorrect, Veuillez vérifier.";
        } else if (Long.parseLong(montantTotal) != montantTotalCal) {
            resultat = "Montant Total calculé différent du montant total dans l'entete du fichier.";

        }

        prelevements = listPrelevements.toArray(new Prelevements[listPrelevements.size()]);//conversion liste to array
        nbPrelevements = listPrelevements.size();
        System.out.println(" prel : " + listPrelevements.size());

closeFile();
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
