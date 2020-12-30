<%@page  import="java.net.URLEncoder" contentType="text/html"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<jsp:include page="checkaccess.jsp"/>
<style type="text/css">

      body {
          font-style: italic;
          font-weight: bold;
      }

</style>
<script type="text/javascript"  src="mootools-packed.js"></script>
<script type="text/javascript"  src="clearing.js"></script>
<script type="text/javascript"  src="cookies.js"></script>
<link href="travail_prive2.css" rel="stylesheet" type="text/css" />
<jsp:include page="checkaccess.jsp"/>
<body onload="affiche_master();">
<%
        request.setAttribute("filtre", URLEncoder.encode(request.getParameter("requete"), "UTF-8"));
        request.setAttribute("filtre1", URLEncoder.encode(request.getParameter("requeteDetail"), "UTF-8"));

        %>
     <form name="detailForm" id="detailForm">
        <input type="hidden" name="requeteDetail" value='${requestScope['filtre1']}'/>
        <input type="hidden" name="cle" value="${param['cle']}"/>
    </form>
    <div id="imageDetailDIV" >
        <form name='imgForm'>
            <img name='imgchqr' id='imgchqr' src='' width="540" height="220" alt='|Aucune image recto associee|' />
            <img name='imgchqv' id='imgchqv' src='' width="540" height="220" alt='|Aucune image verso associee|' />

        </form>

    </div>
    <input name="printDetail" type="button"   onClick="printdiv('imageDetailDIV');" value=" Imprimer ">
    <div>
        <a href="#" id="prive_btn_masque_baniere" title="Masquer / Afficher l'image"></a>
    </div>
    <DIV  >

        <a:widget name="yahoo.dataTable" id="detail"  args="{scrollable:true}" service="data.jsp?only=cols&requete=${requestScope['filtre1']}&objet=${param['objet']}&nomidobjet=${param['nomidobjet']}&dropdown=${param['dropdown']}&radio=${param['radio']}"/>

    </DIV>
    <DIV  >
        
        <a:widget name="yahoo.dataTable" id="master" args="{scrollable:true}" service="data.jsp?requete=${requestScope['filtre']}&objet=${param['objet']}&nomidobjet=${param['nomidobjet']}&dropdown=${param['dropdown']}&radio=${param['radio']}"
                  />

    </DIV>
     <div id="imageMasterDIV" >
        <form name='imgRemForm'>
            <img name='imgremr' id='imgremr' src='' width="400" height="150" alt='|Aucune image recto associee|' />
            <img name='imgremv' id='imgremv' src='' width="400" height="150" alt='|Aucune image verso associee|' />

        </form>

    </div>
    <input name="printMaster" type="button"   onClick="printdiv('imageMasterDIV');" value=" Imprimer ">
    <div>
        <a href="#" id="prive_btn_masque_master" title="Masquer / Afficher l'image"></a>
    </div>
    <br>

   
    <script>

        function zoomImgr(e){

            document.getElementById('imgchqr').width = document.getElementById('imgchqr').width + 30;
            document.getElementById('imgchqr').height = document.getElementById('imgchqr').height + 15;
        }
        function zoomImgv(e){

            document.getElementById('imgchqv').width = document.getElementById('imgchqr').width + 30;
            document.getElementById('imgchqv').height = document.getElementById('imgchqr').height + 15;
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
        var slideBaniere = new Fx.Slide('imageDetailDIV');

        $('prive_btn_masque_baniere').addEvent('click', function(e){
            e = new Event(e);
            slideBaniere.toggle();
            e.stop();

            if (document.getElementById('prive_btn_masque_baniere').style.backgroundImage != 'url(images/btn_montre_entete.gif)') {
                document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_montre_entete.gif)';
                EffaceCookie('imageDetail');
                EcrireCookie('imageDetail', 'non');
            } else {
                document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_masque_entete.gif)';
                EffaceCookie('imageDetail');
                EcrireCookie('imageDetail', 'oui');
            }
        });

        function affiche_baniere(){

            if (LireCookie('imageDetail') == 'non') {
                document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                slideBaniere.hide();
            }

        }

        //-->

    </script>

    <script>

        function zoomImgr(e){

            document.getElementById('imgremr').width = document.getElementById('imgremr').width + 20;
            document.getElementById('imgremr').height = document.getElementById('imgremr').height + 10;
        }
        function zoomImgv(e){

            document.getElementById('imgremv').width = document.getElementById('imgremr').width + 20;
            document.getElementById('imgremv').height = document.getElementById('imgremr').height + 10;
        }

        function deZoomImgr(e){

            document.oncontextmenu = new Function ("return false");
            if ((!document.all && e.which == 3) || (document.all && e.button==2))
            {
                document.getElementById('imgremr').width = document.getElementById('imgremr').width - 20;
                document.getElementById('imgremr').height = document.getElementById('imgremr').height - 10;
            }
            //document.oncontextmenu = null;

        }
        function deZoomImgv(e){
            document.oncontextmenu = new Function ("return false");
            if ((!document.all && e.which == 3) || (document.all && e.button==2))
            {

                document.getElementById('imgremv').width = document.getElementById('imgremv').width - 20;
                document.getElementById('imgremv').height = document.getElementById('imgremv').height - 10;
            }

            // document.oncontextmenu = null;
        }
        function changeCursorToZoom(e){
            document.getElementById('imgremr').style.cursor="crosshair";
            document.getElementById('imgremv').style.cursor="crosshair";
        }
        function changeCursorToDefault(e){
            document.getElementById('imgremr').style.cursor="default";
            document.getElementById('imgremv').style.cursor="default";
        }
        document.getElementById('imgremr').onmousedown = deZoomImgr;
        document.getElementById('imgremr').onclick=zoomImgr;
        document.getElementById('imgremr').onmouseover=changeCursorToZoom;
        document.getElementById('imgremr').onmouseout=changeCursorToDefault;
        document.getElementById('imgremv').onmousedown = deZoomImgv;
        document.getElementById('imgremv').onclick=zoomImgv;
        document.getElementById('imgremv').onmouseover=changeCursorToZoom;
        document.getElementById('imgremv').onmouseout=changeCursorToDefault;
    </script>
    <script>
        <!--
        var slideMaster = new Fx.Slide('imageMasterDIV');

        $('prive_btn_masque_master').addEvent('click', function(e){
            e = new Event(e);
            slideMaster.toggle();
            e.stop();

            if (document.getElementById('prive_btn_masque_master').style.backgroundImage != 'url(images/btn_montre_entete.gif)') {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_montre_entete.gif)';
                EffaceCookie('imageMaster');
                EcrireCookie('imageMaster', 'non');
            } else {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_masque_entete.gif)';
                EffaceCookie('imageMaster');
                EcrireCookie('imageMaster', 'oui');
            }
        });

        function affiche_master(){

            if (LireCookie('imageMaster') == 'non') {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                slideMaster.hide();
            }

        }

        //-->

    </script>
    
</body> 
