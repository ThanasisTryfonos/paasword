/*
 * Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.paasword.paaswordlibrary.semanticauthorizationengine;

import java.util.logging.Level;
import java.util.logging.Logger;

import eu.paasword.paaswordlibrary.transferobject.ApplicationValidation;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/*
 *  
 * @author smantzouratos
 */
public enum SemanticAuthorizationEngineHandler {

    INSTANCE;

    private static final Logger logger = Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName());

    final RestTemplate restTemplate;

    private SemanticAuthorizationEngineHandler() {
        restTemplate = new RestTemplate();
    }

    /**
     * Check if the current PaaSword Controller is running
     *
     * @return Return true if PaaSword Controller is running or false
     * on error
     */
    public synchronized boolean isAlive(String paaswordControllerURL) {
        boolean isSuccess = false;

        ResponseEntity<String> responseEntity = restTemplate.exchange(paaswordControllerURL + "/api/v1/semanticauthorizationengine", HttpMethod.GET, null, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            isSuccess = true;
            logger.info("PaaSword Controller is alive..");

        } else {
            logger.log(Level.SEVERE, "SemanticAuthorizationEngineHandler: Response Status: {0}", new Object[]{responseEntity.getStatusCode()});
        }

        return isSuccess;
    }

    /**
     * Validates an application during bootstrapping
     *
     * @param jsonMessage A JSON formatted String representing information of a PaaSword-enabled application
     * @param isDebugEnabled
     * if an error was occurred.
     */
    public ApplicationValidation validateApplication(String paaswordControllerURL, String jsonMessage, boolean isDebugEnabled) {

        logger.info("ValidateApplication is invoked()..");

        if (isAlive(paaswordControllerURL)) {

            if (null != jsonMessage) {

                if (isDebugEnabled) {
                    Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.INFO, "JSON message:\n{0}", jsonMessage);
                }

                HttpEntity entity = new HttpEntity(jsonMessage.toString(), null);

                ResponseEntity<String> responseEntity = restTemplate.exchange(paaswordControllerURL + "/api/v1/semanticauthorizationengine/validation", HttpMethod.POST, entity, String.class);

                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                    JSONObject response = new JSONObject(responseEntity.getBody());

                    if (response.getString("code").equalsIgnoreCase("SUCCESS")) {

                        Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.INFO, "Method response: {0}", response.getString("message"));

                        return new ApplicationValidation(null, true, response.getBoolean("returnobject"));

                    } else {
                        Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.SEVERE, "Method failed: {0}", response.getString("message"));
                        return null;
                    }

                } else {
                    Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.SEVERE, "Method failed: {0}", responseEntity.getStatusCode());
                    return null;
                }

            } else {
                return null;
            }

        } else {
            return null;
        }

    }

    public boolean invokeHttpRequestToSemanticAuthorizationEngine(String paaswordControllerURL, String jsonMessage, boolean isDebugEnabled) {

        logger.info("Interception request is invoked()..");

        String jsonResponse = "";
        if (!isAlive(paaswordControllerURL)) {
            Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).info("Could not proceed, PaaSword Controller instance is down...");
        } else {

            HttpEntity entity = new HttpEntity(jsonMessage.toString(), null);

            ResponseEntity<String> responseEntity = restTemplate.exchange(paaswordControllerURL + "/api/v1/semanticauthorizationengine/getAuthenticationPermission", HttpMethod.POST, entity, String.class);

            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                JSONObject response = new JSONObject(new String(responseEntity.getBody()));

                if (response.getString("code").equalsIgnoreCase("SUCCESS")) {
                    logger.info("Response: " + response);

//                    Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.INFO, "Method response: {0}", response.getString("message"));

                    String advice = new JSONObject(response.getString("message")).getJSONObject("returnobject").getString("advice");

                    logger.info("Advice: " + advice);

                    if (advice.equalsIgnoreCase("DENY")) {

                        return false;

                    }
                } else {
                    Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.SEVERE, "Method failed: {0}", response.getString("message"));
                    return false;
                }

            } else {
                Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.SEVERE, "Method failed: {0}", responseEntity.getStatusCode());
                return false;
            }
        }
        return true;
    }

    public boolean authorizationRequest(String paaswordControllerURL, String jsonMessage, boolean isDebugEnabled) {

        logger.info("Authorization request is invoked()..");

        String jsonResponse = "";
        if (!isAlive(paaswordControllerURL)) {
            Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).info("Could not proceed, PaaSword Controller instance is down...");
        } else {

            HttpEntity entity = new HttpEntity(jsonMessage.toString(), null);

            ResponseEntity<String> responseEntity = restTemplate.exchange(paaswordControllerURL + "/api/v1/semanticauthorizationengine/authorizationrequest", HttpMethod.POST, entity, String.class);

            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                JSONObject response = new JSONObject(new String(responseEntity.getBody()));

                if (response.getString("code").equalsIgnoreCase("SUCCESS")) {

                    logger.info("Response: " + response);

//                    Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.INFO, "Method response: {0}", response.getString("message"));

                    String advice = new JSONObject(response.getString("message")).getJSONObject("returnobject").getString("advice");

                    logger.info("Advice: " + advice);

                    if (advice.equalsIgnoreCase("DENY")) {

                        return false;

                    }
                } else {
                    Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.SEVERE, "Method failed: {0}", response.getString("message"));
                    return false;
                }

            } else {
                Logger.getLogger(SemanticAuthorizationEngineHandler.class.getName()).log(Level.SEVERE, "Method failed: {0}", responseEntity.getStatusCode());
                return false;
            }

        }
        return true;
    }
}
