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
package eu.paasword.handler.vhosthandler;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 * @author smantzouratos
 *
 */
public class VHostHandler {

    private static final Logger logger = Logger.getLogger(VHostHandler.class.getName());
    final static String VHOST_HANDLER_URL = "http://api.hackertarget.com/reverseiplookup/?q=";
    
    /**
     * Uses the api of http://api.hackertarget.com/reverseiplookup/?q=46.4.215.41 in order to resolve the target
     * @param ipAddress
     * @return
     */

    public static ArrayList<VirtualHostname> virtualHostsRetriever(String ipAddress, HttpClient client) {
        ArrayList<VirtualHostname> vhosts = new ArrayList();

        String str = null;

        GetMethod method = new GetMethod(VHOST_HANDLER_URL + ipAddress);
        try {
            // Provide custom retry handler is necessary
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {

                logger.log(Level.SEVERE, "VHostHandler: Response Status: {0} - {1}", new Object[] { statusCode, method.getStatusLine()});
                return null;

            } else {
//                logger.log(Level.INFO, "VHostHandler: Response Status: {0}", statusCode);
                byte[] responseBody = IOUtils.toByteArray(method.getResponseBodyAsStream());
                str = new String(responseBody);

                String[] uris = str.split("\n");
                for (String uri : uris) {
                    //add only if exists
                    if (uri.indexOf("No records found")==-1)
                        vhosts.add(new VirtualHostname(uri));
                }//for

            }
        } catch (IOException ex) {
            Logger.getLogger(VHostHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } // Always release connection
        finally {
            method.releaseConnection();
        }

        return vhosts;
    }//EoM
    
}