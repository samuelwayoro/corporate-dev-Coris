/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.action.writers.sica;

import clearing.model.CMPUtility;
import clearing.model.EnteteLot;
import clearing.model.EnteteRemise;
import clearing.model.Operation;
import clearing.model.RIO;
import clearing.table.Cheques;
import clearing.table.Effets;
import clearing.table.Lotcom;
import clearing.table.Prelevements;
import clearing.table.Remcom;
import clearing.table.Virements;
import clearing.utils.StaticValues;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.action.file.FlatFileWriter;
import static org.patware.action.file.FlatFileWriter.createBlancs;
import org.patware.jdbc.DataBase;
import org.patware.utils.ResLoader;
import org.patware.utils.Utility;
import org.patware.xml.JDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class Icom3NatWriter extends FlatFileWriter {

    private String sql = "";
    private int refLot = 0;
    private Vector<EnteteLot> vEnteteLots = new Vector<EnteteLot>();
    private String sequence = null;
    EnteteRemise enteteRemise = new EnteteRemise();
    Remcom remcom = null;
    Lotcom lotcom = null;
    private boolean remiseHasRemcom = false;
    private boolean remiseHasLotcom = false;
    private boolean remiseHasCheques = false;
    private boolean remiseHasEffets = false;
    private boolean remiseHasVirements = false;
    private boolean remiseHasPrelevements = false;

    public Icom3NatWriter() {
        setDescription("Envoi ICOM3 National vers la BCEAO ");
    }

    @Override
    public void execute() throws Exception {

        super.execute();
        DataBase db = new DataBase(JDBCXmlReader.getDriver());
        db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
        if (Utility.getParam("ENVOI_ICOM_NAT") == null || Utility.getParam("ENVOI_ICOM_NAT").equals("0")) {

            if (Utility.getParam("ATTENTE_ICOMA_NAT") == null || Utility.getParam("ATTENTE_ICOMA_NAT").equals("0")) {
                if (Utility.getParam("ENVOI_ICOM_NAT") != null) {
                    db.executeUpdate("UPDATE PARAMS SET VALEUR='1' WHERE NOM='ENVOI_ICOM_NAT'");
                    Utility.clearParamsCache();
                }

                prepareRemiseAnnulation();
                annulationTotale(db);
                updateEtatOperations(db);
            } else {
                logEvent("WARNING", "Le Système est en attente d'un ICOMA|ATTENTE_ICOMA_NAT=1");
                setDescription(getDescription() + " - WARNING: Le Système est en attente d'un ICOMA|ATTENTE_ICOMA_NAT=1");
            }
        } else {
            logEvent("WARNING", "Un ICOM3 est en cours d'envoi|ENVOI_ICOM_NAT=1");
            setDescription(getDescription() + " - WARNING: Un ICOM3 est en cours d'envoi|ENVOI_ICOM_NAT=1");
        }

        if (remiseHasRemcom || remiseHasLotcom) {
            if (Utility.getParam("ATTENTE_ICOMA_NAT") != null) {
                db.executeUpdate("UPDATE PARAMS SET VALEUR='1' WHERE NOM='ATTENTE_ICOMA_NAT'");
                Utility.clearParamsCache();
            }
            if (Utility.getParam("ENVOI_ICOM_NAT") != null) {
                db.executeUpdate("UPDATE PARAMS SET VALEUR='0' WHERE NOM='ENVOI_ICOM_NAT'");
                Utility.clearParamsCache();
            }
        }

        //Methode pour donner le reste a annuler
        db.close();
    }

    public void prepareRemiseAnnulation() {
        enteteRemise = new EnteteRemise();
        sequence = Utility.bourrageGZero(Utility.computeCompteur("ICOM_NAT", Utility.getParam("DATECOMPENS_NAT")), 3);
        enteteRemise.setIdEntete("EREM");
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            enteteRemise.setIdEmetteur(CMPUtility.getCodeBanque().charAt(0) + "SCPM");
        } else {
            enteteRemise.setIdEmetteur(CMPUtility.getCodeBanqueSica3().substring(0, 2) + Utility.getParam("CONSTANTE_SICA").trim());
        }
        enteteRemise.setRefRemise(sequence);
        enteteRemise.setDatePresentation(Utility.convertStringToDate(Utility.getParam("DATECOMPENS_NAT"), "yyyyMMdd"));
        if (Utility.getParam("VERSION_SICA").equals("2")) {
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanque());
        } else {
            enteteRemise.setIdRecepeteur(CMPUtility.getCodeBanqueSica3());
        }
        enteteRemise.setDevise(CMPUtility.getDevise());
        enteteRemise.setTypeRemise("ICOM3");
        enteteRemise.setRefRemRelatif("000");
        enteteRemise.setNbLots("00");
        enteteRemise.setBlancs(createBlancs(15, " "));
    }

    public void annulationTotale(DataBase db) {
        String whereClause = " AND IDEMETTEUR NOT IN ('SNSSR','KSCSR')";

        // Cas d'une annulation totale de remise
        sql = "SELECT * FROM REMCOM WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + whereClause;
        Remcom[] remcoms = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());
        if (remcoms != null && remcoms.length > 0) {
            remiseHasRemcom = true;
            enteteRemise.setRefRemRelatif(Utility.bourrageGZero(remcoms[0].getRefremise(), 3));
        } else {
            BigDecimal remcomValue = null;
            // Cas d'une annulation totale de lot
            sql = "SELECT * FROM LOTCOM WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "";
            Lotcom[] lotcoms = (Lotcom[]) db.retrieveRowAsObject(sql, new Lotcom());
            if (lotcoms != null && lotcoms.length > 0) {

                sql = "SELECT * FROM REMCOM WHERE IDREMCOM = " + lotcoms[0].getIdremcom() + whereClause;
                remcoms = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());
                if (remcoms != null && remcoms.length > 0) {
                    remiseHasLotcom = true;
                    enteteRemise.setRefRemRelatif(Utility.bourrageGZero(remcoms[0].getRefremise(), 3));
                }
                enteteRemise.setNbLots(Utility.bourrageGZero("" + lotcoms.length, 2));
                //Remplissage des lots
                for (int i = 0; i < lotcoms.length; i++) {
                    EnteteLot enteteLot = new EnteteLot();
                    enteteLot.setIdEntete("ELOT");
                    enteteLot.setRefLot("" + ++refLot);
                    enteteLot.setRefBancaire(lotcoms[i].getRefbancaire());
                    enteteLot.setTypeOperation(lotcoms[i].getTypeoperation());
                    enteteLot.setIdBanRem(lotcoms[i].getIdbanrem());
                    enteteLot.setNbOperations("0000");
                    enteteLot.setMontantTotal(createBlancs(16, "0"));
                    enteteLot.setBlancs(createBlancs(24, " "));
                    vEnteteLots.add(enteteLot);
                }
            } else {
                //Cas d'annulation ordres
                sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " ORDER BY remcom ,lotcom ";
                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                int j = 0;
                if (cheques != null && cheques.length > 0) {
                    System.out.println("Cheques annulation");
                    remiseHasLotcom = true;
                    remcomValue = cheques[0].getRemcom();
                    sql = "SELECT * FROM CHEQUES WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue + "  ORDER BY remcom ,lotcom ";;
                    cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    for (int i = 0; i < cheques.length; i += j) {
                        //Tous les cheques validés

                        sql = "SELECT * FROM CHEQUES WHERE LOTCOM=" + cheques[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Cheques[] chequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());

                        sql = "SELECT * FROM LOTCOM WHERE IDLOTCOM=" + cheques[i].getLotcom();
                        lotcoms = (Lotcom[]) db.retrieveRowAsObject(sql, new Lotcom());

                        sql = "SELECT * FROM CHEQUES WHERE LOTCOM=" + cheques[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Cheques[] allChequesVal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        try {
                            prepareCheques(allChequesVal, lotcoms[0]);

                        } catch (NumberFormatException | SQLException ex) {
                            Logger.getLogger(Icom3NatWriter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        j = chequesVal.length;

                        //CETAOPEALLICOM3TRAN Mise a jour transitoire
                    }
                    sql = "SELECT * FROM REMCOM WHERE IDREMCOM = " + cheques[0].getRemcom() + whereClause;
                    remcoms = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());
                    if (remcoms != null && remcoms.length > 0) {

                        enteteRemise.setRefRemRelatif(Utility.bourrageGZero(remcoms[0].getRefremise(), 3));
                    }
                    for (Cheques cheque : cheques) {
                        //CETAOPEALLICOM3TRAN Mise a jour transitoire
                        cheque.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM3TRAN")));
                        db.updateRowByObjectByQuery(cheque, "CHEQUES", "IDCHEQUE=" + cheque.getIdcheque());
                    }
                }
                if (remcomValue == null) {
                    sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRSTACLINOUNOR") + "'  ORDER BY remcom ,lotcom ";
                } else {
                    sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRSTACLINOUNOR") + "'  ORDER BY remcom ,lotcom ";

                }
                Virements[] virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                j = 0;
                if (virements != null && virements.length > 0) {
                    System.out.println("Virements annulation");
                    remcomValue = virements[0].getRemcom();
                    remiseHasLotcom = true;
                    sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " "
                            + " AND REMCOM = " + remcomValue + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRSTACLINOUNOR") + "'  ORDER BY remcom ,lotcom ";
                    virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                    for (int i = 0; i < virements.length; i += j) {
                        //Tous les virements validés
                        System.out.println("remcomValue " + remcomValue);

                        sql = "SELECT * FROM VIREMENTS WHERE LOTCOM=" + virements[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Virements[] virementsVal = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                        sql = "SELECT * FROM LOTCOM WHERE IDLOTCOM=" + virements[i].getLotcom();
                        lotcoms = (Lotcom[]) db.retrieveRowAsObject(sql, new Lotcom());

                        sql = "SELECT * FROM VIREMENTS WHERE LOTCOM=" + virements[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Virements[] allVirementsVal = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                        try {
                            prepareVirements(allVirementsVal, lotcoms[0]);
                        } catch (NumberFormatException | SQLException ex) {
                            Logger.getLogger(Icom3NatWriter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        j = virementsVal.length;

                    }

                    sql = "SELECT * FROM REMCOM WHERE IDREMCOM = " + virements[0].getRemcom() + whereClause;
                    remcoms = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());
                    if (remcoms != null && remcoms.length > 0) {

                        enteteRemise.setRefRemRelatif(Utility.bourrageGZero(remcoms[0].getRefremise(), 3));
                    }
                    for (Virements virement : virements) {
                        //CETAOPEALLICOM3TRAN Mise a jour transitoire
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM3TRAN")));
                        db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                    }

                }
                if (remcomValue == null) {
                    sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRBANBANANCNOR") + "' ORDER BY remcom ,lotcom ";
                } else {
                    sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + "   AND TYPE_VIREMENT ='" + Utility.getParam("VIRBANBANANCNOR") + "'  ORDER BY remcom ,lotcom ";
                }

                virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                j = 0;
                if (virements != null && virements.length > 0) {
                    System.out.println("Virements annulation 1 ");
                    remcomValue = virements[0].getRemcom();
                    remiseHasLotcom = true;
                    sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + "   AND TYPE_VIREMENT ='" + Utility.getParam("VIRBANBANANCNOR") + "'  ORDER BY remcom ,lotcom ";
                    virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                    for (int i = 0; i < virements.length; i += j) {
                        //Tous les virements validés

                        sql = "SELECT * FROM VIREMENTS WHERE LOTCOM=" + virements[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Virements[] virementsVal = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                        sql = "SELECT * FROM LOTCOM WHERE IDLOTCOM=" + virements[i].getLotcom();
                        lotcoms = (Lotcom[]) db.retrieveRowAsObject(sql, new Lotcom());

                        sql = "SELECT * FROM VIREMENTS WHERE LOTCOM=" + virements[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Virements[] allVirementsVal = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                        try {
                            prepareVirements(allVirementsVal, lotcoms[0]);
                        } catch (NumberFormatException | SQLException ex) {
                            Logger.getLogger(Icom3NatWriter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        j = virementsVal.length;

                    }
                    sql = "SELECT * FROM REMCOM WHERE IDREMCOM = " + virements[0].getRemcom() + whereClause;
                    remcoms = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());
                    if (remcoms != null && remcoms.length > 0) {

                        enteteRemise.setRefRemRelatif(Utility.bourrageGZero(remcoms[0].getRefremise(), 3));
                    }
                    for (Virements virement : virements) {
                        //CETAOPEALLICOM3TRAN Mise a jour transitoire
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM3TRAN")));
                        db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                    }
                }
                if (remcomValue == null) {
                    sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRMISDISNOUNOR") + "'  ORDER BY remcom ,lotcom ";
                } else {
                    sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRMISDISNOUNOR") + "'  ORDER BY remcom ,lotcom ";
                }

                virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                j = 0;
                if (virements != null && virements.length > 0) {
                    System.out.println("Virements annulation 2");
                    remcomValue = virements[0].getRemcom();
                    sql = "SELECT * FROM VIREMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + " AND TYPE_VIREMENT ='" + Utility.getParam("VIRMISDISNOUNOR") + "'  ORDER BY remcom ,lotcom ";
                    virements = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                    remiseHasLotcom = true;
                    for (int i = 0; i < virements.length; i += j) {
                        //Tous les virements validés

                        sql = "SELECT * FROM VIREMENTS WHERE LOTCOM=" + virements[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Virements[] virementsVal = (Virements[]) db.retrieveRowAsObject(sql, new Virements());

                        sql = "SELECT * FROM LOTCOM WHERE IDLOTCOM=" + virements[i].getLotcom();
                        lotcoms = (Lotcom[]) db.retrieveRowAsObject(sql, new Lotcom());

                        sql = "SELECT * FROM VIREMENTS WHERE LOTCOM=" + virements[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Virements[] allVirementsVal = (Virements[]) db.retrieveRowAsObject(sql, new Virements());
                        try {
                            prepareVirements(allVirementsVal, lotcoms[0]);
                        } catch (NumberFormatException | SQLException ex) {
                            Logger.getLogger(Icom3NatWriter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        j = virementsVal.length;

                        System.out.println(" virements[i] lotcom " + virements[i].getLotcom() + "  virements[i] " + virements[i].getIdvirement());
                        //CETAOPEALLICOM3TRAN Mise a jour transitoire

                    }
                    sql = "SELECT * FROM REMCOM WHERE IDREMCOM = " + virements[0].getRemcom() + whereClause;
                    remcoms = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());
                    if (remcoms != null && remcoms.length > 0) {

                        enteteRemise.setRefRemRelatif(Utility.bourrageGZero(remcoms[0].getRefremise(), 3));
                    }
                    for (Virements virement : virements) {
                        //CETAOPEALLICOM3TRAN Mise a jour transitoire
                        virement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM3TRAN")));
                        db.updateRowByObjectByQuery(virement, "VIREMENTS", "IDVIREMENT=" + virement.getIdvirement());
                    }
                }
                if (remcomValue == null) {
                    sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " AND TYPE_EFFET ='" + Utility.getParam("BILORDNOUNOR") + "'  ORDER BY remcom ,lotcom ";
                } else {
                    sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + " AND TYPE_EFFET ='" + Utility.getParam("BILORDNOUNOR") + "' ORDER BY remcom ,lotcom ";
                }

                Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                j = 0;
                if (effets != null && effets.length > 0) {
                    System.out.println("EFFETS annulation");
                    remcomValue = effets[0].getRemcom();
                    remiseHasLotcom = true;
                    sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + " AND TYPE_EFFET ='" + Utility.getParam("BILORDNOUNOR") + "' ORDER BY remcom ,lotcom ";
                    effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                    for (int i = 0; i < effets.length; i += j) {
                        //Tous les effets validés

                        sql = "SELECT * FROM EFFETS WHERE LOTCOM=" + effets[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Effets[] effetsVal = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

                        sql = "SELECT * FROM LOTCOM WHERE IDLOTCOM=" + effets[i].getLotcom();
                        lotcoms = (Lotcom[]) db.retrieveRowAsObject(sql, new Lotcom());

                        sql = "SELECT * FROM EFFETS WHERE LOTCOM=" + effets[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Effets[] allEffetsVal = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        try {
                            prepareEffets(allEffetsVal, lotcoms[0]);
                        } catch (NumberFormatException | SQLException ex) {
                            Logger.getLogger(Icom3NatWriter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        j = effetsVal.length;

                    }
                    sql = "SELECT * FROM REMCOM WHERE IDREMCOM = " + effets[0].getRemcom() + whereClause;
                    remcoms = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());
                    if (remcoms != null && remcoms.length > 0) {

                        enteteRemise.setRefRemRelatif(Utility.bourrageGZero(remcoms[0].getRefremise(), 3));
                    }
                    for (Effets effet : effets) {
                        //CETAOPEALLICOM3TRAN Mise a jour transitoire
                        effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM3TRAN")));
                        db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                    }
                }
                if (remcomValue == null) {
                    sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " AND TYPE_EFFET ='" + Utility.getParam("LETCHANOUNOR") + "'  ORDER BY remcom ,lotcom ";
                } else {
                    sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + " AND TYPE_EFFET ='" + Utility.getParam("LETCHANOUNOR") + "'  ORDER BY remcom ,lotcom ";
                }

                effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                j = 0;
                if (effets != null && effets.length > 0) {
                    System.out.println("EFFETS annulation 1 ");
                    remcomValue = effets[0].getRemcom();
                    remiseHasLotcom = true;
                    sql = "SELECT * FROM EFFETS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + " AND TYPE_EFFET ='" + Utility.getParam("LETCHANOUNOR") + "'  ORDER BY remcom ,lotcom ";
                    effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                    for (int i = 0; i < effets.length; i += j) {
                        //Tous les effets validés

                        sql = "SELECT * FROM EFFETS WHERE LOTCOM=" + effets[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Effets[] effetsVal = (Effets[]) db.retrieveRowAsObject(sql, new Effets());

                        sql = "SELECT * FROM LOTCOM WHERE IDLOTCOM=" + effets[i].getLotcom();
                        lotcoms = (Lotcom[]) db.retrieveRowAsObject(sql, new Lotcom());

                        sql = "SELECT * FROM EFFETS WHERE LOTCOM=" + effets[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Effets[] allEffetsVal = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        try {
                            prepareEffets(allEffetsVal, lotcoms[0]);
                        } catch (NumberFormatException | SQLException ex) {
                            Logger.getLogger(Icom3NatWriter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        j = effetsVal.length;

                    }
                    sql = "SELECT * FROM REMCOM WHERE IDREMCOM = " + effets[0].getRemcom() + whereClause;
                    remcoms = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());
                    if (remcoms != null && remcoms.length > 0) {

                        enteteRemise.setRefRemRelatif(Utility.bourrageGZero(remcoms[0].getRefremise(), 3));
                    }
                    for (Effets effet : effets) {
                        //CETAOPEALLICOM3TRAN Mise a jour transitoire
                        effet.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM3TRAN")));
                        db.updateRowByObjectByQuery(effet, "EFFETS", "IDEFFET=" + effet.getIdeffet());
                    }
                }

                if (remcomValue == null) {
                    sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + " AND TYPE_PRELEVEMENT ='025'  ORDER BY remcom ,lotcom ";
                } else {
                    sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + " AND TYPE_PRELEVEMENT ='025'  ORDER BY remcom ,lotcom ";
                }

                Prelevements[] prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
                j = 0;
                if (prelevements != null && prelevements.length > 0) {
                    System.out.println("Prelevements annulation");
                    remcomValue = prelevements[0].getRemcom();
                    remiseHasLotcom = true;
                    sql = "SELECT * FROM PRELEVEMENTS WHERE ETAT = " + Utility.getParam("CETAOPEALLICOM3") + "  AND REMCOM = " + remcomValue + " AND TYPE_PRELEVEMENT ='025'  ORDER BY remcom ,lotcom ";
                    prelevements = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
                    for (int i = 0; i < prelevements.length; i += j) {
                        //Tous les prelevements validés

                        sql = "SELECT * FROM PRELEVEMENTS WHERE LOTCOM=" + prelevements[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Prelevements[] prelevementsVal = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());

                        sql = "SELECT * FROM LOTCOM WHERE IDLOTCOM=" + prelevements[i].getLotcom();
                        lotcoms = (Lotcom[]) db.retrieveRowAsObject(sql, new Lotcom());

                        sql = "SELECT * FROM PRELEVEMENTS WHERE LOTCOM=" + prelevements[i].getLotcom() + " AND ETAT =" + Utility.getParam("CETAOPEALLICOM3") + " AND REMCOM = " + remcomValue;
                        Prelevements[] allPrelevementsVal = (Prelevements[]) db.retrieveRowAsObject(sql, new Prelevements());
                        try {
                            preparePrelevements(allPrelevementsVal, lotcoms[0]);
                        } catch (NumberFormatException | SQLException ex) {
                            Logger.getLogger(Icom3NatWriter.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        j = prelevementsVal.length;

                    }
                    sql = "SELECT * FROM REMCOM WHERE IDREMCOM = " + prelevements[0].getRemcom() + whereClause;
                    remcoms = (Remcom[]) db.retrieveRowAsObject(sql, new Remcom());
                    if (remcoms != null && remcoms.length > 0) {

                        enteteRemise.setRefRemRelatif(Utility.bourrageGZero(remcoms[0].getRefremise(), 3));
                    }

                }
                for (Prelevements prelevement : prelevements) {
                    //CETAOPEALLICOM3TRAN Mise a jour transitoire
                    prelevement.setEtat(new BigDecimal(Utility.getParam("CETAOPEALLICOM3TRAN")));
                    db.updateRowByObjectByQuery(prelevement, "PRELEVEMENTS", "IDPRELEVEMENT=" + prelevement.getIdprelevement());
                }

            }

        }

    }

    private void preparePrelevements(Prelevements[] prelevements, Lotcom lotcom1) throws SQLException {
        if (prelevements != null && prelevements.length > 0) {
            remiseHasPrelevements = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(lotcom1.getRefbancaire());
            enteteLot.setTypeOperation(lotcom1.getTypeoperation());
            enteteLot.setIdBanRem(lotcom1.getIdbanrem());
            enteteLot.setNbOperations(Utility.bourrageGZero("" + prelevements.length, 4));
            enteteLot.setMontantTotal(createBlancs(16, "0"));
            enteteLot.setBlancs(createBlancs(24, " "));

            Operation[] operations = new Operation[prelevements.length];
            long montantLot = 0;
            for (int j = 0; j < prelevements.length; j++) {
                Prelevements prelevement = prelevements[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(prelevement.getMontantprelevement().trim());

                operations[j].setTypeOperation(lotcom1.getTypeoperation());
                operations[j].setRefOperation("" + prelevement.getIdprelevement());
                operations[j].setIdObjetOrigine(prelevement.getIdprelevement());
                switch (Integer.parseInt(operations[j].getTypeOperation())) {
                    case StaticValues.PRELEVEMENT_SICA3: {
                        feedOperationWithPrelevement(j, operations, prelevement);
                    }
                    break;
                    case StaticValues.REJ_PRELEVEMENT_SICA3: {
                        operations[j].setRefOperation(prelevement.getReference_Operation_Rejet().trim());
                        operations[j].setIdAgeRem(prelevement.getAgenceremettant());

                        operations[j].setRioOperInitial(new RIO(prelevement.getRio()));
                        operations[j].setMotifRejet(prelevement.getMotifrejet());
                        operations[j].setBlancs(createBlancs(10, " "));
                        operations[j].setIdObjetOrigine(prelevement.getIdprelevement());
                    }
                    break;

                }

            }

            //enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
        }
    }

    private void prepareVirements(Virements[] virements, Lotcom lotcom1) throws SQLException {
        if (virements != null && virements.length > 0) {
            remiseHasVirements = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(lotcom1.getRefbancaire());
            enteteLot.setTypeOperation(lotcom1.getTypeoperation());
            enteteLot.setIdBanRem(lotcom1.getIdbanrem());
            enteteLot.setNbOperations(Utility.bourrageGZero("" + virements.length, 4));
            enteteLot.setMontantTotal(createBlancs(16, "0"));
            enteteLot.setBlancs(createBlancs(24, " "));
            Operation[] operations = new Operation[virements.length];
            long montantLot = 0;
            for (int j = 0; j < virements.length; j++) {
                Virements virement = virements[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(virement.getMontantvirement().trim());

                operations[j].setTypeOperation(virement.getType_Virement());
                operations[j].setRefOperation("" + virement.getIdvirement());
                operations[j].setIdObjetOrigine(virement.getIdvirement());
                switch (Integer.parseInt(operations[j].getTypeOperation())) {
                    case StaticValues.VIR_CLIENT: {
                        feedOperationWithVirement10(virement, j, operations);
                    }
                    break;
                    case StaticValues.VIR_BANQUE: {
                        feedOperationWithVirement11(operations, j, virement);
                    }
                    break;

                    case StaticValues.VIR_DISPOSITION: {
                        feedOperationWithVirement12(j, operations, virement);

                    }
                    break;
                    case StaticValues.VIR_CLIENT_SICA3: {
                        feedOperationWithVirement15(virement, j, operations);

                    }
                    break;
                    case StaticValues.VIR_DISPOSITION_SICA3: {
                        feedOperationWithVirement17(j, operations, virement);

                    }
                    break;
                }

            }

            enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
        }
    }

    private void prepareEffets(Effets[] effets, Lotcom lotcom1) throws SQLException {
        if (effets != null && effets.length > 0) {
            remiseHasEffets = true;
            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(lotcom1.getRefbancaire());
            enteteLot.setTypeOperation(lotcom1.getTypeoperation());
            enteteLot.setIdBanRem(lotcom1.getIdbanrem());
            enteteLot.setNbOperations(Utility.bourrageGZero("" + effets.length, 4));
            enteteLot.setMontantTotal(createBlancs(16, "0"));
            enteteLot.setBlancs(createBlancs(24, " "));
            Operation[] operations = new Operation[effets.length];
            long montantLot = 0;
            for (int j = 0; j < effets.length; j++) {
                Effets effet = effets[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(effet.getMontant_Effet().trim());

                operations[j].setTypeOperation(lotcom1.getTypeoperation());
                operations[j].setRefOperation("" + effet.getIdeffet());
                operations[j].setIdObjetOrigine(effet.getIdeffet());
                switch (Integer.parseInt(operations[j].getTypeOperation())) {
                    case StaticValues.REJ_BLT_ORD_SCAN_SICA3:
                    case StaticValues.REJ_BLT_ORD_SCAN: {
                        operations[j].setRefOperation(effet.getReference_Operation_Rejet().trim());
                        operations[j].setIdAgeRem(effet.getAgenceremettant());

                        operations[j].setRioOperInitial(new RIO(effet.getRio()));
                        operations[j].setMotifRejet(effet.getMotifrejet());
                        operations[j].setBlancs(createBlancs(10, " "));
                        operations[j].setIdObjetOrigine(effet.getIdeffet());
                    }
                    break;
                    case StaticValues.BLT_ORD_SCAN_SICA3: {
                        feedOperationWithBltOrdre45(j, operations, effet);
                    }
                    break;
                    case StaticValues.REJ_LTR_CHG_SCAN:

                    case StaticValues.REJ_LTR_CHG_SCAN_SICA3: {
                        operations[j].setRefOperation(effet.getReference_Operation_Rejet().trim());
                        operations[j].setIdAgeRem(effet.getAgenceremettant());

                        operations[j].setRioOperInitial(new RIO(effet.getRio()));
                        operations[j].setMotifRejet(effet.getMotifrejet());
                        operations[j].setBlancs(createBlancs(10, " "));
                        operations[j].setIdObjetOrigine(effet.getIdeffet());
                    }
                    break;
                    case StaticValues.LTR_CHG_SCAN_SICA3: {
                        feedOperationWithLtrChange46(operations, j, effet);
                    }
                    break;
                }

            }

            //enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
        }
    }

    private void prepareCheques(Cheques[] cheques, Lotcom lotcom1) throws NumberFormatException, SQLException {

        if (cheques != null && cheques.length > 0) {
            remiseHasCheques = true;

            EnteteLot enteteLot = new EnteteLot();
            enteteLot.setIdEntete("ELOT");
            enteteLot.setRefLot("" + ++refLot);
            enteteLot.setRefBancaire(lotcom1.getRefbancaire());
            enteteLot.setTypeOperation(lotcom1.getTypeoperation());
            enteteLot.setIdBanRem(lotcom1.getIdbanrem());
            enteteLot.setNbOperations(Utility.bourrageGZero("" + cheques.length, 4));
            enteteLot.setMontantTotal(createBlancs(16, "0"));
            enteteLot.setBlancs(createBlancs(24, " "));
            Operation[] operations = new Operation[cheques.length];
            long montantLot = 0;
            for (int j = 0; j < cheques.length; j++) {
                Cheques cheque = cheques[j];
                operations[j] = new Operation();
                montantLot = montantLot + Long.parseLong(cheque.getMontantcheque().trim());
                operations[j].setIdObjetOrigine(cheque.getIdcheque());
                switch (Integer.parseInt(lotcom1.getTypeoperation())) {

                    case StaticValues.REJ_CHQ_SCAN_SICA3: {
                        feedOperationWithRejCheque(j, cheque, operations);
                    }
                    break;
                    case StaticValues.CHQ_SCAN_SICA3: {
                        feedOperationWithCheque35(j, cheque, operations);
                    }
                    break;

                }

            }

            //enteteLot.setMontantTotal("" + montantLot);
            enteteLot.operations = operations;
            vEnteteLots.add(enteteLot);
        }

    }

    public void printRemiseAnnulation(DataBase db) throws Exception {
        System.out.println("printRemiseAnnulation method");
        String fileName = CMPUtility.getNatFileName(sequence, enteteRemise.getTypeRemise());
        setOut(createFlatFile(fileName));
        enteteRemise.setNbLots("" + vEnteteLots.size());
        String line = enteteRemise.getIdEntete() + enteteRemise.getIdEmetteur() + enteteRemise.getRefRemise() + Utility.convertDateToString(enteteRemise.getDatePresentation(), "yyyyMMdd") + enteteRemise.getIdRecepeteur() + enteteRemise.getDevise() + enteteRemise.getTypeRemise() + enteteRemise.getRefRemRelatif() + enteteRemise.getNbLots() + enteteRemise.getBlancs();
        writeln(line);
        remcom = CMPUtility.insertRemcom(enteteRemise, Integer.parseInt(Utility.getParam("CETAOPEALLPREICOM3")));
        if (remiseHasRemcom) {
            System.out.println("remiseHasRemcom printRemiseAnnulation ");
            sql = "UPDATE VIREMENTS SET REMCOM=" + remcom.getIdremcom() + ",ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE PRELEVEMENTS SET REMCOM=" + remcom.getIdremcom() + ",ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE EFFETS SET REMCOM=" + remcom.getIdremcom() + ",ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE CHEQUES SET REMCOM=" + remcom.getIdremcom() + ",ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE LOTCOM SET IDREMCOM=" + remcom.getIdremcom() + ",ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3");
            db.executeUpdate(sql);
        }
        if (remiseHasLotcom) {
            System.out.println("remiseHasLotcom  printRemiseAnnulation");
            enteteRemise.enteteLots = vEnteteLots.toArray(new EnteteLot[vEnteteLots.size()]);
            for (int i = 0; i < enteteRemise.enteteLots.length; i++) {
                line = printEnteteLot(enteteRemise, remcom, i);
                writeln(line);
                for (int j = 0; j < enteteRemise.enteteLots[i].operations.length; j++) {

                    switch (Integer.parseInt(enteteRemise.enteteLots[i].getTypeOperation())) {
                        case StaticValues.VIR_CLIENT:
                        case StaticValues.VIR_CLIENT_SICA3: {
                            line = printVirements15(enteteRemise, j, i);
                            writeln(line);
                            System.out.println("requete vir");
                            sql = "UPDATE VIREMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ",ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " "
                                    + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3TRAN");
                            System.out.println("requete vir");
                            db.executeUpdate(sql);
                        }
                        ;
                        System.out.println("VIR_BANQUE");
                        break;
                        case StaticValues.VIR_BANQUE: {
                            line = printVirements11(enteteRemise, j, i);
                            writeln(line);
                            sql = "UPDATE VIREMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ","
                                    + " ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3TRAN");
                            db.executeUpdate(sql);
                        }
                        ;
                        System.out.println("VIR_DISPOSITION_SICA3");
                        break;

                        case StaticValues.VIR_DISPOSITION:
                        case StaticValues.VIR_DISPOSITION_SICA3: {
                            line = printVirements17(enteteRemise, j, i);
                            sql = "UPDATE VIREMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ",ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " "
                                    + "WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3TRAN");
                            db.executeUpdate(sql);
                        }
                        ;
                        break;
                        case StaticValues.PRELEVEMENT:
                        case StaticValues.PRELEVEMENT_SICA3: {
                            line = printPrelevements(enteteRemise, j, i);
                            writeln(line);
                            sql = "UPDATE PRELEVEMENTS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ","
                                    + "ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3TRAN");
                            db.executeUpdate(sql);
                        }
                        case StaticValues.REJ_PRELEVEMENT_SICA3: {
                            line = printRejets(enteteRemise, i, j);
                            writeln(line);
                            db.executeUpdate(sql);
                        }
                        break;
                        case StaticValues.CHQ_SCAN:
                        case StaticValues.CHQ_SCAN_SICA3:
                        case StaticValues.CHQ_PAP: {
                            line = printCheques35(enteteRemise, j, i);
                            writeln(line);
                            sql = "UPDATE CHEQUES SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ","
                                    + "ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3TRAN");
                            db.executeUpdate(sql);
                        }
                        break;
                        case StaticValues.REJ_CHQ_SCAN_SICA3:
                        case StaticValues.REJ_CHQ_SCAN: {
                            line = printRejets(enteteRemise, i, j);
                            writeln(line);
                            sql = "UPDATE CHEQUES SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ",ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " "
                                    + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3TRAN");
                            db.executeUpdate(sql);
                        }
                        break;
                        case StaticValues.BLT_ORD_SCAN:
                        case StaticValues.BLT_ORD_PAP:
                        case StaticValues.BLT_ORD_SCAN_SICA3: {
                            line = printEffets45(enteteRemise, j, i);
                            writeln(line);
                            sql = "UPDATE EFFETS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ","
                                    + " ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3TRAN");
                            db.executeUpdate(sql);
                        }
                        ;
                        break;
                        case StaticValues.LTR_CHG_SCAN:
                        case StaticValues.LTR_CHG_PAP:
                        case StaticValues.LTR_CHG_SCAN_SICA3: {
                            line = printEffets46(enteteRemise, j, i);
                            writeln(line);
                            sql = "UPDATE EFFETS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ","
                                    + " ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3TRAN");
                            db.executeUpdate(sql);
                        }
                        break;
                        case StaticValues.REJ_BLT_ORD_SCAN:
                        case StaticValues.REJ_BLT_ORD_PAP:
                        case StaticValues.REJ_BLT_ORD_SCAN_SICA3:
                        case StaticValues.REJ_LTR_CHG_SCAN:
                        case StaticValues.REJ_LTR_CHG_PAP:
                        case StaticValues.REJ_LTR_CHG_SCAN_SICA3: {
                            line = printRejets(enteteRemise, i, j);
                            writeln(line);
                            sql = "UPDATE EFFETS SET LOTCOM=" + lotcom.getIdlotcom() + ", REMCOM=" + remcom.getIdremcom() + ","
                                    + " ETAT=" + Utility.getParam("CETAOPEALLPREICOM3") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3TRAN");
                            db.executeUpdate(sql);
                        }
                        break;
                    }
                }
            }
        }

        writeEOF("FREM", createBlancs(28, " "));
    }

    public void updateEtatOperations(DataBase db) throws Exception {
        if (remiseHasRemcom || remiseHasLotcom) {
            printRemiseAnnulation(db);
            sql = "UPDATE PRELEVEMENTS SET ETAT=" + Utility.getParam("CETAOPEALLICOM3ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE VIREMENTS SET ETAT=" + Utility.getParam("CETAOPEALLICOM3ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE EFFETS SET ETAT=" + Utility.getParam("CETAOPEALLICOM3ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE CHEQUES SET ETAT=" + Utility.getParam("CETAOPEALLICOM3ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE LOTCOM SET ETAT=" + Utility.getParam("CETAOPEALLICOM3ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE REMCOM SET ETAT=" + Utility.getParam("CETAOPEALLICOM3ENV") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLPREICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE LOTCOM SET ETAT=" + Utility.getParam("CETAOPEALLICOM3GEN") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3");
            db.executeUpdate(sql);
            sql = "UPDATE REMCOM SET ETAT=" + Utility.getParam("CETAOPEALLICOM3GEN") + " WHERE ETAT =" + Utility.getParam("CETAOPEALLICOM3");
            db.executeUpdate(sql);
        } else {
            logEvent("WARNING", "Il n'y a aucun element disponible");
            setDescription(getDescription() + " - WARNING: Il n'y a aucun element disponible");
        }
    }

    private void feedOperationWithCheque35(int j, Cheques cheque, Operation[] operations) {
        operations[j].setTypeOperation(cheque.getType_Cheque());
        operations[j].setRefOperation("" + cheque.getIdcheque());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setIPac(cheque.getVilleremettant());
        operations[j].setNomCrediteur(cheque.getNombeneficiaire());
        operations[j].setAdrCrediteur(createBlancs(50, " "));
        operations[j].setNumCheque(cheque.getNumerocheque());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(cheque.getDatesaisie(), ResLoader.getMessages("patternDate")));
        operations[j].setIdBanDeb(cheque.getBanque());
        operations[j].setIdAgeDeb(cheque.getAgence());
        operations[j].setNumCptDeb(cheque.getNumerocompte());
        operations[j].setCleRibDeb(Utility.computeCleRIB(cheque.getBanque(), cheque.getAgence(), cheque.getNumerocompte()));
        operations[j].setMontant(cheque.getMontantcheque());
        operations[j].setCodeCertif(cheque.getCodecertification());
        operations[j].setBlancs(createBlancs(241, " "));
    }

    private void feedOperationWithRejCheque(int j, Cheques cheque, Operation[] operations) {
        operations[j].setTypeOperation(cheque.getType_Cheque().replaceFirst("0", "1"));
        operations[j].setRefOperation(cheque.getReference_Operation_Interne().trim());
        operations[j].setIdAgeRem(cheque.getAgenceremettant());
        operations[j].setRioOperInitial(new RIO(cheque.getRio()));
        operations[j].setMotifRejet(cheque.getMotifrejet());
        operations[j].setBlancs(createBlancs(10, " "));
    }

    private void feedOperationWithBltOrdre45(int j, Operation[] operations, Effets effet) {
        operations[j].setIdAgeRem(effet.getAgenceremettant());
        operations[j].setIPac(effet.getVille());
        operations[j].setNumIntOrdre(String.valueOf(effet.getIdeffet()));
        operations[j].setDateEcheance(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")));

        operations[j].setRibDebiteur(effet.getBanque() + effet.getAgence() + effet.getNumerocompte_Tire() + Utility.computeCleRIB(effet.getBanque(), effet.getAgence(), effet.getNumerocompte_Tire()));
        operations[j].setMontant(effet.getMontant_Effet());
        operations[j].setCodeFrais(effet.getCode_Frais());
        operations[j].setMontantFrais(effet.getMontant_Frais());
        operations[j].setMontantBrut(effet.getMontant_Brut());
        operations[j].setNomSouscripteur(effet.getNom_Tire());
        operations[j].setAdrSouscripteur(effet.getAdresse_Tire());
        operations[j].setNomCrediteur(effet.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(effet.getAdresse_Beneficiaire());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(effet.getDate_Creation(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeAcceptation(effet.getCode_Acceptation());
        operations[j].setRefSouscripteur(effet.getIdentification_Tire());
        operations[j].setCodeEndossement(effet.getCode_Endossement());
        operations[j].setProtet(effet.getProtet());
        operations[j].setCodeAval(effet.getCode_Aval());
        operations[j].setBlancs(createBlancs(96, " "));
    }

    private void feedOperationWithLtrChange46(Operation[] operations, int j, Effets effet) {
        operations[j].setIdAgeRem(effet.getAgenceremettant());
        operations[j].setIPac(effet.getVille());
        operations[j].setNumIntOrdre(String.valueOf(effet.getIdeffet()));
        operations[j].setDateEcheance(Utility.convertStringToDate(effet.getDate_Echeance(), ResLoader.getMessages("patternDate")));

        operations[j].setRibDebiteur(effet.getBanque() + effet.getAgence() + Utility.bourrageGZero(effet.getNumerocompte_Tire(), 12) + Utility.computeCleRIB(effet.getBanque(), effet.getAgence(), Utility.bourrageGZero(effet.getNumerocompte_Tire(), 12)));
        operations[j].setMontant(effet.getMontant_Effet());
        operations[j].setCodeFrais(effet.getCode_Frais());
        operations[j].setMontantFrais(effet.getMontant_Frais());
        operations[j].setMontantBrut(effet.getMontant_Brut());
        operations[j].setNomSouscripteur(effet.getNom_Tire());
        operations[j].setAdrSouscripteur(effet.getAdresse_Tire());
        operations[j].setNomCrediteur(effet.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(effet.getAdresse_Beneficiaire());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(effet.getDate_Creation(), ResLoader.getMessages("patternDate")));
        operations[j].setCodeAcceptation(effet.getCode_Acceptation());
        operations[j].setRefSouscripteur(effet.getIdentification_Tire());
        operations[j].setCodeEndossement(effet.getCode_Endossement());
        operations[j].setProtet(effet.getProtet());
        operations[j].setCodeAval(effet.getCode_Aval());
        operations[j].setBlancs(createBlancs(96, " "));
    }

    private void feedOperationWithPrelevement(int j, Operation[] operations, Prelevements prelevement) {
        operations[j].setIdAgeRem(prelevement.getAgenceremettant());
        operations[j].setIPac(prelevement.getVille());
        operations[j].setNumIntOrdre(String.valueOf(prelevement.getNumeroprelevement()));
        operations[j].setIdObjetOrigine(prelevement.getIdprelevement());

        operations[j].setRibDebiteur(prelevement.getBanque() + prelevement.getAgence() + Utility.bourrageGZero(prelevement.getNumerocompte_Tire(), 12) + Utility.computeCleRIB(prelevement.getBanque(), prelevement.getAgence(), Utility.bourrageGZero(prelevement.getNumerocompte_Tire(), 12)));
        operations[j].setMontant(prelevement.getMontantprelevement());

        operations[j].setNomSouscripteur(prelevement.getNom_Tire());
        operations[j].setAdrSouscripteur(prelevement.getAdresse_Tire());
        operations[j].setNomCrediteur(prelevement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(prelevement.getAdresse_Beneficiaire());
        operations[j].setRibCrediteur(prelevement.getBanqueremettant() + prelevement.getAgenceremettant() + Utility.bourrageGZero(prelevement.getNumerocompte_Beneficiaire(), 12) + Utility.computeCleRIB(prelevement.getBanqueremettant(), prelevement.getAgenceremettant(), Utility.bourrageGZero(prelevement.getNumerocompte_Beneficiaire(), 12)));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(prelevement.getDatetraitement(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(prelevement.getLibelle());
        operations[j].setBlancs(createBlancs(173, " "));
    }

    private void feedOperationWithVirement10(Virements virement, int j, Operation[] operations) {
        operations[j].setFlagIBANDeb("0");
        operations[j].setPfxIBANDeb(createBlancs(4, " "));
        operations[j].setRibDebiteur(virement.getBanqueremettant() + virement.getAgenceremettant() + virement.getNumerocompte_Tire() + Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(), virement.getNumerocompte_Tire()));
        operations[j].setFlagIBANCre("0");
        operations[j].setPfxIBANCre(createBlancs(4, " "));
        operations[j].setRibCrediteur(virement.getBanque() + virement.getAgence() + virement.getNumerocompte_Beneficiaire() + Utility.computeCleRIB(virement.getBanque(), virement.getAgence(), virement.getNumerocompte_Beneficiaire()));

        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setAdrDebiteur(virement.getAdresse_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(virement.getAdresse_Beneficiaire());
        operations[j].setNumIntOrdre(String.valueOf(virement.getIdvirement()));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(57, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void feedOperationWithVirement15(Virements virement, int j, Operation[] operations) {
        if (Character.isDigit(virement.getBanqueremettant().charAt(1))) {//Ancienne Norme
            operations[j].setFlagIBANDeb("1");
        } else {//Nouvelle Norme
            operations[j].setFlagIBANDeb("2");
        }

        operations[j].setRibDebiteur(virement.getBanqueremettant() + virement.getAgenceremettant() + Utility.bourrageGZero(virement.getNumerocompte_Tire(), 12) + Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(), Utility.bourrageGZero(virement.getNumerocompte_Tire(), 12)));

        if (Character.isDigit(virement.getBanque().charAt(1))) {//Ancienne Norme
            operations[j].setFlagIBANCre("1");
        } else {//Nouvelle Norme
            operations[j].setFlagIBANCre("2");
        }
        operations[j].setRibCrediteur(virement.getBanque() + virement.getAgence() + Utility.bourrageGZero(virement.getNumerocompte_Beneficiaire(), 12) + Utility.computeCleRIB(virement.getBanque(), virement.getAgence(), Utility.bourrageGZero(virement.getNumerocompte_Beneficiaire(), 12)));

        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setAdrDebiteur(virement.getAdresse_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(virement.getAdresse_Beneficiaire());
        operations[j].setNumIntOrdre(String.valueOf(virement.getIdvirement()));
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(65, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void feedOperationWithVirement11(Operation[] operations, int j, Virements virement) {
        operations[j].setIdAgeRem(virement.getAgenceremettant());
        operations[j].setIdBanCre(virement.getBanque());
        operations[j].setIdAgeCre(virement.getAgence());
        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setRefEmetteur(virement.getReference_Emetteur());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(200, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void feedOperationWithVirement12(int j, Operation[] operations, Virements virement) {
        operations[j].setFlagIBANDeb("0");
        operations[j].setPfxIBANDeb(createBlancs(4, " "));
        operations[j].setRibDebiteur(virement.getBanqueremettant() + virement.getAgenceremettant() + virement.getNumerocompte_Tire() + Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(), virement.getNumerocompte_Tire()));
        operations[j].setIdBanCre(virement.getBanque());
        operations[j].setIdAgeCre(virement.getAgence());
        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setAdrDebiteur(virement.getAdresse_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(virement.getAdresse_Beneficiaire());
        operations[j].setRefEmetteur(virement.getReference_Emetteur());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(76, " "));
        operations[j].setIPac(virement.getVille());
    }

    private void feedOperationWithVirement17(int j, Operation[] operations, Virements virement) {
        operations[j].setRibDebiteur(virement.getBanqueremettant() + virement.getAgenceremettant() + virement.getNumerocompte_Tire() + Utility.computeCleRIB(virement.getBanqueremettant(), virement.getAgenceremettant(), virement.getNumerocompte_Tire()));
        operations[j].setIdBanCre(virement.getBanque());
        operations[j].setIdAgeCre(virement.getAgence());
        operations[j].setMontant(Utility.bourrageGZero(virement.getMontantvirement(), 16));
        operations[j].setNomDebiteur(virement.getNom_Tire());
        operations[j].setAdrDebiteur(virement.getAdresse_Tire());
        operations[j].setNomCrediteur(virement.getNom_Beneficiaire());
        operations[j].setAdrCrediteur(virement.getAdresse_Beneficiaire());
        operations[j].setRefEmetteur(virement.getReference_Emetteur());
        operations[j].setDateOrdreClient(Utility.convertStringToDate(virement.getDateordre(), ResLoader.getMessages("patternDate")));
        operations[j].setLibelle(virement.getLibelle());
        operations[j].setBlancs(createBlancs(81, " "));
        operations[j].setIPac(virement.getVille());
    }

    private String printEnteteLot(EnteteRemise enteteRemise, Remcom remcom, int i) throws Exception {
        lotcom = CMPUtility.insertLotcom(enteteRemise.enteteLots[i], remcom);
        String line = new String(enteteRemise.enteteLots[i].getIdEntete()
                + enteteRemise.enteteLots[i].getRefLot()
                + enteteRemise.enteteLots[i].getRefBancaire()
                + enteteRemise.enteteLots[i].getTypeOperation()
                + enteteRemise.enteteLots[i].getIdBanRem()
                + enteteRemise.enteteLots[i].getNbOperations()
                + enteteRemise.enteteLots[i].getMontantTotal()
                + enteteRemise.enteteLots[i].getBlancs());
        return line;
    }

    private String printCheques35(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation() + enteteRemise.enteteLots[i].operations[j].getIdAgeRem() + enteteRemise.enteteLots[i].operations[j].getIPac() + enteteRemise.enteteLots[i].operations[j].getNomCrediteur() + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur() + enteteRemise.enteteLots[i].operations[j].getNumCheque() + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd") + enteteRemise.enteteLots[i].operations[j].getIdBanDeb() + enteteRemise.enteteLots[i].operations[j].getIdAgeDeb() + enteteRemise.enteteLots[i].operations[j].getNumCptDeb() + enteteRemise.enteteLots[i].operations[j].getCleRibDeb() + enteteRemise.enteteLots[i].operations[j].getMontant() + enteteRemise.enteteLots[i].operations[j].getCodeCertif() + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printRejets(EnteteRemise enteteRemise, int i, int j) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getRioOperInitial()
                + enteteRemise.enteteLots[i].operations[j].getMotifRejet()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printPrelevements(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + "2"
                + enteteRemise.enteteLots[i].operations[j].getRibCrediteur()
                + "2"
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printVirements10(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANCre()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANCre()
                + enteteRemise.enteteLots[i].operations[j].getRibCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printVirements15(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANCre()
                + enteteRemise.enteteLots[i].operations[j].getRibCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printVirements11(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation() + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIdBanCre()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeCre()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getRefEmetteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle() + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printVirements12(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getIdBanCre()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeCre()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getRefEmetteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printVirements17(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getIdBanCre()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeCre()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getNomDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getRefEmetteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getLibelle()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());

        return line;
    }

    private String printEffets4042(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation()
                + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIPac()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getCodeFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantBrut()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getCodeAcceptation()
                + enteteRemise.enteteLots[i].operations[j].getRefSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getCodeEndossement()
                + enteteRemise.enteteLots[i].operations[j].getNumCedant()
                + enteteRemise.enteteLots[i].operations[j].getCodeAval()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printEffets45(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation()
                + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIPac()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getCodeFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantBrut()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getCodeAcceptation()
                + enteteRemise.enteteLots[i].operations[j].getRefSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getCodeEndossement()
                + enteteRemise.enteteLots[i].operations[j].getProtet()
                + enteteRemise.enteteLots[i].operations[j].getCodeAval() + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printEffets4143(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation() + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIPac()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getFlagIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getPfxIBANDeb()
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getCodeFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantBrut()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getCodeAcceptation()
                + enteteRemise.enteteLots[i].operations[j].getRefSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getCodeEndossement()
                + enteteRemise.enteteLots[i].operations[j].getNumCedant()
                + enteteRemise.enteteLots[i].operations[j].getCodeAval() + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }

    private String printEffets46(EnteteRemise enteteRemise, int j, int i) {
        String line;

        line = new String(enteteRemise.enteteLots[i].operations[j].getTypeOperation() + enteteRemise.enteteLots[i].operations[j].getRefOperation() + enteteRemise.enteteLots[i].operations[j].getIdAgeRem()
                + enteteRemise.enteteLots[i].operations[j].getIPac()
                + enteteRemise.enteteLots[i].operations[j].getNumIntOrdre()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateEcheance(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getRibDebiteur()
                + enteteRemise.enteteLots[i].operations[j].getMontant()
                + enteteRemise.enteteLots[i].operations[j].getCodeFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantFrais()
                + enteteRemise.enteteLots[i].operations[j].getMontantBrut()
                + enteteRemise.enteteLots[i].operations[j].getNomSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getNomCrediteur()
                + enteteRemise.enteteLots[i].operations[j].getAdrCrediteur()
                + Utility.convertDateToString(enteteRemise.enteteLots[i].operations[j].getDateOrdreClient(), "yyyyMMdd")
                + enteteRemise.enteteLots[i].operations[j].getCodeAcceptation()
                + enteteRemise.enteteLots[i].operations[j].getRefSouscripteur()
                + enteteRemise.enteteLots[i].operations[j].getCodeEndossement()
                + enteteRemise.enteteLots[i].operations[j].getProtet()
                + enteteRemise.enteteLots[i].operations[j].getCodeAval()
                + enteteRemise.enteteLots[i].operations[j].getBlancs());
        return line;
    }
}
