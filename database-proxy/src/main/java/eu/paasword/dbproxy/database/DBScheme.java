package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the scheme of a database. A scheme consists of one or more relations.
 * 
 * @author Yvonne Muelle
 * 
 */
public class DBScheme {
	// For each table, the columns in it
	private Map<String, List<Column>> tableToColumns;
	
	/**
	 * Constructs an object of this class
	 * 
	 * @param tableToColumns
	 *            maps the name of each relation in the database to which this scheme belongs, to its columns
	 */
	public DBScheme(Map<String, List<Column>> tableToColumns) {
		this.tableToColumns = tableToColumns;
		
	}
	
	/**
	 * Returns the type of a specific column in a specific table
	 * 
	 * @param column
	 *            column which to get the type from
	 * @param table
	 *            table to which the column belongs to
	 * @return type of the column
	 */
	public Type getType(String column, String table) {
		List<Column> cols = tableToColumns.get(table);
		
		for (Column c : cols) {
			if (c.getName().equals(column)) {
				return c.getType();
			}
		}
		
		return null;
	}
	
	/**
	 * Returns all column names of the table in the order in which the columns are specified in the scheme in the real
	 * database.
	 * 
	 * @param table
	 *            table in the database
	 * @return column names of the table or <code>null</code> if the table is not part of this scheme.
	 */
	public List<String> getNames(String table) {
		List<String> names = new ArrayList<String>();
		
		List<Column> cols = tableToColumns.get(table);
		if(cols != null) {
		for (Column c : cols) {
			names.add(c.getName());
		}
		}
		return names;
	}
	
	/**
	 * Returns all columns of the table in the order in which the columns are specified in the scheme in the real
	 * database.
	 * 
	 * @param table
	 *            table in the database
	 * @return columns of the table or <code>null</code> if the table is not part of this scheme.
	 */
	public List<Column> getColumns(String table) {
		return tableToColumns.get(table);
	}
	
	/**
	 * Returns all relation names that are specified in this database scheme.
	 * 
	 * @return relation names of this scheme or an empty set if the table is not part of this scheme.
	 */
	public Set<String> getTableNames() {
		return tableToColumns.keySet();
	}
	
	/**
	 * Returns the column object that belongs to the column name in the table
	 * 
	 * @param columnName
	 *            name of the column
	 * @param table
	 *            name of the table
	 * @return column object or <code>null</code> if no column with this name in this table exists.
	 */
	public Column getColumnByName(String columnName, String table) {
		List<Column> cols = tableToColumns.get(table);
		
		for (Column c : cols) {
			if (c.getName().equals(columnName)) {
				return c;
			}
		}
		
		return null;
	}
	
	/**
	 * Checks if the column is part of the table.
	 * 
	 * @param table
	 *            table in which to check
	 * @param column
	 *            to be checked
	 * @return <code>true</code> if the table contains this column, otherwise <code>false</code>
	 */
	public boolean containsColumn(String table, String column) {
		String col = column.substring(column.lastIndexOf(".") + 1, column.length());

		String tab = table.substring(table.lastIndexOf(".") + 1, table.length());
		List<String> colNames = getNames(tab);

		if (colNames.contains(col)) {
			return true;
		}
		
		return false;
		
	}
	
}
