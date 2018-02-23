/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.paasword.dbproxy.database;

import eu.paasword.dbproxy.database.utils.WhereClause;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ubuntu
 */
public class BatchOfUpdateStatements {

    String table;
    String databaseName;
    String sql;
    List<UpdateStatement> updatestatements;

    public BatchOfUpdateStatements(String sql, String table, String databaseName) {
        this.sql = sql;
        this.table = table;
        this.databaseName = databaseName;
        updatestatements = new ArrayList();
    }

    public void addUpdateStatement(String into, List<WhereClause> where, Map<String, Object> updateData) {
        UpdateStatement statement = new UpdateStatement(into, where, updateData);
        updatestatements.add(statement);
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

    public List<UpdateStatement> getUpdatestatements() {
        return updatestatements;
    }

    public void setUpdatestatements(List<UpdateStatement> updatestatements) {
        this.updatestatements = updatestatements;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

} //EoC


