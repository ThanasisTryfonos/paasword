/*
 * Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.paasword.keymanagement;

/**
 *
 * @author gabel
 */
public class Certificate {
    
    private Key privateKey;
    private Key publicKey;
    
    public boolean check() {
        return true;
    }
    
    public Certificate() {
        privateKey = new Key();
        publicKey = new Key();
    }
    
    public Certificate(Key privateKey, Key publicKey) {
        this.privateKey = this.privateKey;
        this.publicKey = this.publicKey;
    }

    public Key getPrivateKey() {
        return privateKey;
    }

    public Key getPublicKey() {
        return publicKey;
    }
}
