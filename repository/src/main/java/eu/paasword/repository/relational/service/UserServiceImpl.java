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

import eu.paasword.api.repository.IUserService;
import eu.paasword.api.repository.exception.user.InvalidEmailAddressException;
import eu.paasword.api.repository.exception.user.UserDoesNotExist;
import eu.paasword.api.repository.exception.user.UsernameAlreadyExistsException;
import eu.paasword.api.repository.exception.user.UsernameIsTooShortException;
import eu.paasword.api.repository.exception.user.WeakPasswordException;
import eu.paasword.repository.relational.dao.UserRepository;
import eu.paasword.repository.relational.domain.Namespace;
import eu.paasword.repository.relational.domain.User;
import eu.paasword.repository.relational.domain.UserRole;
import eu.paasword.util.Patterns;
import eu.paasword.util.Util;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@Component
public class UserServiceImpl implements IUserService<User> {

    @Autowired
    UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void create(User user) throws UsernameAlreadyExistsException, UsernameIsTooShortException, WeakPasswordException, InvalidEmailAddressException {

        //Check if username is too short
        if (null == user.getUsername() || user.getUsername().length() < MIN_CHARS_FOR_SPECIAL_FIELDS) {
            throw new UsernameIsTooShortException();
        }

        //Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException(user.getUsername());
        }

        //Check if password is weak
        if (null == user.getPassword() || user.getPassword().length() < MIN_CHARS_FOR_SPECIAL_FIELDS) {
            throw new WeakPasswordException(MIN_CHARS_FOR_SPECIAL_FIELDS);
        }

        //Check if password is weak
        if (null == user.getEmail() || false == Patterns.EMAIL_ADDRESS.matcher(user.getEmail()).find()) {
            throw new InvalidEmailAddressException(user.getEmail());
        }

        //Hash the users password using SHA-1
        user.setPassword(Util.createAlgorithm(user.getPassword(), Util.ALGORITHM.SHA.toString()));

        //Set UserRole
        user.setUserRole(null == user.getUserRole()? new UserRole(UserRole.RoleName.ROLE_DEVELOPER, user) : user.getUserRole());

        //Store user to database
        userRepository.save(user);
    }

    @Override
    public void edit(User user) throws UserDoesNotExist, WeakPasswordException, InvalidEmailAddressException {

        User currentUser = userRepository.findOne(user.getId());

        //Check if current user exists
        if (null == currentUser) {
            throw new UserDoesNotExist(user.getId());
        }

        //Check if password is weak
        if (null == user.getPassword() || user.getPassword().length() < MIN_CHARS_FOR_SPECIAL_FIELDS) {
            throw new WeakPasswordException(MIN_CHARS_FOR_SPECIAL_FIELDS);
        }

        //Check if password is weak
        if (null == user.getEmail() || false == Patterns.EMAIL_ADDRESS.matcher(user.getEmail()).find()) {
            throw new InvalidEmailAddressException(user.getEmail());
        }

        //Hash the users password using SHA-1
        currentUser.setPassword(Util.createAlgorithm(user.getPassword(), Util.ALGORITHM.SHA.toString()));
        
        currentUser.setFirstName(user.getFirstName());
        currentUser.setLastName(user.getLastName());
        currentUser.setEmail(user.getEmail());
        
        //Store user to database
        userRepository.save(currentUser);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAll(Pageable page) {
        return userRepository.findAll(page);
    }


    @Override
    public Optional<User> findOne(long id) throws UserDoesNotExist {
        Optional<User> user = Optional.ofNullable(userRepository.findOne(id));

        if (user.isPresent()) {
            return user;
        }

        throw new UserDoesNotExist(id);
    }

    @Override
    public void delete(long id) throws UserDoesNotExist {
        try {
            userRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new UserDoesNotExist(id);
        }
    }

}
