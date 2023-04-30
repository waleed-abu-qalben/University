package org.example.dao;

import org.apache.log4j.Logger;
import org.example.config.ConnectionManager;
import org.example.model.Course;
import org.example.model.StudentSchedule;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursesDao {
    private static final Logger LOGGER = Logger.getLogger(CoursesDao.class.getName());
    private ConnectionManager dataSource;
    private final String GET_ALL_COURSES_QUERY = "SELECT * FROM courses;";
    private final String INSERT_COURSE_QUERY = "INSERT INTO courses (name, teacher_id, max_size, start_time, end_time) VALUES (?, ?, ?, ?, ?);";
    private final String UPDATE_COURSE_QUERY = "UPDATE courses SET name=?," +
                                                         "teacher_id=?, max_size=?, " +
                                                          " start_time=?, end_time=? WHERE id=?";
    private final String DELETE_COURSE_QUERY = "DELETE FROM courses WHERE id=?";
    private final String GET_COURSE_MAX_SIZE = "SELECT max_size FROM courses WHERE id=?;";
    private final String GET_ALL_COURSES_BY_TEACHER_ID = "SELECT * FROM courses WHERE teacher_id =?;";
    private final String GET_COURSE_BY_ID = "SELECT * FROM courses WHERE id = ?;";
    private final String GET_STUDENT_SCHEDULE = "select c.name, c.start_time, c.end_time  from courses c" +
            " where c.id in (select e.course_id " +
            " from enrollments e where e.student_id = ?);";


    public CoursesDao () {
        try {
            dataSource = ConnectionManager.getInstance();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

    }
    public List<Course> getAll() throws SQLException {
        List<Course> courseList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareCall(GET_ALL_COURSES_QUERY);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setName(rs.getString("name"));
                course.setTeacherId(rs.getInt("teacher_id"));
                course.setMax_size(rs.getInt("max_size"));
                course.setStart_time(rs.getTime("start_time"));
                course.setEnd_time(rs.getTime("end_time"));
                courseList.add(course);
            }
        }
        return courseList;
    }

    public void add(Course course) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(INSERT_COURSE_QUERY, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, course.getName());
            statement.setInt(2, course.getTeacherId());
            statement.setInt(3, course.getMax_size());
            statement.setTime(4, course.getStart_time());
            statement.setTime(5, course.getEnd_time());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating course failed, no rows affected.");
            }
        }
    }

    public void update(Course course) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {

            PreparedStatement statement = conn.prepareStatement(UPDATE_COURSE_QUERY);
            statement.setString(1, course.getName());
            statement.setInt(2, course.getTeacherId());
            statement.setInt(3, course.getMax_size());
            statement.setTime(4, course.getStart_time());
            statement.setTime(5, course.getEnd_time());
            statement.setInt(6, course.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating course failed, no rows affected.");
            }


        }
    }
    public void delete(int id) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(DELETE_COURSE_QUERY);
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting course failed, no rows affected.");
            }
        }
    }

    public int getCourseMaxSize(int course_id) throws SQLException {
        Course course = getCourseById(course_id);
        return course.getMax_size();
    }

    public List<Course> getAllCoursesByTeacherId(int teacher_id) throws SQLException {
        List<Course> courseList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareCall(GET_ALL_COURSES_BY_TEACHER_ID);
            statement.setInt(1, teacher_id);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setName(rs.getString("name"));
                course.setTeacherId(rs.getInt("teacher_id"));
                course.setMax_size(rs.getInt("max_size"));
                course.setStart_time(rs.getTime("start_time"));
                course.setEnd_time(rs.getTime("end_time"));
                courseList.add(course);
            }
        }
        return courseList;
    }

    public Course getCourseById(int course_id) throws SQLException {
        Course course = new Course();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareCall(GET_COURSE_BY_ID);
            statement.setInt(1, course_id);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                course.setId(rs.getInt("id"));
                course.setName(rs.getString("name"));
                course.setTeacherId(rs.getInt("teacher_id"));
                course.setMax_size(rs.getInt("max_size"));
                course.setStart_time(rs.getTime("start_time"));
                course.setEnd_time(rs.getTime("end_time"));
            } else {
                LOGGER.error("No course found with id: "+course_id);
            }

        }
        return course;
    }

    public List<StudentSchedule> getStudentSchedule (int student_id) throws SQLException {
        List<StudentSchedule> studentSchedule = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareCall(GET_STUDENT_SCHEDULE);
            statement.setInt(1, student_id);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                StudentSchedule schedule = new StudentSchedule();
                schedule.setCourseName(rs.getString("name"));
                schedule.setStartTime(rs.getTime("start_time"));
                schedule.setEndTime(rs.getTime("end_time"));
                studentSchedule.add(schedule);
            }
        }
        return studentSchedule;
    }

}

