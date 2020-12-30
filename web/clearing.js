function doAjaxSubmitEncoding(form, fields, servlet) {
    var fieldList = new Array();
    var fieldListValues = new Array();

    if (typeof fields != "string") {
        for (i = 0; i < form.elements.length; i++) {
            fieldList[i] = form.elements[i].name;
            fieldListValues[i] = form.elements[i].value;


        }
    } else {
        fieldList[0] = fields;
        for (i = 0; i < form.elements.length; i++) {
            if (fieldList[0] == form.elements[i].name)
                fieldListValues[0] = form.elements[i].value;



        }

    }


    var servletUrl = servlet + "?";
    for (i = 0; i < fieldList.length; i++)
    {
        servletUrl += fieldList[i] + "=" + encodeURIComponent(fieldListValues[i]);
        if (fieldListValues[i].trim() == '') {
            alert(fieldList[i] + " requiert une valeur");
            return false;
        }
        if (i < fieldList.length - 1) {
            servletUrl += "&";
        }

    }
    //alert(servletUrl);


    jmaki.doAjax({
        url: servletUrl,
        method: 'POST',
        callback: function (req) {
            var _req = req;
            postProcessResponse(_req);
        }
    });
    return false;

}


function doAjaxSubmit(form, fields, servlet) {
    var fieldList = new Array();
    var fieldListValues = new Array();

    if (typeof fields != "string") {
        for (i = 0; i < form.elements.length; i++) {
            fieldList[i] = form.elements[i].name;
            fieldListValues[i] = form.elements[i].value;


        }
    } else {
        fieldList[0] = fields;
        for (i = 0; i < form.elements.length; i++) {
            if (fieldList[0] == form.elements[i].name)
                fieldListValues[0] = form.elements[i].value;
        }

    }


    var servletUrl = servlet + "?";
    for (i = 0; i < fieldList.length; i++)
    {
        servletUrl += fieldList[i] + "=" + fieldListValues[i];
        if (fieldListValues[i].trim() == '') {
            alert(fieldList[i] + " requiert une valeur");
            return false;
        }
        if (i < fieldList.length - 1) {
            servletUrl += "&";
        }

    }
    //alert(servletUrl);


    jmaki.doAjax({
        url: servletUrl,
        method: 'POST',
        callback: function (req) {
            var _req = req;
            postProcessResponse(_req);
        }
    });
    return false;

}


function doAjaxSubmitWithResult(form, fields, servlet) {
    var fieldList = new Array();
    var fieldListValues = new Array();
    var returnValue;

    if (typeof fields != "string") {
        for (i = 0; i < form.elements.length; i++) {            
            fieldList[i] = form.elements[i].name;
            fieldListValues[i] = form.elements[i].value;
            
        }
    } else {
        fieldList[0] = fields;
        for (i = 0; i < form.elements.length; i++) {
            if (fieldList[0] == form.elements[i].name)
                fieldListValues[0] = form.elements[i].value;
        }

    }


    var servletUrl = servlet + "?";
    for (i = 0; i < fieldList.length; i++)
    {
        servletUrl += fieldList[i] + "=" + encodeURIComponent(fieldListValues[i]);
        //  servletUrl += fieldList[i] + "=" + fieldListValues[i];
        if (fieldListValues[i].trim() == '') {
            alert(fieldList[i] + " requiert une valeur");
            if (document['remiseForm'] !== undefined) {
                document.forms['remiseForm'].valider.disabled = false;
                document['remiseForm'].valider.style.display = "block";
                document['remiseForm'].numero.focus();
            }
            if (document['chequeForm'] !== undefined) {
                document.forms['chequeForm'].valider.disabled = false;
                document['chequeForm'].valider.style.display = "block";

                document['chequeForm'].montantCheque.focus();

            }

            return false;
        }
        if (i < fieldList.length - 1) {
            servletUrl += "&";
        }

    }
    console.log("servletUrl :" + servletUrl);
    jmaki.doAjax({
        url: servletUrl,
        method: 'POST',
        callback: function (req) {
            var _req = req;
            postProcessSilentResponse(_req);

        }
    });


}
function doAjaxSubmitVignette(form, fields, servlet) {
    var fieldList = new Array();
    var fieldListValues = new Array();


    if (typeof fields != "string") {
        for (i = 0; i < form.elements.length; i++) {
            fieldList[i] = form.elements[i].name;
            fieldListValues[i] = form.elements[i].value;

        }
    } else {
        fieldList[0] = fields;
        for (i = 0; i < form.elements.length; i++) {
            if (fieldList[0] == form.elements[i].name)
                fieldListValues[0] = form.elements[i].value;
        }

    }


    var servletUrl = servlet + "?";
    for (i = 0; i < fieldList.length; i++)
    {
        servletUrl += fieldList[i] + "=" + fieldListValues[i];
        if (fieldListValues[i].trim() == '') {
            alert(fieldList[i] + " requiert une valeur");
            return false;
        }
        if (i < fieldList.length - 1) {
            servletUrl += "&";
        }

    }

    jmaki.doAjax({
        url: servletUrl,
        method: 'POST',
        callback: function (req) {
            var _req = req;
            postProcessVignetteResponse(_req);

        }
    });


}
function getVirSiblingsForm(form) {


    var url = jmaki.webRoot + "/data.jsp?only=rows&requete=" + document['creationVirementClient'].requete.value +
            " where " + document['creationVirementClient'].cle.value + "='" + form.elements['numero'].value + "'" +
            " and " + document['creationVirementClient'].cle2.value + "='" + form.elements['numeroCompteBen'].value + "'";
    // console.log("getVirSiblings form " + url);

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
function getVirSiblingsFormBNP(form) {


    var url = jmaki.webRoot + "/data.jsp?only=rows&requete=" + document['creationVirementClientBNP'].requete.value +
            " where " + document['creationVirementClientBNP'].cle.value + "='" + form.elements['numero'].value + "'" +
            " and " + document['creationVirementClientBNP'].cle2.value + "='" + form.elements['numeroCompteBen'].value + "'";


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
function postProcessVignetteResponse(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var response = req.responseText;
            if (response.trim() != "OK") {
                //alert(response);
                var obj = eval("(" + response + ")");
                if (obj[0].action == "9") {
                    document.getElementById("messageDiv").innerHTML = "<font color=\"red\">" + obj[0].message + "</font>"
                    //  alert(obj[0].message);
                } else {
                    document.getElementById("messageDiv").innerHTML = "<font color=\"green\">" + obj[0].message + "</font>"
                }
                document.forms['confirmForm'].confirmer.style.display = "block";

            }




        }
    }
}
function postProcessSilentResponse(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var response = req.responseText;
            if (response.trim() != "OK") {
                alert(response);
            }

            if (location.href.match('validationRemises.jsp') != null) {
                location.replace('validationRemises.jsp');
            } 
            //same
              else if (location.href.match('validationRemisesEffets.jsp') != null) {
                location.replace('validationRemisesEffets.jsp');
            }
            else if (location.href.match('correctionremises_effets.jsp') != null) {
                location.replace('correctionremises_effets.jsp');
            }
              else if (location.href.match('validationRemisesBornes.jsp') != null) {
                location.replace('validationRemisesBornes.jsp');
            } else if (location.href.match('validationRemisesBornes_eci.jsp') != null) {
                location.replace('validationRemisesBornes_eci.jsp');
            } else if (location.href.match('validationRemisesAgence.jsp') != null) {
                location.replace('validationRemisesAgence.jsp');
            } else if (location.href.match('validationRemisesAgence_cbao.jsp') != null) {
                location.replace('validationRemisesAgence_cbao.jsp');
            } else if (location.href.match('validationRemises_cbao.jsp') != null) {
                location.replace('validationRemises_cbao.jsp');
            } else if (location.href.match('validationRemises_ubaguinee.jsp') != null) {
                location.replace('validationRemises_ubaguinee.jsp');
            } else if (location.href.match('validationRemises_bdk.jsp') != null) {
                location.replace('validationRemises_bdk.jsp');
            } else if (location.href.match('validationVirements.jsp') != null) {
                location.replace('validationVirements.jsp');
            } else if (location.href.match('validationVirementsFichiers.jsp') != null) {
                location.replace('validationVirementsFichiers.jsp');
            } else if (location.href.match('validationVirementsFichiersECM.jsp') != null) {
                location.replace('validationVirementsFichiersECM.jsp');
            } else if (location.href.match('validationRemises_batch_lite.jsp') != null) {
                location.replace('validationRemises_batch_lite.jsp');
            } else if (location.href.match('validationRemises_batch_lite_1.jsp') != null) {
                location.replace('validationRemises_batch_lite_1.jsp');
            } else if (location.href.match('validationEffets.jsp') != null) {
                location.replace('validationEffets.jsp');
            } else if (location.href.match('validationEffets.jsp') != null) {
                location.replace('validationEffets.jsp');
            } else if (location.href.match('verificationImages.jsp') != null) {
                location.replace('verificationImages.jsp');
            } else if (location.href.match('verificationSignatures.jsp') != null) {
                location.replace('verificationSignatures.jsp');
            } else if (location.href.match('verificationVignettes.jsp') != null) {
                location.replace('verificationVignettes.jsp');
            } else if (location.href.match('verificationSignatures_cbao.jsp') != null) {
                location.replace('verificationSignatures_cbao.jsp');
            } else if (location.href.match('verificationSignatures_bdk.jsp') != null) {
                location.replace('verificationSignatures_bdk.jsp');
            } else if (location.href.match('verificationSignatures_esn.jsp') != null) {
                location.replace('verificationSignatures_esn.jsp');
            } else if (location.href.match('verificationSignatures_tpci.jsp') != null) {
                location.replace('verificationSignatures_tpci.jsp');
            } else if (location.href.match('verificationSignatures_versus.jsp') != null) {
                location.replace('verificationSignatures_versus.jsp');
            } else if (location.href.match('saisieremises_tresorbf.jsp') != null) {
                location.replace('saisieremises_tresorbf.jsp');
            } else if (location.href.match('saisieremises_cnce.jsp') != null) {
                location.replace('saisieremises_cnce.jsp');
            } else if (location.href.match('saisieremises_tpci.jsp') != null) {
                location.replace('saisieremises_tpci.jsp');
            } else if (location.href.match('saisieremises_tpci_cu.jsp') != null) {
                location.replace('saisieremises_tpci_cu.jsp');
            } else if (location.href.match('saisieremises_cbao.jsp') != null) {
                location.replace('saisieremises_cbao.jsp');
            } else if (location.href.match('saisieremises_nsia.jsp') != null) {
                location.replace('saisieremises_nsia.jsp');
            } else if (location.href.match('saisieremises_nsia_1.jsp') != null) {
                location.replace('saisieremises_nsia_1.jsp');
            } else if (location.href.match('saisieremises_signatures_cbao.jsp') != null) {
                location.replace('saisieremises_signatures_cbao.jsp');
            } else if (location.href.match('saisiecheques_cbao.jsp') != null) {
                location.replace('saisiecheques_cbao.jsp');
            } else if (location.href.match('saisiecheques_bnp.jsp') != null) {
                location.replace('saisiecheques_bnp.jsp');
            } else if (location.href.match('saisiecheques_ubaguinee.jsp') != null) {
                location.replace('saisiecheques_ubaguinee.jsp');
            } else if (location.href.match('saisiecheques_bdk.jsp') != null) {
                location.replace('saisiecheques_bdk.jsp');
            } else if (location.href.match('saisiecheques_bdk_1.jsp') != null) {
                location.replace('saisiecheques_bdk_1.jsp');
            } else if (location.href.match('saisiecheques_lite.jsp') != null) {
                location.replace('saisiecheques_lite.jsp');
            } else if (location.href.match('saisiecheques_lite.jsp') != null) {
                location.replace('saisiecheques_lite.jsp');
            } else if (location.href.match('saisiecheques_lite_1.jsp') != null) {
                location.replace('saisiecheques_lite_1.jsp');
            } else if (location.href.match('saisiecheques_tpci.jsp') != null) {
                location.replace('saisiecheques_tpci.jsp');
            } else if (location.href.match('saisiecheques_tpci_cu.jsp') != null) {
                location.replace('saisiecheques_tpci_cu.jsp');
            } else if (location.href.match('saisieremises_bdk.jsp') != null) {
                location.replace('saisieremises_bdk.jsp');
            } else if (location.href.match('saisieremises_ubaguinee.jsp') != null) {
                location.replace('saisieremises_ubaguinee.jsp');
            } else if (location.href.match('correctionremises_cbao.jsp') != null) {
                location.replace('correctionremises_cbao.jsp');
            } else if (location.href.match('correctionremises_ubaguinee.jsp') != null) {
                location.replace('correctionremises_ubaguinee.jsp');
            } else if (location.href.match('correctionremises_tpci.jsp') != null) {
                location.replace('correctionremises_tpci.jsp');
            } else if (location.href.match('correctionremises_tpci_cu.jsp') != null) {
                location.replace('correctionremises_tpci_cu.jsp');
            } else if (location.href.match('correctionremises.jsp') != null) {
                location.replace('correctionremises.jsp');
            } else if (location.href.match('correctionremises_1.jsp') != null) {
                location.replace('correctionremises_1.jsp');
            } else if (location.href.match('correctionremises_lite.jsp') != null) {
                location.replace('correctionremises_lite.jsp');
            } else if (location.href.match('validationRemises_lite_corporates.jsp') != null) {
                location.replace('validationRemises_lite_corporates.jsp');
            } else if (location.href.match('validationRemises_lite.jsp') != null) {
                location.replace('validationRemises_lite.jsp');
            } else if (location.href.match('validationRemises_lite_rc.jsp') != null) {
                location.replace('validationRemises_lite_rc.jsp');
            } else if (location.href.match('validationRemises_lite_rnc.jsp') != null) {
                location.replace('validationRemises_lite_rnc.jsp');
            } else if (location.href.match('validationRemisesSignatures_etg.jsp') != null) {
                location.replace('validationRemisesSignatures_etg.jsp');
            } else if (location.href.match('validationRemisesSignatures_eci.jsp') != null) {
                location.replace('validationRemisesSignatures_eci.jsp');
            } else if (location.href.match('saisieremises_ecobank.jsp') != null) {
                location.replace('saisieremises_ecobank.jsp');
            } else if (location.href.match('saisieremises_signatures.jsp') != null) {
                location.replace('saisieremises_signatures.jsp');
            } else if (location.href.match('saisieremises_signatures_etg.jsp') != null) {
                location.replace('saisieremises_signatures_etg.jsp');
            } else if (location.href.match('saisieremises_uv.jsp') != null) {
                location.replace('saisieremises_uv.jsp');
            } else if (location.href.match('saisiecheques.jsp') != null) {
                location.replace('saisiecheques.jsp');
            } 
            
                //eugene same 
             else if (location.href.match('saisieeffets.jsp') != null) {
                location.replace('saisieeffets.jsp');
            }else if (location.href.match('saisieeffets_amplitude.jsp') != null) {
                location.replace('saisieeffets_amplitude.jsp');
            }
            else if (location.href.match('saisieeffets_type.jsp') != null) {
                location.replace('saisieeffets_type.jsp');
                //eugene same 
            }
            else if (location.href.match('saisieremises_etg.jsp') != null) {
                location.replace('saisieremises_etg.jsp');
            } else if (location.href.match('saisiecheques_esn.jsp') != null) {
                location.replace('saisiecheques_esn.jsp');
            } else if (location.href.match('saisiecheques_signatures.jsp') != null) {
                location.replace('saisiecheques_signatures.jsp');
            } else if (location.href.match('saisiecheques_signatures_esn.jsp') != null) {
                location.replace('saisiecheques_signatures.jsp');
            } else if (location.href.match('saisiecheques_ebj.jsp') != null) {
                location.replace('saisiecheques_ebj.jsp');
            } else if (location.href.match('saisiecheques_signatures_ebj.jsp') != null) {
                location.replace('saisiecheques_signatures_ebj.jsp');
            } else if (location.href.match('saisiecheques_signatures_cbao.jsp') != null) {
                location.replace('saisiecheques_signatures_cbao.jsp');
            } else if (location.href.match('saisieremises.jsp') != null) {
                location.replace('saisieremises.jsp');
            }

            if (document['remiseForm'] != undefined) {
                if (document['remiseForm'].valider == undefined) {
                    document.forms['chequeForm'].valider.disabled = false;
                    document['chequeForm'].valider.style.display = "block";
                    document['chequeForm'].montantCheque.focus();
                } else {
                    document.forms['remiseForm'].valider.disabled = false;
                    document['remiseForm'].valider.style.display = "block";
                    document['remiseForm'].numero.focus();
                }
            }

        }
    }
}

function postProcessResponse(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var response = req.responseText;
            if (response.trim() != "") {
                alert(response);
            }

            if (document['creationEffetClient'] != undefined) {
                document['creationEffetClient'].reset();
            }
            if (document['creationVirementClient'] != undefined) {
                document['creationVirementClient'].reset();
            }
            if (document['inscription'] != undefined) {
                document['inscription'].reset();
            }
            if (document['modification'] != undefined) {
                document['modification'].reset();
            }
            if (document['modification'] != undefined && location.href.match('ControlServlet') != null) {
                location.replace('login.jsp');
            }


        }
    }
}

function getVignettes(numerocheque, numeroendos) {

    var url = jmaki.webRoot + "/data.jsp?only=rows&requete=" + document['searchVignette'].requete.value + " where " + document['searchVignette'].cle.value + "='" + numerocheque + "' or " + document['searchVignette'].cle1.value + "='" + numeroendos + "'";

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
function getVirSiblings(numerocompte) {

    var url = jmaki.webRoot + "/data.jsp?only=rows&requete=" + document['creationVirementClient'].requete.value + " where " + document['creationVirementClient'].cle.value + "='" + numerocompte + "'";

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
function getVirSiblingsBNP(numerocompte) {

    var url = jmaki.webRoot + "/data.jsp?only=rows&requete=" + document['creationVirementClientBNP'].requete.value + " where " + document['creationVirementClientBNP'].cle.value + "='" + numerocompte + "'";

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
function numeriqueValide(evt) {
    /* var keyCode = evt.which ? evt.which : evt.keyCode;
     if (((keyCode==9)||(keyCode==8))||(keyCode==13)) return true;
     var accepter = "0123456789";
     if (accepter.indexOf(String.fromCharCode(keyCode)) >= 0) {
     return true;
     }
     else {
     return false;
     }*/
    var keyCode = evt.which ? evt.which : evt.keyCode;
    if (keyCode > 31 && (keyCode < 45 || keyCode > 57))
        return false;
    return true;
}


function checkPrelParam() {
    var strDate = jmaki.getWidget('datefin').getValue();
    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);
    var dateDebChosen = year + "/" + month + "/" + day;
    document['UploadForm'].param1.value = dateDebChosen;
    return true;
}

function  chargeCompteFromSibICCPT() {
    var message = document.forms['creationCompteClientNSIA'].numeroCompte.value.trim();
    if (message != '') {

        document.forms['creationCompteClientNSIA'].nomClient.value = "";
        document.forms['creationCompteClientNSIA'].agenceCompte.value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixCompteFromSibICCPT&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + document.forms['creationCompteClientNSIA'].numeroCompte.value + " n'existe pas dans SIBICCPT</H2>"); //sibiccpt
                }
                var obj = eval("(" + tmp + ")");
                //jmaki.publish('/yahoo/autocomplete/setValues', obj);
                //jmaki.publish('/yahoo/autocomplete/setValue', obj);
                //jmaki.log("obj ="+ obj);

                document.forms['creationCompteClientNSIA'].numeroCompte.value = obj[0].numeroCompte;
                document.forms['creationCompteClientNSIA'].nomClient.value = obj[0].nomClient;
                document.forms['creationCompteClientNSIA'].agenceCompte.value = obj[0].agence;
                //   document.forms['creationCompteClientNSIA'].adresse.value = obj[0].adresse;

                //      document.forms['creationCompteClientNSIA'].escompte.value = obj[0].etat;



                //                if(obj[0].action == "9") {
                //                     document.write("<H2>Le numéro de compte "+document.forms['remiseForm'].numero.value+" est rejeté pour motif :"+obj[0].messageResult+"</H2>");
                //                }else{
                //                    if(obj[0].actionStatut == "9"){
                //                        document.write("<H2>Le numéro de compte "+document.forms['remiseForm'].numero.value+" est rejeté pour motif :"+obj[0].messageStatutResult+"</H2>");
                //                    }else{
                //                        document.forms['remiseForm'].messageStatut.value=obj[0].messageStatutResult;
                //                        document.forms['remiseForm'].message.value=obj[0].messageResult;
                //                        document.forms['remiseForm'].numero.value=obj[0].numeroCompte;
                //                        document.forms['remiseForm'].nom.value = obj[0].nomClient;
                //                        document.forms['remiseForm'].agence.value = obj[0].agenceCompte;
                //                        document.forms['remiseForm'].escompte.value = obj[0].escompte;
                //                    }
                //
                //                }



                // handle any errors
            }
        });
    }
}

function chargeInfoCompte() {

    var message = document.forms['remiseForm'].numero.value.trim();
    if (message != '') {

        document.forms['remiseForm'].nom.value = "";
        document.forms['remiseForm'].agence.value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompte&message=" + encodeURIComponent(message),
            callback: function (_req) {
                
                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");
                //jmaki.publish('/yahoo/autocomplete/setValues', obj);
                //jmaki.publish('/yahoo/autocomplete/setValue', obj);
                //jmaki.log("obj ="+ obj);


                document.forms['remiseForm'].numero.value = obj[0].numero;
                document.forms['remiseForm'].nom.value = obj[0].nom;
                document.forms['remiseForm'].agence.value = obj[0].agence;
                document.forms['remiseForm'].escompte.value = obj[0].etat;



                //                if(obj[0].action == "9") {
                //                     document.write("<H2>Le numéro de compte "+document.forms['remiseForm'].numero.value+" est rejeté pour motif :"+obj[0].messageResult+"</H2>");
                //                }else{
                //                    if(obj[0].actionStatut == "9"){
                //                        document.write("<H2>Le numéro de compte "+document.forms['remiseForm'].numero.value+" est rejeté pour motif :"+obj[0].messageStatutResult+"</H2>");
                //                    }else{
                //                        document.forms['remiseForm'].messageStatut.value=obj[0].messageStatutResult;
                //                        document.forms['remiseForm'].message.value=obj[0].messageResult;
                //                        document.forms['remiseForm'].numero.value=obj[0].numeroCompte;
                //                        document.forms['remiseForm'].nom.value = obj[0].nomClient;
                //                        document.forms['remiseForm'].agence.value = obj[0].agenceCompte;
                //                        document.forms['remiseForm'].escompte.value = obj[0].escompte;
                //                    }
                //
                //                }



                // handle any errors
            }
        });
    }
}
function chargeInfoCompteLite() {
    var message = document.forms['remiseForm'].numero.value.trim();
    if (message != '') {

        document.forms['remiseForm'].nom.value = "";
        document.forms['remiseForm'].agence.value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompteLite&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");
                //jmaki.publish('/yahoo/autocomplete/setValues', obj);
                //jmaki.publish('/yahoo/autocomplete/setValue', obj);
                //jmaki.log("obj ="+ obj);


                document.forms['remiseForm'].numero.value = obj[0].numero;
                document.forms['remiseForm'].nom.value = obj[0].nom;
                document.forms['remiseForm'].agence.value = obj[0].agence;
                document.forms['remiseForm'].escompte.value = obj[0].etat;



                //                if(obj[0].action == "9") {
                //                     document.write("<H2>Le numéro de compte "+document.forms['remiseForm'].numero.value+" est rejeté pour motif :"+obj[0].messageResult+"</H2>");
                //                }else{
                //                    if(obj[0].actionStatut == "9"){
                //                        document.write("<H2>Le numéro de compte "+document.forms['remiseForm'].numero.value+" est rejeté pour motif :"+obj[0].messageStatutResult+"</H2>");
                //                    }else{
                //                        document.forms['remiseForm'].messageStatut.value=obj[0].messageStatutResult;
                //                        document.forms['remiseForm'].message.value=obj[0].messageResult;
                //                        document.forms['remiseForm'].numero.value=obj[0].numeroCompte;
                //                        document.forms['remiseForm'].nom.value = obj[0].nomClient;
                //                        document.forms['remiseForm'].agence.value = obj[0].agenceCompte;
                //                        document.forms['remiseForm'].escompte.value = obj[0].escompte;
                //                    }
                //
                //                }



                // handle any errors
            }
        });
    }
}

function chargeLibelleBanque() {
    var message = document.forms['chequeForm'].codeBanque.value.trim();
    if (message != '') {


        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixCodeBanque&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le code Banque " + document.forms['chequeForm'].codeBanque.value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");



                document.forms['chequeForm'].codeBanque.value = obj[0].codebanque;
                document.forms['chequeForm'].libelleBanque.value = obj[0].libellebanque;






                // handle any errors
            }
        });
    }
}

function chargeInfoCompteCNCE() {
    var message = document.forms['remiseForm'].numero.value.trim();
    if (message != '') {

        document.forms['remiseForm'].nom.value = "";
        //document.forms['remiseForm'].agence.value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompteCNCE&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");

                document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                document.forms['remiseForm'].nom.value = obj[0].nomClient;
                document.forms['remiseForm'].escompte.value = obj[0].escompte;

                //Ancienne version

                //                if(obj[0].action == "9") {
                //                     document.write("<H2>Le numéro de compte "+document.forms['remiseForm'].numero.value+" est rejeté pour motif :"+obj[0].messageResult+"</H2>");
                //                }else{
                //                    if(obj[0].actionStatut == "9"){
                //                        document.write("<H2>Le numéro de compte "+document.forms['remiseForm'].numero.value+" est rejeté pour motif :"+obj[0].messageStatutResult+"</H2>");
                //                    }else{
                //                        document.forms['remiseForm'].messageStatut.value=obj[0].messageStatutResult;
                //                        document.forms['remiseForm'].message.value=obj[0].messageResult;
                //                        document.forms['remiseForm'].numero.value=obj[0].numeroCompte;
                //                        document.forms['remiseForm'].nom.value = obj[0].nomClient;
                //                        //document.forms['remiseForm'].agence.value = obj[0].agenceCompte;
                //
                //                        document.forms['remiseForm'].escompte.value = obj[0].escompte;
                //                    }
                //
                //                }



                // handle any errors
            }
        });
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=agenceCompte&message=" + encodeURIComponent(message),
            callback: function (_req) {
                var tmp = _req.responseText;
                var obj = eval("(" + tmp + ")");
                jmaki.publish('/cl/setValues', obj);
                // handle any errors
            }
        });
    }
}


function chargeInfoCompteCORIS() {
        var message = document.forms['remiseForm'].numero.value.trim();
    if (message != '') {

        document.forms['remiseForm'].nom.value = "";
        //document.forms['remiseForm'].agence.value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompteBDK&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");

                document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                document.forms['remiseForm'].nom.value = obj[0].nomClient;
                document.forms['remiseForm'].agence.value = obj[0].agence;

                if (obj[0].action == "9") {
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageResult + "</H2>");
                } else {
                    if (obj[0].actionStatut == "9") {
                        document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageStatutResult + "</H2>");
                    } else {
                        document.forms['remiseForm'].messageStatut.value = obj[0].messageStatutResult;
                        document.forms['remiseForm'].message.value = obj[0].messageResult;
                        document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                        document.forms['remiseForm'].nom.value = obj[0].nomClient;
                        //document.forms['remiseForm'].agence.value = obj[0].agenceCompte;
                        //document.forms['remiseForm'].escompte.value = obj[0].escompte;
                    }

                }



            }
        });
        
         jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=agenceCompteCORIS&message=" + encodeURIComponent(message),
            callback: function (_req) {
                var tmp = _req.responseText;
                var obj = eval("(" + tmp + ")");
                jmaki.publish('/cl/setValues', obj);
                // handle any errors
            }
        });

    }
}

function chargeInfoCompteTPCI() {
    var message = document.forms['remiseForm'].numero.value.trim();
    if (message != '') {

        document.forms['remiseForm'].nom.value = "";
        //document.forms['remiseForm'].agence.value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompteTPCI&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");

                document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                document.forms['remiseForm'].nom.value = obj[0].nomClient;
                document.forms['remiseForm'].agence.value = obj[0].agence;
                document.forms['remiseForm'].escompte.value = obj[0].escompte;

                // handle any errors
            }
        });

    }
}
function chargeInfoCompteNSIA() {
    var message = document.forms['remiseForm'].numero.value.trim();
    if (message != '') {

        document.forms['remiseForm'].nom.value = "";
        //document.forms['remiseForm'].agence.value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompteNSIA&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " n'existe pas dans Amplitude</H2>");
                }
                var obj = eval("(" + tmp + ")");

                document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                document.forms['remiseForm'].nom.value = obj[0].nomClient;
                document.forms['remiseForm'].escompte.value = obj[0].escompte;
                document.forms['remiseForm'].agence.value = obj[0].agence;

                if (obj[0].action == "9") {
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageResult + "</H2>");
                } else {
                    if (obj[0].actionStatut == "9") {
                        document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageStatutResult + "</H2>");
                    } else {
                        document.forms['remiseForm'].messageStatut.value = obj[0].messageStatutResult;
                        document.forms['remiseForm'].message.value = obj[0].messageResult;
                        document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                        document.forms['remiseForm'].nom.value = obj[0].nomClient;
                        //document.forms['remiseForm'].agence.value = obj[0].agenceCompte;

                        document.forms['remiseForm'].escompte.value = obj[0].escompte;
                    }

                }



            }
        });
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=agenceCompteNSIA&message=" + encodeURIComponent(message),
            callback: function (_req) {
                var tmp = _req.responseText;
                var obj = eval("(" + tmp + ")");
                jmaki.publish('/cl/setValues', obj);
                // handle any errors
            }
        });

    }
}

function chargeInfoCompteCBAO() {
    var message = document.forms['remiseForm'].numero.value.trim();
    if (message != '') {

        document.forms['remiseForm'].nom.value = "";
        //document.forms['remiseForm'].agence.value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompteCBAO&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");

                document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                document.forms['remiseForm'].nom.value = obj[0].nomClient;
                document.forms['remiseForm'].escompte.value = obj[0].escompte;
                document.forms['remiseForm'].agence.value = obj[0].agence;

                if (obj[0].action == "9") {
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageResult + "</H2>");
                } else {
                    if (obj[0].actionStatut == "9") {
                        document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageStatutResult + "</H2>");
                    } else {
                        document.forms['remiseForm'].messageStatut.value = obj[0].messageStatutResult;
                        document.forms['remiseForm'].message.value = obj[0].messageResult;
                        document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                        document.forms['remiseForm'].nom.value = obj[0].nomClient;
                        //document.forms['remiseForm'].agence.value = obj[0].agenceCompte;

                        document.forms['remiseForm'].escompte.value = obj[0].escompte;
                    }

                }



            }
        });

    }
}
function chargeInfoCompteBDK() {
    var message = document.forms['remiseForm'].numero.value.trim();
    if (message != '') {

        document.forms['remiseForm'].nom.value = "";
        //document.forms['remiseForm'].agence.value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompteBDK&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");

                document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                document.forms['remiseForm'].nom.value = obj[0].nomClient;
                document.forms['remiseForm'].agence.value = obj[0].agence;

                if (obj[0].action == "9") {
                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageResult + "</H2>");
                } else {
                    if (obj[0].actionStatut == "9") {
                        document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " est rejeté pour motif :" + obj[0].messageStatutResult + "</H2>");
                    } else {
                        document.forms['remiseForm'].messageStatut.value = obj[0].messageStatutResult;
                        document.forms['remiseForm'].message.value = obj[0].messageResult;
                        document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                        document.forms['remiseForm'].nom.value = obj[0].nomClient;
                        //document.forms['remiseForm'].agence.value = obj[0].agenceCompte;
                        //document.forms['remiseForm'].escompte.value = obj[0].escompte;
                    }

                }



            }
        });

    }
}
function chargeInfoCompteECI() {
    var message = document.forms['remiseForm'].numero.value.trim();
    if (message != '') {

        document.forms['remiseForm'].nom.value = "";

        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompteECI&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {

                    document.write("<H2>Le numéro de compte " + document.forms['remiseForm'].numero.value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");

                document.forms['remiseForm'].numero.value = obj[0].numeroCompte;
                document.forms['remiseForm'].nom.value = obj[0].nomClient;
                document.forms['remiseForm'].escompte.value = obj[0].escompte;

       

            }
        });
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=agenceCompteECI&message=" + encodeURIComponent(message),
            callback: function (_req) {
                var tmp = _req.responseText;
                var obj = eval("(" + tmp + ")");

                jmaki.publish('/cl/setValues', obj);



                // handle any errors
            }
        });
    }
}

function recupereInfoVignette(form) {
    var message = form.elements['numero'].value.trim();
    var message1 = form.elements['numeroEndos'].value.trim();
    if (message != '' || message1 != '') {

//        form.elements['nom'].value = "";
//        form.elements['agence'].value = "";
        getVignettes(message, message1);
//        jmaki.doAjax({
//            method: "POST",
//            url: "ControlServlet?action=choixNumeroCheque&message=" + encodeURIComponent(message),
//            callback: function(_req) {
//
//                var tmp = _req.responseText;
//
//                if(tmp=="rien"){
//                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
//                    document.write("<H2>Le numéro de cheque "+form.elements['numero'].value+" n'existe pas dans la base Clearing</H2>");
//                }
//                var obj = eval("(" + tmp + ")");
//
//
//                getVignettes(obj[0].numero);
//            // handle any errors
//            }
//        });
    }
}

function recupereBenef(form) {
    var message = form.elements['numeroCompteBen'].value.trim();
    if (message != '') {

        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompteBen&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + form.elements['numero'].value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");
                //jmaki.publish('/yahoo/autocomplete/setValues', obj);
                //jmaki.publish('/yahoo/autocomplete/setValue', obj);
                //jmaki.log("obj ="+ obj);


                form.elements['numero'].value = obj[0].numero
                form.elements['nom'].value = obj[0].nom;
                form.elements['agence'].value = obj[0].agence;
                //form.elements['nom'].disabled = true;
                form.elements['agence'].disabled = true;

                getVirSiblings(obj[0].numero);
                // handle any errors
            }
        });
    }

}
function recupereInfoCompteBNP(form) {
    var message = form.elements['numero'].value.trim();
    if (message != '') {

        form.elements['nom'].value = "";
        form.elements['agence'].value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompte&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + form.elements['numero'].value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");
                //jmaki.publish('/yahoo/autocomplete/setValues', obj);
                //jmaki.publish('/yahoo/autocomplete/setValue', obj);



                form.elements['numero'].value = obj[0].numero;
                form.elements['nom'].value = obj[0].nom;
                form.elements['agence'].value = obj[0].agence;
                //form.elements['nom'].disabled = true;
                form.elements['agence'].disabled = true;

                getVirSiblingsBNP(obj[0].numero);
                // handle any errors
            }
        });
    }
    console.log(form.elements['action'].value.trim());


}

function recupereInfoCompte(form) {
    var message = form.elements['numero'].value.trim();
    if (message != '') {

        form.elements['nom'].value = "";
        form.elements['agence'].value = "";
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=choixNumeroCompte&message=" + encodeURIComponent(message),
            callback: function (_req) {

                var tmp = _req.responseText;

                if (tmp == "rien") {
                    //alert("Le numéro de compte "+document.forms['remiseForm'].numero.value+" n'existe pas dans la base Clearing");
                    document.write("<H2>Le numéro de compte " + form.elements['numero'].value + " n'existe pas dans la base Clearing</H2>");
                }
                var obj = eval("(" + tmp + ")");
                //jmaki.publish('/yahoo/autocomplete/setValues', obj);
                //jmaki.publish('/yahoo/autocomplete/setValue', obj);



                form.elements['numero'].value = obj[0].numero;
                form.elements['nom'].value = obj[0].nom;
                form.elements['agence'].value = obj[0].agence;
                //form.elements['nom'].disabled = true;
                form.elements['agence'].disabled = true;

                getVirSiblings(obj[0].numero);
                // handle any errors
            }
        });
    }
    console.log(form.elements['action'].value.trim());


}
function refreshSignaturePanel() {
    console.log('refreshSignaturePanel');
    //recuperer l'agence et le numero de compte 
    //faire une requete ajax et rafraichir le panel des images de signatures imageSignDIV
}
function refreshSignature() {

    document.forms['chequeForm'].action.value = "refreshSignature";


    doAjaxSubmitWithResult(document['chequeForm'], undefined, 'ControlServlet');

}

function checkRemiseVierge() {

    document.forms['remiseForm'].valider.disabled = true;

    if ((location.href.match('saisiecheques_esn.jsp') != null) && document['remiseForm'].reference.value.trim() == '') {

    } else if (document['remiseForm'].reference != undefined && document['remiseForm'].reference.value.trim() == '') {
        document['remiseForm'].reference.value = "00";
    }

    if (jmaki.getWidget('agenceCompte') != undefined) {
        document['remiseForm'].agence.value = jmaki.getWidget('agenceCompte').wrapper.comboBoxSelectionValue.value.substring(1);
    }
   
    if (jmaki.getWidget('reference') != undefined) {
        document['remiseForm'].reference.value = jmaki.getWidget('reference').wrapper.comboBoxSelectionValue.value;
    }

    if (document['remiseForm'].message != undefined && document['remiseForm'].message.value.trim() == '') {
        document['remiseForm'].message.value = "OK";
    }

    if (document['remiseForm'].messageStatut != undefined && document['remiseForm'].messageStatut.value.trim() == '') {
        document['remiseForm'].messageStatut.value = "OK";
    }
    doAjaxSubmitWithResult(document['remiseForm'], undefined, 'ControlServlet');

}


function checkRemiseViergeEffets() {

    document.forms['remiseForm'].valider.disabled = true;

    if ((location.href.match('saisiecheques_esn.jsp') != null) && document['remiseForm'].reference.value.trim() == '') {

    } else if (document['remiseForm'].reference != undefined && document['remiseForm'].reference.value.trim() == '') {
        document['remiseForm'].reference.value = "00";
    }

    if (jmaki.getWidget('agenceCompte') != undefined) {
        document['remiseForm'].agence.value = jmaki.getWidget('agenceCompte').wrapper.comboBoxSelectionValue.value.substring(1);
    }
   
    if (jmaki.getWidget('reference') != undefined) {
        document['remiseForm'].reference.value = jmaki.getWidget('reference').wrapper.comboBoxSelectionValue.value;
    }

    if (document['remiseForm'].message != undefined && document['remiseForm'].message.value.trim() == '') {
        document['remiseForm'].message.value = "OK";
    }

    if (document['remiseForm'].messageStatut != undefined && document['remiseForm'].messageStatut.value.trim() == '') {
        document['remiseForm'].messageStatut.value = "OK";
    }
    doAjaxSubmitWithResult(document['remiseForm'], undefined, 'ControlServlet');

}


//page effets avec choix type
function checkRemiseViergeEffetsType() {

    document.forms['remiseForm'].valider.disabled = true;

    if ((location.href.match('saisiecheques_esn.jsp') != null) && document['remiseForm'].reference.value.trim() == '') {

    } else if (document['remiseForm'].reference != undefined && document['remiseForm'].reference.value.trim() == '') {
        document['remiseForm'].reference.value = "00";
    }

    if (jmaki.getWidget('agenceCompte') != undefined) {
        document['remiseForm'].agence.value = jmaki.getWidget('agenceCompte').wrapper.comboBoxSelectionValue.value.substring(1);
    }
   
    if (jmaki.getWidget('reference') != undefined) {
        document['remiseForm'].reference.value = jmaki.getWidget('reference').wrapper.comboBoxSelectionValue.value;
    }

    if (document['remiseForm'].message != undefined && document['remiseForm'].message.value.trim() == '') {
        document['remiseForm'].message.value = "OK";
    }

    if (document['remiseForm'].messageStatut != undefined && document['remiseForm'].messageStatut.value.trim() == '') {
        document['remiseForm'].messageStatut.value = "OK";
    }
    doAjaxSubmitWithResult(document['remiseForm'], undefined, 'ControlServlet');

}

//page correction remises effets

function checkRemiseEffets() {

    //document['remiseForm'].valider.style.display = "none";
    
    
    document.forms['remiseForm'].valider.disabled = true;
    if (document['remiseForm'].reference != undefined && document['remiseForm'].reference.value.trim() == '') {
        document['remiseForm'].reference.value = "00";
    }
    if (jmaki.getWidget('agenceCompte') != undefined) {
        document['remiseForm'].agence.value = jmaki.getWidget('agenceCompte').wrapper.comboBoxSelectionValue.value.substring(1);
    }
    if (jmaki.getWidget('reference') != undefined) {
        document['remiseForm'].reference.value = jmaki.getWidget('reference').wrapper.comboBoxSelectionValue.value;
    }

    if (document['remiseForm'].message != undefined && document['remiseForm'].message.value.trim() == '') {
        document['remiseForm'].message.value = "OK";
    }

    if (document['remiseForm'].messageStatut != undefined && document['remiseForm'].messageStatut.value.trim() == '') {
        document['remiseForm'].messageStatut.value = "OK";
    }
  /// console.log(document['remiseForm'].serializeArray());
   doAjaxSubmitWithResult(document['remiseForm'], undefined, 'ControlServlet');

}





function checkRemise() {

    //document['remiseForm'].valider.style.display = "none";
    document.forms['remiseForm'].valider.disabled = true;
    if (document['remiseForm'].reference != undefined && document['remiseForm'].reference.value.trim() == '') {
        document['remiseForm'].reference.value = "00";
    }

    if (jmaki.getWidget('agenceCompte') != undefined) {
        document['remiseForm'].agence.value = jmaki.getWidget('agenceCompte').wrapper.comboBoxSelectionValue.value.substring(1);
    }
    if (jmaki.getWidget('reference') != undefined) {
        document['remiseForm'].reference.value = jmaki.getWidget('reference').wrapper.comboBoxSelectionValue.value;
    }

    if (document['remiseForm'].message != undefined && document['remiseForm'].message.value.trim() == '') {
        document['remiseForm'].message.value = "OK";
    }

    if (document['remiseForm'].messageStatut != undefined && document['remiseForm'].messageStatut.value.trim() == '') {
        document['remiseForm'].messageStatut.value = "OK";
    }
    doAjaxSubmitWithResult(document['remiseForm'], undefined, 'ControlServlet');

}

function checkCheque() {
    document.forms['chequeForm'].valider.disabled = true;

    //Gestion Date
    if ((jmaki.getWidget('dateemission') != undefined)) {
        var strDate = jmaki.getWidget('dateemission').getValue();

        var year = strDate.substring(0, 4);
        var month = strDate.substring(5, 7);
        var day = strDate.substring(8, 10);

        document['chequeForm'].dateEmis.value = year + "/" + month + "/" + day;

    }
    if ((document['chequeForm'].nomTire != undefined)
            && (document['chequeForm'].nomTire.value.trim() == '')) {
        document['chequeForm'].nomTire.value = "XX";
    }
    if ((location.href.match('saisieremises_bdk.jsp') != null) && (document['chequeForm'].nomTire.value.trim() == '')) {
        document['chequeForm'].nomTire.value = "XX";
    }
    if ((location.href.match('saisieremises_uv') != null) && (document['chequeForm'].codeUV.value.trim() == '')) {
        document['chequeForm'].codeUV.value = "XX";
    }
    if ((location.href.match('saisieremises_uv') != null) && (document['chequeForm'].numeroEndos.value.trim() == '')) {
        document['chequeForm'].numeroEndos.value = "XX";
    }

    //document['chequeForm'].valider.style.display = "none";
    if (document['chequeForm'].clerib.value.trim() == '') {
        document['chequeForm'].clerib.value = "";
    }
    if (document.getElementById('force') != undefined && document.getElementById('force').checked) {
        document['chequeForm'].forcage.value = "ON";
    }
    doAjaxSubmitWithResult(document['chequeForm'], undefined, 'ControlServlet');




}

//page de corrcetion des effets
function checkEffet() {
    document.forms['chequeForm'].valider.disabled = true;

    //Gestion Date
    if ((jmaki.getWidget('dateemission') != undefined)) {
        var strDate = jmaki.getWidget('dateemission').getValue();

        var year = strDate.substring(0, 4);
        var month = strDate.substring(5, 7);
        var day = strDate.substring(8, 10);

        document['chequeForm'].dateEmis.value = year + "/" + month + "/" + day;

    }
    if ((document['chequeForm'].nomTire != undefined)
            && (document['chequeForm'].nomTire.value.trim() == '')) {
        document['chequeForm'].nomTire.value = "XX";
    }
    if ((location.href.match('saisieremises_bdk.jsp') != null) && (document['chequeForm'].nomTire.value.trim() == '')) {
        document['chequeForm'].nomTire.value = "XX";
    }
    if ((location.href.match('saisieremises_uv') != null) && (document['chequeForm'].codeUV.value.trim() == '')) {
        document['chequeForm'].codeUV.value = "XX";
    }
    if ((location.href.match('saisieremises_uv') != null) && (document['chequeForm'].numeroEndos.value.trim() == '')) {
        document['chequeForm'].numeroEndos.value = "XX";
    }

    //document['chequeForm'].valider.style.display = "none";
    if (document['chequeForm'].clerib.value.trim() == '') {
        document['chequeForm'].clerib.value = "";
    }
    if (document.getElementById('force') != undefined && document.getElementById('force').checked) {
        document['chequeForm'].forcage.value = "ON";
    }
    doAjaxSubmitWithResult(document['chequeForm'], undefined, 'ControlServlet');




}


function checkChequeVierge() {

    document.forms['chequeForm'].valider.disabled = true;
    //Gestion Date

    if ((jmaki.getWidget('dateemission') !== undefined)) {
        var strDate = jmaki.getWidget('dateemission').getValue();

        var year = strDate.substring(0, 4);
        var month = strDate.substring(5, 7);
        var day = strDate.substring(8, 10);

        document['chequeForm'].dateEmis.value = year + "/" + month + "/" + day;
    }

    if ((jmaki.getWidget('dateecheance') != undefined)) {
        var strDate = jmaki.getWidget('dateecheance').getValue();

        var year = strDate.substring(0, 4);
        var month = strDate.substring(5, 7);
        var day = strDate.substring(8, 10);

        document['chequeForm'].dateEch.value = year + "/" + month + "/" + day;
    }


    if ((location.href.match('saisiecheques_signatures_ebj') === null || location.href.match('saisiecheques_ebj') === null) &&
            (document['chequeForm'].nomTire != undefined) && (document['chequeForm'].nomTire.value.trim() == '')) {
        document['chequeForm'].nomTire.value = "XX";
    }

    if ((document['chequeForm'].nomTire != undefined)
            && (document['chequeForm'].nomTire.value.trim() == '')) {
        document['chequeForm'].nomTire.value = "XX";
    }
    //document['chequeForm'].valider.style.display = "none";
    if (document['chequeForm'].clerib.value.trim() == '') {
        document['chequeForm'].clerib.value = "";
    }

    if (document.getElementById('force') != undefined && document.getElementById('force').checked) {
        document['chequeForm'].forcage.value = "ON";
    }
    if (document.getElementById('jplus1') != undefined && document.getElementById('jplus1').checked) {
        document['chequeForm'].datvalj1.value = "ON";
    }
    if (document.getElementById('escompte') != undefined && document.getElementById('escompte').checked) {
        document['chequeForm'].escompte.value = "ON";
    }
    if (document.getElementById('garde') != undefined && document.getElementById('garde').checked) {
        document['chequeForm'].garde.value = "ON";
    }
    if (document.getElementById('calcul') != undefined && document.getElementById('calcul').checked) {
        document['chequeForm'].calcul.value = "ON";
    }
    doAjaxSubmitWithResult(document['chequeForm'], undefined, 'ControlServlet');



}

//same et eugene
function checkEffetVierge() {
    
       document.forms['chequeForm'].valider.disabled = true;
    //Gestion Date
    
    //alert('la valeur de numero effet '+document['chequeForm'].numeroEffet.value);
    
    if ((jmaki.getWidget('datecreation') != undefined)) {
        var strDate = jmaki.getWidget('datecreation').getValue();
        
        var year = strDate.substring(0, 4);
        var month = strDate.substring(5, 7);
        var day = strDate.substring(8, 10);

        document['chequeForm'].dateCrea.value = year + "/" + month + "/" + day;
    }

    if ((jmaki.getWidget('dateecheance') != undefined)) {

        var strDate = jmaki.getWidget('dateecheance').getValue();

        var year = strDate.substring(0, 4);
        var month = strDate.substring(5, 7);
        var day = strDate.substring(8, 10);

        document['chequeForm'].dateEch.value = year + "/" + month + "/" + day;
    }
    if ((document['chequeForm'].nomTire != undefined) && (document['chequeForm'].nomTire.value.trim() == '')) {
        document['chequeForm'].nomTire.value = "XX";
    }


    //document['chequeForm'].valider.style.display = "none";
    if (document['chequeForm'].clerib.value.trim() == '') {
        document['chequeForm'].clerib.value = "";
    }

    if (document.getElementById('force') != undefined && document.getElementById('force').checked) {
        document['chequeForm'].forcage.value = "ON";
    }
    if (document.getElementById('escompte') != undefined && document.getElementById('escompte').checked) {
        document['chequeForm'].escompte.value = "ON";
    }
    if (document.getElementById('garde') != undefined && document.getElementById('garde').checked) {
        document['chequeForm'].garde.value = "ON";
    }
    if (document.getElementById('calcul') != undefined && document.getElementById('calcul').checked) {
        document['chequeForm'].calcul.value = "ON";
    }
    doAjaxSubmitWithResult(document['chequeForm'], undefined, 'ControlServlet');



}

function rejete() {

    var tableChequesWidget = jmaki.getWidget('tableCheques');
    var sids = tableChequesWidget.dataTable.getSelectedRows();
    if (sids != "") {
        var idobjet = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData(document['rejetForm'].numcolobjet.value);

        var motifrejet = jmaki.getWidget('comboMotif').wrapper.comboBoxSelectionValue.value;

        if (motifrejet.trim() != "") {

            if (idobjet != undefined) {
                document['rejetForm'].idobjet.value = idobjet;
                document['rejetForm'].motifrejet.value = motifrejet;

                doAjaxSubmit(document['rejetForm'], undefined, 'ControlServlet');
            }

        } else
            alert("Selectionnez un code motif rejet, svp");
    } else
        alert("Selectionnez un ordre, svp");
}
function rejeterSignCheque() {

    var motifrejet = jmaki.getWidget('comboMotif').wrapper.comboBoxSelectionValue.value;

    if (motifrejet.trim() != "") {
        document['rejetForm'].motifrejet.value = motifrejet;
        doAjaxSubmitWithResult(document['rejetForm'], undefined, 'ControlServlet');
    } else
        alert("Selectionnez un code motif rejet, svp");

}
function setFocus() {
    if (document['remiseForm'] == undefined) {
        document['chequeForm'].valider.style.display = "block";
        document['chequeForm'].montantCheque.focus();
    } else {
        document['remiseForm'].valider.style.display = "block";
        document['remiseForm'].numero.focus();
    }

}

function setFocusCheque() {
    if (document['chequeForm'] == undefined) {
        document['remiseForm'].valider.style.display = "block";
        document['remiseForm'].numero.focus();
    } else {
        document['chequeForm'].valider.style.display = "block";
        document['chequeForm'].montantCheque.focus();
    }

}

function supprime() {
    console.log("supprime");

    var tableChequesWidget = jmaki.getWidget('tableCheques');

    var sids = tableChequesWidget.dataTable.getSelectedRows();
    if (sids != "") {
        var idobjet = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData(document['outilsForm'].numcolobjet.value);
        console.log("idobjet supprime " + idobjet);

        if (idobjet != undefined) {
            document['outilsForm'].idobjet.value = idobjet;
            document['outilsForm'].action.value = "supprime";
            doAjaxSubmit(document['outilsForm'], undefined, 'ControlServlet');
        }
    } else
        alert("Selectionnez une ligne, svp");
}
function dataTableAsTSV() {

    var tableChequesWidget = jmaki.getWidget('tableCheques');
    var i, j, oData, newWin = window.open(),
            aRecs = tableChequesWidget.dataTable.getRecordSet().getRecords(),
            aCols = tableChequesWidget.dataTable.getColumnSet().keys;

    newWin.document.write("<pre>");

    for (i = 0; i < aRecs.length; i++) {
        oData = aRecs[i].getData();

        for (j = 0; j < aCols.length; j++) {
            newWin.document.write(oData[aCols[j].key] + "\t");

        }
        newWin.document.write("\n");

    }

    newWin.document.write("</pre>");
    newWin.document.close();
}

function exportToCSV() {
    var tableDataString = "";
    var tableChequesWidget = jmaki.getWidget('tableCheques');
    var i, j, k, oData;
    var aRecs = tableChequesWidget.dataTable.getRecordSet().getRecords(), aCols = tableChequesWidget.dataTable.getColumnSet().keys;
    for (i = 0; i < aCols.length; i++) {
        tableDataString += aCols[i].label.replace(/(&nbsp;)*/g, "").replace(/<br[^>]*>/g, " ") + (i < aCols.length - 1 ? ";" : "");
    }
    tableDataString += "\n";
    for (j = 0; j < aRecs.length; j++) {
        oData = aRecs[j].getData();
        for (k = 0; k < aCols.length; k++) {
            tableDataString += ("" + (oData[aCols[k].key] + "") + "" + (k < aCols.length - 1 ? ";" : ""));
        }
        tableDataString += "\n";
    }
    var randomnumber = Math.floor(Math.random() * Math.pow(10, 6) + Math.pow(10, 5));
    var thisForm = document.createElement('form');
    thisForm.style.display = 'none';
    document.body.appendChild(thisForm);
    var fileName = document.createElement('input');
    fileName.type = 'hidden';
    fileName.name = 'fileName';
    fileName.value = 'Export-' + randomnumber;
    thisForm.appendChild(fileName);
    var dataTable = document.createElement('input');
    dataTable.type = 'hidden';
    dataTable.name = 'tableData';
    dataTable.value = tableDataString;
    thisForm.appendChild(dataTable);

    thisForm.method = 'POST';
    thisForm.action = 'csvDownload.jsp';
    thisForm.submit();
    document.body.removeChild(thisForm);
}

function exportToEXCEL()
{

    var tab_text = "<table border='2px'><tr bgcolor='#87AFC6'><td>";

    var textRange;
    var j = 0;
    var tableChequesWidget = jmaki.getWidget('tableCheques');

    table = tableChequesWidget.dataTable; // id of table

    var i, j, k, oData;

    var includeLastColumn = true;

    var aRecs = table.getRecordSet().getRecords(), aCols = table.getColumnSet().keys;

    var aColsLength = aCols.length;

    var aRecsLength = aRecs.length;

    for (i = 0; i < aColsLength; i++)

    {

        tab_text += aCols[i].label.replace(/(&nbsp;)*/g, "").replace(/<br[^>]*>/g, " ") + (i < aColsLength ? "</td><td>" : "");

    }

    tab_text += "</td></tr><tr><td>";

    for (j = 0; j < aRecsLength; j++) {

        oData = aRecs[j].getData();

        for (k = 0; k < aColsLength; k++) {

            tab_text += (oData[aCols[k].key] + (k < aColsLength ? "</td><td>" : ""));

        }

        tab_text += "</td></tr><tr><td>";

    }

    tab_text = tab_text + "</td></tr></table>";

    tab_text = tab_text.replace(/<A[^>]*>|<\/A>/g, "");//remove if u want links in your table

    tab_text = tab_text.replace(/<img[^>]*>/gi, ""); // remove if u want images in your table

    tab_text = tab_text.replace(/<input[^>]*>|<\/input>/gi, ""); // reomves input params

    var thisForm = document.createElement('form');
    var randomnumber = Math.floor(Math.random() * Math.pow(10, 6) + Math.pow(10, 5));
    thisForm.style.display = 'none';

    document.body.appendChild(thisForm);

    var fileName = document.createElement('input');

    fileName.type = 'hidden';

    fileName.name = 'fileName';

    fileName.value = 'Export-' + randomnumber;

    thisForm.appendChild(fileName);

    var dataTable = document.createElement('input');

    dataTable.type = 'hidden';

    dataTable.name = 'tableData';

    dataTable.value = tab_text;

    thisForm.appendChild(dataTable);

    thisForm.method = 'POST';

    thisForm.action = 'excelDownload.jsp';

    thisForm.submit();

    document.body.removeChild(thisForm);

}
function annule() {

    var tableChequesWidget = jmaki.getWidget('tableCheques');
    var sids = tableChequesWidget.dataTable.getSelectedRows();
    if (sids != "") {
        var idobjet = tableChequesWidget.dataTable.getRecordSet().getRecord(sids[0]).getData(document['outilsForm'].numcolobjet.value);

        if (idobjet != undefined) {
            document['outilsForm'].idobjet.value = idobjet;
            document['outilsForm'].action.value = 'annule';
            doAjaxSubmit(document['outilsForm'], undefined, 'ControlServlet');
        }
    } else
        alert("Selectionnez une ligne, svp");
}

function startValidation() {
    document['choixUtilisateur'].utilisateur.value = jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value;
    doAjaxSubmitWithResult(document['choixUtilisateur'], undefined, 'ControlServlet');

}

function startValidationEffet() {
    document['choixUtilisateurEffet'].utilisateur.value = jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value;
    doAjaxSubmitWithResult(document['choixUtilisateurEffet'], undefined, 'ControlServlet');
}

function startValidationAgence() {
    document['choixAgence'].agenceDepot.value = jmaki.getWidget('idagence').wrapper.comboBoxSelectionValue.value.substring(1);
    doAjaxSubmitWithResult(document['choixAgence'], undefined, 'ControlServlet');

}

function startValidationEtablissement() {
    document['choixEtablissement'].etablissementChoisi.value = jmaki.getWidget('idetablissement').wrapper.comboBoxSelectionValue.value;
    doAjaxSubmitWithResult(document['choixEtablissement'], undefined, 'ControlServlet');

}
function filterDataTable() {

    var strDate = jmaki.getWidget('dateparam1').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    document['choixFiltre'].filtreChoisi.value = '"' + document['choixFiltre'].param1.value + '"' + '=' + "'" + year + "/" + month + "/" + day + "'";
    return true;

}

function filterDataTable2() {


    var strDate = jmaki.getWidget('dateparam1').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    document['choixFiltre'].filtreChoisi.value = '"' + document['choixFiltre'].param1.value + '"' + '>=' + "'" + year + "/" + month + "/" + day + "'";
    strDate = jmaki.getWidget('dateparam2').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    document['choixFiltre'].filtreChoisi2.value = '"' + document['choixFiltre'].param2.value + '"' + '<=' + "'" + year + "/" + month + "/" + day + "'";
    return true;

}

function startValidationParBanque() {
    document['choixCodeBanques'].codeBanque.value = jmaki.getWidget('banques').wrapper.comboBoxSelectionValue.value;
    doAjaxSubmitWithResult(document['choixCodeBanques'], undefined, 'ControlServlet');

}
function startValidationParAgence() {
    document['choixCodeAgences'].codeAgence.value = jmaki.getWidget('agences').wrapper.comboBoxSelectionValue.value.substring(1);
    doAjaxSubmitWithResult(document['choixCodeAgences'], undefined, 'ControlServlet');

}


function validerVirement() {
    if (confirm('Voulez vous valider ce virement?')) {
        document['detailForm'].message.value = "UPDATE VIREMENTS SET ETAT=50 WHERE IDVIREMENT=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "50";
        document['detailForm'].primaryClause.value = "IDVIREMENT=" + document['detailForm'].idObjet.value;
        document['detailForm'].remarques.value = "XX"
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

function validerFichierVirement() {
    if (confirm('Voulez vous valider ce fichier de virement?')) {
        document['detailForm'].message.value = "UPDATE VIREMENTS SET ETAT=50 WHERE ETAT=30 AND REMISE=" + document['detailForm'].curRemise.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "50";
        document['detailForm'].primaryClause.value = "REMISE=" + document['detailForm'].curRemise.value;
        document['detailForm'].remarques.value = "XX"
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

function validerEffet() {
    if (confirm('Voulez vous valider cet effet?')) {
        document['detailForm'].message.value = "UPDATE EFFETS SET ETAT=50 WHERE IDEFFET=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "50";
        document['detailForm'].primaryClause.value = "IDEFFET=" + document['detailForm'].idObjet.value;
        document['detailForm'].remarques.value = "XX"
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

function validerRemiseBatchLite() {
    if (confirm('Voulez vous valider cette remise?')) {
        document['detailForm'].message.value = "UPDATE REMISES SET ETAT=30 WHERE IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "17";
        document['detailForm'].newvalue.value = "30";
        document['detailForm'].primaryClause.value = "IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].remarques.value = "XX"
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

//CBAO
function validerRemiseCBAO() {
    if (confirm('Voulez vous valider cette remise?')) {
        document['detailForm'].message.value = "UPDATE REMISES SET ETAT=35 WHERE IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "35";
        document['detailForm'].primaryClause.value = "IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].remarques.value = "XX"
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

//Ecobank
function validerRemise() {
    if (confirm('Voulez vous valider cette remise?')) {
        document['detailForm'].message.value = "UPDATE REMISES SET ETAT=50 WHERE IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "50";
        document['detailForm'].primaryClause.value = "IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].remarques.value = "XX"
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}


function validerRemiseEffet() {
    if (confirm('Voulez vous valider cette remise?')) {
        document['detailForm'].message.value = "UPDATE REMISESEFFETS SET ETAT=35 WHERE IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "35";
        document['detailForm'].primaryClause.value = "IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].remarques.value = "XX"
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}


function rejeterRemiseEffet() {
    var result = prompt('Voulez vous rejeter cette remise?');
    if (result != null && result.length > 0) {
        document['detailForm'].remarques.value = result;
        document['detailForm'].message.value = "UPDATE REMISESEFFETS SET ETAT=20 WHERE IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "20";
        document['detailForm'].primaryClause.value = "IDREMISE=" + document['detailForm'].idObjet.value;
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

function validerRemiseBorne() {
    if (confirm('Voulez vous valider cette remise?')) {
        document['detailForm'].message.value = "UPDATE REMISES SET ETAT=" + document['detailForm'].newvalue.value + "WHERE IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "16";
        document['detailForm'].newvalue.value = "50";
        document['detailForm'].primaryClause.value = "IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].remarques.value = "XX"
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

function verifierVignette() {

    if (document['detailForm'].numeroEndos != undefined && document['detailForm'].numeroEndos.value.trim() == '') {
        document['detailForm'].numeroEndos.value = "XX";
    }
    if (document['detailForm'].codeVignette != undefined && document['detailForm'].codeVignette.value.trim() == '') {
        document['detailForm'].codeVignette.value = "XX";
    }
    doAjaxSubmitVignette(document['detailForm'], undefined, 'ControlServlet');

}
function confirmerVignette() {
    document['confirmForm'].numeroEndos.value = document['detailForm'].numeroEndos.value;
    document['confirmForm'].codeVignette.value = document['detailForm'].codeVignette.value;
    doAjaxSubmitWithResult(document['confirmForm'], undefined, 'ControlServlet');


}

function rejeterRemise() {
    var result = prompt('Voulez vous rejeter cette remise?');
    if (result != null && result.length > 0) {
        document['detailForm'].remarques.value = result;
        document['detailForm'].message.value = "UPDATE REMISES SET ETAT=20 WHERE IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "20";
        document['detailForm'].primaryClause.value = "IDREMISE=" + document['detailForm'].idObjet.value;
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}
function rejeterRemiseBatchLite() {
    var result = prompt('Voulez vous rejeter cette remise?');
    if (result != null && result.length > 0) {
        document['detailForm'].remarques.value = result;
        document['detailForm'].message.value = "UPDATE REMISES SET ETAT=20 WHERE IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "17";
        document['detailForm'].newvalue.value = "20";
        document['detailForm'].primaryClause.value = "IDREMISE=" + document['detailForm'].idObjet.value;
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}
function passerRemise() {
    var result = prompt('Voulez vous passer cette remise?');
    if (result != null && result.length > 0) {
        document['detailForm'].remarques.value = result;


        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

//function rejeterRemiseLite(){
//    var result = prompt('Voulez vous rejeter cet ordre?');
//    if (result != null && result.length >0) {
//                    document['outilsForm'].action.value ='rejectDocBtn';
//                    document['outilsForm'].remarques.value = result;
//                    doAjaxSubmitWithResult(document['outilsForm'],undefined,'ControlServlet');
//                }
//}

function rejeterRemiseLite() {

    var motifrejet = jmaki.getWidget('comboMotif').wrapper.comboBoxSelectionValue.value;

    if (motifrejet.trim() != "") {
        document['rejetForm'].motifrejet.value = motifrejet;

        document['rejetForm'].remarques.value = jmaki.getWidget('comboMotif').getValue();
        doAjaxSubmitWithResult(document['rejetForm'], undefined, 'ControlServlet');
    } else
        alert("Selectionnez un code motif rejet, svp");

}
function rejeterVirement() {
    var result = prompt('Voulez vous rejeter ce virement?');
    if (result != null && result.length > 0) {
        document['detailForm'].remarques.value = result;
        document['detailForm'].message.value = "UPDATE VIREMENTS SET ETAT=20 WHERE IDVIREMENT=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "20";
        document['detailForm'].primaryClause.value = "IDVIREMENT=" + document['detailForm'].idObjet.value;
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

function rejeterEffet() {
    var result = prompt('Voulez vous rejeter cet effet?');
    if (result != null && result.length > 0) {
        document['detailForm'].remarques.value = result;
        document['detailForm'].message.value = "UPDATE EFFETS SET ETAT=20 WHERE IDEFFET=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "30";
        document['detailForm'].newvalue.value = "20";
        document['detailForm'].primaryClause.value = "IDEFFET=" + document['detailForm'].idObjet.value;
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

function rejeterRemiseBorne() {
    var result = prompt('Voulez vous rejeter cette remise?');
    if (result != null && result.length > 0) {
        document['detailForm'].remarques.value = result;
        document['detailForm'].message.value = "UPDATE REMISES SET ETAT=20 WHERE IDREMISE=" + document['detailForm'].idObjet.value;
        document['detailForm'].oldvalue.value = "16";
        document['detailForm'].newvalue.value = "20";
        document['detailForm'].primaryClause.value = "IDREMISE=" + document['detailForm'].idObjet.value;
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

function validerSignCheque() {
    if (confirm('Voulez vous valider la signature de ce cheque?')) {
        document['detailForm'].action.value = "validerSignCheque";
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}
function passerSignCheque() {
    if (confirm('Voulez vous passer ce cheque?')) {
        document['detailForm'].action.value = "passerSignCheque";
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }
}
function validerImageCheque() {
    if (confirm("Voulez vous valider ce cheque?")) {
        document['detailForm'].action.value = "validerImageCheque";
        doAjaxSubmitWithResult(document['detailForm'], undefined, 'ControlServlet');
    }

}

function resolveQuotidienChequesLite() {
    var strDate = jmaki.getWidget('dateSaisie').getValue();
    //alert(strDate);
    if (strDate !== undefined && strDate !== null) {
        var year = strDate.substring(0, 4);
        var month = strDate.substring(5, 7);
        var day = strDate.substring(8, 10);

//        var dateDebChosen = day + "/" + month + "/" + year;
        var dateDebChosen = year + "/" + month + "/" + day;
        document['printReport'].dateChoisie.value = dateDebChosen;
    }


    if (document.getElementById('orderbybanque').checked || document.getElementById('orderbybanquetire').checked) {
        document['printReport'].requete.value = document['printReport'].query.value + " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTCHEQUE)";
    } else {
        document['printReport'].requete.value = document['printReport'].query.value + " ORDER BY C1.REMISE,TO_NUMBER(C1.MONTANTCHEQUE)";
    }
    if (document['printReport'].nomrapport.value === 'Cheques_clients_remises_signe') {
        document['printReport'].requete.value = document['printReport'].query2.value;
    }
    return true;
}

function resolveSyntheses() {
    //Gestion Date
    var strDate = jmaki.getWidget('dateCompensation').getValue();
    //alert(strDate);
    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateDebChosen = day + "/" + month + "/" + year;
    document['paramrapport'].dateChoisie.value = dateDebChosen;
    strDate = jmaki.getWidget('dateCompensationPrec').getValue();
    //alert(strDate);
    year = strDate.substring(0, 4);
    month = strDate.substring(5, 7);
    day = strDate.substring(8, 10);

    dateDebChosen = day + "/" + month + "/" + year;
    document['paramrapport'].datePrecChoisie.value = dateDebChosen;

    return true;

}
function resolveEscompte() {

    //Gestion Date   console.log("Entréee dateFinEscompte");
    var strDateDebut = jmaki.getWidget('dateDebutEscompte').getValue();
    var strDateFin = jmaki.getWidget('dateFinEscompte').getValue();
    if (strDateFin < strDateDebut) {
        alert("La date de fin d'escompte est inférieure à la  date de debut d'escompte");
        return false;
    } else {

        var year = strDateFin.substring(0, 4);
        var month = strDateFin.substring(5, 7);
        var day = strDateFin.substring(8, 10);
        var dateFinEscompte = year + "/" + month + "/" + day;

        document.forms['creationCompteClientNSIA'].dateFinEscompte.value = dateFinEscompte;

        year = strDateDebut.substring(0, 4);
        month = strDateDebut.substring(5, 7);
        day = strDateDebut.substring(8, 10);
        var dateDebutEscompte = year + "/" + month + "/" + day;
        document.forms['creationCompteClientNSIA'].dateDebutEscompte.value = dateDebutEscompte;

        return true;
    }



}
function resolveActivite() {
    //Gestion Date
    var strDate = jmaki.getWidget('dateCompensation').getValue();
    //alert(strDate);
    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateDebChosen = day + "/" + month + "/" + year;
    document['paramrapport'].dateChoisie.value = dateDebChosen;

    return true;

}

function resolveQuotidien() {
    if (document.getElementById('orderbybanque').checked) {
        document['printReport'].requete.value = document['printReport'].query.value + " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTCHEQUE)";
    } else {
        document['printReport'].requete.value = document['printReport'].query.value + " ORDER BY C1.REMISE,TO_NUMBER(C1.MONTANTCHEQUE)";
    }
    if (document['printReport'].nomrapport.value === 'Cheques_clients_remises_signe') {
        document['printReport'].requete.value = document['printReport'].query2.value;
    }
    if (document['printReport'].nomrapport.value === 'Cheques_clients_remises_signe_dec') {
        document['printReport'].requete.value = document['printReport'].query2.value;
    }
    return true;
}



function  resolveQuotidieneffets() {
    if (document.getElementById('orderbybanque').checked) {
        document['printReport'].requete.value = document['printReport'].query.value + " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANT_EFFET)";
    } else {
        document['printReport'].requete.value = document['printReport'].query.value + " ORDER BY C1.REMISE,TO_NUMBER(C1.MONTANT_EFFET)";
    }
    return true;
}




function resolve() {

    var montantClause = "";
    var userClause = "";
    var machineClause = "";
    var compteClause = "";
    var dateClause = "";
    var orderByClause = "";



    if (document['paramrapport'].montantMin.checked == false) {
        document['paramrapport'].montantMinimum.value = "0";
    }
    if (document['paramrapport'].cptcre.checked == false) {
        document['paramrapport'].cptCrediteur.value = "defaut";
    }
    if (document['paramrapport'].cptdeb.checked == false) {
        document['paramrapport'].cptDebiteur.value = "defaut";
    }
    if (document['paramrapport'].utilisateur.checked == false) {
        jmaki.getWidget('user').wrapper.setValue("TOUS");
        jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value = "%";
    }
    if (document['paramrapport'].machine.checked == false) {
        jmaki.getWidget('machinescan').wrapper.setValue("TOUS");
        jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value = "%";
    }

    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTCHEQUE)";
            //   orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE,TO_NUMBER(C1.MONTANTCHEQUE)";
                //  orderByClause = " ORDER BY C1.NUMEROCOMPTE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.COMPTEREMETTANT,TO_NUMBER(C1.MONTANTCHEQUE)";
                //     orderByClause = " ORDER BY C1.COMPTEREMETTANT,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[2].checked) {
                orderByClause = " ORDER BY C1.REMISE,TO_NUMBER(C1.MONTANTCHEQUE)";
                //     orderByClause = " ORDER BY C1.REMISE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            }

        }
    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTPRELEVEMENT)";
            //   orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,TO_NUMBER(C1.MONTANTPRELEVEMENT)";
                //  orderByClause = " ORDER BY C1.NUMEROCOMPTE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,TO_NUMBER(C1.MONTANTPRELEVEMENT)";
                //     orderByClause = " ORDER BY C1.COMPTEREMETTANT,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            }

        }
    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTVIREMENT)";
            //    orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANTVIREMENT,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,TO_NUMBER(C1.MONTANTVIREMENT)";
                //   orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,convert(C1.MONTANTVIREMENT,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,TO_NUMBER(C1.MONTANTVIREMENT)";
                //       orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,convert(C1.MONTANTVIREMENT,SIGNED)" ;
            }

        }

    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANT_EFFET)";
            //   orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANT_EFFET,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,TO_NUMBER(C1.MONTANT_EFFET)";
                // orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,convert(C1.MONTANT_EFFET,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,TO_NUMBER(C1.MONTANT_EFFET)";
                //  orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,convert(C1.MONTANT_EFFET,SIGNED)" ;
            }

        }

    //Gestion Date
    var strDate = jmaki.getWidget('datedebut').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateDebChosen = year + "/" + month + "/" + day;

    strDate = jmaki.getWidget('datefin').getValue();

    year = strDate.substring(0, 4);
    month = strDate.substring(5, 7);
    day = strDate.substring(8, 10);

    var dateFinChosen = year + "/" + month + "/" + day;

    //Gestion Sous regionale
    var srgdeb = jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value;
    var srgcre = jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value;
    srgdeb = (srgdeb.lastIndexOf('%') === 3) ? 'not' : '';
    srgcre = (srgcre.lastIndexOf('%') === 3) ? 'not' : '';



    if (document['paramrapport'].traitement.checked) {
        dateClause = "  and C1.datetraitement >= '" + dateDebChosen + "'  and C1.datetraitement  <='" + dateFinChosen + "'";
        document['paramrapport'].interval.value = "Date de Traitement du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramrapport'].compensation.checked) {
        dateClause = dateClause + "  and C1.datecompensation >= '" + dateDebChosen + "'  and C1.datecompensation  <='" + dateFinChosen + "'";
        document['paramrapport'].interval.value = "Date de Compensation du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramrapport'].utilisateur.checked) {
        userClause = userClause + "  and C1.codeutilisateur like '" + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value + "'";
        document['paramrapport'].interval.value += " | Saisie faite par " + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramrapport'].machine.checked) {
        machineClause = machineClause + "  and C1.machinescan like '" + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value + "'";
        document['paramrapport'].interval.value += " | Saisie faite sur " + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramrapport'].cptcre.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and C1.compteremettant like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_BENEFICIAIRE like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_BENEFICIAIRE like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_BENEFICIAIRE like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        document['paramrapport'].interval.value += " | Compte Crediteur " + document['paramrapport'].cptCrediteur.value;
    }

    if (document['paramrapport'].cptdeb.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and C1.numerocompte like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_TIRE like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_TIRE like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_TIRE like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        document['paramrapport'].interval.value += " | Compte Debiteur " + document['paramrapport'].cptDebiteur.value;
    }

    var query = "";
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTCHEQUE) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.IDCHEQUE,C1.REMISE,C1.REFREMISE,C1.DEVISE,C1.NUMEROCHEQUE,C1.NUMEROCOMPTE,C1.NOMBENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.COMPTEREMETTANT,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantcheque ,C1.MOTIFREJET" + ((document['paramrapport'].parCompte[1].checked) ? ',A1.NOM' : '')
                + " from all_cheques C1,all_cheques C2,"
                + " Banques B1,Banques B2" + ((document['paramrapport'].parCompte[1].checked) ? ',Comptes A1' : '')
                + " where C1.IDCHEQUE=C2.IDCHEQUE  AND C1.banqueremettant=B1.codebanque " + ((document['paramrapport'].parCompte[1].checked) ? ' AND C1.NUMEROCOMPTE=A1.NUMERO' : '')
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + userClause + machineClause + compteClause + montantClause + orderByClause;
    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTVIREMENT) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.idvirement,C1.DEVISE,C1.NUMEROVIREMENT,C1.NUMEROCOMPTE_BENEFICIAIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.NUMEROCOMPTE_TIRE,C1.NOM_TIRE,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantvirement ,C1.MOTIFREJET,C1.LIBELLE"
                + " from all_virements C1,all_virements C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDVIREMENT=C2.IDVIREMENT  AND C1.banqueremettant=B1.codebanque"
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + userClause + compteClause + montantClause + orderByClause;
    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANT_EFFET) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.ideffet,C1.DEVISE,C1.NUMEROEFFET,C1.NUMEROCOMPTE_BENEFICIAIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.NUMEROCOMPTE_TIRE,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montant_effet ,C1.MOTIFREJET"
                + " from all_effets C1,all_effets C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDEFFET=C2.IDEFFET  AND C1.banqueremettant=B1.codebanque"
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + userClause + compteClause + montantClause + orderByClause;
    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTPRELEVEMENT) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.idprelevement,C1.DEVISE,C1.NUMEROPRELEVEMENT,C1.NUMEROCOMPTE_BENEFICIAIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.NUMEROCOMPTE_TIRE,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantprelevement ,C1.MOTIFREJET"
                + " from all_prelevements C1,all_prelevements C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDPRELEVEMENT=C2.IDPRELEVEMENT  AND C1.banqueremettant=B1.codebanque"
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + userClause + compteClause + montantClause + orderByClause;
    }
    //alert(query);
    document['paramrapport'].requete.value = query;


    if (document['paramrapport'].parCompte[1].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_deb";
    } else if (document['paramrapport'].parCompte[0].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_cred";
    } else if (document['paramrapport'].parCompte[2].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_remises";
    } else {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value;
    }
    if (!document['paramrapport'].avecDetail.checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_sans_details";
    }
    return true;

}


function chargeTous() {
    jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value = '%%';
    jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value = '%%';
    jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value = '%%';
    jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value = '%%';
    jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value = '%%';
    jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value = 'Cheques';
}

function addSetQuery() {


    if (document['paramUpdate'].query.value.trim() == "") {
        document['param'].rootset.value = "UPDATE " + jmaki.getWidget('tables').wrapper.comboBoxSelectionValue.value + " SET " + jmaki.getWidget('colSet').wrapper.comboBoxSelectionValue.value
                + " = " + document['param'].newvaleur.value.trim();
        document['paramUpdate'].query.value = document['param'].rootset.value;
    } else {
        document['param'].otherset.value = document['param'].otherset.value + "," +
                jmaki.getWidget('colSet').wrapper.comboBoxSelectionValue.value
                + " = " + document['param'].newvaleur.value.trim();
        document['paramUpdate'].query.value = document['param'].rootset.value
                + document['param'].otherset.value
                + ((document['param'].where.value.trim() == "") ? "" : " WHERE " + document['param'].where.value);
    }

}

function addWhereQuery() {


    if (document['paramUpdate'].query.value.trim() != "") {
        if (document['param'].where.value.trim() == "") {
        }
        document['param'].where.value = document['param'].where.value + jmaki.getWidget('colWhere').wrapper.comboBoxSelectionValue.value
                + " " + jmaki.getWidget('operateur').wrapper.comboBoxSelectionValue.value
                + " (" + document['param'].valeur.value.trim()
                + ")  " + jmaki.getWidget('conjonction').wrapper.comboBoxSelectionValue.value
                + " \n";
        document['paramUpdate'].query.value = document['param'].rootset.value
                + document['param'].otherset.value
                + ((document['param'].from == undefined) ? "" : document['param'].from.value)
                + " WHERE " + document['param'].where.value;
    }

}

function addSelectQuery() {


    if (document['paramUpdate'].query.value.trim() == "") {
        document['param'].rootset.value = "SELECT " + jmaki.getWidget('colSet').wrapper.comboBoxSelectionValue.value
                + ' AS "' + ((document['param'].newvaleur.value.trim() == "") ?
                        jmaki.getWidget('colSet').wrapper.comboBoxSelectionValue.value : document['param'].newvaleur.value.trim()) + '"';
        document['param'].from.value = " FROM " + jmaki.getWidget('tables').wrapper.comboBoxSelectionValue.value;
        document['paramUpdate'].query.value = document['param'].rootset.value + document['param'].from.value;
    } else {
        document['param'].otherset.value = document['param'].otherset.value + "," +
                jmaki.getWidget('colSet').wrapper.comboBoxSelectionValue.value
                + ' AS "' + ((document['param'].newvaleur.value.trim() == "") ?
                        jmaki.getWidget('colSet').wrapper.comboBoxSelectionValue.value : document['param'].newvaleur.value.trim()) + '"';
        document['paramUpdate'].query.value = document['param'].rootset.value
                + document['param'].otherset.value
                + document['param'].from.value
                + ((document['param'].where.value.trim() == "") ? "" : " WHERE " + document['param'].where.value);
    }

}

function addUrl() {
    var message = jmaki.getWidget('type').wrapper.comboBoxSelectionValue.value;
    var query = document['paramUpdate'].query.value;
    var action = document.getElementById("actionUrl");
    var page = document.getElementById("page");
    if (message == 'dataTable.jsp') {
        if (query.trim() == "")
            return false;
        document.forms['creationTacheForm'].url.value = message + "?requete=" + query;

    } else if (message == 'dataTableMasterDetail.jsp') {
        if (query.trim() == "")
            return false;
        document.forms['creationTacheForm'].url.value = message + "?requete=" + query;

    } else if (message == 'ControlServlet') {
        if (action.trim() == "")
            return false;
        document.forms['creationTacheForm'].url.value = message + "?action=" + action.value;

    } else if (message == 'PAGE') {
        if (page.trim() == "")
            return false;
        document.forms['creationTacheForm'].url.value = page.value;

    }
    return true;
}

function addHidden() {
    var parent = jmaki.getWidget('tacheParent').wrapper.comboBoxSelectionValue.value;
    var enfant = jmaki.getWidget('tacheEnfant').wrapper.comboBoxSelectionValue.value;


    if (parent.trim() == "")
        return false;
    document.forms['creationMenuForm'].tacheParent.value = parent;
    if (enfant.trim() == "")
        return false;
    document.forms['creationMenuForm'].tacheEnfant.value = enfant;

    return true;
}

function addHiddenCompte() {
    var agence = jmaki.getWidget('agenceCompte').wrapper.comboBoxSelectionValue.value;

    if (agence.trim() == "")
        return false;
    document.forms['creationCompteClient'].agenceCompte.value = agence;
    return true;
}

function addHiddenVirement() {
    var bancre = jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value;
    var agecre = '' + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1);
    var typevirement = '' + jmaki.getWidget('typevirement').wrapper.comboBoxSelectionValue.value;

    document.forms['creationVirementClient'].bancre.value = bancre;
    document.forms['creationVirementClient'].agecre.value = agecre;
    document.forms['creationVirementClient'].typevirement.value = typevirement;
    document.forms['creationVirementClient'].montant.value = document.forms['creationVirementClient'].montant.value.replace(/\s/g, '');

    return true;
}
function addHiddenVirementBNP() {
    var bancre = jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value;
    var agecre = '' + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1);
    var typevirement = '' + jmaki.getWidget('typevirement').wrapper.comboBoxSelectionValue.value;

    document.forms['creationVirementClientBNP'].bancre.value = bancre;
    document.forms['creationVirementClientBNP'].agecre.value = agecre;
    document.forms['creationVirementClientBNP'].typevirement.value = typevirement;
    document.forms['creationVirementClientBNP'].montant.value = document.forms['creationVirementClientBNP'].montant.value.replace(/\s/g, '');

    return true;
}
function changerBenef() {
    jmaki.getWidget('idbancre').wrapper.setValue("");
    jmaki.getWidget('idagecre').wrapper.setValue("");
    document.forms['creationVirementClient'].libelleVir.value = "";
    document.forms['creationVirementClient'].numeroVir.value = "";
    document.forms['creationVirementClient'].addressBen.value = "";
    document.forms['creationVirementClient'].nomBen.value = "";
    document.forms['creationVirementClient'].montant.value = "";
    document.forms['creationVirementClient'].numeroBen.value = "";

    return true;
}

function addHiddenProfils() {
    document.forms['creationProfils'].poids.value = jmaki.getWidget('idpoids').wrapper.comboBoxSelectionValue.value.substring(1);

    return true;
}

function addHiddenUser() {
    if (jmaki.getWidget('idagence') != undefined)
    {
        document.forms['inscription'].agence.value = jmaki.getWidget('idagence').wrapper.comboBoxSelectionValue.value.substring(1);
    }

    if (jmaki.getWidget('idetablissement') != undefined) {
        document.forms['inscription'].agence.value = jmaki.getWidget('idetablissement').wrapper.comboBoxSelectionValue.value;
    }

    document.forms['inscription'].poids.value = jmaki.getWidget('idpoids').wrapper.comboBoxSelectionValue.value.substring(1);
    return true;
}

function addHiddenCompteLite() {

    if (jmaki.getWidget('idetablissement') != undefined) {
        document.forms['creationCompteClient'].adresse.value = jmaki.getWidget('idetablissement').wrapper.comboBoxSelectionValue.value;
    }

    return true;
}

function addHiddenEffet() {
    var bandeb = jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value;
    var agedeb = jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1);

    //Gestion Date
    var strDate = jmaki.getWidget('dateecheance').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateecheance = year + "/" + month + "/" + day;

    strDate = jmaki.getWidget('datecreation').getValue();

    year = strDate.substring(0, 4);
    month = strDate.substring(5, 7);
    day = strDate.substring(8, 10);

    var datecreation = year + "/" + month + "/" + day;

    document.forms['creationEffetClient'].dateecheance.value = dateecheance;
    document.forms['creationEffetClient'].datecreation.value = datecreation;

    document.forms['creationEffetClient'].bandeb.value = bandeb;
    document.forms['creationEffetClient'].agedeb.value = agedeb;

    return true;
}
function addHiddenPrelevement() {
    var bandeb = jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value;
    var agedeb = jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1);

    document.forms['creationPrelevementClient'].bandeb.value = bandeb;
    document.forms['creationPrelevementClient'].agedeb.value = agedeb;

    return true;
}
function addHiddenMachine() {

    var agence = jmaki.getWidget('idagence').wrapper.comboBoxSelectionValue.value.substring(1);

    //Gestion Date
    var strDate = jmaki.getWidget('dateinstall').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);
    var dateinstall = year + "/" + month + "/" + day;
    document.forms['creationMachine'].dateinstall.value = dateinstall;
    document.forms['creationMachine'].agence.value = agence;

    return true;
}
function addHiddenRepertoire() {

    if (document.forms['creationRepertoire'].partenaire.value.trim() == '')
        document.forms['creationRepertoire'].partenaire.value = 'XX';

    return true;
}

function addHiddenAgence() {

    document.forms['creationAgence'].codeBanque.value = jmaki.getWidget('idbanque').wrapper.comboBoxSelectionValue.value;
    return true;
}
function showQueryResult() {
    var apercu = "dataTable.jsp?requete=" + encodeURIComponent(document['paramUpdate'].query.value);
    window.open(apercu);

    return false;
}

function showSearchResult1() {
    var select = 'select idcheque as "IDENTIFIANT ORDRE",ETAT,BANQUEREMETTANT as "BANQUE REMETTANTE",AGENCEREMETTANT as "AGENCE REMETTANTE",NUMEROCOMPTE as "NUMERO COMPTE",NUMEROCHEQUE as "NUMERO CHEQUE",MONTANTCHEQUE as "MONTANT",NOMBENEFICIAIRE as "NOM DU BENEFICIAIRE",DATETRAITEMENT as "DATE TRAITEMENT",PATHIMAGE,FICHIERIMAGE,DATECOMPENSATION as "DATE DE COMPENSATION",CODEUTILISATEUR as "UTILISATEUR",MACHINESCAN as "MACHINE DE SCAN",MOTIFREJET as "MOTIF REJET" from cheques_arch '
            + document['paramUpdate'].query.value;
    var apercu = "imageobjet.jsp?requete=" + encodeURIComponent(select) + "&zonerejet=non&objet=cheques&nomidobjet=IDCHEQUE&numcolobjet=1&csv=oui";
    window.open(apercu);

    return false;
}
function showSearchResultBICI() {
    var select = 'select idcheque as "IDENTIFIANT ORDRE",ETAT,BANQUEREMETTANT as "BANQUE REMETTANTE",AGENCE as "AGENCE",NUMEROCOMPTE as "NUMERO COMPTE",NUMEROCHEQUE as "NUMERO CHEQUE",MONTANTCHEQUE as "MONTANT",NOMBENEFICIAIRE as "NOM DU BENEFICIAIRE",DATETRAITEMENT as "DATE TRAITEMENT",PATHIMAGE,FICHIERIMAGE,DATECOMPENSATION as "DATE DE COMPENSATION",CODEUTILISATEUR as "UTILISATEUR",MACHINESCAN as "MACHINE DE SCAN",MOTIFREJET as "MOTIF REJET" from all_cheques '
            + document['paramUpdate'].query.value;
    var apercu = "imageobjet.jsp?requete=" + encodeURIComponent(select) + "&zonerejet=specifique&objet=cheques&requetecoderejet=select * from params where nom in('201','202') and type='CODE_REJET'&nomidobjet=IDCHEQUE&numcolobjet=1&etatdebut=(170,172,180,182)&etatfin=250&csv=oui";

    window.open(apercu);

    return false;
}
function showSearchResult() {
    var select = 'select idcheque as "IDENTIFIANT ORDRE",ETAT,BANQUEREMETTANT as "BANQUE REMETTANTE",AGENCE as "AGENCE",NUMEROCOMPTE as "NUMERO COMPTE",NUMEROCHEQUE as "NUMERO CHEQUE",MONTANTCHEQUE as "MONTANT",NOMBENEFICIAIRE as "NOM DU BENEFICIAIRE",DATETRAITEMENT as "DATE TRAITEMENT",PATHIMAGE,FICHIERIMAGE,DATECOMPENSATION as "DATE DE COMPENSATION",CODEUTILISATEUR as "UTILISATEUR",MACHINESCAN as "MACHINE DE SCAN",MOTIFREJET as "MOTIF REJET" from all_cheques '
            + document['paramUpdate'].query.value;
    var apercu = "imageobjet.jsp?requete=" + encodeURIComponent(select) + "&zonerejet=oui&objet=cheques&nomidobjet=IDCHEQUE&numcolobjet=1&etatdebut=(170,172,180,182)&etatfin=250&csv=oui";
    window.open(apercu);

    return false;
}
function showSearchDocsResult(vue, requete, objet, nomidobjet, dropdown) {
    var select = requete + document['paramUpdate'].query.value;
    var apercu = vue + "?requete=" + decodeURIComponent(select) + "&objet=" + objet + "&nomidobjet=" + nomidobjet + "&csv=oui&dropdown=" + decodeURIComponent(dropdown);
    window.open(apercu);

    return false;
}
function showSearchDocsResult(vue, requete, objet, nomidobjet, dropdown, requeteDetail, cle) {
    var select = requete + document['paramUpdate'].query.value;
    var apercu = vue + "?requete=" + decodeURIComponent(select) + "&objet=" + objet + "&nomidobjet=" + nomidobjet + "&dropdown=" + decodeURIComponent(dropdown) + "&requeteDetail=" + decodeURIComponent(requeteDetail) + "&cle=" + cle;
    window.open(apercu);

    return false;
}
function showSearchResult2() {
    var select = 'select * from all_effets '
            + document['paramUpdate'].query.value;
    var apercu = "imageobjet.jsp?requete=" + encodeURIComponent(select) + "&zonerejet=oui&objet=EFFETS&nomidobjet=IDEFFET&numcolobjet=2&etatdebut=172&etatfin=250&csv=oui";
    window.open(apercu);

    return false;
}
function showSearchResult3() {
    var select = 'select  idremise,etat,compteremettant,nomclient,montant,devise,nboperation,datesaisie,agenceremettant,pathimage,fichierimage,nomutilisateur from all_remises '
            + document['paramUpdate'].query.value;
    var apercu = 'imageobjetMasterDetail.jsp?requete=' + encodeURIComponent(select) + '&requeteDetail=select idcheque as "IDENTIFIANT ORDRE",ETAT,BANQUEREMETTANT as "BANQUE REMETTANTE",AGENCEREMETTANT as "AGENCE REMETTANTE",NUMEROCOMPTE as "NUMERO COMPTE",NUMEROCHEQUE as "NUMERO CHEQUE",MONTANTCHEQUE as "MONTANT",NOMBENEFICIAIRE as "NOM DU BENEFICIAIRE",DATETRAITEMENT as "DATE TRAITEMENT",PATHIMAGE,FICHIERIMAGE,DATECOMPENSATION as "DATE DE COMPENSATION" from all_cheques&cle=remise';
    window.open(apercu);

    return false;
}
function showSearchResult4() {
    var select = 'SELECT NUMERO, AGENCE, NOM,PRENOM, ETAT as "ESCOMPTE.", DATEFINESCOMPTE AS "DATE FIN ESCOMPTE." FROM COMPTES '
            + document['paramUpdate'].query.value;
    var apercu = 'dataTable.jsp?requete=' + encodeURIComponent(select) + '&objet=COMPTES&nomidobjet=NUMERO&dropdown=ETAT["0","1"]';
    window.open(apercu);

    return false;
}

function showSearchResultAudit() {
    var select = 'SELECT * FROM AUDITLOG '
            + document['paramUpdate'].query.value;
    var apercu = 'dataTable.jsp?requete=' + encodeURIComponent(select) + '&csv=oui';
    window.open(apercu);

    return false;
}
function areFieldsNotEmpty(form) {
    var fieldList = new Array();
    var fieldListValues = new Array();


    for (i = 0; i < form.elements.length; i++) {
        fieldList[i] = form.elements[i].name;
        fieldListValues[i] = form.elements[i].value;

    }

    for (i = 0; i < fieldList.length; i++)
    {
        if (fieldListValues[i].trim() == '') {
            alert(fieldList[i] + " requiert une valeur");
            return false;
        }

    }

    return true;

}
function testPass(args) {

    var form = args;
    if (form.password1.value != form.password2.value) {

        alert("Vos mots de passes sont diff&eacute;rents: " + form.password1.value + " " + form.password2.value);
        return false;
    }


    return true;
}



function activerObjet(id, booleen) {

    var obj = getElementOfId(id);
    if (obj != null)
        obj.disabled = !booleen;


}

function checkForm() {

    document.forms['outilsForm'].oui.value = 'Envoi en cours...';
    document.forms['outilsForm'].oui.disabled = true;
    document.forms['outilsForm'].non.disabled = true;
    if (document.forms['outilsForm'].param1 != undefined) {
        //Gestion Date
        var strDate = jmaki.getWidget('dateparam1').getValue();
        // alert(strDate);
        var year = strDate.substring(0, 4);
        var month = strDate.substring(5, 7);
        var day = strDate.substring(8, 10);
        var dateDebChosen = year + month + day;
        document.forms['outilsForm'].param1.value = dateDebChosen;
    }
    if (document.forms['outilsForm'].param2 != undefined) {
        //Gestion Date
        strDate = jmaki.getWidget('dateparam2').getValue();
        // alert(strDate);
        year = strDate.substring(0, 4);
        month = strDate.substring(5, 7);
        day = strDate.substring(8, 10);
        dateDebChosen = year + month + day;
        document.forms['outilsForm'].param2.value = dateDebChosen;
    }

    if (document.forms['outilsForm'].param3 != undefined) {
        //Gestion Date
        strDate = jmaki.getWidget('dateparam3').getValue();
        // alert(strDate);
        year = strDate.substring(0, 4);
        month = strDate.substring(5, 7);
        day = strDate.substring(8, 10);
        dateDebChosen = year + month + day;
        document.forms['outilsForm'].param3.value = dateDebChosen;
    }


    return true;
}
function verifyIcomParams() {
    var actionFrm = document.forms['outilsForm'].action.value.trim();

    console.log("action" + actionFrm); //clearing.action.writers.sica.Icom
    if (~actionFrm.indexOf('clearing.action.writers.sica.Icom')) {
        console.log("action contient clearing.action.writers.sica.Icom "); //=' + encodeURIComponent(select) + '&csv=oui';
        jmaki.doAjax({
            method: "POST",
            url: "ControlServlet?action=checkICOM&message=" + encodeURIComponent(actionFrm),
            callback: function (_req) {
                var tmp = _req.responseText;
                console.log("tmp" + tmp);
                var obj = eval(tmp);
                console.log("obj" + obj);



                // handle any errors
            }
        });

    } else {
        console.log("action ne contient pas clearing.action.writers.sica.Icom ");
    }

    return true;
}
function montrerObjet(id, booleen) {

    var obj = getElementOfId(id);
    if (obj != null)
        obj.style.display = (booleen) ? "block" : "none";
}

function getElementOfId(id) {
    var result = document.getElementById(id);
    return result;
}

String.prototype.trim = function () {
    return this.replace(/^\s+/, "").replace(/\s+$/, "");
}


var infoPanelId = 0;

function infoPanelLoadHandler( )
{

    // Start the timer

    infoPanelId = setInterval("refreshInfoPanel()", 10000);

}

function clearInfoPanelLoadHandler( )
{

    // Stop the timer

    clearInterval(infoPanelId);

}
function refreshInfoPanel()
{

    jmaki.doAjax({
        method: "POST",
        url: "ControlServlet?action=refreshInfoPanel",
        callback: function (_req) {

            var tmp = _req.responseText;

            document.getElementById("infoPanelDIV").innerHTML = tmp;

            // handle any errors
        }
    });


}
function killPopup() {
    window.open("killpopup.html", 'Heure', 'resizable=no, location=no, width=200, height=100, menubar=no, status=no, scrollbars=no, menubar=no');
}

function ouvrirPopup() {
    window.open('bienvenue.jsp', 'Heure', 'resizable=no, location=no, width=200, height=100, menubar=no, status=no, scrollbars=no, menubar=no');
    this.focus();
}
function printSignDiv(printpage) {
    var dataTableWidget = null;
    var info = "<B><U>Informations:</U></B><BR>\n";
    if (jmaki.getWidget('tableCheques') != undefined) {
        dataTableWidget = jmaki.getWidget('tableCheques');
    }

    var randomnumber = Math.floor(Math.random() * Math.pow(10, 6) + Math.pow(10, 5));
    if (dataTableWidget != null) {
        var aRecs = dataTableWidget.dataTable.getRecordSet().getRecords();
        var oData = aRecs[0].getData();
        var cols = dataTableWidget.dataTable.getColumnSet().keys;


        for (var i = 0; i < cols.length; i++) {
            info += "&nbsp;-&nbsp;<font style=\"font-size:9pt\"> <B>" + cols[i].label + "</B>=" + oData[cols[i].key] + "</font>&nbsp;<BR>\n";
        }
    }

    var headstr = "<html><head><title>Impression-" + randomnumber + "</title></head><body><div style=\"width:100%;\">";
    var footstr = "<div style=\"width:40%;\">" + info + "</div></div></body>";
    var newstr = "<div style=\"float:left;width:60%;\">" + document.getElementById(printpage).innerHTML + "</div>";
    var oldstr = document.body.innerHTML;
    document.body.innerHTML = headstr + newstr + footstr;
    window.print();
    document.body.innerHTML = oldstr;
    document.location.reload();
    return false;
}
function printdiv(printpage)
{
    var dataTableWidget = null;
    var info = "<B><U>Informations:</U></B><BR>\n";
    if (jmaki.getWidget('tableCheques') != undefined) {
        dataTableWidget = jmaki.getWidget('tableCheques');
    } else if (jmaki.getWidget('master') != undefined) {
        dataTableWidget = jmaki.getWidget('master');
    } else if (jmaki.getWidget('detail') != undefined) {
        dataTableWidget = jmaki.getWidget('detail');
    }

    var randomnumber = Math.floor(Math.random() * Math.pow(10, 6) + Math.pow(10, 5));
    if (dataTableWidget != null) {
        var sids = dataTableWidget.dataTable.getSelectedRows();
        var cols = dataTableWidget.dataTable.getColumnSet().keys;
        var oRecord = dataTableWidget.dataTable.getRecordSet().getRecord(sids[0]);

        for (var i = 0; i < cols.length; i++) {
            info += "&nbsp;-&nbsp;<font style=\"font-size:9pt\"> <B>" + cols[i].label + "</B>=" + oRecord.getData(cols[i].key) + "</font>&nbsp;<BR>\n";
        }
    }

    var headstr = "<html><head><title>Impression-" + randomnumber + "</title></head><body><div style=\"width:100%;\">";
    var footstr = "<div style=\"float:right;width:40%;\">" + info + "</div></div></body>";
    var newstr = "<div style=\"float:left;width:60%;\">" + document.getElementById(printpage).innerHTML + "</div>";
    var oldstr = document.body.innerHTML;
    document.body.innerHTML = headstr + newstr + footstr;
    window.print();
    document.body.innerHTML = oldstr;
    document.location.reload();
    return false;
}
function printdivold(printpage)
{

    var headstr = "<html><head><title></title></head><body>";
    var footstr = "</body>";
    var newstr = document.getElementById(printpage).innerHTML;
    var oldstr = document.body.innerHTML;
    document.body.innerHTML = headstr + newstr + footstr;
    window.print();
    document.body.innerHTML = oldstr;
    document.location.reload();
    return false;
}

function ajouter(url) {
    // var tmp = "{widgetId : 'yahoo_menu2' , type : undefined , targetId : undefined , topic : '/jmaki/glue' , message : {id : 'COMPTES CLIENTS' , url : 'creationCompte.jsp'}}";
    //var obj = eval("(" + tmp + ")");
    //jmaki.publish('/jmaki/glue',obj);
    window.location.replace(url);
}


function formatInt(ctrl)
{
    var separator = " ";
    var temp = ctrl.value.replace(new RegExp(separator, "g"), "");
    var regexp = new RegExp("\\B(\\d{3})(" + separator + "|$)");
    do
    {
        temp = temp.replace(regexp, separator + "$1");
    } while (temp.search(regexp) >= 0)
    ctrl.value = temp;
}
function currencyFormat(fld, milSep, evt) {

    //var key = '';
    var i = j = 0;
    var len = len2 = 0;
    var strCheck = '0123456789';
    var aux = aux2 = '';
    //var whichCode = (window.Event) ? e.which : e.keyCode;
    //if (whichCode == 13) return true;  // Enter
    //    key = String.fromCharCode(whichCode);  // Get key value from key code
    //    if (strCheck.indexOf(key) == -1) return false;  // Not a valid key

    var keyCode = evt.which ? evt.which : evt.keyCode;

    if (keyCode == 13 || keyCode == 10 || keyCode == 9)
        return true;  // Enter
    if (keyCode > 31 && (keyCode < 45 || keyCode > 57))
        return false;

    len = fld.value.length;

    aux = '';
    for (; i < len; i++)
        if (strCheck.indexOf(fld.value.charAt(i)) != -1)
            aux += fld.value.charAt(i);

    len = aux.length;

    if (len > 2) {
        aux2 = '';
        for (j = 0, i = len; i >= 0; i--) {
            if (j == 3) {
                aux2 += milSep;
                j = 0;
            }
            aux2 += aux.charAt(i);
            j++;
        }
        fld.value = '';
        len2 = aux2.length;
        for (i = len2 - 1; i >= 0; i--)
            fld.value += aux2.charAt(i);
    }
    return true;
}

function resolveVignette() {

    var montantClause = "";
    var userClause = "";
    var machineClause = "";
    var compteClause = "";
    var dateClause = "";
    var orderByClause = "";
    var montant = "";
    var numero = "";


    if (document['paramsearch'].mnt.checked == false) {
        document['paramsearch'].montant.value = "0";
    }
    if (document['paramsearch'].num.checked == false) {
        document['paramsearch'].numero.value = "0";
    }

    if (document['paramsearch'].montantMin.checked == false) {
        document['paramsearch'].montantMinimum.value = "0";
    }
    if (document['paramsearch'].montantMax.checked == false) {
        document['paramsearch'].montantMaximum.value = "0";
    }
    if (document['paramsearch'].cptcre.checked == false) {
        document['paramsearch'].cptCrediteur.value = "defaut";
    }
    if (document['paramsearch'].cptdeb.checked == false) {
        document['paramsearch'].cptDebiteur.value = "defaut";
    }
    if (document['paramsearch'].utilisateur.checked == false) {
        jmaki.getWidget('user').wrapper.setValue("TOUS");
        jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value = "%";
    }
    if (document['paramsearch'].machine.checked == false) {
        jmaki.getWidget('machinescan').wrapper.setValue("TOUS");
        jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value = "%";
    }


    //Gestion Date
    var strDate = jmaki.getWidget('datedebut').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateDebChosen = year + "/" + month + "/" + day;

    strDate = jmaki.getWidget('datefin').getValue();

    year = strDate.substring(0, 4);
    month = strDate.substring(5, 7);
    day = strDate.substring(8, 10);

    var dateFinChosen = year + "/" + month + "/" + day;

    //Gestion Sous regionale
    var srgdeb = jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value;
    var srgcre = jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value;
    srgdeb = (srgdeb.lastIndexOf('%') >= 2) ? 'not' : '';
    srgcre = (srgcre.lastIndexOf('%') >= 2) ? 'not' : '';

    if (document['paramsearch'].traitement.checked) {
        dateClause = "  and datetraitement >= '" + dateDebChosen + "'  and datetraitement  <='" + dateFinChosen + "'";
        document['paramsearch'].interval.value = "Date de Traitement du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramsearch'].compensation.checked) {
        dateClause = dateClause + "  and datecompensation >= '" + dateDebChosen + "'  and datecompensation  <='" + dateFinChosen + "'";
        document['paramsearch'].interval.value = "Date de Compensation du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramsearch'].utilisateur.checked) {
        userClause = userClause + "  and codeutilisateur like '" + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value + "'";
        document['paramsearch'].interval.value += " | Saisie faite par " + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramsearch'].machine.checked) {
        machineClause = machineClause + "  and machinescan like '" + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value + "'";
        document['paramsearch'].interval.value += " | Saisie faite sur " + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramsearch'].cptcre.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and compteremettant like '" + document['paramsearch'].cptCrediteur.value + "'";
        }

        document['paramsearch'].interval.value += " | Compte Crediteur " + document['paramsearch'].cptCrediteur.value;
    }

    if (document['paramsearch'].cptdeb.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and numerocompte like '" + document['paramsearch'].cptDebiteur.value + "'";
        }

        document['paramsearch'].interval.value += " | Compte Debiteur " + document['paramsearch'].cptDebiteur.value;
    }

    var query = "";
    var suitequery = "";
    var apercu = "";
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
        if (document['paramsearch'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTCHEQUE) >= TO_NUMBER('" + document['paramsearch'].montantMinimum.value + "')";
            document['paramsearch'].interval.value += " | Montant Minimum " + document['paramsearch'].montantMinimum.value;
        }
        if (document['paramsearch'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTCHEQUE) <= TO_NUMBER('" + document['paramsearch'].montantMaximum.value + "')";
            document['paramsearch'].interval.value += " | Montant Maximum " + document['paramsearch'].montantMaximum.value;
        }
        if (document['paramsearch'].mnt.checked) {
            montant = montant + "  and TO_NUMBER(MONTANTCHEQUE) = TO_NUMBER('" + document['paramsearch'].montant.value + "')";
            document['paramsearch'].interval.value += " | Montant " + document['paramsearch'].montant.value;
        }
        if (document['paramsearch'].num.checked) {
            numero = numero + "  and NUMEROCHEQUE LIKE '" + document['paramsearch'].numero.value + "'";
            document['paramsearch'].interval.value += " | Numero " + document['paramsearch'].numero.value;
        }

        query = document['paramsearch'].requetecheques.value;
        suitequery = " where banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and etat like ('" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + "')"
                + dateClause + userClause + machineClause + compteClause + montantClause + montant + numero + orderByClause;

        query += encodeURIComponent(suitequery);
        // alert(query);
        document['paramsearch'].requete.value = query;

        apercu = document['paramsearch'].vuecheques.value + "?requete=" + (query);
        window.open(apercu);

    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
        if (document['paramsearch'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTVIREMENT) >= TO_NUMBER('" + document['paramsearch'].montantMinimum.value + "')";
            document['paramsearch'].interval.value += " | Montant Minimum " + document['paramsearch'].montantMinimum.value;
        }
        if (document['paramsearch'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTVIREMENT) <= TO_NUMBER('" + document['paramsearch'].montantMaximum.value + "')";
            document['paramsearch'].interval.value += " | Montant Maximum " + document['paramsearch'].montantMaximum.value;
        }
        if (document['paramsearch'].mnt.checked) {
            montant = montant + "  and TO_NUMBER(MONTANTVIREMENT) = TO_NUMBER('" + document['paramsearch'].montant.value + "')";
            document['paramsearch'].interval.value += " | Montant " + document['paramsearch'].montant.value;
        }
        if (document['paramsearch'].num.checked) {
            numero = numero + "  and NUMEROVIREMENT LIKE '" + document['paramsearch'].numero.value + "'";
            document['paramsearch'].interval.value += " | Numero " + document['paramsearch'].numero.value;
        }

        query = document['paramsearch'].requetevirements.value;
        suitequery = " where banque " + srgcre + " like '"
                + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and banqueremettant " + srgdeb + " like '" + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and agenceremettant like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and agence like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and etat like ('" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + "')"
                + dateClause + userClause + machineClause + compteClause + montantClause + montant + numero + orderByClause;

        query += encodeURIComponent(suitequery);
        //alert(query);
        document['paramsearch'].requete.value = query;

        apercu = document['paramsearch'].vuevirements.value + "?requete=" + (query);
        window.open(apercu);

    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
        if (document['paramsearch'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANT_EFFET) >= TO_NUMBER('" + document['paramsearch'].montantMinimum.value + "')";
            document['paramsearch'].interval.value += " | Montant Minimum " + document['paramsearch'].montantMinimum.value;
        }
        if (document['paramsearch'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANT_EFFET) <= TO_NUMBER('" + document['paramsearch'].montantMaximum.value + "')";
            document['paramsearch'].interval.value += " | Montant Maximum " + document['paramsearch'].montantMaximum.value;
        }
        if (document['paramsearch'].mnt.checked) {
            montant = montant + "  and TO_NUMBER(MONTANT_EFFET) = TO_NUMBER('" + document['paramsearch'].montant.value + "')";
            document['paramsearch'].interval.value += " | Montant " + document['paramsearch'].montant.value;
        }
        if (document['paramsearch'].num.checked) {
            numero = numero + "  and NUMEROEFFET LIKE '" + document['paramsearch'].numero.value + "'";
            document['paramsearch'].interval.value += " | Numero " + document['paramsearch'].numero.value;
        }

        query = document['paramsearch'].requeteeffets.value;
        suitequery = " where banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and etat like ('" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + "')"
                + dateClause + userClause + machineClause + compteClause + montantClause + montant + numero + orderByClause;

        query += encodeURIComponent(suitequery);
        //alert(query);
        document['paramsearch'].requete.value = query;

        apercu = document['paramsearch'].vueeffets.value + "?requete=" + (query);
        window.open(apercu);

    }


    return false

}

function resolveSearch() {

    var montantClause = "";
    var userClause = "";
    var machineClause = "";
    var compteClause = "";
    var dateClause = "";
    var orderByClause = "";
    var montant = "";
    var numero = "";
    var remise = "";
    var refremise = "";



    if (!document['paramsearch'].mnt.checked &&
            !document['paramsearch'].num.checked &&
            !document['paramsearch'].rem.checked &&
            !document['paramsearch'].refrem.checked &&
            !document['paramsearch'].montantMin.checked &&
            !document['paramsearch'].montantMax.checked &&
            !document['paramsearch'].cptcre.checked &&
            !document['paramsearch'].cptdeb.checked &&
            !document['paramsearch'].utilisateur.checked &&
            !document['paramsearch'].machine.checked &&
            !document['paramsearch'].traitement.checked &&
            !document['paramsearch'].compensation.checked) {
        alert("Merci de cocher au moins un critère")
        return false;
    }
    if (document['paramsearch'].mnt.checked == false) {
        document['paramsearch'].montant.value = "0";
    }
    if (document['paramsearch'].rem.checked == false) {
        document['paramsearch'].remise.value = "0";
    }
    if (document['paramsearch'].refrem.checked == false) {
        document['paramsearch'].refremise.value = "0";
    }
    if (document['paramsearch'].num.checked == false) {
        document['paramsearch'].numero.value = "0";
    }

    if (document['paramsearch'].montantMin.checked == false) {
        document['paramsearch'].montantMinimum.value = "0";
    }
    if (document['paramsearch'].montantMax.checked == false) {
        document['paramsearch'].montantMaximum.value = "0";
    }
    if (document['paramsearch'].cptcre.checked == false) {
        document['paramsearch'].cptCrediteur.value = "defaut";
    }
    if (document['paramsearch'].cptdeb.checked == false) {
        document['paramsearch'].cptDebiteur.value = "defaut";
    }
    if (document['paramsearch'].utilisateur.checked == false) {
        jmaki.getWidget('user').wrapper.setValue("TOUS");
        jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value = "%";
    }
    if (document['paramsearch'].machine.checked == false) {
        jmaki.getWidget('machinescan').wrapper.setValue("TOUS");
        jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value = "%";
    }


    //Gestion Date
    var strDate = jmaki.getWidget('datedebut').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateDebChosen = year + "/" + month + "/" + day;

    strDate = jmaki.getWidget('datefin').getValue();

    year = strDate.substring(0, 4);
    month = strDate.substring(5, 7);
    day = strDate.substring(8, 10);

    var dateFinChosen = year + "/" + month + "/" + day;

    //Gestion Sous regionale
    var srgdeb = jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value;
    var srgcre = jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value;
    srgdeb = (srgdeb.lastIndexOf('%') >= 2) ? 'not' : '';
    srgcre = (srgcre.lastIndexOf('%') >= 2) ? 'not' : '';

    if (document['paramsearch'].traitement.checked) {
        dateClause = "  and datetraitement >= '" + dateDebChosen + "'  and datetraitement  <='" + dateFinChosen + "'";
        document['paramsearch'].interval.value = "Date de Traitement du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramsearch'].compensation.checked) {
        dateClause = dateClause + "  and datecompensation >= '" + dateDebChosen + "'  and datecompensation  <='" + dateFinChosen + "'";
        document['paramsearch'].interval.value = "Date de Compensation du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramsearch'].utilisateur.checked) {
        userClause = userClause + "  and codeutilisateur like '" + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value + "'";
        document['paramsearch'].interval.value += " | Saisie faite par " + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramsearch'].machine.checked) {
        machineClause = machineClause + "  and machinescan like '" + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value + "'";
        document['paramsearch'].interval.value += " | Saisie faite sur " + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramsearch'].cptcre.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and compteremettant like '" + document['paramsearch'].cptCrediteur.value + "'";
        }

        document['paramsearch'].interval.value += " | Compte Crediteur " + document['paramsearch'].cptCrediteur.value;
    }

    if (document['paramsearch'].cptdeb.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and numerocompte like '" + document['paramsearch'].cptDebiteur.value + "'";
        }

        document['paramsearch'].interval.value += " | Compte Debiteur " + document['paramsearch'].cptDebiteur.value;
    }

    var query = "";
    var suitequery = "";
    var apercu = "";
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
        if (document['paramsearch'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTCHEQUE) >= TO_NUMBER('" + document['paramsearch'].montantMinimum.value + "')";
            document['paramsearch'].interval.value += " | Montant Minimum " + document['paramsearch'].montantMinimum.value;
        }
        if (document['paramsearch'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTCHEQUE) <= TO_NUMBER('" + document['paramsearch'].montantMaximum.value + "')";
            document['paramsearch'].interval.value += " | Montant Maximum " + document['paramsearch'].montantMaximum.value;
        }
        if (document['paramsearch'].mnt.checked) {
            montant = montant + "  and TO_NUMBER(MONTANTCHEQUE) = TO_NUMBER('" + document['paramsearch'].montant.value + "')";
            document['paramsearch'].interval.value += " | Montant " + document['paramsearch'].montant.value;
        }
        if (document['paramsearch'].num.checked) {
            numero = numero + "  and NUMEROCHEQUE LIKE '" + document['paramsearch'].numero.value + "'";
            document['paramsearch'].interval.value += " | Numero " + document['paramsearch'].numero.value;
        }
        if (document['paramsearch'].rem.checked) {
            remise = remise + "  and REMISE = " + document['paramsearch'].remise.value + "";
            document['paramsearch'].interval.value += " | Remise " + document['paramsearch'].remise.value;
        }
        if (document['paramsearch'].refrem.checked) {
            refremise = refremise + "  and REFREMISE LIKE '" + document['paramsearch'].refremise.value + "'";
            document['paramsearch'].interval.value += " | RefRemise " + document['paramsearch'].refremise.value;
        }

        query = document['paramsearch'].requetecheques.value;
        suitequery = " where banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and etat like ('" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + "')"
                + dateClause + userClause + machineClause + compteClause + montantClause + montant + numero + remise + refremise + orderByClause;

        query += encodeURIComponent(suitequery);
        // alert(query);
        document['paramsearch'].requete.value = query;

        apercu = document['paramsearch'].vuecheques.value + "?requete=" + (query);
        window.open(apercu);

    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
        if (document['paramsearch'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTVIREMENT) >= TO_NUMBER('" + document['paramsearch'].montantMinimum.value + "')";
            document['paramsearch'].interval.value += " | Montant Minimum " + document['paramsearch'].montantMinimum.value;
        }
        if (document['paramsearch'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTVIREMENT) <= TO_NUMBER('" + document['paramsearch'].montantMaximum.value + "')";
            document['paramsearch'].interval.value += " | Montant Maximum " + document['paramsearch'].montantMaximum.value;
        }
        if (document['paramsearch'].mnt.checked) {
            montant = montant + "  and TO_NUMBER(MONTANTVIREMENT) = TO_NUMBER('" + document['paramsearch'].montant.value + "')";
            document['paramsearch'].interval.value += " | Montant " + document['paramsearch'].montant.value;
        }
        if (document['paramsearch'].num.checked) {
            numero = numero + "  and NUMEROVIREMENT LIKE '" + document['paramsearch'].numero.value + "'";
            document['paramsearch'].interval.value += " | Numero " + document['paramsearch'].numero.value;
        }

        query = document['paramsearch'].requetevirements.value;
        suitequery = " where banque " + srgcre + " like '"
                + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and banqueremettant " + srgdeb + " like '" + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and agenceremettant like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and agence like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and etat like ('" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + "')"
                + dateClause + userClause + machineClause + compteClause + montantClause + montant + numero + orderByClause;

        query += encodeURIComponent(suitequery);
        //alert(query);
        document['paramsearch'].requete.value = query;

        apercu = document['paramsearch'].vuevirements.value + "?requete=" + (query);
        window.open(apercu);

    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
        if (document['paramsearch'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANT_EFFET) >= TO_NUMBER('" + document['paramsearch'].montantMinimum.value + "')";
            document['paramsearch'].interval.value += " | Montant Minimum " + document['paramsearch'].montantMinimum.value;
        }
        if (document['paramsearch'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANT_EFFET) <= TO_NUMBER('" + document['paramsearch'].montantMaximum.value + "')";
            document['paramsearch'].interval.value += " | Montant Maximum " + document['paramsearch'].montantMaximum.value;
        }
        if (document['paramsearch'].mnt.checked) {
            montant = montant + "  and TO_NUMBER(MONTANT_EFFET) = TO_NUMBER('" + document['paramsearch'].montant.value + "')";
            document['paramsearch'].interval.value += " | Montant " + document['paramsearch'].montant.value;
        }
        if (document['paramsearch'].num.checked) {
            numero = numero + "  and NUMEROEFFET LIKE '" + document['paramsearch'].numero.value + "'";
            document['paramsearch'].interval.value += " | Numero " + document['paramsearch'].numero.value;
        }

        query = document['paramsearch'].requeteeffets.value;
        suitequery = " where banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and etat like ('" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + "')"
                + dateClause + userClause + machineClause + compteClause + montantClause + montant + numero + orderByClause;

        query += encodeURIComponent(suitequery);
        //alert(query);
        document['paramsearch'].requete.value = query;

        apercu = document['paramsearch'].vueeffets.value + "?requete=" + (query);
        window.open(apercu);

    }


    return false

}

function resolveSearchLite() {

    var montantClause = "";
    var userClause = "";
    var machineClause = "";
    var compteClause = "";
    var dateClause = "";
    var orderByClause = "";
    var montant = "";
    var numero = "";
    var remise = "";


    if (!document['paramsearch'].mnt.checked &&
            !document['paramsearch'].num.checked &&
            !document['paramsearch'].rem.checked &&
            !document['paramsearch'].montantMin.checked &&
            !document['paramsearch'].montantMax.checked &&
            !document['paramsearch'].cptcre.checked &&
            !document['paramsearch'].cptdeb.checked &&
            !document['paramsearch'].utilisateur.checked &&
            !document['paramsearch'].machine.checked &&
            !document['paramsearch'].traitement.checked &&
            !document['paramsearch'].compensation.checked) {
        alert("Merci de cocher au moins un critère")
        return false;
    }
    if (document['paramsearch'].mnt.checked == false) {
        document['paramsearch'].montant.value = "0";
    }
    if (document['paramsearch'].num.checked == false) {
        document['paramsearch'].numero.value = "0";
    }

    if (document['paramsearch'].montantMin.checked == false) {
        document['paramsearch'].montantMinimum.value = "0";
    }
    if (document['paramsearch'].montantMax.checked == false) {
        document['paramsearch'].montantMaximum.value = "0";
    }
    if (document['paramsearch'].cptcre.checked == false) {
        document['paramsearch'].cptCrediteur.value = "defaut";
    }
    if (document['paramsearch'].cptdeb.checked == false) {
        document['paramsearch'].cptDebiteur.value = "defaut";
    }
    if (document['paramsearch'].utilisateur.checked == false) {
        jmaki.getWidget('user').wrapper.setValue("TOUS");
        jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value = "%";
    }
    if (document['paramsearch'].machine.checked == false) {
        jmaki.getWidget('machinescan').wrapper.setValue("TOUS");
        jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value = "%";
    }


    //Gestion Date
    var strDate = jmaki.getWidget('datedebut').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateDebChosen = year + "/" + month + "/" + day;

    strDate = jmaki.getWidget('datefin').getValue();

    year = strDate.substring(0, 4);
    month = strDate.substring(5, 7);
    day = strDate.substring(8, 10);

    var dateFinChosen = year + "/" + month + "/" + day;

    //Gestion Sous regionale
    var srgdeb = jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value;
    var srgcre = jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value;
    srgdeb = (srgdeb.lastIndexOf('%') >= 2) ? 'not' : '';
    srgcre = (srgcre.lastIndexOf('%') >= 2) ? 'not' : '';

    if (document['paramsearch'].traitement.checked) {
        dateClause = "  and datetraitement >= '" + dateDebChosen + "'  and datetraitement  <='" + dateFinChosen + "'";
        document['paramsearch'].interval.value = "Date de Traitement du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramsearch'].compensation.checked) {
        dateClause = dateClause + "  and datecompensation >= '" + dateDebChosen + "'  and datecompensation  <='" + dateFinChosen + "'";
        document['paramsearch'].interval.value = "Date de Compensation du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramsearch'].utilisateur.checked) {
        userClause = userClause + "  and codeutilisateur like '" + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value + "'";
        document['paramsearch'].interval.value += " | Saisie faite par " + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramsearch'].machine.checked) {
        machineClause = machineClause + "  and machinescan like '" + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value + "'";
        document['paramsearch'].interval.value += " | Saisie faite sur " + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramsearch'].cptcre.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and compteremettant like '" + document['paramsearch'].cptCrediteur.value + "'";
        }

        document['paramsearch'].interval.value += " | Compte Crediteur " + document['paramsearch'].cptCrediteur.value;
    }

    if (document['paramsearch'].cptdeb.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and numerocompte like '" + document['paramsearch'].cptDebiteur.value + "'";
        }

        document['paramsearch'].interval.value += " | Compte Debiteur " + document['paramsearch'].cptDebiteur.value;
    }

    var query = "";
    var suitequery = "";
    var apercu = "";
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
        if (document['paramsearch'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTCHEQUE) >= TO_NUMBER('" + document['paramsearch'].montantMinimum.value + "')";
            document['paramsearch'].interval.value += " | Montant Minimum " + document['paramsearch'].montantMinimum.value;
        }
        if (document['paramsearch'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTCHEQUE) <= TO_NUMBER('" + document['paramsearch'].montantMaximum.value + "')";
            document['paramsearch'].interval.value += " | Montant Maximum " + document['paramsearch'].montantMaximum.value;
        }
        if (document['paramsearch'].mnt.checked) {
            montant = montant + "  and TO_NUMBER(MONTANTCHEQUE) = TO_NUMBER('" + document['paramsearch'].montant.value + "')";
            document['paramsearch'].interval.value += " | Montant " + document['paramsearch'].montant.value;
        }
        if (document['paramsearch'].num.checked) {
            numero = numero + "  and NUMEROCHEQUE LIKE '" + document['paramsearch'].numero.value + "'";
            document['paramsearch'].interval.value += " | Numero " + document['paramsearch'].numero.value;
        }
        if (document['paramsearch'].rem.checked) {
            remise = remise + "  and REMISE = " + document['paramsearch'].remise.value + "";
            document['paramsearch'].interval.value += " | Remise " + document['paramsearch'].remise.value;
        }

        var etabClause = " AND ETABLISSEMENT= TRIM('" + document['paramsearch'].etablissement.value + "')";

        query = document['paramsearch'].requetecheques.value;
        suitequery = " where banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and etat like ('" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + "')"
                + dateClause + userClause + machineClause + compteClause + montantClause + montant + numero + remise + etabClause + orderByClause;

        query += encodeURIComponent(suitequery);
        // alert(query);
        document['paramsearch'].requete.value = query;

        apercu = document['paramsearch'].vuecheques.value + "?requete=" + (query);
        window.open(apercu);

    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
        if (document['paramsearch'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTVIREMENT) >= TO_NUMBER('" + document['paramsearch'].montantMinimum.value + "')";
            document['paramsearch'].interval.value += " | Montant Minimum " + document['paramsearch'].montantMinimum.value;
        }
        if (document['paramsearch'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANTVIREMENT) <= TO_NUMBER('" + document['paramsearch'].montantMaximum.value + "')";
            document['paramsearch'].interval.value += " | Montant Maximum " + document['paramsearch'].montantMaximum.value;
        }
        if (document['paramsearch'].mnt.checked) {
            montant = montant + "  and TO_NUMBER(MONTANTVIREMENT) = TO_NUMBER('" + document['paramsearch'].montant.value + "')";
            document['paramsearch'].interval.value += " | Montant " + document['paramsearch'].montant.value;
        }
        if (document['paramsearch'].num.checked) {
            numero = numero + "  and NUMEROVIREMENT LIKE '" + document['paramsearch'].numero.value + "'";
            document['paramsearch'].interval.value += " | Numero " + document['paramsearch'].numero.value;
        }

        query = document['paramsearch'].requetevirements.value;
        suitequery = " where banque " + srgcre + " like '"
                + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and banqueremettant " + srgdeb + " like '" + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and agenceremettant like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and agence like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and etat like ('" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + "')"
                + dateClause + userClause + machineClause + compteClause + montantClause + montant + numero + orderByClause;

        query += encodeURIComponent(suitequery);
        //alert(query);
        document['paramsearch'].requete.value = query;

        apercu = document['paramsearch'].vuevirements.value + "?requete=" + (query);
        window.open(apercu);

    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
        if (document['paramsearch'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANT_EFFET) >= TO_NUMBER('" + document['paramsearch'].montantMinimum.value + "')";
            document['paramsearch'].interval.value += " | Montant Minimum " + document['paramsearch'].montantMinimum.value;
        }
        if (document['paramsearch'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(MONTANT_EFFET) <= TO_NUMBER('" + document['paramsearch'].montantMaximum.value + "')";
            document['paramsearch'].interval.value += " | Montant Maximum " + document['paramsearch'].montantMaximum.value;
        }
        if (document['paramsearch'].mnt.checked) {
            montant = montant + "  and TO_NUMBER(MONTANT_EFFET) = TO_NUMBER('" + document['paramsearch'].montant.value + "')";
            document['paramsearch'].interval.value += " | Montant " + document['paramsearch'].montant.value;
        }
        if (document['paramsearch'].num.checked) {
            numero = numero + "  and NUMEROEFFET LIKE '" + document['paramsearch'].numero.value + "'";
            document['paramsearch'].interval.value += " | Numero " + document['paramsearch'].numero.value;
        }

        query = document['paramsearch'].requeteeffets.value;
        suitequery = " where banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and etat like ('" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + "')"
                + dateClause + userClause + machineClause + compteClause + montantClause + montant + numero + orderByClause;

        query += encodeURIComponent(suitequery);
        //alert(query);
        document['paramsearch'].requete.value = query;

        apercu = document['paramsearch'].vueeffets.value + "?requete=" + (query);
        window.open(apercu);

    }


    return false

}

function resolvePrint() {

    var montantClause = "";
    var userClause = "";
    var machineClause = "";
    var compteClause = "";
    var dateClause = "";
    var orderByClause = "";



    if (document['paramprint'].montantMin.checked == false) {
        document['paramprint'].montantMinimum.value = "0";
    }
    if (document['paramprint'].montantMax.checked == false) {
        document['paramprint'].montantMaximum.value = "0";
    }
    if (document['paramprint'].cptcre.checked == false) {
        document['paramprint'].cptCrediteur.value = "defaut";
    }
    if (document['paramprint'].cptdeb.checked == false) {
        document['paramprint'].cptDebiteur.value = "defaut";
    }
    if (document['paramprint'].utilisateur.checked == false) {
        jmaki.getWidget('user').wrapper.setValue("TOUS");
        jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value = "%";
    }
    if (document['paramprint'].machine.checked == false) {
        jmaki.getWidget('machinescan').wrapper.setValue("TOUS");
        jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value = "%";
    }




    //Gestion Date
    var strDate = jmaki.getWidget('datedebut').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateDebChosen = year + "/" + month + "/" + day;

    strDate = jmaki.getWidget('datefin').getValue();

    year = strDate.substring(0, 4);
    month = strDate.substring(5, 7);
    day = strDate.substring(8, 10);

    var dateFinChosen = year + "/" + month + "/" + day;

    //Gestion Sous regionale
    var srgdeb = jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value;
    var srgcre = jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value;
    srgdeb = (srgdeb.lastIndexOf('%') >= 2) ? 'not' : '';
    srgcre = (srgcre.lastIndexOf('%') >= 2) ? 'not' : '';

    if (document['paramprint'].traitement.checked) {
        dateClause = "  and C1.datetraitement >= '" + dateDebChosen + "'  and C1.datetraitement  <='" + dateFinChosen + "'";
        document['paramprint'].interval.value = "Date de Traitement du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramprint'].compensation.checked) {
        dateClause = dateClause + "  and C1.datecompensation >= '" + dateDebChosen + "'  and C1.datecompensation  <='" + dateFinChosen + "'";
        document['paramprint'].interval.value = "Date de Compensation du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramprint'].utilisateur.checked) {
        userClause = userClause + "  and C1.codeutilisateur like '" + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value + "'";
        document['paramprint'].interval.value += " | Saisie faite par " + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramprint'].machine.checked) {
        machineClause = machineClause + "  and C1.machinescan like '" + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value + "'";
        document['paramprint'].interval.value += " | Saisie faite sur " + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramprint'].cptcre.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and C1.compteremettant like '" + document['paramprint'].cptCrediteur.value + "'";
        }

        document['paramprint'].interval.value += " | Compte Crediteur " + document['paramprint'].cptCrediteur.value;
    }

    if (document['paramprint'].cptdeb.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and C1.numerocompte like '" + document['paramprint'].cptDebiteur.value + "'";
        }

        document['paramprint'].interval.value += " | Compte Debiteur " + document['paramprint'].cptDebiteur.value;
    }

    var query = "";
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
        if (document['paramprint'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTCHEQUE) >= TO_NUMBER('" + document['paramprint'].montantMinimum.value + "')";
            document['paramprint'].interval.value += " | Montant Minimum " + document['paramprint'].montantMinimum.value;
        }
        if (document['paramprint'].montantMax.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTCHEQUE) <= TO_NUMBER('" + document['paramprint'].montantMaximum.value + "')";
            document['paramprint'].interval.value += " | Montant Maximum " + document['paramprint'].montantMaximum.value;
        }
        query = "select C1.* "
                + " from cheques C1,cheques C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDCHEQUE=C2.IDCHEQUE  AND C1.banqueremettant=B1.codebanque "
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + compteClause + montantClause + orderByClause;

    }


    //alert(query);
    document['paramprint'].requete.value = query;



    return true;

}

function resolveLite() {

    var montantClause = "";
    var userClause = "";
    var machineClause = "";
    var compteClause = "";
    var dateClause = "";
    var orderByClause = "";



    if (document['paramrapport'].montantMin.checked == false) {
        document['paramrapport'].montantMinimum.value = "0";
    }
    if (document['paramrapport'].cptcre.checked == false) {
        document['paramrapport'].cptCrediteur.value = "defaut";
    }
    if (document['paramrapport'].cptdeb.checked == false) {
        document['paramrapport'].cptDebiteur.value = "defaut";
    }
    if (document['paramrapport'].utilisateur.checked == false) {
        jmaki.getWidget('user').wrapper.setValue("TOUS");
        jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value = "%";
    }
    if (document['paramrapport'].machine.checked == false) {
        jmaki.getWidget('machinescan').wrapper.setValue("TOUS");
        jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value = "%";
    }
    if (document['paramrapport'].agenceDepot.checked == false) {
        jmaki.getWidget('agenceDep').wrapper.setValue("TOUS");
        jmaki.getWidget('agenceDep').wrapper.comboBoxSelectionValue.value = "%%";
    }
    if (document['paramrapport'].etablissements.checked == false) {
        jmaki.getWidget('etablissement').wrapper.setValue("TOUS");
        jmaki.getWidget('etablissement').wrapper.comboBoxSelectionValue.value = "%%";
    }

    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTCHEQUE)";
            //   orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE,TO_NUMBER(C1.MONTANTCHEQUE)";
                //  orderByClause = " ORDER BY C1.NUMEROCOMPTE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.COMPTEREMETTANT,TO_NUMBER(C1.MONTANTCHEQUE)";
                //     orderByClause = " ORDER BY C1.COMPTEREMETTANT,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[2].checked) {
                orderByClause = " ORDER BY C1.REMISE,TO_NUMBER(C1.MONTANTCHEQUE)";
                //     orderByClause = " ORDER BY C1.REMISE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            }

        }
    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTPRELEVEMENT)";
            //   orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,TO_NUMBER(C1.MONTANTPRELEVEMENT)";
                //  orderByClause = " ORDER BY C1.NUMEROCOMPTE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,TO_NUMBER(C1.MONTANTPRELEVEMENT)";
                //     orderByClause = " ORDER BY C1.COMPTEREMETTANT,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            }

        }
    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTVIREMENT)";
            //    orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANTVIREMENT,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,TO_NUMBER(C1.MONTANTVIREMENT)";
                //   orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,convert(C1.MONTANTVIREMENT,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,TO_NUMBER(C1.MONTANTVIREMENT)";
                //       orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,convert(C1.MONTANTVIREMENT,SIGNED)" ;
            }

        }

    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANT_EFFET)";
            //   orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANT_EFFET,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,TO_NUMBER(C1.MONTANT_EFFET)";
                // orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,convert(C1.MONTANT_EFFET,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,TO_NUMBER(C1.MONTANT_EFFET)";
                //  orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,convert(C1.MONTANT_EFFET,SIGNED)" ;
            }

        }

    //Gestion Date
    var strDate = jmaki.getWidget('datedebut').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateDebChosen = year + "/" + month + "/" + day;

    strDate = jmaki.getWidget('datefin').getValue();

    year = strDate.substring(0, 4);
    month = strDate.substring(5, 7);
    day = strDate.substring(8, 10);

    var dateFinChosen = year + "/" + month + "/" + day;

    //Gestion Sous regionale
    var srgdeb = jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value;
    var srgcre = jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value;
    srgdeb = (srgdeb.lastIndexOf('%') === 3) ? 'not' : '';
    srgcre = (srgcre.lastIndexOf('%') === 3) ? 'not' : '';

    if (document['paramrapport'].traitement.checked) {
        dateClause = "  and C1.datetraitement >= '" + dateDebChosen + "'  and C1.datetraitement  <='" + dateFinChosen + "'";
        document['paramrapport'].interval.value = "Date de Traitement du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramrapport'].compensation.checked) {
        dateClause = dateClause + "  and C1.datecompensation >= '" + dateDebChosen + "'  and C1.datecompensation  <='" + dateFinChosen + "'";
        document['paramrapport'].interval.value = "Date de Compensation du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramrapport'].utilisateur.checked) {
        userClause = userClause + "  and C1.codeutilisateur like '" + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value + "'";
        document['paramrapport'].interval.value += " | Saisie faite par " + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramrapport'].machine.checked) {
        machineClause = machineClause + "  and C1.machinescan like '" + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value + "'";
        document['paramrapport'].interval.value += " | Saisie faite sur " + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value;
    }
    if (document['paramrapport'].agenceDepot.checked) {
        machineClause = machineClause + "  and C1.remise=R.idremise and R.agenceDepot like '" + jmaki.getWidget('agenceDep').wrapper.comboBoxSelectionValue.value.substring(1) + "'";
        document['paramrapport'].interval.value += " | Agence de saisie = " + jmaki.getWidget('agenceDep').wrapper.comboBoxSelectionValue.value.substring(1);
    }
    if (document['paramrapport'].etablissements.checked) {
        machineClause = machineClause + "  and C1.etablissement like '" + jmaki.getWidget('etablissement').wrapper.comboBoxSelectionValue.value.trim() + "'";
        document['paramrapport'].interval.value += " | Etablissement de saisie = " + jmaki.getWidget('etablissement').wrapper.comboBoxSelectionValue.value.trim();
    }

    if (document['paramrapport'].cptcre.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and C1.compteremettant like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_BENEFICIAIRE like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_BENEFICIAIRE like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_BENEFICIAIRE like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        document['paramrapport'].interval.value += " | Compte Crediteur " + document['paramrapport'].cptCrediteur.value;
    }

    if (document['paramrapport'].cptdeb.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and C1.numerocompte like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_TIRE like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_TIRE like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_TIRE like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        document['paramrapport'].interval.value += " | Compte Debiteur " + document['paramrapport'].cptDebiteur.value;
    }

    var query = "";
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTCHEQUE) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.IDCHEQUE,C1.REMISE,C1.REFREMISE,C1.DEVISE,C1.NUMEROCHEQUE,C1.NUMEROCOMPTE,C1.NOMBENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.COMPTEREMETTANT,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantcheque ,C1.MOTIFREJET" + ((document['paramrapport'].parCompte[1].checked) ? ',A1.NOM' : '')
                + " from cheques C1,cheques C2,"
                + " Banques B1,Banques B2" + ((document['paramrapport'].parCompte[1].checked) ? ',Comptes A1' : '') + ((document['paramrapport'].agenceDepot.checked) ? ',Remises R' : '')
                + " where C1.IDCHEQUE=C2.IDCHEQUE  AND C1.banqueremettant=B1.codebanque " + ((document['paramrapport'].parCompte[1].checked) ? ' AND C1.NUMEROCOMPTE=A1.NUMERO' : '')
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + userClause + machineClause + compteClause + montantClause + orderByClause;
    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTVIREMENT) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.idvirement,C1.DEVISE,C1.NUMEROVIREMENT,C1.LIBELLE,C1.NUMEROCOMPTE_BENEFICIAIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.NUMEROCOMPTE_TIRE,C1.NOM_TIRE,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantvirement ,C1.MOTIFREJET"
                + " from virements C1,virements C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDVIREMENT=C2.IDVIREMENT  AND C1.banqueremettant=B1.codebanque"
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + compteClause + montantClause + orderByClause;
    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANT_EFFET) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.ideffet,C1.DEVISE,C1.NUMEROEFFET,C1.NUMEROCOMPTE_BENEFICIAIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.NUMEROCOMPTE_TIRE,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montant_effet ,C1.MOTIFREJET"
                + " from effets C1,effets C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDEFFET=C2.IDEFFET  AND C1.banqueremettant=B1.codebanque"
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + compteClause + montantClause + orderByClause;
    }

    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTPRELEVEMENT) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.idprelevement,C1.DEVISE,C1.NUMEROPRELEVEMENT,C1.NUMEROCOMPTE_BENEFICIAIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.NUMEROCOMPTE_TIRE,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantprelevement ,C1.MOTIFREJET"
                + " from prelevements C1,prelevements C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDPRELEVEMENT=C2.IDPRELEVEMENT  AND C1.banqueremettant=B1.codebanque"
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + userClause + compteClause + montantClause + orderByClause;
    }
    //alert(query);
    document['paramrapport'].requete.value = query;


    if (document['paramrapport'].parCompte[1].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_deb";
    } else if (document['paramrapport'].parCompte[0].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_cred";
    } else if (document['paramrapport'].parCompte[2].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_remises";
    } else {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value;
    }
    if (!document['paramrapport'].avecDetail.checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_sans_details";
    }
    return true;

}

function resolveWebLite() {

    var montantClause = "";
    var userClause = "";
    var machineClause = "";
    var compteClause = "";
    var dateClause = "";
    var orderByClause = "";



    if (document['paramrapport'].montantMin.checked == false) {
        document['paramrapport'].montantMinimum.value = "0";
    }
    if (document['paramrapport'].cptcre.checked == false) {
        document['paramrapport'].cptCrediteur.value = "defaut";
    }
    if (document['paramrapport'].cptdeb.checked == false) {
        document['paramrapport'].cptDebiteur.value = "defaut";
    }
    if (document['paramrapport'].utilisateur.checked == false) {
        jmaki.getWidget('user').wrapper.setValue("TOUS");
        jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value = "%";
    }
    if (document['paramrapport'].machine.checked == false) {
        jmaki.getWidget('machinescan').wrapper.setValue("TOUS");
        jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value = "%";
    }


    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTCHEQUE)";
            //   orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE,TO_NUMBER(C1.MONTANTCHEQUE)";
                //  orderByClause = " ORDER BY C1.NUMEROCOMPTE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.COMPTEREMETTANT,TO_NUMBER(C1.MONTANTCHEQUE)";
                //     orderByClause = " ORDER BY C1.COMPTEREMETTANT,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[2].checked) {
                orderByClause = " ORDER BY C1.REMISE,TO_NUMBER(C1.MONTANTCHEQUE)";
                //     orderByClause = " ORDER BY C1.REMISE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            }

        }

    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTPRELEVEMENT)";
            //   orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,TO_NUMBER(C1.MONTANTPRELEVEMENT)";
                //  orderByClause = " ORDER BY C1.NUMEROCOMPTE,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,TO_NUMBER(C1.MONTANTPRELEVEMENT)";
                //     orderByClause = " ORDER BY C1.COMPTEREMETTANT,convert(C1.MONTANTCHEQUE,SIGNED)" ;
            }

        }

    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANTVIREMENT)";
            //    orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANTVIREMENT,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,TO_NUMBER(C1.MONTANTVIREMENT)";
                //   orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,convert(C1.MONTANTVIREMENT,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,TO_NUMBER(C1.MONTANTVIREMENT)";
                //       orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,convert(C1.MONTANTVIREMENT,SIGNED)" ;
            }

        }

    if ((jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets"))
        if (!document['paramrapport'].parCompte[1].checked && !document['paramrapport'].parCompte[0].checked && !document['paramrapport'].parCompte[2].checked) {
            orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,TO_NUMBER(C1.MONTANT_EFFET)";
            //   orderByClause = " ORDER BY C1.BANQUEREMETTANT,C1.BANQUE,convert(C1.MONTANT_EFFET,SIGNED)" ;
        } else {
            if (document['paramrapport'].parCompte[1].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,TO_NUMBER(C1.MONTANT_EFFET)";
                // orderByClause = " ORDER BY C1.NUMEROCOMPTE_TIRE,convert(C1.MONTANT_EFFET,SIGNED)" ;
            } else if (document['paramrapport'].parCompte[0].checked) {
                orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,TO_NUMBER(C1.MONTANT_EFFET)";
                //  orderByClause = " ORDER BY C1.NUMEROCOMPTE_BENEFICIAIRE,convert(C1.MONTANT_EFFET,SIGNED)" ;
            }

        }

    //Gestion Date
    var strDate = jmaki.getWidget('datedebut').getValue();

    var year = strDate.substring(0, 4);
    var month = strDate.substring(5, 7);
    var day = strDate.substring(8, 10);

    var dateDebChosen = year + "/" + month + "/" + day;

    strDate = jmaki.getWidget('datefin').getValue();

    year = strDate.substring(0, 4);
    month = strDate.substring(5, 7);
    day = strDate.substring(8, 10);

    var dateFinChosen = year + "/" + month + "/" + day;

    //Gestion Sous regionale
    var srgdeb = jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value;
    var srgcre = jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value;
    srgdeb = (srgdeb.lastIndexOf('%') >= 2) ? 'not' : '';
    srgcre = (srgcre.lastIndexOf('%') >= 2) ? 'not' : '';

    if (document['paramrapport'].traitement.checked) {
        dateClause = "  and C1.datetraitement >= '" + dateDebChosen + "'  and C1.datetraitement  <='" + dateFinChosen + "'";
        document['paramrapport'].interval.value = "Date de Traitement du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramrapport'].compensation.checked) {
        dateClause = dateClause + "  and C1.datecompensation >= '" + dateDebChosen + "'  and C1.datecompensation  <='" + dateFinChosen + "'";
        document['paramrapport'].interval.value = "Date de Compensation du " + dateDebChosen + " au " + dateFinChosen;
    }

    if (document['paramrapport'].utilisateur.checked) {
        userClause = userClause + "  and C1.codeutilisateur like '" + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value + "'";
        document['paramrapport'].interval.value += " | Saisie faite par " + jmaki.getWidget('user').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramrapport'].machine.checked) {
        machineClause = machineClause + "  and C1.machinescan like '" + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value + "'";
        document['paramrapport'].interval.value += " | Saisie faite sur " + jmaki.getWidget('machinescan').wrapper.comboBoxSelectionValue.value;
    }

    if (document['paramrapport'].cptcre.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and C1.compteremettant like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_BENEFICIAIRE like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_BENEFICIAIRE like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_BENEFICIAIRE like '" + document['paramrapport'].cptCrediteur.value + "'";
        }
        document['paramrapport'].interval.value += " | Compte Crediteur " + document['paramrapport'].cptCrediteur.value;
    }

    if (document['paramrapport'].cptdeb.checked) {
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
            compteClause = compteClause + "  and C1.numerocompte like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_TIRE like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_TIRE like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements") {
            compteClause = compteClause + "  and C1.NUMEROCOMPTE_TIRE like '" + document['paramrapport'].cptDebiteur.value + "'";
        }
        document['paramrapport'].interval.value += " | Compte Debiteur " + document['paramrapport'].cptDebiteur.value;
    }

    var query = "";
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTCHEQUE) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.IDCHEQUE,C1.REMISE,C1.REFREMISE,C1.ETABLISSEMENT,C1.DEVISE,C1.NUMEROCHEQUE,C1.NUMEROCOMPTE,C1.NOMBENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.COMPTEREMETTANT,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantcheque ,C1.MOTIFREJET" + ((document['paramrapport'].parCompte[1].checked) ? ',A1.NOM' : '')
                + " from cheques C1,cheques C2,"
                + " Banques B1,Banques B2" + ((document['paramrapport'].parCompte[1].checked) ? ',Comptes A1' : '') //+ ((document['paramrapport'].agenceDepot.checked)?',Remises R':'')
                + " where C1.IDCHEQUE=C2.IDCHEQUE  AND C1.banqueremettant=B1.codebanque " + ((document['paramrapport'].parCompte[1].checked) ? ' AND C1.NUMEROCOMPTE=A1.NUMERO' : '')
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + userClause + machineClause + compteClause + montantClause + orderByClause;
    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Virements") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTVIREMENT) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.idvirement,C1.DEVISE,C1.NUMEROVIREMENT,C1.LIBELLE,C1.NUMEROCOMPTE_BENEFICIAIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.NUMEROCOMPTE_TIRE,C1.NOM_TIRE,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantvirement ,C1.MOTIFREJET"
                + " from virements C1,virements C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDVIREMENT=C2.IDVIREMENT  AND C1.banqueremettant=B1.codebanque"
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + compteClause + montantClause + orderByClause;
    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Effets") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANT_EFFET) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.ideffet,C1.DEVISE,C1.NUMEROEFFET,C1.NUMEROCOMPTE_BENEFICIAIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.NUMEROCOMPTE_TIRE,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montant_effet ,C1.MOTIFREJET"
                + " from effets C1,effets C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDEFFET=C2.IDEFFET  AND C1.banqueremettant=B1.codebanque"
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + compteClause + montantClause + orderByClause;
    }
    if (jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Prelevements") {
        if (document['paramrapport'].montantMin.checked) {
            montantClause = montantClause + "  and TO_NUMBER(C1.MONTANTPRELEVEMENT) >= TO_NUMBER('" + document['paramrapport'].montantMinimum.value + "')";
            document['paramrapport'].interval.value += " | Montant Minimum " + document['paramrapport'].montantMinimum.value;
        }
        query = "select C1.idprelevement,C1.DEVISE,C1.NUMEROPRELEVEMENT,C1.NUMEROCOMPTE_BENEFICIAIRE,C1.NOM_BENEFICIAIRE,C1.ETAT,C1.AGENCE,C1.NUMEROCOMPTE_TIRE,C1.AGENCEREMETTANT,C1.DATETRAITEMENT,C1.DATECOMPENSATION,C1.banqueremettant ,B1.libellebanque as LIBELLEBANQUEREMETTANT,C2.banque ,B2.libellebanque as LIBELLEBANQUE,C1.montantprelevement ,C1.MOTIFREJET"
                + " from prelevements C1,prelevements C2,"
                + " Banques B1,Banques B2"
                + " where C1.IDPRELEVEMENT=C2.IDPRELEVEMENT  AND C1.banqueremettant=B1.codebanque"
                + " and C2.banque=B2.codebanque and C1.banque " + srgdeb + " like '"
                + jmaki.getWidget('idbandeb').wrapper.comboBoxSelectionValue.value
                + "' and C1.banqueremettant " + srgcre + " like '" + jmaki.getWidget('idbancre').wrapper.comboBoxSelectionValue.value
                + "' and C1.agenceremettant like '" + jmaki.getWidget('idagecre').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.agence like '" + jmaki.getWidget('idagedeb').wrapper.comboBoxSelectionValue.value.substring(1)
                + "' and C1.etat " + ((jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value == "%%") ? "like ('%')" : "in (" + jmaki.getWidget('etat').wrapper.comboBoxSelectionValue.value + ")")
                + dateClause + userClause + compteClause + montantClause + orderByClause;
    }

    //alert(query);
    document['paramrapport'].requete.value = query;


    if (document['paramrapport'].parCompte[1].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_deb";
    } else if (document['paramrapport'].parCompte[0].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_cred";
    } else if (document['paramrapport'].parCompte[2].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_remises";
    } else if (document['paramrapport'].parCompte[3].checked) {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_clients_remises_break";
    } else {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value;
    }
    if (!document['paramrapport'].avecDetail.checked && jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value == "Cheques") {
        document['paramrapport'].nomrapport.value = jmaki.getWidget('typeordre').wrapper.comboBoxSelectionValue.value + "_sans_details";
    }
    return true;

}
