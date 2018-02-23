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
public class InsertStatement {

    private String into;
    private List<WhereClause> where;

    public InsertStatement(String into, List<WhereClause> where) {
        this.into = into;
        this.where = where;
    }

    
    public String getInto() {
        return into;
    }

    public void setInto(String into) {
        this.into = into;
    }

    public List<WhereClause> getWhere() {
        return where;
    }

    public void setWhere(ArrayList<WhereClause> where) {
        this.where = where;
    }

}