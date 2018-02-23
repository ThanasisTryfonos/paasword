package eu.paasword.dbproxy.entities;

/**
 * Created by valentin on 02.06.16.
 */
public class Uni implements ICRUD {
    private static final String INSERT_INTO = "INSERT INTO unis (id, name, number_of_lecutre_halls, fk_city) VALUES(%s, %s, %s, %s)";
    public static final String CREATE_TABLE_UNIS =  "CREATE TABLE unis(id int primary key, name char(50) not null, number_of_lecutre_halls int, fk_city int references Cities);";
    public static final String DROP_TABLE_UNIS = "DROP TABLE IF EXISTS unis CASCADE;";
    public static final String TABLE_NAME = "unis";


    private  long id;
    private  String name;
    private  long fk_city;
    private  int number_of_lecutre_halls;

    public Uni(long id, String name, int number_of_lecutre_halls, City city) {
        this.id = id;
        this.name = name;
        this.fk_city = city.getId();
        this.number_of_lecutre_halls = number_of_lecutre_halls;
    }


    @Override
    public String insertInto() {
        String insertInto = new String(INSERT_INTO);
        return String.format(insertInto, id, Wrap.w(name), number_of_lecutre_halls, fk_city);
    }

    @Override
    public long getId() {
        return id;
    }
}
