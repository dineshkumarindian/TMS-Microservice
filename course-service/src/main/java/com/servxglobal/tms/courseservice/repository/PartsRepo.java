package com.servxglobal.tms.courseservice.repository;

import com.servxglobal.tms.courseservice.model.Parts;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartsRepo extends MongoRepository<Parts, Long> {

     @Query("{is_deleted :  false}")
    List<Parts> findAllParts();

    @Query("{'session_id': ?0}")  // to get parts by session id
     List<Parts> findPartsBySession_id(Long id);

    @Query("{'topicDetails.id': ?0}") // to get parts by topic id
    List<Parts> findPartsByTopic_Id(Long id);

    @Query("{'max_level' : {$lte: ?1}, 'is_deleted': false, 'course_id': ?0}")
    List<Parts> getPartsByCourseIdAndLevel(Long courseId, int level);

    @Query("{'course_id': ?0}")
    List<Parts> findPartsByCourseId(Long id);
}
