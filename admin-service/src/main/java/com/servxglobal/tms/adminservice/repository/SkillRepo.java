package com.servxglobal.tms.adminservice.repository;

import com.servxglobal.tms.adminservice.model.Skill;
import jdk.dynalink.linker.LinkerServices;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepo extends MongoRepository<Skill, Long> {

    @Query("{ 'is_deleted' :  false }")
    List<Skill> getActiveSkills();

    @Query(" { 'is_deleted' :  true }")
    List<Skill> getInactiveSkills();
}
