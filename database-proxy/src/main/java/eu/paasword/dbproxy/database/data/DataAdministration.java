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
package eu.paasword.dbproxy.database.data;

import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.util.List;



/**
 * This Class represents the administration of the table where the encrypted data rows are stored
 * @author Mark Brenner
 *
 */
public abstract class DataAdministration {
	
	/**
	 * Insert the data given by values into the encrypted data table
	 * @param columns the columns of the table the data shall be saved into
	 * @param values the values the data shall be saved into
	 * @param newID a new ID aka key for this row
	 * @param remoteDB the Java database object the data shall be saved to
	 * @param remoteTable the name of the table which contains the encrypted datarows
	 * @throws DatabaseException
	 */
	public abstract void insertOnlyData(List<String> columns,
			List<Object> values, int newID, Database remoteDB,
			String remoteTable,String sessionid) throws DatabaseException;
	
	/**
	 * Deletes all rows in the data table identified by the given keys
	 * @param dataIndicies the keys which identify the rows to delete
	 * @throws DatabaseException
	 */
	public abstract void deleteAllFromData(List<String> dataIndicies,String sessionid) throws DatabaseException;
	
	/**
	 * Selects and encrypts the rows identified by the given keys
	 * @param allKeys the keys which identify the rows to select
	 * @return the encrypted rows identified by the given keys
	 * @throws DatabaseException
	 */
	public abstract List<List<String>> selectData(List<String> allKeys,String sessionid) throws DatabaseException;
	
	/**
	 * Updates the rows stored entcrypted in the data table
	 * @param from the table the rows belong to
	 * @param where where clauses defining the rows to update
	 * @param attribute the values which shall be updated
	 * @param newValue the new values 
	 * @throws DatabaseException
	 */
	public abstract void updateDataValue(String from, List<WhereClause> where,
			String attribute, String newValue,String sessionid) throws DatabaseException;
}

