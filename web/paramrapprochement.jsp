<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page  import="clearing.web.bean.ComboIdBean" contentType="text/html"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Rapprochement</title>
          <script type="text/javascript"  src="clearing.js"></script>
    </head>
    <body>
        
        <jsp:useBean id="comboIdBean" scope="session" class="clearing.web.bean.ComboIdBean" />  
<form  id="param" name="param">
        <table>
            
            <tbody>
                <tr>
                    <td>Rapprochement: </td>
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
                <tr>
                    <td>Colonne</td>
                    <td><a:widget name="dojo.combobox" id="colonne" value="" subscribe="/cl" /></td>
                    <td>Operateur</td>
                    <td><a:widget id="operateur" name="dojo.combobox"
   value="[
       {label : 'Egal à', value : '='},
       {label : 'Comme', value : 'LIKE'},
       {label : 'Supérieur à', value : '>'},
       {label : 'Inférieur à', value : '<'},
       {label : 'Supérieur ou Egal à', value : '>='},
       {label : 'Inférieur ou Egal ', value : '<='},	           
       {label : 'Différent de', value : '<>'},
       {label : 'Pas Comme', value : 'NOT LIKE'},	           
       {label : 'Dans', value : 'IN'},
       {label : 'Pas Dans', value : 'NOT IN'}
    ]" />
</td>
                    <td>Valeur</td>
                    <td><input type="text" name="valeur" value="" /></td>
                    <td><a:widget name="dojo.combobox" id="conjonction" value="[
       {label : 'ET', value : 'AND'},
       {label : 'OU', value : 'OR'}
    ]" /></td>
                    <td><input type="button" value="Ajouter" name="ok" onclick="resolve()"/></td>
                </tr>
            </tbody>
        </table>
          </form>
        <form  id="paramrapprochement" name="paramrapprochement" method="POST" action="ControlServlet" onsubmit=" return areFieldsNotEmpty(this);">
        <textarea name="filtre" rows="10" cols="100">
        </textarea>
        <br/>
        <input type="submit" value="executer" name="executer" />
           <input type=hidden name="action" value="rapprochement"/>
              
            </form>
    </body>
</html>
