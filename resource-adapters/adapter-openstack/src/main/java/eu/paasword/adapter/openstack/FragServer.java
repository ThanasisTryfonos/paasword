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

import eu.paasword.adapter.openstack.IaaS;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class FragServer {

    public static final String CLASS_PAASWORD = "eu.paasword.dbproxy.database.SQLDatabase";
    public static final String DRIVER_POSTGRESS = "org.postgresql.Driver";

    public enum DBType {
        remote, remote_index, local
    };


    public enum SchemePlace {
        remote, local
    };

    public enum SchemeType {
        database
    };

    private DBType dbtype;
    private String host;
    private String name;
    private String user;
    private String password;
    private SchemePlace schemaplace;
    private SchemeType schematype;

    private IaaS iaas;
    private String serverid;
    private String deploymentinstanceid;

    public FragServer() {
    }

    public FragServer(DBType dbtype, String host, String name, String user, String password, SchemePlace schemaplace, SchemeType schematype, String deploymentinstanceid) {
        this.dbtype = dbtype;
        this.host = host;
        this.name = name;
        this.user = user;
        this.password = password;
        this.schemaplace = schemaplace;
        this.schematype = schematype;
        this.deploymentinstanceid = deploymentinstanceid;
    }

    public DBType getDbtype() {
        return dbtype;
    }

    public void setDbtype(DBType dbtype) {
        this.dbtype = dbtype;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SchemePlace getSchemaplace() {
        return schemaplace;
    }

    public void setSchemaplace(SchemePlace schemaplace) {
        this.schemaplace = schemaplace;
    }

    public SchemeType getSchematype() {
        return schematype;
    }

    public void setSchematype(SchemeType schematype) {
        this.schematype = schematype;
    }

    public String getServerid() {
        return serverid;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }    

    public IaaS getIaas() {
        return iaas;
    }

    public void setIaas(IaaS iaas) {
        this.iaas = iaas;
    }

    public String getDeploymentinstanceid() {
        return deploymentinstanceid;
    }

    public void setDeploymentinstanceid(String deploymentinstanceid) {
        this.deploymentinstanceid = deploymentinstanceid;
    }    
    
    
    @Override
    public String toString() {
        String retstr = "<database>                                           \n"
                + "         <type>" + dbtype + "</type>                       \n"
                + "         <class>" + CLASS_PAASWORD + "</class>             \n"
                + "         <driver>" + DRIVER_POSTGRESS + "</driver>         \n"
                + "         <driverConn>jdbc:postgresql</driverConn>          \n"
                + "         <host>" + host + "</host>                         \n"
                + "         <name>" + name + "</name>                         \n"
                + "          <user>" + user + "</user>                        \n"
                + "          <password>" + password + "</password>            \n"
                + "          <scheme>                                         \n"
                + "             <place>" + schemaplace + "</place>            \n"
                + "             <type>" + schematype + "</type>               \n"
                + "             <name></name>                                 \n"
                + "             </scheme>                                     \n"
                + "         </database>                                       \n";
        return retstr;
    }

}//EoC
