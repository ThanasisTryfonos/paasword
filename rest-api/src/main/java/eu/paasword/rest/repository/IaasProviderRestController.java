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

import eu.paasword.api.repository.*;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.repository.transferobject.TIaaSProvider;
import eu.paasword.rest.repository.transferobject.TIaaSProviderImage;
import eu.paasword.rest.repository.transferobject.TIaaSProviderInstance;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.util.security.auth.UserAuthentication;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/iaas")
public class IaasProviderRestController {

    private static final Logger logger = Logger.getLogger(IaasProviderRestController.class.getName());

    @Autowired
    IIaaSProviderTypeService<IaaSProviderType> iaasProviderTypeService;

    @Autowired
    IIaaSProviderService<IaaSProvider, User> iaasProviderService;

    @Autowired
    IUserService<User> userservice;

    @Autowired
    IIaaSProviderImageService<IaaSProviderImage> iaasProviderImageService;

    @Autowired
    IIaaSProviderInstanceService<IaaSProviderInstance> iaasProviderInstanceService;

    /**
     * Fetch all Registered IaaS Providers
     *
     * @return PaaSwordRestResponse It returns a list of IaaS providers that are
     * associated with a specific user
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getIaaSProviders() {
        try {
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, iaasProviderService.findIaaSProvidersByUsername(username));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

    }//EoM

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new
     * IaaS Registration to the database.
     *
     * @param tIaasProvider A JSON object which will be casted to a
     * TNewIaaSRegistration (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TIaaSProvider tIaasProvider) {
        try {

            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            IaaSProviderType iaaSProviderType = (IaaSProviderType) iaasProviderTypeService.findOne(tIaasProvider.getIaasProviderTypeID()).get();

//            // Validate OpenStack Connection
//            Class cls = Class.forName(iaaSProviderType.getAdapterImplementation());
//            Object obj = cls.newInstance();
//
//            //String connectionURL
//            Class[] paramClasses = new Class[5];
//            paramClasses[0] = String.class;
//
//            //String username
//            paramClasses[1] = String.class;
//
//            //String password
//            paramClasses[2] = String.class;
//
//            //String tenantName
//            paramClasses[3] = String.class;
//
//            //String projectName
//            paramClasses[4] = String.class;
//
//            Method method = cls.getDeclaredMethod("testConnectionV3", paramClasses);
            boolean isSuccess = true; //(boolean) method.invoke(obj, new String[] {tIaasProvider.getConnectionURL(), tIaasProvider.getUsername(), tIaasProvider.getPassword(), tIaasProvider.getTenantName(), tIaasProvider.getProject()});

            logger.info("Test Connection: " + isSuccess);

//            boolean isSuccess = OpenStackAdapter.testConnection(tIaasProvider.getConnectionURL(), tIaasProvider.getUsername(), tIaasProvider.getPassword(), tIaasProvider.getTenantName());

            if (isSuccess) {

                IaaSProvider newIaaSProvider = new IaaSProvider();

                newIaaSProvider.setIaasProviderTypeID(iaaSProviderType);
                newIaaSProvider.setUsername(tIaasProvider.getUsername());
                newIaaSProvider.setFriendlyName(tIaasProvider.getFriendlyName());
                newIaaSProvider.setConnectionURL(tIaasProvider.getConnectionURL());
                newIaaSProvider.setPassword(tIaasProvider.getPassword());
                newIaaSProvider.setTenantName(tIaasProvider.getTenantName());
                newIaaSProvider.setProject(tIaasProvider.getProject());
                newIaaSProvider.setDateCreated(new Date());
                newIaaSProvider.setUserID(userservice.findByUsername(username).get());

                iaasProviderService.create(newIaaSProvider);
            } else {
                return new PaaSwordRestResponse(BasicResponseCode.INVALID, Message.NOT_AUTHENTICATED, Optional.empty());
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to edit an IaaS
     * registration in the database.
     *
     * @param tIaasProvider A JSON object which will be casted to a IaasProvider
     * (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse update(@RequestBody TIaaSProvider tIaasProvider) {
        try {
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            IaaSProvider existingIaaSprovider = (IaaSProvider) iaasProviderService.findOne(tIaasProvider.getId()).get();

            existingIaaSprovider.setIaasProviderTypeID((IaaSProviderType) iaasProviderTypeService.findOne(tIaasProvider.getIaasProviderTypeID()).get());
            existingIaaSprovider.setUsername(tIaasProvider.getUsername());
            existingIaaSprovider.setFriendlyName(tIaasProvider.getFriendlyName());
            existingIaaSprovider.setConnectionURL(tIaasProvider.getConnectionURL());
            existingIaaSprovider.setPassword(tIaasProvider.getPassword());
            existingIaaSprovider.setTenantName(tIaasProvider.getTenantName());
            existingIaaSprovider.setProject(tIaasProvider.getProject());
            existingIaaSprovider.setUserID(userservice.findByUsername(username).get());

            iaasProviderService.edit(existingIaaSprovider);

        } catch (Exception ex) {    //TODO Expand Exceptions

            logger.log(Level.SEVERE, ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.UPDATED, Optional.empty());
    }

    /**
     * Deletes an IaaS Provider from database.
     *
     * @param id The id of the IaaS Provider to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            //get authorization realms
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            //delete from service
            iaasProviderService.delete(id);

        } catch (Exception ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.DELETED, Optional.empty());
    }

    /**
     * Adds an image to an IaaS Provider to database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/image", method = RequestMethod.POST)
    public PaaSwordRestResponse addIaaSProviderImage(@RequestBody TIaaSProviderImage tIaaSProviderImage) {
        try {
            //get authorization realms
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();


            if (null != tIaaSProviderImage) {

                IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(tIaaSProviderImage.getIaasProviderID()).get();

                if (null != iaasProvider && iaasProvider.getUserID().getUsername().equalsIgnoreCase(username)) {

                    IaaSProviderImage image = new IaaSProviderImage();

                    image.setDateCreated(new Date());
                    image.setFriendlyName(tIaaSProviderImage.getFriendlyName());
                    image.setImageID(tIaaSProviderImage.getImageID());
                    image.setIaasProviderID(iaasProvider);
                    image.setUserID(iaasProvider.getUserID());

                    iaasProviderImageService.create(image);

                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.IMAGE_CREATED, Optional.empty());

                } else {
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.WRONG_USER, Optional.empty());
                }

            }


        } catch (Exception ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Error Occurred", Optional.empty());
    }

    /**
     * Adds an image to an IaaS Provider to database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/image", method = RequestMethod.PUT)
    public PaaSwordRestResponse editIaaSProviderImage(@RequestBody TIaaSProviderImage tIaaSProviderImage) {
        try {
            //get authorization realms
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();


            if (null != tIaaSProviderImage) {

                IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(tIaaSProviderImage.getIaasProviderID()).get();

                if (null != iaasProvider && iaasProvider.getUserID().getUsername().equalsIgnoreCase(username)) {

                    IaaSProviderImage image = (IaaSProviderImage) iaasProviderImageService.findOne(tIaaSProviderImage.getId()).get();

                    image.setFriendlyName(tIaaSProviderImage.getFriendlyName());
                    image.setImageID(tIaaSProviderImage.getImageID());

                    iaasProviderImageService.edit(image);

                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.IMAGE_UPDATED, Optional.empty());

                } else {
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.WRONG_USER, Optional.empty());
                }

            }


        } catch (Exception ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Error Occurred", Optional.empty());
    }

    /**
     * Deletes an IaaS Provider from database.
     *
     * @param id The id of the IaaS Provider to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/image/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse deleteIaaSProviderImage(@PathVariable("id") long id) {
        try {
            //get authorization realms
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            //delete from service
            IaaSProviderImage iaaSProviderImage = (IaaSProviderImage) iaasProviderImageService.findOne(id).get();

            if (null != iaaSProviderImage && iaaSProviderImage.getUserID().getUsername().equalsIgnoreCase(username)) {

                //delete from service
                iaasProviderImageService.delete(id);

                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.IMAGE_DELETED, Optional.empty());
            }


        } catch (Exception ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Error Occurred", Optional.empty());
    }

    /**
     * Adds an instance to an IaaS Provider to database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/instance", method = RequestMethod.POST)
    public PaaSwordRestResponse addIaaSProviderInstance(@RequestBody TIaaSProviderInstance tIaaSProviderInstance) {
        try {
            //get authorization realms
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            if (null != tIaaSProviderInstance) {

                IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(tIaaSProviderInstance.getIaasProviderID()).get();

                if (null != iaasProvider && iaasProvider.getUserID().getUsername().equalsIgnoreCase(username)) {

                    IaaSProviderInstance instance = new IaaSProviderInstance();

                    instance.setDateCreated(new Date());
                    instance.setUserID(iaasProvider.getUserID());
                    instance.setIaasProviderID(iaasProvider);
                    instance.setFriendlyName(tIaaSProviderInstance.getFriendlyName());
                    instance.setFlavorID(tIaaSProviderInstance.getFlavorID());
                    instance.setIaasProviderImageID(iaasProviderImageService.findOne(tIaaSProviderInstance.getImageID()).get());
                    instance.setNetwork("[\"b0bf3ba2-560d-4fed-a674-e869c9102754\",\"4a00bb11-4ad2-4649-b348-3fe9642f4468\"]");

                    iaasProviderInstanceService.create(instance);

                    // Create new Instance to IaaS Provider

                    // Validate OpenStack Connection
                    Class cls = Class.forName(iaasProvider.getIaasProviderTypeID().getAdapterImplementation());
                    Object obj = cls.newInstance();

                    //String connectionURL
                    Class[] paramClasses = new Class[5];
                    paramClasses[0] = String.class;

                    //String username
                    paramClasses[1] = String.class;

                    //String password
                    paramClasses[2] = String.class;

                    //String tenantName
                    paramClasses[3] = String.class;

                    //String projectName
                    paramClasses[4] = String.class;

                    Method method = cls.getDeclaredMethod("testConnectionV3", paramClasses);
                    boolean isSuccess = (boolean) method.invoke(obj, new String[] {iaasProvider.getConnectionURL(), iaasProvider.getUsername(), iaasProvider.getPassword(), iaasProvider.getTenantName(), iaasProvider.getProject()});


                    logger.info("Test Connection: " + isSuccess);

                    if (isSuccess) {

                        // Create Server

                        paramClasses = new Class[11];
                        // Friendly
                        paramClasses[0] = String.class;

                        // String flavorID
                        paramClasses[1] = String.class;

                        // String imageID
                        paramClasses[2] = String.class;

                        // String connectionURL
                        paramClasses[3] = String.class;

                        // String username
                        paramClasses[4] = String.class;

                        // String password
                        paramClasses[5] = String.class;

                        // String tenantName
                        paramClasses[6] = String.class;

                        // String dbName
                        paramClasses[7] = String.class;

                        // String dbUsername
                        paramClasses[8] = String.class;

                        // String dbPassword
                        paramClasses[9] = String.class;

                        // String keyPairName
                        paramClasses[10] = String.class;

                        method = cls.getDeclaredMethod("createNewInstance", paramClasses);
                        isSuccess = (boolean) method.invoke(obj, new String[] {instance.getFriendlyName().trim(), String.valueOf(instance.getFlavorID()), instance.getIaasProviderImageID().getImageID(), iaasProvider.getConnectionURL(), iaasProvider.getUsername(), iaasProvider.getPassword(), iaasProvider.getTenantName(),
                                instance.getDbName(), instance.getDbUser(), instance.getDbPassword(), instance.getKeyPair()});

                        logger.info("Instance status: " + isSuccess);

                        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.INSTANCE_CREATED, Optional.empty());

                    } else {

                        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.TEST_FAILED, Optional.empty());

                    }

                } else {
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.WRONG_USER, Optional.empty());
                }

            }


        } catch (Exception ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Error Occurred", Optional.empty());
    }

    /**
     * Edits an instance to an IaaS Provider to database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/instance", method = RequestMethod.PUT)
    public PaaSwordRestResponse editIaaSProviderInstance(@RequestBody TIaaSProviderInstance tIaaSProviderInstance) {
        try {
            //get authorization realms
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();


            if (null != tIaaSProviderInstance) {

                IaaSProvider iaasProvider = (IaaSProvider) iaasProviderService.findOne(tIaaSProviderInstance.getIaasProviderID()).get();

                if (null != iaasProvider && iaasProvider.getUserID().getUsername().equalsIgnoreCase(username)) {

                    IaaSProviderInstance instance = (IaaSProviderInstance) iaasProviderInstanceService.findOne(tIaaSProviderInstance.getId()).get();

                    // Delete Instance from IaaS Provider first
                    // TODO

                    instance.setFriendlyName(tIaaSProviderInstance.getFriendlyName());
                    instance.setFlavorID(tIaaSProviderInstance.getFlavorID());
                    instance.setIaasProviderImageID(iaasProviderImageService.findOne(tIaaSProviderInstance.getImageID()).get());

                    iaasProviderInstanceService.edit(instance);

                    // Create new Instance to IaaS Provider
                    // TODO

                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.INSTANCE_UPDATED, Optional.empty());

                } else {
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.WRONG_USER, Optional.empty());
                }

            }


        } catch (Exception ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Error Occured", Optional.empty());
    }

    /**
     * Deletes an instance of an IaaS Provider from database.
     *
     * @param id The id of the instance of IaaS Provider to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/instance/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse deleteIaaSProviderInstance(@PathVariable("id") long id) {
        try {
            //get authorization realms
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            //delete from service
            IaaSProviderInstance iaaSProviderInstance = (IaaSProviderInstance) iaasProviderInstanceService.findOne(id).get();

            if (null != iaaSProviderInstance && iaaSProviderInstance.getUserID().getUsername().equalsIgnoreCase(username)) {

                //delete from service
                iaasProviderInstanceService.delete(id);

                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.INSTANCE_DELETED, Optional.empty());
            }


        } catch (Exception ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Error Occured", Optional.empty());
    }

    /**
     * Tests the connection of an IaaS Provider
     *
     * @param id The id of the IaaS Provider to be tested
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public PaaSwordRestResponse testConnection(@PathVariable("id") long id) {
        try {
            //get authorization realms
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            //delete from service
            IaaSProvider iaaSProvider = (IaaSProvider) iaasProviderService.findOne(id).get();

            if (null != iaaSProvider && iaaSProvider.getUserID().getUsername().equalsIgnoreCase(username)) {

                // Validate OpenStack Connection
                Class cls = Class.forName(iaaSProvider.getIaasProviderTypeID().getAdapterImplementation());
                Object obj = cls.newInstance();

                //String connectionURL
                Class[] paramClasses = new Class[5];
                paramClasses[0] = String.class;

                //String username
                paramClasses[1] = String.class;

                //String password
                paramClasses[2] = String.class;

                //String tenantName
                paramClasses[3] = String.class;

                //String projectName
                paramClasses[4] = String.class;

                Method method = cls.getDeclaredMethod("testConnectionV3", paramClasses);
                boolean isSuccess = (boolean) method.invoke(obj, new String[] {iaaSProvider.getConnectionURL(), iaaSProvider.getUsername(), iaaSProvider.getPassword(), iaaSProvider.getTenantName(), iaaSProvider.getProject()});

                logger.info("Test Connection: " + isSuccess);

                if (isSuccess) {
                    return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.TEST_OK, Optional.empty());
                } else {
                    return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.TEST_FAILED, Optional.empty());
                }

            }


        } catch (Exception ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Error Occurred", Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an
     * PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String NOT_AUTHENTICATED = "IaaS cannot be authenticated";
        final static String CREATED = "IaaS has been registered";
        final static String IMAGE_CREATED = "IaaS Image has been created";
        final static String IMAGE_UPDATED = "IaaS Image has been updated";
        final static String IMAGE_DELETED = "IaaS Image has been deleted";
        final static String INSTANCE_CREATED = "IaaS Instance has been created";
        final static String INSTANCE_UPDATED = "IaaS Instance has been updated";
        final static String INSTANCE_DELETED = "IaaS Instance has been deleted";
        final static String WRONG_USER = "Permission denied";
        final static String UPDATED = "IaaS has been updated";
        final static String DELETED = "IaaS has been deleted";
        final static String TEST_OK = "Connection tested successfully";
        final static String TEST_FAILED = "Connection test failed";
    }

}//EoC
