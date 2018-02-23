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

import eu.paasword.api.repository.IApplicationAffinityConstraintService;
import eu.paasword.api.repository.exception.applicationAffinityConstraint.ApplicationAffinityConstraintAlreadyExistsException;
import eu.paasword.api.repository.exception.applicationAffinityConstraint.ApplicationAffinityConstraintDoesNotExist;
import eu.paasword.api.repository.exception.applicationAffinityConstraint.ApplicationAffinityConstraintNameDoesNotExist;
import eu.paasword.repository.relational.dao.ApplicationAffinityConstraintRepository;
import eu.paasword.repository.relational.domain.ApplicationAffinityConstraint;
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
public class ApplicationAffinityConstraintServiceImpl implements IApplicationAffinityConstraintService<ApplicationAffinityConstraint> {

    @Autowired
    ApplicationAffinityConstraintRepository applicationAffinityConstraintRepository;

    private static final Logger logger = Logger.getLogger(ApplicationAffinityConstraintServiceImpl.class.getName());

    @Override
    public void create(ApplicationAffinityConstraint applicationAffinityConstraint) throws ApplicationAffinityConstraintAlreadyExistsException {

        //Check if name already exists
        if (null != applicationAffinityConstraintRepository.findByName(applicationAffinityConstraint.getName())) {
            throw new ApplicationAffinityConstraintAlreadyExistsException(applicationAffinityConstraint.getName());
        }

        //Store application affinity constraint to database
        applicationAffinityConstraintRepository.save(applicationAffinityConstraint);
    }

    @Override
    public void delete(long id) throws ApplicationAffinityConstraintDoesNotExist {
        try {
            applicationAffinityConstraintRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(ApplicationAffinityConstraintServiceImpl.class.getName()).severe(ex.getMessage());
            throw new ApplicationAffinityConstraintDoesNotExist(id);
        }
    }

    @Override
    public Optional<ApplicationAffinityConstraint> findByName(String name) throws ApplicationAffinityConstraintNameDoesNotExist {
        Optional<ApplicationAffinityConstraint> applicationAffinityConstraint = Optional.ofNullable(applicationAffinityConstraintRepository.findByName(name));

        if (applicationAffinityConstraint.isPresent()) {
            return applicationAffinityConstraint;
        }

        throw new ApplicationAffinityConstraintNameDoesNotExist(name);
    }

    @Override
    public Optional<ApplicationAffinityConstraint> findOne(long id) throws ApplicationAffinityConstraintDoesNotExist {
        Optional<ApplicationAffinityConstraint> applicationAffinityConstraint = Optional.ofNullable(applicationAffinityConstraintRepository.findOne(id));

        if (applicationAffinityConstraint.isPresent()) {
            return applicationAffinityConstraint;
        }

        throw new ApplicationAffinityConstraintDoesNotExist(id);
    }

    @Override
    public List<ApplicationAffinityConstraint> findAll() {
        return applicationAffinityConstraintRepository.findAll();
    }

    @Override
    public void edit(ApplicationAffinityConstraint applicationAffinityConstraint) throws ApplicationAffinityConstraintDoesNotExist {

        ApplicationAffinityConstraint currentApplicationAffinityConstraint = applicationAffinityConstraintRepository.findOne(applicationAffinityConstraint.getId());

        //Check if current exists
        if (null == currentApplicationAffinityConstraint) {
            throw new ApplicationAffinityConstraintDoesNotExist(applicationAffinityConstraint.getId());
        }

        //Store application affinity constraint to database
        applicationAffinityConstraintRepository.save(applicationAffinityConstraint);

    }

    public List<ApplicationAffinityConstraint> findByApplicationID(long applicationID) {
        return applicationAffinityConstraintRepository.findByApplicationID(applicationID);
    }

    public ApplicationAffinityConstraint findOneWithoutApplication(long applicationPrivacyConstraintID) {
        return applicationAffinityConstraintRepository.findOneWithoutApplication(applicationPrivacyConstraintID);
    }


}
