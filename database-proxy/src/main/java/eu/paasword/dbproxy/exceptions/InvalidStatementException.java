package eu.paasword.dbproxy.exceptions;
/**
 * Exception for the construction of an statement which would be invalid. Used by the Statementpreparer and -filler
 * @author Mark Brenner
 *
 */
public class InvalidStatementException extends Exception {

	public InvalidStatementException(String string) {
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -677295094782569677L;

}
