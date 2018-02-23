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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;


/**
 * A class that implements the "Minimal Fragmentation"-algorithm suggested by Ciriani et al. in
 * "Fragmentation and Encryption to Enforce Privacy in Data Storage".
 * @author Malin Lindstr�m & Christian Nordahl
 *
 */
public class FragmentationUtil {

	// All the attributes/columns in the database.
	private List<String> a_toPlace = new ArrayList<String>();
	// All the defined constraints to solve.
	private ArrayList<ArrayList<String>> c_toSolve = new ArrayList<>();
	// Will contain the number of servers that are available for the
	// fragmentation scheme, when a conflict exists.
	private int nbrOfServers = -1;
	// name of the table for which is to be fragmented
//	private String tableName;

	// used as temporary storage containers, since the algorithm needs to be
	// able to remove values from the other two and to save between loops in the
	// use case that the program have to fragment several times
	private List<String> a_repl;
	private ArrayList<ArrayList<String>> c_repl;

	/**
	 * Constructor for the instance when there is no limitation on the number of
	 * servers. Creates an instance of the class, also checks if the input is
	 * valid.
	 *
	 * @param a_toPlace,
	 *            all the attributes
	 * @param c_toSolve,
	 *            all the constraints
	 */
	public FragmentationUtil(List<String> a_toPlace, ArrayList<ArrayList<String>> c_toSolve) { //, String tableName) {
		inputCheck(a_toPlace, c_toSolve);
		this.a_toPlace = a_toPlace;
		this.c_toSolve = c_toSolve;
//		this.tableName = tableName;
	}

	/**
	 * Constructor for the instance when there is a limitations on the number of
	 * servers. Creates an instance of the class, also checks if the input is
	 * valid.
	 *
	 * @param a_toPlace,
	 *            all the attributes
	 * @param c_toSolve,
	 *            all the constraints
	 * @param inputServers,
	 *            the amount of available servers
	 */
	public FragmentationUtil(List<String> a_toPlace, ArrayList<ArrayList<String>> c_toSolve, int inputServers) {
			//	, String tableName) {
		if (inputServers <= 0) {
			throw new IllegalArgumentException("Invalid value of inputServers");
		}
		inputCheck(a_toPlace, c_toSolve);
		this.a_toPlace = a_toPlace;
		this.c_toSolve = c_toSolve;
		this.nbrOfServers = inputServers;
//		this.tableName = tableName;

		this.a_repl = new ArrayList<String>(a_toPlace);
		this.c_repl = new ArrayList<ArrayList<String>>(c_toSolve);
	}

	/**
	 * Checks that the input parameters are valid. Possibly move this error
	 * management to the user interface instead, since we want to catch the
	 * error as soon as possible.
	 *
	 * @param a_toPlace,
	 *            all the attributes
	 * @param c_toSolve,
	 *            all the constraints
	 */
	private void inputCheck(List<String> a_toPlace, ArrayList<ArrayList<String>> c_toSolve) {
		if (a_toPlace == null) {
			throw new IllegalArgumentException("Invalid value of a_toPlace");
		}
		if (c_toSolve == null) {
			throw new IllegalArgumentException("Invalid value of c_toSolve");
		}

	}

	/**
	 * Loops through all the provided constraints to find the one attribute
	 * which occurs the most.
	 *
	 * @return the name of the most occurred attribute
	 */
	private String occurancers() {
		String maxAttri = "error";
		int currMax = 0;
		Map<String, Integer> counter = new HashMap<>();
		for (ArrayList<String> inner : c_toSolve) {
			for (String attribute : inner) {
				Integer temp = counter.get(attribute);
				if (temp == null) {
					counter.put(attribute, 1);
					temp = counter.get(attribute);
					if (1 > currMax) {
						currMax = 1;
						maxAttri = attribute;
					}
				} else {
					temp = temp + 1;
					counter.put(attribute, temp);
					if (temp > currMax) {
						currMax = temp;
						maxAttri = attribute;
					}
				}
			}
		}
		System.out
				.println("Most occurring attribute in constraints is: " + maxAttri + " with the value of: " + currMax);
		return maxAttri;
	}

	/**
	 * Deletes all constraints concerning the inputed parameter.
	 *
	 * @param attributeName,
	 *            the name of the attribute to have its constraints deleted
	 */
	private void disperseConstraints(String attributeName) {
		for (ArrayList<String> inner : new ArrayList<ArrayList<String>>(c_toSolve)) {
			if (inner.indexOf(attributeName) > -1) {
				c_toSolve.remove(inner);
				System.out.println("A constraints have been removed");
			}
		}
		c_repl = new ArrayList<ArrayList<String>>(c_toSolve);
	}

	/**
	 * Implements an algorithm by Ciriani et al. that divides a set of
	 * attributes into fragments based on a set of constraints.
	 *
	 * @return A list of fragments in a GSON prettyPrint format
	 */
	@SuppressWarnings("unchecked")
	public String fragment() {
		ArrayList<ArrayList<ArrayList<String>>> a_con = new ArrayList<>();
		ArrayList<ArrayList<String>> fragments = new ArrayList<ArrayList<String>>();
		ArrayList<Integer> nr_con = new ArrayList<>();
		int attr_idx = 0;
		int temp_idx = 0;
		String attr_name;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Set a_con[a] with all constraints in c_toSolve that involve
		// a_toPlace[a]
		// Set nr_con[a] to the number of constraints in a_con[a] (i.e. the
		// number of constraints on a_toPlace[a])
		for (int a = 0; a < a_toPlace.size(); a++) {
			attr_name = a_toPlace.get(a);
			a_con.add(get_constraints(c_toSolve, attr_name));
			nr_con.add(a_con.get(a).size());
		}

		while (a_toPlace.size() != 0)// Still attributes to place
		{
			if (c_toSolve.size() != 0)// Still constraints to solve
			{
				// Get the attribute index of the attribute with max unresolved
				// constraints
				attr_idx = nr_con.indexOf(Collections.max(nr_con));

				// Get the list of constraints on a_toPlace[attr_idx]
				ArrayList<ArrayList<String>> attr_con = new ArrayList<>();
				attr_con.addAll(a_con.get(attr_idx));
				for (int a = 0; a < attr_con.size(); a++) {
					// For each matching constraint c in {c_toSolve AND
					// a_con[attr_idx]}
					if (c_toSolve.contains(attr_con.get(a))) {
						// Remove c from c_toSolve
						ArrayList<String> c = attr_con.get(a);
						c_toSolve.remove(c);

						// For each attribute a in c, decrease its cardinality
						// in nr_con by 1
						for (int b = 0; b < c.size(); b++) {
							temp_idx = a_toPlace.indexOf(c.get(b));
							nr_con.set(temp_idx, nr_con.get(temp_idx) - 1);
						}
					}
				}
			} else// Constraints are solved but attributes remain that need to
			// be placed
			{
				// Pick any attribute
				attr_idx = 0;
			}
			attr_name = a_toPlace.get(attr_idx);
			boolean done = false;
			// Try to place attribute in an existing fragment
			for (int a = 0; a < fragments.size(); a++) {
				if (fragment_ok(attr_name, fragments.get(a), a_con.get(attr_idx))) {
					fragments.get(a).add(attr_name);
					done = true;
					break;
				}
			}
			if (!done)// Then attribute could not be placed, need to create a
			// new fragment
			{
				ArrayList<String> temp = new ArrayList<>();
				temp.add(attr_name);
				fragments.add(temp);
			}
			// Remove attribute that was just placed
			a_toPlace.remove(attr_idx);
			nr_con.remove(attr_idx);
			a_con.remove(attr_idx);
		}

		if (nbrOfServers > 0) {
			if (nbrOfServers < fragments.size()) {
				// too few servers
				System.out.println(
						"The fragmentation is not successfull, since the number of needed servers is too low.");
				// a strategy for which to execute the fragmentation
				// again, but with fewer constraints.
				// we disperse of the constraints for which a single most
				// occurring attribute is associated with. Then we try to
				// fragment again.
				a_toPlace = new ArrayList<String>(a_repl);
				c_toSolve = new ArrayList<ArrayList<String>>(c_repl);
				disperseConstraints(occurancers());
				fragments = gson.fromJson(fragment(), fragments.getClass());
			}
		}

		String jsonOutput = gson.toJson(fragments);

		return jsonOutput;
	}

	/**
	 *
	 * @param constraints,
	 *            list of constraints to check
	 * @param name,
	 *            the name to check for
	 * @return A subset of the constraints that contain name
	 */
	public ArrayList<ArrayList<String>> get_constraints(ArrayList<ArrayList<String>> constraints, String name) {
		ArrayList<ArrayList<String>> result = new ArrayList<>();
		for (ArrayList<String> con : constraints) {
			if (con.contains(name)) {
				result.add(con);
			}
		}

		return result;
	}

	/**
	 * Checks if adding attr to fragment will violate any constraints. A
	 * constraint is violated if, and only if, the constraint is either EQUAL
	 * to, or a SUBSET of, the fragment. That means that if ALL attributes in
	 * constraint are also in the fragment, then adding @param attr to that
	 * fragment will violate the constraint.
	 *
	 * @param attr
	 * @param fragment
	 * @param constraints
	 * @return true if no constraints are violated, false otherwise
	 */
	private boolean fragment_ok(String attr, List<String> fragment, ArrayList<ArrayList<String>> constraints) {
		// For each constraint
		for (ArrayList<String> con : constraints) {
			boolean is_ok = false;
			ArrayList<String> temp = new ArrayList<String>();
			// Move constraint to a copy and remove the attribute we want to add
			temp.addAll(con);
			temp.remove(attr);

			// For each attribute in constraint
			for (String a : temp) {
				// If ANY of the attributes in temp are NOT in the fragment
				// then this constraint will not be violated by adding @param
				// attr
				if (!fragment.contains(a)) {
					is_ok = true;
				}
			}
			// If is_ok is still false, then we have checked every
			// attribute in temp and all of them are in fragment and
			// @param attr cannot be placed in fragment
			if (!is_ok) {
				return false;
			}
		}

		// If we have not returned earlier due to constraint violation
		// then no constraint is violated and this fragment is OK
		return true;
	}
}