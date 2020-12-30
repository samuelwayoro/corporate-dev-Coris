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
        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
           <jsp:include page="checkaccess.jsp"/>
<body >
 <div id="imageMasterDIV" >
            <form name='imgRemForm'>
                <img name='imgremr' id='imgremr' src='' width="400" height="150" alt='|Aucune image recto associee|' />
                <img name='imgremv' id='imgremv' src='' width="400" height="150" alt='|Aucune image verso associee|' />

            </form>

        </div>
        <div>
             <a href="#" id="prive_btn_masque_master" title="Masquer / Afficher l'image"></a>
        </div>
    <DIV  >
          <%
         request.setAttribute("filtre", URLEncoder.encode(request.getParameter("requete"), "UTF-8"));
         request.setAttribute("filtre1", URLEncoder.encode(request.getParameter("requeteDetail"), "UTF-8"));
         request.setAttribute("objetDetail", URLEncoder.encode(request.getParameter("objetDetail"), "UTF-8"));
         request.setAttribute("nomidobjetDetail", URLEncoder.encode(request.getParameter("nomidobjetDetail"), "UTF-8"));
%>  
        <a:widget name="yahoo.dataTable" id="master"  service="data.jsp?requete=${requestScope['filtre']}&objet=${param['objet']}&nomidobjet=${param['nomidobjet']}&dropdown=${param['dropdown']}&radio=${param['radio']}"
                  />
                  
    </DIV> 
    <br>
        
        <form name="detailForm" id="detailForm">
           <input type="hidden" name="requeteDetail" value='${requestScope['filtre1']}'/> 
           <input type="hidden" name="cle" value="${param['cle']}"/>
        </form>
         <div id="imageDetailDIV" >
            <form name='imgForm'>
                <img name='imgchqr' id='imgchqr' src='' width="400" height="150" alt='|Aucune image recto associee|' />
                <img name='imgchqv' id='imgchqv' src='' width="400" height="150" alt='|Aucune image verso associee|' />

            </form>

        </div>
        <div>
             <a href="#" id="prive_btn_masque_baniere" title="Masquer / Afficher l'image"></a>
        </div>
    <DIV  >
       
        <a:widget name="yahoo.dataTable" id="detail"  service="data.jsp?only=cols&requete=${requestScope['filtre1']}&objetDetail=${requestScope['objetDetail']}&nomidobjetDetail=${requestScope['nomidobjetDetail']}"/>
                  
    </DIV> 
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
            var slideBaniere = new Fx.Slide('imageDetailDIV');

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

         <script>

            function zoomImgr(e){

                document.getElementById('imgremr').width = document.getElementById('imgremr').width + 10;
                document.getElementById('imgremr').height = document.getElementById('imgremr').height + 5;
            }
            function zoomImgv(e){

                document.getElementById('imgremv').width = document.getElementById('imgremr').width + 10;
                document.getElementById('imgremv').height = document.getElementById('imgremr').height + 5;
            }

            function deZoomImgr(e){

                document.oncontextmenu = new Function ("return false");
                if ((!document.all && e.which == 3) || (document.all && e.button==2))
                    {
                        document.getElementById('imgremr').width = document.getElementById('imgremr').width - 10;
                        document.getElementById('imgremr').height = document.getElementById('imgremr').height - 5;
                    }
                    //document.oncontextmenu = null;

                }
                function deZoomImgv(e){
                    document.oncontextmenu = new Function ("return false");
                    if ((!document.all && e.which == 3) || (document.all && e.button==2))
                        {

                            document.getElementById('imgremv').width = document.getElementById('imgremv').width - 10;
                            document.getElementById('imgremv').height = document.getElementById('imgremv').height - 5;
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
                    EffaceCookie('image');
                    EcrireCookie('image', 'non');
                } else {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_masque_entete.gif)';
                EffaceCookie('image');
                EcrireCookie('image', 'oui');
            }
        });

        function affiche_master(){

            if (LireCookie('image') == 'non') {
                document.getElementById('prive_btn_masque_master').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                slideMaster.hide();
            }

        }

        //-->

        </script>


</body> 
