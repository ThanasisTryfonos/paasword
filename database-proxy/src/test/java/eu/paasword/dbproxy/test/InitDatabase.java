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
package eu.paasword.dbproxy.test;

import eu.paasword.adapter.openstack.IaaS;
import eu.paasword.dbproxy.DBProxyOrchestratorResponse;
import eu.paasword.dbproxy.DatabaseProxyEngine;
import eu.paasword.dbproxy.DatabaseProxyEngineImpl;
import eu.paasword.util.Util;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Wrapper class to run the main method that will create the local and remote
 * database.
 */
public class InitDatabase {

    private static final Logger logger = Logger.getLogger(InitDatabase.class.getName());

    public static void main(String[] args) {

        String deploymentInstanceID = "00c34888-50c6-4368-a5da-da2e8e321836"; // UniqueID;

        List<IaaS> iaasResources = new ArrayList<>();
        List<String> createStatements = new ArrayList<>();
        List<String> allFields = new ArrayList<>();
        ArrayList<ArrayList<String>> constraints = new ArrayList<>();

        constructParams(iaasResources, createStatements, allFields, constraints);

        DatabaseProxyEngine dbProxyEngine = new DatabaseProxyEngineImpl();

        String tenantKey = Util.generateRandomString(16, Util.Mode.ALPHA);

        DBProxyOrchestratorResponse response = dbProxyEngine.initializeDBProxy(deploymentInstanceID, tenantKey, iaasResources, createStatements, allFields, constraints);

        logger.info("Response: " + response.getConfigurationxml());

    }//EoM


    public static void constructParams(List<IaaS> iaasResources, List<String> createStatements, List<String> allFields, ArrayList<ArrayList<String>> constraints) {

        IaaS iaas1 = new IaaS("1", "http://192.168.3.253:5000/v3", "paasword", "!paasword!", "default", "paasword", "9106ea21-56a2-4330-8d91-92859aae163c", "484e62cb-9e39-4e46-b64a-2dbb63a80a29", "1ebf540d-6a2d-490a-8872-6538ba744dde");
        iaasResources.add(iaas1);

        JSONArray createStatementsArray = new JSONArray("[\"CREATE TABLE service   (id int  primary key  , fk_orbiuser_orbiuser int     references orbiuser , service_name char (225)  not null , description char (225)   , price double precision    , vat char (20)   , last_modified char (100)   , date_created Date   not null ) \"," +
                "\"CREATE TABLE orbiuser   (id int  primary key  , username char (225)   , password char (225)   , first_name char (225)   , last_name char (225)   , phone char (50)   , email char (225)   , date_created Date   not null , first_login int    , enabled int    , fk_role_role int     references role ) \", " +
                "\"CREATE TABLE contact   (id int  primary key  , fk_orbiuser_orbiuser int     references orbiuser , first_name char (225)   , last_name char (225)   , company_name char (225)   , phone char (30)   , fax char (30)   , mobile char (30)   , email char (225)  not null , address char (225)   , postal_code char (10)   , city char (50)   , country char (50)   , website char (225)   , comments char (225)   , vat_number char (20)   , vat_office char (50)   , vat_category char (20)   , client_type char (20)  not null , profession char (225)   , category char (50)   , last_modified char (50)   , date_created Date   not null , enabled int    ) \", " +
                "\"CREATE TABLE role   (id int  primary key  , actor char (128)  not null , description char (225)   ) \", " +
                "\"CREATE TABLE project   (id int  primary key  , fk_orbiuser_orbiuser int     references orbiuser , fk_contact_contact int     references contact , project_name char (225)   , description char (225)   , date_started Date   not null , date_ended Date   not null , budget double precision    , status char (30)   , last_modified char (100)   , date_created Date   not null , services char (225)   ) \", " +
                "\"CREATE TABLE payment   (id int  primary key  , payment_external_id char (100)   , payment_category char (50)   , reason char (225)   , payment_type char (50)   , fk_orbiuser_orbiuser int     references orbiuser , fk_project_project int     references project , fk_contact_contact int     references contact , amount double precision    , payment_date Date   not null , last_modified char (100)   , date_created Date   not null ) \", " +
                "\"CREATE TABLE invoice   (id int  primary key  , invoice_external_id char (100)   , invoice_type char (50)  not null , payment_type char (50)   , fk_orbiuser_orbiuser int     references orbiuser , fk_project_project int     references project , fk_contact_contact int     references contact , budget double precision    , vat double precision    , date_issued Date   not null , last_modified char (50)   , date_created Date   not null ) \"]");

        for (Object stmt : createStatementsArray) {
            createStatements.add((String) stmt);
        }

        JSONArray fieldsArray = new JSONArray("[\"service.id\", \"service.fk_orbiuser_orbiuser\", \"service.service_name\", \"service.description\", \"service.price\", \"service.vat\", \"service.last_modified\", \"service.date_created\", \"role.id\", \"role.actor\", \"role.description\", \"orbiuser.id\", \"orbiuser.username\", \"orbiuser.password\", \"orbiuser.first_name\", \"orbiuser.last_name\", \"orbiuser.phone\", \"orbiuser.email\", \"orbiuser.date_created\", \"orbiuser.first_login\", \"orbiuser.enabled\", \"orbiuser.fk_role_role\", \"project.id\", \"project.fk_orbiuser_orbiuser\", \"project.fk_contact_contact\", \"project.project_name\", \"project.description\", \"project.date_started\", \"project.date_ended\", \"project.budget\", \"project.status\", \"project.last_modified\", \"project.date_created\", \"project.services\", \"payment.id\", \"payment.payment_external_id\", \"payment.payment_category\", \"payment.reason\", \"payment.payment_type\", \"payment.fk_orbiuser_orbiuser\", \"payment.fk_project_project\", \"payment.fk_contact_contact\", \"payment.amount\", \"payment.payment_date\", \"payment.last_modified\", \"payment.date_created\", \"invoice.id\", \"invoice.invoice_external_id\", \"invoice.invoice_type\", \"invoice.payment_type\", \"invoice.fk_orbiuser_orbiuser\", \"invoice.fk_project_project\", \"invoice.fk_contact_contact\", \"invoice.budget\", \"invoice.vat\", \"invoice.date_issued\", \"invoice.last_modified\", \"invoice.date_created\", \"contact.id\", \"contact.fk_orbiuser_orbiuser\", \"contact.first_name\", \"contact.last_name\", \"contact.company_name\", \"contact.phone\", \"contact.fax\", \"contact.mobile\", \"contact.email\", \"contact.address\", \"contact.postal_code\", \"contact.city\", \"contact.country\", \"contact.website\", \"contact.comments\", \"contact.vat_number\", \"contact.vat_office\", \"contact.vat_category\", \"contact.client_type\", \"contact.profession\", \"contact.category\", \"contact.last_modified\", \"contact.date_created\", \"contact.enabled\"]");


        for (Object fld : fieldsArray) {
            allFields.add((String) fld);
        }

        ArrayList<String> constraint1 = new ArrayList<>();
        constraint1.add("contact.email");
        constraint1.add("contact.vat_number");
        constraints.add(constraint1);

        ArrayList<String> constraint2 = new ArrayList<>();
        constraint2.add("orbiuser.username");
        constraint2.add("orbiuser.password");
        constraints.add(constraint2);

        ArrayList<String> constraint3 = new ArrayList<>();
        constraint3.add("orbiuser.username");
        constraint3.add("orbiuser.email");
        constraints.add(constraint3);

//        ArrayList<String> constraint4 = new ArrayList<>();
//        constraint4.add("orbiuser.password");
//        constraint4.add("orbiuser.email");
//        constraints.add(constraint4);

    }

    /*
    //        DistributedDBInitializer initializer = new DistributedDBInitializer();
    //        initializer.clearAll();
    //        initializer.setUpRemoteDatabase();
    //        initializer.setUpLocalDatabase();
    //        initializer.createIndexServers();
    //        System.out.println("------------- Fragmentation-------------------");
    //        ((DistributedDBInitializer) initializer).performFragmentation2();
     */

}//EoC
