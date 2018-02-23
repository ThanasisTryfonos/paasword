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
package eu.paasword.app.config;

import eu.paasword.triplestoreapi.client.FusekiTriplestoreClient;
import eu.paasword.triplestoreapi.client.TriplestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by smantzouratos on 12/04/16.
 */
@Configuration
public class FusekiTriplestoreClientContext {

    @Autowired
    Environment environment;

    @Bean
    public TriplestoreClient getTriplestoreClient() {

        return new FusekiTriplestoreClient(environment.getProperty("fuseki.triplestore.triplestore-query-url"), environment.getProperty("fuseki.triplestore.triplestore-export-url"), environment.getProperty("fuseki.triplestore.triplestore-upload-url"), environment.getProperty("fuseki.triplestore.triplestore-upload-replace-contents"));

    }

}
