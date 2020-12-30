<%@page  import="java.net.URLEncoder,java.net.URLDecoder" contentType="text/html"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
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

        <%


        String filterData = (String) session.getAttribute("filterData");
        if (filterData == null) {
            filterData = "";
        }
        String paramDate1 =  request.getParameter("paramDate1");

        String action = request.getParameter("choixFiltre");

        
        
        if (action != null && action.equalsIgnoreCase("true")) {
            if (paramDate1 != null && !paramDate1.isEmpty() && !paramDate1.equalsIgnoreCase("null")) {
                request.setAttribute("filterData", URLEncoder.encode(request.getParameter("requete"), "UTF-8"));

        %>
        <div align="center">
            <form name='choixFiltre' action="smartDataTable.jsp" method="POST" onsubmit=" return filterDataTable();" accept-charset="utf8">
                <tr>
                    <td><%=request.getParameter("paramDate1")%>: </td>
                    <td><a:widget name="dojo.dropdowndatepicker" id="dateparam1"/></td>
                </tr>
                <input type="submit" name="filter" value="Afficher" />
                <input type='hidden' name='param1' value='${param['paramDate1']}' />
                <input type='hidden' name='requete' value=${requestScope['filterData']}/>
                <input type='hidden' name='objet' value='${param['objet']}' />
                <input type='hidden' name='nomidobjet' value='${param['nomidobjet']}' />
                <input type='hidden' name='dropdown' value='${param['dropdown']}' />
                <input type='hidden' name='radio' value='${param['radio']}' />
                <input type='hidden' name='csv' value='${param['csv']}' />
                <input type='hidden' name='xls' value='${param['xls']}' />
                <input type='hidden' name='suppression' value='${param['suppression']}' />
                <input type='hidden' name='filtreChoisi' value='dynamique' />
            </form>
        </div>
        <%
                    }
                } else {
        %>

        
        <DIV id="tableDIV" >
            <%


                    String prequery = "SELECT * FROM (";
                    String postquery = ") WHERE " + request.getParameter("filtreChoisi");
                    String query = URLDecoder.decode(request.getParameter("requete"),"UTF-8");
                    if(query.endsWith("/")) query = query.substring(0, query.length()-1);
                   // out.print(prequery + URLDecoder.decode(request.getParameter("requete"), "UTF-8") + postquery);
                    request.setAttribute("filtre", URLEncoder.encode(prequery + query + postquery, "UTF-8"));

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
                    <%            }
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
                    <input type='button' name='supprimer' value='Supprimer' onclick="if (confirm('Etes vous sur?')) {supprime();}"/>
                    <%}
                    }%>

                    <input type='hidden' name='action' value='outilsForm' />
                </div>
            </form>

            <a:widget name="yahoo.dataTable" id="tableCheques" service="data.jsp?requete=${requestScope['filtre']}&objet=${param['objet']}&nomidobjet=${param['nomidobjet']}&dropdown=${param['dropdown']}&radio=${param['radio']}"
                      args="rowSingleSelect=true" publish="/jmaki/tableCheques/onClick"  />





        </DIV>


        <%
        }
        %>
    </body>
</html>
