/*
 * These are some predefined glue listeners that you can
 *  modify to fit your application.
 *
 * This file should not placed in the /resources directory of your application
 * as that directory is for jmaki specific resources.
 */

// uncomment to turn on the logger
jmaki.debug = false;
// uncomment to show publish/subscribe messages
jmaki.debugGlue = false;

// map topic dojo/fisheye to fisheye handler
jmaki.subscribe("/dojo/fisheye*", function (args) {
    jmaki.log("glue.js : fisheye event");
});


// map topics   ending with  /onSave to the handler
jmaki.subscribe("*onSave", function (args) {
    jmaki.log("glue.js : onSave request from: " + args.id + " value=" + args.value);
});

// map topics ending with  /onSave to the handler
jmaki.subscribe("*onSelect", function (args) {
    jmaki.log("glue.js : onSelect request from: " + args.widgetId);
});

// map topics ending with  /onSave to the handler
jmaki.subscribe("*onClick", function (args) {
    jmaki.log("glue.js : onClick request from: " + args.widgetId);
});




var isIE = /MSIE/i.test(navigator.userAgent);
var tabMappings = {};
var button;
var refresh;
var tabbedView;
var removeTab = function () {
    var tView = jmaki.getWidget('tView');
    var tab = tView._tabView.get('activeTab');

    if (tab.index != 0) {
        tView._tabView.removeTab(tab);
        tabMappings[tab.tid] = undefined;
    }
}
var refreshTab = function () {
    var tView = jmaki.getWidget('tView');
    var tab = tView._tabView.get('activeTab');

    if (tab.index != 0) {

        tab.dcontainer.loadURL(tab.url);
        tab.contentLoaded = true;


    }
}

function refreshTabWithUrl(url) {

    var tab = tabbedView._tabView.get('activeTab');

    if (tab.index != 0) {

        tab.dcontainer.loadURL(url);
        tab.contentLoaded = true;


    }
}
jmaki.subscribe("/yahoo/tabbedview/*", function (args) {
    if (args.targetId == 'tView_tab_0') {
        document.getElementById('buttonDIV').style.display = "none";
    } else
        document.getElementById('buttonDIV').style.display = "block";
});

jmaki.subscribe("/jmaki/glue", function (args) {
    //  jmaki.log("Menu args.targetId="+args.targetId+" args.message.id="+args.message.id+" args.message.href="+args.message.url);
    //jmaki.publish("/jmaki/tView/setInclude", {value: args.message.url});
    var tView = jmaki.getWidget('tView');
    tabbedView = tView;
    if (tabMappings[args.message.id] == undefined) {
        if (typeof button == 'undefined') {
            div = document.createElement('div');
            attr_id = document.createAttribute("id");
            attr_id.nodeValue = "buttonDIV";
            div.setAttributeNode(attr_id);
            button = document.createElement('button');
            refresh = document.createElement('button');
            YAHOO.util.Event.on(button, 'click', removeTab);
            YAHOO.util.Event.on(refresh, 'click', refreshTab);
            button.innerHTML = 'Fermer';
            refresh.innerHTML = 'Rafraichir';
            div.appendChild(refresh);
            div.appendChild(button);
            //tView._tabView.appendChild(button); 
            tView._tabView.appendChild(div);
        }

        // calculate height here
        var h = document.getElementById('tView').parentNode.clientHeight - 35; // TODO : Get the true label height
        var w = document.getElementById('tView').parentNode.clientWidth - 2;
        if (h <= 50)
            h = 300;
        if (isIE)
            h = h - 3;
        var currentTab = new YAHOO.widget.Tab({
            label: args.message.id,
            content: 'Chargement en cours ...',
            active: true
        });

        var _url = undefined;

        _url = decodeURIComponent(args.message.url);

        var query = encodeURI(_url);
        _url = query;
        currentTab.url = _url;
        currentTab.contentLoaded = false;

        currentTab.tid = args.message.id;
        currentTab.label = currentTab.label;
        tView._tabView.addTab(currentTab);


        var of = 'auto';

        var iframe = true;

        var cv = currentTab.get('contentEl');

        cv.id = args.message.id;
        var iargs = {
            target: cv,
            useIframe: iframe,
            overflow: of,
            overflowX: 'auto',
            overflowY: 'auto',
            content: 'Chargement en cours...',
            startHeight: h,
            startWidth: w,
            autosize: true
        };
        currentTab.dcontainer = new jmaki.DContainer(iargs);
        currentTab.index = tView.getTabCount() - 1;
        //alert("url="+_url +" query="+query);
        currentTab.dcontainer.loadURL(_url);
        currentTab.contentLoaded = true;
        tabMappings[args.message.id] = currentTab;
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=" + args.message.id + "&message=open",
            callback: function (_req) {

                // handle any errors
            }
        });

    } else {
        tView._tabView.set("activeTab", tabMappings[args.message.id]);

    }




});


jmaki.subscribe("/yahoo/dataTable/on*", function (args) {
    if (args.widgetId == 'master') {
        var masterWidget = jmaki.getWidget('master');
        var sids = masterWidget.dataTable.getSelectedRows();
        var idMaster = masterWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('1');
        //jmaki.log("targetId : onSelect request from: " + args.targetId);
        //jmaki.publish('/yahoo/dataTable/detail', company);
        getDetail(idMaster);
        //  jmaki.log("targetId : onSelect request from: " + args.targetId);
    }
});


function getDetail(idMaster) {

    var url = jmaki.webRoot + "/data.jsp?only=rows&requete=" + document['detailForm'].requeteDetail.value + " where " + document['detailForm'].cle.value + "=" + idMaster;

    jmaki.doAjax({
        url: url,
        callback: function (req) {
            var _req = req;
            postProcessDetail(_req);
        }
    });

    function postProcessDetail(req) {
        jmaki.log("process? : " + req.status);
        if (req.readyState == 4) {
            if (req.status == 200) {
                var response = eval("(" + req.responseText + ")");

                //jmaki.publish("/yahoo/dataTable/detail", req.responseText);
                jmaki.publish("/yahoo/dataTable/detail/addRows", {
                    value: response
                });
            }
        }
    }
}
jmaki.subscribe("/yahoo/dataTable/detail*", function (args) {
    jmaki.getWidget('detail').clear();
    jmaki.getWidget('detail').addRows(args);
});


String.prototype.trim = function () {
    return this.replace(/^\s+/, "").replace(/\s+$/, "");
}

jmaki.subscribe("/yahoo/dataTable/on*", function (args) {

    if (args.widgetId == 'master') {

        var tableChequesWidget = jmaki.getWidget('master');
        var sids = tableChequesWidget.dataTable.getSelectedRows();
        var pathimage = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('10');
        var fichierimage = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('11');

        if (pathimage != undefined) {

            pathimage = pathimage.substr(3).trim();
            webpath = pathimage.concat(fichierimage.trim());
            webpath = webpath.replace(/\\/g, "/");


            document.forms['imgRemForm'].imgremr.src = webpath.concat("f.jpg");
            document.forms['imgRemForm'].imgremv.src = webpath.concat("r.jpg");
            if (fichierimage.trim() != '') {
                document.forms['imgRemForm'].imgremr.alt = webpath.concat("f.jpg");
                document.forms['imgRemForm'].imgremv.alt = webpath.concat("r.jpg");
                document.forms['imgRemForm'].imgremr.title = webpath.concat("f.jpg");
                document.forms['imgRemForm'].imgremv.title = webpath.concat("r.jpg");
            } else {
                document.forms['imgRemForm'].imgremr.alt = "|Aucune image recto associee|";
                document.forms['imgRemForm'].imgremv.alt = "|Aucune image verso associee|";
                document.forms['imgRemForm'].imgremr.title = "|Aucune image recto associee|";
                document.forms['imgRemForm'].imgremv.title = "|Aucune image verso associee|";
            }

        } else {
            document.forms['imgRemForm'].imgremr.src = "";
            document.forms['imgRemForm'].imgremv.src = "";
            document.forms['imgRemForm'].imgremr.alt = "|Aucune image recto associee|";
            document.forms['imgRemForm'].imgremv.alt = "|Aucune image verso associee|";
            document.forms['imgRemForm'].imgremr.title = "|Aucune image recto associee|";
            document.forms['imgRemForm'].imgremv.title = "|Aucune image verso associee|";
        }
    }
});


jmaki.subscribe("/yahoo/dataTable/on*", function (args) {

    if (args.widgetId == 'detail' && document['validationVirementFichier'] != undefined) {
        console.log("validationVirementFichier");
        var tableChequesWidget = jmaki.getWidget('detail');
        var sids = tableChequesWidget.dataTable.getSelectedRows();
        var banque = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('1');
        var agence = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('2');
        var numerocompte_beneficiaire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('3');
        var nom_beneficiaire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('4');
        var montantvirement = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('5');
        var libelle = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('6');
        var adresse_beneficiaire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('7');
        var compteTire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('9');
        var numero_virement = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('10');

        //jmaki.publish('/cl/setValues', obj);
//        jmaki.getWidget('idbancre').wrapper.setValue(banque);
//        jmaki.getWidget('idagecre').wrapper.setValue(agence);
//        jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value=banque;
//        jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value='A'+agence;
        document.forms['validationVirementFichier'].libelleVir.value = libelle;

        document.forms['validationVirementFichier'].addressBen.value = adresse_beneficiaire;
        document.forms['validationVirementFichier'].nomBen.value = nom_beneficiaire;
        document.forms['validationVirementFichier'].montant.value = montantvirement;
        document.forms['validationVirementFichier'].numeroBen.value = numerocompte_beneficiaire;
        document.forms['validationVirementFichier'].numeroTire.value = compteTire;
        document.forms['validationVirementFichier'].numero_virement.value = numero_virement;


    }

    if (args.widgetId == 'detail' && document['creationVirementClient'] != undefined) {

        var tableChequesWidget = jmaki.getWidget('detail');
        var sids = tableChequesWidget.dataTable.getSelectedRows();
        var banque = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('1');
        var agence = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('2');
        var numerocompte_beneficiaire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('3');
        var nom_beneficiaire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('4');
        var montantvirement = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('5');
        var libelle = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('6');
        var adresse_beneficiaire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('7');

        //jmaki.publish('/cl/setValues', obj);
        jmaki.getWidget('idbancre').wrapper.setValue(banque);
        jmaki.getWidget('idagecre').wrapper.setValue(agence);
        jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value = banque;
        jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value = 'A' + agence;
        document.forms['creationVirementClient'].libelleVir.value = libelle;
        document.forms['creationVirementClient'].numeroVir.value = "";
        document.forms['creationVirementClient'].addressBen.value = adresse_beneficiaire;
        document.forms['creationVirementClient'].nomBen.value = nom_beneficiaire;
        document.forms['creationVirementClient'].montant.value = montantvirement;
        document.forms['creationVirementClient'].numeroBen.value = numerocompte_beneficiaire;


    }
    if (args.widgetId == 'detail' && document['creationVirementClientBNP'] != undefined) {

        var tableChequesWidget = jmaki.getWidget('detail');
        var sids = tableChequesWidget.dataTable.getSelectedRows();
        var banque = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('1');
        var agence = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('2');
        var numerocompte_beneficiaire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('3');
        var nom_beneficiaire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('4');
        var montantvirement = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('5');
        var libelle = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('6');
        var adresse_beneficiaire = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('7');

        //jmaki.publish('/cl/setValues', obj);
        jmaki.getWidget('idbancre').wrapper.setValue(banque);
        jmaki.getWidget('idagecre').wrapper.setValue(agence);
        jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value = banque;
        jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value = 'A' + agence;
        document.forms['creationVirementClientBNP'].libelleVir.value = libelle;
        document.forms['creationVirementClientBNP'].numeroVir.value = "";
        document.forms['creationVirementClientBNP'].addressBen.value = adresse_beneficiaire;
        document.forms['creationVirementClientBNP'].nomBen.value = nom_beneficiaire;
        document.forms['creationVirementClientBNP'].montant.value = montantvirement;
        document.forms['creationVirementClientBNP'].numeroBen.value = numerocompte_beneficiaire;


    }

    if (args.widgetId == 'detail' && document['creationVirementClient'] == undefined) {

        tableChequesWidget = jmaki.getWidget('detail');
        sids = tableChequesWidget.dataTable.getSelectedRows();
        var pathimage = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('10');
        var fichierimage = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('11');

        if (pathimage != undefined) {

            pathimage = pathimage.substr(3).trim();
            webpath = pathimage.concat(fichierimage.trim());
            webpath = webpath.replace(/\\/g, "/");

            document.forms['imgForm'].imgchqr.src = webpath.concat("f.jpg");
            document.forms['imgForm'].imgchqv.src = webpath.concat("r.jpg");
            if (fichierimage.trim() != '') {
                document.forms['imgForm'].imgchqr.alt = webpath.concat("f.jpg");
                document.forms['imgForm'].imgchqv.alt = webpath.concat("r.jpg");
                document.forms['imgForm'].imgchqr.title = webpath.concat("f.jpg");
                document.forms['imgForm'].imgchqv.title = webpath.concat("r.jpg");
            } else {
                document.forms['imgForm'].imgchqr.alt = "|Aucune image recto associee|";
                document.forms['imgForm'].imgchqv.alt = "|Aucune image verso associee|";
                document.forms['imgForm'].imgchqr.title = "|Aucune image recto associee|";
                document.forms['imgForm'].imgchqv.title = "|Aucune image verso associee|";
            }

        } else {
            document.forms['imgForm'].imgchqr.src = "";
            document.forms['imgForm'].imgchqv.src = "";
            document.forms['imgForm'].imgchqr.alt = "|Aucune image recto associee|";
            document.forms['imgForm'].imgchqv.alt = "|Aucune image verso associee|";
            document.forms['imgForm'].imgchqr.title = "|Aucune image recto associee|";
            document.forms['imgForm'].imgchqv.title = "|Aucune image verso associee|";
        }
    }
});



jmaki.subscribe("/jmaki/tableCheques/on*", function (args) {

    if (args.widgetId == 'tableCheques') {

        var tableChequesWidget = jmaki.getWidget('tableCheques');
        var sids = tableChequesWidget.dataTable.getSelectedRows();
        var pathimage = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('10');
        var fichierimage = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('11');

        if (pathimage != undefined) {

            pathimage = pathimage.substr(3).trim();
            webpath = pathimage.concat(fichierimage.trim());
            webpath = webpath.replace(/\\/g, "/");

            document.forms['imgForm'].imgchqr.src = webpath.concat("f.jpg");
            document.forms['imgForm'].imgchqv.src = webpath.concat("r.jpg");
            jmaki.log(document.forms['imgForm'].imgchqr.src);
            if (fichierimage.trim() != '') {
                document.forms['imgForm'].imgchqr.alt = webpath.concat("f.jpg");
                document.forms['imgForm'].imgchqv.alt = webpath.concat("r.jpg");
                document.forms['imgForm'].imgchqr.title = webpath.concat("f.jpg");
                document.forms['imgForm'].imgchqv.title = webpath.concat("r.jpg");
            } else {
                document.forms['imgForm'].imgchqr.alt = "|Aucune image recto associee|";
                document.forms['imgForm'].imgchqv.alt = "|Aucune image verso associee|";
                document.forms['imgForm'].imgchqr.title = "|Aucune image recto associee|";
                document.forms['imgForm'].imgchqv.title = "|Aucune image verso associee|";
            }

        } else {
            document.forms['imgForm'].imgchqr.src = "";
            document.forms['imgForm'].imgchqv.src = "";
            document.forms['imgForm'].imgchqr.alt = "|Aucune image recto associee|";
            document.forms['imgForm'].imgchqv.alt = "|Aucune image verso associee|";
            document.forms['imgForm'].imgchqr.title = "|Aucune image recto associee|";
            document.forms['imgForm'].imgchqv.title = "|Aucune image verso associee|";
        }
    }
});

jmaki.subscribe("/jmaki/tablePGTF/on*", function (args) {

    if (args.widgetId == 'tablePGTF') {

        var tablePGTFWidget = jmaki.getWidget('tablePGTF');
        var sids = tablePGTFWidget.dataTable.getSelectedRows();
        var pathimage = tablePGTFWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('10');
        var recto = tablePGTFWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('11');
        var verso = tablePGTFWidget.dataTable.getRecordSet().getRecord(sids[0]).getData('12');

        if (pathimage != undefined) {

            pathimage = pathimage.substr(3).trim();
            webpathrecto = pathimage.concat("\\").concat(recto.trim());
            webpathverso = pathimage.concat("\\").concat(verso.trim());
            webpathrecto = webpathrecto.replace(/\\/g, "/");
            webpathverso = webpathverso.replace(/\\/g, "/");

            document.forms['imgForm'].imgchqr.src = webpathrecto;
            document.forms['imgForm'].imgchqv.src = webpathverso;
            jmaki.log(document.forms['imgForm'].imgchqr.src);
            if (recto.trim() != '') {
                document.forms['imgForm'].imgchqr.alt = webpathrecto;
                document.forms['imgForm'].imgchqv.alt = webpathverso;
                document.forms['imgForm'].imgchqr.title = webpathrecto;
                document.forms['imgForm'].imgchqv.title = webpathverso;
            } else {
                document.forms['imgForm'].imgchqr.alt = "|Aucune image recto associee|";
                document.forms['imgForm'].imgchqv.alt = "|Aucune image verso associee|";
                document.forms['imgForm'].imgchqr.title = "|Aucune image recto associee|";
                document.forms['imgForm'].imgchqv.title = "|Aucune image verso associee|";
            }

        } else {
            document.forms['imgForm'].imgchqr.src = "";
            document.forms['imgForm'].imgchqv.src = "";
            document.forms['imgForm'].imgchqr.alt = "|Aucune image recto associee|";
            document.forms['imgForm'].imgchqv.alt = "|Aucune image verso associee|";
            document.forms['imgForm'].imgchqr.title = "|Aucune image recto associee|";
            document.forms['imgForm'].imgchqv.title = "|Aucune image verso associee|";
        }
    }
});

jmaki.subscribe("/tf/getId/*", function (args) {
    var message = args.value;
    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=idRapprochement&message=" + encodeURIComponent(message),
        callback: function (_req) {
            var tmp = _req.responseText;
            var obj = eval("(" + tmp + ")");
            jmaki.publish('/tf/setValues', obj);
            // handle any errors
        }
    });
});

jmaki.subscribe("/cl/getTableFiltre/*", function (args) {
    var message = args.value;
    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=tableFiltre&message=" + encodeURIComponent(message),
        callback: function (_req) {
            var tmp = _req.responseText;
            var obj = eval("(" + tmp + ")");
            jmaki.publish('/cl/setValues', obj);
            // handle any errors
        }
    });
});

jmaki.subscribe("/cl/getTables/*", function (args) {
    var message = args.value;
    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=tableSelect&message=" + encodeURIComponent(message),
        callback: function (_req) {
            var tmp = _req.responseText;
            var obj = eval("(" + tmp + ")");
            jmaki.publish('/cl/setValues', obj);
            // handle any errors
        }
    });
});

jmaki.subscribe("/cl/getAgences/*", function (args) {
    var message = args.value;
    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=listAgences&message=" + encodeURIComponent(message),
        callback: function (_req) {
            var tmp = _req.responseText;
            var obj = eval("(" + tmp + ")");
            jmaki.publish('/cl/setValues', obj);
            // handle any errors
        }
    });
});
jmaki.subscribe("/cl/getCompteRemettant/*", function (args) {
    var message = args.value;
    var arrayString = message.split('-');
    var numero = arrayString[0];
    var escompte = arrayString[1];
    var agence = arrayString[2];
    var nomclient = arrayString[3];
    var user = arrayString[4];
    document.forms['remiseForm'].numero.value = numero;
    document.forms['remiseForm'].escompte.value = escompte;
    document.forms['remiseForm'].nom.value = nomclient;
    document.forms['remiseForm'].agence.value = agence;


});
jmaki.subscribe("/cl/getBordereau/*", function (args) {
    var message = args.value;
    var arrayString = message.split('-');
    var noex = arrayString[0];
    var idop = arrayString[1];
    var cdscpta = arrayString[2];
    var mtop = arrayString[3];
    var typeinter = arrayString[4];
    var nminter = arrayString[5];
    // alert(noex +" "+idop+" "+cdscpta+" "+mtop+" "+typeinter+" "+nminter);
    document.forms['remiseForm'].montantRemise.value = mtop;
    document.forms['remiseForm'].nom.value = nminter;
    document.forms['remiseForm'].noex.value = noex;
    document.forms['remiseForm'].idop.value = idop;
    document.forms['remiseForm'].cdscpta.value = cdscpta;
    document.forms['remiseForm'].typeinter.value = typeinter;



});

jmaki.subscribe("/choix/virement/*", function (args) {
    var message = args.value;
    if (message == 'VIRSTACLIANCNOR' || message == 'VIRSTACLINOUNOR') {

        document.forms['creationVirementClient'].action.value = 'creationVirementClient';
        document.forms['creationVirementClient'].numeroCompteBen.value = '';
        document.forms['creationVirementClient'].numero.value = '';
        document.getElementById("numeroTire").style.display = "block";
        document.getElementById("numeroBen").style.display = "block";
        document.getElementById("nomTire").style.display = "block";
        document.getElementById("agenceTire").style.display = "block";
        document.getElementById("nomBen").style.display = "block";
        document.getElementById("addressBen").style.display = "block";
        document.getElementById("numeroVir").style.display = "block";

        document.getElementById("libelleVir").style.display = "block";

    } else if (message == 'VIRMISDISANCNOR' || message == 'VIRSTACLINOUNOR') {
        document.forms['creationVirementClient'].action.value = 'creationVirementDisposition';
        document.forms['creationVirementClient'].numeroCompteBen.value = '000';
        document.forms['creationVirementClient'].numero.value = '';
        document.getElementById("numeroTire").style.display = "block";
        document.getElementById("numeroBen").style.display = "none";

        document.getElementById("nomTire").style.display = "block";
        document.getElementById("agenceTire").style.display = "block";
        document.getElementById("nomBen").style.display = "block";
        document.getElementById("addressBen").style.display = "block";
        document.getElementById("numeroVir").style.display = "block";
        document.getElementById("libelleVir").style.display = "block";

    } else if (message == 'VIRBANBANANCNOR') {
        document.forms['creationVirementClient'].action.value = 'creationVirementBanque';
        document.forms['creationVirementClient'].numeroCompteBen.value = '000';
        document.forms['creationVirementClient'].numero.value = '000';
        document.getElementById("numeroTire").style.display = "none";
        document.getElementById("numeroBen").style.display = "none";

        document.getElementById("nomTire").style.display = "block";
        document.getElementById("agenceTire").style.display = "block";
        document.getElementById("nomBen").style.display = "block";
        document.getElementById("addressBen").style.display = "block";
        document.getElementById("numeroVir").style.display = "block";
        document.getElementById("libelleVir").style.display = "block";

        document.getElementById("nomTire").disabled = false;
        document.getElementById("agenceTire").disabled = false;
    } else if (message == 'VIRSTACLIACP' || message == 'VIRSTACLIACPREP') {
        document.forms['creationVirementClient'].action.value = 'creationVirementClientACP';
        document.forms['creationVirementClient'].numeroCompteBen.value = '';
        document.forms['creationVirementClient'].numero.value = '';
        document.getElementById("numeroTire").style.display = "block";
        document.getElementById("numeroBen").style.display = "block";
        document.getElementById("nomTire").style.display = "block";
        document.getElementById("agenceTire").style.display = "block";
        document.getElementById("nomBen").style.display = "block";
        document.getElementById("addressBen").style.display = "block";
        document.getElementById("numeroVir").style.display = "block";
        document.getElementById("libelleVir").style.display = "block";

    }

});
jmaki.subscribe("/choix/virementBNP/*", function (args) {
    var message = args.value;
    if (message == 'VIRSTACLIANCNOR' || message == 'VIRSTACLINOUNOR') {

        document.forms['creationVirementClientBNP'].action.value = 'creationVirementClientBNP';
        document.forms['creationVirementClientBNP'].numeroCompteBen.value = '';
        document.forms['creationVirementClientBNP'].numero.value = '';
        document.getElementById("numeroTire").style.display = "block";
        document.getElementById("numeroBen").style.display = "block";
        document.getElementById("nomTire").style.display = "block";
        document.getElementById("agenceTire").style.display = "block";
        document.getElementById("nomBen").style.display = "block";
        document.getElementById("addressBen").style.display = "block";
        document.getElementById("numeroVir").style.display = "block";

        document.getElementById("libelleVir").style.display = "block";

    } else if (message == 'VIRMISDISANCNOR' || message == 'VIRSTACLINOUNOR') {
        document.forms['creationVirementClientBNP'].action.value = 'creationVirementDisposition';
        document.forms['creationVirementClientBNP'].numeroCompteBen.value = '000';
        document.forms['creationVirementClientBNP'].numero.value = '';
        document.getElementById("numeroTire").style.display = "block";
        document.getElementById("numeroBen").style.display = "none";

        document.getElementById("nomTire").style.display = "block";
        document.getElementById("agenceTire").style.display = "block";
        document.getElementById("nomBen").style.display = "block";
        document.getElementById("addressBen").style.display = "block";
        document.getElementById("numeroVir").style.display = "block";
        document.getElementById("libelleVir").style.display = "block";

    } else if (message == 'VIRBANBANANCNOR') {
        document.forms['creationVirementClientBNP'].action.value = 'creationVirementBanque';
        document.forms['creationVirementClientBNP'].numeroCompteBen.value = '000';
        document.forms['creationVirementClientBNP'].numero.value = '000';
        document.getElementById("numeroTire").style.display = "none";
        document.getElementById("numeroBen").style.display = "none";

        document.getElementById("nomTire").style.display = "block";
        document.getElementById("agenceTire").style.display = "block";
        document.getElementById("nomBen").style.display = "block";
        document.getElementById("addressBen").style.display = "block";
        document.getElementById("numeroVir").style.display = "block";
        document.getElementById("libelleVir").style.display = "block";

        document.getElementById("nomTire").disabled = false;
        document.getElementById("agenceTire").disabled = false;
    } else if (message == 'VIRSTACLIACP' || message == 'VIRSTACLIACPREP') {
        document.forms['creationVirementClientBNP'].action.value = 'creationVirementClientACP';
        document.forms['creationVirementClientBNP'].numeroCompteBen.value = '';
        document.forms['creationVirementClientBNP'].numero.value = '';
        document.getElementById("numeroTire").style.display = "block";
        document.getElementById("numeroBen").style.display = "block";
        document.getElementById("nomTire").style.display = "block";
        document.getElementById("agenceTire").style.display = "block";
        document.getElementById("nomBen").style.display = "block";
        document.getElementById("addressBen").style.display = "block";
        document.getElementById("numeroVir").style.display = "block";
        document.getElementById("libelleVir").style.display = "block";

    }

});
jmaki.subscribe("/type/taches/*", function (args) {
    var message = args.value;
    jmaki.log("mssage =" + message);
    document.getElementById("requeteMasterDIV").style.display = "none";
    document.getElementById("actionDIV").style.display = "none";
    if (message == 'MENU') {
        document.forms['creationTacheForm'].typetache.value = message;
        document.forms['creationTacheForm'].url.value = message;

    } else if (message == 'dataTable.jsp') {
        document.forms['creationTacheForm'].typetache.value = "PAGE";

        document.getElementById("requeteMasterDIV").style.display = "block";
    } else if (message == 'dataTableMasterDetail.jsp') {
        document.forms['creationTacheForm'].typetache.value = "PAGE";
        document.forms['creationTacheForm'].url.value = message + "?requete=";
        document.getElementById("requeteMasterDIV").style.display = "block";
    } else if (message == 'ControlServlet') {
        document.forms['creationTacheForm'].typetache.value = "PAGE";
        document.forms['creationTacheForm'].url.value = message + "?action=";
        document.getElementById("actionDIV").style.display = "block";
    } else if (message == 'PAGE') {
        document.forms['creationTacheForm'].typetache.value = message;
        document.getElementById("pageDIV").style.display = "block";
    }


});
jmaki.subscribe("/ic/getNumero/*", function (args) {
    var message = args.value;
    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=choixNumeroCompte&message=" + encodeURIComponent(message),
        callback: function (_req) {

            var tmp = _req.responseText;

            var obj = eval("(" + tmp + ")");
            //jmaki.publish('/yahoo/autocomplete/setValues', obj);
            //jmaki.publish('/yahoo/autocomplete/setValue', obj);
            jmaki.log("obj =" + obj[0].numero);
            jmaki.publish('/nomCompte/select', obj[0].nom);
            jmaki.publish('/agenceCompte/select', obj[0].agence);
            // handle any errors
        }
    });
});



jmaki.subscribe("/nomCompte/*", function (args) {

    var nomCompteWidget = jmaki.getWidget('nomCompte');
    nomCompteWidget.setValue(args.toString());

});

jmaki.subscribe("/agenceCompte/*", function (args) {

    var agenceCompteWidget = jmaki.getWidget('agenceCompte');
    agenceCompteWidget.setValue(args.toString());

});


jmaki.subscribe("/choix/agenceECI/*", function (args) {

    var agence = args.value.substring(1);
    var numeroCompte = document['remiseForm'].numero.value;

    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=verificationCompteECI&agence=" + encodeURIComponent(agence) + "&numeroCompte=" + encodeURIComponent(numeroCompte),
        callback: function (_req) {

            var tmp = _req.responseText;

            var obj = eval("(" + tmp + ")");

            document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
            document.forms['remiseForm'].nom.value = obj[0].nomClient;
            document.forms['remiseForm'].escompte.value = obj[0].escompte;
            document.forms['remiseForm'].clerib.value = obj[0].cleRib;



            // handle any errors
        }
    });




});

jmaki.subscribe("/choix/agenceCNCE/*", function (args) {

    var agence = args.value.substring(1);
    var numeroCompte = document['remiseForm'].numero.value;

    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=verificationCompteCNCE&agence=" + encodeURIComponent(agence) + "&numeroCompte=" + encodeURIComponent(numeroCompte),
        callback: function (_req) {

            var tmp = _req.responseText;

            var obj = eval("(" + tmp + ")");

            if (obj[0].action == "9") {
                document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageResult + "</H2>");
            } else {
                if (obj[0].actionStatut == "9") {
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageStatutResult + "</H2>");
                } else {
                    document.forms['remiseForm'].messageStatut.value = obj[0].messageStatutResult;
                    document.forms['remiseForm'].message.value = obj[0].messageResult;
                    //document.forms['remiseForm'].numero.value=obj[0].numeroCompte;
                    //document.forms['remiseForm'].nom.value = obj[0].nomClient;

                    document.forms['remiseForm'].escompte.value = obj[0].escompte;
                }

            }



            // handle any errors
        }
    });




});

jmaki.subscribe("/choix/agenceNSIA/*", function (args) {

    var agence = args.value.substring(1);
    var numeroCompte = document['remiseForm'].numero.value;

    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=verificationCompteNSIA&agence=" + encodeURIComponent(agence) + "&numeroCompte=" + encodeURIComponent(numeroCompte),
        callback: function (_req) {

            var tmp = _req.responseText;

            var obj = eval("(" + tmp + ")");

            if (obj[0].action == "9") {
                document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageResult + "</H2>");
            } else {
                if (obj[0].actionStatut == "9") {
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageStatutResult + "</H2>");
                } else {
                    document.forms['remiseForm'].messageStatut.value = obj[0].messageStatutResult;
                    document.forms['remiseForm'].message.value = obj[0].messageResult;
                    //document.forms['remiseForm'].numero.value=obj[0].numeroCompte;
                    //document.forms['remiseForm'].nom.value = obj[0].nomClient;

                    document.forms['remiseForm'].escompte.value = obj[0].escompte;
                }

            }



            // handle any errors
        }
    });




});
jmaki.subscribe("/choix/agenceETG/*", function (args) {

    var agence = args.value.substring(1);
    var numeroCompte = document['remiseForm'].numero.value;

    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=verificationCompteETG&agence=" + encodeURIComponent(agence) + "&numeroCompte=" + encodeURIComponent(numeroCompte),
        callback: function (_req) {

            var tmp = _req.responseText;

            var obj = eval("(" + tmp + ")");
            
            document.forms['remiseForm'].messageStatut.value = obj[0].messageStatutResult;
                    document.forms['remiseForm'].message.value = obj[0].messageResult;
                    //document.forms['remiseForm'].numero.value=obj[0].numeroCompte;
                    //document.forms['remiseForm'].nom.value = obj[0].nomClient;

                    document.forms['remiseForm'].escompte.value = obj[0].escompte;
                    

//            if (obj[0].action == "9") {
//                document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageResult + "</H2>");
//            } else {
//                if (obj[0].actionStatut == "9") {
//                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageStatutResult + "</H2>");
//                } else {
//                    document.forms['remiseForm'].messageStatut.value = obj[0].messageStatutResult;
//                    document.forms['remiseForm'].message.value = obj[0].messageResult;
//                    //document.forms['remiseForm'].numero.value=obj[0].numeroCompte;
//                    //document.forms['remiseForm'].nom.value = obj[0].nomClient;
//
//                    document.forms['remiseForm'].escompte.value = obj[0].escompte;
//                }
//
//            }



            // handle any errors
        }
    });




});
/*
 jmaki.subscribe("/ag/getNomCompte/*", function(args) {
 var message = args.value;
 jmaki.doAjax({method: "POST",
 url: "ControlServlet?action=choixNomCompte&message=" + encodeURIComponent(message),
 callback: function(_req) {
 var tmp = _req.responseText;
 var obj = eval("(" + tmp + ")");
 jmaki.publish('/ag/setValues', obj);
 // handle any errors
 }
 });
 });*/