package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * Represents an jpa that supports all necessary SQL statments as methods.
 * 
 * @author Yvonne Muelle
 * 
 */
public interface Database {
	
	/**
	 * Deletes the tuple in the relation which fulfills the where-clause. The conditions in the where clause are linked
	 * with the "AND" operator.
	 * 
	 * @param from
	 *            name of the relation which tuples should be deleted.
	 * @param where
	 *            specifies which tuples should be deleted. The where clause as key-value-pairs. The key specifies the
	 *            left argument, the value the right argument which are combined by the operator. The conditions in the
	 *            where clause are linked with the "AND" operator.
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void delete(String from, List<WhereClause> where,String sessionid) throws DatabaseException;
	
	/**
	 * Deletes all tuple in the relation from.
	 * 
	 * @param from
	 *            name of the relation which tuples should be deleted.
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void deleteAll(String from,String sessionid) throws DatabaseException;
	
	/**
	 * Returns all columns of a relation in the order in which they are specified in the relation scheme.
	 * 
	 * @param table
	 *            Table with the columns
	 * @return all columns of a relation in the correct order.
	 */
	List<Column> getColumns(String table);
	
	/**
	 * Returns the name of all relations which are part of the database.
	 * 
	 * @return all relation names in this database.
	 */
	Set<String> getRelationNames();
	
	/**
	 * Inserts the values into the database. The inserting begins with the first column. If the number of values is
	 * unequal to the number of columns, then beginning with the first column, the values are inserted. This is the same
	 * behaviour as in every SQL insert statement.
	 * 
	 * @param into
	 *            relation in which the tuple should be inserted.
	 * @param values
	 *            The values of the data which should be inserted. The number of values can be less than the number of
	 *            columns.
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void insert(String into, List<Object> values,String sessionid) throws DatabaseException;
	
	/**
	 * Inserts the mapping between columns and values into the specified relation in into. The order in columns and
	 * values must be the same, as e.g. the first entry in columns refers to the first entry in values and so on.
	 * 
	 * @param into
	 *            relation in which the tuple should be inserted.
	 * @param columns
	 *            The columns, in which the data should be inserted.
	 * @param values
	 *            The values of the data which should be inserted. If the number of values is unequal to the number of
	 *            columns then the smaller value defines the number of values being inserted.
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void insert(String into, List<String> columns, List<Object> values, boolean batch,String sessionid) throws DatabaseException;
	
	/**
	 * Executes the query. The query must be a syntactic and semantic correct sql statement.
	 * 
	 * @param query
	 *            sql query that should be executed.
	 * @return The result of the query
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	ResultSet performQuery(String query,String sessionid) throws DatabaseException;
	
	/**
	 * Performs a SQL select statement. The conditions in the where clause are linked with the "AND" operator.
	 * 
	 * @param select
	 *            specifies what should be selected.
	 * @param from
	 *            specifies the relation(s) on which the select statement is executed.
	 * @param where
	 *            The where clause as key-value-pairs. The key specifies the left argument, the value the right argument
	 *            which are combined by the operator. The conditions in the where clause are linked with the "AND"
	 *            operator.
	 * 
	 * @param distinct
	 * 		   	  indicates whether Distinct should be enabled!
	 * @param groupBy 
	 * 	          List of columns of the group by clause
	 *            the operator in the where clause, e.g. "="
	 * @param 
	 * 		  updatable Whether the resulset shall be updatable (Is necessary if updates shall be performed on the resultset)
	 * @return the result set if the query could be executed
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	ResultSet select(List<String> select, List<String> from, List<WhereClause> where, boolean distinct, List<String> groupBy, boolean updatable,String sessionid)
	        throws DatabaseException;
	
	/**
	 * Selects all columns specified in select from the relations specified in from.
	 * 
	 * @param select
	 *            specifies what should be selected.
	 * @param from
	 *            specifies the relation(s) on which the select statement is executed.
	 * @param 
	 * 			  updatable Whether the resulset shall be updatable (Is necessary if updates shall be performed on the resultset)
	 * @return the result set if the query could be executed
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	ResultSet selectAll(List<String> select, List<String> from, boolean updatable, String sessionid) throws DatabaseException;
	
	/**
	 * Allows to specify, if the values which should be inserted or updated or the values in the where clauses are
	 * already escaped or if they should be escaped when performing the action.
	 * 
	 * @param on
	 *            <code>true</code> enables auto escaping, <code>false</code> disables auto escaping.
	 */
	void setAutoEscaping(boolean on);
	
	/**
	 * Updates the data to the in udpateData given values. where specifiers which tuple should be updated. The
	 * conditions in the where clause are linked with the "AND" operator.
	 * 
	 * @param table
	 *            relation in which the tuple should be updated.
	 * @param updateData
	 *            The new values for the update data. The key specifies which column should be updated and the value the
	 *            new value.
	 * @param where
	 *            The where clause as key-value-pairs. The key specifies the left argument, the value the right argument
	 *            which are combined by the operator. The conditions in the where clause are linked with the "AND"
	 *            operator.
	 * @param batch
	 *            the operator in the where clause, e.g. "="
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void update(String table, Map<String, Object> updateData, List<WhereClause> where, boolean batch,String sessionid)
	        throws DatabaseException;
	
	/**
	 * Updates the data to the in udpateData given values. Every tuple in the relation is updated.
	 * 
	 * @param table
	 *            relation in which the tuple should be updated.
	 * @param updateData
	 *            The new values for the update data. The key specifies which column should be updated and the value the
	 *            new value.
	 * @throws DatabaseException
	 *             if a error while trying to execute the query.
	 */
	void updateAll(String table, Map<String, Object> updateData,String sessionid) throws DatabaseException;
	
	/**
	 * Important for refreshing the local database scheme from remote
	 */
	void refreshLocalScheme(DBScheme newScheme,String sessionid);
	/**
	 * Important for refreshing the local database scheme from remote
	 */
	void refreshLocalScheme(Map<String, List<Column>> newScheme,String sessionid);

	/**
	 * Selects from the Database with order by 
	 * @param 
	 * 		  select which columns
	 * @param 
	 * 		  from which tables
	 * @param 
	 * 		  where conditions
	 * @param 
	 * 		  distinct whether the query features distinct values
	 * @param 
	 * 		  groupBy the columns to group by, null if no group by 
	 * @param 
	 * 		  by Order by this column
	 * @param 
	 * 		  updatable Whether the resulset shall be updatable (Is necessary if updates shall be performed on the resultset)
	 * @return a Resultset for the given Query
	 * @throws DatabaseException
	 */
	ResultSet selectOrdered(List<String> select, List<String> from,List<WhereClause> where, boolean distinct, List<String> groupBy, List<String> by, boolean updatable,String sessionid) throws DatabaseException;
	/**
	 * Drops the table with the given Name if it exists and deletes all included Data
	 * @param tableName the name of the table to be deleted
	 * @throws DatabaseException
	 */
	void dropTable(String tableName,String sessionid) throws DatabaseException;
	
	/**
	 * Method which uses the multiple row insertion to accelarte the insertion of values into the database (Used for insertion into local database)
	 * @param into the table where the values shall be inserted
	 * @param columns the column names of the table
	 * @param values the Values (each row should match the columns)
	 * @throws DatabaseException
	 */
	void insertBatch(String into, List<Column> columns, List<List<String>> values,String sessionid) throws DatabaseException;
	
	/**
	 * Triggers the stored Updated queries to be executed 
	 * @return true if successfull
	 * @throws SQLException
	 */
	public boolean executeUpdateBatch(String sessionid) throws SQLException;
	
	/**
	 * Triggers the stored Insert queries to be executed 
	 * @return true if successfull
	 * @throws SQLException
	 */
	public boolean executeInsertBatch(String sessionid) throws SQLException;
}
