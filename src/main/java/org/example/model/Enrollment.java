package org.example.model;

public class Enrollment {
    private int student_id;
    private int course_id;

    public Enrollment() {

    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getCourse_id() {
        return course_id;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "student_id=" + student_id +
                ", course_id=" + course_id +
                '}';
    }
}
