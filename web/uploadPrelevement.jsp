<%-- 
    Document   : index
    Created on : 19 mai 2016, 10:51:54
    Author     : DavyStephane
--%>

<%@page contentType="text/html" pageEncoding="UTF8"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css">
        </style>
        <script type="text/javascript"  src="clearing.js"></script>
        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
        <script src="jquery/1.8/jquery.js" ></script>
        <script src="jquery/1.8/jquery.form.js"></script>
        <script src="js/fileUploadScript.js" ></script>
        <!-- Include css styles here -->
        
    </head>
    <jsp:include page="checkaccess.jsp"/>

    <body>
        <div id="result">
            <h3>${requestScope["message"]}</h3>

        </div>
        <div>

            <h3> Choisissez un fichier de prélèvement à uploader sur le Serveur </h3>

            <form  id="UploadForm" name ="UploadForm" action="FileUploadServlet" method="post" onsubmit="return checkPrelParam();" enctype="multipart/form-data">
                <table >

                    <tbody>
                        <tr>
                             <td> <input required="true"  id="myfile"  type="file" name="myfile" /> 
                           
                            <td> <input type="submit" value="Envoyer sur le serveur" /></td>
                        </tr>
                        <tr>
                            <td>Date de Fin de Recyclage:</td>
                            <td><a:widget name="dojo.dropdowndatepicker" id="datefin"/></td>
                            
                        </tr>
                        <tr>
                            <td>Param 2</td>
                            <td><input type="text" name="param2" /></td>
                        </tr>
                    </tbody>
                </table>
                <input type=hidden name="param1" value=""/>
                <br />
                <div id="progressbox">
                    <div id="progressbar"></div>
                    <div id="percent">0%</div>

                </div>
                 <br />
                <div id="message"></div>
            </form>

        </div>



    </body>

</html>
