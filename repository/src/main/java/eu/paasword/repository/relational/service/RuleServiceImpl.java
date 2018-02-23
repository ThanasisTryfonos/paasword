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

import eu.paasword.api.repository.IRuleService;
import eu.paasword.api.repository.exception.rule.RuleAlreadyExistsException;
import eu.paasword.api.repository.exception.rule.RuleDoesNotExist;
import eu.paasword.api.repository.exception.rule.RuleNameDoesNotExist;
import eu.paasword.api.repository.exception.rule.RuleValidityException;
import eu.paasword.repository.relational.dao.RuleRepository;
import eu.paasword.repository.relational.domain.Expression;
import eu.paasword.repository.relational.domain.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author smantzouratos
 */
@Component
public class RuleServiceImpl implements IRuleService<Rule, Expression> {

    @Autowired
    RuleRepository ruleRepository;

    @Autowired
    TriplestoreService triplestoreService;

    private static final Logger logger = Logger.getLogger(RuleServiceImpl.class.getName());

    @Override
    public void create(Rule rule) throws RuleAlreadyExistsException, RuleValidityException {

        //Check if rule name already exists
        if (null != ruleRepository.findByRuleName(rule.getRuleName())) {
            throw new RuleAlreadyExistsException(rule.getRuleName());
        }

        String triplestoreResponse = triplestoreService.validatePolicyModel(null, null, rule, null);

        if (null == triplestoreResponse) {

            ruleRepository.save(rule);

        } else {
            throw new RuleValidityException(triplestoreResponse);
        }

    }

    @Override
    public void delete(long id) throws RuleDoesNotExist, RuleValidityException {
        try {

            ruleRepository.delete(id);

        } catch (EmptyResultDataAccessException ex) {
            Logger.getLogger(RuleServiceImpl.class.getName()).severe(ex.getMessage());
            throw new RuleDoesNotExist(id);
        }
    }

    @Override
    public Optional<Rule> findByRuleName(String ruleName) throws RuleNameDoesNotExist {
        Optional<Rule> policy = Optional.ofNullable(ruleRepository.findByRuleName(ruleName));

        if (policy.isPresent()) {
            return policy;
        }

        throw new RuleNameDoesNotExist(ruleName);
    }

    @Override
    public Optional<Rule> findOne(long id) throws RuleDoesNotExist {
        Optional<Rule> rule = Optional.ofNullable(ruleRepository.findOne(id));

        if (rule.isPresent()) {
            return rule;
        }

        throw new RuleDoesNotExist(id);
    }

    @Override
    public List<Rule> findAll() {
        return ruleRepository.findAll();
    }

    @Override
    public Page<Rule> findAll(Pageable pageable) {
        return ruleRepository.findAll(pageable);
    }

    @Override
    public void edit(Rule rule) throws RuleDoesNotExist, RuleValidityException {

        Rule ruleInstance = ruleRepository.findOne(rule.getId());

        //Check if current rule exists
        if (null == ruleInstance) {
            throw new RuleDoesNotExist(rule.getId());
        }

        String triplestoreResponse = triplestoreService.validatePolicyModel(null, null, rule, null);

        if (null == triplestoreResponse) {

            ruleRepository.save(rule);

        } else {
            throw new RuleValidityException(triplestoreResponse);
        }

    }

    @Override
    public List<Rule> findByExpression_id(Expression expression) throws RuleDoesNotExist, RuleValidityException {

        return ruleRepository.findByExpressionID(expression);
    }

}
