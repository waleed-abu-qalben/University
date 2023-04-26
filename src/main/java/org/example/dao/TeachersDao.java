package org.example.dao;

import org.example.config.UniversityDataSource;
import org.example.model.Teacher;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeachersDao {
    private DataSource dataSource;
    private final String GET_ALL_TEACHERS_QUERY = "SELECT * FROM teachers";
    private final String INSERT_TEACHER_QUERY = "INSERT INTO teachers (name, email) VALUES (?, ?)";
    private final String UPDATE_TEACHER_QUERY = "UPDATE teachers SET name=?, email=? WHERE id=?";
    private final String DELETE_TEACHER_QUERY = "DELETE FROM teachers WHERE id=?";

    public TeachersDao() {
        try {
            dataSource = UniversityDataSource.getDataSource();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public List<Teacher> getAll () throws SQLException {
        List<Teacher> teacherList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareCall(GET_ALL_TEACHERS_QUERY);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Teacher teacher = new Teacher();
                teacher.setId(rs.getInt("id"));
                teacher.setName(rs.getString("name"));
                teacher.setEmail(rs.getString("email"));
                teacherList.add(teacher);
            }

        }
        return teacherList;

    }

    public void add(Teacher teacher) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {

            PreparedStatement statement = conn.prepareStatement(INSERT_TEACHER_QUERY, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, teacher.getName());
            statement.setString(2, teacher.getEmail());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating teacher failed, no rows affected.");
            }
        }
    }

    public void update(Teacher teacher) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(UPDATE_TEACHER_QUERY);
            statement.setString(1, teacher.getName());
            statement.setString(2, teacher.getEmail());
            statement.setInt(3, teacher.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating teacher failed, no rows affected.");
            }

        }
    }
    public void delete(int id) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(DELETE_TEACHER_QUERY);
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting teacher failed, no rows affected.");
            }
        }
    }
    public boolean  isExist (int id) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT id FROM teacher WHERE id =?;");
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if(rs.wasNull()) {
                return false;
            }
        }
        return true;
    }



}
