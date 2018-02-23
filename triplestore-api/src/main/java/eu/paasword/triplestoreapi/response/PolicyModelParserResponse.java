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
package eu.paasword.triplestoreapi.response;

public class PolicyModelParserResponse extends ParserResponse {
	protected String rdfStr;
	protected String xacmlStr;
	
	public PolicyModelParserResponse(ParserResponse.VALIDATION_RESULT result, String mesg, String rdf, String xacml) {
		this.result = result;
		this.message = mesg;
		this.rdfStr = rdf;
		this.xacmlStr = xacml;
	}
	public PolicyModelParserResponse(String result, String mesg, String rdf, String xacml) {
		this.result = VALIDATION_RESULT.valueOf(result);
		this.message = mesg;
		this.rdfStr = rdf;
		this.xacmlStr = xacml;
	}
	
	public String getRdf() { return rdfStr; }
	public String getXacml() { return xacmlStr; }
}