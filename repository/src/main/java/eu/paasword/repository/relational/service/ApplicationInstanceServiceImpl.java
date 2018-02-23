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
package eu.paasword.repository.relational.service;

import eu.paasword.api.repository.IApplicationInstanceService;
import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceDoesNotExist;
import eu.paasword.api.repository.exception.applicationInstance.ApplicationInstanceNameDoesNotExist;
import eu.paasword.repository.relational.dao.ApplicationInstanceRepository;
import eu.paasword.repository.relational.domain.ApplicationInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Component
public class ApplicationInstanceServiceImpl implements IApplicationInstanceService<ApplicationInstance> {

    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;

    private static final Logger logger = Logger.getLogger(ApplicationInstanceServiceImpl.class.getName());

    @Override
    public void create(ApplicationInstance applicationInstance) throws ApplicationInstanceAlreadyExistsException {

        //Check if name already exists
        if (null != applicationInstanceRepository.findByName(applicationInstance.getName())) {
            throw new ApplicationInstanceAlreadyExistsException(applicationInstance.getName());
        }

        //Store application privacy constraint to database
        applicationInstanceRepository.save(applicationInstance);
    }

    @Override
    public void delete(long id) throws ApplicationInstanceDoesNotExist {
        try {
            applicationInstanceRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(ApplicationInstanceServiceImpl.class.getName()).severe(ex.getMessage());
            throw new ApplicationInstanceDoesNotExist(id);
        }
    }

    @Override
    public Optional<ApplicationInstance> findByName(String name) throws ApplicationInstanceNameDoesNotExist {
        Optional<ApplicationInstance> applicationInstance = Optional.ofNullable(applicationInstanceRepository.findByName(name));

        if (applicationInstance.isPresent()) {
            return applicationInstance;
        }

        throw new ApplicationInstanceNameDoesNotExist(name);
    }

    @Override
    public Optional<ApplicationInstance> findOne(long id) throws ApplicationInstanceDoesNotExist {
        Optional<ApplicationInstance> applicationInstance = Optional.ofNullable(applicationInstanceRepository.findOne(id));

        if (applicationInstance.isPresent()) {
            return applicationInstance;
        }

        throw new ApplicationInstanceDoesNotExist(id);
    }

    @Override
    public List<ApplicationInstance> findAll() {
        return applicationInstanceRepository.findAll();
    }

    @Override
    public void edit(ApplicationInstance applicationInstance) throws ApplicationInstanceDoesNotExist {

        ApplicationInstance currentApplicationInstance = applicationInstanceRepository.findOne(applicationInstance.getId());

        //Check if current exists
        if (null == currentApplicationInstance) {
            throw new ApplicationInstanceDoesNotExist(applicationInstance.getId());
        }

//        currentApplicationInstance.setName(applicationInstance.getName());

        //Store application privacy constraint to database
        applicationInstanceRepository.save(applicationInstance);

    }

    @Override
    public ApplicationInstance findByValidator(String validator) {
        return applicationInstanceRepository.findByValidator(validator);
    }

    public ApplicationInstance findByApplicationID(long applicationID) {
        return applicationInstanceRepository.findByApplicationID(applicationID);
    }

    public ApplicationInstance findByApplicationIDWithPaaS(long applicationID) {
        return applicationInstanceRepository.findByApplicationIDWithPaaS(applicationID);
    }

    public ApplicationInstance findOneWithoutApplication(long applicationInstanceID) {
        return applicationInstanceRepository.findOneWithoutApplication(applicationInstanceID);
    }

    public ApplicationInstance findOneWithPaaSProviderWithoutApplication(long applicationInstanceID) {
        return applicationInstanceRepository.findOneWithPaaSProviderWithoutApplication(applicationInstanceID);
    }

//    public ApplicationInstance findOneOnlyWithApplicationID(long applicationInstanceID) {
//        return applicationInstanceRepository.findOneOnlyWithApplicationID(applicationInstanceID);
//    }
//
//    public ApplicationInstance findOneWithPaaSProviderAndOnlyWithApplicationID(long applicationInstanceID) {
//        return applicationInstanceRepository.findOneWithPaaSProviderAndOnlyWithApplicationID(applicationInstanceID);
//    }

    public ApplicationInstance findByUniqueID(String uniqueID) {
        return applicationInstanceRepository.findByUniqueID(uniqueID);
    }
}
