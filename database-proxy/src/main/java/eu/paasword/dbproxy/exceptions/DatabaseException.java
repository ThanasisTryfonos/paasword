package eu.paasword.dbproxy.exceptions;

/**
 * Exception if an error occurs while communicating with a database.
 * 
 * @author Yvonne Muelle
 * 
 */
public class DatabaseException extends Exception {

	private static final long serialVersionUID = -3021437597962988111L;

	public DatabaseException() {
		super();
	}

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(Throwable cause) {
		super(cause);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

}
