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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;


import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.ParseException;
import eu.paasword.dbproxy.exceptions.DatabaseException;

/**
 * This class encapsulates a SQL statement for the secure database prototype and returns a SQL {@link ResultSet}.
 * @author Steffen M�ller
 *
 */
public class SDPStatement implements Statement {

	private SDPConnection connection;
	private SDPResultSet lastResultSet;
	/**
	 * The query timeout. Default in most sql databases is 30 sec.
	 */
	private int queryTimeout = 30;

	public SDPStatement(SDPConnection connection) {
		this.connection = connection;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("Interface is not implemented by this driver.");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		if (sql == null || sql.isEmpty())
			throw new SQLException("Malformed SQL!");
		if(sql.endsWith(";"))
			throw new SQLException("Malformed SQL! SQL string must not end with ';'!");
		
		sql = sql + ";";

		try {
			Adapter a = this.connection.getAdapter();
			ArrayList<ResultSet> result = a.query(sql).getResultSet();
			if(result == null || result.isEmpty())
				return null;

			this.lastResultSet = new SDPResultSet(result.get(0));
			return lastResultSet;
		} catch (ParseException e) {
			throw new SQLException(e.getMessage());
		} catch (DatabaseException e) {
			throw new SQLException(e.getMessage());
		} catch (StandardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lastResultSet;
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		int resultCount = -1;
		try {
			this.executeQuery(sql);
			resultCount = 1;
		} catch (Exception ex) {
			throw (new SQLException(ex.getMessage()));
		}
		return resultCount;
	}

	@Override
	public void close() throws SQLException {
		if (this.lastResultSet != null)
			this.lastResultSet.close();
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return 0;
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		
	}

	@Override
	public int getMaxRows() throws SQLException {
		return 0;
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		// Do nothing
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		// Do nothing
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		return queryTimeout;
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		if (seconds < 0)
			throw new SQLException("Timeout must not be lower than zero!");
		
		this.queryTimeout = seconds;
	}

	@Override
	public void cancel() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCursorName(String name) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean execute(String sql) throws SQLException {
		this.executeQuery(sql);
		
		return true;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return this.lastResultSet;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFetchDirection() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		
	}

	@Override
	public int getFetchSize() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getResultSetType() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		throw new SQLException("Driver does not support batch operations!");
	}

	@Override
	public void clearBatch() throws SQLException {
		throw new SQLException("Driver does not support batch operations!");
	}

	@Override
	public int[] executeBatch() throws SQLException {
		throw new SQLException();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return this.connection;
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		throw new SQLException();
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		throw new SQLException();
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		return executeUpdate(sql);
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		return executeUpdate(sql);
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		return executeUpdate(sql);
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		throw new SQLException();
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		throw new SQLException();
	}

	@Override
	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		throw new SQLException();
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isClosed() throws SQLException {
		if(connection == null)
			return true;
		
		return false;
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPoolable() throws SQLException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
