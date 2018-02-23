/**
/**
 * User
 */

$(document).ready(function () {
    logger.d("Loaded application.js");
});

// Static variables
var APPLICATION_REST_URL = "/api/v1/application/";

var APIKEY_REST_URL = "/api/v1/apikey/";

var PRIVACY_CONSTRAINT_REST_URL = "/api/v1/privacyconstraint/";

var AFFINITY_CONSTRAINT_REST_URL = "/api/v1/affinityconstraint/";

var APPLICATION_INSTANCE_REST_URL = "/api/v1/applicationinstance/";

var APPLICATION_INSTANCE_USER_REST_URL = "/api/v1/applicationinstanceuser/";

var DB_PROXY_REST_URL = "/api/v1/query/";

/*
 * Controllers
 */

var application = new Object();

application.list = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-application").addClass("active");
    });
};

application.add = function add(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/application/add", function (data) {
        $("#content").html(data);
        $("#menu-application").addClass("active");
    });
};

application.edit = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id, function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-info").addClass("active");
    });
};

application.uploadSourceCode = function uploadSourceCode(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/upload", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-info").addClass("active");

    });
};


application.appKeys = function appKeys(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/appkey", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-keys").addClass("active");
    });
};

application.affinityConstraintsSets = function affinityConstraintsSets(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/affinity", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-affinity").addClass("active");

    });
};

application.addAffinityConstraintsSet = function addAffinityConstraintsSet(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/affinity/add", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-affinity").addClass("active");

        $(".chosen-select").chosen({width: "300px"});

    });
};

application.editAffinityConstraintsSet = function editAffinityConstraintsSet(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var affinityConstraintID = ctx.params.affinityConstraintID;
    $.post("/application/" + id + "/affinity/" + affinityConstraintID, function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-affinity").addClass("active");

        $(".chosen-select").chosen({width: "300px"});

    });
};

application.privacyConstraintsSets = function privacyConstraintsSets(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/privacy", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-privacy").addClass("active");

    });
};

application.addPrivacyConstraintsSet = function addPrivacyConstraintsSet(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/privacy/add", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-privacy").addClass("active");

        $(".chosen-select").chosen({width: "300px"});

    });
};

application.editPrivacyConstraintsSet = function editPrivacyConstraintsSet(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var privacyConstraintID = ctx.params.privacyConstraintID;
    $.post("/application/" + id + "/privacy/" + privacyConstraintID, function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-privacy").addClass("active");

        $(".chosen-select").chosen({width: "300px"});

    });
};

application.pep = function pep(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/pep", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-pep").addClass("active");

    });
};

application.entity = function dataModel(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/entity", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-datamodel").addClass("active");

    });
};

application.instance = function instance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");


    });
};

application.newInstance = function newInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/instance/new", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");

        $(".chosen-select").chosen({width: "300px"});

        $("#errorMessage").hide();

    });
};

application.deployInstance = function deployInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $("#loader-full").show();
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;
    $.post("/application/" + id + "/instance/" + instanceID + "/deploy", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");
        $("#menu-application-instance-deploy").addClass("active");

        $("body").tooltip({
            selector: 'a[rel=tooltip]',
            html: true,
            placement: "right"
        });

        $("#paasProviderSelectBox").val('');

        if ($("#deploymentType").length > 0) {

            $("#deploymentType").change(function () {

                var deploymentType = $("#deploymentType").val();

                if (deploymentType === '1') {
                    $('#paasProviderSelectBox').removeAttr('disabled');
                } else {
                    $("#paasProviderSelectBox").val('');
                    $('#paasProviderSelectBox').attr("disabled", true);
                }

            });

        }

        $("#loader-full").hide();

    });
};

application.proxyInstance = function proxyInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;
    $.post("/application/" + id + "/instance/" + instanceID + "/proxy", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");
        $("#menu-application-instance-dbproxy").addClass("active");

        $(".chosen-select").chosen({width: "300px"});

        $("body").tooltip({
            selector: 'a[rel=tooltip]',
            html: true,
            placement: "right"
        });

        if ($("#deploymentType").length > 0) {

            $("#deploymentType").change(function () {
                var deploymentType = $("#deploymentType").val();
                if (deploymentType === '1') {
                    $('#iaasProviderSelectBox').prop('disabled', false).trigger("chosen:updated");
                } else {
                    $("#iaasProviderSelectBox").val('');
                    $('#iaasProviderSelectBox').prop('disabled', true).trigger("chosen:updated");
                }

            });

        }

    });
};

application.activityInstance = function activityInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;

    $("#loader-full").show();

    $.post("/application/" + id + "/instance/" + instanceID + "/activity", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");
        $("#menu-application-instance-activity").addClass("active");

        $("#loader-full").hide();

    });
};

application.validateInstance = function validateInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/application/" + id + "/instance/validate", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");

        $(".chosen-select").chosen({width: "300px"});

        $("#errorMessage").hide();

    });
};

application.infoInstance = function infoInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;
    $.post("/application/" + id + "/instance/" + instanceID, function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");
        $("#menu-application-instance-info").addClass("active");

        $(".chosen-select").chosen({width: "300px"});
        $("#errorMessage").hide();

    });
};

application.keyManagementInstance = function keyManagementInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;
    $.post("/application/" + id + "/instance/" + instanceID + "/keymgmt", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");
        $("#menu-application-instance-keymgmt").addClass("active");

    });
};

application.keyManagementAddUserInstance = function keyManagementAddUserInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;
    $.post("/application/" + id + "/instance/" + instanceID + "/keymgmt/adduser", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");
        $("#menu-application-instance-keymgmt").addClass("active");

    });
};

application.handlerInstance = function handlerInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $("#loader-full").show();
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;
    $.post("/application/" + id + "/instance/" + instanceID + "/handler", function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");
        $("#menu-application-instance-handler").addClass("active");
        $("#loader-full").hide();

    });
};

application.editInstance = function editInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;
    $.post("/application/" + id + "/instance/" + instanceID, function (data) {
        $("#content").html(data);
//        $("#menu-application").addClass("active");
        $("#menu-application-instance").addClass("active");

        $(".chosen-select").chosen({width: "300px"});
        $("#errorMessage").hide();

        $("body").tooltip({
            selector: 'a[rel=tooltip]',
            html: true,
            placement: "right"
        });


    });
};

/*
 *  Handlers
 */

function addApplicationHandler() {
    // Create Application object
    var application = new Object();
    application.name = $("#applicationName").val();
    application.description = $("#description").val();
    application.version = $("#version").val();
    application.rootPackage = $("#rootPackage").val();

    $("#loader-full").show();

    // Make the add call
    $.ajax({
        type:'POST',
        data: JSON.stringify(application),
        url: APPLICATION_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        //Check if the new application is created
        if ("SUCCESS" === data.code) {
            logger.i("Registration is success");
            //Redirect user to login
            paasword.notify(data.message);
            page.redirect("/application");
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

function deleteApplicationHandler(ID) {
    logger.d("Trying to delete application with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: APPLICATION_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload application page
        page.redirect("/application");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function generateAPIKeyHandler(applicationID) {
    //Create APIKey object
    var apiKey = new Object();

    var application = new Object();
    application.id = applicationID;

    apiKey.applicationID = application;

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(apiKey),
        url: APIKEY_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("API Key created successfully");
            $("#placeholder").hide();
//            $("#apiKeyTable").show();
            paasword.notify(data.message);
            $("#title").hide();
            $("#apikey-token").show();
            $("#apikey-token").html("<span class='oi oi-check'></span> " + data.returnobject)
            $("#btn-generateAPIKey").attr("disabled", true);

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

function deleteAPIKeyHandler(ID, appID) {
    logger.d("Trying to delete API Key with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: APIKEY_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload application page
        page.redirect("/application/" + appID);
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function uploadNewVersionHandler(applicationID) {
   var formData = new FormData();
   // Main magic with files here
   formData.append('binary', $('input[type=file]')[0].files[0]);

   $("#loader-full").show();

   //Make the add call
   $.ajax({
       type: 'POST',
       data: formData,
       url: APPLICATION_REST_URL + "upload/" + applicationID,
//       contentType: "application/json; charset=utf-8"
       contentType: false,
       processData: false,
   }).success(function (data, status, xhr) {

       $("#loader-full").hide();

       //Check if account is updated
       if ("SUCCESS" === data.code) {
           logger.i("Upload is successful");
           //Redirect user to login
           paasword.notify(data.message);
           page.redirect("/application/" + applicationID);
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

function addPrivacyConstraintSetHandler(applicationID) {

    var privacyConstraint = new Object();
    privacyConstraint.name = $("#constraintName").val();
    privacyConstraint.applicationID = applicationID;
    privacyConstraint.privacyConstraint = $("#privacyConstraints").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(privacyConstraint),
        url: PRIVACY_CONSTRAINT_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Privacy Constraint created successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationID + "/privacy");

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

function editPrivacyConstraintSetHandler(applicationID, privacyConstraintID) {

    var privacyConstraint = new Object();
    privacyConstraint.id = privacyConstraintID;
    privacyConstraint.name = $("#constraintName").val();
    privacyConstraint.applicationID = applicationID;
    privacyConstraint.privacyConstraint = $("#privacyConstraints").val();

    $("#loader-full").show();

    //Make the add call
    $.ajax({
        type: 'PUT',
        data: JSON.stringify(privacyConstraint),
        url: PRIVACY_CONSTRAINT_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        //Check if the new account is created
        $("#loader-full").hide();
        if ("SUCCESS" === data.code) {
            logger.i("Privacy Constraint created successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationID + "/privacy");

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

function deletePrivacyConstraintHandler(appID, ID) {
    logger.d("Trying to delete Privacy Constraint with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: PRIVACY_CONSTRAINT_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload application page
        page.redirect("/application/" + appID + "/privacy");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addAffinityConstraintSetHandler(applicationID) {

    var affinityConstraint = new Object();
    affinityConstraint.name = $("#constraintName").val();
    affinityConstraint.applicationID = applicationID;
    affinityConstraint.affinityConstraint = $("#affinityConstraints").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(affinityConstraint),
        url: AFFINITY_CONSTRAINT_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Affinity Constraint created successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationID + "/affinity");

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

function editAffinityConstraintSetHandler(applicationID, affinityConstraintID) {

    var affinityConstraint = new Object();
    affinityConstraint.id = affinityConstraintID;
    affinityConstraint.name = $("#constraintName").val();
    affinityConstraint.applicationID = applicationID;
    affinityConstraint.affinityConstraint = $("#affinityConstraints").val();

    $("#loader-full").show();

    //Make the add call
    $.ajax({
        type: 'PUT',
        data: JSON.stringify(affinityConstraint),
        url: AFFINITY_CONSTRAINT_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        //Check if the new account is created
        $("#loader-full").hide();
        if ("SUCCESS" === data.code) {
            logger.i("Affinity Constraint created successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationID + "/affinity");

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

function deleteAffinityConstraintHandler(appID, ID) {
    logger.d("Trying to delete Affinity Constraint with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: AFFINITY_CONSTRAINT_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload application page
        page.redirect("/application/" + appID + "/affinity");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function assignHandlerToApplicationInstanceHandler(applicationID, appInstanceID, handlerID) {

    $("#loader-full").show();

    //Make the add call
    $.post({
         url: APPLICATION_INSTANCE_REST_URL + appInstanceID + "/assignhandler/" + handlerID,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the handler is assigned to the instance

         if ("SUCCESS" === data.code) {
             logger.i("Handler assigned successfully");
             //Redirect user to policy editor
             paasword.notify(data.message);
             page.redirect("/application/" + applicationID + "/instance/" + appInstanceID + "/handler");
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

function unassignHandlerToApplicationInstanceHandler(applicationID, appInstanceID, handlerID) {

    $("#loader-full").show();

    //Make the add call
    $.post({
         url: APPLICATION_INSTANCE_REST_URL + appInstanceID + "/unassignhandler/" + handlerID,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the handler is assigned to the instance

         if ("SUCCESS" === data.code) {
             logger.i("Handler unassigned successfully");
             //Redirect user to policy editor
             paasword.notify(data.message);
             page.redirect("/application/" + applicationID + "/instance/" + appInstanceID + "/handler");
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

function validateInstanceConstraintsHandler() {

    var applicationInstance = new Object();
    applicationInstance.id = $("#instanceID").val();
    applicationInstance.applicationID = $("#applicationID").val();
    applicationInstance.privacyConstraintSetIDs = $("#privacyConstraints").val();
//    applicationInstance.affinityConstraintSetIDs = $("#affinityConstraints").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(applicationInstance),
        url: APPLICATION_INSTANCE_REST_URL + "validate",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        //Check if the new account is created
        $("#loader-full").hide();

        if ("SUCCESS" === data.code) {
            logger.i("Application Instance validated successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + applicationInstance.id + "/proxy");

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

function nextStepApplicationInstanceHandler() {

    var applicationInstance = new Object();
    applicationInstance.name = $("#instanceName").val();
    applicationInstance.description = $("#description").val();
    applicationInstance.applicationID = $("#applicationID").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(applicationInstance),
        url: APPLICATION_INSTANCE_REST_URL + "new",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        //Check if the new account is created
        $("#loader-full").hide();

        if ("SUCCESS" === data.code) {
            logger.i("Application Instance created successfully");
            paasword.notify(data.message);

            var isDataModel = $("#dataModel").val();
            var isPEP = $("#pep").val();

            // Case 1
            if (isDataModel == '0' && isPEP == '0') {
                // Redirect to deployment step
                page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + data.returnobject + "/deploy");

            } else if (isPEP == '1' && isDataModel == '0') {

                // Redirect to deployment step
                page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + data.returnobject + "/deploy");

            } else if (isPEP == '0' && isDataModel == '1') {

                // Redirect to DB Proxy initialization step
                page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + data.returnobject + "/proxy");

            } else {

                // Redirect to DB Proxy initialization step
                page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + data.returnobject + "/proxy");

            }

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

//function validateApplicationInstanceHandler() {
//
//    var applicationInstance = new Object();
//    applicationInstance.id = $("#instanceID").val();
//    applicationInstance.name = $("#instanceName").val();
//    applicationInstance.description = $("#description").val();
//    applicationInstance.applicationID = $("#applicationID").val();
//
//    if ($("#dataModel").val() == '1') {
//        applicationInstance.dataModel = 1;
//        applicationInstance.privacyConstraintSetIDs = $("#privacyConstraints").val();
//        applicationInstance.affinityConstraintSetIDs = $("#affinityConstraints").val();
//    } else {
//        applicationInstance.dataModel = 0;
//    }
//
////    applicationInstance.locationConstraint = $("#locationConstraints").val();
////    applicationInstance.encryptionAlgorithm = $("#encryptionAlgorithm").val();
////    applicationInstance.paaSproviderID = $("#paasProvider").val();
//
//    $("#loader-full").show();
//
//    //Make the add call
//    $.post({
//        data: JSON.stringify(applicationInstance),
//        url: APPLICATION_INSTANCE_REST_URL + "validate",
//        contentType: "application/json; charset=utf-8"
//    }).success(function (data, status, xhr) {
//        //Check if the new account is created
//        $("#loader-full").hide();
//
//        if ("SUCCESS" === data.code) {
//            logger.i("Application Instance validated successfully");
//
//            paasword.notify(data.message);
//
//            page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + data.returnobject);
//
//        } else {
//            paasword.notify("Validation Error", {mode: "notification-error"});
//            $("#errorMessage").html(data.message);
//            $("#errorMessage").show();
//        }
//
//    }).fail(function (error) {
//          $("#loader-full").hide();
//          var response = JSON.parse(error.responseText);
//          paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
//          logger.e("Code: " + error.status + " Message: " + error.responseText);
//      });
//
//}

function changeInstanceConstraintsHandler() {

    var applicationInstance = new Object();
    applicationInstance.id = $("#instanceID").val();
    applicationInstance.applicationID = $("#applicationID").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(applicationInstance),
        url: APPLICATION_INSTANCE_REST_URL + "refragment",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Application Instance reconfigured successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + applicationInstance.id + "/proxy");

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

function initializeInstanceDeployDBProxyHandler() {

    var applicationInstance = new Object();
    applicationInstance.id = $("#instanceID").val();

    var deploymentType = $("#deploymentType").val();

    applicationInstance.dbProxyDeploymentType = deploymentType;
    applicationInstance.applicationID = $("#applicationID").val();
    applicationInstance.privacyConstraintSetIDs = $("#privacyConstraints").val();
    applicationInstance.affinityConstraintSetIDs = $("#affinityConstraints").val();
    applicationInstance.appKey = $("#apiKey").val();

    if (deploymentType === '1') {
        applicationInstance.iaasProviderIDs = $("#iaasProviderSelectBox").val();
    }

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(applicationInstance),
        url: APPLICATION_INSTANCE_REST_URL + "deploydbproxy",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("DB Proxy deployed successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + applicationInstance.id + "/proxy");

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

function deployApplicationInstanceHandler() {

    var applicationInstance = new Object();
    applicationInstance.id = $("#instanceID").val();
    applicationInstance.applicationID = $("#applicationID").val();

    var deploymentType = $("#deploymentType").val();

    applicationInstance.deploymentType = deploymentType;
    applicationInstance.privacyConstraintSetIDs = $("#privacyConstraints").val();
    applicationInstance.affinityConstraintSetIDs = $("#affinityConstraints").val();
    applicationInstance.appKey = $("#apiKey").val();
    applicationInstance.appInstanceKey = $("#appInstanceKey").val();

    if (deploymentType === '1') {
        applicationInstance.paaSproviderID = $("#paasProviderSelectBox").val();
    }

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(applicationInstance),
        url: APPLICATION_INSTANCE_REST_URL + "deploy",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Application Instance deployed successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + applicationInstance.id + "/deploy");

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

function authorizeAppInstanceUserHandler(applicationID) {

    var applicationInstanceUser = new Object();;
    applicationInstanceUser.friendlyName = $("#friendlyName").val();
    applicationInstanceUser.email = $("#email").val();
    applicationInstanceUser.principal = $("#principal").val();
    applicationInstanceUser.applicationInstanceKey = $("#applicationInstanceKey").val();
    applicationInstanceUser.applicationInstanceID = new Object();
    applicationInstanceUser.applicationInstanceID.id = $("#applicationInstanceID").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(applicationInstanceUser),
        url: APPLICATION_INSTANCE_USER_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Application user has been authorized successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationID + "/instance/" + applicationInstanceUser.applicationInstanceID.id + "/keymgmt");

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


function deleteApplicationInstanceUserHandler(applicationID, applicationInstanceUserID) {

    logger.d("Trying to delete Application instance user with id: " + applicationInstanceUserID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: APPLICATION_INSTANCE_USER_REST_URL + applicationInstanceUserID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload application page
        page.redirect("/application/" + applicationID + "/instance/" + applicationInstanceUserID + "/keymgmt");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });

}

function revokeAllKeys(applicationID) {

    $("#loader-full").show();

    //Make the add call
    $.post({
        type: 'DELETE',
        url: APPLICATION_INSTANCE_REST_URL + applicationID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Application user has been authorized successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationID + "/instance/" + applicationInstanceUser.applicationInstanceID.id + "/keymgmt");

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

function startApplicationInstanceHandler() {

    var applicationInstance = new Object();
    applicationInstance.id = $("#instanceID").val();
    applicationInstance.applicationID = $("#applicationID").val();
    var deploymentType = $("#deploymentTypeID").val();

    applicationInstance.deploymentType = deploymentType;

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(applicationInstance),
        url: APPLICATION_INSTANCE_REST_URL + "start",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Application Instance started successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + applicationInstance.id + "/deploy");

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

function stopApplicationInstanceHandler() {

    var applicationInstance = new Object();
    applicationInstance.id = $("#instanceID").val();
    applicationInstance.applicationID = $("#applicationID").val();
    var deploymentType = $("#deploymentTypeID").val();

    applicationInstance.deploymentType = deploymentType;

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(applicationInstance),
        url: APPLICATION_INSTANCE_REST_URL + "stop",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Application Instance stopped successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationInstance.applicationID + "/instance/" + applicationInstance.id + "/deploy");

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

function undeployApplicationInstanceHandler() {

    var applicationInstance = new Object();
    applicationInstance.id = $("#instanceID").val();
    applicationInstance.applicationID = $("#applicationID").val();
    applicationInstance.paaSproviderID = $("#paasProvider").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(applicationInstance),
        url: APPLICATION_INSTANCE_REST_URL + "undeploy",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Application Instance undeployed successfully");

            paasword.notify(data.message);

            page.redirect("/application/" + applicationInstance.applicationID + "/instance");

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

function deleteApplicationInstanceHandler(appInstanceID) {
    logger.d("Trying to delete Application Instance with id: " + appInstanceID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: APPLICATION_INSTANCE_REST_URL + appInstanceID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload application page
        page.redirect("/application/" + appID + "/instance");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function executeQueryHandler() {

    var tQuery = new Object();
    tQuery.query = $("#query").val();
    tQuery.appAPIKey = $("#apiKey").val();
    tQuery.appInstanceAPIKey = $("#appInstanceKey").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(tQuery),
        url: DB_PROXY_REST_URL + "executeraw",
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        //Check if the new account is created
        if ("SUCCESS" === data.code) {
            logger.i("Query executed successfully");

            paasword.notify(data.message);

            $("#queryResults").val(data.returnobject);

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

function editApplicationInstanceRedirect(applicationID, applicationInstanceID) {
    page.redirect('/application/' + applicationID + "/instance/" + applicationInstanceID);
}