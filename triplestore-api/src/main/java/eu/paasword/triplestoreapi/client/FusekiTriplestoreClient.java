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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

public class FusekiTriplestoreClient implements TriplestoreClient {

	private static final Logger logger = Logger.getLogger(FusekiTriplestoreClient.class.getName());

	final MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	final HttpClient client;

	protected Properties configProperties;

	public FusekiTriplestoreClient() {
		configProperties = new Properties();
		configProperties.setProperty("triplestore-query-url", "http://localhost:3030/ds/query");
		configProperties.setProperty("triplestore-export-url", "http://localhost:3030/ds/query?query=construct%20%7B%3Fs%20%3Fp%20%3Fo%20%7D%0Awhere%20%7B%0A%20%3Fs%20%3Fp%20%3Fo%20.%0A%7D&output=text&stylesheet=");
		configProperties.setProperty("triplestore-upload-url", "http://localhost:3030/ds/data?default");
		configProperties.setProperty("triplestore-upload-replace-contents", "true");
		HttpConnectionManagerParams params = connectionManager.getParams();
		params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 1000);
		params.setMaxTotalConnections(1000);
		connectionManager.setParams(connectionManager.getParams());
		client = new HttpClient(connectionManager);
	}

	public FusekiTriplestoreClient(String triplestoreQueryURL, String triplestoreExportURL, String triplestoreUploadURL, String triplestoreUploadReplaceContents) {
		configProperties = new Properties();
		configProperties.setProperty("triplestore-query-url", triplestoreQueryURL);
		configProperties.setProperty("triplestore-export-url", triplestoreExportURL);
		configProperties.setProperty("triplestore-upload-url", triplestoreUploadURL);
		configProperties.setProperty("triplestore-upload-replace-contents", triplestoreUploadReplaceContents);
		HttpConnectionManagerParams params = connectionManager.getParams();
		params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 1000);
		params.setMaxTotalConnections(1000);
		connectionManager.setParams(connectionManager.getParams());
		client = new HttpClient(connectionManager);
	}
	
	// -----------------------------------------------------------------------------------------------------------------------------------------
	
	public boolean uploadToTriplestore(String contents) {
		boolean success = false;
		logger.log(Level.INFO, "uploadToTriplestore: BEGIN: content-length= {0}", contents.length());
		
		String uploadUrl = configProperties.getProperty("triplestore-upload-url");
		String mode = configProperties.getProperty("triplestore-upload-replace-contents").trim().toLowerCase();
		boolean replaceTriplestoreContents = mode.equals("yes") || mode.equals("true") || mode.equals("replace");

		logger.log(Level.INFO, "uploadToTriplestore: Upload to triplestore: Upload url: {0}", uploadUrl);
		logger.log(Level.INFO, "uploadToTriplestore: Append to triplestore: Replace contents: {0}", replaceTriplestoreContents);

		if (replaceTriplestoreContents) {

			PutMethod method = new PutMethod(uploadUrl);

			try {
				RequestEntity re = new StringRequestEntity(contents, "text/turtle", "UTF-8");
				method.setRequestEntity(re);
				int statusCode = client.executeMethod(method);
				long callEndTm = System.currentTimeMillis();

				if (statusCode != HttpStatus.SC_OK) {
					Logger.getLogger(FusekiTriplestoreClient.class.getName()).log(Level.SEVERE, "Method failed: {0}", method.getStatusLine());
					success = false;
				} else {
//					byte[] bytesArray = IOUtils.toByteArray(method.getResponseBodyAsStream());
//					Logger.getLogger(FusekiTriplestoreClient.class.getName()).log(Level.INFO, "Method response: {0}", new String(bytesArray));
					logger.log(Level.INFO, "uploadToTriplestore: Fuseki Server Response: status: {0}", statusCode);
					success = true;
				}
			} catch (UnsupportedEncodingException ex) {
				Logger.getLogger(FusekiTriplestoreClient.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (IOException ex) {
				Logger.getLogger(FusekiTriplestoreClient.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} finally {
				method.releaseConnection();
			}

		} else {

			PostMethod method = new PostMethod(uploadUrl);

			try {
				RequestEntity re = new StringRequestEntity(contents, "text/turtle", "UTF-8");
				method.setRequestEntity(re);
				int statusCode = client.executeMethod(method);
				long callEndTm = System.currentTimeMillis();

				if (statusCode != HttpStatus.SC_OK) {
					Logger.getLogger(FusekiTriplestoreClient.class.getName()).log(Level.SEVERE, "Method failed: {0}", method.getStatusLine());
					success = false;
				} else {
//					byte[] bytesArray = IOUtils.toByteArray(method.getResponseBodyAsStream());
//					Logger.getLogger(FusekiTriplestoreClient.class.getName()).log(Level.INFO, "Method response: {0}", new String(bytesArray));
					logger.log(Level.INFO, "uploadToTriplestore: Fuseki Server Response: status: {0}", statusCode);
					success = true;
				}
			} catch (UnsupportedEncodingException ex) {
				Logger.getLogger(FusekiTriplestoreClient.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (IOException ex) {
				Logger.getLogger(FusekiTriplestoreClient.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} finally {
				method.releaseConnection();
			}

		}

		logger.log(Level.INFO, "uploadToTriplestore: END: result: {0}", success);
		return success;
	}
	
	// -----------------------------------------------------------------------------------------------------------------------------------------
	
	public String exportFromTriplestore() {
		String str = null;

		logger.log(Level.INFO, "exportFromTriplestore: BEGIN: n/a");

		String exportUrl = configProperties.getProperty("triplestore-export-url");
		logger.log(Level.INFO, "exportFromTriplestore: Querying export service: Query url: {0}", exportUrl);


		GetMethod method = new GetMethod(exportUrl);
		try {
			// Provide custom retry handler is necessary
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				logger.log(Level.INFO, "exportFromTriplestore: Response Status: {0} - {1}", new Object[] { statusCode, method.getStatusLine()});
			} else {
				logger.log(Level.INFO, "exportFromTriplestore: Response Status: {0}", statusCode);
				byte[] responseBody = IOUtils.toByteArray(method.getResponseBodyAsStream());
				str = new String(responseBody);
			}
		} catch (IOException ex) {
			Logger.getLogger(FusekiTriplestoreClient.class.getName()).log(Level.SEVERE, null, ex);
		} //Always release connection
		finally {
			method.releaseConnection();
		}

		logger.log(Level.INFO, "exportFromTriplestore: END: result:\n{0}", str);
		return str;
	}
	
	// -----------------------------------------------------------------------------------------------------------------------------------------
	
	public String getRdfClass(String individual) {
		// If individual has >1 classes it returns only one (the first returned from triplestore)
		String query = String.format("SELECT ?classUri { <%s> a ?classUri }", individual);
		Object val = queryValue(query);
		return (val!=null ? val.toString() : null);
	}
	
	protected Object queryValue(String selectQuery) {
		long startTm = System.nanoTime();
		logger.log(Level.FINE, "queryValue: BEGIN: query={0}", selectQuery);
		String queryService = configProperties.getProperty("triplestore-query-url");
		logger.log(Level.FINE, "queryValue: Querying service: {0}...", queryService);
		QueryExecution qeSelect = QueryExecutionFactory.sparqlService(queryService, selectQuery);
		RDFNode result = null;
		try {
			ResultSet rs = qeSelect.execSelect();
			if (rs.hasNext()) {
				QuerySolution soln = rs.next();
				Iterator<String> it = soln.varNames();
				if (it.hasNext()) {
					String key = it.next();
					result = soln.get(key);
					return result;
				}
			}
			return null;
		} finally {
			qeSelect.close();
			long duration = System.nanoTime()-startTm;
			logger.log(Level.FINE, "queryValue: END: result={0}", result);
		}
	}
}