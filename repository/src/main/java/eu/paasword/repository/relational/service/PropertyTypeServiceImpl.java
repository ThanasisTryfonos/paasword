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


import eu.paasword.api.repository.IPropertyTypeService;
import eu.paasword.api.repository.exception.propertyType.PropertyTypeAlreadyExistsException;
import eu.paasword.api.repository.exception.propertyType.PropertyTypeDoesNotExist;
import eu.paasword.api.repository.exception.propertyType.PropertyTypeNameDoesNotExist;
import eu.paasword.repository.relational.dao.PropertyTypeRepository;
import eu.paasword.repository.relational.domain.PropertyType;
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
public class PropertyTypeServiceImpl implements IPropertyTypeService<PropertyType> {

    @Autowired
    PropertyTypeRepository propertyTypeRepository;

    private static final Logger logger = Logger.getLogger(PropertyTypeServiceImpl.class.getName());

    @Override
    public void create(PropertyType propertyType) throws PropertyTypeAlreadyExistsException {

        //Check if property name already exists
        if (null != propertyTypeRepository.findByName(propertyType.getName())) {
            throw new PropertyTypeAlreadyExistsException(propertyType.getName());
        }

        //Store property type to database
        propertyTypeRepository.save(propertyType);
    }

    @Override
    public void delete(long id) throws PropertyTypeDoesNotExist {
        try {
            propertyTypeRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(PropertyTypeServiceImpl.class.getName()).severe(ex.getMessage());
            throw new PropertyTypeDoesNotExist(id);
        }
    }

    @Override
    public Optional<PropertyType> findByName(String name) throws PropertyTypeNameDoesNotExist {
        Optional<PropertyType> propertyType = Optional.ofNullable(propertyTypeRepository.findByName(name));

        if (propertyType.isPresent()) {
            return propertyType;
        }

        throw new PropertyTypeNameDoesNotExist(name);
    }

    @Override
    public Optional<PropertyType> findOne(long id) throws PropertyTypeDoesNotExist {
        Optional<PropertyType> propertyType = Optional.ofNullable(propertyTypeRepository.findOne(id));

        if (propertyType.isPresent()) {
            return propertyType;
        }

        throw new PropertyTypeDoesNotExist(id);
    }

    @Override
    public List<PropertyType> findAll() {
        return propertyTypeRepository.findAll();
    }

    @Override
    public void edit(PropertyType propertyType) throws PropertyTypeDoesNotExist {

        PropertyType currentPropertyType = propertyTypeRepository.findOne(propertyType.getId());

        //Check if current PropertyType exists
        if (null == currentPropertyType) {
            throw new PropertyTypeDoesNotExist(propertyType.getId());
        }

        currentPropertyType.setName(propertyType.getName());
        currentPropertyType.setRegexpRule(propertyType.getRegexpRule());

        //Store PropertyType to database
        propertyTypeRepository.save(currentPropertyType);

    }

}
