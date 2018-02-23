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
package eu.paasword.dbproxy.database.index;

import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.database.RemoteDBConstants;
import eu.paasword.dbproxy.database.RemoteDBHelper;
import eu.paasword.dbproxy.database.TableMapper;
import eu.paasword.dbproxy.database.utils.*;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.utils.QueryLexer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class IndexTableAdministration extends IndexAdministration {
    private Logger logger = Logger.getLogger(IndexTableAdministration.class.getName());
    protected TableMapper tableMapper = TableMapper.getInstance();
    protected Map<String, Database> relationToDB;
    protected RemoteDBHelper helper;
    protected String remoteDBName;
    protected QueryLexer lexer = QueryLexer.getInstance();
    protected HashMap<String, Integer> tableMaxID;

    public IndexTableAdministration(Map<String, Database> relationDB, RemoteDBHelper DBhelper, String remoteName) {
        relationToDB = relationDB;
        helper = DBhelper;
        remoteDBName = remoteName;
        tableMaxID = new HashMap<String, Integer>();
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.selectAllKeys(int tableID)
     */
    @Override
    public List<String> selectAllKeys(int tableID, String sessionid) throws DatabaseException {
        List<String> result = new ArrayList<String>();
        Map<String, Column> columns = tableMapper.getRemoteColumns(tableID);
        if (columns != null) {
            List<String> columnIDs = new ArrayList<String>(columns.keySet());
            //Only first Column is needed since all entries should have it!
            String colID = columnIDs.get(0);
            int datatypeID = columns.get(colID).getId();
            // Get the necessary DB where the IndexRelation for this column is stored
            String datatypeName = columns.get(colID).getType().toString().toLowerCase();
            String indexTableName = datatypeName + "Index";
            String remoteTable = remoteDBName + "." + indexTableName;
            Database indexDB = relationToDB.get(remoteTable);
            //Set up the Query
            List<String> select = new ArrayList<String>();
            select.add("*");
            List<String> from2 = new ArrayList<String>();
            from2.add(indexTableName);
            ArrayList<WhereClause> where = new ArrayList<WhereClause>();
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, datatypeID, "="));
            // select all from datatype where field_id=datatypeID
            ResultSet res = indexDB.select(select, from2, where, false, null, false, sessionid);
            try {
                result = getKeysFromIndex(res, indexTableName, indexDB,sessionid);
            } catch (SQLException e) {
                String msg = "Could not select requested data";
                logger.log(Level.INFO, msg, e);
                throw new DatabaseException(msg, e);
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.deleteAllFromIndex(int tableID)
     */
    public List<String> deleteAllFromIndex(int tableID, String sessionid) throws DatabaseException {
        List<String> deletedIndices = selectAllKeys(tableID, sessionid);
        HashMap<String, Column> columns = tableMapper.getRemoteColumns(tableID);
        if (columns != null) {
            Set<String> columnNames = columns.keySet();
            //for every column
            for (String columnID : columnNames) {
                Column columnFields = columns.get(columnID);
                //Get the necessary DB where the IndexRelation for this column is stored
                String datatypeName = columns.get(columnID).getType().toString().toLowerCase();
                String indexTableName = datatypeName + "Index";
                String remoteTable = remoteDBName + "." + indexTableName;
                Database indexDB = relationToDB.get(remoteTable);
                String datatypeTable = columnFields.getType().toString().toLowerCase() + "Index";
                ArrayList<WhereClause> where = new ArrayList<WhereClause>();
                where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, columnFields.getId(), "="));
                try {
                    deleteFromIndex(datatypeTable, where, indexDB, sessionid);
                } catch (SQLException e) {
                    throw new DatabaseException(e.getMessage());
                }
            }
        }
        return deletedIndices;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.insertIndexValue(String tableName, int maxID,
            String columnName, Object columnValue)
     */
    public void insertIndexValue(String tableName, int maxID,
                                 String columnName, Object columnValue, String sessionid) throws DatabaseException {

        // get Stuff from datastructures
        int tableID = 0;
        if (tableMapper.containsKey(tableName)) {
            tableID = tableMapper.getRemoteTableID(tableName);
        } else {
            throw new DatabaseException("Could not find Table " + tableName
                    + " in Database! \n");
        }
        HashMap<String, Column> columns = tableMapper.getRemoteColumns(tableID);
        int columnID = 0;
        if (columns.containsKey(columnName)) {
            columnID = (int) columns.get(columnName).getId();
        } else {
            throw new DatabaseException("Could not find Column " + columnName
                    + "! \n");
        }
        String datatype = columns.get(columnName).getType().toString()
                .toLowerCase();
        // sql-query
        List<String> select = new ArrayList<String>();
        select.add("*");
        List<String> from = new ArrayList<String>();
        String datatypeIndex = datatype + "Index";
        from.add(datatypeIndex);
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        if (columnValue == null) {
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, "",
                    RemoteDBConstants.ISNULL));
        } else {
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY,
                    columnValue, "="));
        }
        where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID,
                columnID, "=")); // Identical Data Values can exist in different
        // Tables
        //Get the necessary DB where the IndexRelation for this column is stored
        String remoteTable = remoteDBName + "." + datatypeIndex;
        Database indexDB = relationToDB.get(remoteTable);
        ResultSet resSet = indexDB.select(select, from, where, false, null, true, sessionid);
        try {
            ArrayList<String> keys = new ArrayList<String>();
            keys.add(Integer.toString(maxID));
            insertNewValueIntoIndex(resSet, indexDB, keys, columnValue, columnID, datatypeIndex,sessionid);
        } catch (SQLException e) {
            String msg = "Insert could not be performed";
            logger.log(Level.INFO, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.selectKeys(String from, List<WhereClause> where, List<String> keysToRetain)
     */
    public List<String> selectKeys(String from, List<WhereClause> where, List<String> keysToRetain, boolean or, String sessionid)
            throws DatabaseException {
        // remove alias from map, joins could not occur because only one table is processed.
        ArrayList<WhereClause> newWhere = new ArrayList<WhereClause>();
        for (WhereClause clause : where) {
            String key = clause.getLeftOperand();
            String newKey = key.substring(key.lastIndexOf(".") + 1, key.length());
            if (clause instanceof WhereClauseBinary) {
                WhereClauseBinary bin = (WhereClauseBinary) clause;
                newWhere.add(new WhereClauseBinary(newKey, bin.getRightOperand(), bin.getOperator())); //Only one value operand used
            } else if (clause instanceof WhereClauseIn) {
                if (clause instanceof WhereClauseNotIn) {
                    WhereClauseNotIn in = (WhereClauseNotIn) clause;
                    newWhere.add(new WhereClauseNotIn(newKey, in.getIn()));
                } else {
                    WhereClauseIn in = (WhereClauseIn) clause;
                    newWhere.add(new WhereClauseIn(newKey, in.getIn()));
                }
            }
        }

        // For each entry in where, the rows must be selected
        List<List<String>> allPossibleKeys = new ArrayList<>();

        for (WhereClause clause : newWhere) {
            // select keys of condition
            ArrayList<WhereClause> singleWhere = new ArrayList<WhereClause>();
            singleWhere.add(clause);
            ResultSet res = helper.selectSingleData(from, singleWhere, clause.getLeftOperand(), sessionid);
            Column column = helper.getColumnFromName(from, clause.getLeftOperand());
            String indexTableName = column.getType().toString().toLowerCase() + "Index";
            String remoteTable = remoteDBName + "." + indexTableName;
            Database indexDB = relationToDB.get(remoteTable);
            try {
                List<String> keys = getKeysFromIndex(res, indexTableName, indexDB, sessionid);
                allPossibleKeys.add(keys);
            } catch (SQLException e) {
                String msg = "Index table could not be accessed! \n";
                logger.log(Level.SEVERE, msg, e);
                throw new DatabaseException(msg, e);
            }

        }

        // Remove keys which does not fulfill "and"

        ArrayList<String> indices = new ArrayList<String>();
        if (!allPossibleKeys.isEmpty()) {
            indices.addAll(allPossibleKeys.get(0));
            if (or) {
                for (int i = 1; i < allPossibleKeys.size(); i++) {
                    indices.addAll(allPossibleKeys.get(i));
                }
            } else {
                for (int i = 1; i < allPossibleKeys.size(); i++) {
                    indices.retainAll(allPossibleKeys.get(i));
                }
            }
            if (!keysToRetain.isEmpty()) {
                indices.retainAll(keysToRetain);
            }
        }
        return indices;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.updateIndexValues(ArrayList<String> allkeys, Entry<String, Object> entry, String datatype, String table)
     */
    public void updateIndexValues(List<String> allkeys, Entry<String, Object> entry, String datatype, String table, String sessionid) throws DatabaseException {
        //Get the necessary DB where the IndexRelation for this column is stored
        String indexTableName = datatype + "Index";
        String remoteTable = remoteDBName + "." + indexTableName;
        Database indexDB = relationToDB.get(remoteTable);
        ArrayList<Integer> IndexentriesToDelete = new ArrayList<Integer>();
        ArrayList<WhereClause> indexWheres = new ArrayList<WhereClause>();
        if (entry.getValue() == null) {
            indexWheres.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, "", RemoteDBConstants.ISNOTNULL));    //If Value is null it may become a problem during the comparism later so exclude it and remove the keys anyway
        }
        int fieldID = tableMapper.getColumnID(table, entry.getKey());
        indexWheres.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, fieldID, "="));
        List<String> selectIndex = new ArrayList<String>();
        selectIndex.add("*");
        List<String> from = new ArrayList<String>();
        from.add(indexTableName);
        try {
            ResultSet res = indexDB.select(selectIndex, from, indexWheres, false, null, true, sessionid);

            updateIndexEntries(res, IndexentriesToDelete, allkeys, entry, indexTableName, indexDB,sessionid);
            //Delete all unecessary entries for this index
            deleteUnnessaryIndexEntries(IndexentriesToDelete, indexTableName, indexDB,sessionid);
            insertIntoIndex(indexTableName, fieldID, entry.getValue(), allkeys, indexDB, sessionid);
        } catch (SQLException e) {
            String msg = "Could not select requested data! \n";
            logger.log(Level.INFO, msg, e);
            //throw new DatabaseException(msg, e);
        } catch (DatabaseException e) {
            String msg = "Could not select requested data from Database! \n";
            logger.log(Level.INFO, msg, e);
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.deleteAttributesFromIndex(String table, ArrayList<String> allkeys, ArrayList<String> escapedColumns, ArrayList<ArrayList<String>> rowsToDelete)
     */
    public void deleteAttributesFromIndex(String table, List<String> allkeys, List<String> escapedColumns, List<List<String>> rowsToDelete,String sessionid) throws DatabaseException {
        if (!tableMapper.containsKey(table)) {
            throw new DatabaseException("Could not find Table " + table + " in Database! \n");
        }
        //Optimize Iteration here -> If small amount of data shall be deleted it is better to iterate over the rows to be deleted
        //if many entries shall be deleted it is better to iterate over the columns of the table where the data shall be deleted from!
        if (allkeys.size() > 10) { // 10 is a sample border here (Should be adjusted) Idea: Indexsize*0.5 < #UpdateZeilen or similar
            //Delete from Index
            HashMap<String, Column> columns = tableMapper.getRemoteColumns(tableMapper.getRemoteTableID(table));
            List<Column> cols = new ArrayList<Column>(columns.values());
            Collections.sort(cols);
            for (Column col : cols) {
                //Get the Index for the column and select all entries from the necessary IndexTable not the logical Table
                WhereClauseBinary fieldWhere = new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, col.getId(), "=");
                ArrayList<WhereClause> where = new ArrayList<WhereClause>();
                where.add(fieldWhere);
                String datatype = col.getType().toString().toLowerCase();
                String indexTableName = datatype + "Index";
                String remoteTable = remoteDBName + "." + indexTableName;
                Database indexDB = relationToDB.get(remoteTable);

                ArrayList<Integer> IndexentriesToDelete = new ArrayList<Integer>();
                try {
                    removeKeysFromIndex(allkeys, IndexentriesToDelete, where, indexTableName, indexDB, sessionid);
                    deleteUnnessaryIndexEntries(IndexentriesToDelete, indexTableName, indexDB,sessionid);
                } catch (SQLException e) {
                    String msg = "Could not select requested data! \n";
                    logger.log(Level.INFO, msg, e);
                    //throw new DatabaseException(msg, e);
                } catch (DatabaseException e) {
                    String msg = "Could not select requested data from Database! \n";
                    logger.log(Level.INFO, msg, e);
                    e.printStackTrace();
                }
            }
        } else {
            //Iterate over them and delete the data from entry step by step
            HashMap<String, Column> cols = tableMapper.getRemoteColumns(tableMapper.getRemoteTableID(table));
            ArrayList<Column> columns = new ArrayList<Column>(cols.values());
            Collections.sort(columns);
            for (List<String> row : rowsToDelete) {
                for (int i = 0; i < row.size(); i++) {
                    Column currentColumn = columns.get(i);
                    String indexTable = currentColumn.getType().toString().toLowerCase() + "Index";
                    String remoteTable = remoteDBName + "." + indexTable;
                    Database indexDB = relationToDB.get(remoteTable);
                    int fieldID = currentColumn.getId();
                    ArrayList<WhereClause> whereIndex = new ArrayList<WhereClause>();
                    whereIndex.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, fieldID, "="));
                    //For the String index both 'null' and null have to be checked
                    boolean checkStringNull = false;
                    if (row.get(i).equals("null") && (escapedColumns.contains(currentColumn.getName()) || currentColumn.getType().toString().toLowerCase().equals("string"))) {
                        checkStringNull = true;
                    }
                    if (row.get(i).equals("null")) {
                        whereIndex.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, "", RemoteDBConstants.ISNULL));
                    } else {
                        whereIndex.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, row.get(i), "="));
                    }
                    try {
                        ArrayList<Integer> IndexentriesToDelete = new ArrayList<Integer>();
                        removeKeysFromIndex(allkeys, IndexentriesToDelete, whereIndex, indexTable, indexDB, sessionid);
                        if (checkStringNull) { //Update Whereclauses to get the 'null' entries in Stringtable
                            whereIndex = new ArrayList<WhereClause>();
                            whereIndex.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, fieldID, "="));
                            whereIndex.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, "'null'", "="));
                            removeKeysFromIndex(allkeys, IndexentriesToDelete, whereIndex, indexTable, indexDB, sessionid);
                        }
                        deleteUnnessaryIndexEntries(IndexentriesToDelete, indexTable, indexDB,sessionid);
                    } catch (SQLException e) {
                        String msg = "Could not select requested data! \n";
                        logger.log(Level.INFO, msg, e);
                        //throw new DatabaseException(msg, e);
                    } catch (DatabaseException e) {
                        String msg = "Could not select requested data from Database! \n";
                        logger.log(Level.INFO, msg, e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.insertIntoIndex()
     */
    @Override
    public void insertIntoIndex(String indexTableName, int fieldID, Object columnValue, List<String> keys, Database indexDB, String sessionid) throws DatabaseException {
        // sql-query
        List<String> select = new ArrayList<String>();
        select.add("*");
        List<String> from = new ArrayList<String>();
        from.add(indexTableName);
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        if (columnValue == null) {
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, "",
                    RemoteDBConstants.ISNULL));
        } else {
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY,
                    columnValue, "="));
        }
        where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID,
                fieldID, "=")); // Identical Data Values can exist in different
        // Tables
        String remoteTable = remoteDBName + "." + indexTableName;
//        Database indexDB = relationToDB.get(remoteTable);
        ResultSet resSet = indexDB.select(select, from, where, false, null, true, sessionid);
        try {
            insertNewValueIntoIndex(resSet, indexDB, keys, columnValue, fieldID, indexTableName,sessionid);
        } catch (SQLException e) {
            String msg = "Insertion into Index " + indexTableName
                    + " of value " + columnValue + " could not be performed";
            logger.log(Level.INFO, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.deleteFromIndexWhere()
     */
    @Override
    public void deleteFromIndexWhere(String indexTableName, List<WhereClause> where, Database indexDB, String sessionid) throws DatabaseException, SQLException {
        deleteFromIndex(indexTableName, where, indexDB,sessionid);
    }

    /**
     * Remove the given Keys from the underlying Indexstructure and give back empty indexentries
     *
     * @param allkeys              the keys to remove
     * @param IndexentriesToDelete the Indexentries which are now empty
     * @param where                WhereClauses defining the entries to delete the keys from
     * @param indexTableName       the name of the index from which shall be deleted
     * @param indexDB              the database from which index shall be deleted
     * @throws SQLException
     * @throws DatabaseException
     */
    protected abstract void removeKeysFromIndex(List<String> allkeys, List<Integer> IndexentriesToDelete, List<WhereClause> where, String indexTableName, Database indexDB, String sessionid) throws SQLException, DatabaseException;

    /**
     * @param res            the Resultset for the general IndexTable
     * @param indexTableName the name of the indexTable where the keys are selected from
     * @param indexDB        the database from which index shall be selected
     * @return the Keys from the indexstructure belonging to the resultset
     * @throws SQLException
     * @throws DatabaseException
     */
    protected abstract List<String> getKeysFromIndex(ResultSet res, String indexTableName, Database indexDB, String sessionid) throws SQLException, DatabaseException;

    /**
     * Delete all keys from an index fullfilling the given whereClauses
     *
     * @param indexTable the indexTableName where shall be deleted from
     * @param where      The WhereClauses defining the entries to delete
     * @param remoteDB   the database where the entries shall be delete from
     * @throws DatabaseException
     * @throws SQLException
     */
    protected abstract void deleteFromIndex(String indexTable, List<WhereClause> where, Database remoteDB, String sessionid) throws DatabaseException, SQLException;

    /**
     * Delete empty Entries from the Index definied by primKeys
     *
     * @param primKeys       The primery Keys of the index identifieing the entries
     * @param indexTableName the Name of the indexTable to delete from
     * @param indexDB        the database to delete from
     * @throws DatabaseException
     */
    protected abstract void deleteUnnessaryIndexEntries(List<Integer> primKeys, String indexTableName, Database indexDB, String sessionid) throws DatabaseException;

    /**
     * Insert a new Value into the Index
     *
     * @param res            the ResultSet selecting the new value from the index to check whether it exists
     * @param indexDB        the database where the new Value shall be inserted into
     * @param keys           the belonging to the new entry
     * @param columnValue    the value which shall be inserted
     * @param fieldID        the fieldID of the value
     * @param indexTableName the name of the index where it shall be inserted into
     * @throws SQLException
     * @throws DatabaseException
     */
    protected abstract void insertNewValueIntoIndex(ResultSet res, Database indexDB, List<String> keys, Object columnValue, int fieldID, String indexTableName, String sessionid) throws SQLException, DatabaseException;

    /**
     * Update the Index entries with the given keys (Deletes the old value from the index)
     *
     * @param res                  Resultset for all values where which can contain the key and where it should be removed from
     * @param IndexentriesToDelete a list of indexentries which are empty after the deletion
     * @param allkeys              the keys which are to be removed
     * @param entry                the new value of the index
     * @param indexTableName       the name of the index the new Value belongs to
     * @param indexDB              the database where it shall be inserted
     * @throws SQLException
     * @throws DatabaseException
     */
    protected abstract void updateIndexEntries(ResultSet res, List<Integer> IndexentriesToDelete, List<String> allkeys, Entry<String, Object> entry, String indexTableName, Database indexDB, String sessionid) throws SQLException, DatabaseException;
}