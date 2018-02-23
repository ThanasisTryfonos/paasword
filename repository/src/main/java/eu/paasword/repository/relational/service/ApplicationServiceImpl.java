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

import eu.paasword.api.repository.IApplicationService;
import eu.paasword.api.repository.exception.application.ApplicationAlreadyExistsException;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.api.repository.exception.application.ApplicationNameDoesNotExist;
import eu.paasword.repository.relational.dao.ApplicationRepository;
import eu.paasword.repository.relational.domain.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author smantzouratos
 */
@Component
public class ApplicationServiceImpl implements IApplicationService<Application> {

    @Autowired
    private ApplicationRepository applicationRepository;

    private static final Logger logger = Logger.getLogger(ApplicationServiceImpl.class.getName());

    @Override
    public void create(Application application) throws ApplicationAlreadyExistsException {

        //Check if name already exists
        if (null != applicationRepository.findByName(application.getName())) {
            throw new ApplicationAlreadyExistsException(application.getName());
        }

        //Store application to database
        applicationRepository.save(application);
    }

    @Override
    public void delete(long id) throws ApplicationDoesNotExist {
        try {
            applicationRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(ApplicationServiceImpl.class.getName()).severe(ex.getMessage());
            throw new ApplicationDoesNotExist(id);
        }
    }

    @Override
    public Optional<Application> findByName(String name) throws ApplicationNameDoesNotExist {
        Optional<Application> application = Optional.ofNullable(applicationRepository.findByName(name));

        if (application.isPresent()) {
            return application;
        }

        throw new ApplicationNameDoesNotExist(name);
    }

    @Override
    public Optional<Application> findOne(long id) throws ApplicationDoesNotExist {
        Optional<Application> application = Optional.ofNullable(applicationRepository.findOne(id));

        if (application.isPresent()) {
            return application;
        }

        throw new ApplicationDoesNotExist(id);
    }

    @Override
    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    @Override
    public void edit(Application application) throws ApplicationDoesNotExist {

        Application currentApplication = applicationRepository.findOne(application.getId());

        //Check if current clazz exists
        if (null == currentApplication) {
            throw new ApplicationDoesNotExist(application.getId());
        }

        //Store application to database
        applicationRepository.save(application);

    }

    @Override
    public Page<Application> findAllWithoutBlob(Pageable pageable) {

        return applicationRepository.findAllByOrderByNameAsc(pageable);
    }

    public Optional<Application> findOneWithoutBlob(long id) throws ApplicationDoesNotExist {

        Optional<Application> application = Optional.ofNullable(applicationRepository.findOneWithoutBlob(id));

        if (application.isPresent()) {
            return application;
        }

        throw new ApplicationDoesNotExist(id);

    }
}
