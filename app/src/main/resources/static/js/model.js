/**
 * User
 */

$(document).ready(function () {
    logger.d("Loaded model.js");
});

// Static variables
var MODEL_REST_URL = "/api/v1/model/";

var CLASSES_REST_URL = "/api/v1/class/";

var HANDLERS_REST_URL = "/api/v1/handler/";

var EXPRESSION_REST_URL = "/api/v1/expression/";

var INSTANCES_REST_URL = "/api/v1/instance/";

var PROPERTIES_REST_URL = "/api/v1/property/";

var RULES_REST_URL = "/api/v1/rule/";

var POLICIES_REST_URL = "/api/v1/policy/";

var POLICY_SETS_REST_URL = "/api/v1/policyset/";

var NAMESPACES_REST_URL = "/api/v1/namespace/";

/*
 * Controllers
 */

var model = new Object();

model.namespace = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);

    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-namespaces").addClass("active");

//        initializeViewNamespacesFragment();
    });
};

model.namespace.addNamespace = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/model/namespace/add", function (data) {
        $("#content").html(data);
        $("#menu-model-namespaces").addClass("active");
    });
};

model.namespace.editNamespace = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/namespace/" + id, function (data) {
        $("#content").html(data);
        $("#menu-model-namespaces").addClass("active");
    });
};

model.handler = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-handlers").addClass("active");


    });
};

model.handler.addHandler = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-handlers").addClass("active");

        initializeAutocompleteForHandlerView();
    });
};

model.handler.editHandler = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/handler/" + id, function (data) {
        $("#content").html(data);
        $("#menu-model-handlers").addClass("active");

        initializeAutocompleteForHandlerView();

    });
};

model.hlo = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/model/hlo", function (data) {
        $("#content").html(data);
        $("#menu-model-hlo").addClass("active");
    });
};

model.lifecycle = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/model/lifecycle", function (data) {
        $("#content").html(data);
        $("#menu-model-lifecycle").addClass("active");
    });
};

model.context = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/model/context", function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");
        $("#menu-security-context-element").addClass("active");
        loadClassesForTreeGrid(1);
    });
};

model.context.object = function object(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/model/context/object", function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");
        $("#menu-object").addClass("active");
        loadClassesForTreeGrid(2);
    });
};

model.context.request = function request(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/model/context/request", function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");
        $("#menu-request").addClass("active");
        loadClassesForTreeGrid(3);
    });
};

model.context.subject = function subject(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/model/context/subject", function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");
        $("#menu-subject").addClass("active");
        loadClassesForTreeGrid(4);
    });
};

model.context.pattern = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post("/model/context/pattern", function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");
        $("#menu-context-pattern").addClass("active");
        loadClassesForTreeGrid(5);
    });
};

model.context.addClass = function add(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");

        initializeAddClassFragment();

    });
};

model.context.editClass = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/context/class/" + id, function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");
        $("#menu-edit-class").addClass("active");

        initializeEditClassFragment();

    });
};

model.context.viewInstances = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/context/class/" + id + "/instance", function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");
        $("#menu-instances").addClass("active");

        initializeViewInstancesFragment(id);

    });
};

model.context.addInstance = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/context/class/" + id + "/instance/add", function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");

        initializeAddEditInstanceFragment();

    });
};

model.context.editInstance = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var instanceID = ctx.params.instanceID;
    $.post("/model/context/class/" + id + "/instance/" + instanceID, function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");

        initializeAddEditInstanceFragment();
    });
};

model.context.viewProperties = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/context/class/" + id + "/property", function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");
        $("#menu-properties").addClass("active");
    });
};

model.context.addProperty = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/context/class/" + id + "/property/add", function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");

        $("#transitivity").attr("disabled", true);

        initializeAddPropertyFragment();


    });
};

model.context.editProperty = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    var propertyID = ctx.params.propertyID;
    $.post("/model/context/class/" + id + "/property/" + propertyID, function (data) {
        $("#content").html(data);
        $("#menu-model-context").addClass("active");

        $("#transitivity").attr("disabled", true);

        initializeEditPropertyFragment();
    });
};

model.expression = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-expression").addClass("active");
    });
};

model.expression.add = function add(ctx) {
    logger.i("Current page set to: " + ctx.pathname);

    $("#loader-full").show();

    $.post("/model/expression/add", function (data) {
        $("#content").html(data);
        $("#menu-model-expression").addClass("active");
        $("#errorMessage").hide();

        $("body").tooltip({
            selector: 'a[rel=tooltip]',
            html: true,
            placement: "right"
        });

        $(".chosen-select").chosen({width: "300px"});

        $("#loader-full").hide();

    });
};


model.expression.edit = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $("#loader-full").show();

    $.post("/model/expression/" + id, function (data) {
        $("#content").html(data);
        $("#menu-model-expression").addClass("active");
        $("#errorMessage").hide();

        $("body").tooltip({
            selector: 'a[rel=tooltip]',
            html: true,
            placement: "right"
        });

        $(".chosen-select").chosen({width: "300px"});

        loadQueryBuilder('edit');

        $("#loader-full").hide();

    });
};

model.policyset = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#menu-policy-set").addClass("active");
    });
};

model.policyset.add = function add(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
//    $.post("/model/policyset/add", function (data) {
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#errorMessage").hide();

        initializeAddPolicySetFragment();

    });
};

model.policyset.edit = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/policyset/" + id, function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#errorMessage").hide();

        initializeEditPolicySetFragment();
    });
};

model.policyset.addRemovePolicies = function addRemovePolicies(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/policyset/" + id + "/policy", function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#errorMessage").hide();

    });
};

model.policy = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#menu-policy").addClass("active");
    });
};

model.policy.add = function add(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
//    $.post("/model/policy/add", function (data) {
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#errorMessage").hide();

        initializeAddPolicyFragment();

    });
};

model.policy.edit = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/policy/" + id, function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#errorMessage").hide();

        initializeEditPolicyFragment();

    });
};

model.policy.addRemoveRules = function addRemoveRules(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/policy/" + id + "/rule", function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#errorMessage").hide();
    });
};

model.rule = function list(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#menu-rule").addClass("active");
    });
};

model.rule.add = function add(ctx) {
    logger.i("Current page set to: " + ctx.pathname);

//    $.post("/model/rule/add", function (data) {
    $.post(ctx.canonicalPath, function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#errorMessage").hide();

        initializeOnChangeOfPermissionTypeFunctionality();

    });
};

model.rule.edit = function edit(ctx) {
    logger.i("Current page set to: " + ctx.pathname);
    var id = ctx.params.id;
    $.post("/model/rule/" + id, function (data) {
        $("#content").html(data);
        $("#menu-model-policyset").addClass("active");
        $("#errorMessage").hide();

        initializeOnChangeOfPermissionTypeFunctionality();

//        initializeOnChangeOfExpressionFunctionality();

    });
};

/*
 *  Handlers
 */
function addHandlerHandler() {

    //Create Handler object
    var handler = new Object();

    handler.handlerName = $("#handlerName").val();
    handler.restEndpointURI = $("#restEndpointURI").val();
    handler.hasInputID = $("#hasInputID").val();
    handler.hasOutputID = $("#hasOutputID").val();
    handler.namespaceID = $("#namespace").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(handler),
        url: HANDLERS_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the new handler is created
        if ("SUCCESS" === data.code) {
            logger.i("Handler created successfully");
            // Redirect user to namespaces
            paasword.notify(data.message);
            page.redirect("/model/handler");
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

function editHandlerHandler() {

    //Edit Handler object
    var handler = new Object();

    handler.id = $("#hid").val();
    handler.handlerName = $("#handlerName").val();
    handler.restEndpointURI = $("#restEndpointURI").val();
    handler.hasInputID = $("#hasInputID").val();
    handler.hasOutputID = $("#hasOutputID").val();
    handler.namespaceID = $("#namespace").val();

    $("#loader-full").show();

    //Make the update call
    $.ajax({
        type: 'PUT',
        data: JSON.stringify(handler),
        url: HANDLERS_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the handler is updated
        if ("SUCCESS" === data.code) {
          logger.i("Handler updated successfully");
          //Redirect user to handler
          paasword.notify(data.message);
          page.redirect("/model/handler");
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

function deleteHandlerHandler(ID) {
    logger.d("Trying to delete handler with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: HANDLERS_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload users page
        page.redirect("/model/handler");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addNamespaceHandler() {

    //Create Namespace object
    var namespace = new Object();

    namespace.name = $("#namespaceName").val();
    namespace.prefix = $("#namespacePrefix").val();
    namespace.uri = $("#namespaceURI").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
         data: JSON.stringify(namespace),
         url: NAMESPACES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the new namespace is created
        if ("SUCCESS" === data.code) {
            logger.i("Namespace added successfully");
            //Redirect user to namespaces
            paasword.notify(data.message);
            page.redirect("/model/namespace");
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

function editNamespaceHandler() {

    //Edit Namespace object
    var namespace = new Object();

    namespace.id = $("#nid").val();
    namespace.name = $("#namespaceName").val();
    namespace.prefix = $("#namespacePrefix").val();
    namespace.uri = $("#namespaceURI").val();

    $("#loader-full").show();

    //Make the update call
    $.ajax({
         type: 'PUT',
         data: JSON.stringify(namespace),
         url: NAMESPACES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        //Check if the namespace is updated
        if ("SUCCESS" === data.code) {
            logger.i("Namespace updated successfully");
            //Redirect user to namespaces
            paasword.notify(data.message);
            page.redirect("/model/namespace");
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

function deleteNamespaceHandler(ID) {
    logger.d("Trying to delete namespace with id: " + ID);
    $("#loader-full").show();
    $.ajax({
        type: 'DELETE',
        url: NAMESPACES_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload users page
        page.redirect("/model/namespace");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addClazzHandler() {

    //Create Clazz object
    var clazz = new Object();

    clazz.className = $("#className").val();

    if ($("#hasParent").is(':checked')) {
        clazz.parentID = $("#parentID").val();
    } else {
        clazz.parentID = $("#rootClassID").val();
    }

    clazz.rootID = $("#rootClassID").val();

    clazz.namespaceID = $("#namespace").val();

    var rootID = $("#rootClassID").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
         data: JSON.stringify(clazz),
         url: CLASSES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
         //Check if the new class is created
         $("#loader-full").hide();

         if ("SUCCESS" === data.code) {

             logger.i("Class created successfully");
             //Redirect user to context model
             paasword.notify(data.message);

             if (rootID === '1') {
                page.redirect("/model/context");
             } else if (rootID === '2') {
                page.redirect("/model/context/pattern");
             } else if (rootID === '3') {
                page.redirect("/model/context/permission");
             } else if (rootID === '4') {
                page.redirect("/model/context/dde");
             } else if (rootID === '5') {
                page.redirect("/model/context/external");
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

function editClazzHandler() {
    //Construct Clazz object
    var clazz = new Object();
    clazz.id = $("#cid").val();
    clazz.className = $("#className").val();

    if ($("#hasParent").is(':checked')) {
        clazz.parentID = $("#parentID").val();
    }

    clazz.rootID = $("#rootClassID").val();

    clazz.namespaceID = $("#namespace").val();

    var rootID = $("#rootClassID").val();

    $("#loader-full").show();

    //Make the add call
    $.ajax({
         type: 'PUT',
         data: JSON.stringify(clazz),
         url: CLASSES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

         $("#loader-full").hide();

         //Check if the class is updated
         if ("SUCCESS" === data.code) {
             logger.i("Class updated successfully");
             //Redirect user to context model
             paasword.notify(data.message);
             if (rootID === '1') {
                page.redirect("/model/context");
             } else if (rootID === '2') {
                page.redirect("/model/context/pattern");
             } else if (rootID === '3') {
                page.redirect("/model/context/permission");
             } else if (rootID === '4') {
                page.redirect("/model/context/dde");
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

function deleteClazzHandler(ID) {
    logger.d("Trying to delete clazz with id: " + ID);

    $("#loader-full").show();

    $.ajax({
        type: 'DELETE',
        url: CLASSES_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        paasword.notify(data.message);
        //Reload users page
        page.redirect("/model/context");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addExpressionHandler() {
    //Create Expression object
    var expression = new Object();
    expression.expressionName = $("#expressionName").val();
    expression.namespaceID = $("#namespace").val();
    expression.description = $("#description").val();
    expression.instances = $("#instances").val();
    expression.condition = $("#condition").val();
    expression.expressions = $("#referredExpressions").val();
    var exp = $('#builder').queryBuilder('getRules');
    expression.expression =  JSON.stringify(exp, null, 2);

    $("#loader-full").show();

    //Make the add call
    $.post({
        data: JSON.stringify(expression),
        url: EXPRESSION_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        //Check if the new expression is created
        if ("SUCCESS" === data.code) {
            logger.i("Expression created successfully");
            //Redirect user to login
            paasword.notify(data.message);
            page.redirect("/model/expression");
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

function editExpressionHandler() {
    // Create Expression object
    var expression = new Object();
    expression.id = $("#eid").val();
    expression.namespaceID = $("#namespace").val();
    expression.expressionName = $("#expressionName").val();
    expression.instances = $("#instances").val();
    expression.expressions = $("#referredExpressions").val();
    expression.condition = $("#condition").val();
    expression.description = $("#description").val();
    var exp = $('#builder').queryBuilder('getRules');
    expression.expression =  JSON.stringify(exp, null, 2);

    $("#loader-full").show();

    //Make the add call
    $.ajax({
        type: 'PUT',
        data: JSON.stringify(expression),
        url: EXPRESSION_REST_URL,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        //Check if expression is updated
        if ("SUCCESS" === data.code) {
            logger.i("Expression updated successfully");
            //Redirect user to login
            paasword.notify(data.message);
            page.redirect("/model/expression");
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

function deleteExpressionHandler(ID) {
    logger.d("Trying to delete expression with id: " + ID);

    $("#loader-full").show();

    $.ajax({
        type: 'DELETE',
        url: EXPRESSION_REST_URL + ID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        paasword.notify(data.message);
        //Reload users page
        page.redirect("/model/expression");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addInstanceHandler(classID) {

    //Create Instance object
    var instance = new Object();
    instance.instanceName = $("#instanceName").val();
    instance.classID = classID;

    instance.namespaceID = $("#namespace").val();

    var propertyInstances = [];

    $(".property" ).each(function() {

       var propertyInstance = new Object();

       propertyInstance.propertyID = $(this).attr('id');;

       propertyInstance.name = $(this).val();

       propertyInstances.push(propertyInstance);

    });

    instance.propertyInstances = propertyInstances;

    $("#loader-full").show();

    //Make the add call
    $.post({
         data: JSON.stringify(instance),
         url: INSTANCES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

         $("#loader-full").hide();

         //Check if the new instance is created
         if ("SUCCESS" === data.code) {
             logger.i("Instance created successfully");
             //Redirect user to context model
             paasword.notify(data.message);
             page.redirect("/model/context/class/" + classID + "/instance");
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

function editInstanceHandler(classID) {
    //Create Instance object
    var instance = new Object();
    instance.id = $("#iid").val();
    instance.instanceName = $("#instanceName").val();
    instance.classID = classID;

    instance.namespaceID = $("#namespace").val();

    var propertyInstances = [];

    $(".property" ).each(function() {

       var propertyInstance = new Object();

       propertyInstance.propertyID = $(this).attr('id');;

       propertyInstance.name = $(this).val();

       propertyInstances.push(propertyInstance);

    });

    instance.propertyInstances = propertyInstances;

    $("#loader-full").show();

    //Make the add call
    $.ajax({
         type: 'PUT',
         data: JSON.stringify(instance),
         url: INSTANCES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the instance is updated
         if ("SUCCESS" === data.code) {
             logger.i("Instance updated successfully");
             //Redirect user to context model
             paasword.notify(data.message);
             page.redirect("/model/context/class/" + classID + "/instance");
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

function deleteInstanceHandler(instanceID, classID) {
    logger.d("Trying to delete instance with id: " + instanceID);

    $("#loader-full").show();

    $.ajax({
        type: 'DELETE',
        url: INSTANCES_REST_URL + instanceID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();
        paasword.notify(data.message);
        page.redirect("/model/context/class/" + classID + "/instance");

    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addPropertyHandler(classID) {

    //Create Property object

    var property = new Object();
    property.name = $("#propertyName").val();
    property.classID = classID;

    property.namespaceID = $("#namespace").val();

    if (!$("#isObjectProperty").is(':checked')) {

        property.propertyTypeID = $("#propertyType").val();
        property.objectProperty = false;
        property.transitivity = 0;

    } else {

        property.objectProperty = true;
        property.objectPropertyClassID = $("#objectPropertyClassID").val();
        property.transitivity = $("#transitivity").val();
    }

    if ($("#isSubPropertyOf").is(':checked')) {
        property.subPropertyOfID = $("#subPropertyOfID").val();
    } else {
        property.subPropertyOfID = 0;
    }
    $("#loader-full").show();

    //Make the add call
    $.post({
         data: JSON.stringify(property),
         url: PROPERTIES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the new property is created
         if ("SUCCESS" === data.code) {
             logger.i("Property created successfully");
             //Redirect user to context model
             paasword.notify(data.message);
             page.redirect("/model/context/class/" + classID + "/property");
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

function editPropertyHandler(classID) {

    //Create Property object

    var property = new Object();
    property.id = $("#pid").val();
    property.name = $("#propertyName").val();
    property.classID = classID;

    property.namespaceID = $("#namespace").val();

    if (!$("#isObjectProperty").is(':checked')) {

        property.propertyTypeID = $("#propertyType").val();
        property.objectProperty = false;
        property.transitivity = 0;

    } else {

        property.objectProperty = true;
        property.transitivity = $("#transitivity").val();
        property.objectPropertyClassID = $("#objectPropertyClassID").val();
    }

    if ($("#isSubPropertyOf").is(':checked')) {
        property.subPropertyOfID = $("#subPropertyOfID").val();
    } else {
        property.subPropertyOfID = 0;
    }

    $("#loader-full").show();

    //Make the add call
    $.ajax({
         type: "PUT",
         data: JSON.stringify(property),
         url: PROPERTIES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the property is updated
         if ("SUCCESS" === data.code) {
             logger.i("Property updated successfully");
             //Redirect user to context model
             paasword.notify(data.message);
             page.redirect("/model/context/class/" + classID + "/property");
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

function deletePropertyHandler(propertyID, classID) {

    $("#loader-full").show();

    logger.d("Trying to delete property with id: " + propertyID);
    $.ajax({
        type: 'DELETE',
        url: PROPERTIES_REST_URL + propertyID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        paasword.notify(data.message);
        //Reload users page
        page.redirect("/model/context/class/" + classID + "/property");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addRuleHandler() {
    //Create Rule object
    var rule = new Object();
    rule.ruleName = $("#ruleName").val();
    rule.description = $("#description").val();
    rule.namespaceID = $("#namespace").val();
    rule.controlledObject = $("#controlledObject").val();
    rule.authorization = $("#authorization").val();
    rule.action = $("#action").val();
    rule.actor = $("#actor").val();
    rule.expressionID = $("#expression").val();
    rule.permissionType = $("#permissionType").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
         data: JSON.stringify(rule),
         url: RULES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the new rule is created
         if ("SUCCESS" === data.code) {
             logger.i("Rule created successfully");
             //Redirect user to context model
             paasword.notify(data.message);
             page.redirect("/model/rule");
         } else {

            paasword.notify("Validation Error", {mode: "notification-error"});

            $("#errorMessage").html(data.message);

            $("#errorMessage").show();
         }

    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function editRuleHandler() {
    //Create Rule object
    var rule = new Object();

    rule.id = $("#rid").val();
    rule.ruleName = $("#ruleName").val();
    rule.description = $("#description").val();
    rule.controlledObject = $("#controlledObject").val();
    rule.authorization = $("#authorization").val();
    rule.namespaceID = $("#namespace").val();
    rule.action = $("#action").val();
    rule.actor = $("#actor").val();
    rule.expressionID = $("#expression").val();
    rule.permissionType = $("#permissionType").val();

    $("#loader-full").show();

    //Make the add call
    $.ajax({
         type: 'PUT',
         data: JSON.stringify(rule),
         url: RULES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the rule is updated
         if ("SUCCESS" === data.code) {
             logger.i("Rule updated successfully");
             //Redirect user to context model
             paasword.notify(data.message);
             page.redirect("/model/rule");

         } else {

            paasword.notify("Validation Error", {mode: "notification-error"});

            $("#errorMessage").html(data.message);

            $("#errorMessage").show();
         }

    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function deleteRuleHandler(ruleID) {
    logger.d("Trying to delete rule with id: " + ruleID);

    $("#loader-full").show();

    $.ajax({
        type: 'DELETE',
        url: RULES_REST_URL + ruleID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        paasword.notify(data.message);
        //Reload users page
        page.redirect("/model/rule");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addPolicyHandler() {

//    var rules = $('select#rules').val();
//    var rulesCustom = "#" + rules + "#";

    // Create Policy object
    var policy = new Object();
    policy.policyName = $("#policyName").val();
    policy.policyCombiningAlgorithmID = $("#policyCombiningAlgorithm").val();
    policy.namespaceID = $("#namespace").val();
//    policy.rulesCustom = rulesCustom;
    policy.description = $("#description").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
         data: JSON.stringify(policy),
         url: POLICIES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the new policy is created
         if ("SUCCESS" === data.code) {
             logger.i("Policy created successfully");
             //Redirect user to policy editor
             paasword.notify(data.message);
             page.redirect("/model/policy");
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

function editPolicyHandler() {

    var rules = $('select#rules').val();
    var rulesCustom = "#" + rules + "#";

    // Create Policy object
    var policy = new Object();
    policy.id = $("#pid").val();
    policy.policyName = $("#policyName").val();
    policy.policyCombiningAlgorithmID = $("#policyCombiningAlgorithm").val();
    policy.namespaceID = $("#namespace").val();
    policy.rulesCustom = rulesCustom;
    policy.description = $("#description").val();

    $("#loader-full").show();

    //Make the add call
    $.ajax({
         type: "PUT",
         data: JSON.stringify(policy),
         url: POLICIES_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the policy is updated
         if ("SUCCESS" === data.code) {
             logger.i("Policy updated successfully");
             //Redirect user to policy editor
             paasword.notify(data.message);
             page.redirect("/model/policy");
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

function assignRuleToPolicyHandler(policyID) {

    var ruleToBeAssigned = $("#rule").val();

    $("#loader-full").show();

    //Make the add call
    $.post({

         url: POLICIES_REST_URL + policyID + "/assign/" + ruleToBeAssigned,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the new rule is assigned to policy

         if ("SUCCESS" === data.code) {
             logger.i("Rule assigned successfully");
             //Redirect user to policy editor
             paasword.notify(data.message);
             page.redirect("/model/policy");
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

function unassignRuleToPolicyHandler(policyID, ruleID) {

    $("#loader-full").show();

    //Make the add call
    $.post({

         url: POLICIES_REST_URL + policyID + "/unassign/" + ruleID,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the new rule is assigned to policy

         if ("SUCCESS" === data.code) {
             logger.i("Rule un-assigned successfully");
             //Redirect user to policy editor
             paasword.notify(data.message);
             page.redirect("/model/policy");
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

function deletePolicyHandler(policyID) {
    logger.d("Trying to delete policy with id: " + policyID);

    $("#loader-full").show();

    $.ajax({
        type: 'DELETE',
        url: POLICIES_REST_URL + policyID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        paasword.notify(data.message);
        //Reload users page
        page.redirect("/model/policy");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

function addPolicySetHandler() {

//    var policies = $('select#policies').val();
//    var policiesCustom = "#" + policies + "#";

    // Create PolicySet object
    var policySet = new Object();
    policySet.policySetName = $("#policySetName").val();
    policySet.policySetCombiningAlgorithmID = $("#policySetCombiningAlgorithm").val();
    policySet.namespaceID = $("#namespace").val();
//    policySet.policiesCustom = policiesCustom;
    policySet.description = $("#description").val();

    $("#loader-full").show();

    //Make the add call
    $.post({
         data: JSON.stringify(policySet),
         url: POLICY_SETS_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the new policy set is created
         if ("SUCCESS" === data.code) {
             logger.i("Policy Set created successfully");
             //Redirect user to policy editor
             paasword.notify(data.message);
             page.redirect("/model/policyset");
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

function editPolicySetHandler() {

//    var policies = $('select#policies').val();
//    var policiesCustom = "#" + policies + "#";

    // Create PolicySet object
    var policySet = new Object();
    policySet.id = $("#pid").val();
    policySet.policySetName = $("#policySetName").val();
    policySet.policySetCombiningAlgorithmID = $("#policySetCombiningAlgorithm").val();
    policySet.namespaceID = $("#namespace").val();
//    policySet.policiesCustom = policiesCustom;
    policySet.description = $("#description").val();

    $("#loader-full").show();

    //Make the add call
    $.ajax({
         type: "PUT",
         data: JSON.stringify(policySet),
         url: POLICY_SETS_REST_URL,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the new rule is created
         if ("SUCCESS" === data.code) {
             logger.i("Registration is successful");
             //Redirect user to policy set editor
             paasword.notify(data.message);
             page.redirect("/model/policyset");
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

function assignPolicyToPolicySetHandler(policySetID) {

    var policyToBeAssigned = $("#policy").val();

    $("#loader-full").show();

    //Make the add call
    $.post({

         url: POLICY_SETS_REST_URL + policySetID + "/assign/" + policyToBeAssigned,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the new policy is assigned to policy set

         if ("SUCCESS" === data.code) {
             logger.i("Policy assigned successfully");
             //Redirect user to policy editor
             paasword.notify(data.message);
             page.redirect("/model/policyset");
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

function unassignPolicyToPolicySetHandler(policySetID, policyID) {

    $("#loader-full").show();

    //Make the add call
    $.post({
         url: POLICY_SETS_REST_URL + policySetID + "/unassign/" + policyID,
         contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

         //Check if the new rule is assigned to policy

         if ("SUCCESS" === data.code) {
             logger.i("Policy un-assigned successfully");
             //Redirect user to policy editor
             paasword.notify(data.message);
             page.redirect("/model/policyset");
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

function deletePolicySetHandler(policySetID) {
    logger.d("Trying to delete policy set with id: " + policySetID);

    $("#loader-full").show();

    $.ajax({
        type: 'DELETE',
        url: POLICY_SETS_REST_URL + policySetID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {

        $("#loader-full").hide();

        paasword.notify(data.message);
        //Reload users page
        page.redirect("/model/policyset");
    }).fail(function (error) {
        $("#loader-full").hide();
        var response = JSON.parse(error.responseText);
        paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
        logger.e("Code: " + error.status + " Message: " + error.responseText);
    });
}

/*
 *  Loading Data
 */
function loadClassesForTreeGrid(rootClassID) {

    $("#loader-full").show();

    $.get({
        url: CLASSES_REST_URL + "tree/" + rootClassID,
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        if ("SUCCESS" === data.code) {
            logger.i("Classes loaded successfully!");
            paasword.notify(data.message);
            var classes = $.parseJSON(data.returnobject);
            constructTreeGrid(classes, rootClassID);
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

function constructTreeGrid(classes, rootClassID) {

    $("#loader-full").show();

    var $tree = $('#tree1');

    $tree.tree({
        data: classes,
        autoOpen: true,
        onCreateLi: function(node, $li) {


            if (rootClassID !== 3  && rootClassID !== 4) {


                $li.find('.jqtree-element').append(
                    ' <a class="btn btn-xs btn-default edit" data-node-id="'+ node.id +'"> view </a>'
//                    + '&nbsp;<button class="btn btn-xs btn-default delete" data-node-id="'+ node.id +'"> delete </button>'
//                    + '&nbsp;<button class="btn btn-xs btn-default viewProperties" data-node-id="'+ node.id +'"> view properties </button>'
//                    + '&nbsp;<button class="btn btn-xs btn-default viewInstances" data-node-id="'+ node.id +'"> view instances </button>'
//                    + '&nbsp;<button class="btn btn-xs btn-default viewHandler" data-node-id="'+  node.id +'"> view handler </button>'
                );

            } else {

                $li.find('.jqtree-element').append(
                    ' <a class="btn btn-xs btn-default edit" data-node-id="'+ node.id +'"> view </a>'
//                    + '&nbsp;<button class="btn btn-xs btn-default delete" data-node-id="'+ node.id +'"> delete </button>'
//                    + '&nbsp;<button class="btn btn-xs btn-default viewProperties" data-node-id="'+ node.id +'"> view properties </button>'
//                    + '&nbsp;<button class="btn btn-xs btn-default viewInstances" data-node-id="'+ node.id +'"> view instances </button>'
                );

            }
        }
    });

    // Handle a click on the edit link
    $tree.on(
        'click', '.edit',
        function(e) {
            // Get the id from the 'node-id' data property
            var node_id = $(e.target).data('node-id');

            // Get the node from the tree
            var node = $tree.tree('getNodeById', node_id);

            if (node) {

                page.redirect("/model/context/class/" + node.id);

            }
        }
    );

//    // Handle a click on the delete link
//    $tree.on(
//        'click', '.delete',
//        function(e) {
//            // Get the id from the 'node-id' data property
//            var node_id = $(e.target).data('node-id');
//
//            // Get the node from the tree
//            var node = $tree.tree('getNodeById', node_id);
//
//            if (node) {
//                // Display the node name
//                deleteClazzHandler(node.id);
//            }
//        }
//    );

    // Handle a click on the view instances handler link
//    $tree.on(
//        'click', '.viewInstances',
//        function(e) {
//            // Get the id from the 'node-id' data property
//            var node_id = $(e.target).data('node-id');
//
//            // Get the node from the tree
//            var node = $tree.tree('getNodeById', node_id);
//
//            if (node) {
//
//
//                page.redirect("/model/context/class/" + node.id + "/instance");
//
//            }
//        }
//    );
//
//    // Handle a click on the view instances handler link
//    $tree.on(
//        'click', '.viewProperties',
//        function(e) {
//            // Get the id from the 'node-id' data property
//            var node_id = $(e.target).data('node-id');
//
//            // Get the node from the tree
//            var node = $tree.tree('getNodeById', node_id);
//
//            if (node) {
//                page.redirect("/model/context/class/" + node.id + "/property");
//            }
//        }
//    );

    $("#loader-full").hide();
}

function addClassRedirect(rootClassID) {
    page.redirect('/model/context/class/add?r=' + rootClassID);
}

function editInstanceRedirect(instanceID, classID) {
    page.redirect('/model/context/class/'+ classID + '/instance/' + instanceID);
}

function editHandlerRedirect(handlerID) {
    page.redirect('/model/handler/'+ handlerID);
}

function editNamespaceRedirect(namespaceID) {
    page.redirect('/model/namespace/'+ namespaceID);
}

function editExpressionRedirect(expressionID) {
    page.redirect('/model/expression/'+ expressionID);
}

function editRuleRedirect(ruleID) {
    page.redirect('/model/rule/'+ ruleID);
}

function editPolicyRedirect(policyID) {
    page.redirect('/model/policy/'+ policyID);
}

function editPolicySetRedirect(policySetID) {
    page.redirect('/model/policyset/' + policySetID);
}

function loadQueryBuilder(method) {

    $("#loader-full").show();

    var expression = new Object();
    expression.instances = $("#instances").val();

    $.post({
        url: EXPRESSION_REST_URL + "querybuilder",
        data: JSON.stringify(expression),
        contentType: "application/json; charset=utf-8"
    }).success(function (data, status, xhr) {
        $("#loader-full").hide();
        if ("SUCCESS" === data.code) {
            logger.i("Expression builder loaded successfully!");

            paasword.notify(data.message);

            var filters = $.parseJSON(data.returnobject);

            constructQueryBuilder(filters, method);

            $("#instances").prop('disabled', true).trigger("chosen:updated");

            $("#btn-loadquerybuilder").attr("disabled", true);

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

function constructQueryBuilder(filtersData, method) {

    var buttonHTML = "";

    buttonHTML += "<button id='btn-reset' class='btn action' ><span class='oi oi-action-undo'></span> Reset </button> ";
    if (method === "add") {
        buttonHTML += "<button id='btn-expressionadd' onclick='addExpressionHandler()' class='btn action'><span class='oi oi-task'></span> Save</button>";
    } else {
        buttonHTML += "<button id='btn-expressionedit' onclick='editExpressionHandler()' class='btn action'><span class='oi oi-task'></span> Save</button>";
    }

    $('#buttons').html(buttonHTML);

    $('#builder').queryBuilder({
        filters: filtersData
    });

    $('#btn-reset').on('click', function() {
        $('#builder').queryBuilder('reset');
    });

    $('#btn-get').on('click', function() {
        var result = $('#builder').queryBuilder('getRules');

        if (!$.isEmptyObject(result)) {
            alert(JSON.stringify(result, null, 2));
        }
    });

    if ($("#expressionRules").length) {

        var rules_basic = $.parseJSON($("#expressionRules").val());

        $('#builder').queryBuilder('setRules', rules_basic);
    }

}

function initializeAddClassFragment() {

    $("body").tooltip({
        selector: 'a[rel=tooltip]',
        html: true,
        placement: "right"
    });

    var rootClassID = $("#rootClassID").val();

    $("#hasParent").click(function () {
        if (!$("#hasParent").is(':checked')) {

            $("#parentClass").val('');
            $("#parentClass").attr("disabled", true);
        } else {
            $("#parentClass").removeAttr("disabled");
        }
    });

    $("#parentClass").autocomplete({
        minLength: 1,
        source: function (request, response) {

            var keyword = new Object();
            keyword.keyword = request.term;

            jQuery.ajax({
                featureClass: "P",
                style: "full",
                maxRows: 12,
                url: CLASSES_REST_URL + "autocomplete",
                contentType: "application/json; charset=UTF-8",
                encoding: "UTF-8",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                },
                data: {
                    format: "json",
                    keyword: request.term,
                    rootID: rootClassID,
                    operation: "autocomplete"
                },
                success: function (data) {
                    var json = $.parseJSON(data.returnobject);

                    if (json.total === 0) {
                        // Do nothing
                    } else {
                        response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                            return {
                                label: item.value,
                                value: item.value,
                                id: item.id
                            }
                        }));
                    }
                },
                error: function (data) {

                }
            });
        },
        select: function (event, ui) {
            jQuery("#parentClass").val(ui.item.value);
            jQuery("#parentID").val(ui.item.id);
            return false;
        }
    });

}

function initializeEditClassFragment() {

    $("body").tooltip({
        selector: 'a[rel=tooltip]',
        html: true,
        placement: "right"
    });

    var rootClassID = $("#rootClassID").val();

    $("#hasParent").click(function () {
        if (!$("#hasParent").is(':checked')) {

            $("#parentClass").val('');
            $("#parentClass").attr("disabled", true);
        } else {
            $("#parentClass").removeAttr("disabled");
        }
    });

    $("#parentClass").autocomplete({
        minLength: 1,
        source: function (request, response) {

            var keyword = new Object();
            keyword.keyword = request.term;

            jQuery.ajax({
                featureClass: "P",
                style: "full",
                maxRows: 12,
                url: CLASSES_REST_URL + "autocomplete",
                contentType: "application/json; charset=UTF-8",
                encoding: "UTF-8",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                },
                data: {
                    format: "json",
                    keyword: request.term,
                    rootID: rootClassID,
                    operation: "autocomplete"
                },
                success: function (data) {
                    var json = $.parseJSON(data.returnobject);

                    if (json.total === 0) {
                        // Do nothing
                    } else {
                        response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                            return {
                                label: item.value,
                                value: item.value,
                                id: item.id
                            }
                        }));
                    }
                },
                error: function (data) {

                }
            });
        },
        select: function (event, ui) {
            jQuery("#parentClass").val(ui.item.value);
            jQuery("#parentID").val(ui.item.id);
            return false;
        }
    });

}

function initializeAddEditInstanceFragment() {

    $(".objectProperty" ).each(function() {

       var propertyID = $(this).attr('id');

       $(this).autocomplete({
           minLength: 1,
           source: function (request, response) {

               var keyword = new Object();
               keyword.keyword = request.term;

               jQuery.ajax({
                   featureClass: "P",
                   style: "full",
                   maxRows: 12,
                   url: INSTANCES_REST_URL + "autocomplete",
                   contentType: "application/json; charset=UTF-8",
                   encoding: "UTF-8",
                   beforeSend: function (xhr) {
                       xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                   },
                   data: {
                       format: "json",
                       keyword: request.term,
                       propertyID: propertyID,
                       operation: "autocomplete"
                   },
                   success: function (data) {
                       var json = $.parseJSON(data.returnobject);

                       if (json.total === 0) {
                           // Do nothing
                       } else {
                           response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                               return {
                                   label: item.value,
                                   value: item.value,
                                   id: item.id
                               }
                           }));
                       }
                   },
                   error: function (data) {

                   }
               });
           },
           select: function (event, ui) {
               jQuery("#" + propertyID).val(ui.item.value);
//                   $(this).val(ui.item.value);
//                   jQuery("#parentID").val(ui.item.id);
               return false;
           }
        });

    });


}

function initializeViewInstancesFragment(classID) {

     $("#instances-table").bootgrid({
        cssClass: 'text-center',
        columnSelection: false,
        caseSensitive: false,
        formatters: {
            "commands": function (column, row) {
                return "<span onclick='editInstanceRedirect("  +  row.id + "," + classID +")' data-row-id='" + row.id + "' class='oi oi-pencil'></span> " +
                        "<span onclick='deleteInstanceHandler("  +  row.id + "," + classID +")' data-row-id='" + row.id + "' class='oi oi-trash'></span>";

            }
        }

    });

    $('.bootgrid-header').css({'height':"40px"});

}

function initializeAddPolicySetFragment() {

    $('#policies').multiSelect({keepOrder: true});

}

function initializeEditPolicySetFragment() {

    $('#policies').multiSelect({keepOrder: true});

    var selectedPolicies = $('#selectedPolicies').val();

    if (selectedPolicies !== '') {

        if (selectedPolicies.indexOf(",") !== -1) {
            var policiesArray = [];

            policiesArray = selectedPolicies.split(",");

            $('#policies').multiSelect('select', policiesArray);
        } else {
            $('#policies').multiSelect('select', selectedPolicies);
        }

    }

}

function initializeAddPolicyFragment() {

    $('#rules').multiSelect({keepOrder: true});

}

function initializeEditPolicyFragment() {

    $('#rules').multiSelect({keepOrder: true});

    var selectedRules = $('#selectedRules').val();

    if (selectedRules !== '') {

        if (selectedRules.indexOf(",") !== -1) {
            var rulesArray = [];

            rulesArray = selectedRules.split(",");

            $('#rules').multiSelect('select', rulesArray);
        } else {
            $('#rules').multiSelect('select', selectedRules);
        }

    }

}

function initializeAddPropertyFragment() {

     $("body").tooltip({
            selector: 'a[rel=tooltip]',
            html: true,
            placement: "right"
        });

    var rootClassID = $("#rootClassID").val();

    var classID = $("#classID").val();

    $("#isObjectProperty").click(function () {
        if (!$("#isObjectProperty").is(':checked')) {

            $("#objectPropertyClassName").val('');
            $("#objectPropertyClassName").attr("disabled", true);

            $("#propertyType").removeAttr("disabled");
            $("#transitivity").attr("disabled", true);
        } else {
            $("#objectPropertyClassName").removeAttr("disabled");

            $("#propertyType").val('');
            $("#propertyType").attr("disabled", true);
            $("#transitivity").removeAttr("disabled");
        }
    });

    $("#isSubPropertyOf").click(function () {

        //TODO

        if (!$("#isSubPropertyOf").is(':checked')) {

            $("#subPropertyOfPropertyName").val('');
            $("#subPropertyOfPropertyName").attr("disabled", true);

            $("#propertyType").removeAttr("disabled");
            $("#transitivity").attr("disabled", true);

            $("#isObjectProperty").attr("checked", false);
            $("#isObjectProperty").attr("disabled", false);

            $("#objectPropertyClassName").val('');
            $("#objectPropertyClassName").attr("disabled", true);

            $("#objectPropertyClassName").autocomplete({
                    minLength: 1,
                    source: function (request, response) {

                        var keyword = new Object();
                        keyword.keyword = request.term;

                        jQuery.ajax({
                            featureClass: "P",
                            style: "full",
                            maxRows: 12,
                            url: CLASSES_REST_URL + "autocomplete",
                            contentType: "application/json; charset=UTF-8",
                            encoding: "UTF-8",
                            beforeSend: function (xhr) {
                                xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                            },
                            data: {
                                format: "json",
                                keyword: request.term,
                                rootID: rootClassID,
                                operation: "autocomplete"
                            },
                            success: function (data) {
                                var json = $.parseJSON(data.returnobject);

                                if (json.total === 0) {
                                    // Do nothing
                                } else {
                                    response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                                        return {
                                            label: item.value,
                                            value: item.value,
                                            id: item.id
                                        }
                                    }));
                                }
                            },
                            error: function (data) {

                            }
                        });
                    },
                    select: function (event, ui) {
                        jQuery("#objectPropertyClassName").val(ui.item.value);
                        jQuery("#objectPropertyClassID").val(ui.item.id);
                        return false;
                    }
                });

        } else {
            $("#subPropertyOfPropertyName").removeAttr("disabled");
            $("#transitivity").removeAttr("disabled");

//            $("#propertyType").val('');
//            $("#propertyType").attr("disabled", true);

//            $("#objectPropertyClassName").attr("disabled", true);

        }
    });

    $("#subPropertyOfPropertyName").autocomplete({
            minLength: 1,
            source: function (request, response) {

                var keyword = new Object();
                keyword.keyword = request.term;

                jQuery.ajax({
                    featureClass: "P",
                    style: "full",
                    maxRows: 12,
                    url: PROPERTIES_REST_URL + "autocomplete",
                    contentType: "application/json; charset=UTF-8",
                    encoding: "UTF-8",
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                    },
                    data: {
                        format: "json",
                        keyword: request.term,
                        classID: classID,
                        operation: "autocomplete"
                    },
                    success: function (data) {
                        var json = $.parseJSON(data.returnobject);

                        if (json.total === 0) {
                            // Do nothing
                        } else {
                            response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                                return {
                                    label: item.value,
                                    value: item.value,
                                    id: item.id,
                                    isObjectProperty : item.isObjectProperty,
                                    objectPropertyClassID : item.objectPropertyClassID
                                }
                            }));

                        }
                    },
                    error: function (data) {

                    }
                });
            },
            select: function (event, ui) {
                jQuery("#subPropertyOfPropertyName").val(ui.item.value);
                jQuery("#subPropertyOfID").val(ui.item.id);

                performValidations(ui.item);

                return false;
            }
        });

    $("#objectPropertyClassName").autocomplete({
        minLength: 1,
        source: function (request, response) {

            var keyword = new Object();
            keyword.keyword = request.term;

            jQuery.ajax({
                featureClass: "P",
                style: "full",
                maxRows: 12,
                url: CLASSES_REST_URL + "autocomplete",
                contentType: "application/json; charset=UTF-8",
                encoding: "UTF-8",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                },
                data: {
                    format: "json",
                    keyword: request.term,
                    rootID: rootClassID,
                    operation: "autocomplete"
                },
                success: function (data) {
                    var json = $.parseJSON(data.returnobject);

                    if (json.total === 0) {
                        // Do nothing
                    } else {
                        response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                            return {
                                label: item.value,
                                value: item.value,
                                id: item.id
                            }
                        }));
                    }
                },
                error: function (data) {

                }
            });
        },
        select: function (event, ui) {
            jQuery("#objectPropertyClassName").val(ui.item.value);
            jQuery("#objectPropertyClassID").val(ui.item.id);
            return false;
        }
    });

}

function performValidations(property) {

    if (property.isObjectProperty === true) {

        $("#objectPropertyClassName").val('');

        $("#objectPropertyClassName").removeAttr("disabled");

        $("#isObjectProperty").attr("checked", true);

        $("#isObjectProperty").attr("disabled", true);

        if ($("#isObjectProperty").is(':checked')) {

            $("#propertyType").val('');
            $("#propertyType").attr("disabled", true);

        }

        var subPropertyOfID = $("#subPropertyOfID").val();

        $("#objectPropertyClassName").autocomplete({
                minLength: 1,
                source: function (request, response) {

                    var keyword = new Object();
                    keyword.keyword = request.term;

                    jQuery.ajax({
                        featureClass: "P",
                        style: "full",
                        maxRows: 12,
                        url: CLASSES_REST_URL + "autocompleteObj",
                        contentType: "application/json; charset=UTF-8",
                        encoding: "UTF-8",
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                        },
                        data: {
                            format: "json",
                            keyword: request.term,
                            subPropertyOfID: subPropertyOfID,
                            operation: "autocomplete"
                        },
                        success: function (data) {
                            var json = $.parseJSON(data.returnobject);

                            if (json.total === 0) {
                                // Do nothing
                            } else {
                                response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                                    return {
                                        label: item.value,
                                        value: item.value,
                                        id: item.id
                                    }
                                }));
                            }
                        },
                        error: function (data) {

                        }
                    });
                },
                select: function (event, ui) {
                    jQuery("#objectPropertyClassName").val(ui.item.value);
                    jQuery("#objectPropertyClassID").val(ui.item.id);
                    return false;
                }
            });


    } else {
        // DO NOTHING

    }

}

function performEditValidations(property) {

    var rootClassID = $("#rootClassID").val();

    if (property.isObjectProperty === true) {

        $("#objectPropertyClassName").val('');

        $("#objectPropertyClassName").removeAttr("disabled");

        $("#isObjectProperty").attr("checked", true);

        $("#isObjectProperty").attr("disabled", true);

        if ($("#isObjectProperty").is(':checked')) {

            $("#propertyType").val('');
            $("#propertyType").attr("disabled", true);

        }

        var subPropertyOfID = $("#subPropertyOfID").val();

        $("#objectPropertyClassName").autocomplete({
                minLength: 1,
                source: function (request, response) {

                    var keyword = new Object();
                    keyword.keyword = request.term;

                    jQuery.ajax({
                        featureClass: "P",
                        style: "full",
                        maxRows: 12,
                        url: CLASSES_REST_URL + "autocompleteObj",
                        contentType: "application/json; charset=UTF-8",
                        encoding: "UTF-8",
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                        },
                        data: {
                            format: "json",
                            keyword: request.term,
                            subPropertyOfID: subPropertyOfID,
                            operation: "autocomplete"
                        },
                        success: function (data) {
                            var json = $.parseJSON(data.returnobject);

                            if (json.total === 0) {
                                // Do nothing
                            } else {
                                response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                                    return {
                                        label: item.value,
                                        value: item.value,
                                        id: item.id
                                    }
                                }));
                            }
                        },
                        error: function (data) {

                        }
                    });
                },
                select: function (event, ui) {
                    jQuery("#objectPropertyClassName").val(ui.item.value);
                    jQuery("#objectPropertyClassID").val(ui.item.id);
                    return false;
                }
            });


    } else {

        $("#objectPropertyClassName").val('');

        $("#objectPropertyClassName").attr("disabled", true);

        $("#isObjectProperty").attr("checked", false);

        $("#propertyType").attr("disabled", false);

        $("#objectPropertyClassName").autocomplete({
            minLength: 1,
            source: function (request, response) {

                var keyword = new Object();
                keyword.keyword = request.term;

                jQuery.ajax({
                    featureClass: "P",
                    style: "full",
                    maxRows: 12,
                    url: CLASSES_REST_URL + "autocomplete",
                    contentType: "application/json; charset=UTF-8",
                    encoding: "UTF-8",
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                    },
                    data: {
                        format: "json",
                        keyword: request.term,
                        rootID: rootClassID,
                        operation: "autocomplete"
                    },
                    success: function (data) {
                        var json = $.parseJSON(data.returnobject);

                        if (json.total === 0) {
                            // Do nothing
                        } else {
                            response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                                return {
                                    label: item.value,
                                    value: item.value,
                                    id: item.id
                                }
                            }));
                        }
                    },
                    error: function (data) {

                    }
                });
            },
            select: function (event, ui) {
                jQuery("#objectPropertyClassName").val(ui.item.value);
                jQuery("#objectPropertyClassID").val(ui.item.id);
                return false;
            }
        });


    }

}

function initializeEditPropertyFragment() {

    $("body").tooltip({
        selector: 'a[rel=tooltip]',
        html: true,
        placement: "right"
    });

    var rootClassID = $("#rootClassID").val();

    var classID = $("#classID").val();

    var objectProperty = $("#objectProperty").val();

    if (objectProperty === '1') {
        $("#propertyType").val('');
        $("#propertyType").attr("disabled", true);
        $("#transitivity").removeAttr("disabled");
    } else {
        $("#objectPropertyClassName").val('');
        $("#objectPropertyClassName").attr("disabled", true);
    }

    var subProperty = $("#subProperty").val();
    var subPropertyOfID = $("#subPropertyOfID").val();

    if (subProperty === '1') {

        $("#isSubPropertyOf").attr("checked", true);

        $("#objectPropertyClassName").autocomplete({
            minLength: 1,
            source: function (request, response) {

                var keyword = new Object();
                keyword.keyword = request.term;

                jQuery.ajax({
                    featureClass: "P",
                    style: "full",
                    maxRows: 12,
                    url: CLASSES_REST_URL + "autocompleteObj",
                    contentType: "application/json; charset=UTF-8",
                    encoding: "UTF-8",
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                    },
                    data: {
                        format: "json",
                        keyword: request.term,
                        subPropertyOfID: subPropertyOfID,
                        operation: "autocomplete"
                    },
                    success: function (data) {
                        var json = $.parseJSON(data.returnobject);

                        if (json.total === 0) {
                            // Do nothing
                        } else {
                            response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                                return {
                                    label: item.value,
                                    value: item.value,
                                    id: item.id
                                }
                            }));
                        }
                    },
                    error: function (data) {

                    }
                });
            },
            select: function (event, ui) {
                jQuery("#objectPropertyClassName").val(ui.item.value);
                jQuery("#objectPropertyClassID").val(ui.item.id);
                return false;
            }
        });

    } else {

        $("#subPropertyOfPropertyName").val('');
        $("#subPropertyOfPropertyName").attr("disabled", true);

        $("#objectPropertyClassName").autocomplete({
                minLength: 1,
                source: function (request, response) {

                    var keyword = new Object();
                    keyword.keyword = request.term;

                    jQuery.ajax({
                        featureClass: "P",
                        style: "full",
                        maxRows: 12,
                        url: CLASSES_REST_URL + "autocomplete",
                        contentType: "application/json; charset=UTF-8",
                        encoding: "UTF-8",
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                        },
                        data: {
                            format: "json",
                            keyword: request.term,
                            rootID: rootClassID,
                            operation: "autocomplete"
                        },
                        success: function (data) {
                            var json = $.parseJSON(data.returnobject);

                            if (json.total === 0) {
                                // Do nothing
                            } else {
                                response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                                    return {
                                        label: item.value,
                                        value: item.value,
                                        id: item.id
                                    }
                                }));
                            }
                        },
                        error: function (data) {

                        }
                    });
                },
                select: function (event, ui) {
                    jQuery("#objectPropertyClassName").val(ui.item.value);
                    jQuery("#objectPropertyClassID").val(ui.item.id);
                    return false;
                }
            });

    }

    $("#isSubPropertyOf").click(function () {

        // TODO
        if (!$("#isSubPropertyOf").is(':checked')) {

            $("#subPropertyOfPropertyName").val('');
            $("#subPropertyOfPropertyName").attr("disabled", true);

            $("#propertyType").removeAttr("disabled");
            $("#transitivity").attr("disabled", true);

            $("#isObjectProperty").attr("checked", false);
            $("#isObjectProperty").attr("disabled", false);

            $("#objectPropertyClassName").val('');
            $("#objectPropertyClassName").attr("disabled", true);

            $("#objectPropertyClassName").autocomplete({
                minLength: 1,
                source: function (request, response) {

                    var keyword = new Object();
                    keyword.keyword = request.term;

                    jQuery.ajax({
                        featureClass: "P",
                        style: "full",
                        maxRows: 12,
                        url: CLASSES_REST_URL + "autocomplete",
                        contentType: "application/json; charset=UTF-8",
                        encoding: "UTF-8",
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                        },
                        data: {
                            format: "json",
                            keyword: request.term,
                            rootID: rootClassID,
                            operation: "autocomplete"
                        },
                        success: function (data) {
                            var json = $.parseJSON(data.returnobject);

                            if (json.total === 0) {
                                // Do nothing
                            } else {
                                response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                                    return {
                                        label: item.value,
                                        value: item.value,
                                        id: item.id
                                    }
                                }));
                            }
                        },
                        error: function (data) {

                        }
                    });
                },
                select: function (event, ui) {
                    jQuery("#objectPropertyClassName").val(ui.item.value);
                    jQuery("#objectPropertyClassID").val(ui.item.id);
                    return false;
                }
            });

        } else {
            $("#transitivity").removeAttr("disabled");
            $("#subPropertyOfPropertyName").removeAttr("disabled");
            $("#propertyType").attr("disabled", true);
            $("#propertyType").val('');
        }
    });

    $("#subPropertyOfPropertyName").autocomplete({
        minLength: 1,
        source: function (request, response) {

            var keyword = new Object();
            keyword.keyword = request.term;

            jQuery.ajax({
                featureClass: "P",
                style: "full",
                maxRows: 12,
                url: PROPERTIES_REST_URL + "autocomplete",
                contentType: "application/json; charset=UTF-8",
                encoding: "UTF-8",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                },
                data: {
                    format: "json",
                    keyword: request.term,
                    classID: classID,
                    operation: "autocomplete"
                },
                success: function (data) {
                    var json = $.parseJSON(data.returnobject);

                    if (json.total === 0) {
                        // Do nothing
                    } else {
                        response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                            return {
                                label: item.value,
                                value: item.value,
                                id: item.id,
                                isObjectProperty : item.isObjectProperty,
                                objectPropertyClassID : item.objectPropertyClassID
                            }
                        }));

                    }
                },
                error: function (data) {

                }
            });
        },
        select: function (event, ui) {
            jQuery("#subPropertyOfPropertyName").val(ui.item.value);
            jQuery("#subPropertyOfID").val(ui.item.id);

            performEditValidations(ui.item);

            return false;
        }
    });

    $("#isObjectProperty").click(function () {
        if (!$("#isObjectProperty").is(':checked')) {

            $("#objectPropertyClassName").val('');
            $("#objectPropertyClassName").attr("disabled", true);

            $("#propertyType").removeAttr("disabled");
            $("#transitivity").attr("disabled", true);
        } else {
            $("#objectPropertyClassName").removeAttr("disabled");

            $("#propertyType").val('');
            $("#propertyType").attr("disabled", true);
            $("#transitivity").removeAttr("disabled");
        }
    });

}

function initializeOnChangeOfExpressionFunctionality() {
    $("#expression").change(function () {
        $("#loader-full").show();
        var expressionID = $("#expression").val();

        $.get({
            url: EXPRESSION_REST_URL + expressionID,
            contentType: "application/json; charset=utf-8"
        }).success(function (data, status, xhr) {
            //Check if the class exists is created
            if ("SUCCESS" === data.code) {
                logger.i("Expression loaded successful");

                var html = "<a href='/model/expression/" + data.returnobject.id + "' target='_blank'>" + data.returnobject.expressionName + "</a>";

                $("#expressionTitle").html(html);

                $("#expressionFriendlyData").text(data.returnobject.expressionFriendlyData);

                $("#expressionDescription").text(data.returnobject.description);

            } else {

                paasword.notify(data.message, {mode: "notification-error"});
            }

            $("#loader-full").hide();

        }).fail(function (error) {
          $("#loader-full").hide();
          var response = JSON.parse(error.responseText);
          paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
          logger.e("Code: " + error.status + " Message: " + error.responseText);
      });



    });
}

function initializeOnChangeOfPermissionTypeFunctionality() {
    $("#permissionType").change(function () {
        $("#loader-full").show();

        var className = $("#permissionType").val();

        $.get({
            url: INSTANCES_REST_URL + "class/" + className,
            contentType: "application/json; charset=utf-8"
        }).success(function (data, status, xhr) {

            //Check if the action field is created
            if ("SUCCESS" === data.code) {
                logger.i("Action field loaded successful");

                var array = data.returnobject;
                var htmlVar = "";

                var deploymentType = $("#deploymentType").val();

                $("#action").removeAttr('disabled')

                for (var i = 0; i < array.length; i++ ) {

                    htmlVar += '<option text="' + array[i].instanceName + '" value="' + array[i].instanceName + '">' + array[i].instanceName + '</option>';
                }

                $("#action").html(htmlVar);

            } else {

                paasword.notify(data.message, {mode: "notification-error"});
            }

        $("#loader-full").hide();

       }).fail(function (error) {
         $("#loader-full").hide();
         var response = JSON.parse(error.responseText);
         paasword.notify(response.message + " [" + response.status + "]", {mode: "notification-error"});
         logger.e("Code: " + error.status + " Message: " + error.responseText);
     });



   });
}

function initializeAutocompleteForHandlerView() {

    $("#hasInput").autocomplete({
        minLength: 1,
        source: function (request, response) {

          var keyword = new Object();
          keyword.keyword = request.term;

          jQuery.ajax({
              featureClass: "P",
              style: "full",
              maxRows: 12,
              url: CLASSES_REST_URL + "autocomplete",
              contentType: "application/json; charset=UTF-8",
              encoding: "UTF-8",
              beforeSend: function (xhr) {
                  xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
              },
              data: {
                  format: "json",
                  keyword: request.term,
                  rootID: 0,
                  operation: "autocomplete"
              },
              success: function (data) {
                  var json = $.parseJSON(data.returnobject);

                  if (json.total === 0) {
                      // Do nothing
                  } else {
                      response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                          return {
                              label: item.value,
                              value: item.value,
                              id: item.id
                          }
                      }));
                  }
              },
              error: function (data) {

              }
          });
        },
        select: function (event, ui) {
          jQuery("#hasInput").val(ui.item.value);
          jQuery("#hasInputID").val(ui.item.id);
          return false;
        }
    });

    $("#hasOutput").autocomplete({
            minLength: 1,
            source: function (request, response) {

              var keyword = new Object();
              keyword.keyword = request.term;

              jQuery.ajax({
                  featureClass: "P",
                  style: "full",
                  maxRows: 12,
                  url: CLASSES_REST_URL + "autocomplete",
                  contentType: "application/json; charset=UTF-8",
                  encoding: "UTF-8",
                  beforeSend: function (xhr) {
                      xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
                  },
                  data: {
                      format: "json",
                      keyword: request.term,
                      rootID: 0,
                      operation: "autocomplete"
                  },
                  success: function (data) {
                      var json = $.parseJSON(data.returnobject);

                      if (json.total === 0) {
                          // Do nothing
                      } else {
                          response(jQuery.map(jQuery.makeArray(json.values), function (item) {
                              return {
                                  label: item.value,
                                  value: item.value,
                                  id: item.id
                              }
                          }));
                      }
                  },
                  error: function (data) {

                  }
              });
            },
            select: function (event, ui) {
              jQuery("#hasOutput").val(ui.item.value);
              jQuery("#hasOutputID").val(ui.item.id);
              return false;
            }
    });

}