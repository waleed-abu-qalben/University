package org.example.dao;

import org.apache.log4j.Logger;
import org.example.config.ConnectionManager;
import org.example.model.Student;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentsDao {
    private static final Logger LOGGER = Logger.getLogger(StudentsDao.class.getName());
    private ConnectionManager dataSource;
    private final String GET_ALL_STUDENTS_QUERY = "SELECT * FROM students";
    private final String INSERT_STUDENT_QUERY = "INSERT INTO students (name, email) VALUES (?, ?)";
    private final String UPDATE_STUDENT_QUERY = "UPDATE students SET name=?, email=? WHERE id=?";
    private final String DELETE_STUDENT_QUERY = "DELETE FROM students WHERE id=?";
    private final String GET_STUDENT_BY_ID = "SELECT * FROM students WHERE id =?";

    public StudentsDao() {
        try {
            dataSource = ConnectionManager.getInstance();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public List<Student> getAll() throws SQLException {
        List<Student> studentList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareCall(GET_ALL_STUDENTS_QUERY);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setName(rs.getString("name"));
                student.setEmail(rs.getString("email"));
                studentList.add(student);
            }

        }
        return studentList;

    }

    public void add(Student student) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {

            PreparedStatement statement = conn.prepareStatement(INSERT_STUDENT_QUERY, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, student.getName());
            statement.setString(2, student.getEmail());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating student failed, no rows affected.");
            }
        }
    }

    public void update(Student student) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(UPDATE_STUDENT_QUERY);
            statement.setString(1, student.getName());
            statement.setString(2, student.getEmail());
            statement.setInt(3, student.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating student failed, no rows affected.");
            }

        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(DELETE_STUDENT_QUERY);
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting student failed, no rows affected.");
            }
        }
    }

    public Student getStudentById(int student_id) throws SQLException {
        Student student = new Student();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareCall(GET_STUDENT_BY_ID);
            statement.setInt(1, student_id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                student.setId(rs.getInt("id"));
                student.setEmail(rs.getString("email"));
                student.setName(rs.getString("name"));
            } else {
                LOGGER.error("No Student found with id: " + student_id);
            }

        }
        return student;
    }


}

