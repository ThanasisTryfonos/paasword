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
package eu.paasword.dbproxy.database.index;

import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.database.RemoteDBConstants;
import eu.paasword.dbproxy.database.RemoteDBHelper;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.database.utils.WhereClauseBinary;
import eu.paasword.dbproxy.database.utils.WhereClauseIn;
import eu.paasword.dbproxy.encryption.HMAC;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which represents the an Administration for a hashed index
 * @author Mark
 *
 */
public class IndexHashedAdministration extends IndexTableAdministration {
	private HMAC hmac;
	private static  final String HASH_HASH = "hashedkey";
	private static  final String HASH_VALUE = "value";
	private Logger logger = Logger.getLogger("prototype.database.IndexHashedAdministration");
	
	public IndexHashedAdministration(Map<String, Database> relationDB,RemoteDBHelper remoteHelper, String DBName, byte[] signingKey) {
		super(relationDB, remoteHelper, DBName);
		hmac = new HMAC(signingKey);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.removeKeysFromIndex()
	 */
	@Override
	protected void removeKeysFromIndex(List<String> allkeys,List<Integer> IndexentriesToDelete, List<WhereClause> where,String indexTableName, Database indexDB,String sessionid) throws SQLException,
			DatabaseException {
		//Collect ids and hashes from normal Index
		HashMap<String, Integer> hashesToPrimkeys = collectHashesAndIDsFromIndex(indexTableName, indexDB, where,sessionid);
		if(!hashesToPrimkeys.isEmpty()) {
		//Delete from Hashed Index
		deleteKeysFromHashIndex(hashesToPrimkeys, indexTableName, IndexentriesToDelete, indexDB, allkeys,sessionid);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.getKeysFromIndex()
	 */
	@Override
	protected List<String> getKeysFromIndex(ResultSet res,String indexTableName, Database indexDB,String sessionid) throws SQLException, DatabaseException {
		ArrayList<Object> hashes = new ArrayList<Object>();
		while(res.next()) {
			int id = res.getInt(RemoteDBConstants.INDEX_PRIMKEY);
			hashes.add(hmac.hash(String.valueOf(id)));
		}
		if(!hashes.isEmpty()){
		//Select all Columns of the Hashed table at once
		String indexHashTable = indexTableName + "ht";
		List<String> from = new ArrayList<String>();
		from.add(indexHashTable);
		List<String> columnsHash = new ArrayList<String>();
		columnsHash.add(HASH_VALUE);
		//Set the Hashes in the Wherein Clause
		ArrayList<WhereClause> where = new ArrayList<WhereClause>();
		where.add(new WhereClauseIn(HASH_HASH, hashes));
	    ResultSet resHash = indexDB.select(columnsHash, from, where, false, null, false,sessionid);
	    ArrayList<String> keys = new ArrayList<String>();
	    while(resHash.next()) {
	    	keys.addAll(lexer.splitDecryptedString(resHash.getString(HASH_VALUE)));
		}
	    if(!keys.isEmpty()) {
	    return keys;
	    }
		}
		logger.log(Level.INFO, "No Keys found for this conditions");
	    return new ArrayList<String>();
	}

	@Override
	protected void deleteFromIndex(String indexTable, List<WhereClause> where,Database remoteDB,String sessionid) throws DatabaseException, SQLException {
		ArrayList<Object> hashes = new ArrayList<Object>();
		List<String> columnsSelect = new ArrayList<String>();
		columnsSelect.add(RemoteDBConstants.INDEX_PRIMKEY);
		List<String> from = new ArrayList<String>();
		from.add(indexTable);
		ResultSet res = remoteDB.select(columnsSelect, from, where, false, null, true,sessionid);
		ArrayList<Object> in = new ArrayList<Object>();
		while(res.next()) {
			int id = res.getInt(RemoteDBConstants.INDEX_PRIMKEY);
			String hash = hmac.hash(String.valueOf(id));
			hashes.add(hash);
			in.add(id);
		}
		if(!hashes.isEmpty()) {
			//Delete from Index
			ArrayList<WhereClause> whereIndex = new ArrayList<WhereClause>();
			whereIndex.add(new WhereClauseIn(RemoteDBConstants.INDEX_PRIMKEY, in));
			remoteDB.delete(indexTable, whereIndex,sessionid);
			//Delete from Hashindex
			String indexHashTable = indexTable + "ht";
			ArrayList<WhereClause> whereHash = new ArrayList<WhereClause>();
			whereHash.add(new WhereClauseIn(HASH_HASH, hashes));
			remoteDB.delete(indexHashTable, whereHash,sessionid);
		} else {
			logger.log(Level.INFO, "Nothing to delete from Index!\n");
		}
	}

	@Override
	protected void deleteUnnessaryIndexEntries(List<Integer> primKeys,String indexTableName, Database indexDB,String sessionid) throws DatabaseException {
		if(!primKeys.isEmpty()) {
		//find the necessary Hashes
		ArrayList<String> hashes = new ArrayList<String>();
		for(int key : primKeys) {
			hashes.add(hmac.hash(String.valueOf(key)));
		}
		//Delete from normal index
		ArrayList<WhereClause> whereIndex = new ArrayList<WhereClause>();
		whereIndex.add(new WhereClauseIn(RemoteDBConstants.INDEX_PRIMKEY, new ArrayList<Object>(primKeys)));
		indexDB.delete(indexTableName, whereIndex,sessionid);
		//Delete from Hash index
		ArrayList<WhereClause> whereHash = new ArrayList<WhereClause>();
		whereHash.add(new WhereClauseIn(HASH_HASH, new ArrayList<Object>(hashes)));
		indexDB.delete(indexTableName + "ht", whereHash,sessionid);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.insertNewValueIntoIndex()
	 */
	@Override
	protected void insertNewValueIntoIndex(ResultSet res, Database indexDB,List<String> keys, Object columnValue, int fieldID,String indexTableName,String sessionid) throws SQLException, DatabaseException {
		String indexHashTable = indexTableName + "ht";
		List<String> columnsHash = new ArrayList<String>();
		columnsHash.add(HASH_HASH);
		columnsHash.add(HASH_VALUE);
		if (res.next()) {
			int id = res.getInt(RemoteDBConstants.INDEX_PRIMKEY);
			String hash = hmac.hash(String.valueOf(id));
			ArrayList<WhereClause> where = new ArrayList<WhereClause>();
			where.add(new WhereClauseBinary(HASH_HASH, hash, "="));
			List<String> from = new ArrayList<String>();
			from.add(indexHashTable);
			ResultSet selectresult = indexDB.select(columnsHash, from, where, false,null, true,sessionid);
			if(selectresult.next()) {
				String oldkeys = selectresult.getString(HASH_VALUE);
				String updatedkeys = oldkeys + lexer.DELIMITER + lexer.joinDecryptedString(keys);
				ArrayList<WhereClause> whereindex = new ArrayList<WhereClause>();
				whereindex.add(new WhereClauseBinary(HASH_HASH, hash, "="));
				HashMap<String, Object> updateData = new HashMap<String, Object>();
				updateData.put(RemoteDBConstants.INDEX_VALUE, updatedkeys);
				indexDB.update(indexHashTable, updateData, whereindex, true,sessionid);
			} else {
				throw new DatabaseException("Inconsisntence between Index and Hashedindex!\n");
			}
		} else {
			//Insert into actual IndexTable
			List<String> columnsInsert = new ArrayList<String>();
			columnsInsert.add(RemoteDBConstants.INDEX_PRIMKEY);
			columnsInsert.add(RemoteDBConstants.INDEX_FIELD_ID);
			columnsInsert.add(RemoteDBConstants.INDEX_KEY);
			columnsInsert.add(RemoteDBConstants.INDEX_VALUE);
			List<Object> valuesInsert = new ArrayList<Object>();
			int id = 0;
			if(tableMaxID.containsKey(indexTableName)) {
				id = tableMaxID.get(indexTableName) + 1;
			} else {
				id = helper.selectMaxIDPlusOne(indexTableName, indexDB,RemoteDBConstants.INDEX_PRIMKEY,sessionid);
			}
			tableMaxID.put(indexTableName, id);
			String hash = hmac.hash(String.valueOf(id));
			//Check whether the insertion would cause a hashcollision, if more than 2^160 Values are inserted into the DB this would cause an endless Loop!
			while(checkForHashCollision(hash, indexDB, indexHashTable,sessionid)) {
				id++;
				hash = hmac.hash(String.valueOf(id));
			}
			valuesInsert.add(id);
			valuesInsert.add(fieldID);
			if (columnValue != null && columnValue.equals("null")) {
				valuesInsert.add("'null'");
			} else {
				valuesInsert.add(columnValue);
			}
			valuesInsert.add("dummy");
			indexDB.insert(indexTableName, columnsInsert, valuesInsert, true,sessionid);
			//Insert into datatypeIndexHT table
			List<Object> valuesHash = new ArrayList<Object>();
			String indexInsert = lexer.joinDecryptedString(keys);
			valuesHash.add(hash);
			valuesHash.add(indexInsert);
			indexDB.insert(indexHashTable, columnsHash, valuesHash, true,sessionid);
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.updateIndexEntries()
	 */
	@Override
	protected void updateIndexEntries(ResultSet res,List<Integer> IndexentriesToDelete, List<String> allkeys,Entry<String, Object> entry, String indexTableName, Database indexDB,String sessionid)
			throws SQLException, DatabaseException {
		HashMap<String, Integer> hashesToPrimkeys = new HashMap<String, Integer>();
			while(res.next()) {
				Object value = res.getObject(3); //Improves performance 3 = Index_Key
				//if the selecte value is null then it can not be in the update data as it would have been excluded before
				if(entry.getValue() == null || value == null || !value.equals(entry.getValue())) {
					int id = res.getInt(RemoteDBConstants.INDEX_PRIMKEY);
					hashesToPrimkeys.put(hmac.hash(String.valueOf(id)), id);
				}
			}
			if(!hashesToPrimkeys.isEmpty()) {
				//Delete from Hashed Index
				deleteKeysFromHashIndex(hashesToPrimkeys, indexTableName, IndexentriesToDelete, indexDB, allkeys,sessionid);
			}
	}

	//Checks whether hash alreadey exist in Table 
	private boolean checkForHashCollision(String hash, Database indexdb, String indexHashTable, String sessionid ) throws SQLException, DatabaseException {
		List<String> select = new ArrayList<String>();
		select.add(HASH_HASH);
		ArrayList<WhereClause> where = new ArrayList<WhereClause>();
		where.add(new WhereClauseBinary(HASH_HASH, hash, "="));
		List<String> from = new ArrayList<String>();
		from.add(indexHashTable);
		if(indexdb.select(select, from, where,  false, null, false,sessionid).next()) {
			return true;
		}
		return false;
	}
	
	//Collect all Hashes and indices fullfilling the whereclause
	private HashMap<String, Integer> collectHashesAndIDsFromIndex(String indexTableName, Database indexDB, List<WhereClause> where,String sessionid) throws DatabaseException, SQLException {
		HashMap<String, Integer> hashesToPrimkeys = new HashMap<String, Integer>();
		List<String> select = new ArrayList<String>();
		select.add(RemoteDBConstants.INDEX_PRIMKEY);
		select.add(RemoteDBConstants.INDEX_VALUE);
		List<String> from = new ArrayList<String>();
		from.add(indexTableName);
		ResultSet res = indexDB.select(select,from, where, false, null, true,sessionid);
		while(res.next()) {
			int id = res.getInt(RemoteDBConstants.INDEX_PRIMKEY);
			String hash = hmac.hash(String.valueOf(id));		
			hashesToPrimkeys.put(hash, id);
		}
		return hashesToPrimkeys;
	}
	
	//Delete the Keys from the hashindex 
	private void deleteKeysFromHashIndex(Map<String, Integer> hashesToPrimkeys, String indexTableName, List<Integer>  IndexentriesToDelete, Database indexDB, List<String> allkeys, String sessionid) throws DatabaseException, SQLException {
		ArrayList<Object> whereList = new ArrayList<Object>(hashesToPrimkeys.keySet());
		ArrayList<WhereClause> whereHashes = new ArrayList<WhereClause>();
		whereHashes.add(new WhereClauseIn(HASH_HASH, whereList));
		List<String> select = new ArrayList<String>();
		select.add(HASH_HASH);
		select.add(HASH_VALUE);
		List<String> from = new ArrayList<String>();
		from.add(indexTableName + "ht");
		ResultSet resHash = indexDB.select(select, from, whereHashes, false, null, true,sessionid);
		while(resHash.next()) {
			String storedKeys = resHash.getString(HASH_VALUE);
			ArrayList<String> keys = lexer.splitDecryptedString(storedKeys);
			int before = keys.size();
			keys.removeAll(allkeys);
			int after = keys.size();
			String hash = resHash.getString(HASH_HASH);
			if(keys.isEmpty()) {
			  int id = hashesToPrimkeys.get(hash);
			  IndexentriesToDelete.add(id);
			} else {
			  if(before != after) {
			  storedKeys = lexer.joinDecryptedString(keys);
				ArrayList<WhereClause> where = new ArrayList<WhereClause>();
				where.add(new WhereClauseBinary(HASH_HASH, hash , "="));
				HashMap<String, Object> updateData = new HashMap<String, Object>();
				updateData.put(HASH_VALUE, storedKeys);
				indexDB.update(indexTableName + "ht", updateData, where, true,sessionid);
			  }
			}
		}
	}
}//EoC
