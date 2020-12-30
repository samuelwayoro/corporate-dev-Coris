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

        <!--
                <link href="http://hayageek.github.io/jQuery-Upload-File/4.0.10/uploadfile.css" rel="stylesheet">
                <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
                <script src="http://hayageek.github.io/jQuery-Upload-File/4.0.10/jquery.uploadfile.min.js"></script>
        -->
        <script src="jquery/1.8/jquery.min.js" type="text/javascript"></script>
        <link href="js/uploadfile.css" rel="stylesheet" type="text/css"/>
        <script src="js/jquery.uploadfile.min.js" type="text/javascript"></script>
    </head>
    <jsp:include page="checkaccess.jsp"/>

    <body>
        <div id="result">
            <h3>${requestScope["message"]}</h3>

        </div>
        <h3> Choisissez un fichier de virements à uploader ECM sur le Serveur </h3>
        <div id="fileuploaderECM">Qpload</div>





        <script>
            $(document).ready(function ()
            {
                $("#fileuploaderECM").uploadFile({
                    url: "VirementECMFileUpload",
                    fileName: "myfile",
                    dragDrop: true,
                    multiple: false,
                    showProgress: true,
                    maxFileCount: 1,
                    uploadStr: "Chargez un Fichier ",

                    allowedTypes: "xls,xlsx",
                    acceptFiles: "application/vnd.ms-excel",
                    returnType: "text",
                    onLoad: function (obj)
                    {
                        $("#eventsmessage").html($("#eventsmessage").html() + "<br/>Widget Loaded:");
                    },
                    onSubmit: function (files)
                    {
                        $("#eventsmessage").html($("#eventsmessage").html() + "<br/>Submitting:" + JSON.stringify(files));
                        //return false;
                    },
                    onSuccess: function (files, data, xhr, pd)
                    {
                        $("#eventsmessage").html($("#eventsmessage").html() + "<br/>Success for: " + JSON.stringify(data));
                    },
                    afterUploadAll: function (obj)
                    {
                        window.setTimeout(function () {
                            window.location = "resultUploadVirementsECM.jsp";
                        }, 2000);
                    },
                    onError: function (files, status, errMsg, pd)
                    {
                        $("#eventsmessage").html($("#eventsmessage").html() + "<br/>Error for: " + JSON.stringify(files));
                    },
                    onCancel: function (files, pd)
                    {
                        $("#eventsmessage").html($("#eventsmessage").html() + "<br/>Canceled  files: " + JSON.stringify(files));
                    }
                });
            });
        </script>

    </body>

</html>
