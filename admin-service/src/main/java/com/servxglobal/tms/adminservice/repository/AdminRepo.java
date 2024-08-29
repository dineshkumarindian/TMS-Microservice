package com.servxglobal.tms.adminservice.repository;

import com.servxglobal.tms.adminservice.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepo extends MongoRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    Boolean existsByEmail(String email);
}
