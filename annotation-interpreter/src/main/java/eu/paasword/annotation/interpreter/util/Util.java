package eu.paasword.annotation.interpreter.util;

import eu.paasword.annotation.interpreter.entity.Introspect;
import eu.paasword.annotation.interpreter.model.GenericApplicationModel;
import eu.paasword.annotations.PaaSwordEntity;
import eu.paasword.annotations.PaaSwordPEP;
import eu.paasword.jpa.PaaSwordQueryHandler;
import eu.paasword.jpa.exceptions.CyclicDependencyException;
import eu.paasword.jpa.exceptions.NoClassToProcessException;
import eu.paasword.jpa.exceptions.NotAValidPaaSwordEntityException;
import eu.paasword.jpa.exceptions.UnSatisfiedDependencyException;
import eu.paasword.triplestoreapi.parser.drools.Clazz;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;

/**
 * Created by smantzouratos on 18/07/16.
 */
public class Util {

    private static final Logger logger = Logger.getLogger(Util.class.getName());

    public static <T> Collector<T, List<T>, T> singletonCollector() {
        return Collector.of(
                ArrayList::new,
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                list -> {
                    if (list.size() != 1) {
                        //throw new IllegalStateException();
                        return null;
                    }
                    return list.get(0);
                }
        );
    }

    public static boolean isJavaCode(String fileName) {
        return fileName != null && (fileName.toLowerCase().endsWith(".jar") || fileName.toLowerCase().endsWith(".war") || fileName.toLowerCase().endsWith(".ear"));
    }

    public static Map<String, Class> checkForPaaSwordAnnotationsAtRuntime(Map<String, Class<?>> discoveredClasses) {

        // Create a map with methods per class
        Map<String, Class> annotatedClasses = new HashMap<>();

        discoveredClasses.entrySet().stream().forEach(clazz -> {

            try {

//                logger.info("Checking class : " + clazz.getKey());

                if (!annotatedClasses.containsKey(clazz.getKey())) {

                    Class tempClass = Class.forName(clazz.getKey());

                    // Check annotations of each discovered class
                    for (Annotation tempAnnotation : tempClass.getDeclaredAnnotations()) {

                        if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                            // Case 1 - Annotation: PaaSwordPEP
                            annotatedClasses.put(clazz.getKey(), tempClass);

                        } else if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordEntity")) {

                            // Case 2 - Annotation: PaaSwordEntity
                            annotatedClasses.put(clazz.getKey(), tempClass);

                        }

                    }

                    // No PaaSword Annotations at Class Level (have to check its methods)
                    for (Method tempMethod : tempClass.getMethods()) {

                        for (Annotation tempMethodAnnotation : tempMethod.getDeclaredAnnotations()) {

                            if (tempMethodAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                                annotatedClasses.put(clazz.getKey(), tempClass);
                            }
                        }

                    }

                }

            } catch (ClassNotFoundException e) {
                logger.info("Class " + clazz.getKey() + " cannot be found!");
            }

        });


        return annotatedClasses;

    }

    public static Map<String, JavaClass> checkForPaaSwordAnnotationsWithBCEL(Map<String, Class<?>> discoveredClasses, Map<String, JavaClass> discoveredDAOs) {

        // Create a map with methods per class
        Map<String, JavaClass> annotatedClasses = new HashMap<>();

        if (null != discoveredClasses && discoveredClasses.size() > 0) {

            try {

                discoveredClasses.entrySet().stream().forEach(clazz -> {

                    boolean checkMethods = true;

//                logger.info("Checking class : " + clazz.getKey());

                    if (!annotatedClasses.containsKey(clazz.getKey())) {

                        Class tempClass = clazz.getValue();

                        try {

                            JavaClass javaClass = Repository.lookupClass(tempClass);

                            if (null != javaClass) {

                                if (null != javaClass.getAnnotationEntries()) {

                                    for (AnnotationEntry tempAnnotation : javaClass.getAnnotationEntries()) {

//                                        logger.info("Annotation of Class: " + tempAnnotation.getAnnotationType());

                                        if (tempAnnotation.getAnnotationType().contains("PaaSwordPEP")) {

                                            // Case 1 - Annotation: PaaSwordPEP
                                            annotatedClasses.put(clazz.getKey(), javaClass);

                                        } else if (tempAnnotation.getAnnotationType().contains("PaaSwordEntity")) {

                                            // Case 2 - Annotation: PaaSwordEntity
                                            annotatedClasses.put(clazz.getKey(), javaClass);

                                            discoveredDAOs.put(clazz.getKey(), javaClass);

                                        } else if (tempAnnotation.getAnnotationType().contains("EnableAspectJAutoProxy")) {
                                            checkMethods = false;
                                        }

                                    }

                                }

                                if (checkMethods) {

                                    if (null != javaClass.getMethods()) {

                                        // No PaaSword Annotations to class level (have to check its methods)
                                        for (org.apache.bcel.classfile.Method tempMethod : javaClass.getMethods()) {

//                                            logger.info("Method of Class: " + tempMethod.getName());

                                            if (null != tempMethod.getAnnotationEntries()) {

                                                for (AnnotationEntry tempMethodAnnotation : tempMethod.getAnnotationEntries()) {

//                                                    logger.info("Annotation of Class: " + tempMethodAnnotation.getAnnotationType());

                                                    if (tempMethodAnnotation.getAnnotationType().contains("PaaSwordPEP")) {

                                                        annotatedClasses.put(clazz.getKey(), javaClass);

                                                    }
                                                }

                                            }

                                        }

                                    }

                                }

                            }


                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }

                });

            } catch (NoClassDefFoundError e) {
                e.printStackTrace();

            }

        }

        logger.info("Annotated classes: " + annotatedClasses.size() + ", DAO Classes: " + discoveredDAOs.size());

        return annotatedClasses;

    }

    public static Map<String, Class> checkForPaaSwordAnnotationsInsideBinary(Map<String, Class<?>> discoveredClasses, Map<String, Class<?>> discoveredDAOs) {

        // Create a map with methods per class
        Map<String, Class> annotatedClasses = new HashMap<>();

        if (null != discoveredClasses && discoveredClasses.size() > 0) {

            try {

                for (Map.Entry<String, Class<?>> clazz : discoveredClasses.entrySet()) {

                    boolean checkMethods = true;

//                logger.info("Checking class : " + clazz.getKey());

                    if (!annotatedClasses.containsKey(clazz.getKey())) {

                        Class tempClass = clazz.getValue();

                        if (null != tempClass) {

                            if (null != tempClass.getDeclaredAnnotations()) {

                                // Check annotations of each discovered class
                                for (Annotation tempAnnotation : tempClass.getDeclaredAnnotations()) {

                                    logger.info("Annotation: " + tempAnnotation.annotationType().getName());

                                    if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                                        // Case 1 - Annotation: PaaSwordPEP
                                        annotatedClasses.put(clazz.getKey(), tempClass);

                                    } else if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordEntity")) {

                                        // Case 2 - Annotation: PaaSwordEntity
                                        annotatedClasses.put(clazz.getKey(), tempClass);

                                        discoveredDAOs.put(clazz.getKey(), tempClass);

                                        logger.info("Clazz: " + clazz.getKey());

                                    } else if (tempAnnotation.annotationType().getName().equals("org.springframework.context.annotation.EnableAspectJAutoProxy")) {
                                        checkMethods = false;
                                    }

                                }

                            }

                            if (checkMethods) {

                                if (null != tempClass.getDeclaredMethods()) {

                                    // No PaaSword Annotations to class level (have to check its methods)
                                    for (Method tempMethod : tempClass.getDeclaredMethods()) {

                                        if (null != tempMethod.getDeclaredAnnotations()) {

                                            for (Annotation tempMethodAnnotation : tempMethod.getDeclaredAnnotations()) {

                                                if (tempMethodAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                                                    annotatedClasses.put(clazz.getKey(), tempClass);
                                                }
                                            }

                                        }

                                    }

                                }

                            }

                        }

                    }

                }

            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
                annotatedClasses.clear();
                discoveredDAOs.clear();

            }

        }

        logger.info("Annotated classes: " + annotatedClasses.size() + ", DAO Classes: " + discoveredDAOs.size());

        return annotatedClasses;

    }

    public static Map<String, Class> checkForPaaSwordAnnotationsInsideBinary1(Map<String, InputStream> discoveredClasses) {

        // Create a map with methods per class
        Map<String, Class> annotatedClasses = new HashMap<>();

        discoveredClasses.entrySet().stream().forEach(clazz -> {

            boolean checkMethods = true;

//            logger.info("Checking class : " + clazz.getKey());

            if (!annotatedClasses.containsKey(clazz.getKey())) {

                Class tempClass = clazz.getValue().getClass();

                // Check annotations of each discovered class
                for (Annotation tempAnnotation : tempClass.getDeclaredAnnotations()) {

                    if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                        // Case 1 - Annotation: PaaSwordPEP
                        annotatedClasses.put(clazz.getKey(), tempClass);

                    } else if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordEntity")) {

                        // Case 2 - Annotation: PaaSwordEntity
                        annotatedClasses.put(clazz.getKey(), tempClass);

                    } else if (tempAnnotation.annotationType().getName().equals("org.springframework.context.annotation.EnableAspectJAutoProxy")) {
                        checkMethods = false;
                    }

                }

                if (checkMethods) {

                    // No PaaSword Annotations to class level (have to check its methods)
                    for (Method tempMethod : tempClass.getMethods()) {

                        for (Annotation tempMethodAnnotation : tempMethod.getDeclaredAnnotations()) {

                            if (tempMethodAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                                annotatedClasses.put(clazz.getKey(), tempClass);
                            }
                        }

                    }
                }

            }

        });


        return annotatedClasses;

    }

    public static String constructSemanticAuthorizationEngineJSON(String apiKey, Map<String, Class> annotatedClasses) {

        boolean checkForMethods;
        boolean checkForFields;

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("apiKey", apiKey);
        JSONArray jsonClasses = new JSONArray();

        for (Map.Entry<String, Class> clazz : annotatedClasses.entrySet()) {

            checkForFields = false;
            checkForMethods = false;

            JSONObject jsonClazz = new JSONObject();
            jsonClazz.put("type", "Class");
            jsonClazz.put("name", clazz.getKey());

            JSONArray jsonAnnotations = new JSONArray();
            JSONArray jsonMethods = new JSONArray();
            JSONArray jsonFields = new JSONArray();

            try {

                Class tempClass = Class.forName(clazz.getKey());

                for (Annotation tempAnnotation : tempClass.getDeclaredAnnotations()) {

                    if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                        jsonClazz.put("category", "PEP");

                        // Case 1 - Annotation: PaaSwordPEP
                        JSONObject jsonAnnotation = new JSONObject();

                        jsonAnnotation.put("name", tempAnnotation.annotationType().getName());
                        PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempAnnotation;

                        String value = tempPaaSwordPEPAnnotation.value();

                        JSONArray jsonPolicies = new JSONArray();

                        if (value.contains(",")) {
                            String policies[] = value.split("\\,");

                            for (String policy : policies) {
                                jsonPolicies.put(policy);
                            }

                        } else {
                            jsonPolicies.put(value);
                        }

                        jsonAnnotation.put("value", jsonPolicies);

                        jsonAnnotations.put(jsonAnnotation);

                        checkForMethods = true;
//                        checkForFields = true;

                    } else if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordEntity")) {

                        jsonClazz.put("category", "DataModel");

                        // Case 2 - Annotation: PaaSwordEntity
                        JSONObject jsonAnnotation = new JSONObject();

                        jsonAnnotation.put("name", tempAnnotation.annotationType().getName());
                        PaaSwordEntity tempPaaSwordEntityAnnotation = (PaaSwordEntity) tempAnnotation;
                        jsonAnnotation.put("value", tempPaaSwordEntityAnnotation.value());

                        jsonAnnotations.put(jsonAnnotation);

                        checkForMethods = true;
                        checkForFields = true;

                    } else {

                        // Case 3 - No PaaSword Annotations (have to check its methods)
                        checkForMethods = true;
//                        checkForFields = true;

                    }

                }

                // Check Methods
                if (checkForMethods) {

                    for (Method tempMethod : tempClass.getMethods()) {

                        for (Annotation tempMethodAnnotation : tempMethod.getDeclaredAnnotations()) {

                            if (tempMethodAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                                jsonClazz.put("category", "PEP");

                                JSONObject jsonMethod = new JSONObject();

                                jsonMethod.put("name", tempMethod.getName());

                                // Parameters
                                JSONArray jsonMethodParameters = new JSONArray();

                                for (Parameter tempParameter : tempMethod.getParameters()) {
                                    JSONObject jsonMethodParameter = new JSONObject();

                                    jsonMethodParameter.put("name", tempParameter.getName());
                                    jsonMethodParameter.put("type", tempParameter.getType().getName());

                                    jsonMethodParameters.put(jsonMethodParameter);
                                }


                                jsonMethod.put("parameters", jsonMethodParameters);

                                // Annotations
                                JSONArray jsonMethodAnnotations = new JSONArray();

                                JSONObject jsonMethodAnnotation = new JSONObject();
                                jsonMethodAnnotation.put("name", tempMethodAnnotation.annotationType().getName());
                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempMethodAnnotation;

                                String value = tempPaaSwordPEPAnnotation.value();

                                JSONArray jsonPolicies = new JSONArray();

                                if (value.contains(",")) {
                                    String policies[] = value.split("\\,");

                                    for (String policy : policies) {
                                        jsonPolicies.put(policy);
                                    }

                                } else {
                                    jsonPolicies.put(value);
                                }

                                jsonMethodAnnotation.put("value", jsonPolicies);

                                jsonMethodAnnotations.put(jsonMethodAnnotation);

                                jsonMethod.put("annotations", jsonMethodAnnotations);

                                jsonMethods.put(jsonMethod);
                            }
                        }

                    }

                }

                // Check Fields
                if (checkForFields) {

                    for (Field tempField : tempClass.getDeclaredFields()) {

                        JSONObject jsonField = new JSONObject();

                        jsonField.put("name", tempField.getName());
                        jsonField.put("type", tempField.getType().getName());

                        // Annotations
                        JSONArray jsonFieldAnnotations = new JSONArray();

                        JSONObject jsonFieldAnnotation = new JSONObject();
//                                jsonFieldAnnotation.put("name", tempFieldAnnotation.annotationType().getName());
//                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempFieldAnnotation;
//
//                                String value = tempPaaSwordPEPAnnotation.value();
//
//                                JSONArray jsonPolicies = new JSONArray();
//
//                                if (value.contains(",")) {
//                                    String policies[] = value.split("\\,");
//
//                                    for (String policy : policies) {
//                                        jsonPolicies.put(policy);
//                                    }
//
//                                } else {
//                                    jsonPolicies.put(value);
//                                }
//
//                                jsonFieldAnnotation.put("value", jsonPolicies);

                        jsonFieldAnnotations.put(jsonFieldAnnotation);

                        jsonField.put("annotations", jsonFieldAnnotations);


                        jsonFields.put(jsonField);

//                        for (Annotation tempFieldAnnotation : tempField.getAnnotations()) {
//
//                            if (tempFieldAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {
//
//                                JSONObject jsonField = new JSONObject();
//
//                                jsonField.put("name", tempField.getName());
//                                jsonField.put("type", tempField.getType().getName());
//
//                                // Annotations
//                                JSONArray jsonFieldAnnotations = new JSONArray();
//
//                                JSONObject jsonFieldAnnotation = new JSONObject();
//                                jsonFieldAnnotation.put("name", tempFieldAnnotation.annotationType().getName());
//                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempFieldAnnotation;
//
//                                String value = tempPaaSwordPEPAnnotation.value();
//
//                                JSONArray jsonPolicies = new JSONArray();
//
//                                if (value.contains(",")) {
//                                    String policies[] = value.split("\\,");
//
//                                    for (String policy : policies) {
//                                        jsonPolicies.put(policy);
//                                    }
//
//                                } else {
//                                    jsonPolicies.put(value);
//                                }
//
//                                jsonFieldAnnotation.put("value", jsonPolicies);
//
//                                jsonFieldAnnotations.put(jsonFieldAnnotation);
//
//                                jsonField.put("annotations", jsonFieldAnnotations);
//
//
//                                jsonFields.put(jsonField);
//
//                            } else {
//
//                                JSONObject jsonField = new JSONObject();
//
//                                jsonField.put("name", tempField.getName());
//                                jsonField.put("type", tempField.getType().getName());
//
//                                // Annotations
//                                JSONArray jsonFieldAnnotations = new JSONArray();
//
//                                JSONObject jsonFieldAnnotation = new JSONObject();
////                                jsonFieldAnnotation.put("name", tempFieldAnnotation.annotationType().getName());
////                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempFieldAnnotation;
////
////                                String value = tempPaaSwordPEPAnnotation.value();
////
////                                JSONArray jsonPolicies = new JSONArray();
////
////                                if (value.contains(",")) {
////                                    String policies[] = value.split("\\,");
////
////                                    for (String policy : policies) {
////                                        jsonPolicies.put(policy);
////                                    }
////
////                                } else {
////                                    jsonPolicies.put(value);
////                                }
////
////                                jsonFieldAnnotation.put("value", jsonPolicies);
//
//                                jsonFieldAnnotations.put(jsonFieldAnnotation);
//
//                                jsonField.put("annotations", jsonFieldAnnotations);
//
//
//                                jsonFields.put(jsonField);
//
//                            }
//                        }

                    }

                }


            } catch (ClassNotFoundException e) {
                logger.severe("Class " + clazz.getKey() + " cannot be found!");
            }

            jsonClazz.put("annotations", jsonAnnotations);
            jsonClazz.put("methods", jsonMethods);
            jsonClazz.put("fields", jsonFields);

            jsonClasses.put(jsonClazz);

        }

        JSONArray createStatements = new JSONArray();

        // Create Table Statements
//        try {
//            List<String> createTableCommands = PaaSwordQueryHandler.generateOrderedCreateTableStatementsForManyClasses(daoClasses);
//            for (String command : createTableCommands) {
//                createStatements.put(command);
//            }
//        } catch (CyclicDependencyException | NotAValidPaaSwordEntityException | NoClassToProcessException | UnSatisfiedDependencyException ex) {
//            logger.log(Level.SEVERE, ex.getMessage(), ex);
//        }


        jsonObj.put("annotatedCode", jsonClasses);
        jsonObj.put("createStatements", createStatements);

//        logger.info("JSON: " + jsonObj.toString());

        return jsonObj.toString();
    }

    public static Introspect constructParamsJSON(URLClassLoader cl, String apiKey, Map<String, Class> annotatedClasses, Map<String, Class<?>> discoveredDAOs) {

//        List<Class> daoClasses = new ArrayList<>();

        Introspect introspect = new Introspect();

        boolean checkForMethods;
        boolean checkForFields;

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("apiKey", apiKey);
        JSONArray jsonClasses = new JSONArray();

        if (null != annotatedClasses && !annotatedClasses.isEmpty()) {

            for (Map.Entry<String, Class> clazz : annotatedClasses.entrySet()) {

                checkForFields = false;
                checkForMethods = false;

                JSONObject jsonClazz = new JSONObject();
                jsonClazz.put("type", "Class");
                jsonClazz.put("name", clazz.getKey());

                JSONArray jsonAnnotations = new JSONArray();
                JSONArray jsonMethods = new JSONArray();
                JSONArray jsonFields = new JSONArray();

//            try {

                Class tempClass = clazz.getValue();

                for (Annotation tempAnnotation : tempClass.getDeclaredAnnotations()) {

                    if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                        jsonClazz.put("category", "PEP");

                        introspect.setHasPEP(true);

                        // Case 1 - Annotation: PaaSwordPEP
                        JSONObject jsonAnnotation = new JSONObject();

                        jsonAnnotation.put("name", tempAnnotation.annotationType().getName());
                        PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempAnnotation;

                        String value = tempPaaSwordPEPAnnotation.value();

                        JSONArray jsonPolicies = new JSONArray();

                        if (value.contains(",")) {
                            String policies[] = value.split("\\,");

                            for (String policy : policies) {
                                jsonPolicies.put(policy);
                            }

                        } else {
                            jsonPolicies.put(value);
                        }

                        jsonAnnotation.put("value", jsonPolicies);

                        String type = tempPaaSwordPEPAnnotation.type().name();

                        jsonAnnotation.put("type", type);

                        jsonAnnotations.put(jsonAnnotation);

                        checkForMethods = true;
//                        checkForFields = true;

                    } else if (tempAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordEntity")) {

                        jsonClazz.put("category", "DataModel");

                        introspect.setHasDataModel(true);

                        // Case 2 - Annotation: PaaSwordEntity
                        JSONObject jsonAnnotation = new JSONObject();

                        jsonAnnotation.put("name", tempAnnotation.annotationType().getName());
                        PaaSwordEntity tempPaaSwordEntityAnnotation = (PaaSwordEntity) tempAnnotation;
                        jsonAnnotation.put("value", tempPaaSwordEntityAnnotation.value());

                        jsonAnnotations.put(jsonAnnotation);

                        checkForMethods = true;
                        checkForFields = true;

                    } else {

                        // Case 3 - No PaaSword Annotations (have to check its methods)
                        checkForMethods = true;

//                        checkForFields = true;

                    }

                }

                // Check Methods
                if (checkForMethods) {

                    for (Method tempMethod : tempClass.getMethods()) {

                        for (Annotation tempMethodAnnotation : tempMethod.getDeclaredAnnotations()) {

                            if (tempMethodAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {

                                jsonClazz.put("category", "PEP");
                                introspect.setHasPEP(true);

                                JSONObject jsonMethod = new JSONObject();

                                jsonMethod.put("name", tempMethod.getName());

                                // Parameters
                                JSONArray jsonMethodParameters = new JSONArray();

                                for (Parameter tempParameter : tempMethod.getParameters()) {
                                    JSONObject jsonMethodParameter = new JSONObject();

                                    jsonMethodParameter.put("name", tempParameter.getName());
                                    jsonMethodParameter.put("type", tempParameter.getType().getName());

                                    jsonMethodParameters.put(jsonMethodParameter);
                                }


                                jsonMethod.put("parameters", jsonMethodParameters);

                                // Annotations
                                JSONArray jsonMethodAnnotations = new JSONArray();

                                JSONObject jsonMethodAnnotation = new JSONObject();
                                jsonMethodAnnotation.put("name", tempMethodAnnotation.annotationType().getName());
                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempMethodAnnotation;

                                String value = tempPaaSwordPEPAnnotation.value();

                                JSONArray jsonPolicies = new JSONArray();

                                if (value.contains(",")) {
                                    String policies[] = value.split("\\,");

                                    for (String policy : policies) {
                                        jsonPolicies.put(policy);
                                    }

                                } else {
                                    jsonPolicies.put(value);
                                }

                                jsonMethodAnnotation.put("value", jsonPolicies);

                                String type = tempPaaSwordPEPAnnotation.type().name();

                                jsonMethodAnnotation.put("type", type);

                                jsonMethodAnnotations.put(jsonMethodAnnotation);

                                jsonMethod.put("annotations", jsonMethodAnnotations);

                                jsonMethods.put(jsonMethod);
                            }
                        }

                    }

                }


                // Check Fields
                if (checkForFields) {

                    for (Field tempField : tempClass.getDeclaredFields()) {

                        JSONObject jsonField = new JSONObject();

                        jsonField.put("name", tempField.getName());
                        jsonField.put("type", tempField.getType().getName());

                        // Annotations
                        JSONArray jsonFieldAnnotations = new JSONArray();

                        JSONObject jsonFieldAnnotation = new JSONObject();

//                                jsonFieldAnnotation.put("name", tempFieldAnnotation.annotationType().getName());
//                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempFieldAnnotation;
//
//                                String value = tempPaaSwordPEPAnnotation.value();
//
//                                JSONArray jsonPolicies = new JSONArray();
//
//                                if (value.contains(",")) {
//                                    String policies[] = value.split("\\,");
//
//                                    for (String policy : policies) {
//                                        jsonPolicies.put(policy);
//                                    }
//
//                                } else {
//                                    jsonPolicies.put(value);
//                                }
//
//                                jsonFieldAnnotation.put("value", jsonPolicies);

                        jsonFieldAnnotations.put(jsonFieldAnnotation);

                        jsonField.put("annotations", jsonFieldAnnotations);


                        jsonFields.put(jsonField);

//                        for (Annotation tempFieldAnnotation : tempField.getAnnotations()) {
//
//                            if (tempFieldAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {
//
//                                JSONObject jsonField = new JSONObject();
//
//                                jsonField.put("name", tempField.getName());
//                                jsonField.put("type", tempField.getType().getName());
//
//                                // Annotations
//                                JSONArray jsonFieldAnnotations = new JSONArray();
//
//                                JSONObject jsonFieldAnnotation = new JSONObject();
//                                jsonFieldAnnotation.put("name", tempFieldAnnotation.annotationType().getName());
//                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempFieldAnnotation;
//
//                                String value = tempPaaSwordPEPAnnotation.value();
//
//                                JSONArray jsonPolicies = new JSONArray();
//
//                                if (value.contains(",")) {
//                                    String policies[] = value.split("\\,");
//
//                                    for (String policy : policies) {
//                                        jsonPolicies.put(policy);
//                                    }
//
//                                } else {
//                                    jsonPolicies.put(value);
//                                }
//
//                                jsonFieldAnnotation.put("value", jsonPolicies);
//
//                                jsonFieldAnnotations.put(jsonFieldAnnotation);
//
//                                jsonField.put("annotations", jsonFieldAnnotations);
//
//
//                                jsonFields.put(jsonField);
//
//                            } else {
//
//                                JSONObject jsonField = new JSONObject();
//
//                                jsonField.put("name", tempField.getName());
//                                jsonField.put("type", tempField.getType().getName());
//
//                                // Annotations
//                                JSONArray jsonFieldAnnotations = new JSONArray();
//
//                                JSONObject jsonFieldAnnotation = new JSONObject();
////                                jsonFieldAnnotation.put("name", tempFieldAnnotation.annotationType().getName());
////                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempFieldAnnotation;
////
////                                String value = tempPaaSwordPEPAnnotation.value();
////
////                                JSONArray jsonPolicies = new JSONArray();
////
////                                if (value.contains(",")) {
////                                    String policies[] = value.split("\\,");
////
////                                    for (String policy : policies) {
////                                        jsonPolicies.put(policy);
////                                    }
////
////                                } else {
////                                    jsonPolicies.put(value);
////                                }
////
////                                jsonFieldAnnotation.put("value", jsonPolicies);
//
//                                jsonFieldAnnotations.put(jsonFieldAnnotation);
//
//                                jsonField.put("annotations", jsonFieldAnnotations);
//
//
//                                jsonFields.put(jsonField);
//
//                            }
//                        }

                    }

                }


//            } catch (ClassNotFoundException e) {
//                logger.severe("Class " + clazz.getKey() + " cannot be found!");
//            }

                jsonClazz.put("annotations", jsonAnnotations);
                jsonClazz.put("methods", jsonMethods);
                jsonClazz.put("fields", jsonFields);

                jsonClasses.put(jsonClazz);

            }

        }

        List<Class> daoClasses = new ArrayList<>();

        JSONObject dbProxy = new JSONObject();
        JSONArray createStatements = new JSONArray();
        JSONArray fields = new JSONArray();

        if (null != discoveredDAOs && !discoveredDAOs.isEmpty()) {

            discoveredDAOs.entrySet().stream().forEach(dao -> {
                daoClasses.add(dao.getValue());
            });

            // Create Table Statements
            try {
                List<String> createTableCommands = PaaSwordQueryHandler.generateOrderedCreateTableStatementsForManyClasses(daoClasses);
                createTableCommands.forEach(createStatements::put);
            } catch (CyclicDependencyException | NotAValidPaaSwordEntityException | NoClassToProcessException | UnSatisfiedDependencyException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

            // List of Fields
            try {
                List<String> generatedFields = PaaSwordQueryHandler.generateFieldsForManyClasses(daoClasses);
                generatedFields.forEach(fields::put);
            } catch (NotAValidPaaSwordEntityException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

        }


        jsonObj.put("annotatedCode", jsonClasses);
        dbProxy.put("createStatements", createStatements);
        dbProxy.put("fields", fields);
        jsonObj.put("dbProxy", dbProxy);

//        logger.info("JSON: " + jsonObj.toString());

        introspect.setAnnotatedCode(jsonObj.toString());

        return introspect;
    }

    public static Introspect constructParamsJSONFromBCEL(URLClassLoader cl, String apiKey, Map<String, JavaClass> annotatedClasses, Map<String, JavaClass> discoveredDAOs) {

//        List<Class> daoClasses = new ArrayList<>();

        Introspect introspect = new Introspect();

        boolean checkForMethods;
        boolean checkForFields;

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("apiKey", apiKey);
        JSONArray jsonClasses = new JSONArray();

        if (null != annotatedClasses && !annotatedClasses.isEmpty()) {

            for (Map.Entry<String, JavaClass> clazz : annotatedClasses.entrySet()) {

                checkForFields = false;
                checkForMethods = false;

                JSONObject jsonClazz = new JSONObject();
                jsonClazz.put("type", "Class");
                jsonClazz.put("name", clazz.getKey());

                JSONArray jsonAnnotations = new JSONArray();
                JSONArray jsonMethods = new JSONArray();
                JSONArray jsonFields = new JSONArray();

//            try {

                JavaClass tempClass = clazz.getValue();

                for (AnnotationEntry tempAnnotation : tempClass.getAnnotationEntries()) {

                    if (tempAnnotation.getAnnotationType().contains("PaaSwordPEP")) {

                        jsonClazz.put("category", "PEP");

                        introspect.setHasPEP(true);

                        // Case 1 - Annotation: PaaSwordPEP
                        JSONObject jsonAnnotation = new JSONObject();

                        jsonAnnotation.put("name", tempAnnotation.getAnnotationType());
                        PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempAnnotation;

                        String value = tempPaaSwordPEPAnnotation.value();

                        JSONArray jsonPolicies = new JSONArray();

                        if (value.contains(",")) {
                            String policies[] = value.split("\\,");

                            for (String policy : policies) {
                                jsonPolicies.put(policy);
                            }

                        } else {
                            jsonPolicies.put(value);
                        }

                        jsonAnnotation.put("value", jsonPolicies);

                        String type = tempPaaSwordPEPAnnotation.type().name();

                        jsonAnnotation.put("type", type);

                        jsonAnnotations.put(jsonAnnotation);

                        checkForMethods = true;
//                        checkForFields = true;

                    } else if (tempAnnotation.getAnnotationType().contains("PaaSwordEntity")) {

                        jsonClazz.put("category", "DataModel");

                        introspect.setHasDataModel(true);

                        // Case 2 - Annotation: PaaSwordEntity
                        JSONObject jsonAnnotation = new JSONObject();

                        jsonAnnotation.put("name", "PaaSwordEntity");
//                        PaaSwordEntity tempPaaSwordEntityAnnotation = (PaaSwordEntity) tempAnnotation;
                        jsonAnnotation.put("value", "Entity");

                        jsonAnnotations.put(jsonAnnotation);

                        checkForMethods = true;
                        checkForFields = true;

                    } else {

                        // Case 3 - No PaaSword Annotations (have to check its methods)
                        checkForMethods = true;

//                        checkForFields = true;

                    }

                }

                // Check Methods
                if (checkForMethods) {

                    for (org.apache.bcel.classfile.Method tempMethod : tempClass.getMethods()) {

                        for (AnnotationEntry tempMethodAnnotation : tempMethod.getAnnotationEntries()) {

                            if (tempMethodAnnotation.getAnnotationType().contains("PaaSwordPEP")) {

                                jsonClazz.put("category", "PEP");
                                introspect.setHasPEP(true);

                                JSONObject jsonMethod = new JSONObject();

                                jsonMethod.put("name", tempMethod.getName());

                                // Parameters
                                JSONArray jsonMethodParameters = new JSONArray();

                                for (org.apache.bcel.generic.Type tempParameter : tempMethod.getArgumentTypes()) {
                                    JSONObject jsonMethodParameter = new JSONObject();

                                    jsonMethodParameter.put("name", tempParameter.getSignature());
                                    jsonMethodParameter.put("type", tempParameter.getSignature());

                                    jsonMethodParameters.put(jsonMethodParameter);
                                }


                                jsonMethod.put("parameters", jsonMethodParameters);

                                // Annotations
                                JSONArray jsonMethodAnnotations = new JSONArray();

                                JSONObject jsonMethodAnnotation = new JSONObject();
                                jsonMethodAnnotation.put("name", "eu.paasword.annotations.PaaSwordPEP");

                                ElementValuePair[] elements = tempMethodAnnotation.getElementValuePairs();

                                JSONArray jsonPolicies = new JSONArray();

                                if (null != elements && elements.length > 0) {

                                    Arrays.stream(elements).forEach(element -> {

                                        ElementValue value = element.getValue();

                                        logger.info("Value: " + value.toString());

                                        jsonPolicies.put(value.toString());

                                    });

                                }


//                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempMethodAnnotation;
//
//                                String value = tempPaaSwordPEPAnnotation.value();

//                                if (value.contains(",")) {
//                                    String policies[] = value.split("\\,");
//
//                                    for (String policy : policies) {
//                                        jsonPolicies.put(policy);
//                                    }
//
//                                } else {
//                                    jsonPolicies.put(value);
//                                }

                                jsonMethodAnnotation.put("value", jsonPolicies);

                                String type = "POLICY"; //tempPaaSwordPEPAnnotation.type().name();

                                jsonMethodAnnotation.put("type", type);

                                jsonMethodAnnotations.put(jsonMethodAnnotation);

                                jsonMethod.put("annotations", jsonMethodAnnotations);

                                jsonMethods.put(jsonMethod);
                            }
                        }

                    }

                }


                // Check Fields
                if (checkForFields) {

                    for (org.apache.bcel.classfile.Field tempField : tempClass.getFields()) {

                        JSONObject jsonField = new JSONObject();

                        jsonField.put("name", tempField.getName());

                        String type = tempField.getType().getSignature();

                        if (type.toLowerCase().contains("string")) {
                            jsonField.put("type", "java.lang.String");
                        } else if (type.toLowerCase().contains("date")) {
                            jsonField.put("type", "java.util.Date");
                        } else if (type.toLowerCase().contains("int")) {
                            jsonField.put("type", "int");
                        } else if (type.toLowerCase().contains("double")) {
                            jsonField.put("type", "double");
                        } else if (type.toLowerCase().contains("long")) {
                            jsonField.put("type", "long");
                        } else {
                            jsonField.put("type", type);
                        }

                        // Annotations
                        JSONArray jsonFieldAnnotations = new JSONArray();

                        JSONObject jsonFieldAnnotation = new JSONObject();

//                                jsonFieldAnnotation.put("name", tempFieldAnnotation.annotationType().getName());
//                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempFieldAnnotation;
//
//                                String value = tempPaaSwordPEPAnnotation.value();
//
//                                JSONArray jsonPolicies = new JSONArray();
//
//                                if (value.contains(",")) {
//                                    String policies[] = value.split("\\,");
//
//                                    for (String policy : policies) {
//                                        jsonPolicies.put(policy);
//                                    }
//
//                                } else {
//                                    jsonPolicies.put(value);
//                                }
//
//                                jsonFieldAnnotation.put("value", jsonPolicies);

                        jsonFieldAnnotations.put(jsonFieldAnnotation);

                        jsonField.put("annotations", jsonFieldAnnotations);


                        jsonFields.put(jsonField);

//                        for (Annotation tempFieldAnnotation : tempField.getAnnotations()) {
//
//                            if (tempFieldAnnotation.annotationType().getName().equals("eu.paasword.annotations.PaaSwordPEP")) {
//
//                                JSONObject jsonField = new JSONObject();
//
//                                jsonField.put("name", tempField.getName());
//                                jsonField.put("type", tempField.getType().getName());
//
//                                // Annotations
//                                JSONArray jsonFieldAnnotations = new JSONArray();
//
//                                JSONObject jsonFieldAnnotation = new JSONObject();
//                                jsonFieldAnnotation.put("name", tempFieldAnnotation.annotationType().getName());
//                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempFieldAnnotation;
//
//                                String value = tempPaaSwordPEPAnnotation.value();
//
//                                JSONArray jsonPolicies = new JSONArray();
//
//                                if (value.contains(",")) {
//                                    String policies[] = value.split("\\,");
//
//                                    for (String policy : policies) {
//                                        jsonPolicies.put(policy);
//                                    }
//
//                                } else {
//                                    jsonPolicies.put(value);
//                                }
//
//                                jsonFieldAnnotation.put("value", jsonPolicies);
//
//                                jsonFieldAnnotations.put(jsonFieldAnnotation);
//
//                                jsonField.put("annotations", jsonFieldAnnotations);
//
//
//                                jsonFields.put(jsonField);
//
//                            } else {
//
//                                JSONObject jsonField = new JSONObject();
//
//                                jsonField.put("name", tempField.getName());
//                                jsonField.put("type", tempField.getType().getName());
//
//                                // Annotations
//                                JSONArray jsonFieldAnnotations = new JSONArray();
//
//                                JSONObject jsonFieldAnnotation = new JSONObject();
////                                jsonFieldAnnotation.put("name", tempFieldAnnotation.annotationType().getName());
////                                PaaSwordPEP tempPaaSwordPEPAnnotation = (PaaSwordPEP) tempFieldAnnotation;
////
////                                String value = tempPaaSwordPEPAnnotation.value();
////
////                                JSONArray jsonPolicies = new JSONArray();
////
////                                if (value.contains(",")) {
////                                    String policies[] = value.split("\\,");
////
////                                    for (String policy : policies) {
////                                        jsonPolicies.put(policy);
////                                    }
////
////                                } else {
////                                    jsonPolicies.put(value);
////                                }
////
////                                jsonFieldAnnotation.put("value", jsonPolicies);
//
//                                jsonFieldAnnotations.put(jsonFieldAnnotation);
//
//                                jsonField.put("annotations", jsonFieldAnnotations);
//
//
//                                jsonFields.put(jsonField);
//
//                            }
//                        }

                    }

                }


//            } catch (ClassNotFoundException e) {
//                logger.severe("Class " + clazz.getKey() + " cannot be found!");
//            }

                jsonClazz.put("annotations", jsonAnnotations);
                jsonClazz.put("methods", jsonMethods);
                jsonClazz.put("fields", jsonFields);

                jsonClasses.put(jsonClazz);

            }

        }

        List<JavaClass> daoClasses = new ArrayList<>();

        JSONObject dbProxy = new JSONObject();
        JSONArray createStatements = new JSONArray();
        JSONArray fields = new JSONArray();

        if (null != discoveredDAOs && !discoveredDAOs.isEmpty()) {

            discoveredDAOs.entrySet().stream().forEach(dao -> {
                daoClasses.add(dao.getValue());
            });

            // Create Table Statements
            try {
                List<String> createTableCommands = PaaSwordQueryHandler.generateOrderedCreateTableStatementsForManyClassesWithBcel(daoClasses);
                createTableCommands.forEach(createStatements::put);
            } catch (CyclicDependencyException | NotAValidPaaSwordEntityException | NoClassToProcessException | UnSatisfiedDependencyException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

            // List of Fields
            try {
                List<String> generatedFields = PaaSwordQueryHandler.generateFieldsForManyClassesWithBcel(daoClasses);
                generatedFields.forEach(fields::put);
            } catch (NotAValidPaaSwordEntityException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

        }

        jsonObj.put("annotatedCode", jsonClasses);
        dbProxy.put("createStatements", createStatements);
        dbProxy.put("fields", fields);
        jsonObj.put("dbProxy", dbProxy);

        logger.info("JSON: " + jsonObj.toString());

        introspect.setAnnotatedCode(jsonObj.toString());

        return introspect;
    }

}
