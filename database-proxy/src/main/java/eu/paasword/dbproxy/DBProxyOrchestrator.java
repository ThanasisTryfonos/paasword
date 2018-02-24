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
import eu.paasword.adapter.openstack.IaaS;
import eu.paasword.adapter.openstack.OpenStackAdapter;
import eu.paasword.dbproxy.jdbc.JDBCInterface;
import eu.paasword.dbproxy.fragmentation.FragmentationUtil;
import eu.paasword.dbproxy.helper.AdapterHelper;
import eu.paasword.dbproxy.initialization.DistributedDBInitializer;
import eu.paasword.dbproxy.transaction.DistributedTransactionalManager;
import eu.paasword.jpa.PaaSwordQueryHandler;
import eu.paasword.jpa.exceptions.CyclicDependencyException;
import eu.paasword.jpa.exceptions.NoClassToProcessException;
import eu.paasword.jpa.exceptions.NotAValidPaaSwordEntityException;
import eu.paasword.jpa.exceptions.UnSatisfiedDependencyException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;

/**
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class DBProxyOrchestrator {

    private static final Logger logger = Logger.getLogger(DBProxyOrchestrator.class.getName());
    private static final String CONFIG_FILEPATH = System.getProperty("user.home") + File.separator;
    private static final int TIMEOUTOUTMILLISPERSERVER = 15000;

//    private static APIKeyRepository apiKeyService;
//    private static ApplicationInstanceRepository applicationInstanceService;
    public static DBProxyOrchestratorResponse orchestrateDeployment(String deploymentinstanceid, String tenantKey, List<IaaS> iaasresources, List<Class> daoclasses, ArrayList<ArrayList<String>> constraints) {

        DBProxyOrchestratorResponse orchestrationresponse = new DBProxyOrchestratorResponse();
        try {

            //Step 1 - check connectivity with all the IaaSes (in parallel for optimization)
            ExecutorService executor = Executors.newWorkStealingPool();
            List<Callable<Boolean>> connectioncallables = new ArrayList<>();

            for (IaaS iaasresource : iaasresources) {
                connectioncallables.add(() -> {
                    return OpenStackAdapter.testConnectionV3(iaasresource);
                });
            }//for

            try {
                executor.invokeAll(connectioncallables)
                        .stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .forEach(System.out::println);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                //TODO add throw
            }

            //Step 2 - Validate that classes correspond to a VALID Direct Acyclic Graph
            try {
                List<String> createtablecommands = PaaSwordQueryHandler.generateOrderedCreateTableStatementsForManyClasses(daoclasses);
                for (String command : createtablecommands) {
                    logger.info("Command: " + command);
                }
            } catch (CyclicDependencyException | NotAValidPaaSwordEntityException | NoClassToProcessException | UnSatisfiedDependencyException ex) {
                logger.log(Level.SEVERE, null, ex);
                //TODO add throwable
            }

            //Step 3 - Generate Input For Fragmentation
            List<String> allfields = new ArrayList();
            try {
                allfields = PaaSwordQueryHandler.generateFieldsForManyClasses(daoclasses);
                for (String field : allfields) {
                    logger.info(field);
                }
            } catch (NotAValidPaaSwordEntityException ex) {
                logger.log(Level.SEVERE, null, ex);
            }

            //Step 4 - Generate Fragmentation based on Constraints
            FragmentationUtil noServerLimit = new FragmentationUtil(allfields, constraints);
            String fragmentation = noServerLimit.fragment();
            logger.info("Fragmentation: \n" + fragmentation);

            JSONParser jsonParser = new JSONParser();

            List<Map<String, List<String>>> indexservers = new ArrayList<>();
            try {
                JSONArray jsonarray = (JSONArray) jsonParser.parse(fragmentation);
                int numberoffragments = jsonarray.size();
                //parse
                for (int i = 0; i < numberoffragments; i++) {
                    JSONArray fragarray = (JSONArray) jsonarray.get(i);
                    Map<String, List<String>> tableColumnMapForAFragment = new HashMap<>();
                    //process fragments
                    for (int j = 0; j < fragarray.size(); j++) {
                        String fragment = (String) fragarray.get(j);
                        logger.info("Fragment: " + fragment);
                        String[] strs = fragment.split("\\.");
                        String table = strs[0];
                        String column = strs[1];
                        //add it to the map. if the table does not exist create it. If it does append new item in the list
                        tableColumnMapForAFragment.put(table, tableColumnMapForAFragment.get(table) == null ? new ArrayList(Arrays.asList(column)) : (tableColumnMapForAFragment.get(table).contains(column) ? tableColumnMapForAFragment.get(table) : appendToList(tableColumnMapForAFragment.get(table), column)));
                        logger.info("Fragment added: " + fragment);
                    }//for

                    //add it to the index servers
                    indexservers.add(tableColumnMapForAFragment);
                }//for
            } catch (ParseException ex) {
                Logger.getLogger(DBProxyOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Step 5 Infer amount of DBs and generate AdapterProxy configuration
            logger.info("The amount of index servers that will be used are: " + indexservers.size());
            logger.info("Total amount of servers: " + (indexservers.size() + 2));
            int amountofservers = (indexservers.size() + 2);
            List<FragServer> fragservers = new ArrayList<>();
            for (int i = 0; i < amountofservers; i++) {
                FragServer fragserver;
                if (i == 0) {                               //REMOTE
                    fragserver = new FragServer(FragServer.DBType.remote, "127.0.0.1", "kit_mimosecco_remote", "postgres", "postgres", FragServer.SchemePlace.remote, FragServer.SchemeType.database, deploymentinstanceid);
                } else if (i == (amountofservers - 1)) {    //LOCAL
                    fragserver = new FragServer(FragServer.DBType.local, "127.0.0.1", "kit_mimosecco_local", "postgres", "postgres", FragServer.SchemePlace.local, FragServer.SchemeType.database, deploymentinstanceid);
                } else {            //INDEX SERVERS
                    fragserver = new FragServer(FragServer.DBType.remote_index, "127.0.0.1", "kit_server_" + (i - 1), "postgres", "postgres", FragServer.SchemePlace.remote, FragServer.SchemeType.database, deploymentinstanceid);
                }
                //add it to the list
                fragservers.add(fragserver);
            }//for

            //Step 6 - Choose placement IaaS (Distribute Fragservers to IaaSes) using a round robin algorithm
            int iaascounter = 0;
            int totaliaases = iaasresources.size();
            for (FragServer fragserver : fragservers) {
                int placement = (iaascounter % totaliaases) + 1;
                fragserver.setIaas(iaasresources.get(placement - 1));
                logger.info(fragserver.getName() + " will be placed on " + placement);
                iaascounter++;
            }//debug for

            //Step 7 - Boot the databases in the IaaSes and update Fragservers with the public IP
            long startTime = System.currentTimeMillis();

            List<Callable<String>> iaascallables = new ArrayList<>();
            //prepare parallel execution
            for (FragServer fragserver : fragservers) {
                iaascallables.add(() -> {               //TODO pass the arguments correctly from fragments
                    String serverid = OpenStackAdapter.createNewInstanceV3WithoutFloating(fragserver);
                    fragserver.setServerid(serverid);
                    return serverid;
                });
            }

            //Boot VM
            try {
                executor.invokeAll(iaascallables)
                        .stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .forEach(System.out::println);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                //TODO add throw
            }

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            logger.info("IaaS creation time: " + elapsedTime);

            //Step 7 - Assign Sequentially Floating IPs (SOS not parallel)
            for (FragServer fragserver : fragservers) {
                String floating = OpenStackAdapter.assignFloatingToServer(fragserver.getServerid(), fragserver.getIaas());
                fragserver.setHost(floating);
            }

            //     Step 8 - Generate DB-Proxy  configuration file
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\"?>\n"
                    + "<?xml-stylesheet type=\"text/xsl\" href=\"xml2yaml.xsl\"?>\n"
                    + "<databases>\n");
            for (FragServer fragserver : fragservers) {
                sb.append(fragserver.toString());
            }
            sb.append("    <wibustick>\n"
                    + "        <number>1</number>\n"
                    + "        <hcmse>0</hcmse>\n"
                    + "        <firmcode>10</firmcode>\n"
                    + "        <productcode>1608</productcode>\n"
                    + "        <featurecode>1</featurecode>\n"
                    + "    </wibustick>\n"
                    + "    <deamon>\n"
                    + "        <port>1338</port>\n"
                    + "        <poolsize>5</poolsize>\n"
                    + "    </deamon>\n"
                    + "    <index>\n"
                    + "        <indextype>manualDistribution</indextype>\n"
                    + "        <indexhashkey>ABCDEFGHIJKLMNOP</indexhashkey>\n"
                    + "        <bucketsize>10</bucketsize>\n"
                    + "    </index>\n"
                    + "    <global>\n"
                    + "        <logging>true</logging>\n"
                    + "    </global>\n"
                    + "</databases>");

            logger.info(sb.toString());

            FileWriter fw;
            try {
//            fw = new FileWriter(CONFIG_FILE, false);
//            fw.write(sb.toString());
//            fw.close();
                fw = new FileWriter(getConfigurationFile(deploymentinstanceid), false);
                fw.write(sb.toString());
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(DBProxyOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Step 9 - DBProxy initialization
            List<String> createcommands = PaaSwordQueryHandler.generateOrderedCreateTableStatementsForManyClasses(daoclasses);
            for (String command : createcommands) {
                logger.info("Command: " + command);
            }

            //Step 10 - Gracefull sleep
            logger.info("Sleeping in order for db to be ready");                                    //sleep 5 seconds in order for the database tp be ready    
            //Thread.currentThread().sleep(amountofservers * TIMEOUTOUTMILLISPERSERVER);            //TODO substitute that with check business logic   
            List<Callable<Boolean>> sleepcallables = new ArrayList<>();
            //prepare parallel execution
            for (FragServer fragserver : fragservers) {
                sleepcallables.add(() -> {               //TODO pass the arguments correctly from fragments
                    boolean finished = false;
                    while (!finished) {
                        JDBCInterface dbms = null;
                        try {
                            dbms = new JDBCInterface("Postgre", "jdbc:postgresql" + "://" + fragserver.getHost() + "/" + fragserver.getName(), fragserver.getUser(), fragserver.getPassword());
                            dbms.query("select 1;");
                            finished = true;
                            logger.info("Success: Remote database ready " + fragserver.getName());
                        } catch (Exception ex) {
                            logger.severe("Error: Remote database not ready yet " + fragserver.getName());
                            Thread.currentThread().sleep(5000);
                        } finally {
                            //dbms.closeConnection();
                        }
                    }//while
                    return finished;
                });
            }

            //Check sleeping state
            try {
                executor.invokeAll(sleepcallables)
                        .stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .forEach(System.out::println);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                //TODO add throw
            }

            logger.info("Sleep is over");                                                           //sleep 5 seconds in order for the database tp be ready                

            //Step 11 - DB initialization
            try {
                DistributedTransactionalManager dtm = AdapterHelper.getDTMByAdapterId(deploymentinstanceid);
                String sessionid = dtm.initiateTransaction();

                DistributedDBInitializer initializer = new DistributedDBInitializer(fragservers, deploymentinstanceid);
                initializer.clearAll(sessionid);
                initializer.setUpRemoteDatabase(sessionid);
                initializer.setUpLocalDatabase(sessionid);
                initializer.createIndexServers();
                logger.info("Performing Fragmentation....and schema initialization");
                ((DistributedDBInitializer) initializer).performFragmentation(indexservers, createcommands, tenantKey, sessionid);
                //commit
                dtm.commitTransaction(sessionid);

                //Create Reponse
                orchestrationresponse.setSuccessresult(true);
                orchestrationresponse.setFragservers(fragservers);
                orchestrationresponse.setConfigurationxml(sb.toString());

            } catch (Exception ex) {
                logger.severe("Error during Orchrstration of Fragmentation");
                ex.printStackTrace();
            }

        } catch (CyclicDependencyException | NotAValidPaaSwordEntityException | NoClassToProcessException | UnSatisfiedDependencyException ex) {
            logger.severe("Exception during DBProxy bootstrapping");
            orchestrationresponse.setSuccessresult(false);
            orchestrationresponse.setResultstatus(ex.getMessage());
        }
        return orchestrationresponse;
    }//EoM

    public static String getConfigurationFile(String deploymentinstanceid) {

        try {

            File file = new File(CONFIG_FILEPATH + deploymentinstanceid + ".xml");

            if (file.exists()) {
                logger.info("File already exists.");
//                file.delete();
//                file.createNewFile();
                logger.info("New file is created!");
            } else {
                file.createNewFile();
                logger.info("File is created!");

            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return CONFIG_FILEPATH + deploymentinstanceid + ".xml";

    }

    private static List<String> appendToList(List<String> list, String element) {
        list.add(element);
        return list;
    }//EoM

    public static boolean testConnection(String connectionURL, String username, String password, String domainstr, String projectstr) {
        logger.info("Testing OpenStack connection...");
        boolean isSuccess = false;
        try {
            Identifier domain = Identifier.byName(domainstr);
            Identifier project = Identifier.byName(projectstr);

            OSClientV3 os = OSFactory.builderV3()
                    .endpoint(connectionURL)
                    .credentials(username, password, domain).scopeToProject(project, domain).authenticate();

            os.compute().flavors().list();

        } catch (AuthenticationException e) {
            logger.severe(e.getMessage());
        }
        return isSuccess;
    }//EoM

    public static DBProxyOrchestratorResponse orchestrateSlipStreamDeployment(String deploymentinstanceid, String tenantKey, org.json.JSONArray iaasresources, List<String> createtablecommands, List<String> allfields, ArrayList<ArrayList<String>> constraints) {

        logger.info("DeploymentID: " + deploymentinstanceid);

        DBProxyOrchestratorResponse orchestrationresponse = new DBProxyOrchestratorResponse();
        try {

            ExecutorService executor = Executors.newWorkStealingPool();

            //Step 1 - Generate Fragmentation based on Constraints
            FragmentationUtil noServerLimit = new FragmentationUtil(allfields, constraints);
            String fragmentation = noServerLimit.fragment();
            logger.info("Fragmentation: \n" + fragmentation);

            JSONParser jsonParser = new JSONParser();

            List<Map<String, List<String>>> indexservers = new ArrayList<>();
            try {
                JSONArray jsonarray = (JSONArray) jsonParser.parse(fragmentation);
                int numberoffragments = jsonarray.size();
                //parse
                for (int i = 0; i < numberoffragments; i++) {
                    JSONArray fragarray = (JSONArray) jsonarray.get(i);
                    Map<String, List<String>> tableColumnMapForAFragment = new HashMap<>();
                    //process fragments
                    for (int j = 0; j < fragarray.size(); j++) {
                        String fragment = (String) fragarray.get(j);
                        logger.info("Fragment: " + fragment);
                        String[] strs = fragment.split("\\.");
                        String table = strs[0];
                        String column = strs[1];
                        //add it to the map. if the table does not exist create it. If it does append new item in the list
                        tableColumnMapForAFragment.put(table, tableColumnMapForAFragment.get(table) == null ? new ArrayList(Arrays.asList(column)) : (tableColumnMapForAFragment.get(table).contains(column) ? tableColumnMapForAFragment.get(table) : appendToList(tableColumnMapForAFragment.get(table), column)));
                        logger.info("Fragment added: " + fragment);
                    }//for

                    //add it to the index servers
                    indexservers.add(tableColumnMapForAFragment);
                }//for
            } catch (ParseException ex) {
                Logger.getLogger(DBProxyOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Step 5 Infer amount of DBs and generate AdapterProxy configuration
            logger.info("The amount of index servers that will be used are: " + indexservers.size());
            logger.info("Total amount of servers: " + (indexservers.size() + 2));
            int amountofservers = (indexservers.size() + 2);

            List<FragServer> fragservers = new ArrayList<>();

            for (Object fragServerObj : iaasresources) {

                JSONObject fragServerJSON = (JSONObject) fragServerObj;

                String dbName = fragServerJSON.getString("dbName");
                String dbPort = fragServerJSON.getString("dbPort");
                String dbHost = fragServerJSON.getString("dbHost");

                FragServer fragServer;

                if (dbName.equals("kit_mimosecco_remote")) {
                    fragServer = new FragServer(FragServer.DBType.remote, dbHost, "kit_mimosecco_remote", "postgres", "postgres", FragServer.SchemePlace.remote, FragServer.SchemeType.database, deploymentinstanceid);
                } else if (dbName.equals("kit_mimosecco_local")) {
                    fragServer = new FragServer(FragServer.DBType.local, dbHost, "kit_mimosecco_local", "postgres", "postgres", FragServer.SchemePlace.local, FragServer.SchemeType.database, deploymentinstanceid);
                } else {
                    fragServer = new FragServer(FragServer.DBType.remote_index, dbHost, dbName, "postgres", "postgres", FragServer.SchemePlace.remote, FragServer.SchemeType.database, deploymentinstanceid);
                }

                fragservers.add(fragServer);

            }

            //     Step 8 - Generate DB-Proxy  configuration file
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\"?>\n"
                    + "<?xml-stylesheet type=\"text/xsl\" href=\"xml2yaml.xsl\"?>\n"
                    + "<databases>\n");
            for (FragServer fragserver : fragservers) {
                sb.append(fragserver.toString());
            }
            sb.append("    <wibustick>\n"
                    + "        <number>1</number>\n"
                    + "        <hcmse>0</hcmse>\n"
                    + "        <firmcode>10</firmcode>\n"
                    + "        <productcode>1608</productcode>\n"
                    + "        <featurecode>1</featurecode>\n"
                    + "    </wibustick>\n"
                    + "    <deamon>\n"
                    + "        <port>1338</port>\n"
                    + "        <poolsize>5</poolsize>\n"
                    + "    </deamon>\n"
                    + "    <index>\n"
                    + "        <indextype>manualDistribution</indextype>\n"
                    + "        <indexhashkey>ABCDEFGHIJKLMNOP</indexhashkey>\n"
                    + "        <bucketsize>10</bucketsize>\n"
                    + "    </index>\n"
                    + "    <global>\n"
                    + "        <logging>true</logging>\n"
                    + "    </global>\n"
                    + "</databases>");

            logger.info(sb.toString());

            FileWriter fw;
            try {
//            fw = new FileWriter(CONFIG_FILE, false);
//            fw.write(sb.toString());
//            fw.close();
                fw = new FileWriter(getConfigurationFile(deploymentinstanceid), false);
                fw.write(sb.toString());
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(DBProxyOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Step 10 - Gracefull sleep
            logger.info("Sleeping in order for db to be ready");                                    //sleep 5 seconds in order for the database tp be ready
            //Thread.currentThread().sleep(amountofservers * TIMEOUTOUTMILLISPERSERVER);            //TODO substitute that with check business logic
            List<Callable<Boolean>> sleepcallables = new ArrayList<>();
            //prepare parallel execution
            for (FragServer fragserver : fragservers) {
                sleepcallables.add(() -> {               //TODO pass the arguments correctly from fragments
                    boolean finished = false;
                    while (!finished) {
                        JDBCInterface dbms = null;
                        try {
                            dbms = new JDBCInterface("Postgres", "jdbc:postgresql" + "://" + fragserver.getHost() + "/" + fragserver.getName(), fragserver.getUser(), fragserver.getPassword());
                            dbms.query("select 1;");
                            finished = true;
                            logger.info("Success: Remote database ready " + fragserver.getName());
                        } catch (Exception ex) {
                            logger.severe("Error: Remote database not ready yet " + fragserver.getName());
                            Thread.currentThread().sleep(5000);
                        } finally {
                            //dbms.closeConnection();
                        }
                    }//while
                    return finished;
                });
            }

            //Check sleeping state
            try {
                executor.invokeAll(sleepcallables)
                        .stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .forEach(System.out::println);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                //TODO add throw
            }

            //Step 11 - Initialize Database
            try {
                DistributedTransactionalManager dtm = AdapterHelper.getDTMByAdapterId(deploymentinstanceid);
                String sessionid = dtm.initiateTransaction();

                DistributedDBInitializer initializer = new DistributedDBInitializer(fragservers, deploymentinstanceid);
                initializer.clearAll(sessionid);
                initializer.setUpRemoteDatabase(sessionid);
                initializer.setUpLocalDatabase(sessionid);
                initializer.createIndexServers();
                logger.info("Performing Fragmentation....and schema initialization");
                ((DistributedDBInitializer) initializer).performFragmentation(indexservers, createtablecommands, tenantKey, sessionid);

                //commit
                dtm.commitTransaction(sessionid);

                //Create Response
                orchestrationresponse.setSuccessresult(true);
                orchestrationresponse.setFragservers(fragservers);
                orchestrationresponse.setConfigurationxml(sb.toString());

            } catch (Exception ex) {
                logger.severe("Error during parseDBConfig");
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe("Exception during DBProxy bootstrapping");
            orchestrationresponse.setSuccessresult(false);
            orchestrationresponse.setResultstatus(ex.getMessage());
        }
        return orchestrationresponse;
    }//EoM

    public static DBProxyOrchestratorResponse orchestrateDeployment(String deploymentinstanceid, String tenantKey, List<IaaS> iaasresources, List<String> createtablecommands, List<String> allfields, ArrayList<ArrayList<String>> constraints) {

        DBProxyOrchestratorResponse orchestrationresponse = new DBProxyOrchestratorResponse();
        try {
            //Step 1 - check connectivity with all the IaaSes (in parallel for optimization)
            ExecutorService executor = Executors.newWorkStealingPool();
            List<Callable<Boolean>> connectioncallables = new ArrayList<>();

            for (IaaS iaasresource : iaasresources) {
                connectioncallables.add(() -> {
                    return OpenStackAdapter.testConnectionV3(iaasresource);
                });
            }//for

            try {
                executor.invokeAll(connectioncallables)
                        .stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .forEach(System.out::println);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                //TODO add throw
            }

            //Step 2 - Validate that classes correspond to a VALID Direct Acyclic Graph
//            try {
//                List<String> createtablecommands = PaaSwordQueryHandler.generateOrderedCreateTableStatementsForManyClasses(daoclasses);
//                for (String command : createtablecommands) {
//                    logger.info("Command: " + command);
//                }
//            } catch (CyclicDependencyException | NotAValidPaaSwordEntityException | NoClassToProcessException | UnSatisfiedDependencyException ex) {
//                logger.log(Level.SEVERE, null, ex);
//                //TODO add throwable
//            }
            //Step 3 - Generate Input For Fragmentation
//            List<String> allfields = new ArrayList();
//            try {
//                allfields = PaaSwordQueryHandler.generateFieldsForManyClasses(daoclasses);
//                for (String field : allfields) {
//                    logger.info(field);
//                }
//            } catch (NotAValidPaaSwordEntityException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }
            //Step 4 - Generate Fragmentation based on Constraints
            FragmentationUtil noServerLimit = new FragmentationUtil(allfields, constraints);
//            logger.info("All Fields: " + allfields.size());
//            logger.info("Constraints: " + constraints.size());
            String fragmentation = noServerLimit.fragment();

            logger.info("Fragmentation: \n" + fragmentation);

            JSONParser jsonParser = new JSONParser();

            List<Map<String, List<String>>> indexservers = new ArrayList<>();
            try {
                JSONArray jsonarray = (JSONArray) jsonParser.parse(fragmentation);
                int numberoffragments = jsonarray.size();
                //parse
                for (int i = 0; i < numberoffragments; i++) {
                    JSONArray fragarray = (JSONArray) jsonarray.get(i);
                    Map<String, List<String>> tableColumnMapForAFragment = new HashMap<>();
                    //process fragments
                    for (int j = 0; j < fragarray.size(); j++) {
                        String fragment = (String) fragarray.get(j);
//                        logger.info("Fragment: " + fragment);
                        String[] strs = fragment.split("\\.");
                        String table = strs[0];
                        String column = strs[1];
                        //add it to the map. if the table does not exist create it. If it does append new item in the list
                        tableColumnMapForAFragment.put(table, tableColumnMapForAFragment.get(table) == null ? new ArrayList(Arrays.asList(column)) : (tableColumnMapForAFragment.get(table).contains(column) ? tableColumnMapForAFragment.get(table) : appendToList(tableColumnMapForAFragment.get(table), column)));
//                        logger.info("Fragment added: " + fragment);
                    }//for

                    //add it to the index servers
                    indexservers.add(tableColumnMapForAFragment);
                }//for
            } catch (ParseException ex) {
                Logger.getLogger(DBProxyOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Step 5 Infer amount of DBs and generate AdapterProxy configuration
            logger.info("The amount of index servers that will be used are: " + indexservers.size());
            logger.info("Total amount of servers: " + (indexservers.size() + 2));
            int amountofservers = (indexservers.size() + 2);
            List<FragServer> fragservers = new ArrayList<>();
            for (int i = 0; i < amountofservers; i++) {
                FragServer fragserver;
                if (i == 0) {                               //REMOTE
                    fragserver = new FragServer(FragServer.DBType.remote, "127.0.0.1", "kit_mimosecco_remote", "postgres", "postgres", FragServer.SchemePlace.remote, FragServer.SchemeType.database, deploymentinstanceid);
                } else if (i == (amountofservers - 1)) {    //LOCAL
                    fragserver = new FragServer(FragServer.DBType.local, "127.0.0.1", "kit_mimosecco_local", "postgres", "postgres", FragServer.SchemePlace.local, FragServer.SchemeType.database, deploymentinstanceid);
                } else {            //INDEX SERVERS
                    fragserver = new FragServer(FragServer.DBType.remote_index, "127.0.0.1", "kit_server_" + (i - 1), "postgres", "postgres", FragServer.SchemePlace.remote, FragServer.SchemeType.database, deploymentinstanceid);
                }
                //add it to the list
                fragservers.add(fragserver);
            }//for

            //Step 6 - Choose placement IaaS (Distribute Fragservers to IaaSes) using a round robin algorithm
            int iaascounter = 0;
            int totaliaases = iaasresources.size();
            for (FragServer fragserver : fragservers) {
                int placement = (iaascounter % totaliaases) + 1;
                fragserver.setIaas(iaasresources.get(placement - 1));
                logger.info(fragserver.getName() + " will be placed on " + placement);
                iaascounter++;
            }//debug for

            //Step 7 - Boot the databases in the IaaSes and update Fragservers with the public IP
            long startTime = System.currentTimeMillis();

            List<Callable<String>> iaascallables = new ArrayList<>();
            //prepare parallel execution
            for (FragServer fragserver : fragservers) {
                iaascallables.add(() -> {               //TODO pass the arguments correctly from fragments
                    String serverid = OpenStackAdapter.createNewInstanceV3WithoutFloating(fragserver);
                    fragserver.setServerid(serverid);
                    return serverid;
                });
            }

            //Boot VM
            try {
                executor.invokeAll(iaascallables)
                        .stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .forEach(System.out::println);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                //TODO add throw
            }

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            logger.info("IaaS creation time: " + elapsedTime);

            //Step 7 - Assign Sequentially Floating IPs (SOS not parallel)
            for (FragServer fragserver : fragservers) {
                String floating = OpenStackAdapter.assignFloatingToServer(fragserver.getServerid(), fragserver.getIaas());
                fragserver.setHost(floating);
            }

//     Step 8 - Generate DB-Proxy  configuration file
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\"?>\n"
                    + "<?xml-stylesheet type=\"text/xsl\" href=\"xml2yaml.xsl\"?>\n"
                    + "<databases>\n");
            for (FragServer fragserver : fragservers) {
                sb.append(fragserver.toString());
            }
            sb.append("    <wibustick>\n"
                    + "        <number>1</number>\n"
                    + "        <hcmse>0</hcmse>\n"
                    + "        <firmcode>10</firmcode>\n"
                    + "        <productcode>1608</productcode>\n"
                    + "        <featurecode>1</featurecode>\n"
                    + "    </wibustick>\n"
                    + "    <deamon>\n"
                    + "        <port>1338</port>\n"
                    + "        <poolsize>5</poolsize>\n"
                    + "    </deamon>\n"
                    + "    <index>\n"
                    + "        <indextype>manualDistribution</indextype>\n"
                    + "        <indexhashkey>ABCDEFGHIJKLMNOP</indexhashkey>\n"
                    + "        <bucketsize>10</bucketsize>\n"
                    + "    </index>\n"
                    + "    <global>\n"
                    + "        <logging>true</logging>\n"
                    + "    </global>\n"
                    + "</databases>");

            logger.info(sb.toString());

            FileWriter fw;
            try {
//            fw = new FileWriter(CONFIG_FILE, false);
//            fw.write(sb.toString());
//            fw.close();
                fw = new FileWriter(getConfigurationFile(deploymentinstanceid), false);
                fw.write(sb.toString());
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(DBProxyOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Step 9 - DBProxy initialization
//            List<String> createcommands = PaaSwordQueryHandler.generateOrderedCreateTableStatementsForManyClasses(daoclasses);
//            for (String command : createcommands) {
//                logger.info("Command: " + command);
//            }
            //Step 10 - Gracefull sleep
            logger.info("Sleeping in order for db to be ready");                                    //sleep 5 seconds in order for the database tp be ready    
            //Thread.currentThread().sleep(amountofservers * TIMEOUTOUTMILLISPERSERVER);            //TODO substitute that with check business logic   
            List<Callable<Boolean>> sleepcallables = new ArrayList<>();
            //prepare parallel execution
            for (FragServer fragserver : fragservers) {
                sleepcallables.add(() -> {               //TODO pass the arguments correctly from fragments
                    boolean finished = false;
                    while (!finished) {
                        JDBCInterface dbms = null;
                        try {
                            dbms = new JDBCInterface("Postgre", "jdbc:postgresql" + "://" + fragserver.getHost() + "/" + fragserver.getName(), fragserver.getUser(), fragserver.getPassword());
                            dbms.query("select 1;");
                            finished = true;
                            logger.info("Success: Remote database ready " + fragserver.getName());
                        } catch (Exception ex) {
                            logger.severe("Error: Remote database not ready yet " + fragserver.getName());
                            Thread.currentThread().sleep(5000);
                        } finally {
                            //dbms.closeConnection();
                        }
                    }//while
                    return finished;
                });
            }

            //Check sleeping state
            try {
                executor.invokeAll(sleepcallables)
                        .stream()
                        .map(future -> {
                            try {
                                return future.get();
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .forEach(System.out::println);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                //TODO add throw
            }

            //Step 11 - Initialize Database
            try {
                DistributedTransactionalManager dtm = AdapterHelper.getDTMByAdapterId(deploymentinstanceid);
                String sessionid = dtm.initiateTransaction();

                DistributedDBInitializer initializer = new DistributedDBInitializer(fragservers, deploymentinstanceid);
                initializer.clearAll(sessionid);
                initializer.setUpRemoteDatabase(sessionid);
                initializer.setUpLocalDatabase(sessionid);
                initializer.createIndexServers();
                logger.info("Performing Fragmentation....and schema initialization");
                ((DistributedDBInitializer) initializer).performFragmentation(indexservers, createtablecommands, tenantKey, sessionid);

                //commit
                dtm.commitTransaction(sessionid);

                //Create Reponse
                orchestrationresponse.setSuccessresult(true);
                orchestrationresponse.setFragservers(fragservers);
                orchestrationresponse.setConfigurationxml(sb.toString());

            } catch (Exception ex) {
                logger.severe("Error during parseDBConfig");
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe("Exception during DBProxy bootstrapping");
            orchestrationresponse.setSuccessresult(false);
            orchestrationresponse.setResultstatus(ex.getMessage());
        }
        return orchestrationresponse;
    }//EoM

    public static boolean DestroyDBProxy(String deploymentinstanceid, List<FragServer> fragservers) {
        boolean successfullundeployment = false;

        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Boolean>> destroycallables = new ArrayList<>();

        for (FragServer fragserver : fragservers) {
            destroycallables.add(() -> {
                return OpenStackAdapter.destroyServer(deploymentinstanceid, fragserver.getIaas().getConnectionURL(), fragserver.getIaas().getUsername(), fragserver.getIaas().getPassword(), fragserver.getIaas().getDomain(), fragserver.getIaas().getProject(), fragserver.getServerid(), fragserver.getName());
            });
        }//for

        try {
            executor.invokeAll(destroycallables)
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .forEach(System.out::println);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
            //TODO add throw
        }

        return successfullundeployment;
    }//EoM

}//EoC
