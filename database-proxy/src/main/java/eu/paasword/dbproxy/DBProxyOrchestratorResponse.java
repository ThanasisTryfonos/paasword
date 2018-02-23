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

import eu.paasword.adapter.openstack.FragServer;
import java.util.List;

/**
 *
 * @author ubuntu
 */
public class DBProxyOrchestratorResponse {
    
    private boolean successresult;
    private String resultstatus;
    private List<FragServer> fragservers;
    private String configurationxml;    

    public boolean isSuccessresult() {
        return successresult;
    }

    public void setSuccessresult(boolean successresult) {
        this.successresult = successresult;
    }

    public String getResultstatus() {
        return resultstatus;
    }

    public void setResultstatus(String resultstatus) {
        this.resultstatus = resultstatus;
    }

    public List<FragServer> getFragservers() {
        return fragservers;
    }

    public void setFragservers(List<FragServer> fragservers) {
        this.fragservers = fragservers;
    }

    public String getConfigurationxml() {
        return configurationxml;
    }

    public void setConfigurationxml(String configurationxml) {
        this.configurationxml = configurationxml;
    }

    
    
}
