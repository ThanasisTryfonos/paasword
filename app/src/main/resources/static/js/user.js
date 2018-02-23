/**
 * User
 */

$(document).ready(function () {
    logger.d("Loaded user.js");
});

// Static variables
var DELETE_REST_URL = "/api/v1/user/";

/*
 * Controllers
 */

var user = new Object();

user.list = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-user").addClass("active");
    });
};

user.add = function add(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/user/add", function (data) {
        $("#content").html(data);
        $("#menu-user").addClass("active");
    });
};

user.edit = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/user/" + id, function (data) {
        $("#content").html(data);
        $("#menu-user").addClass("active");
    });
};

/*
 *  Handlers
 */

function addUserHandler() {
    //Create User object
    var user = new Object();
    user.username = $("#username").val();
    user.password = $("#password").val();
    user.firstname = $("#firstname").val();
    user.lastname = $("#lastname").val();
    user.email = $("#email").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(user),
        url: REGISTER_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the new user is created
        if ("SUCCESS" === data.code) {
            logger.i("User created successfully");
            //Redirect user to login
            paasword.notify(data.message);
            page.redirect("/user");
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

function editUserHandler() {
    //Create User object
    var user = new Object();
    user.id = $("#uid").val();
    user.username = $("#username").val();
    user.password = $("#password").val();
    user.firstname = $("#firstname").val();
    user.lastname = $("#lastname").val();
    user.email = $("#email").val();

    $("#loader-full").show();

    //Make the add call
    $.ajax({
        type: 'PUT',
        data: JSON.stringify(user),
        url: REGISTER_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the user is updated
        if ("SUCCESS" === data.code) {
            logger.i("User updated successfully");
            //Redirect user to login
            paasword.notify(data.message);
            page.redirect("/user");
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

function deleteUserHandler(ID) {
    logger.d("Trying to delete user with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: DELETE_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload users page
        page.redirect("/user");
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