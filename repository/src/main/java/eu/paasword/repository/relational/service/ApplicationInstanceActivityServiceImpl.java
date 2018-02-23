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


import eu.paasword.api.repository.IApplicationInstanceActivityService;
import eu.paasword.api.repository.exception.applicationInstanceActivity.ApplicationInstanceActivityDoesNotExist;
import eu.paasword.repository.relational.dao.ApplicationInstanceActivityRepository;
import eu.paasword.repository.relational.domain.ApplicationInstanceActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Component
public class ApplicationInstanceActivityServiceImpl implements IApplicationInstanceActivityService<ApplicationInstanceActivity> {

    @Autowired
    ApplicationInstanceActivityRepository applicationInstanceActivityRepository;

    private static final Logger logger = Logger.getLogger(ApplicationInstanceActivityServiceImpl.class.getName());

    @Override
    public void create(ApplicationInstanceActivity applicationInstanceActivity) {

        applicationInstanceActivityRepository.save(applicationInstanceActivity);
    }

    @Override
    public void delete(long id) throws ApplicationInstanceActivityDoesNotExist {
        try {

            applicationInstanceActivityRepository.delete(id);

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(ApplicationInstanceActivityServiceImpl.class.getName()).severe(ex.getMessage());
            throw new ApplicationInstanceActivityDoesNotExist(id);
        }
    }

    @Override
    public Optional<ApplicationInstanceActivity> findOne(long id) throws ApplicationInstanceActivityDoesNotExist {
        Optional<ApplicationInstanceActivity> applicationInstanceActivity = Optional.ofNullable(applicationInstanceActivityRepository.findOne(id));

        if (applicationInstanceActivity.isPresent()) {
            return applicationInstanceActivity;
        }

        throw new ApplicationInstanceActivityDoesNotExist(id);
    }

    @Override
    public List<ApplicationInstanceActivity> findAll() {

        return applicationInstanceActivityRepository.findAll();
    }

    @Override
    public List<ApplicationInstanceActivity> findByApplicationInstanceID(long id) {

        Pageable pageable = new PageRequest(0, 100, new Sort(Sort.Direction.DESC, "invocationTimestamp"));

        return applicationInstanceActivityRepository.findByApplicationInstanceID(id, pageable);
    }

    @Override
    public List<ApplicationInstanceActivity> findByApplicationInstanceIDFirst100(long id) {

        Pageable pageable = new PageRequest(0, 100, new Sort(Sort.Direction.DESC, "invocationTimestamp"));

        return applicationInstanceActivityRepository.findByApplicationInstanceIDFirst100(id, pageable);
    }

    @Override
    public void edit(ApplicationInstanceActivity applicationInstanceActivity) throws ApplicationInstanceActivityDoesNotExist {

        ApplicationInstanceActivity currentApplicationInstanceActivity = applicationInstanceActivityRepository.findOne(applicationInstanceActivity.getId());

        if (null == currentApplicationInstanceActivity) {
            throw new ApplicationInstanceActivityDoesNotExist(applicationInstanceActivity.getId());
        }

        applicationInstanceActivityRepository.save(applicationInstanceActivity);

    }

}
