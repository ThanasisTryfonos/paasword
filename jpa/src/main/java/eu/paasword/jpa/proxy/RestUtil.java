/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.paasword.jpa.proxy;

import eu.paasword.jpa.exceptions.QueryException;
import eu.paasword.rest.dbproxy.transferobject.TDistributedTransactionContext;
import eu.paasword.rest.dbproxy.transferobject.TDistributedTransactionInit;
import eu.paasword.rest.dbproxy.transferobject.TQuery;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordObjectResponse;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class RestUtil {

    private static final Logger logger = Logger.getLogger(RestUtil.class.getName());

    public static boolean checkProxy(String uri, String appinstanceid) {
        boolean proxyisworking = false;
        final String qargument = "select 1";
        TQuery queryarg = new TQuery(appinstanceid, appinstanceid, qargument);
        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse< List< Map<String, String>>> result = null;
        try {
            result = restTemplate.postForObject(uri, queryarg, PaaSwordObjectResponse.class);
            proxyisworking = result.getCode().equalsIgnoreCase(BasicResponseCode.SUCCESS.name());
            logger.info("Test query during bootstrapping returned #rows: " + result.getReturnobject().size());
        } catch (Exception ex) {
            proxyisworking = false;
        }
        return proxyisworking;
    }//EoM    

    public static List< Map<String, String>> performQuery(String uri, String appinstanceid, String query, String userKey, String userPrincipal) throws QueryException {
        TQuery queryarg = new TQuery(appinstanceid, appinstanceid, query);

        if (null != userKey && !userKey.isEmpty()
                && null != userPrincipal && !userPrincipal.isEmpty()) {

            queryarg.setUserKey(userKey);
            queryarg.setUserPrincipal(userPrincipal);

        }

        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse< List< Map<String, String>>> result = null;
        try {
            result = restTemplate.postForObject(uri, queryarg, PaaSwordObjectResponse.class);
            logger.info("Query '" + query + "' returned #rows: " + result.getReturnobject().size());
        } catch (Exception ex) {
            throw new QueryException("Exception during query execution");
        }
        return result.getReturnobject();
    }//EoM       

    //----------------SingleDB Transaction---------    
    public static String initiateTransaction(String uri, String appinstanceid) throws QueryException {
        uri += "transactioninit";
        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse< String> result = null;
        try {
            result = restTemplate.postForObject(uri, appinstanceid, PaaSwordObjectResponse.class);
            logger.info("initiateTransaction returned: " + result.getReturnobject());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new QueryException("Exception during query execution" + ex.getStackTrace());
        }
        return result.getReturnobject();
    }//EoM   

    public static String commitTransaction(String uri, String appinstanceid, String tid) throws QueryException {
        uri += "transactioncommit";
        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse< String> result = null;
        try {
            result = restTemplate.postForObject(uri, appinstanceid+"_"+tid, PaaSwordObjectResponse.class);
            logger.info("CommitTransaction returned: " + result.getReturnobject());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new QueryException("Exception during query execution" + ex.getStackTrace());
        }
        return result.getReturnobject();
    }//EoM   

    public static String performRawCUDQueryDuringTransaction(String uri, String appinstanceid, String query, String tid) throws QueryException {
        uri += "rawcudtransaction";
        TQuery queryarg = new TQuery(appinstanceid, appinstanceid, query, tid);
        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse<String> result = null;
        try {
            result = restTemplate.postForObject(uri, queryarg, PaaSwordObjectResponse.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new QueryException("REST CLIENT-->Exception during query execution");
        }
        return result.getReturnobject();
    }//EoM        

    public static List<Map<String, String>> performRawRQueryDuringTransaction(String uri, String appinstanceid, String query, String tid) throws QueryException {
        uri += "rawrtransaction";
        TQuery queryarg = new TQuery(appinstanceid, appinstanceid, query, tid);
        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse<List<Map<String, String>>> result = null;
        try {
            result = restTemplate.postForObject(uri, queryarg, PaaSwordObjectResponse.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new QueryException("REST CLIENT--> Exception during query execution");
        }
        return result.getReturnobject();
    }//EoM        

    //----------------Distributed Transaction---------
    public static String initiateDistributedTransaction(String uri, String appinstanceid, List<String> resources) throws QueryException {
        uri += "distributedtransactioninit";
        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse< String> result = null;
        try {
            TDistributedTransactionInit tdrs = new TDistributedTransactionInit(appinstanceid,resources);
            result = restTemplate.postForObject(uri, tdrs, PaaSwordObjectResponse.class);
            logger.info("REST CLIENT--> Initiate Distributed Transaction returned: " + result.getReturnobject());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new QueryException("REST CLIENT--> Exception during query execution" + ex.getStackTrace());
        }
        return result.getReturnobject();
    }//EoM     

    public static String commitDistributedTransaction(String uri, String appinstanceid, String tid) throws QueryException {
        uri += "distributedtransactioncommit";
        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse< String> result = null;
        try {
            TDistributedTransactionContext transcontext = new TDistributedTransactionContext(appinstanceid, tid);
            result = restTemplate.postForObject(uri, transcontext, PaaSwordObjectResponse.class);
            logger.info("REST CLIENT--> Commit Distributed Transaction returned: " + result.getReturnobject());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new QueryException("REST CLIENT-->Exception during query execution" + ex.getStackTrace());
        }
        return result.getReturnobject();
    }//EoM     

    public static String performRawCUDQueryDuringDistributedTransaction(String uri, String appinstanceid, String query, String resid, String tid) throws QueryException {
        uri += "rawcuddistributedtransaction";
        TQuery queryarg = new TQuery(appinstanceid, appinstanceid, query, resid, tid);
        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse<String> result = null;
        try {
            result = restTemplate.postForObject(uri, queryarg, PaaSwordObjectResponse.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new QueryException("REST CLIENT--> Exception during query execution");
        }
        return result.getReturnobject();
    }//EoM        

    public static List<Object[]> performRawRQueryDuringDistributedTransaction(String uri, String appinstanceid, String query, String resid, String tid) throws QueryException {
        uri += "rawrdistributedtransaction";
        TQuery queryarg = new TQuery(appinstanceid, appinstanceid, query, resid, tid);
        RestTemplate restTemplate = new RestTemplate();
        PaaSwordObjectResponse<List<Object[]>> result = null;
        try {
            result = restTemplate.postForObject(uri, queryarg, PaaSwordObjectResponse.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new QueryException("REST CLIENT--> Exception during query execution");
        }
        return result.getReturnobject();
    }//EoM      
    
    
}//EoC
