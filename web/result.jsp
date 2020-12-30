<%-- 
    Document   : result
    Created on : 19 mai 2016, 12:13:06
    Author     : DavyStephane
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
    </head>
    <jsp:include page="checkaccess.jsp"/>
    <body>
        <% String resultat = (String) session.getAttribute("resultat");

            if (resultat == null) {

        %>
        <div id="result">
            <h3>${sessionScope["message"]}</h3>
            <br />

            <form action="FileUploadServlet" method="post" >
                Confirmez-vous l'integration du fichier de pr&eacute;l&egrave;vements?
                <br />
                <button  type="submit"  name="action" value="OUI" >OUI  </button>
                <button type="submit" name="action" value="NON" > NON</button>
            </form>

        </div>
        <%  } else {%>
        <div>
            <h3>${sessionScope["resultat"]}</h3>
            <br/>
        </div>
        <%  }%>
    </body>
</html>
