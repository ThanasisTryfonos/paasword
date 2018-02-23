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

import eu.paasword.api.repository.IUserCredentialService;
import eu.paasword.api.repository.exception.userCredential.UserCredentialDoesNotExist;
import eu.paasword.repository.relational.dao.UserCredentialRepository;
import eu.paasword.repository.relational.domain.ProxyCloudProvider;
import eu.paasword.repository.relational.domain.User;
import eu.paasword.repository.relational.domain.UserCredential;
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
public class UserCredentialServiceImpl implements IUserCredentialService<UserCredential, ProxyCloudProvider, User> {

    @Autowired
    UserCredentialRepository userCredentialRepository;

    private static final Logger logger = Logger.getLogger(UserCredentialServiceImpl.class.getName());

    @Override
    public void create(UserCredential userCredential) {

        userCredentialRepository.save(userCredential);
    }

    @Override
    public void delete(long id) throws UserCredentialDoesNotExist {
        try {
            userCredentialRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(UserCredentialServiceImpl.class.getName()).severe(ex.getMessage());
            throw new UserCredentialDoesNotExist(id);
        }
    }


    @Override
    public List<UserCredential> findByProxyCloudProvider(ProxyCloudProvider proxyCloudProvider) {
        return userCredentialRepository.findByProxyCloudProvider(proxyCloudProvider);
    }

    @Override
    public UserCredential findByProxyCloudProviderAndUser(Long proxyCloudProviderID, Long userID) {
        return userCredentialRepository.findByProxyCloudProviderAndUser(proxyCloudProviderID, userID);
    }

    @Override
    public List<UserCredential> findByUser(User user) {
        return userCredentialRepository.findByUser(user);
    }

    @Override
    public Optional<UserCredential> findOne(long id) throws UserCredentialDoesNotExist {
        Optional<UserCredential> userCredential = Optional.ofNullable(userCredentialRepository.findOne(id));

        if (userCredential.isPresent()) {
            return userCredential;
        }

        throw new UserCredentialDoesNotExist(id);
    }

    @Override
    public List<UserCredential> findAll() {
        return userCredentialRepository.findAll();
    }

    @Override
    public void edit(UserCredential userCredential) throws UserCredentialDoesNotExist {

        UserCredential currentUserCredential = userCredentialRepository.findOne(userCredential.getId());

        //Check if currentUserCredential exists
        if (null == currentUserCredential) {
            throw new UserCredentialDoesNotExist(userCredential.getId());
        }

        //Store currentUserCredential to database
        userCredentialRepository.save(currentUserCredential);

    }

}
