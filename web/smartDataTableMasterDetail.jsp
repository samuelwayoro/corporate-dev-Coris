<%@page  import="java.net.URLEncoder,java.net.URLDecoder" contentType="text/html"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<jsp:include page="checkaccess.jsp"/>
<style type="text/css">
  
    body {
        font-style: italic;
        font-weight: bold;
    }
    
    </style>
    <script type="text/javascript"  src="clearing.js"></script>
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
                request.setAttribute("filterDataDetail", URLEncoder.encode(request.getParameter("requeteDetail"), "UTF-8"));

        %>
        <div align="center">
            <form name='choixFiltre' action="smartDataTableMasterDetail.jsp" method="POST" onsubmit=" return filterDataTable();" accept-charset="utf8">
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
                <input type='hidden' name='cle' value='${param['cle']}' />
                <input type='hidden' name='csv' value='${param['csv']}' />
                <input type='hidden' name='requeteDetail' value='${requestScope['filterDataDetail']}' />
                <input type='hidden' name='filtreChoisi' value='dynamique' />
            </form>
        </div>
        <%
                    }
                } else {
        %>
<%


                    String prequery = "SELECT * FROM (";
                    String postquery = ") WHERE " + request.getParameter("filtreChoisi");
                    String query = URLDecoder.decode(request.getParameter("requete"),"UTF-8");
                    String queryDetail = URLDecoder.decode(request.getParameter("requeteDetail"),"UTF-8");
                    if(query.endsWith("/")) query = query.substring(0, query.length()-1);
                   // out.print(prequery + URLDecoder.decode(request.getParameter("requete"), "UTF-8") + postquery);
                    request.setAttribute("filtre", URLEncoder.encode(prequery + query + postquery, "UTF-8"));

                    String supp = request.getParameter("suppression");
                    String annul = request.getParameter("annulation");
                    String modif = request.getParameter("modification");
                    String creation = request.getParameter("creation");
                    String csv = request.getParameter("csv");
            %>

    <DIV id="imageDIV" >
          <%
         //request.setAttribute("filtre", URLEncoder.encode(request.getParameter("requete"), "UTF-8"));
         request.setAttribute("filtre1", URLEncoder.encode(queryDetail, "UTF-8"));
%>  
        <a:widget name="yahoo.dataTable" id="master" service="data.jsp?requete=${requestScope['filtre']}&objet=${param['objet']}&nomidobjet=${param['nomidobjet']}&dropdown=${param['dropdown']}&radio=${param['radio']}"
                  />
                  
    </DIV> 
    <br>
        
        <form name="detailForm" id="detailForm">
           <input type="hidden" name="requeteDetail" value='${requestScope['filtre1']}'/> 
           <input type="hidden" name="cle" value="${param['cle']}"/>
        </form>
    <DIV id="imageDIV" >
       
        <a:widget name="yahoo.dataTable" id="detail"  service="data.jsp?only=cols&requete=${requestScope['filtre1']}"/>
                  
    </DIV> 
  
 <%
        }
        %>
</body> 
