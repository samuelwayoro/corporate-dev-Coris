<%@page import="java.net.URLEncoder,java.net.URLDecoder" contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<jsp:include page="checkaccess.jsp"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <style type="text/css">
        </style>
        <script type="text/javascript"  src="mootools-packed.js"></script>
        <script type="text/javascript"  src="clearing.js"></script>
        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
    </head>
    <jsp:include page="checkaccess.jsp"/>
    <body>

<%


        String filterData = (String) session.getAttribute("filterData");
        if (filterData == null) {
            filterData = "";
        }
        String paramDate1 =  request.getParameter("paramDate1");

        String action = request.getParameter("choixFiltre");



        if (action != null && action.equalsIgnoreCase("true")) {
            if (paramDate1 != null && !paramDate1.isEmpty() && !paramDate1.equalsIgnoreCase("null")) {
                request.setAttribute("filterData", URLEncoder.encode(request.getParameter("requete"), "UTF-8"));

        %>
        <div align="center">
            <form name='choixFiltre' action="smartImageobjet.jsp" method="POST" onsubmit=" return filterDataTable();" accept-charset="utf8">
                <tr>
                    <td><%=request.getParameter("paramDate1")%>: </td>
                    <td><a:widget name="dojo.dropdowndatepicker" id="dateparam1"/></td>
                </tr>
                <input type="submit" name="filter" value="Afficher" />
                <input type='hidden' name='param1' value='${param['paramDate1']}' />
                <input type='hidden' name='requete' value=${requestScope['filterData']}/>
                <input type='hidden' name='objet' value='${param['objet']}' />
                <input type='hidden' name='nomidobjet' value='${param['nomidobjet']}' />
                <input type='hidden' name='dropdown' value='${param['dropdown']}' />
                <input type='hidden' name='radio' value='${param['radio']}' />
                <input type='hidden' name='csv' value='${param['csv']}' />
                <input type='hidden' name='suppression' value='${param['suppression']}' />
                <input type='hidden' name='filtreChoisi' value='dynamique' />
            </form>
        </div>
          <%
                    }
                } else {
        %>

        <div id="imageDIV" >
            <form name='imgForm'>
                <img name='imgchqr' id='imgchqr' src='' width="540" height="220" alt='|Aucune image recto associee|' />
                <img name='imgchqv' id='imgchqv' src='' width="540" height="220" alt='|Aucune image verso associee|' />

            </form>

        </div>
        <input name="printDetail" type="button"   onClick="printdiv('imageDIV');" value=" Imprimer ">
        <div>
            <a href="#" id="prive_btn_masque_baniere" title="Masquer / Afficher l'image"></a>
        </div>
        <%
          String prequery = "SELECT * FROM (";
                    String postquery = ") WHERE " + request.getParameter("filtreChoisi");
                    String query = URLDecoder.decode(request.getParameter("requete"),"UTF-8");
                    if(query.endsWith("/")) query = query.substring(0, query.length()-1);
                   // out.print(prequery + URLDecoder.decode(request.getParameter("requete"), "UTF-8") + postquery);
                    request.setAttribute("filtre", URLEncoder.encode(prequery + query + postquery, "UTF-8"));

        String supp = request.getParameter("suppression");
        String annul = request.getParameter("annulation");
        String modif = request.getParameter("modification");
        String creation = request.getParameter("creation");
        String zonerejet = request.getParameter("zonerejet");
        String csv = request.getParameter("csv");
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
                <input type='hidden' name='etatdebut' value='${param['etatdebut']}' />
                <input type='hidden' name='etatfin' value='${param['etatfin']}' />
                <input type='button' name='rejet' value='Rejeter' onclick='rejete();'/>
                <input type='hidden' name='action' value='rejetForm' />
            </div>
        </form>
        <%            }       if (zonerejet.equalsIgnoreCase("specifique")) {
                comboMotifRejetBean.setRequete(request.getParameter("requetecoderejet"));
        %>

        <form name='rejetForm'>
            <div id="rejetDIV">

                <a:widget name="dojo.combobox" id="comboMotif" value="${comboMotifRejetBean.comboDatas}" />
                <input type='hidden' name='motifrejet' value='dynamique' />
                <input type='hidden' name='idobjet' value='dynamique' />
                <input type='hidden' name='objet' value='${param['objet']}' />
                <input type='hidden' name='nomidobjet' value='${param['nomidobjet']}' />
                <input type='hidden' name='numcolobjet' value='${param['numcolobjet']}' />
                <input type='hidden' name='etatdebut' value='${param['etatdebut']}' />
                <input type='hidden' name='etatfin' value='${param['etatfin']}' />
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
        if (csv != null) {

                %>
                <input type='button' name='csv' value='Export CSV' onclick='exportToCSV();'/>
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

                document.getElementById('imgchqr').width = document.getElementById('imgchqr').width + 20;
                document.getElementById('imgchqr').height = document.getElementById('imgchqr').height + 10;
            }
            function zoomImgv(e){

                document.getElementById('imgchqv').width = document.getElementById('imgchqr').width + 20;
                document.getElementById('imgchqv').height = document.getElementById('imgchqr').height + 10;
            }

            function deZoomImgr(e){

                document.oncontextmenu = new Function ("return false");
                if ((!document.all && e.which == 3) || (document.all && e.button==2))
                {
                    document.getElementById('imgchqr').width = document.getElementById('imgchqr').width - 20;
                    document.getElementById('imgchqr').height = document.getElementById('imgchqr').height - 10;
                }
                //document.oncontextmenu = null;

            }
            function deZoomImgv(e){
                document.oncontextmenu = new Function ("return false");
                if ((!document.all && e.which == 3) || (document.all && e.button==2))
                {

                    document.getElementById('imgchqv').width = document.getElementById('imgchqv').width - 20;
                    document.getElementById('imgchqv').height = document.getElementById('imgchqv').height - 10;
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
        <script>
            <!--
            var slideBaniere = new Fx.Slide('imageDIV');

            $('prive_btn_masque_baniere').addEvent('click', function(e){
                e = new Event(e);
                slideBaniere.toggle();
                e.stop();

                if (document.getElementById('prive_btn_masque_baniere').style.backgroundImage != 'url(images/btn_montre_entete.gif)') {
                    document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_montre_entete.gif)';
                    EffaceCookie('image');
                    EcrireCookie('image', 'non');
                } else {
                    document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_masque_entete.gif)';
                    EffaceCookie('image');
                    EcrireCookie('image', 'oui');
                }
            });

            function affiche_baniere(){

                if (LireCookie('image') == 'non') {
                    document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                    slideBaniere.hide();
                }

            }

            //-->

        </script>

        <%
        }
        %>
    </body>
</html>
