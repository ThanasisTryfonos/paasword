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
package eu.paasword.util;

import eu.paasword.util.entities.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author smantzouratos
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public final class Util {

    private static final Logger logger = Logger.getLogger(Util.class.getName());

    public enum ALGORITHM {
        SHA, MD5
    }

    public static String encode(String s, String key) {
        return base64Encode(xorWithKey(s.getBytes(), key.getBytes()));
    }

    public static String decode(String s, String key) {
        return new String(xorWithKey(base64Decode(s), key.getBytes()));
    }

    private static byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }

    private static byte[] base64Decode(String s) {
        try {
            BASE64Decoder d = new BASE64Decoder();
            return d.decodeBuffer(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String base64Encode(byte[] bytes) {
        BASE64Encoder enc = new BASE64Encoder();
        return enc.encode(bytes).replaceAll("\\s", "");

    }

    /**
     * Read the object from byte array.
     */
    public static Object decodeObject(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write the object to a byte array.
     */
    public static byte[] encodeObject(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return baos.toByteArray();
    }

    public static byte[] encodeClass(Class clazz) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(clazz);
        oos.close();
        return baos.toByteArray();
    }

    public static Class decodeClass(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Class clazz = (Class) ois.readObject();
        ois.close();
        return clazz;
    }


    public static String createAlgorithm(String content, String algorithm) {

        StringBuilder hexString = new StringBuilder();

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hexString.toString();
    }

    /**
     * Read the object from Base64 string.
     *
     * @param s
     * @return
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public static Object deserializeFromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = java.util.Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write the object to a Base64 string.
     *
     * @param o
     * @return
     * @throws java.io.IOException
     */
    public static String serializeToString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static List<PrivacyConstraint> parsePrivacyConstraintsSets(String strJSONObj) {

        List<PrivacyConstraint> listOfPrivacyConstraints = new ArrayList<>();

        PrivacyConstraint pc = new PrivacyConstraint();

        List<String> listOfConstraints = new ArrayList<>();

        JSONArray constraints = new JSONArray(strJSONObj);

        for (Object constraint : constraints) {

            JSONObject newConstraint = (JSONObject) constraint;

            pc.setId(newConstraint.getInt("id"));
            pc.setName(newConstraint.getString("name"));

            JSONArray array = newConstraint.getJSONArray("constraints");

            for (int i = 0; i < array.length(); i++) {

                listOfConstraints.add(array.getString(i));

            }

            pc.setConstraints(listOfConstraints);

            listOfPrivacyConstraints.add(pc);
        }


        return listOfPrivacyConstraints;
    }

    public static List<AnnotatedCode> parseAnnotatedSourceCodeJSONOnlyForPEPs(String strJSONObj) {

        List<AnnotatedCode> listOfAnnotatedCode = new ArrayList<>();
        AnnotatedCode annotatedCode = null;

        boolean findPEP = false;

        if (null != strJSONObj && !strJSONObj.isEmpty()) {

            JSONObject jsonObject = new JSONObject(strJSONObj);

            try {

                JSONArray annotatedCodeJSONArray = jsonObject.getJSONArray("annotatedCode");

                for (int i = 0; i < annotatedCodeJSONArray.length(); i++) {

                    findPEP = false;

                    JSONObject annot = annotatedCodeJSONArray.getJSONObject(i);

                    annotatedCode = new AnnotatedCode();

                    annotatedCode.setId(i + 1);
                    annotatedCode.setName(annot.getString("name"));
                    annotatedCode.setType(annot.getString("type"));

                    if (!annot.getString("category").equalsIgnoreCase("PEP")) {
                        continue;
                    }

                    // Check for Methods
                    if (null != annot.getJSONArray("methods") && annot.getJSONArray("methods").length() > 0) {

                        List<AnnotatedMethod> listOfAnnotatedMethods = new ArrayList<>();

                        for (int j = 0; j < annot.getJSONArray("methods").length(); j++) {

                            JSONObject method = annot.getJSONArray("methods").getJSONObject(j);

                            AnnotatedMethod annotMethod = new AnnotatedMethod();
                            annotMethod.setId(j + 1);
                            annotMethod.setName(method.getString("name"));

                            if (null != method.getJSONArray("annotations") && method.getJSONArray("annotations").length() > 0 && method.getJSONArray("annotations").toString().contains("PaaSwordPEP")) {

                                findPEP = true;

                                List<AnnotatedAnnotation> listOfPaaSwordAnnotations = new ArrayList<>();

                                for (int k = 0; k < method.getJSONArray("annotations").length(); k++) {

                                    JSONObject annotJSON = method.getJSONArray("annotations").getJSONObject(k);

                                    AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                                    paaSwordAnnotation.setId(k + 1);
                                    paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));

                                    if (annotJSON.get("value") instanceof JSONArray) {

                                        String annotArray = annotJSON.getJSONArray("value").toString();

                                        paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));

                                    } else {
                                        paaSwordAnnotation.setValue(annotJSON.getString("value"));
                                    }

                                    paaSwordAnnotation.setType(annotJSON.getString("type"));

                                    listOfPaaSwordAnnotations.add(paaSwordAnnotation);

                                }

                                annotMethod.setMethodAnnotations(listOfPaaSwordAnnotations);

                            }

                            if (null != method.getJSONArray("parameters") && method.getJSONArray("parameters").length() > 0) {

                                List<AnnotatedParameter> listOfParameters = new ArrayList<>();

                                for (int m = 0; m < method.getJSONArray("parameters").length(); m++) {

                                    JSONObject paramJSON = method.getJSONArray("parameters").getJSONObject(m);

                                    AnnotatedParameter annotatedParameter = new AnnotatedParameter();
                                    annotatedParameter.setId(m + 1);
                                    annotatedParameter.setName(paramJSON.getString("name"));
                                    annotatedParameter.setType(paramJSON.getString("type"));

                                    listOfParameters.add(annotatedParameter);

                                }

                                annotMethod.setParameters(listOfParameters);

                            }

                            listOfAnnotatedMethods.add(annotMethod);

                        }

                        annotatedCode.setMethods(listOfAnnotatedMethods);

                    }

                    // Check for Annotations
                    if (null != annot.getJSONArray("annotations") && annot.getJSONArray("annotations").length() > 0 && annot.getJSONArray("annotations").toString().contains("PaaSwordPEP")) {

                        findPEP = true;

                        List<AnnotatedAnnotation> listOfAnnotations = new ArrayList<>();

                        for (int n = 0; n < annot.getJSONArray("annotations").length(); n++) {

                            JSONObject annotJSON = annot.getJSONArray("annotations").getJSONObject(n);

                            AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                            paaSwordAnnotation.setId(n + 1);
                            paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));

                            if (annotJSON.get("value") instanceof JSONArray) {
                                String annotArray = annotJSON.getJSONArray("value").toString();

                                paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));
                            } else {
                                paaSwordAnnotation.setValue(annotJSON.getString("value"));
                            }

                            paaSwordAnnotation.setType(annotJSON.getString("type"));

                            listOfAnnotations.add(paaSwordAnnotation);


                        }

                        annotatedCode.setAnnotations(listOfAnnotations);

                    }

//                    // Check for Fields
//                    if (null != annot.getJSONArray("fields") && annot.getJSONArray("fields").length() > 0 && annot.getJSONArray("fields").toString().contains("PaaSwordPEP")) {
//
//                        findPEP = true;
//
//                        List<AnnotatedField> listOfAnnotatedFields = new ArrayList<>();
//
//                        for (int o = 0; o < annot.getJSONArray("fields").length(); o++) {
//
//                            JSONObject field = annot.getJSONArray("fields").getJSONObject(o);
//
//                            AnnotatedField annotField = new AnnotatedField();
//                            annotField.setId(o + 1);
//                            annotField.setName(field.getString("name"));
//                            annotField.setType(field.getString("type"));
//
//                            List<AnnotatedAnnotation> listOfAnnotations = new ArrayList<>();
//
//                            for (int p = 0; p < field.getJSONArray("annotations").length(); p++) {
//
//                                JSONObject annotJSON = field.getJSONArray("annotations").getJSONObject(p);
//
//
//                                    AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
//                                    paaSwordAnnotation.setId(p + 1);
//                                    paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));
//
//                                    if (annotJSON.get("value") instanceof JSONArray) {
//                                        String annotArray = annotJSON.getJSONArray("value").toString();
//
//                                        paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));
//                                    } else {
//                                        paaSwordAnnotation.setValue(annotJSON.getString("value"));
//                                    }
//
//                                    listOfAnnotations.add(paaSwordAnnotation);
//
//
//                            }
//
//                            annotField.setFieldAnnotations(listOfAnnotations);
//
//                            listOfAnnotatedFields.add(annotField);
//
//                        }
//
//                        annotatedCode.setFields(listOfAnnotatedFields);
//
//                    }


                    if (findPEP) {
                        listOfAnnotatedCode.add(annotatedCode);
                    }

                }

            } catch (JSONException e) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        return listOfAnnotatedCode;
    }

    public static List<AnnotatedCode> parseAnnotatedSourceCodeJSONOnlyForDataModels(String strJSONObj) {

        List<AnnotatedCode> listOfAnnotatedCode = new ArrayList<>();
        AnnotatedCode annotatedCode = null;

        boolean findEntity = false;

        if (null != strJSONObj && !strJSONObj.isEmpty()) {

            JSONObject jsonObject = new JSONObject(strJSONObj);

            try {

                JSONArray annotatedCodeJSONArray = jsonObject.getJSONArray("annotatedCode");

                for (int i = 0; i < annotatedCodeJSONArray.length(); i++) {

                    findEntity = false;

                    JSONObject annot = annotatedCodeJSONArray.getJSONObject(i);

                    annotatedCode = new AnnotatedCode();

                    annotatedCode.setId(i + 1);
                    annotatedCode.setName(annot.getString("name"));
                    annotatedCode.setType(annot.getString("type"));

                    if (!annot.getString("category").equalsIgnoreCase("DataModel")) {
                        continue;
                    }

//                    // Check for Methods
//                    if (null != annot.getJSONArray("methods") && annot.getJSONArray("methods").length() > 0) {
//
//                        List<AnnotatedMethod> listOfAnnotatedMethods = new ArrayList<>();
//
//                        for (int j = 0; j < annot.getJSONArray("methods").length(); j++) {
//
//                            JSONObject method = annot.getJSONArray("methods").getJSONObject(j);
//
//                            AnnotatedMethod annotMethod = new AnnotatedMethod();
//                            annotMethod.setId(j + 1);
//                            annotMethod.setName(method.getString("name"));
//
//                            if (null != method.getJSONArray("annotations") && method.getJSONArray("annotations").length() > 0 && method.getJSONArray("annotations").toString().contains("PaaSwordEntity")) {
//
//                                findEntity = true;
//
//                                List<AnnotatedAnnotation> listOfPaaSwordAnnotations = new ArrayList<>();
//
//                                for (int k = 0; k < method.getJSONArray("annotations").length(); k++) {
//
//                                    JSONObject annotJSON = method.getJSONArray("annotations").getJSONObject(k);
//
//                                    AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
//                                    paaSwordAnnotation.setId(k + 1);
//                                    paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));
//
//                                    if (annotJSON.get("value") instanceof JSONArray) {
//
//                                        String annotArray = annotJSON.getJSONArray("value").toString();
//
//                                        paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));
//
//                                    } else {
//                                        paaSwordAnnotation.setValue(annotJSON.getString("value"));
//                                    }
//
//                                    listOfPaaSwordAnnotations.add(paaSwordAnnotation);
//
//                                }
//
//                                annotMethod.setMethodAnnotations(listOfPaaSwordAnnotations);
//
//                            }
//
//                            if (null != method.getJSONArray("parameters") && method.getJSONArray("parameters").length() > 0) {
//
//                                List<AnnotatedParameter> listOfParameters = new ArrayList<>();
//
//                                for (int m = 0; m < method.getJSONArray("parameters").length(); m++) {
//
//                                    JSONObject paramJSON = method.getJSONArray("parameters").getJSONObject(m);
//
//                                    AnnotatedParameter annotatedParameter = new AnnotatedParameter();
//                                    annotatedParameter.setId(m + 1);
//                                    annotatedParameter.setName(paramJSON.getString("name"));
//                                    annotatedParameter.setType(paramJSON.getString("type"));
//
//                                    listOfParameters.add(annotatedParameter);
//
//                                }
//
//                                annotMethod.setParameters(listOfParameters);
//
//                            }
//
//                            listOfAnnotatedMethods.add(annotMethod);
//
//                        }
//
//                        annotatedCode.setMethods(listOfAnnotatedMethods);
//
//                    }

                    // Check for Annotations
                    if (null != annot.getJSONArray("annotations") && annot.getJSONArray("annotations").length() > 0 && annot.getJSONArray("annotations").toString().contains("PaaSwordEntity")) {

                        findEntity = true;

                        List<AnnotatedAnnotation> listOfAnnotations = new ArrayList<>();

                        for (int n = 0; n < annot.getJSONArray("annotations").length(); n++) {

                            JSONObject annotJSON = annot.getJSONArray("annotations").getJSONObject(n);

                            AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                            paaSwordAnnotation.setId(n + 1);
                            paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));

                            if (annotJSON.get("value") instanceof JSONArray) {
                                String annotArray = annotJSON.getJSONArray("value").toString();

                                paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));
                            } else {
                                paaSwordAnnotation.setValue(annotJSON.getString("value"));
                            }

                            listOfAnnotations.add(paaSwordAnnotation);


                        }

                        annotatedCode.setAnnotations(listOfAnnotations);

                    }

                    // Check for Fields
                    if (null != annot.getJSONArray("fields") && annot.getJSONArray("fields").length() > 0) {

                        findEntity = true;

                        List<AnnotatedField> listOfAnnotatedFields = new ArrayList<>();

                        for (int o = 0; o < annot.getJSONArray("fields").length(); o++) {

                            JSONObject field = annot.getJSONArray("fields").getJSONObject(o);


                            if (field.getString("name").equalsIgnoreCase("serialVersionUID")) {
                                continue;
                            }

                            AnnotatedField annotField = new AnnotatedField();
                            annotField.setId(o + 1);
                            annotField.setName(field.getString("name"));
                            annotField.setType(field.getString("type"));

                            List<AnnotatedAnnotation> listOfAnnotations = new ArrayList<>();

//                            for (int p = 0; p < field.getJSONArray("annotations").length(); p++) {
//
//                                JSONObject annotJSON = field.getJSONArray("annotations").getJSONObject(p);
//
//                                AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
//                                paaSwordAnnotation.setId(p + 1);
//                                paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));
//
//                                if (annotJSON.get("value") instanceof JSONArray) {
//                                    String annotArray = annotJSON.getJSONArray("value").toString();
//
//                                    paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));
//                                } else {
//                                    paaSwordAnnotation.setValue(annotJSON.getString("value"));
//                                }
//
//                                listOfAnnotations.add(paaSwordAnnotation);
//
//
//                            }

                            annotField.setFieldAnnotations(listOfAnnotations);

                            listOfAnnotatedFields.add(annotField);

                        }

                        annotatedCode.setFields(listOfAnnotatedFields);

                    }


                    if (findEntity) {
                        listOfAnnotatedCode.add(annotatedCode);
                    }

                }

            } catch (JSONException e) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        return listOfAnnotatedCode;
    }

    public static JSONArray parseAnnotatedSourceCodeJSONForMultiplePEPs(String strJSONObj) {

        JSONArray array = new JSONArray();
        AnnotatedCode annotatedCode = null;

        if (null != strJSONObj && !strJSONObj.isEmpty()) {

            JSONObject jsonObject = new JSONObject(strJSONObj);

            try {

                JSONArray annotatedCodeJSONArray = jsonObject.getJSONArray("annotatedCode");

                for (int i = 0; i < annotatedCodeJSONArray.length(); i++) {

                    JSONObject annot = annotatedCodeJSONArray.getJSONObject(i);

                    annotatedCode = new AnnotatedCode();

                    annotatedCode.setId(i + 1);
                    annotatedCode.setName(annot.getString("name"));
                    annotatedCode.setType(annot.getString("type"));

                    // Check for Methods
                    if (null != annot.getJSONArray("methods") && annot.getJSONArray("methods").length() > 0) {

                        List<AnnotatedMethod> listOfAnnotatedMethods = new ArrayList<>();
                        for (int j = 0; j < annot.getJSONArray("methods").length(); j++) {

                            JSONObject method = annot.getJSONArray("methods").getJSONObject(j);

                            AnnotatedMethod annotMethod = new AnnotatedMethod();
                            annotMethod.setId(j + 1);
                            annotMethod.setName(method.getString("name"));

                            if (null != method.getJSONArray("annotations") && method.getJSONArray("annotations").length() > 0) {

                                List<AnnotatedAnnotation> listOfPaaSwordAnnotations = new ArrayList<>();

                                for (int k = 0; k < method.getJSONArray("annotations").length(); k++) {

                                    JSONObject annotJSON = method.getJSONArray("annotations").getJSONObject(k);

                                    AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                                    paaSwordAnnotation.setId(k + 1);
                                    paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));

                                    if (paaSwordAnnotation.getName().equalsIgnoreCase("PaaSwordPEP")) {

                                        JSONArray jsonPolicies = annotJSON.getJSONArray("value");

                                        array.put(jsonPolicies);

                                    }

                                    listOfPaaSwordAnnotations.add(paaSwordAnnotation);

                                }

                                annotMethod.setMethodAnnotations(listOfPaaSwordAnnotations);

                            }

                            if (null != method.getJSONArray("parameters") && method.getJSONArray("parameters").length() > 0) {

                                List<AnnotatedParameter> listOfParameters = new ArrayList<>();

                                for (int m = 0; m < method.getJSONArray("parameters").length(); m++) {

                                    JSONObject paramJSON = method.getJSONArray("parameters").getJSONObject(m);

                                    AnnotatedParameter annotatedParameter = new AnnotatedParameter();
                                    annotatedParameter.setId(m + 1);
                                    annotatedParameter.setName(paramJSON.getString("name"));
                                    annotatedParameter.setType(paramJSON.getString("type"));

                                    listOfParameters.add(annotatedParameter);

                                }

                                annotMethod.setParameters(listOfParameters);

                            }

                            listOfAnnotatedMethods.add(annotMethod);

                        }

                        annotatedCode.setMethods(listOfAnnotatedMethods);

                    }

                    // Check for Annotations
                    if (null != annot.getJSONArray("annotations") && annot.getJSONArray("annotations").length() > 0) {

                        List<AnnotatedAnnotation> listOfAnnotations = new ArrayList<>();

                        for (int n = 0; n < annot.getJSONArray("annotations").length(); n++) {

                            JSONObject annotJSON = annot.getJSONArray("annotations").getJSONObject(n);

                            AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                            paaSwordAnnotation.setId(n + 1);
                            paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));

                            if (paaSwordAnnotation.getName().equalsIgnoreCase("PaaSwordPEP")) {

                                JSONArray jsonPolicies = annotJSON.getJSONArray("value");

                                array.put(jsonPolicies);

                            }

                            listOfAnnotations.add(paaSwordAnnotation);

                        }

                        annotatedCode.setAnnotations(listOfAnnotations);

                    }

//                    // Check for Fields
//                    if (null != annot.getJSONArray("fields") && annot.getJSONArray("fields").length() > 0) {
//
//                        List<AnnotatedField> listOfAnnotatedFields = new ArrayList<>();
//
//                        for (int o = 0; o < annot.getJSONArray("fields").length(); o++) {
//
//                            JSONObject field = annot.getJSONArray("fields").getJSONObject(o);
//
//                            AnnotatedField annotField = new AnnotatedField();
//                            annotField.setId(o + 1);
//                            annotField.setName(field.getString("name"));
//                            annotField.setType(field.getString("type"));
//
//                            List<AnnotatedAnnotation> listOfAnnotations = new ArrayList<>();
//
//                            for (int p = 0; p < field.getJSONArray("annotations").length(); p++) {
//
//                                JSONObject annotJSON = field.getJSONArray("annotations").getJSONObject(p);
//
//                                AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
//                                paaSwordAnnotation.setId(p + 1);
//                                paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));
//
//                                if (paaSwordAnnotation.getName().equalsIgnoreCase("PaaSwordPEP")) {
//
//                                    JSONArray jsonPolicies = annotJSON.getJSONArray("value");
//
//                                    array.put(jsonPolicies);
//
//                                }
//
//                                listOfAnnotations.add(paaSwordAnnotation);
//
//                            }
//
//                            annotField.setFieldAnnotations(listOfAnnotations);
//
//                            listOfAnnotatedFields.add(annotField);
//
//                        }
//
//                        annotatedCode.setFields(listOfAnnotatedFields);
//
//                    }

//                    listOfAnnotatedCode.add(annotatedCode);
                }

            } catch (JSONException e) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        return array;
    }


    @Deprecated
    public static List<String> parseAnnotatedSourceCodeJSONForPEPs(String strJSONObj) {

        //TODO check duplications

        List<String> listOfExistingPolicies = new ArrayList<>();
        AnnotatedCode annotatedCode = null;

        if (null != strJSONObj && !strJSONObj.isEmpty()) {

            JSONObject jsonObject = new JSONObject(strJSONObj);

            try {

                JSONArray annotatedCodeJSONArray = jsonObject.getJSONArray("annotatedCode");

                for (int i = 0; i < annotatedCodeJSONArray.length(); i++) {

                    JSONObject annot = annotatedCodeJSONArray.getJSONObject(i);

                    annotatedCode = new AnnotatedCode();

                    annotatedCode.setId(i + 1);
                    annotatedCode.setName(annot.getString("name"));
                    annotatedCode.setType(annot.getString("type"));

                    // Check for Methods
                    if (null != annot.getJSONArray("methods") && annot.getJSONArray("methods").length() > 0) {

                        List<AnnotatedMethod> listOfAnnotatedMethods = new ArrayList<>();
                        for (int j = 0; j < annot.getJSONArray("methods").length(); j++) {

                            JSONObject method = annot.getJSONArray("methods").getJSONObject(j);

                            AnnotatedMethod annotMethod = new AnnotatedMethod();
                            annotMethod.setId(j + 1);
                            annotMethod.setName(method.getString("name"));

                            if (null != method.getJSONArray("annotations") && method.getJSONArray("annotations").length() > 0) {

                                List<AnnotatedAnnotation> listOfPaaSwordAnnotations = new ArrayList<>();

                                for (int k = 0; k < method.getJSONArray("annotations").length(); k++) {

                                    JSONObject annotJSON = method.getJSONArray("annotations").getJSONObject(k);

                                    AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                                    paaSwordAnnotation.setId(k + 1);
                                    paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));

                                    if (annotJSON.get("value") instanceof JSONArray) {
                                        String annotArray = annotJSON.getJSONArray("value").toString();

                                        paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));
                                    } else {
                                        paaSwordAnnotation.setValue(annotJSON.getString("value"));
                                    }

                                    if (paaSwordAnnotation.getName().equalsIgnoreCase("PaaSwordPEP")) {

                                        String value = paaSwordAnnotation.getValue();

                                        if (value.contains(",")) {
                                            String policies[] = value.split("\\,");

                                            for (String policy : policies) {
                                                listOfExistingPolicies.add(policy);
                                            }

                                        } else {
                                            listOfExistingPolicies.add(value);
                                        }


                                    }

                                    listOfPaaSwordAnnotations.add(paaSwordAnnotation);

                                }

                                annotMethod.setMethodAnnotations(listOfPaaSwordAnnotations);

                            }

                            if (null != method.getJSONArray("parameters") && method.getJSONArray("parameters").length() > 0) {

                                List<AnnotatedParameter> listOfParameters = new ArrayList<>();

                                for (int m = 0; m < method.getJSONArray("parameters").length(); m++) {

                                    JSONObject paramJSON = method.getJSONArray("parameters").getJSONObject(m);

                                    AnnotatedParameter annotatedParameter = new AnnotatedParameter();
                                    annotatedParameter.setId(m + 1);
                                    annotatedParameter.setName(paramJSON.getString("name"));
                                    annotatedParameter.setType(paramJSON.getString("type"));

                                    listOfParameters.add(annotatedParameter);

                                }

                                annotMethod.setParameters(listOfParameters);

                            }

                            listOfAnnotatedMethods.add(annotMethod);

                        }

                        annotatedCode.setMethods(listOfAnnotatedMethods);

                    }

                    // Check for Annotations
                    if (null != annot.getJSONArray("annotations") && annot.getJSONArray("annotations").length() > 0) {

                        List<AnnotatedAnnotation> listOfAnnotations = new ArrayList<>();

                        for (int n = 0; n < annot.getJSONArray("annotations").length(); n++) {

                            JSONObject annotJSON = annot.getJSONArray("annotations").getJSONObject(n);

                            AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                            paaSwordAnnotation.setId(n + 1);
                            paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));
                            if (annotJSON.get("value") instanceof JSONArray) {
                                String annotArray = annotJSON.getJSONArray("value").toString();

                                paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));
                            } else {
                                paaSwordAnnotation.setValue(annotJSON.getString("value"));
                            }

                            if (paaSwordAnnotation.getName().equalsIgnoreCase("PaaSwordPEP")) {

                                String value = paaSwordAnnotation.getValue();

                                if (value.contains(",")) {
                                    String policies[] = value.split("\\,");

                                    for (String policy : policies) {
                                        listOfExistingPolicies.add(policy);
                                    }

                                } else {
                                    listOfExistingPolicies.add(value);
                                }

                            }

                            listOfAnnotations.add(paaSwordAnnotation);

                        }

                        annotatedCode.setAnnotations(listOfAnnotations);

                    }

                    // Check for Fields
                    if (null != annot.getJSONArray("fields") && annot.getJSONArray("fields").length() > 0) {

                        List<AnnotatedField> listOfAnnotatedFields = new ArrayList<>();

                        for (int o = 0; o < annot.getJSONArray("fields").length(); o++) {

                            JSONObject field = annot.getJSONArray("fields").getJSONObject(o);

                            AnnotatedField annotField = new AnnotatedField();
                            annotField.setId(o + 1);
                            annotField.setName(field.getString("name"));
                            annotField.setType(field.getString("type"));

                            List<AnnotatedAnnotation> listOfAnnotations = new ArrayList<>();

                            for (int p = 0; p < field.getJSONArray("annotations").length(); p++) {

                                JSONObject annotJSON = field.getJSONArray("annotations").getJSONObject(p);

                                AnnotatedAnnotation paaSwordAnnotation = new AnnotatedAnnotation();
                                paaSwordAnnotation.setId(p + 1);
                                paaSwordAnnotation.setName(annotJSON.getString("name").substring(annotJSON.getString("name").lastIndexOf(".") + 1));
                                if (annotJSON.get("value") instanceof JSONArray) {
                                    String annotArray = annotJSON.getJSONArray("value").toString();

                                    paaSwordAnnotation.setValue(annotArray.substring(1, annotArray.length() - 1));
                                } else {
                                    paaSwordAnnotation.setValue(annotJSON.getString("value"));
                                }

                                if (paaSwordAnnotation.getName().equalsIgnoreCase("PaaSwordPEP")) {
                                    String value = paaSwordAnnotation.getValue();

                                    if (value.contains(",")) {
                                        String policies[] = value.split("\\,");

                                        for (String policy : policies) {
                                            listOfExistingPolicies.add(policy);
                                        }

                                    } else {
                                        listOfExistingPolicies.add(value);
                                    }
                                }

                                listOfAnnotations.add(paaSwordAnnotation);

                            }

                            annotField.setFieldAnnotations(listOfAnnotations);

                            listOfAnnotatedFields.add(annotField);

                        }

                        annotatedCode.setFields(listOfAnnotatedFields);

                    }

//                    listOfAnnotatedCode.add(annotatedCode);
                }

            } catch (JSONException e) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        return listOfExistingPolicies;
    }


    public static String transformDBProxyConfFileToXML(String jsonConfFile) {

        // Initialize XML Document
        Document dbProxyConfigDoc = XMLUtil.initializeXMLDocument();

        if (null == dbProxyConfigDoc) {
            return null;
        }

        // Root Element with name "add"
        Element rootElement = XMLUtil.rootElementADD(dbProxyConfigDoc);

        List<XMLAttribute> listOfAttributes = new ArrayList<>();

//        listOfAttributes.add(new XMLAttribute("commitWithin", SystemProperties.INSTANCE.getSystemProperty("solr_commitWithin")));
        listOfAttributes.add(new XMLAttribute("overwrite", "true"));

        XMLUtil.addListOfAttributesToElement(dbProxyConfigDoc, listOfAttributes, rootElement);

        // Create Element with name "doc"
        Element docField = XMLUtil.createElement(dbProxyConfigDoc, "doc", null);

        // Add all data of SES_DOCUMENT to element "doc"
//        addDataToXMLElements(solrDoc, docField, _sesDocument, _listOfDefaultFields, _listOfEntities, _listOfMetadata, _listOfExtractedMetadata, _listOfFields);

        XMLUtil.addChildToElement(docField, rootElement);

        if (listOfAttributes.size() > 0) {
            listOfAttributes.clear();
            listOfAttributes = null;
        }

        // Returns XML Document
//        return dbProxyConfigDoc;


        return null;
    }

    public static String sha256hash(String str) {
        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes("UTF-8"));
            byte[] digest = md.digest();
            hash = String.format("%064x", new java.math.BigInteger(1, digest)).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Not a valid Hash Algorithm for string: " + str);
        } catch (UnsupportedEncodingException e) {
            logger.severe("Not a valid Encoding for string: " + str);
        }
        return hash;
    }

    public static enum Mode {

        ALPHA, ALPHANUMERIC, NUMERIC, SYMBOL
    }

    /**
     *
     * Generates a new Random String for Password
     *
     * @param length
     * @param mode
     *
     * @return A String object
     *
     */
    public static String generateRandomString(int length, Mode mode) {

        StringBuffer buffer = new StringBuffer();
        String characters = "";

        switch (mode) {

            case ALPHA:
                characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                break;

            case ALPHANUMERIC:
                characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$";
                break;

            case NUMERIC:
                characters = "1234567890";
                break;

        }

        int charactersLength = characters.length();

        for (int i = 0; i < length; i++) {
            double index = Math.random() * charactersLength;
            buffer.append(characters.charAt((int) index));
        }
        return buffer.toString();
    } // EoM generateRandomPassword


}
