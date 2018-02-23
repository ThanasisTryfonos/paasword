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
package eu.paasword.triplestoreapi.client;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TriplestoreClientFactory {

	private static final Logger logger = Logger.getLogger(TriplestoreClientFactory.class.getName());
	
	public static TriplestoreClient getClientInstance() {
		//String clss = eu.paasword.triplestoreapi.client.FusekiTriplestoreClient.class.getName();
		String clss = eu.paasword.triplestoreapi.client.TriplestoreClientImpl.class.getName();
		return getClientInstance(clss);
	}
	
	public static TriplestoreClient getClientInstance(String clss) {
		try {
			TriplestoreClient client = (TriplestoreClient)Class.forName(clss).newInstance();
			logger.log(Level.FINE, "TriplestoreClientFactory.getClientInstance: Created new instance of class {0}", clss);
			return client;
		} catch (Exception e) {
			logger.log(Level.SEVERE,"TriplestoreClientFactory.getClientInstance: class= {0} EXCEPTION THROWN: {1}", new Object[] {clss, e});
			throw new RuntimeException(e);
		}
	}
}