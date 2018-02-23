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

import java.security.SecureRandom;
import java.util.Random;

/**
 *
 * @author gabel
 */
public class Key {

    public static final int keyLength = 32; // in bits
    private static Random random = new SecureRandom();
    private boolean[] key = new boolean[keyLength];

    public Key(boolean[] k) {
        System.arraycopy(k, 0, key, 0, keyLength);
    }
    
    public Key(String s) {
        for (int i = 0; i < keyLength; ++i) {
            key[i] = (s.charAt(i) == '1');
        }
    }

    public Key() {
        this.key = Key.getRandomKey().key;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(keyLength);
        for (int i = 0; i < keyLength; ++i) {
            s.append(key[i] ? '1' : '0');
        }
        return s.toString();
    }
    
    public String toStringHex() {
        String s = toString();
        long decimal = Long.parseLong(s, 2);
        return Long.toString(decimal, 16);
    }
    
    public static Key getRandomKey() {
        boolean[] bools = new boolean[keyLength];
        for (int i = 0; i < keyLength; ++i) {
            bools[i] = random.nextBoolean();
        }
        return new Key(bools);
    }
    
    public boolean[] getKey() {
        return key;
    }

}
