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
public class UpdateStatement {

    private String into;
    private List<WhereClause> where;
    private Map<String, Object> updateData;

    public UpdateStatement(String into, List<WhereClause> where, Map<String, Object> updateData) {
        this.into = into;
        this.where = where;
        this.updateData = updateData;
    }//

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

    public Map<String, Object> getUpdateData() {
        return updateData;
    }

    public void setUpdateData(Map<String, Object> updateData) {
        this.updateData = updateData;
    }    
    
}
