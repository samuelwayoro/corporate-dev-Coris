$(document).ready(function() {
    var options = {
        beforeSend: function() {
            $("#progressbox").show();
            // clear everything
            $("#progressbar").width('0%');
            $("#message").empty();
            $("#percent").html("0%");
            var strDate = jmaki.getWidget('datefin').getValue();
            var year = strDate.substring(0, 4);
            var month = strDate.substring(5, 7);
            var day = strDate.substring(8, 10);
            var dateDebChosen = year + "/" + month + "/" + day;
            document['UploadForm'].param1.value = dateDebChosen;
          
            

        },
        uploadProgress: function(event, position, total, percentComplete) {
            $("#progressbar").width(percentComplete + '%');
            $("#percent").html(percentComplete + '%');

            // change message text and % to red after 50%
            if (percentComplete > 50) {
                $("#message").html("<font color='red'>Traitement du fichier en cours ... </font>");
            }
        },
        success: function() {
            $("#progressbar").width('100%');
            $("#percent").html('100%');
            $("#message").html("<font color='blue'>Fichier uploade</font>");
        },
        complete: function(response) {
            $("#message").html("<font color='blue'>Fichier traite!</font>");
            //timeout
            setTimeout(function() {

            }, 4000);
            //redirect in here
            window.location = "result.jsp";

        },
        error: function() {
            $("#message").html("<font color='red'> ERROR: Impossible de traiter ce fichier</font>");
        }
    };
    $("#UploadForm").ajaxForm(options);
});