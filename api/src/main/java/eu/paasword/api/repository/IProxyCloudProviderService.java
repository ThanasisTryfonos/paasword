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
package eu.paasword.api.repository;

import eu.paasword.api.repository.exception.proxyCloudProvider.ProxyCloudProviderAlreadyExistsException;
import eu.paasword.api.repository.exception.proxyCloudProvider.ProxyCloudProviderDoesNotExist;
import eu.paasword.api.repository.exception.proxyCloudProvider.ProxyCloudProviderNameDoesNotExist;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author smantzouratos
 * @param <T>
 */
@Service
public interface IProxyCloudProviderService<T, U> {

    /**
     * Fetch a Proxy Cloud Provider from database given a name.
     * 
     * @param friendlyName Find a Proxy Cloud Provider based on a name
     * @return An instance of ProxyCloudProvider object wrapped in an Optional object
     * @throws ProxyCloudProviderNameDoesNotExist
     */
    public Optional<T> findByFriendlyName(String friendlyName) throws ProxyCloudProviderNameDoesNotExist;

    /**
     * Fetch a ProxyCloudProvider from database given an id.
     *
     * @param id The id of the ProxyCloudProvider to fetch
     * @return An instance of ProxyCloudProvider object wrapped in an Optional object
     * @throws ProxyCloudProviderDoesNotExist
     */
    public Optional<T> findOne(long id) throws ProxyCloudProviderDoesNotExist;

    /**
     * Deletes an Proxy Cloud Provider from database.
     *
     * @param id The id of the ProxyCloudProvider to be deleted
     * @throws ProxyCloudProviderDoesNotExist
     */
    public void delete(long id) throws ProxyCloudProviderDoesNotExist;

    /**
     * Fetch all Proxy Cloud Providers from database.
     *
     * @return A list of ProxyCloudProvider objects
     */
    public List<T> findAll();

    public List<T> findByUser(U u);

    /**
     * Creates a new Proxy Cloud Provider to the database.
     *
     * @param t A ProxyCloudProvider object
     * @throws ProxyCloudProviderAlreadyExistsException
     */
    public void create(T t) throws ProxyCloudProviderAlreadyExistsException;
    
    public void edit(T t) throws ProxyCloudProviderDoesNotExist;


}
