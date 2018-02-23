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

import eu.paasword.api.repository.IProxyCloudProviderService;
import eu.paasword.api.repository.exception.proxyCloudProvider.ProxyCloudProviderAlreadyExistsException;
import eu.paasword.api.repository.exception.proxyCloudProvider.ProxyCloudProviderDoesNotExist;
import eu.paasword.api.repository.exception.proxyCloudProvider.ProxyCloudProviderNameDoesNotExist;
import eu.paasword.repository.relational.dao.ProxyCloudProviderRepository;
import eu.paasword.repository.relational.domain.ProxyCloudProvider;
import eu.paasword.repository.relational.domain.User;
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
public class ProxyCloudProviderServiceImpl implements IProxyCloudProviderService<ProxyCloudProvider, User> {

    @Autowired
    ProxyCloudProviderRepository proxyCloudProviderRepository;

    private static final Logger logger = Logger.getLogger(ProxyCloudProviderServiceImpl.class.getName());

    @Override
    public void create(ProxyCloudProvider proxyCloudProvider) throws ProxyCloudProviderAlreadyExistsException {

        //Check if proxyCloudProvider already exists
        if (null != proxyCloudProviderRepository.findByFriendlyName(proxyCloudProvider.getFriendlyName())) {
            throw new ProxyCloudProviderAlreadyExistsException(proxyCloudProvider.getFriendlyName());
        }

        //Store proxyCloudProvider to database
        proxyCloudProviderRepository.save(proxyCloudProvider);
    }

    @Override
    public void delete(long id) throws ProxyCloudProviderDoesNotExist {
        try {
            proxyCloudProviderRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(ProxyCloudProviderServiceImpl.class.getName()).severe(ex.getMessage());
            throw new ProxyCloudProviderDoesNotExist(id);
        }
    }

    @Override
    public Optional<ProxyCloudProvider> findByFriendlyName(String friendlyName) throws ProxyCloudProviderNameDoesNotExist {
        Optional<ProxyCloudProvider> proxyCloudProvider = Optional.ofNullable(proxyCloudProviderRepository.findByFriendlyName(friendlyName));

        if (proxyCloudProvider.isPresent()) {
            return proxyCloudProvider;
        }

        throw new ProxyCloudProviderNameDoesNotExist(friendlyName);
    }

    @Override
    public Optional<ProxyCloudProvider> findOne(long id) throws ProxyCloudProviderDoesNotExist {
        Optional<ProxyCloudProvider> iaasProvider = Optional.ofNullable(proxyCloudProviderRepository.findOne(id));

        if (iaasProvider.isPresent()) {
            return iaasProvider;
        }

        throw new ProxyCloudProviderDoesNotExist(id);
    }

    @Override
    public List<ProxyCloudProvider> findAll() {
        return proxyCloudProviderRepository.findAll();
    }

    @Override
    public List<ProxyCloudProvider> findByUser(User user) {
        return proxyCloudProviderRepository.findByUserID(user);
    }

    @Override
    public void edit(ProxyCloudProvider proxyCloudProvider) throws ProxyCloudProviderDoesNotExist {

        ProxyCloudProvider currentProxyCloudProvider = proxyCloudProviderRepository.findOne(proxyCloudProvider.getId());

        //Check if current ProxyCloudProvider exists
        if (null == currentProxyCloudProvider) {
            throw new ProxyCloudProviderDoesNotExist(proxyCloudProvider.getId());
        }

        //Store ProxyCloudProvider to database
        proxyCloudProviderRepository.save(proxyCloudProvider);

    }

}
