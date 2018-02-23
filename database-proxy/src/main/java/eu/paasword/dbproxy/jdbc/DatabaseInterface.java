package eu.paasword.dbproxy.jdbc;

/**
 * Abstract jpa for a Databaseconnection used for the benchmark
 * @author Mark Brenner
 *
 */
public abstract class DatabaseInterface {

	private String identifier;
	
	/**
	 * Super constructor for the jpa
	 * @param name the name which shall be used in the logs
	 */
	public DatabaseInterface(String name) {
		identifier = name;
	}
	
	/**
	 * 
	 * @return the name/identifier for this database
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Abstract Method which sends a query to the database
	 * @param query the sql statement to be performed
	 * @return if run without errors false if an error occured
	 */
	public abstract boolean query(String query);
	
}
