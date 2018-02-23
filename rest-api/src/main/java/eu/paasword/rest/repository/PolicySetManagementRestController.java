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
import eu.paasword.api.repository.IPolicySetService;
import eu.paasword.api.repository.exception.combiningAlgorithm.CombiningAlgorithmDoesNotExist;
import eu.paasword.api.repository.exception.namespace.NamespaceDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyAlreadyExistsException;
import eu.paasword.api.repository.exception.policy.PolicyDoesNotExist;
import eu.paasword.api.repository.exception.policy.PolicyValidityException;
import eu.paasword.api.repository.exception.policySet.PolicySetAlreadyExistsException;
import eu.paasword.api.repository.exception.policySet.PolicySetDoesNotExist;
import eu.paasword.api.repository.exception.policySet.PolicySetValidityException;
import eu.paasword.api.repository.exception.rule.RuleDoesNotExist;
import eu.paasword.repository.relational.dao.PolicySetPolicyRepository;
import eu.paasword.repository.relational.domain.*;
import eu.paasword.rest.repository.transferobject.TPolicySet;
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
 * Contains all the rest endpoints regarding policy sets
 *
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/policyset")
public class PolicySetManagementRestController {

    private static final Logger logger = Logger.getLogger(PolicySetManagementRestController.class.getName());

    @Value("${paasword.semauthengine.url}")
    private String semauthengineURL;

    @Autowired
    IPolicySetService<PolicySet> policySetService;

    @Autowired
    IPolicyService<Policy> policyService;

    @Autowired
    PolicySetPolicyRepository policySetPolicyRepository;

    @Autowired
    INamespaceService<Namespace> namespaceService;

    @Autowired
    ICombiningAlgorithmService<CombiningAlgorithm> combiningAlgorithmService;

    /**
     * Fetch all available policy sets from database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse getPolicySets() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, policySetService.findAll());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to fetch a policy set with a specific ID from database.
     *
     * @param id The id of the policy set to fetch
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public PaaSwordRestResponse getPolicySetByID(@PathVariable("id") long id) {
        try {
            return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, null, policySetService.findOne(id));
        } catch (PolicySetDoesNotExist ex) {
            Logger.getLogger(PolicySetManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to create a new policy set to the database.
     *
     * @param tPolicySet A JSON object which will be casted to a PolicySet (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.POST)
    public PaaSwordRestResponse create(@RequestBody TPolicySet tPolicySet) {

        try {

            PolicySet policySet = new PolicySet();

            policySet.setPolicySetName(tPolicySet.getPolicySetName());
            policySet.setDescription(tPolicySet.getDescription());

            if (0 == tPolicySet.getPolicySetCombiningAlgorithmID()) {
                policySet.setCombiningAlgorithmID(null);
            } else {
                policySet.setCombiningAlgorithmID(combiningAlgorithmService.findOne(tPolicySet.getPolicySetCombiningAlgorithmID()).get());
            }

            if (0 == tPolicySet.getNamespaceID()) {
                policySet.setNamespaceID(null);
            } else {
                policySet.setNamespaceID(namespaceService.findOne(tPolicySet.getNamespaceID()).get());
            }

            policySet.setDateCreated(new Date());

            policySetService.create(policySet);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicySetAlreadyExistsException | CombiningAlgorithmDoesNotExist | NamespaceDoesNotExist | PolicySetValidityException ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_SET_CREATED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to assign a policy to an existing policy set to the database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}/assign/{policyID}", method = RequestMethod.POST)
    public PaaSwordRestResponse assignPolicy(@PathVariable("id") long id, @PathVariable("policyID") long policyID) {

        try {

            PolicySet policySet = (PolicySet) policySetService.findOne(id).get();

            List<PolicySetPolicy> policySetPolicies = policySet.getPolicySetPolicies();

            PolicySetPolicy policySetPolicy = new PolicySetPolicy();

            if (null != policySetPolicies && !policySetPolicies.isEmpty()) {

                policySetPolicy.setPolicySet(policySet);
                policySetPolicy.setPolicy((Policy) policyService.findOne(policyID).get());

                policySetPolicies.add(policySetPolicy);

            } else {

                policySetPolicies = new ArrayList<>();

                policySetPolicy.setPolicy((Policy) policyService.findOne(policyID).get());
                policySetPolicy.setPolicySet(policySet);

                policySetPolicies.add(policySetPolicy);

            }

            policySetPolicyRepository.save(policySetPolicy);

            policySet.setPolicySetPolicies(policySetPolicies);

            policySetService.edit(policySet);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicySetDoesNotExist | PolicyDoesNotExist | PolicySetValidityException ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_SET_POLICY_ASSIGNED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to unassign a policy to an existing policy set to the database.
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}/unassign/{policyID}", method = RequestMethod.POST)
    public PaaSwordRestResponse unassignRule(@PathVariable("id") long id, @PathVariable("policyID") long policyID) {

        try {

            PolicySet policySet = (PolicySet) policySetService.findOne(id).get();

            List<PolicySetPolicy> policySetPolicies = policySet.getPolicySetPolicies();
            List<PolicySetPolicy> newPolicySetPolicies = new ArrayList<>();

            if (null != policySetPolicies && !policySetPolicies.isEmpty()) {

                policySetPolicies.stream().forEach(policySetPolicy -> {

                    if (policySetPolicy.getPolicy().getId() == policyID && policySetPolicy.getPolicySet().getId() == id) {
                        policySetPolicyRepository.delete(policySetPolicy);
                    } else {
                        newPolicySetPolicies.add(policySetPolicy);
                    }
                });

            }

            if (newPolicySetPolicies.isEmpty()) {
                policySet.setPolicySetPolicies(null);
            } else {
                policySet.setPolicySetPolicies(newPolicySetPolicies);
            }

            policySetService.edit(policySet);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicySetDoesNotExist | PolicySetValidityException ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_SET_POLICY_UNASSIGNED, Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to update an existing policy set to the database.
     *
     * @param tPolicySet A JSON object which will be casted to a TPolicySet (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.PUT)
    public PaaSwordRestResponse edit(@RequestBody TPolicySet tPolicySet) {

        try {

            PolicySet existingPolicySet = new PolicySet();

            existingPolicySet.setId(tPolicySet.getId());
            existingPolicySet.setPolicySetName(tPolicySet.getPolicySetName());
            existingPolicySet.setDescription(tPolicySet.getDescription());

            if (0 == tPolicySet.getPolicySetCombiningAlgorithmID()) {
                existingPolicySet.setCombiningAlgorithmID(null);
            } else {
                existingPolicySet.setCombiningAlgorithmID(combiningAlgorithmService.findOne(tPolicySet.getPolicySetCombiningAlgorithmID()).get());
            }

            if (0 == tPolicySet.getNamespaceID()) {
                existingPolicySet.setNamespaceID(null);
            } else {
                existingPolicySet.setNamespaceID(namespaceService.findOne(tPolicySet.getNamespaceID()).get());
            }

            policySetService.edit(existingPolicySet);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(semauthengineURL + "/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicySetDoesNotExist | CombiningAlgorithmDoesNotExist | NamespaceDoesNotExist | PolicySetValidityException ex) {
            Logger.getLogger(PolicyManagementRestController.class.getName()).severe(ex.getMessage());
            return new PaaSwordRestResponse(BasicResponseCode.INVALID, ex.getMessage(), Optional.empty());
        }

        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_SET_UPDATED, Optional.empty());
    }

    /**
     * Deletes a policy set from database.
     *
     * @param id The id of the policy set to be deleted
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public PaaSwordRestResponse delete(@PathVariable("id") long id) {
        try {
            policySetService.delete(id);

            // TODO
            //Call update of Semantic Authorization Service for all knowledge bases that make use of the updated expression
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject("http://localhost:8082/api/semanticpolicyengine/loadrules", String.class);
            logger.info(response);

        } catch (PolicySetDoesNotExist | PolicySetValidityException ex) {
            Logger.getLogger(PolicySetManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordRestResponse(BasicResponseCode.EXCEPTION, ex.getMessage(), Optional.empty());
        }
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_SET_DELETED, Optional.empty());
    }

    /**
     * Inner class containing all the static messages which will be used in an PaaSwordRestResponse.
     *
     */
    private final static class Message {

        final static String POLICY_SET_DELETED = "Policy Set has been deleted";
        final static String POLICY_SET_UPDATED = "Policy Set has been updated";
        final static String POLICY_SET_CREATED = "Policy Set has been created";
        final static String POLICY_SET_POLICY_ASSIGNED = "Policy has been assigned successfully";
        final static String POLICY_SET_POLICY_UNASSIGNED = "Policy has been unassigned successfully";
    }
}
