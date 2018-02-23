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

import eu.paasword.api.repository.ISystemPropertyService;
import eu.paasword.api.repository.exception.systemProperty.SystemPropertyAlreadyExistsException;
import eu.paasword.api.repository.exception.systemProperty.SystemPropertyDoesNotExist;
import eu.paasword.api.repository.exception.systemProperty.SystemPropertyNameDoesNotExist;
import eu.paasword.repository.relational.dao.SystemPropertyRepository;
import eu.paasword.repository.relational.domain.SystemProperty;
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
public class SystemPropertyServiceImpl implements ISystemPropertyService<SystemProperty> {

    @Autowired
    SystemPropertyRepository systemPropertyRepository;

    private static final Logger logger = Logger.getLogger(SystemPropertyServiceImpl.class.getName());

    @Override
    public void create(SystemProperty systemProperty) throws SystemPropertyAlreadyExistsException {

        //Check if system property already exists
        if (null != systemPropertyRepository.findByName(systemProperty.getName())) {
            throw new SystemPropertyAlreadyExistsException(systemProperty.getName());
        }

        //Store system property to database
        systemPropertyRepository.save(systemProperty);
    }

    @Override
    public void delete(long id) throws SystemPropertyDoesNotExist {
        try {
            systemPropertyRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(SystemPropertyServiceImpl.class.getName()).severe(ex.getMessage());
            throw new SystemPropertyDoesNotExist(id);
        }
    }

    @Override
    public Optional<SystemProperty> findByName(String name) throws SystemPropertyNameDoesNotExist {
        Optional<SystemProperty> systemProperty = Optional.ofNullable(systemPropertyRepository.findByName(name));

        if (systemProperty.isPresent()) {
            return systemProperty;
        }

        throw new SystemPropertyNameDoesNotExist(name);
    }

    @Override
    public Optional<SystemProperty> findOne(long id) throws SystemPropertyDoesNotExist {
        Optional<SystemProperty> systemProperty = Optional.ofNullable(systemPropertyRepository.findOne(id));

        if (systemProperty.isPresent()) {
            return systemProperty;
        }

        throw new SystemPropertyDoesNotExist(id);
    }

    @Override
    public List<SystemProperty> findAll() {
        return systemPropertyRepository.findAll();
    }

    @Override
    public void edit(SystemProperty systemProperty) throws SystemPropertyDoesNotExist {

        SystemProperty currentSystemProperty = systemPropertyRepository.findOne(systemProperty.getId());

        //Check if current SystemProperty exists
        if (null == currentSystemProperty) {
            throw new SystemPropertyDoesNotExist(systemProperty.getId());
        }

        currentSystemProperty.setName(systemProperty.getName());
        currentSystemProperty.setValue(systemProperty.getValue());

        //Store SystemProperty to database
        systemPropertyRepository.save(currentSystemProperty);

    }

}
