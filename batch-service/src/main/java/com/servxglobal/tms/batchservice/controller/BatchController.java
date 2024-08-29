package com.servxglobal.tms.batchservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servxglobal.tms.batchservice.dto.BatchTraineeDto;
//import com.servxglobal.tms.batchservice.dto.BatchTrainerDto;
import com.servxglobal.tms.batchservice.dto.ResponseDto;
import com.servxglobal.tms.batchservice.model.Batch;
import com.servxglobal.tms.batchservice.model.CourseCategoryDetails;
import com.servxglobal.tms.batchservice.model.Schedule;
import com.servxglobal.tms.batchservice.otherService.TraineeServiceClient;
//import com.servxglobal.tms.batchservice.otherService.TrainerServiceClient;
import com.servxglobal.tms.batchservice.otherService.TrainerServiceClient;
import com.servxglobal.tms.batchservice.repository.BatchRepo;
import com.servxglobal.tms.batchservice.repository.ScheduleRepo;
//import org.apache.logging.log4j.core.config.Scheduled;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

import com.servxglobal.tms.batchservice.otherService.CourseServiceClient;

@RestController
@RequestMapping("/api/batch")
//@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BatchController {
    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);
    /**
     * This function handles the "add batch" API.
     * It reads the data from request body
     * The function returns the newly added batch.
     */
    @Autowired
    private BatchRepo batchRepo;

    @Autowired
    private ScheduleRepo scheduleRepo;

    @Autowired
    private CourseServiceClient courseServiceClient;

    @Autowired
    private TraineeServiceClient traineeServiceClient;

    @Autowired
    private TrainerServiceClient trainerServiceClient;

    @PostMapping("/add-batch")
    public ResponseEntity<ResponseDto> addBatch(@RequestBody Batch batchDetails) {
        logger.info("BatchController(addBatch) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<Batch> allBatches = batchRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
            if (allBatches.isEmpty() || allBatches.size() == 0) {
                batchDetails.setId(1L);
            } else {
                batchDetails.setId(allBatches.get(0).getId() + 1);
            }
            batchDetails.setCreated_time(new Date());
            batchDetails.setModified_time(new Date());
            Batch save_data = batchRepo.save(batchDetails);
            final Long Batch_id = save_data.getId();
            response.setSuccess(true);
            response.setMessage("batch added Successful !!");
            response.setData(new Gson().toJson(save_data));
            BatchTraineeDto batchTraineeDto = new BatchTraineeDto();
            batchTraineeDto.setBatch_id(save_data.getId());
            batchTraineeDto.setBatch_name(save_data.getBatch_name());
            List<Long> dummy = new ArrayList<>();
            ResponseDto traineeData = traineeServiceClient.TraineesBatchUpdate(batchTraineeDto, save_data.getTraineesIds(), dummy);
            ResponseDto trainerData = trainerServiceClient.TrainerBatchUpdate(batchTraineeDto, save_data.getTrainersIds(), dummy);

            Batch batchIdDetails = new Batch();
            batchIdDetails = save_data;
            Long courseCategoryId =  save_data.getCourseCategoryDetails().getCourse_category_id();
            ResponseDto courseCategoryDetails = courseServiceClient.getByStreamId(courseCategoryId);
            String data = courseCategoryDetails.getData();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(data);
            // Extract details from the JSON
            String name = rootNode.get("courseDetailsWithLevel").asText();
//            ObjectMapper objectMapper1 = new ObjectMapper();
            JsonNode jsonArray = objectMapper.readTree(name);

            for (JsonNode jsonObject : jsonArray) {
                Schedule scheduleDetails = new Schedule();
                int count = 0;
                int course_id =jsonObject.get("id").asInt();
                int level_id = jsonObject.get("level_id").asInt();
                String course_name = jsonObject.get("course_name").asText();
                ResponseDto getLevelDetails = courseServiceClient.getCoursesByLevels(Long.valueOf(level_id));
                String levelData = getLevelDetails.getData();
                JsonNode levelRootNode = objectMapper.readTree(levelData);
                for (JsonNode itemNode : levelRootNode) {
                    JsonNode childrenNode = itemNode.get("children");
                    if (childrenNode != null) {
                        for (JsonNode child : childrenNode) {
//                            if(child.get("item").asText().equals(""))
                            String itemName = child.get("item").asText();
//                            System.out.println(itemName);
                            if (itemName.equals("Session")) {
                                JsonNode sessionChildrenNode = child.get("children");
//                                System.out.println(sessionChildrenNode);
                                for (JsonNode sessionChild : sessionChildrenNode) {
//                                    System.out.println("sessionChild---"+sessionChild);
                                    JsonNode topicChild = sessionChild.get("children");
                                    List topicDetails = new ArrayList<>();
                                    for (JsonNode topicChildDetails : topicChild) {
                                        topicDetails.add(topicChildDetails.get("topic"));
                                    }
//                                    List<Schedule> allSchedule = scheduleRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
//                                    if(allSchedule.isEmpty()){
//                                        scheduleDetails.setId(1L);
//                                    } else {
//                                        scheduleDetails.setId(allSchedule.get(0).getId()+1);
//                                    }

                                    scheduleDetails.setId(scheduleRepo.count() + 1);
                                    scheduleDetails.setBatch_id(batchIdDetails.getId());
                                    scheduleDetails.setBatch_name(batchIdDetails.getBatch_name());
                                    scheduleDetails.setCourse_id(Long.valueOf(course_id));
                                    scheduleDetails.setCourse_name(course_name);
                                    scheduleDetails.setCreated_time(new Date());
                                    scheduleDetails.setModified_time(new Date());
                                    scheduleDetails.setSchedule_topic_details(topicDetails.toArray());
//                                    System.out.println(scheduleDetails);
                                    logger.info("ScheduleController(updateScheduleDetails) >> save data");
                                    Schedule postSchedule = scheduleRepo.save(scheduleDetails);

//                                    count = count+1;
                                }
//                              JsonNode topicNode = child.get("topic");a
                            }
                        }
                    }
                }
//                System.out.println("count----"+count);
            }

            logger.info("BatchController(addBatch)>> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(addBatch)>> Exit");
        }
        return null;
    }

    @GetMapping("/get-all-schedule")
    public ResponseEntity<List<Schedule>> getAllSchedule() {
        ResponseDto responseDto = new ResponseDto();
        logger.info("ScheduleController(getAllSchedule) >> Entry");
        try {
            List<Schedule> scheduleList = scheduleRepo.findAll();
            logger.info("ScheduleController(getAllSchedule) >> exit");
            return new ResponseEntity<List<Schedule>>(scheduleList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in ScheduleController(getAllSchedule) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves all active batches from the database.
     *
     * @return A ResponseEntity containing a list of batch objects.
     */
    @GetMapping("/active-get-all-batches")
    public ResponseEntity<List<Batch>> getAllActiveBatches() {
        logger.info("BatchController(getAllActiveBatches) >> Entry");
        try {
            List<Batch> batchList = batchRepo.findAllActiveBatches();
            logger.info("BatchController(getAllActiveBatches)>> Exit");
            return new ResponseEntity<List<Batch>>(batchList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(getAllActiveBatches)>> Exit");
        }
        return null;
    }

    @GetMapping("/completed-get-all-batches")
    public ResponseEntity<List<Batch>> getAllCompletedBathes() {
        logger.info("BatchController(getAllActiveBatches) >> Entry");
        try {
            List<Batch> batchList = batchRepo.findAllCompletedBatches();
            logger.info("BatchController(getAllActiveBatches)>> Exit");
            return new ResponseEntity<List<Batch>>(batchList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(getAllActiveBatches)>> Exit");
        }
        return null;
    }


    /**
     * Retrieves all the batch details.
     *
     * @return ResponseEntity<List < batch>> - The response entity containing the list of batches.
     */
    @GetMapping("/get-all-batches")
    public ResponseEntity<List<Batch>> getAllBatches() {
        logger.info("BatchController(getAllBatches) >> Entry");
        try {
            List<Batch> batchList = batchRepo.findAll();
            logger.info("BatchController(getAllBatches)>> Exit");
            return new ResponseEntity<List<Batch>>(batchList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(getAllBatches)>> Exit");
        }
        return null;
    }

    @GetMapping("/get-by-batch/{id}")
    public ResponseEntity<ResponseDto> getByBatchId(@PathVariable Long id) {
        logger.info("BatchController(getByBatchId) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Batch> batchList = batchRepo.findById(id);
            logger.info("BatchController(getByBatchId)>> Exit");
            responseDto.setSuccess(true);
            responseDto.setMessage("batch got successful !!");
            responseDto.setData(new Gson().toJson(batchList.get()));
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(getByBatchId)>> Exit");
        }
        return null;
    }

    /**
     * Update the batch details by the given ID.
     *
     * @param id The ID of the batch to update.
     * @return The updated batch if successful, or null if an exception occurred.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDto> updatebatch(@PathVariable Long id, @RequestBody Batch BatchData) {
        logger.info("BatchController(updateBatch) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Batch> Batch = batchRepo.findById(id);
            Batch trainerInfo = new Batch();
            if (Batch.isPresent()) {
                List<Long> oldTraineesId = Batch.get().getTraineesIds();
                List<Long> oldTrainersId = Batch.get().getTrainersIds();
                Batch.get().setBatch_name(BatchData.getBatch_name());
                Batch.get().setTraineesIds(BatchData.getTraineesIds());
                Batch.get().setTrainersIds(BatchData.getTrainersIds());
                Batch.get().setStart_date(BatchData.getStart_date());
                Batch.get().setEnd_date(BatchData.getEnd_date());
                Batch.get().setOwnerDetails(BatchData.getOwnerDetails());
                Batch.get().setCourseCategoryDetails(BatchData.getCourseCategoryDetails());
                Batch.get().setModified_time(new Date());
                trainerInfo = batchRepo.save(Batch.get());

                BatchTraineeDto batchTraineeDto = new BatchTraineeDto();
                batchTraineeDto.setBatch_id(trainerInfo.getId());
                batchTraineeDto.setBatch_name(trainerInfo.getBatch_name());
                List<Long> missingTrainersId = oldTrainersId.stream()
                        .filter(ids -> !BatchData.getTrainersIds().contains(ids))
                        .collect(Collectors.toList());
                List<Long> missingTraineesId = oldTraineesId.stream()
                        .filter(ids -> !BatchData.getTraineesIds().contains(ids))
                        .collect(Collectors.toList());
                ResponseDto traineeData = traineeServiceClient.TraineesBatchUpdate(batchTraineeDto, trainerInfo.getTraineesIds(), missingTraineesId);
                ResponseDto trainerData = trainerServiceClient.TrainerBatchUpdate(batchTraineeDto, trainerInfo.getTrainersIds(), missingTrainersId);
                logger.info("BatchController(updateBatch)>> Exit");
                responseDto.setSuccess(true);
                responseDto.setMessage("batch updated successful !");
                responseDto.setData(String.valueOf(trainerInfo));
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                trainerInfo = null;
                logger.info("BatchController(updateBatch)>> Exit");
                responseDto.setSuccess(false);
                responseDto.setMessage("batch update failed!");
                responseDto.setData(String.valueOf(trainerInfo));
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(updateBatch)>> Exit");
            responseDto.setSuccess(false);
            responseDto.setMessage("batch update Exception occurred !");
            responseDto.setData("");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.BAD_REQUEST);

        }
//        return null;
    }

    /**
     * Soft deletes a batch by setting its "_deleted" flag to true.
     *
     * @param id The ID of the batch to delete.
     * @return The updated batch if found, or a bad request response if not.
     */
    @GetMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteBatch(@PathVariable Long id) {
        logger.info("BatchController(deleteBatch) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Batch> batchDetails = batchRepo.findById(id);
            Batch updateBatch = new Batch();
            if (batchDetails.isPresent()) {
                if (batchDetails.get().getTraineesIds().size() > 0) {
                    ResponseDto updateTrainee = traineeServiceClient.updateBatchDelete(id);
                }
                if (batchDetails.get().getTrainersIds().size() > 0) {
                    ResponseDto updateTrainer = trainerServiceClient.updateBatchDelete(id);
                }
                batchDetails.get().set_deleted(true);
                updateBatch = batchRepo.save(batchDetails.get());
                logger.info("BatchController(deleteBatch)>> Exit");
                responseDto.setSuccess(true);
                responseDto.setMessage("batch deleted successfully !!");
                responseDto.setData(new Gson().toJson(batchDetails.get()));
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                updateBatch = null;
                responseDto.setSuccess(false);
                responseDto.setMessage("Failed to activate the batches");
                responseDto.setData(null);
                logger.info("BatchController(deleteBatch) >> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);

            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in batchDetails(deleteBatch)>> Exit");
        }
        return null;
    }

    /**
     * Deletes a batch permanently by its ID.
     *
     * @param id The ID of the batch to be deleted.
     * @return A ResponseEntity with a message indicating the result of the deletion.
     */
    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<String> hardDeleteBatchById(@PathVariable Long id) {
        logger.info("BatchController(hardDeleteBatchById) >> Entry");
        try {
            Optional<Batch> batchDetails = batchRepo.findById(id);
            if (batchDetails.isPresent()) {
                batchRepo.deleteById(id);
                logger.info("BatchController(hardDeleteBatchById) >> Exit");
                return new ResponseEntity<String>("delete successfully", HttpStatus.OK);
            } else {
                logger.info("BatchController(hardDeleteBatchById) >> Exit");
                return new ResponseEntity<String>("data not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(hardDeleteBatchById)");
        }
        return null;
    }

    // To show trainers details onclick trainers in batch table by batch id
    @GetMapping("/get-trainer-details-by-batch/{id}")
    public ResponseEntity<ResponseDto> getTrainerDetailsByBatchId(@PathVariable Long id) {
        logger.info("BatchController(getTrainerDetailsByBatchId) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Batch> batchDetails = batchRepo.findTrainersByBatchId(id);
            responseDto.setSuccess(true);
            responseDto.setMessage("Batch details got successful !!");
            responseDto.setData(new Gson().toJson(batchDetails.get()));
            logger.info("BatchController(getTrainerDetailsByBatchId) >> Exit");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("BatchController(getTrainerDetailsByBatchId) >> Exit");
        }
        return null;
    }

    // To show trainees details onclick trainees in batch table by batch id
    @GetMapping("/get-trainee-details-by-batch/{id}")
    public ResponseEntity<ResponseDto> getTraineeDetailsByBatchId(@PathVariable Long id) {
        logger.info("BatchController(getTraineeDetailsByBatchId) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Batch> batchDetails = batchRepo.findTraineesByBatchId(id);
            responseDto.setSuccess(true);
            responseDto.setMessage("Batch details got successful !!");
            responseDto.setData(new Gson().toJson(batchDetails.get()));
            logger.info("BatchController(getTraineeDetailsByBatchId) >> Exit");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("BatchController(getTraineeDetailsByBatchId) >> Exit");
        }
        return null;
    }

    @GetMapping("/getByNamesBatches")
    public ResponseEntity<ResponseDto> getByNamesBatches() {
        logger.info("BatchController(getByNamesBatches) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            List<?> getTheNameBatch = batchRepo.findSpecificBatchNameDetails();
            responseDto.setSuccess(true);
            responseDto.setMessage("Batch Names added successful !!");
            responseDto.setData(new Gson().toJson(getTheNameBatch));
            logger.info("BatchController(getByNamesBatches)>> Exit");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(getByNamesBatches)>> Exit");
        }
        return null;
    }

    /**
     * Updates the batch status.
     *
     * @param id     the ID of the batch
     * @param status the new status to update to
     * @return the response entity containing the updated batch information
     */
    @GetMapping("/update-batch-status/{status}/{id}")
    public ResponseEntity<ResponseDto> updateBatchStatus(@PathVariable Long id, @PathVariable String status) {
        logger.info("BatchController(updateBatchStatus) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Batch> batchIdDetails = batchRepo.findById(id);
            Batch batchInfo = new Batch();
            if (batchIdDetails.isPresent()) {
                if (batchIdDetails.get().getTraineesIds().size() > 0) {
                    ResponseDto updateTrainee = traineeServiceClient.updateBatchStatus(id, status);
                }
                if (batchIdDetails.get().getTrainersIds().size() > 0) {
                    ResponseDto updateTrainer = trainerServiceClient.updateBatchStatus(id, status);
                }
                batchIdDetails.get().set_completed(true);
                batchIdDetails.get().setBatch_status(status);
                batchInfo = batchRepo.save(batchIdDetails.get());
                response.setSuccess(true);
                response.setMessage("batch status updated successfully");
                response.setData(new Gson().toJson(batchInfo));
                logger.info("BatchController(updateBatchStatus) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                response.setSuccess(false);
                response.setMessage("Failed to update the batches status");
                response.setData(null);
                logger.info("BatchController(updateBatchStatus) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(updateBatchStatus)>> Exit");
        }
        return null;
    }

    @GetMapping("/activate-batches/{id}")
    public ResponseEntity<ResponseDto> updateActivateBatches(@PathVariable Long id) {
        logger.info("BatchController(updateActivateBatches) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Batch> batchIdDetails = batchRepo.findById(id);
            Batch batchInfo = new Batch();
            if (batchIdDetails.isPresent()) {
                batchIdDetails.get().set_completed(false);
                batchInfo = batchRepo.save(batchIdDetails.get());
                response.setSuccess(true);
                response.setMessage("activate the batches successfully");
                response.setData(new Gson().toJson(batchInfo));
                logger.info("BatchController(updateActivateBatches) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                batchInfo = null;
                response.setSuccess(false);
                response.setMessage("Failed to activate the batches");
                response.setData(null);
                logger.info("TrainerController(updateActivateBatches) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(updateActivateBatches)>> Exit");
        }
        return null;
    }

 /*   update trainee id in the batch details
    and remove the trainee details in batch details for unselect batch id*/
    @GetMapping("/update-batch-in-trainee/{batchId}/{traineeId}/{removeId}")
    public ResponseEntity<ResponseDto> updateBatchInTraineeDetails(@PathVariable Long batchId,@PathVariable Long traineeId,@PathVariable Long removeId) {
        logger.info("BatchController(updateBatchInTraineeDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try{
            Optional<Batch> batchdetails = batchRepo.findById(batchId);
            Batch updateTraineeId = new Batch();
            if(batchdetails.isPresent()) {
                List<Long> traineeIds = batchdetails.get().getTraineesIds();
                traineeIds.add(traineeId);
                batchdetails.get().setTraineesIds(traineeIds);
                updateTraineeId = batchRepo.save(batchdetails.get());
                response.setSuccess(true);
                response.setMessage("batch details update the trainee id successfully");
                response.setData(new Gson().toJson(updateTraineeId));
            }
            if(removeId != 0) {
                Optional<Batch> removetraineeDetails = batchRepo.findById(removeId);
                List<Long> removeTraineeIds = removetraineeDetails.get().getTraineesIds();
                removeTraineeIds.remove(traineeId);
                removetraineeDetails.get().setTraineesIds(removeTraineeIds);
                batchRepo.save(removetraineeDetails.get());
                response.setSuccess(true);
                response.setMessage("batch details update the trainee id successfully");
                response.setData(new Gson().toJson(removetraineeDetails.get()));
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        logger.info("BatchController(updateBatchInTraineeDetails) >> Exit");
        return null;
    }

    /*   update trainer ids in the batch details
    and remove the trainer details in batch details for unselect batch ids */
    @GetMapping("/update-batch-in-trainer/{trainerId}")
    public ResponseEntity<ResponseDto> updatebatchInTrainerDetails(@PathVariable Long trainerId,@RequestParam("ids") List<Long> ids,@RequestParam("remove_ids") List<Long> remove_ids) {
        logger.info("BatchController(updatebatchInTrainerDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
//            List<Long> batchIds = ids;
            Batch updateTrainersData = new Batch();
            for (int i = 0; i < ids.size(); i++) {
                Optional<Batch> batchDetails = batchRepo.findById(ids.get(i));
                List<Long> batchIdsData = batchDetails.get().getTrainersIds();
                batchIdsData.add(trainerId);
                batchDetails.get().setTrainersIds(batchIdsData);
                updateTrainersData = batchRepo.save(batchDetails.get());
            }
            for (int j = 0; j < remove_ids.size(); j++) {
                Optional<Batch> removeBatchDetails = batchRepo.findById(remove_ids.get(j));
                List<Long> batchIdsData = removeBatchDetails.get().getTrainersIds();
                batchIdsData.remove(trainerId);
                removeBatchDetails.get().setTrainersIds(batchIdsData);
                updateTrainersData = batchRepo.save(removeBatchDetails.get());
            }
            response.setSuccess(true);
            response.setMessage("batch details update the trainee id successfully");
            response.setData(new Gson().toJson(updateTrainersData));
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("BatchController(updatebatchInTrainerDetails) >> Exit");
        return null;
    }
    /**
     * Updates the status of multiple batches in bulk.
     *
     * @param ids    a list of batch IDs to update
     * @param status the new status to set for the batches
     * @return the response entity containing the result of the update
     */
    @GetMapping("/bulk-update-batch-status/{status}/{ids}")
    public ResponseEntity<ResponseDto> bulkUpdateBatchStatus(@PathVariable List<Long> ids, @PathVariable String status) {
        logger.info("BatchController(bulkUpdateBatchStatus) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<Batch> batchesIdDetails = batchRepo.findByIdIn(ids);
            if (!batchesIdDetails.isEmpty()) {
                for (Batch batchIdDetails : batchesIdDetails) {
                    if (batchIdDetails.getTraineesIds().size() > 0) {
                        ResponseDto updateTrainee = traineeServiceClient.updateBatchStatus(batchIdDetails.getId(), status);
                    }
                    if (batchIdDetails.getTrainersIds().size() > 0) {
                        ResponseDto updateTrainer = trainerServiceClient.updateBatchStatus(batchIdDetails.getId(), status);
                    }
                    batchIdDetails.set_completed(true);
                    batchIdDetails.setBatch_status(status);
                    batchRepo.save(batchIdDetails);

                }
                response.setSuccess(true);
                response.setMessage("Batches status updated successfully");
                response.setData("Batches status updated successfully");
                logger.info("BatchController(bulkUpdateBatchStatus) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                response.setSuccess(false);
                response.setMessage("Batches not available to update status");
                response.setData(null);
                logger.info("BatchController(bulkUpdateBatchStatus) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(bulkUpdateBatchStatus)>> Exit");
            response.setSuccess(false);
            response.setMessage("Exception in batch update status");
            response.setData(null);
            return new ResponseEntity<ResponseDto>(response, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * A description of the bulkDeleteBatch function.
     *
     * @param ids a List of Long containing the ids to be bulk deleted
     * @return a ResponseEntity containing a ResponseDto object
     */
    @GetMapping("/bulk-delete/{ids}")
    public ResponseEntity<ResponseDto> bulkDeleteBatch(@PathVariable List<Long> ids) {
        logger.info("BatchController(bulkDeleteBatch) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            List<Batch> batchesDetails = batchRepo.findByIdIn(ids);
            if (!batchesDetails.isEmpty()) {
                for (Batch batchDetails : batchesDetails) {
                    if (batchDetails.getTraineesIds().size() > 0) {
                        ResponseDto updateTrainee = traineeServiceClient.updateBatchDelete(batchDetails.getId());
                    }
                    if (batchDetails.getTrainersIds().size() > 0) {
                        ResponseDto updateTrainer = trainerServiceClient.updateBatchDelete(batchDetails.getId());
                    }
                    Batch updateBatch = new Batch();
                    batchDetails.set_deleted(true);
                    updateBatch = batchRepo.save(batchDetails);
                }
                logger.info("BatchController(bulkDeleteBatch)>> Exit");
                responseDto.setSuccess(true);
                responseDto.setMessage("Batch bulk deleted successfully !!");
                responseDto.setData("Batch bulk deleted successfully !!");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                responseDto.setSuccess(false);
                responseDto.setMessage("Batches not available to delete");
                responseDto.setData(null);
                logger.info("BatchController(bulkDeleteBatch) >> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in batchDetails(bulkDeleteBatch)>> Exit");
            responseDto.setSuccess(false);
            responseDto.setMessage("Exception in batches delete");
            responseDto.setData(null);
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /** Automatically batch status = Complete and is_completed = true if it reaches its end - date
     ** Using the cron at 11.59pm it will compare all batches end date with current date,
     **  if it matches with current date it's status will be changed to completed
    **/
    @Scheduled(cron = "0 59 23 ? * MON-SUN", zone = "Asia/Calcutta")
    public String automaticBatchCompletion() throws ParseException {
        logger.info("BatchController(automaticBatchCompletion)>> Entry");
        try {
            List<Batch> batchList = batchRepo.findAllActiveBatches();
            if(!batchList.isEmpty()) {
                Date newdate = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateValue = formatter.format(newdate);
                for(int i=0;i<batchList.size();i++) {
                    String formattedEndDate = formatter.format(batchList.get(i).getEnd_date());
                    if(formattedEndDate.equals(dateValue)) {
                        batchList.get(i).set_completed(true);
                        batchList.get(i).setBatch_status("Complete");
                        Batch batchDetails = batchRepo.save(batchList.get(i));
                    }
                }
                logger.info("BatchController(automaticBatchCompletion)>> Exit");
                return null;
            }
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("BatchController(automaticBatchCompletion)>> Exit");
        }
        return null;
    }

    /**
     * Retrieves the course IDs for a given trainer.
     *
     * @param trainerId The ID of the trainer.
     * @return A ResponseEntity containing the response DTO.
     */
    @GetMapping("getCourseIdsByTrainer/{trainerId}")
    public ResponseEntity<ResponseDto> getCourseIdsByTrainer(@PathVariable Long trainerId) {
        logger.info("BatchController(getCourseIdsByTrainer) >> Entry");
        ResponseDto responseDto = new ResponseDto();

        try {
            // Find batches associated with the trainer
            List<Batch> trainerBatch = batchRepo.findByTrainersIds(trainerId);

            if (!trainerBatch.isEmpty()) {
                // Retrieve unique course IDs from the batches
                Set<Long> uniqueIds = new HashSet<>();
                for (Batch batch : trainerBatch) {
                    uniqueIds.add(batch.getCourseCategoryDetails().getCourse_category_id());
                }
                List<Long> TrainerCourses = new ArrayList<>(uniqueIds);

                // Set success response
                responseDto.setSuccess(true);
                responseDto.setMessage("Trainer batch course Ids getting successfully !!");
                responseDto.setData(new Gson().toJson(TrainerCourses));
                logger.info("BatchController(getCourseIdsByTrainer)>> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                // Set error response if no trainer batches found
                responseDto.setSuccess(false);
                responseDto.setMessage("Trainer batch courses is empty !!");
                logger.info("BatchController(getCourseIdsByTrainer)>> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(getCourseIdsByTrainer)>> Exit");
            responseDto.setSuccess(false);
            responseDto.setMessage("Exception in get trainer batch course Ids !!");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        }
    }


    /**
     * Retrieves the course IDs associated with a trainee.
     *
     * @param  traineeId  the ID of the trainee
     * @return            the response containing the course IDs of the trainee's batches
     */
    @GetMapping("getCourseIdsByTrainee/{traineeId}")
    public ResponseEntity<ResponseDto> getCourseIdsByTrainee(@PathVariable Long traineeId) {
        logger.info("BatchController(getCourseIdsByTrainee) >> Entry");
        ResponseDto responseDto = new ResponseDto();

        try {
            // Find batches associated with the trainer
            List<Batch> traineeBatch = batchRepo.findByTraineesIds(traineeId);

            if (!traineeBatch.isEmpty()) {
                // Retrieve unique course IDs from the batches
                Set<Long> uniqueIds = new HashSet<>();
                for (Batch batch : traineeBatch) {
                    uniqueIds.add(batch.getCourseCategoryDetails().getCourse_category_id());
                }
                List<Long> TraineeCourses = new ArrayList<>(uniqueIds);

                // Set success response
                responseDto.setSuccess(true);
                responseDto.setMessage("Trainer batch course Ids getting successfully !!");
                responseDto.setData(new Gson().toJson(TraineeCourses));
                logger.info("BatchController(getCourseIdsByTrainee)>> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                // Set error response if no trainer batches found
                responseDto.setSuccess(false);
                responseDto.setMessage("Trainer batch courses is empty !!");
                logger.info("BatchController(getCourseIdsByTrainee)>> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(getCourseIdsByTrainee)>> Exit");
            responseDto.setSuccess(false);
            responseDto.setMessage("Exception in get trainer batch course Ids !!");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        }
    }

    /**
     * Deletes a trainee from all batches associated with the trainee.
     *
     * @param traineeId The ID of the trainee to be deleted.
     * @return A ResponseEntity containing a ResponseDto with the result of the operation.
     */
    @GetMapping("trainee-delete-update-batch/{traineeId}")
    public ResponseEntity<ResponseDto> traineeDeleteUpdateBatch(@PathVariable Long traineeId) {
        logger.info("BatchController(traineeDeleteUpdateBatch) >> Entry");
        ResponseDto responseDto = new ResponseDto();

        try {
            // Find batches associated with the trainee
            List<Batch> traineeBatches = batchRepo.findByTraineesIds(traineeId);

            if (!traineeBatches.isEmpty()) {
                // Iterate over the batches
                for (Batch batch : traineeBatches) {
                    Iterator<Long> iterator = batch.getTraineesIds().iterator();
                    // Iterate over the trainees in the batch
                    while (iterator.hasNext()) {
                        Long tempId = iterator.next();
                        // Remove the trainee from the batch
                        if (Objects.equals(tempId, traineeId)) {
                            iterator.remove();
                            batchRepo.save(batch);
                            // If you want to remove only one occurrence, break after removal
                            break;
                        }
                    }
                }

                // Set success response
                responseDto.setSuccess(true);
                responseDto.setMessage("Trainee ID removed from batches successfully!");
                logger.info("BatchController(traineeDeleteUpdateBatch) >> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                // Set error response if no trainee batches found
                responseDto.setSuccess(true);
                responseDto.setMessage("Trainee not found in any batches!");
                logger.info("BatchController(traineeDeleteUpdateBatch) >> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(traineeDeleteUpdateBatch) >> Exit");
            responseDto.setSuccess(false);
            responseDto.setMessage("Exception occurred while removing trainee from batches!");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        }
    }

    /**
     * Delete or update batches associated with a trainer.
     *
     * @param  trainerId	  the ID of the trainer
     * @return         	  a ResponseEntity containing a ResponseDto
     */
    @GetMapping("trainer-delete-update-batch/{trainerId}")
    public ResponseEntity<ResponseDto> trainerDeleteUpdateBatch(@PathVariable Long trainerId) {
        logger.info("BatchController(trainerDeleteUpdateBatch) >> Entry");
        ResponseDto responseDto = new ResponseDto();

        try {
            // Find batches associated with the trainee
            List<Batch> trainerBatches = batchRepo.findByTrainersIds(trainerId);

            if (!trainerBatches.isEmpty()) {
                // Iterate over the batches
                for (Batch batch : trainerBatches) {
                    Iterator<Long> iterator = batch.getTrainersIds().iterator();
                    // Iterate over the trainees in the batch
                    while (iterator.hasNext()) {
                        Long tempId = iterator.next();
                        // Remove the trainee from the batch
                        if (Objects.equals(tempId, trainerId)) {
                            iterator.remove();
                            batchRepo.save(batch);
                            // If you want to remove only one occurrence, break after removal
                            break;
                        }
                    }
                }

                // Set success response
                responseDto.setSuccess(true);
                responseDto.setMessage("Trainer ID removed from batches successfully!");
                logger.info("BatchController(trainerDeleteUpdateBatch) >> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                // Set error response if no trainee batches found
                responseDto.setSuccess(true);
                responseDto.setMessage("Trainer not found in any batches!");
                logger.info("BatchController(trainerDeleteUpdateBatch) >> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace(System.out);
            logger.info("Exception in BatchController(trainerDeleteUpdateBatch) >> Exit");
            responseDto.setSuccess(false);
            responseDto.setMessage("Exception occurred while removing trainee from batches!");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        }
    }

}
