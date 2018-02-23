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

import eu.paasword.api.repository.IPaaSProviderService;
import eu.paasword.api.repository.exception.paasProvider.PaaSProviderAlreadyExistsException;
import eu.paasword.api.repository.exception.paasProvider.PaaSProviderDoesNotExist;
import eu.paasword.api.repository.exception.paasProvider.PaaSProviderNameDoesNotExist;
import eu.paasword.repository.relational.dao.PaaSProviderRepository;
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
public class PaaSProviderServiceImpl implements IPaaSProviderService<PaaSProvider, User> {

    @Autowired
    PaaSProviderRepository paasProviderRepository;

    private static final Logger logger = Logger.getLogger(PaaSProviderServiceImpl.class.getName());

    @Override
    public void create(PaaSProvider paasProvider) throws PaaSProviderAlreadyExistsException {

        //Check if paas provider already exists
        if (null != paasProviderRepository.findByFriendlyName(paasProvider.getFriendlyName())) {
            throw new PaaSProviderAlreadyExistsException(paasProvider.getFriendlyName());
        }

        //Store paas provider to database
        paasProviderRepository.save(paasProvider);
    }

    @Override
    public void delete(long id) throws PaaSProviderDoesNotExist {
        try {
            paasProviderRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(PaaSProviderServiceImpl.class.getName()).severe(ex.getMessage());
            throw new PaaSProviderDoesNotExist(id);
        }
    }

    @Override
    public Optional<PaaSProvider> findByName(String name) throws PaaSProviderNameDoesNotExist {
        Optional<PaaSProvider> paasProvider = Optional.ofNullable(paasProviderRepository.findByFriendlyName(name));

        if (paasProvider.isPresent()) {
            return paasProvider;
        }

        throw new PaaSProviderNameDoesNotExist(name);
    }

    @Override
    public Optional<PaaSProvider> findOne(long id) throws PaaSProviderDoesNotExist {
        Optional<PaaSProvider> iaasProvider = Optional.ofNullable(paasProviderRepository.findOne(id));

        if (iaasProvider.isPresent()) {
            return iaasProvider;
        }

        throw new PaaSProviderDoesNotExist(id);
    }

    @Override
    public List<PaaSProvider> findAll() {
        return paasProviderRepository.findAll();
    }

    @Override
    public void edit(PaaSProvider paasProvider) throws PaaSProviderDoesNotExist {

        PaaSProvider currentPaaSProvider = paasProviderRepository.findOne(paasProvider.getId());

        //Check if current PaaSProvider exists
        if (null == currentPaaSProvider) {
            throw new PaaSProviderDoesNotExist(paasProvider.getId());
        }

        currentPaaSProvider.setFriendlyName(paasProvider.getFriendlyName());

        //Store PaaSProvider to database
        paasProviderRepository.save(currentPaaSProvider);

    }

    public List<PaaSProvider> findPaaSProvidersByUsername(String username) {
        return paasProviderRepository.getPaaSProvidersByUsername(username);
    }

    @Override
    public Page<PaaSProvider> findByUserID(User user, Pageable pageable) {
        return paasProviderRepository.findByUserID(user, pageable);
    }


}
