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
package eu.paasword.dbproxy.transaction;

/**
 *
 * @author ubuntu
 */
public class ConnectionContext { 
    
    private String code;
    private String url;
    private String username;
    private String password;
    private String xadatasourceclassname;

    public ConnectionContext(String code, String url, String username, String password) {
        this.code = code;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public ConnectionContext(String code, String url, String username, String password, String xadatasourceclassname) {
        this.code = code;
        this.url = url;
        this.username = username;
        this.password = password;
        this.xadatasourceclassname = xadatasourceclassname;
    }

    public String getXadatasourceclassname() {
        return xadatasourceclassname;
    }

    public void setXadatasourceclassname(String xadatasourceclassname) {
        this.xadatasourceclassname = xadatasourceclassname;
    }    
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }    
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    
}//EoC
