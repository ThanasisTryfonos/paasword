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
import eu.paasword.dbproxy.database.RemoteDBHelper;
import eu.paasword.dbproxy.database.utils.*;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class which implements the Administration of the Bucketindex
 * 
 * @author Mark Brenner
 *
 */
public class IndexBucketAdministration extends IndexTableAdministration {
	
	private int bucketSize;
	private Encryption encrypt;
	private Logger logger = Logger
			.getLogger("prototype.database.IndexBucketAdministration");
	private final String BUCKET_PRIMKEY = "id";
	private final String BUCKET_FIELDID = "field_id";
	private final String BUCKET_KEY = "key";
	public static final String BUCKET_BUCKETID = "bucket_id";
	public static final String BUCKET_TABLE = "bucket_nodes";
	public static final String BUCKET_INTERVALSTART = "interval_start";
	public static final String BUCKET_VALUE = "value";
	public static final String BUCKET_INDEX = "index_for";

	public IndexBucketAdministration(Map<String, Database> relationDB,RemoteDBHelper remoteHelper, String DBName, Integer bucketsize,Encryption encryptor) {
		super(relationDB, remoteHelper, DBName);
		bucketSize = bucketsize;
		encrypt = encryptor;

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.selectKeys()
	 */
	@Override
	public List<String> selectKeys(String from, List<WhereClause> where,
								   List<String> keysToRetain, boolean or, String sessionid) throws DatabaseException {

		// remove alias from map, joins could not occur because only one table
		// is processed.
		ArrayList<WhereClause> newWhere = new ArrayList<WhereClause>();
		for (WhereClause clause : where) {
			String key = clause.getLeftOperand();
			String newKey = key.substring(key.lastIndexOf(".") + 1,
					key.length());
			if (clause instanceof WhereClauseBinary) {
				WhereClauseBinary bin = (WhereClauseBinary) clause;
				newWhere.add(new WhereClauseBinary(newKey, bin
						.getRightOperand(), bin.getOperator())); // Only one
																	// value
																	// operand
																	// used
			} else if (clause instanceof WhereClauseIn) {
				WhereClauseIn in = (WhereClauseIn) clause;
				if(in instanceof WhereClauseNotIn) {
					newWhere.add(new WhereClauseNotIn(newKey, in.getIn()));
				} else {
					newWhere.add(new WhereClauseIn(newKey, in.getIn()));
				}
			}
		}

		// For each entry in where, the rows must be selected
		ArrayList<String> allPossibleKeys = new ArrayList<String>();
		
		for (WhereClause clause : newWhere) {
			ArrayList<String> keysOfWhereClause = new ArrayList<String>();
			// select keys of condition
			ArrayList<WhereClause> singleWhere = new ArrayList<WhereClause>();
			singleWhere.add(clause);
			ResultSet res = helper.selectSingleData(from, singleWhere,
					clause.getLeftOperand(),sessionid);
			Column column = helper.getColumnFromName(from,
					clause.getLeftOperand());
			String indexTableName = column.getType().toString().toLowerCase()
					+ "Index";
			String remoteTable = remoteDBName + "." + indexTableName;
			Database indexDB = relationToDB.get(remoteTable);
			try {
				while (res.next()) {
					Bucket bucket = new Bucket(res.getInt(BUCKET_BUCKETID),
							bucketSize, encrypt, indexDB, indexTableName, true,sessionid);
					if (keysToRetain.isEmpty()) {
						keysOfWhereClause.addAll(bucket.getAllKeys(sessionid));
					} else {
						keysOfWhereClause.addAll(bucket.getKeys(keysToRetain,sessionid));
					}
				}
			} catch (SQLException e) {
				String msg = "Index table could not be accessed! \n";
				logger.log(Level.SEVERE, msg, e);
				throw new DatabaseException(msg, e);
			}
			if(or || allPossibleKeys.isEmpty()) {
				allPossibleKeys.addAll(keysOfWhereClause);
			} else {
				allPossibleKeys.retainAll(keysOfWhereClause);
			}
		}
		return allPossibleKeys;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.removeKeys()
	 */
	@Override
	protected void removeKeysFromIndex(List<String> allkeys,List<Integer> IndexentriesToDelete, List<WhereClause> where,String indexTableName, Database indexDB, String sessionid) throws SQLException,
			DatabaseException {
		List<Integer> bucketIDs = getBucketIDsFromIndex(indexTableName,indexDB, where, sessionid);
		if (!bucketIDs.isEmpty()) {
			for (int id : bucketIDs) {
				Bucket bucket = new Bucket(id, bucketSize, encrypt, indexDB,
						indexTableName, true,sessionid);
				if (bucket.removeKeys(allkeys,sessionid)) {
					IndexentriesToDelete.add(id);
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.getKeysFromIndex()
	 */
	@Override
	protected List<String> getKeysFromIndex(ResultSet res,String indexTableName, Database indexDB, String sessionid) throws SQLException,
			DatabaseException {
		ArrayList<String> keys = new ArrayList<String>();
		while (res.next()) {
			int bucket_id = res.getInt(BUCKET_BUCKETID);
			Bucket bucket = new Bucket(bucket_id, bucketSize, encrypt, indexDB,indexTableName, true,sessionid);
			keys.addAll(bucket.getAllKeys(sessionid));
		}
		return keys;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.deleteFromIndex()
	 */
	@Override
	protected void deleteFromIndex(String indexTable, List<WhereClause> where,Database remoteDB, String sessionid) throws DatabaseException, SQLException {
		ArrayList<Object> bucketIDs = new ArrayList<Object>();
		Vector<String> columnsSelect = new Vector<String>();
		columnsSelect.add(BUCKET_BUCKETID);
		columnsSelect.add(BUCKET_PRIMKEY);
		Vector<String> from = new Vector<String>();
		from.add(indexTable);
		ResultSet res = remoteDB.select(columnsSelect, from, where, false,null, true, sessionid);
		while (res.next()) {
			bucketIDs.add(res.getInt(BUCKET_BUCKETID));
		}
		if (!bucketIDs.isEmpty()) {
			// IndexTable
			ArrayList<WhereClause> whereIndex = new ArrayList<WhereClause>();
			whereIndex.add(new WhereClauseIn(BUCKET_BUCKETID, bucketIDs));
			remoteDB.delete(indexTable, whereIndex,sessionid);
			// Bucket itself
			whereIndex
					.add(new WhereClauseBinary(BUCKET_INDEX, indexTable, "="));
			remoteDB.delete(BUCKET_TABLE, whereIndex,sessionid);
		} else {
			logger.log(Level.INFO, "Nothing to delete from Index!\n");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.deleteUnnessaryIndexEntries()
	 */
	@Override
	protected void deleteUnnessaryIndexEntries(List<Integer> primKeys,String indexTableName, Database indexDB,String sessionid) throws DatabaseException {
		if (!primKeys.isEmpty()) {
			// Delete Buckets
			ArrayList<WhereClause> whereIndex = new ArrayList<WhereClause>();
			whereIndex.add(new WhereClauseIn(BUCKET_BUCKETID,new ArrayList<Object>(primKeys)));
			indexDB.delete(indexTableName, whereIndex,sessionid);
			// Delete IndexEntries
			whereIndex.add(new WhereClauseBinary(BUCKET_INDEX, indexTableName,
					"="));
			indexDB.delete(BUCKET_TABLE, whereIndex,sessionid);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.insertNewValueIntoIndex()
	 */
	@Override
	protected void insertNewValueIntoIndex(ResultSet res, Database indexDB,List<String> keys, Object columnValue, int fieldID,String indexTableName,String sessionid) throws SQLException, DatabaseException {
		if (res.next()) {
			int bucketID = res.getInt(BUCKET_BUCKETID);
			Bucket bucket = new Bucket(bucketID, bucketSize, encrypt, indexDB,indexTableName, false,sessionid);
			bucket.addKeys(keys,sessionid);
		} else {

			Vector<String> columnsInsert = new Vector<String>();
			columnsInsert.add(BUCKET_PRIMKEY);
			columnsInsert.add(BUCKET_FIELDID);
			columnsInsert.add(BUCKET_KEY);
			columnsInsert.add(BUCKET_BUCKETID);

			Vector<Object> valuesInsert = new Vector<Object>();
			int id = 0;
			if (tableMaxID.containsKey(indexTableName)) {
				id = tableMaxID.get(indexTableName) + 1;
			} else {
				id = helper.selectMaxIDPlusOne(indexTableName, indexDB,BUCKET_PRIMKEY,sessionid);
			}
			valuesInsert.add(id);
			valuesInsert.add(fieldID);
			if (columnValue != null && columnValue.equals("null")) {
				valuesInsert.add("'null'");
			} else {
				valuesInsert.add(columnValue);
			}
			int bucketID = helper.selectMaxIDPlusOne(indexTableName, indexDB,BUCKET_BUCKETID,sessionid);
			valuesInsert.add(bucketID);
			indexDB.insert(indexTableName, columnsInsert, valuesInsert, false,sessionid);
			Bucket bucket = new Bucket(bucketID, bucketSize, encrypt, indexDB,indexTableName, false,sessionid);
			bucket.addKeys(keys,sessionid);
		}

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.database.index.IndexTableAdministration.updateIndexEntries()
	 */
	@Override
	protected void updateIndexEntries(ResultSet res,List<Integer> IndexentriesToDelete, List<String> allkeys,Entry<String, Object> entry, String indexTableName, Database indexDB, String sessionid)
			throws SQLException, DatabaseException {
		ArrayList<Integer> bucketIDs = new ArrayList<Integer>();
		while (res.next()) {
			Object value = res.getObject(3); // Improves performance 3 =
												// Index_Key
			// if the select value is null then it can not be in the update
			// data as it would have been excluded before
			if (entry.getValue() == null || value == null
					|| !value.equals(entry.getValue())) {
				bucketIDs.add(res.getInt(BUCKET_BUCKETID));
			}
		}
		if (!bucketIDs.isEmpty()) {
			for (int id : bucketIDs) {
				Bucket bucket = new Bucket(id, bucketSize, encrypt, indexDB,indexTableName, true, sessionid);
				if (bucket.removeKeys(allkeys,sessionid)) {
					IndexentriesToDelete.add(id);
				}
			}
		}
	}

	// Collect the ids of the buckets fullfilling the whereClauses
	private List<Integer> getBucketIDsFromIndex(String indexTableName,Database indexDB, List<WhereClause> where, String sessionid)throws DatabaseException, SQLException {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		Vector<String> select = new Vector<String>();
		select.add(BUCKET_BUCKETID);
		Vector<String> from = new Vector<String>();
		from.add(indexTableName);
		ResultSet res = indexDB.select(select, from, where, false, null, true,sessionid);
		while (res.next()) {
			ids.add(res.getInt(BUCKET_BUCKETID));
		}
		return ids;
	}
}
