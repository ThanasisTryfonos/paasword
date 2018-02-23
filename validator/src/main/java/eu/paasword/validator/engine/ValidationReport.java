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
package eu.paasword.validator.engine;

import java.util.List;

/**
 * Created by Chris Petsos
 */
public class ValidationReport {

    private List<ProblematicRules> ruleContradictions;
    private List<String> policyContradictions;
    private List<ProblematicRules> ruleSubsumptions;
    private boolean valid;

    public void setContradictingRules(List<ProblematicRules> ruleContradictions)
    {
        this.ruleContradictions = ruleContradictions;
    }

    public void setContradictingPolicies(List<String> policyContradictions)
    {
        this.policyContradictions = policyContradictions;
    }

    public void setSubsumptiveRules(List<ProblematicRules> ruleSubsumptions)
    {
        this.ruleSubsumptions = ruleSubsumptions;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
