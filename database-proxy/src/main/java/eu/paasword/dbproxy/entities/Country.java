package eu.paasword.dbproxy.entities;

/**
 * Created by valentin on 03.06.16.
 */
public class Country implements ICRUD{

    public static final String CREATE_TABLE_COUNTRY = "CREATE TABLE countries (id int primary key, name char(50) not null, inhabitants int not null)";
    public static final String DROP_TABLE_COUNTRY = "DROP TABLE IF EXISTS countries CASCADE;";
    private static final String INSERT_INTO = "INSERT INTO countries (id, name, inhabitants) VALUES (%s, %s, %s);";
    public static final String TABLE_NAME = "countries";

    private long id;
    private String name;
    private int inhabitants;

    public Country(long id, String name, int inhabitants) {
        this.id = id;
        this.name = name;
        this.inhabitants = inhabitants;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getInhabitants() {
        return inhabitants;
    }

    @Override
    public String insertInto() {
        return String.format(INSERT_INTO, id, Wrap.w(name), inhabitants);
    }
}
