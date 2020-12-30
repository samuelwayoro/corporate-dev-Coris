<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<jsp:include page="checkaccess.jsp"/>
<style type="text/css">
  
    body {
        font-style: italic;
        font-weight: bold;
    }
    
    </style>
<body >   
    <DIV id="imageDIV" >
          <%
         request.setAttribute("filtre", URLEncoder.encode(request.getParameter("requete"), "UTF-8"));
         request.setAttribute("filtre1", URLEncoder.encode(request.getParameter("requeteDetail"), "UTF-8"));
         request.setAttribute("objetDetail", URLEncoder.encode(request.getParameter("objetDetail"), "UTF-8"));
         request.setAttribute("nomidobjetDetail", URLEncoder.encode(request.getParameter("nomidobjetDetail"), "UTF-8"));
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
       
        <a:widget name="yahoo.dataTable" id="detail"  service="data.jsp?only=cols&requete=${requestScope['filtre1']}&objetDetail=${requestScope['objetDetail']}&nomidobjetDetail=${requestScope['nomidobjetDetail']}"/>
                  
    </DIV> 
  

</body> 
