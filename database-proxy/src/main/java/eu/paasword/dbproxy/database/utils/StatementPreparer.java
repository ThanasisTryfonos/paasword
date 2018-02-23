package eu.paasword.dbproxy.database.utils;

import eu.paasword.dbproxy.database.RemoteDBConstants;
import eu.paasword.dbproxy.exceptions.InvalidStatementException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class responsible to generate the empty statements with values escaped by "?". These are used for the Prepared Statements
 * @author Mark Brenner
 *
 */
public class StatementPreparer {
	/**
	 * Prepares the java.sql.PreparedStatement with ?, where later the values  are inserted. whereClause
	 * @param sql the beginning of the SQL-Query, e.g. "update table..."
	 * @param where the where clauses 
	 * @param nullAware escapes null with is NULL 
	 * @return a prototype of a prepared Statement 
	 */
	public static String prepareString(String sql, List<WhereClause> where, boolean nullAware) {
		Iterator<WhereClause> iter = where.iterator();
		while (iter.hasNext()) {
			WhereClause clause = iter.next();
			if (clause instanceof WhereClauseBinary) {
				WhereClauseBinary wherebin = (WhereClauseBinary) clause;
				// Handle the where IS (NOT) NULL here
				if (wherebin.getOperator().equals(RemoteDBConstants.ISNULL)
						|| (nullAware && wherebin.getRightOperand().equals("null"))) {
					sql += "(" + wherebin.getLeftOperand() + " " + RemoteDBConstants.ISNULL
							+ ") and ";
					iter.remove(); // Remove the IS (NOT) NUll where clause here
									// to avoid Problems later
				} else if (wherebin.getOperator().equals(RemoteDBConstants.ISNOTNULL)) {
					sql += "(" + wherebin.getLeftOperand() + " " + RemoteDBConstants.ISNOTNULL
							+ ") and ";
					iter.remove(); // Remove the IS (NOT) NUll where clause here
									// to avoid Problems later
				} else {
					sql += "(" + wherebin.getLeftOperand() + " " + wherebin.getOperator()
							+ " ?) and ";
				}
			} else if (clause instanceof WhereClauseIn) {
				WhereClauseIn wherein = (WhereClauseIn) clause;
				String operator = " in ( ";
				if(wherein instanceof WhereClauseNotIn) {
					 operator = " not in ( ";
				}
				if(!wherein.getIn().isEmpty()) {
				sql += wherein.getLeftOperand() + operator;
				for(@SuppressWarnings("unused") Object o : wherein.getIn()) {
					sql += "?, ";
				}
				sql = sql.substring(0, sql.lastIndexOf(","));
				sql += ") and ";
				}
			}
		}
		return sql.substring(0, Math.max(sql.lastIndexOf(" and"), sql.lastIndexOf(")")));
	}

	/**
	 * Prepares an empty Insert Query with several values
	 * @param into the table where the data shall be inserted into
	 * @param columns the name of the columns to be inserted into
	 * @param currenValues the values to be inserted (each row is a separate List)
	 * @return an insert statement with several rows
	 * @throws InvalidStatementException 
	 */
	public static String prepareInsertBatch(String into, List<Column> columns,
			List<List<String>> currenValues) throws InvalidStatementException {
			int counter = 0;
			if(currenValues.size() < 1) {
				throw new InvalidStatementException("No values given for this insert!\n");
			}
			if(columns.size() < 1) {
				throw new InvalidStatementException("No columns given for this insert!\n");
			}
			if(into == null) {
				throw new InvalidStatementException("Undefined table!\n");
			}
			int size = Math.min(columns.size(), currenValues.get(0).size());
			String row = " ( ";
			for (int j = 0; j < size-1; j++) {
				row += "?, ";
			}
			row += "?),";
			String valueString = "";
			int evenuneven = currenValues.size() % 2;
			for (int i = 0; i < (currenValues.size() / 2); i++) {
				valueString += row;
			}
			valueString += valueString;
			if(evenuneven == 1) {
				valueString += row;
			}
			valueString = valueString.substring(0, valueString.lastIndexOf(","));
			String sql = "INSERT INTO \"" + into + "\" (";
			for (Column col : columns) {
				if (counter == size) {
					break;
				}
				counter++;

				sql += "\"" + col.getName() + "\", ";
			}
			sql = sql.substring(0, sql.lastIndexOf(","));
			sql += ") " + "VALUES" + valueString;
		 return sql;
	}
	
	/**
	 * Prepares a normal Insert with only one row 
	 * @param into the table where the data shall be inserted into
	 * @param columns the name of the columns to be inserted
	 * @param values the values to be inserted 
	 * @param where an empty list which is used for the filling of the statement later (is filled here)
	 * @return single row insert statement
	 * @throws InvalidStatementException 
	 */
	public static String prepareInsert(String into, List<String> columns, List<Object> values, List<WhereClause> where) throws InvalidStatementException {
		int size = Math.min(columns.size(), values.size());
		if(columns.size() != values.size()) {
			throw new InvalidStatementException("Number of values and columns does not match!\n");
		}
		String valueString = "VALUES (";
		for (int i = 0; i < size; i++) {
			valueString += "?, ";
		}
		
		valueString = valueString.substring(0, valueString.lastIndexOf(",")) + ")";
		
		int counter = 0;
		String sql = "INSERT INTO \"" + into + "\" (";
		for (String col : columns) {
			if(col == null) {
				throw new InvalidStatementException("Column is null!\n");
			}
			if (counter == size) {
				break;
			}
			counter++;
			
			sql += col + ", ";
		}
		
		sql = sql.substring(0, sql.lastIndexOf(","));
		sql += ") " + valueString;
		for (int i = 0; i < size; i++) {
			where.add(new WhereClauseBinary(columns.get(i), values.get(i), null));
		}
		return sql;
	}
	
	/**
	 * Prepares a select statement 
	 * @param select the columns to select 
	 * @param from the tables to select from
	 * @param where where clauses as a list
	 * @param distinct duplicate rows are only selected once if enabled
	 * @param groupBy the columns to group the result by (overgive null if no group by is needed)
	 * @return a prepared Select Statement 
	 * @throws InvalidStatementException 
	 */
	public static String prepareSelect(List<String> select, List<String> from, List<WhereClause> where, 
		       boolean distinct, List<String> groupBy) throws InvalidStatementException {
		if(select == null || select.isEmpty()) {
			throw new InvalidStatementException("What to select is undefined!\n");
		}
		if(from == null || from.isEmpty()) {
			throw new InvalidStatementException("Where to select from is undefined!\n");
		}
		
		String sql = "SELECT ";
		if(distinct) {
			sql += "DISTINCT ";
		}
		for (String s : select) {
			sql += s + ", ";
		} 
		//Remove the last ,
		sql = sql.substring(0, sql.lastIndexOf(","));
		sql += " FROM ";
		
		for (String f : from) {
			sql += "\"" + f + "\"" + ", ";
		}
		//Remove the last ,
		sql = sql.substring(0, sql.lastIndexOf(","));
		
		if (where != null && !where.isEmpty()) {
			sql += " WHERE ";
			sql = prepareString(sql, where, false);
		}
		
		if(groupBy != null && !groupBy.isEmpty()) {
			sql += " GROUP BY ";
			for(String col : groupBy) {
				sql += col + ", ";
			}
			sql = sql.substring(0, sql.lastIndexOf(","));
		}
		return sql;
	}
	
	/**
	 * Prepares an update statement
	 * @param table the table to be updated
	 * @param updateData the new data which shall be inserted
	 * @param where the whereclauses defining the rows to be updated
	 * @return an update statement
	 * @throws InvalidStatementException 
	 */
	public static String prepareUpdate(String table, Map<String, Object> updateData, List<WhereClause> where) throws InvalidStatementException {
		String sql = "UPDATE \"" + table + "\" SET ";	
		// set part of the update statement
		if(updateData == null || updateData.isEmpty()) {
			throw new InvalidStatementException("Update data is undefined!\n");
		}
		for (String s : updateData.keySet()) {
			sql += s + " = ?, ";
		}
		sql = sql.substring(0, sql.lastIndexOf(","));
		// sql = prepareString(sql, keysData, "=", updateData, false);
		// Where part of the update statement;
		if (!where.isEmpty()) {
			sql = prepareString(sql + " WHERE ", where, false);
		}
		return sql;
	}
	
	/**
	 * Prepares to select all data from the tables
	 * @param select the columns which shall be selected
	 * @param from the tables to be selected from
	 * @return a select all statement
	 * @throws InvalidStatementException 
	 */
	public static String prepareSelectAll(List<String> select, List<String> from) throws InvalidStatementException {
		String sql = "SELECT ";
		if(select == null || select.isEmpty()) {
			throw new InvalidStatementException("What to select is undefined!\n");
		}
		if(from == null || from.isEmpty()) {
			throw new InvalidStatementException("Where to select from is undefined!\n");
		}
		for (String s : select) {
			sql += s + ", ";
		}
		
		sql = sql.substring(0, sql.lastIndexOf(","));
		sql += " FROM ";
		
		for (String f : from) {
			sql += "\"" + f + "\"" + ", ";
		}
		
		sql = sql.substring(0, sql.lastIndexOf(","));
		return sql;
	}
	
	/**
	 * Prepares a select which contains a order orderBy clause
 * @param select the columns to select 
	 * @param from the tables to select from
	 * @param where where clauses as a list
	 * @param distinct duplicate rows are only selected once if enabled
	 * @param groupBy the columns to group the result orderBy (overgive null if no group orderBy is needed)
	 * @param orderBy columns to order orderBy
	 * @return a select statement with an order orderBy clause
	 * @throws InvalidStatementException 
	 */
	public static String prepareSelectOrdered(List<String> select, List<String> from, List<WhereClause> where, 
		       boolean distinct, List<String> groupBy, List<String> orderBy) throws InvalidStatementException {
		String sql = prepareSelect(select, from, where, distinct, groupBy);
		if(orderBy != null && !orderBy.isEmpty()) {
			sql += " ORDER BY ";
			for (String b : orderBy) {
				sql += "\"" + b + "\"" + ", ";
			}
			
			sql = sql.substring(0, sql.lastIndexOf(","));
		}
		return sql;
	}
}
