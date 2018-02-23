package eu.paasword.dbproxy.encryption;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Encryption Helper with randomized IV-Vector. The IV-Vector is inserted into the encryption as first block and removed 
 * after decryption.
 * 
 * @author Matthias Huber
 * @author Matthias Gabel
 * @author Mark Brenner
 */
public class EncryptionHelperBaseIV implements Encryption {

	private final String ALGORITHM;
	private final String ALGORITHM_WITH_PARAMS;
	private final byte[] keyValue;
	private final Key key;
	private Cipher encrypter = null;
	private Cipher decrypter = null;
	private final BASE64Encoder encoder;
	private final BASE64Decoder decoder;
	private final IvParameterSpec iv;
	private SecureRandom ivGenerator;
	private final int IVSIZE = 16;
	
	public EncryptionHelperBaseIV() {
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
		try {
			ivGenerator = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.encryption.Encryption.encrypt()
	 */
	@Override
	public String encrypt(String valueToEnc) {
		//System.out.println("BaseEnc...");
		byte[] encodedValue;
		try {
			byte[] value = valueToEnc.getBytes();
			//Generate IV
			byte[] iv = new byte[IVSIZE];
			ivGenerator.nextBytes(iv);
			//Copy the IV into the same array
			byte[] toencrypt = new byte[iv.length + value.length];
			System.arraycopy(iv, 0, toencrypt, 0, iv.length);
			System.arraycopy(value, 0, toencrypt, iv.length, value.length);
			encodedValue = encrypter.doFinal(toencrypt);
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
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.iks.sdp.encryption.Encryption.decrypt()
	 */
	@Override
	public String decrypt(String encryptedValue) {
		byte[] decodedValue;
		try {
			decodedValue = decoder.decodeBuffer(encryptedValue);
			byte[] encoded = decrypter.doFinal(decodedValue);
			//Remove IV froom the plaintext
			int valuesize = encoded.length - IVSIZE;
			byte[] value = new byte[valuesize];
			System.arraycopy(encoded,  IVSIZE, value, 0, valuesize);
			return new String(value);	
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