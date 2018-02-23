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
 * With this interface you can retrieve the information which columns of database tables belong to which distributed index server.
 */
public interface IDistributedTableConfiguration {

    /**
     * Returns the server on which the column of the table is stored.
     * @param tableName The table name of the column.
     * @param columnName The column inside the table.
     * @return Get the server on which the column of the specific table  is distributed to.
     */
    public IDistributedServer getServer(String tableName, String columnName);

    /**
     * Set the configuration by API which table/columns belong to which server
     * @param theConfiguration The configuration is expected as follows:
     *                         <ul>
     *                         <li> Key of the outer map is the server name (or any other identifier for the server).</li>
     *                         <li> The value of that outer map is a map containing as key the table name, and as values a list of columns of that table, stored on the server</li>
     *                         </ul>
     *
     */
    public void setServerTableMapping(Map<IDistributedServer, Map<String, List<String>>> theConfiguration);
}
