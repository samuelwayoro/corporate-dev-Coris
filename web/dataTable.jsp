<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css">

            #tableDIV{

                overflow: none;
                width: 100%;
                height:100%;
            }
            body {

                font-style: italic;
                font-weight: bold;
            }

        </style>
        <script type="text/javascript"  src="clearing.js"></script>
    </head>

    <body >
        <DIV id="tableDIV" >
            <%

                request.setAttribute("filtre", URLEncoder.encode(request.getParameter("requete"), "UTF-8"));

                String supp = request.getParameter("suppression");
                String annul = request.getParameter("annulation");
                String modif = request.getParameter("modification");
                String creation = request.getParameter("creation");
                String csv = request.getParameter("csv");
                String xls = request.getParameter("xls");
            %>

            <form name='outilsForm'>
                <div id="outilsDIV">
                    <input type='hidden' name='idobjet' value='dynamique' />
                    <input type='hidden' name='objet' value='${param['objet']}' />
                    <input type='hidden' name='nomidobjet' value='${param['nomidobjet']}' />
                    <input type='hidden' name='numcolobjet' value='${param['numcolobjet']}' />
                    <%
                        if (annul != null) {
                            if (annul.equalsIgnoreCase("oui")) {
                    %>
                    <input type='button' name='annuler' value='Annuler' onclick='annule();'/>
                    <%}
                        }
                        if (modif != null) {
                            if (modif.equalsIgnoreCase("oui")) {
                    %>
                    <input type='button' name='modifier' value='Modifier' onclick='modifier();'/>
                    <%}
                        }
                        if (creation != null) {

                    %>
                    <input type='button' name='creer' value='Créer' onclick='ajouter("${param['creation']}");'/>
                    <%                        }
                        if (csv != null) {

                    %>
                    <input type='button' name='csv' value='Export CSV' onclick='exportToCSV();'/>
                    <%        }
                        if (xls != null) {

                    %>
                    <input type='button' name='xls' value='Export XLS' onclick='exportToEXCEL();'/>
                    <%        }

                        if (supp != null) {
                            if (supp.equalsIgnoreCase("oui")) {
                             
                    %>
                    <input type='button' name='supprimer' value='Supprimer' onclick="if (confirm('Etes vous sur?')) {
                                supprime();
                            }"/>
                    <%}
                        }%>

                    <input type='hidden' name='action' value='outilsForm' />
                </div>
            </form>

            <a:widget name="yahoo.dataTable" id="tableCheques" service="data.jsp?requete=${requestScope['filtre']}&objet=${param['objet']}&nomidobjet=${param['nomidobjet']}&dropdown=${param['dropdown']}&radio=${param['radio']}"
                      args="rowSingleSelect=true" publish="/jmaki/tableCheques/onClick"  />
           





        </DIV> 



    </body>
</html>
