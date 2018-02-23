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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class TransactionalManager {

    private static final Logger logger = Logger.getLogger(TransactionalManager.class.getName());
    private ComboPooledDataSource connectionPool;           //C3P0
    private static TransactionalManager instance;
    private ConcurrentHashMap<String, Object> connectionamp;

//    ConnectionContext concontext = new ConnectionContext(
//            "1", 
//            "jdbc:mysql://192.168.3.51:3306/transactiontest?tcpKeepAlive=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", 
//            "root", 
//            "!r00t!");

    private final ConnectionContext concontext = new ConnectionContext(
            "1",
            "jdbc:postgresql://212.101.173.19/kit_server_0",
            "postgres",
            "postgres"
    );    
    
    public TransactionalManager() {
        connectionamp = new ConcurrentHashMap<>();
        logger.info("Created TransactionManager");
        if (connectionPool == null) {
            logger.info("Performing ConnectionPool configuration");

            /* C3P0 */
            connectionPool = new ComboPooledDataSource();
            connectionPool.setJdbcUrl(concontext.getUrl());
            connectionPool.setUser(concontext.getUsername());
            connectionPool.setPassword(concontext.getPassword());
            connectionPool.setMinPoolSize(5);
            connectionPool.setAcquireIncrement(5);
            connectionPool.setMaxPoolSize(20);
            connectionPool.setMaxStatements(180);
        }//if
    }//EoConstructor

    public synchronized static TransactionalManager getInstance() {

        if (instance == null) {
            instance = new TransactionalManager();
        }
        return instance;
    }//EoM

    public String initiateTransaction() throws SQLException {
        Random random = new Random();
        String tid = ("" + random.nextInt()).substring(1, 6);
        logger.info("initiateSynchTransaction called " + tid);
        logger.info("get a connection from the pool " + tid);
        synchronized (connectionPool) {
            logger.info("Attempting to fetch " + tid + " while used connections are: " + connectionamp.size() );
//                    + " poolsizeActive: " + connectionPool.get + " poolsizeIdle:" + connectionPool.getNumIdle());
            Connection connection = connectionPool.getConnection();
            connection.setAutoCommit(false);
            logger.info("connection fetched " + tid);
//            connection = ((DelegatingConnection) connection).getInnermostDelegate();
            logger.info("add connection " + tid);
            connectionamp.put(tid, connection);
        }
        logger.info("connection added" + tid);
        return tid;
    }//EoM    

    public void commitTransaction(String tid) throws SQLException {
        logger.info("initiateSynchCommitTransaction called " + tid);
        Connection connection = (Connection) connectionamp.get(tid);
        logger.info("Attempting to commit tid: " + tid + " isclosed?: " + connection.isClosed());
        connection.commit();
        logger.info("Commited tid: " + tid + " isclosed?: " + connection.isClosed());
        connection.close();
        logger.info("Connection tid: " + tid + " isclosed?: " + connection.isClosed() + " returned to pool");
        connectionamp.remove(tid);
        logger.info("----> Transaction " + tid + " commited");
    }//EoM      

    public Connection executeCUDQueryDuringTransaction(String query, String tid) throws SQLException {
        Connection connection = (Connection) connectionamp.get(tid);
        logger.info("Connection for CUD: " + tid + " isclosed?: " + connection.isClosed());
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            int amount = pst.executeUpdate();
            logger.info("CUD executed - affected rows: " + amount +"("+tid+")");
            pst.close();
            logger.info("pst closed ("+tid+")");            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "SQL Error " + ex.getMessage());
            ex.printStackTrace();
        }
//        connectionamp.put(tid, connection);
        return connection;
    }//EoM           

    public List<Object[]> executeRDuringTransaction(String query, String tid) throws SQLException {
        Connection connection = (Connection) connectionamp.get(tid);
        logger.info("Connection for R: " + tid + " isclosed?: " + connection.isClosed());        
        ResultSet rset;
        List<Object[]> results = new ArrayList<Object[]>();
        try {            //connectionamp.get(tid)
            PreparedStatement pst = connection.prepareStatement(query);
            rset = pst.executeQuery();
            while (rset.next()) {
                int cols = rset.getMetaData().getColumnCount();
                Object[] arr = new Object[cols];
                for (int i = 0; i < cols; i++) {
                    arr[i] = rset.getObject(i + 1);
                }//for
                results.add(arr);
            }//while
            rset.close();
            pst.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }//try

        logger.info("executeQueryDuringTransaction finished!");
        return results;
    }//EoM       

    public static ResultSet executeAtomicQuery(ConnectionContext concontext, String query) throws SQLException {
        Connection connection = null;
        connection = DriverManager.getConnection(concontext.getUrl(), concontext.getUsername(), concontext.getPassword());
        PreparedStatement pst = connection.prepareStatement(query);
        ResultSet rset = pst.executeQuery();
        //connection.close();
        return rset;
    }//EoM    

    public static void executeAtomicUpdate(ConnectionContext concontext, String query) throws SQLException {
        Connection connection = null;
        connection = DriverManager.getConnection(concontext.getUrl(), concontext.getUsername(), concontext.getPassword());
        PreparedStatement pst = connection.prepareStatement(query);
        pst.executeUpdate();
    }//EoM        
  
}//EoC
