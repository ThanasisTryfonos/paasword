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
package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.StatementFiller;
import eu.paasword.dbproxy.database.utils.StatementPreparer;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.exceptions.InvalidStatementException;
import eu.paasword.dbproxy.helper.AdapterHelper;
import eu.paasword.dbproxy.transaction.DistributedTransactionalManager;
import eu.paasword.dbproxy.utils.SchemeLoader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a concrete instance of a SQL Database. Except for select
 * statements and whole queries, aliasing is not allowed. Arguments for "in" and
 * "between" must be stored in "List<?>". Select clauses in the where clause are
 * not supported
 *
 * @author Yvonne Muelle
 */
public class SQLDatabase implements Database {

    private Logger logger = Logger.getLogger(SQLDatabase.class.getName());

    private String adapterid;   //very crucial during the lookup of the exact proxy
    private boolean autoEscapingOn = false;
    private DBScheme scheme;
    private String databaseName;
    private StatementFiller filler;
    private HashMap<String, BatchOfUpdateStatements> batchstatUpdate;
    private HashMap<String, BatchOfInsertStatements> batchstatInsert;
//    private Connection connection;
    DistributedTransactionalManager dtm;

    /**
     * Constructs a new object of this class
     *
     * @param dbConfig contains all information on how to access the real
     * database this object encapsulates.
     * @throws DatabaseException if a connection to the database is not possible
     * @throws IOException If database scheme could not be loaded from file
     */
    public SQLDatabase(Map<String, String> dbConfig,String sessionid) throws DatabaseException, IOException {
        adapterid = dbConfig.get("adapterid");
        databaseName = dbConfig.get("name");
        String url = dbConfig.get("driverConn") + "://" + dbConfig.get("host") + "/" + databaseName;
        String user = dbConfig.get("user");
        String pwd = dbConfig.get("password");
        logger.info("SQLDatabase--> Creating new DB for adapter: " + adapterid + "(" + databaseName + ")");
        
        //load the schema once
        try {
            dtm = AdapterHelper.getDTMByAdapterId(adapterid);
            //String tid = dtm.initiateTransaction();
            scheme = new DBScheme(SchemeLoader.loadScheme("database", databaseName, adapterid, databaseName, sessionid));
            //dtm.commitTransaction(tid);
        } catch (Exception ex) {
            logger.severe("Error Schema could not be loaded");
        }

        filler = new StatementFiller(scheme);
        batchstatUpdate = new HashMap<String, BatchOfUpdateStatements>();
        batchstatInsert = new HashMap<String, BatchOfInsertStatements>();
    }

    /**
     * to drop the current scheme and generates a new one
     */
    @Override
    public void refreshLocalScheme(DBScheme newScheme, String sessionid) {
        SchemeLoader.dropScheme(scheme.getTableNames(), adapterid, databaseName, sessionid);
        SchemeLoader.createScheme(newScheme);
    }

    /**
     * to drop the current scheme and generates a new one
     */
    @Override
    public void refreshLocalScheme(Map<String, List<Column>> newScheme, String sessionid) {
        SchemeLoader.dropScheme(scheme.getTableNames(), adapterid, databaseName, sessionid);
        SchemeLoader.createScheme(newScheme, adapterid, databaseName, sessionid);
        try {
            scheme = new DBScheme(SchemeLoader.loadScheme("database", databaseName, adapterid, databaseName, sessionid));
            filler.refreshScheme(scheme);
        } catch (DatabaseException | IOException e) {
            e.printStackTrace();
        }
    }//EoM

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#getRelationNames()
     */
    @Override
    public Set<String> getRelationNames() {
        return scheme.getTableNames();
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#getColumns(java.lang.String)
     */
    @Override
    public List<Column> getColumns(String table) {
        return scheme.getColumns(table);
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#delete(java.lang.String, java.util.Map,
     * java.lang.String)
     */
    @Override
    public void delete(String from, List<WhereClause> where, String sessionid) throws DatabaseException {
        String sql = StatementPreparer.prepareString("delete from \"" + from + "\" where ", where, true);
        logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing delete-query " + sql);
        try {
            //initialize Transactional manager
//            DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
            dtm.executeAtomicCUDQuery(sql, this.databaseName, filler, from, where, sessionid);
            //old
//            PreparedStatement stat = connection.prepareStatement(sql);
//            filler.fillValues(from, stat, where);
//            stat.setEscapeProcessing(autoEscapingOn);
//            stat.executeUpdate();            
            logger.info("SQLDatabase-->Performing query " + sql + " was successfull ");
        } catch (SQLException e) {
            String msg = "Delete statement " + sql + " could not be performed! \n";
            logger.log(Level.SEVERE, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#deleteAll(java.lang.String)
     */
    @Override
    public void deleteAll(String from, String sessionid) throws DatabaseException {
        String sql = "delete from \"" + from + "\"";
        logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing deleteAll-query " + sql);
        try {
            //initialize Transactional manager
//            DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
            dtm.executeAtomicCUDQuery(sql, this.databaseName, sessionid);
            //old
//            PreparedStatement stat = connection.prepareStatement(sql);
//            stat.setEscapeProcessing(autoEscapingOn);
//            stat.executeUpdate();
            logger.info("SQLDatabase-->Performing query " + sql + " was successfull ");
        } catch (SQLException e) {
            String msg = "Delete statement " + sql + " could not be performed! \n";
            logger.log(Level.SEVERE, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /**
     */
    @Override
    public void insertBatch(String into, List<Column> columns, List<List<String>> values, String sessionid) throws DatabaseException {
        logger.info("into: " + "\"" + into + "\"");
        logger.info("columns: " + columns);
        logger.info("Batch");
        final int MAXSIZE = 32767;
        if (!values.isEmpty()) {
            int valuesPerColumn = values.get(0).size();
            int currentSize = (values.size() * valuesPerColumn);
            int batchSize = ((MAXSIZE + 1) / valuesPerColumn) - 1;
            double numberOfBatches = currentSize / 32767.0;
            int intNumber = (int) Math.ceil(numberOfBatches);
            int start = 0;
            int end = Math.min(start + batchSize, values.size());
            for (int i = 0; i < intNumber; i++) {
                List<List<String>> currenValues = values.subList(start, end);
                String sql;
                PreparedStatement stat;
                try {
                    sql = StatementPreparer.prepareInsertBatch(into, columns, currenValues);
                } catch (InvalidStatementException e1) {
                    throw new DatabaseException(e1.getLocalizedMessage());
                }
                try {
                    logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing insert-query " + sql);
//                    DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
                    dtm.executeAtomicInsertBatch(sql, this.databaseName, filler, columns, currenValues, sessionid);
                    //old
//                    stat = connection.prepareStatement(sql);
//                    filler.fillValuesInsertBatch(stat, columns, currenValues);
//                    stat.setEscapeProcessing(autoEscapingOn);
//                    stat.executeUpdate();                    
                    logger.info("Performing query " + sql + " was successfull. ");
                } catch (SQLException e) {
                    String msg = "Insert statement " + sql + " could not be performed! \n";
                    logger.log(Level.SEVERE, msg, e);
                    throw new DatabaseException(msg, e);
                }
                start = end;
                end = Math.min(start + batchSize, values.size());
            }
        } else {
            logger.info("Nothing to insert in " + into);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#insert()
     */
    @Override
    public void insert(String into, List<String> columns, List<Object> values, boolean batch, String sessionid) throws DatabaseException {
//        DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
        // DEBUG
        logger.info("into: " + into);
        logger.info("columns: " + columns);
        logger.info("values: " + values);
        if (columns.size() != values.size()) {
            throw new DatabaseException("Number of Values does not match number of columns!");
        }
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        String sql = "";
        try {
            sql = StatementPreparer.prepareInsert(into, columns, values, where);
        } catch (InvalidStatementException e1) {
            throw new DatabaseException("Column name is null!");
        }
        // DEBUG
        logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing Insert-query " + sql);

        BatchOfInsertStatements batchstat;
        try {
            if (batch) {
                batchstat = getInsertBatch(sql, into, batchstatInsert);
                //filler.fillValues(into, stat, where);
                //stat.setEscapeProcessing(autoEscapingOn);
                batchstat.addInsertStatement(into, where);
                logger.info("Added query " + sql + " to batch");
            } else {
                dtm.executeAtomicInsertQuery(sql, databaseName, filler, into, where, sessionid);
                //old
//                stat = connection.prepareStatement(sql);
//                filler.fillValues(into, stat, where);
//                stat.setEscapeProcessing(autoEscapingOn);
//                stat.executeUpdate();
//                stat.close();
                logger.info("Performing query " + sql + " was successfull.");
            }

        } catch (SQLException e) {
            String msg = "Insert statement " + sql + " could not be performed! \n";
            logger.log(Level.SEVERE, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#insert(java.lang.String,
     * java.util.List)
     */
    @Override
    public void insert(String into, List<Object> values, String sessionid) throws DatabaseException {
        List<String> keys = new ArrayList<String>(scheme.getNames(into));
        insert(into, keys, values, false, sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#update(java.lang.String, java.util.Map,
     * java.util.Map, java.lang.String)
     */
    @Override
    public void update(String table, Map<String, Object> updateData, List<WhereClause> where, boolean batch, String sessionid) throws DatabaseException {

//        DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
        if (updateData.size() > getColumns(table).size()) {
            throw new DatabaseException("More Columns to be updated than exist!\n");
        }
        String sql;
        try {
            sql = StatementPreparer.prepareUpdate(table, updateData, where);
            logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing Insert-query " + sql);
        } catch (InvalidStatementException e1) {
            throw new DatabaseException(e1.getMessage());
        }

        try {
            BatchOfUpdateStatements batchstat;
            if (batch) {
                batchstat = getUpdateBatch(sql, table, batchstatUpdate);
                //filler.fillValuesUpdate(stat, table, updateData, where);
                //stat.setEscapeProcessing(autoEscapingOn);
                batchstat.addUpdateStatement(table, where, updateData);
                logger.info("Added query " + sql + " to batch");
            } else {
                dtm.executeAtomicUpdateQuery(sql, databaseName, filler, table, updateData, where, sessionid);
                //old
//                stat = connection.prepareStatement(sql);
//                filler.fillValuesUpdate(stat, table, updateData, where);
//                stat.setEscapeProcessing(autoEscapingOn);
//                stat.executeUpdate();
                logger.info("Performing query " + sql + " was successfull ");
            }
        } catch (SQLException e) {
            String msg = "Update statement " + sql + " could not be performed! \n";
            logger.log(Level.SEVERE, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#updateAll(java.lang.String,
     * java.util.Map)
     */
    @Override
    public void updateAll(String table, Map<String, Object> updateData, String sessionid) throws DatabaseException {
        update(table, updateData, new ArrayList<WhereClause>(), false, sessionid);
    }

    /*
     * (non-Javadoc)
	 * 
	 * @see prototype.database.Database#setAutoEscaping(boolean)
     */
    /**
     * {@inheritDoc} This database does not support this method! Setting the
     * escaping on or off does not have any effect.
     */
    @Override
    public void setAutoEscaping(boolean on) {
        autoEscapingOn = on;
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#performQuery(String)
     */
    @Override
    public ResultSet performQuery(String query, String sessionid) throws DatabaseException {
        try {
            logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing performQuery " + query);
//            DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
            ResultSet res = dtm.executeAtomicRQuery(query, this.databaseName, sessionid);
            //old
//            Statement stat = connection.createStatement();
//            stat.setEscapeProcessing(true);
//            ResultSet res = stat.executeQuery(query);            
            logger.info("Performing query " + query + " was successfull ");
            return res;
        } catch (SQLException e) {
            String msg = "Select statement " + query + " could not be performed! \n";
            Set<String> relNames = getRelationNames();
            for (String name : relNames) {
                deleteAll(name, sessionid);
            }
            logger.log(Level.SEVERE, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#select()
     */
    @Override
    public ResultSet select(List<String> select, List<String> from, List<WhereClause> where, boolean distinct, List<String> groupBy, boolean updatable, String sessionid) throws DatabaseException {
        if (where == null) {
            throw new DatabaseException("No WhereClause specified!\n");
        }
        if (select == null) {
            throw new DatabaseException("No columns to select specified!\n");
        }
        if (from == null) {
            throw new DatabaseException("No table to select from specified!\n");
        }
        String sql;
        try {
            sql = StatementPreparer.prepareSelect(select, from, where, distinct, groupBy);
        } catch (InvalidStatementException e1) {
            throw new DatabaseException(e1.getMessage());
        }
        try {
            logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing select-query " + sql);
            return executeSelect(false, updatable, sql, from, where, sessionid);
        } catch (SQLException e) {
            String msg = "Select statement " + sql + " could not be performed";
            logger.log(Level.SEVERE, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#selectAll()
     */
    @Override
    public ResultSet selectAll(List<String> select, List<String> from, boolean updateable, String sessionid) throws DatabaseException {
        if (select.isEmpty()) {
            throw new DatabaseException("No columns to be selected specified");
        }
        if (from.isEmpty()) {
            throw new DatabaseException("No tables to be selected specified");
        }
        String sql = "";
        try {
            sql = StatementPreparer.prepareSelectAll(select, from);
        } catch (InvalidStatementException e1) {
            throw new DatabaseException(e1.getMessage());
        }
        try {
            logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing selectAll-query " + sql);
            return executeSelect(true, updateable, sql, from, null, sessionid);
        } catch (SQLException e) {
            String msg = "Select statement " + sql + " could not be performed! \n";
            logger.log(Level.SEVERE, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#selectOrdered()
     */
    @Override
    public ResultSet selectOrdered(List<String> select, List<String> from, List<WhereClause> where, boolean distinct, List<String> groupBy, List<String> by, boolean updatable, String sessionid) throws DatabaseException {
        String sql = null;
        try {
            sql = StatementPreparer.prepareSelectOrdered(select, from, where, distinct, groupBy, by);
        } catch (InvalidStatementException e1) {
            throw new DatabaseException(e1.getMessage());
        }
        try {
            logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing selectOrdered-query " + sql);
            return executeSelect(false, updatable, sql, from, where, sessionid);
        } catch (SQLException e) {
            String msg = "Select statement " + sql + " could not be performed! \n";
            logger.log(Level.SEVERE, msg, e);
            throw new DatabaseException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#dropTable(String)
     */
    @Override
    public void dropTable(String tableName, String sessionid) throws DatabaseException {        //migrated
        String sql = "Drop table if exists \"" + tableName + "\";";
        try {
            logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing Drop-query " + sql);

//            DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
            dtm.executeAtomicCUDQuery(sql, databaseName, sessionid);
            //old
//            PreparedStatement stat = connection.prepareStatement(sql);
//            stat.setEscapeProcessing(autoEscapingOn);
//            stat.executeUpdate();
            logger.info("Performing query " + sql + " was successfull.");
        } catch (SQLException e) {
            String msg = "Select statement " + sql + " could not be performed! \n";
            logger.log(Level.SEVERE, msg, e);
            throw new DatabaseException(msg, e);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#executeUpdateBatch()
     */
    @Override
    public boolean executeUpdateBatch(String sessionid) throws SQLException {
        return executeUpdateBatch(batchstatUpdate, sessionid);
    }

    private boolean executeUpdateBatch(HashMap<String, BatchOfUpdateStatements> batchstat, String sessionid) throws SQLException {
//        DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);

        if (!batchstat.isEmpty()) {

            for (String table : batchstat.keySet()) {
                dtm.executeAtomicBatchUpdateQuery(batchstat.get(table), databaseName, filler, sessionid);
            }

            //old
//            for (PreparedStatement stat : batchstat.values()) {
//                stat.executeBatch();
//                stat.close();
//            }
            //Avoid Concurrent Modification Exceptions
            Iterator<Entry<String, BatchOfUpdateStatements>> it = batchstat.entrySet().iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
            return true;
        } else {
            return false;
        }
    }//EoM    

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.Database#executeInsertBatch()
     */
    @Override
    public boolean executeInsertBatch(String sessionid) throws SQLException {
        return executeInsertBatch(batchstatInsert, sessionid);
    }

    //Method to finally execute all stored Batchqueries
    private boolean executeInsertBatch(HashMap<String, BatchOfInsertStatements> batchstat, String sessionid) throws SQLException {
//        DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);

        if (!batchstat.isEmpty()) {

            for (String table : batchstat.keySet()) {
                dtm.executeAtomicBatchInsertQuery(batchstat.get(table), databaseName, filler, sessionid);
            }

            //old
//            for (PreparedStatement stat : batchstat.values()) {
//                stat.executeBatch();
//                stat.close();
//            }
            //Avoid Concurrent Modification Exceptions
            Iterator<Entry<String, BatchOfInsertStatements>> it = batchstat.entrySet().iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
            return true;
        } else {
            return false;
        }
    }//EoM

    //Prepares the given Batchupdatestatement e.g. adds the new query to it. Be careful with this it can cause inconsistencies
    private BatchOfInsertStatements getInsertBatch(String sql, String table, HashMap<String, BatchOfInsertStatements> batchstat) throws SQLException {
//        DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
        if (batchstat.containsKey(table)) {
            return batchstat.get(table);
        } else {
//            PreparedStatement stat = connection.prepareStatement(sql);
            BatchOfInsertStatements stat = new BatchOfInsertStatements(sql, table, databaseName);
            batchstat.put(table, stat);
            return stat;
        }
    }//EoM

    private BatchOfUpdateStatements getUpdateBatch(String sql, String table, HashMap<String, BatchOfUpdateStatements> batchstat) throws SQLException {
//        DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
        if (batchstat.containsKey(table)) {
            return batchstat.get(table);
        } else {
//            PreparedStatement stat = connection.prepareStatement(sql);
            BatchOfUpdateStatements stat = new BatchOfUpdateStatements(sql, table, databaseName);
            batchstat.put(table, stat);
            return stat;
        }
    }//EoM    

    //Method to bundle different selects in one method e.g. runs the actual queries
    private ResultSet executeSelect(boolean selectAll, boolean updatable, String sql, List<String> from, List<WhereClause> where, String sessionid) throws SQLException, DatabaseException {
        //new
        //initialize Transactional manager
        logger.info("SQLDatabase.executeSelect--> Getting dt for adapter " + adapterid + " and resource " + this.databaseName);
//        DistributedTransactionalManager dtm = AdapterHelper.getTmanagerById(adapterid);
        logger.info("SQLDatabase.executeSelect--> dt.isNull:  " + (dtm == null) + "  " + AdapterHelper.adaptermap.size());
        ResultSet res = dtm.executeAtomicRQuery(sql, this.databaseName, filler, from, where, selectAll, updatable, sessionid);
        //old
//        PreparedStatement stat = null;
//        if (updatable) {
//            logger.info("[Session:" + sessionid + " db:" + databaseName + " ] Executing Select-query " + sql);
//            stat = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
//        } else { // Default is Readonly
//            stat = connection.prepareStatement(sql);
//        }
//        if (!selectAll) {
//            filler.fillValuesSelect(from, stat, where);
//        }
//        stat.setEscapeProcessing(autoEscapingOn);
//        ResultSet res = stat.executeQuery();

        logger.info("Performing query " + sql + " was successfull.");
        return res;
    }//EoM

}//EoC
