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
package eu.paasword.dbproxy.database.utils;

import eu.paasword.dbproxy.database.DBScheme;
import eu.paasword.dbproxy.database.RemoteDBConstants;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.exceptions.IllegalQueryException;
import eu.paasword.dbproxy.exceptions.InvalidStatementException;
import eu.paasword.dbproxy.exceptions.UnknownTypeException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class should be used to fill Prepared Statements with data
 * @author Mark Brenner
 *
 */
public class StatementFiller {
	/**
	 * The Scheme of the remoteDB (must be updated with every change)
	 */
	DBScheme scheme;
	Logger logger = Logger.getLogger("Database.Utils.StatementFiller");
	
	/**
	 * Constructor for the Filler. Used to set the scheme the first time
	 * @param localScheme the scheme of the remote database
	 */
	public StatementFiller(DBScheme localScheme) {
		scheme = localScheme;
	}
	
	/**
	 * Refreshes the scheme of the remote Database with the locally stored scheme Object
	 * @param localScheme locally stored scheme Object
	 */
	public void refreshScheme(DBScheme localScheme) {
		scheme = localScheme;
	}
	/**
	 * Standard fill values used for deletes etc.
	 * @param from the Table where shall be selected from
	 * @param stat the Prepared satement which shall be inserted into
	 * @param where list of where clauses responsible for the data
	 * @throws SQLException
	 * @throws DatabaseException 
	 */
	public void fillValues(String from, PreparedStatement stat, List<WhereClause> where) throws SQLException, DatabaseException {
		// counter is necessary in order to know where the value is inserted in the prepared Statement.
		int counter = 1;
		for (WhereClause clause : where) {
			counter = fillWhereClause(from, clause, counter, stat);
		}
	}
	
	/**
	 * Special fill procedure for an insert
	 * @param stat the Statement to be filled 
	 * @param columns the columns of the Table which shall be filled
	 * @param currentValues the data to be inserted
	 * @throws SQLException
	 * @throws InvalidStatementException 
	 */
	public void fillValuesInsertBatch(PreparedStatement stat, List<Column> columns, List<List<String>> currentValues) throws SQLException, InvalidStatementException {
		int position = 1;
		int size = Math.min(columns.size(), currentValues.get(0).size());
		if(size > 0) {
		for(int i = 0; i < currentValues.size();i++) {
			if(!currentValues.get(i).isEmpty()) {
				for(int j = 0; j < size; j++) {
					fill(stat, columns.get(j).getType(), position , currentValues.get(i).get(j));
					position++;
				}
			}
		}
		} else {
			throw new InvalidStatementException("Values or columns not specified for filling!\n");
		}
	}
	
	//Fills the data in every Whereclause into the statement
	private int fillWhereClause(String from, WhereClause clause, int counter, PreparedStatement stat) throws SQLException, DatabaseException {
		Type type = scheme.getType(clause.getLeftOperand(), from);	
		if(type == null) {
			throw new DatabaseException("Column " + clause.getLeftOperand() + " does not exist in Table "+ from + "!\n" );
		}
		if (clause instanceof WhereClauseIn) {
			WhereClauseIn in = (WhereClauseIn) clause;
			List<Object> list = in.getIn();
			for(Object o : list){
				fill(stat, type, counter, o);
				counter++;
			}
			
		} else if(clause instanceof WhereClauseBinary) {
			WhereClauseBinary wherebin = (WhereClauseBinary) clause;
			Object o = wherebin.getRightOperand();
			fill(stat, type, counter, o);
			counter++;
		}
		return counter;
	}
	
	/**
	 * Special fill procedure for select (has to differentiate between the tables if there are more than one)
	 * @param from the tables where shall be selected from
	 * @param stat the statement to be filled
	 * @param where list of where clauses responsible for the data
	 * @throws SQLException
	 * @throws DatabaseException 
	 */
	 public void fillValuesSelect(List<String> from, PreparedStatement stat, List<WhereClause> where) throws SQLException, DatabaseException {
			int counter = 1;
			for (WhereClause clause : where) {
				String table = findTable(from, clause.getLeftOperand());
				counter = fillWhereClause(table, clause, counter, stat);
			}
			
		}
	 
	//select the table to the given column from the list
	private String findTable(List<String> from, String column) {
		if (column.contains(".")) {
			// Alias as identificator
			String alias = column.substring(0, column.indexOf("."));
			
			for (String f : from) {
				if (f.equals(alias)) {
					return f;
				}
			}
		} else {
			// all possible relations must be searched.
			for (String f : from) {
				if (scheme.containsColumn(f, column)) {
					return f;
				}
			}
			
		}
		
		throw new IllegalQueryException("Tables or Column " + column + " is not part of this database!\n");
		
	}	
	
	//sets the statement for one single Entry
	private void fill(PreparedStatement stat, Type type, int counter, Object o) throws SQLException {
		// Null
		if (o == null || o.equals(RemoteDBConstants.NULL)) {
			int sqlType = 0;
			switch (type) {
			case Integer:
				sqlType = java.sql.Types.INTEGER;
				break;
			case Double:
				sqlType = java.sql.Types.DOUBLE;
				break;
			case Date:
				sqlType = java.sql.Types.DATE;
				break;
			case String:
				sqlType = java.sql.Types.VARCHAR;
				break;
			case Boolean:
				sqlType = java.sql.Types.BIT;
				break;
			default:
				String msg = "Unknown column type detected " + type;
				logger.log(Level.INFO, msg);
				throw new UnknownTypeException(msg);
			}
			stat.setNull(counter, sqlType);
		} else {
			//may contain ' so remove them!
			String stringvalue = o.toString();
			stringvalue = stringvalue.replace("'", "");
			switch (type) {
			case Integer:
				int value = Integer.valueOf(stringvalue);
				stat.setInt(counter, value);
				break;
			case Boolean:
				boolean valueBoolean = Boolean.valueOf(stringvalue);
				stat.setBoolean(counter, valueBoolean);
				break;
			case Double:
				double doubleValue = Double.valueOf(stringvalue);
				stat.setDouble(counter, doubleValue); 
				break;
			case Date:
				String dateStringClean;
				if ((stringvalue.charAt(0) == '\'') && (stringvalue.charAt(stringvalue.length() - 1) == '\'')) {
					dateStringClean = stringvalue.substring(1, stringvalue.length() - 1);
				} else {
					dateStringClean = stringvalue;
				}
				Date date = Date.valueOf(dateStringClean);
				stat.setDate(counter, date);
				break;
			case String:
				stat.setString(counter, stringvalue);
				break;
			default:
				String msg = "Unknown column type detected " + type +"! \n";
				logger.log(Level.INFO, msg);
				throw new UnknownTypeException(msg);
			}
		}
	}
	
	/**
	 * Special procedure for updates (updatedata is inserted through the whereclauses here)
	 * @param stat the Statement which shall be filled
	 * @param table the table to be updated (needed for determination of the types
	 * @param updateData the data which shall be inserted
	 * @param where whereClause determining the update conditions
	 * @throws SQLException
	 * @throws DatabaseException 
	 */
	public void fillValuesUpdate(PreparedStatement stat, String table, Map<String, Object> updateData, List<WhereClause> where) throws SQLException, DatabaseException {
		// Only in the where-Clause, null must be skipped, as "is null" is just inserted.
		ArrayList<WhereClause> wherenew = new ArrayList<WhereClause>();
		wherenew.addAll(where);
		Object[] keys = updateData.keySet().toArray();
		for(int i = 0; i < keys.length; i++) {
		wherenew.add(i, new WhereClauseBinary((String)keys[i], updateData.get((String)keys[i]), null));
		}
		fillValues(table, stat, wherenew);
	}
}