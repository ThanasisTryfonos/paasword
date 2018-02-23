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

import eu.paasword.api.repository.IAPIKeyService;

import eu.paasword.api.repository.exception.apikey.APIKeyAlreadyExistsException;
import eu.paasword.api.repository.exception.apikey.APIKeyDescriptionDoesNotExist;
import eu.paasword.api.repository.exception.apikey.APIKeyDoesNotExist;
import eu.paasword.api.repository.exception.apikey.APIKeyUniqueIDDoesNotExist;
import eu.paasword.repository.relational.dao.APIKeyRepository;
import eu.paasword.repository.relational.domain.APIKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Component
public class APIKeyServiceImpl implements IAPIKeyService<APIKey> {

    @Autowired
    private APIKeyRepository apiKeyRepository;

    private static final Logger logger = Logger.getLogger(APIKeyServiceImpl.class.getName());

    @Override
    public void create(APIKey apiKey) throws APIKeyAlreadyExistsException {

        //Check if apiKey description already exists
        if (null != apiKeyRepository.findByDescription(apiKey.getDescription())) {
            throw new APIKeyAlreadyExistsException(apiKey.getDescription());
        }

        apiKey.setUniqueID(UUID.randomUUID().toString());

        //Store apiKey to database
        apiKeyRepository.save(apiKey);
    }

    @Override
    public void delete(long id) throws APIKeyDoesNotExist {
        try {
            apiKeyRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(APIKeyServiceImpl.class.getName()).severe(ex.getMessage());
            throw new APIKeyDoesNotExist(id);
        }
    }

    /**
     *
     * @param uniqueID
     * @return
     * @throws APIKeyUniqueIDDoesNotExist
     */
    @Override
    public Optional<APIKey> findByUniqueID(String uniqueID) throws APIKeyUniqueIDDoesNotExist {
        Optional<APIKey> apiKey = Optional.ofNullable(apiKeyRepository.findByUniqueID(uniqueID));

        if (apiKey.isPresent()) {
            return apiKey;
        }

        throw new APIKeyUniqueIDDoesNotExist(uniqueID);
    }

    @Override
    public Optional<APIKey> findByDescription(String description) throws APIKeyDescriptionDoesNotExist {
        Optional<APIKey> apiKey = Optional.ofNullable(apiKeyRepository.findByDescription(description));

        if (apiKey.isPresent()) {
            return apiKey;
        }

        throw new APIKeyDescriptionDoesNotExist(description);
    }

    @Override
    public Optional<APIKey> findOne(long id) throws APIKeyDoesNotExist {
        Optional<APIKey> apiKey = Optional.ofNullable(apiKeyRepository.findOne(id));

        if (apiKey.isPresent()) {
            return apiKey;
        }

        throw new APIKeyDoesNotExist(id);
    }

    @Override
    public List<APIKey> findAll() {
        return apiKeyRepository.findAll();
    }

    public List<APIKey> findByApplicationID(long applicationID) {
        return apiKeyRepository.findByApplicationID(applicationID);
    }

    @Override
    public void edit(APIKey apiKey) throws APIKeyDoesNotExist {

        APIKey currentAPIKey = apiKeyRepository.findOne(apiKey.getId());

        //Check if current apiKey doesn't exists
        if (null == currentAPIKey) {
            throw new APIKeyDoesNotExist(apiKey.getId());
        }

        //Store apiKey to database
        apiKeyRepository.save(apiKey);

    }

}
