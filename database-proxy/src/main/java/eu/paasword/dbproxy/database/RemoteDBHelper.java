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
package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.utils.*;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.utils.DistributedIndexTablesServer;
import eu.paasword.dbproxy.utils.IDistributedServer;
import eu.paasword.dbproxy.utils.QueryLexer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a Helper for RemoteDBAdministration it contains all Methods
 * that were previously used privately in RemoteDBAdministration
 *
 * @author Mark Brenner
 *
 */
public class RemoteDBHelper {

    private Logger logger = Logger
            .getLogger("prototype.database.RemoteDBAdministrationHelper");
    private RemoteDBAdminstration remoteDBBase;
    private String remoteDBName;
    private TableMapper tableMapper = TableMapper.getInstance();
    QueryLexer lexer = QueryLexer.getInstance();
    Encryption encrypt;
    Map<String, Database> relationToDB;

    public RemoteDBHelper(RemoteDBAdminstration db, String dbName, Encryption encryptor, Map<String, Database> relationDB) {
        remoteDBBase = db;
        remoteDBName = dbName;
        encrypt = encryptor;
        relationToDB = relationDB;
    }

    /**
     * Method to refresh the mappings saved by the Adapter
     *
     * @throws DatabaseException
     */
    protected void refreshMappings(String sessionid) throws DatabaseException {
        String remoteTable = remoteDBName + "." + RemoteDBConstants.DATA_TABLE;
        Database remoteDB = relationToDB.get(remoteTable);
        // Table
        List<String> select = new ArrayList<String>();
        select.add("*");
        List<String> from = new ArrayList<String>();
        from.add(RemoteDBConstants.TABLE_NAMES);
        // Column
        List<String> selectColumn = new ArrayList<String>();
        selectColumn.add("*");
        List<String> fromColumn = new ArrayList<String>();
        fromColumn.add(RemoteDBConstants.COLUMN_NAMES);
        ArrayList<String> by = new ArrayList<String>();
        by.add(RemoteDBConstants.TABLE_IDS);
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        tableMapper.refresh(remoteDB.selectAll(select, from, false,sessionid),
                remoteDB.selectOrdered(selectColumn, fromColumn, where, false,
                        null, by, false,sessionid));
    }

    // Mapping scheme for the keys in columnToIndex and in relationToDB
    /**
     * @param first Table name
     * @param second column name
     * @return the Mapping scheme for the keys in columnToIndex and in
     * relationToDB
     */
    protected String getMappingScheme(String first, String second) {
        return first + "." + second;
    }

    // name of the index tables in the database!
    /**
     *
     * @param tableName the Table name
     * @param columnName the name of the column
     * @return the concanated indexTable
     */
    protected String getIndexName(String tableName, String columnName) {
        return tableName + "_" + columnName;
    }

    /**
     *
     * @param indexName the index table name
     * @return the indextable name in the format of getMappingScheme()
     */
    protected String fromIndexNameToMappingScheme(String indexName) {
        return indexName.replaceFirst("_", ".");
    }

    // changes the values of localeToRemote (database is included) to simply the
    // name of the remote table as for an sql
    // query database.table is not valid.
    /**
     *
     * @param fullName the full table name as database.table
     * @return the simple name of a table without the database for an sql query
     */
    public String remoteTableName(String fullName) {
        return fullName.substring(fullName.lastIndexOf(".") + 1,
                fullName.length());
    }

    /**
     * Selects the maximum id in the database, incremented by one. Beginning
     * with one.
     *
     * @param table the name of the table e.g a index or data table
     * @param db the database to select from
     * @param primKey the id-column of the table
     * @return the new maxid
     * @throws DatabaseException
     */
    public int selectMaxIDPlusOne(String table, Database db, String primKey,String sessionid)
            throws DatabaseException {
        int maxID = 1;
        List<String> select = new ArrayList<String>();
        select.add("max(" + primKey + ")");
        List<String> from = new ArrayList<String>();
        from.add(remoteTableName(table));

        ResultSet res = db.selectAll(select, from, false,sessionid);
        try {
            if (res.next()) {
                maxID = res.getInt(1) + 1; // new id must be one greater than
                // the maximum
            } // else: no entries in database, 1 as first primary key
        } catch (SQLException e1) {
            String msg = "Could not get max primary key";
            logger.log(Level.INFO, msg, e1);
            throw new DatabaseException(msg, e1);
        }

        return maxID;
    }

    // Selects a single entry in a relation
    /**
     * Select a single entry in a relation
     *
     * @param from the indextable to select from
     * @param where where clauses to define the query
     * @param columnName the column name to select
     * @return
     * @throws DatabaseException
     */
    public ResultSet selectSingleData(String from, List<WhereClause> where,
            String columnName,String sessionid) throws DatabaseException {
        // searching index table
        Column columnFields = getColumnFromName(from, columnName);
        int columnIndex = (int) columnFields.getId();
        String datatype = columnFields.getType().toString().toLowerCase();

        String indexTableName = datatype + "Index";
        String remoteTable = remoteDBName + "." + indexTableName;
        Database index = relationToDB.get(remoteTable);

        // select statement for keys
        ArrayList<WhereClause> newWhere = new ArrayList<WhereClause>();
        newWhere.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID,
                columnIndex, "="));
        for (WhereClause clause : where) {
            if (clause.getLeftOperand().equals(columnName)) {
                if (clause instanceof WhereClauseBinary) {
                    WhereClauseBinary binary = (WhereClauseBinary) clause;
                    newWhere.add(new WhereClauseBinary(
                            RemoteDBConstants.INDEX_KEY, binary
                            .getRightOperand(), binary.getOperator()));
                } else if (clause instanceof WhereClauseIn) {
                    if (clause instanceof WhereClauseNotIn) {
                        WhereClauseNotIn in = (WhereClauseNotIn) clause;
                        newWhere.add(new WhereClauseNotIn(RemoteDBConstants.INDEX_KEY,
                                in.getIn()));
                    } else {
                        WhereClauseIn in = (WhereClauseIn) clause;
                        newWhere.add(new WhereClauseIn(RemoteDBConstants.INDEX_KEY,
                                in.getIn()));
                    }
                }
            }
        }
        List<String> select = new ArrayList<String>();
        select.add("*");
        List<String> from2 = new ArrayList<String>();
        from2.add(indexTableName);

        ResultSet resKeys = index.select(select, from2, newWhere, false, null, false,sessionid);
        return resKeys;
    }

    /**
     * Deletes uneccesary entries in the field-Table for a given Table (Used to
     * get rid of a complete Table)
     *
     * @throws DatabaseException
     */
    protected void deleteFields(int tableID, Database remoteDB, String sessionid)
            throws DatabaseException {
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        where.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_TABLE_IDS,
                tableID, "="));
        remoteDB.delete(RemoteDBConstants.COLUMN_NAMES, where, sessionid);
    }

    // Method deletes the entries of a column from the Entries in the Data Table
    // row by row
    /**
     * Delete a whole column in all rows in the encrypted data table belonging
     * to a table
     *
     * @param tableName the name of the table
     * @param fieldID the fieldID which the column belongs to
     * @param keys the keys identifiing the table in the data relation
     * @param remote the database to delete from
     * @throws DatabaseException
     */
    protected void deleteColumnFromDataTable(String tableName, int fieldID,
            List<String> keys, Database remote,String sessionid) throws DatabaseException {
        List<String> select = new ArrayList<String>();
        select.add(RemoteDBConstants.DATA_DATA);
        select.add(RemoteDBConstants.DATA_PRIMKEY);
        List<String> from = new ArrayList<String>();
        from.add(RemoteDBConstants.DATA_TABLE);
        ArrayList<WhereClause> dataWhere = new ArrayList<WhereClause>();
        // Find the Position of the Column in the entry
        HashMap<String, Column> columns = tableMapper.getRemoteColumns(tableMapper.getRemoteTableID(tableName));
        ArrayList<Column> columnList = new ArrayList<Column>(columns.values());
        Collections.sort(columnList);
        int pos = 0;
        for (Column current : columnList) {
            if (current.getId() == fieldID) {
                break;
            }
            pos++;
        }
        dataWhere.add(new WhereClauseIn(RemoteDBConstants.DATA_PRIMKEY,
                new ArrayList<Object>(keys)));
        ResultSet res = remote.select(select, from, dataWhere, false, null, true, sessionid);
        try {
            while (res.next()) {
                String s = res.getString(1); //Improves Performance 1 = Data_Data
                s = encrypt.decrypt(s);
                ArrayList<String> row = lexer.splitDecryptedString(s);
                row.remove(pos); // delete the column from the row
                if (row.isEmpty()) { // Delete Entry if no Columns exist anymore
                    ArrayList<WhereClause> currentRow = new ArrayList<WhereClause>();
                    currentRow.add(new WhereClauseBinary(
                            RemoteDBConstants.DATA_PRIMKEY, res
                            .getInt(RemoteDBConstants.DATA_PRIMKEY), "="));
                    remote.delete(RemoteDBConstants.DATA_TABLE, currentRow,sessionid);
                } else { // Update Entry to exclude the value of the deleted
                    // Column
                    res.updateString(1, encrypt.encrypt(lexer.joinDecryptedString(row)));
                    res.updateRow();
                }
            }
        } catch (SQLException e) {
            String msg = "Could not select requested data! \n";
            logger.log(Level.INFO, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /**
     * Delete a column from the fieldMeta with the given id
     *
     * @param fieldID the fieldID of the column to delete
     * @param remote the database to where the fieldMeta table is stored
     * @throws DatabaseException
     */
    protected void deleteColumnFromFieldMeta(int fieldID, Database remote,String sessionid)
            throws DatabaseException {
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        where.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_ID, fieldID,
                "="));
        remote.delete(RemoteDBConstants.COLUMN_NAMES, where,sessionid);
    }

    /**
     * Inserts null values in a new column marked by the die newFieldID and the
     * tableName (Used to initialize a newly added column through alter table
     * !not create table!)
     *
     * @param tableName the name of the table to insert into
     * @param remote the database where the data and index tables are stored
     * @param datatype the type of the new column
     * @param newFieldID the fieldID of the new column
     * @return the keys which the new column belongs to
     * @throws DatabaseException
     */
    protected List<String> insertNullintoColumn(String tableName, Database remote,
            Type datatype, int newFieldID,String sessionid) throws DatabaseException {
        // Insert null value into dataTables
        List<String> keys = remoteDBBase.selectAllKeys(tableName,sessionid);
        List<String> select = new ArrayList<String>();
        select.add(RemoteDBConstants.DATA_PRIMKEY);
        select.add(RemoteDBConstants.DATA_DATA);
        List<String> from = new ArrayList<String>();
        from.add(RemoteDBConstants.DATA_TABLE);
        ArrayList<WhereClause> whereData = new ArrayList<WhereClause>();
        whereData.add(new WhereClauseIn(RemoteDBConstants.DATA_PRIMKEY,
                new ArrayList<Object>(keys)));
        ResultSet res = remote.select(select, from, whereData, false, null, true,sessionid);
        try {
            while (res.next()) {
                String s = res.getString(2); //Improves Performance 2 = Data_Data
                s = encrypt.decrypt(s);
                ArrayList<String> values = lexer.splitDecryptedString(s);
                //Add "null" to mark empty column at the end of the row
                values.add(RemoteDBConstants.NULL);
                res.updateString(2, encrypt.encrypt(lexer.joinDecryptedString(values)));  //Improves Performance 2 = Data_Data
                res.updateRow();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not select requested data! \n");
        }
        return keys;
    }

    /**
     *
     * @param from the name of table to select from
     * @param columnName the name of the column
     * @return a column Object with all information about the column only
     * identified by its name
     * @throws DatabaseException
     */
    public Column getColumnFromName(String from, String columnName) throws DatabaseException {
        int tableID = 0;
        if (tableMapper.containsKey(from.toString())) {
            tableID = tableMapper.getRemoteTableID(from.toString());
        } else {
            throw new DatabaseException("Could not find Table '" + from
                    + "' in Database! \n");
        }
        HashMap<String, Column> columnIDs = tableMapper.getRemoteColumns(tableID);
        Column columnFields = columnIDs.get(columnName);
        if (columnFields == null) {
            throw new DatabaseException("Column '" + columnName
                    + "' in Table '" + from + "' could not be found in Database!\n");
        }
        return columnFields;
    }

    /**
     * This is the pendant to the
     * {@link #selectSingleData(String, List, String)} method of this class. It
     * is called by
     * {@link eu.paasword.dbproxy.database.index.IndexManualAdministration} when
     * the indexing type for distributed index tables / distributed data was set
     * in the config file.
     *
     * @param from
     * @param where
     * @param columnName
     * @param serverID
     * @return
     * @throws DatabaseException
     */
    public ResultSet selectSingleDataDistributed(String from, List<WhereClause> where,
            String columnName, int serverID,String sessionid) throws DatabaseException {
        // searching index table
        Column columnFields = getColumnFromName(from, columnName);
        int columnIndex = (int) columnFields.getId();

        String datatype = columnFields.getType().toString().toLowerCase();
        String indexTableNameWithDataType = datatype + "Index";
        IDistributedServer indexServerDAO = getServerBy(serverID,sessionid);
        String remoteTable = indexServerDAO.getServerName() + "." + indexTableNameWithDataType;
        Database indexServer = relationToDB.get(remoteTable);

        // select statement for keys
        ArrayList<WhereClause> newWhere = new ArrayList<WhereClause>();
        newWhere.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID,
                columnIndex, "="));
        for (WhereClause clause : where) {
            if (clause.getLeftOperand().equals(columnName)) {
                if (clause instanceof WhereClauseBinary) {
                    WhereClauseBinary binary = (WhereClauseBinary) clause;
                    newWhere.add(new WhereClauseBinary(
                            RemoteDBConstants.INDEX_KEY, binary
                            .getRightOperand(), binary.getOperator()));
                } else if (clause instanceof WhereClauseIn) {
                    if (clause instanceof WhereClauseNotIn) {
                        WhereClauseNotIn in = (WhereClauseNotIn) clause;
                        newWhere.add(new WhereClauseNotIn(RemoteDBConstants.INDEX_KEY,
                                in.getIn()));
                    } else {
                        WhereClauseIn in = (WhereClauseIn) clause;
                        newWhere.add(new WhereClauseIn(RemoteDBConstants.INDEX_KEY,
                                in.getIn()));
                    }
                }
            }
        }
        List<String> select = new ArrayList<String>();
        select.add("*");
        List<String> from2 = new ArrayList<String>();
        from2.add(indexTableNameWithDataType);

        ResultSet resKeys = indexServer.select(select, from2, newWhere, false, null, false,sessionid);
        return resKeys;
    }

    /**
     * Get the index server behind this server id. With this DAO that is
     * returned by this method we can get the actual {@link Database} instance
     * to that server.
     *
     * @param serverId
     * @return The index server
     */
    private IDistributedServer getServerBy(int serverId,String sessionid) {
        String columnServerMappingFQDN = remoteDBName + "." + RemoteDBConstants.COLUMN_SERVER_MAPPING;

        Database columnServerMapping = relationToDB.get(columnServerMappingFQDN);

        List<String> select = new ArrayList<String>();
        select.add(RemoteDBConstants.COLUMN_SERVER_SERVERID);
        select.add(RemoteDBConstants.COLUMN_SERVER_SERVERNAME);
        List<String> from = new ArrayList<String>();
        from.add(RemoteDBConstants.COLUMN_SERVER_MAPPING);
        List<WhereClause> where = new ArrayList<>();
        where.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_SERVER_SERVERID, serverId, "="));

        String serverNameFromDB = null;
        Integer serverIdFromDB = null;
        try {
            ResultSet serverNameResultSet = columnServerMapping.select(select, from, where, true, new ArrayList<String>(), false,sessionid);
            while (serverNameResultSet.next()) {
                serverIdFromDB = serverNameResultSet.getInt(1);
                serverNameFromDB = serverNameResultSet.getString(2);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        IDistributedServer indexServer = null;
        if (null != serverIdFromDB && serverIdFromDB.equals(serverId)) {
            indexServer = new DistributedIndexTablesServer(serverIdFromDB, serverNameFromDB);
        } else {
            throw new IllegalStateException("Server id that was requested was not found in data base!");
        }

        return indexServer;
    }

}
