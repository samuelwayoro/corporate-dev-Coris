/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.bfi;

import clearing.model.CMPUtility;
import clearing.model.EnteteRemise;
import clearing.model.ImageIndex;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Effets;
import java.io.BufferedInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import org.patware.action.file.BinFileWriter;
import static org.patware.action.file.BinFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class PakAcpAchWriter extends BinFileWriter {

    private String sql = "";
    private String sequence = "";
    EnteteRemise enteteRemise = new EnteteRemise();
    private byte[] rImageBytes;
    private byte[] vImageBytes;
    private byte[] imageBytes;
    private ImageIndex imageIndex = new ImageIndex();
//    Cheques cheques[] = null;
    Cheques cheques35[] = null;
    Effets effets40[] = null;
    Effets effets41[] = null;
    Effets effets42[] = null;
    Effets effets43[] = null;
    Effets effets45[] = null;
    Effets effets46[] = null;
    private boolean remiseHasCheques = false;
    private boolean remiseHasEffets = false;
    private DataBase db;
    private Cheques[] cheques = null;
    private String fileName;

    public PakAcpAchWriter(Cheques[] cheques, String fileName) {
        System.setProperty("file.encoding", "ISO-8859-1");
        this.cheques = cheques;
        this.fileName = fileName;
    }

    public PakAcpAchWriter() {

        setDescription("Envoi PAK vers ADT");
        System.setProperty("file.encoding", "ISO-8859-1");
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());

        if (cheques != null) {
            prepareChequesAcpAch(cheques, fileName + ".PAK");
//            setOut(createBinFile(fileName + ".PAK"));
//
//            prepareChequesAcpAch(cheques, fileName + ".PAK");
        }
        updateEtatOperations(db);
        db.close();
        logEndOfTask();
    }

    private void prepareChequesAcpAch(Cheques[] cheques, String fileName) throws Exception {
        System.out.println("prepareChequesAcpAch");

        if (cheques != null && cheques.length > 0) {
            remiseHasCheques = true;
            enteteRemise.setIdEntete("EIMG");
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdEmetteur(CMPUtility.getCodeBanque().charAt(0) + "SCPM");
            } else {
                enteteRemise.setIdEmetteur(CMPUtility.getCodeBanqueSica3().substring(0, 2) + Utility.getParam("CONSTANTE_SICA").trim());
            }
            sequence = Utility.bourrageGZero(Utility.computeCompteur("MAILI_NAT", Utility.getParam("DATECOMPENS_NAT")), 3);
            enteteRemise.setRefRemise(sequence);
            enteteRemise.setDatePresentation(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"));
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanque());
            } else {
                enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanqueSica3());
            }
            enteteRemise.setDevise(CMPUtility.getDevise());
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setTypeRemise("IMAGC");
            } else {
                enteteRemise.setTypeRemise("IMC");
            }

            enteteRemise.setIdDestinataire(CMPUtility.getCodeBanqueDestinataire(cheques[0].getBanque()));
            enteteRemise.setCodeLieu(cheques[0].getVille());
            enteteRemise.setNbTotOperVal(String.valueOf(cheques.length));

            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setBlancs(createBlancs(20, " "));
            } else {
                enteteRemise.setBlancs(createBlancs(22, " "));
            }
            CMPUtility.insertRemcom(enteteRemise, Integer.parseInt(Utility.getParam("CETAOPEALLICOM1ACC")));

            //    String fileName = CMPUtility.getMailiNatFileName(enteteRemise.getIdDestinataire(), enteteRemise.getCodeLieu(), sequence, "IMC", "MAILI");
            setOut(createBinFile(fileName));
            String line = new String(enteteRemise.getIdEntete()
                    + enteteRemise.getIdEmetteur()
                    + enteteRemise.getRefRemise()
                    + Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd")
                    + enteteRemise.getIdRecepeteur()
                    + enteteRemise.getDevise()
                    + enteteRemise.getTypeRemise()
                    + enteteRemise.getIdDestinataire()
                    + enteteRemise.getCodeLieu()
                    + enteteRemise.getNbTotOperVal()
                    + enteteRemise.getBlancs());
//            getOut().println(line);
            imageIndex = new ImageIndex();

            File aRectoFile = null;
            File aVersoFile = null;
            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];

                imageIndex.setTypeOperation(cheque.getType_Cheque());
                imageIndex.setNumCheque(cheque.getNumerocheque());
                imageIndex.setPfxIBANDeb(createBlancs(4, " "));
                imageIndex.setIdBanTire(cheque.getBanque());
                imageIndex.setIdAgeTire(cheque.getAgence());
                imageIndex.setNumCptTire(cheque.getNumerocompte());
                imageIndex.setCleRibDeb(cheque.getRibcompte());
                imageIndex.setIdBanRem(cheque.getBanqueremettant());
                imageIndex.setIdAgeRem(cheque.getAgenceremettant());
                imageIndex.setRio(new RIO("BF083BF1XOF004201611020000500002694"));
                imageIndex.setDateOrdreClient(Utility.convertStringToDate(cheque.getDatetraitement(), ResLoader.getMessages("patternDate")));
                aRectoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "f.jpg");
                imageIndex.setLongRecto(String.valueOf(aRectoFile.length()));
                aVersoFile = new File(cheque.getPathimage() + File.separator + cheque.getFichierimage() + "r.jpg");
                imageIndex.setLongVerso(String.valueOf(aVersoFile.length()));
                imageIndex.setMontant(cheque.getMontantcheque());
                imageIndex.setCodeCmc7(String.valueOf(cheque.getIndicateurmodificationcmc7()));

                rImageBytes = new byte[(int) aRectoFile.length()];
                BufferedInputStream isRecto = openFile(aRectoFile);
                isRecto.read(rImageBytes);
                vImageBytes = new byte[(int) aVersoFile.length()];
                BufferedInputStream isVerso = openFile(aVersoFile);
                isVerso.read(vImageBytes);
                Boolean mayTattoo = Boolean.valueOf(Utility.getParam("TATOO_PICTURE"));

                isVerso.close();
                isRecto.close();

//                getOut().println(line);
                getOut().write(rImageBytes);
//                getOut().println();
                getOut().write(vImageBytes);
//                getOut().println();
                cheque.setEtatimage(new BigDecimal(Utility.getParam("CETAIMATRA")));
                cheque.setFichiermaili(fileName);

                db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE = " + cheque.getIdcheque());

            }

            getOut().close();
        }
        System.out.println("Fin prepareChequesAcpAch");
    }

    private void prepareEffets(Effets[] effets) throws Exception {

        if (cheques != null && effets.length > 0) {
            remiseHasEffets = true;
            enteteRemise.setIdEntete("EIMG");
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdEmetteur(CMPUtility.getCodeBanque().charAt(0) + "SCPM");
            } else {
                enteteRemise.setIdEmetteur(CMPUtility.getCodeBanqueSica3().substring(0, 2) + Utility.getParam("CONSTANTE_SICA").trim());
            }
            sequence = Utility.bourrageGZero(Utility.computeCompteur("MAILI_NAT", Utility.getParam("DATECOMPENS_NAT")), 3);
            enteteRemise.setRefRemise(sequence);
            enteteRemise.setDatePresentation(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"));
            if (Utility.getParam("VERSION_SICA").equals("2")) {
                enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanque());
            } else {
                enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanqueSica3());
            }
            enteteRemise.setDevise(CMPUtility.getDevise());
            enteteRemise.setTypeRemise("IMAGF");
            enteteRemise.setIdDestinataire(effets[0].getBanque());
            enteteRemise.setCodeLieu(effets[0].getVille());
            enteteRemise.setNbTotOperVal(String.valueOf(effets.length));
            enteteRemise.setBlancs(createBlancs(20, " "));

            String fileName = CMPUtility.getMailiNatFileName(enteteRemise.getIdDestinataire(), enteteRemise.getCodeLieu(), sequence, "IME", "MAILI");
            setOut(createBinFile(fileName));
            String line = new String(enteteRemise.getIdEntete() + enteteRemise.getIdEmetteur() + enteteRemise.getRefRemise() + Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd") + enteteRemise.getIdRecepeteur() + enteteRemise.getDevise() + enteteRemise.getTypeRemise() + enteteRemise.getIdDestinataire() + enteteRemise.getCodeLieu() + enteteRemise.getNbTotOperVal() + enteteRemise.getBlancs());
            getOut().println(line);
            imageIndex = new ImageIndex();

            for (int j = 0; j < effets.length; j++) {
                Effets effet = effets[j];

                imageIndex.setTypeOperation(effet.getType_Effet());
                imageIndex.setNumCheque(effet.getNumeroeffet());
                imageIndex.setPfxIBANDeb(effet.getIban_Tire());
                imageIndex.setIdBanTire(effet.getBanque());
                imageIndex.setIdAgeTire(effet.getAgence());
                imageIndex.setNumCptTire(effet.getNumerocompte_Tire());
                imageIndex.setCleRibDeb(effet.getZoneinterbancaire_Tire());
                imageIndex.setIdBanRem(effet.getBanqueremettant());
                imageIndex.setIdAgeRem(effet.getAgenceremettant());
                imageIndex.setRio(new RIO(effet.getRio()));
                imageIndex.setDateOrdreClient(Utility.convertStringToDate(effet.getDatetraitement(), ResLoader.getMessages("patternDate")));
                File aRectoFile = new File(effet.getPathimage() + File.pathSeparator + effet.getFichierimage() + "f.jpg");
                imageIndex.setLongRecto(String.valueOf(aRectoFile.length()));
                File aVersoFile = new File(effet.getPathimage() + File.pathSeparator + effet.getFichierimage() + "r.jpg");
                imageIndex.setLongVerso(String.valueOf(aVersoFile.length()));
                imageIndex.setMontant(effet.getMontant_Effet());
                imageIndex.setCodeCmc7("0");
                line = new String(imageIndex.getTypeOperation() + createBlancs(7, "0") + imageIndex.getNumEffet()
                        + "0" + imageIndex.getPfxIBANDeb() + imageIndex.getIdBanTire() + imageIndex.getIdAgeTire()
                        + imageIndex.getNumCptTire() + imageIndex.getCleRibDeb() + imageIndex.getIdBanRem()
                        + imageIndex.getIdAgeRem() + imageIndex.getRio().getRio() + imageIndex.getDateOrdreClient()
                        + imageIndex.getLongRecto() + imageIndex.getLongVerso() + imageIndex.getMontant()
                        + imageIndex.getCodeCmc7() + createBlancs(263, " "));
                getOut().println(line);
                imageBytes = new byte[(int) aRectoFile.length()];
                BufferedInputStream isRecto = openFile(aRectoFile);
                isRecto.read(imageBytes);
                getOut().write(imageBytes);
                getOut().println();
                imageBytes = new byte[(int) aVersoFile.length()];
                BufferedInputStream isVerso = openFile(aVersoFile);
                isVerso.read(imageBytes);
                getOut().write(imageBytes);
                getOut().println();
                effet.setEtatimage(new BigDecimal(Utility.getParam("CETAIMATRA")));
                db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET = " + effet.getIdeffet());

            }

            getOut().close();
        }
    }

    private void updateEtatOperations(DataBase db) throws SQLException {
        String l_sql = "";

        if (remiseHasCheques) {
            l_sql = "UPDATE CHEQUES SET ETATIMAGE=" + Utility.getParam("CETAIMAENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM1ACC") + " AND ETATIMAGE=" + Utility.getParam("CETAIMATRA");
            db.executeUpdate(l_sql);
        }

        if (remiseHasEffets) {
            l_sql = "UPDATE EFFETS SET ETATIMAGE=" + Utility.getParam("CETAIMAENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM1ACC") + " AND ETATIMAGE=" + Utility.getParam("CETAIMATRA");
            db.executeUpdate(l_sql);
        }

    }

    public Cheques[] getCheques() {
        return cheques;
    }

    public void setCheques(Cheques[] cheques) {
        this.cheques = cheques;
    }
}
