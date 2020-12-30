<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page pageEncoding="UTF-8" import="org.patware.jdbc.DataBase,org.patware.xml.JDBCXmlReader,clearing.model.CMPUtility"%>
<jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css">


            body {

                font-style: italic;
                font-weight: bold;
            }

        </style>
        <script type="text/javascript"  src="clearing.js"></script>
        <script src="jquery/1.8/jquery.min.js" type="text/javascript"></script>
        <script src="js/checkICOMParams.js" type="text/javascript"></script>
    </head>


    <body>
        <div align="center">
            <%
                String action = request.getParameter("action");
                if (action != null && action.equals("reset")) {
            %>

            Action annulée par l'utilisateur. Veuillez fermer l'onglet.
            <%
            } else {
            %>

            <form id="confirmationFrm" name='outilsForm' method="POST" action="ControlServlet" onsubmit="return   checkForm();"  
                  onreset="window.location.replace('confirmation.jsp?action=reset')">
                <div id="outilsDIV" align="center">
                    Vous essayez d'executer une tache sensible. En êtes vous sur?
                    <table>
                        <%
                            String paramDate1 = request.getParameter("paramDate1");
                            if (paramDate1 != null && !paramDate1.isEmpty() && !paramDate1.equalsIgnoreCase("null")) {
                                String paramDate1Value = request.getParameter("paramDate1Value");

                                if (paramDate1Value != null && !paramDate1Value.isEmpty() && !paramDate1Value.equalsIgnoreCase("null")) {
                                    request.setAttribute("paramDate1Value", CMPUtility.getParamDateDefaultValue(paramDate1Value));
                                }
                        %>
                        <tr>
                            <td><%=request.getParameter("paramDate1")%>: </td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateparam1" value="${requestScope['paramDate1Value']}"/></td>
                        </tr>
                        <input type='hidden' name='param1' value='dynamique' />
                        <%
                            }
                        %>
                        <%
                            String paramDate2 = request.getParameter("paramDate2");
                            if (paramDate2 != null && !paramDate2.isEmpty() && !paramDate2.equalsIgnoreCase("null")) {

                        %>
                        <tr>
                            <td><%=request.getParameter("paramDate2")%>: </td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateparam2"/></td>
                        </tr>
                        <input type='hidden' name='param2' value='dynamique' />
                        <%
                            }
                        %>
                        <%
                            String paramDate3 = request.getParameter("paramDate3");
                            if (paramDate3 != null && !paramDate3.isEmpty() && !paramDate3.equalsIgnoreCase("null")) {
                        %>
                        <tr>
                            <td><%=request.getParameter("paramDate3")%>: </td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateparam3"/></td>
                        </tr>
                        <input type='hidden' name='param3' value='dynamique' />
                        <%
                            }
                        %>
                        <%
                            String paramQuery = request.getParameter("paramQuery");
                            if (paramQuery != null && !paramQuery.isEmpty() && !paramQuery.equalsIgnoreCase("null")) {
                                DataBase db = new DataBase(JDBCXmlReader.getDriver());
                                db.open(JDBCXmlReader.getUrl(), JDBCXmlReader.getUser(), JDBCXmlReader.getPassword());
                        %>
                        <tr>
                            <td><%=request.getParameter("paramQueryLabel")%>: </td>
                            <td><% out.print(db.getResultOfSQLFunction(request.getParameter("paramQuery")));%></td>
                        </tr>

                        <%
                                db.close();
                            }

                        %>
                        <%    String paramText1 = request.getParameter("paramText1");
                            if (paramText1 != null && !paramText1.isEmpty() && !paramText1.equalsIgnoreCase("null")) {

                        %>
                        <tr>
                            <td><%=request.getParameter("paramText1")%> </td>
                            <td><input type="text" name="textParam1" value="${param['paramText1Value']}" /></td>
                        </tr>


                        <input type='hidden' name='sftpIdRepertoire' value='<%=request.getParameter("sftpIdRepertoire")%>' />
                        <%
                            }

                        %>
                        <%    String paramText2 = request.getParameter("paramText2");
                            if (paramText2 != null && !paramText2.isEmpty() && !paramText2.equalsIgnoreCase("null")) {

                        %>
                        <tr>
                            <td><%=request.getParameter("paramText2")%> </td>
                            <td><input type="text" name="textParam2" value="${param['paramText2Value']}" /></td>
                        </tr>
                        <%
                            }

                        %>
                        <tr>
                            <td><input id="submitBtn" type="submit" value="Oui" name="oui" /></td>
                            <td><input type="reset" value="Non" name="non"/></td>
                        </tr>
                        <tr></tr>
                        <tr><b><h1>${param['description']}</h1></b> </tr>
                    </table>
                    <span id="msgIcom"></span>
                    <input type='hidden' id="action" name='action' value='${param['tache']}' />
                    <input type='hidden' name='description' value='${param['description']}' />
                    <input type='hidden' name='requete' value='${param['requete']}' />
                    <input type='hidden' name='objets' value='${param['objets']}' />
                    <input type='hidden' name='validate' value='validate' />

                </div>
            </form>
        </div>
        <%            }
            //request.setAttribute("filtre", URLEncoder.encode(request.getParameter("requete"), "UTF-8"));
            // out.print(request.getAttribute("filtre"));
        %>


        <a:widget name="yahoo.tooltip" args="{context:['outilsForm']}" value="Confirmation"  />
    </body>
</html>
