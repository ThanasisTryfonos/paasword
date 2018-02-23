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
package eu.paasword.jpa.introspection;

import eu.paasword.jpa.PaaSwordQueryHandler;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 *
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class IntrospectionUtil {

    private static final Logger logger = Logger.getLogger(IntrospectionUtil.class.getName());

    public static List<Class> getPaaSwordEntities() {
        List<Class> paaswordenityclasses = new ArrayList<>();

        List<String> classnames = new ArrayList<>();
        try {
            ClassLoader myCL = Thread.currentThread().getContextClassLoader();
            while (myCL != null) {
                //System.out.println("ClassLoader: " + myCL);
                for (Iterator iter = list(myCL); iter.hasNext();) {
                    classnames.add("" + iter.next());
                }
                myCL = myCL.getParent();
            }

            for (String classname : classnames) {
                //System.out.println("Checking class:'" + classname + "'"); // the name is class GAP package
                String correctclassname = classname.split(" ")[1].trim();
                System.out.println("'" + correctclassname + "'");
                try {
                    Class<?> clazz = Class.forName(correctclassname);
                    Annotation[] classannotations = clazz.getAnnotations();
                    for (Annotation classannotation : classannotations) {
                        String annotationcanonicalname = classannotation.annotationType().getCanonicalName();
                        //logger.info("Checking:  <" + annotationcanonicalname + ">");
                        if (annotationcanonicalname.equalsIgnoreCase(PaaSwordQueryHandler.PAASWORD_ENTITY)) {
                            paaswordenityclasses.add(clazz);
                            logger.info("New Entity Class: " + clazz.getSimpleName());
                        }//if
                    }
                } catch (NoClassDefFoundError | ClassNotFoundException ex) {
                    logger.severe("Error during class introspection of " +correctclassname);                    
                }
            }//for annotations                                                   

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.severe("Fatal error during introspection " );
        }

        return paaswordenityclasses;
    }//EoM

    private static Iterator list(ClassLoader CL) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class CL_class = CL.getClass();
        while (CL_class != java.lang.ClassLoader.class) {
            CL_class = CL_class.getSuperclass();
        }
        java.lang.reflect.Field ClassLoader_classes_field = CL_class.getDeclaredField("classes");
        ClassLoader_classes_field.setAccessible(true);
        Vector classes = (Vector) ClassLoader_classes_field.get(CL);
        return classes.iterator();
    }

}//EoC
