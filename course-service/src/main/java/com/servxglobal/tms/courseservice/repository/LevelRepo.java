package com.servxglobal.tms.courseservice.repository;

import com.servxglobal.tms.courseservice.model.Level;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelRepo extends MongoRepository<Level, Long> {

    /* soft delete query */
    @Query("{ 'is_deleted' : false }")
    List<Level> findAllLevel();

    @Query("{'course_id': ?0, 'is_deleted': false}")
    List<Level> getLevelDetailsByCourseId(Long id);

    @Query("{ 'level' :  ?1, 'is_deleted': false, 'course_id': ?0}")
     List<Level>  findByLevel(Long courseId, int maxLevel);

}
