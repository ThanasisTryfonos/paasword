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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author gabel
 */
public class TenantAdmin {

    private final List<User> users;
    private final Certificate cert;
    private final Application app;
    private final Key tenantKey;

    public TenantAdmin() {
        users = new LinkedList<>();
        cert = new Certificate();
        tenantKey = new Key();
        Key tenantKeyEnc = new Key(Encryption.encrypt(new Key(), tenantKey.toString()));
        app = new Application(this, tenantKeyEnc);
    }

    public void addUser(String userName) {
        
        // add locally
        User u = new User(userName);
        users.add(u);

        // add remotely
        Key keyUser = u.getKey();
        Key keyApp = Key.getRandomKey();
        Key keyDbProxy = calcThirdKey(keyUser, keyApp); // proxy key for specific user
        String keyDbProxyEnc = Encryption.encrypt(app.getDbProxy().getpublicKey(), keyDbProxy.toString());
        app.addUser(userName, keyApp, keyDbProxyEnc);
    }

    public void removeUser(String userName) {
        
        // remove locally
        for (Iterator<User> iter = users.listIterator(); iter.hasNext();) {
            User u = iter.next();
            if (u.getName().equals(userName)) {
                iter.remove();
            }
        }
        
        // remove remotely
        app.removeUser(userName);
    }

    public void recoverUserKey(String userName) {
        
    }
    
    public void updateUserKey(String userName) {
        
    }
    
    public void recoverApplicationKeys() {
        // app key is corrupt and needs to be changed while user keys and tenant key are unchanged
        // ... dbproxy key then needs to be changed (for all users)
        
        Iterator<User> iter = users.iterator();
        while (iter.hasNext()) {
            User u = iter.next();
            String userName = u.getName();
            Key keyUser = u.getKey();
            app.removeUser(userName);
            Key keyApp = Key.getRandomKey();
            Key keyDbProxy = calcThirdKey(keyUser, keyApp);
            String keyDbProxyEnc = Encryption.encrypt(app.getDbProxy().getpublicKey(), keyDbProxy.toString());
            app.addUser(userName, keyApp, keyDbProxyEnc);
        }
    }

    public List<User> listUsers() {
        return users;
    }

    private Key calcThirdKey(Key key1, Key key2) {
        boolean[] key3bool = new boolean[Key.keyLength];
        for (int i = 0; i < Key.keyLength; ++i) {
            key3bool[i] = (key1.getKey()[i] ^ key2.getKey()[i]) ^ tenantKey.getKey()[i];
        }
        return new Key(key3bool);
    }

}
