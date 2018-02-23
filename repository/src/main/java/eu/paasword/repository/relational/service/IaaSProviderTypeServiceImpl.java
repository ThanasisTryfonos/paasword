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

import eu.paasword.api.repository.IIaaSProviderTypeService;
import eu.paasword.api.repository.exception.iaasProviderType.IaaSProviderTypeAlreadyExistsException;
import eu.paasword.api.repository.exception.iaasProviderType.IaaSProviderTypeDoesNotExist;
import eu.paasword.api.repository.exception.iaasProviderType.IaaSProviderTypeNameDoesNotExist;
import eu.paasword.repository.relational.dao.IaaSProviderTypeRepository;
import eu.paasword.repository.relational.domain.IaaSProviderType;
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
public class IaaSProviderTypeServiceImpl implements IIaaSProviderTypeService<IaaSProviderType> {

    @Autowired
    IaaSProviderTypeRepository iaaSProviderTypeRepository;

    private static final Logger logger = Logger.getLogger(IaaSProviderTypeServiceImpl.class.getName());

    @Override
    public void create(IaaSProviderType iaasProviderType) throws IaaSProviderTypeAlreadyExistsException {

        //Check if iaas provider type already exists
        if (null != iaaSProviderTypeRepository.findByName(iaasProviderType.getName())) {
            throw new IaaSProviderTypeAlreadyExistsException(iaasProviderType.getName());
        }

        //Store iaas provider type to database
        iaaSProviderTypeRepository.save(iaasProviderType);
    }

    @Override
    public void delete(long id) throws IaaSProviderTypeDoesNotExist {
        try {
            iaaSProviderTypeRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(IaaSProviderTypeServiceImpl.class.getName()).severe(ex.getMessage());
            throw new IaaSProviderTypeDoesNotExist(id);
        }
    }

    @Override
    public Optional<IaaSProviderType> findByName(String name) throws IaaSProviderTypeNameDoesNotExist {
        Optional<IaaSProviderType> iaasProviderType = Optional.ofNullable(iaaSProviderTypeRepository.findByName(name));

        if (iaasProviderType.isPresent()) {
            return iaasProviderType;
        }

        throw new IaaSProviderTypeNameDoesNotExist(name);
    }

    @Override
    public Optional<IaaSProviderType> findOne(long id) throws IaaSProviderTypeDoesNotExist {
        Optional<IaaSProviderType> iaasProviderType = Optional.ofNullable(iaaSProviderTypeRepository.findOne(id));

        if (iaasProviderType.isPresent()) {
            return iaasProviderType;
        }

        throw new IaaSProviderTypeDoesNotExist(id);
    }

    @Override
    public List<IaaSProviderType> findAll() {
        return iaaSProviderTypeRepository.findAll();
    }

    @Override
    public void edit(IaaSProviderType iaasProviderType) throws IaaSProviderTypeDoesNotExist {

        IaaSProviderType currentIaaSProviderType = iaaSProviderTypeRepository.findOne(iaasProviderType.getId());

        //Check if current IaaSProviderType exists
        if (null == currentIaaSProviderType) {
            throw new IaaSProviderTypeDoesNotExist(iaasProviderType.getId());
        }

        currentIaaSProviderType.setName(iaasProviderType.getName());
        currentIaaSProviderType.setAdapterImplementation(iaasProviderType.getAdapterImplementation());

        //Store PaaSProviderType to database
        iaaSProviderTypeRepository.save(currentIaaSProviderType);

    }

}
