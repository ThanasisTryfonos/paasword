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
package eu.paasword.util.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class JWTSecurityHandler {

    public static String createEncryptedToken(String sginerSecret, RSAPublicKey publicKey, JWTClaimsSet jwtClaims) throws NoSuchAlgorithmException, JOSEException, ParseException {
        //Create a new signer
        JWSSigner signer = new MACSigner(sginerSecret);

        // Request JWT encrypted with RSA-OAEP and 128-bit AES/GCM
        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);

        // Sign
        signedJWT.sign(signer);

        //RSAKey key = RSAKeyMaker.make(2048, KeyUse.ENCRYPTION, Algorithm.NONE, "123");
        JWEObject jweObject = new JWEObject(header, new Payload(signedJWT));

        return encryptedToken(jweObject, publicKey).serialize();

    }

    private static JWEObject encryptedToken(JWEObject jwt, RSAPublicKey publicKey) throws JOSEException {
        // Create an encrypter with the specified public RSA key
        RSAEncrypter encrypter = new RSAEncrypter(publicKey);
        jwt.encrypt(encrypter);
        return jwt;
    }

    /**
     *
     * @param jwtToken The encrypted JWT
     * @param signerKey
     * @param privateKey An RSAPrivateKey which is used to decrypt an encrypted JWT
     * @return
     * @throws ParseException
     * @throws eu.paasword.util.security.SignatureNotVerifiedException
     */
    public static SignedJWT parseEncryptedJWT(String jwtToken, String signerKey, RSAPrivateKey privateKey) throws ParseException, JOSEException, SignatureNotVerifiedException {
        JWEObject jweObject = JWEObject.parse(jwtToken);
        // Create a decrypter with the specified private RSA key
        RSADecrypter decrypter = new RSADecrypter(privateKey);
        // Decrypt
        jweObject.decrypt(decrypter);
        // Extract payload
        SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        //Verify 
        boolean isVerified = signedJWT.verify(new MACVerifier(signerKey));
        if (isVerified) {
            return signedJWT;
        }
        throw new SignatureNotVerifiedException();

    }

//        public static void main(String... args) throws IOException, JOSEException, ClassNotFoundException, ParseException {
//        RSAKey key = RSAKeyMaker.make(2048, KeyUse.ENCRYPTION, Algorithm.NONE, "1");
//        RSAPublicKey castPK = (RSAPublicKey)Util.deserializeFromString(publicKey);
//        JWSSigner signer = new MACSigner(SIGNER_SECRET_KEY);
//
//        // Prepare JWT with claims set
//        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().subject("alice").issueTime(new Date()).issuer("jpa").jwtID(UUID.randomUUID().toString()).build();
//
//        // Request JWT encrypted with RSA-OAEP and 128-bit AES/GCM
//        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);
//
//        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);
//
//        // Sign
//        signedJWT.sign(signer);
//
//        EncryptedJWT jwt = new EncryptedJWT(header, jwtClaims);
//
//        RSAKey key = RSAKeyMaker.make(2048, KeyUse.ENCRYPTION, Algorithm.NONE, "123");
//
//        JWEObject jweObject = new JWEObject(header, new Payload(signedJWT));
//
//        jweObject = encryptedToken(jweObject, key.toRSAPublicKey());
//
//        parseEncryptedJWT(jweObject.serialize(), key.toRSAPrivateKey());
//    }
}
