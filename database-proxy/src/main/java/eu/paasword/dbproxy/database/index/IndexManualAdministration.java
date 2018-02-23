package eu.paasword.dbproxy.database.index;

import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.database.RemoteDBConstants;
import eu.paasword.dbproxy.database.RemoteDBHelper;
import eu.paasword.dbproxy.database.TableMapper;
import eu.paasword.dbproxy.database.utils.*;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.utils.DistributedIndexTablesServer;
import eu.paasword.dbproxy.utils.IDistributedServer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents an IndexManualAdministration (the user manually decides which server each column is stored on)
 *
 * @author Mark Brenner
 */
public class IndexManualAdministration extends IndexTableAdministration {
    private Logger logger = Logger.getLogger(IndexManualAdministration.class.getName());
    private Encryption encrypt;
    /**
     * This instance is used internally to reduce code duplication.
     * There are many methods that would have been copied instead.
     */
    private IndexTableAdministration standardIndexAdministration;


    /**
     * Constructor for the IndexAdministration using the original approach
     *
     * @param relationDB   the Map which stores the information in which physical database which table is saved
     * @param remoteHelper the remoteHelper for this database giving standard private methods for the remote tables
     * @param encryption   the encryption module which shall be used
     * @param DBName       the name of the remote database where the index tables are stored
     */
    public IndexManualAdministration(Map<String, Database> relationDB, RemoteDBHelper remoteHelper, Encryption encryption, String DBName) {
        super(relationDB, remoteHelper, DBName);
        encrypt = encryption;
        standardIndexAdministration = new IndexStandardAdministration(relationDB, remoteHelper, encryption, DBName);
    }

    /*
 * (non-Javadoc)
 *
 * @see edu.kit.iks.sdp.database.index.IndexAdministration.deleteAttributesFromIndex(String table, ArrayList<String> allkeys, ArrayList<String> escapedColumns, ArrayList<ArrayList<String>> rowsToDelete)
 */
    private void deleteAttributesFromIndexDistributed(String table, List<String> allkeys, List<String> escapedColumns, List<List<String>> rowsToDelete,String sessionid) throws DatabaseException {
        if (tableMapper.containsKey(table)) {
            if (allkeys.size() > 10) { // 10 is a sample border here (Should be adjusted) Idea: Indexsize*0.5 < #UpdateZeilen or similar
                deleteAttributesSmallKeySize(table, allkeys, escapedColumns, rowsToDelete,sessionid);
            } else {
                deleteAttributesLargeKeySize(table, allkeys, escapedColumns, rowsToDelete,sessionid);
            }
        }
    }

    /**
     * If many entries shall be deleted it is better to iterate over the columns of the table where the data shall be deleted from.
     * This approach is implemented in this method.
     * If small amount of data shall be deleted it is better to iterate over the rows to be deleted (see {@link #deleteAttributesSmallKeySize(String, List, List, List)}.
     *
     * @param table
     * @param allkeys
     * @param escapedColumns
     * @param rowsToDelete
     * @throws DatabaseException
     */
    private void deleteAttributesLargeKeySize(String table, List<String> allkeys, List<String> escapedColumns, List<List<String>> rowsToDelete,String sessionid) throws DatabaseException {
        //Iterate over them and delete the data from entry step by step
        HashMap<String, Column> cols = tableMapper.getRemoteColumns(tableMapper.getRemoteTableID(table));
        ArrayList<Column> columns = new ArrayList<Column>(cols.values());
        Collections.sort(columns);
        for (List<String> row : rowsToDelete) {
            for (int i = 0; i < row.size(); i++) {

                Column currentColumn = columns.get(i);
                String dataType = currentColumn.getType().toString().toLowerCase();
                String indexTable = dataType + "Index";
                IDistributedServer indexServerDAO = getServerBy(TableMapper.getInstance().getRemoteTableID(table), currentColumn.getName(),sessionid);
                String remoteTable = indexServerDAO.getServerName() + "." + indexTable;
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
                    List<Integer> indexEntriesToDelete = new ArrayList<Integer>();
                    removeKeysFromIndex(allkeys, indexEntriesToDelete, whereIndex, indexTable, indexDB,sessionid);
                    if (checkStringNull) { //Update Whereclauses to get the 'null' entries in Stringtable
                        whereIndex = new ArrayList<WhereClause>();
                        whereIndex.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, fieldID, "="));
                        whereIndex.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, "'null'", "="));
                        removeKeysFromIndex(allkeys, indexEntriesToDelete, whereIndex, indexTable, indexDB,sessionid);
                    }
                    deleteUnnessaryIndexEntries(indexEntriesToDelete, indexTable, indexDB,sessionid);
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

    /**
     * If small amount of data shall be deleted it is better to iterate over the rows to be deleted.
     * This approach is implemented in this method.
     * If many entries shall be deleted it is better to iterate over the columns of the table where the data shall be deleted from (see {@link #deleteAttributesLargeKeySize(String, List, List, List)}.
     *
     * @param table
     * @param allkeys
     * @param escapedColumns
     * @param rowsToDelete
     * @throws DatabaseException
     */
    private void deleteAttributesSmallKeySize(String table, List<String> allkeys, List<String> escapedColumns, List<List<String>> rowsToDelete,String sessionid) throws DatabaseException {
        HashMap<String, Column> columns = tableMapper.getRemoteColumns(tableMapper.getRemoteTableID(table));
        List<Column> cols = new ArrayList<Column>(columns.values());
        Collections.sort(cols);
        for (Column col : cols) {
            //Get the Index for the column and select all entries from the necessary IndexTable not the logical Table
            WhereClauseBinary fieldWhere = new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, col.getId(), "=");
            ArrayList<WhereClause> where = new ArrayList<WhereClause>();
            where.add(fieldWhere);

            String dataType = col.getType().toString().toLowerCase();
            String indexTable = dataType + "Index";
            IDistributedServer indexServerDAO = getServerBy(TableMapper.getInstance().getRemoteTableID(table), col.getName(),sessionid);
            String remoteTable = indexServerDAO.getServerName() + "." + indexTable;
            Database indexDB = relationToDB.get(remoteTable);

            List<Integer> indexEntriesToDelete = new ArrayList<Integer>();
            try {
                removeKeysFromIndex(allkeys, indexEntriesToDelete, where, indexTable, indexDB,sessionid);
                deleteUnnessaryIndexEntries(indexEntriesToDelete, indexTable, indexDB,sessionid);
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
    }    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.selectAllKeys(int tableID)
     */

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.deleteAllFromIndex(int tableID)
     */
    public List<String> deleteAllFromIndex(int tableID,String sessionid) throws DatabaseException {
        return deleteAllFromIndexDistributed(tableID,sessionid);
    }

    /**
     * Delete all entries from a table in all distributed index tables/servers.
     *
     * @param tableID
     * @return
     * @throws DatabaseException
     */
    private List<String> deleteAllFromIndexDistributed(int tableID,String sessionid) throws DatabaseException {
        List<String> deletedIndices = selectAllKeys(tableID,sessionid);
        HashMap<String, Column> columns = tableMapper.getRemoteColumns(tableID);
        if (columns != null) {
            Set<String> columnNames = columns.keySet();
            for (String columnID : columnNames) {
                Column columnFields = columns.get(columnID);

                String dataType = columnFields.getType().toString().toLowerCase();
                String indexTableName = dataType + "Index";
                IDistributedServer indexServerDAO = getServerBy(tableID, columnFields.getName(),sessionid);
                String remoteTable = indexServerDAO.getServerName() + "." + indexTableName;
                Database indexDB = relationToDB.get(remoteTable);

                List<WhereClause> where = new ArrayList<WhereClause>();
                where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, columnFields.getId(), "="));
                deleteFromIndex(indexTableName, where, indexDB,sessionid);
            }
        }
        return deletedIndices;
    }

    @Override
    public List<String> selectAllKeys(int tableID,String sessionid) throws DatabaseException {
        return selectAllKeysDistributed(tableID,sessionid);
    }

    /**
     * Get the server on which the column of the table is stored.
     *
     * @param tableID    The table
     * @param columnName The column name
     * @return A DAO object that contains the necessary information to retrieve the actual {@link Database} object through relationToDB. Null can be returned if there was an exception
     * @throws DatabaseException
     */
    private IDistributedServer getServerBy(int tableID, final String columnName,String sessionid) throws DatabaseException {
        String tableName = tableMapper.getRemoteTableName(tableID);
        int columnID = tableMapper.getColumnID(tableName, columnName);

        //	String columnName = columns.get(columnID).getName().toLowerCase();
        Database columnServerMapping = relationToDB.get(remoteDBName + "." + RemoteDBConstants.COLUMN_SERVER_MAPPING);

        //Set up the Query
        List<String> select = new ArrayList<String>();
        select.add("*");
        List<String> from2 = new ArrayList<String>();
        from2.add(RemoteDBConstants.COLUMN_SERVER_MAPPING);
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        where.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_SERVER_ID, columnID, "="));
        // select all which satisfy
        IDistributedServer server = null;
        try {
            ResultSet res = columnServerMapping.select(select, from2, where, true, null, false,sessionid);
            String serverId = null;
            String serverName = null;
            while (res.next()) {
                // careful if res has more than 1 element!!!
                serverId = res.getString(RemoteDBConstants.COLUMN_SERVER_SERVERID);
                serverName = res.getString((RemoteDBConstants.COLUMN_SERVER_SERVERNAME));
            }
            server = new DistributedIndexTablesServer(Integer.parseInt(serverId), serverName);
        } catch (SQLException e) {
            String msg = "Could not select requested data";
            logger.log(Level.INFO, msg, e);
            throw new DatabaseException(msg, e);
        }
        return server;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.deleteFromIndex()
     */
    @Override
    protected void deleteFromIndex(String indexTable, List<WhereClause> where, Database indexDB,String sessionid) throws DatabaseException {
        try {
            getDefaultIndexAdministration().deleteFromIndex(indexTable, where, indexDB,sessionid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<String> selectAllKeysDistributed(int tableID,String sessionid) throws DatabaseException {

        List<String> result = new ArrayList<String>();
        Map<String, Column> columns = tableMapper.getRemoteColumns(tableID);
        if (columns != null) {
            List<String> columnIDs = new ArrayList<String>(columns.keySet());

            //Only first Column is needed since all entries should have it!
            int datatypeID = columns.get(columnIDs.get(0)).getId();

            String dataType = columns.get(columnIDs.get(0)).getType().toString().toLowerCase();
            String indexTableName = dataType + "Index";
            IDistributedServer indexServerDAO = getServerBy(tableID, columns.get(columnIDs.get(0)).getName(),sessionid);
            String remoteTable = indexServerDAO.getServerName() + "." + indexTableName;
            Database indexServer = relationToDB.get(remoteTable);

            //Set up the Query
            List<String> select = new ArrayList<String>();
            select.add("*");
            List<String> from = new ArrayList<String>();
            from.add(indexTableName);
            List<WhereClause> where = new ArrayList<WhereClause>();
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, datatypeID, "="));
            // select all from datatype where field_id=datatypeID
            ResultSet res = indexServer.select(select, from, where, false, null, false,sessionid);
            try {
                result = getKeysFromIndex(res, indexTableName, indexServer,sessionid);
            } catch (SQLException e) {
                String msg = "Could not select requested data";
                logger.log(Level.INFO, msg, e);
                throw new DatabaseException(msg, e);
            }
        }
        return result;
    }

    /**
     * Get the default index administration.
     * We use this instance to delegate to implementations that allready exist.
     *
     * @return An instance of {@link IndexTableAdministration}
     */
    private IndexTableAdministration getDefaultIndexAdministration() {
        if (null == standardIndexAdministration) {
            throw new IllegalStateException("Default index administration is null!");
        }
        return standardIndexAdministration;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.getKeysFromIndex()
     */
    @Override
    protected List<String> getKeysFromIndex(ResultSet res, String indexTableName, Database indexDB,String sessionid) throws SQLException {
        List<String> keysToBeReturned = new ArrayList<>();
        try {
            keysToBeReturned = getDefaultIndexAdministration().getKeysFromIndex(res, indexTableName, indexDB,sessionid);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        return keysToBeReturned;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.selectKeys(String from, List<WhereClause> where, List<String> keysToRetain)
     */
    public List<String> selectKeys(String from, List<WhereClause> where, List<String> keysToRetain, boolean or, String sessionid)
            throws DatabaseException {
        return selectKeysDistributed(from, where, keysToRetain, or,sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.selectKeys(String from, List<WhereClause> where, List<String> keysToRetain)
     */
    private List<String> selectKeysDistributed(String from, List<WhereClause> where, List<String> keysToRetain, boolean or,String sessionid) throws DatabaseException {
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
        List<List<String>> allPossibleKeys = new ArrayList<List<String>>();

        for (WhereClause clause : newWhere) {
            // select keys of condition
            ArrayList<WhereClause> singleWhere = new ArrayList<WhereClause>();
            singleWhere.add(clause);
            ResultSet res = helper.selectSingleDataDistributed(from, singleWhere, clause.getLeftOperand(), getServerWhereColumnIsStored(tableMapper.getRemoteTableID(from), clause.getLeftOperand(),sessionid),sessionid);
            Column column = helper.getColumnFromName(from, clause.getLeftOperand());

            IDistributedServer indexServerDAO = getServerBy(TableMapper.getInstance().getRemoteTableID(from), column.getName(),sessionid);

            String type = column.getType().toString().toLowerCase();
            String datatypeIndexTable = type + "Index";
            String remoteTable = indexServerDAO.getServerName() + "." + datatypeIndexTable;

            Database indexDB = relationToDB.get(remoteTable);
            try {
                List<String> keys = getKeysFromIndex(res, datatypeIndexTable, indexDB, sessionid);

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

    /**
     * Get the unique server id of the server on which the column of the table is stored.
     *
     * @param tableID    The table name
     * @param columnName The column name
     * @return The id. Zero is returned if an exception occurs.
     * @throws DatabaseException
     */
    private int getServerWhereColumnIsStored(int tableID, String columnName, String sessionid) throws DatabaseException {
        String tableName = tableMapper.getRemoteTableName(tableID);
        int columnID = tableMapper.getColumnID(tableName, columnName);

        //	String columnName = columns.get(columnID).getName().toLowerCase();
        Database columnServerMapping = relationToDB.get(remoteDBName + "." + RemoteDBConstants.COLUMN_SERVER_MAPPING);

        //Set up the Query
        List<String> select = new ArrayList<String>();
        select.add("*");
        List<String> from2 = new ArrayList<String>();
        from2.add(RemoteDBConstants.COLUMN_SERVER_MAPPING);
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        where.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_SERVER_ID, columnID, "="));
        // select all which satisfy
        int resultServer = 0;
        try {
            ResultSet res = columnServerMapping.select(select, from2, where, false, null, false, sessionid);
            String s = "";
            while (res.next()) {
                // careful if res has more than 1 element!!!
                s = res.getString(RemoteDBConstants.COLUMN_SERVER_SERVERID);
            }
            resultServer = Integer.parseInt(s);
        } catch (SQLException e) {
            String msg = "Could not select requested data";
            logger.log(Level.INFO, msg, e);
            throw new DatabaseException(msg, e);
        }
        return resultServer;

    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.insertIndexValue(String tableName, int maxID,
            String columnName, Object columnValue)
     */
    public void insertIndexValue(String tableName, int maxID,String columnName, Object columnValue,String sessionid) throws DatabaseException {
        insertIndexValueDistributed(tableName, maxID, columnName, columnValue,sessionid);
    }

    /**
     * * This is the pendant to the {@link #insertIndexValue(String, int, String, Object)} method of this class.
     * It is used to parallely design the actual distribution while keeping the current functionality at work.
     * It will replace the {@link #insertIndexValue(String, int, String, Object)} method if everything is finished.
     *
     * @param tableName
     * @param maxID
     * @param columnName
     * @param columnValue
     * @throws DatabaseException
     */
    private void insertIndexValueDistributed(String tableName, int maxID,String columnName, Object columnValue,String sessionid) throws DatabaseException {

        // get Stuff from datastructures
        int tableID = getTableID(tableName);
        int columnID = getColumnID(columnName, tableID);

        IDistributedServer server = getServerBy(tableID, columnName,sessionid);

        String datatype = tableMapper.getColumnDataType(tableName, columnName).toString().toLowerCase();
        String dataIndexTableName = datatype + "Index";
        String remoteIndexTable = server.getServerName() + "." + dataIndexTableName;

        Database remoteIndexDatabase = relationToDB.get(remoteIndexTable);


        // sql-query
        List<String> select = new ArrayList<String>();
        select.add("*");
        List<String> from = new ArrayList<String>();
        from.add(dataIndexTableName);

        List<WhereClause> where = new ArrayList<WhereClause>();
        if (columnValue == null) {
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, "",
                    RemoteDBConstants.ISNULL));
        } else {
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY,
                    columnValue, "="));
        }
        where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID,
                columnID, "=")); // Identical Data Values can exist in different


        ResultSet resSetRemoteInex = remoteIndexDatabase.select(select, from, where, false, null, true,sessionid);
        try {
            ArrayList<String> keys = new ArrayList<String>();
            keys.add(Integer.toString(maxID));
            insertNewValueIntoIndex(resSetRemoteInex, remoteIndexDatabase, keys, columnValue, columnID, dataIndexTableName,sessionid);
        } catch (SQLException e) {
            String msg = "Insert could not be performed";
            logger.log(Level.INFO, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    private int getTableID(final String tableName) throws DatabaseException {
        if (tableMapper.containsKey(tableName)) {
            return tableMapper.getRemoteTableID(tableName);
        } else {
            throw new DatabaseException("Could not find Table " + tableName
                    + " in Database! \n");
        }
    }

    private int getColumnID(final String columnName, final int tableID) throws DatabaseException {
        HashMap<String, Column> columns = tableMapper.getRemoteColumns(tableID);
        if (columns.containsKey(columnName)) {
            return (int) columns.get(columnName).getId();
        } else {
            throw new DatabaseException("Could not find Column " + columnName);

        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.insertNewValueIntoIndex()
     */
    @Override
    protected void insertNewValueIntoIndex(ResultSet res, Database indexDB, List<String> keys, Object columnValue, int fieldID, String indexTableName,String sessionid) throws SQLException, DatabaseException {
        getDefaultIndexAdministration().insertNewValueIntoIndex(res, indexDB, keys, columnValue, fieldID, indexTableName,sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.updateIndexValues(ArrayList<String> allkeys, Entry<String, Object> entry, String datatype, String table)
     */
    public void updateIndexValues(List<String> allkeys, Entry<String, Object> entry, String dataType, String table,String sessionid) throws DatabaseException {
        updateIndexValuesDistributed(allkeys, entry, dataType, table, sessionid);
    }

    /**
     * Update the values of the keywords of the selected keys (allKeys) of the row.
     *
     * @param allkeys  The affected keys
     * @param entry    The actual value that is changed (e.g. SET columnName=NewValue)
     * @param dataType The data type to find the index table
     * @param table    The table
     * @throws DatabaseException
     */
    private void updateIndexValuesDistributed(List<String> allkeys, Entry<String, Object> entry, String dataType, String table, String sessionid) throws DatabaseException {

        String indexTableName = dataType + "Index";
        IDistributedServer indexServerDAO = getServerBy(TableMapper.getInstance().getRemoteTableID(table), entry.getKey().toLowerCase(),sessionid);
        String remoteTable = indexServerDAO.getServerName() + "." + indexTableName;
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

            ResultSet res = indexDB.select(selectIndex, from, indexWheres, false, null, true,sessionid);
            updateIndexEntries(res, IndexentriesToDelete, allkeys, entry, indexTableName, indexDB,sessionid);
            //Delete all unecessary entries for this index
            deleteUnnessaryIndexEntries(IndexentriesToDelete, indexTableName, indexDB,sessionid);
            insertIntoIndex(indexTableName, fieldID, entry.getValue(), allkeys, indexDB,sessionid);

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
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.updateIndexEntries()
     */
    @Override
    protected void updateIndexEntries(ResultSet res, List<Integer> indexEntriesToDelete, List<String> allkeys, Entry<String, Object> entry, String indexTableName, Database indexDB, String sessionid) throws SQLException, DatabaseException {
        getDefaultIndexAdministration().updateIndexEntries(res, indexEntriesToDelete, allkeys, entry, indexTableName, indexDB,sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.deleteUnnessaryIndexEntries()
     */
    @Override
    protected void deleteUnnessaryIndexEntries(List<Integer> primKeys, String indexTableName, Database indexDB, String sessionid) throws DatabaseException {
        getDefaultIndexAdministration().deleteUnnessaryIndexEntries(primKeys, indexTableName, indexDB, sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.deleteAttributesFromIndex(String table, ArrayList<String> allkeys, ArrayList<String> escapedColumns, ArrayList<ArrayList<String>> rowsToDelete)
     */
    public void deleteAttributesFromIndex(String table, List<String> allkeys, List<String> escapedColumns, List<List<String>> rowsToDelete,String sessionid) throws DatabaseException {
        deleteAttributesFromIndexDistributed(table, allkeys, escapedColumns, rowsToDelete, sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.removeKeysFromIndex()
     */
    @Override
    protected void removeKeysFromIndex(List<String> allkeys, List<Integer> indexEntriesToDelete, List<WhereClause> where, String indexTableName, Database indexDB,String sessionid) throws SQLException, DatabaseException {
        getDefaultIndexAdministration().removeKeysFromIndex(allkeys, indexEntriesToDelete, where, indexTableName, indexDB,sessionid);
    }
}
