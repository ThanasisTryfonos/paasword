/*
 * Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.ubitech;

import eu.paasword.jpa.PaaSwordEntityHandler;
import eu.paasword.jpa.exceptions.NotAValidPaaSwordEntityException;
import eu.paasword.jpa.exceptions.ProxyInitializationException;
import eu.ubitech.model.Country;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class App {

    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String PROXYID = "993974";

    public static void main(String[] args) throws ProxyInitializationException, NotAValidPaaSwordEntityException, InterruptedException {
//        App.singleThreadTransactionTest(PROXYID);
//        App.multiThreadedTransactionTest(PROXYID,  10 );     
//        App.singleThreadDistributedTransactionTest(PROXYID);
        App.multiThreadedDistributedTransactionTest(PROXYID, 20);
//        App.performJPATests(PROXYID);
    }//main

    public static void performJPATests(String adapterid) throws ProxyInitializationException {
//        create handler
        PaaSwordEntityHandler entityhandler = PaaSwordEntityHandler.getInstance(adapterid);

        //Query-1 delete everything
        entityhandler.customQuery("delete from country ;");

//        //Query-2 insert
        entityhandler.customQuery("INSERT INTO country (id, name, inhabitants) VALUES (1, 'country1', 111);");
        entityhandler.customQuery("INSERT INTO country (id, name, inhabitants) VALUES (2, 'country2', 222);");
//
////        //Query-3
        entityhandler.customQuery("update country set name='italy' where id=1 ;");
//        
////        //Query-4
        entityhandler.customQuery("select * from country ;");

//        
//        Country country11 = new Country("paraskeva33",333);
//        Country country22 = new Country(22,"paraskeva22",222);
//        entityhandler.save(country11);
//        entityhandler.save(country22);
//        logger.info(entityhandler.findAll(Country.class).size()+"");
//        entityhandler.save(country11);
//        logger.info(entityhandler.findAll(Country.class).size()+"");
//        entityhandler.delete(new Long(1), Country.class);
//        City newcity = new City();
//        newcity.setId(1);
//        newcity.setName("city1");
//        entityhandler.save(newcity);        
//        List<Object> cities = entityhandler.findAll(City.class);
//        for (Iterator<Object> it = cities.iterator(); it.hasNext();) {
//            City city = (City) it.next();
//            System.out.println(city.getId() + " " + city.getName());
//        }
//           
    }

    public static void singleThreadTransactionTest(String adapterid) throws ProxyInitializationException {

//        create handler
        PaaSwordEntityHandler entityhandler = PaaSwordEntityHandler.getInstance(adapterid);

        //Test for Transactions        
        String selq = "select * from country";
        String insq = "INSERT INTO country (id, name, inhabitants) VALUES ( 10, 'country10', 10 );";
        String updq = "update country set name='italy',inhabitants=1000 where id=10 ; ";
        String delq = "delete from country; ";

        String tid = entityhandler.initiateTransaction();
        logger.info("Transactionid: " + tid);

        entityhandler.performCUDQueryDuringTransaction(delq, tid);

        entityhandler.performCUDQueryDuringTransaction(insq, tid);

        List<Object[]> results = entityhandler.performRQueryDuringTransaction(selq, tid);
        logger.info("#results: " + results.size());

        entityhandler.performCUDQueryDuringTransaction(updq, tid);

        entityhandler.commitTransaction(tid);
    }//EoM

    public static void multiThreadedTransactionTest(String adapterid, int numofthreads) throws ProxyInitializationException, InterruptedException {

        PaaSwordEntityHandler entityhandler = PaaSwordEntityHandler.getInstance(adapterid);

        //prepare the environment for clean experiment
        String inittid = entityhandler.initiateTransaction();
        entityhandler.performCUDQueryDuringTransaction("delete from country", inittid);
        entityhandler.performCUDQueryDuringTransaction("INSERT INTO country (id, name, inhabitants) VALUES ( 1, 'country1', 100 );", inittid);
        entityhandler.commitTransaction(inittid);

        logger.info("Sleeping before all threads start");
        Thread.sleep(new Integer(2000));

        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Boolean>> callables = new ArrayList<>();

        for (int i = 0; i < numofthreads; i++) {
            callables.add(() -> {
                String tid = entityhandler.initiateTransaction();
                Random random = new Random();
                String delay = ("" + random.nextInt()).substring(2, 5);
                logger.info("+++++>Thread started for tid: " + tid);
                int id = 1;
                int inhabitants = 0;

                List<Object[]> selresults = (List<Object[]>) entityhandler.performRQueryDuringTransaction("select id,inhabitants from country where id = 1", tid);
                logger.info("# results: " + selresults.size());
                for (Object list : selresults) {
                    Iterator iterator = ((ArrayList) list).iterator();
                    id = new Integer((Integer) iterator.next());
                    inhabitants = new Integer((Integer) iterator.next());
                }//for

                //first add
                int newvalue = inhabitants + new Integer(delay);
                logger.info("sending update tid: " + tid);
                entityhandler.performCUDQueryDuringTransaction("update country set inhabitants=" + newvalue + " where id = " + id + ";", tid);
                logger.info("update returned tid: " + tid);

                try {
                    Thread.sleep(new Integer(delay));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }

                selresults = (List<Object[]>) entityhandler.performRQueryDuringTransaction("select id,inhabitants from country where id = 1", tid);
                logger.info("# new results: " + selresults.size());
                for (Object list : selresults) {
                    Iterator iterator = ((ArrayList) list).iterator();
                    id = new Integer((Integer) iterator.next());
                    inhabitants = new Integer((Integer) iterator.next());
                }//for

                //then delete
                newvalue = inhabitants - new Integer(delay);
                logger.info("sending update tid: " + tid);
                entityhandler.performCUDQueryDuringTransaction("update country set inhabitants=" + newvalue + " where id = " + id + ";", tid);
                logger.info("update returned tid: " + tid);

                //commit the transaction
                logger.info("commiting transaction tid: " + tid);
                entityhandler.commitTransaction(tid);
                logger.info("transaction commited  tid: " + tid);

                logger.info("************* Thread terminated " + tid);
                //return
                return true;
            });
        }//for
        try {
            executor.invokeAll(callables)
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .forEach(System.out::println);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
            //TODO add throw
        }
    }//EoM

    public static void singleThreadDistributedTransactionTest(String adapterid) throws ProxyInitializationException, InterruptedException {

//        create handler
        PaaSwordEntityHandler entityhandler = PaaSwordEntityHandler.getInstance(adapterid);

        //Test for Transactions        
        String selq = "select * from country";
        String delq = "delete from country; ";
        String insq = "insert into country (id, name, inhabitants) values ( 10, 'country10', 10 );";
        String updq = "update country set name='italy',inhabitants=1001 where id=10 ; ";

        List<String> res = new ArrayList<>(Arrays.asList("1", "2"));

        String tid = entityhandler.initiateDistributedTransaction(res);
        logger.info("CLIENT--> Distributed Transaction Created: " + tid);
        
        Thread.sleep(5000);
        
        entityhandler.performCUDQueryDuringDistributedTransaction(delq, "1", tid);
        entityhandler.performCUDQueryDuringDistributedTransaction(insq, "1", tid);
        entityhandler.performCUDQueryDuringDistributedTransaction(updq, "1", tid);

        entityhandler.performCUDQueryDuringDistributedTransaction(delq, "2", tid);
        entityhandler.performCUDQueryDuringDistributedTransaction(insq, "2", tid);
        entityhandler.performCUDQueryDuringDistributedTransaction(updq, "2", tid);

        List<Object[]> results = entityhandler.performRQueryDuringDistributedTransaction(selq, "1",tid);
        logger.info("#results: " + results.size());
//
//        entityhandler.performCUDQueryDuringTransaction(updq, tid);
        entityhandler.commitDistributedTransaction(tid);
    }//EoM    

    public static void multiThreadedDistributedTransactionTest(String adapterid, int numofthreads) throws ProxyInitializationException, InterruptedException {

        PaaSwordEntityHandler entityhandler = PaaSwordEntityHandler.getInstance(adapterid);

        //prepare the environment for clean experiment
        List<String> res = new ArrayList<>(Arrays.asList("1", "2"));

        String inittid = entityhandler.initiateDistributedTransaction(res);
        //DB-1
        entityhandler.performCUDQueryDuringDistributedTransaction("delete from country", "1", inittid);
        entityhandler.performCUDQueryDuringDistributedTransaction("INSERT INTO country (id, name, inhabitants) VALUES ( 1, 'country1', 100 );", "1", inittid);
        //DB-2
        entityhandler.performCUDQueryDuringDistributedTransaction("delete from country", "2", inittid);
        entityhandler.performCUDQueryDuringDistributedTransaction("INSERT INTO country (id, name, inhabitants) VALUES ( 1, 'country2', 100 );", "2", inittid);
        //commit        
        entityhandler.commitDistributedTransaction(inittid);

        logger.info("Sleeping before all threads start");
        Thread.sleep(new Integer(500));

        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Boolean>> callables = new ArrayList<>();

        for (int i = 0; i < numofthreads; i++) {
            callables.add(() -> {

                String tid = entityhandler.initiateDistributedTransaction(res);
                Random random = new Random();
                String delay = ("" + random.nextInt()).substring(2, 5);
                logger.info("+++++>Thread started for tid: " + tid);
                int id = 1;
                int inhabitants = 0;

                //DB1
                List<Object[]> selresults = (List<Object[]>) entityhandler.performRQueryDuringDistributedTransaction("select id,inhabitants from country where id = 1", "1", tid);
                logger.info("# results: " + selresults.size());
                for (Object list : selresults) {
                    Iterator iterator = ((ArrayList) list).iterator();
                    id = new Integer((Integer) iterator.next());
                    inhabitants = new Integer((Integer) iterator.next());
                }//for

                //first add
                int newvalue = inhabitants + new Integer(delay);
                logger.info("sending update tid: " + tid);
                entityhandler.performCUDQueryDuringDistributedTransaction("update country set inhabitants=" + newvalue + " where id = " + id + ";", "1", tid);
                logger.info("update returned tid: " + tid);

                try {
                    Thread.sleep(new Integer(delay));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }

                selresults = (List<Object[]>) entityhandler.performRQueryDuringDistributedTransaction("select id,inhabitants from country where id = 1", "1", tid);
                logger.info("# new results: " + selresults.size());
                for (Object list : selresults) {
                    Iterator iterator = ((ArrayList) list).iterator();
                    id = new Integer((Integer) iterator.next());
                    inhabitants = new Integer((Integer) iterator.next());
                }//for

                //then delete
                newvalue = inhabitants - new Integer(delay);
                logger.info("sending update tid: " + tid);
                entityhandler.performCUDQueryDuringDistributedTransaction("update country set inhabitants=" + newvalue + " where id = " + id + ";", "1", tid);
                logger.info("update returned tid: " + tid);

                //DB2
                selresults = (List<Object[]>) entityhandler.performRQueryDuringDistributedTransaction("select id,inhabitants from country where id = 1", "2", tid);
                logger.info("# results: " + selresults.size());
                for (Object list : selresults) {
                    Iterator iterator = ((ArrayList) list).iterator();
                    id = new Integer((Integer) iterator.next());
                    inhabitants = new Integer((Integer) iterator.next());
                }//for

                //first add
                newvalue = inhabitants + new Integer(delay);
                logger.info("sending update tid: " + tid);
                entityhandler.performCUDQueryDuringDistributedTransaction("update country set inhabitants=" + newvalue + " where id = " + id + ";", "2", tid);
                logger.info("update returned tid: " + tid);

//                try {
//                    Thread.sleep(new Integer(delay));
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//                }
                selresults = (List<Object[]>) entityhandler.performRQueryDuringDistributedTransaction("select id,inhabitants from country where id = 1", "2", tid);
                logger.info("# new results: " + selresults.size());
                for (Object list : selresults) {
                    Iterator iterator = ((ArrayList) list).iterator();
                    id = new Integer((Integer) iterator.next());
                    inhabitants = new Integer((Integer) iterator.next());
                }//for

                //then delete
                newvalue = inhabitants - new Integer(delay);
                logger.info("sending update tid: " + tid);
                entityhandler.performCUDQueryDuringDistributedTransaction("update country set inhabitants=" + newvalue + " where id = " + id + ";", "2", tid);
                logger.info("update returned tid: " + tid);

                //commit the transaction
                logger.info("commiting distributed transaction tid: " + tid);
                entityhandler.commitDistributedTransaction(tid);
                logger.info("transaction commited  tid: " + tid);

                logger.info("************* Thread terminated " + tid);
                //return
                return true;
            });
        }//for
        try {
            executor.invokeAll(callables)
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .forEach(System.out::println);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
            //TODO add throw
        }
    }//EoM    

}//class
