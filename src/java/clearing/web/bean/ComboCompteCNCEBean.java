/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clearing.web.bean;

import org.patware.web.json.bean.ComboBean;
import clearing.table.delta.Icsibtc1;
import clearing.table.delta.Icsibtc2;
import clearing.table.delta.Sibiccpt;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.patware.jdbc.DataBase;
import org.patware.utils.Utility;
import org.patware.web.json.JSONConverter;
import org.patware.xml.ExtJDBCXmlReader;

/**
 *
 * @author Patrick
 */
public class ComboCompteCNCEBean extends ComboBean {

    private String requete = "select * from sibiccpt";
    private String[] codeAgence;

    public String getInfoCompte(String numeroCompte) {
        try {
            JSONConverter jsonConverter = new JSONConverter();
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

            Sibiccpt[] sibiccpt = (Sibiccpt[]) dbExt.retrieveRowAsObject(requete + " where compte like '" + Utility.bourrageGauche(numeroCompte, 11, "0") + "'", new Sibiccpt());


            if (sibiccpt != null && sibiccpt.length > 0) {
                sibiccpt[0].setNomabreg(sibiccpt[0].getNomabreg().replaceAll("\\p{Punct}", " "));
                if (sibiccpt[0].getPrenom() != null) {
                    sibiccpt[0].setPrenom(sibiccpt[0].getPrenom().replaceAll("\\p{Punct}", " "));
                }

                // if(sibiccpt[0].get!= null) comptes[0].setAdresse1(comptes[0].getAdresse1().replaceAll("\\p{Punct}", " "));


                //Insertion dans le bean
                CompteMessageBean compteMessageBean = new CompteMessageBean();
                compteMessageBean.setNumeroCompte(sibiccpt[0].getCompte());
                compteMessageBean.setNomClient(Utility.bourrageDroite(sibiccpt[0].getNomabreg().trim() + " " + sibiccpt[0].getPrenom().trim(), 35, " "));

                compteMessageBean.setAdresseCompte("");
                compteMessageBean.setEscompte(Utility.getParam("LCE_SBF").trim());

                // compteMessageBean.setAgenceCompte(codeAgence);

                
                //Test de validité sur type de compte
                /*
                Icsibtc1[] icsibtc1 = (Icsibtc1[]) dbExt.retrieveRowAsObject("SELECT * FROM Icsibtc1 WHERE TYPECPT ='" + sibiccpt[0].getTypecpt() + "'", new Icsibtc1());
                if (icsibtc1 != null && icsibtc1.length > 0) {
                    switch (icsibtc1[0].getActions().intValue()) {
                        case 0:
                             {
                                //On ne fait rien
                                compteMessageBean.setAction("0");
                                compteMessageBean.setMessageResult("");

                            }
                            break;
                        case 1:
                             {
                                //On affiche un message non bloquant
                                compteMessageBean.setAction("1");
                                compteMessageBean.setMessageResult(icsibtc1[0].getLibelle());


                            }
                            break;
                        case 9:
                             {
                                //On affiche un message bloquant
                                compteMessageBean.setAction("9");
                                compteMessageBean.setMessageResult(icsibtc1[0].getLibelle());


                            }
                            break;
                        default: {
                            compteMessageBean.setAction("9");
                            compteMessageBean.setMessageResult("ACTION "+ icsibtc1[0].getActions() +" INCORRECTE POUR TYPE COMPTE "+sibiccpt[0].getTypecpt());

                        }
                    }

                } else {
                    compteMessageBean.setAction("9");
                    compteMessageBean.setMessageResult("TYPE COMPTE INEXISTANT ="+sibiccpt[0].getTypecpt());

                }

                Icsibtc2[] icsibtc2 = (Icsibtc2[]) dbExt.retrieveRowAsObject("SELECT * FROM Icsibtc2 WHERE STATUCPT ='" + sibiccpt[0].getStatucpt() + "'", new Icsibtc2());
                if (icsibtc2 != null && icsibtc2.length > 0) {
                    switch (icsibtc2[0].getActionsb().intValue()) {
                        case 0:
                             {
                                //On ne fait rien
                                compteMessageBean.setActionStatut("0");
                                compteMessageBean.setMessageStatutResult("");
                            }
                            break;
                        case 1:
                             {
                                //On affiche un message non bloquant
                                compteMessageBean.setActionStatut("1");
                                compteMessageBean.setMessageStatutResult(icsibtc2[0].getLibelle());
                            }
                            break;
                        case 9:
                             {
                                //On affiche un message bloquant
                                compteMessageBean.setActionStatut("9");
                                compteMessageBean.setMessageStatutResult(icsibtc2[0].getLibelle());

                            }
                            break;
                        default: {
                            compteMessageBean.setActionStatut("9");
                            compteMessageBean.setMessageStatutResult("ACTION "+ icsibtc2[0].getActionsb()+" INCORRECTE POUR STATUT COMPTE ="+icsibtc2[0].getStatucpt());

                        }
                    }

                } else {
                    compteMessageBean.setActionStatut("9");
                    compteMessageBean.setMessageStatutResult("STATUT COMPTE INEXISTANT ="+ sibiccpt[0].getStatucpt());

                }

*/
                dbExt.close();

                return jsonConverter.objectToJSONStringArray(compteMessageBean);


            }
            dbExt.close();
            return "rien";
        //return getComboLiteral(new String[]{comptes[0].getNom()}, new String[]{comptes[0].getAgence()}).toString();


        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

     public String getInfoCompte( String agence, String numeroCompte) {
        try {
            JSONConverter jsonConverter = new JSONConverter();
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

            Sibiccpt[] sibiccpt = (Sibiccpt[]) dbExt.retrieveRowAsObject(requete + " where compte like '" + Utility.bourrageGauche(numeroCompte, 11, "0") + "' and guichet like '"+ agence +"'", new Sibiccpt());


            if (sibiccpt != null && sibiccpt.length > 0) {


                //Insertion dans le bean
                CompteMessageBean compteMessageBean = new CompteMessageBean();
                compteMessageBean.setNumeroCompte(sibiccpt[0].getCompte());
                compteMessageBean.setNomClient(Utility.bourrageDroite(sibiccpt[0].getNomabreg().trim() + " " + sibiccpt[0].getPrenom().trim(), 35, " "));

                compteMessageBean.setAdresseCompte("");
                compteMessageBean.setEscompte(Utility.getParam("LCE_SBF").trim());

                // compteMessageBean.setAgenceCompte(codeAgence);


                //Test de validité sur type de compte

                Icsibtc1[] icsibtc1 = (Icsibtc1[]) dbExt.retrieveRowAsObject("SELECT * FROM Icsibtc1 WHERE TYPECPT ='" + sibiccpt[0].getTypecpt() + "'", new Icsibtc1());
                if (icsibtc1 != null && icsibtc1.length > 0) {
                    switch (icsibtc1[0].getActions().intValue()) {
                        case 0:
                             {
                                //On ne fait rien
                                compteMessageBean.setAction("0");
                                compteMessageBean.setMessageResult("");

                            }
                            break;
                        case 1:
                             {
                                //On affiche un message non bloquant
                                compteMessageBean.setAction("1");
                                compteMessageBean.setMessageResult(icsibtc1[0].getLibelle());


                            }
                            break;
                        case 9:
                             {
                                //On affiche un message bloquant
                                compteMessageBean.setAction("9");
                                compteMessageBean.setMessageResult(icsibtc1[0].getLibelle());


                            }
                            break;
                        default: {
                            compteMessageBean.setAction("9");
                            compteMessageBean.setMessageResult("ACTION "+ icsibtc1[0].getActions() +" INCORRECTE POUR TYPE COMPTE "+sibiccpt[0].getTypecpt());

                        }
                    }

                } else {
                    compteMessageBean.setAction("9");
                    compteMessageBean.setMessageResult("TYPE COMPTE INEXISTANT ="+sibiccpt[0].getTypecpt());

                }

                Icsibtc2[] icsibtc2 = (Icsibtc2[]) dbExt.retrieveRowAsObject("SELECT * FROM Icsibtc2 WHERE STATUCPT ='" + sibiccpt[0].getStatucpt() + "'", new Icsibtc2());
                if (icsibtc2 != null && icsibtc2.length > 0) {
                    switch (icsibtc2[0].getActionsb().intValue()) {
                        case 0:
                             {
                                //On ne fait rien
                                compteMessageBean.setActionStatut("0");
                                compteMessageBean.setMessageStatutResult("");
                            }
                            break;
                        case 1:
                             {
                                //On affiche un message non bloquant
                                compteMessageBean.setActionStatut("1");
                                compteMessageBean.setMessageStatutResult(icsibtc2[0].getLibelle());
                            }
                            break;
                        case 9:
                             {
                                //On affiche un message bloquant
                                compteMessageBean.setActionStatut("9");
                                compteMessageBean.setMessageStatutResult(icsibtc2[0].getLibelle());

                            }
                            break;
                        default: {
                            compteMessageBean.setActionStatut("9");
                            compteMessageBean.setMessageStatutResult("ACTION "+ icsibtc2[0].getActionsb()+" INCORRECTE POUR STATUT COMPTE ="+icsibtc2[0].getStatucpt());

                        }
                    }

                } else {
                    compteMessageBean.setActionStatut("9");
                    compteMessageBean.setMessageStatutResult("STATUT COMPTE INEXISTANT ="+ sibiccpt[0].getStatucpt());

                }


                dbExt.close();

                return jsonConverter.objectToJSONStringArray(compteMessageBean);


            }
            dbExt.close();
            return "rien";
        //return getComboLiteral(new String[]{comptes[0].getNom()}, new String[]{comptes[0].getAgence()}).toString();


        } catch (Exception ex) {
            Logger.getLogger(ComboIdBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public String getComboDatas() {
        try {

            String[] labels = null;
            String[] values = null;
            if (codeAgence != null && codeAgence.length > 0) {
                labels = new String[codeAgence.length];
                values = new String[codeAgence.length];
                for (int i = 0; i < codeAgence.length; i++) {
                    labels[i] = "AGENCE " + codeAgence[i];
                    values[i] = "A" + codeAgence[i];
                }
                System.out.println(getComboLiteral(labels, values).toString());
                return getComboLiteral(labels, values).toString();
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ComboCompteBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getComboAgenceDatas(String numeroCompte) {
        try {
            String[] labels = null;
            String[] values = null;
            DataBase dbExt = new DataBase(ExtJDBCXmlReader.getDriver());
            dbExt.open(ExtJDBCXmlReader.getUrl(), ExtJDBCXmlReader.getUser(), ExtJDBCXmlReader.getPassword());

            Sibiccpt[] sibiccpt = (Sibiccpt[]) dbExt.retrieveRowAsObject(requete + " where compte like '" + Utility.bourrageGauche(numeroCompte, 11, "0") + "'", new Sibiccpt());


            if (sibiccpt != null && sibiccpt.length > 0) {
                labels = new String[sibiccpt.length];
                values = new String[sibiccpt.length];
                for (int i = 0; i < sibiccpt.length; i++) {
                    labels[i] = "AGENCE " + sibiccpt[i].getGuichet();
                    values[i] = "A" + sibiccpt[i].getGuichet();
                }
                dbExt.close();
                return getComboLiteral(labels, values).toString();
            }
            dbExt.close();
        } catch (Exception ex) {
            Logger.getLogger(ComboCompteBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public ComboCompteCNCEBean() {
    }
}
