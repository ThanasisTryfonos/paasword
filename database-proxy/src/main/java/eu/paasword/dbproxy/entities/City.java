package eu.paasword.dbproxy.entities;

/**
 * Created by valentin on 03.06.16.
 */
public class City implements ICRUD{

    public static final String CREATE_TABLE_CITY = "CREATE TABLE cities (id int primary key, name char(50) not null, fk_country int references Countries);";
    public static final String DROP_TABLE_CITY = "DROP TABLE IF EXISTS cities CASCADE;";
    private final String INSERT_INTO = "INSERT INTO cities(id, name, fk_country) VALUES (%s, %s, %s);";
    public static final String TABLE_NAME = "cities";

    private long id;
    private String name;
    private long fk_country;

    public City(long id, String name, Country country) {
        this.id = id;
        this.name = name;
        this.fk_country = country.getId();
    }

    @Override
    public String insertInto() {
        return String.format(INSERT_INTO, id, Wrap.w(name), fk_country);
    }

    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
