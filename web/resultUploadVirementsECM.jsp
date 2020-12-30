<%-- 
    Document   : result
    Created on : 19 mai 2016, 12:13:06
    Author     : DavyStephane
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
         
        <link href="css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
         <!-- Bootstrap CSS -->
  
    </head>
    <jsp:include page="checkaccess.jsp"/>
    <body>
        <% String resultat = (String) session.getAttribute("resultat");

            if (resultat == null) {

        %>
        <div id="result">
            <h3>${sessionScope["message"]}</h3>
            <br />

            <form action="VirementECMFileUpload" method="post" >
                Confirmez-vous l'integration du fichier de virements?
                <br />
                <button class="btn btn-primary" type="submit"  name="action" value="OUI" >OUI  </button>
                <button type="submit" class="btn btn-danger" name="action" value="NON" > NON</button>
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
