<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   <jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Creation manuelle d'un compte client</title>
        <style type="text/css">
            body {
                
                font-weight: normal;
                font-size: 12px;
                color: #333333;
            }
            
        </style>
        <script type="text/javascript"  src="clearing.js"></script>
    </head>
     <jsp:include page="checkaccess.jsp"/>
    <body>
    <jsp:useBean id="comboAgencesBean" class="clearing.web.bean.ComboAgenceBean" />
    
    <h2> Creation d'un Compte Client </h2>
    <div align="center">
        <form  id="creationCompteClient" name="creationCompteClient" onsubmit="return ( doAjaxSubmit(this,undefined,'ControlServlet'));">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Numéro de Compte:</td>
                        <td><input type=text name="numeroCompte" maxlength="12" onKeypress="return numeriqueValide(event);"/></td>
                    </tr>
                    <tr>
                        <td>Numéro de Compte Externe:</td>
                        <td><input type=text name="numCptEx" maxlength="19" onKeypress="return numeriqueValide(event);"/></td>
                    </tr>
                    <tr>
                        <td>Agence du Compte:</td>
                        <td><input type=text name="agenceCompte" maxlength="5" onKeypress="return numeriqueValide(event);"/></td>
                        
                    </tr>
                    <tr>
                        <td>Nom du Client:</td>
                        <td><input type=text name="nomClient" maxlength="25" /></td>
                    </tr>
                    <tr>
                        <td>Adresse du Client:</td>
                        <td><input type=text name="adresse" /></td>
                    </tr>
                    
                    <tr>
                        
                        <td> <input type=submit value="Creer "/></td>
                        <td><input type=reset value="Recommencer"/> </td>
                    </tr>
                </tbody>
            </table>
            
            <a:widget name="yahoo.tooltip" args="{context:['creationCompteClient']}" value="Formulaire de Cr&eacute;ation"  />

            
            <input type=hidden name="action" value="creationCompteClient"/>
            
        </form>
         </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>
   
    
    </body>
</html>
