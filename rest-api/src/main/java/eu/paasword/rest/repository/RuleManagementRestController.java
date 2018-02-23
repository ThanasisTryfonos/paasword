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
package eu.paasword.rest.repository;

import eu.paasword.api.repository.IExpressionService;
import eu.paasword.api.repository.INamespaceService;
import eu.paasword.api.repository.IRuleService;
import eu.paasword.api.repository.exception.expression.ExpressionDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.api.repository.exception.rule.RuleAlreadyExistsException;
import eu.paasword.api.repository.exception.rule.RuleDoesNotExist;
import eu.paasword.api.repository.exception.rule.RuleValidityException;
import eu.paasword.repository.relational.domain.Expression;
import eu.paasword.repository.relational.domain.Namespace;
import eu.paasword.repository.relational.domain.Rule;
import eu.paasword.rest.repository.transferobject.TRule;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding rules
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/rule")
public class RuleManagementRestController {

    private static final Logger logger = Logger.getLogger(RuleManagementRestController.class.getName());

    @Value("${paasword.semauthengine.url}")
    private String semauthengineURL;

    @Autowired
    private IRuleService<Rule, Expression> ruleService;

    @Autowired
    private IExpressionService<Expression> expressionService;

    @Autowired
    private INamespaceService<Namespace> namespaceService;

    /**
     * Fetch all available rules from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getRules() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, ruleService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch a rule
     * with a specific ID from database.
     *
     * @param id The id of the rule to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getRuleByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, ruleService.findOne(id));
        } catch (RuleDoesNotExist ex) {
            Logger.getLogger(RuleManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new
     * rule to the database.
     *
     * @param tRule A JSON object which will be casted to a TRule (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TRule tRule) {

        try {

            Rule rule = new Rule();
            rule.setRuleName(tRule.getRuleName());
            rule.setDateCreated(new Date());
            rule.setDescription(tRule.getDescription());
            rule.setPermissionType(tRule.getPermissionType());

            if (tRule.getAction().equalsIgnoreCase("0")) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setAction(tRule.getAction());

            if (tRule.getActor().equalsIgnoreCase("0")) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setActor(tRule.getActor());

            if (tRule.getControlledObject().equalsIgnoreCase("0")) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setControlledObject(tRule.getControlledObject().substring(tRule.getControlledObject().indexOf(":") + 1));

            if (tRule.getAuthorization().equalsIgnoreCase("0")) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setAuthorization(tRule.getAuthorization());

            if (0 == tRule.getExpressionID()) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setExpressionID((Expression) expressionService.findOne(tRule.getExpressionID()).get());

            if (0 == tRule.getNamespaceID()) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setNamespaceID(namespaceService.findOne(tRule.getNamespaceID()).get());

            ruleService.create(rule);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (RuleAlreadyExistsException | ExpressionDoesNotExist | NamespaceDoesNotExist | RuleValidityException ex) {
            Logger.getLogger(RuleManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.RULE_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an
     * existing rule to the database.
     *
     * @param tRule A JSON object which will be casted to a Rule (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody TRule tRule) {
        try {

            Rule rule = (Rule) ruleService.findOne(tRule.getId()).get();

            rule.setRuleName(tRule.getRuleName());
            rule.setDescription(tRule.getDescription());
            rule.setPermissionType(tRule.getPermissionType());

            if (tRule.getAction().equalsIgnoreCase("0")) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setAction(tRule.getAction());

            if (tRule.getActor().equalsIgnoreCase("0")) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setActor(tRule.getActor());

            if (tRule.getControlledObject().equalsIgnoreCase("0")) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

//            rule.setControlledObject(tRule.getControlledObject());
            rule.setControlledObject(tRule.getControlledObject().substring(tRule.getControlledObject().indexOf(":") + 1));

            if (tRule.getAuthorization().equalsIgnoreCase("0")) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setAuthorization(tRule.getAuthorization());

            if (0 == tRule.getExpressionID()) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setExpressionID((Expression) expressionService.findOne(tRule.getExpressionID()).get());

            if (0 == tRule.getNamespaceID()) {
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, Message.ALL_FIELDS_REQUIRED, Optional.empty());
            }

            rule.setNamespaceID(namespaceService.findOne(tRule.getNamespaceID()).get());

            ruleService.edit(rule);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (RuleDoesNotExist | ExpressionDoesNotExist | NamespaceDoesNotExist | RuleValidityException ex) {
            Logger.getLogger(RuleManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.RULE_UPDATED, Optional.empty());
    }

    /**
     * Deletes a rule from database.
     *
     * @param id The id of the rule to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            ruleService.delete(id);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (RuleDoesNotExist | RuleValidityException ex) {
            Logger.getLogger(RuleManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.RULE_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an
     * PaaSwordRestResponse.
     */
    private final static class Message {

        final static String RULE_DELETED = "Rule has been deleted";
        final static String RULE_UPDATED = "Rule has been updated";
        final static String RULE_CREATED = "Rule has been created";
        final static String ALL_FIELDS_REQUIRED = "All fields are required";
    }
}
