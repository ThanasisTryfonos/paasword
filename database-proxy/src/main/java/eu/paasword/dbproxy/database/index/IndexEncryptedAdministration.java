package eu.paasword.dbproxy.database.index;

import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.database.RemoteDBConstants;
import eu.paasword.dbproxy.database.RemoteDBHelper;
import eu.paasword.dbproxy.database.utils.*;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.encryption.IndexEncryption;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which represents the administration for an encrypted index
 *
 * @author Kateryna Yurchenko
 */
public class IndexEncryptedAdministration extends IndexTableAdministration {

    private Encryption encrypt;
    private IndexEncryption indexEncryption;
    private Logger logger = Logger.getLogger("prototype.database.IndexEncryptedAdministration");

    /**
     * Constructor for the IndexEncryptedAdministration using the original approach
     *
     * @param relationDB   the Map which stores the information in which physical database which table is saved
     * @param remoteHelper the remoteHelper for this database giving standard private methods for the remote tables
     * @param encryption   the encryption module which shall be used
     * @param DBName       the name of the remote database where the index tables are stored
     */
    public IndexEncryptedAdministration(Map<String, Database> relationDB,RemoteDBHelper remoteHelper, Encryption encryption, String DBName) {
        super(relationDB, remoteHelper, DBName);
        encrypt = encryption;
        indexEncryption = new IndexEncryption();
    }


    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.selectKeys(String from, List<WhereClause> where, List<String> keysToRetain)
     */
    public List<String> selectKeys(String from, List<WhereClause> where, List<String> keysToRetain, boolean or,String sessionid)
            throws DatabaseException {
        // remove alias from map, joins could not occur because only one table is processed.
        boolean allMustBeSelected = false;
        ArrayList<WhereClause> newWhere = new ArrayList<WhereClause>();
        for (WhereClause clause : where) {
            String key = clause.getLeftOperand();
            String newKey = key.substring(key.lastIndexOf(".") + 1, key.length());
            if (clause instanceof WhereClauseBinary) {
                WhereClauseBinary bin = (WhereClauseBinary) clause;
                Type type = Type.customValueOf(bin.getRightOperand().getClass().getSimpleName());
                if (bin.getOperator().equals("=") || bin.getOperator().equals("<>")) {
                    newWhere.add(new WhereClauseBinary(newKey, indexEncryption.encrypt(bin.getRightOperand(), type), bin.getOperator())); //Only one value operand used
                } else {
                    allMustBeSelected = true;
                }
            } else if (clause instanceof WhereClauseIn) {
                ArrayList<Object> newIn = new ArrayList<Object>();

                if (clause instanceof WhereClauseNotIn) {
                    WhereClauseNotIn in = (WhereClauseNotIn) clause;
                    List<Object> ins = in.getIn();
                    for (Object i : ins) {
                        Type type = Type.customValueOf(i.getClass().getSimpleName());
                        newIn.add(indexEncryption.encrypt(i, type));
                    }
                    newWhere.add(new WhereClauseNotIn(newKey, newIn));
                } else {
                    WhereClauseIn in = (WhereClauseIn) clause;
                    List<Object> ins = in.getIn();
                    for (Object i : ins) {
                        Type type = Type.customValueOf(i.getClass().getSimpleName());
                        newIn.add(indexEncryption.encrypt(i, type));
                    }
                    newWhere.add(new WhereClauseIn(newKey, newIn));
                }
            }
        }
        // For each entry in where, the rows must be selected
        List<List<String>> allPossibleKeys = new ArrayList<List<String>>();

        if (!allMustBeSelected) {
            for (WhereClause clause : newWhere) {
                // select keys of condition
                ArrayList<WhereClause> singleWhere = new ArrayList<WhereClause>();
                singleWhere.add(clause);
                ResultSet res = helper.selectSingleData(from, singleWhere, clause.getLeftOperand(),sessionid);
                Column column = helper.getColumnFromName(from, clause.getLeftOperand());
                String indexTableName = column.getType().toString().toLowerCase() + "Index";
                String remoteTable = remoteDBName + "." + indexTableName;
                Database indexDB = relationToDB.get(remoteTable);
                try {
                    List<String> keys = getKeysFromIndex(res, indexTableName, indexDB,sessionid);
                    allPossibleKeys.add(keys);
                } catch (SQLException e) {
                    String msg = "Index table could not be accessed! \n";
                    logger.log(Level.SEVERE, msg, e);
                    throw new DatabaseException(msg, e);
                }

            }
        } else {
            int tableID = 0;
            tableID = tableMapper.getRemoteTableID(from);
            return selectAllKeys(tableID,sessionid);
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
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.insertIndexValue(String tableName, int maxID,
            String columnName, Object columnValue)
     */
    @Override
    public void insertIndexValue(String tableName, int maxID, String columnName, Object columnValue, String sessionid) throws DatabaseException {

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
        Object encryptedColumnValue;
        Type type = columns.get(columnName).getType();
        if (columnValue == null) {
            encryptedColumnValue = indexEncryption.encrypt("null", type); //TODO
        } else {
            encryptedColumnValue = indexEncryption.encrypt(columnValue, type);
        }
        where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, encryptedColumnValue, "="));
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
            insertNewValueIntoIndex(resSet, indexDB, keys, encryptedColumnValue, columnID, datatypeIndex,sessionid);
        } catch (SQLException e) {
            String msg = "Insert could not be performed";
            logger.log(Level.INFO, msg, e);
            throw new DatabaseException(msg, e);
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.removeKeysFromIndex()
     */
    @Override
    protected void removeKeysFromIndex(List<String> allkeys, List<Integer> IndexentriesToDelete,List<WhereClause> where, String indexTableName, Database indexDB,String sessionid)
            throws SQLException, DatabaseException {
        List<String> select = new ArrayList<String>();
        select.add(RemoteDBConstants.INDEX_PRIMKEY);
        select.add(RemoteDBConstants.INDEX_VALUE);
        List<String> from = new ArrayList<String>();
        from.add(indexTableName);
        ResultSet res = indexDB.select(select, from, where, false, null, true,sessionid);
        while (res.next()) {
            //delete the previously collected keys from the entry
            String s = res.getString(RemoteDBConstants.INDEX_VALUE);
            s = encrypt.decrypt(s);
            ArrayList<String> keys = lexer.splitDecryptedString(s);
            int sizeBefore = keys.size();
            keys.removeAll(allkeys);
            int sizeAfter = keys.size();
            if (sizeBefore != sizeAfter) { //Only do the Work if something changed e.g. the size of the keylist changed
                int indexKey = res.getInt(RemoteDBConstants.INDEX_PRIMKEY);
                if (keys.isEmpty()) {
                    IndexentriesToDelete.add(indexKey);
                } else {
                    where = new ArrayList<WhereClause>();
                    where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_PRIMKEY, res.getString(RemoteDBConstants.INDEX_PRIMKEY), "="));
                    HashMap<String, Object> updateData = new HashMap<String, Object>();
                    updateData.put(RemoteDBConstants.INDEX_VALUE, encrypt.encrypt(lexer.joinDecryptedString(keys)));
                    indexDB.update(indexTableName, updateData, where, true,sessionid);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.getKeysFromIndex()
     */
    @Override
    protected List<String> getKeysFromIndex(ResultSet res, String indexTableName, Database indexDB,String sessionid)
            throws SQLException, DatabaseException {
        ArrayList<String> result = new ArrayList<String>();
        while (res.next()) {
            String s = res.getString(RemoteDBConstants.INDEX_VALUE);
            s = encrypt.decrypt(s);
            result.addAll(lexer.splitDecryptedString(s));
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.deleteFromIndex()
     */
    @Override
    protected void deleteFromIndex(String indexTable, List<WhereClause> where, Database remoteDB,String sessionid) throws DatabaseException, SQLException {
        remoteDB.delete(indexTable, where,sessionid);

    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.deleteUnnessaryIndexEntries()
     */
    @Override
    protected void deleteUnnessaryIndexEntries(List<Integer> primKeys, String indexTableName, Database indexDB, String sessionid) throws DatabaseException {
        if (!primKeys.isEmpty()) {
            ArrayList<WhereClause> deleteWhere = new ArrayList<WhereClause>();
            deleteWhere.add(new WhereClauseIn(RemoteDBConstants.INDEX_PRIMKEY, new ArrayList<Object>(primKeys)));
            indexDB.delete(indexTableName, deleteWhere,sessionid);
        }

    }


    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.insertIntoIndex()
     */
    @Override
    public void insertIntoIndex(String indexTableName, int fieldID, Object columnValue, List<String> keys, Database indexDB,String sessionid) throws DatabaseException {
        // sql-query
        List<String> select = new ArrayList<String>();
        select.add("*");
        List<String> from = new ArrayList<String>();
        from.add(indexTableName);
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        if (columnValue == null) {
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, "null",
                    "="));
        } else {
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY,
                    columnValue, "="));
        }
        where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID,
                fieldID, "=")); // Identical Data Values can exist in different
        // Tables
        String remoteTable = remoteDBName + "." + indexTableName;
//		Database indexDB = relationToDB.get(remoteTable);
        ResultSet resSet = indexDB.select(select, from, where, false, null, true,sessionid);
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
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.insertNewValueIntoIndex()
     */
    @Override
    protected void insertNewValueIntoIndex(ResultSet res, Database indexDB, List<String> keys, Object columnValue,int fieldID, String indexTableName,String sessionid) throws SQLException, DatabaseException {
        if (res.next()) {
            // if there are already entries with given value in the column:
            // get this entry, decrypt it, add new value, encrypt it and
            // update
            String data = res.getString(RemoteDBConstants.INDEX_VALUE);

            data = encrypt.decrypt(data);
            ArrayList<String> tmp = lexer.splitDecryptedString(data);
            tmp.addAll(keys);
            data = lexer.joinDecryptedString(tmp);
            data = encrypt.encrypt(data);
            ArrayList<WhereClause> where = new ArrayList<WhereClause>();
            where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_PRIMKEY, res.getString(RemoteDBConstants.INDEX_PRIMKEY), "="));
            HashMap<String, Object> updateData = new HashMap<String, Object>();
            updateData.put(RemoteDBConstants.INDEX_VALUE, data);
            indexDB.update(indexTableName, updateData, where, true,sessionid);
        } else {
            // 3.2: Sonst: id als string, encrypten, insert
            // 3.2.1. MaxID aus IndexTable holen
            // select max(ID) from indexTable

            String indexInsert = encrypt.encrypt(lexer.joinDecryptedString(keys));
            List<String> columnsInsert = new ArrayList<String>();
            columnsInsert.add(RemoteDBConstants.INDEX_PRIMKEY);
            columnsInsert.add(RemoteDBConstants.INDEX_FIELD_ID);
            columnsInsert.add(RemoteDBConstants.INDEX_KEY);
            columnsInsert.add(RemoteDBConstants.INDEX_VALUE);

            List<Object> valuesInsert = new ArrayList<Object>();
            int id = 0;
            if (tableMaxID.containsKey(indexTableName)) {
                id = tableMaxID.get(indexTableName) + 1;
            } else {
                id = helper.selectMaxIDPlusOne(indexTableName, indexDB,RemoteDBConstants.INDEX_PRIMKEY,sessionid);
            }
            tableMaxID.put(indexTableName, id);
            valuesInsert.add(id);
            valuesInsert.add(fieldID);
            if (columnValue == null || columnValue.equals("null")) {
                valuesInsert.add(indexEncryption.encrypt("null", Type.String));
            } else {
                valuesInsert.add(columnValue);
            }
            valuesInsert.add(indexInsert);
            indexDB.insert(indexTableName, columnsInsert, valuesInsert, true,sessionid);
        }

    }


    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexAdministration.updateIndexValues(ArrayList<String> allkeys, Entry<String, Object> entry, String datatype, String table)
     */
    public void updateIndexValues(List<String> allkeys, Entry<String, Object> entry, String datatype, String table, Database indexDB,String sessionid) throws DatabaseException {
        //Get the necessary DB where the IndexRelation for this column is stored
        String indexTableName = datatype + "Index";
        String remoteTable = remoteDBName + "." + indexTableName;
//		Database indexDB = relationToDB.get(remoteTable);
        ArrayList<Integer> IndexentriesToDelete = new ArrayList<Integer>();
        ArrayList<WhereClause> indexWheres = new ArrayList<WhereClause>();
        if (entry.getValue() == null) {
            indexWheres.add(new WhereClauseBinary(RemoteDBConstants.INDEX_KEY, "", RemoteDBConstants.ISNOTNULL));    //If Value is null it may become a problem during the comparism later so exclude it and remove the keys anyway
        }
        Type encrType;
        if (entry.getValue() != null) {
            encrType = Type.customValueOf(entry.getValue().getClass().getSimpleName());
        } else {
            encrType = Type.String;
        }
        Entry<String, Object> encrEntry = entry;
        encrEntry.setValue(indexEncryption.encrypt(entry.getValue(), encrType));
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
    protected void updateIndexEntries(ResultSet res, List<Integer> IndexentriesToDelete, List<String> allkeys,Entry<String, Object> entry, String indexTableName, Database indexDB,String sessionid)
            throws SQLException, DatabaseException {
        ArrayList<String> keys = new ArrayList<String>();
        while (res.next()) {
            String s = res.getString(RemoteDBConstants.INDEX_VALUE);
            s = encrypt.decrypt(s);
            keys = lexer.splitDecryptedString(s);
            int sizeBefore = keys.size();
            Object value = res.getObject(RemoteDBConstants.INDEX_KEY);
            //if the selected value is null then it can not be in the update data as it would have been excluded before
            if (entry.getValue() == null || value == null) {
                keys.removeAll(allkeys);
            } else if (!value.equals(entry.getValue())) { //Update only if the selected value is not in the update data
                keys.removeAll(allkeys);
            }
            int sizeAfter = keys.size();
            if (sizeBefore != sizeAfter) { //Only do the Work if something changed e.g. the size of the keylist changed
                int indexKey = res.getInt(RemoteDBConstants.INDEX_PRIMKEY);
                if (keys.isEmpty()) {
                    //Collect the rows to delete and delete them all at once later -> reduces the accesses to the remote DB
                    IndexentriesToDelete.add(indexKey);
                } else {
                    ArrayList<WhereClause> where = new ArrayList<WhereClause>();
                    where.add(new WhereClauseBinary(RemoteDBConstants.INDEX_PRIMKEY, res.getString(RemoteDBConstants.INDEX_PRIMKEY), "="));
                    HashMap<String, Object> updateData = new HashMap<String, Object>();
                    updateData.put(RemoteDBConstants.INDEX_VALUE, encrypt.encrypt(lexer.joinDecryptedString(keys)));
                    indexDB.update(indexTableName, updateData, where, true,sessionid);
                }
            }
        }
    }

}//EoC
