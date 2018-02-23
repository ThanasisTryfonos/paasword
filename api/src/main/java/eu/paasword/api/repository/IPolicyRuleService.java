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
package eu.paasword.api.repository;

import eu.paasword.api.repository.exception.policy.PolicyNameDoesNotExist;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 * @param <P> as PolicyRule
 * @param <R> as Rule
 */
@Service
public interface IPolicyRuleService<P,R> {

    /**
     * Fetch a list of policies from database given a rule.
     * 
     * @param rule
     * @return An instance of instance object wrapped in an Optional object
     * @throws PolicyNameDoesNotExist
     */
    public List<P> findByRule(R rule) throws PolicyNameDoesNotExist;

   
    

}
