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
package eu.paasword.dbproxy.helper;

import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.exceptions.PluginLoadFailure;
import eu.paasword.dbproxy.impl.Adapter;
import eu.paasword.dbproxy.transaction.ConnectionContext;
import eu.paasword.dbproxy.transaction.DistributedTransactionalManager;
import eu.paasword.dbproxy.utils.ConfigParser;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

/**
 * Singleton instance to get access to the Adapter that is loaded with a local
 * config file.
 */
public class AdapterHelper {

    private static final Logger logger = Logger.getLogger(AdapterHelper.class.getName());
    private static AdapterHelper instance = null;
    public static ConcurrentHashMap<String, Adapter> adaptermap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, DistributedTransactionalManager> transactmap = new ConcurrentHashMap<>();

    public static Adapter getAdapter(String adapterid) {
        Adapter adapter = null;
        if (!adaptermap.containsKey(adapterid)) { //if adapter is not created
            synchronized (transactmap) {
                adapter = initializeAdapter(adapterid, null);
            }
        } else { // adapter exists
            logger.info("AdapterHelper-->Adapter EXISTS. It will be fetched ");
            adapter = (Adapter) adaptermap.get(adapterid);
        }
        return adapter;
    }//EoM

    public static Adapter getAdapter(String adapterid, String tenantKey) {
        Adapter adapter = null;
        if (!adaptermap.containsKey(adapterid)) { //if adapter is not created
            synchronized (transactmap) {
                adapter = initializeAdapter(adapterid, tenantKey);
            }
        } else { // adapter exists
            logger.info("AdapterHelper-->Adapter EXISTS. It will be fetched ");
            adapter = (Adapter) adaptermap.get(adapterid);
        }
        return adapter;
    }//EoM

    public synchronized static DistributedTransactionalManager getDTMByAdapterId(String adapterid) {
        synchronized (transactmap) {                                                //TODO Transactional Manager has to be booted first
            if (transactmap.get(adapterid) == null) {
                if (adapterid.equalsIgnoreCase("test")) {
                    initializeTestTransactionManager();
                } else {
                    initializeTransactionManager(adapterid);
                }
            }
        }
        return transactmap.get(adapterid);
    }

    private static synchronized Adapter initializeTestTransactionManager() {
        Adapter adapter = null;
        try {
            logger.info("initializeAdapter--> for Testing ");
            DistributedTransactionalManager tmanager = new DistributedTransactionalManager();
            transactmap.put("test", tmanager);
        } catch (NotSupportedException | SystemException | PropertyVetoException | SQLException ex) {
            logger.severe(ex.getMessage());
        }
        return adapter;
    }//EoM       

    private static synchronized void initializeTransactionManager(String adapterid) {
        try {
            logger.info("DTM will be initialized for adapter ---> " + adapterid);
            List<ConnectionContext> concontextlist = new ArrayList();
            concontextlist = getContextFromConfiguration(adapterid);
            DistributedTransactionalManager tmanager = new DistributedTransactionalManager(adapterid, concontextlist);
            transactmap.put(adapterid, tmanager);
        } catch (NotSupportedException | SystemException | PropertyVetoException | SQLException ex) {
            logger.severe(ex.getMessage());
        }
    }//EoM    

    public static List<String> getResourcesForAdapter(String adapterid) {
            List<String> reslist = new ArrayList();
        try {
            logger.info("getting resources for--> for: " + adapterid);
            List<ConnectionContext> concontextlist = getContextFromConfiguration(adapterid);
            for (ConnectionContext connectionContext : concontextlist) {
                reslist.add(connectionContext.getCode());
            }
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
        return reslist;
    }//EoM      
    
    private static Adapter initializeAdapter(String adapterid, String tenantKey) {
        Adapter adapter = null;
        try {
            logger.info("AdapterHelper-->Adapter DOES NOT exist for: " + adapterid);
            adapter = new Adapter(adapterid, tenantKey);
            //add it to the global map
            adaptermap.put(adapterid, adapter);
        } catch (PluginLoadFailure | IOException | DatabaseException ex) {
            logger.severe(ex.getMessage());
        }
        return adapter;
    }//EoM

    private static List<ConnectionContext> getContextFromConfiguration(String adapterid) {
        List<ConnectionContext> cons = new ArrayList();
        ConfigParser config = ConfigParser.getInstance(adapterid);
        //iterate structures
        //1
        List<Map<String, String>> remoteDatabases = config.getRemoteDatabases();
        for (Map<String, String> remoteDatabase : remoteDatabases) {
            ConnectionContext context = new ConnectionContext(
                    remoteDatabase.get("name"),
                    "jdbc:postgresql://" + remoteDatabase.get("host") + "/" + remoteDatabase.get("name"),
                    remoteDatabase.get("user"),
                    remoteDatabase.get("password"),
                    "org.postgresql.xa.PGXADataSource"
            );
            //add it to the list
            cons.add(context);
//            remoteDatabase.forEach((k, v) -> {
//                logger.info("r-key: " + k + " value:" + v);
//            });
        }//remote

        //2
        Map<String, String> localDatabase = config.getLocalDatabase();
        ConnectionContext lcontext = new ConnectionContext(
                localDatabase.get("name"),
                "jdbc:postgresql://" + localDatabase.get("host") + "/" + localDatabase.get("name"),
                localDatabase.get("user"),
                localDatabase.get("password"),
                "org.postgresql.xa.PGXADataSource"
        );
        cons.add(lcontext);
//        localDatabase.forEach((k, v) -> {
//            logger.info("l-key: " + k + " value:" + v);
//        });

        //3
        List<Map<String, String>> remoteIndexDatabases = config.getRemoteIndexDatabases();
        for (Map<String, String> remoteIndexDatabase : remoteIndexDatabases) {
            ConnectionContext context = new ConnectionContext(
                    remoteIndexDatabase.get("name"),
                    "jdbc:postgresql://" + remoteIndexDatabase.get("host") + "/" + remoteIndexDatabase.get("name"),
                    remoteIndexDatabase.get("user"),
                    remoteIndexDatabase.get("password"),
                    "org.postgresql.xa.PGXADataSource"
            );
            //add it to the list
            cons.add(context);
//            remoteIndexDatabase.forEach((k, v) -> {
//                logger.info("i-key: " + k + " value:" + v);
//            });
        }//remote

        logger.info("Database configured: " + cons.size());

        return cons;
    }//EoM

}//EoC
