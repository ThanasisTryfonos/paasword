package eu.paasword.dbproxy.encryption;

/**
 * Encapsulates the possiblity to encrypt and decrypt a string.
 * 
 * @author Yvonne Muelle
 * 
 */
public interface Encryption {
	/**
	 * Encrypts the given string
	 * 
	 * @param toEncrypt
	 *            to encrypt
	 * @return the encrypted string
	 */
	String encrypt(String toEncrypt);

	/**
	 * Decrypts the given string
	 * 
	 * @param toDecrypt
	 *            to decrypt
	 * @return decrypted string
	 */
	String decrypt(String toDecrypt);
}
