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

import eu.paasword.api.repository.IPropertyService;
import eu.paasword.api.repository.exception.property.PropertyAlreadyExistsException;
import eu.paasword.api.repository.exception.property.PropertyDoesNotExist;
import eu.paasword.api.repository.exception.property.PropertyNameDoesNotExist;
import eu.paasword.repository.relational.dao.PropertyRepository;
import eu.paasword.repository.relational.domain.Clazz;
import eu.paasword.repository.relational.domain.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
@Component
public class PropertyServiceImpl implements IPropertyService<Property, Clazz> {

    @Autowired
    PropertyRepository propertyRepository;

    private static final Logger logger = Logger.getLogger(PropertyServiceImpl.class.getName());

    @Override
    public void create(Property property) throws PropertyAlreadyExistsException {

        //Check if Property name already exists
        if (null != propertyRepository.findByName(property.getName())) {
            throw new PropertyAlreadyExistsException(property.getName());
        }

        //Store Property to database
        propertyRepository.save(property);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();
    }

    @Override
    public void delete(long id) throws PropertyDoesNotExist {
        try {
            propertyRepository.delete(id);

            // Not needed, scheduler does this
//            triplestoreService.synchronizeCMToTripleStore();

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(PropertyServiceImpl.class.getName()).severe(ex.getMessage());
            throw new PropertyDoesNotExist(id);
        }
    }

    @Override
    public Optional<Property> findByName(String name) throws PropertyNameDoesNotExist {
        Optional<Property> property = Optional.ofNullable(propertyRepository.findByName(name));

        if (property.isPresent()) {
            return property;
        }

        throw new PropertyNameDoesNotExist(name);
    }

    @Override
    public Optional<Property> findOne(long id) throws PropertyDoesNotExist {
        Optional<Property> property = Optional.ofNullable(propertyRepository.findOne(id));

        if (property.isPresent()) {
            return property;
        }

        throw new PropertyDoesNotExist(id);
    }

    @Override
    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    @Override
    public Page<Property> findByClassID(Clazz clazz, Pageable pageable) {
        return propertyRepository.findByClassID(clazz, pageable);
    }

    @Override
    public Property findByNameAndClassID(String name, long classID) {
        return propertyRepository.findByNameAndClassID(name, classID);
    }

    @Override
    public List<Property> findByNameAndIdIn(String keyword, List<Long> ids) {
        return propertyRepository.findByNameAndIdIn(keyword, ids);
    }

    @Override
    public List<Property> findByIdIn(List<Long> ids) {
        return propertyRepository.findByIdIn(ids, null);
    }

    public List<Property> getPropertiesByKeyword(String keyword, List<Long> ids) {

        List<Property> listOfProperties = new ArrayList<>();
        if (keyword.matches("\\*+")) {
            int length = keyword.length();
            int page = 10 * (length - 1);
            Pageable pageable = new PageRequest(page, 10, new Sort(Sort.Direction.ASC, "name"));

            listOfProperties = propertyRepository.findByIdIn(ids, pageable);

        } else {
            listOfProperties = propertyRepository.findByNameAndIdIn(keyword, ids);

        }

        return listOfProperties;
    }

    @Override
    public void edit(Property property) throws PropertyDoesNotExist {

        Property currentProperty = propertyRepository.findOne(property.getId());

        //Check if current Property exists
        if (null == currentProperty) {
            throw new PropertyDoesNotExist(property.getId());
        }

        //Store Property to database
        propertyRepository.save(property);

        // Not needed, scheduler does this
//        triplestoreService.synchronizeCMToTripleStore();

    }

}
