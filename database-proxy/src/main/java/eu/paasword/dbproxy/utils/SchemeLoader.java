/*
 *  Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.paasword.dbproxy.utils;

import eu.paasword.dbproxy.database.DBScheme;
import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.helper.AdapterHelper;
import eu.paasword.dbproxy.transaction.DistributedTransactionalManager;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Encapsulates the method to load a database scheme from file or from database
 *
 * @author Yvonne Muelle
 *
 */
public class SchemeLoader {

    private static final Logger logger = Logger.getLogger(SchemeLoader.class.getName());

    private static String adapterid;
    private static String databaseName;

    /**
     * Loads the information where to get the scheme and from that place the
     * whole scheme information. The scheme information can be stored in a local
     * file, in a remote file or simply in the database.
     *
     * @param schemePlace
     * @param schemeType
     * @param connection Connection to the database to which this scheme
     * belongs. The scheme is loaded out of the database.
     * @return mapping between the relation and its columns
     * @throws DatabaseException
     * @throws IOException if the scheme is stored in a file and this file could
     * not be accessed.
     */
    public static Map<String, List<Column>> loadScheme(String schemePlace, String schemeType, String adapterid, String databaseName,String sessionid) throws DatabaseException, IOException {
        DistributedTransactionalManager dtm;
        Map<String, List<Column>> ret = null;
        try {
            //load TManager            
            dtm = AdapterHelper.getDTMByAdapterId(adapterid);
            ret = dtm.executeLoadSchemaQuery(databaseName,sessionid);
        } //EoM
        catch (SQLException ex) {
            logger.severe("SchemeLoader-->Error during schema loading");
        }
        return ret;
    }//EoM

    // NOTE: Code fits to PostgresSQL; for other DBs ist could be easier
    public static void dropScheme(Set<String> tables, String adapterid, String databaseName, String sessionid) {
        DistributedTransactionalManager dtm;
        try {
            dtm = AdapterHelper.getDTMByAdapterId(adapterid);
            for (String tableName : tables) {
                String sql = "drop table if exists \"" + tableName + "\" cascade;";
                dtm.executeAtomicCUDQuery(sql, databaseName,sessionid);
            }
        } catch (SQLException e) {
            logger.severe("SchemeLoader-->Error during schema drop");
        }
    }//EoM

    public static void createScheme(DBScheme newScheme) {
        System.out.println("received Scheme: " + newScheme);
        System.out.println();
    }

    public static void createScheme(Map<String, List<Column>> newScheme, String adapterid, String databaseName, String sessionid) {
        DistributedTransactionalManager dtm;
        try {
            dtm = AdapterHelper.getDTMByAdapterId(adapterid);            
//            Statement st = con.createStatement();

            //CREATE TABLE phonebook(phone VARCHAR(32), firstname VARCHAR(32), lastname VARCHAR(32), address VARCHAR(64));
            Set<String> tableNames = newScheme.keySet();
            String sql = null;
            for (String tableName : tableNames) {
                sql = new String();
                sql += "create table ";
                sql += "\"" + tableName + "\"";
                sql += "(";
                List<Column> columns = newScheme.get(tableName);
                boolean first = true;
                for (Column column : columns) {
                    if (first) {
                        sql += "\"" + column.getName() + "\" ";
                    } else {
                        sql += ", \"" + column.getName() + "\" ";
                    }
                    String type = column.getType().toString();
                    if (type.equalsIgnoreCase("String")) {
                        sql += "TEXT";
                    } else if (type.equalsIgnoreCase("double")) {
                        // TODO Fix parser and remove this!
                        sql += "DOUBLE PRECISION"; //Workaround for parser problem 
                    } else {
                        sql += type.toUpperCase();
                    }
                    first = false;
                }
                sql += ");";
                dtm.executeAtomicCUDQuery(sql, databaseName,sessionid);
            }
        } catch (SQLException e) {
            System.out.println("Error creating new local DB scheme.");
            e.printStackTrace();
        }
    }
}
