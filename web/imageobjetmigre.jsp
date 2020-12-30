<%@page import="java.net.URLEncoder" contentType="text/html"%>
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
        
        <div id="imageDIV" >  
            <form name='imgForm'>
                <img name='imgchqr' id='imgchqr' src='' width="400" height="150" alt='|Aucune image recto associee|' />
                <img name='imgchqv' id='imgchqv' src='' width="400" height="150" alt='|Aucune image verso associee|' />
                
            </form>
            
        </div>
        <div>
             <a href="#" id="prive_btn_masque_baniere" title="Masquer / Afficher l'image"></a>
        </div> 
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
        <%
                }
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
                    <%
        }
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
            <a:widget name="yahoo.dataTable" id="tablePGTF" service="data.jsp?requete=${requestScope['filtre']}&objet=${param['objet']}&nomidobjet=${param['nomidobjet']}&dropdown=${param['dropdown']}&radio=${param['radio']}"
                      args="rowSingleSelect=true" publish="/jmaki/tablePGTF/onClick" />
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
    </body>
</html>
