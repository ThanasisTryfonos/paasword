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
package eu.paasword.annotation.interpreter.enumerator;

import com.comoyo.emjar.EmJarClassLoader;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.paasword.annotation.interpreter.c2j.SyntheticClassException;
import eu.paasword.annotation.interpreter.c2j.asm.ClassJsonVisitor;
import eu.paasword.annotation.interpreter.c2j.json.*;
import org.objectweb.asm.ClassReader;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 *
 * @author smantzouratos
 */
public class ClassEnumerator {

    private static final Logger logger = Logger.getLogger(ClassEnumerator.class.getName());

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unexpected ClassNotFoundException loading class '" + className + "'");
        }
    }

    /**
     * Given a package name and a directory returns all classes within that
     * directory
     *
     * @param directory
     * @param pkgname
     * @return Classes within Directory with package name
     */
    public static List<Class<?>> processDirectory(File directory, String pkgname) {

        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

//		log("Reading Directory '" + directory + "'");
        // Get the list of the files contained in the package
        String[] files = directory.list();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i];
            String className = null;

            // we are only interested in .class files
            if (fileName.endsWith(".class")) {
                // removes the .class extension
                className = pkgname + '.' + fileName.substring(0, fileName.length() - 6);
            }

//			log("FileName '" + fileName + "'  =>  class '" + className + "'");
            if (className != null) {
                classes.add(loadClass(className));
            }

            //If the file is a directory recursively class this method.
            File subdir = new File(directory, fileName);
            if (subdir.isDirectory()) {
                classes.addAll(processDirectory(subdir, pkgname + '.' + fileName));
            }
        }
        return classes;
    }

    /**
     * Given a jar file's URL and a package name returns all classes within jar
     * file.
     *
     * @param jarFile
     */
    public static Map<String, Class<?>> processJarfile(ClassLoader classLoader, JarFile jarFile) {
        Map<String, Class<?>> classes = new HashMap<>();

        //get contents of jar file and iterate through them
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            // Get content name from jar file
            String entryName = entry.getName();
            String className = null;

//            logger.info("Entry Name: " + entryName);

            // If content is a class save class name.
            if (entryName.endsWith(".class")) {
//                && } entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {
                className = entryName.replace('/', '.').replace('\\', '.');//.replace(".class", "");
                logger.info("JarEntry '" + entryName + "'  =>  class '" + className + "'");
                try {
                    classes.put(className, classLoader.loadClass(className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }

        logger.info("Classes: " + classes.size());

        return classes;
    }


    public static Map<String, PackageJson> processJar(JarFile jar){
        Map<String, PackageJson> processed = Maps.newHashMap();

        //Jar classes
        List<InputStream> inputs = Lists.newArrayList();

        // process all classes
        Enumeration<JarEntry> enries = jar.entries();
        while (enries.hasMoreElements()) {
            JarEntry entry = enries.nextElement();
            if (entry.getName().endsWith(".class")) {
//                logger.info("Classes-->"+entry.getName());
                try {
                    inputs.add(jar.getInputStream(entry));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (InputStream c : inputs) { //String c : toProcess
            try {
                ClassReader cr = new ClassReader(c);//getClassReader(c);
                ClassJsonVisitor cv = new ClassJsonVisitor();
                cr.accept(cv, ClassReader.SKIP_FRAMES);
                if (!processed.containsKey(cv.getPackage())) {
                    PackageJson j = new PackageJson();
                    processed.put(cv.getPackage(), j);
                }
                PackageJson pkg = processed.get(cv.getPackage());
                switch (cv.getType()) {
                    case CLASS:
                        pkg.addClass((ClassJson) cv.getJson());
                        break;
                    case ANNOTATION:
                        pkg.addAnnotation((AnnotationJson) cv.getJson());
                        break;
                    case ENUM:
                        pkg.addEnum((EnumJson) cv.getJson());
                        break;
                    case INTERFACE:
                        pkg.addInterface((InterfaceJson) cv.getJson());
                        break;
                }
            } catch (SyntheticClassException e) {
                // ignore synthetic classes
            } catch (IOException e) {
                e.printStackTrace();
            }
        } //finised populating produced json

        return processed;
    }


    /**
     * Give a package this method returns all classes contained in that package
     *
     * @param pkgName
     * @return
     */
    public static Map<String, Class<?>> getClassesForPackage(String pkgName) {

        EmJarClassLoader emJarClassLoader = new EmJarClassLoader();

        emJarClassLoader.setPackageAssertionStatus(pkgName, true);

        Map<String, Class<?>> classes = new HashMap<>();

        Reflections reflections = new Reflections(pkgName, new SubTypesScanner(false));

        Set<String> allClasses = reflections.getStore().getSubTypesOf(Object.class.getName());

        for (String tempClass : allClasses) {

            try {

//                logger.info("Adding class: " + tempClass);

                emJarClassLoader.loadClass(tempClass);

                classes.put(tempClass, Class.forName(tempClass));

            } catch (ClassNotFoundException e) {
               logger.severe("Class " + tempClass + " cannot be found!");
            }

        }

//        // Get name of package and turn it to a relative path
//        String pkgname = pkgName;//pkg.getName();
//        String relPath = pkgname.replace('.', '/');
//
////        // Get a File object for the package
//        URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);
//
//        //If we can't find the resource we throw an exception
//        if (resource == null) {
//            throw new RuntimeException("Unexpected problem: No resource for " + relPath);
//        }
//
//        //If the resource is a jar get all classes from jar
//        if (resource.toString().startsWith("jar:")) {
//            classes.addAll(processJarfile(resource, pkgname));
//        } else {
//            classes.addAll(processDirectory(new File(resource.getPath()), pkgname));
//        }

        return classes;
    }
}
