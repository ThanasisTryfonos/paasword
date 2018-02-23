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

import eu.paasword.api.repository.IIaaSProviderImageService;
import eu.paasword.api.repository.exception.iaasProviderImage.IaaSProviderImageAlreadyExistsException;
import eu.paasword.api.repository.exception.iaasProviderImage.IaaSProviderImageDoesNotExist;
import eu.paasword.api.repository.exception.iaasProviderImage.IaaSProviderImageNameDoesNotExist;
import eu.paasword.repository.relational.dao.IaaSProviderImageRepository;
import eu.paasword.repository.relational.domain.IaaSProviderImage;
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
public class IaaSProviderImageServiceImpl implements IIaaSProviderImageService<IaaSProviderImage> {

    @Autowired
    IaaSProviderImageRepository iaaSProviderImageRepository;

    private static final Logger logger = Logger.getLogger(IaaSProviderImageServiceImpl.class.getName());

    @Override
    public void create(IaaSProviderImage iaaSProviderImage) throws IaaSProviderImageAlreadyExistsException {

        //Check if iaas provider image already exists
        if (null != iaaSProviderImageRepository.findByFriendlyName(iaaSProviderImage.getFriendlyName())) {
            throw new IaaSProviderImageAlreadyExistsException(iaaSProviderImage.getFriendlyName());
        }

        //Store iaas provider image to database
        iaaSProviderImageRepository.save(iaaSProviderImage);
    }

    @Override
    public void delete(long id) throws IaaSProviderImageDoesNotExist {
        try {
            iaaSProviderImageRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(IaaSProviderImageServiceImpl.class.getName()).severe(ex.getMessage());
            throw new IaaSProviderImageDoesNotExist(id);
        }
    }

    @Override
    public Optional<IaaSProviderImage> findByName(String name) throws IaaSProviderImageNameDoesNotExist {
        Optional<IaaSProviderImage> iaasProviderImage = Optional.ofNullable(iaaSProviderImageRepository.findByFriendlyName(name));

        if (iaasProviderImage.isPresent()) {
            return iaasProviderImage;
        }

        throw new IaaSProviderImageNameDoesNotExist(name);
    }

    @Override
    public Optional<IaaSProviderImage> findOne(long id) throws IaaSProviderImageDoesNotExist {
        Optional<IaaSProviderImage> iaasProviderImage = Optional.ofNullable(iaaSProviderImageRepository.findOne(id));

        if (iaasProviderImage.isPresent()) {
            return iaasProviderImage;
        }

        throw new IaaSProviderImageDoesNotExist(id);
    }

    @Override
    public List<IaaSProviderImage> findAll() {
        return iaaSProviderImageRepository.findAll();
    }

    @Override
    public void edit(IaaSProviderImage iaasProviderImage) throws IaaSProviderImageDoesNotExist {

        IaaSProviderImage currentIaaSProviderImage = iaaSProviderImageRepository.findOne(iaasProviderImage.getId());

        //Check if current IaaSProvider Image exists
        if (null == currentIaaSProviderImage) {
            throw new IaaSProviderImageDoesNotExist(iaasProviderImage.getId());
        }

        currentIaaSProviderImage.setFriendlyName(iaasProviderImage.getFriendlyName());

        //Store PaaSProvider to database
        iaaSProviderImageRepository.save(currentIaaSProviderImage);

    }

    public List<IaaSProviderImage> findIaaSProviderImagesByIaaSProvider(long iaasProviderID) {
        return iaaSProviderImageRepository.getIaaSProviderImagesByIaaSProvider(iaasProviderID);
    }

}
