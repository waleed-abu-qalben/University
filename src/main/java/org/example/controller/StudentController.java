package org.example.controller;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.example.customGson.CustomGson;
import org.example.dao.CoursesDao;
import org.example.dao.StudentsDao;
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
import java.util.List;

import static javax.servlet.http.HttpServletResponse.*;
import static org.example.util.Writer.JSON_CONTENT;
import static org.example.util.Writer.TEXT_CONTENT;
import static org.example.util.Role.STUDENT;

@WebServlet(urlPatterns = "/student/*")
public class StudentController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(StudentController.class.getName());
    private StudentsDao studentsDao;
    private CoursesDao coursesDao;
    private Gson gson;

    public void init() {
        studentsDao = new StudentsDao();
        coursesDao = new CoursesDao();
        gson = CustomGson.getGson();
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) {
        String pathInfo = request.getPathInfo();

        if (pathInfo.equals("/getAll")) {
            getAllStudents(response);
        } else if (pathInfo.equals("/showSchedule")) {
            try {
                showStudentSchedule(request, response);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Student student = gson.fromJson(reader, Student.class);

        try {
            studentsDao.add(student);
            LOGGER.info(MessageGenerator.insertedSuccessfully(STUDENT, student.toString()));
            Writer.write(response, SC_OK, TEXT_CONTENT,
                    MessageGenerator.insertedSuccessfully(STUDENT, student.toString()));
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Student student = gson.fromJson(reader, Student.class);
        try {
            studentsDao.update(student);
            LOGGER.info(MessageGenerator.updatedSuccessfully(STUDENT, student.toString()));
            LOGGER.info(MessageGenerator.insertedSuccessfully(STUDENT, student.toString()));
            Writer.write(response, SC_OK, TEXT_CONTENT,
                    MessageGenerator.updatedSuccessfully(STUDENT, student.toString()));

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParameter = request.getParameter("id");
        if (idParameter == null) {
            LOGGER.error(MessageGenerator.missingIdParameter(STUDENT));
            Writer.write(response, SC_BAD_REQUEST, TEXT_CONTENT, MessageGenerator.missingIdParameter(STUDENT));

        } else {
            int student_id = Integer.parseInt(idParameter);
            try {
                Student student = studentsDao.getStudentById(student_id);

                if (student.isEmpty()) {
                    LOGGER.error(MessageGenerator.notFound(Role.STUDENT, student_id));
                    Writer.write(response, SC_NOT_FOUND,
                            TEXT_CONTENT,
                            MessageGenerator.notFound(Role.STUDENT, student_id));
                } else {
                    studentsDao.delete(student_id);
                    LOGGER.info(MessageGenerator.deletedSuccessfully(STUDENT, student_id));
                    Writer.write(response, SC_OK, TEXT_CONTENT, MessageGenerator.deletedSuccessfully(STUDENT, student_id));

                }
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }


    }

    private void getAllStudents(HttpServletResponse response) {
        try {
            List<Student> studentList = studentsDao.getAll();
            String studentsJson = gson.toJson(studentList);
            LOGGER.info("Gel all students: " + studentsJson);
            Writer.write(response, SC_OK, JSON_CONTENT, studentsJson);

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showStudentSchedule(HttpServletRequest request,
                                     HttpServletResponse response) throws SQLException, IOException {
        String idParameter = request.getParameter("id");
        if (idParameter == null) {
            LOGGER.error("Missing student (id) parameter");
            Writer.write(response, SC_BAD_REQUEST, TEXT_CONTENT, MessageGenerator.missingIdParameter(STUDENT));

        } else {

            int stdId = Integer.parseInt(idParameter);
            try {
                List<StudentSchedule> studentSchedules = coursesDao.getStudentSchedule(stdId);
                String json = gson.toJson(studentSchedules);
                String msg = "Show student schedule with id " + stdId + "-> " + json;
                LOGGER.info(msg);
                Writer.write(response, SC_OK, JSON_CONTENT, json);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);

            }

        }
    }


}
