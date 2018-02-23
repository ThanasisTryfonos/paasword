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
package eu.paasword.api.repository.exception.expression;


import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author smantzouratos
 */
public class ExpressionValidityException extends Exception {

    private final String message;

    public ExpressionValidityException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {

        String responseMsg = "";

        JSONObject jsonObject = new JSONObject(message);

        if (jsonObject.has("ruleContradictions") && jsonObject.getJSONArray("ruleContradictions").length() > 0) {

            JSONArray contradictions = jsonObject.getJSONArray("ruleContradictions");

            responseMsg = "Contradictions: <br/>";

            for (int i=0; i < contradictions.length(); i++) {

                JSONObject contr = (JSONObject) contradictions.get(i);
                responseMsg += "(" + contr.getString("rule1") + " - " + contr.getString("rule2") + ")<br/>";
            }
        } else {
            responseMsg = "Contradictions: - <br/>";
        }

        if (jsonObject.has("policyContradictions") && jsonObject.getJSONArray("policyContradictions").length() > 0) {

            JSONArray policyContradictions = jsonObject.getJSONArray("policyContradictions");

            responseMsg += "Policy Contradictions: <br/>";

            for (int i=0; i < policyContradictions.length(); i++) {

                String policy = (String) policyContradictions.get(i);
                responseMsg += "(" + policy + ")<br/>";
            }
        } else {
            responseMsg += "Policy Contradictions: - <br/>";
        }

        if (jsonObject.has("ruleSubsumptions") && jsonObject.getJSONArray("ruleSubsumptions").length() > 0) {

            JSONArray subsumptions = jsonObject.getJSONArray("ruleSubsumptions");

            responseMsg += "Subsumptions: <br/>";

            for (int i=0; i < subsumptions.length(); i++) {

                JSONObject subs = (JSONObject) subsumptions.get(i);
                responseMsg += "(" + subs.getString("rule1") + "-" + subs.getString("rule2") + ")<br/>";
            }
        } else {
            responseMsg += "Subsumptions: - <br/>";
        }


        return responseMsg;
    }

}
