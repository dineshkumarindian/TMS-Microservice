package com.servxglobal.tms.batchservice.repository;

import com.servxglobal.tms.batchservice.model.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ScheduleRepo extends MongoRepository<Schedule, Long> {

    @Query("{ 'is_deleted' : false }")
    List<Schedule> findActiveSchedule();
}