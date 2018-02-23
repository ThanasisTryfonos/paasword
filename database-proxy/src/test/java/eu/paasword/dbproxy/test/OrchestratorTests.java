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
import eu.paasword.dbproxy.DBProxyOrchestrator;
import eu.paasword.dbproxy.exceptions.IaaSNotAvailableOrchestrationException;
import eu.paasword.jpa.exceptions.CyclicDependencyException;
import eu.paasword.jpa.exceptions.NoClassToProcessException;
import eu.paasword.jpa.exceptions.NotAValidPaaSwordEntityException;
import eu.paasword.jpa.exceptions.UnSatisfiedDependencyException;
import eu.paasword.dbproxy.model.City;
import eu.paasword.dbproxy.model.Country;
import eu.paasword.dbproxy.model.Faculty;
import eu.paasword.dbproxy.model.Student;
import eu.paasword.dbproxy.model.University;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class OrchestratorTests {
    
    public static void main(String[] args) throws IaaSNotAvailableOrchestrationException, CyclicDependencyException, NotAValidPaaSwordEntityException, NoClassToProcessException, UnSatisfiedDependencyException, InterruptedException {

        //Argument 1 - Define PaaSword entity classes
        List<IaaS> iaasresources = new ArrayList<>();

        IaaS iaas1 = new IaaS("1", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");
        IaaS iaas2 = new IaaS("2", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");
        IaaS iaas3 = new IaaS("3", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");
        IaaS iaas4 = new IaaS("4", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");
        IaaS iaas5 = new IaaS("5", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");
        IaaS iaas6 = new IaaS("6", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");
        IaaS iaas7 = new IaaS("7", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");
        IaaS iaas8 = new IaaS("8", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");
        IaaS iaas9 = new IaaS("9", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");
        IaaS iaas10 = new IaaS("10", "http://147.102.23.40:5000/v3/", "admin", "!1q2w3e!", "default", "paasword", "d7f259cf-ee03-40ea-bd6b-687e66dc305f","2a24c559-2c9e-4d0b-b636-9920d193c0b3", "bf5894b2-d42a-4ab5-8b20-0c688e5d6a6d");

        //add them to the list
        iaasresources.add(iaas1);
        iaasresources.add(iaas2);
        iaasresources.add(iaas3);
        iaasresources.add(iaas4);
        iaasresources.add(iaas5);
        iaasresources.add(iaas6);
        iaasresources.add(iaas7);
        iaasresources.add(iaas8);
        iaasresources.add(iaas9);
        iaasresources.add(iaas10);
//        iaasresources.add(iaas11);

        //Argument 2 - Define PaaSword entity classes
        ArrayList<Class> daoclasses = new ArrayList<>();
        daoclasses.add(Country.class);
        daoclasses.add(City.class);
        daoclasses.add(Faculty.class);
        daoclasses.add(Student.class);
        daoclasses.add(University.class);        

        //Argument 3 - Define Constraints
        ArrayList constraints = new ArrayList<>();
        ArrayList<String> c1 = new ArrayList<>();
        ArrayList<String> c2 = new ArrayList<>();
        ArrayList<String> c3 = new ArrayList<>();
        ArrayList<String> c4 = new ArrayList<>();
        ArrayList<String> c5 = new ArrayList<>();
//        ArrayList<String> c6 = new ArrayList<>();
        c1.add("country.id");
        c1.add("country.name");

        c2.add("city.id");
        c2.add("country.id");

        c3.add("student.name");
        c3.add("student.grade");
        c4.add("student.name");
        c4.add("student.semester");
        c5.add("student.semester");
        c5.add("student.grade");
//        c6.add("city.id");
//        c6.add("faculty.id");

        constraints.add(c1);
        constraints.add(c2);
        constraints.add(c3);   //with this extra constraint
//        constraints.add(c4);
//        constraints.add(c5);
//        constraints.add(c6);

        String randomkey = new Random().nextInt() + "";
        String deploymentinstanceid = randomkey.substring(2, randomkey.length() - 2);                       
        DBProxyOrchestrator.orchestrateDeployment(deploymentinstanceid,null, iaasresources, daoclasses, constraints);

    }//EoMain
    
    
}
