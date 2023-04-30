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

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

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
            response.setContentType("application/json");
            LOGGER.info("Get all enrollments: "+enrollmentsJson);
            response.getWriter().write(enrollmentsJson);

        } catch (SQLException e ) {
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
                LOGGER.error("The student with ID " + student_id + " could not be found.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain");
                response.getWriter().write("The student with ID " + student_id + " could not be found.\n");

            } if(course.isEmpty()) {
                LOGGER.error("The course with ID " + course_id + " could not be found.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain");
                response.getWriter().write("The course with ID " + course_id + " could not be found.\n");

            } else if(isCourseFull(course_id)) {
                LOGGER.error("This course with id "+course_id+" is full, can't register any new student in it");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("This course is full, can't register any new student in it\n");

            } else if ( !isStudentAvailable(student_id,course.getStart_time(), course.getEnd_time()) ) {
                LOGGER.error("Student with id " +student_id+" not available in this period of time -> " +course.getStart_time()+" - "+course.getEnd_time());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Student with id " +student_id+" not available in this period of time -> " +course.getStart_time()+" - "+course.getEnd_time()+"\n");
            }
            else {
                enrollmentsDao.registerStudentToCourse(enrollment);
                LOGGER.info("Register Student with id "+student_id+" to course with id "+ course_id+" Done successful");
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain");
                response.getWriter().write("Register Student with id "+student_id+" to course with id "+ course_id+" Done successful\n");
            }

        } catch (SQLException e){
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
            if(student.isEmpty()) {
                LOGGER.error("The student with ID " + student.getId() + " could not be found.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain");
                response.getWriter().write("The student with ID " + student.getId() + " could not be found.\n");
            }
            if (course.isEmpty()) {
                LOGGER.error("The course with ID " + course.getId() + " could not be found.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain");
                response.getWriter().write("The course with ID " + course.getId() + " could not be found.\n");
            }

            else {
                enrollmentsDao.delete(enrollment);
                LOGGER.info("Delete enrollment: student_id: " + enrollment.getStudent_id() +
                        " course_id: " + enrollment.getCourse_id());
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean isCourseFull(int course_id) throws SQLException {
        int numberOfStudents = enrollmentsDao.getNumberOfStudentsInCourse(course_id);
        int courseMaxSize =  coursesDao.getCourseMaxSize(course_id);
        return numberOfStudents == courseMaxSize;

    }

    private boolean isStudentAvailable(int student_id, Time start_time, Time end_time) throws SQLException {
       List<StudentSchedule> studentSchedule = coursesDao.getStudentSchedule(student_id);
       for (StudentSchedule schedule : studentSchedule) {
            boolean isEqualsStartTime = start_time.compareTo(schedule.getStartTime()) == 0;
            boolean isEqualsEndTime = end_time.compareTo(schedule.getEndTime()) == 0;
            if( isEqualsEndTime || isEqualsStartTime ) {
                return false;
            }
       }
       return true;
    }


}
