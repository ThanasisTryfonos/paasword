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
package eu.paasword.jpa.client;

import eu.paasword.annotations.PaaSwordEntity;
import eu.paasword.jpa.PaaSwordEntityHandler;
import eu.paasword.jpa.client.entities.*;
import eu.paasword.jpa.exceptions.NotAValidPaaSwordEntityException;
import eu.paasword.jpa.exceptions.ProxyInitializationException;
import eu.paasword.jpa.exceptions.QueryException;
import eu.paasword.rest.dbproxy.transferobject.TQuery;
import eu.paasword.rest.response.PaaSwordObjectResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ubuntu
 */
public class TransactionTests {

    private static final Logger logger = Logger.getLogger(TransactionTests.class.getName());
    private static final String PROXYID = "00c34888-50c6-4368-a5da-da2e8e321836";

    public static void main(String[] args) throws ProxyInitializationException, InterruptedException {
        //Run Initialize Database FIrst
        //-------Do not parallelize it since it is using distributed transaction
//      for (int i = 0; i < 5; i++)
      TransactionTests.SingleTransactionsTest(PROXYID);       
//      TransactionTests.CompoundTransactionTest(PROXYID);   
//      TransactionTests.multiThreadedTransactionTest(PROXYID, 2 );
        
        
    }//EoM

//------------------------------------Executed in the PaaSword DBProxy    
    
    public static void SingleTransactionsTest(String adapterid) throws ProxyInitializationException {
//        create handler
        PaaSwordEntityHandler entityhandler = PaaSwordEntityHandler.getInstance(adapterid);

//        //Query-1 delete everything
//        entityhandler.customQuery("delete from role ;");
//
//        //Query-2 insert
//        entityhandler.customQuery("INSERT INTO role (id, actor, description) VALUES (1, 'act1', 'desc111');");
//        entityhandler.customQuery("INSERT INTO role (id, actor, description) VALUES (2, 'act2', 'desc222');");
//
//        //Query-3
//        entityhandler.customQuery("update role set actor='act11' where id=1 ;");
//        entityhandler.customQuery("update role set actor='act22' where id=2 ;");
//
//        //Query-4
        List<Map<String, String>> customQuery = entityhandler.customQuery("select * from role ;");
        logger.info(customQuery.toString());
    
    }//EoM
           
    public static void CompoundTransactionTest(String adapterid) throws ProxyInitializationException {

//        create handler
        PaaSwordEntityHandler entityhandler = PaaSwordEntityHandler.getInstance(adapterid);

        //Test for Transactions        
        String selq = "select * from role";
        String insq = "insert into role (id, actor, description) VALUES ( 11, 'act11', 'descr11' );";
        String updq = "update role set actor='act112',description='descr112' where id=11 ; ";
        String delq = "delete from role; ";

        String tid = entityhandler.initiateTransaction();
        logger.info("Transactionid: " + tid);

        entityhandler.performCUDQueryDuringTransaction(delq, tid);
        entityhandler.performCUDQueryDuringTransaction(insq, tid);
        List<Map<String, String>> results = entityhandler.performRQueryDuringTransaction(selq, tid);
        logger.info("#results: " + results.size() + " "+results.toString());
        entityhandler.performCUDQueryDuringTransaction(updq, tid);
        List<Map<String, String>> results2 = entityhandler.performRQueryDuringTransaction(selq, tid);
        logger.info("#results: " + results2.size() + " "+results2.toString());
        
        entityhandler.commitTransaction(tid);
    }//EoM    

    public static void multiThreadedTransactionTest(String adapterid, int numofthreads) throws ProxyInitializationException, InterruptedException {

        PaaSwordEntityHandler entityhandler = PaaSwordEntityHandler.getInstance(adapterid);

        //prepare the environment for clean experiment
        String inittid = entityhandler.initiateTransaction();
        entityhandler.performCUDQueryDuringTransaction("delete from role", inittid);
        entityhandler.performCUDQueryDuringTransaction("INSERT INTO role (id, actor, description) VALUES ( 1, '1', 'description1' );", inittid);
        entityhandler.commitTransaction(inittid);

        logger.info("Sleeping before all threads start");
        Thread.sleep(new Integer(2000));

        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Boolean>> callables = new ArrayList<>();

        for (int i = 0; i < numofthreads; i++) {
            callables.add(() -> {
                //Initiate Transaction
                String tid = entityhandler.initiateTransaction();
                Random random = new Random();
                String delay = ("" + random.nextInt()).substring(2, 5);
                logger.info("+++++>Thread started for tid: " + tid);
                int id = 1;
                int variableoffset = 0;

                List<Map<String, String>> selresults = entityhandler.performRQueryDuringTransaction("select id,actor,description from role where id = 1", tid);
                logger.info("# results: " + selresults.size());
                variableoffset = Integer.parseInt(selresults.get(0).get("actor"));

                //first add
                int newvalue = variableoffset + new Integer(delay);
                logger.info("sending update tid: " + tid);
                entityhandler.performCUDQueryDuringTransaction("update role set actor=" + newvalue + " where id = " + id + ";", tid);
                logger.info("update returned tid: " + tid);

                try {
                    Thread.sleep(new Integer(delay));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    logger.log(Level.SEVERE, null, ex);
                }

                selresults = entityhandler.performRQueryDuringTransaction("select id,actor from role where id = 1", tid);
                logger.info("# new results: " + selresults.size());
                variableoffset = Integer.parseInt(selresults.get(0).get("actor"));

                //then delete
                newvalue = variableoffset - new Integer(delay);
                logger.info("sending update tid: " + tid);
                entityhandler.performCUDQueryDuringTransaction("update role set actor=" + newvalue + " where id = " + id + ";", tid);
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


}//EoC
