package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.Type;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * This class holds the internal Structure of the database as local Copy & stores the necessary constants
 * @author Mark Brenner
 *
 */
public class TableMapper {
	//Table Constants
	private final int TABLE_NAME = 2;
	private final int TABLE_IDS = 1;
	private HashMap<String, Integer> remoteTables;
	private ColumnMapper columnMapper;	
	private Logger logger = Logger.getLogger("database.TableMapper");
	private static TableMapper instance;
	
	
	public static TableMapper getInstance(){
		if(instance == null) {
			instance = new TableMapper();
		}
		return instance;
	}
	
	/**
	 * Constructor for a table Mapper which also initsthe necessary columMapper
	 */
	private TableMapper() {
		remoteTables = new HashMap<String, Integer>();
		columnMapper = new ColumnMapper();
	}
	
	
	/**
	 * Enables the reading of the Hashmap to public
	 * @param identifier the key which is searched, i. e. a tableName
	 * @return the tableId which belongs to the key or null if it was not found
	 */
	public int getRemoteTableID(String identifier) {
		return instance.remoteTables.get(identifier);
	}

	/**
	 * Enables the reading of the Hashmap to public
	 * @param tableID the id which is searched
	 * @return the table name of the table with this id
	 */
	public String getRemoteTableName(int tableID) {
		Set<String> keys = instance.remoteTables.keySet();
		for (String k:keys) {
			if (instance.remoteTables.get(k) == tableID) {
				return k;
				}
			}
		return "no such table";
	}


	/**
	 * Enables the reading of the Hashmap for the columns to public
	 * @param identifier the TableId
	 * @return a Hashmap of the Columns for a certain TableID, which has the Columnname as key
	 */
	public HashMap<String, Column> getRemoteColumns(int identifier) {
		return instance.columnMapper.getRemoteColumns(identifier);
	}
	
	/**
	 * Checks whether the TableMap constains a certain table
	 * @param key the name of the table to be found
	 * @return true if the table exists in the mapping else false
	 */
	public boolean containsKey(String key) {
		return instance.remoteTables.containsKey(key);
	}

	/**
	 * This method is used to refresh the stored Mapping of the Database
	 * @param tables the Resultset of a query for the tableMeta
	 * @param Columns the Resultste of a query for the fieldMeta
	 * @throws DatabaseException 
	 */
	public void refresh(ResultSet tables, ResultSet Columns) throws DatabaseException {
		try {
			instance = new TableMapper();
			while (tables.next()) {
				instance.remoteTables.put(tables.getString(TABLE_NAME), tables.getInt(TABLE_IDS));
			}
		} catch (SQLException e1) {
			String msg = "Could not get remote tables";
			logger.log(Level.INFO, msg, e1);
			throw new DatabaseException(msg, e1);
		}
		instance.columnMapper.refresh(Columns);
	}
	
	/**
	 * 
	 * @param table the name of the table
	 * @return all primary keys for a table
	 */
	public Map<Column, Integer> getPrimaryKeys(String table) {
		HashMap<Column, Integer> primarykeys = new HashMap<Column, Integer>();
		int i = 0;
		for(Entry<String, Column> entry : instance.getRemoteColumns(instance.getRemoteTableID(table)).entrySet()) {
			if(entry.getValue().isPrimary_key()) {
				primarykeys.put(entry.getValue(), i);
			}
			i++;
		}
		return primarykeys;
	}
	/**
	 *
	 * @param tableName the table the column belongs to
	 * @param columnName the column which id shall be selected
	 * @return  the internal ID of the given Column belonging to the given table
	 * @throws DatabaseException if Column of table do not exist
	 */
	public int getColumnID(String tableName, String columnName) throws DatabaseException {
		if(!containsKey(tableName)) {
			 throw new DatabaseException("Could not find Table " + tableName + " in Database! \n");
		}
		int tableID = getRemoteTableID(tableName);
		if(!getRemoteColumns(tableID).containsKey(columnName)) { //Check whether column exists in the table
			throw new DatabaseException("Column " + columnName + " does not exist in " + tableName + "\n");
		}
		HashMap<String, Column> columns = getRemoteColumns(tableID);
		Column col = columns.get(columnName);
		return col.getId();
	}
	
	/**
	 *
	 * @param tableName the table the column belongs to
	 * @param columnName the column which datatype shall be selected
	 * @return  the internal datatype of the given Column belonging to the given table as Type 
	 * @throws DatabaseException if Column of table do not exist
	 */
	public Type getColumnDataType(String tableName, String columnName) throws DatabaseException {
		if(!containsKey(tableName)) {
			 throw new DatabaseException("Could not find Table " + tableName + " in Database! \n");
		}
		int tableID = getRemoteTableID(tableName);
		if(!getRemoteColumns(tableID).containsKey(columnName)) { //Check whether column exists in the table
			throw new DatabaseException("Column " + columnName + " does not exist in " + tableName + "\n");
		}
		HashMap<String, Column> columns = getRemoteColumns(tableID);
		Column col = columns.get(columnName);
		return col.getType();
	}
	
	/**
	 * builds a DBScheme of remoteTable for building a new localDB Structure
	 * @return a raw Scheme of the the tables structure
	 */
	 public Map<String, List<Column>> getPlaineDBScheme() {
			Map<String, List<Column>> schemeData = new HashMap<String, List<Column>>();
			Set<String> tableNames = instance.remoteTables.keySet();
			for (String tableName : tableNames) {
				int tableID = instance.remoteTables.get(tableName);
				List<Column> schemeColumns = new ArrayList<Column>();
				HashMap<String, Column> columns = instance.columnMapper.getRemoteColumns(tableID);
				if(columns != null) {
				schemeColumns.addAll(columns.values());
				Collections.sort(schemeColumns);
				schemeData.put(tableName, schemeColumns);
				}
			}
			return schemeData;
		}
	
	/**
	 * Private Class which stores the mapping of the Columns which is necessary to project the whole database Structure into 
	 * the mapping
	 * @author Mark Brenner
	 *
	 */
	class ColumnMapper {
		private Logger logger = Logger.getLogger("database.ColumnMapper");
		protected TreeMap<Integer, HashMap<String, Column>> remoteColumns; //maps tableID to a map of column names to id and datatype
		
		/**
		 * Constructor for the ColumnMapper, should only be invoked by the TableMapper to avoid inconsistencies
		 */
		protected ColumnMapper() {
			remoteColumns = new TreeMap<Integer, HashMap<String, Column>>();
		}
		/**
		 * Gets the columns for a certain TableID
		 * @param identifier the TableID which columns shall be found
		 * @return a Hashmap of the Columns for a certain TableID, which has the Columnname as key
		 */
		protected  HashMap<String, Column> getRemoteColumns(int identifier) {
			  return  remoteColumns.get(identifier);
		}
		
		/**
		 * Refreshes the Column Mapping stored in this class
		 * @param columns a resultset for fieldMeta
		 * @throws DatabaseException
		 */
		protected void refresh(ResultSet columns) throws DatabaseException {
				try {
					while(columns.next()) {
						int id = columns.getInt(2); //Improves Performance 2 = Column_Table_ID
						Type datatype = Type.customValueOf(columns.getString(4));
					 
						if(datatype == null) {
							throw new DatabaseException("Type " + columns.getString(RemoteDBConstants.COLUMN_DATATYPE) + " of the column" + columns.getString(RemoteDBConstants.COLUMN_NAME) + " is unkown");
						}
						Column column = new Column(columns.getString(3), datatype, columns.getInt(1), columns.getInt(5), columns.getBoolean(6), columns.getBoolean(RemoteDBConstants.COLUMN_NOT_NULL), columns.getBoolean(RemoteDBConstants.COLUMN_UNIQUE), columns.getBoolean(RemoteDBConstants.COLUMN_PRIMARY_KEY));  //Improves Performance 3 = Column_Name 1 = Column_Id
						if(!remoteColumns.containsKey(id)) {
							remoteColumns.put(id, new HashMap<String, Column>());
						}
						remoteColumns.get(id).put(columns.getString(RemoteDBConstants.COLUMN_NAME), column);
					}
				} catch (SQLException e1) {
					String msg = "Could not get columndata to insert";
					logger.log(Level.INFO, msg, e1);
					throw new DatabaseException(msg, e1);
				}
		}
	}
}
