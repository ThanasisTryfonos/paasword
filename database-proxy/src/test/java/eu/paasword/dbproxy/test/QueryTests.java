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
package eu.paasword.dbproxy.test;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.ParseException;
import eu.paasword.dbproxy.exceptions.DatabaseException;
import eu.paasword.dbproxy.helper.AdapterHelper;
import eu.paasword.dbproxy.helper.QueryHelper;
import eu.paasword.dbproxy.impl.Adapter;
import eu.paasword.dbproxy.output.OutputHandler;
import eu.paasword.jpa.PaaSwordEntityHandler;
import eu.paasword.jpa.exceptions.ProxyInitializationException;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by gabel on 05.09.16.
 */
public class QueryTests {

    private static final Logger logger = Logger.getLogger(QueryTests.class.getName());

    public static void main(String[] args) throws ProxyInitializationException, ParseException, StandardException, DatabaseException {

//        Adapter adapter = AdapterHelper.getAdapter("00c34888-50c6-4368-a5da-da2e8e321836");
//        TestHelper.INSTANCE.print(adapter.query("delete from faculty ;"));
//        TestHelper.INSTANCE.print(adapter.query("delete from country ;"));
//        TestHelper.INSTANCE.print(adapter.query("INSERT INTO country (id, name, inhabitants) VALUES ( 1, 'country1', 111 );"));
//        TestHelper.INSTANCE.print(adapter.query("INSERT INTO faculty (id, name) VALUES ( 1, 'fac1' );"));
//        TestHelper.INSTANCE.print(adapter.query("update country set name='italy',inhabitants=1000 where id=1 ;"));
//        TestHelper.INSTANCE.print(adapter.query("select * from country ;"));        
//        TestHelper.INSTANCE.print(adapter.query("select * from faculty ;"));        
//        TestHelper.INSTANCE.print(adapter.query("update faculty set name='fac1prime' where id=1 ;"));        
//        TestHelper.INSTANCE.print(adapter.query("update country set name='country1' where id=1 ;"));        
//        TestHelper.INSTANCE.print(adapter.query("select * from faculty ;"));        
//        TestHelper.INSTANCE.print(adapter.query("select * from faculty ;"));        
//        Adapter adapter = AdapterHelper.getAdapter("00c34888-50c6-4368-a5da-da2e8e321824"); //733759"); 
//        TestHelper.INSTANCE.print(adapter.query("delete from country ;"));
//        TestHelper.INSTANCE.print(adapter.query("INSERT INTO country (id, name, inhabitants) VALUES ( 1, 'country1', 111 );"));
//        TestHelper.INSTANCE.print(adapter.query("select id from country;"));
//        TestHelper.INSTANCE.print(adapter.query("select * from country ;"));
//        TestHelper.INSTANCE.print(adapter.query("update country set id=1,name='country1',inhabitants=112 where id=1 ;"));
//        TestHelper.INSTANCE.print(adapter.query("select * from country ;"));
//        TestHelper.INSTANCE.print(adapter.query("INSERT INTO country (id, name, inhabitants) VALUES (select, 'countryx', 102);"));
//        TestHelper.INSTANCE.print(adapter.query("select max(id) from country;"));
//        TestHelper.INSTANCE.print(adapter.query("INSERT INTO country (id, name, inhabitants) VALUES ('select max(id) from country', 'countryχ', 100);"));
//        TestHelper.INSTANCE.print(adapter.query(TestHelper.INSTANCE.getQuery("country_select_simple")));
//        TestHelper.INSTANCE.print(adapter.query(TestHelper.INSTANCE.getQuery("country_delete_simple")));
//        TestHelper.INSTANCE.print(adapter.query(TestHelper.INSTANCE.getQuery("select_simple_where_and_binary_op")));
//        TestHelper.INSTANCE.print(adapter.query(TestHelper.INSTANCE.getQuery("select_simple_join")));
        // OrbiUser
//        TestHelper.INSTANCE.print(adapter.query("select * from orbiuser;"));
//        TestHelper.INSTANCE.print(adapter.query("insert into orbiuser (id, username, password, first_name, last_name, email, phone, enabled, first_login, date_created, fk_role_role) values (1, 'gledakis', '3d3a9798b1d61670d701130edbb5f7b3a26b85c2', 'Giannis', 'Ledakis', 'gledakis@gmail.com', '+306944111111', 1, 0, '2017-06-01', 1);"));
//        TestHelper.INSTANCE.print(adapter.query("delete from orbiuser where id = 1;"));
//        TestHelper.INSTANCE.print(adapter.query("select * from orbiuser;"));
//        PaaSwordEntityHandler paaSwordEntityHandler = PaaSwordEntityHandler.getInstance("http://localhost:8080/api/v1/query/execute", "00c34888-50c6-4368-a5da-da2e8e321836");
//        List<Map<String, String>> results = paaSwordEntityHandler.customQuery("select * from orbiuser;");
//
//        if (null != results && !results.isEmpty()) {
//
//            results.stream().forEach(result -> {
//
//                String row = "";
//
//                for (Map.Entry<String, String> entry : result.entrySet()) {
//
//                    row += entry.getKey() + ": " + entry.getValue() + ", ";
//                }
//                logger.info(row);
//                row = "";
//            });        
//        }

        String q1 = " delete from role ; ";
        String q2 = " INSERT INTO role (id, actor, description) VALUES (1, 'act1', 'desc111'); ";
        String q3 = " update role set actor='act11' where id=1 ; ";
        String q4 = " select * from role; ";        
        
        Adapter adapter = AdapterHelper.getAdapter("00c34888-50c6-4368-a5da-da2e8e321836");
        adapter.query(q1);
        adapter.query(q2);
        adapter.query(q3);
        OutputHandler qo4 = adapter.query(q4);
        
        List< Map<String, String>> results = QueryHelper.getSerializedOutput(qo4);
        logger.info( " Returned #results: " + results.size()+ "  "+results.toString() );


    }//EoM

}//EoC
