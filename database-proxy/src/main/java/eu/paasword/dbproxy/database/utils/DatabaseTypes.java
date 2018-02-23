package eu.paasword.dbproxy.database.utils;

/**
 *
 * This enumeration covers the types of databases that exist.
 * In the code there are many hard coded Strings that should be replaced by an instance of this.
 *
 * By default there where two types (local, remote).
 * But for a distributed scenario there will be more different types to capture.
 *
 */
public enum DatabaseTypes {
    LOCAL("local"),
    REMOTE("remote"),
    REMOTE_INDEX("remote_index");

    private String type;

    DatabaseTypes(final String pType){
        type = pType;
    }
}
