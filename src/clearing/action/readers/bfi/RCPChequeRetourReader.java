/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.bfi;

import clearing.table.Cheques;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;

import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class RCPChequeRetourReader extends FlatFileReader {

    public RCPChequeRetourReader() {
        setHasNormalExtension(false);
        setExtensionType(END_EXT);
    }

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);
        String dateCopy = Utility.convertDateToString(new java.util.Date(), "ddMMyyyy");

        System.out.println("dateCopy " + dateCopy);
        String substring = aFile.getName().substring(10, 18);

//        if (substring.equals(dateCopy)) {
        String line = null;
        String typeOperation = null;

        Cheques cheque = new Cheques();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        int compteur = 0;
        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            compteur++;
            if (compteur != 1) {
                getChamp(1); //Sens
                getChamp(2); //Code centre de compensation 
                getChamp(2); //Code pays de lemetteur
                getChamp(8); //Date de generation
                getChamp(6); //Heure generation 
                cheque.setType_Cheque(getChamp(2)); //Code valeur
                getChamp(3); //Code participant 
                cheque.setDatecompensation(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "ddMMyyyy"), ResLoader.getMessages("patternDate"))); //Date de presentation    la compensation JJMMAAAA
                getChamp(8); //Date de presentation appliquee
                cheque.setRemcom(new BigDecimal(getChamp(7))); //Numero de la remise
                typeOperation = getChamp(2); //Code enregistrement typeOperation 21 / 22 / 23 / 24
                getChamp(3); //Code devise
                cheque.setDevise("GNF");

                cheque.setCalcul("0" + getChamp(1)); //Champ Calcul //Nature du chèque 01 : cheque personnel 02 : cheque d?entreprise 03 : Chèque de Banque
                cheque.setMontantcheque(String.valueOf(Long.parseLong(getChamp(15)))); //Montant
                // getChamp(2);//Decimal 00
                cheque.setNumerocheque(getChamp(8)); //Numero du chque
                cheque.setAgenceremettant(getChamp(3)); //Code Agence remettante
                getChamp(8); //Date remise
                cheque.setBanque(getChamp(3)); //compte du tire sur 18 pos bnk+agence+compte+rib
                cheque.setAgence(getChamp(3));
                cheque.setNumerocompte(getChamp(10));
                cheque.setRibcompte(getChamp(2));
                cheque.setNomemetteur(Utility.removeAccent(getChamp(30))); //Nom ou raison sociale du Tireur
                getChamp(30); //Adresse du Tireur
                getChamp(3); //Code participant destinataire
                getChamp(2);//Code pays du participant destinataire 
                cheque.setBanqueremettant(getChamp(3)); //Compte du beneficiaire sur 18 pos bnk+agence+compte+rib
                cheque.setAgenceremettant(getChamp(3));
                cheque.setCompteremettant(getChamp(10));
                cheque.setVilleremettant(getChamp(2));
                cheque.setNombeneficiaire(Utility.removeAccent(getChamp(30))); //Nom ou raison sociale du beneficiaire
                getChamp(30); //Adresse du beneficiaire
                cheque.setDateemission(Utility.convertDateToString(Utility.convertStringToDate(getChamp(8), "ddMMyyyy"), ResLoader.getMessages("patternDate"))); //Date demission du chque
                getChamp(15);//Motif representation
                getChamp(8);//Date de valeur
                getChamp(6);//Motif rejet
                cheque.setMotifrejet(getChamp(2)); //Motif rejet
                getChamp(23); //Zone libre

                cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERET")));
                cheque.setDatetraitement(Utility.convertDateToString(new Date(System.currentTimeMillis()), ResLoader.getMessages("patternDate")));
                cheque.setOrigine(new BigDecimal(0));
                cheque.setEtablissement(Utility.getParam("CODE_BANQUE"));

                //
                if (typeOperation.equals("21")) { //-	Remises de presentation d'operations 
                    //MAJ ou INSERTION
                    System.out.println("RCPChequeRetourReader requete ");
                    String sql = "SELECT * FROM CHEQUES WHERE "
                            + " TRIM(BANQUE) ='" + cheque.getBanque().trim() + "'"
                            + " AND TRIM(AGENCE) ='" + cheque.getAgence().trim() + "'"
                            + " AND TRIM(NUMEROCOMPTE) ='" + cheque.getNumerocompte().trim() + "'"
                            + " AND TRIM(NUMEROCHEQUE) ='" + cheque.getNumerocheque().trim() + "'"
                            + " AND TRIM(BANQUEREMETTANT) ='" + cheque.getBanqueremettant().trim() + "'"
                            + " AND TRIM(COMPTEREMETTANT) ='" + cheque.getCompteremettant().trim() + "'"
                            + " AND TRIM(AGENCEREMETTANT) ='" + cheque.getAgenceremettant().trim() + "'"
                            + " AND TRIM(MONTANTCHEQUE) ='" + cheque.getMontantcheque().trim() + "'";
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    //   System.out.println("Test voir si le cheque existe en Base de Données;  si oui, mise a jour avec les chemins des images etat passe a 152. Si non, insertion d'une ligne de cheque retour a 52");

                    if (cheques != null && cheques.length > 0) {
                        System.out.println("Cheque Existe RCPChequeRetourReader requete");
                        // aCheque.setEtat(new BigDecimal(Integer.parseInt(Utility.getParam("CETAOPERETREC"))));

                        //Cheque existe deja
                        cheque.setPathimage(cheques[0].getPathimage());
                        cheque.setFichierimage(cheques[0].getFichierimage());
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERETREC")));
                        cheque.setIdcheque(cheques[0].getIdcheque()); //cheques[0].getIdcheque()

                        sql = " IDCHEQUE =" + cheques[0].getIdcheque();
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", sql);

                    } else {
                        System.out.println("Cheque n'existe pas RCPChequeRetourReader requete");
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPERET")));
                        cheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                        db.insertObjectAsRowByQuery(cheque, "CHEQUES");

                    }

                }

                if (typeOperation.equals("22")) { //-	Remises de rejets d'operations prealablement recues 
                    //Cheque Aller rjt
                    System.out.println("rejets doperations prealablement recues "); // les etats des ordres sont CETAOPEALLICOM1ACCENVSIB ou 
                    String sql = "SELECT * FROM CHEQUES WHERE "
                            + " TRIM(BANQUE) ='" + cheque.getBanque().trim() + "'"
                            + " AND TRIM(AGENCE) ='" + cheque.getAgence().trim() + "'"
                            + " AND TRIM(NUMEROCOMPTE) ='" + cheque.getNumerocompte().trim() + "'"
                            + " AND TRIM(NUMEROCHEQUE) ='" + cheque.getNumerocheque().trim() + "'"
                            + " AND TRIM(BANQUEREMETTANT) ='" + cheque.getBanqueremettant().trim() + "'"
                            + " AND TRIM(COMPTEREMETTANT) ='" + cheque.getCompteremettant().trim() + "'"
                            + " AND TRIM(AGENCEREMETTANT) ='" + cheque.getAgenceremettant().trim() + "'"
                            + " AND TRIM(MONTANTCHEQUE) ='" + cheque.getMontantcheque().trim() + "'"
                            + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "";
//                    String sql = "SELECT * FROM CHEQUES WHERE "
//                            + " REMCOM=" + cheque.getRemcom() + "  AND"
//                            + " DATEEMISSION='" + cheque.getDateemission() + "' "
//                            + " AND BANQUE ='" + cheque.getBanque() + "' AND NUMEROCHEQUE ='" + cheque.getNumerocheque() + "' "
//                            + " AND NUMEROCOMPTE ='" + cheque.getNumerocompte() + "' AND MONTANTCHEQUE ='" + cheque.getMontantcheque() + "' "
//                            + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM1ACCENVSIB") + "";
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                    if (cheques != null && cheques.length > 0) {
                        cheques[0].setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEREJRET"))));
                        cheques[0].setMotifrejet(cheque.getMotifrejet());
                        sql = " IDCHEQUE =" + cheques[0].getIdcheque();
                        db.updateRowByObjectByQuery(cheques[0], "CHEQUES", sql);
                        //C'est un Cheque Aller rejeté
                    }

                }
                if (typeOperation.equals("23")) {//REMISE d'annulations doperations presentees 
                    //Pour annuler un cheque UBA presenté par le confrere CETAOPERET & CETAOPERETREC sont les etats possible des ordres
                    String sql = "SELECT * FROM CHEQUES WHERE "
                            + " TRIM(BANQUE) ='" + cheque.getBanque().trim() + "'"
                            + " AND TRIM(NUMEROCHEQUE) ='" + cheque.getNumerocheque().trim() + "'"
                            + " AND TRIM(NUMEROCOMPTE) ='" + cheque.getNumerocompte().trim() + "'"
                            + " AND TRIM(MONTANTCHEQUE) ='" + cheque.getMontantcheque().trim() + "'"
                            + " AND ETAT IN (" + Utility.getParam("CETAOPERET") + ", " + Utility.getParam("CETAOPERETREC") + "  ) ";
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                    if (cheques != null && cheques.length > 0) {
                        cheques[0].setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEANO"))));
                        sql = " IDCHEQUE =" + cheques[0].getIdcheque();
                        db.updateRowByObjectByQuery(cheques[0], "CHEQUES", sql);
                    }
                }

                if (typeOperation.equals("24")) {//	Remises d'annulations d'operations rejetees 
                    //le confrere veut Annuler  un rejet qu'il a fait 
                    String sql = "SELECT * FROM CHEQUES WHERE "
                            + " TRIM(BANQUE) ='" + cheque.getBanque().trim() + "'"
                            + " AND TRIM(AGENCE) ='" + cheque.getAgence().trim() + "'"
                            + " AND TRIM(NUMEROCOMPTE) ='" + cheque.getNumerocompte().trim() + "'"
                            + " AND TRIM(NUMEROCHEQUE) ='" + cheque.getNumerocheque().trim() + "'"
                            + " AND TRIM(BANQUEREMETTANT) ='" + cheque.getBanqueremettant().trim() + "'"
                            + " AND TRIM(COMPTEREMETTANT) ='" + cheque.getCompteremettant().trim() + "'"
                            + " AND TRIM(AGENCEREMETTANT) ='" + cheque.getAgenceremettant().trim() + "'"
                            + " AND TRIM(MONTANTCHEQUE) ='" + cheque.getMontantcheque().trim() + "'"
                            + " AND ETAT =" + Utility.getParam("CETAOPEREJRET") + "";

                    /**
                     * String sql = "SELECT * FROM CHEQUES WHERE" + " REMCOM=" +
                     * cheque.getRemcom() + " " + " AND DATEEMISSION='" +
                     * cheque.getDateemission() + "' AND BANQUE ='" +
                     * cheque.getBanque() + "' AND NUMEROCHEQUE ='" +
                     * cheque.getNumerocheque() + "' AND NUMEROCOMPTE ='" +
                     * cheque.getNumerocompte() + "' AND MONTANTCHEQUE ='" +
                     * cheque.getMontantcheque() + "'" + " AND ETAT =" +
                     * Utility.getParam("CETAOPEREJRET") + "";
                     *
                     */
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                    if (cheques != null && cheques.length > 0) {
                        cheques[0].setEtat(new BigDecimal(Long.parseLong(Utility.getParam("CETAOPEALLICOM1ACCENVSIB"))));
                        cheques[0].setMotifrejet(cheque.getMotifrejet());
                        sql = " IDCHEQUE =" + cheques[0].getIdcheque();
                        db.updateRowByObjectByQuery(cheques[0], "CHEQUES", sql);
                    }

                }

            }

        }
        System.out.println("INSERTION DES CHK RETOUR TERMINEE");
        //Aller chercher le fichier .CAT et composer l'image
        db.close();
        closeFile();
        return aFile;
//        } else {
//            return null;
//        }

    }

}
