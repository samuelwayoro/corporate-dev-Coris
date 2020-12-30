function readyFn(jQuery) {
    // Code to run when the document is ready.
    // dataString = $("#confirmationFrm").serialize();
    var actionFrm = $("#action").val();
    console.log("action dans le formulaire" + actionFrm);
    if (~actionFrm.indexOf('clearing.action.writers.sica.Icom')) {
        dataString = "action=checkICOM&message=" + encodeURIComponent(actionFrm);
        $.ajax({
            type: "POST",
            url: "ControlServlet",
            data: dataString,
            dataType: "json",
            //if received a response from the server
            success: function (data, textStatus, jqXHR) {
                //our country code was correct so we have some information to display
                console.log(data);

                if (data.success) {
                    console.log("data success");
                    if (data.data ["ENVOI_ICOM_NAT"] !== undefined) {
                        console.log("ENVOI_ICOM_NAT is not undefined" + data.data ["ENVOI_ICOM_NAT"]);
                        if (data.data ["ENVOI_ICOM_NAT"] == 1) {
                            console.log("here we are");
                            $('#submitBtn').attr("disabled", true);
                            $('#msgIcom').html("Un ICOM National est en cours d'envoi.Parametre <b  style= 'color : red'> ENVOI_ICOM_NAT=1</b>");



                            console.log("Button disabled");
                        } else {
                            console.log("Button not disabled");
                            $('#submitBtn').attr("disabled", false);
                        }
                    }
                    if (data.data ["ENVOI_ICOM_SRG"] !== undefined) {
                        console.log("ENVOI_ICOM_SRG is not undefined" + data.data ["ENVOI_ICOM_SRG"]);
                        if (data.data ["ENVOI_ICOM_SRG"] == 1) {
                            console.log("here we are");
                            $('#submitBtn').attr("disabled", true);
                            $('#msgIcom').html("Un ICOM National est en cours d'envoi.Parametre <b> ENVOI_ICOM_SRG=1</b>");
                            console.log("Button disabled");
                        } else {
                            $('#submitBtn').attr("disabled", false);
                            console.log("Button not disabled");
                        }

                    }
                }
            },
            //If there was no resonse from the server
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("Something really bad happened " + textStatus);

                //   $("#ajaxResponse").html(jqXHR.responseText);
            },
            //capture the request before it was sent to server
            beforeSend: function (jqXHR, settings) {

                //disable the button until we get the response
                $('#submitBtn').attr("disabled", true);
            },
            //this is called after the response or error functions are finsihed
            //so that we can take some action
            complete: function (jqXHR, textStatus) {
                //enable the button 
//            $('#submitBtn').attr("disabled", false);
            }

        });
    }


}

$(document).ready(readyFn);

//$( document ).ready( readyFn );



