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
package eu.paasword.validator;

import eu.paasword.validator.engine.OnlineSecurityPolicyValidator;
import eu.paasword.validator.engine.SecurityPolicyValidator;
import eu.paasword.validator.engine.ValidationReport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Created by smantzouratos on 04/07/16.
 */
public class ValidatorClient implements Validator {

    private static final Logger logger = Logger.getLogger(ValidatorClient.class.getName());

    public ValidatorClient() {

    }

    public ValidationReport validatePolicyModel(String rdf) {

        InputStream stream = new ByteArrayInputStream(rdf.getBytes(StandardCharsets.UTF_8));

        SecurityPolicyValidator policyValidator = new SecurityPolicyValidator(stream);

        ValidationReport report = policyValidator.validate();

        return report;
    }

    public ValidationReport validateOnlinePolicyModel(String contextModelTTL, String policyModelTTL) {

        ValidationReport report = OnlineSecurityPolicyValidator.validatePolicy(contextModelTTL, policyModelTTL);

        return report;
    }
}
