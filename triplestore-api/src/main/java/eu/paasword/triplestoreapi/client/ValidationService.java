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

import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ValidationService {
	public static void main(String args[]) throws Exception {
		if (args.length==0) {
			System.err.println("No input");
			return;
		}
		
		// Contact Validation Service...
		ValidationService service = new ValidationService();
		String responseStr = service.sendFilesForValidation(args);
		//-or- 
		//String responseStr = service.sendStringsForValidation(...a String[] array with TTL content...);
		System.out.println("Validation Service Response:");
		System.out.println(responseStr);
		
		// Parse Validation Service response into a ValidationResponse object...
		ValidationResponse vr = service.parseValidationResponse(responseStr);
		vr.setValidWithWarns(false);
			
		System.out.println();
		System.out.println("=========================================================");
		System.out.println(vr.toString());
		
		System.out.println();
		System.out.println("Validation Status: "+(vr.isValid() ? "VALID" : "INVALID"));
		System.out.println("Has Errors:        "+(vr.hasErrors() ? "Yes" : "No"));
		System.out.println("Has Warnings:      "+(vr.hasWarns() ? "Yes" : "No"));
	}
	
	/// Constructors and fields
	
	protected String validationServiceUrl = "http://securitypolicyvalidator.herokuapp.com/rest/validateSecurityPolicy";
	protected String validationServiceResponseStr;
	
	public ValidationService() {}
	
	public ValidationService(String serviceUrl) {
		validationServiceUrl = serviceUrl;
	}
	
	/// API
	
	public String getValidationServiceUrl() { return validationServiceUrl; }
	public void setValidationServiceUrl(String serviceUrl) { validationServiceUrl = serviceUrl; }
	
	public String sendFilesForValidation(String[] ttlFile) throws IOException, ValidationException {
		// prepare connection
		HttpURLConnection conn = prepareConnection();
		
		// send TTL files
		DataOutputStream output = new DataOutputStream(conn.getOutputStream());
		for (int i=0; i<ttlFile.length; i++) {
			if (ttlFile[i]==null || ttlFile[i].isEmpty()) continue;
			String ttlStr = new java.util.Scanner(new java.io.File(ttlFile[i])).useDelimiter("\\Z").next();
			output.writeBytes("# FILE: ");
			output.writeBytes(ttlFile[i]);
			output.writeBytes("\n\n");
			output.writeBytes(ttlStr);
			output.writeBytes("\n\n");
		}
		output.close();
		
		// read response
		return getResponseAsString(conn);
	}
	
	public String sendStringsForValidation(String[] ttlStr) throws IOException, ValidationException {
		// prepare connection
		HttpURLConnection conn = prepareConnection();
		
		// send TTL strings
		DataOutputStream output = new DataOutputStream(conn.getOutputStream());
		for (int i=0; i<ttlStr.length; i++) {
			if (ttlStr[i]==null || ttlStr[i].trim().isEmpty()) continue;
			output.writeBytes(ttlStr[i]);
			output.writeBytes("\n\n");
		}
		output.close();
		
		// read response
		return getResponseAsString(conn);
	}
	
	public ValidationResponse parseValidationResponse(String responseStr) throws ValidationException {
		// parse response into a ValidationResponse object
		if (responseStr.startsWith("[")) {
			Gson gson = new GsonBuilder().create();
			ValidationResponse vr = gson.fromJson("{\"results\":"+responseStr+"}", ValidationResponse.class);
			
			return vr;
		}
		throw new ValidationException("Not valid Validation Service JSON response", responseStr);
	}
	
	/// Protected methods
	
	protected HttpURLConnection prepareConnection() throws IOException, MalformedURLException {
		URL url = new URL(validationServiceUrl);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "text/plain");
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.connect();
		return conn;
	}
	
	protected String getResponseAsString(HttpURLConnection conn) throws IOException, ValidationException {
		// get the response
		int code = conn.getResponseCode(); // 200 = HTTP_OK
		//System.out.println("Response    (Code):" + code);
		//System.out.println("Response (Message):" + conn.getResponseMessage());
		
		// read the response
		DataInputStream input = new DataInputStream(conn.getInputStream());
		int c;
		StringBuilder resultBuf = new StringBuilder();
		while ( (c = input.read()) != -1) {
			resultBuf.append((char) c);
		}
		input.close();
		String responseStr = resultBuf.toString();
		
		// if failed, throw exception
		if (code<200 || code>299) throw new ValidationException("Validation Service reported an ERROR: code="+code+", description="+conn.getResponseMessage());
		
		// return response
		return (validationServiceResponseStr = responseStr);
	}
	
	/// Inner Classes
	
	public static class ValidationResponse {
		private boolean isValidWithWarns = true;
		Vector<ValidationResponseItem> results;
		
		public boolean isValid() {
			if (hasErrors()) return false; 
			if (!isValidWithWarns) return ! hasWarns() ;
			return true;
		}
		public boolean isValidWithWarns() { return this.isValidWithWarns; }
		public void setValidWithWarns(boolean b) { this.isValidWithWarns = b; }
		
		public boolean hasErrors() { return hasOutcome("error"); }
		public boolean hasWarns() { return hasOutcome("warning"); }
		public boolean hasOutcome(String level) {
			for (ValidationResponseItem vr : results) {
				if (vr.hasOutcome(level)) return true;
			}
			return false;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("ValidationResponse\n\tItem Count: ").append(results.size()).append("\n");
			for (ValidationResponseItem vr : results) {
				vr.toString(sb);
			}
			return sb.toString();
		}
	}
	
	public static class ValidationResponseItem {
		HashMap<String,String> queryConstraint;
		Vector<String> problematicResources;
		public boolean hasErrors() { return hasOutcome("error"); }
		public boolean hasWarns() { return hasOutcome("warning"); }
		public boolean hasOutcome(String level) {
			String lvl = queryConstraint.get("constraintLevel");
			if (lvl!=null && lvl.equals(level)) return true;
			return false;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			toString(sb);
			return sb.toString();
		}
		public void toString(StringBuilder sb) {
			sb.append(	String.format("ValidationResponseItem:\n\tconstraintStatements: %s\n\tquery: %s\n\tconstraintDescription: %s\n\tconstraintLevel: %s\n\tproblematicResources:\n",
								queryConstraint.get("constraintStatements"), queryConstraint.get("query"), queryConstraint.get("constraintDescription"), queryConstraint.get("constraintLevel"))
					);
			for (String it : problematicResources) {
				sb.append("\t\t").append(it).append("\n");
			}
		}
	}
	
	public static class ValidationException extends RuntimeException {
		protected String responseStr;
		public ValidationException(String message) {
			super(message);
		}
		public ValidationException(String message, String responseStr) {
			super(message);
			this.responseStr = responseStr;
		}
		public String getResponse() { return responseStr; }
	}
}