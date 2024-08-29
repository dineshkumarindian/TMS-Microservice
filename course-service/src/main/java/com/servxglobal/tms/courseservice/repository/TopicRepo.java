package com.servxglobal.tms.courseservice.repository;

import com.servxglobal.tms.courseservice.model.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TopicRepo  extends MongoRepository<Topic, Long>{

    /* soft delete query */
    @Query("{ 'is_deleted' : false }")
    List<Topic> findAllTopic();

}
