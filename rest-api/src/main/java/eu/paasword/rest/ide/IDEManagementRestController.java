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
package eu.paasword.rest.ide;

import eu.paasword.annotation.interpreter.PaaSwordInterpreter;
import eu.paasword.annotation.interpreter.entity.Introspect;
import eu.paasword.annotation.interpreter.util.Util;
import eu.paasword.api.repository.IAPIKeyService;
import eu.paasword.api.repository.IApplicationService;
import eu.paasword.api.repository.IClazzService;
import eu.paasword.api.repository.IInstanceService;
import eu.paasword.api.repository.exception.apikey.APIKeyUniqueIDDoesNotExist;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.api.repository.exception.clazz.ClassNameDoesNotExist;
import eu.paasword.api.repository.exception.instance.InstanceAlreadyExistsException;
import eu.paasword.repository.relational.dao.ApplicationBinaryRepository;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.util.entities.AnnotatedCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contains all the rest endpoints regarding query handling of IDE Plugin
 *
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/ide")
public class IDEManagementRestController {

    private static final Logger logger = Logger.getLogger(IDEManagementRestController.class.getName());

    @Autowired
    IApplicationService<Application> applicationService;

    @Autowired
    IAPIKeyService<APIKey> apiKeyService;

    @Autowired
    IClazzService<Clazz> classService;

    @Autowired
    ApplicationBinaryRepository applicationBinaryRepository;

    @Autowired
    IInstanceService<Instance, Clazz> instanceService;

    /**
     * Hello from IDE Plugin Management
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse hello() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, "Hello from IDE Plugin API!", Optional.empty());
    }



    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public PaaSwordRestResponse validateAPIKey(@RequestHeader(value = "Authorization", required = true, defaultValue = "") String apiKey) {

        try {

            logger.info("RECEIVED API KEY: "+ apiKey);

            APIKey apiKeyObj = (APIKey) apiKeyService.findByUniqueID(apiKey).get();

            if (null != apiKeyObj) {

                logger.info("API Key found!");

                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.API_KEY_VALIDATED, Optional.empty());

            }

        } catch (APIKeyUniqueIDDoesNotExist  e) {
            Logger.getLogger(IDEManagementRestController.class.getName()).severe(e.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.GENERAL_ERROR, Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.GENERAL_ERROR, Optional.empty());

    }//EoM

    /**
     * The exposed endpoint is used via the UI, which attempts to upload a JAR File
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = "multipart/form-data")
    public PaaSwordRestResponse uploadApplication(@RequestHeader(value = "Authorization", required = true, defaultValue = "") String apiKey, @RequestParam("componentJar") MultipartFile uploadedComponent) {

        Application application = null;

        try {

            if (uploadedComponent.isEmpty()) {
                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.FILE_IS_EMPTY, Optional.empty());
            }

            logger.info("RECEIVED API KEY: "+ apiKey);

            APIKey apiKeyObj = (APIKey) apiKeyService.findByUniqueID(apiKey).get();

            if (null != apiKeyObj) {

                application = apiKeyObj.getApplicationID();

                if (null != application) {

                    String originalFilename = uploadedComponent.getOriginalFilename();

                    if (Util.isJavaCode(originalFilename)) {

//                        application.setBinary(uploadedComponent.getBytes());

                        ApplicationBinary applicationBinary = applicationBinaryRepository.findByApplicationID(application);

                        if (null != applicationBinary) {
                            applicationBinary.setApplicationID(application);
                            applicationBinary.setBinary(uploadedComponent.getBytes());

                            applicationBinaryRepository.save(applicationBinary);
                        } else {

                            applicationBinary = new ApplicationBinary();
                            applicationBinary.setApplicationID(application);
                            applicationBinary.setBinary(uploadedComponent.getBytes());

                            applicationBinaryRepository.save(applicationBinary);
                        }

                        application.setFileName(originalFilename);

                        // Introspect and validate binary file
                        Introspect introspect = PaaSwordInterpreter.introspectBinaryApplication(uploadedComponent.getBytes(), uploadedComponent.getOriginalFilename(), apiKey, application.getRootPackage());
                        if (null != introspect && !introspect.getAnnotatedCode().isEmpty()) {

                            if (introspect.isHasPEP()) {
                                application.setPep(true);
                                application.setAnnotatedCodePEP(introspect.getAnnotatedCode());

                                List<AnnotatedCode> annotatedCode = new ArrayList<>();
                                List<String> objects = new ArrayList<>();

                                annotatedCode = eu.paasword.util.Util.parseAnnotatedSourceCodeJSONOnlyForPEPs(introspect.getAnnotatedCode());

                                annotatedCode.stream().filter(code -> null != code)
                                        .collect(Collectors.toList()).forEach(annotCode -> {

                                    if (null != annotCode.getMethods() && !annotCode.getMethods().isEmpty()) {
                                        annotCode.getMethods().stream().forEach(method -> {
                                            objects.add(annotCode.getName() + "." + method.getName());
                                        });
                                    } else {
                                        objects.add(annotCode.getName());
                                    }

                                });

                                // Add Instances
                                try {
                                    Clazz objClazz = (Clazz) classService.findByClassName("Object").get();

                                    objects.stream().forEach(object -> {

                                        try {

                                            Instance tempInstance = new Instance();
                                            tempInstance.setClassID(objClazz);
                                            tempInstance.setInstanceName(object);
                                            tempInstance.setNamespaceID(null);
                                            tempInstance.setPropertyInstances(null);

                                            instanceService.create(tempInstance);
                                        } catch (InstanceAlreadyExistsException e) {
                                            logger.info("Instance exists...");
                                        }

                                    });

                                } catch (ClassNameDoesNotExist e) {
                                    e.printStackTrace();
                                }

                            }

                            if (introspect.isHasDataModel()) {
                                application.setDataModel(true);
                                application.setAnnotatedCodeDataModel(introspect.getAnnotatedCode());
                            }

                        }

                        applicationService.edit(application);

                        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_UPLOADED, Optional.empty());

                    }
                }

            }

        } catch (ApplicationDoesNotExist | APIKeyUniqueIDDoesNotExist | IOException e) {
            Logger.getLogger(IDEManagementRestController.class.getName()).severe(e.getMessage());

        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.GENERAL_ERROR, Optional.empty());

    }//EoM
    
    /**
     * Inner class containing all the static messages which will be used in an
     * PaaSwordRestResponse.
     *
     */
    private final static class Message {
        final static String GENERAL_ERROR = "General Error";
        final static String FILE_IS_EMPTY = "File is empty";
        final static String APPLICATION_UPLOADED = "Application uploaded successfully";
        final static String API_KEY_VALIDATED = "API Key validated successfully";
        final static String AUTHORIZATION_FAILED = "Authorization failed";
    }//Message

}
