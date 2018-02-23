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

import eu.paasword.api.repository.IPropertyInstanceService;
import eu.paasword.api.repository.exception.propertyInstance.PropertyInstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.propertyInstance.PropertyInstanceDoesNotExist;
import eu.paasword.api.repository.exception.propertyInstance.PropertyInstanceNameDoesNotExist;
import eu.paasword.repository.relational.dao.PropertyInstanceRepository;
import eu.paasword.repository.relational.domain.PropertyInstance;
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
public class PropertyInstanceServiceImpl implements IPropertyInstanceService<PropertyInstance> {

    @Autowired
    PropertyInstanceRepository propertyInstanceRepository;

    private static final Logger logger = Logger.getLogger(PropertyInstanceServiceImpl.class.getName());

    @Override
    public void create(PropertyInstance propertyInstance) throws PropertyInstanceAlreadyExistsException {

        //Check if property instance name already exists
        if (null != propertyInstanceRepository.findByName(propertyInstance.getName())) {
            throw new PropertyInstanceAlreadyExistsException(propertyInstance.getName());
        }

        //Store property instance to database
        propertyInstanceRepository.save(propertyInstance);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();
    }

    @Override
    public void delete(long id) throws PropertyInstanceDoesNotExist {
        try {
            propertyInstanceRepository.delete(id);

            // Not needed, scheduler does this
//            triplestoreService.synchronizeCMToTripleStore();

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(PropertyInstanceServiceImpl.class.getName()).severe(ex.getMessage());
            throw new PropertyInstanceDoesNotExist(id);
        }
    }

    @Override
    public Optional<PropertyInstance> findByName(String name) throws PropertyInstanceNameDoesNotExist {
        Optional<PropertyInstance> propertyInstance = Optional.ofNullable(propertyInstanceRepository.findByName(name));

        if (propertyInstance.isPresent()) {
            return propertyInstance;
        }

        throw new PropertyInstanceNameDoesNotExist(name);
    }

    @Override
    public List<PropertyInstance> findByPropertyID(long propertyID) {
        List<PropertyInstance> propertyInstance = propertyInstanceRepository.findByPropertyID(propertyID);

        if (!propertyInstance.isEmpty()) {
            return propertyInstance;
        }

        return null;

    }

    public List<PropertyInstance> findByInstanceID(long instanceID) {

        List<PropertyInstance> propertyInstance = propertyInstanceRepository.findByInstanceID(instanceID);

        if (!propertyInstance.isEmpty()) {
            return propertyInstance;
        }

        return null;
    }

    @Override
    public Optional<PropertyInstance> findOne(long id) throws PropertyInstanceDoesNotExist {
        Optional<PropertyInstance> propertyInstance = Optional.ofNullable(propertyInstanceRepository.findOne(id));

        if (propertyInstance.isPresent()) {
            return propertyInstance;
        }

        throw new PropertyInstanceDoesNotExist(id);
    }

    @Override
    public List<PropertyInstance> findAll() {
        return propertyInstanceRepository.findAll();
    }

    @Override
    public void edit(PropertyInstance propertyInstance) throws PropertyInstanceDoesNotExist {

        PropertyInstance currentPropertyInstance = propertyInstanceRepository.findOne(propertyInstance.getId());

        //Check if current PropertyInstance exists
        if (null == currentPropertyInstance) {
            throw new PropertyInstanceDoesNotExist(currentPropertyInstance.getId());
        }

        //Store PropertyType to database
        propertyInstanceRepository.save(propertyInstance);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();

    }

}
