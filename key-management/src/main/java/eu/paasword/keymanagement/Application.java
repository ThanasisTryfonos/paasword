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
public class Application {
    
    private final DbProxy dbProxy;
    private final TenantAdmin ta;
    private final Certificate cert;
    private final List<User> users;
    
   
    public Application(TenantAdmin ta, Key tenantKeyEncrypted) {
        this.ta = ta;
        cert = new Certificate();
        dbProxy = new DbProxy(this, tenantKeyEncrypted);
        users = new LinkedList<User>();
    }
    
    public void addUser(String userName, Key keyUser, String keyDbProxyEnc) {
        
        // add locally
        users.add(new User(userName, keyUser));
        System.out.println("App: added user " + userName + " with key " + keyUser.toStringHex());
        
        // add remotely
        dbProxy.addUser(userName, keyDbProxyEnc);
    }
    
    public Key getpublicKey() {
        return cert.getPublicKey();
    }
    
    public DbProxy getDbProxy() {
        return dbProxy;
    }

    void removeUser(String userName) {
        for (Iterator<User> iter = users.listIterator(); iter.hasNext();) {
            User u = iter.next();
            if (u.getName().equals(userName)) {
                iter.remove();
            }
        }
        
        dbProxy.removeUser(userName);
    }
}
