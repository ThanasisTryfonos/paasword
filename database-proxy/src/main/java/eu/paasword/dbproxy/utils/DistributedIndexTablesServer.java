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

/**
 * This class is a limited proxy object when using the {@link DistributedTablesConfiguration}.
 * It is meant to be a simple DAO to get the necessary data to access the correct remotely distributed index tables.
 * Currently the approach is only to make a lookup int the 'columnServerMapping' table and to read:
 * 1) Which column resides on which server
 * 2) What is the server name (e.g. to laterly get the actual connection to the data base through remoteDB objects)
 */
public class DistributedIndexTablesServer implements IDistributedServer {

    private Integer uniqueId;
    private String serverName;

    public DistributedIndexTablesServer(final Integer pUniqeId, final String pServerName) {
        if (null != pUniqeId && null != pServerName) {
            uniqueId = pUniqeId;
            serverName = pServerName;
        } else {
            throw new IllegalArgumentException("Server id and server name must nut be null!");
        }
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public Integer getServerId() {
        return uniqueId;
    }
}
