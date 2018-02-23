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
package eu.paasword.rest.slipstream;

import eu.paasword.api.repository.IAPIKeyService;
import eu.paasword.api.repository.IApplicationInstanceService;
import eu.paasword.api.repository.IApplicationPrivacyConstraintService;
import eu.paasword.api.repository.IApplicationService;
import eu.paasword.api.repository.exception.apikey.APIKeyUniqueIDDoesNotExist;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceDoesNotExist;
import eu.paasword.dbproxy.DBProxyOrchestratorResponse;
import eu.paasword.dbproxy.DatabaseProxyEngine;
import eu.paasword.repository.relational.dao.ApplicationBinaryRepository;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.spi.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding SlipStream Proxy
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/slipstream")
public class SlipStreamProxyManagementRestController {

    private static final Logger logger = Logger.getLogger(SlipStreamProxyManagementRestController.class.getName());

    @Autowired
    IApplicationInstanceService<ApplicationInstance> applicationInstanceService;

    @Autowired
    IAPIKeyService<APIKey> apiKeyService;

    @Autowired
    IApplicationService<Application> applicationService;

    @Autowired
    DatabaseProxyEngine databaseProxyEngine;

    @Autowired
    ApplicationBinaryRepository applicationBinaryRepository;

    @Autowired
    IApplicationPrivacyConstraintService<ApplicationPrivacyConstraint> applicationPrivacyConstraintService;

    /**
     * Hello from SlipStream Proxy API
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse info() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, "SlipStream Proxy API is activated!", Optional.empty());
    }

    /**
     * This method is called by SlipStream Proxy in order to initialize the databases of the DB Proxy
     *
     * @param responseFromSlipStream A JSON object which will be casted to a String object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/dbproxy/{dbProxyID}", method = RequestMethod.PUT)
    public PaaSwordRestResponse finalizeDBProxy(@RequestBody String responseFromSlipStream, @PathVariable("dbProxyID") String dbProxyID) {


        if (null != dbProxyID && !dbProxyID.isEmpty() && null != responseFromSlipStream && !responseFromSlipStream.isEmpty()) {

            logger.info("JSON from SlipStream: " + responseFromSlipStream);

            JSONObject responseFromSlipStreamObj = new JSONObject(responseFromSlipStream);

            try {

                Application application = ((APIKey) apiKeyService.findByUniqueID(dbProxyID).get()).getApplicationID();

                if (null != application) {

                    ApplicationInstance appInstance = application.getApplicationInstance();

                    if (null != appInstance && appInstance.getOverallStatus() == 6) {

                        JSONArray fragmentsArray = null;

                        if (responseFromSlipStreamObj.getInt("status") == 200) {

                            fragmentsArray = new JSONArray(appInstance.getFragmentationSchema());

                            int fragments = fragmentsArray.length(); // + 2 Index Servers

                            JSONArray iaasInstances = new JSONArray();

                            for (int i = 1; i <= fragments; i++) {

                                JSONObject srv = new JSONObject();

                                srv.put("dbHost", responseFromSlipStreamObj.getJSONObject(i + "").getString("hostname"));
                                srv.put("dbPort", responseFromSlipStreamObj.getJSONObject(i + "").getString("port"));
                                srv.put("dbName", "kit_server_" + (i-1));
                                srv.put("serverID", responseFromSlipStreamObj.getJSONObject(i + "").getString("hostname"));
                                srv.put("iaasProviderID", responseFromSlipStreamObj.getJSONObject(i + "").getString("hostname"));
                                srv.put("iaasFriendlyName", "Deployed to SlipStream");

                                iaasInstances.put(srv);


                            }

                            // Index Servers
                            JSONObject remoteIndexServer = new JSONObject();
                            remoteIndexServer.put("dbHost", responseFromSlipStreamObj.getJSONObject(String.valueOf(fragments + 1)).getString("hostname"));
                            remoteIndexServer.put("dbPort", responseFromSlipStreamObj.getJSONObject(String.valueOf(fragments + 1)).getString("port"));
                            remoteIndexServer.put("dbName", "kit_mimosecco_remote");
                            remoteIndexServer.put("serverID", responseFromSlipStreamObj.getJSONObject(String.valueOf(fragments + 1)).getString("hostname"));
                            remoteIndexServer.put("iaasProviderID", responseFromSlipStreamObj.getJSONObject(String.valueOf(fragments + 1)).getString("hostname"));
                            remoteIndexServer.put("iaasFriendlyName", "Deployed to SlipStream");

                            iaasInstances.put(remoteIndexServer);

                            JSONObject localIndexServer = new JSONObject();
                            localIndexServer.put("dbHost", responseFromSlipStreamObj.getJSONObject(String.valueOf(fragments + 2)).getString("hostname"));
                            localIndexServer.put("dbPort", responseFromSlipStreamObj.getJSONObject(String.valueOf(fragments + 2)).getString("port"));
                            localIndexServer.put("dbName", "kit_mimosecco_local");
                            localIndexServer.put("serverID", responseFromSlipStreamObj.getJSONObject(String.valueOf(fragments + 2)).getString("hostname"));
                            localIndexServer.put("iaasProviderID", responseFromSlipStreamObj.getJSONObject(String.valueOf(fragments + 2)).getString("hostname"));
                            localIndexServer.put("iaasFriendlyName", "Deployed to SlipStream");

                            iaasInstances.put(localIndexServer);

                            appInstance.setIaasProviderInstances(iaasInstances.toString());

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
                            JSONArray privacyConstraintsSetArray = new JSONArray(appInstance.getPrivacyConstraintsSet());

                            for (Object privacyConstraintsSetObj : privacyConstraintsSetArray) {

                                String privacyConstraintStr = (String) privacyConstraintsSetObj;

                                ApplicationPrivacyConstraint privacyConstraint = applicationPrivacyConstraintService.findOneWithoutApplication(Long.valueOf(privacyConstraintStr));

                                ArrayList<String> constraints = new ArrayList<String>();

                                JSONArray jsonConstraints = new JSONArray(privacyConstraint.getPrivacyConstraint());

                                for (Object constr : jsonConstraints) {

                                    constraints.add(((String) constr).toLowerCase());

                                }
                                constraintSets.add(constraints);

                            }

                            String tenantKey = Util.generateRandomString(16, Util.Mode.ALPHA);

                            // Call DBProxyOrchestrator to initialize DBs
                            DBProxyOrchestratorResponse dbResponse = databaseProxyEngine.initializeSlipStreamDBProxy(application.getApiKeys().get(0).getUniqueID(), tenantKey, iaasInstances, createStatements, fields, constraintSets);

                            if (null != dbResponse && dbResponse.isSuccessresult()) {

                                appInstance.setApplicationID(application);
                                appInstance.setConfigurationFile(dbResponse.getConfigurationxml());
                                appInstance.setOverallStatus(5);

                                application.setTenantKey(tenantKey);

                                applicationService.edit(application);

                                appInstance.setTenantKey(tenantKey);

                                applicationInstanceService.edit(appInstance);

                                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.SLIPSTREAM_DB_PROXY_DEPLOYED, Optional.empty());

                            } else {

                                appInstance.setApplicationID(application);
                                appInstance.setOverallStatus(4);

                                applicationInstanceService.edit(appInstance);

                                logger.severe(Message.SLIPSTREAM_DB_PROXY_DEPLOYED_ERROR);
                                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.SLIPSTREAM_DB_PROXY_DEPLOYED_ERROR, Optional.empty());

                            }

                        } else {

                            appInstance.setApplicationID(application);
                            appInstance.setOverallStatus(4);

                            applicationInstanceService.edit(appInstance);

                            logger.severe(Message.SLIPSTREAM_DB_PROXY_DEPLOYED_ERROR);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.SLIPSTREAM_DB_PROXY_DEPLOYED_ERROR, Optional.empty());

                        }

                    } else {
                        logger.severe(Message.INVALID_DB_PROXY);
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_DB_PROXY, Optional.empty());
                    }

                } else {
                    logger.severe(Message.INVALID_DB_PROXY);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_DB_PROXY, Optional.empty());
                }


            } catch (APIKeyUniqueIDDoesNotExist | ApplicationInstanceDoesNotExist | ApplicationDoesNotExist e) {
                logger.severe(e.getMessage());
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, e.getMessage(), e);
            }


        } else {
            logger.severe(Message.SLIPSTREAM_DB_PROXY_DEPLOYED_ERROR);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.SLIPSTREAM_DB_PROXY_DEPLOYED_ERROR, Optional.empty());
        }


    }//EoM

    /**
     * This method is called by SlipStream Proxy in order to inform where an existing PaaSword-enabled Application is deployed
     *
     * @param responseFromSlipStream A JSON object which will be casted to a String object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/application/{appID}", method = RequestMethod.PUT)
    public PaaSwordRestResponse finalizeDeployment(@RequestBody String responseFromSlipStream, @PathVariable("appID") String appID) {


        if (null != appID && !appID.isEmpty() && null != responseFromSlipStream && !responseFromSlipStream.isEmpty()) {

            logger.info("JSON from SlipStream: " + responseFromSlipStream);

            JSONObject responseFromSlipStreamObj = new JSONObject(responseFromSlipStream);

            try {

                Application application = ((APIKey) apiKeyService.findByUniqueID(appID).get()).getApplicationID();

                if (null != application) {

                    ApplicationInstance appInstance = application.getApplicationInstance();

                    if (null != appInstance && appInstance.getOverallStatus() == 12) {

                        if (responseFromSlipStreamObj.getInt("status") == 200) {

                            String hostname = responseFromSlipStreamObj.getJSONObject(appID).getString("hostname");

                            appInstance.setRunningEndpointURL(hostname);

                            appInstance.setApplicationID(application);
                            appInstance.setOverallStatus(11);

                            applicationInstanceService.edit(appInstance);

                            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.SLIPSTREAM_APPLICATION_DEPLOYED, Optional.empty());


                        } else {

                            appInstance.setApplicationID(application);
                            appInstance.setOverallStatus(9);

                            applicationInstanceService.edit(appInstance);

                            logger.severe(Message.SLIPSTREAM_APPLICATION_DEPLOYED_ERROR);
                            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.SLIPSTREAM_APPLICATION_DEPLOYED_ERROR, Optional.empty());

                        }

                    } else {
                        logger.severe(Message.INVALID_APPLICATION);
                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_DB_PROXY, Optional.empty());
                    }

                } else {
                    logger.severe(Message.INVALID_APPLICATION);
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_DB_PROXY, Optional.empty());
                }


            } catch (APIKeyUniqueIDDoesNotExist | ApplicationInstanceDoesNotExist e) {
                logger.severe(e.getMessage());
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, e.getMessage(), e);
            }


        } else {
            logger.severe(Message.SLIPSTREAM_DB_PROXY_DEPLOYED_ERROR);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.SLIPSTREAM_APPLICATION_DEPLOYED_ERROR, Optional.empty());
        }


    }//EoM

    @RequestMapping(value = "/archive/{appKey}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> fetchArchive(@PathVariable("appKey") String appKey, HttpServletRequest request) {

        try {

            APIKey apiKey = (APIKey) apiKeyService.findByUniqueID(appKey).get();

            if (null != apiKey) {

                Application application = (Application) applicationService.findOne(apiKey.getApplicationID().getId()).get();

                ApplicationBinary applicationBinary = applicationBinaryRepository.findByApplicationID(application);

                ApplicationInstance applicationInstance = applicationInstanceService.findByApplicationID(application.getId());

                if (null != application && null != applicationInstance && null != applicationBinary && null != applicationBinary.getBinary() && applicationInstance.getOverallStatus() == 12) {

                    HttpHeaders responseHeaders = new HttpHeaders();
                    responseHeaders.set("Charset", StandardCharsets.UTF_8.displayName());
                    responseHeaders.set("Content-Description", "File Transfer");
                    responseHeaders.set("Content-type", "application/octet-stream");
                    responseHeaders.set("Content-disposition", "attachment; filename=" + application.getFileName());
                    responseHeaders.setContentLength(applicationBinary.getBinary().length);
                    return new ResponseEntity<>(applicationBinary.getBinary(), responseHeaders, HttpStatus.OK);

                } else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (APIKeyUniqueIDDoesNotExist | ApplicationDoesNotExist e) {
            e.printStackTrace();
            logger.severe(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Inner class containing all the static messages which will be used in an
     * PaaSwordRestResponse.
     */
    private final static class Message {

        final static String SLIPSTREAM_DB_PROXY_DEPLOYED = "DB Proxy deployed successfully";
        final static String SLIPSTREAM_DB_PROXY_DEPLOYED_ERROR = "DB Proxy hasn't been deployed successfully";
        final static String SLIPSTREAM_APPLICATION_DEPLOYED = "Application instance has been deployed successfully";
        final static String SLIPSTREAM_APPLICATION_DEPLOYED_ERROR = "Application instance hasn't been deployed successfully";
        final static String INVALID_DB_PROXY = "Invalid DB Proxy ID";
        final static String INVALID_APPLICATION = "Invalid Application ID";


    }//Message

}
