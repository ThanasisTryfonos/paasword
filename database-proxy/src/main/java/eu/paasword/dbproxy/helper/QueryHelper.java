/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.paasword.dbproxy.helper;

import eu.paasword.dbproxy.output.OutputHandler;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ubuntu
 */
public class QueryHelper {

    public static List< Map<String, String>> getSerializedOutput(OutputHandler handler) {
        List resultlist = new ArrayList<>();
        int rowCount = 0;
        for (ResultSet rs : handler.getResultSet()) {
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                while (rs.next()) {
                    Map rowmap = new HashMap();
                    for (int i = 1; i <= columnsNumber; i++) {
                        String columnValue = rs.getString(i);
                        rowmap.put(rsmd.getColumnName(i), columnValue);
                        //System.out.printf("++++++++++++++++++++%-24s", rsmd.getColumnName(i) + "=" + columnValue);
                    }
                    resultlist.add(rowmap);
                    rowCount++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resultlist;
    }//EoM    

    public static String formatForWeb(OutputHandler handler) {
        String output="";
        int rowCount = 0;
        for (ResultSet rs : handler.getResultSet()) {
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                while (rs.next()) {
                    for (int i = 1; i <= columnsNumber; i++) {
                        String columnValue = rs.getString(i);
                        output+=""+rsmd.getColumnName(i) + ": " + columnValue + ", ";
                    }
                    output = output.substring(0, output.length()-2) + "\n";
                    rowCount++;
                }
                output+="###################################################################\n";
                output+="A total of " + rowCount + " rows where returned by the query\n";
                output+="###################################################################\n";
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (output.isEmpty()) {
            output = "Query executed successfully!";
        }

        return output;
    }//EoM

}//EoC
