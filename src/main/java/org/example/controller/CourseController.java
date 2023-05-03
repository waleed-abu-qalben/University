package org.example.controller;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.example.customGson.CustomGson;
import org.example.dao.CoursesDao;
import org.example.model.Course;
import org.example.util.MessageGenerator;
import org.example.util.Writer;

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

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.example.util.Writer.JSON_CONTENT;
import static org.example.util.Writer.TEXT_CONTENT;
import static org.example.util.Role.COURSE;
import static org.example.util.Role.TEACHER;

@WebServlet(urlPatterns = "/course")
public class CourseController extends HttpServlet {
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
            LOGGER.info("Get all courses: " + coursesJson);
            Writer.write(response, SC_OK,
                    JSON_CONTENT, coursesJson);

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Course course = gson.fromJson(reader, Course.class);

        Time start_time = course.getStart_time();
        Time end_time = course.getEnd_time();
        int teacher_id = course.getTeacherId();
        if (!isValidTime(start_time, end_time)) {

            LOGGER.error(MessageGenerator.invalidTime(start_time, end_time));
            Writer.write(response, SC_BAD_REQUEST,
                    TEXT_CONTENT, MessageGenerator.invalidTime(start_time, end_time));


        } else if (!isTeacherAvailable(teacher_id, start_time, end_time)) {
            LOGGER.error(MessageGenerator.notAvailable(TEACHER, start_time, end_time));
            Writer.write(response, SC_BAD_REQUEST,
                    TEXT_CONTENT,
                    MessageGenerator.notAvailable(TEACHER, start_time, end_time));

        } else {
            try {
                coursesDao.add(course);
                String msg = MessageGenerator.insertedSuccessfully(COURSE, course.toString());
                LOGGER.info(msg);
                Writer.write(response, SC_OK, TEXT_CONTENT, msg);

            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException();
            }
        }
    }
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Course course = gson.fromJson(reader, Course.class);

        Time start_time = course.getStart_time();
        Time end_time = course.getEnd_time();
        int teacher_id = course.getTeacherId();
        if (!isValidTime(start_time, end_time)) {
            LOGGER.error(MessageGenerator.invalidTime(start_time, end_time));

            Writer.write(response, SC_BAD_REQUEST,
                    TEXT_CONTENT,
                    MessageGenerator.invalidTime(start_time, end_time));

        }
        if (!isTeacherAvailable(teacher_id, start_time, end_time)) {
            LOGGER.error(MessageGenerator.notAvailable(TEACHER, start_time, end_time));

            Writer.write(response, SC_BAD_REQUEST,
                    TEXT_CONTENT,
                    MessageGenerator.notAvailable(TEACHER, start_time, end_time));

        } else {

            try {
                coursesDao.update(course);
                LOGGER.info(MessageGenerator.updatedSuccessfully(COURSE, course.toString()));
                Writer.write(response, SC_OK, TEXT_CONTENT,
                        MessageGenerator.updatedSuccessfully(COURSE, course.toString()));

            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
    }
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String idParameter = request.getParameter("id");
        if (idParameter == null) {
            LOGGER.error(MessageGenerator.missingIdParameter(COURSE));
            Writer.write(response, SC_BAD_REQUEST,
                    TEXT_CONTENT,
                    MessageGenerator.missingIdParameter(COURSE));
        } else {
            int courseId = Integer.parseInt(idParameter);
            try {
                coursesDao.delete(courseId);
                LOGGER.info(MessageGenerator.deletedSuccessfully(COURSE, courseId));
                Writer.write(response, SC_BAD_REQUEST,
                        TEXT_CONTENT,
                        MessageGenerator.deletedSuccessfully(COURSE, courseId));

            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isValidTime(Time start_time, Time end_time) {
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

    private boolean isTeacherAvailable(int teacher_id, Time start_time, Time end_time) {
        try {
            List<Course> courses = coursesDao.getAllCoursesByTeacherId(teacher_id);
            for (Course course : courses) {
                boolean startTimeEquals = start_time.compareTo(course.getStart_time()) == 0;
                boolean endTimeEquals = end_time.compareTo(course.getEnd_time()) == 0;
                if (startTimeEquals || endTimeEquals) {
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