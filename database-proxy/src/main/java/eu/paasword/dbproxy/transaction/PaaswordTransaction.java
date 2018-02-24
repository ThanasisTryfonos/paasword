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

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.sun.rowset.CachedRowSetImpl;
import eu.paasword.dbproxy.database.BatchOfInsertStatements;
import eu.paasword.dbproxy.database.BatchOfUpdateStatements;
import eu.paasword.dbproxy.database.InsertStatement;
import eu.paasword.dbproxy.database.UpdateStatement;
import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.StatementFiller;
import eu.paasword.dbproxy.database.utils.Type;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.exceptions.UnknownTypeException;
import eu.paasword.dbproxy.helper.AdapterHelper;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.XAConnection;
import javax.sql.rowset.CachedRowSet;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class PaaswordTransaction implements Runnable {

    //define structures
    private static final Logger logger = Logger.getLogger(PaaswordTransaction.class.getName());
    private String adapterid;
    private List<String> resources;
    private String tid;
    private DistributedTransactionalManager dtm;
    private boolean commited = false;
    private UserTransactionManager utm;
//    private TransactionManager tm;
    private Transaction tx;
    private ConcurrentHashMap<String, Object> conmap;          // key=tid_resid  value=Connection
    private ConcurrentHashMap<String, Object> xaconmap;          // key=tid_resid  value=XAConnection    

    //adapterid = test for demo purposes
    public PaaswordTransaction(String adapterid, List<String> resources, String tid, DistributedTransactionalManager dtm) {
        this.adapterid = adapterid;
        this.resources = resources;
        this.tid = tid;
        //local structures
        conmap = new ConcurrentHashMap<>();
        xaconmap = new ConcurrentHashMap<>();
        //initialize transactional manager
        try {
            //this.dtm = AdapterHelper.getDTMByAdapterId(adapterid);
            this.dtm = dtm;
            //this.dtm = dtm;
            logger.info("PaaswordTransaction Initiating new Transaction for adapter " + dtm.getAdapterid() + " and tid " + tid);
            //initialize the transaction
            utm = new UserTransactionManager();
            utm.setTransactionTimeout(DistributedTransactionalManager.TXTIMEOUT);
            utm.setForceShutdown(true);
//            utm.init();
            logger.info("PaaswordTransaction Initiation FINISHED SUCCESSFULLY for adapter " + dtm.getAdapterid() + " and tid " + tid);
        } catch (Exception ex) {
            logger.severe("PaaswordTransaction-->Error during the initialization of the transaction" + tid + " \n" + ex.getMessage());
            ex.printStackTrace();
        }
    }//EoC

    @Override
    public void run() {
        try {
                                           
            //Start Transaction Manager
            logger.info("PaaswordTransaction--> run() utm.getStatus(): " + utm.getStatus());
            utm.setTransactionTimeout(DistributedTransactionalManager.TXTIMEOUT);
            utm.setForceShutdown(true);
            utm.setStartupTransactionService(true);
//            utm.init();

            utm.begin();
            tx = utm.getTransaction();
            logger.info("PaaswordTransaction--> New Transaction tx.isNull: " + (tx == null));
            //step-2 get connections
            for (String rid : resources) {
                AtomikosDataSourceBean pool = ((AtomikosDataSourceBean) dtm.getPoolForResource(rid));
                logger.info("PaaswordTransaction--> Got Pool isNull: " + (pool == null) + " getXaDataSource().isNull " + (pool.getXaDataSource() == null));
                XAConnection xacon = pool.getXaDataSource().getXAConnection();
//                XAConnection xacon = dtm.getXAConnectionFromPool(rid);
                Connection con = xacon.getConnection();
                con.setAutoCommit(false);
                logger.info("PaaswordTransaction.XAConnection for " + rid + " is " + xacon + " isNull: " + (xacon == null) + " xacon.getXAResource().isNull: " + (xacon.getXAResource() == null));
                //tx.enlistResource(xacon.getXAResource());
                conmap.put(tid + "_" + rid, con);
                xaconmap.put(tid + "_" + rid, xacon);
            }
        } catch (Exception ex) {
            logger.severe("PaaswordTransaction->Initialization Exception " + tid + " " + ex.getMessage());
            ex.printStackTrace();
        }

        //start queue
        BlockingQueue inqueue = dtm.getInQueueForTransaction(tid);
        BlockingQueue outqueue = dtm.getOutQueueForTransaction(tid);
        logger.info("PaaswordTransaction (" + tid + ")---> got Inqueue.isNull? " + (inqueue == null));
        while (!commited) {
            try {
                TransactionSegment message = (TransactionSegment) inqueue.take();
                logger.info("PaaswordTransaction (" + tid + ") Message received " + message.getType());
                switch (message.getType()) {
                    case 0:     //COMMIT
                        commited = commitTransaction();
                        break;

                    case 1:     //CUD
                        executeCUDQueryDuringTransaction(message.getQuery(), message.getRid());
                        break;

                    case 2:     //R
                        List<Object[]> returnobjects = executeRDuringDistributedTransaction(message.getQuery(), message.getRid());
                        TransactionSegment outputmessage = new TransactionSegment(3, returnobjects);
                        outqueue.put(outputmessage);
                        break;

                    case 3:     //void executeAtomicCUDQuery(String query, String rid, StatementFiller filler, String from, List<WhereClause> where)
                        HandleexecuteAtomicCUDQuery(message.getQuery(), message.getRid(), message.getFiller(), message.getFrom(), message.getWhere());
                        break;

                    case 4:     //void executeAtomicBatchInsertQuery(BatchOfInsertStatements batch, String rid, StatementFiller filler)
                        HandleexecuteAtomicBatchInsertQuery(message.getBatchinsert(), message.getRid(), message.getFiller());
                        break;

                    case 5:     //public void executeAtomicBatchUpdateQuery(BatchOfUpdateStatements batch, String rid, StatementFiller filler)
                        HandleexecuteAtomicBatchUpdateQuery(message.getBatchupdate(), message.getRid(), message.getFiller());
                        break;

                    case 6:     //void executeAtomicInsertQuery(String query, String rid, StatementFiller filler, String into, ArrayList<WhereClause> where)
                        HandleexecuteAtomicInsertQuery(message.getQuery(), message.getRid(), message.getFiller(), message.getInto(), message.getWhere());
                        break;

                    case 7:     //void executeAtomicUpdateQuery(String query, String rid, StatementFiller filler, String table, Map<String, Object> updateData, List<WhereClause> where)
                        HandleexecuteAtomicUpdateQuery(message.getQuery(), message.getRid(), message.getFiller(), message.getTable(), message.getUpdateData(), message.getWhere());
                        break;

                    case 8:     //void executeAtomicInsertBatch(String query, String rid, StatementFiller filler, List<Column> columns, List<List<String>> currenValues)
                        HandleexecuteAtomicInsertBatch(message.getQuery(), message.getRid(), message.getFiller(), message.getColumns(), message.getCurrenValues());
                        break;

                    case 9:     //CachedRowSet executeAtomicRQuery(String query, String rid, StatementFiller filler, List<String> from, List<WhereClause> where, boolean selectAll, boolean updateable)
                        CachedRowSet cachedrowset2 = HandleexecuteAtomicRQuery(message.getQuery(), message.getRid(), message.getFiller(), message.getFromlist(), message.getWhere(), message.isSelectAll(), message.isUpdateable());
                        TransactionSegment outputmessage2 = new TransactionSegment(cachedrowset2);
                        outqueue.put(outputmessage2);
                        break;

                    case 10:    //CachedRowSet executeAtomicRQuery(String query, String rid)
                        CachedRowSet cachedrowset3 = HandleexecuteAtomicRQuery(message.getQuery(), message.getRid());
                        TransactionSegment outputmessage3 = new TransactionSegment(cachedrowset3);
                        outqueue.put(outputmessage3);
                        break;

                    case 11:    //Map<String, List<Column>> executeLoadSchemaQuery(String rid)
                        Map<String, List<Column>> schema = HandleexecuteLoadSchemaQuery(message.getRid());
                        TransactionSegment outputmessage4 = new TransactionSegment(schema);
                        outqueue.put(outputmessage4);
                        break;

                    // shoule NEVER be here
                    default:
                        break;
                }
            } catch (Exception ex) {
                logger.severe("PaaswordTransaction->Runtime Exception " + tid + " \n" + ex.getMessage());
                ex.printStackTrace();
            }
        }//while not commited (//TODO add timer for thread)        
        //closeXAConnection();
        logger.info("PaaswordTransaction (" + tid + ") Thread  Exiting.... ");
    }//EoM run

    /**
     * ******* Handlers ****************
     */
    public void executeCUDQueryDuringTransaction(String query, String resid) throws SQLException {
        try {
            Connection connection = ((Connection) conmap.get(tid + "_" + resid));
            logger.info("PaaswordTransaction.executeCUDQueryDuringTransaction-->Connection for CUD: " + tid + " isclosed?: " + connection.isClosed());
            PreparedStatement pst = connection.prepareStatement(query);
            int amount = pst.executeUpdate();
            logger.info("PaaswordTransaction.executeCUDQueryDuringTransaction-->CUD executed - affected rows: " + amount + "(" + tid + ")");
            pst.close();
            logger.info("PaaswordTransaction-->executeCUDQueryDuringTransaction finished! (" + tid + ")");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "PaaswordTransaction.executeCUDQueryDuringTransaction-->  SQL Error during CUD" + ex.getMessage());
        }
    }//EoM  executeCUDQueryDuringTransaction

    public List<Object[]> executeRDuringDistributedTransaction(String query, String resid) throws SQLException {
        Connection connection = ((Connection) conmap.get(tid + "_" + resid));
        logger.info("PaaswordTransaction.executeRDuringDistributedTransaction-->Connection for R: " + tid + " isclosed?: " + connection.isClosed());
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

        logger.info("PaaswordTransaction-->executeRDuringDistributedTransaction finished! (" + tid + ")");
        return results;
    }//EoM      

    private boolean commitTransaction() {
        logger.info("PaaswordTransaction.commitTransaction-->() called " + tid);
        boolean rollback = false;
        boolean success = false;
        try {

            try {
                //step-1 close the connections and clear connection map 
                for (String resid : (List<String>) resources) {
                    Connection connection = (Connection) conmap.get(tid + "_" + resid);
                    connection.close();
                    connection = null;
                    conmap.remove(tid + "_" + resid);
                }
            } catch (Exception ex) {
                logger.info("PaaswordTransaction.commitTransaction--> Connection could not be closed. Have to ROLLback");
                rollback = true;
            } finally {
                if (rollback) {
                    logger.severe("PaaswordTransaction.executeAtomicCUDQuery1--> Rollback");
//                    tm.rollback();
                } else {
//                    tm.commit();
                    logger.info("PaaswordTransaction.commitTransaction--> commited status: " + utm.getStatus());
                }
                utm.close();
                //close xaconnection
                for (String rid : resources) {
                    XAConnection xacon = (XAConnection) xaconmap.get(tid + "_" + rid);
                    xacon.close();
                }

                success = true;
            }
        } catch (Exception ex) {
            rollback = true;
        }
        logger.info("PaaswordTransaction.commit--> Transaction " + tid + " commited");
        return success;
    }//EoM  commitTransaction        

    /**
     * ***************** Pass through for DB Proxy **************************
     */
    //Messagetype 3
    public void HandleexecuteAtomicCUDQuery(String query, String rid, StatementFiller filler, String from, List<WhereClause> where) throws SQLException {
        try {
            Connection connection = ((Connection) conmap.get(tid + "_" + rid));
            logger.info("PaaswordTransaction.HandleexecuteAtomicCUDQuery-->Connection for CUD: " + tid + " isclosed?: " + connection.isClosed());
            //business logic
            PreparedStatement pst = connection.prepareStatement(query);
            filler.fillValues(from, pst, where);
            pst.setEscapeProcessing(false);
            int amount = pst.executeUpdate();
            logger.info("PaaswordTransaction.HandleexecuteAtomicCUDQuery-->CUD executed - affected rows: " + amount + "(" + tid + ")");
            pst.close();
            logger.info("PaaswordTransaction-->HandleexecuteAtomicCUDQuery finished! (" + tid + ")");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "PaaswordTransaction.HandleexecuteAtomicCUDQuery-->  SQL Error during CUD" + ex.getMessage());
        }
    }//EoM

    //Messagetype 4
    public void HandleexecuteAtomicBatchInsertQuery(BatchOfInsertStatements batch, String rid, StatementFiller filler) throws SQLException {
        try {
            Connection connection = ((Connection) conmap.get(tid + "_" + rid));
            logger.info("PaaswordTransaction.HandleexecuteAtomicBatchInsertQuery-->Connection for CUD: " + tid + " isclosed?: " + connection.isClosed());
            //business logic
            PreparedStatement pst = connection.prepareStatement(batch.getSql());
            List<InsertStatement> insertstatements = batch.getInsertstatements();
            for (InsertStatement insertstatement : insertstatements) {
                filler.fillValues(insertstatement.getInto(), pst, insertstatement.getWhere());
                pst.addBatch();
            }
            int[] amount = pst.executeBatch();
            logger.info("PaaswordTransaction.HandleexecuteAtomicBatchInsertQuery-->CUD executed - (" + tid + ")");
            pst.close();
            logger.info("PaaswordTransaction-->HandleexecuteAtomicBatchInsertQuery finished! (" + tid + ")");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "PaaswordTransaction.HandleexecuteAtomicBatchInsertQuery-->  SQL Error during CUD" + ex.getMessage());
        }
    }//EoM

    //Messagetype 5
    public void HandleexecuteAtomicBatchUpdateQuery(BatchOfUpdateStatements batch, String rid, StatementFiller filler) throws SQLException {
        try {
            Connection connection = ((Connection) conmap.get(tid + "_" + rid));
            logger.info("PaaswordTransaction.HandleexecuteAtomicBatchUpdateQuery-->Connection for CUD: " + tid + " isclosed?: " + connection.isClosed());
            //business logic
            PreparedStatement pst = connection.prepareStatement(batch.getSql());
            List<UpdateStatement> updatestatements = batch.getUpdatestatements();
            for (UpdateStatement updatestatement : updatestatements) {
                filler.fillValuesUpdate(pst, updatestatement.getInto(), updatestatement.getUpdateData(), updatestatement.getWhere());
                pst.addBatch();
            }
            int[] amount = pst.executeBatch();
            logger.info("PaaswordTransaction.HandleexecuteAtomicBatchUpdateQuery-->CUD executed - affected rows: (" + tid + ")");
            pst.close();
            logger.info("PaaswordTransaction-->HandleexecuteAtomicBatchUpdateQuery finished! (" + tid + ")");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "PaaswordTransaction.HandleexecuteAtomicBatchUpdateQuery-->  SQL Error during CUD" + ex.getMessage());
        }
    }//EoM

    //Messagetype 6
    public void HandleexecuteAtomicInsertQuery(String query, String rid, StatementFiller filler, String into, List<WhereClause> where) throws SQLException {
        try {
            Connection connection = ((Connection) conmap.get(tid + "_" + rid));
            logger.info("PaaswordTransaction.HandleexecuteAtomicInsertQuery-->Connection for CUD: " + tid + " isclosed?: " + connection.isClosed());
            //business logic
            PreparedStatement pst = connection.prepareStatement(query);
            filler.fillValues(into, pst, where);
            pst.setEscapeProcessing(false);
            int amount = pst.executeUpdate();
            logger.info("PaaswordTransaction.HandleexecuteAtomicInsertQuery-->CUD executed - affected rows: " + amount + "(" + tid + ")");
            pst.close();
            logger.info("PaaswordTransaction-->HandleexecuteAtomicInsertQuery finished! (" + tid + ")");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "PaaswordTransaction.HandleexecuteAtomicInsertQuery-->  SQL Error during CUD" + ex.getMessage());
        }
    }//EoM

    //Messagetype 7
    public void HandleexecuteAtomicUpdateQuery(String query, String rid, StatementFiller filler, String table, Map<String, Object> updateData, List<WhereClause> where) throws SQLException {
        try {
            Connection connection = ((Connection) conmap.get(tid + "_" + rid));
            logger.info("PaaswordTransaction.HandleexecuteAtomicUpdateQuery-->Connection for CUD: " + tid + " isclosed?: " + connection.isClosed());
            //business logic
            PreparedStatement pst = connection.prepareStatement(query);
            filler.fillValuesUpdate(pst, table, updateData, where);
            pst.setEscapeProcessing(false);
            int amount = pst.executeUpdate();
            logger.info("PaaswordTransaction.HandleexecuteAtomicUpdateQuery-->CUD executed - affected rows: " + amount + "(" + tid + ")");
            pst.close();
            logger.info("PaaswordTransaction-->HandleexecuteAtomicUpdateQuery finished! (" + tid + ")");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "PaaswordTransaction.HandleexecuteAtomicUpdateQuery-->  SQL Error during CUD" + ex.getMessage());
        }
    }//EoM

    //Messagetype 8
    public void HandleexecuteAtomicInsertBatch(String query, String rid, StatementFiller filler, List<Column> columns, List<List<String>> currenValues) throws SQLException {
        try {
            Connection connection = ((Connection) conmap.get(tid + "_" + rid));
            logger.info("PaaswordTransaction.HandleexecuteAtomicInsertBatch-->Connection for CUD: " + tid + " isclosed?: " + connection.isClosed());
            //business logic
            PreparedStatement pst = connection.prepareStatement(query);
            filler.fillValuesInsertBatch(pst, columns, currenValues);
            pst.setEscapeProcessing(false);
            int amount = pst.executeUpdate();
            logger.info("PaaswordTransaction.HandleexecuteAtomicInsertBatch-->CUD executed - affected rows: " + amount + "(" + tid + ")");
            pst.close();
            logger.info("PaaswordTransaction-->HandleexecuteAtomicInsertBatch finished! (" + tid + ")");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "PaaswordTransaction.HandleexecuteAtomicInsertBatch-->  SQL Error during CUD" + ex.getMessage());
        }
    }//EoM

    //Messagetype 9    
    public CachedRowSet HandleexecuteAtomicRQuery(String query, String rid, StatementFiller filler, List<String> from, List<WhereClause> where, boolean selectAll, boolean updateable) throws SQLException {
        ResultSet res;
        CachedRowSet crs = null;
        Connection connection = ((Connection) conmap.get(tid + "_" + rid));
        logger.info("PaaswordTransaction.HandleexecuteAtomicRQuery-->Connection for R: " + tid + " isclosed?: " + connection.isClosed());
        PreparedStatement pst;
        if (updateable) {
            pst = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } else { // Default is Readonly
            pst = connection.prepareStatement(query);
        }
        if (!selectAll) {
            try {
                filler.fillValuesSelect(from, pst, where);
            } catch (DatabaseException ex) {
                logger.log(Level.SEVERE, "PaaswordTransaction.HandleexecuteAtomicRQuery", ex);
            }
        }
        pst.setEscapeProcessing(false);
        res = pst.executeQuery();
        //cache the results
        crs = new CachedRowSetImpl();
        crs.populate(res);
        logger.info("PaaswordTransaction.HandleexecuteAtomicRQuery--> executed ");
        logger.info("PaaswordTransaction-->HandleexecuteAtomicRQuery finished! (" + tid + ")");
        return crs;
    }//EoM

    //Messagetype 10
    public CachedRowSet HandleexecuteAtomicRQuery(String query, String rid) throws SQLException {
        ResultSet res;
        CachedRowSet crs = null;
        Connection connection = ((Connection) conmap.get(tid + "_" + rid));
        logger.info("PaaswordTransaction.HandleexecuteAtomicRQuery-->Connection for R: " + tid + " isclosed?: " + connection.isClosed());
        PreparedStatement pst = connection.prepareStatement(query);
        res = pst.executeQuery();
        crs = new CachedRowSetImpl();
        crs.populate(res);
        logger.info("PaaswordTransaction.HandleexecuteAtomicRQuery--> executed ");
        //close jdbc
        res.close();
        pst.close();
        logger.info("PaaswordTransaction-->HandleexecuteAtomicRQuery finished! (" + tid + ")");
        return crs;
    }//EoM        

    //Messagetype 11
    public Map<String, List<Column>> HandleexecuteLoadSchemaQuery(String rid) throws SQLException {
        Map<String, List<Column>> tableToColumns = new TreeMap<String, List<Column>>();
        Connection connection = ((Connection) conmap.get(tid + "_" + rid));
        logger.info("PaaswordTransaction.executeLoadSchemaQuery-->Connection for R: " + tid + " isclosed?: " + connection.isClosed());

        DatabaseMetaData metadata = connection.getMetaData();
        String[] types = {"TABLE"};
        ResultSet tables = metadata.getTables(null, null, "%", types);

        while (tables.next()) {
            List<Column> cols = new ArrayList<Column>();
            String tableName = tables.getString(3);

            // That's the only way to get the column names and types
            Statement stm = connection.createStatement();
            //DEBUG
            //System.out.println("tablename: "+tableName);
            ResultSet rs = stm.executeQuery("select * from \"" + tableName + "\" limit 1");

            ResultSetMetaData rsMd = rs.getMetaData();
            int colCount = rsMd.getColumnCount();

            for (int i = 1; i < colCount + 1; i++) {
                String name = rsMd.getColumnName(i);
                int type = rsMd.getColumnType(i);
                Column c;
                switch (type) {
                    case Types.INTEGER:
                        c = new Column(Type.Integer, name, -1, false);
                        cols.add(c);
                        break;
                    case Types.DOUBLE:
                        c = new Column(Type.Double, name, -1, false);
                        cols.add(c);
                        break;
                    case Types.VARCHAR:
                        c = new Column(Type.String, name, -1, false); // TO check 
                        cols.add(c);
                        break;
                    case Types.DATE:
                        c = new Column(Type.Date, name, -1, false);
                        cols.add(c);
                        break;
                    case Types.BOOLEAN:
                        c = new Column(Type.Boolean, name, -1, false);
                        cols.add(c);
                        break;
                    case Types.BIT: //Booleans are Interpretated als Bits sometimes
                        c = new Column(Type.Boolean, name, -1, false);
                        cols.add(c);
                        break;
                    case Types.CHAR:
                        c = new Column(Type.String, name, rsMd.getColumnDisplaySize(i), false);
                        cols.add(c);
                        break;
                    default:
                        throw new UnknownTypeException("Does not support type: " + type);
                }
            }//for
            //close
            rs.close();
            stm.close();
            tableToColumns.put(tableName, cols);
        }//while

        logger.info("PaaswordTransaction-->executeLoadSchemaQuery finished! (" + tid + ")");
        return tableToColumns;
    }//EoM

}//EoC
