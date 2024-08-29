package com.servxglobal.tms.batchservice.repository;

import com.servxglobal.tms.batchservice.model.Batch;
import com.servxglobal.tms.batchservice.model.CourseCategoryDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepo extends MongoRepository<Batch, Long> {
    @Query("{ 'is_deleted' : false,'batch_status': 'Active' }")
    List<Batch> findAllActiveBatches();

    @Query("{'is_deleted' : false ,'batch_status': 'Complete'}")
    List<Batch> findAllCompletedBatches();

    @Query( value = "{'is_deleted' :  false}", fields = "{'batch_name': 1}")
    List<?> findSpecificBatchNameDetails();

    @Query( value = "{'_id': ?0 }",fields = "{'trainerDetailsWithBranch': 1 }")
    Optional<Batch> findTrainersByBatchId(Long id);

    @Query( value = "{'_id': ?0 }",fields = "{'traineeDetailsWithBranch': 1 }")
    Optional<Batch> findTraineesByBatchId(Long id);

    /**
     * Retrieves a list of Batch objects based on a list of batch IDs.
     *
     * @param  batchIds  a list of batch IDs to search for
     * @return           a list of Batch objects matching the given batch IDs
     */
    List<Batch> findByIdIn(List<Long> batchIds);

    /**
     * Retrieves a list of batches that contain the specified trainer ID.
     *
     * @param  trainerId  the ID of the trainer to search for in the batches
     * @return            a list of batches that contain the specified trainer ID
     */
    @Query("{ 'trainersIds': ?0,'is_deleted' : false}")
    List<Batch> findByTrainersIds(Long trainerId);

    /**
     * Finds batches that contain a specific trainee ID.
     *
     * @param  traineeId  the ID of the trainee to search for
     * @return            a list of batches that contain the trainee ID
     */
    @Query("{ 'traineesIds': ?0,'is_deleted' : false}")
    List<Batch> findByTraineesIds(Long traineeId);




}
