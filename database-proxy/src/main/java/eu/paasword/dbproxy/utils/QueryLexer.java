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
package eu.paasword.dbproxy.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the functionality of joining and splitting string with / at {@see QueryLexer#DELIMITER}. Special for the
 * split function is that it only splits at not escaped {@see QueryLexer#DELIMITER}.
 * 
 * @author Yvonne Muelle
 * 
 */
public class QueryLexer {
	/**
	 * Delimiter which is used as split point to split a string or as the delimiter when joining a list of strings
	 */
	public final char DELIMITER = ',';
	private final char ESCAPING = '\\';
	private static QueryLexer instance;

	public static QueryLexer getInstance(){
		if(instance == null) {
			instance = new QueryLexer();
		}
		return instance;
	}
	/**
	 * Constructs an object of this class
	 */
	private QueryLexer() {
		// empty constructor, no attributes must be configured
	}

	/**
	 * Splits toSplit at {@see QueryLexer#DELIMITER}, but only if {@link QueryLexer#DELIMITER} is not escaped by
	 * {@link QueryLexer#ESCAPING}.
	 * 
	 * @param toSplit
	 *            String to split
	 * @return the splittet string
	 */
	public ArrayList<String> splitDecryptedString(String toSplit) {
		ArrayList<String> result = new ArrayList<String>();
		char[] tokens = toSplit.toCharArray();
		int lastFoundPosition = 0;
		int i = 0;
		boolean split = false;
		while (i < tokens.length - 1) {
			if (tokens[i] == ESCAPING && (tokens[i + 1] == DELIMITER || tokens[i + 1] == ESCAPING)) {
				i += 2;
			} else if (tokens[i] == DELIMITER) {
				// Only if a not escaped delimiter is found, the string must be split
				split = true;
				String tmp = toSplit.substring(lastFoundPosition, i);
				// Escaping is reversed
				tmp = tmp.replace("\\\\", "\\");
				tmp = tmp.replace("\\,", ",");
				result.add(tmp);
				lastFoundPosition = i + 1;
				i++;
			} else {
				i++;
			}
		}
		String last = toSplit.substring(lastFoundPosition, toSplit.length());
		if(!split) {
			last = last.replace("\\\\", "\\");
			last = last.replace("\\,", ",");
		}
		result.add(last);

		return result;
	}

	/**
	 * Joins the list with {@see QueryLexer#DELIMITER}. No escaping is performed.
	 * 
	 * @param keys
	 *            string list that should be joined to one string
	 * @return the joined string
	 */
	public String joinDecryptedString(List<String> keys) {
		StringBuilder builder = new StringBuilder();

		for (String s : keys) {
			// Escaping is made
			// s = s.replace(String.valueOf(DELIMITER), DELIMITER + "" + DELIMITER);
			s = s.replace("\\", "\\\\");
			s = s.replace(",", "\\,");
			if (s.startsWith("'") && s.endsWith("'")) {
				s = s.substring(1, s.length() - 1);
			}

			builder.append(s);
			builder.append(DELIMITER);
		}
		String result = "";
		if (builder.length() != 0) {
			result = builder.substring(0, builder.length() - 1);
		}

		return result;
	}

}
