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

/**
 * Just a main method to display how the fragmentation class work.
 * 
 * @author Tobias Andersson
 *
 */

public class FragmentationImpl {

	private static ArrayList<String> attributes = new ArrayList<>();
	private static String tableName = "Students";
	private static ArrayList<ArrayList<String>> relations = new ArrayList<>();

//	public static

	public static void main(String[] args) {
		
		refillInput();
//
		FragmentationUtil noServerLimit = new FragmentationUtil(attributes, relations);
		String fragmentation = noServerLimit.fragment();
//		System.out.println(
//				"The attributes should be fragmented as such, when we assume that we have no limiting amount of servers: ");
//		System.out.println();
		System.out.println(fragmentation);
//
//		refillInput();
//
//		int serverLim = 2;
//
//		FragmentationUtil serverLimit = new FragmentationUtil(attributes, relations, serverLim, tableName);
//		fragmentation = serverLimit.fragment();
//		System.out.println("The attributes should be fragmented as such, when a limiting amount of servers is defined: ");
//		System.out.println();
//		System.out.println(fragmentation);

	}

	// user specified input variables, which are defined in the user
	// interface. Thus, for now it is only hard coded
	private static void refillInput() {
//		attributes.add("name");
//		attributes.add("surname");
//		attributes.add("university");
//		attributes.add("faculty");
//		attributes.add("semester");
//		attributes.add("birth_date");
//		attributes.add("gender");
//		attributes.add("postgraduate");
//		attributes.add("grade");

		attributes.add("com.mycompany.xerp.repository.domain.Client.id");
		attributes.add("com.mycompany.xerp.repository.domain.Client.firstName");
		attributes.add("com.mycompany.xerp.repository.domain.Client.lastName");
		attributes.add("com.mycompany.xerp.repository.domain.Client.address");
		attributes.add("com.mycompany.xerp.repository.domain.Client.phone");
		attributes.add("com.mycompany.xerp.repository.domain.Client.email");
		attributes.add("com.mycompany.xerp.repository.domain.Client.userId");
		attributes.add("com.mycompany.xerp.repository.domain.Client.lastModified");


		ArrayList<String> c1 = new ArrayList<String>();
		ArrayList<String> c2 = new ArrayList<String>();
		ArrayList<String> c3 = new ArrayList<String>();
		ArrayList<String> c4 = new ArrayList<String>();
		ArrayList<String> c5 = new ArrayList<String>();
		ArrayList<String> c6 = new ArrayList<String>();
		ArrayList<String> c7 = new ArrayList<String>();
		ArrayList<String> c8 = new ArrayList<String>();

//		c1.add("name");
//		c1.add("surname");
//		c2.add("name");
//		c2.add("university");
//		c3.add("university");
//		c3.add("faculty");
//		c4.add("surname");
//		c4.add("gender");
//		c5.add("university");
//		c5.add("postgraduate");
//		c6.add("name");
//		c6.add("gender");
//		c7.add("surname");
//		c7.add("grade");
//		c8.add("surname");
//		c8.add("university");

		c1.add("com.mycompany.xerp.repository.domain.Client.lastName");
		c1.add("com.mycompany.xerp.repository.domain.Client.address");

		relations.add(c1);
//		relations.add(c2);
//		relations.add(c3);
//		relations.add(c4);
//		relations.add(c5);
//		relations.add(c6);
//		relations.add(c7);
//		relations.add(c8);
	}
}
