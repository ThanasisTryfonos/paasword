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
package eu.paasword.rest.repository.transferobject;

/**
 *
 * @author Panagiotis Gouvas
 */
public class TEditIaaSRegistration {
    
    private String iaasregistrationid;
    private String iaastypeid;
    private String friendlyname;    

    public String getIaasregistrationid() {
        return iaasregistrationid;
    }

    public void setIaasregistrationid(String iaasregistrationid) {
        this.iaasregistrationid = iaasregistrationid;
    }    
    
    public String getIaastypeid() {
        return iaastypeid;
    }

    public void setIaastypeid(String iaastypeid) {
        this.iaastypeid = iaastypeid;
    }

    public String getFriendlyname() {
        return friendlyname;
    }

    public void setFriendlyname(String friendlyname) {
        this.friendlyname = friendlyname;
    }
    
}
