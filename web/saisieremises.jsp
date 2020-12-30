<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,java.util.Date,clearing.table.Utilisateurs"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.Sequences,clearing.table.Remises,clearing.table.Sequences,clearing.table.Cheques,clearing.table.Banques"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>saisie remise.jsp</title>
        <style type="text/css">
        </style>
        <script type="text/javascript"  src="clearing.js"></script>
        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
    </head>
    <jsp:include page="checkaccess.jsp"/>
    <body onload="setFocus()">
        <%

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            String lastDocType = "";
            String nmourem = "0";
            int index = 0;
            int nbOpers = 0;
            Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            String action = request.getParameter("action");
            String params_auto = Utility.getParam("AUTORISATION_SAISIE");
            if (params_auto == null || (params_auto != null && params_auto.equalsIgnoreCase("1"))) {

                //out.println(action);
                if (action != null && (action.equalsIgnoreCase("precedent") || action.equalsIgnoreCase("suivant"))) {
                    //out.println(request.getParameter("idcheque"));

                    String idObjet = request.getParameter("idcheque");
                    String idRemise = request.getParameter("idremise");
                    lastDocType = "CHEQUE";
                    request.setAttribute("typeObjet", lastDocType);
                    request.setAttribute("sequenceObjet", "0");
                    long idCheque = 0;
                    String sql = "";
                    if (action.equalsIgnoreCase("precedent")) {
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + idRemise + " AND IDCHEQUE<" + idObjet + " ORDER BY IDCHEQUE ASC";

                    } else {
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + idRemise + " AND IDCHEQUE>" + idObjet + " ORDER BY IDCHEQUE DESC";

                    }
                    Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    if (cheques != null && cheques.length > 0) {
                        idCheque = cheques[cheques.length - 1].getIdcheque().longValue();
                    }
                    action = null;
                    sql = "SELECT * FROM CHEQUES WHERE IDCHEQUE=" + idCheque;
                    cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                    if (cheques != null && cheques.length > 0) {
                        BigDecimal curIdCheque = cheques[0].getIdcheque();
                        request.setAttribute("idObjet", curIdCheque);
                        session.setAttribute("currentCheque", cheques[0]);
                        request.setAttribute("montantCheque", cheques[0].getMontantcheque());
                        request.setAttribute("codeBanque", (cheques[0].getBanque() == null) ? "" : cheques[0].getBanque().contains("_") ? "" : cheques[0].getBanque());
                        request.setAttribute("codeAgence", (cheques[0].getAgence() == null) ? "" : cheques[0].getAgence().contains("_") ? "" : cheques[0].getAgence());
                        request.setAttribute("numeroCompte", (cheques[0].getNumerocompte() == null) ? "" : cheques[0].getNumerocompte().contains("_") ? "" : cheques[0].getNumerocompte());
                        request.setAttribute("clerib", (cheques[0].getRibcompte() == null) ? "" : cheques[0].getRibcompte().contains("_") ? "" : cheques[0].getRibcompte());
                        request.setAttribute("numeroCheque", (cheques[0].getNumerocheque() == null) ? "" : cheques[0].getNumerocheque().contains("_") ? "" : cheques[0].getNumerocheque());
                        request.setAttribute("idremise", cheques[0].getRemise());
                        sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[0].getRemise();
                        Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                        if (remises != null && remises.length > 0) {
                            session.setAttribute("currentRemise", remises[0]);
                            sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[0].getRemise() + " ORDER BY SEQUENCE";
                            Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                            for (int i = 0; i < chequesCal.length; i++) {
                                if (chequesCal[i].getIdcheque().equals(cheques[0].getIdcheque())) {
                                    index = i + 1;
                                }
                            }
                            nbOpers = remises[0].getNbOperation().intValue();
                            request.setAttribute("index", index);
                            request.setAttribute("nbOpers", nbOpers);

                        }

                        String fPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "f.jpg";
                        String rPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "r.jpg";
                        request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                        request.setAttribute("rPicture", rPicture.replace("\\", "/"));
                    }
                } else if (action != null && action.equalsIgnoreCase("remise")) {
                    out.println(request.getParameter("idremise"));
                    action = null;
                    String idRemise = request.getParameter("idremise");
                    lastDocType = "REMISE";
                    request.setAttribute("typeObjet", lastDocType);
                    request.setAttribute("sequenceObjet", "0");
                    request.setAttribute("nbDocs", "N/A");
                    String sql = "SELECT * FROM REMISES WHERE IDREMISE=" + idRemise;
                    Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                    if (remises != null && remises.length > 0) {
                        BigDecimal curIdRemise = remises[0].getIdremise();
                        session.setAttribute("currentRemise", remises[0]);
                        request.setAttribute("idObjet", curIdRemise);

                        String fPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "f.jpg";
                        String rPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "r.jpg";
                        request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                        request.setAttribute("rPicture", rPicture.replace("\\", "/"));

                        request.setAttribute("montantRemise", remises[0].getMontant());
                        request.setAttribute("compteRemettant", remises[0].getCompteRemettant());
                        sql = "SELECT * FROM CHEQUES WHERE REMISE=" + curIdRemise;
                        Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                        if (cheques != null && cheques.length > 0) {
                            nmourem = String.valueOf(cheques.length);
                        }

                        request.setAttribute("nmourem", nmourem);
                    }

                } else {
                    String sql = "SELECT * FROM SEQUENCES WHERE (MACHINESCAN IN (SELECT MACHINE FROM MACUTI WHERE UTILISATEUR = '" + user.getLogin().trim() + "')) AND (UTILISATEUR='' OR UTILISATEUR IS NULL OR UTILISATEUR = '" + user.getLogin().trim() + "') ORDER BY MACHINESCAN,IDSEQUENCE FOR UPDATE";
                    Sequences[] sequences = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());

                    if (sequences != null && sequences.length > 0) {
                        BigDecimal lastIdSeq = sequences[0].getIdsequence();
                        lastDocType = sequences[0].getTypedocument();
                        request.setAttribute("typeObjet", lastDocType);
                        request.setAttribute("sequenceObjet", lastIdSeq);
                        request.setAttribute("nbDocs", sequences.length);
                        if (lastDocType.equalsIgnoreCase("REMISE")) {

                            sql = "SELECT * FROM REMISES WHERE ETAT=" + Utility.getParam("CETAOPESTO") + " AND SEQUENCE=" + lastIdSeq;
                            Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                            if (remises != null && remises.length > 0) {
                                BigDecimal curIdRemise = remises[0].getIdremise();
                                session.setAttribute("currentRemise", remises[0]);
                                request.setAttribute("idObjet", curIdRemise);
                                String fPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "f.jpg";
                                String rPicture = remises[0].getPathimage().substring(3) + "\\" + remises[0].getFichierimage() + "r.jpg";
                                request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                                request.setAttribute("rPicture", rPicture.replace("\\", "/"));
                                request.setAttribute("montantRemise", remises[0].getMontant());
                                request.setAttribute("compteRemettant", remises[0].getCompteRemettant());
                                sql = "SELECT * FROM CHEQUES WHERE REMISE=" + curIdRemise + " ORDER BY SEQUENCE";
                                Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                                if (cheques != null && cheques.length > 0) {
                                    nmourem = String.valueOf(cheques.length);
                                    String lastChqSeq = cheques[cheques.length - 1].getSequence().toPlainString();
                                    sql = "UPDATE SEQUENCES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE (IDSEQUENCE>=" + lastIdSeq + "  AND IDSEQUENCE<=" + lastChqSeq + ") AND MACHINESCAN='" + cheques[cheques.length - 1].getMachinescan() + "'";
                                    db.executeUpdate(sql);
                                } else {// Cas de remise sans chq ou chq passé en remise
                                    sql = "UPDATE SEQUENCES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE =" + lastIdSeq;
                                    db.executeUpdate(sql);
                                }
                                request.setAttribute("nmourem", nmourem);
                            } else {
                                out.println("Incohérence entre REMISES et SEQUENCES, Cliquez sur Rafraichir");
                                sql = "UPDATE SEQUENCES SET UTILISATEUR='EMPTY' WHERE IDSEQUENCE=" + lastIdSeq;
                                db.executeUpdate(sql);
                                sql = "DELETE SEQUENCES WHERE IDSEQUENCE=" + lastIdSeq;
                                db.executeUpdate(sql);
                            }
                        } else {

                            sql = "UPDATE SEQUENCES SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE =" + lastIdSeq;
                            db.executeUpdate(sql);
                            sql = "SELECT * FROM CHEQUES WHERE ETAT=" + Utility.getParam("CETAOPESTO") + "  AND SEQUENCE=" + lastIdSeq;
                            Cheques[] cheques = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                            if (cheques != null && cheques.length > 0) {
                                Banques[] banques = (Banques[]) db.retrieveRowAsObject("SELECT * FROM BANQUES WHERE CODEBANQUE=" + ((cheques[0].getBanque() == null) ? "''" : "'" + cheques[0].getBanque() + "'"), new Banques());
                                if (banques != null && banques.length > 0) {
                                    request.setAttribute("libelleBanque", banques[0].getLibellebanque());
                                }
                                BigDecimal curIdCheque = cheques[0].getIdcheque();
                                request.setAttribute("idObjet", curIdCheque);
                                session.setAttribute("currentCheque", cheques[0]);
                                request.setAttribute("codeBanque", (cheques[0].getBanque() == null) ? "" : cheques[0].getBanque().contains("_") ? "" : cheques[0].getBanque());
                                request.setAttribute("codeAgence", (cheques[0].getAgence() == null) ? "" : cheques[0].getAgence().contains("_") ? "" : cheques[0].getAgence());
                                request.setAttribute("numeroCompte", (cheques[0].getNumerocompte() == null) ? "" : cheques[0].getNumerocompte().contains("_") ? "" : cheques[0].getNumerocompte());
                                request.setAttribute("clerib", (cheques[0].getRibcompte() == null) ? "" : cheques[0].getRibcompte().contains("_") ? "" : cheques[0].getRibcompte());
                                request.setAttribute("numeroCheque", (cheques[0].getNumerocheque() == null) ? "" : cheques[0].getNumerocheque().contains("_") ? "" : cheques[0].getNumerocheque());
                                request.setAttribute("idremise", cheques[0].getRemise());
                                sql = "SELECT * FROM REMISES WHERE IDREMISE=" + cheques[0].getRemise();
                                Remises[] remises = (Remises[]) db.retrieveRowAsObject(sql, new Remises());
                                if (remises != null && remises.length > 0) {
                                    session.setAttribute("currentRemise", remises[0]);
                                    sql = "SELECT * FROM CHEQUES WHERE REMISE=" + cheques[0].getRemise() + " ORDER BY SEQUENCE";
                                    Cheques[] chequesCal = (Cheques[]) db.retrieveRowAsObject(sql, new Cheques());
                                    for (int i = 0; i < chequesCal.length; i++) {
                                        if (chequesCal[i].getIdcheque().equals(cheques[0].getIdcheque())) {
                                            index = i + 1;
                                        }
                                    }

                                    if (remises[0].getNbOperation() != null) {
                                        nbOpers = remises[0].getNbOperation().intValue();
                                        request.setAttribute("nbOpers", nbOpers);
                                    }

                                    request.setAttribute("index", index);

                                }

                                String fPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "f.jpg";
                                String rPicture = cheques[0].getPathimage().substring(3) + "\\" + cheques[0].getFichierimage() + "r.jpg";
                                request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                                request.setAttribute("rPicture", rPicture.replace("\\", "/"));
                            } else {
                                out.println("Incohérence entre CHEQUES et SEQUENCES, Cliquez sur Rafraichir");
                                sql = "UPDATE SEQUENCES SET UTILISATEUR='EMPTY' WHERE IDSEQUENCE=" + lastIdSeq;
                                db.executeUpdate(sql);
                                sql = "DELETE SEQUENCES WHERE IDSEQUENCE=" + lastIdSeq;
                                db.executeUpdate(sql);
                            }
                        }
                    } else {
                        out.println("Plus rien à saisir");
                        lastDocType = "";

                        out.println("Voulez-vous imprimer votre rapport quotidien?" + "<BR>");

                        String dateTraitement = Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyy/MM/dd");

                        String interval = "Agence de Saisie:" + user.getAdresse().trim() + "| Date de Traitement du " + dateTraitement;
                        out.println(interval);
                        String query = "select C1.IDCHEQUE,C1.REMISE,C1.REFREMISE,C1.DEVISE,C1.NUMEROCHEQUE,C1.NUMEROCOMPTE,C1.NOMBENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.COMPTEREMETTANT,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,"
                                + "C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantcheque ,C1.MOTIFREJET "
                                + "from cheques C1,cheques C2, Banques B1,Banques B2,Remises R "
                                + "where C1.IDCHEQUE=C2.IDCHEQUE  AND C1.banqueremettant=B1.codebanque  and C2.banque=B2.codebanque "
                                + "and C1.etat in (30)  "
                                + "and C1.datetraitement ='" + dateTraitement + "' and C1.remise=R.idremise "
                                + "and R.agenceDepot like '" + user.getAdresse().trim() + "' ";
        %>
        <form name="printReport"  action="ControlServlet" method="POST" onsubmit="return resolveQuotidien();">
            <table>

                <tr>
                    <td><input type="radio" id="orderbybanque" name="nomrapport" value="Cheques" checked="checked"/>Regroupé par Banque</td>
                    <td><input type="radio" id="orderbyremise" name="nomrapport" value="Cheques_clients_remises"  />Regroupé par Remise</td>
                </tr>
                <tr>
                    <td><input type="radio" name="typerapport" value="application/vnd.ms-excel" />EXCEL</td>
                    <td><input type="radio" name="typerapport" value="application/rtf" />WORD</td>
                    <td><input type="radio" name="typerapport" value="application/pdf"  checked="checked"/>PDF</td>
                    <td><input type="radio" name="typerapport" value="text/html" />Aperçu HTML</td>
                </tr>
                <tr>
                    <td><input type="submit" value="Imprimer" name="oui" /></td>
                    <td><input type="checkbox" name="avecDetail" value="ON" checked="checked"/>Avec Détails</td>

                </tr>
            </table>
            <input type='hidden' name='action' value='rapport' />
            <input type='hidden' name='interval' value='<%=interval%>'/>
            <input type='hidden' name='requete' value="dynamique" />
            <input type='hidden' name='query' value="<%=query%>"/>

        </form>

    </div>


    <%

                }
            }

        } else {
            out.println("Saisie désactivée par Administrateur|AUTORISATION_SAISIE = " + params_auto);
            lastDocType = "";
        }
        db.close();

    %>
    <%if (!lastDocType.equals("")) {%>

    <div align="center">
        <form name='outilsForm'>

            <input type="button" value="Supprimer" name="deleteDocBtn" onclick="  if (confirm('Etes vous sur?')) {
                        document['outilsForm'].action.value = 'deleteDocBtn';
                        doAjaxSubmitWithResult(document['outilsForm'], undefined, 'ControlServlet');
                    }"/>
            <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
            <input type="hidden" name="typeObjet" value="${requestScope['typeObjet']}"/>
            <input type="hidden" name="sequenceObjet" value="${requestScope['sequenceObjet']}"/>
            <input type="hidden" name="action" value="dynamique"/>
        </form>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null && message.getMessage() != null) {
                out.print(message.getMessage());
            }%>
    </div>
    <br>

    <div id="imageDIV" >
        <font style="font-weight:bold;color:blue"> Remises et Cheques Restants : ${requestScope['nbDocs']}</font>
        <form name='imgForm'>
            <img name='imgchqr' id='imgchqr' src='${requestScope['fPicture']}' width="610" height="300" alt='|Aucune image recto associee|' />

            <img name='imgchqv' id='imgchqv' src='${requestScope['rPicture']}' width="610" height="300" alt='|Aucune image verso associee|' />

        </form>

    </div>

    <%}%>
    <% if (lastDocType.equalsIgnoreCase("REMISE")) {%>
    <div align="center">
        <form name='transForm'>
            <input type="button" value="Transformer en Chèque" name="transformBtn" onclick="if (confirm('Etes vous sur?')) {
                        document['transForm'].action.value = 'transformBtn';
                        doAjaxSubmitWithResult(document['transForm'], undefined, 'ControlServlet');
                    }"/>
            <input type="hidden" name="idObjet" value="${requestScope['idObjet']}"/>
            <input type="hidden" name="typeObjet" value="${requestScope['typeObjet']}"/>
            <input type="hidden" name="sequenceObjet" value="${requestScope['sequenceObjet']}"/>
            <input type="hidden" name="action" value="dynamique"/>
        </form>
    </div>
    <div id="tableSaisieRemiseDIV" align="center">
        <fieldset id="divField">
            <legend id="divLegend">Saisie de la Remise</legend>
            <jsp:useBean id="comboCompteBean" scope="session" class="clearing.web.bean.ComboCompteBean" />
            <form  id="remiseForm" name="remiseForm" onsubmit=" return false;">
                <table>

                    <tbody>

                        <tr>
                            <td>Numéro de Compte: </td>

                            <%  if (request.getAttribute("compteRemettant") == null) {%>
                            <td><input type="text" name="numero" value="" maxlength="16" onKeypress="return numeriqueValide(event);" onblur=" chargeInfoCompte();" /></td>
                                <% } else {%>
                            <td><input type="text" name="numero" value="${requestScope['compteRemettant']}" maxlength="16" onKeypress="return numeriqueValide(event);" onblur=" chargeInfoCompte();" /></td>
                                <%}
                                    ;%>
                            <td></td>
                            <td></td>
                            <td>Montant: </td>
                            <%  if (request.getAttribute("montantRemise") == null) {%>
                            <!--<td><input type="text" name="montantRemise" value="" onKeypress="return(currencyFormat(this,' ',event));" /></td>-->
                            <td><input type="text" name="montantRemise" value="" onKeypress="return(currencyFormat(this, ' ', event));" /></td>
                                <% } else {%>
                            <td><input type="text" name="montantRemise" value="${requestScope['montantRemise']}" onKeypress="return numeriqueValide(event);"/></td>
                                <%}
                                    ;%>

                            <td></td>
                        </tr>
                        <tr>
                            <td>Nom: </td>
                            <td><input type="text" name="nom" value="" size="25" disabled /></td>
                            <td></td>
                            <td></td>
                            <td>Nbre d'instruments:</td>
                            <td><input type="text" name="nmourem" value="${requestScope['nmourem']}" disabled/></td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>Agence: </td>
                            <td><input type="text" name="agence" value=""  disabled/></td>
                            <td></td>
                            <td></td>
                            <td>Reference: </td>
                            <td><input type="text" name="reference" value=""  /></td>
                            <td></td>
                            <td><input type="submit" name="valider" value="Valider" onclick="checkRemise();" /></td>
                                <a:widget name="yahoo.tooltip" args="{context:['remiseForm']}" value="Saisie de Remise"  />
                        </tr>
                    </tbody>
                </table>

                <input type="hidden" value="checkRemise" name="action" />
                <input type="hidden" value="" name="escompte" />
                <input type="hidden" name="sequenceObjet" value="${requestScope['sequenceObjet']}"/>
            </form>

        </fieldset>
    </div>

    <%}
        if (lastDocType.equalsIgnoreCase("CHEQUE")) {%>
    <div id="tableSaisieChequeDIV" align="center">
        <fieldset id="divField">
            <legend id="divLegend">Saisie des Chèques</legend>
            <form  id="chequeForm" name="chequeForm" onsubmit="return false;">
                <table>

                    <tbody>
                        <tr>
                            <td>Montant: </td>
                            <%  if (request.getAttribute("montantCheque") == null) {%>
                            <td><input type="text" name="montantCheque" value="" onKeypress="return(currencyFormat(this, ' ', event));"/></td>
                                <% } else {%>
                            <td><input type="text" name="montantCheque" value="${requestScope['montantCheque']}" onKeypress="return numeriqueValide(event);"/></td>
                                <%}
                                    ;%>


                            <td></td>
                            <td>Remise: </td>
                            <td><input type="button" name="idremise" value="${requestScope['idremise']}" onclick="location.replace('saisieremises.jsp?action=remise&idremise=${requestScope["idremise"]}')"/></td>
                            <td><input type="checkbox" id="force" name="forcage" value="OFF" />Forcage Doublon</td>
                        </tr>
                        <tr>
                            <td>Emetteur: </td>
                            <td><input type="text" name="nomTire" value="${requestScope['nomTire']}" maxlength="25" /></td>
                            <td></td>
                            <td></td>
                            <td>Date Emission:</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateemission" /> </td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>Banque: </td>

                            <td><input type="text" name="libelleBanque" value="${requestScope['libelleBanque']}" disabled/></td>
                            <td><input type="text" name="codeBanque" value="${requestScope['codeBanque']}" maxlength="5" size="5" onblur=" chargeLibelleBanque();"/></td>
                            <td></td>
                            <td>Agence:</td>
                            <td><input type="text" name="codeAgence" value="${requestScope['codeAgence']}" maxlength="5" onKeypress="return numeriqueValide(event);"/></td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>Numero de Compte: </td>
                            <td><input type="text" name="numeroCompte"  value="${requestScope['numeroCompte']}" maxlength="12" onKeypress="return numeriqueValide(event);"/></td>
                            <td><input type="text" name="clerib" size="2" value="${requestScope['clerib']}"  maxlength="2"/></td>
                            <td></td>

                            <td>Numero de Cheque:</td>
                            <td><input type="text" name="numeroCheque"  value="${requestScope['numeroCheque']}" maxlength="7" onKeypress="return numeriqueValide(event);"/></td>
                            <td></td>
                            <td><input type="submit" name="valider" value="Valider" onclick="checkCheque();"/></td>

                            <a:widget name="yahoo.tooltip" args="{context:['chequeForm']}" value="Saisie de Chèques"  />



                        </tr>
                    </tbody>
                </table>
                <input type="hidden" value="" name="dateEmis" />
                <input type="hidden" value="checkCheque" name="action" />
                <input type="hidden" value="${requestScope['index']}" name="index" />
            </form>
            <b>Cheque ${requestScope['index']} sur ${requestScope['nbOpers']}</b>
            <br>
            <%if (index > 1) {%>
            <input type="button" value="Precedent" name="Précédent" onclick="window.location.replace('saisieremises.jsp?action=precedent&idcheque=${requestScope['idObjet']}&idremise=${requestScope['idremise']}')"/>
            <%}%>
            <%if (nbOpers > index) {%>
            <input type="button" value="Suivant" name="Suivant" onclick="window.location.replace('saisieremises.jsp?action=suivant&idcheque=${requestScope['idObjet']}&idremise=${requestScope['idremise']}')"/>
            <%}%>
        </fieldset>
    </div>
    <%}%>


    <script>

        function zoomImgr(e) {

            document.getElementById('imgchqr').width = document.getElementById('imgchqr').width + 20;
            document.getElementById('imgchqr').height = document.getElementById('imgchqr').height + 10;
        }
        function zoomImgv(e) {

            document.getElementById('imgchqv').width = document.getElementById('imgchqr').width + 20;
            document.getElementById('imgchqv').height = document.getElementById('imgchqr').height + 10;
        }

        function deZoomImgr(e) {

            document.oncontextmenu = new Function("return false");
            if ((!document.all && e.which == 3) || (document.all && e.button == 2))
            {
                document.getElementById('imgchqr').width = document.getElementById('imgchqr').width - 20;
                document.getElementById('imgchqr').height = document.getElementById('imgchqr').height - 10;
            }
            //document.oncontextmenu = null;

        }
        function deZoomImgv(e) {
            document.oncontextmenu = new Function("return false");
            if ((!document.all && e.which == 3) || (document.all && e.button == 2))
            {

                document.getElementById('imgchqv').width = document.getElementById('imgchqv').width - 20;
                document.getElementById('imgchqv').height = document.getElementById('imgchqv').height - 10;
            }

            // document.oncontextmenu = null;
        }
        function changeCursorToZoom(e) {
            document.getElementById('imgchqr').style.cursor = "crosshair";
            document.getElementById('imgchqv').style.cursor = "crosshair";
        }
        function changeCursorToDefault(e) {
            document.getElementById('imgchqr').style.cursor = "default";
            document.getElementById('imgchqv').style.cursor = "default";
        }
        document.getElementById('imgchqr').onmousedown = deZoomImgr;
        document.getElementById('imgchqr').onclick = zoomImgr;
        document.getElementById('imgchqr').onmouseover = changeCursorToZoom;
        document.getElementById('imgchqr').onmouseout = changeCursorToDefault;
        document.getElementById('imgchqv').onmousedown = deZoomImgv;
        document.getElementById('imgchqv').onclick = zoomImgv;
        document.getElementById('imgchqv').onmouseover = changeCursorToZoom;
        document.getElementById('imgchqv').onmouseout = changeCursorToDefault;
    </script>
</body>
</html>