package eu.paasword.adapter.openstack.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by smantzouratos on 16/09/16.
 */
public class KeyPairUtil {

    static{
        if (Security.getProvider("BC") == null) Security.addProvider(new BouncyCastleProvider());

    }

    public static KeyPair generateKeypair(){
        KeyPair keyPair = null;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(keySize);
            keyPair = generator.generateKeyPair();

        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }
        catch (NoSuchProviderException e) {
            e.printStackTrace();

        }

        return keyPair;

    }

    public static String getPrivateKey(KeyPair keyPair){
        RSAPrivateKey priv = (RSAPrivateKey) keyPair.getPrivate();
        StringWriter writer = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(writer);
        try {
            pemWriter.writeObject(priv);
            pemWriter.flush();
            pemWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    public static String getPublicKey(KeyPair keyPair){
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        BASE64Encoder encoder = new BASE64Encoder();
        String encodedPublicKey = null;
        try {
            encodedPublicKey = "ssh-rsa " + encoder.encode(encodePublicKey(publicKey));
        } catch (IOException e) {
            e.printStackTrace();
        }
        encodedPublicKey = encodedPublicKey.replaceAll("\\n", "");
        return  encodedPublicKey;
    }

    public static byte[] encodePublicKey(RSAPublicKey key) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        /* encode the "ssh-rsa" string */
        byte[] sshrsa = new byte[]{0, 0, 0, 7, 's', 's', 'h', '-', 'r', 's', 'a'};
        out.write(sshrsa);
        /* Encode the public exponent */
        BigInteger e = key.getPublicExponent();
        byte[] data = e.toByteArray();
        encodeUInt32(data.length, out);
        out.write(data);
        /* Encode the modulus */
        BigInteger m = key.getModulus();
        data = m.toByteArray();
        encodeUInt32(data.length, out);
        out.write(data);

        return out.toByteArray();

    }

    public static void encodeUInt32(int value, OutputStream out) throws IOException {

        byte[] tmp = new byte[4];
        tmp[0] = (byte) ((value >>> 24) & 0xff);
        tmp[1] = (byte) ((value >>> 16) & 0xff);
        tmp[2] = (byte) ((value >>> 8) & 0xff);
        tmp[3] = (byte) (value & 0xff);
        out.write(tmp);
    }

    private static final int keySize = 2048;

}
