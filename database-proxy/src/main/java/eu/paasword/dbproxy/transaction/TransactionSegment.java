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
package eu.paasword.dbproxy.transaction;

import eu.paasword.dbproxy.database.BatchOfInsertStatements;
import eu.paasword.dbproxy.database.BatchOfUpdateStatements;
import eu.paasword.dbproxy.database.utils.Column;
import eu.paasword.dbproxy.database.utils.StatementFiller;
import eu.paasword.dbproxy.database.utils.WhereClause;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.rowset.CachedRowSet;

/**
 *
 * @author ligas
 */
public class TransactionSegment {

    private int type;   //0 = commit , 1 = CUD , 2 = select query , 3 = select return
    private String query;
    private String rid;
    //return
    List<Object[]> returnobjects;
    //extended fields
    private StatementFiller filler;
    private String from;
    private String into;
    private List<WhereClause> where;
    private BatchOfInsertStatements batchinsert;    
    private BatchOfUpdateStatements batchupdate;
    private String table;
    private Map<String, Object> updateData;
    private List<Column> columns;
    private List<List<String>> currenValues;
    private List<String> fromlist;
    private boolean selectAll;
    private boolean updateable;
    private CachedRowSet cachedrowset;   
    private Map<String, List<Column>> schema;
    
    public TransactionSegment(int type, List<Object[]> returnobjects) {
        this.type = type;
        this.returnobjects = returnobjects;
    }    
    
    public TransactionSegment(int type, String query, String rid) {
        this.type = type;
        this.query = query;
        this.rid = rid;
    }

    public TransactionSegment(int type) {       //commit
        this.type = type;
    }

    TransactionSegment(int type, String query, String rid, StatementFiller filler, String from, List<WhereClause> where) {
        this.type = type;
        this.query = query;
        this.rid = rid;
        this.filler = filler;
        this.from = from;
        this.where = where;
    }

    TransactionSegment(int type, BatchOfInsertStatements batchinsert, String rid, StatementFiller filler) {
        this.type = type;
        this.rid = rid;        
        this.batchinsert = batchinsert;        
        this.filler = filler;        
    }

    TransactionSegment(int type, BatchOfUpdateStatements batchupdate, String rid, StatementFiller filler) {
        this.type = type;
        this.rid = rid;        
        this.batchupdate = batchupdate;        
        this.filler = filler; 
    }

    TransactionSegment(int type, String query, String rid, StatementFiller filler, ArrayList<WhereClause> where, String into) {
        this.type = type;
        this.rid = rid;   
        this.query = query; 
        this.filler = filler;   
        this.where = where;  
        this.into = into;
    }

    TransactionSegment(int type, String query, String rid, StatementFiller filler, String table, Map<String, Object> updateData, List<WhereClause> where) {
        this.type = type;
        this.rid = rid;   
        this.query = query;
        this.filler = filler;      
        this.table = table;
        this.updateData = updateData;
        this.where = where;
    }

    TransactionSegment(int type, String query, String rid, StatementFiller filler, List<Column> columns, List<List<String>> currenValues) {
        this.type = type;
        this.rid = rid;   
        this.query = query;
        this.filler = filler;
        this.columns = columns;
        this.currenValues = currenValues;
    }

    TransactionSegment(int type, String query, String rid, StatementFiller filler, List<String> fromlist, List<WhereClause> where, boolean selectAll, boolean updateable) {
        this.type = type;
        this.rid = rid;   
        this.query = query;
        this.filler = filler;
        this.fromlist = fromlist;
        this.where = where;
        this.selectAll = selectAll;
        this.updateable = updateable;
    }

    TransactionSegment(int type, String rid) {
        this.type = type;
        this.rid = rid;           
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public List<Object[]> getReturnobjects() {
        return returnobjects;
    }

    public void setReturnobjects(List<Object[]> returnobjects) {
        this.returnobjects = returnobjects;
    }

    public StatementFiller getFiller() {
        return filler;
    }

    public void setFiller(StatementFiller filler) {
        this.filler = filler;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<WhereClause> getWhere() {
        return where;
    }

    public void setWhere(List<WhereClause> where) {
        this.where = where;
    }    

    public BatchOfInsertStatements getBatchinsert() {
        return batchinsert;
    }

    public void setBatchinsert(BatchOfInsertStatements batchinsert) {
        this.batchinsert = batchinsert;
    }

    public BatchOfUpdateStatements getBatchupdate() {
        return batchupdate;
    }

    public void setBatchupdate(BatchOfUpdateStatements batchupdate) {
        this.batchupdate = batchupdate;
    }    

    public String getInto() {
        return into;
    }

    public void setInto(String into) {
        this.into = into;
    }    

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Map<String, Object> getUpdateData() {
        return updateData;
    }

    public void setUpdateData(Map<String, Object> updateData) {
        this.updateData = updateData;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<List<String>> getCurrenValues() {
        return currenValues;
    }

    public void setCurrenValues(List<List<String>> currenValues) {
        this.currenValues = currenValues;
    }    

    public List<String> getFromlist() {
        return fromlist;
    }

    public void setFromlist(List<String> fromlist) {
        this.fromlist = fromlist;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public void setUpdateable(boolean updateable) {
        this.updateable = updateable;
    }

    public CachedRowSet getCachedrowset() {
        return cachedrowset;
    }

    public void setCachedrowset(CachedRowSet cachedrowset) {
        this.cachedrowset = cachedrowset;
    }    

    public Map<String, List<Column>> getSchema() {
        return schema;
    }

    public void setSchema(Map<String, List<Column>> schema) {
        this.schema = schema;
    }           
    
}//EoC
