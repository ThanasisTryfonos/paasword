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
package eu.paasword.dbproxy.impl;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.*;
import com.foundationdb.sql.parser.ConstraintDefinitionNode.ConstraintType;
import eu.paasword.dbproxy.DBProxyOrchestrator;
import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.database.RemoteDBAdminstration;
import eu.paasword.dbproxy.database.RemoteDBConstants;
import eu.paasword.dbproxy.database.RemoteDatabase;
import eu.paasword.dbproxy.database.utils.*;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.encryption.EncryptionHelperBase;
import eu.paasword.dbproxy.encryption.EncryptionHelperWibu;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.exceptions.PluginLoadFailure;
import eu.paasword.dbproxy.helper.AdapterHelper;
import eu.paasword.dbproxy.output.OutputHandler;
import eu.paasword.dbproxy.transaction.DistributedTransactionalManager;
import eu.paasword.dbproxy.utils.*;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

/**
 * Encapsulates the possibility to execute sql statements (SQL-92), independent
 * of the database in which the data is stored and where and how the information
 * is stored (e.g. remotely stored, encrypted or not)
 *
 * @author Yvonne Muelle
 * @author Tobias Beck
 */
public class Adapter {

    protected RemoteDatabase remoteDB; // defined in config-file
    protected Database localDB; // defined in config-file
    private SQLParser parser = new SQLParser();
    private final boolean HARDWARE_ENC = false;
    private final String ESCAPENULL = "'null'";
    private String dbConfig;

    private Logger logger = Logger.getLogger(Adapter.class.getName());

    public String getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(String dbConfig) {
        this.dbConfig = dbConfig;
    }

    /**
     * Constructs a new object of this class from a default config
     *
     * @throws IOException If a file could not be accessed
     * @throws PluginLoadFailure If a class could not be loaded.
     * @throws DatabaseException
     */
    public Adapter() throws PluginLoadFailure, IOException, DatabaseException {
        parseDBConfig("config-default.xml");
    }

    /**
     * Constructs a new object of this class from the given config
     *
     * @param dbConfig contains all information about the databases and how to
     * connect to them.
     * @throws IOException If a file could not be accessed
     * @throws PluginLoadFailure If a class could not be loaded.
     * @throws DatabaseException
     */
    public Adapter(String dbConfig) throws PluginLoadFailure, IOException, DatabaseException {
        logger.info("Adapter parsing Config: " + dbConfig);
        parseDBConfig(dbConfig);
        this.dbConfig = dbConfig;
    }

    public Adapter(String dbConfig, String tenantKey) throws PluginLoadFailure, IOException, DatabaseException {
        logger.info("Adapter parsing Config: " + dbConfig);
        parseDBConfig(dbConfig, tenantKey);
        this.dbConfig = dbConfig;
    }

    // loads the xml config file and creates the local and remote database
    // objects.
    private void parseDBConfig(String adapterid, String tenantKey) throws PluginLoadFailure, IOException, DatabaseException {
        Map<String, String> localDbConf = ConfigParser.getInstance(adapterid).getLocalDatabase();
        Map<String, String> globalOptions = ConfigParser.getInstance(adapterid).getGlobalConfig();
        if (globalOptions.get("logging").equals("false")) {
            logger.setUseParentHandlers(false);
            logger.setLevel(null);

            LogManager.getLogManager().reset();
            Logger globalLogger = Logger
                    .getLogger(Logger.GLOBAL_LOGGER_NAME);
            globalLogger.setLevel(Level.OFF);
        }

        Encryption encrypt = new EncryptionHelperBase(tenantKey);
        remoteDB = new RemoteDBAdminstration(encrypt, adapterid, "atomic");        //TODO   empty sessionid

        localDB = DatabaseLoader.loadDatabase(localDbConf);
        reinitializeLocalDB("atomic");                                //TODO check empty sessionid

    }

    // loads the xml config file and creates the local and remote database
    // objects.
    private void parseDBConfig(String adapterid) throws PluginLoadFailure, IOException, DatabaseException {
        Map<String, String> localDbConf = ConfigParser.getInstance(adapterid).getLocalDatabase();
        Map<String, String> globalOptions = ConfigParser.getInstance(adapterid).getGlobalConfig();
        if (globalOptions.get("logging").equals("false")) {
            logger.setUseParentHandlers(false);
            logger.setLevel(null);

            LogManager.getLogManager().reset();
            Logger globalLogger = Logger
                    .getLogger(Logger.GLOBAL_LOGGER_NAME);
            globalLogger.setLevel(Level.OFF);
        }

        Encryption encrypt;
        if (!HARDWARE_ENC) {
            encrypt = new EncryptionHelperBase(null);
        } else {
            encrypt = new EncryptionHelperWibu();
        }

//        encrypt = new EncryptionHelperBase();
        remoteDB = new RemoteDBAdminstration(encrypt, adapterid, "atomic");        //TODO   empty sessionid

        localDB = DatabaseLoader.loadDatabase(localDbConf);
        reinitializeLocalDB("atomic");                                //TODO check empty sessionid          

    }

    /**
     * drops the local DB and recreates it whit the current scheme from remote
     */
    private void reinitializeLocalDB(String sessionid) {
        localDB.refreshLocalScheme(remoteDB.getPlaineDBScheme(), sessionid);
    }

    /**
     * Executes the SQL statements in query and returns the result as
     * {@link OutputHandler}.
     *
     * @param query contains all SQL statements that should be executed.
     * Delimiter ";". Even if it is a single statement, then it is close by ";"
     * @return an {@link OutputHandler} object
     * @throws ParseException if the query contains wrong SQL syntax.
     * @throws DatabaseException If an error occurs during query execution
     * @throws StandardException
     */
    public synchronized OutputHandler query(String query) throws ParseException, DatabaseException, StandardException {
        //Necessary to remove ; to avoid parser errors with fdb
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }

        ArrayList<ResultSet> results = new ArrayList<ResultSet>();

        logger.info("Adapter . Attempting to det DTM for " + this.getDbConfig());
        DistributedTransactionalManager dtm = AdapterHelper.getDTMByAdapterId(this.getDbConfig());
        try {
            String sessionid = dtm.initiateTransaction();

            List<StatementNode> nodes = null;
            try {
                logger.info("Adapter.query: " + query + " allocated session: " + sessionid);
                nodes = parser.parseStatements(query);
            } catch (StandardException e) {
                e.printStackTrace();
            }

            boolean localinsert = false;
            if (nodes != null) {
                for (StatementNode node : nodes) {
                    boolean successful = false;
                    switch (node.getNodeType()) {
                        case (NodeTypes.CREATE_TABLE_NODE):
                            handleCreateTable((CreateTableNode) node, sessionid);
                            localinsert = false;
                            break;
                        case (NodeTypes.DROP_TABLE_NODE):
                            handleDropTable((DropTableNode) node, sessionid);
                            localinsert = false;
                            break;
                        case (NodeTypes.CURSOR_NODE):                    // Join is handled here
                            CursorNode cursor = (CursorNode) node;
                            if (cursor.statementToString().equals("SELECT")) {
                                results.add(handleSelect((CursorNode) node, query, sessionid));
                            }
                            localinsert = true;
                            break;
                        case (NodeTypes.INSERT_NODE):
                            successful = handleInsert((InsertNode) node, sessionid);
                            localinsert = false;
                            break;
                        case (NodeTypes.DELETE_NODE):
                            successful = handleDelete((DeleteNode) node, sessionid);
                            localinsert = false;
                            break;
                        case (NodeTypes.UPDATE_NODE):
                            successful = handleUpdate((UpdateNode) node, sessionid);
                            localinsert = false;
                            break;
                        case (NodeTypes.ALTER_TABLE_NODE):
                            successful = handleAlterTabe((AlterTableNode) node, sessionid);
                            localinsert = false;
                            break;
                        default:
                            throw new UnsupportedOperationException(
                                    "This SQL query is not supported: "
                                    + node.statementToString());
                    }
                    if (successful) {
                        logger.log(Level.INFO, "Query: " + query + " successfully performed!");
                    }
                    // The content of the local database is erased after having
                    // evaluated one of the statements.
                    if (localinsert) {
                        Set<String> relNames = localDB.getRelationNames();
                        for (String name : relNames) {
                            localDB.deleteAll(name, sessionid);
                        }
                    }//if
                }//for statement nodes
            }// nodes != null

            dtm.commitTransaction(sessionid);

        } catch (SQLException | SystemException | IllegalStateException | RollbackException | NotSupportedException ex) {
            Logger.getLogger(Adapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new OutputHandler(results);
    }//EoM

    /**
     * Gets the names of the columns in the query which creates a table.
     *
     * @param query contains a create SQL statement that should be executed
     * @return the names of the columns
     * @throws DatabaseException If an error occurs during query execution
     */
    public ArrayList<String> getColumnNames(String query) throws DatabaseException {
        ArrayList<String> colNames = new ArrayList<String>();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }

        List<StatementNode> nodes = null;

        try {
            nodes = parser.parseStatements(query);
        } catch (StandardException e) {
            e.printStackTrace();
        }

        if (nodes != null) {
            for (StatementNode node : nodes) {
                if (node.getNodeType() == NodeTypes.CREATE_TABLE_NODE) {
                    colNames.addAll(StatementExtractor.extractColNames((CreateTableNode) node));
                }
            }
        }

        boolean containsNull = colNames.remove(null);
        while (containsNull) {
            containsNull = colNames.remove(null);
        }

        return colNames;
    }

    /**
     * Saves on which server every column is saved.
     *
     * @param tableName
     * @param mapping column-server mapping
     * @throws DatabaseException
     */
    public void saveColumnServerMapping(String tableName, Map<String, Integer> mapping, String sessionid) throws DatabaseException {
        if (remoteDB instanceof RemoteDBAdminstration) {
            ((RemoteDBAdminstration) remoteDB).saveColumnServerMapping(tableName, mapping, sessionid);
        }
    }

    //Handles all supported alter Table operations, currently: Add Column, Drop Column, Rename Column
    private boolean handleAlterTabe(AlterTableNode node, String sessionid) throws DatabaseException {
        String table = node.getFullName();
        TableElementNode tableelement = node.getTableElementList().get(0);
        switch (tableelement.getNodeType()) {
            case NodeTypes.CONSTRAINT_DEFINITION_NODE:
                ConstraintDefinitionNode constraint = (ConstraintDefinitionNode) tableelement;
                ArrayList<String> colummname = new ArrayList<String>();
                for (ResultColumn col : constraint.getColumnList()) {
                    colummname.add(col.getName());
                }
                if (constraint.getConstraintType() == ConstraintType.UNIQUE) {
                    remoteDB.addConstraint(table, colummname, true, false, sessionid);
                }
                if (constraint.getConstraintType() == ConstraintType.PRIMARY_KEY) {
                    remoteDB.addConstraint(table, colummname, false, true, sessionid);
                }
                break;
            case NodeTypes.COLUMN_DEFINITION_NODE:
                ColumnDefinitionNode definition = (ColumnDefinitionNode) tableelement;
                boolean var = (definition.getType().getTypeName().equalsIgnoreCase("varchar"));
                remoteDB.addColumn(table, definition.getColumnName(), StatementExtractor.extractAddTable(definition.getType().getTypeName()), definition.getType().getMaximumWidth(), var, sessionid);
                break;
            case NodeTypes.DROP_COLUMN_NODE:
                ModifyColumnNode drop = (ModifyColumnNode) tableelement;
                remoteDB.dropColumn(table, drop.getColumnName(), sessionid);
                break;
            case NodeTypes.AT_RENAME_COLUMN_NODE:
                AlterTableRenameColumnNode rename = (AlterTableRenameColumnNode) tableelement;
                remoteDB.renameColumn(table, rename.getName(), rename.newName(), sessionid);
                break;
            default:
        }
        reinitializeLocalDB(sessionid);
        return false;
    }

    //handles the creation of a table
    private void handleCreateTable(CreateTableNode create, String sessionid) throws DatabaseException {
        remoteDB.createTable(create.getObjectName().getTableName(), StatementExtractor.extractCreateTable(create), sessionid);
        reinitializeLocalDB(sessionid);
        System.out.println("Succesfully created table " + create.getObjectName().getTableName());
    }

    //handles the dropping of a table
    private void handleDropTable(DropTableNode drop, String sessionid) throws DatabaseException {
        boolean ifexists = (drop.getExistenceCheck().ordinal() == 1); //Check whether ifexists is demanded
        remoteDB.dropTable(drop.getObjectName().getTableName(), ifexists, sessionid);
        reinitializeLocalDB(sessionid);
        System.out.println("Dropped table: " + drop.getObjectName().getTableName());
    }

    //handles a normal select routine
    private ResultSet handleSelect(CursorNode select, String query, String sessionid) throws DatabaseException, StandardException {
        QueryKeyVault keysToBeInserted = new QueryKeyVault();
        if (select.getResultSetNode().getNodeType() == NodeTypes.SELECT_NODE) {
            SelectNode selectNode = (SelectNode) select.getResultSetNode();
            fetchKeysFor(selectNode, keysToBeInserted, false, sessionid);
        } else { //Contains Set
            traverseSelects(select.getResultSetNode(), keysToBeInserted, sessionid);
        }
        insertKeysFromMapToLocalDB(keysToBeInserted, new ArrayList<String>(), sessionid);
        return localDB.performQuery(query, sessionid);
    }

    private void insertKeysFromMapToLocalDB(final IKeyVault keyVault, List<String> escapedColumns, String sessionid) throws DatabaseException {
        for (String tableName : keyVault.getKeysToBeInserted().keySet()) {
            List<List<String>> dataForTable = remoteDB.selectData(keyVault.getKeysToBeInserted().get(tableName), sessionid);
            insertIntoLocalDB(tableName, dataForTable, escapedColumns, sessionid);
        }
    }

    //handles an insert into the remoteDB
    private boolean handleInsert(InsertNode insert, String sessionid) throws DatabaseException, StandardException {
        String into = insert.getTargetTableName().getTableName();
        if (insert.getResultSetNode().getNodeType() == NodeTypes.ROW_RESULT_SET_NODE) {
            List<Object> values = new ArrayList<Object>();
            for (ResultColumn column : insert.getResultSetNode().getResultColumns()) {
                ConstantNode val = (ConstantNode) column.getExpression();
                if (val.getValue() != null && val.getValue().equals("null")) {
                    values.add(ESCAPENULL);
                } else {
                    values.add(val.getValue());
                }
            }
            insertIntoRemoteDB(insert.getTargetColumnList(), values, into, sessionid);
            return true;
            //In case more than one Value should be inserted
        } else if (insert.getResultSetNode().getNodeType() == NodeTypes.ROWS_RESULT_SET_NODE) {
            RowsResultSetNode rows = (RowsResultSetNode) insert.getResultSetNode();
            for (RowResultSetNode row : rows.getRows()) {
                List<Object> values = new ArrayList<Object>();
                for (ResultColumn column : row.getResultColumns()) {
                    ConstantNode val = (ConstantNode) column.getExpression();
                    if (val.getValue() != null && val.getValue().equals("null")) {
                        values.add(ESCAPENULL);
                    } else {
                        values.add(val.getValue());
                    }
                }
                insertIntoRemoteDB(insert.getTargetColumnList(), values, into, sessionid);
            }
            return true;
            //
        } else if (insert.getResultSetNode().getNodeType() == NodeTypes.SELECT_NODE) {
            SelectNode source = (SelectNode) insert.getResultSetNode();
            FromList from = source.getFromList();
            ArrayList<String> from2 = new ArrayList<String>();
            for (int i = 0; i < from.size(); i++) {
                localDB.deleteAll(from.get(i).getTableName().getFullTableName(), sessionid);
                from2.add(from.get(i).getTableName().getFullTableName());
            }

            IKeyVault keysToBeInserted = new QueryKeyVault();
            fetchKeysFor(source, keysToBeInserted, false, sessionid);
            insertKeysFromMapToLocalDB(keysToBeInserted, new ArrayList<String>(), sessionid);
            List<String> select = new ArrayList<String>();
            select.add("*");
            ResultSet res = localDB.selectAll(select, from2, false, sessionid);
            ResultSetMetaData rsMetaData;
            try {
                rsMetaData = res.getMetaData();
                int numberOfColumns = rsMetaData.getColumnCount() + 1;
                ArrayList<Object> values = new ArrayList<Object>();
                while (res.next()) {
                    for (int i = 1; i < numberOfColumns; i++) {
                        values.add(res.getObject(i));
                    }
                    insertIntoRemoteDB(insert.getTargetColumnList(), values, into, sessionid);
                    values = new ArrayList<Object>();
                }
            } catch (SQLException e) {
                throw new DatabaseException(e.getMessage());
            }
        }
        return false;
    }

    //handles the delete routines
    private boolean handleDelete(DeleteNode delete, String sessionid) throws StandardException, DatabaseException {
        SelectNode select = (SelectNode) delete.getResultSetNode();
        if (select.getWhereClause() != null) {
            //Evaluate WhereClause to extract keys
            ArrayList<String> escapedColumns = new ArrayList<String>();
            IKeyVault keysToBeInserted = new QueryKeyVault();
            traverseTree(select.getWhereClause(), delete.getTargetTableName().getTableName(), escapedColumns, new HashSet<>(), false, keysToBeInserted, sessionid);
            if (keysToBeInserted.hasKeys(delete.getTargetTableName().getTableName())) {
                remoteDB.delete(delete.getTargetTableName().getTableName(), keysToBeInserted.getKeys(delete.getTargetTableName().getTableName()), escapedColumns, sessionid);
                System.out.println("Deleted Data from " + delete.getTargetTableName().getTableName() + " with the given Whereclause");
            } else {
                System.out.println("Nothing to delete from " + delete.getTargetTableName().getTableName() + " with the given Whereclause");
                return false;
            }
            return true;
        } else {
            remoteDB.deleteAll(delete.getTargetTableName().getTableName(), sessionid);
            return true;
        }
    }

    //handles the update routines
    private boolean handleUpdate(UpdateNode update, String sessionid) throws StandardException, DatabaseException {
        SelectNode select = (SelectNode) update.getResultSetNode();
        String table = update.getTargetTableName().getTableName();
        //Later used for console print!
        Map<String, Object> updateData = new TreeMap<String, Object>();
        for (ResultColumn column : update.getResultSetNode().getResultColumns()) {
            ConstantNode value = (ConstantNode) column.getExpression();
            updateData.put(column.getName(), value.getValue());
        }
        if (select.getWhereClause() != null) {
            //Evaluate WhereClause to extract keys
            ArrayList<String> escapedColumns = new ArrayList<String>();
            IKeyVault keysToBeInserted = new QueryKeyVault();

            traverseTree(select.getWhereClause(), table, escapedColumns, new HashSet<>(), false, keysToBeInserted, sessionid);

            if (!keysToBeInserted.hasKeys(table)) {
                logger.log(Level.INFO, "Nothing to update with this where clause");
                return false;
            }
            WhereClauseIn in = new WhereClauseIn(RemoteDBConstants.DATA_KEY, new ArrayList<Object>(keysToBeInserted.getKeys(table)));
            ArrayList<WhereClause> where = new ArrayList<WhereClause>();
            where.add(in);
            remoteDB.update(table, updateData, where, keysToBeInserted.getKeys(table), sessionid);
        } else {
            //Just take all keys from the specified table to update them
            List<String> keys = selectAllKeysFromRemoteDB(table, sessionid);
            WhereClauseIn in = new WhereClauseIn(RemoteDBConstants.DATA_KEY, new ArrayList<Object>(keys));
            ArrayList<WhereClause> where = new ArrayList<WhereClause>();
            where.add(in);
            remoteDB.update(table, updateData, where, keys, sessionid);
        }
        return true;
    }

    //Helper to avoid unnecessary if clauses
    private void insertIntoRemoteDB(ResultColumnList targetColumns, List<Object> values, String into, String sessionid) throws DatabaseException {
        if (targetColumns == null) {
            //If no Columns are specified try this!
            remoteDB.insert(into, values, sessionid);
        } else {
            List<String> columns = new ArrayList<String>();
            for (ResultColumn column : targetColumns) {
                columns.add(column.getName());
            }
            if (columns.size() < values.size()) {
                throw new DatabaseException("More Values than columns to be inserted into " + into + "!\n");
            }
            remoteDB.insert(into, columns, values, sessionid);
        }
    }

    //traverses SetOperations for select!
    private void traverseSelects(ResultSetNode node, IKeyVault keyVault, String sessionid) throws StandardException, DatabaseException {
        if (node.getNodeType() == NodeTypes.SELECT_NODE) {
            SelectNode select = (SelectNode) node;
            fetchKeysFor(select, keyVault, false, sessionid);
        } else { //Set Operations: Union, Intersect, Except
            SetOperatorNode set = (SetOperatorNode) node;
            traverseSelects(set.getLeftResultSet(), keyVault, sessionid);
            traverseSelects(set.getRightResultSet(), keyVault, sessionid);
        }
    }

    /**
     * Executes the select statement given by the parameter. This method also
     * handles joins that are placed into the select statement. This method also
     * handles sub select queries in the from statement of the select statement.
     * <p>
     * The {@link NodeTypes} that are handeled:
     * <ul>
     * <li>JOIN_NODE</li>
     * <li>FULL_OUTER_JOIN_NODE</li>
     * <li>HALF_OUTER_JOIN_NODE</li>
     * </ul>
     * <ul>
     * <li>FROM_SUBQUERY</li>
     * </ul>
     *
     * @param selectNode The select node for which the keys of the remote db
     * should be extracted.
     * @param keyVault The vault in which the keys of the remote db will be
     * written into
     * @param negated The default input param
     * @throws StandardException
     * @throws DatabaseException
     */
    private void fetchKeysFor(SelectNode selectNode, IKeyVault keyVault, boolean negated, String sessionid) throws StandardException, DatabaseException {
        for (FromTable item : selectNode.getFromList()) {

            // if inner join, right/left join or full join, both left and right tables (and their children) need to be saved in localDB
            if (isJoinNode(item)) {
                insertJoinChildrenInLocalDB(selectNode, item, keyVault, sessionid);
            } else if (item.getNodeType() == NodeTypes.FROM_SUBQUERY) {
                FromSubquery subquery = (FromSubquery) item;
                if (subquery.getSubquery().getNodeType() == NodeTypes.SELECT_NODE) {
                    fetchKeysFor((SelectNode) subquery.getSubquery(), keyVault, false, sessionid);
                }
            } else // only data with the keys that satisfy where-clause must be inserted into localDB
            {
                if (selectNode.getWhereClause() != null) {
                    ArrayList<String> escapedColumns = new ArrayList<String>();
                    traverseTree(selectNode.getWhereClause(), item.getTableName().getTableName(), escapedColumns, new HashSet<>(), negated, keyVault, sessionid);
                } else {
                    List<String> allKeysOfItem = selectAllKeysFromRemoteDB(item.getTableName().getTableName(), sessionid);
                    keyVault.addKeys(item.getTableName().getTableName(), allKeysOfItem);
                }
            }
        }
    }

    /**
     * Checks if this table is part of a join.
     *
     * @param fromTable
     * @return True if this is a join node. Otherwise false.
     */
    private boolean isJoinNode(final FromTable fromTable) {
        return (fromTable.getNodeType() == NodeTypes.JOIN_NODE
                || fromTable.getNodeType() == NodeTypes.FULL_OUTER_JOIN_NODE
                || fromTable.getNodeType() == NodeTypes.HALF_OUTER_JOIN_NODE);
    }

    // Inserts right and left children of the join node into localDB
    private void insertJoinChildrenInLocalDB(SelectNode selectNode, FromTable item, IKeyVault keyVault, String sessionid) throws StandardException, DatabaseException {

        String rightTableName = ((FromTable) ((JoinNode) item).getLogicalRightResultSet()).getTableName().getTableName();
        getKeysFromJoinWhereClause(selectNode, rightTableName, keyVault, sessionid);

        // if left child has children, insert them into localDB
        FromTable leftChildren = ((FromTable) ((JoinNode) item).getLogicalLeftResultSet());
        if (isJoinNode(leftChildren)) {
            insertJoinChildrenInLocalDB(selectNode, leftChildren, keyVault, sessionid);
        } else {

            String leftTableName = ((FromTable) ((JoinNode) item).getLogicalLeftResultSet()).getTableName().getTableName();
            getKeysFromJoinWhereClause(selectNode, leftTableName, keyVault, sessionid);
        }
    }

    private void getKeysFromJoinWhereClause(SelectNode selectNode, final String tableName, final IKeyVault keyVault, String sessionid) throws DatabaseException, StandardException {
        List<String> escapedColumnsRight = new ArrayList<>();
        if (selectNode.getWhereClause() != null) {
            ValueNode where = selectNode.getWhereClause();
            ArrayList<String> escapedColumnsRightWhere = new ArrayList<String>();
            traverseTree(where, tableName, escapedColumnsRightWhere, new HashSet<>(), false, keyVault, sessionid);

            if (!keyVault.hasKeys(tableName)) {
                keyVault.addKeys(tableName, selectAllKeysFromRemoteDB(tableName, sessionid));
            }
        } else {
            keyVault.addKeys(tableName, selectAllKeysFromRemoteDB(tableName, sessionid));
        }
    }

    //Traverses the Tree of Where-Clauses and extracts Keys. Can handle Whereclauses connect with and, or, In, Between, IS NULL, IS NOT NULL and standard whereclauses with =, <, >, !=
    private void traverseTree(ValueNode where, String from, ArrayList<String> escapedColumns, Set<String> keysToRetain, boolean negated, IKeyVault keyVault, String sessionid) throws StandardException, DatabaseException {
        switch (where.getNodeType()) {
            case (NodeTypes.OR_NODE):
                OrNode innerOr = (OrNode) where;
                IKeyVault keys_or_node_left = new QueryKeyVault();
                traverseTree(innerOr.getLeftOperand(), from, escapedColumns, keysToRetain, negated, keys_or_node_left, sessionid);

                IKeyVault keys_or_node_right = new QueryKeyVault();
                traverseTree(innerOr.getRightOperand(), from, escapedColumns, keysToRetain, negated, keys_or_node_right, sessionid);

                keyVault.addAllKeys(keys_or_node_left);
                keyVault.addAllKeys(keys_or_node_right);
                break;
            case (NodeTypes.AND_NODE):
                AndNode innerAnd = (AndNode) where;
                IKeyVault keys_left_inner_and = new QueryKeyVault();
                traverseTree(innerAnd.getLeftOperand(), from, escapedColumns, keysToRetain, negated, keys_left_inner_and, sessionid);

                //Only the intersection of the data shall be added
                if (null != keys_left_inner_and.getKeysToBeInserted().get(from)) {
                    keysToRetain.addAll(keys_left_inner_and.getKeysToBeInserted().get(from));
                }
                IKeyVault keys_right_inner_and = new QueryKeyVault();
                traverseTree(innerAnd.getRightOperand(), from, escapedColumns, keysToRetain, negated, keys_right_inner_and, sessionid);

                keys_left_inner_and.merge(keys_right_inner_and);
                keyVault.addAllKeys(keys_left_inner_and);
                break;
            case (NodeTypes.IN_LIST_OPERATOR_NODE):
                if (!shouldPerformWhere(from, where)) {
                    return;
                }
                //Select keys from Remote as this is a leaf of the tree
                InListOperatorNode list = (InListOperatorNode) where;

                List<Object> inList = new ArrayList<Object>();
                for (ValueNode data : list.getRightOperandList().getNodeList()) {
                    ConstantNode constant = (ConstantNode) data;
                    if (constant.getValue() != null && constant.getValue().equals("null")) { //'null' must be handled here otherwise it would be inserted in the remote and local DB as sql null
                        inList.add(ESCAPENULL);
                        escapedColumns.add(list.getLeftOperand().getNodeList().get(0).getColumnName()); //Remember the column for the insertion into the local db
                    } else {
                        inList.add(constant.getValue());
                    }
                }
                List<WhereClause> whereIn = new ArrayList<WhereClause>();
                if (negated) {
                    WhereClauseIn in = new WhereClauseNotIn(list.getLeftOperand().getNodeList().get(0).getColumnName(), inList);
                    whereIn.add(in);
                } else {
                    WhereClauseIn in = new WhereClauseIn(list.getLeftOperand().getNodeList().get(0).getColumnName(), inList);
                    whereIn.add(in);
                }
                keyVault.addKeys(from, selectKeysFromRemoteDB(from, keysToRetain, whereIn, false, sessionid));
                break;
            case (NodeTypes.IS_NULL_NODE):
                //Select keys from Remote as this is a leaf of the tree
                IsNullNode unary = (IsNullNode) where;
                List<String> keys_is_null_node = new ArrayList<String>();
                List<WhereClause> whereIsNull = new ArrayList<WhereClause>();
                if (negated) {
                    whereIsNull.add(new WhereClauseBinary(unary.getOperand().getColumnName(), "", RemoteDBConstants.ISNOTNULL)); //Set Operater to "" to make it a unary statement
                    negated = false;
                } else {
                    whereIsNull.add(new WhereClauseBinary(unary.getOperand().getColumnName(), "", RemoteDBConstants.ISNULL)); //Set Operater to "" to make it a unary statement
                }
                keyVault.addKeys(from, selectKeysFromRemoteDB(from, keysToRetain, whereIsNull, false, sessionid));
                break;
            case (NodeTypes.NOT_NODE):
                //Traverse further to the other nodes
                NotNode notNode = (NotNode) where;
                IKeyVault keys_not_node = new QueryKeyVault();
                traverseTree(notNode.getOperand(), from, escapedColumns, keysToRetain, true, keys_not_node, sessionid);
                keyVault.addAllKeys(keys_not_node);
                break;
            case (NodeTypes.BETWEEN_OPERATOR_NODE): //Map between to two where clauses with < and >
                if (!shouldPerformWhere(from, where)) {
                    return;
                }

                BetweenOperatorNode between = (BetweenOperatorNode) where;
                List<String> keys_between_node = new ArrayList<String>();
                String column = between.getLeftOperand().getColumnName();
                ValueNodeList range = between.getRightOperandList();
                ConstantNode greater = (ConstantNode) range.get(0); //Between means two values inside the list not more or less
                ConstantNode less = (ConstantNode) range.get(1);
                List<WhereClause> whereBet = new ArrayList<WhereClause>();
                if (negated) {
                    whereBet.add(new WhereClauseBinary(column, greater.getValue(), "<="));
                    whereBet.add(new WhereClauseBinary(column, less.getValue(), ">="));
                    keys_between_node.addAll(selectKeysFromRemoteDB(from, keysToRetain, whereBet, true, sessionid));
                } else {
                    whereBet.add(new WhereClauseBinary(column, greater.getValue(), ">="));
                    whereBet.add(new WhereClauseBinary(column, less.getValue(), "<="));
                    keys_between_node.addAll(selectKeysFromRemoteDB(from, keysToRetain, whereBet, false, sessionid));
                }
                keyVault.addKeys(from, keys_between_node);
                break;
            /**
             * If the where statement is as follows: .... WEHRE ... [NOT] EXISTS
             * ... the ValueNode #where is typed as {@link SubqueryNode} If the
             * where statement is as follows: .... WEHRE ... IN ... the
             * ValueNode #where is typed as {@link SubqueryNode} If the where
             * statement is as follows: .... WHERE field = (SUBQUERY) .... the
             * ValueNode #where is typed as {@link BinaryOperatorNode} and is
             * handled in the default section
             */
            case (NodeTypes.SUBQUERY_NODE):

                SubqueryNode whereSubquery = (SubqueryNode) where;
                if (whereSubquery.getSubqueryType() == SubqueryNode.SubqueryType.IN
                        || whereSubquery.getSubqueryType() == SubqueryNode.SubqueryType.NOT_IN) {

                    /**
                     * Cross-Product-Case: Do we try to create the IN-List for a
                     * column that does not exist in the from table of the
                     * current where statement. Since the same where-statement
                     * is applied to all from tables in the select statement,
                     * this might be the case. If this is the case, then we
                     * return early since there is nothing to compute.
                     */
                    if (!tableContainsColumn(from, whereSubquery.getLeftOperand().getColumnName())) {
                        return;
                    }

                    /**
                     * Now we drill down recursively for further Subselects in
                     * this IN-Statement: We use our own key-vault here for if
                     * we have a further IN-Statement nested then we have to
                     * turn the keys that are contained by this into constant
                     * values so that the "outer" IN-Statement can be performed
                     */
                    IKeyVault subqueryVault = new QueryKeyVault();
                    fetchWhereSubquery(whereSubquery, subqueryVault, negated, sessionid);

                    /**
                     * Now we are drilled down fully an know that this is the
                     * last IN-Subquery-Statement. Now we begin to turn the keys
                     * into constant values by creating a new
                     * {@link InListOperatorNode} to fetch the keys for that.
                     */
                    RowConstructorNode nodeRight = new RowConstructorNode();
                    ValueNodeList leftHandValues = new ValueNodeList();
                    ColumnReference leftRef = new ColumnReference();
                    TableName tableName = new TableName();
                    tableName.init(null, from);
                    leftRef.init(whereSubquery.getLeftOperand().getColumnName(), tableName);
                    leftHandValues.addValueNode(leftRef);

                    RowConstructorNode nodeLeft = new RowConstructorNode();
                    ValueNodeList rightHandValues = new ValueNodeList();

                    SelectNode subSelect = (SelectNode) whereSubquery.getResultSet();
                    String subqueryFrom = subSelect.getFromList().get(0).getTableName().getTableName();
                    String subqueryColumn = subSelect.getResultColumns().get(0).getName();

                    int colIndex = getColumnIndex(subqueryFrom, subqueryColumn);
                    for (List<String> dataList : remoteDB.selectData(subqueryVault.getKeysToBeInserted().get(subqueryFrom), sessionid)) {
                        ConstantNode node = new CharConstantNode();
                        node.setValue(dataList.get(colIndex));
                        node.setNodeType(NodeTypes.CHAR_CONSTANT_NODE);
                        rightHandValues.addValueNode(node);
                    }

                    nodeLeft.init(leftHandValues, new int[]{0});
                    nodeRight.init(rightHandValues, new int[]{0});

                    InListOperatorNode inListOp = new InListOperatorNode();
                    inListOp.init(nodeLeft, nodeRight);
                    inListOp.setNodeType(NodeTypes.IN_LIST_OPERATOR_NODE);

                    keyVault.addAllKeys(subqueryVault);
                    traverseTree(inListOp, from, escapedColumns, keysToRetain, negated, keyVault, sessionid);
                } else if (whereSubquery.getSubqueryType() == SubqueryNode.SubqueryType.EXISTS) {
                    fetchWhereSubquery(whereSubquery, keyVault, negated, sessionid);
                }
                break;
            default:
                if (!shouldPerformWhere(from, where)) {
                    return;
                }

                //Select keys from Remote as this is a leaf of the tree
                String columnName = "";
                String operator = "";
                if (negated) {
                    operator = "NOT";
                }
                ValueNode right = null;
                if (where instanceof BinaryRelationalOperatorNode) {
                    BinaryRelationalOperatorNode bin = (BinaryRelationalOperatorNode) where;
                    columnName = bin.getLeftOperand().getColumnName();
                    operator += bin.getOperator();
                    right = bin.getRightOperand();
                } else if (where instanceof LikeEscapeOperatorNode) { //for like operators
                    LikeEscapeOperatorNode lik = (LikeEscapeOperatorNode) where;
                    columnName = lik.getReceiver().getColumnName();
                    operator += lik.getOperator();
                    right = lik.getLeftOperand(); // names of parameters in foundationDB are strange, but in case "like name = 'k%'" name is receiver and 'k%' is leftOperand
                } else {
                    TernaryOperatorNode ten = (TernaryOperatorNode) where;
                    columnName = ten.getLeftOperand().getColumnName();
                    operator += ten.getOperator();
                    right = ten.getRightOperand();
                }

                List<String> keys7 = new ArrayList<String>();
                Object value = null;
                if (right instanceof ConstantNode) {
                    ConstantNode rightNew = (ConstantNode) right;
                    value = rightNew.getValue();
                    if (value != null && value.equals("null")) { //'null' must be handled here otherwise it would be inserted in the remote and local DB als sql null
                        value = ESCAPENULL;
                        escapedColumns.add(columnName); // remember the column for the insertion into the local db
                    }
                    List<WhereClause> whereBin = new ArrayList<WhereClause>();
                    whereBin.add(new WhereClauseBinary(columnName, value, operator));
                    keys7.addAll(selectKeysFromRemoteDB(from, keysToRetain, whereBin, false, sessionid));
                } /**
                 * If there is a cross product like: SELECT * from students, uni
                 * WHERE students.fk_uni = uni.id
                 *
                 * the right hand operator is of type ColumnReference.
                 *
                 * We have to find out if there is a chance to not fetch ALL
                 * entries from both tables of left and right hand operator to
                 * process this WHERE clause
                 *
                 */
                else if (right instanceof ColumnReference) {
                    ColumnReference ref = (ColumnReference) right;
                    /**
                     * The table name of the column reference is only set if it
                     * is defined explicitly in the sql statement (uni.id vs id;
                     * for the latter the table name is null)
                     */
                    if (ref.getTableName() != null && !ref.getTableName().isEmpty()) {
                        keyVault.addKeys(ref.getTableName(), selectAllKeysFromRemoteDB(ref.getTableName(), sessionid));
                        keyVault.addKeys(from, selectAllKeysFromRemoteDB(from, sessionid));
                        return;
                    } else {
                        //TODO We could make a lookup to which table this specific right hand operator belongs.
                        //TODO But what if there are two different tables that have identical attributes (like ids)
                        //TODO Then we can't tell which keys to fetch here!
                    }
                } else if (right.getNodeType() == NodeTypes.SUBQUERY_NODE) {
                    fetchWhereSubquery((SubqueryNode) right, keyVault, negated, sessionid);
                } else { //Could also be a Table in join like selects then select complete table!
                    keys7.addAll(selectAllKeysFromRemoteDB(from, sessionid));
                }
                keyVault.addKeys(from, keys7);
        }
    }

    /**
     * This method checks whether to apply the where statement to this column.
     * There are at least two cases where it does not makes sense: 1) The from
     * table does not contain a column with the column name 2) The from table
     * does contain the column by accident (e.g. the table that is actually
     * queried by the where clause is a different one than specified by from but
     * their column names match
     * <p>
     * This method assumes that it is called when you are in the leave of the
     * traversed tree. It makes a lookup for the column and table name of the
     * left operand of the queriedElement. If the queriedElement itself is the
     * root node for further leaves, the left and right operands are not given.
     *
     * @param from The table for which the entire where statement should be
     * applied
     * @param queriedElement The element of the where clause that shall be
     * checked if the application of the where clause makes sense.
     * @return True if the application of the where clause makes sense.
     * Otherwise false. False is also returned if the queried Element is not a
     * leave node.
     */
    private boolean shouldPerformWhere(String from, ValueNode queriedElement) {
        OperandHelper op = new OperandHelper();
        op.loadFrom(queriedElement);

        final boolean fromTableHasColumn = tableContainsColumn(from, op.getLeftOperand());
        final boolean fromTableTargetOfWhereClause = from.equals(op.getTableOfLeftOperand());

        return true;
        /**
         * The table of the operand is only set here if it is set explicitly in
         * the original sql query (e.g. tablename.columname instead of just the
         * column name) If this is set properly we can distinguish also
         * identical columns among tables.
         */
        //if(op.getTableOfLeftOperand() != null && !op.getTableOfLeftOperand().isEmpty()){
        //	return fromTableHasColumn && fromTableTargetOfWhereClause;
        //}
        /**
         * If the query is not set up, just return if the column is contained by
         * the table
         */
        //return fromTableHasColumn;
    }

    /**
     * Select the keys from the remote data base. This method also deals with
     * the problem that there may be columns in the where statement that are not
     * part of the columns in the actual from table. In this case all keys of
     * the from table are returned. If a table is specified in from that does
     * not exist an empty list is returned.
     *
     * @param from the from table
     * @param keysToRetain a list of keys which shall be matched (intersected)
     * to the keys selected by the whereclauses in whereBet
     * @param whereBet the where clause
     * @param or If true, all where clauses in whereBet are connected with an
     * OR. Otherwise they will be connected by an AND.
     * @return The keys
     * @throws DatabaseException
     */
    private List<String> selectKeysFromRemoteDB(String from, Set<String> keysToRetain, List<WhereClause> whereBet, boolean or, String sessionid) {

        /*TODO: Instead of waiting for an exception to occur we might
         check earlier in the code whether the colums that are fetched by the where clauses are contained
		 in the from table.
		 It would make it clearer that the problem (e.g. columns that do not exist in the from table) tried to be fetched
		 is solved by adding all keys of the from table to ensure that the query result on the client is at least complete.
		 But we know that adding all keys means adding all rows of that table and can lead to a huge overhead.
         */
        try {
            List<String> keysToRetainList = new ArrayList<>(keysToRetain);
            return remoteDB.selectKeys(from, whereBet, keysToRetainList, or, sessionid);
        } catch (DatabaseException e) {
            String msg = e.getMessage();
            msg += "*** WHERE statement might be applied to this column/table ***\n";
            logger.log(Level.WARNING, msg);
        }

        return selectAllKeysFromRemoteDB(from, sessionid);
    }

    /**
     * Get all keys of the from table.
     *
     * @param from the from table
     * @return Returns all keys of the specified from table. If this table does
     * not exist an empty list is returned.
     */
    private List<String> selectAllKeysFromRemoteDB(String from, String sessionid) {
        try {
            logger.log(Level.WARNING, "\n***Adding all keys of table '" + from + "' to the database ***\n");
            return remoteDB.selectAllKeys(from, sessionid);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Check if the column is contained by the table.
     *
     * @param tableName The table
     * @param columnName The column
     * @return True if the table contains the column. Otherwise false. Note that
     * the search is case sensitive!
     */
    private boolean tableContainsColumn(final String tableName, final String columnName) {

        if (null == localDB.getColumns(tableName)) {
            logger.log(Level.WARNING, "The local data base does not have any columns for the table '" + tableName + "'");
        }
        for (Column col : localDB.getColumns(tableName)) {
            if (col.getName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the index of the column in the table.
     *
     * @param tableName The table
     * @param columnName The column
     * @return The column index. If the column is not contained in the table an
     * exception is thrown.
     */
    private int getColumnIndex(final String tableName, final String columnName) throws StandardException {
        int colIndex = 0;
        for (Column col : localDB.getColumns(tableName)) {
            if (col.getName().equals(columnName)) {
                break;
            }
            colIndex++;
        }

        if (colIndex > localDB.getColumns(tableName).size()) {
            throw new StandardException("Invalid column index '" + colIndex + "' for column '" + columnName + "' in table '" + tableName + "'. The column might not be contained in this table?");
        }
        return colIndex;
    }

    /**
     * If a where clause contains a subquery then this method will add the
     * result of that subquery to the keyVault In fact if there are further
     * where clauses that also contain subqueries the method is called
     * recursively
     * <p>
     * {@link NodeTypes} EXPRESSION, EXISTS, NOT_EXISTS
     *
     * @param subquery
     * @param keyVault
     * @param negated
     * @throws DatabaseException
     * @throws StandardException
     */
    private void fetchWhereSubquery(final SubqueryNode subquery, IKeyVault keyVault, boolean negated, String sessionid) throws DatabaseException, StandardException {
        ResultSetNode resultSet = subquery.getResultSet();
        if (subquery.getSubqueryType() == SubqueryNode.SubqueryType.EXPRESSION
                || subquery.getSubqueryType() == SubqueryNode.SubqueryType.EXISTS
                || subquery.getSubqueryType() == SubqueryNode.SubqueryType.NOT_EXISTS
                || subquery.getSubqueryType() == SubqueryNode.SubqueryType.IN
                || subquery.getSubqueryType() == SubqueryNode.SubqueryType.NOT_IN) {
            if (resultSet.getNodeType() == NodeTypes.SELECT_NODE) {
                SelectNode selectNode = (SelectNode) resultSet;
                fetchKeysFor(selectNode, keyVault, negated, sessionid);
            }
        }
    }

    /**
     * Inserts the tuples which are specified in the file into the relation. The
     * attributes of each tuple must be separated by the delimiter and the
     * number of attributes per tuple must be consistent with the number of
     * attributes specified by the relation scheme.
     *
     * @param relation Relation in which the data should be inserted
     * @param filename fully qualified name of the file in which the data is
     * stored.
     * @param delimiter delimiter between the attribute values
     * @throws DatabaseException if an error occurred while trying to execute
     * the query
     * @throws IOException if the file could not be processed
     */
    public void insertFromFile(String relation, String filename, char delimiter, String sessionid)
            throws IOException, DatabaseException {
        remoteDB.insertFromFile(relation, filename, delimiter, sessionid);
    }

    // Inserts the values in the local table "into", EscapedColumns contains the columns for which "null" shall be escaped
    private void insertIntoLocalDB(String into,
            List<List<String>> values, List<String> escapedColumns, String sessionid) throws DatabaseException {
        localDB.setAutoEscaping(true);
        List<Column> col = remoteDB.getPlaineDBScheme().get(into);
        if (col == null) {
            throw new DatabaseException("Table " + into + " Does not exist in Database!\n");
        }
        if (!escapedColumns.isEmpty()) {
            for (List<String> val : values) {
                for (int i = 0; i < val.size(); i++) {
                    if (val.get(i) != null && val.get(i).equals("null")) {
                        Iterator<String> iter = escapedColumns.iterator();
                        while (iter.hasNext()) {
                            if (iter.next().equals(col.get(i).getName())) {
                                val.set(i, ESCAPENULL);
                            }
                        }
                    }
                }
            }
        }
        localDB.insertBatch(into, col, values, sessionid);
    }

}
