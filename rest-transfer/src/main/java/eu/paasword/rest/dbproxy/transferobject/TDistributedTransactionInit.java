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
package eu.paasword.rest.dbproxy.transferobject;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ubuntu
 */
public class TDistributedTransactionInit implements Serializable {

    String appinstanceid;
    private List<String> resources;

    public TDistributedTransactionInit() {
    }    
    
    public TDistributedTransactionInit(String appinstanceid, List<String> resources) {
        this.appinstanceid = appinstanceid;
        this.resources = resources;
    }    
    
    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public String getAppinstanceid() {
        return appinstanceid;
    }

    public void setAppinstanceid(String appinstanceid) {
        this.appinstanceid = appinstanceid;
    }
    
}
