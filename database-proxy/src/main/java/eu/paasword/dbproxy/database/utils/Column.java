package eu.paasword.dbproxy.database.utils;

/**
 * Represents a the type and name of a column in a relational database
 * 
 * @author Yvonne Muelle
 * 
 */
public class Column implements Comparable<Column> {
	protected Type type;
	protected String name;
	private int id;
	protected boolean varcharType;
	protected boolean charType;
	protected int length;
	protected boolean not_null;
	protected boolean unique;
	protected boolean primary_key;
	public boolean isNot_null() {
		return not_null;
	}

	/**
	 * 
	 * @return whether column is only allowed to contain unique values inclusvie null
	 */
	public boolean isUnique() {
		return unique;
	}
	
	/**
	 * 
	 * @return whether this column belongs to the primary key of the table
	 */
	public boolean isPrimary_key() {
		return primary_key;
	}

	/**
	 * Constructs a Column where the id is unimportant
	 * 
	 * @param type
	 *            type of the column, could be as defined in {@link Type}
	 * @param name
	 *            Name of the column
	 */
	public Column(Type type, String name, int length, boolean varchar) {
		this.type = type;
		this.name = name;
		this.length = length;
		varcharType = varchar;
		if(type.equals(Type.String)) {
			charType = true;
		}
		id = -1; // means the id is irrelevant
		not_null = false;
		unique = false;
		primary_key = false;
	}
	
	/**
	 * Constructs a column without id but with constraints
	 * @param type type of the column, could be as defined in {@link Type}
	 * @param name   Name of the column
	 * @param length the allowed length 
	 * @param varchar whether this is a variable char type
	 * @param notnull whether it can contain null values
	 * @param uniquecol whether it shall be a column with unique values
	 * @param primkey whether its part of the primary key of the column
	 */
	public Column(Type type, String name, int length, boolean varchar, boolean notnull, boolean uniquecol, boolean primkey) {
		this.type = type;
		this.name = name;
		this.length = length;
		varcharType = varchar;
		if(type.equals(Type.String)) {
			charType = true;
		}
		id = -1; // means the id is irrelevant
		not_null = notnull;
		unique = uniquecol;
		primary_key = primkey;
		if(primkey) {
			not_null = true;
		}
	}
	
	/**
	 * 
	 * @return whether this column is a variable char type
	 */
	public boolean isVarcharType() {
		return varcharType;
	}

	/**
	 * 
	 * @return this column does contain any char type
	 */
	public boolean isCharType() {
		return charType;
	}

	/**
	 * 
	 * @return the maximum length of the column entries or -1 if no maximum length is specified (Only important for chars)
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Constructs a Column with id as well
	 * @param name Name of the column
	 * @param datatype type of the column, could be as defined in {@link Type}
	 * @param Id the internal id in the fieldMeta
	 */
	public Column(String name, Type datatype, int Id, int length, boolean varchar, boolean nullable, boolean uniquecol, boolean primkey) {
		this.type = datatype;
		this.name = name;
		id = Id;
		this.length = length;
		varcharType = varchar;
		if(type.equals(Type.String)) {
			charType = true;
		}
		not_null = nullable;
		unique = uniquecol;
		primary_key = primkey;
		if(primkey) {
			not_null = true;
		}
	}
	
	/**
	 * 
	 * @return the internal id of the Column
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the type of the column
	 * 
	 * @return type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * returns the name of the column
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Tells whether two Columns object are equal in all their attributes
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Column) {
			Column c = (Column) o;

			return c.name.equals(name) && c.type.equals(type) && (c.id == id);
		}

		return false;
	}
	
	/**
	 * Prints a String representation of this Object including its type, name and id which is -1 if it has not been specified
	 */
	@Override
	public String toString() {
		return type + " " + name + " " + id;
	}

	/**
	 * Compare Method which is used so sort Lists of Columns by their id
	 */
	@Override
	public int compareTo(Column o) {
		  if (id < o.getId()) {
		      return -1;
		  }
		  else if (id == o.getId()) {
		      return 0;
		  } else {
			  return 1;
		  }
	}
}
