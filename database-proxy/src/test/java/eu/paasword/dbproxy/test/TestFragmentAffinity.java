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
package eu.paasword.dbproxy.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.paasword.dbproxy.fragmentation.FragmentAffinity;

/**
 * A main method to showcase the functionality of the FragmetnAffinity class
 *
 * @author Tobias Andersson
 *
 */
public class TestFragmentAffinity {

    private static ArrayList<String> attributes = new ArrayList<>();
    private static ArrayList<String> attributesCopy = new ArrayList<>();
    private static ArrayList<ArrayList<String>> relations = new ArrayList<>();
    private static Table<String, String, Integer> affinityMapping = HashBasedTable.create();

    public static void main(String[] args) {

        // test case 1 normal set up.
        refillInput();
        attributesCopy = deepCopy(attributes);
        clearAffinity();

        System.out.println("test case 1, normal set up");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        printConstraints();

        FragmentAffinity frags = new FragmentAffinity(attributes, relations, affinityMapping);

        String res = frags.fragment();
        System.out.println("The fragmentation");
        System.out.println(res);

        // test case 2 case 1 but with different affinity set up.
        refillInput();
        clearAffinity();

        System.out.println("test case 2, case 1 but with different affinity set up");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);
        affinityMapping.put("postgraduate", "faculty", 40);

        printAffinity();
        printConstraints();

        FragmentAffinity frags2 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res2 = frags2.fragment();
        System.out.println("The fragmentation");
        System.out.println(res2);

        // test case 3 Singleton constraint.
        refillInputSingle();
        clearAffinity();

        System.out.println("test case 3, singleton constraint case");
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);
        affinityMapping.put("postgraduate", "faculty", 40);

        printAffinity();
        printConstraints();

        FragmentAffinity frags3 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res3 = frags3.fragment();
        System.out.println("The fragmentation");
        System.out.println(res3);

        // test case 4 constraint and affinity conflict.
        refillInput();
        clearAffinity();

        System.out.println("test case 4, constraint and affinity conflict");
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);
        affinityMapping.put("name", "grade", 40);

        printAffinity();
        printConstraints();

        FragmentAffinity frags4 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res4 = frags4.fragment();
        System.out.println("The fragmentation");
        System.out.println(res4);

        // test case 5 constraints in reversed order.
        refillInputWrongOrder();
        clearAffinity();

        System.out.println("test case 5, constraints in reversed order. Fragmentation should look like test case 1");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        printConstraints();

        FragmentAffinity frags5 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res5 = frags5.fragment();
        System.out.println("The fragmentation");
        System.out.println(res5);

        // test case 6 constraints having attributes which doesn't exist.
        refillInputNoneExistentAttribute();
        clearAffinity();

        System.out.println(
                "test case 6, constraints having attributes which doesn't exist. The constraint should be ignored and the fragmentation should look like test case 5.");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        printConstraints();

        FragmentAffinity frags6 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res6 = frags6.fragment();
        System.out.println("The fragmentation");
        System.out.println(res6);

        // test case 7 constraint list is null.
        refillInputConNull();
        clearAffinity();

        System.out.println("test case 7, constraint list is null.");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        FragmentAffinity frags7 = null;
        try {
            frags7 = new FragmentAffinity(attributes, relations, affinityMapping);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("An exception was thrown as expected.");
            System.out.println();
        }

        // test case 8 attribute list is null.
        refillInputAttNull();
        clearAffinity();

        System.out.println("test case 8, attribute list is null.");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();

        FragmentAffinity frags8 = null;
        try {
            frags8 = new FragmentAffinity(attributes, relations, affinityMapping);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("An exception was thrown as expected.");
            System.out.println();
        }

        // test case 9 affinity matrix is null.
        refillInput();
        clearAffinity();

        System.out.println("test case 9, affinity matrix is null.");
        affinityMapping = null;

        printConstraints();

        FragmentAffinity frags9 = null;
        try {
            frags9 = new FragmentAffinity(attributes, relations, affinityMapping);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("An exception was thrown as expected.");
            System.out.println();
        }
        affinityMapping = HashBasedTable.create();

        // test case 10 constraint list is empty.
        refillInput();
        relations = new ArrayList<ArrayList<String>>();
        clearAffinity();

        System.out.println("test case 10, constraint list is empty.");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        FragmentAffinity frags10 = null;
        try {
            frags10 = new FragmentAffinity(attributes, relations, affinityMapping);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("An exception was thrown as expected.");
            System.out.println();
        }

        // test case 11 attribute list is empty.
        refillInput();
        attributes = new ArrayList<String>();
        clearAffinity();

        System.out.println("test case 11, attribute list is empty.");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        FragmentAffinity frags11 = null;
        try {
            frags11 = new FragmentAffinity(attributes, relations, affinityMapping);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("An exception was thrown as expected.");
            System.out.println();
        }

        // test case 12 affinity matrix list is empty.
        refillInput();
        affinityMapping = HashBasedTable.create();

        System.out.println("test case 12, attribute list is empty.");

        FragmentAffinity frags12 = null;
        try {
            frags12 = new FragmentAffinity(attributes, relations, affinityMapping);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("An exception was thrown as expected.");
            System.out.println();
        }

        // test case 13 inner array list of constraints is null.
        refillInput();
        relations.clear();
        ArrayList<String> tmp = null;
        relations.add(tmp);
        clearAffinity();

        System.out.println("test case 13, inner array list of constraints is null.");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        FragmentAffinity frags13;
        try {
            frags13 = new FragmentAffinity(attributes, relations, affinityMapping);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("An exception was thrown as expected.");
            System.out.println();
        }

        // test case 14 inner array list of constraints is empty.
        refillInput();
        relations.clear();
        tmp = new ArrayList<String>();
        relations.add(tmp);
        clearAffinity();

        System.out.println("test case 14, inner array list of constraints is empty.");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();

        FragmentAffinity frags14;

        try {
            frags14 = new FragmentAffinity(attributes, relations, affinityMapping);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("An exception was thrown as expected.");
            System.out.println();
        }


        // test case 15 duplicate attributes in a_toPlace.
        refillInput();
        attributes.add("name");
        clearAffinity();

        System.out.println("test case 15, duplicate attributes in a_toPlace.");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        printConstraints();

        FragmentAffinity frags15 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res15 = frags15.fragment();
        System.out.println("The fragmentation");
        System.out.println(res15);


        // test case 16 privacy constraint with the same attribute, e.g. "name - name".
        refillInput();
        ArrayList<String> c8 = new ArrayList<String>();
        c8.add("university");
        c8.add("university");
        relations.add(0,c8);
        clearAffinity();

        System.out.println("test case 16, privacy constraint with the same attribute, e.g. \"name - name\".");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        printConstraints();

        FragmentAffinity frags16 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res16 = frags16.fragment();
        System.out.println("The fragmentation");
        System.out.println(res16);


        // test case 17 duplicate privacy constraints.
        refillInput();
        c8 = new ArrayList<String>();
        c8.add("name");
        c8.add("surname");
        relations.add(0,c8);
        clearAffinity();

        System.out.println("test case 17, duplicate privacy constraints.");
        affinityMapping.put("name", "faculty", 10);
        affinityMapping.put("name", "gender", 6);
        affinityMapping.put("name", "university", 30);
        affinityMapping.put("university", "gender", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        printConstraints();

        FragmentAffinity frags17 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res17 = frags17.fragment();
        System.out.println("The fragmentation");
        System.out.println(res17);


        // test case 18 privacy constraints with 4+ attributes.
        refillInput();
        c8 = new ArrayList<String>();
        c8.add("gender");
        c8.add("name");
        c8.add("university");
        c8.add("faculty");
        c8.add("semester");
        relations.remove(2);
        relations.remove(relations.size()-1);
        relations.add(c8);
        clearAffinity();

        System.out.println("test case 18, privacy constraints with 4+ attributes.");
        affinityMapping.put("surname", "semester", 10);
        affinityMapping.put("surname", "faculty", 10);

        printAffinity();
        printConstraints();

        FragmentAffinity frags18 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res18 = frags18.fragment();
        System.out.println("The fragmentation");
        System.out.println(res18);


        // test case 19, example from said paper.
        refillInput();
        attributes.remove(5);
        attributes.remove(5);
        attributes.remove(5);
        attributes.remove(5);

        relations.clear();
        ArrayList<String> c1 = new ArrayList<String>();
        ArrayList<String> c2 = new ArrayList<String>();
        ArrayList<String> c3 = new ArrayList<String>();
        ArrayList<String> c4 = new ArrayList<String>();
        ArrayList<String> c5 = new ArrayList<String>();
        ArrayList<String> c6 = new ArrayList<String>();

        c1.add("name"); c1.add("surname");
        c2.add("name"); c2.add("university");
        c3.add("name"); c3.add("faculty");
        c4.add("name"); c4.add("semester");
        c5.add("surname"); c5.add("university"); c5.add("faculty");
        c6.add("surname"); c6.add("university"); c6.add("semester");

        relations.add(c1);
        relations.add(c2);
        relations.add(c3);
        relations.add(c4);
        relations.add(c5);
        relations.add(c6);

        clearAffinity();

        System.out.println("test case 19, example from said paper.");
        affinityMapping.put("name", "surname", 10);
        affinityMapping.put("name", "university", 5);
        affinityMapping.put("name", "faculty", 25);
        affinityMapping.put("name", "semester", 15);

        affinityMapping.put("surname", "unisersity", 5);
        affinityMapping.put("surname", "faculty", 20);
        affinityMapping.put("surname", "semester", 30);

        affinityMapping.put("unisersity", "faculty", 10);
        affinityMapping.put("university", "semester", 5);

        affinityMapping.put("faculty", "semester", 15);

        printAffinity();
        printConstraints();

        FragmentAffinity frags19 = new FragmentAffinity(attributes, relations, affinityMapping);

        String res19 = frags19.fragment();
        System.out.println("The fragmentation");
        System.out.println(res19);
    }

    private static void clearAffinity() {
        if (attributes == null) {
            for (String outer : attributesCopy) {
                for (String inner : attributesCopy) {
                    affinityMapping.put(outer, inner, 0);
                }
            }
        } else {
            for (String outer : attributes) {
                for (String inner : attributes) {
                    affinityMapping.put(outer, inner, 0);
                }
            }
        }
    }

    private static void refillInputAttNull() {
        refillInput();
        attributes = null;
    }

    private static void refillInputConNull() {
        refillInput();
        relations = null;
    }

    private static void refillInputNoneExistentAttribute() {
        refillInput();
        ArrayList<String> c2 = new ArrayList<String>();
        c2.add("phone_number");
        c2.add("grade");
        relations.add(1, c2);
    }

    private static void refillInputWrongOrder() {
        refillInput();
        Collections.reverse(relations);
    }

    /**
     * As refillInput() but only swap out c1 to a singleton constraint holding
     * only "name".
     */
    private static void refillInputSingle() {
        refillInput();
        relations.remove(0);
        relations.remove(0);
        ArrayList<String> c1 = new ArrayList<String>();
        c1.add("name");
        relations.add(0, c1);
    }

    private static void refillInput() {
        if (attributes != null) {
            attributes.clear();
        } else {
            attributes = new ArrayList<String>();
        }
        if (relations != null) {
            relations.clear();
        } else {
            relations = new ArrayList<ArrayList<String>>();
        }

        attributes.add("name");
        attributes.add("surname");
        attributes.add("university");
        attributes.add("faculty");
        attributes.add("semester");
        attributes.add("birth_date");
        attributes.add("gender");
        attributes.add("postgraduate");
        attributes.add("grade");

        ArrayList<String> c1 = new ArrayList<String>();
        ArrayList<String> c2 = new ArrayList<String>();
        ArrayList<String> c3 = new ArrayList<String>();
        ArrayList<String> c4 = new ArrayList<String>();
        ArrayList<String> c5 = new ArrayList<String>();
        ArrayList<String> c6 = new ArrayList<String>();
        ArrayList<String> c7 = new ArrayList<String>();

        c1.add("name");
        c1.add("surname");
        c2.add("name");
        c2.add("grade");
        c3.add("university");
        c3.add("faculty");
        c4.add("surname");
        c4.add("gender");
        c5.add("surname");
        c5.add("grade");
        c6.add("grade");
        c6.add("semester");
        c7.add("surname");
        c7.add("university");
        c7.add("postgraduate");

        relations.add(c1);
        relations.add(c2);
        relations.add(c3);
        relations.add(c4);
        relations.add(c5);
        relations.add(c6);
        relations.add(c7);

    }

    private static void printConstraints() {
        System.out.println("The applied constraints are:");
        for (ArrayList<String> outer : relations) {
            for (String inner : outer) {
                System.out.print(inner);
                if (outer.indexOf(inner) != outer.size() - 1) {
                    System.out.print(" - ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private static void printAffinity() {
        Map<String, Map<String, Integer>> map = affinityMapping.rowMap();
        System.out.println("Here are the affinity values:");
        for (String row : map.keySet()) {
            Map<String, Integer> tmp = map.get(row);
            for (Map.Entry<String, Integer> pair : tmp.entrySet()) {
                if (pair.getValue() > 0) {
                    System.out.print(row + " - ");
                    System.out.print(pair.getKey() + " : ");
                    System.out.println(pair.getValue());
                }
            }
        }
        System.out.println();
    }

    /**
     * Makes a deep copy of array list subject. With deep copy means that
     * entirely new objects are created for the new array list instead of just
     * copying references.
     *
     * @param subject,
     *            the array list which gets copied
     * @return a new array list with new objects which corresponds to subject
     */
    private static ArrayList<String> deepCopy(ArrayList<String> subject) {
        ArrayList<String> copy = new ArrayList<String>(subject.size());
        for (String attr : subject) {
            copy.add(new String(attr));
        }
        return copy;
    }
}

