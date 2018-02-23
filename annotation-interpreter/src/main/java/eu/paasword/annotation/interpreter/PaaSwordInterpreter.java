/*
 * Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.paasword.annotation.interpreter;

import eu.paasword.annotation.interpreter.entity.Introspect;
import eu.paasword.annotation.interpreter.enumerator.ClassEnumerator;

import java.io.*;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import eu.paasword.annotation.interpreter.util.JarExtractor;
import eu.paasword.annotation.interpreter.util.Util;
import org.apache.bcel.classfile.JavaClass;
import org.springframework.util.FileCopyUtils;

/**
 * @author smantzouratos
 */
public class PaaSwordInterpreter {

    private static final Logger logger = Logger.getLogger(PaaSwordInterpreter.class.getName());

    public static String introspectSourceCodeApplication(String apiKey, String packageName) {

        // Find all classes
//        Map<String, Class<?>> discoveredClasses = ClassEnumerator.getClassesForPackage(packageName);

        // Find PaaSword annotated classes/methods
//        Map<String, Class> annotatedClasses = Util.checkForPaaSwordAnnotationsInsideBinary(discoveredClasses);
        Map<String, Class> annotatedClasses = new HashMap<>(); // Util.checkForPaaSwordAnnotationsAtRuntime(discoveredClasses);

        // Construct JSONObject for Semantic Authorization Engine
        String jsonObj = Util.constructSemanticAuthorizationEngineJSON(apiKey, annotatedClasses);

//        logger.info("JSON: " + jsonObj);

        return jsonObj;

    }

    public static Introspect introspectBinaryApplication(byte[] binaryFile, String originalName, String apiKey, String packageName) {

        Introspect introspect = null;

        String originalFilename = originalName.substring(0, originalName.lastIndexOf("."));
        String suffix = originalName.substring(originalName.lastIndexOf("."));

//        logger.info("FileName: " + originalFilename + ", Suffix: " + suffix);

        if (Util.isJavaCode(originalName)) {

            try {

                File javaFile = File.createTempFile(originalFilename, suffix);

                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(javaFile));
                FileCopyUtils.copy(binaryFile, stream);
                stream.close();

                Map<String, Class<?>> discoveredClasses = new HashMap<>();

                Map<String, Class<?>> discoveredDAOs = new HashMap<>();

                // Find all classes
                URLClassLoader cl = JarExtractor.parseJarFile(javaFile, discoveredClasses, packageName);

                logger.info("Discovered Classes: " + discoveredClasses.size());

                // Find PaaSword annotated classes/methods
                Map<String, Class> annotatedClasses = Util.checkForPaaSwordAnnotationsInsideBinary(discoveredClasses, discoveredDAOs);

                if (null != annotatedClasses && annotatedClasses.size() > 0) {

                    logger.info("Annotated Classes: " + annotatedClasses.size());

                    // Construct JSONObject for DB
                    introspect = Util.constructParamsJSON(cl, apiKey, annotatedClasses, discoveredDAOs);

                } else {

                    Map<String, JavaClass> discoveredBCELDAOs = new HashMap<>();

                    Map<String, JavaClass> bcelAnnotatedClasses = Util.checkForPaaSwordAnnotationsWithBCEL(discoveredClasses, discoveredBCELDAOs);

                    logger.info("BCEL Annotated Classes: " + bcelAnnotatedClasses.size());

                    if (null != bcelAnnotatedClasses && bcelAnnotatedClasses.size() > 0) {
                        introspect = Util.constructParamsJSONFromBCEL(cl, apiKey, bcelAnnotatedClasses, discoveredBCELDAOs);
                    }


                }

                return introspect;

            } catch (IOException e) {
                e.printStackTrace();
                logger.severe(e.getMessage());
            }

        }

        return null;
    }
}
