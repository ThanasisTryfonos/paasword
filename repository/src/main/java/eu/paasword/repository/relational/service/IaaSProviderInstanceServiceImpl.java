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

import eu.paasword.api.repository.IIaaSProviderInstanceService;
import eu.paasword.api.repository.exception.iaasProviderInstance.IaaSProviderInstanceAlreadyExistsException;
import eu.paasword.api.repository.exception.iaasProviderInstance.IaaSProviderInstanceDoesNotExist;
import eu.paasword.api.repository.exception.iaasProviderInstance.IaaSProviderInstanceNameDoesNotExist;
import eu.paasword.repository.relational.dao.IaaSProviderInstanceRepository;
import eu.paasword.repository.relational.domain.IaaSProviderInstance;
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
public class IaaSProviderInstanceServiceImpl implements IIaaSProviderInstanceService<IaaSProviderInstance> {

    @Autowired
    IaaSProviderInstanceRepository iaaSProviderInstanceRepository;

    private static final Logger logger = Logger.getLogger(IaaSProviderInstanceServiceImpl.class.getName());

    @Override
    public void create(IaaSProviderInstance iaaSProviderImage) throws IaaSProviderInstanceAlreadyExistsException {

        //Check if iaas provider instance already exists
        if (null != iaaSProviderInstanceRepository.findByFriendlyName(iaaSProviderImage.getFriendlyName())) {
            throw new IaaSProviderInstanceAlreadyExistsException(iaaSProviderImage.getFriendlyName());
        }

        //Store iaas provider instance to database
        iaaSProviderInstanceRepository.save(iaaSProviderImage);
    }

    @Override
    public void delete(long id) throws IaaSProviderInstanceDoesNotExist {
        try {
            iaaSProviderInstanceRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(IaaSProviderInstanceServiceImpl.class.getName()).severe(ex.getMessage());
            throw new IaaSProviderInstanceDoesNotExist(id);
        }
    }

    @Override
    public Optional<IaaSProviderInstance> findByName(String name) throws IaaSProviderInstanceNameDoesNotExist {
        Optional<IaaSProviderInstance> iaasProviderImage = Optional.ofNullable(iaaSProviderInstanceRepository.findByFriendlyName(name));

        if (iaasProviderImage.isPresent()) {
            return iaasProviderImage;
        }

        throw new IaaSProviderInstanceNameDoesNotExist(name);
    }

    @Override
    public Optional<IaaSProviderInstance> findOne(long id) throws IaaSProviderInstanceDoesNotExist {
        Optional<IaaSProviderInstance> iaasProviderInstance = Optional.ofNullable(iaaSProviderInstanceRepository.findOne(id));

        if (iaasProviderInstance.isPresent()) {
            return iaasProviderInstance;
        }

        throw new IaaSProviderInstanceDoesNotExist(id);
    }

    @Override
    public List<IaaSProviderInstance> findAll() {
        return iaaSProviderInstanceRepository.findAll();
    }

    @Override
    public void edit(IaaSProviderInstance iaasProviderInstance) throws IaaSProviderInstanceDoesNotExist {

        IaaSProviderInstance currentIaaSProviderInstance = iaaSProviderInstanceRepository.findOne(iaasProviderInstance.getId());

        //Check if current IaaSProvider Instance exists
        if (null == currentIaaSProviderInstance) {
            throw new IaaSProviderInstanceDoesNotExist(iaasProviderInstance.getId());
        }

        currentIaaSProviderInstance.setFriendlyName(iaasProviderInstance.getFriendlyName());

        //Store PaaSProvider Instance to database
        iaaSProviderInstanceRepository.save(currentIaaSProviderInstance);

    }

    public List<IaaSProviderInstance> findIaaSProviderInstancesByIaaSProvider(long iaasProviderID) {
        return iaaSProviderInstanceRepository.getIaaSProviderInstancesByIaaSProvider(iaasProviderID);
    }

}
