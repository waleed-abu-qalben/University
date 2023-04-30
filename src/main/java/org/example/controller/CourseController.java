package org.example.controller;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.example.customGson.CustomGson;
import org.example.dao.CoursesDao;
import org.example.model.Course;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;


@WebServlet(urlPatterns = "/course")
public class CourseController  extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CourseController.class.getName());
    private CoursesDao coursesDao;
    private Gson gson;
    public void init() {
        coursesDao = new CoursesDao();
        gson = CustomGson.getGson();
    }
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
      try {
          List<Course> courseList = coursesDao.getAll();
          String coursesJson = gson.toJson(courseList);
          response.setContentType("application/json");
          LOGGER.info("Get all courses: "+coursesJson);
          response.getWriter().write(coursesJson);

      } catch (SQLException e ) {
         LOGGER.error(e.getMessage());
          throw new RuntimeException(e.getMessage());
      }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
       BufferedReader reader = request.getReader();
        Course course = gson.fromJson(reader, Course.class);
        LOGGER.info("Course to be inserted: "+course);

        Time start_time = course.getStart_time();
        Time end_time = course.getEnd_time();
        int teacher_id = course.getTeacherId();
        if(!isValidTime(start_time, end_time)) {
            LOGGER.error("Invalid Time, (start time) must be less than (end time)" +
                    "and the duration of the course must be 1 Hour");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write("Invalid Time, (start time) must be less than (end time) " +
                                                        "and the duration of the course must be 1 Hour");



        } else if (!isTeacherAvailable(teacher_id,start_time, end_time)) {
            LOGGER.error("Teacher not available in this period of time -> " +start_time+" - "+end_time);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write("Teacher not available in this period of time -> "+start_time+" - "+end_time);

        } else {
            try {
                coursesDao.add(course);
                LOGGER.info("Insert Course successfully: " + course);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain");
                response.getWriter().write("Insert Course successfully: " + course);
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException();
            }
        }
    }


    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Course course = gson.fromJson(reader, Course.class);
        LOGGER.info("Course to be Updated: "+course);

        Time start_time = course.getStart_time();
        Time end_time = course.getEnd_time();
        int teacher_id = course.getTeacherId();
        if(!isValidTime(start_time, end_time)) {
            LOGGER.error("Invalid Time, (start time) must be less than (end time)" +
                    "and the duration of the course must be 1 Hour");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write("Invalid Time, (start time) must be less than (end time) " +
                    "and the duration of the course must be 1 Hour");



        } if (!isTeacherAvailable(teacher_id,start_time, end_time)) {
            LOGGER.error("Teacher not available in this period of time -> " +start_time+" - "+end_time);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write("Teacher not available in this period of time -> "+start_time+" - "+end_time);

        } else {

            try {
                coursesDao.update(course);
                LOGGER.info("Update course successfully: " + course);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain");
                response.getWriter().write("Update Course successfully: " + course);
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String idParameter = request.getParameter("id");
        if (idParameter == null) {
            LOGGER.error("Missing Course (id) parameter");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write("Error: Missing id parameter");
        }
        else {
            int courseId = Integer.parseInt(idParameter);
            try {
                coursesDao.delete(courseId);
                LOGGER.info("Delete course with id: " + courseId);
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }


    private boolean isValidTime (Time start_time, Time end_time) {
        return isStartTimeLessThanEndTime(start_time, end_time)
                && isValidDuration(start_time, end_time);
    }

    // To check if the (start time) less than (end time).
    private boolean isStartTimeLessThanEndTime(Time start_time, Time end_time) {
        return start_time.compareTo(end_time) < 0;
    }
    private boolean isValidDuration(Time start_time, Time end_time) {
        LocalTime start = start_time.toLocalTime();
        LocalTime end = end_time.toLocalTime();
        return Duration.between(start, end).toHours() == 1;
    }
    private boolean isTeacherAvailable(int teacher_id, Time start_time, Time end_time)  {
        try {
            List<Course> courses = coursesDao.getAllCoursesByTeacherId(teacher_id);
            for (Course course : courses) {
                boolean startTimeEquals = start_time.compareTo(course.getStart_time()) == 0;
                boolean endTimeEquals = end_time.compareTo(course.getEnd_time()) == 0;
                if (startTimeEquals || endTimeEquals){
                    return false;
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }
}
