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
package eu.paasword.rest.dbproxy;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.ParseException;
import eu.paasword.api.repository.IAPIKeyService;
import eu.paasword.api.repository.IApplicationInstanceService;
import eu.paasword.api.repository.IApplicationService;
import eu.paasword.api.repository.exception.apikey.APIKeyUniqueIDDoesNotExist;
import eu.paasword.dbproxy.DatabaseProxyEngine;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.helper.AdapterHelper;
import eu.paasword.dbproxy.helper.QueryHelper;
import eu.paasword.dbproxy.impl.Adapter;
import eu.paasword.dbproxy.output.OutputHandler;
import eu.paasword.dbproxy.transaction.DistributedTransactionalManager;
import eu.paasword.dbproxy.transaction.TransactionalManager;
import eu.paasword.repository.relational.domain.APIKey;
import eu.paasword.repository.relational.domain.Application;
import eu.paasword.repository.relational.domain.ApplicationInstance;
import eu.paasword.rest.dbproxy.transferobject.TDistributedTransactionContext;
import eu.paasword.rest.dbproxy.transferobject.TDistributedTransactionInit;
import eu.paasword.rest.dbproxy.transferobject.TQuery;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordObjectResponse;
import eu.paasword.rest.response.PaaSwordRestResponse;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the rest endpoints regarding query handling of DB Proxy
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/query")
public class QueryHandlingManagementRestController {

    private static final Logger logger = Logger.getLogger(QueryHandlingManagementRestController.class.getName());

    @Autowired
    IApplicationInstanceService<ApplicationInstance> applicationInstanceService;

    @Autowired
    IAPIKeyService<APIKey> apiKeyService;

    @Autowired
    DatabaseProxyEngine databaseProxyEngine;

    /**
     * Hello from DB Proxy
     *
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(method = RequestMethod.GET)
    public PaaSwordRestResponse hello() {
        return new PaaSwordRestResponse(BasicResponseCode.SUCCESS, "Hello from DB Proxy API!", Optional.empty());
    }

    /**
     * The exposed endpoint is used via the UI, which attempts to execute a
     * query
     *
     * @param query A JSON object which will be casted to a TQuery (java) object
     * @return PaaSwordRestResponse object
     */
    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    public PaaSwordObjectResponse queryHandling(@RequestBody TQuery query) {

        // TODO Validate user key if exists
//        if (null != query.getUserKey() && !query.getUserKey().isEmpty()
//                && null != query.getUserPrincipal() && !query.getUserPrincipal().isEmpty()) {
//
//            try {
//                String appKey = query.getAppInstanceAPIKey();
//                Application application = ((APIKey) apiKeyService.findByUniqueID(appKey).get()).getApplicationID();
//
//                if (null != application) {
//
//                    String tenantKey = application.getTenantKey();
//
//                    // TODO Call method to check if user key, principal match tenant key
//                    boolean keyOk = true;
//                    if (!keyOk) {
//                        return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.DBPROXYERROR, Optional.empty());
//                    }
//
//                } else {
//                    return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.DBPROXYERROR, Optional.empty());
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.DBPROXYERROR, Optional.empty());
//            }
//
//        }
        try {
            //create transaction
            DistributedTransactionalManager dtm = AdapterHelper.getDTMByAdapterId(query.getAppInstanceAPIKey());
            String sessionid = dtm.initiateTransaction();
            logger.info("Getting adapter for " + query.getAppInstanceAPIKey());
            Adapter adapter = AdapterHelper.getAdapter(query.getAppInstanceAPIKey(), null, sessionid);
//            logger.info("Adapters " + query.getAppInstanceAPIKey());
            OutputHandler queryoutput = adapter.query(query.getQuery(), sessionid);
            //commit
            dtm.commitTransaction(sessionid);
            //handle results
            List<Map<String, String>> results = QueryHelper.getSerializedOutput(queryoutput);
            logger.info(query.getAppInstanceAPIKey() + "-Query1 '" + query.getQuery() + "' returned #results: " + results.size());
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.QUERY_EXECUTED_SUCCESSFULLY, results);
        } catch (Exception ex) {
            Logger.getLogger(QueryHandlingManagementRestController.class.getName()).log(Level.SEVERE, null, ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.DBPROXYERROR, Optional.empty());
        }
    }//EoM

    @RequestMapping(value = "/executeraw", method = RequestMethod.POST)
    public PaaSwordObjectResponse queryHandlingRaw(@RequestBody TQuery query) {

        // TODO Validate user key if exists
        Adapter adapter = AdapterHelper.getAdapter(query.getAppInstanceAPIKey(), null, "panos");
        try {
            OutputHandler queryoutput = adapter.query(query.getQuery(), "panos");

            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.QUERY_EXECUTED_SUCCESSFULLY, QueryHelper.formatForWeb(queryoutput));
        } catch (ParseException | DatabaseException | StandardException | NullPointerException ex) {
            Logger.getLogger(QueryHandlingManagementRestController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), "Error during query processing!", Optional.empty());
        }
    }//EoM

    // TODO Check for Tenant Key
    //----------------Single-IaaS Transactions----------------------------------    
    @RequestMapping(value = "/executetransactioninit", method = RequestMethod.POST)
    public PaaSwordObjectResponse initiatetransaction(@RequestBody String adapterid) {
        try {
            logger.info("REQUEST+++++++> For New Transaction ");
            DistributedTransactionalManager dtm = AdapterHelper.getDTMByAdapterId(adapterid);
            String sessionid = dtm.initiateTransaction();
            logger.info("RESPONSE+++++++>New Transaction: " + sessionid);
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.TRANSACTION_INITIATED_SUCCESSFULLY, sessionid);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.TRANSACTIONERROR, Optional.empty());
        }
    }//EoM    

    @RequestMapping(value = "/executetransactioncommit", method = RequestMethod.POST)
    public PaaSwordObjectResponse committransaction(@RequestBody String adapterid_sessionid) {
        try {
            String adapterid = adapterid_sessionid.split("_")[0];
            String sessionid = adapterid_sessionid.split("_")[1];

            logger.info("REQUEST------> For Committing Transaction ");
            DistributedTransactionalManager dtm = AdapterHelper.getDTMByAdapterId(adapterid);
            logger.info("Commiting Transaction: " + sessionid);
            dtm.commitTransaction(sessionid);
            logger.info("RESPONSE---->Commited Transaction: " + sessionid);
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.TRANSACTION_COMMITED_SUCCESSFULLY, "ok");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.TRANSACTIONERROR, Optional.empty());
        }
    }//EoM     

    @RequestMapping(value = "/executerawcudtransaction", method = RequestMethod.POST)
    public PaaSwordObjectResponse queryHandlingOfCUDRawWithTransaction(@RequestBody TQuery query) {
        try {

            String sessionid = query.getTransactionid();
            Adapter adapter = AdapterHelper.getAdapter(query.getAppInstanceAPIKey(), null, sessionid);
            OutputHandler queryoutput = adapter.query(query.getQuery(), sessionid);
            List<Map<String, String>> results = QueryHelper.getSerializedOutput(queryoutput);
            logger.info("Transaction: " + sessionid + " for Query: " + query.getQuery());
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.QUERY_EXECUTED_SUCCESSFULLY, "ok");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), "Error during query processing!", Optional.empty());
        }
    }//EoM        

    @RequestMapping(value = "/executerawrtransaction", method = RequestMethod.POST)
    public PaaSwordObjectResponse queryHandlingOfRRawWithTransaction(@RequestBody TQuery query) {
        try {
            String sessionid = query.getTransactionid();
            Adapter adapter = AdapterHelper.getAdapter(query.getAppInstanceAPIKey(), null, sessionid);
            OutputHandler queryoutput = adapter.query(query.getQuery(), sessionid);
            List<Map<String, String>> results = QueryHelper.getSerializedOutput(queryoutput);
//            List<Object[]> results = tm.executeRDuringTransaction(qr, tid);
            logger.info("Transaction: " + sessionid + " for Query: " + query.getQuery());
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.QUERY_EXECUTED_SUCCESSFULLY, results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), "Error during query processing!", Optional.empty());
        }
    }//EoM            

    //----------------Distributed Transactions----------------------------------
    @RequestMapping(value = "/executedistributedtransactioninit", method = RequestMethod.POST)
    public PaaSwordObjectResponse initiatedistributedtransaction(@RequestBody TDistributedTransactionInit resources) {
        try {
            logger.info("REST--> Request For New DistributedTransaction ");
            DistributedTransactionalManager tm = AdapterHelper.getDTMByAdapterId(resources.getAppinstanceid());
            //Generate new Transaction Identifier
            String tid = tm.initiateTransaction();
            logger.info("REST--> Response New Distributed Transaction: " + tid);
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.TRANSACTION_INITIATED_SUCCESSFULLY, tid);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.TRANSACTIONERROR, Optional.empty());
        }
    }//EoM    

    @RequestMapping(value = "/executedistributedtransactioncommit", method = RequestMethod.POST)
    public PaaSwordObjectResponse commitdistributedtransaction(@RequestBody TDistributedTransactionContext transcontext) {
        try {
            logger.info("REST--> Request For Committing Distributed Transaction ");
            DistributedTransactionalManager tm = AdapterHelper.getDTMByAdapterId(transcontext.getAppinstanceid());
            logger.info("REST--> Commiting Distributed Transaction: " + transcontext.getTid());
            tm.commitTransaction(transcontext.getTid());
            logger.info("REST--> Response Commited Distributed Transaction: " + transcontext.getTid());
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.TRANSACTION_COMMITED_SUCCESSFULLY, "ok");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), Message.TRANSACTIONERROR, Optional.empty());
        }
    }//EoM     

    @RequestMapping(value = "/executerawcuddistributedtransaction", method = RequestMethod.POST)
    public PaaSwordObjectResponse queryHandlingOfCUDRawWithDistributedTransaction(@RequestBody TQuery query) {
        try {
            logger.info("REST-->For CUD Distributed Transaction ");
            DistributedTransactionalManager tm = AdapterHelper.getDTMByAdapterId(query.getAppInstanceAPIKey());
            String qr = query.getQuery();
            String tid = query.getTransactionid();
            String rid = query.getResid();
            tm.executeCUDQueryDuringTransaction(qr, rid, tid);
            logger.info("REST--> Distributed Transaction: " + tid + " for Query: " + query.getQuery());
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.QUERY_EXECUTED_SUCCESSFULLY, "ok");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), "Error during query processing!", Optional.empty());
        }
    }//EoM        

    @RequestMapping(value = "/executerawrdistributedtransaction", method = RequestMethod.POST)
    public PaaSwordObjectResponse queryHandlingOfRRawWithDistributedTransaction(@RequestBody TQuery query) {
        try {
            DistributedTransactionalManager tm = AdapterHelper.getDTMByAdapterId(query.getAppInstanceAPIKey()); //DistributedTransactionalManager.getInstance();
            String qr = query.getQuery();
            String tid = query.getTransactionid();
            String rid = query.getResid();
            List<Object[]> results = tm.executeRDuringDistributedTransaction(qr, rid, tid);
            logger.info("REST--> Distributed Transaction: " + tid + " for Query: " + query.getQuery());
            return new PaaSwordObjectResponse(BasicResponseCode.SUCCESS.name(), Message.QUERY_EXECUTED_SUCCESSFULLY, results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new PaaSwordObjectResponse(BasicResponseCode.EXCEPTION.name(), "Error during query processing!", Optional.empty());
        }
    }//EoM

    /**
     * Inner class containing all the static messages which will be used in an
     * PaaSwordRestResponse.
     */
    private final static class Message {

        final static String GENERAL_ERROR = "General Error";
        final static String AUTHORIZATION_FAILED = "Authorization failed";
        final static String QUERY_INVALID = "This query is invalid";
        final static String QUERY_EXECUTED_SUCCESSFULLY = "Query executed successfully";
        final static String TRANSACTION_INITIATED_SUCCESSFULLY = "Transaction initiated successfully";
        final static String TRANSACTION_COMMITED_SUCCESSFULLY = "Transaction commited successfully";
        final static String DBPROXYERROR = "Database Proxy Error";
        final static String TRANSACTIONERROR = "Transaction Error";
    }//Message

}
