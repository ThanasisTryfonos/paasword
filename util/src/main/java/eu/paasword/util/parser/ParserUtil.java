package eu.paasword.util.parser;

import eu.paasword.util.entities.HTTPHeader;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by smantzouratos on 23/09/16.
 */
public class ParserUtil {

    public static HTTPHeader parseHTTPHeader(String httpHeaderJSON) {

        JSONArray httpHeaderArray = new JSONArray(httpHeaderJSON);

        HTTPHeader httpHeader = new HTTPHeader();

        for (int i = 0; i < httpHeaderArray.length(); i++) {

            JSONObject header = httpHeaderArray.getJSONObject(i);

            String name = header.getString("name");
            String value = header.getString("value");

            switch (name) {
                case "host":
                    httpHeader.setHost(value);
                    break;
                case "connection":
                    httpHeader.setConnection(value);
                    break;
                case "accept":
                    httpHeader.setAccept(value);
                    break;
                case "upgrade-insecure-requests":
                    httpHeader.setUpgradeInsecureRequests(value);
                    break;
                case "user-agent":
                    httpHeader.setUserAgent(value);
                    break;
                case "referer":
                    httpHeader.setReferer(value);
                    break;
                case "accept-encoding":
                    httpHeader.setAcceptEncoding(value);
                    break;
                case "accept-language":
                    httpHeader.setAcceptLanguage(value);
                    break;
                case "cookie":
                    httpHeader.setCookie(value);
                    break;
                default:
                    break;
            }

        }

        return httpHeader;
    }


}
