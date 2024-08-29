package com.servxglobal.tms.userservice.repository;

import com.servxglobal.tms.userservice.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AdminRepo extends MongoRepository<Admin, Long> {
	Optional<Admin> findByEmail(String email);
	Boolean existsByEmail(String email);
}
