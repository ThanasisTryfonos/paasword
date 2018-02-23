package eu.paasword.dbproxy.database.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import eu.paasword.dbproxy.database.utils.Type;
import eu.paasword.dbproxy.encryption.IndexEncryption;

/**
 * 
 * @author Tobias Andersson
 *
 * This class provides frequency hiding for index tables. 
 * A value b defined at instantiation of class is the requirement of mapped values to any given keyword.
 * A keyword is split into multiple keywords if need be, padding is done if need be to achieve requirement b.
 *
 */
public class KeywordSplitter {
	private int b;

	/**
	 * Constructor
	 * 
	 * @param b, int, defines the requiremet of values mapped to any given keyword 
	 */
	public KeywordSplitter(int b){
		if(b < 1){
			throw new IllegalArgumentException("b IS 0 OR NEGATIVE. YOU NEED b >= 1");
		}else{
			this.b = b;
		}
	}

	/**
	 * 
	 * @param new_b, int
	 */
	public void changeB(int new_b){
		if(b < 1){
			throw new IllegalArgumentException("b IS 0 OR NEGATIVE. YOU NEED b >= 1");
		}else{
			b = new_b;
		}
	}

	/**
	 * 
	 * @param keyword, String, the keyword which should be mapped with the values
	 * @param values, ArrayList<Object>, holds the values for which are to be mapped to the keyword
	 * @param encForValues, IndexEncryption, is the defined index encryption for this list of values connected with this keyword
	 * @return a Map<String, ArrayList<Object>, which maps the keyword to its values given the requirement b. Padding is done if needed (padded value is -1)
	 */
	public Map<String, ArrayList<Object>> makeDecision(String keyword, ArrayList<Object> values, IndexEncryption encForValues){
		checkInput(keyword, values, encForValues);
		Map<String, ArrayList<Object>> allSplits = new HashMap<String, ArrayList<Object>>();
		if(values.size() == b){
			//Nothing needs to be done, since the b requirement is already achieved.
			allSplits.put(keyword, values);
		}else if (values.size() > b){
			//Split the values of the keyword into multiple instances, no amount of values can exceed b. 
			//Pad if needed. Also, added keyword are created keyword+i.
			//Thus, during select the db can search for keyword+i, i=0,1,2...until no other instance is found.
			double splits = roundUp(values.size(), b); 
			for(int i = 0; i < splits; i++){
				ArrayList<Object> tmp = new ArrayList<Object>();
				while(tmp.size() < b){
					if(values.size() > 0){
						tmp.add(values.remove(values.size()-1));
					}else{
						//Padding to reach requirement defined by b.
						while(tmp.size() < b){
							tmp.add(encForValues.encrypt("-1", Type.String));
						}
					}
				}
				allSplits.put(keyword+Integer.toString(i), tmp);
			}
		}else{
			//Adding padding for the number of values to reach requirement b.
			if(encForValues != null){
				while(values.size() < b){
					values.add(encForValues.encrypt("-1", Type.String));
				}
			}
			allSplits.put(keyword, values);
		}
		return allSplits;
	}
	
	/**
	 * Divides a and b, always returns the rounded up value. E.g. 4/3 = 1.333... will return 2.
	 * 
	 * @param a, int, the dividend
	 * @param b, int, the divisor
	 * @return the rounded up value of the quotient
	 */
	private double roundUp(int a, int b){	
		int rem = a%b;
		int wNbr = a/b;
		if(rem > 0){
			wNbr += 1;
		}
		return wNbr;
	}
	
	/**
	 * Checks the validity of the input parameters and throws an exception if something is awry
	 * 
	 * @param keyword, String
	 * @param values, ArrayList<Object>
	 * @param encForValues, IndexEncyrption
	 */
	private void checkInput(String keyword, ArrayList<Object> values, IndexEncryption encForValues){
		if(keyword == null){
			throw new IllegalArgumentException("INPUT String keyword IS NULL");
		}else if(keyword == ""){
			throw new IllegalArgumentException("INPUT String keyword IS EMPTY");
		}else if(values == null){
			throw new IllegalArgumentException("INPUT ArrayList<Object> values IS NULL");
		}else if(values.size() == 0){
			throw new IllegalArgumentException("INPUT ArrayList<Object> values IS EMPTY");
		}else if(encForValues == null){
			throw new IllegalArgumentException("INPUT IndexEncryption encForValues IS NULL");
		}
	}
}
