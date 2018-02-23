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

/**
 * Created by Chris Petsos
 */
public class ProblematicRules {

    String reason;
    String rule1;
    String rule2;

    public ProblematicRules(String reason, String rule1, String rule2)
    {
        this.reason = reason;
        this.rule1 = rule1;
        this.rule2 = rule2;
    }

    public String toString()
    {
        return reason + ": " + rule1 + ", " + rule2;
    }

}
