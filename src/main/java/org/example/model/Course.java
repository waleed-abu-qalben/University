package org.example.model;

import java.sql.Time;

public class Course {
   private int id;
   private String name;
   private int teacherId;
   private int max_size;
   private Time start_time;
   private Time end_time;

   public Course() {
   }



   public Time getStart_time() {
      return start_time;
   }

   public void setStart_time(Time start_time) {
      this.start_time = start_time;
   }

   public Time getEnd_time() {
      return end_time;
   }

   public void setEnd_time(Time end_time) {
      this.end_time = end_time;
   }

   public int getMax_size() {
      return max_size;
   }

   public void setMax_size(int max_size) {
      this.max_size = max_size;
   }

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getTeacherId() {
      return teacherId;
   }

   public void setTeacherId(int teacherId) {
      this.teacherId = teacherId;
   }

   public boolean isEmpty() {
      return id == 0 && teacherId == 0;
   }
   @Override
   public String toString() {
      return "Course{" +
              "id=" + id +
              ", name='" + name + '\'' +
              ", teacherId=" + teacherId +
              ", max_size=" + max_size +
              ", start_time=" + start_time +
              ", end_time=" + end_time +
              '}';
   }
}
