package org.example.dao;

import org.apache.log4j.Logger;
import org.example.config.ConnectionManager;
import org.example.model.Enrollment;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentsDao {
    private static final Logger LOGGER = Logger.getLogger(EnrollmentsDao.class.getName());
    private final String REGISTER_STUDENT_COURSE_QUERY = "INSERT INTO enrollments (student_id, course_id) VALUES (? , ?)";
    private final String GET_ALL_ENROLLMENTS_QUERY = "SELECT * FROM enrollments;";
    private final String DELETE_ENROLLMENT_QUERY = "DELETE FROM enrollments WHERE student_id=? AND course_id =?";
    private final String GET_NUMBER_OF_STUDENTS_IN_COURSE_QUERY = "SELECT COUNT(course_id) count FROM enrollments WHERE course_id = ?;";

    private ConnectionManager dataSource;
    public EnrollmentsDao () {
        try {
            dataSource = ConnectionManager.getInstance();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public List<Enrollment> getAll() throws SQLException {
        List <Enrollment> enrollmentList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareCall(GET_ALL_ENROLLMENTS_QUERY);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setCourse_id(rs.getInt("course_id"));
                enrollment.setStudent_id(rs.getInt("student_id"));
                enrollmentList.add(enrollment);
            }

        }
        return  enrollmentList;
    }
    public void delete(Enrollment enrollment) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            int student_id = enrollment.getStudent_id();
            int course_id = enrollment.getCourse_id();
            PreparedStatement statement = conn.prepareStatement(DELETE_ENROLLMENT_QUERY);
            statement.setInt(1, student_id);
            statement.setInt(2, course_id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting enrollment failed, no rows affected.");
            }
        }
    }
    public void registerStudentToCourse (Enrollment enrollment) throws SQLException {

        try (Connection conn = dataSource.getConnection()) {
            int std_id = enrollment.getStudent_id();
            int curs_id = enrollment.getCourse_id();
            PreparedStatement statement = conn.prepareStatement(REGISTER_STUDENT_COURSE_QUERY);
            statement.setInt(1, std_id);
            statement.setInt(2, curs_id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting enrollment failed, no rows affected.");
            }
        }
    }

    /*
      To know the number of students registered in a particular course.
    */
    public int getNumberOfStudentsInCourse(int course_id) throws SQLException {

        try (Connection conn = dataSource.getConnection()) {

            PreparedStatement statement = conn.prepareStatement(GET_NUMBER_OF_STUDENTS_IN_COURSE_QUERY);
            statement.setInt(1, course_id);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getInt("count");
            } else {
                LOGGER.error("No course found with id: "+course_id);
                throw new RuntimeException("No course found with id: "+course_id);
            }

        }

    }


    }




