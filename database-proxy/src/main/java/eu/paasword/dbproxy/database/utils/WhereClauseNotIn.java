package eu.paasword.dbproxy.database.utils;

import java.util.List;
/**
 * This class represents a Whereclause which specifies a not in. Is only distinguishable from an in-Whereclause by its class type
 * @author Mark Brenner
 *
 */
public class WhereClauseNotIn extends WhereClauseIn {

	/**
	 * Constructs a not in Whereclause
	 * @param left the Columnname
	 * @param In list of object which shall be in the column
	 */
	public WhereClauseNotIn(String left, List<Object> In) {
		super(left, In);
	}

}
