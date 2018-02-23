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

import eu.paasword.api.repository.ICombiningAlgorithmService;
import eu.paasword.api.repository.INamespaceService;
import eu.paasword.api.repository.IPolicyService;
import eu.paasword.api.repository.IRuleService;
import eu.paasword.api.repository.exception.combiningAlgorithm.CombiningAlgorithmDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyAlreadyExistsException;
import eu.paasword.api.repository.exception.policy.PolicyDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyValidityException;
import eu.paasword.api.repository.exception.rule.RuleDoesNotExist;
import eu.paasword.repository.relational.dao.PolicyRuleRepository;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.repository.transferobject.TPolicy;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding policies
 *
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/policy")
public class PolicyManagementRestController {

    private static final Logger logger = Logger.getLogger(PolicyManagementRestController.class.getName());

    @Value("${paasword.semauthengine.url}")
    private String semauthengineURL;

    @Autowired
    IPolicyService<Policy> policyService;

    @Autowired
    IRuleService<Rule,Expression> ruleService;

    @Autowired
    PolicyRuleRepository policyRuleRepository;

    @Autowired
    INamespaceService<Namespace> namespaceService;

    @Autowired
    ICombiningAlgorithmService<CombiningAlgorithm> combiningAlgorithmService;

    /**
     * Fetch all available policies from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getPolicies() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, policyService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch a policy with a specific ID from database.
     *
     * @param id The id of the policy to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getPolicyByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, policyService.findOne(id));
        } catch (PolicyDoesNotExist ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new policy to the database.
     *
     * @param tPolicy A JSON object which will be casted to a TPolicy (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TPolicy tPolicy) {

        try {

            Policy policy = new Policy();

            policy.setPolicyName(tPolicy.getPolicyName());
            policy.setDescription(tPolicy.getDescription());

            if (0 == tPolicy.getPolicyCombiningAlgorithmID()) {
                policy.setCombiningAlgorithmID(null);
            } else {
                policy.setCombiningAlgorithmID(combiningAlgorithmService.findOne(tPolicy.getPolicyCombiningAlgorithmID()).get());
            }

            if (0 == tPolicy.getNamespaceID()) {
                policy.setNamespaceID(null);
            } else {
                policy.setNamespaceID(namespaceService.findOne(tPolicy.getNamespaceID()).get());
            }

            policy.setDateCreated(new Date());

            policyService.create(policy);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicyAlreadyExistsException | CombiningAlgorithmDoesNotExist | NamespaceDoesNotExist | PolicyValidityException ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to assign a rule to an existing policy to the database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}/assign/{ruleID}", method = RequestMethod.POST)
    public PaaSwordRestResponse assignRule(@PathVariable("id") long id, @PathVariable("ruleID") long ruleID) {

        try {

            Policy policy = (Policy) policyService.findOne(id).get();

            List<PolicyRule> policyRules = policy.getPolicyRules();

            PolicyRule policyRule = new PolicyRule();

            if (null != policyRules && !policyRules.isEmpty()) {

                policyRule.setPolicy(policy);
                policyRule.setRule((Rule) ruleService.findOne(ruleID).get());

                policyRules.add(policyRule);

            } else {

                policyRules = new ArrayList<>();

                policyRule.setPolicy(policy);
                policyRule.setRule((Rule) ruleService.findOne(ruleID).get());

                policyRules.add(policyRule);

            }

            policyRuleRepository.save(policyRule);

            policy.setPolicyRules(policyRules);

            policyService.edit(policy);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicyDoesNotExist | RuleDoesNotExist | PolicyValidityException ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_RULE_ASSIGNED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to unassign a rule to an existing policy to the database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}/unassign/{ruleID}", method = RequestMethod.POST)
    public PaaSwordRestResponse unassignRule(@PathVariable("id") long id, @PathVariable("ruleID") long ruleID) {

        try {

            Policy policy = (Policy) policyService.findOne(id).get();

            List<PolicyRule> policyRules = policy.getPolicyRules();
            List<PolicyRule> newPolicyRules = new ArrayList<>();

            if (null != policyRules && !policyRules.isEmpty()) {

                policyRules.stream().forEach(policyRule -> {

                    if (policyRule.getRule().getId() == ruleID && policyRule.getPolicy().getId() == id) {
                        policyRuleRepository.delete(policyRule);
                    } else {
                        newPolicyRules.add(policyRule);
                    }
                });

            }

            if (newPolicyRules.isEmpty()) {
                policy.setPolicyRules(null);
            } else {
                policy.setPolicyRules(newPolicyRules);
            }

            policyService.edit(policy);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicyDoesNotExist | PolicyValidityException ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_RULE_UNASSIGNED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing policy to the database.
     *
     * @param tPolicy A JSON object which will be casted to a TPolicy (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody TPolicy tPolicy) {
        try {

            if (tPolicy.getRulesCustom().equalsIgnoreCase("#null#")) {
                Logger.getLogger(PolicyManagementRestController.class.getName()).severe("Empty Rules");
                return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, "Please add rules first", Optional.empty());
            }

            Policy existingPolicy = (Policy) policyService.findOne(tPolicy.getId()).get();

            existingPolicy.setPolicyName(tPolicy.getPolicyName());
            existingPolicy.setDescription(tPolicy.getDescription());

            if (0 == tPolicy.getPolicyCombiningAlgorithmID()) {
                existingPolicy.setCombiningAlgorithmID(null);
            } else {
                existingPolicy.setCombiningAlgorithmID(combiningAlgorithmService.findOne(tPolicy.getPolicyCombiningAlgorithmID()).get());
            }

            if (0 == tPolicy.getNamespaceID()) {
                existingPolicy.setNamespaceID(null);
            } else {
                existingPolicy.setNamespaceID(namespaceService.findOne(tPolicy.getNamespaceID()).get());
            }

            policyService.edit(existingPolicy);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicyDoesNotExist | CombiningAlgorithmDoesNotExist | NamespaceDoesNotExist | PolicyValidityException ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_UPDATED, Optional.empty());
    }

    /**
     * Deletes a policy from database.
     *
     * @param id The id of the policy to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            policyService.delete(id);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicyDoesNotExist | PolicyValidityException ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String POLICY_DELETED = "Policy has been deleted";
        final static String POLICY_UPDATED = "Policy has been updated";
        final static String POLICY_CREATED = "Policy has been created";
        final static String POLICY_RULE_ASSIGNED = "Rule has been assigned successfully";
        final static String POLICY_RULE_UNASSIGNED = "Rule has been unassigned successfully";
    }
}
