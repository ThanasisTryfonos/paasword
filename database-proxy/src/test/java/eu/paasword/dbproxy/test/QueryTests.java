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

//        String q1 = " delete from role ; ";
//        String q2 = " INSERT INTO role (id, actor, description) VALUES (1, 'act1', 'desc111'); ";
//        String q3 = " update role set actor='act11' where id=1 ; ";
//        String q4 = " select * from role; ";        
//        
//        Adapter adapter = AdapterHelper.getAdapter("00c34888-50c6-4368-a5da-da2e8e321836");
//        adapter.query(q1);
//        adapter.query(q2);
//        adapter.query(q3);
//        OutputHandler qo4 = adapter.query(q4);
//        
//        List< Map<String, String>> results = QueryHelper.getSerializedOutput(qo4);
//        logger.info( " Returned #results: " + results.size()+ "  "+results.toString() );

    }//EoM

}//EoC
