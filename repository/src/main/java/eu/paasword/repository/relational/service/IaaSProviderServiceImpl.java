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

import eu.paasword.api.repository.IIaaSProviderService;
import eu.paasword.api.repository.exception.iaasProvider.IaaSProviderAlreadyExistsException;
import eu.paasword.api.repository.exception.iaasProvider.IaaSProviderDoesNotExist;
import eu.paasword.api.repository.exception.iaasProvider.IaaSProviderNameDoesNotExist;
import eu.paasword.repository.relational.dao.IaaSProviderRepository;
import eu.paasword.repository.relational.domain.IaaSProvider;
import eu.paasword.repository.relational.domain.PaaSProvider;
import eu.paasword.repository.relational.domain.User;
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
public class IaaSProviderServiceImpl implements IIaaSProviderService<IaaSProvider, User> {

    @Autowired
    IaaSProviderRepository iaasProviderRepository;

    private static final Logger logger = Logger.getLogger(IaaSProviderServiceImpl.class.getName());

    @Override
    public void create(IaaSProvider iaasProvider) throws IaaSProviderAlreadyExistsException {

        //Check if iaas provider already exists
        if (null != iaasProviderRepository.findByFriendlyName(iaasProvider.getFriendlyName())) {
            throw new IaaSProviderAlreadyExistsException(iaasProvider.getFriendlyName());
        }

        //Store iaas provider type to database
        iaasProviderRepository.save(iaasProvider);
    }

    @Override
    public void delete(long id) throws IaaSProviderDoesNotExist {
        try {
            iaasProviderRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(IaaSProviderServiceImpl.class.getName()).severe(ex.getMessage());
            throw new IaaSProviderDoesNotExist(id);
        }
    }

    @Override
    public Optional<IaaSProvider> findByName(String name) throws IaaSProviderNameDoesNotExist {
        Optional<IaaSProvider> iaasProvider = Optional.ofNullable(iaasProviderRepository.findByFriendlyName(name));

        if (iaasProvider.isPresent()) {
            return iaasProvider;
        }

        throw new IaaSProviderNameDoesNotExist(name);
    }

    @Override
    public Optional<IaaSProvider> findOne(long id) throws IaaSProviderDoesNotExist {
        Optional<IaaSProvider> iaasProvider = Optional.ofNullable(iaasProviderRepository.findOne(id));

        if (iaasProvider.isPresent()) {
            return iaasProvider;
        }

        throw new IaaSProviderDoesNotExist(id);
    }

    @Override
    public List<IaaSProvider> findAll() {
        return iaasProviderRepository.findAll();
    }

    @Override
    public void edit(IaaSProvider iaasProvider) throws IaaSProviderDoesNotExist {

        IaaSProvider currentIaaSProvider = iaasProviderRepository.findOne(iaasProvider.getId());

        //Check if current IaaSProvider exists
        if (null == currentIaaSProvider) {
            throw new IaaSProviderDoesNotExist(iaasProvider.getId());
        }

        currentIaaSProvider.setFriendlyName(iaasProvider.getFriendlyName());

        //Store PaaSProvider to database
        iaasProviderRepository.save(currentIaaSProvider);

    }

    public List<IaaSProvider> findIaaSProvidersByUsername(String username) {
        return iaasProviderRepository.getIaaSProvidersByUsername(username);
    }

    @Override
    public Page<IaaSProvider> findByUserID(User user, Pageable pageable) {
        return iaasProviderRepository.findByUserID(user, pageable);
    }
}
