/**
 * Documentation
 */

$(document).ready(function () {
    logger.d("Loaded documentation.js");
});

/*
 * Controllers
 */

var documentation = new Object();

documentation.list = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/documentation", function (data) {
        $("#content").html(data);
        $("#menu-documentation").addClass("active");
        $("#menu-documentation-generic").addClass("active");
    });
};

documentation.webapp = function webapp(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/documentation/webapp", function (data) {
        $("#content").html(data);
        $("#menu-documentation").addClass("active");
        $("#menu-documentation-webapp").addClass("active");
    });
};

documentation.pep = function pep(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/documentation/pep", function (data) {
        $("#content").html(data);
        $("#menu-documentation").addClass("active");
        $("#menu-documentation-pep").addClass("active");
    });
};

documentation.jpa = function jpa(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/documentation/jpa", function (data) {
        $("#content").html(data);
        $("#menu-documentation").addClass("active");
        $("#menu-documentation-jpa").addClass("active");
    });
};

/*
 *  Handlers
 */

/*
 *  Actions
 */