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
package eu.paasword.dbproxy.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A simple jpa for a SQL-Database using jdbc to connect to the server
 *
 * @author Mark Brenner
 *
 */
public class JDBCInterface extends DatabaseInterface {

    private Connection con;

    /**
     *
     * @param name the name of the database
     * @param url the URL for the Driver to connect to the database
     * @param username the user name to login on the database server
     * @param password the password to login on the database server
     */
    public JDBCInterface(String name, String url, String username, String password) {
        super(name);
        try {
            //DriverManager.registerDriver(new org.apache.derby.jdbc.ClientDriver());
            if (password == null || username == null) {
                con = DriverManager.getConnection(url);
            } else {
                System.out.println("Returning connection with url: " + url);
                con = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException e) {
            System.out.println("Error creating the JDBC interface");
        }
    }//EoM

    /**
     * Simple query method which uses the general jdbc execeute method
     *
     * @param query
     */
    @Override
    public boolean query(String query) {
        Statement stmt;
        try {
            stmt = con.createStatement();
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Close the connection to the database for this instance.
     */
    public void closeConnection() {
        if (null != con) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
