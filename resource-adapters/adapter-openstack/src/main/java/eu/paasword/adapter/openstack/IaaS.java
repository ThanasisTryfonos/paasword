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
package eu.paasword.adapter.openstack;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class IaaS {
    String id;
    String connectionURL; 
    String username; 
    String password; 
    String domain; 
    String project;    
    String imageid;
    String networkid;
    String flavorid;
    
    public IaaS(String id, String connectionURL, String username, String password, String domain, String project,String imageid,String networkid, String flavorid) {
        this.id = id;
        this.connectionURL = connectionURL;
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.project = project;
        this.imageid = imageid;
        this.networkid = networkid;
        this.flavorid = flavorid;
    }

    public String getFlavorid() {
        return flavorid;
    }

    public void setFlavorid(String flavorid) {
        this.flavorid = flavorid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
        
    
    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getImageid() {
        return imageid;
    }

    public void setImageid(String imageid) {
        this.imageid = imageid;
    }

    public String getNetworkid() {
        return networkid;
    }

    public void setNetworkid(String networkid) {
        this.networkid = networkid;
    }    
    
}
