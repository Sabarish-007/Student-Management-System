import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * StudentDAO.java - Data Access Object (DAO) Pattern
 * ============================================================================
 * 
 * Purpose:
 * Separates database persistence logic and SQL operations from the
 * business model (Student) and the user interface (Main).
 * 
 * Key Highlights:
 * - All SQL queries use PreparedStatement to protect against SQL Injection.
 * - ResultSet is used to retrieve and map database rows to Student objects.
 * - try-with-resources ensures Connection, PreparedStatement, and ResultSet
 *   are automatically and safely closed.
 */
public class StudentDAO {

    /**
     * 1. Add a new student record into the MySQL database.
     * 
     * Uses PreparedStatement with RETURN_GENERATED_KEYS to obtain the
     * auto-generated primary key (Student ID) assigned by MySQL.
     * 
     * @param student Student object to insert (ID will be set upon success).
     * @return true if insertion was successful, false otherwise.
     * @throws SQLException if a database error occurs.
     */
    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (name, department, email, marks) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Bind query parameters securely
            pstmt.setString(1, student.getName().trim());
            pstmt.setString(2, student.getDepartment().trim());
            pstmt.setString(3, student.getEmail().trim().toLowerCase());
            pstmt.setDouble(4, student.getMarks());

            int affectedRows = pstmt.executeUpdate();

            // Retrieve the auto-generated student ID
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * 2. Retrieve all student records from the database.
     * 
     * Uses PreparedStatement to execute SELECT and maps the ResultSet
     * to a List of Student model objects.
     * 
     * @return List of Student objects sorted by ID ascending.
     * @throws SQLException if a database access error occurs.
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, name, department, email, marks FROM students ORDER BY id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Student s = new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getString("email"),
                        rs.getDouble("marks")
                );
                students.add(s);
            }
        }
        return students;
    }

    /**
     * 3. Search for a single student by unique Student ID.
     * 
     * @param id The primary key ID of the student.
     * @return The Student object if found, or null if no record exists.
     * @throws SQLException if a database query error occurs.
     */
    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT id, name, department, email, marks FROM students WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getString("email"),
                            rs.getDouble("marks")
                    );
                }
            }
        }
        return null;
    }

    /**
     * 4. Update an existing student's details in the database.
     * 
     * @param student The Student object containing updated information.
     * @return true if the row was updated, false otherwise.
     * @throws SQLException if a database error occurs.
     */
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, department = ?, email = ?, marks = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName().trim());
            pstmt.setString(2, student.getDepartment().trim());
            pstmt.setString(3, student.getEmail().trim().toLowerCase());
            pstmt.setDouble(4, student.getMarks());
            pstmt.setInt(5, student.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 5. Delete a student record from the database by ID.
     * 
     * @param id The Student ID to delete.
     * @return true if deleted successfully, false otherwise.
     * @throws SQLException if a database error occurs.
     */
    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 6. Validation Helper: Check if an email address is already in use.
     * 
     * When excludeId > 0, skips the student's own record during updates.
     * 
     * @param email The email address to check.
     * @param excludeId ID to exclude from checking (pass 0 for new additions).
     * @return true if email is already taken, false if available.
     * @throws SQLException if a query error occurs.
     */
    public boolean isEmailTaken(String email, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE LOWER(email) = LOWER(?) AND id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.trim());
            pstmt.setInt(2, excludeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
