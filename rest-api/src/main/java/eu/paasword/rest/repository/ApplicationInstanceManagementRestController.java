/*
 *  Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.paasword.rest.repository;

import eu.paasword.adapter.openstack.FragServer;
import eu.paasword.adapter.openstack.IaaS;
import eu.paasword.api.repository.*;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceDoesNotExist;
import eu.paasword.api.repository.exception.applicationInstanceHandler.ApplicationInstanceHandlerDoesNotExist;
import eu.paasword.api.repository.exception.handler.HandlerDoesNotExist;
import eu.paasword.api.repository.exception.iaasProvider.IaaSProviderDoesNotExist;
import eu.paasword.api.repository.exception.paasProvider.PaaSProviderDoesNotExist;
import eu.paasword.dbproxy.DBProxyOrchestratorResponse;
import eu.paasword.dbproxy.DatabaseProxyEngine;
import eu.paasword.dbproxy.fragmentation.FragmentationEngine;
import eu.paasword.repository.relational.dao.ApplicationBinaryRepository;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.repository.transferobject.TApplicationInstance;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.spi.adapter.PaaSAdapter;
import eu.paasword.spi.model.*;
import eu.paasword.spi.response.SPIResponse;
import eu.paasword.spi.util.Util;
import eu.paasword.util.security.auth.UserAuthentication;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import javax.annotation.Resource;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contains all the rest endpoints regarding Application Instances
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/applicationinstance")
public class ApplicationInstanceManagementRestController {

    private static final Logger logger = Logger.getLogger(ApplicationInstanceManagementRestController.class.getName());

    static RestTemplate restTemplate;

    @Autowired
    IApplicationInstanceService<ApplicationInstance> applicationInstanceService;

    @Autowired
    IAPIKeyService<APIKey> apiKeyService;

    @Autowired
    IApplicationPrivacyConstraintService<ApplicationPrivacyConstraint> applicationPrivacyConstraintService;

    @Autowired
    IApplicationAffinityConstraintService<ApplicationAffinityConstraint> applicationAffinityConstraintService;

    @Autowired
    IApplicationService<Application> applicationService;

    @Autowired
    IIaaSProviderService<IaaSProvider, User> iaaSProviderService;

    @Autowired
    IPaaSProviderService<PaaSProvider, User> paaSProviderService;

    @Autowired
    IPaaSProviderTypeService<PaaSProviderType> paaSProviderTypeService;

    @Autowired
    IIaaSProviderInstanceService<IaaSProviderInstance> iaaSProviderInstanceService;

    @Autowired
    IHandlerService<Handler> handlerService;

    @Autowired
    IApplicationInstanceHandlerService<ApplicationInstanceHandler, ApplicationInstance> applicationInstanceHandlerService;

    @Autowired
    FragmentationEngine fragmentationEngine;

    @Autowired
    DatabaseProxyEngine databaseProxyEngine;

    @Resource(name = "paasAdaptersList")
    List paasAdapters;

    @Autowired
    Environment environment;

    @Autowired
    IUserService userService;

    @Autowired
    IProxyCloudProviderService<ProxyCloudProvider, User> proxyCloudProviderService;

    @Autowired
    IUserCredentialService<UserCredential, ProxyCloudProvider, User> userCredentialService;

    @Autowired
    ApplicationBinaryRepository applicationBinaryRepository;


    /**
     * Fetch all available application instances from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getApplicationInstances() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, applicationInstanceService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch an Application Instances with a specific ID from database.
     *
     * @param id The id of the Privacy Constraint to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getApplicationInstanceByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, (ApplicationInstance) applicationInstanceService.findOne(id).get());
        } catch (ApplicationInstanceDoesNotExist ex) {
            logger.severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to assign a handler to an existing application instance to the database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}/assignhandler/{handlerID}", method = RequestMethod.POST)
    public PaaSwordRestResponse assignHandler(@PathVariable("id") long id, @PathVariable("handlerID") long handlerID) {

        try {

            ApplicationInstance applicationInstance = (ApplicationInstance) applicationInstanceService.findOne(id).get();

            List<ApplicationInstanceHandler> applicationInstanceHandlers = applicationInstance.getApplicationInstanceHandlers();

            ApplicationInstanceHandler applicationInstanceHandler = new ApplicationInstanceHandler();

            if (null != applicationInstanceHandlers && !applicationInstanceHandlers.isEmpty()) {

                applicationInstanceHandler.setApplicationInstanceID(applicationInstance);
                applicationInstanceHandler.setHandlerID((Handler) handlerService.findOne(handlerID).get());

                applicationInstanceHandlers.add(applicationInstanceHandler);

            } else {

                applicationInstanceHandlers = new ArrayList<>();

                applicationInstanceHandler.setApplicationInstanceID(applicationInstance);
                applicationInstanceHandler.setHandlerID((Handler) handlerService.findOne(handlerID).get());

                applicationInstanceHandlers.add(applicationInstanceHandler);

            }

            applicationInstanceHandlerService.create(applicationInstanceHandler);

            applicationInstance.setApplicationInstanceHandlers(applicationInstanceHandlers);

            applicationInstanceService.edit(applicationInstance);

        } catch (ApplicationInstanceDoesNotExist | HandlerDoesNotExist ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_HANDLER_ASSIGNED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to unassign a handler to an existing application instance to the database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}/unassignhandler/{handlerID}", method = RequestMethod.POST)
    public PaaSwordRestResponse unassignHandler(@PathVariable("id") long id, @PathVariable("handlerID") long handlerID) {

        try {

            ApplicationInstance applicationInstance = (ApplicationInstance) applicationInstanceService.findOne(id).get();

            List<ApplicationInstanceHandler> applicationInstanceHandlers = applicationInstance.getApplicationInstanceHandlers();
            List<ApplicationInstanceHandler> newApplicationInstanceHandlers = new ArrayList<>();

            if (null != applicationInstanceHandlers && !applicationInstanceHandlers.isEmpty()) {

                applicationInstanceHandlers.stream().forEach(applicationInstanceHandler -> {

                    if (applicationInstanceHandler.getHandlerID().getId() == handlerID && applicationInstanceHandler.getApplicationInstanceID().getId() == id) {
                        try {
                            applicationInstanceHandlerService.delete(applicationInstanceHandler.getId());
                        } catch (ApplicationInstanceHandlerDoesNotExist e) {
                            e.printStackTrace();
                        }
                    } else {
                        newApplicationInstanceHandlers.add(applicationInstanceHandler);
                    }
                });

            }

            if (newApplicationInstanceHandlers.isEmpty()) {
                applicationInstance.setApplicationInstanceHandlers(null);
            } else {
                applicationInstance.setApplicationInstanceHandlers(newApplicationInstanceHandlers);
            }

            applicationInstanceService.edit(applicationInstance);

        } catch (ApplicationInstanceDoesNotExist ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_HANDLER_UNASSIGNED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new Application Instance to the database.
     *
     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public PaaSwordRestResponse bootNewInstance(@RequestBody TApplicationInstance tApplicationInstance) {

        try {

            ApplicationInstance appInstance = new ApplicationInstance();

            Application app = (Application) applicationService.findOneWithoutBlob(tApplicationInstance.getApplicationID()).get();

            appInstance.setApplicationID(app);
            appInstance.setDateCreated(new Date());
            appInstance.setDescription(tApplicationInstance.getDescription());
            appInstance.setName(tApplicationInstance.getName());

//            appInstance.setUniqueID(UUID.randomUUID().toString());

            String appKey = apiKeyService.findByApplicationID(app.getId()).get(0).getUniqueID();

            appInstance.setUniqueID(appKey);
//            String validator = Util.encode(app.getApiKeys().get(0).getUniqueID(), appInstance.getUniqueID());
//            appInstance.setValidator(validator);

            if (app.isDataModel()) {
                appInstance.setOverallStatus(2);
            } else {
                appInstance.setOverallStatus(7);
            }

            applicationInstanceService.create(appInstance);

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_CREATED, appInstance.getId());

        } catch (ApplicationDoesNotExist | ApplicationInstanceAlreadyExistsException ex) {
            logger.severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new Application Instance to the database.
     *
     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public PaaSwordRestResponse validateInstance(@RequestBody TApplicationInstance tApplicationInstance) {

        ArrayList<String> fields = new ArrayList<>();

        String fragments = null;


//        if ((null == tApplicationInstance.getPrivacyConstraintSetIDs() || tApplicationInstance.getPrivacyConstraintSetIDs().size() == 0) &&
//                (null == tApplicationInstance.getAffinityConstraintSetIDs() || tApplicationInstance.getAffinityConstraintSetIDs().size() == 0)) {
//            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_REQUEST, Optional.empty());
//        } else
        if (null == tApplicationInstance.getPrivacyConstraintSetIDs() || tApplicationInstance.getPrivacyConstraintSetIDs().size() == 0) {
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_REQUEST, Optional.empty());
        }

        try {

            Application app = (Application) applicationService.findOneWithoutBlob(tApplicationInstance.getApplicationID()).get();

            ArrayList<ArrayList<String>> privacyConstraintSets = new ArrayList<>();

            if (null != tApplicationInstance.getPrivacyConstraintSetIDs() && !tApplicationInstance.getPrivacyConstraintSetIDs().isEmpty()) {

                tApplicationInstance.getPrivacyConstraintSetIDs().stream().forEach(constraintID -> {

                    ApplicationPrivacyConstraint privacyConstraint = applicationPrivacyConstraintService.findOneWithoutApplication(Long.valueOf(constraintID));

                    ArrayList<String> constraints = new ArrayList<String>();

                    JSONArray jsonConstraints = new JSONArray(privacyConstraint.getPrivacyConstraint());

                    for (Object constr : jsonConstraints) {

                        constraints.add((String) constr);

                    }

                    privacyConstraintSets.add(constraints);

                });

            }

//            Table<String, String, Integer> affinityConstraintSets = HashBasedTable.create();
//
//            if (null != tApplicationInstance.getAffinityConstraintSetIDs() && !tApplicationInstance.getAffinityConstraintSetIDs().isEmpty()) {
//
//                int counter = 1;
//
//                for (String constraintID : tApplicationInstance.getAffinityConstraintSetIDs()) {
//
//                    ApplicationAffinityConstraint affinityConstraint = applicationAffinityConstraintService.findOneWithoutApplication(Long.valueOf(constraintID));
//
//                    ArrayList<String> constraints = new ArrayList<String>();
//
//                    JSONArray jsonConstraints = new JSONArray(affinityConstraint.getAffinityConstraint());
//
//                    affinityConstraintSets.put(jsonConstraints.getString(0), jsonConstraints.getString(1), counter++);
//
//                }
//            }

            JSONArray fieldsArray = new JSONObject(app.getAnnotatedCodeDataModel()).getJSONObject("dbProxy").getJSONArray("fields");
            for (Object fld : fieldsArray) {
                fields.add((String) fld);
            }

            // Choose Fragmentation Algorithm
            // TODO
//            if (affinityConstraintSets.isEmpty()) {
//
//                fragments = fragmentationEngine.fragmentNoServerLimitation(fields, privacyConstraintSets);
//
//            } else {
//                fragments = fragmentationEngine.fragmentNoServerLimitationWithAffinity(fields, privacyConstraintSets, affinityConstraintSets);
//            }

            fragments = fragmentationEngine.fragmentNoServerLimitation(fields, privacyConstraintSets);

            logger.info("Fragments are: " + fragments);

            if (null != fragments) {

                ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOne(tApplicationInstance.getId()).get();

                appInstance.setApplicationID((Application) applicationService.findOne(tApplicationInstance.getApplicationID()).get());
                appInstance.setFragmentationSchema(fragments);

                appInstance.setOverallStatus(3);

                appInstance.setPrivacyConstraintsSet(new JSONArray(tApplicationInstance.getPrivacyConstraintSetIDs()).toString());
//                appInstance.setAffinityConstraintsSet(new JSONArray(tApplicationInstance.getAffinityConstraintSetIDs()).toString());

                applicationInstanceService.edit(appInstance);

                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_VALIDATED, Optional.empty());


            } else {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_FRAGMENTATION_SCHEMA_INVALID, Optional.empty());
            }


        } catch (ApplicationDoesNotExist | ApplicationInstanceDoesNotExist ex) {
            logger.severe(ex.getMessage());
            ex.getMessage();
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

    }

    /**
     * The exposed endpoint is used via the UI, which attempts to change the privacy/affinity constraints of an Application Instance
     *
     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/refragment", method = RequestMethod.POST)
    public PaaSwordRestResponse refragmentInstance(@RequestBody TApplicationInstance tApplicationInstance) {

        try {

            ApplicationInstance appInstance = applicationInstanceService.findOneWithoutApplication(tApplicationInstance.getId());

            Application app = (Application) applicationService.findOneWithoutBlob(tApplicationInstance.getApplicationID()).get();
            appInstance.setApplicationID(app);
            appInstance.setFragmentationSchema(null);
            appInstance.setOverallStatus(2);
            appInstance.setPrivacyConstraintsSet(null);
            appInstance.setAffinityConstraintsSet(null);

            applicationInstanceService.edit(appInstance);

            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_RECONFIGURED, appInstance.getId());

        } catch (ApplicationDoesNotExist | ApplicationInstanceDoesNotExist ex) {
            logger.severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

    }

    /**
     * The exposed endpoint is used via the UI, which attempts to deploy a DB Proxy of an Application Instance
     *
     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/deploydbproxy", method = RequestMethod.POST)
    public PaaSwordRestResponse initializeInstance(@RequestBody TApplicationInstance tApplicationInstance) {


        if (tApplicationInstance.getDbProxyDeploymentType() == 1) {

            // Deploy DB Proxy to IaaS
            if (null == tApplicationInstance.getIaasProviderIDs() || tApplicationInstance.getIaasProviderIDs().size() == 0) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_IAAS_PROVIDERS_REQUIRED, Optional.empty());
            }

            try {

                ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOne(tApplicationInstance.getId()).get();

                if (null != appInstance && appInstance.getApplicationID().getId() == tApplicationInstance.getApplicationID()) {

                    appInstance.setDbProxyDeploymentType(1);

                    Application application = (Application) applicationService.findOne(tApplicationInstance.getApplicationID()).get();

//                jsonIaaSProviders.put((String) iaasProviderID);
//                try {
//                    IaaSProvider iaaSProvider = (IaaSProvider) iaaSProviderService.findOne(Long.valueOf(iaasProviderID)).get();
//                    IaaS iaas = new IaaS(iaasProviderID, iaaSProvider.getConnectionURL(), iaaSProvider.getUsername(), iaaSProvider.getPassword(), iaaSProvider.getTenantName(), iaaSProvider.getProject(), iaaSProvider.getIaasProviderImages().get(0).getImageID(),null);
//                    listOfIaaS.add(iaas);
//                } catch (IaaSProviderDoesNotExist e) {
//                    e.printStackTrace();
//                }

                    // List<IaaS>
                    List<IaaS> listOfIaaS = new ArrayList<>();

                    JSONArray jsonIaaSProviders = new JSONArray();

                    tApplicationInstance.getIaasProviderIDs().stream().forEach(iaasProviderID -> {

                        jsonIaaSProviders.put((String) iaasProviderID);
                        try {
                            IaaSProvider iaaSProvider = (IaaSProvider) iaaSProviderService.findOne(Long.valueOf(iaasProviderID)).get();
                            IaaS iaas = new IaaS(iaasProviderID, iaaSProvider.getConnectionURL(), iaaSProvider.getUsername(), iaaSProvider.getPassword(), iaaSProvider.getTenantName(), iaaSProvider.getProject(), iaaSProvider.getIaasProviderImages().get(0).getImageID(), iaaSProvider.getNetworkID(), iaaSProvider.getFlavorID());

                            // UBI eaa3021a-9ac8-4ce5-b837-9b6291758f0f
                            // ICCS 2a24c559-2c9e-4d0b-b636-9920d193c0b3

                            listOfIaaS.add(iaas);
                        } catch (IaaSProviderDoesNotExist e) {
                            e.printStackTrace();
                        }

                    });

                    logger.info("User selected IaaS providers: " + listOfIaaS.size());
                    logger.info("Application Key: " + appInstance.getUniqueID());
                    logger.info("Application Instance Key: " + appInstance.getUniqueID());

                    appInstance.setIaasProviders(jsonIaaSProviders.toString());
                    appInstance.setApplicationID(application);

                    // Parsing Create Table Statements for Entities
                    List<String> createStatements = new ArrayList<>();
                    JSONArray createStatementsArray = new JSONObject(application.getAnnotatedCodeDataModel()).getJSONObject("dbProxy").getJSONArray("createStatements");
                    for (Object stmt : createStatementsArray) {
                        createStatements.add((String) stmt);
                    }

                    // Parsing Entities
                    List<String> fields = new ArrayList<>();
                    JSONArray fieldsArray = new JSONObject(application.getAnnotatedCodeDataModel()).getJSONObject("dbProxy").getJSONArray("fields");
                    for (Object fld : fieldsArray) {
                        fields.add((String) fld);
                    }

                    // Parsing Privacy/Affinity Constraints
                    ArrayList<ArrayList<String>> constraintSets = new ArrayList<>();
                    tApplicationInstance.getPrivacyConstraintSetIDs().stream().forEach(constraintID -> {

                        ApplicationPrivacyConstraint privacyConstraint = applicationPrivacyConstraintService.findOneWithoutApplication(Long.valueOf(constraintID));

                        ArrayList<String> constraints = new ArrayList<String>();

                        JSONArray jsonConstraints = new JSONArray(privacyConstraint.getPrivacyConstraint());

                        for (Object constr : jsonConstraints) {

                            constraints.add(((String) constr).toLowerCase());

                        }
                        constraintSets.add(constraints);

                    });

                    logger.info("Deploying DB Proxy...");

                    // DB Proxy Orchestrator invocation
                    // TODO
                    // appInstance.getUniqueID()
                    // tApplicationInstance.getAppKey()

                    String tenantKey = Util.generateRandomString(16, Util.Mode.ALPHA);

                    DBProxyOrchestratorResponse dbResponse = databaseProxyEngine.initializeDBProxy(appInstance.getUniqueID(), tenantKey, listOfIaaS, createStatements, fields, constraintSets);

                    if (null != dbResponse && dbResponse.isSuccessresult()) {

                        // Update Application Instance
                        appInstance.setConfigurationFile(dbResponse.getConfigurationxml());
                        appInstance.setOverallStatus(5);

                        List<FragServer> fragServers = dbResponse.getFragservers();

                        JSONArray iaasInstances = new JSONArray();

                        fragServers.stream().forEach(server -> {
                            JSONObject srv = new JSONObject();
                            try {
                                srv.put("dbHost", server.getHost());
                                srv.put("dbName", server.getName());
                                srv.put("serverID", server.getServerid());
                                srv.put("iaasProviderID", server.getIaas().getId());
                                srv.put("iaasFriendlyName", ((IaaSProvider) iaaSProviderService.findOne(Long.valueOf(server.getIaas().getId())).get()).getFriendlyName());
                            } catch (IaaSProviderDoesNotExist e) {
                                e.printStackTrace();
                            }

                            iaasInstances.put(srv);

                        });

                        appInstance.setIaasProviderInstances(iaasInstances.toString());

                        application.setTenantKey(tenantKey);

                        applicationService.edit(application);

                        appInstance.setTenantKey(tenantKey);

                        applicationInstanceService.edit(appInstance);

                        logger.info("DB Proxy deployed successfully!");

                        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_DB_PROXY_DEPLOYMENT_SUCCESS, appInstance.getId());

                    } else {

                        appInstance.setOverallStatus(4);
                        applicationInstanceService.edit(appInstance);

                        logger.info("DB Proxy deploymened failed!");

                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_DB_PROXY_DEPLOYMENT_ERROR, appInstance.getId());
                    }

                } else {
                    logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
                }


            } catch (ApplicationDoesNotExist | ApplicationInstanceDoesNotExist ex) {
                logger.severe(ex.getMessage());
                return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
            }

        } else if (tApplicationInstance.getDbProxyDeploymentType() == 2) {

            // Deploy DB Proxy to SlipStream
            // TODO

            try {

                ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOne(tApplicationInstance.getId()).get();

                if (null != appInstance && appInstance.getApplicationID().getId() == tApplicationInstance.getApplicationID()) {

                    appInstance.setDbProxyDeploymentType(2);

                    Application application = (Application) applicationService.findOneWithoutBlob(tApplicationInstance.getApplicationID()).get();

//                    logger.info("Application Key: " + tApplicationInstance.getAppKey());
                    logger.info("Application Instance Key: " + appInstance.getUniqueID());


                    appInstance.setApplicationID(application);

                    // Check fragments in order to create JSON message for SlipStream

                    JSONArray fragmentsArray = null;

                    if (null != appInstance.getFragmentationSchema() && !appInstance.getFragmentationSchema().isEmpty()) {

                        fragmentsArray = new JSONArray(appInstance.getFragmentationSchema());

                        int fragments = fragmentsArray.length(); // + 2 Index Servers

                        logger.info("Calling SlipStream Proxy...");

                        JSONObject slipStreamProxy = new JSONObject();

                        for (int i = 1; i <= fragments; i++) {

                            JSONObject fragmentObj = new JSONObject();
                            fragmentObj.put("component", "PaaSword/PostgreSQL");
                            fragmentObj.put("db-user", "postgres");
                            fragmentObj.put("db-password", "postgres");
                            fragmentObj.put("db-name", "kit_server_" + (i-1));
                            fragmentObj.put("location", "gr");

                            slipStreamProxy.put(String.valueOf(i), fragmentObj);

                        }

                        // Index Servers
                        JSONObject remoteIndexServer = new JSONObject();
                        remoteIndexServer.put("component", "PaaSword/PostgreSQL");
                        remoteIndexServer.put("db-user", "postgres");
                        remoteIndexServer.put("db-password", "postgres");
                        remoteIndexServer.put("db-name", "kit_mimosecco_remote");
                        remoteIndexServer.put("location", "gr");

                        slipStreamProxy.put(String.valueOf(fragments + 1), remoteIndexServer);

                        JSONObject localIndexServer = new JSONObject();
                        localIndexServer.put("component", "PaaSword/PostgreSQL");
                        localIndexServer.put("db-user", "postgres");
                        localIndexServer.put("db-password", "postgres");
                        localIndexServer.put("db-name", "kit_mimosecco_local");
                        localIndexServer.put("location", "gr");

                        slipStreamProxy.put(String.valueOf(fragments + 2), localIndexServer);

                        // Add Hook URL
                        String paaswordControllerURL = environment.getProperty("paasword.controller.url");

                        slipStreamProxy.put("hookURL", paaswordControllerURL + "/api/v1/slipstream/dbproxy/" + appInstance.getUniqueID());

                        UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
                        String username = userauthentication.getDetails().getUsername();

                        User user = (User) userService.findByUsername(username).get();

                        List<ProxyCloudProvider> proxyCloudProviders = proxyCloudProviderService.findByUser(user);

                        UserCredential userCredential = userCredentialService.findByProxyCloudProviderAndUser(proxyCloudProviders.get(0).getId(), user.getId());

                        if (null != userCredential) {

                            slipStreamProxy.put("username", userCredential.getUsername());
                            slipStreamProxy.put("password", userCredential.getPassword());

                        } else {
                            logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.SLIPSTREAM_ACCOUNT_NOT_AVAILABLE, Optional.empty());
                        }

                        // Call SlipStream Proxy
                        if (null == restTemplate) {
                            restTemplate = new RestTemplate();
                        }

                        logger.info("JSON for SlipStream proxy: " + slipStreamProxy.toString());

                        HttpHeaders headers = new HttpHeaders();
                        headers.put("Content-Type", Arrays.asList("application/json"));

                        HttpEntity entity = new HttpEntity(slipStreamProxy.toString(), headers);

                        String slipStreamProxyURL = environment.getProperty("slipstream.proxy.url");

                        logger.info("SlipStream URL: " + slipStreamProxyURL);

                        ResponseEntity<String> responseEntity = restTemplate.exchange(slipStreamProxyURL, HttpMethod.POST, entity, String.class);

                        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.ACCEPTED) {

                            appInstance.setOverallStatus(6);

                            applicationInstanceService.edit(appInstance);

                            logger.info("DB Proxy request sent to SlipStream Proxy successfully!");

                            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_DB_PROXY_DEPLOYMENT_SUCCESS_PENDING, responseEntity.getBody());

                        } else {

                            appInstance.setOverallStatus(4);

                            applicationInstanceService.edit(appInstance);

                            logger.severe("Response Status: " + responseEntity.getStatusCode());
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, responseEntity.getStatusCode().toString(), responseEntity.getStatusCode());
                        }

                    } else {

                        logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());

                    }

                } else {
                    logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
                }


            } catch (ApplicationDoesNotExist | ApplicationInstanceDoesNotExist ex) {
                logger.severe(ex.getMessage());
                return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
            }


        } else {
            logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
        }

    }

    /**
     * The exposed endpoint is used via the UI, which attempts to deploy an Application Instance
     *
     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/deploy", method = RequestMethod.POST)
    public PaaSwordRestResponse deployInstance(@RequestBody TApplicationInstance tApplicationInstance) {

        if (tApplicationInstance.getDeploymentType() == 1) {

            if (0 == tApplicationInstance.getPaaSproviderID()) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_PAAS_PROVIDER_REQUIRED, Optional.empty());
            }

            try {

                ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOne(tApplicationInstance.getId()).get();

                if (null != appInstance && appInstance.getApplicationID().getId() == tApplicationInstance.getApplicationID()) {

                    appInstance.setDeploymentType(1);

                    Application application = (Application) applicationService.findOne(tApplicationInstance.getApplicationID()).get();

                    PaaSProvider paasProvider = (PaaSProvider) paaSProviderService.findOne(tApplicationInstance.getPaaSproviderID()).get();

                    appInstance.setPaaSProviderID(paasProvider);
                    appInstance.setApplicationID(application);

                    // Step 1: Create Application to PaaS Provider

                    PaaSProviderType paasProviderType = paasProvider.getPaasProviderTypeID();

                    PaaSAdapter paasAdapter = (PaaSAdapter) ((List) paasAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(paasProviderType.getAdapterImplementation())).collect(Collectors.toList())).get(0);

                    CredentialsModel credentialsModel = new CredentialsModel();

                    PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
                    paaSOfferingModelSPI.setEndpointURI(paasProvider.getConnectionURL());
                    credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

                    credentialsModel.setUsername(paasProvider.getUsername());
                    credentialsModel.setPassword(paasProvider.getPassword());
                    credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

                    SPIResponse spiResponse = paasAdapter.validateCredentials(credentialsModel, paaSOfferingModelSPI);

                    if (null != spiResponse && spiResponse.getCode().equals(eu.paasword.spi.response.BasicResponseCode.SUCCESS)) {

                        // Step 1: Deploy Application to PaaS Provider

                        ApplicationModel app = null;

                        ApplicationRequestModel applicationRequestModel = new ApplicationRequestModel();
                        applicationRequestModel.setName(appInstance.getName());
                        applicationRequestModel.setStackName("java_buildpack");

                        spiResponse = paasAdapter.createApplication(credentialsModel, applicationRequestModel);

                        if (null != spiResponse && spiResponse.getCode().equals(eu.paasword.spi.response.BasicResponseCode.SUCCESS)) {

                            app = (ApplicationModel) spiResponse.getReturnobject();

                            if (!app.getApplicationStateTypeModel().equals(ApplicationStateTypeModel.ERROR)) {

                                appInstance.setRunningEndpointURL(app.getRunningEndpoint());

                                app.setName(appInstance.getName());
                                app.setApplicationRequest(applicationRequestModel);

                                PackageLocatorModel packageLocator = new PackageLocatorModel();

                                ApplicationBinary applicationBinary = applicationBinaryRepository.findByApplicationID(application);

                                packageLocator.setFile(applicationBinary.getBinary());
                                packageLocator.setFilename(application.getFileName());

                                spiResponse = paasAdapter.deployApplication(credentialsModel, app, packageLocator);

                                if (null != spiResponse && spiResponse.getCode().equals(eu.paasword.spi.response.BasicResponseCode.SUCCESS)) {

                                    appInstance.setOverallStatus(10);
                                    applicationInstanceService.edit(appInstance);

                                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_SUCCESS_DEPLOYMENT, Optional.empty());
                                } else {
                                    appInstance.setOverallStatus(9);
                                    applicationInstanceService.edit(appInstance);
                                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_DEPLOYMENT, Optional.empty());
                                }

                            } else {
                                appInstance.setOverallStatus(9);
                                applicationInstanceService.edit(appInstance);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_DEPLOYMENT, Optional.empty());
                            }

                        } else {
                            appInstance.setOverallStatus(9);
                            applicationInstanceService.edit(appInstance);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_DEPLOYMENT, Optional.empty());
                        }

                    } else {
                        appInstance.setOverallStatus(9);
                        applicationInstanceService.edit(appInstance);
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_DEPLOYMENT, Optional.empty());
                    }

                } else {
                    logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
                }


            } catch (ApplicationInstanceDoesNotExist | PaaSProviderDoesNotExist | ApplicationDoesNotExist ex) {
                Logger.getLogger(ApplicationInstanceManagementRestController.class.getName()).severe(ex.getMessage());
                return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
            }

        } else if (tApplicationInstance.getDeploymentType() == 2) {

            // Deploy Application Instance to SlipStream
            try {

                ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOne(tApplicationInstance.getId()).get();

                if (null != appInstance && appInstance.getApplicationID().getId() == tApplicationInstance.getApplicationID()) {

                    appInstance.setDeploymentType(2);

                    Application application = (Application) applicationService.findOne(tApplicationInstance.getApplicationID()).get();

                    logger.info("Calling SlipStream Proxy...");

                    JSONObject slipStreamProxy = new JSONObject();

                    JSONObject slipStreamAppInstance = new JSONObject();

                    slipStreamAppInstance.put("component", "PaaSword/SpringBoot");

                    // Add Hook URL
                    String paaswordControllerURL = environment.getProperty("paasword.controller.url");
                    slipStreamAppInstance.put("archive-url", paaswordControllerURL + "/api/v1/slipstream/archive/" + tApplicationInstance.getAppInstanceKey());
                    slipStreamAppInstance.put("controller-url", paaswordControllerURL);
                    slipStreamAppInstance.put("application-id", tApplicationInstance.getAppInstanceKey());
                    slipStreamAppInstance.put("location", "gr");

                    slipStreamProxy.put(tApplicationInstance.getAppInstanceKey(), slipStreamAppInstance);
                    slipStreamProxy.put("hookURL", paaswordControllerURL + "/api/v1/slipstream/application/" + tApplicationInstance.getAppInstanceKey());

                    UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
                    String username = userauthentication.getDetails().getUsername();

                    User user = (User) userService.findByUsername(username).get();

                    List<ProxyCloudProvider> proxyCloudProviders = proxyCloudProviderService.findByUser(user);

                    UserCredential userCredential = userCredentialService.findByProxyCloudProviderAndUser(proxyCloudProviders.get(0).getId(), user.getId());

                    if (null != userCredential) {

                        slipStreamProxy.put("username", userCredential.getUsername());
                        slipStreamProxy.put("password", userCredential.getPassword());

                    } else {
                        logger.severe(Message.SLIPSTREAM_ACCOUNT_NOT_AVAILABLE);
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.SLIPSTREAM_ACCOUNT_NOT_AVAILABLE, Optional.empty());
                    }

                    // Call SlipStream Proxy
                    if (null == restTemplate) {
                        restTemplate = new RestTemplate();
                    }

                    logger.info("JSON for SlipStream Proxy: " + slipStreamProxy.toString());

                    HttpHeaders headers = new HttpHeaders();
                    headers.put("Content-Type", Arrays.asList("application/json"));

                    HttpEntity entity = new HttpEntity(slipStreamProxy.toString(), headers);

                    String slipStreamProxyURL = environment.getProperty("slipstream.proxy.url");

                    logger.info("SlipStream URL: " + slipStreamProxyURL);

                    ResponseEntity<String> responseEntity = restTemplate.exchange(slipStreamProxyURL, HttpMethod.POST, entity, String.class);

                    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.ACCEPTED) {

                        appInstance.setOverallStatus(12);

                        applicationInstanceService.edit(appInstance);

                        logger.info("DB Proxy request sent to SlipStream Proxy successfully!");

                        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_SUCCESS_DEPLOYMENT, Optional.empty());

                    } else {

                        appInstance.setOverallStatus(9);

                        applicationInstanceService.edit(appInstance);

                        logger.severe("Response Status: " + responseEntity.getStatusCode());
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_DEPLOYMENT, Optional.empty());
                    }

                } else {
                    logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
                }


            } catch (ApplicationInstanceDoesNotExist | ApplicationDoesNotExist ex) {
                Logger.getLogger(ApplicationInstanceManagementRestController.class.getName()).severe(ex.getMessage());
                return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
            }

        } else {
            logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
        }

    }

    /**
     * The exposed endpoint is used via the UI, which attempts to start an Application Instance
     *
     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public PaaSwordRestResponse startInstance(@RequestBody TApplicationInstance tApplicationInstance) {

        if (tApplicationInstance.getDeploymentType() == 1) {

            try {

                ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOne(tApplicationInstance.getId()).get();

                if (null != appInstance && appInstance.getApplicationID().getId() == tApplicationInstance.getApplicationID()) {

                    Application application = (Application) applicationService.findOneWithoutBlob(tApplicationInstance.getApplicationID()).get();

                    PaaSProvider paasProvider = appInstance.getPaaSProviderID();

                    appInstance.setPaaSProviderID(paasProvider);
                    appInstance.setApplicationID(application);

                    // Step 1: Start Application to PaaS Provider

                    PaaSProviderType paasProviderType = paasProvider.getPaasProviderTypeID();

                    PaaSAdapter paasAdapter = (PaaSAdapter) ((List) paasAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(paasProviderType.getAdapterImplementation())).collect(Collectors.toList())).get(0);

                    CredentialsModel credentialsModel = new CredentialsModel();

                    PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
                    paaSOfferingModelSPI.setEndpointURI(paasProvider.getConnectionURL());
                    credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

                    credentialsModel.setUsername(paasProvider.getUsername());
                    credentialsModel.setPassword(paasProvider.getPassword());
                    credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

                    ApplicationModel app = new ApplicationModel();
                    app.setName(appInstance.getName());

                    SPIResponse spiResponse = paasAdapter.startApplication(credentialsModel, app);

                    if (null != spiResponse && spiResponse.getCode().equals(eu.paasword.spi.response.BasicResponseCode.SUCCESS)) {

                        app = (ApplicationModel) spiResponse.getReturnobject();

                        if (!app.getApplicationStateTypeModel().equals(ApplicationStateTypeModel.ERROR)) {

                            appInstance.setOverallStatus(11);
                            applicationInstanceService.edit(appInstance);

                            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_SUCCESS_STARTED, Optional.empty());

                        } else {
                            appInstance.setOverallStatus(9);
                            applicationInstanceService.edit(appInstance);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_STARTED, Optional.empty());
                        }

                    } else {
                        appInstance.setOverallStatus(9);
                        applicationInstanceService.edit(appInstance);
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_STARTED, Optional.empty());
                    }

                } else {
                    logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
                }


            } catch (ApplicationInstanceDoesNotExist | ApplicationDoesNotExist ex) {
                Logger.getLogger(ApplicationInstanceManagementRestController.class.getName()).severe(ex.getMessage());
                return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
            }

        } else if (tApplicationInstance.getDeploymentType() == 2) {

            // Deploy Application Instance to SlipStream
            // TODO


            // TODO Remove this
            logger.severe(Message.APPLICATION_INSTANCE_NOT_IMPLEMENTED);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_NOT_IMPLEMENTED, Optional.empty());

        } else {
            logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
        }

    }

    /**
     * The exposed endpoint is used via the UI, which attempts to stop an Application Instance
     *
     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    public PaaSwordRestResponse stopInstance(@RequestBody TApplicationInstance tApplicationInstance) {

        if (tApplicationInstance.getDeploymentType() == 1) {

            try {

                ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOne(tApplicationInstance.getId()).get();

                if (null != appInstance && appInstance.getApplicationID().getId() == tApplicationInstance.getApplicationID()) {

                    Application application = (Application) applicationService.findOneWithoutBlob(tApplicationInstance.getApplicationID()).get();

                    PaaSProvider paasProvider = appInstance.getPaaSProviderID();

                    appInstance.setPaaSProviderID(paasProvider);
                    appInstance.setApplicationID(application);

                    // Step 1: Start Application to PaaS Provider

                    PaaSProviderType paasProviderType = paasProvider.getPaasProviderTypeID();

                    PaaSAdapter paasAdapter = (PaaSAdapter) ((List) paasAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(paasProviderType.getAdapterImplementation())).collect(Collectors.toList())).get(0);

                    CredentialsModel credentialsModel = new CredentialsModel();

                    PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
                    paaSOfferingModelSPI.setEndpointURI(paasProvider.getConnectionURL());
                    credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

                    credentialsModel.setUsername(paasProvider.getUsername());
                    credentialsModel.setPassword(paasProvider.getPassword());
                    credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

                    ApplicationModel app = new ApplicationModel();
                    app.setName(appInstance.getName());

                    SPIResponse spiResponse = paasAdapter.stopApplication(credentialsModel, app);

                    if (null != spiResponse && spiResponse.getCode().equals(eu.paasword.spi.response.BasicResponseCode.SUCCESS)) {

                        app = (ApplicationModel) spiResponse.getReturnobject();

                        if (!app.getApplicationStateTypeModel().equals(ApplicationStateTypeModel.ERROR)) {

                            appInstance.setOverallStatus(10);
                            applicationInstanceService.edit(appInstance);

                            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_SUCCESS_STARTED, Optional.empty());

                        } else {
                            appInstance.setOverallStatus(9);
                            applicationInstanceService.edit(appInstance);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_STARTED, Optional.empty());
                        }

                    } else {
                        appInstance.setOverallStatus(9);
                        applicationInstanceService.edit(appInstance);
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_STARTED, Optional.empty());
                    }

                } else {
                    logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
                }


            } catch (ApplicationInstanceDoesNotExist | ApplicationDoesNotExist ex) {
                Logger.getLogger(ApplicationInstanceManagementRestController.class.getName()).severe(ex.getMessage());
                return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
            }

        } else if (tApplicationInstance.getDeploymentType() == 2) {

            // Deploy Application Instance to SlipStream
            // TODO


            // TODO Remove this
            logger.severe(Message.APPLICATION_INSTANCE_NOT_IMPLEMENTED);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_NOT_IMPLEMENTED, Optional.empty());

        } else {
            logger.severe(Message.APPLICATION_INSTANCE_INVALID_PARAMETERS);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_INVALID_PARAMETERS, Optional.empty());
        }

    }

//    /**
//     * The exposed endpoint is used via the UI, which attempts to initialize the infrastructure of an Application Instance
//     *
//     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
//     * @return PaaSwordRestResponse object
//     */
//    @RequestMapping(value = "/undeploy", method = RequestMethod.POST)
//    public PaaSwordRestResponse undeployInstance(@RequestBody TApplicationInstance tApplicationInstance) {
//
//
//        if (0 == tApplicationInstance.getPaaSproviderID()) {
//            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
//        }
//
//        try {
//
//            ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOne(tApplicationInstance.getId()).get();
//
//            PaaSProvider paaSProvider = appInstance.getPaaSProviderID();
//
//
//            boolean isSuccess = CloudFoundryAdapter.undeployAndDeleteApplication(paaSProvider.getUsername(), paaSProvider.getPassword(), paaSProvider.getConnectionURL(), appInstance.getName().trim());
//
//            if (isSuccess) {
//                appInstance.setPublicURL(null);
//                appInstance.setPaaSProviderID(null);
////                appInstance.setDeployed(false);
//                appInstance.setOverallStatus(2);
//                applicationInstanceService.edit(appInstance);
//
//                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_UNDEPLOYED, Optional.empty());
//            } else {
//                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_ERROR_UNDEPLOYMENT, appInstance.getId());
//            }
//
//
//        } catch (ApplicationInstanceDoesNotExist ex) {
//            Logger.getLogger(ApplicationInstanceManagementRestController.class.getName()).severe(ex.getMessage());
//            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
//        }
//
//    }

//    /**
//     * The exposed endpoint is used via the UI, which attempts to create a new Application Instance to the database.
//     *
//     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
//     * @return PaaSwordRestResponse object
//     */
//    @RequestMapping(method = RequestMethod.POST)
//    public PaaSwordRestResponse create(@RequestBody TApplicationInstance tApplicationInstance) {
//        try {
//
//            Application app = (Application) applicationService.findOne(tApplicationInstance.getApplicationID()).get();
//
//            ApplicationInstance applicationInstance = new ApplicationInstance();
//            applicationInstance.setName(tApplicationInstance.getName());
//            applicationInstance.setApplicationID(app);
////            applicationInstance.setDateCreated(new Date());
//
////            applicationInstance.setPrivacyConstraint(new JSONArray(tApplicationInstance.getPrivacyConstraint()).toString());
//
//            applicationInstanceService.create(applicationInstance);
//        } catch (ApplicationInstanceAlreadyExistsException | ApplicationDoesNotExist ex) {
//            Logger.getLogger(ApplicationInstanceManagementRestController.class.getName()).severe(ex.getMessage());
//            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
//        }
//
//        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_CREATED, Optional.empty());
//    }
//
//    /**
//     * The exposed endpoint is used via the UI, which attempts to update an existing Application Instance to the database.
//     *
//     * @param tApplicationInstance A JSON object which will be casted to a ApplicationInstance (java) object
//     * @return PaaSwordRestResponse object
//     */
//    @RequestMapping(method = RequestMethod.PUT)
//    public PaaSwordRestResponse edit(@RequestBody TApplicationInstance tApplicationInstance) {
//        try {
//
//            Application app = (Application) applicationService.findOne(tApplicationInstance.getApplicationID()).get();
//
//            ApplicationInstance applicationInstance = applicationInstanceService.findOne(tApplicationInstance.getId()).get();
//
//            applicationInstance.setName(tApplicationInstance.getName());
////            applicationInstance.setPrivacyConstraint(new JSONArray(tApplicationInstance.getPrivacyConstraint()).toString());
//
//            applicationInstanceService.edit(applicationInstance);
//
//
//        } catch (ApplicationInstanceDoesNotExist | ApplicationDoesNotExist ex) {
//            Logger.getLogger(ApplicationInstanceManagementRestController.class.getName()).severe(ex.getMessage());
//            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
//        }
//
//        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_UPDATED, Optional.empty());
//    }

    /**
     * The exposed endpoint is used via the UI, which attempts to revoke all keys from the database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}/revokekeys", method = RequestMethod.DELETE)
    public PaaSwordRestResponse revokeAllUsers(@PathVariable(value = "id") long id) {

        try {

            if (0 != id) {

                ApplicationInstance applicationInstance = applicationInstanceService.findOne(id).get();

                if (null != applicationInstance) {


                    // Check if keymgt is enabled



                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_USERS_REVOKED, Optional.empty());

                }

            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            ex.printStackTrace();
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_USERS_REVOKED_ERROR, Optional.empty());
    }

    /**
     * Deletes an Application Instance from database.
     *
     * @param id The id of the Application Instance to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {

            boolean deleteOK = false;

            ApplicationInstance appInstance = (ApplicationInstance) applicationInstanceService.findOne(id).get();

            // TODO
            //



//            if (appInstance.getOverallStatus() >= 2) {
//
//                logger.info("Deleting DB Proxy...");
//
//                List<FragServer> listOfFragServers = new ArrayList<>();
//
//                JSONArray iaasArray = new JSONArray(appInstance.getIaasProviderInstances());
//
//                for (Object iaasObj : iaasArray) {
//
//                    JSONObject json = (JSONObject) iaasObj;
//
//                    FragServer server = new FragServer();
//                    server.setServerid(json.getString("serverID"));
//                    server.setName(json.getString("dbName"));
//
//                    IaaSProvider iaaSProvider = (IaaSProvider) iaaSProviderService.findOne(Long.valueOf(json.getString("iaasProviderID"))).get();
//                    IaaS iaas = new IaaS(String.valueOf(iaaSProvider.getId()), iaaSProvider.getConnectionURL(), iaaSProvider.getUsername(), iaaSProvider.getPassword(), iaaSProvider.getTenantName(), iaaSProvider.getProject(), null);
//
//                    server.setIaas(iaas);
//
//                    listOfFragServers.add(server);
//
//                }
//
//                APIKey apiKey = (APIKey) apiKeyService.findByApplicationID(appInstance.getApplicationIDInstance()).get(0);
//
//                String key = apiKey.getUniqueID();
//
//                boolean isIaaSSuccess = DBProxyOrchestrator.DestroyDBProxy(key, listOfFragServers);
//
//                if (isIaaSSuccess) {
//                    logger.info("DB Proxy deletion failed!");
//                    deleteOK = false;
//                } else {
//                    logger.info("DB Proxy deleted successfully!");
//                }
//
//            }
//
//            if (appInstance.getOverallStatus() == 3) {
//
//                appInstance = (ApplicationInstance) applicationInstanceService.findOneWithPaaSProviderAndOnlyWithApplicationID(id);
//
//                logger.info("Deleting Application Instance from PaaS Provider");
//
//                PaaSProvider paaSProvider = appInstance.getPaaSProviderID();
//                boolean isPaaSSuccess = CloudFoundryAdapter.undeployAndDeleteApplication(paaSProvider.getUsername(), paaSProvider.getPassword(), paaSProvider.getConnectionURL(), appInstance.getName().trim());
//
//                if (!isPaaSSuccess) {
//                    logger.info("Application Instance deletion from PaaS Provider failed");
//                    deleteOK = false;
//                } else {
//                    logger.info("Application Instance deleted successfully from PaaS Provider");
//                }
//            }

            if (deleteOK) {
                applicationInstanceService.delete(id);
                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_INSTANCE_DELETED, Optional.empty());
            } else {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INSTANCE_DELETED_ERROR, Optional.empty());
            }

        } catch (ApplicationInstanceDoesNotExist ex) {
            logger.severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }


    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     */
    private final static class Message {

        final static String APPLICATION_INSTANCE_INVALID_REQUEST = "Privacy and/or Affinity Constraints are required";
        final static String APPLICATION_INSTANCE_INVALID_PARAMETERS = "Invalid parameters";
        final static String APPLICATION_INSTANCE_DELETED = "Application Instance has been deleted successfully";
        final static String APPLICATION_INSTANCE_DELETED_ERROR = "Application Instance has not been deleted successfully";
        final static String APPLICATION_INSTANCE_UPDATED = "Application Instance has been updated";
        final static String APPLICATION_INSTANCE_CREATED = "Application Instance has been created";
        final static String APPLICATION_INSTANCE_CONFIGURED = "Application Instance has been configured successfully";
        final static String APPLICATION_INSTANCE_RECONFIGURED = "Application Instance has been reconfigured successfully";
        final static String APPLICATION_INSTANCE_VALIDATED = "Application Instance has been validated successfully";
        final static String APPLICATION_INSTANCE_DEPLOYED = "Application Instance has been deployed successfully";
        final static String APPLICATION_INSTANCE_UNDEPLOYED = "Application Instance has been undeployed successfully";
        final static String APPLICATION_INSTANCE_DB_PROXY_DEPLOYMENT_ERROR = "DB Proxy has not been deployed successfully";
        final static String APPLICATION_INSTANCE_DB_PROXY_DEPLOYMENT_SUCCESS = "DB Proxy has been deployed successfully";
        final static String APPLICATION_INSTANCE_DB_PROXY_DEPLOYMENT_SUCCESS_PENDING = "DB Proxy is deploying";
        final static String APPLICATION_INSTANCE_ERROR_UNDEPLOYMENT = "Error during undeploying";
        final static String APPLICATION_INSTANCE_ERROR_DEPLOYMENT = "Application Instance has not been deployed successfully";
        final static String APPLICATION_INSTANCE_SUCCESS_DEPLOYMENT = "Application Instance has been deployed successfully";
        final static String APPLICATION_INSTANCE_FRAGMENTATION_SCHEMA_INVALID = "Fragmentation Schema is invalid";
        final static String APPLICATION_INSTANCE_IAAS_PROVIDERS_REQUIRED = "IaaS Providers are required";
        final static String APPLICATION_INSTANCE_PAAS_PROVIDER_REQUIRED = "PaaS Provider is required";
        final static String APPLICATION_INSTANCE_NOT_IMPLEMENTED = "Not implemented yet";
        final static String APPLICATION_INSTANCE_ERROR_STARTED = "Application Instance has not been started successfully";
        final static String APPLICATION_INSTANCE_SUCCESS_STARTED = "Application Instance has been started successfully";
        final static String APPLICATION_INSTANCE_ERROR_STOPPED = "Application Instance has not been stopped successfully";
        final static String APPLICATION_INSTANCE_SUCCESS_STOPPED = "Application Instance has been stopped successfully";
        final static String APPLICATION_INSTANCE_HANDLER_ASSIGNED = "Application Instance has been assigned successfully";
        final static String APPLICATION_INSTANCE_HANDLER_UNASSIGNED = "Application Instance has been unassigned successfully";
        final static String SLIPSTREAM_ACCOUNT_NOT_AVAILABLE = "SlipStream Account is not available";
        final static String APPLICATION_INSTANCE_USERS_REVOKED = "All user keys have been revoked successfully";
        final static String APPLICATION_INSTANCE_USERS_REVOKED_ERROR = "All user keys have not been revoked successfully";
    }

}
