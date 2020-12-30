<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page  import="clearing.web.bean.ComboIdBean" contentType="text/html"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Migration vers Clearing</title>
          <script type="text/javascript"  src="clearing.js"></script>
    </head>
    <body>
        
        <jsp:useBean id="comboIdBean" scope="session" class="clearing.web.bean.ComboIdBean" />  
<form  id="param" name="param">
        <table>
            
            <tbody>
                <tr>
                    <td>Migration: </td>
                    <td><a:widget name="dojo.combobox" id="id" value="${comboIdBean.comboDatas}" publish="/tf/getId"/></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td>Table Filtre: </td>
                    <td><a:widget name="dojo.combobox" id="tableFiltre" value="" subscribe="/tf" publish="/cl/getTableFiltre"/></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
                
            </tbody>
        </table>
          </form>
        <form  id="parammigration" name="parammigration" method="POST" action="ControlServlet" onsubmit=" return areFieldsNotEmpty(this);">
        
        <input type="submit" value="executer" name="executer" />
           <input type=hidden name="action" value="migration"/>
              
            </form>
    </body>
</html>
