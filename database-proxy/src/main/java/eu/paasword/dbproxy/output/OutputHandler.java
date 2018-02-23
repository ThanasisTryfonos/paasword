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
package eu.paasword.dbproxy.output;

import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Encapsulates methods to represent a SQL result in various manners, for example as string.
 * 
 * @author Yvonne Muelle
 * 
 */
public class OutputHandler {
	private static final String sepOut = "+";
	private ArrayList<ResultSet> result;

	/**
	 * Constructs a new object of this class
	 * 
	 * @param result
	 *            SQL result
	 */
	public OutputHandler(ArrayList<ResultSet> result) {
		this.result = result;
	}
	
	/**
	 * Returns the result Set
	 * 
	 */
	public ArrayList<ResultSet> getResultSet() {
		return this.result;
	}

	/**
	 * Returns the query result as string, separated by "," and each tuple in the result on a new line.
	 * 
	 * @return result as string
	 * @throws DatabaseException
	 *             If the query result could not be accessed without errors.
	 */
	public String resultAsString() throws DatabaseException {
		StringBuilder builder = new StringBuilder();

		for (ResultSet rs : result) {

			ResultSetMetaData rsMetaData;
			try {
				rsMetaData = rs.getMetaData();
				int numberOfColumns = rsMetaData.getColumnCount();

				while (rs.next()) {
					for (int i = 1; i <= numberOfColumns; i++) {
						builder.append(rs.getString(i)).append(sepOut);
					}
					builder.deleteCharAt(builder.length() - 1);
					builder.append("\n");
				}
			} catch (SQLException e) {
				throw new DatabaseException(e);
			}

			builder.append("============================\n");

		}

		return builder.toString();
	}

}
