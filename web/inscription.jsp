<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboAgenceBean" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   <jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Creation d'un Utilisateur</title>
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
        
    <h2> Creation d'un utilisateur </h2>
          <jsp:useBean id="comboAgencesBean" class="clearing.web.bean.ComboAgenceBean" />
          <jsp:useBean id="comboPoidsBean" class="clearing.web.bean.ComboPoidsBean" />
    <div align="center">
        <form  id="inscription" name="inscription" onsubmit="return (testPass(this) && addHiddenUser() && doAjaxSubmitEncoding(this,undefined,'ControlServlet'));">
            <table border="0" cellspacing="2" cellpadding="2">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Nom:</td>
                        <td><input type=text name="nom" /></td>
                    </tr>
                    <tr>
                        <td>Prenom:</td>
                        <td><input type=text name="prenom" /></td>
                    </tr>
                    <tr>
                        <td>Courriel:</td>
                        <td><input type=text name="courriel" /></td>
                    </tr>
                    <!--
                    <tr>
                        <td>Adresse:</td>
                        <td><input type=text name="adresse" /></td>
                    </tr>
                    -->
                    <tr>
                    <td>Agence </td>
                     <td><a:widget name="dojo.combobox" id="idagence"
                                              value="${comboAgencesBean.comboBanqueData}" />
                     </td>
                     </tr>
                    <tr>
                        <td>Login:</td>
                        <td><input type=text name="login" maxlength="20"/></td>
                    </tr>
                    <tr>
                        <td>Mot de Passe:</td>
                        <td><input type=password name="password1" /></td>
                    </tr>
                    <tr>
                        <td>Mot de Passe:</td>
                        <td><input type=password name="password2" /></td>
                    </tr>
                    <!--
                    <tr>
                        <td>Poids:</td>
                        <td><input type=text name="poids" onKeypress="return numeriqueValide(event);" /></td>
                    </tr>
                    -->
                    <tr>
                    <td>Profil</td>
                     <td><a:widget name="dojo.combobox" id="idpoids"
                                              value="${comboPoidsBean.comboDatas}" />
                     </td>
                     </tr>
                    <tr>
                        <td> <input type=submit value="Creer "/></td>
                        <td><input type=reset value="Recommencer"/> </td>
                    </tr>
                </tbody>
            </table>
            
            <a:widget name="yahoo.tooltip" args="{context:['inscription']}" value="Formulaire de Cr&eacute;ation"  />


            <input type=hidden name="action" value="inscription"/>
            <input type=hidden name="agence" value=""/>
            <input type=hidden name="poids" value=""/>
            
        </form>
         </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>
   
    
    </body>
</html>
