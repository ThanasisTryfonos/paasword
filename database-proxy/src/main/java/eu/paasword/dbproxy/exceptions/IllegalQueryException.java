package eu.paasword.dbproxy.exceptions;

/**
 * If a semantic invalid sql query should be processed.
 * 
 * @author Yvonne Muelle
 * 
 */
public class IllegalQueryException extends RuntimeException {

	private static final long serialVersionUID = -3599088093963595375L;

	public IllegalQueryException() {
		super();
	}

	public IllegalQueryException(String msg) {
		super(msg);
	}

}
