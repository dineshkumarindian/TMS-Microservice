package com.servxglobal.tms.courseservice.repository;

import com.servxglobal.tms.courseservice.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SessionRepo extends MongoRepository<Session,Long> {

    /* soft delete query */
    @Query("{ 'is_deleted' : false }")
    List<Session> findAllSession();

    @Query(value = "{'level_list': {$elemMatch: {$eq: ?0}}}") // Query for getting all session based on level id inside the array
    List<Session> findByIDInArray(Long id);

    @Query("{'course_id': ?0}")
    List<Session> getSessionByCourseId(Long id);
}
