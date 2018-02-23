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

import eu.paasword.dbproxy.DBProxyOrchestrator;
import eu.paasword.dbproxy.jdbc.JDBCInterface;
import eu.paasword.dbproxy.impl.Adapter;
import eu.paasword.dbproxy.utils.ConfigParser;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Class contains the initialization scripts for local data base.
 *
 * @author Valentin Zipf
 */
public class DefaultDBInitializer implements IDBInitializer {

//    private String dbConfigFile = FragmentationOrchestrator.CONFIG_FILE;
    public String deploymentinstanceid;
    private static final Logger logger = Logger.getLogger(DefaultDBInitializer.class.getName());

//    public DefaultDBInitializer() {
//        //this(ConfigHelper.INSTANCE.getConfigFileFullPath(ConfigHelper.IndexScenario.STANDARD_INDEX));
//        this(FragmentationOrchestrator.CONFIG_FILE);
//    }
    /**
     * Creates an instance of this class.
     *
     */
    public DefaultDBInitializer(String instanceid) {
        this.deploymentinstanceid = instanceid;
        logger.info("Initializing DefaultDBInitializer with instanceid" + deploymentinstanceid);
    }

    public String getDeploymentinstanceid() {
        return deploymentinstanceid;
    }

    public void setDeploymentinstanceid(String deploymentinstanceid) {
        this.deploymentinstanceid = deploymentinstanceid;
    }

    @Override
    public void clearAll() {
        logger.info("clearAll() ");
        clearRemote();
        clearLocal();
    }//EoM

    /**
     * Creates the database scheme for the remote (distributed encryption) data
     * base.
     */
    @Override
    public void setUpRemoteDatabase() {
        logger.info("DefaultDBInitializer--> setUpRemoteDatabase()");
        clearRemote();
        //createRemoteDatabase();       //TODO check if ok. In a multicloud environment the DB should be always there and we never drop it
        createRemoteDatabaseTables();
    }//EoM

    /**
     * Creates the database scheme for the local (application) data base.
     */
    @Override
    public void setUpLocalDatabase() {
        logger.info("DefaultDBInitializer--> setUpLocalDatabase()");
        clearLocal();
        //createLocalDatabase();        //TODO check if ok. In a multicloud environment the DB should be always there and we never drop it
        createLocalDatabaseTables();
    }//EoM

    /**
     * This method adds data base entries using the Adapter-API.
     */
    @Override
    public void createDatabaseEntries() {
//        Adapter adapter = AdapterHelper.STD_INSTANCE.get();                             //TODO add support to local also
//        try {
//            for (String drop_table : DBData.getDropTableStatements().values()) {
//                adapter.query(drop_table);
//            }
//
//            for (String create_table : DBData.getCreateTableStatements().values()) {
//                adapter.query(create_table);
//            }
//            insertDataIntoDatabase(adapter);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }//EoM

    private void createRemoteDatabaseTables() {
        logger.info("DefaultDBInitializer--> createRemoteDatabaseTables()");
        JDBCInterface test = getJDBCForRemoteDB();
        for (String createTable : DatabaseCreator.getRemoteMetaTablesCreateStatements()) {
            test.query(createTable);
        }
        for (String createTable : DatabaseCreator.getRemoteIndexTablesCreateStatement()) {
            test.query(createTable);
        }
        test.closeConnection();
    }//EoM

    protected void insertDataIntoDatabase(final Adapter adapter) {
//        try {
//            for (Country country : DBData.getCountries()) {
//                adapter.query(country.insertInto());
//            }
//
//            for (City city : DBData.getCities()) {
//                adapter.query(city.insertInto());
//            }
//
//            for (Uni uni : DBData.getUnis()) {
//                adapter.query(uni.insertInto());
//            }
//
//            for (Faculty faculty : DBData.getFaculties()) {
//                adapter.query(faculty.insertInto());
//            }
//
//            for (Student student : DBData.getStudents()) {
//                adapter.query(student.insertInto());
//            }
//        } catch(Exception e){
//            e.printStackTrace();
//        }
    }

    /**
     * Delete the remote data base and its contents
     */
    protected void clearRemote() {
        logger.info("DefaultDBInitializer--> clearRemote()");
        JDBCInterface dbms = null;
        try {  //there is a possibility for the database not to be there so we must gracefully handle the exception
            dbms = getJDBCForRemoteDB();
            dbms.query(DatabaseCreator.getDropRemoteDatabaseStatement());
        } catch (Exception ex) {
            logger.severe("DefaultDBInitializer--> Error: Remote database was not existing!");
        } finally {
            //dbms.closeConnection();
        }
    }//EoM

    /**
     * Delete the local data base and its contents
     */
    protected void clearLocal() {
        logger.info("DefaultDBInitializer--> clearLocal()");
        JDBCInterface dbms = null;
        try {
            dbms = getJDBCForLocalDB();
            dbms.query(DatabaseCreator.getDropLocalDatabaseStatement());
        } catch (Exception ex) {
            logger.severe("DefaultDBInitializer--> Error: Local database was not existing!");
        } finally {
            //dbms.closeConnection();
        }
    }//EoM

    protected void createLocalDatabaseTables() {
        logger.info("DefaultDBInitializer--> createLocalDatabaseTables()");
        JDBCInterface test = getJDBCForLocalDB();
        for (String create_table : DBData.getCreateTableStatements().values()) {
            test.query(create_table);
        }
        test.closeConnection();
    }//EoM

    /**
     * This method reads the db config file and creates the connection to the
     * local database from it. Make sure that the configuration (especially the
     * name of the data base) matches with the data defined in
     * {@link DatabaseCreator}
     *
     * @param dbName The name of the database to connect to.
     */
    protected JDBCInterface getJDBCForLocalDB(final String dbName) {
        logger.info("DefaultDBInitializer--> get JDBC for Local");
        //ConfigParser.getInstance().loadConfig(dbConfigFile);    //TODO verify that this is ok
        Map<String, String> localDbConf = ConfigParser.getInstance(deploymentinstanceid).getLocalDatabase();

        if (null == dbName || dbName.isEmpty()) {
            //System.out.println("returning JDBC interface to "+localDbConf.get("host"));
            return new JDBCInterface("Postgre", localDbConf.get("driverConn") + "://" + localDbConf.get("host") + "/" + localDbConf.get("name"), localDbConf.get("user"), localDbConf.get("password"));
        }
        return new JDBCInterface("Postgre", localDbConf.get("driverConn") + "://" + localDbConf.get("host") + "/" + dbName, localDbConf.get("user"), localDbConf.get("password"));
    }

    /**
     * This method reads the db config file and creates the connection to the
     * local database from it. Make sure that the configuration (especially the
     * name of the data base) matches with the data defined in
     * {@link DatabaseCreator}
     */
    protected JDBCInterface getJDBCForLocalDB() {
        return getJDBCForLocalDB("");
    }

    /**
     * This method reads the db config file and creates the connection to the
     * remote database from it. Make sure that the configuration (especially the
     * name of the data base) matches with the data defined in
     * {@link DatabaseCreator}
     */
    protected JDBCInterface getJDBCForRemoteDB() {
        logger.info("DefaultDBInitializer--> get JDBC for Remote");
        List<Map<String, String>> confList = ConfigParser.getInstance(deploymentinstanceid).getRemoteDatabases();
        Map<String, String> localDbConf = confList.get(0);
        return new JDBCInterface("Postgre", localDbConf.get("driverConn") + "://" + localDbConf.get("host") + "/" + localDbConf.get("name"), localDbConf.get("user"), localDbConf.get("password"));

    }

    protected JDBCInterface getJDBCForIndexDB(int index) {
        logger.info("DefaultDBInitializer--> getJDBCForIndexDB()");
        List<Map<String, String>> confList = ConfigParser.getInstance(deploymentinstanceid).getRemoteIndexDatabases();
        Map<String, String> localDbConf = confList.get(index);
        return new JDBCInterface("Postgre", localDbConf.get("driverConn") + "://" + localDbConf.get("host") + "/" + localDbConf.get("name"), localDbConf.get("user"), localDbConf.get("password"));

    }

    /**
     * This method is used if all data bases are setup during the
     * {@link #setUpLocalDatabase()} and {@link #setUpRemoteDatabase()}. The
     * difference to the {@link #getJDBCForLocalDB()} and
     * {@link #getJDBCForRemoteDB()} is that here we do not connect to an
     * existing data base (e.g. remote and local) but connect to the DBMS to
     * create or drop them.
     *
     * In order to do that the connection data is taken from the db config file.
     * We use the first entry of the database with type 'remote' because for
     * testing purposes we assume that all runs on one machine.
     * <b>Make sure that you have created a database 'postgres' with appropriate
     * owner (e.g. like configured for the remote and local db)</b>
     * This is important because one can not drop a data base to which he is
     * connected to. So we connect to 'postgres' deleting all other dbs from
     * that when initializing.
     *
     * @return
     */
    protected JDBCInterface getBasicConnectionToDBMS() {
        logger.info("DefaultDBInitializer--> get JDBC for Basic");
        List<Map<String, String>> confList = ConfigParser.getInstance(deploymentinstanceid).getRemoteDatabases();
        Map<String, String> localDbConf = confList.get(0);
        return new JDBCInterface("Postgre", localDbConf.get("driverConn") + "://" + localDbConf.get("host") + "/postgres", localDbConf.get("user"), localDbConf.get("password"));

    }

    private void createRemoteDatabase() {
        logger.info("DefaultDBInitializer--> createRemoteDatabase()");
        JDBCInterface dbms = getJDBCForRemoteDB();
        dbms.query(DatabaseCreator.getDropRemoteDatabaseStatement());
        dbms.query(DatabaseCreator.getCreateRemoteDatabaseStatement());
        dbms.closeConnection();
    }

    private void createLocalDatabase() {
        logger.info("DefaultDBInitializer--> createLocalDatabase()");
        JDBCInterface dbms = getJDBCForLocalDB();
        dbms.query(DatabaseCreator.getDropLocalDatabaseStatement());
        dbms.query(DatabaseCreator.getCreateLocalDatabaseStatement());
        dbms.closeConnection();
    }

    private void setUpRemoteWithEncryptedIndex() {
        JDBCInterface test = getJDBCForRemoteDB();
        test.query("CREATE TABLE \"data \"(key integer NOT NULL, value text NOT NULL, CONSTRAINT data_key PRIMARY KEY (key))WITH (OIDS=FALSE);");
        test.query("CREATE TABLE \"tableMeta\"(id integer NOT NULL, name text NOT NULL, CONSTRAINT \"tableMeta_pkey\" PRIMARY KEY (id), CONSTRAINT \"tableMeta_unique\" UNIQUE (name))WITH (OIDS=FALSE);");
        test.query("CREATE TABLE \"fieldMeta\"(id integer NOT NULL, table_id integer NOT NULL, name text NOT NULL, datatype text NOT NULL,length integer,var boolean,not_null boolean,primary_key boolean,uniquevalue boolean,CONSTRAINT \"fieldMeta_pkey\" PRIMARY KEY (id),CONSTRAINT \"fieldMeta_fkey\" FOREIGN KEY (table_id) REFERENCES \"tableMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION, CONSTRAINT \"fieldMeta_unique\" UNIQUE (table_id, name))WITH (OIDS=FALSE);");
        test.query("CREATE TABLE  \"booleanIndex\"(id integer NOT NULL,field_id integer NOT NULL, key text, value text NOT NULL,CONSTRAINT \"booleanIndex_pkey\" PRIMARY KEY (id),CONSTRAINT \"booleanIndex\" FOREIGN KEY (field_id)REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION)WITH (OIDS=FALSE);");
        test.query("CREATE TABLE \"dateIndex\"(id integer NOT NULL, field_id integer NOT NULL, key text, value text NOT NULL, CONSTRAINT \"dateIndex_pkey\" PRIMARY KEY (id), CONSTRAINT \"dateIndex_fkey\" FOREIGN KEY (field_id) REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION)WITH (OIDS=FALSE);");
        test.query("CREATE TABLE \"stringIndex\"(id integer NOT NULL, field_id integer NOT NULL, key text, value text NOT NULL, CONSTRAINT \"stringIndex_pkey\" PRIMARY KEY (id), CONSTRAINT \"stringIndex_fkey\" FOREIGN KEY (field_id) REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION)WITH (OIDS=FALSE);");
        test.query("CREATE TABLE \"doubleIndex\"(id integer NOT NULL, field_id integer NOT NULL, key text, value text NOT NULL, CONSTRAINT \"doubleIndex_pkey\" PRIMARY KEY (id), CONSTRAINT \"doubleIndex_fkey\" FOREIGN KEY (field_id) REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION)WITH (OIDS=FALSE);");
        test.query("CREATE TABLE \"integerIndex\"(id integer NOT NULL, field_id integer NOT NULL, key text, value text NOT NULL, CONSTRAINT \"integerIndex_pkey\" PRIMARY KEY (id), CONSTRAINT \"integerIndex\" FOREIGN KEY (field_id) REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION) WITH (OIDS=FALSE);");
        test.query("CREATE INDEX \"fki_fieldMeta_fkey\" ON \"fieldMeta\" USING btree(table_id);");
    }

}
