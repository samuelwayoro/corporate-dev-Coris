<%@page import="java.net.URLEncoder" contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
         <link rel="stylesheet" type="text/css" href="libs/yui/fonts/fonts.css">
    <link type="text/css" rel="stylesheet" href="libs/yui/carousel/assets/skins/sam/carousel.css">

    <script type="text/javascript" src="libs/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="libs/yui/element/element-beta-min.js"></script>
    <script type="text/javascript" src="libs/yui/carousel/carousel-beta-min.js"></script>

<link href="travail_prive2.css" rel="stylesheet" type="text/css" />
        <style type="text/css">
            body {
                margin:0;
                padding:0;
            }

            #container {
                margin: 0 auto;
            }

            .yui-carousel-element li {
                height: 150px;
            }
        </style>

        
        <script type="text/javascript"  src="clearing.js"></script>
        
    </head>
    <jsp:include page="checkaccess.jsp"/>
    <body>

        <div id="imageDIV2" >
            <form name='imgForm'>
                <img name='imgchqr' id='imgchqr' src='' width="400" height="150" alt='|Aucune image recto associee|' />
                <img name='imgchqv' id='imgchqv' src='' width="400" height="150" alt='|Aucune image verso associee|' />


            </form>


        </div>
        
            <div id="container">
                <ol id="carousel">
                    <li>
                        <img src="http://farm1.static.flickr.com/69/213130158_0d1aa23576_d.jpg"
                             height="150" width="200">
                    </li>
                    <li>
                        <img src="http://farm1.static.flickr.com/72/213128367_74b0a657c3_d.jpg"
                             height="150" width="200">
                    </li>
                    <li>
                        <img src="http://farm1.static.flickr.com/98/213129707_1f40c509fa_d.jpg"
                             height="150" width="200">
                    </li>
                    <li>
                        <img src="http://farm1.static.flickr.com/59/213129191_b958880a96_d.jpg"
                             height="150" width="200">
                    </li>
                    <li>
                        <img src="http://farm1.static.flickr.com/92/214077367_77ae970965_d.jpg"
                             height="150" width="200">
                    </li>
                    <li>
                        <img src="http://farm1.static.flickr.com/81/214076446_18fe6a6c91_d.jpg"
                             height="150" width="200">
                    </li>
                    <li>
                        <img src="http://farm1.static.flickr.com/93/214075781_0604edb894_d.jpg"
                             height="150" width="200">
                    </li>
                    <li>
                        <img src="http://farm1.static.flickr.com/40/214075243_ea66c4cb31_d.jpg"
                             height="150" width="200">
                    </li>
                    <li>
                        <img src="http://farm1.static.flickr.com/67/214074120_33933bf232_d.jpg"
                             height="150" width="200">
                    </li>
                    <li>
                        <img src="http://farm1.static.flickr.com/79/214073568_f16d1ffce7_d.jpg"
                             height="150" width="200">
                    </li>
                </ol>
            </div>
        

        <script>
            (function () {
                var carousel;

                YAHOO.util.Event.onDOMReady(function (ev) {
                    var carousel    = new YAHOO.widget.Carousel("container", {
                        isCircular: true, numVisible: 1
                    });

                    carousel.render(); // get ready for rendering the widget
                    carousel.show();   // display the widget
                });
            })();
        </script>
        <%
        request.setAttribute("filtre", URLEncoder.encode(request.getParameter("requete"), "UTF-8"));

        String supp = request.getParameter("suppression");
        String annul = request.getParameter("annulation");
        String modif = request.getParameter("modification");
        String creation = request.getParameter("creation");
        String zonerejet = request.getParameter("zonerejet");
        if (zonerejet != null) {
            if (zonerejet.equalsIgnoreCase("oui")) {
        %>
        <jsp:useBean id="comboMotifRejetBean" class="clearing.web.bean.ComboMotifsRejetBean" />
        <form name='rejetForm'>
            <div id="rejetDIV">

                <a:widget name="dojo.combobox" id="comboMotif" value="${comboMotifRejetBean.comboDatas}" />
                <input type='hidden' name='motifrejet' value='dynamique' />
                <input type='hidden' name='idobjet' value='dynamique' />
                <input type='hidden' name='objet' value='${param['objet']}' />
                <input type='hidden' name='nomidobjet' value='${param['nomidobjet']}' />
                <input type='hidden' name='numcolobjet' value='${param['numcolobjet']}' />
                <input type='button' name='rejet' value='Rejeter' onclick='rejete();'/>
                <input type='hidden' name='action' value='rejetForm' />
            </div>
        </form>
        <%            }
        }
        %>

        <form name='outilsForm'>
            <div id="outilsDIV">
                <input type='hidden' name='idobjet' value='dynamique' />
                <input type='hidden' name='objet' value='${param['objet']}' />
                <input type='hidden' name='nomidobjet' value='${param['nomidobjet']}' />
                <input type='hidden' name='numcolobjet' value='${param['numcolobjet']}' />
                <%
        if (annul != null) {
            if (annul.equalsIgnoreCase("oui")) {
                %>
                <input type='button' name='annuler' value='Annuler' onclick='annule();'/>
                <%}
        }
        if (modif != null) {
            if (modif.equalsIgnoreCase("oui")) {
                %>
                <input type='button' name='modifier' value='Modifier' onclick='modifier();'/>
                <%}
        }
        if (creation != null) {

                %>
                <input type='button' name='creer' value='Créer' onclick='ajouter("${param['creation']}");'/>
                <%        }
        if (supp != null) {
            if (supp.equalsIgnoreCase("oui")) {
                %>
                <input type='button' name='supprimer' value='Supprimer' onclick='supprime();'/>
                <%}
        }%>

                <input type='hidden' name='action' value='outilsForm' />
            </div>
        </form>

        <div id="tableObjetDIV">
            <a:widget name="yahoo.dataTable" id="tableCheques" service="data.jsp?requete=${requestScope['filtre']}&objet=${param['objet']}&nomidobjet=${param['nomidobjet']}&dropdown=${param['dropdown']}&radio=${param['radio']}"
                      args="rowSingleSelect=true" publish="/jmaki/tableCheques/onClick" />
        </div>
        <script>

            function zoomImgr(e){

                document.getElementById('imgchqr').width = document.getElementById('imgchqr').width + 10;
                document.getElementById('imgchqr').height = document.getElementById('imgchqr').height + 5;
            }
            function zoomImgv(e){

                document.getElementById('imgchqv').width = document.getElementById('imgchqr').width + 10;
                document.getElementById('imgchqv').height = document.getElementById('imgchqr').height + 5;
            }

            function deZoomImgr(e){

                document.oncontextmenu = new Function ("return false");
                if ((!document.all && e.which == 3) || (document.all && e.button==2))
                {
                    document.getElementById('imgchqr').width = document.getElementById('imgchqr').width - 10;
                    document.getElementById('imgchqr').height = document.getElementById('imgchqr').height - 5;
                }
                //document.oncontextmenu = null;

            }
            function deZoomImgv(e){
                document.oncontextmenu = new Function ("return false");
                if ((!document.all && e.which == 3) || (document.all && e.button==2))
                {

                    document.getElementById('imgchqv').width = document.getElementById('imgchqv').width - 10;
                    document.getElementById('imgchqv').height = document.getElementById('imgchqv').height - 5;
                }

                // document.oncontextmenu = null;
            }
            function changeCursorToZoom(e){
                document.getElementById('imgchqr').style.cursor="crosshair";
                document.getElementById('imgchqv').style.cursor="crosshair";
            }
            function changeCursorToDefault(e){
                document.getElementById('imgchqr').style.cursor="default";
                document.getElementById('imgchqv').style.cursor="default";
            }
            document.getElementById('imgchqr').onmousedown = deZoomImgr;
            document.getElementById('imgchqr').onclick=zoomImgr;
            document.getElementById('imgchqr').onmouseover=changeCursorToZoom;
            document.getElementById('imgchqr').onmouseout=changeCursorToDefault;
            document.getElementById('imgchqv').onmousedown = deZoomImgv;
            document.getElementById('imgchqv').onclick=zoomImgv;
            document.getElementById('imgchqv').onmouseover=changeCursorToZoom;
            document.getElementById('imgchqv').onmouseout=changeCursorToDefault;
        </script>
        
    </body>
</html>
