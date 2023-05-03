package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.example.dao.CoursesDao;
import org.example.dao.EnrollmentsDao;
import org.example.dao.StudentsDao;
import org.example.model.Course;
import org.example.model.Enrollment;
import org.example.model.Student;
import org.example.model.StudentSchedule;
import org.example.util.MessageGenerator;
import org.example.util.Writer;
import org.example.util.Role;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.*;
import static org.example.util.Writer.TEXT_CONTENT;
import static org.example.util.Role.COURSE;
import static org.example.util.Role.STUDENT;


@WebServlet(urlPatterns = "/enrollment")
public class EnrollmentsController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(EnrollmentsController.class.getName());
    private CoursesDao coursesDao;
    private EnrollmentsDao enrollmentsDao;
    private StudentsDao studentsDao;
    private Gson gson;

    public void init() {
        enrollmentsDao = new EnrollmentsDao();
        coursesDao = new CoursesDao();
        studentsDao = new StudentsDao();
        gson = new GsonBuilder().create();
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        try {
            List<Enrollment> enrollmentList = enrollmentsDao.getAll();
            String enrollmentsJson = gson.toJson(enrollmentList);
            LOGGER.info("Get all enrollments: " + enrollmentsJson);

            Writer.write(response, SC_OK,
                    Writer.JSON_CONTENT, enrollmentsJson);

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Enrollment enrollment = gson.fromJson(reader, Enrollment.class);
        int student_id = enrollment.getStudent_id();
        int course_id = enrollment.getCourse_id();
        try {
            Course course = coursesDao.getCourseById(course_id);
            Student student = studentsDao.getStudentById(student_id);

            if (student.isEmpty()) {
                LOGGER.error(MessageGenerator.notFound(STUDENT, student_id));
                Writer.write(response, SC_NOT_FOUND,
                        TEXT_CONTENT,
                        MessageGenerator.notFound(STUDENT, student_id));

            }
            if (course.isEmpty()) {
                LOGGER.error(MessageGenerator.notFound(COURSE, course_id));
                Writer.write(response, SC_NOT_FOUND,
                        TEXT_CONTENT,
                        MessageGenerator.notFound(COURSE, course_id));

            } else if (isCourseFull(course_id)) {
                LOGGER.error(MessageGenerator.courseFull(course_id));
                Writer.write(response, SC_BAD_REQUEST, TEXT_CONTENT, MessageGenerator.courseFull(course_id));


            } else if (!isStudentAvailable(student_id, course.getStart_time(), course.getEnd_time())) {
                Time start = course.getStart_time();
                Time endTime = course.getEnd_time();
                LOGGER.error(MessageGenerator.notAvailable(STUDENT, start, endTime));
                Writer.write(response, SC_BAD_REQUEST, TEXT_CONTENT, MessageGenerator.notAvailable(STUDENT, start, endTime));

            } else {
                enrollmentsDao.registerStudentToCourse(enrollment);
                LOGGER.info(MessageGenerator.registeredSuccessfully(student_id, course_id));
                Writer.write(response, SC_OK, TEXT_CONTENT,
                        MessageGenerator.registeredSuccessfully(student_id, course_id));
            }

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        BufferedReader reader = request.getReader();
        Enrollment enrollment = gson.fromJson(reader, Enrollment.class);

        try {
            Student student = studentsDao.getStudentById(enrollment.getStudent_id());
            Course course = coursesDao.getCourseById(enrollment.getCourse_id());
            if (student.isEmpty()) {
                LOGGER.error(MessageGenerator.notFound(STUDENT, enrollment.getStudent_id()));
                Writer.write(response, SC_NOT_FOUND,
                        TEXT_CONTENT,
                        MessageGenerator.notFound(STUDENT, enrollment.getStudent_id()));

            }
            if (course.isEmpty()) {
                LOGGER.error(MessageGenerator.notFound(COURSE, enrollment.getCourse_id()));
                Writer.write(response, SC_NOT_FOUND,
                        TEXT_CONTENT,
                        MessageGenerator.notFound(COURSE, enrollment.getCourse_id()));
            } else {
                enrollmentsDao.delete(enrollment);
                LOGGER.info(MessageGenerator.deletedSuccessfully(Role.ENROLLMENT, enrollment.getStudent_id(), enrollment.getCourse_id()));
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean isCourseFull(int course_id) throws SQLException {
        int numberOfStudents = enrollmentsDao.getNumberOfStudentsInCourse(course_id);
        int courseMaxSize = coursesDao.getCourseMaxSize(course_id);
        return numberOfStudents == courseMaxSize;

    }

    private boolean isStudentAvailable(int student_id, Time start_time, Time end_time) throws SQLException {
        List<StudentSchedule> studentSchedule = coursesDao.getStudentSchedule(student_id);
        for (StudentSchedule schedule : studentSchedule) {
            boolean isEqualsStartTime = start_time.compareTo(schedule.getStartTime()) == 0;
            boolean isEqualsEndTime = end_time.compareTo(schedule.getEndTime()) == 0;
            if (isEqualsEndTime || isEqualsStartTime) {
                return false;
            }
        }
        return true;
    }


}
