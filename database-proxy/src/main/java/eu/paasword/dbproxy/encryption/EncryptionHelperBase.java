package eu.paasword.dbproxy.encryption;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encryption Helper
 *
 * @author Matthias Huber
 * @author Matthias Gabel
 */
public class EncryptionHelperBase implements Encryption {

    private final String ALGORITHM;
    private final String ALGORITHM_WITH_PARAMS;
    private final byte[] keyValue;
    private final Key key;
    private Cipher encrypter = null;
    private Cipher decrypter = null;
    private final BASE64Encoder encoder;
    private final BASE64Decoder decoder;
    private final IvParameterSpec iv;

    public EncryptionHelperBase(String tenantKey) {
        // key length: 128 bits

        if (null != tenantKey && !tenantKey.isEmpty()) {
            keyValue = toBytes(tenantKey.toCharArray());
        } else {
            keyValue = new byte[]{'D', 'e', 'r', ' ', 'F', 'e', 'r', 'd', ' ', 'h', 'a', 't', ' ', 'v', 'i', 'e'};
        }

        // initialization vector length: 64 bits
        iv = new IvParameterSpec(new byte[]{1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1});
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
     * (non-Javadoc)
     *
     * @see edu.kit.iks.sdp.encryption.Encryption.encrypt()
     */
    @Override
    public String encrypt(String valueToEnc) {
        //System.out.println("BaseEnc...");
        byte[] encodedValue;
        try {
            encodedValue = encrypter.doFinal(valueToEnc.getBytes());
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
            return new String(decrypter.doFinal(decodedValue));
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

    private byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }
}