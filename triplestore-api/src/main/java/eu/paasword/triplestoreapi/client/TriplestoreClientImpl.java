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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TriplestoreClientImpl implements TriplestoreClient {

	protected final String ttlFile = "POLICIES-RDF.ttl";
	
	public boolean uploadToTriplestore(String contents) {
		try {
			return uploadToTriplestore(ttlFile, contents);
		} catch (Exception e) {}
		return false;
	}
	public boolean uploadToTriplestore(String ttlFile, String contents) throws IOException {
		if (contents==null || contents.trim().isEmpty())
			return true;
		try(Writer writer = new FileWriter(ttlFile)){
			writer.write(contents);
			return true;
		}
	}

	public String exportFromTriplestore() {
		try {
			//String filePath = Paths.get(".").toAbsolutePath().normalize().toString() + File.separator;
			return exportFromTriplestore(ttlFile);
		} catch (Exception e) {}
		return null;
	}
	public String exportFromTriplestore(String ttlFile) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(ttlFile));
			return new String(encoded, java.nio.charset.StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println(e);
		}
		return null;
	}
	
	public String getRdfClass(String individual) {
		throw new RuntimeException(this.getClass().getName()+": getRdfClass: ** NOT SUPPORT IN THIS 'TriplestoreClient' IMPLEMENTATION **");
	}
}