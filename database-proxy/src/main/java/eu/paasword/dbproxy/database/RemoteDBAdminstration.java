/**
 *
 */
package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.data.DataAdministration;
import eu.paasword.dbproxy.database.data.DataStandardAdmininistration;
import eu.paasword.dbproxy.database.index.*;
import eu.paasword.dbproxy.database.utils.*;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.exceptions.PluginLoadFailure;
import eu.paasword.dbproxy.utils.ConfigParser;
import eu.paasword.dbproxy.utils.DatabaseLoader;
import eu.paasword.dbproxy.utils.DistributedTablesConfiguration;
import eu.paasword.dbproxy.utils.IDistributedServer;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Coordinates the encryption and decryption of data for the statement or their results and performs the mapping between
 * the user view on the database and the real database situation. It also converts the representation of the data in the
 * database to a uniform format. This class is only a facade which controls the over classes which are responsible for the details.
 *
 * @author Yvonne Muelle
 */
public class RemoteDBAdminstration implements RemoteDatabase {
    // maps the relation to a concrete database where it can be found. The relations are the remote relations and not
    // the local ones.
    // relation: dbname.table
    private Map<String, Database> relationToDB;
    // maps the information in which database the index for the column is located.
    // column: localTable.columnName
    // index: localtable_column
    private Map<String, Database> columnToIndex;
    // mapping between the name of the local columns and the remote columns
    // remote: dbname.table
    private Database localDB;
    private CSVProcessor csvprocessor;
    private Logger logger = Logger.getLogger(RemoteDBAdminstration.class.getName());
    private TableMapper tableMapper;

    private RemoteDBHelper helper;
    private String remoteDBName;

    private IndexAdministration indexAdmin;
    private DataAdministration dataAdmin;
    private TableManagement tablemanager;

    /**
     * Constructs an object of this class // XML2YAML
     *
     * @param encrypt Intance of the encryption method.
     * @throws PluginLoadFailure if a database class could not be loaded.
     * @throws DatabaseException
     */
    public RemoteDBAdminstration(Encryption encrypt,String adapterid, String sessionid) throws PluginLoadFailure, DatabaseException {
        if (encrypt == null) {
            throw new IllegalArgumentException("Encryption can not be null");
        }
        tableMapper = TableMapper.getInstance();

        // Initialization
        relationToDB = new TreeMap<String, Database>();
        columnToIndex = new TreeMap<String, Database>();

        // create local database
        // create remote databases
        // local database
        Map<String, String> localDbConf = ConfigParser.getInstance(adapterid).getLocalDatabase();


        localDB = DatabaseLoader.loadDatabase(localDbConf);
        List<Map<String, String>> remoteDbs = ConfigParser.getInstance(adapterid).getRemoteDatabases();
        List<Map<String, String>> remoteIndexDatabases = ConfigParser.getInstance(adapterid).getRemoteIndexDatabases();

        for (Map<String, String> rdb : remoteDbs) {
            if (rdb.get("type").equalsIgnoreCase(DatabaseTypes.REMOTE.name())) {
                remoteDBName = rdb.get("name");
            }
        }
        //Initialize helper now
        helper = new RemoteDBHelper(this, remoteDBName, encrypt, relationToDB);
        for (Map<String, String> rdb : remoteDbs) {
            // gets all tablenames from remoteDB
            Database db = DatabaseLoader.loadDatabase(rdb);
            String dbName = rdb.get("name");
            Set<String> relations = db.getRelationNames();
            for (String rel : relations) {
                relationToDB.put(helper.getMappingScheme(dbName, rel), db);
            }
        }
        for (Map<String, String> rdb : remoteIndexDatabases) {
            Database db = DatabaseLoader.loadDatabase(rdb);
            String dbName = rdb.get("name");
            Set<String> relations = db.getRelationNames();
            for (String rel : relations) {
                relationToDB.put(helper.getMappingScheme(dbName, rel), db);
            }
        }
        try {
            helper.refreshMappings(sessionid);
        } catch (DatabaseException e) {
            String msg = "Error while getting initial database access!";
            logger.log(Level.INFO, msg);
        }
        //Set DBName in helper

        // fill indexDB
        Set<String> relNames = localDB.getRelationNames();
        Set<String> indexNames = new TreeSet<String>();
        // Which index tables must exist
        for (String rel : relNames) {
            List<Column> cols = localDB.getColumns(rel);

            for (Column col : cols) {
                indexNames.add(helper.getIndexName(rel, col.getName()));
            }
        }

        Collection<Database> tmp = relationToDB.values();
        Set<Database> dbs = new HashSet<Database>(tmp);

        for (Database d : dbs) {
            Set<String> dbRels = new TreeSet<String>(d.getRelationNames());
            // Index tables in this database d
            dbRels.retainAll(indexNames);

            for (String name : dbRels) {
                String colName = helper.fromIndexNameToMappingScheme(name);
                columnToIndex.put(colName, d);
            }
        }

        Map<String, String> indexConf = ConfigParser.getInstance(adapterid).getIndexConfig();
        String indexType = indexConf.get("indextype");
        if (indexType.equals("standard")) {
            indexAdmin = new IndexStandardAdministration(relationToDB, helper, encrypt, remoteDBName);
        } else if (indexType.equals("manualDistribution")) {
            indexAdmin = new IndexManualAdministration(relationToDB, helper, encrypt, remoteDBName);
        } else if (indexType.equals("hash")) {
            indexAdmin = new IndexHashedAdministration(relationToDB, helper, remoteDBName, indexConf.get("indexhashkey").getBytes());
        } else if (indexType.equals("bucket")) {
            String size = indexConf.get("bucketsize");
            indexAdmin = new IndexBucketAdministration(relationToDB, helper, remoteDBName, Integer.valueOf(size), encrypt);
        } else if (indexType.equals("encrypted")) {
            indexAdmin = new IndexEncryptedAdministration(relationToDB, helper, encrypt, remoteDBName);
        } else {
            throw new DatabaseException("No valid Indexconfiguration was found!\n");
        }
        dataAdmin = new DataStandardAdmininistration(relationToDB, helper, encrypt, remoteDBName);
        tablemanager = new TableManagement(this, remoteDBName, relationToDB);
    }

	/*
     * (non-Javadoc)
	 * 
	 * @see prototype.database.RemoteDatabase#delete()
	 */

    /**
     * @param table takes real table-name in database NOT table stored in adapter-system!
     */
    @Override
    public void delete(String table, List<String> allkeys, List<String> escapedColumns,String sessionid) throws DatabaseException {
        //Delete from Index
        indexAdmin.deleteAttributesFromIndex(table, allkeys, escapedColumns, dataAdmin.selectData(allkeys,sessionid),sessionid);
        for (Database db : relationToDB.values()) {
            try {
                db.executeUpdateBatch(sessionid);
            } catch (SQLException e) {
                throw new DatabaseException(e.getMessage());
            }
        }
        // Delete encrypted data entry / entries.
        WhereClauseIn in = new WhereClauseIn(RemoteDBConstants.DATA_KEY, new ArrayList<Object>(allkeys));
        ArrayList<WhereClause> where = new ArrayList<WhereClause>();
        where.add(in);
        String remoteTable = remoteDBName + "." + (RemoteDBConstants.DATA_TABLE);
        Database remoteDB = relationToDB.get(remoteTable);
        remoteDB.delete(RemoteDBConstants.DATA_TABLE, where,sessionid);

    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#deleteAll(String)
     *
     */
    @Override
    public void deleteAll(String table,String sessionid) throws DatabaseException {
        int tableID = 0;
        if (tableMapper.containsKey(table)) {
            tableID = tableMapper.getRemoteTableID(table);
        } else {
            throw new DatabaseException("Could not find Table " + table + " in Database! \n");
        }
        //Delete from Index and return deleted Indices
        List<String> dataIndicies = indexAdmin.deleteAllFromIndex(tableID,sessionid);
        // Delete all relevant entries from data
        dataAdmin.deleteAllFromData(dataIndicies,sessionid);
    }

    /*
    * (non-Javadoc)
    *
    * @see prototype.database.RemoteDatabase#insert()
    * no collums in insert-query provided so values are inserted as given
    */
    @Override
    public void insert(String into, List<Object> values, String sessionid) throws DatabaseException {
        // Get columns, so that this method can be delegated to the other insert function as this function is a special
        // case of the other insert function.
        List<Column> columns = localDB.getColumns(into);

        List<String> colInserted = new ArrayList<String>();

        for (Column c : columns) {
            colInserted.add(c.getName());
        }

        insert(into, colInserted, values,sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#insert(org.gibello.zql.ZFromItem, java.util.List, java.util.List)
     *
     * This method should only be used if columns contains all columns of "into" because of the encryption. Later, it is
     * not possible to get the information, which columns where only inserted...
     */
    @Override
    public void insert(String into, List<String> columns, List<Object> values,String sessionid) throws DatabaseException {
        // 0. find Data Table
        //Check Whether Table and all Columns exist!
        if (!tableMapper.containsKey(into)) {
            throw new DatabaseException("Could not find Table " + into + " in Database! \n");
        }
        HashMap<String, Column> cols = tableMapper.getRemoteColumns(tableMapper.getRemoteTableID(into));


        for (String column : columns) {
            if (!cols.containsKey(column)) {
                throw new DatabaseException("Could not find Column " + column + " in " + into + " in Database! \n");
            }
        }
        //String remoteTable = localToRemote.get(into.getTable());
        String remoteTable = remoteDBName + "." + RemoteDBConstants.DATA_TABLE;
        Database remoteDB = relationToDB.get(remoteTable);
        // 1. get MaxID from DataTable
        // NOTE: this does not work concurrent
        // select max(ID) from remoteTable
        int maxID = helper.selectMaxIDPlusOne(remoteTable, remoteDB, RemoteDBConstants.DATA_PRIMKEY,sessionid);
        //Order Columns and Values in correct order to insert null values into the data and index table correctly!
        List<String> correctOrderColumns = new ArrayList<String>();
        List<Object> correctOrderValues = new ArrayList<Object>();
        ArrayList<Column> columnList = new ArrayList<Column>();
        HashMap<Column, Object> primKey = new HashMap<Column, Object>();
        columnList.addAll(cols.values());
        Collections.sort(columnList);
        for (int i = 0; i < columnList.size(); i++) {
            correctOrderColumns.add(columnList.get(i).getName());
            int position = columns.indexOf(columnList.get(i).getName()); //column names are unique for a table so this is possible
            if (position == -1) {
                correctOrderValues.add(null);
            } else {
                Column col = columnList.get(position);
                if (col.isCharType() && values.get(position) != null) { //Check the length of the input
                    String val = values.get(position).toString();
                    if (col.getLength() < val.length()) {
                        if (col.getLength() == -2) { //unrestricted String
                            values.set(position, val);
                        } else {
                            values.set(position, val.substring(0, col.getLength()));
                        }
                    }
                }
                correctOrderValues.add(values.get(position));
            }
            //Check Column constraints not_null and unqiue
            Column currentColumn = columnList.get(i);
            tablemanager.checkConstraints(columnList.get(i), correctOrderValues.get(i), into,sessionid);
            //Add for primarykey check if necessary
            if (currentColumn.isPrimary_key()) {
                primKey.put(currentColumn, correctOrderValues.get(i));
            }
        }
        //Check Primary Key if is unique
        if (!primKey.isEmpty()) {
            tablemanager.checkPrimaryKey(primKey, into,sessionid);
        }
        // 2. Insert data
        dataAdmin.insertOnlyData(correctOrderColumns, correctOrderValues, maxID, remoteDB, remoteTable,sessionid);
        // 3. Indextable:
        // Null values must also be inserted, so that delete statements can be correctly performed.
        //for each column to insert and also for not specified columns insert null here
        for (int i = 0; i < correctOrderColumns.size(); i++) {
            indexAdmin.insertIndexValue(into, maxID, correctOrderColumns.get(i), correctOrderValues.get(i),sessionid);
        }
        for (Database db : relationToDB.values()) {
            try {
                db.executeInsertBatch(sessionid);
                db.executeUpdateBatch(sessionid);
            } catch (SQLException e) {
                throw new DatabaseException(e.getMessage() + e.getNextException().getMessage());
            }
        }
    }

    /*
     * (non-Javadoc)
     * This method is called for every table individually.
     *
     * @see prototype.database.RemoteDatabase#selectAllData(org.gibello.zql.ZFromItem)
     */
    @Override
    public List<List<String>> selectAllData(String from,String sessionid) throws DatabaseException {
        List<String> allKeys = selectAllKeys(from,sessionid);
        return dataAdmin.selectData(allKeys,sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#selectAllKeys(org.gibello.zql.ZFromItem)
     */
    @Override
    public List<String> selectAllKeys(String from, String sessionid) throws DatabaseException {
        // For each column, all entries of the index tables must be selected.
        String tableName = from;
        int tableID = 0;
        if (tableMapper.containsKey(tableName)) {
            tableID = tableMapper.getRemoteTableID(tableName);
        } else {
            throw new DatabaseException("Could not find Table " + tableName + " in Database! \n");
        }
        return indexAdmin.selectAllKeys(tableID,sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#selectData(org.gibello.zql.ZFromItem, java.util.Map, java.lang.String)
     */
    @Override
    public List<List<String>> selectData(List<String> keys,String sessionid)
            throws DatabaseException {
        return dataAdmin.selectData(keys,sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#selectKeys(org.gibello.zql.ZFromItem, java.util.Map, java.lang.String)
     */
    @Override
    public List<String> selectKeys(String from, List<WhereClause> where, List<String> keysToRetain, boolean or,String sessionid)
            throws DatabaseException {
        return indexAdmin.selectKeys(from, where, keysToRetain, or, sessionid);
    }


    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#setAutoEscaping(boolean)
     */
    @Override
    public void setAutoEscaping(boolean on) {
        Collection<Database> databases = relationToDB.values();

        for (Database db : databases) {
            db.setAutoEscaping(on);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#update()
     */
    @Override
    public void update(String table, Map<String, Object> updateData, List<WhereClause> where, List<String> allkeys, String sessionid) throws DatabaseException {

        Set<Entry<String, Object>> entries = updateData.entrySet();
        int tableid = tableMapper.getRemoteTableID(table);
        HashMap<String, Column> columns = tableMapper.getRemoteColumns(tableid);
        //Check the impact of this update on the primary keys
        Map<Column, Integer> primaryKeys = tableMapper.getPrimaryKeys(table);
        if (!primaryKeys.isEmpty()) {
            HashMap<Column, Object> affected = new HashMap<Column, Object>();
            for (String key : updateData.keySet()) {
                Column currentCol = columns.get(key);
                if (currentCol.isPrimary_key()) {
                    affected.put(currentCol, updateData.get(key));
                }
            }
            //Check if update could affect primary key
            if (!affected.isEmpty()) {
                if (primaryKeys.size() > 1) {
                    //If so, check whether the update would introduce duplicates of the PK
                    for (Column affcol : affected.keySet()) {
                        primaryKeys.remove(affcol);
                    }
                    if (allkeys.size() > 1) {
                        List<List<String>> data = selectData(allkeys,sessionid);
                        //Only used to check whether the combination of columns would exist twice
                        HashSet<String> testset = new HashSet<String>();
                        boolean unique = true;
                        for (List<String> row : data) {
                            //Concatanete rest of primary key and check with a Hashset if it would exist twice
                            String primkey = "";
                            for (Integer i : primaryKeys.values()) {
                                primkey += row.get(i);
                            }
                            unique = unique & testset.add(primkey);
                            if (!unique) {
                                throw new DatabaseException("This updated would cause duplicate Primary Keys in table " + table + "!\n");
                            }
                        }
                    } else {
                        tablemanager.checkPrimaryKey(affected, table,sessionid);
                    }
                } else {
                    //check if update value already exists
                    tablemanager.checkPrimaryKey(affected, table,sessionid);
                    //check whether this update would affect more than one row
                    if (allkeys.size() > 1) {
                        throw new DatabaseException("This updated would cause duplicate Primary Keys in table " + table + "!\n");
                    }
                }

            }
        }
        for (Entry<String, Object> entry : entries) {
            // 1 get Data
            //Adjust length of entries
            Column currentCol = columns.get(entry.getKey());
            if (currentCol.isCharType() && entry.getValue() != null) {
                String currentValue = entry.getValue().toString();
                if (currentCol.getLength() > -1 && currentCol.getLength() < currentValue.length()) {
                    entry.setValue(currentValue.substring(0, currentCol.getLength()));
                }
            }
            if (currentCol.isUnique() && allkeys.size() > 1) {
                throw new DatabaseException("Tried to update a unique column (" + currentCol.getName() + ") on several rows to the same value(" + entry.getValue() + ")!\n");
            }
            tablemanager.checkConstraints(currentCol, entry.getValue(), table, sessionid);
            String attribute = entry.getKey();
            String newAttrValue = null;
            if (entry.getValue() != null) {
                newAttrValue = entry.getValue().toString();
            }
            String datatype = tableMapper.getColumnDataType(table, entry.getKey()).toString().toLowerCase();
            // 2 update Index Tables and add to new Index entry
            // 2.1 delete from old Index entries
            indexAdmin.updateIndexValues(allkeys, entry, datatype, table,sessionid);
            for (Database db : relationToDB.values()) {
                try {
                    db.executeInsertBatch(sessionid);
                    db.executeUpdateBatch(sessionid);
                } catch (SQLException e) {
                    throw new DatabaseException(e.getMessage() + e.getNextException().getMessage());
                }
            }
            // 3. update data rows
            //remoteDB.delete(fromData, where, "=");
            try {
                dataAdmin.updateDataValue(table, where, attribute, newAttrValue,sessionid);
            } catch (DatabaseException e) {
                String msg = "Could not select requested data from Database! \n";
                logger.log(Level.INFO, msg, e);
                e.printStackTrace();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#updateAll(org.gibello.zql.ZFromItem, java.util.Map)
     */
    @Override
    public void updateAll(String table, Map<String, Object> updateData,String sessionid) {
        throw new UnsupportedOperationException("Updates are not supported as single function.");
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#insertFromFile(String, String, char)
     */
    @Override
    public void insertFromFile(String relation, String filename, char delimiter,String sessionid) throws IOException, DatabaseException {
        csvprocessor = new CSVProcessor(filename, this);
        Map<String, List<Column>> scheme = tableMapper.getPlaineDBScheme();
        List<Column> cols = scheme.get(relation);
        csvprocessor.processCSV(cols, relation, delimiter,sessionid);
    }


    /**
     * builds a DBScheme of remoteTable for building a new localDB Structure
     */
    public Map<String, List<Column>> getPlaineDBScheme() {
        return tableMapper.getPlaineDBScheme();
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#dropTable(STring, boolean)
     */
    @Override
    public void dropTable(String tableName, boolean ifexists,String sessionid) throws DatabaseException {
        String remoteTable = remoteDBName + "." + RemoteDBConstants.TABLE_NAMES;
        Database remoteDB = relationToDB.get(remoteTable);
        if (tableMapper.containsKey(tableName)) {
            int tableID = tableMapper.getRemoteTableID(tableName);
            //Delete Data of table first
            //TODO Check if the code below is neccessary at all with the new distribution since deleteAll will call indexManualAdmin.deleteAll(tableName) which deletes every index entry related to that table.
            deleteAll(tableName, sessionid);
            //Delete unnecessary entries in the server mapping table
            if (isIndexManuallyDistributed()) {
                //TODO Implement deletion for distribution
                Database columnNames = relationToDB.get(remoteDBName + "." + RemoteDBConstants.COLUMN_NAMES);
                List<String> select = new ArrayList<String>();
                select.add("*");
                List<String> from = new ArrayList<String>();
                from.add(RemoteDBConstants.COLUMN_NAMES);
                ArrayList<WhereClause> where = new ArrayList<WhereClause>();
                where.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_TABLE_IDS, tableID, "="));
                ArrayList<Integer> s = new ArrayList<Integer>();

                try {
                    ResultSet res = columnNames.select(select, from, where, false, null, false, sessionid);
                    while (res.next()) {
                        s.add(Integer.parseInt(res.getString(RemoteDBConstants.COLUMN_ID)));
                    }
                } catch (SQLException e) {
                    String msg = "Could not select requested data";
                    logger.log(Level.INFO, msg, e);
                    throw new DatabaseException(msg, e);
                }


                for (int i : s) {
                    ArrayList<WhereClause> where2 = new ArrayList<WhereClause>();
                    where2.add(new WhereClauseBinary(RemoteDBConstants.COLUMN_SERVER_ID, i, "="));
                    //TODO: Delegate deletion to index table
                    remoteDB.delete(RemoteDBConstants.COLUMN_SERVER_MAPPING, where2, sessionid);
                }

            }

            //Delete unnecessary entries in index Tables
            helper.deleteFields(tableID, remoteDB, sessionid);
            //Finally delete tablename from Remote
            ArrayList<WhereClause> where = new ArrayList<WhereClause>();
            where.add(new WhereClauseBinary(RemoteDBConstants.TABLE_IDS, tableID, "="));
            remoteDB.delete(RemoteDBConstants.TABLE_NAMES, where, sessionid);
            //drop Table from Local Database
            localDB.dropTable(tableName, sessionid);
        } else {
            if (!ifexists) {
                throw new DatabaseException("Table " + tableName + " Could not be dropped because it does not exist in database!\n");
            }
        }
        //refresh the local Mapping
        helper.refreshMappings(sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#createTable()
     */
    @Override
    public void createTable(String tableName, List<Column> columns, String sessionid)
            throws DatabaseException {
        // 1. creates specific tables within meta-table-structure
        String remoteTable = remoteDBName + "." + RemoteDBConstants.TABLE_NAMES;
        Database remoteDB = relationToDB.get(remoteTable);
        // get the new tableID
        int newTableID = helper.selectMaxIDPlusOne(RemoteDBConstants.TABLE_NAMES, remoteDB, RemoteDBConstants.TABLE_IDS, sessionid);
        tablemanager.createEntryInTableMeta(newTableID, tableName, sessionid);
        boolean isManuallyDistributed = isIndexManuallyDistributed();
        //2. create fields inside FieldMeta
        for (int i = 0; i < columns.size(); i++) {
            int newColumnID = helper.selectMaxIDPlusOne(RemoteDBConstants.COLUMN_NAMES, remoteDB, RemoteDBConstants.COLUMN_ID, sessionid);
            tablemanager.createColumnInFieldMeta(newTableID, newColumnID, columns.get(i), sessionid);
            if (isManuallyDistributed) {
                IDistributedServer server = DistributedTablesConfiguration.getInstance().getServer(tableName, columns.get(i).getName());
                tablemanager.createColumnServerMapping(newColumnID, server.getServerId(), server.getServerName(), sessionid);
            }
        }
        //3. refresh the local Mapping of the tables!
        helper.refreshMappings(sessionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#renameColumn
     */
    @Override
    public boolean renameColumn(String tableName, String oldColumnName,String newColumnName, String sessionid) throws DatabaseException {
        boolean success = tablemanager.renameColumn(tableName, oldColumnName, newColumnName,sessionid);
        helper.refreshMappings(sessionid); // refresh local Mappings to avoid errors
        return success;

    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#addConstraint()
     */
    public boolean addConstraint(String table, List<String> columnNames, boolean unique, boolean primaryKey,String sessionid) throws DatabaseException {
        if (!tableMapper.containsKey(table)) {
            throw new DatabaseException("Could not find Table " + table + " in Database! \n");
        }
        tablemanager.addConstraint(columnNames, table, unique, primaryKey,sessionid);
        // refresh the local Mapping of the tables!
        helper.refreshMappings(sessionid);
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#addColumn()
     */
    @Override
    public boolean addColumn(String tableName, String newColumnName,
                             Type datatype, int length, boolean var, String sessionid) throws DatabaseException {
        //Insert in fieldMeta first
        if (!tableMapper.containsKey(tableName)) {
            throw new DatabaseException("Could not find Table " + tableName + " in Database! \n");
        }
        int tableID = tableMapper.getRemoteTableID(tableName);
        if (tableMapper.getRemoteColumns(tableID).containsKey(newColumnName)) { //Check whether column exists already in the table
            throw new DatabaseException("Column " + newColumnName + " can not be added to " + tableName + " because it alrady exists! \n");
        }
        int newFieldID = helper.selectMaxIDPlusOne(RemoteDBConstants.COLUMN_NAMES, relationToDB.get(remoteDBName + "." + RemoteDBConstants.COLUMN_NAMES), RemoteDBConstants.COLUMN_ID,sessionid);
        if (isIndexEncrypted()) {
            datatype = Type.String;
        }
        Column column = new Column(datatype, newColumnName, length, var);
        tablemanager.createColumnInFieldMeta(tableID, newFieldID, column,sessionid);
        //Initialize column with null
        Database remote = relationToDB.get(remoteDBName + "." + RemoteDBConstants.DATA_TABLE);
        List<String> keys = helper.insertNullintoColumn(tableName, remote, datatype, newFieldID,sessionid);
        indexAdmin.insertIntoIndex(datatype.toString().toLowerCase() + "Index", newFieldID, null, keys, remote,sessionid);
        for (Database db : relationToDB.values()) {
            try {
                db.executeInsertBatch(sessionid);
                db.executeUpdateBatch(sessionid);
            } catch (SQLException e) {
                throw new DatabaseException(e.getMessage() + e.getNextException().getMessage());
            }
        }
        helper.refreshMappings(sessionid);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#dropColumn()
     */
    @Override
    public void dropColumn(String tableName, String columnName, String sessionid) throws DatabaseException {
        int fieldID = tableMapper.getColumnID(tableName, columnName);
        //Select all keys of the relation before the index entries are deleted so they can be used to update the data-table
        List<String> keys = selectAllKeys(tableName,sessionid);
        //1. delete from index
        String datatype = tableMapper.getColumnDataType(tableName, columnName).toString().toLowerCase();
        String indexTableName = datatype + "Index";
        String remoteTable = remoteDBName + "." + indexTableName;
        Database index = relationToDB.get(remoteTable);
        ArrayList<WhereClause> deleteWhere = new ArrayList<WhereClause>();
        deleteWhere.add(new WhereClauseBinary(RemoteDBConstants.INDEX_FIELD_ID, fieldID, "="));
        //Delete from Index
        try {
            indexAdmin.deleteFromIndexWhere(indexTableName, deleteWhere, index,sessionid);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
        //2. Delete entries from Data Table
        helper.deleteColumnFromDataTable(tableName, fieldID, keys, index,sessionid);
        //3. Delete entry in fieldMeta Table
        helper.deleteColumnFromFieldMeta(fieldID, index,sessionid);
        helper.refreshMappings(sessionid); //Refresh the Mappings
    }

    /*
     * (non-Javadoc)
     *
     * @see prototype.database.RemoteDatabase#isIndexEncrypted()
     */
    @Override
    public boolean isIndexEncrypted() {
        if (indexAdmin instanceof IndexEncryptedAdministration) {
            return true;
        } else return false;
    }

    /*
      * (non-Javadoc)
      *
      * @see prototype.database.RemoteDatabase#isIndexEncrypted()
      */
    @Override
    public boolean isIndexManuallyDistributed() {
        if (indexAdmin instanceof IndexManualAdministration) {
            return true;
        } else return false;
    }

    public void saveColumnServerMapping(String tableName, Map<String, Integer> mapping,String sessionid) throws DatabaseException {
        if (indexAdmin instanceof IndexManualAdministration) {
            Set<String> columnsSet = mapping.keySet();
            for (String columnName : columnsSet) {
                int colID = TableMapper.getInstance().getColumnID(tableName, columnName);
                tablemanager.changeColumnServerMapping(colID, mapping.get(columnName),sessionid);
            }
        }
    }


}
