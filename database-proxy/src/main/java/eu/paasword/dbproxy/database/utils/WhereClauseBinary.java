package eu.paasword.dbproxy.database.utils;
/**
 * Class which represents an Whereclause with two operands
 * @author Mark Brenner
 *
 */
public class WhereClauseBinary extends WhereClause {
	 public WhereClauseBinary(String left) {
		super(left);
	}
	/**
	 * The rightOperand of the Whereclause, can be any type even another column 
	 */
	private Object rightOperand;
	private String Operator;
	/**
	 * Constructs a Where clause with two operands
	 * @param left the left operand e.g. table name
	 * @param right the value 
	 * @param op the operator as String
	 */
	public WhereClauseBinary(String left, Object right, String op) {
		   super(left);
		   rightOperand = right;
		   Operator = op;
	   }
	/**
	 * @return the left Operand e.g. the table
	 */
	public String getLeftOperand() {
		return leftOperand;
	}
	
	/**
	 * @return the right Operand e.g. the value
	 */
	public Object getRightOperand() {
		return rightOperand;
	}
	/**
	 * @return the operand as String
	 */
	public String getOperator() {
		return Operator;
	}
}
