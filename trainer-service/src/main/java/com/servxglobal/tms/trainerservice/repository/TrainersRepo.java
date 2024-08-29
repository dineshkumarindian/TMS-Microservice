package com.servxglobal.tms.trainerservice.repository;

import com.servxglobal.tms.trainerservice.model.Trainers;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TrainersRepo extends MongoRepository<Trainers, Long> {
    Optional<Trainers> findByUserEmail(String email);
    boolean existsByUserEmail(String email);

    // Query to get active trainers
    @Query("{'is_deleted' : false, 'isActive': true}")
    List<Trainers> findActiveTrainers();

    //Query to get inactive trainers
    @Query("{'is_deleted':  false, 'isActive':  false}")
    List<Trainers> findInactiveTrainers();

    @Query( value = "{'is_deleted' :  false, 'isActive': true }", fields = "{'firstname': 1,lastname: 1,'logo_Img': 1, 'branch': 1, 'batches': 1, 'userEmail': 1, 'contact_number': 1}")
    List<?> findSpecificIdNameTrainerDetails();

    @Query( value = "{'is_deleted' :  false}", fields = "{'userEmail': 1}")
    List<?> findSpecificUserMailDetails();

//    @Query( value = "{'_id': ?0 }",fields = "{'firstname': 1,lastname: 1,'logo_Img': 1, 'branch': 1 }" )
    @Query( value = "{'_id': ?0 }",fields = "{'firstname': 1,lastname: 1,'logo_Img': 1, 'branch': 1, 'userEmail': 1, 'contact_number': 1 }" )
    Optional<Trainers> findByTrainerDetailsId(Long id);

    List<Trainers> findByIdIn(List<Long> traineeIds);

    @Query("{'batches.batch_id': ?0}")
    List<Trainers> findByBatchIdInBatches(Long batchId);

    // To get trainer image to show in batch dialog box
    @Query(value = "{'_id': ?0, 'is_deleted': false }", fields = "{'_id':1,'logo_Img':  1}")
    Optional<Trainers> getImages(Long id);

    // To get trainer details(name, email, contact number, branch and logo image) to show in batch dialog box by id
    @Query(value = "{'_id':  ?0, 'is_deleted':  false}", fields = "{'_id': 1, 'firstname': 1, 'lastname': 1, 'userEmail': 1,'contact_number': 1,'branch': 1,'logo_Img': 1}")
    Optional<Trainers> getTrainerDetailsForBatch(Long id);
}
