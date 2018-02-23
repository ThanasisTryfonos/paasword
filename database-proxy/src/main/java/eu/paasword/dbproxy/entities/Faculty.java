package eu.paasword.dbproxy.entities;

/**
 * Created by valentin on 03.06.16.
 */
public class Faculty implements  ICRUD{
    public static final String CREATE_TABLE_FACULTY = "CREATE TABLE faculties (id int primary key, name char(50) not null);";
    public static final String DROP_TABLE_FACULTY = "DROP TABLE IF EXISTS faculties CASCADE;";
    private static final String INSERT_INTO ="INSERT INTO faculties (id, name) VALUES (%s, %s);";
    public static final String TABLE_NAME = "faculties";

    private long id;
    private String name;

    public Faculty(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String insertInto() {
        return String.format(INSERT_INTO, id, Wrap.w(name));
    }

    @Override
    public long getId() {
        return id;
    }
}
