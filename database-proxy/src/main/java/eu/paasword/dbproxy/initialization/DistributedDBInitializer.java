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
package eu.paasword.dbproxy.initialization;

import eu.paasword.adapter.openstack.FragServer;
import eu.paasword.dbproxy.jdbc.JDBCInterface;
import eu.paasword.dbproxy.helper.AdapterHelper;
import eu.paasword.dbproxy.impl.Adapter;
import eu.paasword.dbproxy.utils.DistributedIndexTablesServer;
import eu.paasword.dbproxy.utils.DistributedTablesConfiguration;
import eu.paasword.dbproxy.utils.IDistributedServer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by valentin on 09.07.16.
 */
public class DistributedDBInitializer extends DefaultDBInitializer {

    private static final Logger logger = Logger.getLogger(DefaultDBInitializer.class.getName());

    public static final String CREATE_TABLE_COLUMN_SERVER_MAPPING = "CREATE TABLE \"columnServerMapping\"(id integer NOT NULL, server_id integer NOT NULL, server_name text NOT NULL, CONSTRAINT \"fieldID_pkey\" PRIMARY KEY (id), CONSTRAINT \"serverColumn_fkey\" FOREIGN KEY (id) REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION)WITH (OIDS=FALSE);";
    private static final String DROP_TABLES = "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres; GRANT ALL ON SCHEMA public TO public;"; //DROP DATABASE IF EXISTS %s;";

    private DatabaseCreator dbmanager;
    
    public DistributedDBInitializer(List<FragServer> fragments,String deploymentinstanceid) {
        this(deploymentinstanceid);
        //load the proxy server representations
        dbmanager = new DatabaseCreator(fragments.size() - 2);  //Amount of indexes are the fragments minus the local and the remote      
    }//EoM

    /**
     * Creates an instance of this class.
     *
     * @param configFile the config file defines the indexing type as well as
     * necessary data for the jdbc connections.
     */
    public DistributedDBInitializer(String configFile) {
        super(configFile);
        // Call the initializations so that the (static) data is available afterwards if the methods on this class are called.
        //DBData.init();
        //DatabaseCreator.init();
    }

    @Override
    public void setUpRemoteDatabase(String sessionid) {
        super.setUpRemoteDatabase(sessionid);
        setUpRemoteWithManualIndexDistribution();
    }

    /**
     * This method is extended for the distributed approach to simulate multiple
     * servers on client side. A server will be represented as own database
     * besides the local data base. This approach is only due to
     * demo/prototyping purpose because only one jdbc connetion to the postgres
     * instance of the local machine is used.
     */
    @Override
    public void setUpLocalDatabase(String sessionid) {
        super.setUpLocalDatabase(sessionid);
    }

    @Override
    public void createDatabaseEntries(String sessionid) {
        initializeDistribution(sessionid);
        Adapter adapter = AdapterHelper.getAdapter(deploymentinstanceid,null,sessionid);
        try {
            for (String table : DBData.getCreateTableStatements().keySet()) {
                adapter.query(DBData.getCreateTableStatements().get(table),sessionid);
            }
            //
            insertDataIntoDatabase(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//EoM

    private void initializeDistribution(String sessionid) {
        Adapter adapter = AdapterHelper.getAdapter(deploymentinstanceid,null,sessionid);
        Map<IDistributedServer, Map<String, List<String>>> serverMapping = new HashMap<>();
        int serverNumber = 0;

        try {
            for (String table : DBData.getCreateTableStatements().keySet()) {

                List<String> colNames = adapter.getColumnNames(DBData.getCreateTableStatements().get(table));
                FakeServerAsDatabase server = new ArrayList<FakeServerAsDatabase>(dbmanager.getServers().values()).get(serverNumber);
                IDistributedServer distributedServer = new DistributedIndexTablesServer(serverNumber, server.getName());

                Map<String, List<String>> tableColumnMapForThisServer = new HashMap<>();
                tableColumnMapForThisServer.put(table, colNames);
                serverMapping.put(distributedServer, tableColumnMapForThisServer);

                serverNumber++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Apply the Fragmentation
        DistributedTablesConfiguration.getInstance().setServerTableMapping(serverMapping);
    }//EoM

    public void performFragmentation(String sessionid) {
        Adapter adapter = AdapterHelper.getAdapter(deploymentinstanceid,null,sessionid);

        Map<IDistributedServer, Map<String, List<String>>> serverMapping = new HashMap<>();
        int serverNumber = 0;

        try {
            for (String table : DBData.getCreateTableStatements().keySet()) {

                System.out.println("table: " + table);
                List<String> colNames = adapter.getColumnNames(DBData.getCreateTableStatements().get(table));
                System.out.println("colNames: " + colNames.toString());

                FakeServerAsDatabase server = new ArrayList<FakeServerAsDatabase>(dbmanager.getServers().values()).get(serverNumber);
                IDistributedServer distributedServer = new DistributedIndexTablesServer(serverNumber, server.getName());

                Map<String, List<String>> tableColumnMapForThisServer = new HashMap<>();
                tableColumnMapForThisServer.put(table, colNames);
                serverMapping.put(distributedServer, tableColumnMapForThisServer);

                serverNumber++;

            }//for

            // Apply the Fragmentation
            DistributedTablesConfiguration.getInstance().setServerTableMapping(serverMapping);

            //Create tables
            for (String table : DBData.getCreateTableStatements().keySet()) {
                adapter.query(DBData.getCreateTableStatements().get(table),sessionid);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//EoM

    public void performFragmentation(List<  Map<String, List<String>>> indexservers, List<String> createcommands, String tenantKey, String sessionid) {
        //Step 1 validate adapters
        Adapter adapter = AdapterHelper.getAdapter(deploymentinstanceid, tenantKey,sessionid);

        Map<IDistributedServer, Map<String, List<String>>> serverMapping = new HashMap<>();
        //int serverNumber = 0;
        try {

            for (int i = 0; i < indexservers.size(); i++) {
                FakeServerAsDatabase server = new ArrayList<FakeServerAsDatabase>(dbmanager.getServers().values()).get(i);
                IDistributedServer distributedServer = new DistributedIndexTablesServer(i, server.getName());

                //ApplyFragmentation
                Map<String, List<String>> tableColumnMapForAFragment = indexservers.get(i);
                serverMapping.put(distributedServer, tableColumnMapForAFragment);
            }//for

            // Apply the Fragmentation
            DistributedTablesConfiguration.getInstance().setServerTableMapping(serverMapping);

            //Create tables
            for (String command : createcommands) {
                logger.info("Executing query: " + command);
                adapter.query(command,sessionid);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//EoM

    public void performFragmentation2(String sessionid) {
        //Step 1 validate adapters
        Adapter adapter = AdapterHelper.getAdapter(deploymentinstanceid,null,sessionid);

        Map<IDistributedServer, Map<String, List<String>>> serverMapping = new HashMap<>();
        //int serverNumber = 0;
        try {

            for (int i = 0; i < dbmanager.NUMBER_OF_SERVERS; i++) {
                FakeServerAsDatabase server = new ArrayList<FakeServerAsDatabase>(dbmanager.getServers().values()).get(i);
                IDistributedServer distributedServer = new DistributedIndexTablesServer(i, server.getName());

                //ApplyFragmentation
                Map<String, List<String>> tableColumnMapForAFragment = new HashMap<>();
                if (i == 0) {
                    tableColumnMapForAFragment.put("countries", Arrays.asList("id"));
                } else if (i == 1) {
                    tableColumnMapForAFragment.put("cities", Arrays.asList("id", "name", "fk_country"));
                    tableColumnMapForAFragment.put("countries", Arrays.asList("name"));
                } else if (i == 2) {
                    tableColumnMapForAFragment.put("unis", Arrays.asList("id", "name", "number_of_lecutre_halls", "fk_city"));
                    tableColumnMapForAFragment.put("countries", Arrays.asList("inhabitants"));
                } else if (i == 3) {
                    tableColumnMapForAFragment.put("faculties", Arrays.asList("id", "name"));
                } else if (i == 4) {
                    tableColumnMapForAFragment.put("students", Arrays.asList("id", "name", "surname", "birth_date", "gender", "semester", "grade", "fk_university", "fk_faculty"));
                }

                serverMapping.put(distributedServer, tableColumnMapForAFragment);
            }//for

            // Apply the Fragmentation
            DistributedTablesConfiguration.getInstance().setServerTableMapping(serverMapping);

            //Create tables
            for (String table : DBData.getCreateTableStatements().keySet()) {
                adapter.query(DBData.getCreateTableStatements().get(table),sessionid);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//EoM

    @Override
    protected void clearLocal() {
        super.clearLocal();
    }

    private void setUpRemoteWithManualIndexDistribution() {
        logger.info("setUpRemoteWithManualIndexDistribution() ");
        JDBCInterface test = getJDBCForRemoteDB();
        test.query(CREATE_TABLE_COLUMN_SERVER_MAPPING);
        test.closeConnection();
    }

    /**
     * We simulate different servers using one separated data base for each
     * server. These are created in this method using getJDBCForLocalDB (e.g.
     * they exist on one client)
     */
    private void createServers() {
        logger.info("createServers() ");
        JDBCInterface localDB = getJDBCForLocalDB();
        for (FakeServerAsDatabase server : dbmanager.getServers().values()) {
            localDB.query(server.getCreateDatabaseStatement());
            JDBCInterface serverDB = getJDBCForLocalDB(server.getName());
            for (String createIndexTables : dbmanager.getDistributedRemoteIndexTablesCreateStatements()) {
                serverDB.query(createIndexTables);
            }
//            serverDB.query(server.getCreateTableStatements());
            serverDB.closeConnection();
        }
        localDB.closeConnection();
    }//EoM

    public void createIndexServers() {
        logger.info("createIndexServers() ");
        JDBCInterface remoteindex = null;
        int index = 0;
        for (FakeServerAsDatabase server : dbmanager.getServers().values()) {
            remoteindex = getJDBCForIndexDB(index);
            for (String createIndexTables : dbmanager.getDistributedRemoteIndexTablesCreateStatements()) {
                remoteindex.query(createIndexTables);
            }
//            serverDB.query(server.getCreateTableStatements());
            remoteindex.closeConnection();
            index++;
        }//for
    }//EoM

    @Override
    public void clearAll(String sessionid) {
        super.clearAll(sessionid);
        clearIndexServers();
    }

    public void clearIndexServers() {
        logger.info("clearIndexServers() ");
        JDBCInterface remoteindex = null;
        int index = 0;
        for (FakeServerAsDatabase server : dbmanager.getServers().values()) {       //TODO resolve bug that returns 2 indexes instead of many
            logger.info("Clear indexing server: " + index);
            remoteindex = getJDBCForIndexDB(index);
            remoteindex.query(DROP_TABLES);
            remoteindex.closeConnection();
            index++;
        }//for
    }//EoM

    /**
     * We simulate different servers using one separated data base for each
     * server. These are deleted in this method using getJDBCForLocalDB (e.g.
     * they exist on one client)
     */
    private void deleteServers() {
        System.out.println("deleteServers() ");
        JDBCInterface localDB = getBasicConnectionToDBMS();
        for (FakeServerAsDatabase server : dbmanager.getServers().values()) {
            localDB.query(server.getDropDatabaseStatement());
        }
        localDB.closeConnection();
    }//EoM deleteServers

}//EoC
