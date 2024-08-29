package com.servxglobal.tms.userservice.repository;

import com.servxglobal.tms.userservice.model.Trainee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraineeRepo extends MongoRepository<Trainee, Long> {
	Optional<Trainee> findByEmail(String email);
	boolean existsByEmail(String email);
}
