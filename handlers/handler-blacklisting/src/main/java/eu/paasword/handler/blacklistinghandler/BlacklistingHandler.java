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
package eu.paasword.handler.blacklistinghandler;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 * @author smantzouratos
 */
public class BlacklistingHandler {

    private static final Logger logger = Logger.getLogger(BlacklistingHandler.class.getName());

    final static String BLACKLISTING_HANDLER_URL = "http://www.ipvoid.com/scan/";

    public static int getBlacklistStatus(String ipAddress, HttpClient client) {
        int ret = 0;

        String str = null;

        GetMethod method = new GetMethod(BLACKLISTING_HANDLER_URL + ipAddress + "/");
        try {
            // Provide custom retry handler is necessary
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {

                logger.log(Level.SEVERE, "BlackistingHandler: Response Status: {0} - {1}", new Object[] { statusCode, method.getStatusLine()});
                return -1;

            } else {
//                logger.log(Level.INFO, "BlackistingHandler: Response Status: {0}", statusCode);
                byte[] responseBody = IOUtils.toByteArray(method.getResponseBodyAsStream());
                str = new String(responseBody);

                //green answer is <tr><td>Blacklist Status</td><td><span class="label label-success">POSSIBLY SAFE 0/40</span></td></tr>
                //red answer is <tr><td>Blacklist Status</td><td><span class="label label-danger">BLACKLISTED 5/40</span></td></tr>
                final Pattern pattern = Pattern.compile("<tr><td>Blacklist Status</td><td>(.+?)</td></tr>");
                final Matcher matcher = pattern.matcher(str);
                matcher.find();
                String result = matcher.group(1);
                if (result.indexOf("BLACKLISTED")!=-1) {
                    String scorestr = result.substring(result.indexOf("BLACKLISTED")+11, result.indexOf("</span>")).trim();
                    //logger.info(scorestr);
                    ret = Integer.parseInt( scorestr.split("/")[0] );
                }//if

                //logger.info("logger: "+ret);
                //logger.info("response:"+ret);

            }
        } catch (IOException ex) {
            Logger.getLogger(BlacklistingHandler.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } // Always release connection
        finally {
            method.releaseConnection();
        }

        return ret;
    }//EoM

}
