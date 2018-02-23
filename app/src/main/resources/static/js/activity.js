/**
 * Activity
 */

$(document).ready(function () {
    logger.d("Loaded activity.js");
});

/*
 * Controllers
 */

var activity = new Object();

activity.list = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/activity", function (data) {
        $("#content").html(data);
        $("#menu-activity").addClass("active");

//        initializeViewActivitiesFragment();

    });
};

/*
 *  Handlers
 */

/*
 *  Actions
 */

 function initializeViewActivitiesFragment() {

     $("#activities-table").bootgrid({
         cssClass: 'text-center',
         columnSelection: false,
         caseSensitive: false
     });

     $('.bootgrid-header').css({'height':"40px"});

 }