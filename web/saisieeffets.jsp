<%@page contentType="text/html" import="org.patware.jdbc.DataBase,java.math.BigDecimal,java.util.Date,clearing.table.Utilisateurs"%>
<%@page pageEncoding="UTF-8" import="org.patware.xml.JDBCXmlReader,org.patware.web.bean.MessageBean,org.patware.utils.Utility"%>
<%@page import="org.patware.xml.JDBCXmlReader,clearing.table.SequencesEffets,clearing.table.RemisesEffets,clearing.table.Sequences,clearing.table.Effets,clearing.table.Banques"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <style type="text/css">
        </style>
        <script type="text/javascript"  src="clearing.js"></script>
        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
    </head>
    <jsp:include page="checkaccess.jsp"/>
    <body onload="setFocusCheque()">
        <%

            DataBase db = new DataBase(JDBCXmlReader.getDriver());
            String lastDocType = "";
            String nmourem = "0";
            int index = 0;
            int nbOpers = 0;
            long sumCurEffets = 0;
            Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
            request.setAttribute("escompte", 0);
            db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
            String action = request.getParameter("action");
            String params_auto = Utility.getParam("AUTORISATION_SAISIE");
            if (params_auto == null || (params_auto != null && params_auto.equalsIgnoreCase("1"))) {

                //out.println(action);
                if (action != null && (("precedent|remise").contains(action) || action.equalsIgnoreCase("suivant"))) {
                    //out.println(request.getParameter("idcheque"));

                    String idObjet = request.getParameter("ideffet");
                    String idRemise = request.getParameter("idremise");
                    lastDocType = "EFFETS";
                    request.setAttribute("typeObjet", lastDocType);

                    long idEffet = 0;
                    String sql = "";
                    if (("precedent|remise").contains(action)) {
                        sql = "SELECT * FROM EFFETS WHERE REMISE=" + idRemise + " AND IDEFFET<" + idObjet + " ORDER BY IDEFFET ASC";

                    } else {
                        sql = "SELECT * FROM EFFETS WHERE REMISE=" + idRemise + " AND IDEFFET>" + idObjet + " ORDER BY IDEFFET DESC";

                    }
                    Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                    if (effets != null && effets.length > 0) {
                        if (action.equalsIgnoreCase("remise")) {
                            idEffet = effets[0].getIdeffet().longValue();
                        } else {
                            idEffet = effets[effets.length - 1].getIdeffet().longValue();
                        }
                    }
                    action = null;
                    sql = "SELECT * FROM EFFETS WHERE IDEFFET=" + idEffet;
                    effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                    if (effets != null && effets.length > 0) {
                        BigDecimal curIdEffet = effets[0].getIdeffet();
                        request.setAttribute("idObjet", curIdEffet);
                        session.setAttribute("currentEffet", effets[0]);
                        request.setAttribute("montantEffet", effets[0].getMontant_Effet());
                        request.setAttribute("codeBanque", (effets[0].getBanque() == null) ? "" : effets[0].getBanque().contains("_") ? "" : effets[0].getBanque());
                        request.setAttribute("codeAgence", (effets[0].getAgence() == null) ? "" : effets[0].getAgence().contains("_") ? "" : effets[0].getAgence());
                        request.setAttribute("numeroCompte", (effets[0].getNumerocompte_Tire() == null) ? "" : effets[0].getNumerocompte_Tire().contains("_") ? "" : effets[0].getNumerocompte_Tire());
                        request.setAttribute("clerib", (effets[0].getRibcompte()== null) ? "" : effets[0].getRibcompte().contains("_") ? "" : effets[0].getRibcompte());
                        request.setAttribute("numeroEffet", (effets[0].getNumeroeffet() == null) ? "" : effets[0].getNumeroeffet().contains("_") ? "" : effets[0].getNumeroeffet());
                        request.setAttribute("idremise", effets[0].getRemise());
                        request.setAttribute("sequenceObjet", effets[0].getSequence().toPlainString());
                        Banques[] banques = (Banques[]) db.retrieveRowAsObject("SELECT * FROM BANQUES WHERE CODEBANQUE=" + ((effets[0].getBanque() == null) ? "''" : "'" + effets[0].getBanque() + "'"), new Banques());
                        if (banques != null && banques.length > 0) {
                            request.setAttribute("libelleBanque", banques[0].getLibellebanque());
                        }
                        sql = "SELECT * FROM REMISESEFFETS WHERE IDREMISE=" + effets[0].getRemise();
                        RemisesEffets[] remises = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
                        if (remises != null && remises.length > 0) {
                            session.setAttribute("currentRemise", remises[0]);
                            request.setAttribute("montantRemise", remises[0].getMontant());
                            request.setAttribute("compteRemettant", remises[0].getCompteRemettant());
                            request.setAttribute("nomRemettant", remises[0].getNomClient());
                            request.setAttribute("agenceRemettant", remises[0].getAgenceRemettant());
                            request.setAttribute("reference", remises[0].getReference());
                            request.setAttribute("escompte", remises[0].getEscompte());
                            sql = "SELECT * FROM EFFETS WHERE REMISE=" + effets[0].getRemise() + " ORDER BY SEQUENCE";
                            Effets[] effetsCal = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                            if (effetsCal != null && effetsCal.length > 0) {
                                nmourem = String.valueOf(effetsCal.length);
                                request.setAttribute("nmourem", nmourem);
                                for (int i = 0; i < effetsCal.length; i++) {
                                    if (effetsCal[i].getIdeffet().equals(effets[0].getIdeffet())) {
                                        index = i + 1;
                                    }
                                    if (effetsCal[i].getMontant_Effet() != null) {
                                        sumCurEffets += Long.parseLong(effetsCal[i].getMontant_Effet().trim());

                                    }
                                }
                            }
                            nbOpers = remises[0].getNbOperation().intValue();
                            request.setAttribute("index", index);
                            request.setAttribute("nbOpers", nbOpers);

                        }

                        String fPicture = effets[0].getPathimage().substring(3) + "\\" + effets[0].getFichierimage() + "f.jpg";
                        String rPicture = effets[0].getPathimage().substring(3) + "\\" + effets[0].getFichierimage() + "r.jpg";
                        request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                        request.setAttribute("rPicture", rPicture.replace("\\", "/"));

                    }
                } else {
                    String sql = "SELECT * FROM SEQUENCES_EFFETS WHERE (MACHINESCAN IN (SELECT MACHINE FROM MACUTI WHERE UTILISATEUR = '" + user.getLogin().trim() + "')) AND (UTILISATEUR='' OR UTILISATEUR IS NULL OR UTILISATEUR = '" + user.getLogin().trim() + "') ORDER BY MACHINESCAN,IDSEQUENCE FOR UPDATE";
                    Sequences[] sequences = (Sequences[]) db.retrieveRowAsObject(sql, new Sequences());

                    if (sequences != null && sequences.length > 0) {
                        BigDecimal lastIdSeq = sequences[0].getIdsequence();
                        lastDocType = sequences[0].getTypedocument();
                        request.setAttribute("typeObjet", lastDocType);
                        request.setAttribute("sequenceObjet", lastIdSeq);
                        request.setAttribute("nbDocs", sequences.length);


                        sql = "UPDATE SEQUENCES_EFFETS SET UTILISATEUR='" + user.getLogin().trim() + "' WHERE IDSEQUENCE =" + lastIdSeq;
                        db.executeUpdate(sql);
                        sql = "SELECT * FROM EFFETS WHERE ETAT=" + Utility.getParam("CETAOPESTO") + "  AND SEQUENCE=" + lastIdSeq;//recup des effets dont letat = 10 et le numsequence est egal au dernier num de sequence
                        Effets[] effets = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                        if (effets != null && effets.length > 0) {
                            Banques[] banques = (Banques[]) db.retrieveRowAsObject("SELECT * FROM BANQUES WHERE CODEBANQUE=" + ((effets[0].getBanque() == null) ? "''" : "'" + effets[0].getBanque() + "'"), new Banques());
                            if (banques != null && banques.length > 0) {
                                request.setAttribute("libelleBanque", banques[0].getLibellebanque());
                            }
                            BigDecimal curIdEffet = effets[0].getIdeffet();
                            request.setAttribute("idObjet", curIdEffet);
                            session.setAttribute("currentEffet", effets[0]);
                            request.setAttribute("codeBanque", (effets[0].getBanque() == null) ? "" : effets[0].getBanque().contains("_") ? "" : effets[0].getBanque());
                            request.setAttribute("codeAgence", (effets[0].getAgence() == null) ? "" : effets[0].getAgence().contains("_") ? "" : effets[0].getAgence());
                            request.setAttribute("numeroCompte", (effets[0].getNumerocompte_Tire() == null) ? "" : effets[0].getNumerocompte_Tire().contains("_") ? "" : effets[0].getNumerocompte_Tire());
                            request.setAttribute("clerib", (effets[0].getRibcompte() == null) ? "" : effets[0].getRibcompte().contains("_") ? "" : effets[0].getRibcompte());
                            request.setAttribute("numeroEffet", (effets[0].getNumeroeffet() == null) ? "" : effets[0].getNumeroeffet().contains("_") ? "" : effets[0].getNumeroeffet());


                            if (effets[0].getRemise() != null) {
                                request.setAttribute("idremise", effets[0].getRemise());
                                sql = "SELECT * FROM REMISESEFFETS WHERE IDREMISE=" + effets[0].getRemise();
                                RemisesEffets[] remises = (RemisesEffets[]) db.retrieveRowAsObject(sql, new RemisesEffets());
                                if (remises != null && remises.length > 0) {
                                    session.setAttribute("currentRemise", remises[0]);
                                    //request.setAttribute("idObjet", remises[0].getIdremise());
                                    request.setAttribute("montantRemise", remises[0].getMontant());
                                    request.setAttribute("compteRemettant", remises[0].getCompteRemettant());
                                    request.setAttribute("nomRemettant", remises[0].getNomClient());
                                    request.setAttribute("agenceRemettant", remises[0].getAgenceRemettant());
                                    request.setAttribute("reference", remises[0].getReference());
                                    request.setAttribute("escompte", remises[0].getEscompte());
                                    sql = "SELECT * FROM EFFETS WHERE REMISE=" + effets[0].getRemise() + " ORDER BY SEQUENCE";
                                    Effets[] effetsCal = (Effets[]) db.retrieveRowAsObject(sql, new Effets());
                                    if (effetsCal != null && effetsCal.length > 0) {
                                        nmourem = String.valueOf(effetsCal.length);
                                        request.setAttribute("nmourem", nmourem);
                                        for (int i = 0; i < effetsCal.length; i++) {
                                            if (effetsCal[i].getIdeffet().equals(effets[0].getIdeffet())) {
                                                index = i + 1;
                                            }
                                            if (effetsCal[i].getMontant_Effet() != null) {
                                                sumCurEffets += Long.parseLong(effetsCal[i].getMontant_Effet().trim());

                                            }
                                        }
                                    }
                                    if (remises[0].getNbOperation() != null) {
                                        nbOpers = remises[0].getNbOperation().intValue();
                                        if (nbOpers != Integer.parseInt(nmourem)) {
                                            nbOpers = Integer.parseInt(nmourem);
                                            remises[0].setNbOperation(new BigDecimal(nbOpers));
                                            sql = "UPDATE REMISESEFFETS SET NBOPERATION=" + nmourem + " WHERE IDREMISE=" + remises[0].getIdremise();
                                            db.executeUpdate(sql);
                                            session.setAttribute("currentRemise", remises[0]);
                                        }
                                        request.setAttribute("nbOpers", nbOpers);
                                    }

                                    request.setAttribute("index", index);


                                }


                            } else {
                                request.setAttribute("idremise", null);
                                request.setAttribute("index", index);
                                request.setAttribute("nbOpers", nbOpers);
                                session.setAttribute("currentRemise", null);
                            }

                            String fPicture = effets[0].getPathimage().substring(3) + "\\" + effets[0].getFichierimage() + "f.jpg";
                            String rPicture = effets[0].getPathimage().substring(3) + "\\" + effets[0].getFichierimage() + "r.jpg";
                            request.setAttribute("fPicture", fPicture.replace("\\", "/"));
                            request.setAttribute("rPicture", rPicture.replace("\\", "/"));
                        } else {
                            out.println("Incohérence entre EFFETS et SEQUENCES_EFFETS, Cliquez sur Rafraichir");
                            sql = "UPDATE SEQUENCES_EFFETS SET UTILISATEUR='EMPTY' WHERE IDSEQUENCE=" + lastIdSeq;
                            db.executeUpdate(sql);
                            sql = "DELETE SEQUENCES_EFFETS WHERE IDSEQUENCE=" + lastIdSeq;
                            db.executeUpdate(sql);
                        }

                    } else {
                        out.println("<B>Plus rien à saisir</B>");
                        lastDocType = "";
                        String etat = "";
                        if (Utility.getParam("VALIDATION_CHEQUE_AUTO") != null && Utility.getParam("VALIDATION_CHEQUE_AUTO").equalsIgnoreCase("AUTO")) {
                                 etat = Utility.getParam("CETAOPEALLICOM1") ;
                            }else{
                                 etat = Utility.getParam("CETAOPEVAL") ;
                        }

                        out.println("<BR><B>Voulez-vous imprimer votre rapport quotidien?" + "</B><BR>");

                        String dateTraitement = Utility.convertDateToString(new Date(System.currentTimeMillis()), "yyyy/MM/dd");

                        String interval = "Agence de Saisie:" + user.getAdresse().trim() + "| Date de Traitement du " + dateTraitement;
                        out.println(interval);
                        String query = "select C1.IDEFFET,C1.REMISE,C1.REFREMISE,C1.DEVISE,C1.NUMEROEFFET,C1.NUMEROCOMPTE_TIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.numerocompte_beneficiaire,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,"
                                + "C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montant_effet ,C1.MOTIFREJET "
                                + "from effets C1,effets C2, Banques B1,Banques B2,Remiseseffets R "
                                + "where C1.IDEFFET=C2.IDEFFET  AND C1.banqueremettant=B1.codebanque  and C2.banque=B2.codebanque "
                                + "and C1.etat in ("+ etat +")  "
                                + "and C1.datetraitement ='" + dateTraitement + "' and C1.remise=R.idremise "
                                + "and R.agenceDepot like '" + user.getAdresse().trim() + "' ";


        %>
        <form name="printReport"  action="ControlServlet" method="POST" onsubmit="return resolveQuotidieneffets();">
            <table>

                <tr>
                    <td><input type="radio" id="orderbybanque" name="nomrapport" value="Effets" checked="checked"/>Regroupé par Banque</td>
                    <td><input type="radio" id="orderbyremise" name="nomrapport" value="Effets_clients_remises"  />Regroupé par Remise</td>
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
            request.setAttribute("sumCurEffets", Utility.formatNumber("" + sumCurEffets));

        } else {
            out.println("Saisie désactivée par Administrateur|AUTORISATION_SAISIE = " + params_auto);
            lastDocType = "";
        }
        db.close();

    %>
    <%if (!lastDocType.equals("")) {%>

    <div align="center">
        <form name='outilsForm'>

            <input type="button" value="Supprimer" name="deleteDocEffBtn" onclick="  if (confirm('Etes vous sur?')) {
                document['outilsForm'].action.value = 'deleteDocEffBtn';
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
    <br> <% if (index == 0 || index == 1) {%>

    <div id="tableSaisieRemiseDIV" align="center">
        <fieldset id="divField">
            <legend id="divLegend">Saisie Remise Effets</legend>
            <jsp:useBean id="comboCompteBean" scope="session" class="clearing.web.bean.ComboCompteBean" />
            <form  id="remiseForm" name="remiseForm" onsubmit=" return false;">
                <table>

                    <tbody>

                        <tr>
                            <td>Compte Remettant*: </td>

                            <%  if (request.getAttribute("compteRemettant") == null) {%>
                            <td><input type="text" name="numero" value="" maxlength="16" onKeypress="return numeriqueValide(event);" onblur=" chargeInfoCompte();" /></td>
                                <% } else {%>
                            <td><input type="text" name="numero" value="${requestScope['compteRemettant']}" maxlength="16" onKeypress="return numeriqueValide(event);" onblur=" chargeInfoCompte();" /></td>
                                <%}%>
                            <td></td>
                            <td></td>
                            <td>Montant Remise*: </td>
                            <%  if (request.getAttribute("montantRemise") == null) {%>
                            <td><input type="text" name="montantRemise" value="" onKeypress="return(currencyFormat(this, ' ', event));" /></td>
                                <% } else {%>
                            <td><input type="text" name="montantRemise" value="${requestScope['montantRemise']}" onKeypress="return numeriqueValide(event);"/></td>
                                <%}%>

                            <td></td>
                        </tr>
                        <tr>
                            <td>Nom Client: </td>
                            <%  if (request.getAttribute("nomRemettant") == null) {%>
                            <td><input type="text" name="nom" value="" size="25" disabled /></td>
                                <% } else {%>
                            <td><input type="text" name="nom" value="${requestScope['nomRemettant']}" size="25" disabled /></td>
                                <%}%>
                            <td></td>
                            <td></td>
                            <td>Nbre d'effets*:</td>
                            <%  if (request.getAttribute("nmourem") == null) {%>
                            <td><input type="text" name="nmourem" size="3" value="" onKeypress="return numeriqueValide(event);"/></td>
                                <% } else {%>
                            <td><input type="text" name="nmourem" size="3" value="${requestScope['nmourem']}" onKeypress="return numeriqueValide(event);"/></td>
                                <%}%>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>Agence: </td>
                            <%  if (request.getAttribute("agenceRemettant") == null) {%>
                            <td><input type="text" name="agence" value=""  disabled/></td>
                                <% } else {%>
                            <td><input type="text" name="agence" value="${requestScope['agenceRemettant']}"  disabled/></td>
                                <%}%>
                            <td></td>
                            <td></td>
                            <td>Reference Effet*: </td>
                            <%  if (request.getAttribute("reference") == null) {%>
                            <td><input type="text" name="reference" value="" maxlength="12" /></td>
                                <% } else {%>
                            <td><input type="text" name="reference" value="${requestScope['reference']}" maxlength="12" /></td>
                                <%}%>
                            <td></td>
                            <td><input type="submit" name="valider" value="Valider" onclick="checkRemiseViergeEffets();" /></td>

                        </tr>
                    </tbody>
                </table>

                <input type="hidden" value="checkRemiseViergeEffets" name="action" />
                <input type="hidden"  name="escompte" value="${requestScope['escompte']}"/>
                <input type="hidden" name="sequenceObjet" value="${requestScope['sequenceObjet']}"/>
                <a:widget name="yahoo.tooltip" args="{context:['remiseForm']}" value="${requestScope['nomRemettant']}"  />

            </form>
        </fieldset>
    </div>

    <%}%>

    <div id="imageDIV" >
        <font style="font-weight:bold;color:blue"> Effets Restants : ${requestScope['nbDocs']}</font>
        <form name='imgForm'>
            <img name='imgchqr' id='imgchqr' src='${requestScope['fPicture']}' width="610" height="300" alt='|Aucune image recto associee|' />

            <img name='imgchqv' id='imgchqv' src='${requestScope['rPicture']}' width="610" height="300" alt='|Aucune image verso associee|' />

        </form>

    </div>
    <br><br>
    <% if (index > 0) {%>

    <div id="tableSaisieChequeDIV" align="center">
        <fieldset id="divField">
            <legend id="divLegend">Saisie des Effets</legend>
            <form  id="chequeForm" name="chequeForm" onsubmit="return false;">
                <table>

                    <tbody>
                        <tr>
                            <td>Montant Effet*: </td>
                            <%  if (request.getAttribute("montantEffet") == null) {%>
                            <td><input type="text" name="montantEffet" value="" onKeypress="return(currencyFormat(this, ' ', event));"/></td>
                                <% } else {%>
                            <td><input type="text" name="montantEffet" value="${requestScope['montantEffet']}" onKeypress="return numeriqueValide(event);"/></td>
                                <%}%>


                            <td></td>
                            <td>Somme Courante: </td>
                            <td><input type="button" name="idremise" value="${requestScope['sumCurEffets']}" disabled/></td>
<!--                            <td><input type="checkbox" id="force" name="forcage" value="OFF" />Forcage Doublon</td>-->
                            <td>  
                            <td>
                            <td>
                        </tr>
                         
                        <tr>
                            <td>Emetteur: </td>
                            <td><input type="text" name="nomTire" value="${requestScope['nomTire']}" maxlength="25" /></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                        <tr>
                            <td>Banque*: </td>

                            <td><input type="text" name="libelleBanque" value="${requestScope['libelleBanque']}" disabled/></td>
                            <td><input type="text" name="codeBanque" value="${requestScope['codeBanque']}" maxlength="5" size="5" onblur=" chargeLibelleBanque();"/></td>
                            <td></td>
                            <td>Agence*:</td>
                            <td><input type="text" name="codeAgence" value="${requestScope['codeAgence']}" maxlength="5" onKeypress="return numeriqueValide(event);"/></td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>Montant Frais: </td>
                            <td><input type=text name="montantfrais" value="0" onKeypress="return numeriqueValide(event);" /></td>
                            <td></td>
                            <td></td>
                            <td><b>Type Effet</b></td>
                            <td>
				<SELECT name="typeEffet" size="1">
                                    <OPTION value="" selected></OPTION>
                                    <OPTION value="045">045-Billet à ordre  </OPTION>
                                    <OPTION value="046">046-Lettre de change</OPTION>
                                </SELECT>
			    </td>
                            <td></td>
                        </tr>
                        <tr>
                            <td>Date Echeance</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateecheance"/></td>
                            <td></td>
                            <td></td>
                            <td>Date Creation</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="datecreation"/></td>
                        </tr>
                            
                        <tr>
                            <td>Numero de Compte*: </td>
                            <td><input type="text" name="numeroCompte"  value="${requestScope['numeroCompte']}" maxlength="12" onKeypress="return numeriqueValide(event);"/></td>
                            <td><input type="text" name="clerib" size="2" value="${requestScope['clerib']}"  maxlength="2"/></td>
                            <td></td>

                            <td>Numero de Effet*:</td>
                            <td><input type="text" name="numeroEffet"  value="${requestScope['numeroEffet']}" maxlength="7" onKeypress="return numeriqueValide(event);"/></td>
                            <td></td>

                            <td><input type="submit" name="valider" value="Valider" onclick="checkEffetVierge();"/></td>

                            <a:widget name="yahoo.tooltip" args="{context:['chequeForm']}" value="Saisie des effets"  />
                            
                             


                        </tr>
                    </tbody>
                </table>
                <input type="hidden" value="checkEffetVierge" name="action" />
                <input type="hidden" value="${requestScope['index']}" name="index" />
                
              <!--<input type=hidden name="dateecheance" />-->
              <!--  <input type=hidden name="datecreation" />-->
				
	        <input type=hidden value="" name="dateEch" />  
                <input type=hidden value="" name="dateCrea" />
                
            </form>
            <b>Effet ${requestScope['index']} sur ${requestScope['nbOpers']}</b>
            <br>
            <%if (index > 1) {%>
            <input type="button" value="Remise" name="Remise" onclick="window.location.replace('saisieeffets.jsp?action=remise&ideffet=${requestScope['idObjet']}&idremise=${requestScope['idremise']}')"/>
            <input type="button" value="Precedent" name="Précédent" onclick="window.location.replace('saisieeffets.jsp?action=precedent&ideffet=${requestScope['idObjet']}&idremise=${requestScope['idremise']}')"/>
            <%}%>
            <%if (nbOpers > index) {%>
            <input type="button" value="Suivant" name="Suivant" onclick="window.location.replace('saisieeffets.jsp?action=suivant&ideffet=${requestScope['idObjet']}&idremise=${requestScope['idremise']}')"/>
            <%}%>
        </fieldset>
    </div>

    <%}
        }%>

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
