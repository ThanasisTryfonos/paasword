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
package eu.paasword.dbproxy.utils;

import java.util.List;
import java.util.Map;

/**
 * A key vault should be a sort of container to put in all remote db keys during parsing
 * the sql statement(s) and especially subqueries.
 *
 * During the parsing process you can add the remote db keys that need to be decrypted and inserted into the local db
 * by using the {@link #addKeys(String, List)} method.
 *
 * After the parsing has finished you can get the complete result to be decrypted and added to the local db by
 * using {@link #getKeysToBeInserted()} before querying the actual local db.
 *
 * @author Valentin Zipf
 * */
public interface IKeyVault {

    /**
     * Add remote db keys that have to be decrypted.
     *
     * @param tableName The table for which the remote db keys shall be added.
     * @param remoteDBKeys The list of remote db keys
     */
    public void addKeys(final String tableName, final List<String> remoteDBKeys);

    /**
     * Add all keys from the keyVault to this one.
     * @param keyVault
     */
    public void addAllKeys(final IKeyVault keyVault);

    /**
     * Intersect each key list.
     * @param keyVault
     */
    public void intersect(final IKeyVault keyVault);

    public void merge(final IKeyVault keyVault);

    /**
     * Get the remote db keys that have to be added to the local db.
     * In the returned map there are all key-value pairs that where added with {@link #addKeys(String, List)}.
     * Note that the key of the returned map returns to the data base table and the value is a list of remote db keys for which the data has to be fetched from remote and inserted into the local db
     *
     *  @return The map that contains all remote db keys separated by table name.
     */
    public Map<String, List<String>> getKeysToBeInserted();

    /**
     * Get the keys that were stored under the specified table.
     * @param tableName the table
     * @return A list containing all keys. Can be empty if keys were not yet added.
     */
    public List<String> getKeys(final String tableName);

    /**
     * Check if the key vault contains keys for the specified table
     * @param tableName the table
     * @return True if there if there is at least one key stored for the specified table. Otherwise false
     */
    public boolean hasKeys(final String tableName);
}
