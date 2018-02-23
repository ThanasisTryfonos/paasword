/**
 * 
 */
package eu.paasword.dbproxy.database.utils;

/**
 * Represents the various types that are supported of allowed data types in a database
 * 
 * @author Yvonne Muelle
 * 
 */
public enum Type {
	Integer, Double, Date, String, Boolean;
	// Boolean, Char, Decimal, Float, Numeric, Real, Smallint, Time
	
	/**
	 * Custom Value of for this Enum which ignores cases
	 * @param type the identifier of the type as String which should be found
	 * @return the wanted type of this Enum or null if it could not be found
	 */
	public static Type customValueOf(String type) {
		type = type.toLowerCase();
		switch ((String) type) {
		case "integer":
		    return Type.Integer;
		case "double":
			return Type.Double;
		case "date":
			return Type.Date;
		case "string":
			return Type.String;
		case "boolean":
		   return Type.Boolean;
		default:
			return null;
		}
	}
}
