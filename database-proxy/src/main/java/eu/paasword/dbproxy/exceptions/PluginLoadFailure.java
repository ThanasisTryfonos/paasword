package eu.paasword.dbproxy.exceptions;

/**
 * Plugin could not be loaded due to several causes.
 * 
 * @author Yvonne Muelle
 * 
 */
public class PluginLoadFailure extends Exception {

	private static final long serialVersionUID = -4515844235727371707L;

	public PluginLoadFailure() {
		super();
	}

	public PluginLoadFailure(String message) {
		super(message);
	}

	public PluginLoadFailure(Throwable cause) {
		super(cause);
	}

	public PluginLoadFailure(String message, Throwable cause) {
		super(message, cause);
	}

}
