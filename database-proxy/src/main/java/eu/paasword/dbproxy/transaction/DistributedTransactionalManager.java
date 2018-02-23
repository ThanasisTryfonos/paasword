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
package eu.paasword.dbproxy.transaction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import eu.paasword.dbproxy.database.BatchOfInsertStatements;
import eu.paasword.dbproxy.database.BatchOfUpdateStatements;
import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.StatementFiller;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sql.rowset.CachedRowSet;
import org.postgresql.xa.PGXADataSource;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class DistributedTransactionalManager {

    private static final Logger logger = Logger.getLogger(DistributedTransactionalManager.class.getName());
//    private static DistributedTransactionalManager instance;
    private ConcurrentHashMap<String, Object> threadmap;     //keeps track of the globar resources that exist  
    private ConcurrentHashMap<String, BlockingQueue<TransactionSegment>> inqueuemap;     //keeps track of the globar resources that exist  
    private ConcurrentHashMap<String, BlockingQueue<TransactionSegment>> outqueuemap;     //keeps track of the globar resources that exist
    private ConcurrentHashMap<String, Object> globalresmap;     //keeps track of the globar resources that exist  
    private ConcurrentHashMap<String, Object> transactionsresourcesmap;          //keeps track of the resources that are reserved by a transaction
    private ConcurrentHashMap<String, Object> conmap;          // key=tid_resid  value=Connection
    private ConcurrentHashMap<String, Object> xaconmap;          // key=tid_resid  value=XAConnection
    private ConcurrentHashMap<String, Object> transactionsmap;         // key=tid        value=Transaction
    private ConcurrentHashMap<String, Object> utmmap;         // key=tid        value=utm
    public static final int TXTIMEOUT = 30;  //seconds
    public static final int MAXIDLECONTIMEOUT_INSECONDS = 30;  //seconds
    public static final int POOLSIZE = 0;
    private String adapterid;
    private List<ConnectionContext> concontextlist;

//--MySQL
//    private final ConnectionContext concontext1 = new ConnectionContext(
//            "1", 
//            "jdbc:mysql://192.168.3.51:3306/transactiontest?tcpKeepAlive=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", 
//            "root", 
//            "!r00t!",
//            "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"
//    );
//    private final ConnectionContext concontext2 = new ConnectionContext(
//            "2", 
//            "jdbc:mysql://192.168.3.51:3306/transactiontest2?tcpKeepAlive=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", 
//            "root", 
//            "!r00t!",
//            "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"
//    );
//--Postgress 
    private final ConnectionContext concontext1 = new ConnectionContext(
            "kit_server_0",
            "jdbc:postgresql://212.101.173.19/kit_server_0",
            "postgres",
            "postgres",
            "org.postgresql.xa.PGXADataSource"
    );
    private final ConnectionContext concontext2 = new ConnectionContext(
            "kit_server_1",
            "jdbc:postgresql://212.101.173.22/kit_server_1",
            "postgres",
            "postgres",
            "org.postgresql.xa.PGXADataSource"
    );

    public DistributedTransactionalManager() throws NotSupportedException, SystemException, PropertyVetoException, SQLException {
        //----- initialize local structures
        threadmap = new ConcurrentHashMap<>();
        inqueuemap = new ConcurrentHashMap<>();
        outqueuemap = new ConcurrentHashMap<>();
        //------
        globalresmap = new ConcurrentHashMap<>();

        transactionsresourcesmap = new ConcurrentHashMap<>();
        conmap = new ConcurrentHashMap<>();
        xaconmap = new ConcurrentHashMap<>();
        transactionsmap = new ConcurrentHashMap<>();
        utmmap = new ConcurrentHashMap<>();

        logger.info("Created TransactionManager");
        logger.info("Performing ConnectionPool configuration");

        //------------Atomikos Pooled Version------------------------
        //1
        AtomikosDataSourceBean pool1 = new AtomikosDataSourceBean();
        pool1.setUniqueResourceName(concontext1.getCode());
        pool1.setXaDataSourceClassName(concontext1.getXadatasourceclassname());   //"com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"
        Properties p1 = new Properties();
        p1.setProperty("user", concontext1.getUsername());
        p1.setProperty("password", concontext1.getPassword());
        p1.setProperty("URL", concontext1.getUrl());
        pool1.setXaProperties(p1);
        pool1.setPoolSize(DistributedTransactionalManager.POOLSIZE);
        pool1.setMaxIdleTime(MAXIDLECONTIMEOUT_INSECONDS);
        //MySQL
//        MysqlXADataSource xads1 = new MysqlXADataSource();
        //Postgress
        PGXADataSource xads1 = new PGXADataSource();
        xads1.setUser(concontext1.getUsername());
        xads1.setPassword(concontext1.getPassword());
        xads1.setUrl(concontext1.getUrl());
        //add it to pool
        pool1.setXaDataSource(xads1);

        //2
        AtomikosDataSourceBean pool2 = new AtomikosDataSourceBean();
        pool2.setUniqueResourceName(concontext2.getCode());
        pool2.setXaDataSourceClassName(concontext2.getXadatasourceclassname());
        Properties p2 = new Properties();
        p2.setProperty("user", concontext2.getUsername());
        p2.setProperty("password", concontext2.getPassword());
        p2.setProperty("URL", concontext2.getUrl());
        pool2.setXaProperties(p2);
        pool2.setPoolSize(DistributedTransactionalManager.POOLSIZE);
        pool2.setMaxIdleTime(MAXIDLECONTIMEOUT_INSECONDS);
        //MySQL
//        MysqlXADataSource xads2 = new MysqlXADataSource();
        //Postgress
        PGXADataSource xads2 = new PGXADataSource();
        xads2.setUser(concontext2.getUsername());
        xads2.setPassword(concontext2.getPassword());
        xads2.setUrl(concontext2.getUrl());
        //add it to pool
        pool2.setXaDataSource(xads2);

        globalresmap.put("1", pool1);
        globalresmap.put("2", pool2);

    }//EoConstructor

    public DistributedTransactionalManager(String adapterid, List<ConnectionContext> concontextlist) throws NotSupportedException, SystemException, PropertyVetoException, SQLException {
        this.adapterid = adapterid;
        this.concontextlist = concontextlist;
        //initialize local structures
        threadmap = new ConcurrentHashMap<>();
        inqueuemap = new ConcurrentHashMap<>();
        outqueuemap = new ConcurrentHashMap<>();
        //------------------
        globalresmap = new ConcurrentHashMap<>();
//        globalxaconmap = new ConcurrentHashMap<>();
//        localxapool = new ConcurrentHashMap<>();

        transactionsresourcesmap = new ConcurrentHashMap<>();
        conmap = new ConcurrentHashMap<>();
        xaconmap = new ConcurrentHashMap<>();
        transactionsmap = new ConcurrentHashMap<>();
        utmmap = new ConcurrentHashMap<>();

        logger.info("Created TransactionManager - Performing ConnectionPool configuration");

        for (ConnectionContext concontext : concontextlist) {
            logger.info(concontext.getCode() + " " + concontext.getUsername() + " " + concontext.getPassword() + " " + concontext.getUrl());
            AtomikosDataSourceBean pool = new AtomikosDataSourceBean();
            pool.setUniqueResourceName(concontext.getCode());
            pool.setXaDataSourceClassName(concontext.getXadatasourceclassname());   //"com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"
            Properties p1 = new Properties();
            p1.setProperty("user", concontext.getUsername());
            p1.setProperty("password", concontext.getPassword());
            p1.setProperty("URL", concontext.getUrl());
            pool.setXaProperties(p1);
            pool.setPoolSize(DistributedTransactionalManager.POOLSIZE);
            pool.setMaxIdleTime(MAXIDLECONTIMEOUT_INSECONDS);
            //Postgress
            PGXADataSource xads1 = new PGXADataSource();
            xads1.setUser(concontext.getUsername());
            xads1.setPassword(concontext.getPassword());
            xads1.setUrl(concontext.getUrl());
            //add it to pool
            pool.setXaDataSource(xads1);
            //add it to the pool
            globalresmap.put(concontext.getCode(), pool);
//            XAConnection xacon = pool.getXaDataSource().getXAConnection();
//            globalxaconmap.put(concontext.getCode(), xacon);
        }//for

    }//EoConstructor    

    public synchronized AtomikosDataSourceBean getPoolForResource(String rid) {
        return (AtomikosDataSourceBean) globalresmap.get(rid);
    }

    public synchronized void addQueuesToMap(String tid, BlockingQueue<TransactionSegment> inqueue, BlockingQueue<TransactionSegment> outqueue) {
        this.inqueuemap.put(tid, inqueue);
        this.outqueuemap.put(tid, outqueue);
    }

    public BlockingQueue getInQueueForTransaction(String tid) {
        return this.inqueuemap.get(tid);
    }

    public BlockingQueue getOutQueueForTransaction(String tid) {
        return this.outqueuemap.get(tid);
    }

    /**
     * ***************** These methods are called by the REST layer ***
     */
    public synchronized String initiateTransaction() throws SQLException, SystemException, IllegalStateException, RollbackException, NotSupportedException {
        logger.info("DTManager.initiateTransaction--> called");
        Random random = new Random();
        String tid = ("" + random.nextInt()).substring(1, 6);
        logger.info("DTManager.initiateTransaction--> asigned tid: " + tid);
        PaaswordTransaction paastrans = new PaaswordTransaction(adapterid, getResourcesForAdapter(concontextlist), tid,this);
        Thread thread = new Thread(paastrans);
        threadmap.put(tid, thread);
        //Prepare Queues
        BlockingQueue<TransactionSegment> inqueue = new LinkedBlockingQueue<>();
        BlockingQueue<TransactionSegment> outqueue = new LinkedBlockingQueue<>();
        addQueuesToMap(tid, inqueue, outqueue);
        //start Thread
        thread.start();

        logger.info("DTManager.initiateTransaction --> Transaction created: " + tid);
        return tid;
    }//EoM       

    public static List<String> getResourcesForAdapter(List<ConnectionContext> concontextlist) {
        List<String> reslist = new ArrayList();
        for (ConnectionContext connectionContext : concontextlist) {
            reslist.add(connectionContext.getCode());
        }
        return reslist;
    }//EoM      

//    public String initiateTransaction(List<String> resources, String adapterid) throws SQLException, SystemException, IllegalStateException, RollbackException, NotSupportedException {
//        logger.info("DTManager.initiateTransaction--> called");
//        Random random = new Random();
//        String tid = ("" + random.nextInt()).substring(1, 6);
//        logger.info("DTManager.initiateTransaction--> asigned tid: " + tid);
//        PaaswordTransaction paastrans = new PaaswordTransaction(adapterid, resources, tid);
//        Thread thread = new Thread(paastrans);
//        threadmap.put(tid, thread);
//        //Prepare Queues
//        BlockingQueue<TransactionSegment> inqueue = new LinkedBlockingQueue<>();
//        BlockingQueue<TransactionSegment> outqueue = new LinkedBlockingQueue<>();
//        addQueuesToMap(tid, inqueue, outqueue);
//        //start Thread
//        thread.start();
//
//        logger.info("DTManager.initiateTransaction --> Transaction created: " + tid);
//        return tid;
//    }//EoM    

    public void commitTransaction(String tid) throws SQLException {
        logger.info("DTManager.commitTransaction--> called " + tid);
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment commitmessage = new TransactionSegment(0);
            inqueue.put(commitmessage);
            logger.info("DTManager.commitTransaction--> Queued ");
            threadmap.remove(tid);
        } catch (Exception ex) {
            logger.severe("\"DTManager.commitTransaction-->Exception" + ex.getMessage());
        }
        logger.info("DTManager--> Transaction " + tid + " commited");
    }//EoM      

    public void executeCUDQueryDuringTransaction(String query, String resid, String tid) throws SQLException {
        logger.info("DTManager.executeCUDQueryDuringTransaction--> invoked ");
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment cudquery = new TransactionSegment(1, query, resid);
            inqueue.put(cudquery);
            logger.info("DTManager.executeCUDQueryDuringTransaction--> Queued ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DTManager.executeCUDQueryDuringTransaction -->  SQL Error during CUD" + ex.getMessage());
        }
    }//EoM           

    public List<Object[]> executeRDuringDistributedTransaction(String query, String resid, String tid) throws SQLException {
        logger.info("TManager.executeRDuringDistributedTransaction--> invoked ");
        List<Object[]> ret = null;
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment rquery = new TransactionSegment(2, query, resid);
            inqueue.put(rquery);
            //wait for response
            BlockingQueue outqueue = getOutQueueForTransaction(tid);
            TransactionSegment message = (TransactionSegment) outqueue.take();
            ret = message.getReturnobjects();
            logger.info("TManager.executeRDuringDistributedTransaction--> Executed ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "TManager.executeRDuringDistributedTransaction -->  SQL Error during R" + ex.getMessage());
        }
        return ret;
    }//EoM       

    /**
     * ***************** Pass through for DB Proxy ************************
     */
    //Messagetype 1    
    public void executeAtomicCUDQuery(String query, String rid, String tid) throws SQLException {
        logger.info("DTManager.executeAtomicCUDQuery--> invoked ");
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment tquery = new TransactionSegment(1, query, rid);
            inqueue.put(tquery);
            logger.info("DTManager.executeAtomicCUDQuery--> Queued ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DTManager.executeAtomicCUDQuery -->  SQL Error during Queueing" + ex.getMessage());
        }
    }//EoM

    //Messagetype 3
    public void executeAtomicCUDQuery(String query, String rid, StatementFiller filler, String from, List<WhereClause> where, String tid) throws SQLException {
        logger.info("DTManager.executeAtomicCUDQuery--> invoked ");
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment tquery = new TransactionSegment(3, query, rid, filler, from, where);
            inqueue.put(tquery);
            logger.info("DTManager.executeAtomicCUDQuery--> Queued ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DTManager.executeAtomicCUDQuery -->  SQL Error during Queueing" + ex.getMessage());
        }
    }//EoM

    //Messagetype 4
    public void executeAtomicBatchInsertQuery(BatchOfInsertStatements batch, String rid, StatementFiller filler, String tid) throws SQLException {
        logger.info("DTManager.executeAtomicBatchInsertQuery--> invoked ");
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment tquery = new TransactionSegment(4, batch, rid, filler);
            inqueue.put(tquery);
            logger.info("DTManager.executeAtomicBatchInsertQuery--> Queued ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DTManager.executeAtomicBatchInsertQuery -->  SQL Error during Queueing" + ex.getMessage());
        }
    }//EoM

    //Messagetype 5
    public void executeAtomicBatchUpdateQuery(BatchOfUpdateStatements batch, String rid, StatementFiller filler, String tid) throws SQLException {
        logger.info("DTManager.executeAtomicBatchUpdateQuery--> invoked ");
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment tquery = new TransactionSegment(5, batch, rid, filler);
            inqueue.put(tquery);
            logger.info("DTManager.executeAtomicBatchUpdateQuery--> Queued ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DTManager.executeAtomicBatchUpdateQuery -->  SQL Error during Queueing" + ex.getMessage());
        }
    }//EoM

    //Messagetype 6
    public void executeAtomicInsertQuery(String query, String rid, StatementFiller filler, String into, ArrayList<WhereClause> where, String tid) throws SQLException {
        logger.info("DTManager.executeAtomicInsertQuery--> invoked ");
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment tquery = new TransactionSegment(6, query, rid, filler, where, into);
            inqueue.put(tquery);
            logger.info("DTManager.executeAtomicInsertQuery--> Queued ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DTManager.executeAtomicInsertQuery -->  SQL Error during Queueing" + ex.getMessage());
        }
    }//EoM

    //Messagetype 7
    public void executeAtomicUpdateQuery(String query, String rid, StatementFiller filler, String table, Map<String, Object> updateData, List<WhereClause> where, String tid) throws SQLException {
        logger.info("DTManager.executeAtomicUpdateQuery--> invoked ");
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment tquery = new TransactionSegment(7, query, rid, filler, table, updateData, where);
            inqueue.put(tquery);
            logger.info("DTManager.executeAtomicUpdateQuery--> Queued ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DTManager.executeAtomicUpdateQuery -->  SQL Error during Queueing" + ex.getMessage());
        }
    }//EoM

    //Messagetype 8
    public void executeAtomicInsertBatch(String query, String rid, StatementFiller filler, List<Column> columns, List<List<String>> currenValues, String tid) throws SQLException {
        logger.info("DTManager.executeAtomicInsertBatch--> invoked ");
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment tquery = new TransactionSegment(8, query, rid, filler, columns, currenValues);
            inqueue.put(tquery);
            logger.info("DTManager.executeAtomicInsertBatch--> Queued ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DTManager.executeAtomicInsertBatch -->  SQL Error during Queueing" + ex.getMessage());
        }
    }//EoM

    //Messagetype 9    
    public CachedRowSet executeAtomicRQuery(String query, String rid, StatementFiller filler, List<String> from, List<WhereClause> where, boolean selectAll, boolean updateable, String tid) throws SQLException {
        logger.info("DTManager.executeRDuringDistributedTransaction--> invoked ");
        CachedRowSet ret = null;
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment rquery = new TransactionSegment(9, query, rid, filler, from, where, selectAll, updateable);
            inqueue.put(rquery);
            //wait for response
            BlockingQueue outqueue = getOutQueueForTransaction(tid);
            TransactionSegment message = (TransactionSegment) outqueue.take();
            ret = message.getCachedrowset();
            logger.info("DTManager.executeRDuringDistributedTransaction--> Executed ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "TManager.executeRDuringDistributedTransaction -->  SQL Error during R" + ex.getMessage());
        }
        return ret;
    }//EoM

    //Messagetype 10 
    public CachedRowSet executeAtomicRQuery(String query, String rid, String tid) throws SQLException {
        logger.info("DTManager.executeRDuringDistributedTransaction--> invoked ");
        CachedRowSet ret = null;
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment rquery = new TransactionSegment(10, query, rid);
            inqueue.put(rquery);
            //wait for response
            BlockingQueue outqueue = getOutQueueForTransaction(tid);
            TransactionSegment message = (TransactionSegment) outqueue.take();
            ret = message.getCachedrowset();
            logger.info("DTManager.executeRDuringDistributedTransaction--> Executed ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "TManager.executeRDuringDistributedTransaction -->  SQL Error during R" + ex.getMessage());
        }
        return ret;
    }//EoM    

    //Messagetype 11
    public Map<String, List<Column>> executeLoadSchemaQuery(String rid, String tid) throws SQLException, DatabaseException {
        logger.info("DTManager.executeLoadSchemaQuery--> invoked ");
        Map<String, List<Column>> ret = null;
        try {
            BlockingQueue inqueue = getInQueueForTransaction(tid);
            TransactionSegment rquery = new TransactionSegment(11, rid);
            inqueue.put(rquery);
            //wait for response
            BlockingQueue outqueue = getOutQueueForTransaction(tid);
            TransactionSegment message = (TransactionSegment) outqueue.take();
            ret = message.getSchema();
            logger.info("DTManager.executeLoadSchemaQuery--> Executed ");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "TManager.executeLoadSchemaQuery -->  SQL Error during executeLoadSchemaQuery" + ex.getMessage());
        }
        return ret;
    }//EoM

    public String getAdapterid() {
        return adapterid;
    }

    public void setAdapterid(String adapterid) {
        this.adapterid = adapterid;
    }

    public List<ConnectionContext> getConcontextlist() {
        return concontextlist;
    }

    public void setConcontextlist(List<ConnectionContext> concontextlist) {
        this.concontextlist = concontextlist;
    }

}//EoC
