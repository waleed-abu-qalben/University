package org.example.util;

import java.sql.Time;

public class MessageGenerator {


    public static String notAvailable (String role, Time start_time, Time end_time) {
       return role+" not available in this period of time -> " +start_time+" - "+end_time+"\n";
    }
    public static String invalidTime (Time start_time, Time end_time) {
        return "Invalid Time, start time: "+start_time+"  must be less than end time: " +end_time+
                " and the duration of the course must be 1 Hour\n";
    }
    public static String insertedSuccessfully(String role, String object) {
        return role+" inserted successfully "+object+"\n";
    }
    public static String updatedSuccessfully(String role, String object) {
        return role+" updated successfully "+object+"\n";
    }

    public static String deletedSuccessfully(String role, int id) {
        return role+" with id:"+id+" deleted successfully\n";
    }
    public static String deletedSuccessfully(String role, int id1, int id2) {
        return role+" with id:"+id1+" and id: "+id2+" deleted successfully\n";
    }
    public static String missingIdParameter(String role) {
        return "Missing "+ role+" (id) parameter\n";
    }
    public static String notFound(String role, int id) {
        return "The "+role+" with ID " + id + " not found.\n";
    }

    public static String courseFull(int course_id) {
        return "This course with id "+course_id+" is full, can't register any new student in it\n";
    }
    public static String registeredSuccessfully(int student_id, int course_id) {
        return "Register Student with id "+student_id+" to course with id "+ course_id+" Done successful\n";
    }



}
