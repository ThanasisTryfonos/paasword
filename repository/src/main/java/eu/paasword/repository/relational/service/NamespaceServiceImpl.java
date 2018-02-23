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

import eu.paasword.api.repository.INamespaceService;
import eu.paasword.api.repository.exception.namespace.NamespaceAlreadyExistsException;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceNameDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespacePrefixDoesNotExist;
import eu.paasword.repository.relational.dao.NamespaceRepository;
import eu.paasword.repository.relational.domain.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author smantzouratos
 */
@Component
public class NamespaceServiceImpl implements INamespaceService<Namespace> {

    @Autowired
    NamespaceRepository namespaceRepository;

    private static final Logger logger = Logger.getLogger(NamespaceServiceImpl.class.getName());

    @Override
    public void create(Namespace namespace) throws NamespaceAlreadyExistsException {

        //Check if namespace name already exists
        if (null != namespaceRepository.findByName(namespace.getName())) {
            throw new NamespaceAlreadyExistsException(namespace.getName());
        }

        //Store namespace to database
        namespaceRepository.save(namespace);
    }

    @Override
    public void delete(long id) throws NamespaceDoesNotExist {
        try {
            namespaceRepository.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(NamespaceServiceImpl.class.getName()).severe(ex.getMessage());
            throw new NamespaceDoesNotExist(id);
        }
    }

    /**
     * @param prefix
     * @return
     * @throws NamespacePrefixDoesNotExist
     */
    @Override
    public Optional<Namespace> findByPrefix(String prefix) throws NamespacePrefixDoesNotExist {
        Optional<Namespace> namespace = Optional.ofNullable(namespaceRepository.findByPrefix(prefix));

        if (namespace.isPresent()) {
            return namespace;
        }

        throw new NamespacePrefixDoesNotExist(prefix);
    }

    @Override
    public Optional<Namespace> findByName(String name) throws NamespaceNameDoesNotExist {
        Optional<Namespace> namespace = Optional.ofNullable(namespaceRepository.findByName(name));

        if (namespace.isPresent()) {
            return namespace;
        }

        throw new NamespaceNameDoesNotExist(name);
    }

    @Override
    public Optional<Namespace> findOne(long id) throws NamespaceDoesNotExist {
        Optional<Namespace> namespace = Optional.ofNullable(namespaceRepository.findOne(id));

        if (namespace.isPresent()) {
            return namespace;
        }

        throw new NamespaceDoesNotExist(id);
    }

    @Override
    public List<Namespace> findAll() {
        return namespaceRepository.findAll();
    }

    @Override
    public Page<Namespace> findAll(Pageable page) {
        return namespaceRepository.findAll(page);
    }

    @Override
    public List<Namespace> findAllByOrderByPrefixAsc() {
        return namespaceRepository.findAllByOrderByPrefixAsc();
    }

    @Override
    public void edit(Namespace namespace) throws NamespaceDoesNotExist {

        Namespace currentNamespace = namespaceRepository.findOne(namespace.getId());

        //Check if current namespace doesn't exists
        if (null == currentNamespace) {
            throw new NamespaceDoesNotExist(namespace.getId());
        }

        //Store namespace to database
        namespaceRepository.save(namespace);

    }

}
