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
package eu.paasword.handler;

import eu.paasword.handler.devicetypehandler.DeviceTypeHandler;
import eu.paasword.handler.locationhandler.Location;
import eu.paasword.handler.locationhandler.LocationHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import java.util.logging.Logger;

/**
 *
 * @author pgouvas@ubitech.eu
 * @author smantzouratos
 */
public class HandlerManager {

    private static final Logger logger = Logger.getLogger(HandlerManager.class.getName());

    final static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
    static HttpClient client;

    public static void init() {
        HttpConnectionManagerParams params = connectionManager.getParams();
        params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 1000);
        params.setMaxTotalConnections(1000);
        connectionManager.setParams(connectionManager.getParams());
        client = new HttpClient(connectionManager);
    }

    public static void main(String[] args) {
        String ipAddressStr = "213.249.38.66";//"128.101.101.101"; // //"213.249.38.66";
        HandlerManager.handleRequest(ipAddressStr);

    } //

    public static void handleRequest(String ipAddress) {

        if (null == client) {
            init();
        }

//        Location location = LocationHandler.extractLocationPaidService(ipAddress);
//        int isBlacklisted = BlacklistingHandler.getBlacklistStatus(ipAddress, client);
//        int vHostSize = VHostHandler.virtualHostsRetriever(ipAddress, client).size();
//        logger.info("IP: " + ipAddress + ", City: " + location.getCity() + ", Country Code: " + location.getCountryCode() + ", Country: " + location.getCountryName());
    }//EoM

    public static Location handleLocationRequest(String ipAddress) {

        if (null == client) {
            init();
        }

        return LocationHandler.extractLocation(ipAddress, client);

    }//EoM

    public static boolean isMobileDevice(String httpHeader) {

        return new DeviceTypeHandler(httpHeader).isMobileDevice();

    }//EoM

    public static boolean isAndroidMobileOS(String httpHeader) {

        return new DeviceTypeHandler(httpHeader).detectAndroid();

    }//EoM

    public static boolean isSmartphone(String httpHeader) {

        return new DeviceTypeHandler(httpHeader).detectSmartphone();

    }//EoM

    public static boolean detectChrome(String httpHeader) {

        return new DeviceTypeHandler(httpHeader).detectChrome();

    }//EoM

    public static boolean detectFirefox(String httpHeader) {

        return new DeviceTypeHandler(httpHeader).detectFirefox();

    }//EoM

    public static boolean detectSafari(String httpHeader) {

        return new DeviceTypeHandler(httpHeader).detectSafari();

    }//EoM

}//EoC
