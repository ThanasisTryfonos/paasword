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


import eu.paasword.api.repository.IApplicationPrivacyConstraintService;
import eu.paasword.api.repository.exception.application.ApplicationAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationPrivacyConstraint.ApplicationPrivacyConstraintAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationPrivacyConstraint.ApplicationPrivacyConstraintDoesNotExist;
import eu.paasword.api.repository.exception.applicationPrivacyConstraint.ApplicationPrivacyConstraintNameDoesNotExist;
import eu.paasword.repository.relational.dao.ApplicationPrivacyConstraintRepository;
import eu.paasword.repository.relational.domain.ApplicationPrivacyConstraint;
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
public class ApplicationPrivacyConstraintServiceImpl implements IApplicationPrivacyConstraintService<ApplicationPrivacyConstraint> {

    @Autowired
    ApplicationPrivacyConstraintRepository applicationPrivacyConstraintRepository;

    private static final Logger logger = Logger.getLogger(ApplicationPrivacyConstraintServiceImpl.class.getName());

    @Override
    public void create(ApplicationPrivacyConstraint applicationPrivacyConstraint) throws ApplicationPrivacyConstraintAlreadyExistsException {

        //Check if name already exists
        if (null != applicationPrivacyConstraintRepository.findByName(applicationPrivacyConstraint.getName())) {
            throw new ApplicationPrivacyConstraintAlreadyExistsException(applicationPrivacyConstraint.getName());
        }

        //Store application privacy constraint to database
        applicationPrivacyConstraintRepository.save(applicationPrivacyConstraint);
    }

    @Override
    public void delete(long id) throws ApplicationPrivacyConstraintDoesNotExist {
        try {
            applicationPrivacyConstraintRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(ApplicationPrivacyConstraintServiceImpl.class.getName()).severe(ex.getMessage());
            throw new ApplicationPrivacyConstraintDoesNotExist(id);
        }
    }

    @Override
    public Optional<ApplicationPrivacyConstraint> findByName(String name) throws ApplicationPrivacyConstraintNameDoesNotExist {
        Optional<ApplicationPrivacyConstraint> applicationPrivacyConstraint = Optional.ofNullable(applicationPrivacyConstraintRepository.findByName(name));

        if (applicationPrivacyConstraint.isPresent()) {
            return applicationPrivacyConstraint;
        }

        throw new ApplicationPrivacyConstraintNameDoesNotExist(name);
    }

    @Override
    public Optional<ApplicationPrivacyConstraint> findOne(long id) throws ApplicationPrivacyConstraintDoesNotExist {
        Optional<ApplicationPrivacyConstraint> applicationPrivacyConstraint = Optional.ofNullable(applicationPrivacyConstraintRepository.findOne(id));

        if (applicationPrivacyConstraint.isPresent()) {
            return applicationPrivacyConstraint;
        }

        throw new ApplicationPrivacyConstraintDoesNotExist(id);
    }

    @Override
    public List<ApplicationPrivacyConstraint> findAll() {
        return applicationPrivacyConstraintRepository.findAll();
    }

    @Override
    public void edit(ApplicationPrivacyConstraint applicationPrivacyConstraint) throws ApplicationPrivacyConstraintDoesNotExist {

        ApplicationPrivacyConstraint currentApplicationPrivacyConstraint = applicationPrivacyConstraintRepository.findOne(applicationPrivacyConstraint.getId());

        //Check if current exists
        if (null == currentApplicationPrivacyConstraint) {
            throw new ApplicationPrivacyConstraintDoesNotExist(applicationPrivacyConstraint.getId());
        }

        currentApplicationPrivacyConstraint.setName(applicationPrivacyConstraint.getName());

        //Store application privacy constraint to database
        applicationPrivacyConstraintRepository.save(currentApplicationPrivacyConstraint);

    }

    public List<ApplicationPrivacyConstraint> findByApplicationID(long applicationID) {
        return applicationPrivacyConstraintRepository.findByApplicationID(applicationID);
    }

    public ApplicationPrivacyConstraint findOneWithoutApplication(long applicationPrivacyConstraintID) {
        return applicationPrivacyConstraintRepository.findOneWithoutApplication(applicationPrivacyConstraintID);
    }


}
