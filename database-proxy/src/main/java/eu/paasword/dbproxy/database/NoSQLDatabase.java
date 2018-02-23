package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NoSQLDatabase implements Database {
	
	@Override
	public Set<String> getRelationNames() {
		throw new UnsupportedOperationException("This method is not supported yet.");
	}
	
	@Override
	public List<Column> getColumns(String table) {
		throw new UnsupportedOperationException("This method is not supported yet.");
	}
	
	@Override
	public void delete(String from, List<WhereClause> where, String sessionid) {
		throw new UnsupportedOperationException("This method is not supported yet.");
	}
	
	@Override
	public void deleteAll(String from, String sessionid) {
		throw new UnsupportedOperationException("This method is not supported yet.");
	}
	

	
	@Override
	public void update(String table, Map<String, Object> updateData, List<WhereClause> where, boolean batch,String sessionid) {
		throw new UnsupportedOperationException("This method is not supported yet.");
	}
	
	@Override
	public void updateAll(String table, Map<String, Object> updateData,String sessionid) {
		throw new UnsupportedOperationException("This method is not supported yet.");
	}
	
	@Override
	public void setAutoEscaping(boolean on) {
		throw new UnsupportedOperationException("This method is not supported yet.");
		
	}
	
	@Override
	public ResultSet performQuery(String query, String sessionid) {
		throw new UnsupportedOperationException("This method is not supported yet.");
	}
	
	@Override
	public void refreshLocalScheme(DBScheme scheme,String sessionid) {
		throw new UnsupportedOperationException("This method is not supported yet.");
	}
	
	@Override
	public void refreshLocalScheme(Map<String, List<Column>> scheme,String sessionid) {
		throw new UnsupportedOperationException("This method is not supported yet.");
	}


	@Override
	public void dropTable(String tableName, String sessionid) {
		
	}

	@Override
	public void insertBatch(String into, List<Column> columns, List<List<String>> values, String sessionid) throws DatabaseException {
		
	}

	@Override
	public boolean executeUpdateBatch(String sessionid) throws SQLException {
		return false;
	}

	@Override
	public boolean executeInsertBatch(String seesionid) throws SQLException {
		return false;
	}

	@Override
	public void insert(String into, List<Object> values, String sessionid) throws DatabaseException {
	}

	@Override
	public void insert(String into, List<String> columns, List<Object> values, boolean batch, String sessionid)
			throws DatabaseException {
	}

	@Override
	public ResultSet select(List<String> select, List<String> from, List<WhereClause> where,
			boolean distinct, List<String> groupBy, boolean updatable, String sessionid) throws DatabaseException {
		return null;
	}

	@Override
	public ResultSet selectAll(List<String> select, List<String> from, boolean updatable, String sessionid)
			throws DatabaseException {
		return null;
	}

	@Override
	public ResultSet selectOrdered(List<String> select, List<String> from, List<WhereClause> where,
			boolean distinct, List<String> groupBy, List<String> by, boolean updatable, String sessionid)
			throws DatabaseException {
		return null;
	}
}
