<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboBanqueBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboAgenceBean" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Creation d'une Machine</title>
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
       

        <h2> Creation d'une Machine de Scan </h2>
        <div align="center">
            <jsp:useBean id="comboCompteBean" scope="session" class="clearing.web.bean.ComboCompteBean" />
            <form  id="creationMachine" name="creationMachine" onsubmit="return ( addHiddenMachine() && doAjaxSubmit(this,undefined,'ControlServlet'));">
                <table border="0" cellspacing="2" cellpadding="2">
                    <thead>
                        <tr>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Machine Scan:</td>
                            <td><input type="text" name="idmachinescan" value="" maxlength="25"/></td>
                            <td></td>
                            <td>Machine :</td>
                            <td><input type=text name="machine" maxlength="25" /></td>
                        </tr>

                        <tr>
                            <td>Code Agence</td>
                            <td><a:widget name="dojo.combobox" id="idagence"
                                          value="${comboAgencesBean.comboDatas}" /></td>
                            <td></td>
                            <td>Contact:</td>
                            <td><input type=text name="contact" maxlength="25"/></td>
                        </tr>
                        

                        <tr>
                            <td>Adresse IP 1:</td>
                            <td><input type=text name="adresseip" maxlength="25"/></td>
                            <td></td>
                            <td>Adresse IP 2:</td>
                            <td><input type=text name="adresseip2" maxlength="25"/></td>
                        </tr>

                        <tr>
                            <td>Marque:</td>
                            <td><input type=text name="marque" maxlength="25" /></td>
                            <td></td>
                            <td>Modele:</td>
                            <td><input type=text name="modele" maxlength="25" /></td>


                        </tr>
                        <tr>
                            <td>Date Installation</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateinstall"/></td>
                            <td></td>

                        </tr>
                        <tr>
                            <td></td>
                            <td> <input type=submit value="Creer "/></td>
                            <td><input type=reset value="Recommencer"/> </td>
                            <td></td>
                        </tr>
                    </tbody>
                </table>

                <a:widget name="yahoo.tooltip" args="{context:['creationMachine']}" value="Formulaire de Cr&eacute;ation"  />

                <input type=hidden name="dateinstall" />
                <input type=hidden name="agence" />
                <input type=hidden name="action" value="creationMachine"/>

            </form>
        </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
        if (message != null) {
            out.print(message.getMessage());
        }%>


    </body>
</html>
