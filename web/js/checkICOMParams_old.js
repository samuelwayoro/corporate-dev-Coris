$(document).ready(function () {

    console.log("check ICOM params loaded");
    // Using the core $.ajax() method
    $.ajax({

        // The URL for the request
        url: "post.php",

        // The data to send (will be converted to a query string)
        data: {
            id: 123
        },

        // Whether this is a POST or GET request
        type: "GET",

        // The type of data we expect back
        dataType: "json",
    })
            // Code to run if the request succeeds (is done);
            // The response is passed to the function
            .done(function (json) {
                $("<h1>").text(json.title).appendTo("body");
                $("<div class=\"content\">").html(json.html).appendTo("body");
            })
            // Code to run if the request fails; the raw request and
            // status codes are passed to the function
            .fail(function (xhr, status, errorThrown) {
                alert("Sorry, there was a problem!");
                console.log("Error: " + errorThrown);
                console.log("Status: " + status);
                console.dir(xhr);
            })
            // Code to run regardless of success or failure;
            .always(function (xhr, status) {
                alert("The request is complete!");
            });


});

$(function () {
    $('#searchInput').onkeyup(function () {
        if ($(this).val() == '') {
            $('.enableOnInput').prop('disabled', true);
        } else {
            $('.enableOnInput').prop('disabled', false);
        }
    });
});

// // Disable #x
//$( "#x" ).prop( "disabled", true );
// 
//// Enable #x
//$( "#x" ).prop( "disabled", false );