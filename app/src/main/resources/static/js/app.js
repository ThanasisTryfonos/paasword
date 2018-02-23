/**
 * App
 * 
 * 
 * The current .js is used to initialize and setup the basic tools/setting of 
 * the Single Page Application (SPA) of PaaSword Framework project
 * 
 * 
 * PLEASE DO NOT MAKE ANY ADDITIONS TO THIS JS 
 * 
 * For modifications kindly suggested to send an email to :
 * 
 * ch.paraskeva at gmail dot com
 * tsiolis.g at gmail dot com
 * -----------------------------------------------------------------------------
 * 
 *
 * 
 * Logging Mechanism 
 * -----------------
 *    
 * PaaSword framework supports a javascript logger(console appender). There are 
 * currently three type of message levels (info, debug and error). You can use 
 * the logger in any .js by using the follwoing command :
 * 
 * logger.x('message') where x is one of the following log levels : 
 *        i for INFO
 *        d for DEBUG
 *        e for ERROR
 *        
 *        
 * Notification Mechanism
 * -----------------------
 * 
 * For any kind of client notifications you are suggested to used the custom 
 * implemented notifier of this framework. You can use the notifier using the 
 * following command:
 * 
 * paasword.notify('message') #for information/generic message
 * paasword.notify('message', {mode: 'mode'}) #'mode' actualy is a customised class
 * which can change the appearance of the notification box, current modes:
 *
 * notification-error #use this for error based notifications
 * 
 */

$(document).ready(function () {
    logger.d("Loaded app.js");
});
// Static variables
var DEBUG_PROMPT = "[DEBUG] => ";
var INFO_PROMPT = "[INFO] => ";
var ERROR_PROMPT = "[ERROR] => ";
var ENV_MODE = "DEV";
var AUTHORIZATION_HEADER = "Authorization";
var TOKEN_KEY_NAME = "auth_token";
// Define logger component
var debug_logger = function logger(message) {
    if (isDevMode()) {
       console.log(DEBUG_PROMPT + message);
    }
}

var info_logger = function logger(message) {
    if (isDevMode()) {
        console.log(INFO_PROMPT + message);
    }
}

var error_logger = function logger(message) {
    if (isDevMode()) {
        console.log(ERROR_PROMPT + message);
    }
}

//Definition of Logger class
function Logger() {
    this.i = info_logger;
    this.d = debug_logger;
    this.e = error_logger;
}

/**
 * Return true if current mode is set to development
 *
 * @returns {Boolean}
 */
function isDevMode() {
    return ENV_MODE === "DEV";
}

/**
 * Returns true if user has an access token store in local storage
 * @returns {Boolean}
 */
function hasAccessToken() {
    return null !== localStorage.getItem(TOKEN_KEY_NAME);
}

function bindAJAXCallInterceptor() {
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader(AUTHORIZATION_HEADER, localStorage.auth_token);
        },
        complete: function () {
            //Scroll to top after post load
            window.scrollTo(0, 0);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status === 403) {
                logger.e("Status: " + jqXHR.responseJSON.error + " , Reason: " + jqXHR.responseJSON.message + " , Path: " + jqXHR.responseJSON.path);
                removeAccessToken();
                goHome();
            } else {

            }
        }
    });
    logger.d("AJAX call interceptor has been bound");
}

//Setup JQuery Prefilter
function setupAjaxCallFiltering() {
    $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
    options.async = true;
    });
    logger.d("AJAX call filtering has been setup");
}

//Define a global logger
var logger = new Logger();
//Define redirect page
var redirectTopage = $("#redirect-to-page").val();