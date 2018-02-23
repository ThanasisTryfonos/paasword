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
public class DbProxy {
    
    private final Application app;
    private final Key tenantKey;
    private final List<User> users;
    private final Certificate cert;
    
    public DbProxy(Application app, Key tenantKey) {
        this.app = app;
        this.tenantKey = tenantKey;
        cert = new Certificate();
        users = new LinkedList();
    }
    
    public Key getpublicKey() {
        return cert.getPublicKey();
    }

    void addUser(String userName, String keyDbProxyEnc) {
        Key k = new Key(Encryption.decrypt(cert.getPrivateKey(), keyDbProxyEnc));
        User user = new User(userName, k);
        users.add(user);
        System.out.println("DB Proxy: added user " + userName + " with key " + k.toStringHex());
    }

    void removeUser(String userName) {
        for (Iterator<User> iter = users.listIterator(); iter.hasNext();) {
            User u = iter.next();
            if (u.getName().equals(userName)) {
                iter.remove();
            }
        }
    }
}
