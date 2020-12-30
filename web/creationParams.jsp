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
        <title>Creation d'un Paramètre</title>
        <style type="text/css">
            body {

                font-weight: normal;
                font-size: 12px;
                color: #333333;
            }

        </style>
        <script type="text/javascript"  src="clearing.js"></script>
        <script src="jquery/1.8/jquery.min.js" ></script>

        <script type="text/javascript">
            $(function() { 
                $("#cryptParam").click(function() {
                    if ($(this).is(":checked")) {
                        $("#typeParam").val("CODE_CRYPTED");
                        $("#typeParam").attr("disabled", "disabled");
                    } else {
                        $("#typeParam").removeAttr("disabled");
                        $("#typeParam").focus();
                         $("#typeParam").val("");
                    }
                });
            });
        </script>


    </head>
    <jsp:include page="checkaccess.jsp"/>
    <body>


        <h2> Creation d'un Paramètre </h2>
        <div align="center">
            <form  id="creationParams" name="creationParams" onsubmit="return (doAjaxSubmitEncoding(this, undefined, 'ControlServlet'));">
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
                            <td>Valeur:</td>
                            <td><input type=text name="valeur"  /></td>

                        </tr>
                        <tr>
                            <td>Type:</td>
                            <td><input id="typeParam" type=text name="type" /></td>
                        </tr>
                        <tr>
                            <td>Libelle:</td>
                            <td><input type=text name="libelle" size="40"  /></td>

                        </tr>


                        <tr>
                            <td>Crypter le Param&egrave;tre</td>
                            <td><input type=checkbox name="cryptParam"  id="cryptParam" /></td>

                        </tr>

                        <tr>

                            <td> <input type=submit value="Creer "/></td>
                            <td><input type=reset onclick='$("#typeParam").removeAttr("disabled");' value="Recommencer Encore"/> </td>
                        </tr>
                    </tbody>
                </table>

                <a:widget name="yahoo.tooltip" args="{context:['creationParams']}" value="Formulaire de Cr&eacute;ation"  />

                <input type=hidden name="action" value="creationParams"/>

            </form>
        </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>


    </body>
</html>
