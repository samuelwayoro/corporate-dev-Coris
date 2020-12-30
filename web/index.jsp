
<%@page import="clearing.table.Utilisateurs" %>
<%@page import="org.patware.web.jmaki.MenuModel" buffer="none"%>
<%@page import="org.patware.web.bean.MessageBean" contentType="text/html"%>
<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %> 

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<jsp:include page="checkaccess.jsp"/>
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <link rel="stylesheet" href="jmaki-standard.css" type="text/css"></link>
        <link href="travail_prive2.css" rel="stylesheet" type="text/css" />
        <script type="text/javascript"  src="mootools-packed.js"></script>
        <script type="text/javascript"  src="cookies.js"></script>
        <script type="text/javascript"  src="clearing.js"></script>





        <title>WebClearing &reg;</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

        <style type="text/css">
            #buttonDIV {
                position:absolute;
                top:0;
                right:0;
            }



        </style>

    </head>

    <body onload="affiche_baniere();infoPanelLoadHandler();killPopup()">


        <script>
            history.forward();
            if (typeof (window.innerWidth) == 'number')
            {
                //----------------------------------
                // Non IE
                //----------------------------------
                my_height = window.innerHeight;
            } else if (document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight))
            {
                //----------------------------------
                // IE 6+
                //----------------------------------
                my_height = document.documentElement.clientHeight;
            } else if (document.body && (document.body.clientWidth || document.body.clientHeight))
            {
                //----------------------------------
                // Old IE
                //----------------------------------
                my_height = document.body.clientHeight;
            }

            hconteneur = my_height - 50;

            document.write("<div id='outerBorder' style='height:" + hconteneur + "px;'>");


        </script>


        <script language="javascript">

            var allowPrompt = true;
            //Cette fonction et appelé par chaque lien avec l'événement Onclick
            function NoPrompt()
            {
                allowPrompt = false;
            }

            var confirmOnLeave = function (msg) {

                window.onbeforeunload = function (e) {
                    e = e || window.event;
                    msg = msg || '';
                    if (allowPrompt) {
                        document['resetParamsForm'].action.value = 'closeWindow';
                        doAjaxSubmit(document['resetParamsForm'], new Array('action'), 'ControlServlet');
                    }
                    return msg;
                };

            };
            confirmOnLeave();


        </script>

        <div id="header">

            <div id="banner">

                <img src="images/bank_logo_9.png"  height="55" alt="WebClearing Application"/>

                <div id="prive_docs">
                    |
                    <a href="docs/WEBCLEARING_FRONT_OFFICE_Guide_utilisateur.pdf" onclick="NoPrompt()" target="dynamic">Documentation Front-Office</a>
                    |
                    <a href="docs/WEBCLEARING_BACK_OFFICE_Guide_utilisateur.pdf" onclick="NoPrompt()" target="dynamic">Documentation Back-Office</a>
                    |
                    <a href="docs/flux_WebClearing.pdf" target="dynamic">Flux des opérations dans WebClearing</a>
                    |<br>
                        <font color="red">
                            <% MessageBean message = (MessageBean) request.getAttribute("message");
                if (message != null && message.getMessage() != null) {
                    out.print(message.getMessage());
                }%>
                        </font>
                </div>

            </div>

        </div> <!-- header -->
        <div id="subheader">

            <div>

                <form name='resetParamsForm'>

                    <div id="infoPanelDIV" style="color: black; text-align:right">

                    </div> |
                    <a href="mailto:support.sbs@socitech.com ">Support SBS</a>

                    |
                    <a href="#" onclick="document['resetParamsForm'].action.value = 'resetParams';doAjaxSubmit(document['resetParamsForm'], new Array('action'), 'ControlServlet');">
                        Recharger les Param&egrave;tres
                    </a>
                    <input type="hidden" name="action" value="dynamique"/>
                    |
                    <a href="ControlServlet?action=logoutform" onclick="NoPrompt()">Deconnexion ${utilisateur.nom} </a>
                    |
                </form>
            </div>

        </div> <!-- sub-header -->

        <div>
            <a href="#" id="prive_btn_masque_baniere" title="Masquer / Afficher la baniere"></a>
        </div>
        <div id="main">


            <div id="leftSidebar">



                <a:widget name="yahoo.menu" value="${menuModel.jsonRepresentation}" />



                <!-- width="210" height="102" -->
                <img src="images/bank_logo_2.png" alt="logo"/>



            </div> <!-- leftSidebar -->

            <div id="content" >

                <a:widget name="yahoo.tabbedview"
                          value="{items:[
                          { label : 'Bienvenue ${utilisateur.prenom} ${utilisateur.nom}', include : 'bienvenue.jsp',  selected : true}
                          ]
                          }" id="tView"
                          />





            </div> <!-- content -->

        </div> <!-- main -->
        </div> <!-- outerborder -->
        <script>
            <!--
            var slideBaniere = new Fx.Slide('header');

            $('prive_btn_masque_baniere').addEvent('click', function (e) {
                e = new Event(e);
                slideBaniere.toggle();
                e.stop();

                if (document.getElementById('prive_btn_masque_baniere').style.backgroundImage != 'url(images/btn_montre_entete.gif)') {
                    document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_montre_entete.gif)';
                    EffaceCookie('baniere');
                    EcrireCookie('baniere', 'non');
                } else {
                    document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_masque_entete.gif)';
                    EffaceCookie('baniere');
                    EcrireCookie('baniere', 'oui');
                }
            });

            function affiche_baniere() {

                if (LireCookie('baniere') == 'non') {
                    document.getElementById('prive_btn_masque_baniere').style.backgroundImage = 'url(images/btn_montre_entete.gif)'
                    slideBaniere.hide();
                }

            }

            //-->

        </script>
    </body>


</html>
