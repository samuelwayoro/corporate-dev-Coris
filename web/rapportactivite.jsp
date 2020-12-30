<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page  import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboBanqueBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboAgenceBean" contentType="text/html"%>
<%@page  import="clearing.web.bean.ComboEtatBean" contentType="text/html"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Rapports d'activite</title>
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
        <h2>Rapports d'Activite</h2>

        <div align="center">
            <form  id="paramrapport" name="paramrapport" method="POST" action="ControlServlet" onsubmit=" return (resolveActivite());">
                
                <table cellspacing="5" cellpadding="5">
                    <thead>
                       
                    </thead>
                    <tbody>
                        
                        <tr>
                            <td>Date Compensation</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="dateCompensation"/></td>
                            <td></td>
                        </tr>
                     

                    </tbody>
                </table>
                <hr>
                <table>
                    </tbody>
                        
                      

                     
                        <tr>
                            
                            <td><input type="radio" name="typerapport" value="application/vnd.ms-excel" />EXCEL</td>
                            <td><input type="radio" name="typerapport" value="application/rtf" />WORD</td>
                             <td><input type="radio" name="typerapport" value="application/pdf"  checked="checked"/>PDF</td>
                            <td><input type="radio" name="typerapport" value="text/html" />Aperçu HTML</td>
                         
                            </tr>
                        <tr>
                        <tr>
                            <td></td>
                            <td><input type=submit value="Afficher"/></td>
                            <td></td> 
                            <td><input type=reset value="Recommencer"/> </td>
                        </tr>
                         
                    </tbody>
                </table>
                <input type=hidden name="action" value="rapportActivite"/>
                <input type=hidden name="nomrapport" value="rapportActivite"/>
                <input type=hidden name="dateChoisie" value=""/>
                
                <input type=hidden name="requete" value="dynamique" />
            </form>
        </div>
        <% MessageBean message = (MessageBean) request.getAttribute("message");
            if (message != null) {
                out.print(message.getMessage());
            }%>


    </body>
</html>

