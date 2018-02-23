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
package eu.paasword.paaswordlibrary.service;

import eu.paasword.annotation.interpreter.PaaSwordInterpreter;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.paasword.annotations.PaaSwordPEP;
import eu.paasword.paaswordlibrary.semanticauthorizationengine.SemanticAuthorizationEngineHandler;
import eu.paasword.jpa.PaaSwordEntityHandler;
import eu.paasword.jpa.exceptions.ProxyInitializationException;
import eu.paasword.paaswordlibrary.transferobject.ApplicationValidation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author smantzouratos
 */
@Aspect
public class PaaSwordService {

    private static final Logger logger = Logger.getLogger(PaaSwordService.class.getName());

    private static String APP_KEY;
    private static String paaSwordControllerURL;
    private static PaaSwordEntityHandler paaSwordEntityHandler = null;

    private static PaaSwordService instance = null;

    public PaaSwordService(String paaSwordControllerURL, String appKey) {

        this.APP_KEY = appKey;
        this.paaSwordControllerURL = paaSwordControllerURL;
        initializeService();

    }

    private void initializeService() {

        if (null != paaSwordControllerURL && null != APP_KEY) {

            logger.info("PaaSwordService() is initializing...");

            // Parsing Source Code
            String jsonObj = PaaSwordInterpreter.introspectSourceCodeApplication(APP_KEY, null);

            // Perform Validations
            logger.info("Validating API Key and Source Code via Semantic Authorization Engine...");

            ApplicationValidation applicationValidation = SemanticAuthorizationEngineHandler.INSTANCE.validateApplication(paaSwordControllerURL, jsonObj, true);

            if (null != applicationValidation && applicationValidation.isValidated()) {

                if (applicationValidation.isPaaSwordJPAEnabled()) {
                    try {
                        this.paaSwordEntityHandler = PaaSwordEntityHandler.getInstance(this.paaSwordControllerURL + "/api/v1/query/execute", this.APP_KEY);
                        logger.info("PaaSword JPA initialized successfully!");
                    } catch (ProxyInitializationException e) {
                        throw new RuntimeException("PaaSwordService() initialization failed! PaaSword JPA initialization error!");
                    }
                }

                logger.info("PaaSwordService() initialized successfully!");

            } else {
                logger.severe("PaaSwordService() initialization failed! Semantic Authorization Engine validation error!");
                throw new RuntimeException("PaaSwordService() initialization failed! Semantic Authorization Engine validation error!");
            }

        } else {

            logger.severe("PaaSwordService() initialization failed! PaaSwordService() parameters are missing!");
            throw new RuntimeException("PaaSwordService() initialization failed! PaaSwordService() parameters are missing!");

        }

    }//EoM   initializeService()

    public static PaaSwordService getInstance() {
        if (instance == null) {
            instance = new PaaSwordService(paaSwordControllerURL, APP_KEY);
        }
        return instance;
    }//EoM

    public static PaaSwordService getInstance(String paaSwordControllerURL, String APP_KEY) {
        if (instance == null) {
            instance = new PaaSwordService(paaSwordControllerURL, APP_KEY);
        }
        return instance;
    }//EoM

    @Around("(execution(@eu.paasword.annotations.PaaSwordPEP * *(..)) && @annotation(paaSwordPEP))")
    public Object interceptRequest(ProceedingJoinPoint joinPoint, PaaSwordPEP paaSwordPEP) {

        try {

            // Intercept HTTP Request
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            Enumeration<String> headerNames = request.getHeaderNames();
            JSONObject jsonObj = new JSONObject();


            JSONArray headersJsonArray = new JSONArray();

            while (headerNames.hasMoreElements()) {

                String headerName = headerNames.nextElement();
                Enumeration<String> headers = request.getHeaders(headerName);
                while (headers.hasMoreElements()) {
                    String headerValue = headers.nextElement();
                    JSONObject headerJSON = new JSONObject();
                    headerJSON.put("name", headerName);
                    headerJSON.put("value", headerValue);
                    headersJsonArray.put(headerJSON);
//                    logger.info(headerName + ":" + headerValue);
                }

            }

            // Retrieve class/method information
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String declaringTypeName = signature.getDeclaringTypeName();
            String methodName = signature.getMethod().getName();

            String[] parameterNames = signature.getParameterNames();

            // Construct JSON Message for Semantic Authorization Engine

            jsonObj.put("appKey", APP_KEY);
            jsonObj.put("object", declaringTypeName + "." + methodName);
            jsonObj.put("actor", null != request.getUserPrincipal().getName() ? request.getUserPrincipal().getName() : "Unauthorized");

            String value = paaSwordPEP.value();

//            JSONArray jsonPolicies = new JSONArray();
//
//            if (value.contains(",")) {
//                String policies[] = value.split("\\,");
//
//                for (String policy : policies) {
//                    jsonPolicies.put(policy);
//                }
//
//            } else {
//                jsonPolicies.put(value);
//            }

            jsonObj.put("action", request.getMethod());
            jsonObj.put("PEPIdentifier", value);
            jsonObj.put("PEPType", paaSwordPEP.type().name());
            jsonObj.put("remoteAddress", request.getRemoteAddr());
            jsonObj.put("requestContext", headersJsonArray);

            String jsonMSG = jsonObj.toString();

            logger.info("JSON: " + jsonMSG);

            // Sent to Semantic Authorization Engine
            if (SemanticAuthorizationEngineHandler.INSTANCE.invokeHttpRequestToSemanticAuthorizationEngine(paaSwordControllerURL, jsonMSG, true)) {

                return joinPoint.proceed();

            } else {

                return "error";
            }

        } catch (Throwable ex) {
            Logger.getLogger(PaaSwordService.class.getName()).log(Level.SEVERE, null, ex);
            return "error";
        }

    }

    public Object authorizationRequest(String pepType, String paaSwordPEPIdentifier, String controlledObject, String principal, String action, String requestorIP) {

        try {

            // Construct JSON Message for Semantic Authorization Engine
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("appKey", APP_KEY);
            jsonObj.put("object", controlledObject);
            jsonObj.put("actor", null != principal ? principal : "Unauthorized");
            jsonObj.put("action", action);
            jsonObj.put("PEPIdentifier", paaSwordPEPIdentifier);
            jsonObj.put("PEPType", pepType);
            jsonObj.put("remoteAddress", requestorIP);

            String jsonMSG = jsonObj.toString();

//            logger.info("JSON: " + jsonMSG);

            // Sent to Semantic Authorization Engine
            if (SemanticAuthorizationEngineHandler.INSTANCE.authorizationRequest(paaSwordControllerURL, jsonMSG, true)) {

                return "ALLOW";

            } else {

                return "DENY";
            }

        } catch (Throwable ex) {
            Logger.getLogger(PaaSwordService.class.getName()).log(Level.SEVERE, null, ex);
            return "DENY";
        }

    }

}
