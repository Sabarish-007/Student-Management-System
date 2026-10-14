import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * ============================================================================
 * Main.java - Console User Interface & Application Controller
 * ============================================================================
 * 
 * Provides the interactive menu matching:
 * ===== STUDENT MANAGEMENT SYSTEM =====
 * 1. Add Student
 * 2. View All Students
 * 3. Search Student
 * 4. Update Student
 * 5. Delete Student
 * 6. Exit
 * Enter your choice: 
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    // Data Access Object (DAO) instance handling all database persistence
    private static final StudentDAO studentDAO = new StudentDAO();

    public static void main(String[] args) {
        // Test database connectivity on startup
        initializeDatabaseConnection();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readIntChoice("Enter your choice: ", 1, 6);

            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    viewAllStudents();
                    break;
                case 3:
                    searchStudent();
                    break;
                case 4:
                    updateStudent();
                    break;
                case 5:
                    deleteStudent();
                    break;
                case 6:
                    System.out.println("\nExiting application. Thank you for using the Student Management System!\n");
                    running = false;
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice. Please enter a number between 1 and 6.");
            }
        }

        scanner.close();
    }

    // ==========================================================
    // MENU DISPLAY
    // ==========================================================

    private static void printMenu() {
        System.out.println("\n===== STUDENT MANAGEMENT SYSTEM =====");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. Search Student");
        System.out.println("4. Update Student");
        System.out.println("5. Delete Student");
        System.out.println("6. Exit");
    }

    /**
     * Verifies MySQL database connection and initializes tables if reachable.
     */
    private static void initializeDatabaseConnection() {
        System.out.print("Connecting to MySQL Database... ");
        if (DBConnection.testConnection()) {
            System.out.println("[CONNECTED]");
            DBConnection.initializeDatabase();
        } else {
            System.out.println("[FAILED]");
            System.out.println("------------------------------------------------------------------");
            System.out.println("[WARN] Unable to connect to MySQL database.");
            System.out.println("Source:   " + DBConnection.getCredentialSource());
            System.out.println("URL:      " + DBConnection.getUrl());
            System.out.println("Username: " + DBConnection.getUser());
            System.out.println("Password: " + DBConnection.getPasswordMasked());
            System.out.println("Tip: Set the DB_PASSWORD environment variable or update 'db.properties'.");
            System.out.println("------------------------------------------------------------------");
        }
    }

    // ==========================================================
    // 1. ADD STUDENT
    // ==========================================================

    private static void addStudent() {
        System.out.println("\n--- [ Add Student ] ---");

        try {
            String name = readRequiredString("Enter Student Name: ");
            String department = readRequiredString("Enter Department: ");

            String email;
            while (true) {
                email = readRequiredString("Enter Email: ");
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    System.out.println("[ERROR] Invalid email format. Example: name@example.com");
                } else if (studentDAO.isEmailTaken(email, 0)) {
                    System.out.println("[ERROR] Email '" + email + "' is already registered. Please enter a unique email.");
                } else {
                    break;
                }
            }

            double marks = readDouble("Enter Marks (0 to 100): ", 0.0, 100.0);

            // Construct new Student object (ID is omitted, MySQL auto-generates it)
            Student newStudent = new Student(name, department, email, marks);

            // Save to database via StudentDAO
            boolean isAdded = studentDAO.addStudent(newStudent);

            if (isAdded) {
                System.out.println("\n[SUCCESS] Student added successfully with ID: " + newStudent.getId());
                newStudent.printDetails();
            } else {
                System.out.println("\n[ERROR] Failed to add student to the database.");
            }

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    // ==========================================================
    // 2. VIEW ALL STUDENTS
    // ==========================================================

    private static void viewAllStudents() {
        System.out.println("\n--- [ All Students ] ---");

        try {
            List<Student> students = studentDAO.getAllStudents();
            if (students.isEmpty()) {
                System.out.println("\n[INFO] No student records found in the database.\n");
            } else {
                Student.printTable(students);
            }
        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    // ==========================================================
    // 3. SEARCH STUDENT
    // ==========================================================

    private static void searchStudent() {
        System.out.println("\n--- [ Search Student ] ---");

        try {
            int id = readPositiveInt("Enter Student ID to search: ");
            Student student = studentDAO.getStudentById(id);

            if (student != null) {
                System.out.println("\n[SUCCESS] Student record found:");
                student.printDetails();
            } else {
                System.out.println("\n[ERROR] Student with ID " + id + " not found.\n");
            }

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    // ==========================================================
    // 4. UPDATE STUDENT
    // ==========================================================

    private static void updateStudent() {
        System.out.println("\n--- [ Update Student ] ---");

        try {
            int id = readPositiveInt("Enter Student ID to update: ");
            Student student = studentDAO.getStudentById(id);

            if (student == null) {
                System.out.println("\n[ERROR] Student with ID " + id + " does not exist.\n");
                return;
            }

            System.out.println("\nCurrent Student Profile:");
            student.printDetails();
            System.out.println("Note: Press [Enter] without typing to keep the current value.");

            // 1. Update Name
            System.out.print("New Name [" + student.getName() + "]: ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) {
                student.setName(newName);
            }

            // 2. Update Department
            System.out.print("New Department [" + student.getDepartment() + "]: ");
            String newDept = scanner.nextLine().trim();
            if (!newDept.isEmpty()) {
                student.setDepartment(newDept);
            }

            // 3. Update Email
            System.out.print("New Email [" + student.getEmail() + "]: ");
            String newEmail = scanner.nextLine().trim();
            if (!newEmail.isEmpty()) {
                if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    System.out.println("[WARN] Invalid email format. Kept previous email.");
                } else if (studentDAO.isEmailTaken(newEmail, student.getId())) {
                    System.out.println("[WARN] Email already registered to another student. Kept previous email.");
                } else {
                    student.setEmail(newEmail);
                }
            }

            // 4. Update Marks
            System.out.print("New Marks [" + student.getMarks() + "]: ");
            String marksInput = scanner.nextLine().trim();
            if (!marksInput.isEmpty()) {
                try {
                    double newMarks = Double.parseDouble(marksInput);
                    if (newMarks >= 0.0 && newMarks <= 100.0) {
                        student.setMarks(newMarks);
                    } else {
                        System.out.println("[WARN] Marks must be between 0 and 100. Kept previous marks.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[WARN] Invalid numeric format. Kept previous marks.");
                }
            }

            // Persist changes through StudentDAO
            boolean updated = studentDAO.updateStudent(student);

            if (updated) {
                System.out.println("\n[SUCCESS] Student details updated successfully!");
                student.printDetails();
            } else {
                System.out.println("\n[ERROR] Failed to update student record in the database.");
            }

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    // ==========================================================
    // 5. DELETE STUDENT
    // ==========================================================

    private static void deleteStudent() {
        System.out.println("\n--- [ Delete Student ] ---");

        try {
            int id = readPositiveInt("Enter Student ID to delete: ");
            Student student = studentDAO.getStudentById(id);

            if (student == null) {
                System.out.println("\n[ERROR] Student with ID " + id + " not found.\n");
                return;
            }

            student.printDetails();
            System.out.print("Are you sure you want to permanently delete this student? (yes/no): ");
            String confirm = scanner.nextLine().trim();

            if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
                boolean deleted = studentDAO.deleteStudent(id);
                if (deleted) {
                    System.out.println("\n[SUCCESS] Student with ID " + id + " deleted successfully.\n");
                } else {
                    System.out.println("\n[ERROR] Failed to delete student from the database.\n");
                }
            } else {
                System.out.println("\n[INFO] Deletion cancelled by user.\n");
            }

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    // ==========================================================
    // INPUT UTILITIES & EXCEPTION HANDLING
    // ==========================================================

    private static String readRequiredString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("[ERROR] This field is required and cannot be empty.");
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value > 0) {
                    return value;
                }
                System.out.println("[ERROR] Please enter a positive integer greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid number. Please enter a valid integer.");
            }
        }
    }

    private static int readIntChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("[ERROR] Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid input. Please enter a number from " + min + " to " + max + ".");
            }
        }
    }

    private static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("[ERROR] Marks must be between %.2f and %.2f.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid input. Please enter a valid numeric value.");
            }
        }
    }

    private static void handleSqlException(SQLException e) {
        if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
            System.out.println("\n[ERROR] Duplicate entry detected: " + e.getMessage());
        } else {
            System.out.println("\n[ERROR] Database error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState() + " | Error Code: " + e.getErrorCode());
            System.out.println("Please check your database connection and credentials in 'db.properties'.");
        }
    }
}
