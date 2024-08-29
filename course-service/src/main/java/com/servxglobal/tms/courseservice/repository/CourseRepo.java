package com.servxglobal.tms.courseservice.repository;

import com.servxglobal.tms.courseservice.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CourseRepo extends MongoRepository<Course, Long> {
    /* get all data where soft delete not performed query */
    @Query("{ 'is_deleted' : false }")
    List<Course> findAllActiveCourses();

    @Query( value = "{'is_deleted' :  false}", fields = "{'course_name': 1}")
    List<?> getAllCourseName();
}
