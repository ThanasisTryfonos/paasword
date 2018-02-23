package eu.paasword.dbproxy.database.index;

import eu.paasword.dbproxy.database.Database;
import eu.paasword.dbproxy.database.utils.WhereClause;
import eu.paasword.dbproxy.database.utils.WhereClauseBinary;
import eu.paasword.dbproxy.database.utils.WhereClauseIn;
import eu.paasword.dbproxy.encryption.Encryption;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.utils.QueryLexer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class which reprsents a Bucket of the Bucket-table. This class performans the acctual changes in the Buckets
 * @author Mark Brenner
 *
 */
public class Bucket {
	
	private int bucketId;
	private int bucketSize;
	private Encryption encrypt;
	private Database indexDB;
	private QueryLexer lexer = QueryLexer.getInstance();
	private String indexTableName;
	private Map<Integer, String> cache;
	private boolean cached;

	/**
	 * Constructs a Bucket from the database
	 * @param id the id of the bucket to create
	 * @param size the bucketsize 
	 * @param encryptor the encryption module used for the buckets
	 * @param remoteDB the database where the bucket table is stored 
	 * @param indexName the index table the bucket belongs to
	 * @param enableCache option to cache the whole bucket on the Adapter (avoids too many network queries)
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Bucket(int id, int size, Encryption encryptor, Database remoteDB,
			String indexName, boolean enableCache,String sessionid) throws DatabaseException,
			SQLException {
		bucketId = id;
		bucketSize = size;
		encrypt = encryptor;
		indexDB = remoteDB;
		indexTableName = indexName;
		cached = enableCache;
		if (cached) {
			load(sessionid);
		}
	}

	//loads the bucket from the database if cache is enabled
	private void load(String sessionid) throws DatabaseException, SQLException {
		cache = new HashMap<Integer, String>();
		List<String> select = new ArrayList<String>();
		select.add("*");
		List<String> from = new ArrayList<String>();
		from.add(IndexBucketAdministration.BUCKET_TABLE);
		ArrayList<WhereClause> where = new ArrayList<WhereClause>();
		where.add(new WhereClauseBinary(
				IndexBucketAdministration.BUCKET_BUCKETID, bucketId, "="));
		where.add(new WhereClauseBinary(IndexBucketAdministration.BUCKET_INDEX,
				indexTableName, "="));
		ResultSet res = indexDB.select(select, from, where, false, null, false,sessionid);
		while (res.next()) {
			cache.put(
					res.getInt(IndexBucketAdministration.BUCKET_INTERVALSTART),
					res.getString(IndexBucketAdministration.BUCKET_VALUE));
		}
	}
	
	/**
	 * 
	 * @return all Keys from this bucket
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public ArrayList<String> getAllKeys( String sessionid) throws DatabaseException,
			SQLException {
		ArrayList<String> allkeys = new ArrayList<String>();
		if (cached) {
			for (String encrypted : cache.values()) {
				String decrypted = encrypt.decrypt(encrypted);
				allkeys.addAll(lexer.splitDecryptedString(decrypted));
			}
		} else {
			List<String> select = new ArrayList<String>();
			select.add("*");
			List<String> from = new ArrayList<String>();
			from.add(IndexBucketAdministration.BUCKET_TABLE);
			ArrayList<WhereClause> where = new ArrayList<WhereClause>();
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_BUCKETID, bucketId, "="));
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_INDEX, indexTableName, "="));
			ResultSet res = indexDB.select(select, from, where, false, null,true, sessionid);
			while (res.next()) {
				String decrypted = encrypt.decrypt(res
						.getString(IndexBucketAdministration.BUCKET_VALUE));
				allkeys.addAll(lexer.splitDecryptedString(decrypted));
			}
		}
		return allkeys;
	}

	/**
	 * Adds Keys to the bucket 
	 * @param keys the List of keys to add
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public void addKeys(List<String> keys, String sessionid) throws DatabaseException,
			SQLException {
		HashMap<Integer, ArrayList<String>> AddCache = new HashMap<Integer, ArrayList<String>>();
		for (String key : keys) {
			int start = getIntervalStart(key);
			if (AddCache.containsKey(start)) {
				ArrayList<String> row = AddCache.get(start);
				if (!row.contains(key)) {
					row.add(key);
					AddCache.put(start, row);
				}
			} else {
				List<String> select = new ArrayList<String>();
				select.add("*");
				List<String> from = new ArrayList<String>();
				from.add(IndexBucketAdministration.BUCKET_TABLE);
				ArrayList<WhereClause> where = new ArrayList<WhereClause>();
				where.add(new WhereClauseBinary(
						IndexBucketAdministration.BUCKET_BUCKETID, bucketId,
						"="));
				where.add(new WhereClauseBinary(
						IndexBucketAdministration.BUCKET_INDEX, indexTableName,
						"="));
				where.add(new WhereClauseBinary(
						IndexBucketAdministration.BUCKET_INTERVALSTART, start,
						"="));
				ResultSet res = indexDB.select(select, from, where, false,null, true, sessionid);
				// If Interval exists
				if (res.next()) {
					String encryptedKeys = res
							.getString(IndexBucketAdministration.BUCKET_VALUE);
					String decryptedKeys = encrypt.decrypt(encryptedKeys);
					ArrayList<String> keysList = lexer
							.splitDecryptedString(decryptedKeys);
					if (!keysList.contains(key)) {
						keysList.add(key);
						AddCache.put(start, keysList);
					}
					// Create new intervalentry
				} else {
					List<String> columns = new ArrayList<String>();
					columns.add(IndexBucketAdministration.BUCKET_BUCKETID);
					columns.add(IndexBucketAdministration.BUCKET_INTERVALSTART);
					columns.add(IndexBucketAdministration.BUCKET_VALUE);
					columns.add(IndexBucketAdministration.BUCKET_INDEX);
					List<Object> values = new ArrayList<Object>();
					values.add(bucketId);
					values.add(start);
					values.add(encrypt.encrypt(key));
					values.add(indexTableName);
					indexDB.insert(IndexBucketAdministration.BUCKET_TABLE,columns, values, true,sessionid);
					ArrayList<String> keyList = new ArrayList<String>();
					keyList.add(key);
					AddCache.put(start, keyList);
				}
			}
		}
		for (Integer start : AddCache.keySet()) {
			ArrayList<WhereClause> where = new ArrayList<WhereClause>();
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_BUCKETID, bucketId, "="));
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_INDEX, indexTableName, "="));
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_INTERVALSTART, start, "="));
			ArrayList<String> keysList = AddCache.get(start);
			String updatedKeys = encrypt.encrypt(lexer
					.joinDecryptedString(keysList));
			HashMap<String, Object> updateData = new HashMap<String, Object>();
			updateData.put(IndexBucketAdministration.BUCKET_VALUE, updatedKeys);
			indexDB.update(IndexBucketAdministration.BUCKET_TABLE, updateData,where, true,sessionid);
		}
	}

	/**
	 * Removes keys from the bucket
	 * @param keys the keys to remove
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public boolean removeKeys(List<String> keys, String sessionid) throws DatabaseException,
			SQLException {
		if (cached) {
			boolean changed = false;
			ArrayList<Object> bucketRowsToDelete = new ArrayList<Object>();
			for (String key : keys) {
				int start = getIntervalStart(key);
				if (cache.containsKey(start)) {
					String encryptedkeys = cache.get(start);
					ArrayList<String> keysList = lexer
							.splitDecryptedString(encrypt
									.decrypt(encryptedkeys));
					if (keysList.contains(key)) {
						keysList.remove(key);
						if (keysList.isEmpty()) {
							cache.remove(start);
							bucketRowsToDelete.add(start);
						} else {
							cache.put(start, encrypt.encrypt(lexer
									.joinDecryptedString(keysList)));
						}
						changed = true;
					}
				}
			}
			if (changed && !cache.isEmpty()) {
				persistCache(bucketRowsToDelete, sessionid);
			}
		} else {
			for (String key : keys) {
				int start = getIntervalStart(key);
				List<String> select = new ArrayList<String>();
				select.add("*");
				List<String> from = new ArrayList<String>();
				from.add(IndexBucketAdministration.BUCKET_TABLE);
				ArrayList<WhereClause> where = new ArrayList<WhereClause>();
				where.add(new WhereClauseBinary(
						IndexBucketAdministration.BUCKET_BUCKETID, bucketId,
						"="));
				where.add(new WhereClauseBinary(
						IndexBucketAdministration.BUCKET_INDEX, indexTableName,
						"="));
				where.add(new WhereClauseBinary(
						IndexBucketAdministration.BUCKET_INTERVALSTART, start,
						"="));
				ResultSet res = indexDB.select(select, from, where, false,null, true, sessionid);
				while (res.next()) {
					String decryptedKeys = encrypt.decrypt(res
							.getString(IndexBucketAdministration.BUCKET_VALUE));
					ArrayList<String> keysList = lexer
							.splitDecryptedString(decryptedKeys);
					int sizebefore = keysList.size();
					keysList.remove(key);
					int sizeafter = keysList.size();
					// Delete Row if it is empty
					if (keysList.isEmpty()) {
						res.deleteRow();
					} else if (sizebefore != sizeafter) {
						String cryptedKeys = encrypt.encrypt(lexer
								.joinDecryptedString(keysList));
						res.updateString(
								IndexBucketAdministration.BUCKET_VALUE,
								cryptedKeys);
						res.updateRow();
					}
				}
			}
		}
		// Check whether Bucket is empty and can be removed
		return checkEmpty(sessionid);
	}

	/**
	 * @param keysToRetain the keys which shall be found 
	 * @return all or a part of the given keys if they are in the bucket 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public List<String> getKeys(List<String> keysToRetain, String sessionid)
			throws DatabaseException, SQLException {
		if (keysToRetain.isEmpty()) {
			return getAllKeys(sessionid);
		} else {
			ArrayList<String> allKeys = new ArrayList<String>();
			if (cached) {
				for (String key : keysToRetain) {
					int start = getIntervalStart(key);
					String encrypted = cache.get(start);
					if (encrypted != null) {
						String decryptedKeys = encrypt.decrypt(encrypted);
						allKeys.addAll(lexer
								.splitDecryptedString(decryptedKeys));
						allKeys.retainAll(keysToRetain);
					}
				}
			} else {
				for (String key : keysToRetain) {
					int start = getIntervalStart(key);
					List<String> select = new ArrayList<String>();
					select.add("*");
					List<String> from = new ArrayList<String>();
					from.add(IndexBucketAdministration.BUCKET_TABLE);
					ArrayList<WhereClause> where = new ArrayList<WhereClause>();
					where.add(new WhereClauseBinary(
							IndexBucketAdministration.BUCKET_BUCKETID,
							bucketId, "="));
					where.add(new WhereClauseBinary(
							IndexBucketAdministration.BUCKET_INDEX,
							indexTableName, "="));
					where.add(new WhereClauseBinary(
							IndexBucketAdministration.BUCKET_INTERVALSTART,
							start, "="));
					ResultSet res = indexDB.select(select, from, where, false,
							null, true, sessionid);
					while (res.next()) {
						String decryptedKeys = encrypt
								.decrypt(res
										.getString(IndexBucketAdministration.BUCKET_VALUE));
						allKeys.addAll(lexer
								.splitDecryptedString(decryptedKeys));
						allKeys.retainAll(keysToRetain);
					}
				}
			}
			return allKeys;
		}
	}

	//Saves the caches Bucket to the Database 
	private void persistCache(ArrayList<Object> bucketRowsToDelete, String sessionid)
			throws DatabaseException {
		for (Integer start : cache.keySet()) {
			String encryptedKeys = cache.get(start);
			ArrayList<WhereClause> where = new ArrayList<WhereClause>();
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_BUCKETID, bucketId, "="));
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_INDEX, indexTableName, "="));
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_INTERVALSTART, start, "="));
			HashMap<String, Object> updateData = new HashMap<String, Object>();
			updateData.put(IndexBucketAdministration.BUCKET_VALUE,
					encryptedKeys);
			indexDB.update(IndexBucketAdministration.BUCKET_TABLE, updateData,where, true,sessionid);
		}
		if (!bucketRowsToDelete.isEmpty()) {
			ArrayList<WhereClause> where = new ArrayList<WhereClause>();
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_BUCKETID, bucketId, "="));
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_INDEX, indexTableName, "="));
			where.add(new WhereClauseIn(
					IndexBucketAdministration.BUCKET_INTERVALSTART,
					bucketRowsToDelete));
			indexDB.delete(IndexBucketAdministration.BUCKET_TABLE, where, sessionid);
		}
	}

	//Calculate the interval the given key belongs to
	private int getIntervalStart(String Stringkey) {
		int key = Integer.parseInt(Stringkey);
		return (key / bucketSize) * bucketSize + 1;
	}
	
	//Checks whether the bucket is empty and can be removed
	private boolean checkEmpty(String sessionid) throws DatabaseException, SQLException {
		if (cached) {
			return cache.isEmpty();
		} else {
			List<String> select = new ArrayList<String>();
			select.add("*");
			List<String> from = new ArrayList<String>();
			from.add(IndexBucketAdministration.BUCKET_TABLE);
			ArrayList<WhereClause> where = new ArrayList<WhereClause>();
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_INDEX, indexTableName, "="));
			where.add(new WhereClauseBinary(
					IndexBucketAdministration.BUCKET_BUCKETID, bucketId, "="));
			ResultSet res = indexDB.select(select, from, where, false, null,true, sessionid);
			return !res.next();
		}
	}
}
