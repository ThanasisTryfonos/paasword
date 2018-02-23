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
package eu.paasword.app.security.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import eu.paasword.util.security.DefaultRSAKeypairGenerator;
import eu.paasword.util.security.JWTSecurityHandler;
import eu.paasword.util.security.SignatureNotVerifiedException;
import eu.paasword.util.security.auth.UserAuthentication;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public final class TokenHandler {

    private final String signerSecret;
    private final String TOKEN_ISSUER = "paasword-framework";
    private final String CLAIM_ROLE_NAME = "role";
    private static final long TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 10; //Ten days
    private static final Logger logger = Logger.getLogger(TokenHandler.class.getName());

    public TokenHandler(String signerSecret) {
        this.signerSecret = signerSecret;
    }

    /**
     * Returns a UserAuthantication object containing the principal and the role of the authenticated user.
     *
     * @param token The encrypted token to be parsed
     * @return UserAuthentication object
     */
    public UserAuthentication parseUserFromToken(String token) {
        //Trim "Bearer " prefix from token
        token = token.substring(7, token.length());

        try {
            //Parse JWT
            SignedJWT jwt = JWTSecurityHandler.parseEncryptedJWT(token, signerSecret, DefaultRSAKeypairGenerator.INSTANCE.getPrivateKey());
            //Check if the token has not exprired yet            
            if (Date.from(Instant.ofEpochMilli(System.currentTimeMillis())).getTime() < jwt.getJWTClaimsSet().getExpirationTime().getTime()) {
                //Set the roles of current user
                Set<GrantedAuthority> roles = new HashSet<>();
                roles.add(new SimpleGrantedAuthority((String) jwt.getJWTClaimsSet().getClaim(CLAIM_ROLE_NAME)));
                //Token is not expired, return valid Authentication
                return new UserAuthentication(new User(jwt.getJWTClaimsSet().getSubject(), "", roles));
            }//Token is expired
            else {
                //TODO: Handle token expiration issue
                logger.log(Level.WARNING, "Token has been expired for user: {0}", jwt.getJWTClaimsSet().getSubject());
            }
        } catch (ParseException | JOSEException | SignatureNotVerifiedException ex) {
            //TODO: Proper handle of exceptions especially 'SignatureNotVerifiedException'
            Logger.getLogger(TokenHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Creates a signed & encrypted JWT.
     *
     * @param user The user object which the ClaimSet will be build on
     * @return An encrypted JWT for the specific user
     */
    public String createTokenForUser(User user) {
        //Try to create a JWT for specific user
        try {
            logger.log(Level.INFO, "Trying to create JWT for user: {0}", user.getUsername());
            // Prepare JWT with claims set
            JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issueTime(new Date())
                    .issuer(TOKEN_ISSUER)
                    .jwtID(UUID.randomUUID().toString())
                    .expirationTime(Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME)))
                    .claim(CLAIM_ROLE_NAME, user.getAuthorities().stream().map(auth -> auth.getAuthority()).findFirst().get())
                    .build();
            return JWTSecurityHandler.createEncryptedToken(signerSecret, DefaultRSAKeypairGenerator.INSTANCE.getPublicKey(), jwtClaims);
        } catch (NoSuchAlgorithmException | JOSEException | ParseException ex) {
            logger.severe(ex.getMessage());
        }
        return null;
    }

}
