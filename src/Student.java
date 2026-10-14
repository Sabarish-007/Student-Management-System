import java.util.List;

/**
 * ============================================================================
 * Student.java - Model / Entity Class
 * ============================================================================
 * 
 * Represents a Student entity in the system.
 * 
 * OOP Principles Applied:
 * - Encapsulation: Private member attributes with public getters and setters.
 * - Separation of Concerns: This class models data only; persistence is handled
 *   by StudentDAO.java.
 */
public class Student {

    // ----------------------------------------------------------
    // Private attributes (Encapsulation)
    // ----------------------------------------------------------
    private int id;             // Auto-generated Student ID
    private String name;        // Full name of the student
    private String department;  // Academic department
    private String email;       // Unique email address
    private double marks;       // Marks scored (0 to 100)

    // ==========================================================
    // CONSTRUCTORS
    // ==========================================================

    /**
     * Default No-Arg Constructor
     */
    public Student() {
    }

    /**
     * Constructor for creating a new student (before database insert).
     * The ID is omitted here because MySQL auto-generates it.
     */
    public Student(String name, String department, String email, double marks) {
        this.name = name;
        this.department = department;
        this.email = email;
        this.marks = marks;
    }

    /**
     * Full Constructor for loading an existing student record from the database.
     */
    public Student(int id, String name, String department, String email, double marks) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.email = email;
        this.marks = marks;
    }

    // ==========================================================
    // GETTERS AND SETTERS (Encapsulation)
    // ==========================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    // ==========================================================
    // FORMATTED DISPLAY UTILITIES
    // ==========================================================

    /**
     * Prints a formatted ASCII table representing a list of students.
     */
    public static void printTable(List<Student> students) {
        if (students == null || students.isEmpty()) {
            System.out.println("\n+-------------------------------------------------------------+");
            System.out.println("|                   No student records found.                 |");
            System.out.println("+-------------------------------------------------------------+\n");
            return;
        }

        String border = "+-----+----------------------+----------------------+--------------------------------+--------+";
        System.out.println(border);
        System.out.printf("| %-3s | %-20s | %-20s | %-30s | %-6s |\n",
                "ID", "Name", "Department", "Email", "Marks");
        System.out.println(border);

        for (Student s : students) {
            System.out.printf("| %-3d | %-20s | %-20s | %-30s | %-6.2f |\n",
                    s.getId(),
                    truncate(s.getName(), 20),
                    truncate(s.getDepartment(), 20),
                    truncate(s.getEmail(), 30),
                    s.getMarks());
        }
        System.out.println(border);
        System.out.println("Total Students: " + students.size() + "\n");
    }

    /**
     * Prints a detailed profile card for a single student.
     */
    public void printDetails() {
        System.out.println("\n+-------------------------------------------------------------+");
        System.out.println("|                       STUDENT PROFILE                       |");
        System.out.println("+-------------------------------------------------------------+");
        System.out.printf("| %-15s : %-43d |\n", "Student ID", id);
        System.out.printf("| %-15s : %-43s |\n", "Name", name);
        System.out.printf("| %-15s : %-43s |\n", "Department", department);
        System.out.printf("| %-15s : %-43s |\n", "Email", email);
        System.out.printf("| %-15s : %-43.2f |\n", "Marks", marks);
        System.out.println("+-------------------------------------------------------------+\n");
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    @Override
    public String toString() {
        return "Student [ID=" + id + ", Name=" + name + ", Department=" + department + 
               ", Email=" + email + ", Marks=" + marks + "]";
    }
}
