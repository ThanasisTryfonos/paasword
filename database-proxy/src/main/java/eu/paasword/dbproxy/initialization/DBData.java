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
package eu.paasword.dbproxy.initialization;

import eu.paasword.dbproxy.entities.*;

import java.util.*;

/**
 * This class covers test data to be used for test scenarios.
 * @author Valentin Zipf
 *
 */
public class DBData {

    private static final Integer MAX_SEMESTERS = 12;
    private static int numberOfStudents = 2000;

	private static Map<String, Uni> db_universities;
    private static Map<String, City> db_cities;
    private static Map<String, Country> db_countries;
    private static Map<String, Faculty> db_faculties;
    private static List<Student> db_students;
	private static List<String> db_male_names;
	private static List<String> db_surnames;
	private static List<String> db_birthdays;
	private static List<Double> db_grades;
	private static List<Boolean> db_booleans;
    private static List<String> db_female_names;
    private static Map<String, String> db_create_tables;
    private static Map<String, String> db_drop_tables;

    static {
        createDBTableStatements();
        createDropDBTableStatements();
    }

    /**
     * This method must be called before any other method if this class is called.
     *
     */
    public static void init(){
        init(20);
    }

    /**
     * This method must be called before any other method if this class is called.
     *
     * @param studentCount
     */
    public static void init(int studentCount){
        createCountries();
        createCities();
        createUnis();
        createFaculties();
        createMaleNames();
        createFemaleNames();
        createSurnames();
        createBirthdays();
        createGrades();
        createBooleans();
        createStudents(studentCount);
    }

    private static void createDropDBTableStatements() {
        db_drop_tables = new LinkedHashMap<>();
        db_drop_tables.put(Country.TABLE_NAME, Country.DROP_TABLE_COUNTRY);
        db_drop_tables.put(City.TABLE_NAME, City.DROP_TABLE_CITY);
        db_drop_tables.put(Uni.TABLE_NAME, Uni.DROP_TABLE_UNIS);
        db_drop_tables.put(Faculty.TABLE_NAME, Faculty.DROP_TABLE_FACULTY);
        db_drop_tables.put(Student.TABLE_NAME, Student.DROP_TABLE_STUDENTS);
    }

    private static void createDBTableStatements() {
        db_create_tables = new LinkedHashMap<>();
        db_create_tables.put(Country.TABLE_NAME, Country.CREATE_TABLE_COUNTRY);
        db_create_tables.put(City.TABLE_NAME, City.CREATE_TABLE_CITY);
        db_create_tables.put(Uni.TABLE_NAME, Uni.CREATE_TABLE_UNIS);
        db_create_tables.put(Faculty.TABLE_NAME, Faculty.CREATE_TABLE_FACULTY);
        db_create_tables.put(Student.TABLE_NAME, Student.CREATE_TABLE_STUDENTS);
    }


    public static Map<String, String> getCreateTableStatements(){
        return db_create_tables;
    }

    public static Map<String, String> getDropTableStatements(){
        return  db_drop_tables;
    }

    public static Collection<Uni> getUnis(){
        return db_universities.values();
    }

    public static Collection<Country> getCountries(){
        return db_countries.values();
    }

    public static Collection<City> getCities(){
        return db_cities.values();
    }

    public static Collection<Student> getStudents() {return db_students;}

    public static Collection<Faculty> getFaculties(){
        return db_faculties.values();
    }

    private static void createCountries() {
        db_countries = new HashMap<>();
        db_countries.put("Germany", new Country(1, "Germany", 85000000));
        db_countries.put("England", new Country(2, "England", 45000000));
        db_countries.put("USA", new Country(3, "USA", 185000000));
        db_countries.put("Sweden", new Country(4, "Sweden", 600000));
        db_countries.put("Netherlands", new Country(5, "Netherlands", 900000));
        db_countries.put("France", new Country(6, "France", 77000000));

    }

    private static void createCities() {
        db_cities = new HashMap<>();
        db_cities.put("Berlin", new City(1, "Berlin", db_countries.get("Germany")));
        db_cities.put("Heidelberg", new City(2, "Heidelberg", db_countries.get("Germany")));
        db_cities.put("Mannheim", new City(3, "Mannheim", db_countries.get("Germany")));
        db_cities.put("Dresden", new City(4, "Dresden", db_countries.get("Germany")));
        db_cities.put("Oxford", new City(5, "Oxford", db_countries.get("England")));
        db_cities.put("Birmingham", new City(6, "Birmingham", db_countries.get("England")));
        db_cities.put("Karlsruhe", new City(7, "Karlsruhe", db_countries.get("Germany")));
        db_cities.put("Stockholm", new City(8, "Stockholm", db_countries.get("Sweden")));
        db_cities.put("Amsterdam", new City(9, "Amsterdam", db_countries.get("Netherlands")));
        db_cities.put("Paris", new City(10, "Paris", db_countries.get("France")));


        db_cities.put("Bremen", new City(11, "Bremen", db_countries.get("Germany")));
        db_cities.put("Chemnitz", new City(12, "Chemnitz", db_countries.get("Germany")));
        db_cities.put("Massachusetts", new City(13, "Massachusetts", db_countries.get("USA")));
        db_cities.put("Havard", new City(14, "Havard", db_countries.get("USA")));
        db_cities.put("Stanford", new City(15, "Stanford", db_countries.get("USA")));
        db_cities.put("Chicago", new City(16, "Chicago", db_countries.get("USA")));
        db_cities.put("Aachen", new City(17, "Aachen", db_countries.get("Germany")));

    }

    private static Map<String, Uni> createUnis(){
        db_universities = new HashMap<>();
        db_universities.put("KIT", new Uni(1, "KIT", 120, db_cities.get("Karlsruhe")));
        db_universities.put("Uni-Heidelberg", new Uni(2, "Uni Heidelberg", 70, db_cities.get("Heidelberg")));
        db_universities.put("DHBW-Mannheim", new Uni(3, "DHBW Mannheim", 40, db_cities.get("Mannheim")));
        db_universities.put("TU-Dresden", new Uni(4, "TU-Dresden", 200, db_cities.get("Dresden")));
        db_universities.put("Uni-Paris", new Uni(5, "Uni-Paris", 250, db_cities.get("Paris")));
        db_universities.put("Uni-Amsterdam", new Uni(6, "Uni-Amsterdam", 120, db_cities.get("Amsterdam")));
        db_universities.put("Uni-Stockholm", new Uni(7, "Uni-Stockholm", 65, db_cities.get("Stockholm")));
        db_universities.put("TU-Berlin", new Uni(8, "TU-Berlin", 100, db_cities.get("Berlin")));
        db_universities.put("DHBW-Karlsruhe", new Uni(9, "DHBW-Karlsruhe", 25, db_cities.get("Karlsruhe")));
        db_universities.put("HS-Karlsruhe", new Uni(10, "HS-Karlsruhe", 15, db_cities.get("Karlsruhe")));
        db_universities.put("KIT-Campus-Nord", new Uni(11, "KIT-Campus-Nord", 10, db_cities.get("Karlsruhe")));

        db_universities.put("Med-Uni-HD", new Uni(12, "Med-Uni-HD", 20, db_cities.get("Heidelberg")));
        db_universities.put("MIT", new Uni(13, "MIT", 320, db_cities.get("Massachusetts")));
        db_universities.put("Havard-University", new Uni(14, "Havard-University", 220, db_cities.get("Havard")));
        db_universities.put("Stanford-University", new Uni(15, "Stanford-University", 220, db_cities.get("Stanford")));
        db_universities.put("Chicago-University", new Uni(16, "Chicago-University", 180, db_cities.get("Chicago")));


        return db_universities;
    }

    private static void createStudents(int numberOfStudents) {
        db_students = new ArrayList<>();
        for(long i=1; i<= numberOfStudents; ++i){
            String gender = getRandomGender();
            db_students.add(new Student(i,
                    getRandomName(gender),
                    getRandomSurname(),
                    getRandomBirthday(),
                    gender,
                    getRandomSemester(),
                    getRandomGrade(),
                    getRandomUni(),
                    getRandomFaculty()));
        }
    }

    private static String getRandomGender(){
        return new Random().nextInt(2) == 0 ? "m" : "f";
    }


    private static String getRandomName(final String gender){
        if(gender.equalsIgnoreCase("m")){
        return getMaleNames().get(new Random().nextInt(getMaleNames().size()));
        }
        return getFemaleNames().get(new Random().nextInt(getFemaleNames().size()));
    }
    

    private static String getRandomSurname(){
        return getSurnames().get(new Random().nextInt(getSurnames().size()));
    }

    private static String getRandomBirthday(){
        return getBirthdays().get(new Random().nextInt(getBirthdays().size()));
    }

    private static Faculty getRandomFaculty(){
        Object[] faculties = db_faculties.values().toArray();
        return (Faculty) faculties[new Random().nextInt(faculties.length)];
    }

    private static Uni getRandomUni(){
        Object[] unis = db_universities.values().toArray();
        return (Uni) unis[new Random().nextInt(unis.length)];
    }

    private static Double getRandomGrade(){
        return getGrades().get(new Random().nextInt(getGrades().size()));
    }

    private static boolean getRandomBoolean(){
        return getBooleans().get(new Random().nextInt(getBooleans().size()));
    }

    private static Integer getRandomSemester() { return new Random().nextInt(MAX_SEMESTERS) + 1;}




    private static void createMaleNames(){
        db_male_names = new ArrayList<String>();
        db_male_names.add("Anton");
        db_male_names.add("Martin");
        db_male_names.add("Max");
        db_male_names.add("Elias");
        db_male_names.add("Tobias");
        db_male_names.add("Michael");
        db_male_names.add("Alex");
        db_male_names.add("Bob");
        db_male_names.add("Florian");
        db_male_names.add("Stefan");
        db_male_names.add("Markus");
        db_male_names.add("Manuel");
        db_male_names.add("Matthias");
        db_male_names.add("Jan");
        db_male_names.add("Tom");
        db_male_names.add("Tomas");
        db_male_names.add("Torger");
        db_male_names.add("Tobias");
        db_male_names.add("Todd");
        db_male_names.add("Jonas");
        db_male_names.add("Leander");
        db_male_names.add("Finn");
        db_male_names.add("Wolfgang");
        db_male_names.add("Alexander");
        db_male_names.add("Bert");
        db_male_names.add("Vincent");

    }
    
    private static void createFemaleNames(){
        db_female_names = new ArrayList<String>();
        db_female_names.add("Anabelle");
        db_female_names.add("Bella");
        db_female_names.add("Celine");
        db_female_names.add("Doris");
        db_female_names.add("Ellenore");
        db_female_names.add("Franziska");
        db_female_names.add("Theresa");
        db_female_names.add("Ronja");
        db_female_names.add("Samira");
        db_female_names.add("Emma");
        db_female_names.add("Hannah");
        db_female_names.add("Sofia");
        db_female_names.add("Frida");
        db_female_names.add("Ida");
        db_female_names.add("Ella");
        db_female_names.add("Julia");
        db_female_names.add("Marlene");
        db_female_names.add("Lisa");
        db_female_names.add("Alina");
        db_female_names.add("Alisa");
        db_female_names.add("Sarah");

    }
    
    private static List<String> getMaleNames(){
    	return db_male_names;
    }

    private static List<String> getFemaleNames(){
        return db_female_names;
    }

    private static void createSurnames(){
        db_surnames = new ArrayList<String>();
        db_surnames.add("Müller");
        db_surnames.add("Schmidt");
        db_surnames.add("Becker");
        db_surnames.add("Fischer");
        db_surnames.add("Koch");
        db_surnames.add("Degenhart");
        db_surnames.add("Henn");
        db_surnames.add("Ziegler");
        db_surnames.add("Mahler");
        db_surnames.add("Schneider");
        db_surnames.add("Meyer");
        db_surnames.add("Schulz");
        db_surnames.add("Hoffmann");
        db_surnames.add("Schäfer");
        db_surnames.add("Wolf");
        db_surnames.add("Klein");
        db_surnames.add("Zimmermann");
        db_surnames.add("Braun");
        db_surnames.add("Hartmann");
        db_surnames.add("Lange");
        db_surnames.add("Werner");
        db_surnames.add("Schmitz");
        db_surnames.add("Schubert");
        db_surnames.add("Friedrich");
        db_surnames.add("Keller");
        db_surnames.add("Frank");
        db_surnames.add("Berger");
        db_surnames.add("Winkler");
        db_surnames.add("Brandt");
        db_surnames.add("Jaas");
        db_surnames.add("Schreiber");
        db_surnames.add("Graf");
        db_surnames.add("Schulte");
        db_surnames.add("Kuhn");
        db_surnames.add("Seidel");
    }
    
    private static List<String> getSurnames(){
    	return db_surnames;
    }

    private static void createFaculties(){
        db_faculties = new HashMap<>();
        db_faculties.put("CS", new Faculty(1, "Computer-Science"));
        db_faculties.put("BI", new Faculty(2, "Biology"));
        db_faculties.put("TI", new Faculty(3, "Theoretical Computer-Science"));
        db_faculties.put("MA", new Faculty(4, "Mathematics"));
        db_faculties.put("SP", new Faculty(5, "Sports"));
        db_faculties.put("CH", new Faculty(6, "Chemistry"));
        db_faculties.put("EC", new Faculty(7, "Economy"));
        db_faculties.put("PT", new Faculty(8, "Politics"));

    }
    

   
    private static void createBirthdays(){
        db_birthdays = new ArrayList<String>();
        db_birthdays.add("1990-04-15");
        db_birthdays.add("1991-05-16");
        db_birthdays.add("1992-07-21");
        db_birthdays.add("1989-08-02");
        db_birthdays.add("1988-09-30");
    }
    
    private static List<String> getBirthdays(){
    	return db_birthdays;
    }

    private static void createBooleans(){
        db_booleans = new ArrayList<Boolean>();
        db_booleans.add(true);
        db_booleans.add(false);
    }
    
    private static List<Boolean> getBooleans(){
        return db_booleans;
    }

    private static void createGrades(){
        db_grades = new ArrayList<Double>();
        db_grades.add(1.0);
        db_grades.add(1.3);
        db_grades.add(1.7);
        db_grades.add(2.0);
        db_grades.add(2.3);
        db_grades.add(2.7);
        db_grades.add(3.0);
        db_grades.add(3.3);
        db_grades.add(3.7);
        db_grades.add(4.0);
        db_grades.add(4.3);
        db_grades.add(4.7);
        db_grades.add(5.0);
    }
    
    private static List<Double> getGrades(){
    	return db_grades;
    }


}
