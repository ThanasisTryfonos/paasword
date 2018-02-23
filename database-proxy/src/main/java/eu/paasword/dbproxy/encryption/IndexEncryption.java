/**
 * 
 */
package eu.paasword.dbproxy.encryption;

import java.io.IOException;
import java.security.Key;
import java.sql.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import eu.paasword.dbproxy.database.utils.Type;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @author kateryna
 *
 */
public class IndexEncryption {
	
	private final String ALGORITHM;
	private final String ALGORITHM_WITH_PARAMS;
	private final byte[] keyValue;
	private final Key key;
	private Cipher encrypter = null;
	private Cipher decrypter = null;
	private final BASE64Encoder encoder;
	private final BASE64Decoder decoder;
	private final IvParameterSpec iv;

	
	/**
	 * 
	 */
	public IndexEncryption() {
		// key length: 128 bits
		keyValue = new byte[] { 'D', 'e', 'r', ' ', 'F', 'e', 'r', 'd', ' ', 'h', 'a', 't', ' ', 'v', 'i', 'e' };
		// initialization vector length: 64 bits
		iv = new IvParameterSpec(new byte[] { 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1 });
		ALGORITHM = "AES";
		ALGORITHM_WITH_PARAMS = "AES/CBC/PKCS5Padding";
		// ALGORITHM = "AES/CBC/PKCS5Padding";
		key = new SecretKeySpec(keyValue, ALGORITHM);
		try {
			encrypter = Cipher.getInstance(ALGORITHM_WITH_PARAMS);
			encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
			decrypter = Cipher.getInstance(ALGORITHM_WITH_PARAMS);
			decrypter.init(Cipher.DECRYPT_MODE, key, iv);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		encoder = new BASE64Encoder();
		decoder = new BASE64Decoder();
	}

	/* 
	 * 
	 */
	public Object encrypt(Object valueToEnc, Type type) {
		String valueToEncString;
		if (valueToEnc == null) {
			valueToEncString = "null";
		} else {
			valueToEncString = valueToEnc.toString();
		}
		byte[] encodedValue;
		try {
			encodedValue = encrypter.doFinal(valueToEncString.getBytes());
			return encoder.encode(encodedValue);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new RuntimeException("Encryption could not be performed");
		} catch (BadPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException("Encryption could not be performed");
		}
			
	}
	

	/* 
	 * 
	 */
	public Object decrypt(Object encryptedValue, Type type) {
		String encryptedValueString = encryptedValue.toString();
		byte[] decodedValue;
		try {
			decodedValue = decoder.decodeBuffer(encryptedValueString);
			String finValue = new String(decrypter.doFinal(decodedValue));
			if (finValue.equals("null")) {
				return null;
			} else{
				return finValue;
			}	
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Decryption could not be performed");
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new RuntimeException("Decryption could not be performed");
		} catch (BadPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException("Decryption could not be performed");
		}
		
	}
	

}
