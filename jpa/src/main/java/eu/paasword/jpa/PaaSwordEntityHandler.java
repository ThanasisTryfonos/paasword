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
package eu.paasword.jpa;

import eu.paasword.jpa.exceptions.NotAValidPaaSwordEntityException;
import eu.paasword.jpa.exceptions.ProxyInitializationException;
import eu.paasword.jpa.exceptions.QueryException;
import eu.paasword.jpa.introspection.IntrospectionUtil;
import eu.paasword.jpa.proxy.RestUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class PaaSwordEntityHandler {

    private static final Logger logger = Logger.getLogger(PaaSwordEntityHandler.class.getName());

    private static PaaSwordEntityHandler instance = null;
    private String appinstnceid = "733759";
    private String uri = "http://127.0.0.1:8080/api/v1/query/execute";

    public PaaSwordEntityHandler() throws ProxyInitializationException {
        initializeHandler();
    }

    public PaaSwordEntityHandler(String appinstnceid) throws ProxyInitializationException {
        this.appinstnceid = appinstnceid;
        initializeHandler();
    }

    public PaaSwordEntityHandler(String uri, String appinstnceid) throws ProxyInitializationException {
        this.uri = uri;
        this.appinstnceid = appinstnceid;
        initializeHandler();
    }

    private void initializeHandler() throws ProxyInitializationException {
        //Step 1 - check connectivity with the Proxy
        if (RestUtil.checkProxy(uri, appinstnceid) == false) {
            throw new ProxyInitializationException("Proxy is not configured or not reachable");
        }

        //Step 2 - introspect all classes to identity the entity model
//        List<Class> paaswordenityclasses = IntrospectionUtil.getPaaSwordEntities();
//        //generate exception if not existing entity
//        if (paaswordenityclasses.isEmpty()) {
//            throw new ProxyInitializationException("Problem in the Entity Model. No Entity Model found!");
//        }
        //Step 3 - Validate the model
//        try {
//            List<String> createtablecommands = PaaSwordQueryHandler.generateOrderedCreateTableStatementsForManyClasses(paaswordenityclasses);
//            for (String command : createtablecommands) {
//                logger.info("Command: " + command);
//            }
//        } catch (CyclicDependencyException | NotAValidPaaSwordEntityException | NoClassToProcessException | UnSatisfiedDependencyException ex) {
//            logger.log(Level.SEVERE, null, ex);
//            throw new ProxyInitializationException("Problem in the Entity Model. Cyclic Dependencies Identified.");
//        }
    }//EoM   initializeHandler()  

    public static PaaSwordEntityHandler getInstance() throws ProxyInitializationException {
        if (instance == null) {
            instance = new PaaSwordEntityHandler();
        }
        return instance;
    }//EoM    

    public static PaaSwordEntityHandler getInstance(String appinstnceid) throws ProxyInitializationException {
        if (instance == null) {
            instance = new PaaSwordEntityHandler(appinstnceid);
        }
        return instance;
    }//EoM    

    public static PaaSwordEntityHandler getInstance(String uri, String appinstnceid) throws ProxyInitializationException {
        if (instance == null) {
            instance = new PaaSwordEntityHandler(uri, appinstnceid);
        }
        return instance;
    }//EoM

    public void save(Object obj) throws NotAValidPaaSwordEntityException {
        save(obj, null, null);
    }//EoM

    public void save(Object obj, String userKey, String userPrincipal) throws NotAValidPaaSwordEntityException {

//        List<Object> returnobjects = new ArrayList<>();
        List<Map<String, String>> returnobject = null;
        try {
            Key key = PaaSwordQueryHandler.getPrimaryKeyForClass(obj.getClass());
            String keyvalue = PaaSwordQueryHandler.getValueAsStringForFieldname(obj, key.getFieldname());
            if (keyvalue.equals("0")) {
//                logger.info("No Key is specified so i will perform autoincrement");
                String autoincquery = "select max(" + key.getFieldname() + ") from " + obj.getClass().getSimpleName().toLowerCase();
//                logger.info("autoincquery: " + autoincquery);
                List<Map<String, String>> autoincrementQueryList = customQuery(autoincquery, userKey, userPrincipal);
                Map<String, String> retmap = autoincrementQueryList.get(0);
                String existingvalue = retmap.get("max");
                int newid = 0;
                if (null != existingvalue) {
                    newid = new Integer(existingvalue) + 1;
                } else {
                    newid = 1;
                }
//                logger.info("New ID for the query: " + newid);
                PaaSwordQueryHandler.invokeMethodAndGetObject(obj, key.getFieldname(), "" + newid);
            }//autoincrement
            String query = PaaSwordQueryHandler.generateStoreQuery(obj);
            returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);
//            returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, clazz);     //fetch saved
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }//EoM

    public List<Object> findAll(Class clazz) {
        return findAll(clazz, null, null);
    }//EoM

    public List<Object> findAll(Class clazz, String userKey, String userPrincipal) {
        List<Object> returnobjects = new ArrayList<>();
        String query = "select * from " + clazz.getSimpleName().toLowerCase();
        List<Map<String, String>> returnobject = null;
        try {
            returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);
            returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, clazz);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return returnobjects;
    }//EoM

    public Page<Object> findAll(Class clazz, Pageable pageable) {
        return findAll(clazz, pageable, null, null);
    }//EoM

    public Page<Object> findAll(Class clazz, Pageable pageable, String userKey, String userPrincipal) {
        List<Object> returnobjects = new ArrayList<>();

        int rows = pageable.getPageSize() * pageable.getPageNumber();

        String query = "select * from " + clazz.getSimpleName().toLowerCase() + " limit " + pageable.getPageSize() + " offset " + rows;

        List<Map<String, String>> returnobject = null;

        try {
            returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);
            returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, clazz);

        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        Page<Object> returnPage = new PageImpl(returnobjects, pageable, findAll(clazz, userKey, userPrincipal).size());

        return returnPage;
    }//EoM

    public Page<Object> findByReferenceID(Class clazz, Class referenceClazz, Long id, Pageable pageable) {
        return findByReferenceID(clazz, referenceClazz, id, pageable, null, null);
    }//EoM

    public Page<Object> findByReferenceID(Class clazz, Class referenceClazz, Long id, Pageable pageable, String userKey, String userPrincipal) {
        List<Object> returnobjects = new ArrayList<>();

        int rows = pageable.getPageSize() * pageable.getPageNumber();

        String query = "select * from " + clazz.getSimpleName().toLowerCase() + " where fk_" + referenceClazz.getSimpleName().toLowerCase() + "_" + referenceClazz.getSimpleName().toLowerCase() + " = " + id + " limit " + pageable.getPageSize() + " offset " + rows;
        List<Map<String, String>> returnobject = null;
        try {
            returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);
            returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, clazz);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        Page<Object> returnPage = new PageImpl(returnobjects, pageable, findByReferenceID(clazz, referenceClazz, id, userKey, userPrincipal).size());

        return returnPage;
    }//EoM

    public List<Object> findByReferenceID(Class clazz, Class referenceClazz, Long id) {
        return findByReferenceID(clazz, referenceClazz, id, null, null);
    }//EoM

    public List<Object> findByReferenceID(Class clazz, Class referenceClazz, Long id, String userKey, String userPrincipal) {
        List<Object> returnobjects = new ArrayList<>();
        String query = "select * from " + clazz.getSimpleName().toLowerCase() + " where fk_" + referenceClazz.getSimpleName().toLowerCase() + "_" + referenceClazz.getSimpleName().toLowerCase() + " = " + id;
        List<Map<String, String>> returnobject = null;
        try {
            returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);
            returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, clazz);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return returnobjects;
    }//EoM

    public List<Object> findByField(Class clazz, String fieldName, String value) {
        return findByField(clazz, fieldName, value, null, null);
    }//EoM

    public List<Object> findByField(Class clazz, String fieldName, String value, String userKey, String userPrincipal) {
        List<Object> returnobjects = new ArrayList<>();
        String query = "select * from " + clazz.getSimpleName().toLowerCase() + " where " + fieldName.toLowerCase() + " = '" + value + "'";
        List<Map<String, String>> returnobject = null;
        try {
            returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);
            returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, clazz);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return returnobjects;
    }//EoM

    public void delete(Long id, Class clazz) {
        delete(id, clazz, null, null);
    }//EoM

    public void delete(Long id, Class clazz, String userKey, String userPrincipal) {
        List<Object> returnobjects = new ArrayList<>();
        String query = "delete from " + clazz.getSimpleName().toLowerCase() + " where id=" + id;
        List<Map<String, String>> returnobject = null;
        try {
            returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);
            returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, clazz);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }//EoM

    public void delete(Object obj) {
        delete(obj, null, null);
    }//EoM

    public void delete(Object obj, String userKey, String userPrincipal) {
        Key key;
        try {
            key = PaaSwordQueryHandler.getPrimaryKeyForClass(obj.getClass());
            String value = PaaSwordQueryHandler.getValueAsStringForFieldname(obj, key.getFieldname());
            logger.info(key.getFieldname() + " " + key.getFieldtype());
            List<Object> returnobjects = new ArrayList<>();
            String query = "delete from " + obj.getClass().getSimpleName().toLowerCase() + " where " + key.getFieldname() + "=" + value;
            List<Map<String, String>> returnobject = null;
            returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);
            returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, obj.getClass());
        } catch (QueryException | NotAValidPaaSwordEntityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }//EoM

    public Object findOne(Long id, Class clazz) {
        return findOne(id, clazz, null, null);
    }//EoM

    public Object findOne(Long id, Class clazz, String userKey, String userPrincipal) {

        List<Object> returnobjects = new ArrayList<>();
        String query = "select * from " + clazz.getSimpleName().toLowerCase() + " where id = " + id;
        List<Map<String, String>> returnobject = null;
        try {
            returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);

            returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, clazz);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        if (!returnobjects.isEmpty()) {

            return returnobjects.get(0);
        } else {
            return null;
        }
    }//EoM

    public void update(Object obj) throws NotAValidPaaSwordEntityException, QueryException {
        update(obj, null, null);
    }//EoM

    public void update(Object obj, String userKey, String userPrincipal) throws NotAValidPaaSwordEntityException, QueryException {

        List<Object> returnobjects = new ArrayList<>();
        List<Map<String, String>> returnobject = null;

        Key key = PaaSwordQueryHandler.getPrimaryKeyForClass(obj.getClass());
        String keyvalue = PaaSwordQueryHandler.getValueAsStringForFieldname(obj, key.getFieldname());

        if (keyvalue.equals("0")) {
            throw new QueryException("No primary key is specified!");
        } else {

            Object currentObj = findOne(Long.valueOf(keyvalue), obj.getClass(), userKey, userPrincipal);

            if (null != currentObj) {

                key.setFieldValue(keyvalue);
                String query = PaaSwordQueryHandler.generateUpdateQuery(key, obj);
                returnobject = RestUtil.performQuery(uri, appinstnceid, query, userKey, userPrincipal);
                returnobjects = PaaSwordQueryHandler.generateCastedObjectsFromInstances(returnobject, obj.getClass());

            } else {
                throw new QueryException(obj.getClass().getCanonicalName() + " with primary key '" + keyvalue + "' doesn't exist!");
            }

        }

    }//EoM

    public List<Map<String, String>> customQuery(String namedQuery) {
        return customQuery(namedQuery, null, null);
    }//EoM

    public List<Map<String, String>> customQuery(String namedQuery, String userKey, String userPrincipal) {
        List<Map<String, String>> returnobject = null;
        try {
            returnobject = RestUtil.performQuery(uri, appinstnceid, namedQuery, userKey, userPrincipal);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return returnobject;
    }//EoM

    //------------SingleDB Transaction //TODO TenantKey

    public String initiateTransaction() {
        String tid = null;
        try {
            tid = RestUtil.initiateTransaction(uri, appinstnceid);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return tid;
    }//ΕοΜ

    public String commitTransaction(String tid) {
        String result = null;
        try {
            result = RestUtil.commitTransaction(uri, appinstnceid, tid);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return result;
    }//ΕοΜ

    public String performCUDQueryDuringTransaction(String namedQuery, String tid) {
        String returnobject = null;
        try {
            returnobject = RestUtil.performRawCUDQueryDuringTransaction(uri, appinstnceid, namedQuery, tid);
            logger.info("Executed Part of Transaction: " + namedQuery);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return returnobject;
    }//EoM    

    public List<Map<String, String>> performRQueryDuringTransaction(String namedQuery, String tid) {
        List<Map<String, String>> returnobject = null;
        try {
            returnobject = RestUtil.performRawRQueryDuringTransaction(uri, appinstnceid, namedQuery, tid);
            logger.info("Executed Part of Transaction: " + namedQuery);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return returnobject;
    }//EoM    

    //------------Distributed Transaction    

    public String initiateDistributedTransaction(List<String> resources) {
        String tid = null;
        try {
            tid = RestUtil.initiateDistributedTransaction(uri, appinstnceid, resources);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return tid;
    }//ΕοΜ    


    public String commitDistributedTransaction(String tid) {
        String result = null;
        try {
            result = RestUtil.commitDistributedTransaction(uri, appinstnceid, tid);
        } catch (QueryException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return result;
    }//ΕοΜ

    public String performCUDQueryDuringDistributedTransaction(String namedQuery, String resid, String tid) {
        String returnobject = null;
        try {
            returnobject = RestUtil.performRawCUDQueryDuringDistributedTransaction(uri, appinstnceid, namedQuery, resid, tid);
            logger.info("Executed Part of Transaction: " + namedQuery);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return returnobject;
    }//EoM       

    public List<Object[]> performRQueryDuringDistributedTransaction(String namedQuery, String resid, String tid) {
        List<Object[]> returnobject = null;
        try {
            returnobject = RestUtil.performRawRQueryDuringDistributedTransaction(uri, appinstnceid, namedQuery, resid, tid);
            logger.info("Executed Part of Transaction: " + namedQuery);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return returnobject;
    }//EoM     


}//EoC
