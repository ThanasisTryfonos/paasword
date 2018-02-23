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
package eu.paasword.jpa;

import eu.paasword.jpa.exceptions.CyclicDependencyException;
import eu.paasword.jpa.exceptions.NoClassToProcessException;
import eu.paasword.jpa.exceptions.NotAValidPaaSwordEntityException;
import eu.paasword.jpa.exceptions.UnSatisfiedDependencyException;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Panagiotis Gouvas (pgouvas@ubitech.eu)
 */
public class PaaSwordQueryHandler {

    public static final String PAASWORD_ENTITY = "eu.paasword.annotations.PaaSwordEntity";
    public static final String PAASWORD_PRIMARYID = "javax.persistence.Id";
    public static final String PAASWORD_COLUMN = "javax.persistence.Column";
    public static final String PAASWORD_MANYTOONE = "javax.persistence.ManyToOne";
    public static final String PAASWORD_ONETOONE = "javax.persistence.OneToOne";
    public static final String PAASWORD_DEFAULTLENGTH = "255";

    private static PaaSwordQueryHandler instance = null;
    private static final Logger logger = Logger.getLogger(PaaSwordQueryHandler.class.getName());

    protected PaaSwordQueryHandler() {
        // Exists only to defeat instantiation.
    }

    public static PaaSwordQueryHandler getInstance() {
        if (instance == null) {
            instance = new PaaSwordQueryHandler();
        }
        return instance;
    }//EoM

    public static List<String> generateOrderedCreateTableStatementsForManyClasses(List<Class> classes) throws CyclicDependencyException, NotAValidPaaSwordEntityException, NoClassToProcessException, UnSatisfiedDependencyException {
        List<String> retlist = new ArrayList<>();
        List<String> templist = new ArrayList<>();
        int amountofclasses = classes.size();
        //indexed is used to identify the relative position
        ArrayList index = new ArrayList();
        for (Class clazz : classes) {
            index.add(clazz.getSimpleName().toLowerCase());
        }
        //define directed graph structure
        int[][] matrix = new int[amountofclasses][amountofclasses]; //initialized to zero
        //printMatrix(matrix);
        //initialize matrix
        int row = 0;
        for (Class clazz : classes) {
            String processstr = generateCreateTableStatementForClass(clazz);
            templist.add(processstr);
            //e.g. CREATE TABLE student   (id int  primary key  , name char (50)  not null , surname char (50)  not null , birth_date Date   not null , gender char(1)   not null , semester int   not null , grade double precision    , fk_university int     references university , fk_faculty int     references faculty ) 
            int pivot = 0;
            while (processstr.indexOf("references", pivot) != -1) {
                //new reference found
                pivot = processstr.indexOf("references", pivot);
                String found = processstr.substring(pivot + 11, processstr.indexOf(" ", pivot + 11)).trim();
                int indexed = index.indexOf(found);

                if (indexed == -1) {
                    throw new UnSatisfiedDependencyException("Class " + clazz.getSimpleName() + " has Reference with " + found + " but no respective class has been found");
                }
                logger.info("%%%%%%%% Class " + clazz.getSimpleName() + " has Reference with " + found + " (" + indexed + ") at pivot (" + pivot + ")");

                matrix[row][indexed] = 1;
                //move pivot in the string
                pivot += 11;  //the size of string references
            }//while
            row++;
        }//for

        if (row == 0) {
            throw new NoClassToProcessException("No Classes have been provided as input");
        }

        //printMatrix(matrix);
        boolean isDAGAcyclic = true;
        if (row > 1) {
            isDAGAcyclic = !hasDAGACycles(matrix);      //check that there are no circles in case of two nodes and more
        }
        if (!isDAGAcyclic) {
            throw new CyclicDependencyException("Classes contain Cyclic dependencies and cannot be processed");
        } else { //Acyclic Graph
//            logger.info("Graph has no cycles! Optimal order will be calculated");
            printMatrix(matrix);
            List<Integer> orderedindexofclasses = inferIterateOrder(matrix);
            for (Integer orderedindex : orderedindexofclasses) {
                //define final correct list of loading classes 
                retlist.add(templist.get(orderedindex));
            }//
        }

        return retlist;
    }//EoM

    public static List<String> generateOrderedCreateTableStatementsForManyClassesWithBcel(List<JavaClass> classes) throws CyclicDependencyException, NotAValidPaaSwordEntityException, NoClassToProcessException, UnSatisfiedDependencyException {
        List<String> retlist = new ArrayList<>();
        List<String> templist = new ArrayList<>();
        int amountofclasses = classes.size();
        //indexed is used to identify the relative position
        ArrayList index = new ArrayList();
        for (JavaClass clazz : classes) {
            index.add(clazz.getClassName().toLowerCase());
        }
        //define directed graph structure
        int[][] matrix = new int[amountofclasses][amountofclasses]; //initialized to zero
        //printMatrix(matrix);
        //initialize matrix
        int row = 0;
        for (JavaClass clazz : classes) {
            String processstr = generateCreateTableStatementForClass(clazz);
            templist.add(processstr);
            //e.g. CREATE TABLE student   (id int  primary key  , name char (50)  not null , surname char (50)  not null , birth_date Date   not null , gender char(1)   not null , semester int   not null , grade double precision    , fk_university int     references university , fk_faculty int     references faculty )
            int pivot = 0;
            while (processstr.indexOf("references", pivot) != -1) {
                //new reference found
                pivot = processstr.indexOf("references", pivot);
                String found = processstr.substring(pivot + 11, processstr.indexOf(" ", pivot + 11)).trim();
                int indexed = index.indexOf(found);

                if (indexed == -1) {
                    throw new UnSatisfiedDependencyException("Class " + clazz.getClassName() + " has Reference with " + found + " but no respective class has been found");
                }
                logger.info("%%%%%%%% Class " + clazz.getClassName() + " has Reference with " + found + " (" + indexed + ") at pivot (" + pivot + ")");

                matrix[row][indexed] = 1;
                //move pivot in the string
                pivot += 11;  //the size of string references
            }//while
            row++;
        }//for

        if (row == 0) {
            throw new NoClassToProcessException("No Classes have been provided as input");
        }

        //printMatrix(matrix);
        boolean isDAGAcyclic = true;
        if (row > 1) {
            isDAGAcyclic = !hasDAGACycles(matrix);      //check that there are no circles in case of two nodes and more
        }
        if (!isDAGAcyclic) {
            throw new CyclicDependencyException("Classes contain Cyclic dependencies and cannot be processed");
        } else { //Acyclic Graph
//            logger.info("Graph has no cycles! Optimal order will be calculated");
            printMatrix(matrix);
            List<Integer> orderedindexofclasses = inferIterateOrder(matrix);
            for (Integer orderedindex : orderedindexofclasses) {
                //define final correct list of loading classes
                retlist.add(templist.get(orderedindex));
            }//
        }

        return retlist;
    }//EoM


    private static List<Integer> inferIterateOrder(int[][] matrix) {
        List<Integer> leafs = new ArrayList();

        int[] freqofsources = new int[matrix.length]; //initialized to zero        
        int iteration = 0;
        do {
            for (int i = 0; i < matrix.length; i++) {
                int amountofzeroforsource = 0;
                for (int j = 0; j < matrix.length; j++) {
                    if (matrix[i][j] == 0) {
                        amountofzeroforsource++;
                    }
                }//for
                freqofsources[i] = amountofzeroforsource;
            }//for       
            //find the max and report which sources do not have dependencies - therefore they can be removed from target columns
            printArrayVertical(freqofsources);
            int max = findMax(freqofsources);
            //printArrayVertical(freqofsources);
            List<Integer> sourceelementsthatsatisfymax = findSourceElementsThatSatisfyMaxZeros(max, freqofsources);
            logger.info("At iteration: " + iteration + " the max was: " + max + " and the sources that satisfy it were: " + sourceelementsthatsatisfymax);
            //add them to return structure and CLEAR THEIR columns
            for (Integer tobeexcluded : sourceelementsthatsatisfymax) {
                if (!leafs.contains(tobeexcluded)) {                //double check
                    leafs.add(tobeexcluded);
                    for (int i = 0; i < matrix.length; i++) {
                        matrix[i][tobeexcluded] = 0;
                    }
                }
            }//for
            iteration++;
        } while (leafs.size() != matrix.length);

        logger.info("Final Order: ");
        printArrayHorizontal(leafs);

        return leafs;
    }//EoM

    private static List<Integer> findSourceElementsThatSatisfyMaxZeros(int max, int[] freqofsources) {
        List<Integer> retlist = new ArrayList<>();
        for (int i = 0; i < freqofsources.length; i++) {
            if (freqofsources[i] == max) {
                retlist.add(i);
            }
        }//for
        return retlist;
    }//EoM

    private static int findMax(int[] array) {
        Arrays.sort(array.clone());     //sos it has to be cloned because it passes by reference
        return array[0];
    }//EoM

    private static void printArrayVertical(int[] array) {
        String row = "\n";
        for (int i = 0; i < array.length; i++) {
            row += array[i] + "\n";
        }//for
        logger.info(row);
    } //EoM   

    private static void printArrayHorizontal(List<Integer> array) {
        String row = "\n";
        for (int i = 0; i < array.size(); i++) {
            row += array.get(i) + " ";
        }//for
        logger.info(row);
    } //EoM   

    private static void printMatrix(int[][] matrix) {
        String row = "\n";
        for (int i = 0; i < matrix.length; i++) {

            for (int j = 0; j < matrix.length; j++) {
                row += " " + matrix[i][j];
            }//for
            row += "\n";
        }//for
        logger.info(row);
    }//EoM

    /*
    * If at least one '1' exists at a[i][i] then there is at least one circle
     */
    private static boolean hasDAGACycles(int[][] matrix) {
        boolean ret = false;
        int[][] product = multiplicar(matrix, matrix);
        for (int i = 0; i < product.length; i++) {
            boolean ismiddleOne = (product[i][i] == 1);
            if (ismiddleOne) {
                ret = true;
                break;
            }
        }//for
        return ret;
    }//EoM

    public static int[][] multiplicar(int[][] A, int[][] B) {

        int aRows = A.length;
        int aColumns = A[0].length;
        int bRows = B.length;
        int bColumns = B[0].length;

        if (aColumns != bRows) {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }

        int[][] C = new int[aRows][bColumns];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                C[i][j] = 0;
            }
        }

        for (int i = 0; i < aRows; i++) { // aRow
            for (int j = 0; j < bColumns; j++) { // bColumn
                for (int k = 0; k < aColumns; k++) { // aColumn
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        return C;
    }//EoM    

    public static String generateCreateTableStatementForClass(Class klazz) throws NotAValidPaaSwordEntityException {
        String query = "";
        String classname = klazz.getSimpleName();

        List<String> fields = new ArrayList();
        List<String> types = new ArrayList();
        List<String> typeconsrtaints = new ArrayList();
        List<String> refs = new ArrayList();
        List<Boolean> notnulls = new ArrayList();
        List<Boolean> primkeys = new ArrayList();

        //check that it is a password entity
        Annotation[] classannotations = klazz.getAnnotations();
        boolean ispaaswordentity = false;
        for (Annotation classannotation : classannotations) {
            String annotationcanonicalname = classannotation.annotationType().getCanonicalName();
            //logger.info("<" + annotationcanonicalname + ">");
            if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ENTITY)) {
                ispaaswordentity = true;
                break;
            }//if
        }//for

        //Start introspection in case of paasword entity
        if (!ispaaswordentity) {
            logger.log(Level.SEVERE, "Exception: Class " + classname + " is not a PaaSword entity");
            throw new NotAValidPaaSwordEntityException("Exception: Class " + classname + " is not a PaaSword entity");
        } else {
            logger.info("!!!!!!!!!!!!!!! Class " + classname + " is a valid PaaSword class");
            Field[] fieldz = klazz.getDeclaredFields();
            for (Field field : fieldz) {
                String fieldname = field.getName();
                logger.info("@@@@@@@@@@@@@ Examining field: " + fieldname);
                Annotation[] fieldannotations = field.getAnnotations();
                //initialflags
                boolean ispaaswordcolumn = false;
                boolean isid = false;
                boolean notnull = false;
                String constraint = "";
                boolean isprimitive = true;
                for (Annotation attributeannotation : fieldannotations) {
                    String annotationcanonicalname = attributeannotation.annotationType().getCanonicalName();
                    logger.info("############# Examining fieldannotation: <" + annotationcanonicalname + ">");
                    //switch annotation name
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_COLUMN)) {
                        //set column to true which will affect the overall behavior of the parsing
                        ispaaswordcolumn = true;
                        isprimitive = true;
                        logger.info("++++++ " + attributeannotation.toString());
                        HashMap<String, String> fieldvalues = parseAnnotationFields(attributeannotation.toString());
                        notnull = fieldvalues.get("nullable").equalsIgnoreCase("false");
                        constraint = fieldvalues.get("length");
                    }//ιf
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_PRIMARYID)) {
                        isid = true;
                    }//if
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_MANYTOONE)) {
                        ispaaswordcolumn = true;
                        isprimitive = false;
                        logger.info("++++++ " + attributeannotation.toString());
                    }//if
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ONETOONE)) {
                        ispaaswordcolumn = true;
                        isprimitive = false;
                        logger.info("++++++ " + attributeannotation.toString());
                    }//if

                }//for checking all annotations of a field (class attribute)

                logger.info("$$$$$$$$$$ Field analysis for: <" + fieldname + "> ispaaswordcolumne: " + ispaaswordcolumn + " isid: " + isid + " isprimitive: " + isprimitive);

                //If it is a PaaSword field it has to be processed
                if (!ispaaswordcolumn) {
                    logger.log(Level.WARNING, "Field " + fieldname + " is not a PaaSword Column and it will be ignored");
                } else {
                    logger.log(Level.INFO, "^^^^ {0} -- {1} -- {2} ", new Object[]{field.getType().getSimpleName(), field.getName(), field.getType()});
                    //make it accessible in order to get a handle in the value
                    field.setAccessible(true);
                    //step 1 - add field name
                    if (isprimitive) {
                        fields.add(field.getName());
                    } else {
                        fields.add(inferColumnNameForCustomClassType(field.getType().getSimpleName(), field.getName())); //SOS Country and not country1 in case of declaration Country country1
                    }
                    //step 2 - add type
                    if (isprimitive) {
                        types.add(inferDBTypeForPrimitiveClassType("" + field.getType().getSimpleName()));
                    } else {
                        types.add("int");
                    }
                    //step 3 - define if null
                    notnulls.add(notnull);
                    //step 4 - is it a primary key
                    primkeys.add(isid);
                    //step 5 - length constraints
                    if (isprimitive) {
                        typeconsrtaints.add((constraint.equals(PAASWORD_DEFAULTLENGTH)) ? null : constraint);
                    } else {
                        typeconsrtaints.add(null);
                    }
                    //step 6 - references
                    if (isprimitive) {
                        refs.add(null);
                    } else {
                        refs.add(" references " + field.getType().getSimpleName().toLowerCase() + " ");
                    }

                }//if a valid paasword annotation is identified

            }//for      
            if (fields.size() == 0) {
                logger.log(Level.SEVERE, "Class " + classname + " has no PaaSWord columns so no query can be created");
                //throw
            } else {
                query = createTableStatementGenerator(classname, fields, types, typeconsrtaints, notnulls, primkeys, refs);
                logger.info("Query for creating: " + query);
            }
        }//else handle password entity
        return query;
    }//EoM store

    public static String generateCreateTableStatementForClass(JavaClass klazz) throws NotAValidPaaSwordEntityException {
        String query = "";
        String classname = klazz.getClassName().substring(klazz.getClassName().lastIndexOf(".") + 1);

        List<String> fields = new ArrayList();
        List<String> types = new ArrayList();
        List<String> typeconsrtaints = new ArrayList();
        List<String> refs = new ArrayList();
        List<Boolean> notnulls = new ArrayList();
        List<Boolean> primkeys = new ArrayList();

        //check that it is a password entity
        AnnotationEntry[] classannotations = klazz.getAnnotationEntries();
        boolean ispaaswordentity = false;
        for (AnnotationEntry classannotation : classannotations) {
            String annotationcanonicalname = classannotation.getAnnotationType();
            logger.info("<" + annotationcanonicalname + ">");
            if (annotationcanonicalname.contains("PaaSwordEntity")) {
                ispaaswordentity = true;
                break;
            }//if
        }//for

        //Start introspection in case of paasword entity
        if (!ispaaswordentity) {
            logger.log(Level.SEVERE, "Exception: Class " + classname + " is not a PaaSword entity");
            throw new NotAValidPaaSwordEntityException("Exception: Class " + classname + " is not a PaaSword entity");
        } else {
            logger.info("!!!!!!!!!!!!!!! Class " + classname + " is a valid PaaSword class");
            org.apache.bcel.classfile.Field[] fieldz = klazz.getFields();
            for (org.apache.bcel.classfile.Field field : fieldz) {
                String fieldname = field.getName();
                logger.info("@@@@@@@@@@@@@ Examining field: " + fieldname);
                AnnotationEntry[] fieldannotations = field.getAnnotationEntries();
                //initialflags
                boolean ispaaswordcolumn = false;
                boolean isid = false;
                boolean notnull = false;
                String constraint = "";
                boolean isprimitive = true;
                for (AnnotationEntry attributeannotation : fieldannotations) {
                    String annotationcanonicalname = attributeannotation.getAnnotationType();

                    annotationcanonicalname = annotationcanonicalname.substring(1, annotationcanonicalname.length() - 1);
                    annotationcanonicalname = annotationcanonicalname.replace("/", ".");

                    logger.info("############# Examining fieldannotation: <" + annotationcanonicalname + ">");
                    //switch annotation name
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_COLUMN)) {
                        //set column to true which will affect the overall behavior of the parsing
                        ispaaswordcolumn = true;
                        isprimitive = true;
                        logger.info("++++++ " + attributeannotation.toString());
                        HashMap<String, String> fieldvalues = parseAnnotationFields(attributeannotation.toString());
                        notnull = false; //fieldvalues.get("nullable").equalsIgnoreCase("false");
                        constraint = "225"; //fieldvalues.get("length");
                    }//ιf
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_PRIMARYID)) {
                        isid = true;
                    }//if
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_MANYTOONE)) {
                        ispaaswordcolumn = true;
                        isprimitive = false;
                        logger.info("++++++ " + attributeannotation.toString());
                    }//if
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ONETOONE)) {
                        ispaaswordcolumn = true;
                        isprimitive = false;
                        logger.info("++++++ " + attributeannotation.toString());
                    }//if

                }//for checking all annotations of a field (class attribute)

                logger.info("$$$$$$$$$$ Field analysis for: <" + fieldname + "> ispaaswordcolumne: " + ispaaswordcolumn + " isid: " + isid + " isprimitive: " + isprimitive);

                //If it is a PaaSword field it has to be processed
                if (!ispaaswordcolumn) {
                    logger.log(Level.WARNING, "Field " + fieldname + " is not a PaaSword Column and it will be ignored");
                } else {
                    logger.log(Level.INFO, "^^^^ {0} -- {1} -- {2} ", new Object[]{field.getType().getSignature(), field.getName(), field.getType()});
                    //make it accessible in order to get a handle in the value
//                    field.setAccessible(true);
                    //step 1 - add field name

                    logger.info("Signature: " + field.getType().getSignature() + ", Field Type: " + field.getType().toString());

                    String signature = field.getType().getSignature();

                    if (signature.contains(";")) {
                        signature = signature.substring(1, signature.length() - 1);
                        signature = signature.replace("/", ".");
                        signature = signature.substring(signature.lastIndexOf(".") + 1);
                    }

                    logger.info("New Signature: " + signature);

                    if (isprimitive) {
                        fields.add(field.getName());

                    } else {
                        fields.add(inferColumnNameForCustomClassType(signature, field.getName())); //SOS Country and not country1 in case of declaration Country country1
                    }
                    //step 2 - add type
                    if (isprimitive) {
                        types.add(inferDBTypeForPrimitiveClassType(signature));
                    } else {
                        types.add("int");
                    }
                    //step 3 - define if null
                    notnulls.add(notnull);
                    //step 4 - is it a primary key
                    primkeys.add(isid);
                    //step 5 - length constraints
                    if (isprimitive) {
                        if (signature.equals("String")) {
                            typeconsrtaints.add((constraint.equals(PAASWORD_DEFAULTLENGTH)) ? null : constraint);
                        } else {
                            typeconsrtaints.add(null);
                        }
                    } else {
                        typeconsrtaints.add(null);
                    }
                    //step 6 - references
                    if (isprimitive) {
                        refs.add(null);
                    } else {
                        refs.add(" references " + signature.toLowerCase() + " ");
                    }

                }//if a valid paasword annotation is identified

            }//for
            if (fields.size() == 0) {
                logger.log(Level.SEVERE, "Class " + classname + " has no PaaSWord columns so no query can be created");
                //throw
            } else {
                query = createTableStatementGenerator(classname, fields, types, typeconsrtaints, notnulls, primkeys, refs);
                logger.info("Query for creating: " + query);
            }
        }//else handle password entity
        return query;
    }//EoM store

    public static List<String> generateFieldsForClass(Class klazz) throws NotAValidPaaSwordEntityException {
        String classname = klazz.getSimpleName();
        List<String> fields = new ArrayList();
        //check that it is a password entity
        Annotation[] classannotations = klazz.getAnnotations();
        boolean ispaaswordentity = false;
        for (Annotation classannotation : classannotations) {
            String annotationcanonicalname = classannotation.annotationType().getCanonicalName();
            //logger.info("<" + annotationcanonicalname + ">");
            if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ENTITY)) {
                ispaaswordentity = true;
                break;
            }//if
        }//for

        //Start introspection in case of paasword entity
        if (!ispaaswordentity) {
            logger.log(Level.SEVERE, "Exception: Class " + classname + " is not a PaaSword entity");
            throw new NotAValidPaaSwordEntityException("Exception: Class " + classname + " is not a PaaSword entity");
        } else {
            logger.info("!!!!!!!!!!!!!!! Class " + classname + " is a valid PaaSword class");
            Field[] fieldz = klazz.getDeclaredFields();
            for (Field field : fieldz) {
                String fieldname = field.getName();
                logger.info("@@@@@@@@@@@@@ Examining field: " + fieldname);
                Annotation[] fieldannotations = field.getAnnotations();
                //initialflags
                boolean ispaaswordcolumn = false;
                boolean isid = false;
                boolean notnull = false;
                String constraint = "";
                boolean isprimitive = true;
                for (Annotation attributeannotation : fieldannotations) {
                    String annotationcanonicalname = attributeannotation.annotationType().getCanonicalName();
                    logger.info("############# Examining fieldannotation: <" + annotationcanonicalname + ">");
                    //switch annotation name
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_COLUMN)) {
                        //set column to true which will affect the overall behavior of the parsing
                        ispaaswordcolumn = true;
                        isprimitive = true;
                        logger.info("++++++ " + attributeannotation.toString());
                        HashMap<String, String> fieldvalues = parseAnnotationFields(attributeannotation.toString());
                        notnull = fieldvalues.get("nullable").equalsIgnoreCase("false");
                        constraint = fieldvalues.get("length");
                    }//ιf
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_PRIMARYID)) {
                        isid = true;
                    }//if
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_MANYTOONE)) {
                        ispaaswordcolumn = true;
                        isprimitive = false;
                        logger.info("++++++ " + attributeannotation.toString());
                    }//if
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ONETOONE)) {
                        ispaaswordcolumn = true;
                        isprimitive = false;
                        logger.info("++++++ " + attributeannotation.toString());
                    }//if

                }//for checking all annotations of a field (class attribute)

                logger.info("$$$$$$$$$$ Field analysis for: <" + fieldname + "> ispaaswordcolumne: " + ispaaswordcolumn + " isid: " + isid + " isprimitive: " + isprimitive);

                //If it is a PaaSword field it has to be processed
                if (!ispaaswordcolumn) {
                    logger.log(Level.WARNING, "Field " + fieldname + " is not a PaaSword Column and it will be ignored");
                } else {
                    logger.log(Level.INFO, "^^^^ {0} -- {1} -- {2} ", new Object[]{field.getType().getSimpleName(), field.getName(), field.getType()});
                    //make it accessible in order to get a handle in the value
                    field.setAccessible(true);
                    //step 1 - add field name
                    if (isprimitive) {
                        fields.add(classname.toLowerCase() + "." + field.getName());
                    } else {
                        fields.add(classname.toLowerCase() + "." + inferColumnNameForCustomClassType(field.getType().getSimpleName(), field.getName()));   //SOS Country and not country1 in case of declaration Country country1
                    }
                }//if a valid paasword annotation is identified
            }//for      
        }//else handle password entity
        return fields;
    }//EoM store

    public static List<String> generateFieldsForClass(JavaClass klazz) throws NotAValidPaaSwordEntityException {
        String classname = klazz.getClassName().substring(klazz.getClassName().lastIndexOf(".") + 1);
        List<String> fields = new ArrayList();
        //check that it is a password entity
        AnnotationEntry[] classannotations = klazz.getAnnotationEntries();
        boolean ispaaswordentity = false;
        for (AnnotationEntry classannotation : classannotations) {
            String annotationcanonicalname = classannotation.getAnnotationType();
            logger.info("<" + annotationcanonicalname + ">");
            if (annotationcanonicalname.contains("PaaSwordEntity")) {
                ispaaswordentity = true;
                break;
            }//if
        }//for

        //Start introspection in case of paasword entity
        if (!ispaaswordentity) {
            logger.log(Level.SEVERE, "Exception: Class " + classname + " is not a PaaSword entity");
            throw new NotAValidPaaSwordEntityException("Exception: Class " + classname + " is not a PaaSword entity");
        } else {
            logger.info("!!!!!!!!!!!!!!! Class " + classname + " is a valid PaaSword class");
            org.apache.bcel.classfile.Field[] fieldz = klazz.getFields();
            for (org.apache.bcel.classfile.Field field : fieldz) {
                String fieldname = field.getName();
                logger.info("@@@@@@@@@@@@@ Examining field: " + fieldname);
                AnnotationEntry[] fieldannotations = field.getAnnotationEntries();
                //initialflags
                boolean ispaaswordcolumn = false;
                boolean isid = false;
                boolean notnull = false;
                String constraint = "";
                boolean isprimitive = true;
                for (AnnotationEntry attributeannotation : fieldannotations) {
                    String annotationcanonicalname = attributeannotation.getAnnotationType();

                    annotationcanonicalname = annotationcanonicalname.substring(1, annotationcanonicalname.length() - 1);
                    annotationcanonicalname = annotationcanonicalname.replace("/", ".");

                    logger.info("############# Examining fieldannotation: <" + annotationcanonicalname + ">");
                    //switch annotation name
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_COLUMN)) {
                        //set column to true which will affect the overall behavior of the parsing
                        ispaaswordcolumn = true;
                        isprimitive = true;
                        logger.info("++++++ " + attributeannotation.toString());
                        HashMap<String, String> fieldvalues = parseAnnotationFields(attributeannotation.toString());
                        notnull = true;// fieldvalues.get("nullable").equalsIgnoreCase("false");
                        constraint = "255";//fieldvalues.get("length");
                    }//ιf
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_PRIMARYID)) {
                        isid = true;
                    }//if
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_MANYTOONE)) {
                        ispaaswordcolumn = true;
                        isprimitive = false;
                        logger.info("++++++ " + attributeannotation.toString());
                    }//if
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ONETOONE)) {
                        ispaaswordcolumn = true;
                        isprimitive = false;
                        logger.info("++++++ " + attributeannotation.toString());
                    }//if

                }//for checking all annotations of a field (class attribute)

                logger.info("$$$$$$$$$$ Field analysis for: <" + fieldname + "> ispaaswordcolumne: " + ispaaswordcolumn + " isid: " + isid + " isprimitive: " + isprimitive);

                //If it is a PaaSword field it has to be processed
                if (!ispaaswordcolumn) {
                    logger.log(Level.WARNING, "Field " + fieldname + " is not a PaaSword Column and it will be ignored");
                } else {
                    logger.log(Level.INFO, "^^^^ {0} -- {1} -- {2} ", new Object[]{field.getType().getSignature(), field.getName(), field.getType()});
                    //make it accessible in order to get a handle in the value
//                    field.setAccessible(true);
                    //step 1 - add field name
                    if (isprimitive) {
                        fields.add(classname.toLowerCase() + "." + field.getName());
                    } else {

                        logger.info("Signature: " + field.getType().getSignature());

                        String signature = field.getType().getSignature();

                        if (signature.contains(";")) {
                            signature = signature.substring(1, signature.length() - 1);
                            signature = signature.replace("/", ".");
                            signature = signature.substring(signature.lastIndexOf(".") + 1);
                        }


                        fields.add(classname.toLowerCase() + "." + inferColumnNameForCustomClassType(signature, field.getName()));   //SOS Country and not country1 in case of declaration Country country1
                    }
                }//if a valid paasword annotation is identified
            }//for
        }//else handle password entity
        return fields;
    }//EoM store

    public static List<String> generateFieldsForManyClasses(List<Class> classes) throws NotAValidPaaSwordEntityException {
        List<String> ret = new ArrayList();
        for (Class clazz : classes) {
            List<String> temp = generateFieldsForClass(clazz);
            ret.addAll(temp);
        }//for
        return ret;
    }//EoM generateFieldsForManyClasses

    public static List<String> generateFieldsForManyClassesWithBcel(List<JavaClass> classes) throws NotAValidPaaSwordEntityException {
        List<String> ret = new ArrayList();
        for (JavaClass clazz : classes) {
            List<String> temp = generateFieldsForClass(clazz);
            ret.addAll(temp);
        }//for
        return ret;
    }//EoM generateFieldsForManyClasses

    //@javax.persistence.Column(nullable=true, precision=0, unique=false, name=, length=200, scale=0, updatable=true, columnDefinition=, table=, insertable=true)
    private static HashMap parseAnnotationFields(String annotationvalue) {
        HashMap map = new HashMap();
        annotationvalue = annotationvalue.substring(annotationvalue.indexOf("(") + 1, annotationvalue.length() - 1).trim();
        logger.info("*" + annotationvalue + "*");
        String[] strs = annotationvalue.split(",");
        for (String str : strs) { //key=value
            String[] keyvalue = str.trim().split("=");
            map.put(keyvalue[0], (keyvalue.length == 2) ? keyvalue[1] : "");
        }
        return map;
    }//EoM

    private static String inferDBTypeForPrimitiveClassType(String attributetype) {
        String ret = "";
//        logger.info("inferring DB type for: <" + attributetype + ">");
        switch (attributetype) {
            case ("String"):
                ret = "char";
                break;
            case ("int"):
                ret = "int";
                break;
            case ("long"):
                ret = "int";
                break;
            case ("double"):
                ret = "double precision";
                break;
            case ("boolean"):
                ret = "char(1)";
                break;
            case ("Date"):
                ret = "Date";
                break;
            default:
//                ret = "unknown";
                ret = "int";

        }
        return ret;
    }//EoM

    /*
    * By convention it is fk_attribute
     */
    private static String inferColumnNameForCustomClassType(String attributetype, String fieldname) {

        logger.info("Attribute Type:" + attributetype + ", Field name: " + fieldname);

        String ret = "fk_" + attributetype.toLowerCase() + "_" + fieldname.toLowerCase();
        return ret;
    }//EoM

    //CREATE TABLE countries (id int primary key, name char(50) not null, inhabitants int not null)
    private static String createTableStatementGenerator(String classname, List<String> fields, List<String> types, List<String> typeconsrtaints, List<Boolean> notnull, List<Boolean> primkeys, List<String> refs) {
        String ret = "CREATE TABLE " + classname.toLowerCase() + "  ";
        String elems = "";
        for (int i = 0; i < fields.size(); i++) {
            //                ------------fieldname---------    -----type----------   ----------- -constraints ----------------------------------------------    -----------------------primary key--------------------   -----------------not null-------------------------------
            String fragment = (fields.get(i).toLowerCase() + " " + types.get(i)) + " " + ((typeconsrtaints.get(i) == null) ? "" : "(" + typeconsrtaints.get(i) + ")") + " " + ((primkeys.get(i) == true) ? "primary key" : "") + " " + (((notnull.get(i) == true) ? "not null" : "") + " " + ((refs.get(i) == null) ? "" : refs.get(i)));
            elems += (i == fields.size() - 1) ? fragment : fragment + ", ";
        }//for attribute names
        ret += " (" + elems + ") ";
        return ret;
    }//EoM

    public static String generateStoreQuery(Object obj) throws NotAValidPaaSwordEntityException {
        String querystr = null;
        Class klazz = obj.getClass();
        String classname = klazz.getSimpleName();

        List<String> fls = new ArrayList();
        List<String> vals = new ArrayList();
        List<String> types = new ArrayList();

        //check that it is a password entity
        Annotation[] classannotations = klazz.getAnnotations();
        boolean ispaaswordentity = false;
        for (Annotation classannotation : classannotations) {
            String annotationcanonicalname = classannotation.annotationType().getCanonicalName();
            //logger.info("<" + annotationcanonicalname + ">");
            if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ENTITY)) {
                ispaaswordentity = true;
                break;
            }
        }//for

        //Start introspection in case of paasword entity
        if (!ispaaswordentity) {
            logger.log(Level.SEVERE, "Exception: Class " + classname + " is not a PaaSword entity");
            throw new NotAValidPaaSwordEntityException("Exception: Class " + classname + " is not a PaaSword entity");
        } else {
//            logger.info("Class " + classname + " is a valid PaaSword class");
            Field[] fields = klazz.getDeclaredFields();
            for (Field field : fields) {
                try {
                    String fieldname = field.getName();
                    //logger.info("Processing field:"+fieldname);
                    Annotation[] fieldannotations = field.getAnnotations();
                    boolean ispaaswordcolumn = false;
                    for (Annotation fieldannotation : fieldannotations) {
                        String annotationcanonicalname = fieldannotation.annotationType().getCanonicalName();
                        //logger.info("<" + annotationcanonicalname + ">");
                        if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_COLUMN)) {
                            ispaaswordcolumn = true;
                            break;
                        }//if
                    }//for    

                    //get name and values
                    if (!ispaaswordcolumn) {
//                        logger.log(Level.WARNING, "Field " + fieldname + " is not a PaaSword Column and it will be ignored");

                        //TODO Is a relation field
                        field.setAccessible(true);
                        fls.add("fk_" + field.getName() + "_" + field.getName());

                        Method methodGetForeignKeyObject = getMethodbyName(klazz, "get" + field.getName());

                        Object objfk = methodGetForeignKeyObject.invoke(obj);

                        Method methodGetForeignKeyId = getMethodbyName(objfk.getClass(), "getId");

                        Long foreignKeyID = (Long) methodGetForeignKeyId.invoke(objfk);

                        vals.add("" + foreignKeyID);

                        types.add(inferDBTypeForPrimitiveClassType("" + field.getType().getSimpleName()));

//                        logger.log(Level.INFO, "{0} -- {1} -- {2}", new Object[]{field.getType().getSimpleName(), field.getName(), field.get(obj)});

                    } else {
                        //make it accessible in order to get a handle in the value
                        field.setAccessible(true);
                        fls.add(field.getName());
                        vals.add("" + field.get(obj));
                        //step 2 - add type
                        types.add(inferDBTypeForPrimitiveClassType("" + field.getType().getSimpleName()));

//                        logger.log(Level.INFO, "{0} -- {1} -- {2}", new Object[]{field.getType().getSimpleName(), field.getName(), field.get(obj)});
                    }// else not a column

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    logger.log(Level.SEVERE, "Exception while introspecting Class " + classname);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }//for      
            if (fls.size() == 0 || vals.size() == 0) {
                logger.log(Level.SEVERE, "Class " + classname + " has no PaaSword columns so no query can be created");
                //throw
            } else {
                querystr = insertStatementGenerator(classname, fls, vals, types);
                logger.info("Query for inserting: " + querystr);
            }
        }//else handle password entity
        return querystr;
    }//EoM store

    public static String generateUpdateQuery(Key key, Object obj) throws NotAValidPaaSwordEntityException {
        String querystr = null;
        Class klazz = obj.getClass();
        String classname = klazz.getSimpleName();

        List<String> fls = new ArrayList();
        List<String> vals = new ArrayList();
        List<String> types = new ArrayList();

        //check that it is a password entity
        Annotation[] classannotations = klazz.getAnnotations();
        boolean ispaaswordentity = false;
        for (Annotation classannotation : classannotations) {
            String annotationcanonicalname = classannotation.annotationType().getCanonicalName();
            //logger.info("<" + annotationcanonicalname + ">");
            if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ENTITY)) {
                ispaaswordentity = true;
                break;
            }
        }//for

        //Start introspection in case of paasword entity
        if (!ispaaswordentity) {
            logger.log(Level.SEVERE, "Exception: Class " + classname + " is not a PaaSword entity");
            throw new NotAValidPaaSwordEntityException("Exception: Class " + classname + " is not a PaaSword entity");
        } else {
//            logger.info("Class " + classname + " is a valid PaaSword class");
            Field[] fields = klazz.getDeclaredFields();
            for (Field field : fields) {
                try {
                    String fieldname = field.getName();
//                    logger.info("Processing field: " + fieldname);
                    Annotation[] fieldannotations = field.getAnnotations();
                    boolean ispaaswordcolumn = false;
                    for (Annotation fieldannotation : fieldannotations) {
                        String annotationcanonicalname = fieldannotation.annotationType().getCanonicalName();
                        //logger.info("<" + annotationcanonicalname + ">");
                        if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_COLUMN)) {
                            ispaaswordcolumn = true;
                            break;
                        }//if
                    }//for

                    //get name and values
                    if (!ispaaswordcolumn) {
//                        logger.log(Level.WARNING, "Field " + fieldname + " is not a PaaSword Column and it will be ignored");

                        //TODO Is a relation field
                        field.setAccessible(true);
                        fls.add("fk_" + field.getName() + "_" + field.getName());

                        Method methodGetForeignKeyObject = getMethodbyName(klazz, "get" + field.getName());

                        Object objfk = methodGetForeignKeyObject.invoke(obj);

                        Key keyForRelation = PaaSwordQueryHandler.getPrimaryKeyForClass(objfk.getClass());

                        Method methodGetForeignKeyId = getMethodbyName(objfk.getClass(), "get" + keyForRelation.getFieldname());

                        Long foreignKeyID = (Long) methodGetForeignKeyId.invoke(objfk);

                        vals.add("" + foreignKeyID);

                        types.add(inferDBTypeForPrimitiveClassType("" + field.getType().getSimpleName()));

//                        logger.log(Level.INFO, "{0} -- {1} -- {2}", new Object[]{field.getType().getSimpleName(), field.getName(), field.get(obj)});

                    } else {
                        //make it accessible in order to get a handle in the value
                        field.setAccessible(true);
                        fls.add(field.getName());
                        vals.add("" + field.get(obj));
                        //step 2 - add type
                        types.add(inferDBTypeForPrimitiveClassType("" + field.getType().getSimpleName()));

//                        logger.log(Level.INFO, "{0} -- {1} -- {2}", new Object[]{field.getType().getSimpleName(), field.getName(), field.get(obj)});
                    }// else not a column

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    logger.log(Level.SEVERE, "Exception while introspecting Class " + classname);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }//for
            if (fls.size() == 0 || vals.size() == 0) {
                logger.log(Level.SEVERE, "Class " + classname + " has no PaaSword columns so no query can be created");
                //throw
            } else {
                querystr = updateStatementGenerator(classname, key, fls, vals, types);
                logger.info("Query for updating: " + querystr);
            }
        }//else handle password entity
        return querystr;
    }//EoM update

    private static String updateStatementGenerator(String classname, Key key, List<String> fields, List<String> values, List<String> types) {
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        SimpleDateFormat secondFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String ret = "update " + classname.toLowerCase() + " set ";
        String elems = "";
        for (int i = 0; i < fields.size(); i++) {
            elems += (i == fields.size() - 1) ? fields.get(i).toLowerCase() : fields.get(i).toLowerCase() + " , ";
        }//for attribute names
//        ret += " (" + elems + ") ";
//        ret += " VALUES ";
        String vals = "";
        for (int i = 0; i < values.size(); i++) {

            if (!fields.get(i).equalsIgnoreCase(key.getFieldname())) {

//            logger.info("type: " + types.get(i));
                boolean ischar = (types.get(i).indexOf("char") != -1);
                // TODO Why lowercase ???
//            String argument = values.get(i).toLowerCase();
                String prefix = fields.get(i) + "=";
                String argument = values.get(i);
                if (ischar) {
                    if (!types.get(i).equalsIgnoreCase("char(1)")) {
                        argument = "'" + argument + "'";
                    }
                }
                //TODO Date
                boolean isDate = (types.get(i).indexOf("Date") != -1);
                if (isDate) {
                    try {
                        argument = "'" + secondFormatter.format(formatter.parse(argument)) + "'";
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                vals += (i == values.size() - 1) ? prefix + argument : prefix + argument + " , ";
            }
        }//for attribute values
        ret += vals + " where " + key.getFieldname() + "=" + key.getFieldValue() + ";";

        logger.info("query: " + ret);
        return ret;
    }//EoM

    //INSERT INTO countries (id, name, inhabitants) VALUES (1, 'country1', 100);
    private static String insertStatementGenerator(String classname, List<String> fields, List<String> values, List<String> types) {
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        SimpleDateFormat secondFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String ret = "INSERT INTO " + classname.toLowerCase() + "  ";
        String elems = "";
        for (int i = 0; i < fields.size(); i++) {
            elems += (i == fields.size() - 1) ? fields.get(i).toLowerCase() : fields.get(i).toLowerCase() + " , ";
        }//for attribute names
        ret += " (" + elems + ") ";
        ret += " VALUES ";
        String vals = "";
        for (int i = 0; i < values.size(); i++) {
            logger.info("type: " + types.get(i));
            boolean ischar = (types.get(i).indexOf("char") != -1);
            // TODO Why lowercase ???
//            String argument = values.get(i).toLowerCase();
            String argument = values.get(i);
            if (ischar) {
                if (!types.get(i).equalsIgnoreCase("char(1)")) {
                    argument = "'" + argument + "'";
                }
            }
            //TODO Date
            boolean isDate = (types.get(i).indexOf("Date") != -1);
            if (isDate) {
                try {
//                    logger.info("Date: " + secondFormatter.format(formatter.parse(argument)));
                    argument = "'" + secondFormatter.format(formatter.parse(argument)) + "'";
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            vals += (i == values.size() - 1) ? argument : argument + " , ";
        }//for attribute values      
        ret += " (" + vals + ") ;";

        logger.info("query: " + ret);
        return ret;
    }//EoM

    public static List<Object> generateCastedObjectsFromInstances(List<Map<String, String>> returnobject, Class clazz) {
        List<Object> retobjects = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        for (Map<String, String> objmap : returnobject) {
//            logger.info("Processing new result");
            try {
                //creating the object
                Object obj = clazz.newInstance();

                for (Map.Entry<String, String> entry : objmap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
//                    logger.info("key:" + key + " value:" + value);
                    //find setter

                    // TODO if object with relation
                    if (key.startsWith("fk")) {
                        key = key.substring(key.lastIndexOf("_") + 1);
//                        logger.info("New key: " + key);
                    }

                    Method method = getMethodbyName(clazz, "set" + key);
                    Class paramtype = method.getParameterTypes()[0];
                    String paramtypestr = paramtype.getSimpleName();        //it will  work with String int long                    
//                    logger.info(method.getName() + " paramtype: " + paramtypestr);

                    if (paramtypestr.equalsIgnoreCase("String")) {
                        method.invoke(obj, value);
                    } else if (paramtypestr.equalsIgnoreCase("int")) {
                        method.invoke(obj, new Integer(value));
                    } else if (paramtypestr.equalsIgnoreCase("long")) {
                        method.invoke(obj, new Long(value));
                    } else if (paramtypestr.equalsIgnoreCase("boolean")) {
                        if (value.equalsIgnoreCase("true")) {
                            method.invoke(obj, new Boolean(true));
                        } else {
                            method.invoke(obj, new Boolean(false));
                        }
                    } else if (paramtypestr.equalsIgnoreCase("Date")) {
                        try {
                            method.invoke(obj, formatter.parse(value));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (paramtypestr.equalsIgnoreCase("double")) {
                        method.invoke(obj, new Double(value));
                    }
                    // TODO Other Primitive Types
                    // TODO Other Objects
                    else {

                        Object fkObject = paramtype.newInstance();
                        Method setIdMethod = getMethodbyName(paramtype, "setId");
                        setIdMethod.invoke(fkObject, new Long(value));
                        method.invoke(obj, fkObject);

                    }


                }//fo
                //add it to the list
                retobjects.add(obj);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(PaaSwordQueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//for each element of the list

        return retobjects;
    }//EoM

    private static Method getMethodbyName(Class clazz, String lowercasename) {
        Method retmethod = null;
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase(lowercasename)) {
                retmethod = method;
                break;
            }
        }//for
        return retmethod;
    }//EoM

    public static Key getPrimaryKeyForClass(Class klazz) throws NotAValidPaaSwordEntityException {
        Key key = null;
        String classname = klazz.getSimpleName();

        List<String> fields = new ArrayList();
        List<String> types = new ArrayList();
        List<String> typeconsrtaints = new ArrayList();
        List<String> refs = new ArrayList();
        List<Boolean> notnulls = new ArrayList();
        List<Boolean> primkeys = new ArrayList();

        //check that it is a password entity
        Annotation[] classannotations = klazz.getAnnotations();
        boolean ispaaswordentity = false;
        for (Annotation classannotation : classannotations) {
            String annotationcanonicalname = classannotation.annotationType().getCanonicalName();
            //logger.info("<" + annotationcanonicalname + ">");
            if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ENTITY)) {
                ispaaswordentity = true;
                break;
            }//if
        }//for

        //Start introspection in case of paasword entity
        if (!ispaaswordentity) {
            //logger.log(Level.SEVERE, "Exception: Class " + classname + " is not a PaaSword entity");
            throw new NotAValidPaaSwordEntityException("Exception: Class " + classname + " is not a PaaSword entity");
        } else {
            //logger.info("!!!!!!!!!!!!!!! Class " + classname + " is a valid PaaSword class");
            Field[] fieldz = klazz.getDeclaredFields();
            for (Field field : fieldz) {
                String fieldname = field.getName();
                //logger.info("@@@@@@@@@@@@@ Examining field: " + fieldname);
                Annotation[] fieldannotations = field.getAnnotations();
                //initialflags
                boolean ispaaswordcolumn = false;
                boolean isid = false;
                boolean notnull = false;
                String constraint = "";
                boolean isprimitive = true;
                for (Annotation attributeannotation : fieldannotations) {
                    String annotationcanonicalname = attributeannotation.annotationType().getCanonicalName();
                    //logger.info("############# Examining fieldannotation: <" + annotationcanonicalname + ">");
                    //switch annotation name
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_COLUMN)) {
                        //set column to true which will affect the overall behavior of the parsing
                        ispaaswordcolumn = true;
                        isprimitive = true;
                        //logger.info("++++++ " + attributeannotation.toString());
                        HashMap<String, String> fieldvalues = parseAnnotationFields(attributeannotation.toString());
                        notnull = fieldvalues.get("nullable").equalsIgnoreCase("false");
                        constraint = fieldvalues.get("length");
                    }//ιf
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_PRIMARYID)) {
                        isid = true;
                    }//if
                    if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_MANYTOONE)) {
                        ispaaswordcolumn = true;
                        isprimitive = false;
                        //logger.info("++++++ " + attributeannotation.toString());
                    }//if                    

                }//for checking all annotations of a field (class attribute)

                //logger.info("$$$$$$$$$$ Field analysis for: <" + fieldname + "> ispaaswordcolumne: " + ispaaswordcolumn + " isid: " + isid + " isprimitive: " + isprimitive);
                //If it is a PaaSword field it has to be processed
                if (!ispaaswordcolumn) {
                    //logger.log(Level.WARNING, "Field " + fieldname + " is not a PaaSword Column and it will be ignored");
                } else {
                    //logger.log(Level.INFO, "^^^^ {0} -- {1} -- {2} ", new Object[]{field.getType().getSimpleName(), field.getName(), field.getType()});
                    //make it accessible in order to get a handle in the value
                    field.setAccessible(true);
                    //step 1 - add field name
                    if (isprimitive) {
                        fields.add(field.getName());
                    } else {
                        fields.add(inferColumnNameForCustomClassType(field.getType().getSimpleName(), field.getName())); //SOS Country and not country1 in case of declaration Country country1
                    }
                    //step 2 - add type
                    if (isprimitive) {
                        types.add(inferDBTypeForPrimitiveClassType("" + field.getType().getSimpleName()));
                    } else {
                        types.add("int");
                    }
                    //step 3 - define if null
                    notnulls.add(notnull);
                    //step 4 - is it a primary key
                    primkeys.add(isid);
                    //step 5 - length constraints
                    if (isprimitive) {
                        typeconsrtaints.add((constraint.equals(PAASWORD_DEFAULTLENGTH)) ? null : constraint);
                    } else {
                        typeconsrtaints.add(null);
                    }
                    //step 6 - references
                    if (isprimitive) {
                        refs.add(null);
                    } else {
                        refs.add(" references " + field.getType().getSimpleName().toLowerCase() + " ");
                    }

                }//if a valid paasword annotation is identified

            }//for      
            if (fields.size() == 0) {
                //logger.log(Level.SEVERE, "Class " + classname + " has no PaaSWord columns so no query can be created");
                //throw
            } else {
                key = getKeyFromAnalyzedClass(fields, types, primkeys);
                //logger.info("Query for creating: " + query);
            }
        }//else handle password entity
        return key;
    }//EoM store        

    private static Key getKeyFromAnalyzedClass(List<String> fields, List<String> types, List<Boolean> primkeys) {
        Key key = null;
        for (int i = 0; i < primkeys.size(); i++) {
            if (primkeys.get(i) == true) {
                key = new Key(fields.get(i), types.get(i));
                break;
            }
        }
        return key;
    }//EoM

    public static String getValueAsStringForFieldname(Object obj, String flname) throws NotAValidPaaSwordEntityException {
        String flvalue = null;
        Class klazz = obj.getClass();
        String classname = klazz.getSimpleName();

        List<String> fls = new ArrayList();
        List<String> vals = new ArrayList();
        List<String> types = new ArrayList();

        //check that it is a password entity
        Annotation[] classannotations = klazz.getAnnotations();
        boolean ispaaswordentity = false;
        for (Annotation classannotation : classannotations) {
            String annotationcanonicalname = classannotation.annotationType().getCanonicalName();
            //logger.info("<" + annotationcanonicalname + ">");
            if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_ENTITY)) {
                ispaaswordentity = true;
                break;
            }
        }//for

        //Start introspection in case of paasword entity
        if (!ispaaswordentity) {
//            logger.log(Level.SEVERE, "Exception: Class " + classname + " is not a PaaSword entity");
            throw new NotAValidPaaSwordEntityException("Exception: Class " + classname + " is not a PaaSword entity");
        } else {
//            logger.info("Class " + classname + " is a valid PaaSword class");
            Field[] fields = klazz.getDeclaredFields();
            for (Field field : fields) {
                try {
                    String fieldname = field.getName();
                    //logger.info("Processing field:"+fieldname);
                    Annotation[] fieldannotations = field.getAnnotations();
                    boolean ispaaswordcolumn = false;
                    for (Annotation fieldannotation : fieldannotations) {
                        String annotationcanonicalname = fieldannotation.annotationType().getCanonicalName();
                        //logger.info("<" + annotationcanonicalname + ">");
                        if (annotationcanonicalname.equalsIgnoreCase(PAASWORD_COLUMN)) {
                            ispaaswordcolumn = true;
                            break;
                        }//if
                    }//for    

                    //get name and values
                    if (!ispaaswordcolumn) {
//                        logger.log(Level.WARNING, "Field " + fieldname + " is not a PaaSword Column and it will be ignored");
                    } else {
                        //make it accessible in order to get a handle in the value
                        field.setAccessible(true);
                        fls.add(field.getName());
                        vals.add("" + field.get(obj));
                        //step 2 - add type
                        types.add(inferDBTypeForPrimitiveClassType("" + field.getType().getSimpleName()));

//                        logger.log(Level.INFO, "{0} -- {1} -- {2}", new Object[]{field.getType().getSimpleName(), field.getName(), field.get(obj)});
                    }// else not a column

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    logger.log(Level.SEVERE, "Exception while introspecting Class " + classname);
                }
            }//for      
            if (fls.size() == 0 || vals.size() == 0) {
                logger.log(Level.SEVERE, "Class " + classname + " has no PaaSword columns so no query can be created");
                //throw
            } else {
                for (int i = 0; i < fls.size(); i++) {
                    if (fls.get(i).equalsIgnoreCase(flname)) {
                        flvalue = vals.get(i);
                    }//if
                }
                logger.info("Fieldvalue for " + flname + ": " + flvalue);
            }
        }//else handle password entity
        return flvalue;
    }//EoM store    

    public static Object invokeMethodAndGetObject(Object obj, String fieldname, String value) {
        try {
            Method method = getMethodbyName(obj.getClass(), "set" + fieldname);
            Class paramtype = method.getParameterTypes()[0];
            String paramtypestr = paramtype.getSimpleName();        //it will  work with String int long                    
            System.out.println(method.getName() + " paramtype: " + paramtypestr);

            if (paramtypestr.equalsIgnoreCase("String")) {
                method.invoke(obj, value);
            } else if (paramtypestr.equalsIgnoreCase("int")) {
                method.invoke(obj, new Integer(value));
            } else if (paramtypestr.equalsIgnoreCase("long")) {
                method.invoke(obj, new Long(value));
            }
        } catch (Exception ex) {
            logger.severe("Exception during the invocation of set" + fieldname);
        }
        return obj;
    }//EoM

}//EoC
