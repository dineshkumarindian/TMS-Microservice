package com.servxglobal.tms.userservice.repository;

import com.servxglobal.tms.userservice.model.Trainer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepo extends MongoRepository<Trainer, Long> {
	Optional<Trainer> findByUserEmail(String email);
	boolean existsByUserEmail(String email);

	// Query to get active trainers
	@Query("{ 'is_deleted' : false, 'isActive': true }")
	List<Trainer> findActiveTrainers();

	//Query to get inactive trainers
	@Query("{'is_deleted':  false, 'isActive':  false}")
	List<Trainer> findInactiveTrainers();


}
