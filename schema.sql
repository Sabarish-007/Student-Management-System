-- ============================================================================
-- Student Management System Database Script
-- File: schema.sql
-- ============================================================================

-- Step 1: Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS student_db;

-- Step 2: Switch to the student_db database
USE student_db;

-- Step 3: Create the students table with the specified constraints
CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    marks DECIMAL(5, 2) NOT NULL
);

-- ============================================================================
-- Optional: Initial Seed Records for Testing
-- ============================================================================
INSERT IGNORE INTO students (id, name, department, email, marks) VALUES
(1, 'Aarav Sharma', 'Computer Science', 'aarav.sharma@example.com', 88.50),
(2, 'Diya Patel', 'Electronics', 'diya.patel@example.com', 92.00),
(3, 'Rohan Verma', 'Mechanical', 'rohan.verma@example.com', 76.25),
(4, 'Sneha Iyer', 'Information Technology', 'sneha.iyer@example.com', 95.00),
(5, 'Vikram Singh', 'Civil Engineering', 'vikram.singh@example.com', 81.75);

-- Verify table creation and view sample records:
-- SELECT * FROM students;
