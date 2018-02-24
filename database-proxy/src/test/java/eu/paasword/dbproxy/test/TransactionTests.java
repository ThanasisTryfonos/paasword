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
package eu.paasword.dbproxy.test;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.ParseException;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.helper.AdapterHelper;
import eu.paasword.dbproxy.helper.TestHelper;
import eu.paasword.dbproxy.impl.Adapter;
import eu.paasword.dbproxy.output.OutputHandler;
import eu.paasword.dbproxy.transaction.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class TransactionTests {

    private static final Logger logger = Logger.getLogger(TransactionTests.class.getName());

    public static void main(String[] args) throws ParseException, StandardException, DatabaseException {
        String adapterid = "80702";
//        executeNonTransactionalScenario(adapterid, 10);        
//        executeTransactionalScenario();
    }//EoM    

//    public static void executeTransactionalScenario() {
//        ConnectionContext concontext = new ConnectionContext("1", "jdbc:mysql://192.168.3.51:3306/transactiontest?tcpKeepAlive=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "root", "!r00t!");
//        try {
//            TransactionalManager tm = TransactionalManager.getInstance();
//
//            Connection connection=null; //= tm.initiateTransaction();
//            
////            tm.executeCUDDuringTransaction(connection, "delete from country ;");
////            tm.executeCUDDuringTransaction(connection, "INSERT INTO country (id, name, inhabitants) VALUES ( 10, 'country10', 10 );");
////            tm.executeCUDDuringTransaction(connection, "update country set name='italy',inhabitants=1000 where id=10 ; ");  
//
//            int id = 0;
//            int inhabitants = 0;
//            List<Object[]> results = tm.executeRDuringTransaction(connection, "select id,inhabitants from country where id = 1");
//            id = (Integer) results.get(0)[0];
//            inhabitants = (Integer) results.get(0)[1];
//            System.out.println("[0]: " + id + " [1]:" + inhabitants);
//
//            tm.executeCUDDuringTransaction(connection, "INSERT INTO country (id, name, inhabitants) VALUES ( 11, 'country11', 11 );");            
//
////            Commit Transaction
//            tm.commitTransaction2(connection);
//
//        } catch (SQLException ex) {
//            Logger.getLogger(TransactionalManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }//EoM    

//    public static void executeNonTransactionalScenario(String adapterid, int numofthreads) throws ParseException, StandardException, DatabaseException {
//
//        Adapter adapter = AdapterHelper.getAdapter(adapterid);
//        //insert the first indicative entry which will be subjected to competition
//        TestHelper.INSTANCE.print(adapter.query("delete from country;"));
//        TestHelper.INSTANCE.print(adapter.query("INSERT INTO country (id, name, inhabitants) VALUES ( 1, 'country1', 100 );"));
//        TestHelper.INSTANCE.print(adapter.query("select * from country;"));
//
//        ExecutorService executor = Executors.newWorkStealingPool();
//        List<Callable<Boolean>> callables = new ArrayList<>();
//
//        for (int i = 0; i < numofthreads; i++) {
//            callables.add(() -> {
//                Random random = new Random();
//                String delay = ("" + random.nextInt()).substring(2, 5);
//                logger.info("Thread started with delay: " + delay);
//                int id = 0;
//                int inhabitants = 0;
//                OutputHandler query = adapter.query("select id,inhabitants from country where id = 1");
//                for (ResultSet rs : query.getResultSet()) {
//                    try {
//                        ResultSetMetaData rsmd = rs.getMetaData();
//                        int columnsNumber = rsmd.getColumnCount();
//                        while (rs.next()) {
//                            for (int j = 1; j <= columnsNumber; j++) {
//                                String columnValue = rs.getString(j);
////                                System.out.printf("%-24s", rsmd.getColumnName(j) + "=" + columnValue);
//                                if (rsmd.getColumnName(j).equalsIgnoreCase("id")) {
//                                    id = Integer.parseInt(columnValue);
//                                }
//                                if (rsmd.getColumnName(j).equalsIgnoreCase("inhabitants")) {
//                                    inhabitants = Integer.parseInt(columnValue);
//                                }
//                            }//for
////                            System.out.println("\n\n");
//                        }
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//                }//for
//
//                //first add
//                int newvalue = inhabitants + new Integer(delay);
//                adapter.query("update country set inhabitants=" + newvalue + " where id = " + id + ";");
//
//                Thread.sleep(new Integer(delay));
//
//                query = adapter.query("select id,inhabitants from country where id = 1");
//                for (ResultSet rs : query.getResultSet()) {
//                    try {
//                        ResultSetMetaData rsmd = rs.getMetaData();
//                        int columnsNumber = rsmd.getColumnCount();
//                        while (rs.next()) {
//                            for (int j = 1; j <= columnsNumber; j++) {
//                                String columnValue = rs.getString(j);
////                                System.out.printf("%-24s", rsmd.getColumnName(j) + "=" + columnValue);
//                                if (rsmd.getColumnName(j).equalsIgnoreCase("id")) {
//                                    id = Integer.parseInt(columnValue);
//                                }
//                                if (rsmd.getColumnName(j).equalsIgnoreCase("inhabitants")) {
//                                    inhabitants = Integer.parseInt(columnValue);
//                                }
//                            }//for
////                            System.out.println("\n\n");
//                        }
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//                }//for              
//
//                //then delete
//                newvalue = inhabitants - new Integer(delay);
//                adapter.query("update country set inhabitants=" + newvalue + " where id = " + id + ";");
//
//                logger.info("Thread terminated");
//                //return
//                return true;
//            });
//        }//for
//        try {
//            executor.invokeAll(callables)
//                    .stream()
//                    .map(future -> {
//                        try {
//                            return future.get();
//                        } catch (InterruptedException | ExecutionException e) {
//                            throw new IllegalStateException(e);
//                        }
//                    })
//                    .forEach(System.out::println);
//        } catch (InterruptedException ex) {
//            logger.log(Level.SEVERE, null, ex);
//            //TODO add throw
//        }
//    }//EoM

}//EoC
