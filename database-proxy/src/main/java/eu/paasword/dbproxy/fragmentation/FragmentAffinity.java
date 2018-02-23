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
package eu.paasword.dbproxy.fragmentation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class which implement the algorithm "Vector-minimal fragmentation with
 * affinity matrix" suggested by Ciriani et al. in the paper "Combining
 * Fragmentation and Encryption to Protect Privacy in Data Storage". Work in
 * progress, a test class for establishing the algorithm before moving it over
 * to the Fragment class.
 *
 * @author Tobias Andersson
 *
 */
public class FragmentAffinity {

    // All the attributes/columns in the database.
    private List<String> a_toPlace = new ArrayList<String>();
    // All the defined constraints to solve.
    private ArrayList<ArrayList<String>> c_toSolve = new ArrayList<>();
    // The resulting fragmentation.
    private ArrayList<ArrayList<String>> f;
    // Indices of the fragments which are still existing.
    private ArrayList<Integer> fragmentIndex;
    // The affinity matrix for the fragmentation algorithm. It defines how often
    // certain attributes are used in the same query.
    private Table<String, String, Integer> affinityMapping;

    public FragmentAffinity(List<String> a_toPlace, ArrayList<ArrayList<String>> c_toSolve,
                            Table<String, String, Integer> affinityMapping) {
        inputCheck(a_toPlace, c_toSolve, affinityMapping);
        this.a_toPlace = a_toPlace;
        this.c_toSolve = c_toSolve;
        this.affinityMapping = affinityMapping;
    }

    /**
     * Implements the algorithm suggested by Ciriani et al. which fragments a
     * set of attributes and privacy constraints, but also take the affinity
     * between attributes into account, i.e. how many times certain attributes
     * are used in the same queries to improve the placement of attributes to
     * increase query speed.
     *
     * @return
     */
    public String fragment() {
        f = new ArrayList<ArrayList<String>>();
        fragmentIndex = new ArrayList<Integer>();

        // Establishes the initial fragmentation, i.e. each attribute in its own
        // fragment.
        for (int i = 0; i < a_toPlace.size(); i++) {
            ArrayList<String> tmp = new ArrayList<String>();
            tmp.add(a_toPlace.get(i));
            f.add(tmp);
            fragmentIndex.add(i);
        }

        // Cells in the affinityMapping matrix which corresponds to constraints
        // from c_toSolve are invalidated. Only pairs are considered. If a
        // pair is invalidated its corresponding constraint is removed from
        // c_toSolve.
        Iterator<ArrayList<String>> i = c_toSolve.iterator();
        while (i.hasNext()) {
            ArrayList<String> con = i.next();
            if (con.size() == 2) {
                affinityMapping.put(con.get(0), con.get(1), -1);
                affinityMapping.put(con.get(1), con.get(0), -1);
                i.remove();
            }
        }

        // Extracts the pair of fragments with maximum affinity.
        ArrayList<String> maxPair = fragsWithMaxAffinity();
        while (!ifDone() && fragmentIndex.size() > 1) {
            int f1 = findOuterIndex(maxPair.get(0));

            if (affinityMapping.get(f.get(f1).get(0), maxPair.get(1)) != -1) {

                int f2 = findOuterIndex(maxPair.get(1));
                ArrayList<String> frag1 = f.get(f1);
                ArrayList<String> frag2 = f.get(f2);

                for (String attr : frag2) {
                    frag1.add(attr);
                }
                ArrayList<String> copyF2 = deepCopy(frag2);
                f.remove(f2);

                fragmentIndex.remove(f2);
                updateFragIndex(f2);
                // Updates the affinity matrix.
                for (Integer k : fragmentIndex) {
                    if (k != f1 && f1 < fragmentIndex.size()) {
                        int min = min(f1, k);
                        int max = max(f1, k);

                        ArrayList<String> fragMin = new ArrayList<String>();
                        ArrayList<String> fragMax = new ArrayList<String>();
                        if (k >= f2) {
                            fragMin = copyF2;
                            fragMax = f.get(k);
                        } else {
                            fragMin = f.get(k);
                            fragMax = copyF2;
                        }

                        for (String attr : frag1) {
                            for (ArrayList<String> outer : f) {
                                for (String inner : outer) {
                                    if (affinityMapping.get(attr, inner) == -1) {
                                        affinityMapping.put(frag1.get(0), outer.get(0), -1);
                                    }
                                }
                            }
                        }

                        if (affinityMapping.get(f.get(min).get(0), f.get(max).get(0)) == -1
                                || affinityMapping.get(fragMin.get(0), fragMax.get(0)) == -1) {
                            affinityMapping.put(f.get(min).get(0), f.get(max).get(0), -1);
                            affinityMapping.put(f.get(max).get(0), f.get(min).get(0), -1);
                        } else {
                            Iterator<ArrayList<String>> cIter = c_toSolve.iterator();
                            while (cIter.hasNext()) {
                                ArrayList<String> con = cIter.next();
                                ArrayList<String> tmpMerge = new ArrayList<String>(f.get(f1));
                                for (String attr : f.get(k)) {
                                    tmpMerge.add(attr);
                                }

                                if (con.containsAll(tmpMerge) && con.size() == tmpMerge.size()) {
                                    for (String t_attr : tmpMerge) {
                                        if (con.contains(t_attr)) {
                                            for (String c_attr : con) {
                                                if (!t_attr.equals(c_attr)) {
                                                    affinityMapping.put(t_attr, c_attr, -1);
                                                    affinityMapping.put(c_attr, t_attr, -1);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Integer val = affinityMapping.get(f.get(min).get(0), f.get(max).get(0));

                            if (val != -1) {
                                affinityMapping.put(f.get(min).get(0), f.get(max).get(0),
                                        val + affinityMapping.get(fragMin.get(0), fragMax.get(0)));
                            }
                        }
                    }
                }
                maxPair = fragsWithMaxAffinity();
            } else {
                affinityMapping.put(maxPair.get(0), maxPair.get(1), -1);
                maxPair = fragsWithMaxAffinity();
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(f);
        return jsonOutput;
    }

    /**
     * Iterates through the affinityMapping matrix and finds the pair of
     * fragments which have the best affinity. It only takes the fragments which
     * still exists in i f into account.
     *
     * @return the two fragments' names in an ArrayList<String>
     */
    private ArrayList<String> fragsWithMaxAffinity() {
        ArrayList<String> res = new ArrayList<String>();
        Map<String, Map<String, Integer>> map = affinityMapping.rowMap();
        int curr = -1;

        for (String row : map.keySet()) {
            Map<String, Integer> tmp = map.get(row);
            for (Map.Entry<String, Integer> pair : tmp.entrySet()) {
                ArrayList<String> check = new ArrayList<String>();
                check.add(pair.getKey());
                if (f.contains(check)) {
                    if (pair.getValue() > curr && !row.equals(pair.getKey())) {
                        res.clear();
                        res.add(row);
                        res.add(pair.getKey());
                        curr = pair.getValue();
                    }
                }
            }
        }

        return res;
    }

    /**
     * Checks if the fragment method should end or not by looking at the
     * existing fragments against the affinity matrix.
     *
     * @return true, if to end otherwise false
     */
    private boolean ifDone() {
        int endCount = 0;
        int totalCount = 0;
        for (ArrayList<String> outer : f) {
            if (f.lastIndexOf(outer) < f.size()) {
                for (int i = f.lastIndexOf(outer) + 1; i < f.size(); i++) {
                    totalCount += 1;
                    if (affinityMapping.get(outer.get(0), f.get(i).get(0)) == -1) {
                        endCount += 1;
                    }
                }
            }
        }
        if (endCount == totalCount) {
            return true;
        } else {
            return false;
        }
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
    private ArrayList<String> deepCopy(ArrayList<String> subject) {
        ArrayList<String> copy = new ArrayList<String>(subject.size());
        for (String attr : subject) {
            copy.add(new String(attr));
        }
        return copy;
    }

    /**
     * Updates the fragmentIndex variable, to keep it updated against the
     * fragmentation list f.
     *
     * @param pos,
     *            the position of a fragment which doesn't exist any longer
     */
    private void updateFragIndex(int pos) {
        for (int i = pos; i < fragmentIndex.size(); i++) {
            int old = fragmentIndex.get(i);
            fragmentIndex.set(i, old - 1);
        }
    }

    /**
     * @return the maximum value, either a or b
     */
    private int max(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * @return the minimum value, either a or b
     */
    private int min(int a, int b) {
        if (a < b) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * Finds the index of the outer array list when we only possess the name of
     * the fragment.
     *
     * @param target,
     *            the name of the fragment we want
     * @return the position of said fragment in the outer array list
     */
    private int findOuterIndex(String target) {
        int res = -1;

        for (ArrayList<String> outer : f) {
            res += 1;
            for (String inner : outer) {
                if (inner.equals(target)) {
                    return res;
                }
            }
        }

        return res;
    }

    /**
     * Checks that the input parameters are valid. Possibly move this error
     * management to the user interface instead, since we want to catch the
     * error as soon as possible.
     *
     * @param affinityMapping,
     *            the affinity matrix
     *
     * @param a_toPlace,
     *            all the attributes
     * @param c_toSolve,
     *            all the constraints
     */
    private void inputCheck(List<String> a_toPlace, ArrayList<ArrayList<String>> c_toSolve,
                            Table<String, String, Integer> affinityMapping) {
        if (a_toPlace == null) {
            throw new IllegalArgumentException("Invalid value of a_toPlace, a_toPlace should not be null.");
        } else if (a_toPlace.isEmpty()) {
            throw new IllegalArgumentException("Invalid value of a_toPlace, a_toPlace should not be empty.");
        } else {
            for (String attri : a_toPlace) {
                if (attri == null) {
                    throw new IllegalArgumentException("String value inside a_toPlace is null");
                } else if (attri.isEmpty()) {
                    throw new IllegalArgumentException("String variable inside a_toPlace is empty");
                }
            }
            Set<String> duplicateCheck = new LinkedHashSet<String>(a_toPlace);
            if (duplicateCheck.size() < a_toPlace.size()) {
                // Removing duplicates, order is maintained.
                a_toPlace.clear();
                a_toPlace.addAll(duplicateCheck);
            }
        }
        if (c_toSolve == null) {
            throw new IllegalArgumentException("Invalid value of c_toSolve, c_toSolve should not be null.");
        } else if (c_toSolve.isEmpty()) {
            throw new IllegalArgumentException("Invalid value of c_toSolve, c_toSolve should not be empty.");
        } else {
            for (ArrayList<String> inner : c_toSolve) {
                if (inner == null) {
                    throw new IllegalArgumentException("Inner array list of c_toSolve is null");
                } else if (inner.isEmpty()) {
                    throw new IllegalArgumentException("Inner array list of c_toSolve is empty");
                } else {
                    for (String attri : inner) {
                        if (attri == null) {
                            throw new IllegalArgumentException("String value inside c_toPlace is null");
                        } else if (attri.isEmpty()) {
                            throw new IllegalArgumentException("String variable inside c_toPlace is empty");
                        }
                    }
                }
            }
        }
        if (affinityMapping == null) {
            throw new IllegalArgumentException("Invalid value of affinityMapping, affinityMapping should not be null.");
        } else if (affinityMapping.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid value of affinityMapping, affinityMapping should not be empty.");
        } else {
            Map<String, Map<String, Integer>> map = affinityMapping.rowMap();

            for (String row : map.keySet()) {
                Map<String, Integer> tmp = map.get(row);
                for (Map.Entry<String, Integer> pair : tmp.entrySet()) {
                    if (pair.getValue() < -1) {
                        throw new IllegalArgumentException("Value of keys: " + row + " " + pair.getKey()
                                + " is invalid, value cannot be below -1");
                    } else if (pair.getValue() == null) {
                        throw new IllegalArgumentException(
                                "Value of keys: " + row + " " + pair.getKey() + " is invalid, value cannot be null");
                    }
                }
            }
        }
    }
}