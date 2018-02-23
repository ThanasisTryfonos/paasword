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


import eu.paasword.api.repository.IPolicySetService;
import eu.paasword.api.repository.exception.policy.PolicyAlreadyExistsException;
import eu.paasword.api.repository.exception.policy.PolicyValidityException;
import eu.paasword.api.repository.exception.policySet.PolicySetAlreadyExistsException;
import eu.paasword.api.repository.exception.policySet.PolicySetDoesNotExist;
import eu.paasword.api.repository.exception.policySet.PolicySetNameDoesNotExist;
import eu.paasword.api.repository.exception.policySet.PolicySetValidityException;
import eu.paasword.repository.relational.dao.PolicySetPolicyRepository;
import eu.paasword.repository.relational.dao.PolicySetRepository;
import eu.paasword.repository.relational.domain.PolicySet;
import eu.paasword.repository.relational.domain.PolicySetPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author smantzouratos
 */
@Component
public class PolicySetServiceImpl implements IPolicySetService<PolicySet> {

    @Autowired
    PolicySetRepository policySetRepository;

    @Autowired
    PolicySetPolicyRepository policySetPolicyRepository;

    @Autowired
    TriplestoreService triplestoreService;

    private static final Logger logger = Logger.getLogger(PolicySetServiceImpl.class.getName());

    @Override
    public void create(PolicySet policySet) throws PolicySetAlreadyExistsException, PolicySetValidityException {

        //Check if policy set name already exists
        if (null != policySetRepository.findByPolicySetName(policySet.getPolicySetName())) {
            throw new PolicySetAlreadyExistsException(policySet.getPolicySetName());
        }

        String triplestoreResponse = triplestoreService.validatePolicyModel(policySet, null, null, null);

        if (null == triplestoreResponse) {

            policySetRepository.save(policySet);

        } else {
            throw new PolicySetValidityException(triplestoreResponse);
        }

    }

    @Override
    public void delete(long id) throws PolicySetDoesNotExist, PolicySetValidityException {
        try {

            policySetRepository.delete(id);

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(PolicySetServiceImpl.class.getName()).severe(ex.getMessage());
            throw new PolicySetDoesNotExist(id);
        }
    }

    @Override
    public Optional<PolicySet> findByPolicySetName(String policySetName) throws PolicySetNameDoesNotExist {
        Optional<PolicySet> policySet = Optional.ofNullable(policySetRepository.findByPolicySetName(policySetName));

        if (policySet.isPresent()) {
            return policySet;
        }

        throw new PolicySetNameDoesNotExist(policySetName);
    }

    @Override
    public Optional<PolicySet> findOne(long id) throws PolicySetDoesNotExist {
        Optional<PolicySet> policySet = Optional.ofNullable(policySetRepository.findOne(id));

        if (policySet.isPresent()) {
            return policySet;
        }

        throw new PolicySetDoesNotExist(id);
    }

    @Override
    public List<PolicySet> findAll() {
        return policySetRepository.findAll();
    }

    @Override
    public Page<PolicySet> findAll(Pageable pageable) {
        return policySetRepository.findAll(pageable);
    }

    @Override
    public void edit(PolicySet policySet) throws PolicySetDoesNotExist, PolicySetValidityException {

        PolicySet existingPolicySet = policySetRepository.findOne(policySet.getId());

        //Check if current policy set exists
        if (null == existingPolicySet) {
            throw new PolicySetDoesNotExist(policySet.getId());
        }

        String triplestoreResponse = triplestoreService.validatePolicyModel(existingPolicySet, null, null, null);

        if (null == triplestoreResponse) {

            policySetRepository.save(existingPolicySet);

        } else {
            throw new PolicySetValidityException(triplestoreResponse);
        }

    }

}
