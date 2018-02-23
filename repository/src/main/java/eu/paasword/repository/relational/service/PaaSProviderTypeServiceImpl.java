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

import eu.paasword.api.repository.IPaaSProviderTypeService;
import eu.paasword.api.repository.exception.paasProviderType.PaaSProviderTypeAlreadyExistsException;
import eu.paasword.api.repository.exception.paasProviderType.PaaSProviderTypeDoesNotExist;
import eu.paasword.api.repository.exception.paasProviderType.PaaSProviderTypeNameDoesNotExist;
import eu.paasword.repository.relational.dao.PaaSProviderTypeRepository;
import eu.paasword.repository.relational.domain.PaaSProviderType;
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
public class PaaSProviderTypeServiceImpl implements IPaaSProviderTypeService<PaaSProviderType> {

    @Autowired
    PaaSProviderTypeRepository paaSProviderTypeRepository;

    private static final Logger logger = Logger.getLogger(PaaSProviderTypeServiceImpl.class.getName());

    @Override
    public void create(PaaSProviderType paasProviderType) throws PaaSProviderTypeAlreadyExistsException {

        //Check if paas provider type already exists
        if (null != paaSProviderTypeRepository.findByName(paasProviderType.getName())) {
            throw new PaaSProviderTypeAlreadyExistsException(paasProviderType.getName());
        }

        //Store paas provider type to database
        paaSProviderTypeRepository.save(paasProviderType);
    }

    @Override
    public void delete(long id) throws PaaSProviderTypeDoesNotExist {
        try {
            paaSProviderTypeRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(PaaSProviderTypeServiceImpl.class.getName()).severe(ex.getMessage());
            throw new PaaSProviderTypeDoesNotExist(id);
        }
    }

    @Override
    public Optional<PaaSProviderType> findByName(String name) throws PaaSProviderTypeNameDoesNotExist {
        Optional<PaaSProviderType> paasProviderType = Optional.ofNullable(paaSProviderTypeRepository.findByName(name));

        if (paasProviderType.isPresent()) {
            return paasProviderType;
        }

        throw new PaaSProviderTypeNameDoesNotExist(name);
    }

    @Override
    public Optional<PaaSProviderType> findOne(long id) throws PaaSProviderTypeDoesNotExist {
        Optional<PaaSProviderType> paasProviderType = Optional.ofNullable(paaSProviderTypeRepository.findOne(id));

        if (paasProviderType.isPresent()) {
            return paasProviderType;
        }

        throw new PaaSProviderTypeDoesNotExist(id);
    }

    @Override
    public List<PaaSProviderType> findAll() {
        return paaSProviderTypeRepository.findAll();
    }

    @Override
    public List<PaaSProviderType> findByOrderByNameAsc() {
        return paaSProviderTypeRepository.findByOrderByNameAsc();
    }

    @Override
    public void edit(PaaSProviderType paasProviderType) throws PaaSProviderTypeDoesNotExist {

        PaaSProviderType currentPaaSProviderType = paaSProviderTypeRepository.findOne(paasProviderType.getId());

        //Check if current PaaSProviderType exists
        if (null == currentPaaSProviderType) {
            throw new PaaSProviderTypeDoesNotExist(paasProviderType.getId());
        }

        currentPaaSProviderType.setName(paasProviderType.getName());
        currentPaaSProviderType.setAdapterImplementation(paasProviderType.getAdapterImplementation());

        //Store PaaSProviderType to database
        paaSProviderTypeRepository.save(currentPaaSProviderType);

    }

}
