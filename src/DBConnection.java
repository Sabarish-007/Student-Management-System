import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * ============================================================================
 * DBConnection.java - Centralized JDBC Connection Manager
 * ============================================================================
 * 
 * Secure Credential Hierarchy:
 * 1. Environment Variables:
 *    - DB_URL      : Custom MySQL JDBC URL (e.g. jdbc:mysql://localhost:3306/student_db)
 *    - DB_USER     : MySQL Username (e.g. root)
 *    - DB_PASSWORD : MySQL Password (never committed to version control)
 * 2. Configuration File:
 *    - db.properties in the project root (ignored by .gitignore)
 * 3. Default Fallbacks:
 *    - localhost:3306/student_db, user: root, password: ""
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    // Default connection settings
    private static String url = "jdbc:mysql://localhost:3306/student_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "";
    private static String credentialSource = "Default Defaults";

    static {
        // Step 1: Load configuration with secure precedence
        loadConfiguration();

        // Step 2: Register MySQL JDBC Driver
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("[CRITICAL ERROR] MySQL JDBC Driver not found in classpath!");
            System.err.println("Please verify that 'lib/mysql-connector-j-8.4.0.jar' is included in your classpath.");
        }
    }

    /**
     * Loads connection configuration adhering to security hierarchy:
     * Environment variables take precedence over db.properties.
     */
    public static void loadConfiguration() {
        // 1. First, check db.properties if it exists locally
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
                url = props.getProperty("db.url", url);
                user = props.getProperty("db.user", user);
                password = props.getProperty("db.password", password);
                credentialSource = "Config File (" + CONFIG_FILE + ")";
            } catch (IOException e) {
                System.err.println("[WARN] Could not read " + CONFIG_FILE + ", falling back to defaults.");
            }
        }

        // 2. Environment Variables override file values (Most Secure for CI/CD & Production)
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        if (envUrl != null && !envUrl.trim().isEmpty()) {
            url = envUrl.trim();
            credentialSource = "Environment Variables";
        }
        if (envUser != null && !envUser.trim().isEmpty()) {
            user = envUser.trim();
            credentialSource = "Environment Variables";
        }
        if (envPass != null) {
            password = envPass;
            credentialSource = "Environment Variables";
        }
    }

    /**
     * Saves updated database credentials into db.properties.
     * Note: Does not override active environment variables.
     */
    public static boolean saveConfiguration(String newUrl, String newUser, String newPassword) {
        Properties props = new Properties();
        props.setProperty("db.url", newUrl);
        props.setProperty("db.user", newUser);
        props.setProperty("db.password", newPassword);

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Student Management System - Local Database Settings");
            url = newUrl;
            user = newUser;
            password = newPassword;
            credentialSource = "Config File (" + CONFIG_FILE + ")";
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save database configuration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtains a live connection to the MySQL database.
     * 
     * @return Active Connection instance
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Verifies whether the database connection is currently active.
     * 
     * @return true if connected successfully, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Initializes the database schema and table if they do not exist.
     */
    public static boolean initializeDatabase() {
        // Attempt database creation at server level
        String baseServerUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection serverConn = DriverManager.getConnection(baseServerUrl, user, password);
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS student_db;");
        } catch (SQLException ignored) {
            // Server-level root command might be restricted; continue to table creation
        }

        // Create the students table inside student_db
        String createTableSQL = "CREATE TABLE IF NOT EXISTS students ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "department VARCHAR(100) NOT NULL, "
                + "email VARCHAR(150) NOT NULL UNIQUE, "
                + "marks DECIMAL(5, 2) NOT NULL"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Accessors
    public static String getUrl() { return url; }
    public static String getUser() { return user; }
    public static String getCredentialSource() { return credentialSource; }
    public static String getPasswordMasked() {
        return (password == null || password.isEmpty()) ? "(Not Set)" : "********";
    }
}
