package eu.paasword.dbproxy.database.utils;

import java.util.List;
/**
 * Class represents a Where Clause with an in-operator and a list 
 * @author Mark Brenner
 *
 */
public class WhereClauseIn extends WhereClause{
   /**
    * the List of given possibilites
    */
   private List<Object> in;
   
   /**
    * Constructs an Whereclause with an In-List 
    * @param left the columnname
    * @param In the list of possible values
    */
   public WhereClauseIn(String left, List<Object> In) {
	   super(left);
	   in = In;
}

/**
 * 
 * @return the list of possible values
 */
public List<Object> getIn() {
	return in;
}
   
}
