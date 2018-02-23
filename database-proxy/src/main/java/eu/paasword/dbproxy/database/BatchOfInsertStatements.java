/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.utils.WhereClause;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ubuntu
 */
public class BatchOfInsertStatements {

    String table;
    String databaseName;
    String sql;
    List<InsertStatement> insertstatements;
    
    
    public BatchOfInsertStatements(String sql,String table, String databaseName) {
        this.sql = sql;
        this.table = table;
        this.databaseName = databaseName;
        insertstatements = new ArrayList();
    }

    public void addInsertStatement( String into, List<WhereClause> where) {
        InsertStatement statement = new InsertStatement(into, where);
        insertstatements.add(statement);
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<InsertStatement> getInsertstatements() {
        return insertstatements;
    }

    public void setInsertstatements(List<InsertStatement> insertstatements) {
        this.insertstatements = insertstatements;
    }    
    
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }    
    
    
} //EoC


