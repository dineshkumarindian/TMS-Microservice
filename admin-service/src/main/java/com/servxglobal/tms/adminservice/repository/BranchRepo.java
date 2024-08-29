package com.servxglobal.tms.adminservice.repository;
import com.servxglobal.tms.adminservice.model.Branch;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface BranchRepo extends MongoRepository<Branch, Long> {

    @Query("{ 'is_deleted' : false}")
    List<Branch> findActiveBranches();
    @Query("{ 'is_deleted' : true}")
    List<Branch> findInactiveBranches();
}

