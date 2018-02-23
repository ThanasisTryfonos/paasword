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
package eu.paasword.repository.relational;

import eu.paasword.api.repository.ISystemPropertyService;
import eu.paasword.api.repository.IUserService;
import eu.paasword.api.repository.exception.policy.PolicyNameDoesNotExist;
import eu.paasword.api.repository.exception.systemProperty.SystemPropertyNameDoesNotExist;
import eu.paasword.app.main.Application;
import eu.paasword.repository.relational.dao.ApplicationInstanceRepository;
import eu.paasword.repository.relational.dao.ExpressionRepository;
import eu.paasword.repository.relational.dao.UserRepository;
import eu.paasword.repository.relational.domain.IaaSProvider;
import eu.paasword.repository.relational.domain.IaaSProviderType;
import eu.paasword.repository.relational.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.Transactional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import eu.paasword.repository.relational.dao.IaaSProviderRepository;
import eu.paasword.repository.relational.dao.IaaSProviderTypeRepository;
import eu.paasword.repository.relational.dao.PolicyRuleRepository;
import eu.paasword.repository.relational.dao.RuleRepository;
import eu.paasword.repository.relational.domain.ApplicationInstance;
import eu.paasword.repository.relational.domain.Expression;
import eu.paasword.repository.relational.domain.PolicyRule;
import eu.paasword.repository.relational.domain.Rule;
import eu.paasword.repository.relational.domain.SystemProperty;
import eu.paasword.triplestoreapi.parser.ContextModel2ExpertSystemRulesParser;
//import eu.paasword.triplestoreapi.response.RdfHashMap;

import java.io.IOException;

import org.json.JSONObject;

/**
 * @author vmadmin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@Rollback(true)
public class RelationalTest {

    private static final Logger logger = Logger.getLogger(RelationalTest.class.getName());

    public RelationalTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Autowired
    UserRepository userrepo;
    @Autowired
    IaaSProviderRepository iaasprovrepo;
    @Autowired
    IaaSProviderTypeRepository iaasprovidertyperepo;
    @Autowired
    IUserService userservice;
    @Autowired
    RuleRepository ruleRepository;
    @Autowired
    ExpressionRepository expressionRepository;
    @Autowired
    PolicyRuleRepository policyRuleRepository;

    @Autowired
    ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    ISystemPropertyService systemPropertyService;

    @Ignore
    @Test
    public void testWiring() {
        logger.info("Test Autowiring of all imported repositories");
        Assert.assertNotNull(userrepo);
        Assert.assertNotNull(iaasprovrepo);
        Assert.assertNotNull(iaasprovidertyperepo);
        Assert.assertNotNull(userservice);
        logger.info("Test testWiring finished successfully");
    }//EOM

    @Ignore
    @Test
    @Transactional
    public void testUserRepository() {
        logger.info("Test User repository");
        User user = new User();
        user.setFirstName("holaUser");
        userrepo.save(user);
    }//EOM 

    @Ignore
    @Test
    public void testIaaSProviderRepo() {
        try {
            List<IaaSProvider> optlist = iaasprovrepo.getIaaSProvidersByUsername("paasword");
            logger.info("IaaS Registrations for testuser " + optlist.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//EoM 

    @Ignore
    @Test
    @Transactional
    public void testIaaSRepositories() {
        try {
            logger.info("Test IaaSProvider repository");
//            User user = new User();
//            user.setFirstname("testfirst");            
//            user.setLastname("testlast");            
//            user.setUsername("testuser");
//            user.setPassword("testpass");
//            user.setEmail("jpa@email.com");
//            user.setUserrole(null == user.getUserrole()? new Userrole(Userrole.RoleName.DEVELOPER, user) : user.getUserrole());
//            userservice.create(user);

            Optional<User> user = userrepo.findByUsername("testuser");
            IaaSProviderType iaasprovtype = iaasprovidertyperepo.findByName("openstack");

            IaaSProvider iaasprovider = new IaaSProvider();
            iaasprovider.setFriendlyName("Openstack1");
            iaasprovider.setUserID(user.get());
            iaasprovider.setIaasProviderTypeID(iaasprovtype);

            iaasprovrepo.save(iaasprovider);

        } catch (Exception ex) {
            Logger.getLogger(RelationalTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//EoM

    @Ignore
    @Test
    public void testRuleRepo() {

        List<Expression> expressions = expressionRepository.findAll();

        for (Expression expression : expressions) {
            List<Rule> rules = ruleRepository.findByExpressionID(expression);

            if (rules != null) {
                rules.stream().forEach((rule) -> {
                    logger.log(Level.INFO, "Rule name {0}", rule.getRuleName());
                });
            }
        }

    }//EoM

    /**
     * testPolicyRuleRepo
     */
    @Ignore
    @Test
    public void testPolicyRuleRepo() {

        List<Rule> rules = ruleRepository.findAll();

        for (Rule rule : rules) {

            List<PolicyRule> policyRules = policyRuleRepository.findByRule(rule);

            policyRules.stream().forEach((policyRule) -> {
                logger.log(Level.INFO, "Policy name {0}", policyRule.getPolicy().getPolicyName());
            });

        }


    }//EoM

//    @Ignore
//    @Test
//    public void testFindDeployedAppsOnlyWithApplicationID() {
//        List<ApplicationInstance> applicationInstances = applicationInstanceRepository.findDeployedAppsOnlyWithApplicationID(true);
//
//        logger.info(" applicationInstances.size()" + applicationInstances.size());
//
//    }//EoM

    @Ignore
    @Test
    public void testInferredContextModel2ExpertSystemHashMap() {
        /*try {
            JSONObject contextModel = new JSONObject(((SystemProperty) systemPropertyService.findByName("context_model_json").get()).getValue());

            RdfHashMap rdfHashMap = ContextModel2ExpertSystemRulesParser.inferRulesAsMapFromJSON(contextModel.toString(), null, false);

            logger.info("Hashmap keys");
            for (String key : rdfHashMap.keySet()) {

                if (key.equalsIgnoreCase("http://www.example.com/test/1#Athens")) {
                    logger.info(key);

                    List<RdfHashMap.RdfStatement> a = rdfHashMap.get(key);
                    for (RdfHashMap.RdfStatement a1 : a) {
                        logger.log(Level.INFO, "Statement {0}", a1.toString());
                    }

                }

            }
        } //EoM
        catch (SystemPropertyNameDoesNotExist | IOException ex) {
            Logger.getLogger(RelationalTest.class.getName()).log(Level.SEVERE, null, ex);
        }*/

    }

    @Ignore
    @Test
    public void testContextModel2ExpertSystemHashMap() {
        /*try {
            JSONObject contextModel = new JSONObject(((SystemProperty) systemPropertyService.findByName("context_model_json").get()).getValue());

            RdfHashMap rdfHashMap = ContextModel2ExpertSystemRulesParser.getContextModelAsMap(contextModel.toString());

            logger.info("Hashmap keys");
            for (String key : rdfHashMap.keySet()) {

                if (key.equalsIgnoreCase("http://www.example.com/test/1#Athens")) {
                    logger.info(key);

                    List<RdfHashMap.RdfStatement> a = rdfHashMap.get(key);
                    for (RdfHashMap.RdfStatement a1 : a) {
                        logger.log(Level.INFO, "Statement {0}", a1.toString());
                    }

                }

            }
        } //EoM
        catch (SystemPropertyNameDoesNotExist | IOException ex) {
            Logger.getLogger(RelationalTest.class.getName()).log(Level.SEVERE, null, ex);
        }*/

    }

}//EoC
