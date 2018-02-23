package eu.paasword.dbproxy.entities;


interface HasID{
    public long getId();
}
/**
 * Created by valentin on 03.06.16.
 */
public interface ICRUD extends HasID{
    /**
     * Returns the INSERT INTO table VALUES clause for this instance.
     * @return The INSERT INTO string
     */
    public String insertInto();
}
