/**
 * IaaS Registration
 */

$(document).ready(function () {
    logger.d("Loaded resource.js");
});

var IAAS_PROVIDER_REST_URL = "/api/v1/iaas/";

var PAAS_PROVIDER_REST_URL = "/api/v1/paas/";

var PROXY_CLOUD_PROVIDER_REST_URL = "/api/v1/proxycloudprovider/";

/*
 * Controllers
 */

var resource = new Object();

resource.list = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-paas").addClass("active");
    });
};

resource.paas = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-paas").addClass("active");

    });
};

resource.paas.add = function add(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/resource/paas/add", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
    });
};

resource.paas.edit = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/resource/paas/" + id, function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
    });
};

resource.iaas = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-iaas").addClass("active");

    });
};

resource.iaas.add = function add(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/resource/iaas/add", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
    });
};

resource.iaas.edit = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/resource/iaas/" + id, function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-iaas-info").addClass("active");
    });
};

resource.iaas.images = function images(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/resource/iaas/" + id + "/image", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-iaas").addClass("active");
        $("#menu-iaas-image").addClass("active");
        initializeViewIaaSProviderImagesFragment(id);
    });
};

resource.iaas.addImage = function addImage(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/resource/iaas/" + id + "/image/add", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-iaas").addClass("active");
    });
};

resource.iaas.editImage = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var imageID = ctx.params.imageID;
    $.post("/resource/iaas/" + id + "/image/" + imageID, function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-iaas").addClass("active");
    });
};

resource.iaas.instances = function instances(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/resource/iaas/" + id + "/instance", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-iaas").addClass("active");
        $("#menu-iaas-instance").addClass("active");
    });
};

resource.iaas.addInstance = function addInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/resource/iaas/" + id + "/instance/add", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-iaas").addClass("active");
    });
};

resource.iaas.editInstance = function editInstance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;
    $.post("/resource/iaas/" + id + "/instance/" + instanceID, function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-iaas").addClass("active");
    });
};

resource.slipstream = function slipstream(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $("#loader-full").show();
    $.post("/resource/slipstream", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-slipstream").addClass("active");

        initializeViewProxyCloudProvidersFragment();
        $("#loader-full").hide();
    });
    $("#loader-full").hide();
};

resource.slipstream.authorize = function authorize(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $("#loader-full").show();
    $.post("/resource/slipstream/authorize", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#menu-slipstream").addClass("active");

    });
    $("#loader-full").hide();
};

resource.slipstream.vm = function vm(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $("#loader-full").show();
    $.post("/resource/slipstream/vm", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#loader-full").hide();
    });
    $("#loader-full").hide();
};

resource.slipstream.instance = function instance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $("#loader-full").show();
    $.post("/resource/slipstream/instance", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#loader-full").hide();
    });
    $("#loader-full").hide();
};

resource.slipstream.usage = function instance(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $("#loader-full").show();
    $.post("/resource/slipstream/usage", function (data) {
        $("#content").html(data);
        $("#menu-resource").addClass("active");
        $("#loader-full").hide();
    });
    $("#loader-full").hide();

};

/*
 *  Handlers
 */
function addPaaSProviderHandler() {
    //Create Transfer object
    var paasProvider = new Object();
    paasProvider.friendlyName = $("#friendlyname").val();
    paasProvider.paasProviderTypeID = $("#paasProviderTypeID").val();
    paasProvider.username = $("#username").val();
    paasProvider.password = $("#password").val();
    paasProvider.connectionURL = $("#connectionURL").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(paasProvider),
        url: PAAS_PROVIDER_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the new paas provider is created
        if ("SUCCESS" === data.code) {
            logger.i("PaaS Provider created successfully");
            //Redirect to list view
            paasword.notify(data.message);
            page.redirect("/resource/paas");
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

function editPaaSProviderHandler() {
    //Create Transfer object
    var paasProvider = new Object();
    paasProvider.id = $("#pid").val();
    paasProvider.friendlyName = $("#friendlyname").val();
    paasProvider.paasProviderTypeID = $("#paasProviderTypeID").val();
    paasProvider.username = $("#username").val();
    paasProvider.password = $("#password").val();
    paasProvider.connectionURL = $("#connectionURL").val();

    $("#loader-full").show();

    //Make the add call
    $.ajax({
        type: 'PUT',
        data: JSON.stringify(paasProvider),
        url: PAAS_PROVIDER_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the paas provider is updated
        if ("SUCCESS" === data.code) {
            logger.i("PaaS Provider updated successfully");
            // Redirect to list view
            paasword.notify(data.message);
            page.redirect("/resource/paas");
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

function deletePaaSProviderHandler(ID) {
    logger.d("Trying to delete paas provider with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: PAAS_PROVIDER_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload list view
        page.redirect("/resource/paas");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addIaaSProviderHandler() {
    //Create Transfer object
    var iaasProvider = new Object();

    iaasProvider.friendlyName = $("#friendlyname").val();
    iaasProvider.iaasProviderTypeID = $("#iaasProviderTypeID").val();
    iaasProvider.username = $("#username").val();
    iaasProvider.password = $("#password").val();
    iaasProvider.connectionURL = $("#connectionURL").val();
    iaasProvider.tenantName = $("#tenantName").val();
    iaasProvider.project = $("#project").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(iaasProvider),
        url: IAAS_PROVIDER_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the new iaas provider is created
        if ("SUCCESS" === data.code) {
            logger.i("IaaS Provider created successfully");
            //Redirect to list view
            paasword.notify(data.message);
            page.redirect("/resource/iaas");
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

function editIaaSProviderHandler() {
    //Create Transfer object
    var iaasProvider = new Object();
    iaasProvider.id = $("#iid").val();
    iaasProvider.friendlyName = $("#friendlyname").val();
    iaasProvider.iaasProviderTypeID = $("#iaasProviderTypeID").val();
    iaasProvider.username = $("#username").val();
    iaasProvider.password = $("#password").val();
    iaasProvider.connectionURL = $("#connectionURL").val();
    iaasProvider.tenantName = $("#tenantName").val();
    iaasProvider.project = $("#project").val();

    $("#loader-full").show();

    //Make the add call
    $.ajax({
        type: 'PUT',
        data: JSON.stringify(iaasProvider),
        url: IAAS_PROVIDER_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        //Check if the iaas provider is updated
        $("#loader-full").hide();
        if ("SUCCESS" === data.code) {
            logger.i("IaaS Provider updated successfully");
            //Redirect to list view
            paasword.notify(data.message);
            page.redirect("/resource/iaas");
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

function deleteIaaSProviderHandler(ID) {
    logger.d("Trying to delete iaas provider with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: IAAS_PROVIDER_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload list view
        page.redirect("/resource/iaas");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}


function testIaaSProviderHandler(ID) {
    logger.d("Trying to test connection of iaas provider with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'POST',
        url: IAAS_PROVIDER_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload list view
        page.redirect("/resource/iaas");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function authorizeSlipStreamHandler() {

    var credential = new Object();

    credential.username = $("#username").val();
    credential.password = $("#password").val();

    var user = new Object();
    user.id = $("#uid").val();
    credential.user = user;

    var proxyCloudProvider = new Object();
    proxyCloudProvider.id = $("#proxyCloudProviderID").val();

    credential.proxyCloudProvider = proxyCloudProvider;

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(credential),
        url: PROXY_CLOUD_PROVIDER_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the new iaas provider is created
        if ("SUCCESS" === data.code) {
            logger.i("Proxy Cloud Provider created successfully");
            // Redirect to list view
            paasword.notify(data.message);
            page.redirect("/resource/slipstream");
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

function deauthorizeSlipStreamAccountHandler(ID) {
    logger.d("Trying to deauthorize slipstream account with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: PROXY_CLOUD_PROVIDER_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload list view
        page.redirect("/resource/slipstream");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

//function addIaaSProviderImageHandler() {
//    //Create Transfer object
//    var iaasProviderImage = new Object();
//
//    iaasProviderImage.friendlyName = $("#friendlyname").val();
//    iaasProviderImage.imageID = $("#imageID").val();
//    iaasProviderImage.iaasProviderID = $("#iaasProviderID").val();
//
//    $("#loader-full").hide();
//
//    //Make the add call
//    $.post({
//        data: JSON.stringify(iaasProviderImage),
//        url: IAAS_PROVIDER_REST_URL + "/image",
//        contentType: "application/json; charset=utf-8"
//    }).success(function (data, status, xhr) {
//        $("#loader-full").hide();
//        //Check if the new account is created
//        if ("SUCCESS" === data.code) {
//            logger.i("Registration is success");
//            //Redirect to list view
//            paasword.notify(data.message);
//            page.redirect("/resource/iaas/" + iaasProviderImage.iaasProviderID + "/image");
//        } else {
//            paasword.notify(data.message, {mode: "notification-error"});
//        }
//
//    });
//}
//
//function editIaaSProviderImageHandler() {
//    //Create Transfer object
//    var iaasProviderImage = new Object();
//    iaasProviderImage.id = $("#id").val();
//    iaasProviderImage.friendlyName = $("#friendlyname").val();
//    iaasProviderImage.imageID = $("#imageID").val();
//    iaasProviderImage.iaasProviderID = $("#iaasProviderID").val();
//
//    //Make the add call
//    $.ajax({
//        type: "PUT",
//        data: JSON.stringify(iaasProviderImage),
//        url: IAAS_PROVIDER_REST_URL + "/image",
//        contentType: "application/json; charset=utf-8"
//    }).success(function (data, status, xhr) {
//        //Check if the new account is created
//        if ("SUCCESS" === data.code) {
//            logger.i("Update is success");
//            //Redirect to list view
//            paasword.notify(data.message);
//            page.redirect("/resource/iaas/" + iaasProviderImage.iaasProviderID + "/image");
//        } else {
//            paasword.notify(data.message, {mode: "notification-error"});
//        }
//
//    });
//}
//
//function deleteIaaSProviderImageHandler(imageID, iaasProviderID) {
//
//    //Make the delete call
//    $.ajax({
//        type: "DELETE",
//        url: IAAS_PROVIDER_REST_URL + "/image/" + imageID,
//        contentType: "application/json; charset=utf-8"
//    }).success(function (data, status, xhr) {
//        //Check if the new account is created
//        if ("SUCCESS" === data.code) {
//            logger.i("Delete is success");
//            //Redirect to list view
//            paasword.notify(data.message);
//            page.redirect("/resource/iaas/" + iaasProviderID + "/image");
//        } else {
//            paasword.notify(data.message, {mode: "notification-error"});
//        }
//
//    });
//}
//
//function addIaaSProviderInstanceHandler() {
//    //Create Transfer object
//    var iaasProviderInstance = new Object();
//
//    iaasProviderInstance.friendlyName = $("#friendlyname").val();
//    iaasProviderInstance.imageID = $("#imageID").val();
//    iaasProviderInstance.flavorID = $("#flavorID").val();
//    iaasProviderInstance.iaasProviderID = $("#iaasProviderID").val();
//
//    //Make the add call
//    $.post({
//        data: JSON.stringify(iaasProviderInstance),
//        url: IAAS_PROVIDER_REST_URL + "/instance",
//        contentType: "application/json; charset=utf-8"
//    }).success(function (data, status, xhr) {
//        //Check if the new account is created
//        if ("SUCCESS" === data.code) {
//            logger.i("Registration is success");
//            //Redirect to list view
//            paasword.notify(data.message);
//            page.redirect("/resource/iaas/" + iaasProviderImage.iaasProviderID + "/instance");
//        } else {
//            paasword.notify(data.message, {mode: "notification-error"});
//        }
//
//    });
//}
//
//function editIaaSProviderInstanceHandler() {
//    //Create Transfer object
//    var iaasProviderInstance = new Object();
//    iaasProviderInstance.id = $("#id").val();
//    iaasProviderInstance.friendlyName = $("#friendlyname").val();
//    iaasProviderInstance.imageID = $("#imageID").val();
//    iaasProviderInstance.flavorID = $("#flavorID").val();
//    iaasProviderInstance.iaasProviderID = $("#iaasProviderID").val();
//
//    //Make the add call
//    $.ajax({
//        type: "PUT",
//        data: JSON.stringify(iaasProviderInstance),
//        url: IAAS_PROVIDER_REST_URL + "/instance",
//        contentType: "application/json; charset=utf-8"
//    }).success(function (data, status, xhr) {
//        //Check if the new account is created
//        if ("SUCCESS" === data.code) {
//            logger.i("Update is success");
//            //Redirect to list view
//            paasword.notify(data.message);
//            page.redirect("/resource/iaas/" + iaasProviderImage.iaasProviderID + "/instance");
//        } else {
//            paasword.notify(data.message, {mode: "notification-error"});
//        }
//
//    });
//}
//
//function deleteIaaSProviderInstanceHandler(instanceID, iaasProviderID) {
//
//    //Make the delete call
//    $.ajax({
//        type: "DELETE",
//        url: IAAS_PROVIDER_REST_URL + "/instance/" + instanceID,
//        contentType: "application/json; charset=utf-8"
//    }).success(function (data, status, xhr) {
//        //Check if the new account is created
//        if ("SUCCESS" === data.code) {
//            logger.i("Delete is success");
//            //Redirect to list view
//            paasword.notify(data.message);
//            page.redirect("/resource/iaas/" + iaasProviderID + "/instance");
//        } else {
//            paasword.notify(data.message, {mode: "notification-error"});
//        }
//
//    });
//}

/*
 *  Actions
 */
function initializeViewProxyCloudProvidersFragment() {

    $("#slipstream-table").bootgrid({
            cssClass: 'text-center',
            columnSelection: false,
            caseSensitive: false,
            formatters: {
                "commands": function (column, row) {
                    return "<span onclick='editSlipStreamRedirect("  +  row.id +")' data-row-id='" + row.id + "' class='oi oi-pencil'></span> " +
//                            "<span onclick='testIaaSProviderHandler("  +  row.id +")' data-row-id='" + row.id + "' class='oi oi-monitor'></span> " +
//                            "<span onclick='viewIaaSProviderImagesHandler("  +  row.id +")' data-row-id='" + row.id + "' class='oi oi-target'></span> " +
//                            "<span onclick='viewIaaSProviderInstancesHandler("  +  row.id +")' data-row-id='" + row.id + "' class='oi oi-sun'></span> " +
                            "<span onclick='deleteSlipStreamHandler("  +  row.id +")' data-row-id='" + row.id + "' class='oi oi-trash'></span>";

                }
            }

        });

        $('.bootgrid-header').css({'height':"40px"});

}

function editIaaSProviderRedirect(iaasProviderID) {
    page.redirect('/resource/iaas/' + iaasProviderID);
}


function editPaaSProviderRedirect(paasProviderID) {
    page.redirect('/resource/paas/' + paasProviderID);
}

function viewIaaSProviderImagesHandler(iaasProviderID) {
    page.redirect('/resource/iaas/' + iaasProviderID + '/image');
}

function viewIaaSProviderInstancesHandler(iaasProviderID) {
    page.redirect('/resource/iaas/' + iaasProviderID + '/instance');
}

function initializeViewIaaSProviderImagesFragment(iaasProviderID) {

    $("#iaas-images").bootgrid({
            cssClass: 'text-center',
            columnSelection: false,
            caseSensitive: false,
            formatters: {
                "commands": function (column, row) {
                    return "<span onclick='editIaaSProviderImageRedirect("  +  row.id +"," + iaasProviderID + ")' data-row-id='" + row.id + "' class='oi oi-pencil'></span> " +
                            "<span onclick='deleteIaaSProviderImageHandler("  +  row.id +"," + + iaasProviderID + ")' data-row-id='" + row.id + "' class='oi oi-trash'></span>";

                }
            }

        });

        $('.bootgrid-header').css({'height':"40px"});

}

function editIaaSProviderImageRedirect(imageID, iaasProviderID) {
    page.redirect('/resource/iaas/' + iaasProviderID + '/image/' + imageID);
}

function editIaaSProviderInstanceRedirect(instanceID, iaasProviderID) {
    page.redirect('/resource/iaas/' + iaasProviderID + '/instance/' + instanceID);
}