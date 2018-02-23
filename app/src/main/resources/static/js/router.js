/**
 * Router ( client-side router )
 * 
 * Handles all the requests for each client-side '/path'
 *   
 * For more information abouting using router please
 * refer to https://github.com/visionmedia/page.js
 */

$(document).ready(function () {
    logger.d("Loaded router.js");
});

page.base('/');

page('/', home);

page('login', auth.login);
page('logout', auth.logout);
page('register', auth.create);

page('documentation', documentation.list);
page('documentation/webapp', documentation.webapp);
page('documentation/jpa', documentation.jpa);
page('documentation/pep', documentation.pep);

page('model/hlo', model.hlo);
page('model/lifecycle', model.lifecycle);

page('model/context', model.context);
page('model/context/object', model.context.object);
page('model/context/request', model.context.request);
page('model/context/subject', model.context.subject);

page('model/context/pattern', model.context.pattern);

page('model/context/class/add', model.context.addClass);
page('model/context/class/:id', model.context.editClass);
page('model/context/class/:id/instance', model.context.viewInstances);
page('model/context/class/:id/instance/add', model.context.addInstance);
page('model/context/class/:id/instance/:instanceID', model.context.editInstance);
page('model/context/class/:id/property', model.context.viewProperties);
page('model/context/class/:id/property/add', model.context.addProperty);
page('model/context/class/:id/property/:propertyID', model.context.editProperty);

page('model/namespace', model.namespace);
page('model/namespace/add', model.namespace.addNamespace);
page('model/namespace/:id', model.namespace.editNamespace);

page('model/handler', model.handler);
page('model/handler/add', model.handler.addHandler);
page('model/handler/:id', model.handler.editHandler);

//page('model/context/class/:id/handler/add', model.context.addHandler);
//page('model/context/class/:id/handler/:hid', model.context.editHandler);

page('model/expression', model.expression);
page('model/expression/add', model.expression.add);
page('model/expression/:id', model.expression.edit);

page('model/policyset', model.policyset);
page('model/policyset/add', model.policyset.add);
page('model/policyset/:id', model.policyset.edit);
page('model/policyset/:id/policy', model.policyset.addRemovePolicies);

page('model/policy', model.policy);
page('model/policy/add', model.policy.add);
page('model/policy/:id', model.policy.edit);
page('model/policy/:id/rule', model.policy.addRemoveRules);

page('model/rule', model.rule);
page('model/rule/add', model.rule.add);
page('model/rule/:id', model.rule.edit);

page('user', user.list);
page('user/add', user.add);
page('user/:id', user.edit);

page('dashboard', dashboard.load);

page('resource', resource.list);
page('resource/iaas', resource.iaas);
page('resource/iaas/add', resource.iaas.add);
page('resource/iaas/:id', resource.iaas.edit);
page('resource/iaas/:id/image', resource.iaas.images);
page('resource/iaas/:id/image/add', resource.iaas.addImage);
page('resource/iaas/:id/image/:imageID', resource.iaas.editImage);
page('resource/iaas/:id/instance', resource.iaas.instances);
page('resource/iaas/:id/instance/add', resource.iaas.addInstance);
page('resource/iaas/:id/instance/:instanceID', resource.iaas.editInstance);

page('resource/paas', resource.paas);
page('resource/paas/add', resource.paas.add);
page('resource/paas/:id', resource.paas.edit);

page('resource/slipstream', resource.slipstream);
page('resource/slipstream/authorize', resource.slipstream.authorize);
page('resource/slipstream/vm', resource.slipstream.vm);
page('resource/slipstream/instance', resource.slipstream.instance);
page('resource/slipstream/usage', resource.slipstream.usage);

page('application', application.list);
page('application/add', application.add);
page('application/:id', application.edit);

page('application/:id/activity', activity.list);

page('application/:id/privacy', application.privacyConstraintsSets);
page('application/:id/privacy/add', application.addPrivacyConstraintsSet);
page('application/:id/privacy/:privacyConstraintID', application.editPrivacyConstraintsSet);

page('application/:id/affinity', application.affinityConstraintsSets);
page('application/:id/affinity/add', application.addAffinityConstraintsSet);
page('application/:id/affinity/:affinityConstraintID', application.editAffinityConstraintsSet);

page('application/:id/appkey', application.appKeys);
page('application/:id/pep', application.pep);
page('application/:id/entity', application.entity);
page('application/:id/upload', application.uploadSourceCode);

page('application/:id/instance', application.instance);
page('application/:id/instance/new', application.newInstance);
page('application/:id/instance/validate', application.validateInstance);
page('application/:id/instance/:instanceID', application.infoInstance);
page('application/:id/instance/:instanceID/deploy', application.deployInstance);
page('application/:id/instance/:instanceID/proxy', application.proxyInstance);
page('application/:id/instance/:instanceID/keymgmt', application.keyManagementInstance);
page('application/:id/instance/:instanceID/keymgmt/adduser', application.keyManagementAddUserInstance);
page('application/:id/instance/:instanceID/handler', application.handlerInstance);
page('application/:id/instance/:instanceID/activity', application.activityInstance);

page('*', error);

page();

// home
function home(ctx) {
    if (ctx.init) {
        logger.i("Current page set to: " + ctx.pathname);
        //Urgent bound of Interceptor
        bindAJAXCallInterceptor();
        //Urgent setup of ajax filter
        setupAjaxCallFiltering();

//        if (!hasAccessToken()) {
//
//        }

        //Load header content
        $.post("/header", function (data) {
            $("#header").html(data);
        });

        //Load footer content
        $.post("/footer", function (data) {
            $("#footer").html(data);
        });

        //Check for possible page redirections
        if (undefined !== redirectTopage) {
            logger.i("Found redirect-to-page : " + redirectTopage);
            page.redirect(redirectTopage);
            return;
        }

        //Check if user is authenticated immediately redirect to /dashboard
        if (hasAccessToken()) {
            logger.d("User is authenticated, redirecting user to dashboard page")
            page.redirect("/dashboard");
            return;
        }
        //Load home page content
        else
        {
            $.post("/content", function (data) {
                $("#content").html(data);
                $("body").addClass("home");
            });
        }

    } else {
        location.reload();
    }
}

// error
function error(ctx) {
    logger.e("Could not find pathname: " + ctx.pathname);
    logger.d("Current state: " + JSON.stringify(ctx));
}