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
package eu.paasword.dbproxy;

import eu.paasword.adapter.openstack.IaaS;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by smantzouratos on 15/09/16.
 */
public class DatabaseProxyEngineImpl implements DatabaseProxyEngine {

    private static final Logger logger = Logger.getLogger(DatabaseProxyEngineImpl.class.getName());

    public DatabaseProxyEngineImpl() {
    }

    @Override
    public DBProxyOrchestratorResponse initializeSlipStreamDBProxy(String deploymentInstanceID, String tenantKey, JSONArray iaasResources, List<String> createStatements, List<String> allFields, ArrayList<ArrayList<String>> constraints) {
        return DBProxyOrchestrator.orchestrateSlipStreamDeployment(deploymentInstanceID, tenantKey, iaasResources, createStatements, allFields, constraints);
    }//EoM


    @Override
    public DBProxyOrchestratorResponse initializeDBProxy(String deploymentInstanceID, String tenantKey, List<IaaS> iaasResources, List<String> createStatements, List<String> allFields, ArrayList<ArrayList<String>> constraints) {
        return DBProxyOrchestrator.orchestrateDeployment(deploymentInstanceID, tenantKey, iaasResources, createStatements, allFields, constraints);
    }//EoM

}