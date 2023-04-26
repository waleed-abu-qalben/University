package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.example.dao.TeachersDao;
import org.example.model.Teacher;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = "/teacher")
public class TeacherController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(TeacherController.class.getName());
    private TeachersDao teachersDao;
    private Gson gson;
    public void init() {
        teachersDao = new TeachersDao();
        gson = new GsonBuilder().create();
    }
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        try {
            List<Teacher> teacherList = teachersDao.getAll();
            String teachersJson = gson.toJson(teacherList);
            response.setContentType("application/json");
            LOGGER.info("Get all teachers: "+teachersJson);
            response.getWriter().write(teachersJson);

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Teacher teacher = gson.fromJson(reader, Teacher.class);

        try {
            teachersDao.add(teacher);
            LOGGER.info("Add teacher: "+teacher);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Teacher teacher = gson.fromJson(reader, Teacher.class);
        try {
            teachersDao.update(teacher);
            LOGGER.info("Update teacher: "+teacher);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)  {
        String idParameter = request.getParameter("id");
        int teacherId = Integer.parseInt(idParameter);
        try {
            teachersDao.delete(teacherId);
            LOGGER.info("Delete teacher with id: "+teacherId);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }



}
