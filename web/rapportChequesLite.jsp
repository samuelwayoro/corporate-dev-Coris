<%@page import="clearing.table.Utilisateurs"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboBanqueBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboAgenceBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboEtatBean" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Rapports d'activite</title>
        <style type="text/css">
            body {

                font-weight: normal;
                font-size: 12px;
                color: #333333;
            }

        </style>
        <script type="text/javascript"  src="clearing.js"></script>
        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
    </head>
    <jsp:include page="checkaccess.jsp"/>
    <body>
        <h2>Rapports d'Activite</h2>
        <%
            Utilisateurs user = (Utilisateurs) session.getAttribute("utilisateur");
            String query = "select C1.IDCHEQUE,C1.NOMEMETTEUR,C1.REMISE,C1.REFREMISE,C1.DEVISE,C1.NUMEROCHEQUE,C1.NUMEROCOMPTE,C1.NOMBENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.COMPTEREMETTANT,"
                    + "C1.AGENCEREMETTANT,C1.DATESAISIE,C1.DATETRAITEMENT,C1.DATECOMPENSATION,"
                    + "C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantcheque ,C1.MOTIFREJET "
                    + "from cheques C1,cheques C2, Banques B1,Banques B2,Remises R "
                    + "where C1.IDCHEQUE=C2.IDCHEQUE  AND C1.banqueremettant=B1.codebanque  and C2.banque=B2.codebanque "
                    // + "and C1.etat in (17,30)  "
                    + "and C1.DATESAISIE ='dateSaisie' and C1.remise=R.idremise "
                    + "and R.agenceDepot like '" + user.getAdresse().trim() + "' ";
            String query2 = "SELECT B.LIBELLEBANQUE AS LIBELLEBANQUE, C.NOMEMETTEUR AS TIREUR,C.NUMEROCHEQUE AS NUMEROCHEQUE,C.DATESAISIE, C.DATEEMISSION AS DATEEMISSION,"
                    + " C.MONTANTCHEQUE AS MONTANT, R.COMPTEREMETTANT AS COMPTEREMETTANT,R.IDREMISE,C.ETABLISSEMENT AS ETABLISSEMENT"
                    + " FROM CHEQUES C"
                    + " JOIN BANQUES B ON B.CODEBANQUE = C.BANQUE"
                    + " JOIN REMISES R ON R.IDREMISE = C.REMISE"
                    + " WHERE C.DATESAISIE= 'dateSaisie' AND R.agenceDepot like '" + user.getAdresse().trim() + "' "
                    + " ORDER BY IDREMISE";
        %>
        <div align="center">
            <form name="printReport"  action="ControlServlet" method="POST" onsubmit="return resolveQuotidienChequesLite();">

                <table cellspacing="5" cellpadding="5">
                    <thead>

                    </thead>
                    <tbody>

                        <tr>
                            <td>Date Saisie : </td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateSaisie"/></td>
                            <td></td>
                        </tr>


                    </tbody>
                </table>
                <hr>
                <table>

                    <tr>
                        <td><input type="radio" id="orderbybanque" name="nomrapport" value="Cheques" />Regroupé par Banque</td>
                        <td><input type="radio" id="orderbyremise" name="nomrapport" value="Cheques_clients_remises"  />Regroupé par Remise</td>
                        <td><input type="radio" id="orderbyremisetire" name="nomrapport" value="Cheques_clients_remises_tire"  />Regroupé par Remise avec nom du Tiré</td>
                        <td><input type="radio" id="orderbyremisebreak" name="nomrapport" value="Cheques_clients_remises_break" />Regroupé Par Remise avec saut de page</td>
                        <td><input type="radio" id="orderbyremisesigne" name="nomrapport" checked="checked" value="Cheques_clients_remises_signe" />Regroupé Par Remise pour signature</td>
                        <td><input type="radio" id="orderbybanquetire" name="nomrapport" value="Cheques_clients_tire" />Regroupé Par Banque avec tire</td>
                    
                    </tr>
                    <tr>
                        <td><input type="radio" name="typerapport" value="application/vnd.ms-excel" />EXCEL</td>
                        <td><input type="radio" name="typerapport" value="application/rtf" />WORD</td>
                        <td><input type="radio" name="typerapport" value="application/pdf"  checked="checked"/>PDF</td>
                        <td><input type="radio" name="typerapport" value="text/html" />Aperçu HTML</td>
                        <td><input type="radio" name="typerapport" value="text/csv" />CSV</td>
                    </tr>
                    <tr>
                        <td><input type="submit" value="Imprimer" name="oui" /></td>
                        <td><input type="checkbox" name="avecDetail" value="ON" checked="checked"/>Avec Détails</td>

                    </tr>
                </table>
                <input type='hidden' name='action' value='rapportChequesLite' />
                <input type=hidden name="dateChoisie" value=""/>


                <input type='hidden' name='requete' value="dynamique" />
                <input type='hidden' name='query' value="<%=query%>"/>
                <input type='hidden' name='query2' value="<%=query2%>"/>
            </form>
        </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>


    </body>
</html>

