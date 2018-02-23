package eu.paasword.dbproxy.encryption;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
/**
 * Class which hashes the keys using a HmacSHA1 algorithm used by the Hashedindex structure
 * @author Maximilian Baritz
 * @author Mark Brenner
 *
 */
public class HMAC {
	Mac mac;
	
	/**
	 * Constructs a HMAC object with the given Key which is used for every Hash
	 * @param keyBytes
	 */
	public HMAC(byte[] keyBytes) 
	{
		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
		try {
			mac = Mac.getInstance("HmacSHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("HmacSHA1 Algorithm not supported");
		}
		try {
			mac.init(signingKey);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("Invalid key handed to HMAC");
		}
	}
	
	/**
	 * Method producing the hash of the given Data encoded by UTF-8
	 * @param toEncrypt the keys to be hashed
	 * @return a hash value for the given keys
	 */
	public String hash(String toEncrypt) {
		   byte[] rawHmac = mac.doFinal(toEncrypt.getBytes());

		   // Convert raw bytes to Hex
		   byte[] hexBytes = new Hex().encode(rawHmac);

		   // Covert array of Hex bytes to a String
		   try {
			return new String(hexBytes, "UTF-8");
		   } catch (UnsupportedEncodingException e) {
			return new String(hexBytes);
		  }
	}

}
