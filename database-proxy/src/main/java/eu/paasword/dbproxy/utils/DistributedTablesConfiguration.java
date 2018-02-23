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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by valentin on 11.08.16.
 */
public class DistributedTablesConfiguration implements IDistributedTableConfiguration{

    private static IDistributedTableConfiguration self;

    private Map<IDistributedServer, Map<String, List<String>>> tableColumnServerMapping = new HashMap<>();

    /**
     * Public accessor
     *
     * @return the instance itself
     */
    public static IDistributedTableConfiguration getInstance() {
        if (self == null) {
            self = new DistributedTablesConfiguration();
        }
        return self;
    }


    @Override
    public IDistributedServer getServer(String tableName, String columnName) {
        for (IDistributedServer server : tableColumnServerMapping.keySet()) {
            if(tableColumnServerMapping.get(server).containsKey(tableName)){
                if(tableColumnServerMapping.get(server).get(tableName).contains(columnName)){
                    return server;
                }
            }
        }
        return null;
    }

    @Override
    public void setServerTableMapping(Map<IDistributedServer, Map<String, List<String>>> theConfiguration) {
        tableColumnServerMapping = new HashMap<>(theConfiguration);
    }
}
