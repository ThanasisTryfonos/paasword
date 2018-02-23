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
package eu.paasword.dbproxy.database.data;

import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.database.RemoteDBConstants;
import eu.paasword.dbproxy.database.RemoteDBHelper;
import eu.paasword.dbproxy.database.TableMapper;
import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.database.utils.WhereClauseBinary;
import eu.paasword.dbproxy.database.utils.WhereClauseIn;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.utils.QueryLexer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Standard Administration for the Table where the encrypted Rows are stored
 *
 * @author Mark Brenner
 */
public class DataStandardAdmininistration extends DataAdministration {
    private Logger logger = Logger.getLogger("prototype.database.DataStandardAdministration");
    TableMapper tableMapper = TableMapper.getInstance();
    QueryLexer lexer = QueryLexer.getInstance();
    Encryption encrypt;
    private RemoteDBHelper helper;
    Map<String, Database> relationToDB;
    private String remoteDBName;

    /**
     * Constructor for a normal DatAdministration
     *
     * @param relationDB the Map which stores the information in which physical database which table is saved
     * @param rhelper    the remoteHelper for this database giving standard private methods for the remote tables
     * @param encryptor  the encryption module which shall be used
     * @param DBName     the name of the remote database where the data table is stored
     */
    public DataStandardAdmininistration(Map<String, Database> relationDB, RemoteDBHelper rhelper, Encryption encryptor, String DBName) {
        helper = rhelper;
        relationToDB = relationDB;
        remoteDBName = DBName;
        encrypt = encryptor;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.data.DataAdministration.insertOnlyData(List<String> columns,
            List<Object> values, int newID, Database remoteDB,
            String remoteTable)
     */
    public void insertOnlyData(List<String> columns,
                               List<Object> values, int newID, Database remoteDB,
                               String remoteTable, String sessionid) throws DatabaseException {
        // 1.a For each not mentioned column, null is inserted
        if (columns.size() > values.size()) {
            int colSize = columns.size();

            while (colSize != values.size()) {
                values.add(null);
            }
        }

        // 2. values zu einem String zusammenfassen und encrypten
        String toInsert = "";
        ArrayList<String> join = new ArrayList<String>();
        for (Object v : values) {
            if (v == null) {
                join.add(RemoteDBConstants.NULL);
            } else {
                join.add(v.toString());
            }

        }

        toInsert = lexer.joinDecryptedString(join);
        toInsert = encrypt.encrypt(toInsert);

        List<String> columnsInsert = new ArrayList<String>();
        columnsInsert.add(RemoteDBConstants.DATA_KEY);
        columnsInsert.add(RemoteDBConstants.DATA_DATA);

        List<Object> valuesInsert = new ArrayList<Object>();
        // Column key must also be inserted!
        valuesInsert.add(newID);
        valuesInsert.add(toInsert);

        remoteDB.insert(helper.remoteTableName(remoteTable), columnsInsert,
                valuesInsert, false,sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.data.DataAdministration.deleteAllFromData(List<String> dataIndicies)
     */
    public void deleteAllFromData(List<String> dataIndicies, String sessionid) throws DatabaseException {
        if (dataIndicies.size() != 0) {
            ArrayList<WhereClause> where = new ArrayList<WhereClause>();
            List<Object> indices = new ArrayList<Object>(dataIndicies);
            where.add(new WhereClauseIn(RemoteDBConstants.DATA_KEY, indices));
            Database dataDB = relationToDB.get(remoteDBName + "." + RemoteDBConstants.DATA_TABLE);
            dataDB.delete(RemoteDBConstants.DATA_TABLE, where,sessionid);
        } else {
            logger.log(Level.INFO, "Nothing to delete in " + RemoteDBConstants.DATA_TABLE);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.database.data.DataAdministration.selectData(List<String> allKeys)
     */
    public List<List<String>> selectData(List<String> allKeys,String sessionid) throws DatabaseException {
        List<List<String>> result = new ArrayList<List<String>>();
        if (null != allKeys && !allKeys.isEmpty()) {
            String remoteTable = remoteDBName + "." + RemoteDBConstants.DATA_TABLE;
            Database remoteDB = relationToDB.get(remoteTable); //gets only the database-connection where necessaey tables are stored

            List<String> select = new ArrayList<String>();
            select.add(RemoteDBConstants.DATA_DATA);
            List<String> fromRemoteData = new ArrayList<String>();
            fromRemoteData.add(helper.remoteTableName(remoteTable));

            ArrayList<WhereClause> whereClause = new ArrayList<WhereClause>();
            ArrayList<Object> keys = new ArrayList<Object>(allKeys);
            whereClause.add(new WhereClauseIn(RemoteDBConstants.DATA_KEY, keys));
            ResultSet resData = remoteDB.select(select, fromRemoteData, whereClause, false, null, false,sessionid);
            try {
                while (resData.next()) {
                    String s = resData.getString(1); //Improves Performance DATA_DATA = 1
                    s = encrypt.decrypt(s);
                    result.add(lexer.splitDecryptedString(s));
                    ;
                }
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
     * @see edu.kit.iks.sdp.database.data.DataAdministration.pdateDataValue(String from, List<WhereClause> where,
            String attribute, String newValue)
     */
    public void updateDataValue(String from, List<WhereClause> where, String attribute, String newValue, String sessionid) throws DatabaseException {

        int tableID = tableMapper.getRemoteTableID(from);
        String remoteTable = remoteDBName + "."
                + RemoteDBConstants.DATA_TABLE;
        Database db = relationToDB.get(remoteTable);

        // select statement for keys
        List<String> select = new ArrayList<String>();
        select.add(RemoteDBConstants.DATA_DATA);
        select.add(RemoteDBConstants.DATA_PRIMKEY);
        List<String> from2 = new ArrayList<String>();
        from2.add(RemoteDBConstants.DATA_TABLE);

        ResultSet res = db.select(select, from2, where, false, null, true, sessionid);
        ArrayList<String> values = new ArrayList<String>();
        // to get the right position for replacement
        HashMap<String, Column> columns = tableMapper.getRemoteColumns(tableID);
        ArrayList<Column> columnList = new ArrayList<Column>();
        columnList.addAll(columns.values());
        Collections.sort(columnList);
        int index = 0;
        for (Column col : columnList) {
            if (col.getName().equalsIgnoreCase(attribute)) {
                break;
            }
            index++;
        }
        try {
            while (res.next()) {
                String s = res.getString(1); //Improves Perfromance 1 = Data_Data
                s = encrypt.decrypt(s);
                values = lexer.splitDecryptedString(s);
                //Update the rows
                if (newValue == null) {
                    values.set(index, RemoteDBConstants.NULL);
                } else {
                    values.set(index, newValue);
                }
                String newData = lexer.joinDecryptedString(values);
                newData = encrypt.encrypt(newData);
                HashMap<String, Object> updateData = new HashMap<String, Object>();
                updateData.put(RemoteDBConstants.DATA_DATA, newData);
                where = new ArrayList<WhereClause>();
                where.add(new WhereClauseBinary(RemoteDBConstants.DATA_PRIMKEY, res.getInt(RemoteDBConstants.DATA_PRIMKEY), "="));
                db.update(RemoteDBConstants.DATA_TABLE, updateData, where, true,sessionid);
            }
            db.executeUpdateBatch(sessionid);
        } catch (SQLException e) {
            String msg = "Could not select requested data! \n";
            logger.log(Level.INFO, msg, e);
            e.printStackTrace();
        }
    }
}
