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
package eu.paasword.app.main;

import eu.paasword.spi.adapter.PaaSAdapter;
import eu.paasword.spi.adapter.ProxyAdapter;
import org.springframework.beans.factory.serviceloader.ServiceListFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.logging.Logger;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan({
        // Contains all the security configuration regarding the PaaSword Framework
        "eu.paasword.app.config",
        "eu.paasword.app.scheduler",
        "eu.paasword.app.security.auth",
        "eu.paasword.api.repository",
        "eu.paasword.repository.relational.service",
        "eu.paasword.rest.repository",
        "eu.paasword.rest.dbproxy",
        "eu.paasword.rest.slipstream",
        "eu.paasword.rest.bounce",
        "eu.paasword.rest.policyvalidator",
        "eu.paasword.rest.semanticauthorizationengine",
        "eu.paasword.rest.ide",
        "eu.paasword.app.controller",
        "eu.paasword.annotation.interpreter"})
@EnableJpaRepositories(basePackages = {"eu.paasword.repository.relational.dao"})
@EntityScan(basePackages = {"eu.paasword.repository.relational.domain"})
@EnableAutoConfiguration
@EnableScheduling
//@EnableAsync
public class Application extends SpringBootServletInitializer {

    private static final Logger logger = Logger.getLogger(Application.class.getName());

    @Bean
    public ServiceListFactoryBean paasAdaptersList() {
        ServiceListFactoryBean serviceListFactoryBean = new ServiceListFactoryBean();
        serviceListFactoryBean.setServiceType(PaaSAdapter.class);
        return serviceListFactoryBean;
    }

    @Bean
    public ServiceListFactoryBean proxyAdaptersList() {
        ServiceListFactoryBean serviceListFactoryBean = new ServiceListFactoryBean();
        serviceListFactoryBean.setServiceType(ProxyAdapter.class);
        return serviceListFactoryBean;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
