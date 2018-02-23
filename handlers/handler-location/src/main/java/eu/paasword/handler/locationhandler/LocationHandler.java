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
package eu.paasword.handler.locationhandler;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

//import com.maxmind.geoip2.WebServiceClient;
//import com.maxmind.geoip2.exception.GeoIp2Exception;
//import com.maxmind.geoip2.model.CityResponse;
//import com.maxmind.geoip2.record.City;
//import com.maxmind.geoip2.record.Country;
//import com.maxmind.geoip2.record.Subdivision;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;

/**
 *
 * @author pgouvas@ubitech.eu
 * @author smantzouratos
 */
public class LocationHandler {

    private static final Logger logger = Logger.getLogger(LocationHandler.class.getName());
    final static String LOCATION_HANDLER_URL = "http://freegeoip.net/xml/";

    /**
     *
     * @param ipAddress
     * @return a String with the following format
     * <?xml version="1.0" encoding="UTF-8"?><Response>	<IP>43.229.53.56</IP>
     * <CountryCode>HK</CountryCode>	<CountryName>Hong Kong</CountryName>
     * <RegionCode></RegionCode>	<RegionName></RegionName>	<City></City>
     * <ZipCode></ZipCode>	<TimeZone>Asia/Hong_Kong</TimeZone>
     * <Latitude>22.25</Latitude>	<Longitude>114.1667</Longitude>
     * <MetroCode>0</MetroCode></Response>
     */
    public static Location extractLocation(String ipAddress, HttpClient client) {

        Location location = new Location();
        String str = null;

        GetMethod method = new GetMethod(LOCATION_HANDLER_URL + ipAddress);

        try {
            // Provide custom retry handler is necessary
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                logger.log(Level.SEVERE, "LocationHandler: Response Status: {0} - {1}", new Object[] { statusCode, method.getStatusLine()});
                return null;
            } else {
//                logger.log(Level.INFO, "LocationHandler: Response Status: {0}", statusCode);
                byte[] responseBody = IOUtils.toByteArray(method.getResponseBodyAsStream());
                str = new String(responseBody);

                //logger.info("Location element:" + responsexmlstr);
                InputSource xmlsource = new InputSource(new StringReader(str));
                XPath xpath = XPathFactory.newInstance().newXPath();
                //<?xml version="1.0" encoding="UTF-8"?><Response>	<IP>43.229.53.56</IP>	<CountryCode>HK</CountryCode>	<CountryName>Hong Kong</CountryName>	<RegionCode></RegionCode>	<RegionName></RegionName>	<City></City>	<ZipCode></ZipCode>	<TimeZone>Asia/Hong_Kong</TimeZone>	<Latitude>22.25</Latitude>	<Longitude>114.1667</Longitude>	<MetroCode>0</MetroCode></Response>
                Object responsexml = xpath.evaluate("/Response", xmlsource, XPathConstants.NODE);
                //IP
                String IP = xpath.evaluate("IP", responsexml);
                location.setIP(IP);
                //CountryCode
                String CountryCode = xpath.evaluate("CountryCode", responsexml);
                location.setCountryCode(CountryCode);
                //CountryName
                String CountryName = xpath.evaluate("CountryName", responsexml);
                location.setCountryName(CountryName);
                //RegionCode
                String RegionCode = xpath.evaluate("RegionCode", responsexml);
                location.setRegionCode(RegionCode);
                //RegionName
                String RegionName = xpath.evaluate("RegionName", responsexml);
                location.setRegionName(RegionName);
                //City
                String City = xpath.evaluate("City", responsexml);
                location.setCity(City);
                //ZipCode
                String ZipCode = xpath.evaluate("ZipCode", responsexml);
                location.setZipCode(ZipCode);
                //TimeZone
                String TimeZone = xpath.evaluate("TimeZone", responsexml);
                location.setTimeZone(TimeZone);
                //Latitude
                String Latitude = xpath.evaluate("Latitude", responsexml);
                location.setLatitude(Latitude);
                //Longitude
                String Longitude = xpath.evaluate("Longitude", responsexml);
                location.setLongitude(Longitude);
                //MetroCode
                String MetroCode = xpath.evaluate("MetroCode", responsexml);
                location.setMetroCode(MetroCode);

//                    logger.log(Level.INFO, "Location for: {0}", IP);
//                    logger.info(CountryCode);
//                    logger.info(CountryName);
//                    logger.info(RegionCode);
//                    logger.info(RegionName);
//                    logger.info(City);
//                    logger.info(ZipCode);
//                    logger.info(TimeZone);
//                    logger.info(Latitude);
//                    logger.info(Longitude);
//                    logger.info(MetroCode);

            }

        } catch (IOException | XPathExpressionException ex) {
            Logger.getLogger(LocationHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } // Always release connection
        finally {
            method.releaseConnection();
        }

        return location;
    }//EoM

//    public static Location extractLocationPaidService(String ipAddressStr) {
//
//        Location location = new Location();
//
//        try (WebServiceClient client = new WebServiceClient.Builder(116785, "fCc4TXLG9tAQ")
//                .build()) {
//
//            InetAddress ipAddress = InetAddress.getByName(ipAddressStr);
//
//            // Do the lookup
//            CityResponse response = client.city(ipAddress);
//            Country country = response.getCountry();
//            location.setCountryCode(country.getIsoCode());
//            location.setCountryName(country.getName());
//
//            Subdivision subdivision = response.getMostSpecificSubdivision();
//            location.setRegionName(subdivision.getName());
//
//            City city = response.getCity();
//            location.setCity(city.getName());
//
//            com.maxmind.geoip2.record.Location locationMaxMind = response.getLocation();
//            location.setLatitude(null != locationMaxMind.getLatitude() ? String.valueOf(locationMaxMind.getLatitude()) : "");
//            location.setLongitude(null != locationMaxMind.getLongitude() ? String.valueOf(locationMaxMind.getLongitude()) : "");
//
//        } catch (IOException | GeoIp2Exception e) {
//            e.printStackTrace();
//        }
//
//        return location;
//    }

}//EoC
