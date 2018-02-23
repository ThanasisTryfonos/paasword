package eu.paasword.dbproxy.entities;

/**
 * Created by valentin on 03.06.16.
 */
public class Student implements ICRUD{
    public static final String DROP_TABLE_STUDENTS = "DROP TABLE IF EXISTS students CASCADE";
    public static final String CREATE_TABLE_STUDENTS = "CREATE TABLE students(id int primary key, name char(50) not null, surname char(50) not null, birth_date Date not null, gender char(1) not null,  semester int not null, grade double precision, fk_university int references Unis, fk_faculty int references faculties);";
    public static final String INSERT_INTO = "INSERT INTO students (id, name, surname, birth_date, gender, semester, grade, fk_university, fk_faculty) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);";
    public static final String TABLE_NAME = "students";

    private long id;
    private String name;
    private String surname;
    private String gender;
    private String birth_date;
    private int semester;
    private double grade;
    private long fk_university;
    private long fk_faculty;

    public Student(long id, String name, String surname, String birth_date, String gender, int semester, double grade, Uni fk_university, Faculty fk_faculty) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.birth_date = birth_date;
        this.semester = semester;
        this.grade = grade;
        this.fk_university = fk_university.getId();
        this.fk_faculty = fk_faculty.getId();
    }

    @Override
    public String insertInto() {
        return String.format(INSERT_INTO, id, Wrap.w(name), Wrap.w(surname), Wrap.w(birth_date), Wrap.w(gender), semester, grade, fk_university, fk_faculty);
    }

    @Override
    public long getId() {
        return id;
    }
}
