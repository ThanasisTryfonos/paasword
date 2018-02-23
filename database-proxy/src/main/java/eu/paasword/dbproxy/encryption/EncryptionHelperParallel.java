package eu.paasword.dbproxy.encryption;

import eu.paasword.dbproxy.utils.QueryLexer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



/**
 * Encryption class which parallizes the encryption of several rows
 * @author Mark Brenner
 *
 */
public class EncryptionHelperParallel extends EncryptionHelperBase {
	int cores;
	ConcurrentLinkedQueue<ArrayList<String>> resultQueueData;
	ConcurrentLinkedQueue<String> resultQueueKeys;
	QueryLexer lexer = QueryLexer.getInstance();
	DataDecryptor[] dataDecryptors;
	KeyDecryptor[] keyDecryptors;
	
	//Backupencryptor
	Encryption encrypt = new EncryptionHelperBase(null);
 	public EncryptionHelperParallel() {
		super(null);
		cores = Runtime.getRuntime().availableProcessors();
		dataDecryptors = new DataDecryptor[cores];
		keyDecryptors = new KeyDecryptor[cores];
		for(int i = 0; i < cores; i++) {
			dataDecryptors[i] = new DataDecryptor(new EncryptionHelperBase(null), i);
			keyDecryptors[i] = new KeyDecryptor(new EncryptionHelperBase(null), i);
		}
	}
	/**
	 * Method to decode multiple data-rows (from the data table)  in parallel. Runs only in parallel for more than 30 rows since the 
	 * overhead of parallization is to huge.
	 * @param encryptedRows the rows of data to be decrypted
	 * @return
	 */
	public ArrayList<ArrayList<String>> decodeMultipleData(List<String> encryptedRows) {
		if(encryptedRows.size() > 30) {
		ExecutorService executor = Executors.newCachedThreadPool();
		resultQueueData = new ConcurrentLinkedQueue<ArrayList<String>>();
		for(int i = 0; i < cores; i++) {
			dataDecryptors[i].setData(encryptedRows);
			executor.execute(dataDecryptors[i]);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new ArrayList<ArrayList<String>>(resultQueueData);
		} else {
			ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
			for(String s : encryptedRows) {
				s = encrypt.decrypt(s);
				result.add(lexer.splitDecryptedString(s));
			}
			return result;
		}
	}
	
	/**
	 *  Method to decode multiple key-rows (from the index tables)  in parallel. Runs only in parallel for more than 30 rows since the 
	 *  overhead of parallization is to huge.
	 * @param encryptedRows the rows of keys to be decrypted
	 * @return
	 */
	public ArrayList<String> decodeMultipleKeys(List<String> encryptedRows) {
		if(encryptedRows.size() > 30) {
			resultQueueKeys = new ConcurrentLinkedQueue<String>();
			ExecutorService executor = Executors.newCachedThreadPool();
			for(int i = 0; i < cores; i++) {
				keyDecryptors[i].setData(encryptedRows);
				executor.execute(keyDecryptors[i]);
			}
			executor.shutdown();
			try {
				executor.awaitTermination(1000, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return new ArrayList<String>(resultQueueKeys);
		} else {
			ArrayList<String> result = new ArrayList<String>();
			for(String s : encryptedRows) {
				s = encrypt.decrypt(s);
				result.addAll(lexer.splitDecryptedString(s));
			}
			return result;
		}
	}

/***
 * Abstract class which holds the data for the encryption and decryption
 * @author Mark Brenner
 *
 */
abstract class Cryptor implements Runnable {
	protected List<String> encryptedStrings;
	protected Encryption encryption;
	protected int id;
	
	protected void setData(List<String> encrypted) {
		encryptedStrings = encrypted;
	}
	protected Cryptor(Encryption encrypt, int number) {
		encryption = encrypt;
		id = number;
	}
}
/**
 * Concrete implementation which runs the encryption of the data rows in parallel which are hold in the super class
 * Each Thread ges assigend one of these 
 * @author Mark Brenner
 *
 */
class DataDecryptor extends Cryptor {
	protected DataDecryptor(Encryption encrypt, int number) {
		super(encrypt, number);
	}
	@Override
	public void run() {
		for(int i = id; i < encryptedStrings.size(); i = i + cores) {
			resultQueueData.add(lexer.splitDecryptedString(encryption.decrypt(encryptedStrings.get(i))));
		}
	}
	
}

/**
* Concrete implementation which runs the encryption of the key-rows in parallel which are hold in the super class
* Each Thread ges assigend one of these 
* @author Mark Brenner
*
*/
class KeyDecryptor extends Cryptor {
	protected KeyDecryptor(Encryption encrypt, int number) {
		super(encrypt, number);
	}
	@Override
	public void run() {
		for(int i = id; i < encryptedStrings.size(); i = i + cores) {
			ArrayList<String> keys = lexer.splitDecryptedString(encryption.decrypt(encryptedStrings.get(i)));
			for(String key : keys) {
				resultQueueKeys.add(key);
			}
		}
	}
}
}

