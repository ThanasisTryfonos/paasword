package eu.paasword.dbproxy.entities;

/**
 * Created by valentin on 03.06.16.
 */
public class Wrap {

    public static String w(final String toBeWrapped){
        return "'"+toBeWrapped+"'";
    }


}
