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
package eu.paasword.rest.repository;

import eu.paasword.api.repository.exception.apikey.APIKeyUniqueIDDoesNotExist;
import eu.paasword.api.repository.exception.application.ApplicationDoesNotExist;
import eu.paasword.repository.relational.domain.APIKey;
import eu.paasword.repository.relational.domain.Application;
import eu.paasword.repository.relational.domain.ApplicationBinary;
import eu.paasword.repository.relational.domain.ApplicationInstance;
import eu.paasword.repository.relational.service.TriplestoreService;
import eu.paasword.rest.response.BasicResponseCode;
import eu.paasword.rest.response.PaaSwordRestResponse;
import eu.paasword.spi.adapter.ProxyAdapter;
import eu.paasword.spi.model.CredentialsModel;
import eu.paasword.spi.model.PaaSOfferingModel;
import eu.paasword.spi.response.SPIResponse;
import eu.paasword.util.entities.ContextModel;
import eu.paasword.util.entities.PolicyModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contains all the rest endpoints regarding tests.
 *
 * @author smantzouratos
 */
@RestController
@RequestMapping("/api/v1/export")
public class ExporterManagementRestController {

    private static final Logger logger = Logger.getLogger(ExporterManagementRestController.class.getName());

    @Autowired
    TriplestoreService triplestoreService;


    // Export Context Model to JSON
    @RequestMapping(value = "/contextmodel/json", method = RequestMethod.GET)
    public ResponseEntity<byte[]> exportContextModelToJSON(HttpServletRequest request) {

        ContextModel contextModel = triplestoreService.exportContextModel();

        if (null != contextModel) {

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Charset", StandardCharsets.UTF_8.displayName());
            responseHeaders.set("Content-Description", "File Transfer");
            responseHeaders.set("Content-type", "application/octet-stream");
            responseHeaders.set("Content-disposition", "attachment; filename=contextModelJSON.txt");
            responseHeaders.setContentLength(contextModel.getContextModelJSON().getBytes().length);
            return new ResponseEntity<>(contextModel.getContextModelJSON().getBytes(), responseHeaders, HttpStatus.OK);


        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    // Export Context Model to RDF
    @RequestMapping(value = "/contextmodel/rdf", method = RequestMethod.GET)
    public ResponseEntity<byte[]> exportContextModelToRDF(HttpServletRequest request) {

        ContextModel contextModel = triplestoreService.exportContextModel();

        if (null != contextModel) {

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Charset", StandardCharsets.UTF_8.displayName());
            responseHeaders.set("Content-Description", "File Transfer");
            responseHeaders.set("Content-type", "application/octet-stream");
            responseHeaders.set("Content-disposition", "attachment; filename=contextModelRDF.txt");
            responseHeaders.setContentLength(contextModel.getContextModelRDF().getBytes().length);
            return new ResponseEntity<>(contextModel.getContextModelRDF().getBytes(), responseHeaders, HttpStatus.OK);


        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    // Export Policy Model to JSON
    @RequestMapping(value = "/policymodel/json", method = RequestMethod.GET)
    public ResponseEntity<byte[]> exportPolicyModelToJSON(HttpServletRequest request) {

        PolicyModel policyModel = triplestoreService.exportPolicyModel();

        if (null != policyModel) {

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Charset", StandardCharsets.UTF_8.displayName());
            responseHeaders.set("Content-Description", "File Transfer");
            responseHeaders.set("Content-type", "application/octet-stream");
            responseHeaders.set("Content-disposition", "attachment; filename=policyModelJSON.txt");
            responseHeaders.setContentLength(policyModel.getPolicyModelJSON().getBytes().length);
            return new ResponseEntity<>(policyModel.getPolicyModelJSON().getBytes(), responseHeaders, HttpStatus.OK);


        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    // Export Policy Model to RDF
    @RequestMapping(value = "/policymodel/rdf", method = RequestMethod.GET)
    public ResponseEntity<byte[]> exportPolicyModelToRDF(HttpServletRequest request) {

        PolicyModel policyModel = triplestoreService.exportPolicyModel();

        if (null != policyModel) {

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Charset", StandardCharsets.UTF_8.displayName());
            responseHeaders.set("Content-Description", "File Transfer");
            responseHeaders.set("Content-type", "application/octet-stream");
            responseHeaders.set("Content-disposition", "attachment; filename=policyModelRDF.txt");
            responseHeaders.setContentLength(policyModel.getPolicyModelRDF().getBytes().length);
            return new ResponseEntity<>(policyModel.getPolicyModelRDF().getBytes(), responseHeaders, HttpStatus.OK);


        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    // Export Policy Model to XACML
    @RequestMapping(value = "/policymodel/xacml", method = RequestMethod.GET)
    public ResponseEntity<byte[]> exportPolicyModelToXACML(HttpServletRequest request) {

        PolicyModel policyModel = triplestoreService.exportPolicyModel();

        if (null != policyModel) {

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Charset", StandardCharsets.UTF_8.displayName());
            responseHeaders.set("Content-Description", "File Transfer");
            responseHeaders.set("Content-type", "application/octet-stream");
            responseHeaders.set("Content-disposition", "attachment; filename=policyModelXACML.txt");
            responseHeaders.setContentLength(policyModel.getPolicyModelXACML().getBytes().length);
            return new ResponseEntity<>(policyModel.getPolicyModelXACML().getBytes(), responseHeaders, HttpStatus.OK);


        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }


}
