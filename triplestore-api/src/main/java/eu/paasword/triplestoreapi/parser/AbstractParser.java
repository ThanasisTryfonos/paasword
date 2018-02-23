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
package eu.paasword.triplestoreapi.parser;

/**
 * Created by ipatini on 10/04/2016.
 */
public abstract class AbstractParser {
	protected static String sanitizeUri(String uri) {
		if (uri==null) return null;
		uri = uri.replace(" ", "").replace("\t", "").replace("\r", "").replace("\n", "");
		if (uri.startsWith("http")) uri = "<"+uri+">";
		else uri = uri.replace("#", "_");
		return uri;
	}
	protected static String sanitizeText(String text) {
		if (text==null) return null;
		text = text.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
		return text;
	}
}
