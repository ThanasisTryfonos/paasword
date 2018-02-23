package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.Type;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Interface which encapsulates all supported SQL query types. What the functionality of these methods includes except
 * the sql statement execution is implementation dependent.
 * 
 * @author Yvonne Muelle
 * 
 */
public interface RemoteDatabase {
	
	/**
	 * Method to select the keys which are stored in index tables. The encrypted data is not selected. The statement is
	 * executed until an error occurred.
	 * 
	 * @param from
	 *            original relation
	 * @param where
	 *            The arguments for the where clause. As key the left side, as value the right side of the operator
	 * @param keysToRetain
	 * 			  a List of keys which shall be matched to the keys selected by the whereclause. Is used for and nodes.
	 * @param or whereclauses shall be connected through or
	 * @return List of result of the select statement
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	List<String> selectKeys(String from, List<WhereClause> where, List<String> keysToRetain, boolean or,String sessionid) throws DatabaseException;
	
	/**
	 * Method to select all keys which are stored in index tables. From defines from to which not encrypted table they
	 * belong. The encrypted data is not selected. The statement is executed until an error occurred.
	 * 
	 * @param from
	 *            original relation
	 * @return List of the result of the select statement.
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	List<String> selectAllKeys(String from,String sessionid) throws DatabaseException;
	
	
	
	/**
	 * Method to select the encrypted data. The statement is executed until an error occurred.
	 * 
	 * @param keys
	 *            a List of keys identifying the rows to be selected
	 * @return the decrypted data split in the original relation scheme, one entry for each tuple
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	List<List<String>> selectData(List<String> keys,String sessionid)
	       throws DatabaseException;
	
	/**
	 * Method to select all encrypted data from from. The statement is executed until an error occurs.
	 * 
	 * @param from
	 *            original relation
	 * @return the decrypted data split in the original relation scheme, one entry for each tuple
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	List<List<String>> selectAllData(String from,String sessionid) throws DatabaseException;
	
	/**
	 * Inserts the mapping between columns and values into the specified relation in into. No separation between key and
	 * data must be made. The index table are also updated in this method. The order in columns and values must be the
	 * same, as e.g. the first entry in columns refers to the first entry in values and so on. The statement is executed
	 * until an error occurred.
	 * 
	 * @param into
	 *            original relation
	 * @param columns
	 *            The columns, in which the data should be inserted.
	 * @param values
	 *            The values of the data which should be inserted. The number of values must be equal to the number of
	 *            columns.
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void insert(String into, List<String> columns, List<Object> values,String sessionid) throws DatabaseException;
	
	/**
	 * Inserts the values into the database. The inserting begins with the first column. If the number of values is
	 * unequal to the number of columns, then beginning with the first column, the values are inserted. This is the same
	 * behaviour as in every SQL insert statement. The statement is executed until an error occurred.
	 * 
	 * @param into
	 *            original relation
	 * @param values
	 *            The values of the data which should be inserted. The number of values can be less than the number of
	 *            columns.
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void insert(String into, List<Object> values,String sessionid) throws DatabaseException;
	
	/**
	 * Updates the data to the in udpateData given values. where specifiers which tuple should be updated. This method
	 * includes index and data updates. The statement is executed until an error occurred.
	 * 
	 * @param table
	 *            original relation
	 * @param updateData
	 *            The new values for the update data. The key specifies which column should be updated and the value the
	 *            new value.
	 * @param where
	 *            The where clause as key-value-pairs. The key specifies the left argument, the value the right argument
	 *            which are combined by the operator.
	 * @param keys
	 *            a List of keys which identify the rows to be updated
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void update(String table, Map<String, Object> updateData, List<WhereClause> where, List<String> keys,String sessionid)
	        throws DatabaseException;
	
	/**
	 * Updates the data to the in udpateData given values. Every tuple in the relation is updated. This method includes
	 * index and data updates. The statement is executed until an error occurred.
	 * 
	 * @param table
	 *            original relation
	 * @param updateData
	 *            The new values for the update data. The key specifies which column should be updated and the value the
	 *            new value.
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void updateAll(String table, Map<String, Object> updateData,String sessionid) throws DatabaseException;
	
	/**
	 * Deletes the tuple in the relation which fulfills the where-clause. This method includes index and data deletions.
	 * The statement is executed until an error occurred.
	 * 
	 * @param table
	 *            original relation
	 * @param allkeys
	 *            The keys identifying the rows to be deleted 
	 * @param escapedColumns
	 * 			  A list of columns where null shall be escaped as String 'null'
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	public void delete(String table, List<String> allkeys, List<String> escapedColumns,String sessionid) throws DatabaseException;
	
	/**
	 * Deletes all tuple in the relation. This method includes index and data deletions. The statement is executed until
	 * an error occurred.
	 * 
	 * @param table
	 *            original relation
	 * @throws DatabaseException
	 *             if a error while trying to execute the query. 
	 */
	void deleteAll(String table,String sessionid) throws DatabaseException;
	
	/**
	 * Allows to specify, if the values which should be inserted or updated or the values in the where clauses are
	 * already escaped or if they should be escaped when performing the action.
	 * 
	 * @param on
	 *            <code>true</code> enables auto escaping, <code>false</code> disables auto escaping.
	 */
	void setAutoEscaping(boolean on);
	
	/**
	 * Inserts the tuples which are specified in the file into the original relation. The attributes of each tuple must
	 * be separated by the delimiter and the number of attributes per tuple must be consistent with the number of
	 * attributes specified by the relation scheme.
	 * 
	 * @param relation
	 *            Relation in which the data should be inserted
	 * @param filename
	 *            fully qualified name of the file in which the data is stored.
	 * @param delimiter
	 *            delimiter between the attribute values
	 * @throws DatabaseException
	 *             if an error occurred while trying to execute the query
	 * @throws IOException
	 *             if the file could not be processed
	 */
	void insertFromFile(String relation, String filename, char delimiter,String sessionid) throws IOException, DatabaseException;

	/**
	 * 
	 * @return the remote DBScheme in a Map not a DBScheme-Object
	 */
	Map<String, List<Column>> getPlaineDBScheme();
	
	/**
	 * Creates a new Table in the Database
	 * @param tableName the name of the new Table
	 * @param columns the Columns which shall be created with name and datatype, ignoring the id!
	 * @throws DatabaseException
	 */
	void createTable(String tableName, List<Column> columns, String sessionid)
			throws DatabaseException;
	
	/**
	 * Drops a Table in the remote Database and deletes all the Data which belongs to this table
	 * @param tableName the name of the table
	 * @param ifexists whether if exists was specified, if no a Exception is generated if the table does not exist
	 * @throws DatabaseException
	 */
	void dropTable(String tableName, boolean ifexists, String sessionid) throws DatabaseException;
	/**
	 * Renames a Column in the remote Database
	 * @param tableName the table in which the column shall be renamed
	 * @param oldColumnName the old name of the column to find it
	 * @param newColumnName the new name of the column which shall be applied
	 * @throws DatabaseException 
	 */
	boolean renameColumn(String tableName, String oldColumnName, String newColumnName, String sessionid) throws DatabaseException;
	
	/**
	 * Adds a Column to the specified Table and initializes the column with null values
	 * @param tableName the name of the table the column shall be added to
	 * @param newColumnName the name of the new column
	 * @param datatype the type of the new column
	 * @return whether insertion was sucessful or not
	 * @throws DatabaseException 
	 */
	boolean addColumn(String tableName, String newColumnName, Type datatype, int length, boolean var, String sessionid) throws DatabaseException;
	
	/**
	 * Drops a collum from the specified table 
	 * @param tableName the name of the table the column shall be removed from 
	 * @param columnName the name of the column to remove
	 * @throws DatabaseException
	 * @throws SQLException 
	 */
	void dropColumn(String tableName, String columnName,String sessionid) throws DatabaseException;
	
	/**
	 * Method to add a constraint to one or more columns
	 * @param table the name of the table which shall be changed
	 * @param columnNames the name of the columns which shall be added a constraint to
	 * @param unique whether it shall be a unique constraint
	 * @param primaryKey whether it shall be a primary key constraint
	 * @return
	 * @throws DatabaseException
	 */
	boolean addConstraint(String table, List<String> columnNames, boolean unique, boolean primaryKey, String sessionid) throws DatabaseException;

	/**
	 * Returns if keys in index tables are encrypted
	 * @return
	 */
	boolean isIndexEncrypted();

	/**
	 * Returns if index tables are manually distributed
	 * @return
	 */
	boolean isIndexManuallyDistributed();
}