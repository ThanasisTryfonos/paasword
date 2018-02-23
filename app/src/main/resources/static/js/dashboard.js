/**
 * Dashboard
 */

$(document).ready(function () {
    logger.d("Loaded dashboard.js");
});

var SEMANTIC_AUTHORIZATION_ENGINE_REST_URL = "/api/v1/semanticauthorizationengine/";

/*
 * Controllers
 */

var dashboard = new Object();

dashboard.load = function load(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/dashboard", function (data) {
        $("#content").html(data);
        $("#menu-dashboard").addClass("active");
    });
};

/*
 *  Handlers
 */
function synchronizeSemanticAuthorizationEngineHandler() {

    $("#loader-full").show();

    //Make the add call
    $.post({
        url: SEMANTIC_AUTHORIZATION_ENGINE_REST_URL + "synchronize",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Semantic Authorization Engine synchronized successfully");

            paasword.notify(data.message);

        } else {
            paasword.notify(data.message, {mode: "notification-error"});
        }

    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

/*
 *  Actions
 */
