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

import eu.paasword.api.repository.IApplicationInstanceHandlerService;
import eu.paasword.api.repository.exception.applicationInstanceHandler.ApplicationInstanceHandlerDoesNotExist;
import eu.paasword.repository.relational.dao.ApplicationInstanceHandlerRepository;
import eu.paasword.repository.relational.domain.ApplicationInstance;
import eu.paasword.repository.relational.domain.ApplicationInstanceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Component
public class ApplicationInstanceHandlerServiceImpl implements IApplicationInstanceHandlerService<ApplicationInstanceHandler, ApplicationInstance> {

    @Autowired
    ApplicationInstanceHandlerRepository applicationInstanceHandlerRepository;

    private static final Logger logger = Logger.getLogger(ApplicationInstanceHandlerServiceImpl.class.getName());

    @Override
    public void create(ApplicationInstanceHandler applicationInstanceActivity) {

        applicationInstanceHandlerRepository.save(applicationInstanceActivity);
    }

    @Override
    public void delete(long id) throws ApplicationInstanceHandlerDoesNotExist {
        try {

            applicationInstanceHandlerRepository.delete(id);

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(ApplicationInstanceHandlerServiceImpl.class.getName()).severe(ex.getMessage());
            throw new ApplicationInstanceHandlerDoesNotExist(id);
        }
    }

    @Override
    public Optional<ApplicationInstanceHandler> findOne(long id) throws ApplicationInstanceHandlerDoesNotExist {
        Optional<ApplicationInstanceHandler> applicationInstanceHandler = Optional.ofNullable(applicationInstanceHandlerRepository.findOne(id));

        if (applicationInstanceHandler.isPresent()) {
            return applicationInstanceHandler;
        }

        throw new ApplicationInstanceHandlerDoesNotExist(id);
    }

    @Override
    public List<ApplicationInstanceHandler> findAll() {

        return applicationInstanceHandlerRepository.findAll();
    }

    @Override
    public List<ApplicationInstanceHandler> findByApplicationInstanceID(long id) {
        return applicationInstanceHandlerRepository.findByApplicationInstanceID(id);
    }

    @Override
    public void edit(ApplicationInstanceHandler applicationInstanceHandler) throws ApplicationInstanceHandlerDoesNotExist {

        ApplicationInstanceHandler currentApplicationInstanceHandler = applicationInstanceHandlerRepository.findOne(applicationInstanceHandler.getId());

        if (null == currentApplicationInstanceHandler) {
            throw new ApplicationInstanceHandlerDoesNotExist(applicationInstanceHandler.getId());
        }

        applicationInstanceHandlerRepository.save(applicationInstanceHandler);

    }

}
