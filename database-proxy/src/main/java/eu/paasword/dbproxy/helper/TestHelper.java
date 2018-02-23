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
package eu.paasword.dbproxy.helper;

import eu.paasword.dbproxy.output.OutputHandler;

import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Helper class to retrieve queries defined the TestQueries.xml file and to pretty print the results of a query.
 * @author valentin
 */
public enum TestHelper {

    INSTANCE(System.getProperty("user.dir") + "/database-proxy/src/main/resources/values/TestQueries.xml");

    private final TestQueryParser testQueries;
    private static final Logger logger = Logger.getLogger(TestHelper.class.getName());
    private String lastQueryName;

    TestHelper(String pathToXmlFile){
        lastQueryName = new String();
        testQueries = TestQueryParser.getInstance();
        try {
            testQueries.loadConfig(pathToXmlFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get at test query defined in the TestQueries.xml file.
     * @param queryName The name of the query node.
     */
    public String getQuery(final String queryName){
        setLastQueryName(queryName);
        return testQueries.getQuery(queryName);
    }

    /**
     * Get all queries defiend in the TestQueries.xml file
     * @return A map containing all queries.
     */
    public Map<String, String> getAllQueries(){
        return testQueries.getAllQueries();
    }


    /**
     * Prints out the result sets of the {@link OutputHandler} in a readable format.
     * @param handler
     */
    public  void print(OutputHandler handler){
        int rowCount = 0;
        for(ResultSet rs : handler.getResultSet()){
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                while (rs.next()) {
                    String line = "";
                    for (int i = 1; i <= columnsNumber; i++) {
                        String columnValue = rs.getString(i);
                        line += rsmd.getColumnName(i) + " = " + columnValue + ", ";
//                        logger.info(rsmd.getColumnName(i) + "=" + columnValue); // "%-24s",
                    }

                    logger.info("Result: " + line.substring(0, line.length()-2));
                    line = "";
                    rowCount++;
                }
                logger.info("----------------------------------------------------------------------------------------");
                logger.info("A total of "+ rowCount + " rows where returned by the query '" + getLastQueryName() +"'");
                logger.info("----------------------------------------------------------------------------------------\n");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int count(OutputHandler handler){
        int rowCount = 0;
        for(ResultSet rs : handler.getResultSet()){
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                while (rs.next()) {

                    rowCount++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return rowCount;
    }

    /**
     * Every time {@link #getQuery(String)} is called the name of the query is set by this method.
     * @param queryName
     */
    private void setLastQueryName(final String queryName){
        lastQueryName = queryName;
    }

    /**
     * Get the name of the query that was lastly returned by {@link #getQuery(String)}
     * @return the query
     */
    private String getLastQueryName(){
        return lastQueryName;
    }

    /**
     * Get the total count of rows that where put into local db before the query was applied to it.
     * With this we might messure the difference between actual result size (e.g. number of rows returned by the sql query)
     * versus number of rows that have been put into the db.
     * @param databaseTable
     * @return
     */
    private int getRowCountOfTable(final String databaseTable){
        int rowCount = 0;
        return rowCount;
    }
}
