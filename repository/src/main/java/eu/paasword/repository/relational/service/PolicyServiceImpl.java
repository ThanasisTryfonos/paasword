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

import eu.paasword.api.repository.IPolicyService;
import eu.paasword.api.repository.exception.expression.ExpressionValidityException;
import eu.paasword.api.repository.exception.policy.PolicyAlreadyExistsException;
import eu.paasword.api.repository.exception.policy.PolicyDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyNameDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyValidityException;
import eu.paasword.repository.relational.dao.PolicyRepository;
import eu.paasword.repository.relational.dao.PolicyRuleRepository;
import eu.paasword.repository.relational.dao.RuleRepository;
import eu.paasword.repository.relational.domain.Policy;
import eu.paasword.repository.relational.domain.PolicyRule;
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
public class PolicyServiceImpl implements IPolicyService<Policy> {

    @Autowired
    PolicyRepository policyRepository;

    @Autowired
    PolicyRuleRepository policyRuleRepository;

    @Autowired
    RuleRepository ruleRepository;

    @Autowired
    TriplestoreService triplestoreService;

    private static final Logger logger = Logger.getLogger(PolicyServiceImpl.class.getName());

    @Override
    public void create(Policy policy) throws PolicyAlreadyExistsException, PolicyValidityException {

        //Check if policy name already exists
        if (null != policyRepository.getPolicyByName(policy.getPolicyName())) {
            throw new PolicyAlreadyExistsException(policy.getPolicyName());
        }

        String triplestoreResponse = triplestoreService.validatePolicyModel(null, policy, null, null);

        if (null == triplestoreResponse) {

            policyRepository.save(policy);

        } else {
            throw new PolicyValidityException(triplestoreResponse);
        }

    }

    @Override
    public void delete(long id) throws PolicyDoesNotExist, PolicyValidityException {
        try {

            policyRepository.delete(id);

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(PolicyServiceImpl.class.getName()).severe(ex.getMessage());
            throw new PolicyDoesNotExist(id);
        }
    }

    @Override
    public Optional<Policy> findByPolicyName(String policyName) throws PolicyNameDoesNotExist {

        Optional<Policy> policy = Optional.ofNullable(policyRepository.getPolicyByName(policyName));

        if (policy.isPresent()) {
            return policy;
        }

        throw new PolicyNameDoesNotExist(policyName);
    }

    @Override
    public Optional<Policy> findOne(long id) throws PolicyDoesNotExist {
        Optional<Policy> policy = Optional.ofNullable(policyRepository.findOne(id));

        if (policy.isPresent()) {
            return policy;
        }

        throw new PolicyDoesNotExist(id);
    }

    @Override
    public List<Policy> findAll() {
        return policyRepository.findAll();
    }

    @Override
    public Page<Policy> findAll(Pageable pageable) {
        return policyRepository.findAll(pageable);
    }

    @Override
    public void edit(Policy policy) throws PolicyDoesNotExist, PolicyValidityException {

        Policy existingPolicy = policyRepository.findOne(policy.getId());

        //Check if current policy exists
        if (null == existingPolicy) {
            throw new PolicyDoesNotExist(policy.getId());
        }

        String triplestoreResponse = triplestoreService.validatePolicyModel(null, existingPolicy, null, null);

        if (null == triplestoreResponse) {

            policyRepository.save(existingPolicy);

        } else {
            throw new PolicyValidityException(triplestoreResponse);
        }

    }

}
