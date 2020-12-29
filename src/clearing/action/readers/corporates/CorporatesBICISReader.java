/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.readers.corporates;

import clearing.model.CMPUtility;
import clearing.table.Cheques;
import clearing.table.Remises;
import org.patware.action.file.FlatFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import org.apache.commons.io.FileUtils;

import org.patware.bean.table.Repertoires;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class CorporatesBICISReader extends FlatFileReader {

    @Override
    public File treatFile(File aFile, Repertoires repertoire) throws Exception {
        setFile(aFile);
        String imgAllerFolder = Utility.getParam("IMG_ALLER_FOLDER");
        String imgAllerCorp = Utility.getParam("IMG_ALLER_CORP");
        System.out.println("imgAllerCorp " + imgAllerCorp);
        String srvRootFilePath = imgAllerFolder + File.separator + "CORP_BICIS" + File.separator + CMPUtility.getDate();
        String srvChequeFilePath = srvRootFilePath + File.separator + "CHEQUES";
        String pathimage = "";
        String fichierimage = "";
        String line = null;

        Cheques aCheque = new Cheques();
        Remises aRemise = new Remises();

        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        BufferedReader is = openFile(aFile);
        while ((line = is.readLine()) != null) {
            setCurrentLine(line);
            if (line.startsWith("#dataAndImage") || line.startsWith("L")) {
                System.out.println("Nothing to do. Entete");
            } else {
                String[] champs = line.split(";");
                String typeEnreg = getChamp(1);
                String idRemiseCorp;
                if (typeEnreg.equals("R")) {
                    //R;01768;2178;1;0;0956112349400041; ; ;RUFSAC CI                          ;XOF;1;35540;XOF;0;20180817;01768;N;1;4;0;

                    aRemise.setAgenceDepot(champs[1]);
                    aRemise.setEtablissement(aRemise.getAgenceDepot());
                    idRemiseCorp = champs[3];

                    aRemise.setCompteRemettant(champs[5]);
                    aRemise.setNomClient(champs[8]);
                    aRemise.setNomUtilisateur("CORPORATES");
                    aRemise.setNbOperation(new BigDecimal(champs[10]));
                    aRemise.setMontant(String.valueOf(Long.parseLong(champs[11])));
                    aRemise.setDateSaisie(Utility.convertDateToString(Utility.convertStringToDate(champs[14], "yyyyMMdd"), "yyyy/MM/dd")); //aFile.getName().substring(13, 21) 20170606
                    aRemise.setAgenceRemettant(champs[15]);
                    if (Utility.getParam("VALIDATION_CORP_AUTO") != null && Utility.getParam("VALIDATION_CORP_AUTO").equals("1")) {
                        aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPESUPVAL")));
                    } else {
                        aRemise.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                    }

                    aRemise.setDevise(CMPUtility.getDevise());
                    aRemise.setSequence(new BigDecimal(Utility.computeCompteur("IDREMISE", "REMISE")));
                    aRemise.setIdremise(aRemise.getSequence());
                    db.insertObjectAsRowByQuery(aRemise, "REMISES");

                }
                if (typeEnreg.equals("C")) {
                    aCheque.setRemise(aRemise.getIdremise());
                    aCheque.setEtablissement(aRemise.getEtablissement());
                    aCheque.setEscompte(BigDecimal.ZERO);
                    aCheque.setBanqueremettant(Utility.getParam("CODE_BANQUE_SICA3"));
                    aCheque.setAgenceremettant(aRemise.getAgenceRemettant());
                    aCheque.setCompteremettant(aRemise.getCompteRemettant());
                    aCheque.setNumerocheque(champs[4]);
                    aCheque.setBanque(Utility.getParamNameOfType(champs[5].substring(0, 2), "CODE_SICA3") + "" + champs[5].substring(2)); //
                    aCheque.setAgence(champs[6]);
                    aCheque.setNumerocompte(champs[7]);
                    aCheque.setNombeneficiaire(aRemise.getNomClient());
                    aCheque.setRibcompte(champs[8]);
                    aCheque.setMontantcheque(champs[9]);
                    aCheque.setVilleremettant("01");
                    aCheque.setDatetraitement(Utility.convertDateToString(Utility.convertStringToDate(champs[12], "yyyyMMdd"), ResLoader.getMessages("patternDate")));
                    aCheque.setDatesaisie(aCheque.getDatetraitement());
                    String path = imgAllerCorp + File.separator + champs[12] + File.separator + champs[1] + File.separator + champs[2] + File.separator + champs[3];
                    System.out.println("path " + path);
                    File frontImage = new File(path + File.separator + champs[18]);
                    File rearImage = new File(path + File.separator + champs[19]);
                    System.out.println("frontImage" + frontImage);
                    System.out.println("rearImage" + rearImage);

                    pathimage = srvChequeFilePath + File.separator + aCheque.getBanque() + File.separator + aCheque.getAgence() + File.separator;

                    File waitFolder = new File(pathimage + File.separator);
                    if (!waitFolder.exists()) {
                        if (!waitFolder.mkdirs()) {
                            logEvent("ERREUR", "Impossible de créer " + pathimage);

                        }
                    }
                    aCheque.setPathimage(pathimage);
                    fichierimage = aCheque.getNumerocompte() + aCheque.getRibcompte() + aCheque.getNumerocheque() + "_" + CMPUtility.getDateHeure();
                    if (frontImage.exists()) {
                        System.out.println("frontImage Exist");
                        FileUtils.copyFile(frontImage, new File(pathimage + fichierimage + "f.jpg"));
                    }
                    if (rearImage.exists()) {
                        System.out.println("rearImage Exist");
                        FileUtils.copyFile(rearImage, new File(pathimage + fichierimage + "r.jpg"));
                        aCheque.setFichierimage(fichierimage);
                    }

                    aCheque.setDateemission(Utility.convertDateToString(Utility.convertStringToDate(champs[12], "yyyyMMdd"), ResLoader.getMessages("patternDate")));

                    aCheque.setType_Cheque("035");
                    aCheque.setCodeutilisateur("CORPORATES");
                    aCheque.setDevise(CMPUtility.getDevise());

                    aCheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMASTO")));
                    aCheque.setOrigine(new BigDecimal(0));
                    if (Utility.getParam("VALIDATION_CORP_AUTO") != null && Utility.getParam("VALIDATION_CORP_AUTO").equals("1")) {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPESUPVAL")));
                    } else {
                        aCheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEVAL")));
                    }

                    aCheque.setRemise(aRemise.getIdremise());
                    aCheque.setIdcheque(new BigDecimal(Utility.computeCompteur("IDCHEQUE", "CHEQUES")));
                    db.insertObjectAsRowByQuery(aCheque, "CHEQUES");
                    fichierimage = "";
                }

            }
        }
        db.close();
        return aFile;
    }

}
