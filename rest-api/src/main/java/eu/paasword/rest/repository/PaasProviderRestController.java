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

import eu.paasword.api.repository.IPaaSProviderService;
import eu.paasword.api.repository.IPaaSProviderTypeService;
import eu.paasword.api.repository.IUserService;
import eu.paasword.api.repository.exception.paasProvider.PaaSProviderAlreadyExistsException;
import eu.paasword.api.repository.exception.paasProvider.PaaSProviderDoesNotExist;
import eu.paasword.api.repository.exception.paasProviderType.PaaSProviderTypeDoesNotExist;
import eu.paasword.repository.relational.domain.PaaSProvider;
import eu.paasword.repository.relational.domain.PaaSProviderType;
import eu.paasword.repository.relational.domain.ProxyCloudProvider;
import eu.paasword.repository.relational.domain.User;
import eu.paasword.rest.repository.transferobject.TPaaSProvider;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.spi.adapter.PaaSAdapter;
import eu.paasword.spi.adapter.ProxyAdapter;
import eu.paasword.spi.model.CredentialsModel;
import eu.paasword.spi.model.PaaSOfferingModel;
import eu.paasword.spi.response.SPIResponse;
import eu.paasword.util.security.auth.UserAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/paas")
public class PaasProviderRestController {

    private static final Logger logger = Logger.getLogger(PaasProviderRestController.class.getName());

    @Autowired
    IPaaSProviderTypeService<PaaSProviderType> paasProviderTypeService;

    @Autowired
    IPaaSProviderService<PaaSProvider, User> paasProviderService;

    @Autowired
    IUserService<User> userService;

    @Resource(name = "paasAdaptersList")
    List paasAdapters;

    /**
     * Fetch all Registered IaaS Providers
     *
     * @return PaaSwordRestResponse It returns a list of IaaS providers that are
     * associated with a specific user
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getPaaSProviders() {
        try {
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, paasProviderService.findPaaSProvidersByUsername(username));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }

    }//EoM

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new
     * PaaS Provider to the database.
     *
     * @param tPaasProvider A JSON object which will be casted to a TPaaSProvider (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TPaaSProvider tPaasProvider) {
        try {
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            PaaSProviderType paasProviderType = (PaaSProviderType) paasProviderTypeService.findOne(tPaasProvider.getPaasProviderTypeID()).get();

            PaaSAdapter paasAdapter = (PaaSAdapter) ((List) paasAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(paasProviderType.getAdapterImplementation())).collect(Collectors.toList())).get(0);

            CredentialsModel credentialsModel = new CredentialsModel();

            PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
            paaSOfferingModelSPI.setEndpointURI(tPaasProvider.getConnectionURL());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            credentialsModel.setUsername(tPaasProvider.getUsername());
            credentialsModel.setPassword(tPaasProvider.getPassword());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            SPIResponse spiResponse = paasAdapter.validateCredentials(credentialsModel, paaSOfferingModelSPI);

            if (null != spiResponse && spiResponse.getCode().equals(eu.paasword.spi.response.BasicResponseCode.SUCCESS)) {

                PaaSProvider paasProvider = new PaaSProvider();
                paasProvider.setFriendlyName(tPaasProvider.getFriendlyName());
                paasProvider.setDateCreated(new Date());
                paasProvider.setUserID((User) userService.findByUsername(username).get());
                paasProvider.setPaasProviderTypeID(paasProviderType);
                paasProvider.setUsername(tPaasProvider.getUsername());
                paasProvider.setPassword(tPaasProvider.getPassword());
                paasProvider.setConnectionURL(tPaasProvider.getConnectionURL());

                paasProviderService.create(paasProvider);

                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.CREATED, Optional.empty());

            } else {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_CREDENTIALS, Optional.empty());
            }

        } catch (UsernameNotFoundException | PaaSProviderAlreadyExistsException  | PaaSProviderTypeDoesNotExist ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to edit an PaaS
     * registration in the database.
     *
     * @param tPaasProvider A JSON object which will be casted to a TPaaSProvider object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse update(@RequestBody TPaaSProvider tPaasProvider) {
        try {
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            PaaSProviderType paasProviderType = (PaaSProviderType) paasProviderTypeService.findOne(tPaasProvider.getPaasProviderTypeID()).get();

            PaaSAdapter paasAdapter = (PaaSAdapter) ((List) paasAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(paasProviderType.getAdapterImplementation())).collect(Collectors.toList())).get(0);

            CredentialsModel credentialsModel = new CredentialsModel();

            PaaSOfferingModel paaSOfferingModelSPI = new PaaSOfferingModel();
            paaSOfferingModelSPI.setEndpointURI(tPaasProvider.getConnectionURL());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            credentialsModel.setUsername(tPaasProvider.getUsername());
            credentialsModel.setPassword(tPaasProvider.getPassword());
            credentialsModel.setPaaSOffering(paaSOfferingModelSPI);

            SPIResponse spiResponse = paasAdapter.validateCredentials(credentialsModel, paaSOfferingModelSPI);

            if (null != spiResponse && spiResponse.getCode().equals(eu.paasword.spi.response.BasicResponseCode.SUCCESS)) {

                PaaSProvider existingPaasProvider = (PaaSProvider) paasProviderService.findOne(tPaasProvider.getId()).get();
                existingPaasProvider.setFriendlyName(tPaasProvider.getFriendlyName());
                existingPaasProvider.setUserID((User) userService.findByUsername(username).get());
                existingPaasProvider.setPaasProviderTypeID(paasProviderType);
                existingPaasProvider.setUsername(tPaasProvider.getUsername());
                existingPaasProvider.setPassword(tPaasProvider.getPassword());
                existingPaasProvider.setConnectionURL(tPaasProvider.getConnectionURL());

                paasProviderService.edit(existingPaasProvider);

                return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.UPDATED, Optional.empty());

            } else {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.INVALID_CREDENTIALS, Optional.empty());
            }


        } catch (UsernameNotFoundException | PaaSProviderDoesNotExist | PaaSProviderTypeDoesNotExist ex) {

            logger.log(Level.SEVERE, ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * Deletes an PaaS Provider from database.
     *
     * @param id The id of the PaaS Provider to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            //get authorization realms
            UserAuthentication userauthentication = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
            String username = userauthentication.getDetails().getUsername();

            //delete from service
            paasProviderService.delete(id);

        } catch (Exception ex) {
            Logger.getLogger(UserManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an
     * PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String CREATED = "PaaS Provider has been registered";
        final static String INVALID_CREDENTIALS = "PaaS Provider credentials cannot be validated";
        final static String UPDATED = "PaaS Provider has been updated";
        final static String DELETED = "PaaS Provider has been deleted";
    }

}//EoC
