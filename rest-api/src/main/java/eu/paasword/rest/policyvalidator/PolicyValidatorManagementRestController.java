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
package eu.paasword.rest.policyvalidator;

import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding Fuseki servers
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/policyvalidator")
public class PolicyValidatorManagementRestController {

    private static final Logger logger = Logger.getLogger(PolicyValidatorManagementRestController.class.getName());

    /**
     * Hello from Policy Validator
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse hello() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, "Hello from Policy Validator API!", Optional.empty());
    }

}
