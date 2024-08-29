package com.servxglobal.tms.traineeservice.repository;

import com.servxglobal.tms.traineeservice.model.Trainee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TraineeRepo extends MongoRepository<Trainee, Long> {

    Optional<Trainee> findByEmail(String email);

    /**
     * Finds trainees by their IDs.
     *
     * @param traineeIds  a list of trainee IDs
     * @return            a list of trainees matching the given IDs
     */
    List<Trainee> findByIdIn(List<Long> traineeIds);

    boolean existsByEmail(String email);

    @Query("{'is_deleted' :  false, 'is_activated': true}")
    List<Trainee> findActiveTrainee();

    @Query("{'is_deleted' : false , 'is_activated': false }")
    List<Trainee> findDeactiveTrainee();

    @Query("{'batch_id':  ?0, 'is_deleted': false, 'payment_status' :  true}")
    List<Trainee> findByBatchId(Long id);

    @Query("{'branch' : ?0}")
    List<Trainee> findTraineeByBranch(String branch);

    // Get all details based on branch code eg:CBE or MAA
    @Query(value = "{'branchCode': ?0}", sort = "{'timestamp' : -1}")
    List<Trainee> sortResult(String branchCode);

    @Query( value = "{'is_deleted' :  false}", fields = "{'email': 1}")
    List<?> findSpecificUserMailDetails();

    //	{'is_deleted' :  false, 'is_activated': true }
    @Query( value = "{'is_deleted' :  false, 'is_activated': true }", fields = "{'firstname': 1,lastname: 1,'logo_Img': 1, 'branch': 1,  'email': 1, 'contact_number': 1  }")
    List<?> findSpecificIdNameTraineeDetails();
////	@Query("{'is_deleted' :  false, 'is_activated': true}")
    @Query("{'traineeBatch.batch_id': ?0}")
    List<Trainee> getSpecificBatchDetails(Long id);

    // To get trainee image to show in batch dialog box
    @Query(value = "{'_id': ?0, 'is_deleted': false }", fields = "{'_id':1,'logo_Img':  1}")
    Optional<Trainee> getImages(Long id);

    // To get trainee details(name, email, contact number, branch and logo image) to show in batch dialog box by id
    @Query(value = "{'_id':  ?0, 'is_deleted':  false}", fields = "{'_id': 1, 'firstname': 1, 'lastname': 1, 'email': 1,'contact_number': 1,'branch': 1,'logo_Img': 1}")
    Optional<Trainee> getTraineeDetailsForBatch(Long id);
}
