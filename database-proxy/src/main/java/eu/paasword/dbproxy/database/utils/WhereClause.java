package eu.paasword.dbproxy.database.utils;
/**
 * Class represents a Where class to abstract from parser own constructs
 * @author Mark Brenner
 *
 */
public class WhereClause {
 /**
  * Left Operand, which is usually the column name
  */
   protected String leftOperand;
   
   protected WhereClause(String left) {
	   leftOperand = left;
   }

/**
 * 
 * @return the left operand e.g. the column name.
 */
public String getLeftOperand() {
	return leftOperand;
}
}
