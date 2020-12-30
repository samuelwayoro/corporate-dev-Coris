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

        <jsp:useBean id="comboCompteBean" scope="session" class="clearing.web.bean.ComboCompteNSIABean" />

        <h2> Creation d'un Compte Client </h2> 
        <div align="center">
            <form  id="creationCompteClientNSIA" name="creationCompteClientNSIA" onsubmit="return (resolveEscompte() && doAjaxSubmit(this, undefined, 'ControlServlet'));"
                   > 
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
                            <td><input type=text name="numeroCompte" maxlength="11"  onKeypress="return numeriqueValide(event);" 
                                       onblur=" chargeCompteFromSibICCPT();" /></td>
                        </tr>

                        <tr>
                            <td>Agence du Compte:</td>
                            <td><input disabled  type=text name="agenceCompte" maxlength="5" /></td>

                        </tr>
                        <tr>
                            <td>Nom du Client:</td>
                            <td><input disabled type=text name="nomClient" maxlength="35" /></td>
                        </tr>
                        <tr>
                            <td>Adresse du Client:</td>
                            <td><input type=text name="adresse" /></td>
                        </tr>
                       
                        <tr>
                            <td>Date de Debut d'escompte:</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateDebutEscompte"/></td>

                        </tr>
                        <tr>
                            <td>Date de fin d'escompte:</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateFinEscompte"/></td>

                        </tr>
                        <tr>

                            <td> <input type=submit value="Creer "/></td>
                            <td><input type=reset value="Recommencer"/> </td>
                        </tr>
                    </tbody>
                </table>

                <a:widget name="yahoo.tooltip" args="{context:['creationCompteClientNSIA']}" value="Formulaire de Cr&eacute;ation de Comptes"  />

                <input type=hidden name="dateDebutEscompte" />
                  <input type=hidden name="dateFinEscompte" />
   
                <input type=hidden name="action" value="creationCompteClientNSIA"/>

            </form>
        </div>

        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>


    </body>
</html>
