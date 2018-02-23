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
package eu.paasword.handler.devicetypehandler;

import eu.paasword.util.entities.HTTPHeader;
import eu.paasword.util.parser.ParserUtil;

import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
public class DeviceTypeHandler {

    private static final Logger logger = Logger.getLogger(DeviceTypeHandler.class.getName());

    public static String userAgent;
    public static String acceptInfo;

    public static void main(String[] args) {

        logger.info(String.valueOf(new DeviceTypeHandler("[{\"name\":\"host\",\"value\":\"localhost:8081\"},{\"name\":\"connection\",\"value\":\"keep-alive\"},{\"name\":\"accept\",\"value\":\"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\"},{\"name\":\"upgrade-insecure-requests\",\"value\":\"1\"},{\"name\":\"user-agent\",\"value\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36\"},{\"name\":\"referer\",\"value\":\"http://localhost:8081/dashboard\"},{\"name\":\"accept-encoding\",\"value\":\"gzip, deflate, sdch\"},{\"name\":\"accept-language\",\"value\":\"en-US,en;q=0.8\"},{\"name\":\"cookie\",\"value\":\"csrftoken=eD1A6pgC2pkhhSvD8VYirio2yY43Tm5o; JSESSIONID=A582462A1A2C95B9BAB1A819E6EA8769\"}]").isMobileDevice()));


    }

    public DeviceTypeHandler(String httpHeaders) {

        HTTPHeader header = ParserUtil.parseHTTPHeader(httpHeaders);

        this.userAgent = header.getUserAgent().toLowerCase();
        this.acceptInfo = header.getAccept().toLowerCase();
    }

    public static boolean isMobileDevice() {
        return detectMobileQuick();
    }//EoM


    private static final String engineWebKit = "webkit";
    private static final String deviceAndroid = "android";
    private static final String deviceIphone = "iphone";
    private static final String deviceIpod = "ipod";
    private static final String deviceSymbian = "symbian";
    private static final String deviceS60 = "series60";
    private static final String deviceS70 = "series70";
    private static final String deviceS80 = "series80";
    private static final String deviceS90 = "series90";
    private static final String deviceWinMob = "windows ce";
    private static final String deviceWindows = "windows";
    private static final String deviceIeMob = "iemobile";
    private static final String enginePie = "wm5 pie"; //An old Windows Mobile
    private static final String deviceBB = "blackberry";
    private static final String vndRIM = "vnd.rim"; //Detectable when BB devices emulate IE or Firefox
    private static final String deviceBBStorm = "blackberry95";  //Storm 1 and 2
    private static final String devicePalm = "palm";
    private static final String deviceWebOS = "webos"; //For Palm's new WebOS devices
    private static final String engineBlazer = "blazer"; //Old Palm
    private static final String engineXiino = "xiino"; //Another old Palm
    private static final String vndwap = "vnd.wap";
    private static final String wml = "wml";
    private static final String deviceBrew = "brew";
    private static final String deviceDanger = "danger";
    private static final String deviceHiptop = "hiptop";
    private static final String devicePlaystation = "playstation";
    private static final String deviceNintendoDs = "nitro";
    private static final String deviceNintendo = "nintendo";
    private static final String deviceWii = "wii";
    private static final String deviceXbox = "xbox";
    private static final String deviceArchos = "archos";
    private static final String engineOpera = "opera"; //Popular browser
    private static final String engineNetfront = "netfront"; //Common embedded OS browser
    private static final String engineUpBrowser = "up.browser"; //common on some phones
    private static final String engineOpenWeb = "openweb"; //Transcoding by OpenWave server
    private static final String deviceMidp = "midp"; //a mobile Java technology
    private static final String uplink = "up.link";
    private static final String devicePda = "pda"; //some devices report themselves as PDAs
    private static final String mini = "mini";  //Some mobile browsers put "mini" in their names.
    private static final String mobile = "mobile"; //Some mobile browsers put "mobile" in their user agent strings.
    private static final String mobi = "mobi"; //Some mobile browsers put "mobi" in their user agent strings.
    private static final String maemo = "maemo";
    private static final String maemoTablet = "tablet";
    private static final String linux = "linux";
    private static final String qtembedded = "qt embedded"; //for Sony Mylo
    private static final String mylocom2 = "com2"; //for Sony Mylo also
    private static final String manuSonyEricsson = "sonyericsson";
    private static final String manuericsson = "ericsson";
    private static final String manuSamsung1 = "sec-sgh";
    private static final String manuSony = "sony";
    private static final String svcVodafone = "vodafone";
    private static final String msie = "msie";
    private static final String msie60 = "msie 6.0";
    private static final String msie61 = "msie 6.1";
    private static final String msie7 = "msie 7.0";
    private static final String msie8 = "msie 8.0";
    private static final String msie9 = "msie 9.0";
    private static final String firefox = "firefox";
    private static final String safari = "apple";
    private static final String chrome = "chrome";
    private static final String opera = "presto";
    private static final String windows = "windows";

    /**
     * Detects if the current device is an iPhone.
     */
    public static boolean detectIphone() {
        // The iPod touch says it's an iPhone! So let's disambiguate.
        return userAgent.indexOf(deviceIphone) != -1 && !detectIpod();
    }

    /**
     * Detects if the current device is an iPod Touch.
     */
    public static boolean detectIpod() {
        return userAgent.indexOf(deviceIpod) != -1;
    }

    /**
     * Detects if the current device is an iPhone or iPod Touch.
     */
    public static boolean detectIphoneOrIpod() {
        //We repeat the searches here because some iPods may report themselves as an iPhone, which would be okay.
        return userAgent.indexOf(deviceIphone) != -1 || userAgent.indexOf(deviceIpod) != -1;
    }

    /**
     * Detects if the current device is an Android OS-based device.
     */
    public static boolean detectAndroid() {
        return userAgent.indexOf(deviceAndroid) != -1;
    }

    /**
     * Detects if the current device is an Android OS-based device and
     * the browser is based on WebKit.
     */
    public static boolean detectAndroidWebKit() {
        return detectAndroid() && detectWebkit();
    }

    /**
     * Detects if the current browser is based on WebKit.
     */
    public static boolean detectWebkit() {
        return userAgent.indexOf(engineWebKit) != -1;
    }

    /**
     * Detects if the current browser is the S60 Open Source Browser.
     */
    public static boolean detectS60OssBrowser() {
        //First, test for WebKit, then make sure it's either Symbian or S60.
        return detectWebkit() && (userAgent.indexOf(deviceSymbian) != -1 || userAgent.indexOf(deviceS60) != -1);
    }

    /**
     *
     * Detects if the current device is any Symbian OS-based device,
     *   including older S60, Series 70, Series 80, Series 90, and UIQ,
     *   or other browsers running on these devices.
     */
    public static boolean detectSymbianOS() {
        return userAgent.indexOf(deviceSymbian) != -1 || userAgent.indexOf(deviceS60) != -1 ||
                userAgent.indexOf(deviceS70) != -1 || userAgent.indexOf(deviceS80) != -1 ||
                userAgent.indexOf(deviceS90) != -1;
    }

    /**
     * Detects if the current browser is a Windows Mobile device.
     */
    public static boolean detectWindowsMobile() {
        //Most devices use 'Windows CE', but some report 'iemobile'
        //  and some older ones report as 'PIE' for Pocket IE.
        return userAgent.indexOf(deviceWinMob) != -1 ||
                userAgent.indexOf(deviceIeMob) != -1 ||
                userAgent.indexOf(enginePie) != -1 ||
                userAgent.indexOf(deviceWindows) != -1;
    }

    /**
     * Detects if the current browser is a BlackBerry of some sort.
     */
    public static boolean detectBlackBerry() {
        return userAgent.indexOf(deviceBB) != -1;
    }

    /**
     * Detects if the current browser is a BlackBerry Touch
     * device, such as the Storm
     */
    public static boolean detectBlackBerryTouch() {
        return userAgent.indexOf(deviceBBStorm) != -1;
    }

    /**
     * Detects if the current browser is on a PalmOS device.
     */
    public static boolean detectPalmOS() {
        //Most devices nowadays report as 'Palm', but some older ones reported as Blazer or Xiino.
        if (userAgent.indexOf(devicePalm) != -1 || userAgent.indexOf(engineBlazer) != -1 ||
                userAgent.indexOf(engineXiino) != -1 && !detectPalmWebOS()) {
            //Make sure it's not WebOS first
            if (detectPalmWebOS()) { return false; }
            else { return true; }
        }
        return false;
    }

    /**
     * Detects if the current browser is on a Palm device
     *    running the new WebOS.
     */
    public static boolean detectPalmWebOS() {
        return userAgent.indexOf(deviceWebOS) != -1;
    }

    /**
     * Check to see whether the device is any device
     *   in the 'smartphone' category.
     */
    public static boolean detectSmartphone() {
        return (detectIphoneOrIpod() ||
                detectS60OssBrowser() ||
                detectSymbianOS() ||
                detectWindowsMobile() ||
                detectBlackBerry() ||
                detectPalmOS() ||
                detectPalmWebOS() ||
                detectAndroid());
    }

    /**
     * Detects whether the device is a Brew-powered device.
     */
    public static boolean detectBrewDevice() {
        return userAgent.indexOf(deviceBrew) != -1;
    }

    /**
     * Detects the Danger Hiptop device.
     */
    public static boolean detectDangerHiptop() {
        return userAgent.indexOf(deviceDanger) != -1 || userAgent.indexOf(deviceHiptop) != -1;
    }

    /**
     * Detects Opera Mobile or Opera Mini.
     * Added by AHand
     */
    public static boolean detectOperaMobile() {
        return userAgent.indexOf(engineOpera) != -1 && (userAgent.indexOf(mini) != -1 || userAgent.indexOf(mobi) != -1);
    }

    /**
     * The quick way to detect for a mobile device.
     *  Will probably detect most recent/current mid-tier Feature Phones
     *  as well as smartphone-class devices.
     */
    public static boolean detectMobileQuick() {
        //Ordered roughly by market share, WAP/XML > Brew > Smartphone.
        if (detectBrewDevice()) { return true; }

        // Updated by AHand
        if (detectOperaMobile()) { return true; }

        if (userAgent.indexOf(engineUpBrowser) != -1) { return true; }
        if (userAgent.indexOf(engineOpenWeb) != -1) { return true; }
        if (userAgent.indexOf(deviceMidp) != -1) { return true; }

        if (detectSmartphone()) { return true; }
        if (detectDangerHiptop()) { return true; }

        if (detectMidpCapable()) { return true; }

        if (userAgent.indexOf(devicePda) != -1) { return true; }
        if (userAgent.indexOf(mobile) != -1) { return true; }

        //detect older phones from certain manufacturers and operators.
        if (userAgent.indexOf(uplink) != -1) { return true; }
        if (userAgent.indexOf(manuSonyEricsson) != -1) { return true; }
        if (userAgent.indexOf(manuericsson) != -1) { return true; }
        if (userAgent.indexOf(manuSamsung1) != -1) { return true; }
        if (userAgent.indexOf(svcVodafone) != -1) { return true; }

        return false;
    }

    /**
     * Detects if the current device is a Sony Playstation.
     */
    public static boolean detectSonyPlaystation() {
        return userAgent.indexOf(devicePlaystation) != -1;
    }

    /**
     * Detects if the current device is a Nintendo game device.
     */
    public static boolean detectNintendo() {
        return userAgent.indexOf(deviceNintendo) != -1 || userAgent.indexOf(deviceWii) != -1 ||
                userAgent.indexOf(deviceNintendoDs) != -1;
    }

    /**
     * Detects if the current device is a Microsoft Xbox.
     */
    public static boolean detectXbox() {
        return userAgent.indexOf(deviceXbox) != -1;
    }

    /**
     * Detects if the current device is an Internet-capable game console.
     */
    public static boolean detectGameConsole() {
        return detectSonyPlaystation() || detectNintendo() || detectXbox();
    }

    /**
     * Detects if the current device supports MIDP, a mobile Java technology.
     */
    public static boolean detectMidpCapable() {
        return userAgent.indexOf(deviceMidp) != -1;
    }

    /**
     * Detects if the current device is on one of the Maemo-based Nokia Internet Tablets.
     */
    public static boolean detectMaemoTablet() {
        return (userAgent.indexOf(maemo) != -1 || (userAgent.indexOf(maemoTablet) != -1 && userAgent.indexOf(linux) != -1));
    }

    /**
     * Detects if the current device is an Archos media player/Internet tablet.
     */
    public static boolean detectArchos() {
        return userAgent.indexOf(deviceArchos) != -1;
    }

    /**
     * Detects if the current browser is a Sony Mylo device.
     * Updated by AHand
     */
    public static boolean detectSonyMylo() {
        return userAgent.indexOf(manuSony) != -1 && (userAgent.indexOf(qtembedded) != -1 ||
                userAgent.indexOf(mylocom2) != -1);
    }

    /**
     * The longer and more thorough way to detect for a mobile device.
     *   Will probably detect most feature phones,
     *   smartphone-class devices, Internet Tablets,
     *   Internet-enabled game consoles, etc.
     *   This ought to catch a lot of the more obscure and older devices, also --
     *   but no promises on thoroughness!
     */
    public static boolean detectMobileLong() {
        return detectMobileQuick() || detectMaemoTablet() || detectGameConsole();
    }

    //*****************************
    // For Desktop Browsers
    //*****************************
    public static boolean detectMSIE() {
        return userAgent.indexOf(msie) != -1;
    }

    public static boolean detectMSIE6() {
        return userAgent.indexOf(msie60) != -1 && userAgent.indexOf(msie61) != -1;
    }

    public static boolean detectMSIE7() {
        return userAgent.indexOf(msie7) != -1;
    }

    public static boolean detectMSIE8() {
        return userAgent.indexOf(msie8) != -1;
    }

    public static boolean detectMSIE9() {
        return userAgent.indexOf(msie9) != -1;
    }

    public static boolean detectFirefox() {
        return userAgent.indexOf(firefox) != -1;
    }

    public static boolean detectSafari() {
        return userAgent.indexOf(safari) != -1;
    }

    public static boolean detectChrome() {
        return userAgent.indexOf(chrome) != -1;
    }

    public static boolean detectOpera() {
        return userAgent.indexOf(opera) != -1;
    }

    public static boolean detectWindows() {
        return userAgent.indexOf(windows) != -1;
    }

    //*****************************
    // For Mobile Web Site Design
    //*****************************

    /**
     * The quick way to detect for a tier of devices.
     *   This method detects for devices which can
     *   display iPhone-optimized web content.
     *   Includes iPhone, iPod Touch, Android, Palm WebOS, etc.
     */
    public static boolean detectTierIphone() {
        return detectIphoneOrIpod() || detectPalmWebOS() || detectAndroid() || detectAndroidWebKit();
    }

    /**
     * The quick way to detect for a tier of devices.
     *   This method detects for all smartphones, but
     *   excludes the iPhone Tier devices.
     */
    public static boolean detectTierSmartphones() {
        return detectSmartphone() && (!detectTierIphone());
    }

    /**
     * The quick way to detect for a tier of devices.
     *   This method detects for all other types of phones,
     *   but excludes the iPhone and Smartphone Tier devices.
     */
    public static boolean detectTierOtherPhones() {
        return detectMobileQuick() && (!detectTierIphone()) && (!detectTierSmartphones());
    }

}
