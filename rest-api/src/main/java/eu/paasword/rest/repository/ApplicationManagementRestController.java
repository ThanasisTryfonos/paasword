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

import eu.paasword.annotation.interpreter.PaaSwordInterpreter;
import eu.paasword.annotation.interpreter.entity.Introspect;
import eu.paasword.api.repository.IApplicationService;
import eu.paasword.api.repository.IClazzService;
import eu.paasword.api.repository.IInstanceService;
import eu.paasword.api.repository.exception.application.ApplicationAlreadyExistsException;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.api.repository.exception.clazz.ClassNameDoesNotExist;
import eu.paasword.api.repository.exception.instance.InstanceAlreadyExistsException;
import eu.paasword.repository.relational.dao.ApplicationBinaryRepository;
import eu.paasword.repository.relational.domain.Application;
import eu.paasword.repository.relational.domain.ApplicationBinary;
import eu.paasword.repository.relational.domain.Clazz;
import eu.paasword.repository.relational.domain.Instance;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.util.Util;
import eu.paasword.util.entities.AnnotatedCode;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contains all the rest endpoints regarding applications
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/application")
public class ApplicationManagementRestController {

    private static final Logger logger = Logger.getLogger(ApplicationManagementRestController.class.getName());

    @Autowired
    IApplicationService<Application> applicationService;

    @Autowired
    IClazzService<Clazz> classService;

    @Autowired
    IInstanceService<Instance, Clazz> instanceService;

    @Autowired
    ApplicationBinaryRepository applicationBinaryRepository;

    @Autowired
    Environment environment;

    /**
     * Fetch all available applications from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getApplications() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, applicationService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch an application with a specific ID from database.
     *
     * @param id The id of the application to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getApplicationByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, (Application) applicationService.findOne(id).get());
        } catch (ApplicationDoesNotExist ex) {
            Logger.getLogger(ApplicationManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new Application to the database.
     *
     * @param application A JSON object which will be casted to a Application (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody Application application) {

        try {

            application.setDateCreated(new Date());

            applicationService.create(application);

        } catch (ApplicationAlreadyExistsException ex) {

            Logger.getLogger(ApplicationManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing Application to the database.
     *
     * @param application A JSON object which will be casted to a Application (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody Application application) {
        try {
            applicationService.edit(application);
        } catch (ApplicationDoesNotExist ex) {
            Logger.getLogger(ApplicationManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_UPDATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing Application to the database.
     *
     * @param id The id of the application which will be casted to a Application (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/upload/{id}", method = RequestMethod.POST, consumes = "multipart/form-data")
    public PaaSwordRestResponse uploadNewVersion(@PathVariable("id") long id, MultipartHttpServletRequest request) {// @RequestBody MultipartFile file) {

        try {

            Application application = (Application) applicationService.findOne(id).get();

            if (null != application) {

                Iterator<String> itr = request.getFileNames();

                MultipartFile mpf = request.getFile(itr.next());

                String enabled = environment.getProperty("application.binary.storing.enabled");

                if (enabled.equals("true")) {

                    ApplicationBinary applicationBinary = applicationBinaryRepository.findByApplicationID(application);

                    if (null != applicationBinary) {

                        applicationBinary.setApplicationID(application);
                        applicationBinary.setBinary(mpf.getBytes());

                        applicationBinaryRepository.save(applicationBinary);

                    } else {

                        applicationBinary = new ApplicationBinary();
                        applicationBinary.setApplicationID(application);
                        applicationBinary.setBinary(mpf.getBytes());

                        applicationBinaryRepository.save(applicationBinary);

                    }

//                    application.setBinary(mpf.getBytes());
                }

                application.setFileName(mpf.getOriginalFilename());

                // Introspect and Validate Binary File
                Introspect introspect = PaaSwordInterpreter.introspectBinaryApplication(mpf.getBytes(), mpf.getOriginalFilename(), application.getApiKeys().get(0).getUniqueID(), application.getRootPackage());
                if (null != introspect && !introspect.getAnnotatedCode().isEmpty()) {

                    if (introspect.isHasPEP()) {
                        application.setPep(true);
                        application.setAnnotatedCodePEP(introspect.getAnnotatedCode());

                        List<AnnotatedCode> annotatedCode = new ArrayList<>();
                        List<String> objects = new ArrayList<>();

                        annotatedCode = Util.parseAnnotatedSourceCodeJSONOnlyForPEPs(introspect.getAnnotatedCode());

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

                    // TODO

                    // IF application does have PEP and Data Model return error
                    if (!introspect.isHasPEP() && !introspect.isHasDataModel()) {

                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_NOT_FOUND_PEP_ENTITY, Optional.empty());

                    }

                    applicationService.edit(application);

                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_UPLOADED, Optional.empty());

                }

            }

        } catch (ApplicationDoesNotExist | IOException e) {
            Logger.getLogger(ApplicationManagementRestController.class.getName()).severe(e.getMessage());

        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.APPLICATION_INVALID, Optional.empty());

    }

    /**
     * Deletes a Application from database.
     *
     * @param id The id of the Application to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            applicationService.delete(id);
        } catch (ApplicationDoesNotExist ex) {
            Logger.getLogger(ApplicationManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.APPLICATION_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     */
    private final static class Message {

        final static String APPLICATION_DELETED = "Application has been deleted";
        final static String APPLICATION_UPDATED = "Application has been updated";
        final static String APPLICATION_UPLOADED = "Application has been uploaded";
        final static String APPLICATION_INVALID = "Application is invalid";
        final static String APPLICATION_NOT_FOUND_PEP_ENTITY = "There are no PaaSword annotations";
        final static String APPLICATION_CREATED = "Application has been created";
    }

}
