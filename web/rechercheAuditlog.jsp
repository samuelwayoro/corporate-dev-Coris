<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page  import="clearing.web.bean.ComboTableBean" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Recherche</title>
        <script type="text/javascript"  src="clearing.js"></script>
    </head>
    <body>

        <jsp:useBean id="comboTableBean" scope="session" class="clearing.web.bean.ComboTableBean" />
        <form  id="param" name="param">
            <table>

                <tbody>

                    <tr>
                        <td>Table : </td>
                        <td><a:widget name="dojo.combobox" id="tables"  value="[{label : 'AUDITLOG', value : 'AUDITLOG', selected : true} ]"  publish="/cl/getTables"/></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>

                    <tr>
                        <td>Colonne de condition</td>
                        <td><a:widget name="dojo.combobox" id="colWhere" value="" subscribe="/cl" /></td>
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
                        <td></td>
                    </tr>
                     <tr>
                        <td><input type="button" value="Ajouter" name="addSet" onclick="addWhereQuery()"/> </td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </tbody>
            </table>
            <input type=hidden name="rootset" value=""/>
            <input type=hidden name="otherset" value=""/>
            <input type=hidden name="where" value=""/>
            <input type=hidden name="from" value=""/>

        </form>
        <form  id="paramUpdate" name="paramUpdate"  onsubmit=" return (areFieldsNotEmpty(this) && showSearchResultAudit());">
            <textarea name="query" rows="10" cols="100" disabled>Criteres ... </textarea>
            <br/>
            <input type="submit" value="Rechercher" name="rechercher" />
            <input type="reset" value="Recommencer" name="recommencer" />
            <input type=hidden name="action" value="recherche"/>

        </form>

    </body>
</html>
