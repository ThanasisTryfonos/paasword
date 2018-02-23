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

import java.util.*;

/**
 * This class acts as an global store of remote db keys that will be added to the local data base if the parsing of the
 * query has completed. You might use this data store within your parsing process to store those keys that have to be in
 * the local db so that the original query can be performed and the result is lossless.
 */
public class QueryKeyVault implements IKeyVault {

    private Map<String, Set<String>> keysToBeInserted;

    public QueryKeyVault() {
        keysToBeInserted = new HashMap<>();
    }

    /**
     * Add the resulting keys of a (sub)-query-(statement)
     *
     * @param tableName
     * @param remoteDBKeys
     */
    @Override
    public void addKeys(final String tableName, final List<String> remoteDBKeys) {
        if (null != remoteDBKeys) {
            if (getInternalKeysToBeInserted().containsKey(tableName)) {
                getInternalKeysToBeInserted().get(tableName).addAll(remoteDBKeys);
                return;
            }
            getInternalKeysToBeInserted().put(tableName, asSet(remoteDBKeys));
        }
    }

    @Override
    public void addAllKeys(final IKeyVault keyVault) {
        for (String tableName : keyVault.getKeysToBeInserted().keySet()) {
            addKeys(tableName, keyVault.getKeysToBeInserted().get(tableName));
        }
    }

    @Override
    public void intersect(final IKeyVault keyVault) {
        for (String tableOfOtherKeyVault : keyVault.getKeysToBeInserted().keySet()) {
            if(hasKeys(tableOfOtherKeyVault)){
                getInternalKeysToBeInserted().get(tableOfOtherKeyVault).retainAll(keyVault.getKeys(tableOfOtherKeyVault));
            }
        }
    }

    /**
     * This method merges the entries of keyVault to this instance.
     * When keyVault contains keys for tables that do not exist in this instance, they are added to this instance.
     * <b>Important Note:</b>
     * If keyVault contains keys for tables that do exist in this instance the merge is performed like an intersection.
     * Only those keys will survive that exist in both keyVault and this instance!
     *
     * This merge approach is currently used when it comes to parse the tree of AND operators since there is the need
     * to add the tables/keys from a possible subquery that contains other tables than on the current recursion level
     * of the parsing method.
     * So we can take those other keys into this keyVault and because we apply that in an AND-Construct we only want to
     * have an intersection if the subquery returned keys for tables that do allready exist here.
     *
     * TODO: For very large key vaults (>10k records) this methods performance is not the best. Maybe there is a way to merge two key vaults more efficiently
     * @param keyVault
     */
    @Override
    public void merge(IKeyVault keyVault) {
        for (String tableOfOtherKeyVault : keyVault.getKeysToBeInserted().keySet()) {
            if(hasKeys(tableOfOtherKeyVault)){
                getInternalKeysToBeInserted().get(tableOfOtherKeyVault).retainAll(keyVault.getKeys(tableOfOtherKeyVault));
            }
            else{
                getInternalKeysToBeInserted().put(tableOfOtherKeyVault, asSet(keyVault.getKeys(tableOfOtherKeyVault)));
            }
        }
    }

    /**
     * Get the remote db keys that have to be added to the local db.
     * In the returned map there are all key-value pairs that where added with {@link #addKeys(String, List)}.
     * Note that the key of the returned map returns to the data base table and the value is a list of remote db keys for which the data has to be fetched from remote and inserted into the local db
     *
     *  @return
     */
    @Override
    public Map<String, List<String>> getKeysToBeInserted() {
        Map<String, List<String>> returnKeyMap = new HashMap<>();

        for (String set : getInternalKeysToBeInserted().keySet()) {
            returnKeyMap.put(set, new ArrayList<String>(getInternalKeysToBeInserted().get(set)));
        }
        return returnKeyMap;
    }

    @Override
    public List<String> getKeys(String tableName) {
        if(hasKeys(tableName)){
            return getKeysToBeInserted().get(tableName);
        }
        return new ArrayList<String>();
    }

    @Override
    public boolean hasKeys(String tableName) {
        return (null != getKeysToBeInserted().get(tableName) && !getKeysToBeInserted().get(tableName).isEmpty());
    }

    /**
     * Convert the list into a  set.
     * @param list
     * @return
     */
    private Set<String> asSet(List<String> list) {
        Set<String> set = new HashSet<>(list.size());
        set.addAll(list);
        return set;
    }

    /**
     * Get the keys.
     * In difference to the other method the internal representation of the key store (e.g. a Map in which is a Set and not a List) is returned
     *
     * @return
     */
    private Map<String, Set<String>> getInternalKeysToBeInserted() {
        return keysToBeInserted;
    }
}
