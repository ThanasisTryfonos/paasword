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

/**
 * This class is used in the process of setting up all remote and local data
 * bases for the distributed encryption scenario of the PaasWord adapter. It is
 * a simple DAO that holds the create database statements to ease the creation
 * of multiple servers. All other tables & data are generated in
 * {@link DatabaseCreator} and {@link DBData}.
 */
public class FakeServerAsDatabase {

    private static final String DROP_DATABASE = "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres; GRANT ALL ON SCHEMA public TO public;"; //DROP DATABASE IF EXISTS %s;";
    private static final String CREATE_DATABASE = "CREATE DATABASE %s WITH OWNER %s;";
    private static final String CREATE_SERVER_TABLE = "CREATE TABLE  \"%s\"(id integer NOT NULL,field_id integer NOT NULL, key text, value text NOT NULL, CONSTRAINT \"%s_pkey\" PRIMARY KEY (id));";
    private static final String DROP_SERVER_TABLE = "DROP TABLE IF EXISTS %s CASCADE";

    private String databaseName;
    private String databaseOwner;

    public FakeServerAsDatabase(final String dbName, final String dbOwner) {
        databaseName = dbName;
        databaseOwner = dbOwner;
    }

    public String getName() {
        return databaseName;
    }

    /**
     * Returns the create database statement for this instance.
     *
     * @return CREATE DATABASE ... WITH OWNER ...
     */
    public String getCreateDatabaseStatement() {
        return String.format(CREATE_DATABASE, databaseName, databaseOwner);
    }

    /**
     * Returns the drop database statement for this instance.
     *
     * @return DROP DATABASE IF EXITS ...
     */
    public String getDropDatabaseStatement() {
        return String.format(DROP_DATABASE, databaseName);
    }

    /**
     * Get the create table statements to create all tables needed for this
     * instance.
     *
     * @return All create table statements separated by ';'
     */
    public String getCreateTableStatements() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(CREATE_SERVER_TABLE, databaseName, databaseName));
        return builder.toString();
    }

    /**
     * Get the drop table statements to delete all tables needed for this
     * instance.
     *
     * @return All drop table statements separated by ';'
     */
    public String getDropTableStatements() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(DROP_SERVER_TABLE, databaseName));
        return builder.toString();
    }

}
