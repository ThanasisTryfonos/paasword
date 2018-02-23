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
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.database.utils.WhereClauseBinary;
import eu.paasword.dbproxy.database.utils.WhereClauseIn;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;


/**
 * This class represents an IndexAdministration as ist was designed in the beginning, e.g. severel indexTables controlled by this class
 *
 * @author Mark Brenner
 */
public class IndexStandardAdministration extends IndexTableAdministration {
    private Logger logger = Logger.getLogger("prototype.database.IndexStandardAdministration");
    private Encryption encrypt;

    /**
     * Constructor for the IndexAdministration using the original approach
     *
     * @param relationDB   the Map which stores the information in which physical database which table is saved
     * @param remoteHelper the remoteHelper for this database giving standard private methods for the remote tables
     * @param encryption   the encryption module which shall be used
     * @param DBName       the name of the remote database where the index tables are stored
     */
    public IndexStandardAdministration(Map<String, Database> relationDB, RemoteDBHelper remoteHelper, Encryption encryption, String DBName) {
        super(relationDB, remoteHelper, DBName);
        encrypt = encryption;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.removeKeysFromIndex()
     */
    @Override
    protected void removeKeysFromIndex(List<String> allkeys, List<Integer> IndexentriesToDelete, List<WhereClause> where, String indexTableName, Database indexDB,String sessionid) throws SQLException, DatabaseException {
        List<String> select = new ArrayList<String>();
        select.add(RemoteDBConstants.INDEX_PRIMKEY);
        select.add(RemoteDBConstants.INDEX_VALUE);
        List<String> from = new ArrayList<String>();
        from.add(indexTableName);
        ResultSet res = indexDB.select(select, from, where, false, null, true, sessionid);
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
    protected List<String> getKeysFromIndex(ResultSet res, String indexTableName, Database indexDB,String sessionid) throws SQLException {
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
    protected void deleteFromIndex(String indexTable, List<WhereClause> where, Database remoteDB, String sessionid) throws DatabaseException {
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
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.insertNewValueIntoIndex()
     */
    @Override
    protected void insertNewValueIntoIndex(ResultSet res, Database indexDB, List<String> keys, Object columnValue, int fieldID, String indexTableName, String sessionid) throws SQLException, DatabaseException {
        if (res.next()) {
            // if there are already entries with given value in the column:
            // get this entry, decrypt it, add new value, encrypt it and
            // update
            String data = res.getString(RemoteDBConstants.INDEX_VALUE);

            data = encrypt.decrypt(data);
            ArrayList<String> tmp = lexer
                    .splitDecryptedString(data);
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

            String indexInsert = encrypt.encrypt(
                    lexer.joinDecryptedString(keys));

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
            if (columnValue != null && columnValue.equals("null")) {
                valuesInsert.add("'null'");
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
     * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.updateIndexEntries()
     */
    @Override
    protected void updateIndexEntries(ResultSet res, List<Integer> IndexentriesToDelete, List<String> allkeys, Entry<String, Object> entry, String indexTableName, Database indexDB, String sessionid) throws SQLException, DatabaseException {
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
}