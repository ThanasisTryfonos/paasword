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

import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.database.utils.WhereClauseBinary;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.utils.DistributedIndexTablesServer;
import eu.paasword.dbproxy.utils.IDistributedServer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Class responsible for the management of the meta tables. E.G. the management
 * of the table names, fields etc.
 *
 * @author Mark Brenner
 *
 */
public class TableManagement {

    private String remoteDBName;
    private RemoteDBAdminstration remoteDBAdmin;
    private Map<String, Database> relationToDB;
    private TableMapper tableMapper;

    /**
     * Constructs a Tablemanager which needs the some information from the
     * RemoteDBAdministration
     *
     * @param remoteDBadmin the RemoteDBAdminstration this manager belongs to
     * @param remoteDBname the name of the remote database(s) usually "remote"
     * @param relationToDb a mapping of the index tables to their databases
     */
    public TableManagement(RemoteDBAdminstration remoteDBadmin,
            String remoteDBname, Map<String, Database> relationToDb) {
        remoteDBName = remoteDBname;
        relationToDB = relationToDb;
        tableMapper = TableMapper.getInstance();
        remoteDBAdmin = remoteDBadmin;
    }

    /**
     * Method which renames a column
     *
     * @param tableName the table which the column belongs to
     * @param oldColumnName the old name of the column
     * @param newColumnName the new name to which the column shall be renamed
     * @return true if the renaming was successful
     * @throws DatabaseException
     */
    protected boolean renameColumn(String tableName, String oldColumnName, String newColumnName, String sessionid) throws DatabaseException {
        // Collect information about the field
        // Checks also whether the table and column exists
        int oldfieldid = tableMapper.getColumnID(tableName, oldColumnName);
        Database fieldMeta = relationToDB.get(remoteDBName + "."
                + RemoteDBConstants.COLUMN_NAMES);
        HashMap<String, Object> rename = new HashMap<String, Object>();
        rename.put(RemoteDBConstants.COLUMN_NAME, newColumnName);
        ArrayList<WhereClause> columnWhere = new ArrayList<WhereClause>();
        columnWhere.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_ID,
                oldfieldid, "="));
        fieldMeta.update(RemoteDBConstants.COLUMN_NAMES, rename, columnWhere, false, sessionid); // Rename Column in fieldMeta table
        return true;
    }

    /**
     * Assigns the column to a new server.
     *
     * @param columnID The unique column id of the column
     * @param newServerId The uniqe server id of the server.
     *
     * @throws DatabaseException
     */
    protected void changeColumnServerMapping(int columnID, int newServerId, String sessionid) throws DatabaseException {
        Database columnServerMapping = relationToDB.get(remoteDBName + "."
                + RemoteDBConstants.COLUMN_SERVER_MAPPING);

        HashMap<String, Object> newServer = new HashMap<String, Object>();
        newServer.put(RemoteDBConstants.COLUMN_SERVER_SERVERID, newServerId);
        newServer.put(RemoteDBConstants.COLUMN_SERVER_SERVERNAME, getServerOfColumn(columnID, sessionid).getServerName());
        ArrayList<WhereClause> columnWhere = new ArrayList<WhereClause>();
        columnWhere.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_SERVER_ID,
                columnID, "="));

        columnServerMapping.update(RemoteDBConstants.COLUMN_SERVER_MAPPING, newServer, columnWhere, false, sessionid); //
    }

    /**
     * Method to create a new entry in the table meta
     *
     * @param newTableID the id of the new table
     * @param tableName the name of the new table
     * @throws DatabaseException
     */
    protected void createEntryInTableMeta(int newTableID, String tableName, String sessionid)
            throws DatabaseException {
        String remoteTable = remoteDBName + "." + RemoteDBConstants.TABLE_NAMES;
        Database remoteDB = relationToDB.get(remoteTable);
        List<String> columnsInsert = new ArrayList<String>();
        columnsInsert.add(RemoteDBConstants.TABLE_IDS);
        columnsInsert.add(RemoteDBConstants.TABLE_NAME);
        List<Object> valuesInsert = new ArrayList<Object>();
        valuesInsert.add(newTableID);
        valuesInsert.add(tableName);
        remoteDB.insert(RemoteDBConstants.TABLE_NAMES, columnsInsert,
                valuesInsert, false, sessionid);
    }

    /**
     * Method to create a new column entry in the fieldsmeta
     *
     * @param newTableID the id of the table
     * @param newColumnID the id of the new column
     * @param column a column object containing all relevant information about
     * the new column
     * @throws DatabaseException
     */
    protected void createColumnInFieldMeta(int newTableID, int newColumnID, Column column, String sessionid) throws DatabaseException {
        String remoteTable = remoteDBName + "." + RemoteDBConstants.TABLE_NAMES;
        Database remoteDB = relationToDB.get(remoteTable);
        List<String> columnsInsert = new ArrayList<String>();
        columnsInsert.add(RemoteDBConstants.COLUMN_ID);
        columnsInsert.add(RemoteDBConstants.COLUMN_TABLE_IDS);
        columnsInsert.add(RemoteDBConstants.COLUMN_NAME);
        columnsInsert.add(RemoteDBConstants.COLUMN_DATATYPE);
        columnsInsert.add(RemoteDBConstants.COLUMN_LENGTH);
        columnsInsert.add(RemoteDBConstants.COLUMN_VARIABLE);
        columnsInsert.add(RemoteDBConstants.COLUMN_NOT_NULL);
        columnsInsert.add(RemoteDBConstants.COLUMN_UNIQUE);
        columnsInsert.add(RemoteDBConstants.COLUMN_PRIMARY_KEY);
        List<Object> valuesInsert = new ArrayList<Object>();
        valuesInsert.add(newColumnID);
        valuesInsert.add(newTableID);
        valuesInsert.add(column.getName());

        valuesInsert.add(column.getType().toString().toLowerCase());
        valuesInsert.add(column.getLength());

        valuesInsert.add(column.isVarcharType());
        valuesInsert.add(column.isNot_null());
        valuesInsert.add(column.isUnique());
        valuesInsert.add(column.isPrimary_key());
        // jpa
        remoteDB.insert(RemoteDBConstants.COLUMN_NAMES, columnsInsert, valuesInsert, false, sessionid);
    }

    /**
     * Method to create a new entry in a column server mapping table
     *
     * @param columnID the id of the column
     * @param serverID the id of the server where this column shall be stored
     * @param serverName
     *
     * @throws DatabaseException
     */
    protected void createColumnServerMapping(int columnID, int serverID, String serverName, String sessionid) throws DatabaseException {
        String remoteTable = remoteDBName + "." + RemoteDBConstants.COLUMN_SERVER_MAPPING;
        Database remoteDB = relationToDB.get(remoteTable);
        List<String> columnsInsert = new ArrayList<String>();
        columnsInsert.add(RemoteDBConstants.COLUMN_SERVER_ID);
        columnsInsert.add(RemoteDBConstants.COLUMN_SERVER_SERVERID);
        columnsInsert.add(RemoteDBConstants.COLUMN_SERVER_SERVERNAME);
        List<Object> valuesInsert = new ArrayList<Object>();
        valuesInsert.add(columnID);
        valuesInsert.add(serverID);
        valuesInsert.add(serverName);

        remoteDB.insert(RemoteDBConstants.COLUMN_SERVER_MAPPING, columnsInsert, valuesInsert, false, sessionid);
    }

    /**
     * Adds a constraint to a table for the given columns
     *
     * @param columnNames names of the columns which shall be given a constraint
     * @param table the name of the table the columns belong to
     * @param unique whether the columns shall be made unique
     * @param primaryKey whether the columns shall be made a primary key
     * @throws DatabaseException
     */
    protected void addConstraint(List<String> columnNames, String table,boolean unique, boolean primaryKey, String sessionid) throws DatabaseException {
        int tableID = tableMapper.getRemoteTableID(table);
        Map<String, Column> columns = tableMapper.getRemoteColumns(tableID);
        // Check uniquenes of values
        checkUniquenessAddConstraint(columnNames, columns.values(), table, sessionid);
        for (String columnname : columnNames) {
            Column col = columns.get(columnname);
            if (unique && !col.isUnique()) {
                insertConstraint(col.getId(), RemoteDBConstants.COLUMN_UNIQUE,sessionid);
            }
            if (primaryKey && !col.isPrimary_key()) {
                if (!col.isNot_null()) {
                    throw new DatabaseException(
                            "Can not add constraint Primary Key to column "
                            + col.getName() + " in table " + table
                            + " because it has to be not null!\n");
                }
                insertConstraint(col.getId(),RemoteDBConstants.COLUMN_PRIMARY_KEY,sessionid);
            }
        }
    }

    /**
     * Checks whether value already exists in table or if null is not allowed
     *
     * @param currentColumn the column to insert the value into
     * @param currentValue the value to be inserted
     * @param from the table which the value belongs to
     * @throws DatabaseException
     */
    protected void checkConstraints(Column currentColumn, Object currentValue,
            String from, String sessionid) throws DatabaseException {
        // Check if value is null & column does allow it
        if (currentColumn.isNot_null()) {
            if (currentValue == null) {
                throw new DatabaseException(
                        "Tried to insert a null value into a not null column ("
                        + currentColumn.getName() + ")!\n");
            }
        }
        // Check unique
        if (currentColumn.isUnique()) {
            ArrayList<String> keysToRetain = new ArrayList<String>();
            ArrayList<WhereClause> where = new ArrayList<WhereClause>();
            if (currentValue == null) {
                where.add(new WhereClauseBinary(currentColumn.getName(), "",
                        RemoteDBConstants.ISNULL));
            } else {
                where.add(new WhereClauseBinary(currentColumn.getName(),
                        currentValue, "="));
            }
            if (!remoteDBAdmin.selectKeys(from, where, keysToRetain, false, sessionid)
                    .isEmpty()) {
                throw new DatabaseException("Tried to insert a value ("
                        + currentValue + ") twice in a unique column "
                        + currentColumn.getName() + "!\n");
            }
        }
    }

    /**
     * Checks whether the given values can be inserted into the table without
     * violating the primary key constraint
     *
     * @param columnsToValues mapping of new values and their columns
     * @param from the table to insert into
     * @throws DatabaseException
     */
    protected void checkPrimaryKey(HashMap<Column, Object> columnsToValues,
            String from, String sessionid) throws DatabaseException {
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        ArrayList<String> keysToRetain = new ArrayList<String>();
        for (Entry<Column, Object> entry : columnsToValues.entrySet()) {
            if (entry.getValue() == null) {
                throw new DatabaseException("Value of Primary key for column "
                        + entry.getKey().getName() + " is null!\n");
            }
            where.add(new WhereClauseBinary(entry.getKey().getName(), entry
                    .getValue(), "="));
        }
        if (!remoteDBAdmin.selectKeys(from, where, keysToRetain, false, sessionid)
                .isEmpty()) {
            throw new DatabaseException("Primary key already exists in table "
                    + from + "!\n");
        }
    }

    // Inserts primary key or unique constraint into the fieldMeta, columnType
    // is here the column in the fielmeta e.g. Unique or PrimaryKey
    private void insertConstraint(int colID, String columnType, String sessionid)
            throws DatabaseException {
        String remoteTable = remoteDBName + "."
                + RemoteDBConstants.COLUMN_NAMES;
        Database remoteDB = relationToDB.get(remoteTable);
        HashMap<String, Object> updateData = new HashMap<String, Object>();
        updateData.put(columnType, true);
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        where.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_ID, colID, "="));
        remoteDB.update(RemoteDBConstants.COLUMN_NAMES, updateData, where, false, sessionid);
    }

    // Chck whether the column or the columns can be made unique / primary keys
    private void checkUniquenessAddConstraint(List<String> columnNames,
            Collection<Column> columns, String table, String sessionid) throws DatabaseException {
        ArrayList<Integer> positions = new ArrayList<Integer>();
        Column[] colArray = new Column[columns.size()];
        columns.toArray(colArray);
        Arrays.sort(colArray);
        for (String columnname : columnNames) {
            int position = columns.size();
            for (int i = 0; i < columns.size(); i++) {
                if (colArray[i].getName().equals(columnname)) {
                    position = i;
                    break;
                }
            }
            if (position == columns.size()) {
                throw new DatabaseException("Could not find column "
                        + columnname + " in table " + table + "!\n");
            } else {
                positions.add(position);
            }
        }
        List<String> keys = remoteDBAdmin.selectAllKeys(table, sessionid);
        List<List<String>> data = remoteDBAdmin.selectData(keys, sessionid);
        HashSet<String> checkSet = new HashSet<String>();
        boolean uniqueRow = true;
        for (List<String> row : data) {
            String combinedrow = "";
            for (int pos : positions) {
                combinedrow += row.get(pos);
            }
            uniqueRow = uniqueRow & checkSet.add(combinedrow);
            if (!uniqueRow) {
                throw new DatabaseException(
                        "Cannot turn columns into unique or Primary Key as they already contain duplicates!\n");
            }
        }
    }

    /**
     * Get the index server DAO on which the column is stored.
     *
     * @param columnID The unique column id of the column.
     * @return The server DAO. Null is returned if exceptions are thrown inside
     * this method.
     */
    private IDistributedServer getServerOfColumn(int columnID, String sessionid) {
        Database columnServerMapping = relationToDB.get(remoteDBName + "."
                + RemoteDBConstants.COLUMN_SERVER_MAPPING);

        List<String> select = new ArrayList<>();
        select.add(RemoteDBConstants.COLUMN_SERVER_ID);
        select.add(RemoteDBConstants.COLUMN_SERVER_SERVERNAME);
        List<String> from = new ArrayList<>();
        from.add(remoteDBName + "." + RemoteDBConstants.COLUMN_SERVER_MAPPING);

        List<WhereClause> where = new ArrayList<>();
        where.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_SERVER_ID,
                columnID, "="));

        IDistributedServer indexServerOfColumn = null;
        try {
            ResultSet res = columnServerMapping.select(select, from, where, false, new ArrayList<>(), false, sessionid);
            indexServerOfColumn = new DistributedIndexTablesServer(res.getInt(1), res.getString(2));
        } catch (DatabaseException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return indexServerOfColumn;
    }
}
