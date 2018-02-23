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
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class administrates the handling of all operations on the index tables. The exact behavior is defined in the subclasses for different typse
 * of indices for example hashed-key index
 *
 * @author Mark
 */
public abstract class IndexAdministration {
    /**
     * Select all keys from data in RemoteDB for a specific table
     *
     * @param tableID the id of the table
     * @return a List of keys identifying all rows in the data table which belong to the given table
     * @throws DatabaseException
     */
    public abstract List<String> selectAllKeys(int tableID, String sessionid) throws DatabaseException;

    /**
     * Delete all keys from the index which belong to a given table. If index rows become empty they are removed from the index
     *
     * @param tableID the ID for the table
     * @return the keys of the deleted entries
     * @throws DatabaseException
     */
    public abstract List<String> deleteAllFromIndex(int tableID, String sessionid) throws DatabaseException;

    /**
     * Insert a value into the index
     *
     * @param tableName   the name of the table the value belongs to
     * @param maxID       the new ID for the value
     * @param columnName  the name of the column
     * @param columnValue the value which shall be stored
     * @throws DatabaseException
     */
    public abstract void insertIndexValue(String tableName, int maxID,
                                          String columnName, Object columnValue, String sessionid) throws DatabaseException;

    /**
     * Selects all keys which fulfill the whereclauses. If the keysToRetainlist is not empty only the matching keys are returned
     *
     * @param from         the table where the keys shall be selected from
     * @param where        the whereclauses defining the values to select
     * @param keysToRetain A List of keys from an and-statement (Only matching keys are returned if this list is not empty)
     * @param or
     * @return all keys which fulfill the whereclauses.
     * @throws DatabaseException
     */
    public abstract List<String> selectKeys(String from, List<WhereClause> where, List<String> keysToRetain, boolean or, String sessionid)
            throws DatabaseException;

    /**
     * Updated the values in the Index
     *
     * @param allkeys  the keys of the entries which shall be updated
     * @param entry    the entry e.g. the value which shall be updated
     * @param datatype the datatype of the column to be updated
     * @param table    the table the value belongs to
     * @throws DatabaseException
     */
    public abstract void updateIndexValues(List<String> allkeys, Entry<String, Object> entry, String datatype, String table, String sessionid) throws DatabaseException;

    /**
     * Deletes the values where the keys belong to
     *
     * @param table          the table the keys belong to
     * @param allkeys        all keys of the rows to be deleted
     * @param escapedColumns a list of columns where null shall be escaped as 'null'
     * @param rowsToDelete   the values of the rows to be deleted
     * @throws DatabaseException
     */
    public abstract void deleteAttributesFromIndex(String table,
                                                   List<String> allkeys, List<String> escapedColumns, List<List<String>> rowsToDelete, String sessionid)
            throws DatabaseException;

    /**
     * Insert a value into the index (uses different parameters)
     *
     * @param indexTableName the name of the index table to be inserted into
     * @param fieldID        the field id of the column to insert into
     * @param columnValue    the value to be inserted
     * @param keys           the keys of the encrypted rows belonging to this entry
     * @throws DatabaseException
     */
    public abstract void insertIntoIndex(String indexTableName, int fieldID, Object columnValue, List<String> keys, Database indexDB, String sessionid) throws DatabaseException;


    /**
     * Delete the entries from the Indextable which fullfill the Whereclause (Used for a fast remove of a whole Column in Drop Column)
     *
     * @param indexTableName the name of the Index Table where the data shall be removed from
     * @param where          the Whereclauses defining the rows to delete (usually only fieldID = ...)
     * @param indexDB        the Database where the index Table is stored
     */
    public abstract void deleteFromIndexWhere(String indexTableName, List<WhereClause> where, Database indexDB, String sessionid) throws DatabaseException, SQLException;
}
