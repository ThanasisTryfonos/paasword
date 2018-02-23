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
package eu.paasword.app.security.auth;

import eu.paasword.api.repository.IUserService;
import eu.paasword.repository.relational.domain.User;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@Service
public class UserService implements UserDetailsService {

    private final static Logger logger = Logger.getLogger(UserService.class.getName());
    private final AccountStatusUserDetailsChecker detailsChecker = new AccountStatusUserDetailsChecker();
    private final IUserService userService;

    @Autowired
    public UserService(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.log(Level.INFO, "Trying to authenticate user: {0}", username);
        Optional<User> user = userService.findByUsername(username);

        //Check if a user exist with the specific username 
        if (false == user.isPresent()) {
            logger.log(Level.SEVERE, "User: {0} has not been found to the database", username);
            throw new UsernameNotFoundException("User: " + username + " has not been found to the database");
        }

        //Set the roles of current user
        Set<GrantedAuthority> roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(user.get().getUserRole().getRole()));

        org.springframework.security.core.userdetails.User currentUser;
        currentUser = new org.springframework.security.core.userdetails.User(user.get().getUsername(), user.get().getPassword(), user.get().isEnabled(), true, true, true, roles);
        
        //Check Details of current user
        detailsChecker.check(currentUser);

        return currentUser;
    }

}
