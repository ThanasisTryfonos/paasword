package eu.paasword.dbproxy.exceptions;

/**
 * Unknown sql type should be supported.
 * 
 * @author Yvonne Muelle
 * 
 */
public class UnknownTypeException extends RuntimeException {
	private static final long serialVersionUID = -1642988613365487803L;

	public UnknownTypeException() {
		super();
	}

	public UnknownTypeException(String msg) {
		super(msg);
	}

}
