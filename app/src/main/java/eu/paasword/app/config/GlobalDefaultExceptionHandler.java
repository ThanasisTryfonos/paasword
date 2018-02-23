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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@Controller
@ControllerAdvice
class GlobalDefaultExceptionHandler {

    public static final String DEFAULT_ERROR_VIEW = "error";

    @ExceptionHandler(value = {Exception.class, Throwable.class, NoSuchRequestHandlingMethodException.class})
    public ModelAndView exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        //If exception has a ResponseStatus annotation then use its response code
        ResponseStatus responseStatusAnnotation = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

        return buildModelAndViewErrorPage(request, response, ex, responseStatusAnnotation != null ? responseStatusAnnotation.value() : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping("*")
    public ModelAndView fallbackHandler(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return buildModelAndViewErrorPage(request, response, null, HttpStatus.NOT_FOUND);
    }

    private ModelAndView buildModelAndViewErrorPage(HttpServletRequest request, HttpServletResponse response, Exception exception, HttpStatus httpStatus) {
        response.setStatus(httpStatus.value());

        ModelAndView modelAndview = new ModelAndView(DEFAULT_ERROR_VIEW);
        if (null != exception) {
            modelAndview.addObject("exception", exception);
            Logger.getLogger(GlobalDefaultExceptionHandler.class.getName()).log(Level.SEVERE, null, exception);
        }
        modelAndview.addObject("url", request.getRequestURL());
        modelAndview.addObject("httpStatus", httpStatus);
        return modelAndview;
    }

    //
    //TODO: Distinguish exception per business logic
    //
}
