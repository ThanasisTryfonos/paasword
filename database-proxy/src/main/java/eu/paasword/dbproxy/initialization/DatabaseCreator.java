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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class defines the create and drop database statements to create the local and remote databases and to simulate multiple servers for a distributed scenario.
 * In such a scenario one server here is simulated by one database.
 */
public class DatabaseCreator {
    
    public static int NUMBER_OF_SERVERS = 5;
    private static Map<String, FakeServerAsDatabase> db_servers;
    private static List<String> db_index_tables_create_statements;
    private static List<String> db_index_tables_create_statements_distributed;
    private static List<String> db_meta_tables_create_statements;
    private static int numberOfServers;
    private static FakeServerAsDatabase localDatabase;
    private static FakeServerAsDatabase remoteDatabase;

    public DatabaseCreator(int amountofindexes) {
        init(amountofindexes);
    }//EoCon
    
    /**
     * This method must be called before any other method.
     * It sets the number of server to 5 (e.g. 5 databases/servers are created)
     */
    public static void init() {
        init(NUMBER_OF_SERVERS);
    }

    /**
     * This method must be called before any other method.
     * It inizializes the number of servers (e.g. how many databases/servers should be created)
     *
     * @param countServers
     */
    public static void init(int countServers) {
        System.out.println("DatabaseCreator initialized with servers: "+countServers);
        numberOfServers = countServers;
        initializeRemoteIndexTableStatements();
        initializeStandardDatabases();
        initializeServers();
    }

    private static void initializeRemoteIndexTableStatements() {
        db_index_tables_create_statements = new ArrayList<>();
        db_index_tables_create_statements.add("CREATE TABLE  \"booleanIndex\"(id integer NOT NULL,field_id integer NOT NULL, key boolean, value text NOT NULL,CONSTRAINT \"booleanIndex_pkey\" PRIMARY KEY (id),CONSTRAINT \"booleanIndex\" FOREIGN KEY (field_id)REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION)WITH (OIDS=FALSE);");
        db_index_tables_create_statements.add("CREATE TABLE \"dateIndex\"(id integer NOT NULL, field_id integer NOT NULL, key date, value text NOT NULL, CONSTRAINT \"dateIndex_pkey\" PRIMARY KEY (id), CONSTRAINT \"dateIndex_fkey\" FOREIGN KEY (field_id) REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION)WITH (OIDS=FALSE);");
        db_index_tables_create_statements.add("CREATE TABLE \"stringIndex\"(id integer NOT NULL, field_id integer NOT NULL, key text, value text NOT NULL, CONSTRAINT \"stringIndex_pkey\" PRIMARY KEY (id), CONSTRAINT \"stringIndex_fkey\" FOREIGN KEY (field_id) REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION)WITH (OIDS=FALSE);");
        db_index_tables_create_statements.add("CREATE TABLE \"doubleIndex\"(id integer NOT NULL, field_id integer NOT NULL, key double precision, value text NOT NULL, CONSTRAINT \"doubleIndex_pkey\" PRIMARY KEY (id), CONSTRAINT \"doubleIndex_fkey\" FOREIGN KEY (field_id) REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION)WITH (OIDS=FALSE);");
        db_index_tables_create_statements.add("CREATE TABLE \"integerIndex\"(id integer NOT NULL, field_id integer NOT NULL, key integer, value text NOT NULL, CONSTRAINT \"integerIndex_pkey\" PRIMARY KEY (id), CONSTRAINT \"integerIndex\" FOREIGN KEY (field_id) REFERENCES \"fieldMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION) WITH (OIDS=FALSE);");

        //For the distributet scenario we do not use foreign key constraints
        db_index_tables_create_statements_distributed = new ArrayList<>();
        db_index_tables_create_statements_distributed.add("CREATE TABLE  \"booleanIndex\"(id integer NOT NULL,field_id integer NOT NULL, key boolean, value text NOT NULL,CONSTRAINT \"booleanIndex_pkey\" PRIMARY KEY (id));");
        db_index_tables_create_statements_distributed.add("CREATE TABLE \"dateIndex\"(id integer NOT NULL, field_id integer NOT NULL, key date, value text NOT NULL, CONSTRAINT \"dateIndex_pkey\" PRIMARY KEY (id));");
        db_index_tables_create_statements_distributed.add("CREATE TABLE \"stringIndex\"(id integer NOT NULL, field_id integer NOT NULL, key text, value text NOT NULL, CONSTRAINT \"stringIndex_pkey\" PRIMARY KEY (id));");
        db_index_tables_create_statements_distributed.add("CREATE TABLE \"doubleIndex\"(id integer NOT NULL, field_id integer NOT NULL, key double precision, value text NOT NULL, CONSTRAINT \"doubleIndex_pkey\" PRIMARY KEY (id));");
        db_index_tables_create_statements_distributed.add("CREATE TABLE \"integerIndex\"(id integer NOT NULL, field_id integer NOT NULL, key integer, value text NOT NULL, CONSTRAINT \"integerIndex_pkey\" PRIMARY KEY (id));");


        db_meta_tables_create_statements = new ArrayList<>();
        db_meta_tables_create_statements.add("CREATE TABLE \"data\"(key integer NOT NULL, value text NOT NULL, CONSTRAINT data_key PRIMARY KEY (key))WITH (OIDS=FALSE);");
        db_meta_tables_create_statements.add("CREATE TABLE \"tableMeta\"(id integer NOT NULL, name text NOT NULL, CONSTRAINT \"tableMeta_pkey\" PRIMARY KEY (id), CONSTRAINT \"tableMeta_unique\" UNIQUE (name))WITH (OIDS=FALSE);");
        db_meta_tables_create_statements.add("CREATE TABLE \"fieldMeta\"(id integer NOT NULL, table_id integer NOT NULL, name text NOT NULL, datatype text NOT NULL,length integer,var boolean,not_null boolean,primary_key boolean,uniquevalue boolean,CONSTRAINT \"fieldMeta_pkey\" PRIMARY KEY (id),CONSTRAINT \"fieldMeta_fkey\" FOREIGN KEY (table_id) REFERENCES \"tableMeta\" (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION, CONSTRAINT \"fieldMeta_unique\" UNIQUE (table_id, name))WITH (OIDS=FALSE);");
        db_meta_tables_create_statements.add("CREATE INDEX \"fki_fieldMeta_fkey\" ON \"fieldMeta\" USING btree(table_id);");
    }

    public static Map<String, FakeServerAsDatabase> getServers(){
        return db_servers;
    }

    public static String getCreateLocalDatabaseStatement(){
        return localDatabase.getCreateDatabaseStatement();
    }

    public static String getCreateRemoteDatabaseStatement(){
        return remoteDatabase.getCreateDatabaseStatement();
    }

    public static String getDropLocalDatabaseStatement(){
        return localDatabase.getDropDatabaseStatement();
    }

    public static String getDropRemoteDatabaseStatement(){
        return remoteDatabase.getDropDatabaseStatement();
    }

    public static List<String> getRemoteIndexTablesCreateStatement(){
        return db_index_tables_create_statements;
    }

    public static List<String> getRemoteMetaTablesCreateStatements(){
        return db_meta_tables_create_statements;
    }

    /**
     * We do create the same index tables (integerIndex, booleanIndex etc.) but without any foreign key constraints.
     * @return A list of create table statements
     */
    public static List<String> getDistributedRemoteIndexTablesCreateStatements(){
        return db_index_tables_create_statements_distributed;
    }

    /**
     * Initialize the instances for local and remote databases.
     */
    private static void initializeStandardDatabases() {
        localDatabase = new FakeServerAsDatabase("kit_mimosecco_local", "postgres");    //change     "kit_mimosecco_local", "postgres"); 
        remoteDatabase = new FakeServerAsDatabase("kit_mimosecco_remote", "postgres");   //change    "kit_mimosecco_remote", "postgres
    }

    /**
     * Initialize the create and drop statements for the server databases
     */
    private static void initializeServers() {
        db_servers = new LinkedHashMap<>(numberOfServers);

        for(int i= 0; i < numberOfServers; i++){
            String dbName = "kit_server_" + i;
            FakeServerAsDatabase db = new FakeServerAsDatabase(dbName, "postgres");   //TODO change the owner dynamically
            db_servers.put(db.getName(), db);
        }
    }//EoM
    
}//EoC
