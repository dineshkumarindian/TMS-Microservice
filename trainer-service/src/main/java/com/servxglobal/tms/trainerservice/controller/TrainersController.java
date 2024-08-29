package com.servxglobal.tms.trainerservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servxglobal.tms.trainerservice.dto.*;
import com.servxglobal.tms.trainerservice.model.TrainerBatch;
import com.servxglobal.tms.trainerservice.repository.TrainersRepo;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import  com.servxglobal.tms.trainerservice.model.Trainers;
import com.google.gson.Gson;
import org.springframework.web.multipart.MultipartFile;
//import com.google.gson.Gson;
import java.util.*;
import com.servxglobal.tms.trainerservice.otherService.BatchServiceClient;

@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/trainer")
@Component
@EnableAutoConfiguration
public class TrainersController {

    @Autowired
    private TrainersRepo trainerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BatchServiceClient batchServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(TrainersController.class);

    @PostMapping ("/register")
    public ResponseEntity<SuccessandMessageDto> registerTrainer(@RequestParam("firstname") String firstName, @RequestParam("lastname") String lastName, @RequestParam("userEmail") String userEmail, @RequestParam("password") String password,
                                                                @RequestParam("address") String address, @RequestParam("contact_number") String contactNumber, @RequestParam("alternate_number") String alternateNumber,
                                                                @RequestParam("branch") String branch, @RequestParam("branch_id") Long branch_id,@RequestParam("skill_id") List<Long> skill_id,
                                                                @RequestParam("skills") List<String> skills,
                                                                @RequestParam("image") MultipartFile image,@RequestParam("image_name") String image_name,@RequestParam("batches") String batches) {
//        @RequestHeader(name="Authorization") String token
        SuccessandMessageDto response = new SuccessandMessageDto();
        Optional<Trainers> isEmailPresent = trainerRepo.findByUserEmail(userEmail);
        if(isEmailPresent.isPresent())  {
            response.setMessage("Email is already registered !!");
            response.setSuccess(false);
//            response.setData(userEmail);
            return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.OK);
        }
        Trainers trainerData = new Trainers();
        List<Long> batchIds = new ArrayList<>();
        List<Long> removeIds = new ArrayList<>();
        String batch_data = "";
        try {
            trainerData.setFirstname(firstName);
            trainerData.setLastname(lastName);
            trainerData.setPassword(passwordEncoder.encode(password));
            trainerData.setUserEmail(userEmail);
            trainerData.setId(trainerRepo.count() + 1);
            trainerData.setAddress(address);
            trainerData.setContact_number(contactNumber);
            trainerData.setAlternate_number(alternateNumber);
            trainerData.setBranch_id(branch_id);
            trainerData.setSkill_id(skill_id);
            trainerData.setBranch(branch);
            trainerData.setLogo_Img_name(image_name);
            batch_data = batches;
            List<TrainerBatch> listOfTrainersBatches = new ArrayList<>();
            if(!batch_data.equals("[]")) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(batch_data);
                for (JsonNode jsonObject : jsonNode) {
                    TrainerBatch trainersbatch = new TrainerBatch();
                    trainersbatch.setBatch_id(jsonObject.get("batch_id").asLong());
                    trainersbatch.setBatch_name(jsonObject.get("batch_name").asText());
                    trainersbatch.setBatch_status(jsonObject.get("batch_status").asText());
                    batchIds.add(trainersbatch.getBatch_id());
                    listOfTrainersBatches.add(trainersbatch);
                }
            }
            trainerData.setBatches(listOfTrainersBatches);
            trainerData.setSkills(skills);
            trainerData.setLogo_Img(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
            trainerData.setCreated_time(new Date());
            trainerData.setModified_time(new Date());
//            trainerData.setBatches(batches);
            try {
//                trainerData.setCreatedBy(adminRepo.findByEmail(jwtGenerator.getUsernameFromJWT(token.substring(7))).orElseThrow());
            } catch (Exception e) {
                e.printStackTrace();
                response.setMessage("Unauthorized request");
                response.setSuccess(false);
                return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.UNAUTHORIZED);
        }
        Trainers trainersrepo = trainerRepo.save(trainerData);
        if(!batch_data.equals("[]")) {
            ResponseDto updateTraineeInBatches = batchServiceClient.TrainerBatchUpdate(trainersrepo.getId(), batchIds,removeIds);
        }
        response.setMessage("Profile Created Successfully !!");
        response.setSuccess(true);
        return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all the trainers details.
     *
     * @return ResponseEntity<List<Trainers>> - The response entity containing the list of trainers.
     */
    @GetMapping("/get-all-trainers")
    public ResponseEntity<List<Trainers>> getAllTrainers() {
        logger.info("TrainerController(getAllTrainers) >> Entry");
        try {
            List<Trainers> trainerDetails = trainerRepo.findAll();
            logger.info("TrainerController(getAllTrainers) >> Exit");
            return new ResponseEntity<List<Trainers>>(trainerDetails, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(getAllTrainers) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves all active trainers from the database.
     *
     * @return A ResponseEntity containing a list of trainers.
     */
    @GetMapping("/get-active-trainers")
    public ResponseEntity<List<TrainersDto>> getActiveTrainers() {
        logger.info("TrainerController(getAllActiveTrainers) >> Entry");
        try {
            List<Trainers> trainerData = trainerRepo.findActiveTrainers();
            List<TrainersDto> trainerDetails = new ArrayList<>();
            for (Trainers i : trainerData) {
                TrainersDto customTrainer = new TrainersDto();
                customTrainer.setId(i.getId());
                customTrainer.setFirstname(i.getFirstname());
                customTrainer.setLastname(i.getLastname());
                customTrainer.setUserEmail(i.getUserEmail());
                customTrainer.setContact_number(i.getContact_number());
                customTrainer.setBranch(i.getBranch());
                customTrainer.setBatches(i.getBatches());
                customTrainer.setSkills(i.getSkills());
                trainerDetails.add(customTrainer);
            }
            logger.info("TrainerController(getAllActiveTrainers) >> Exit");
            return new ResponseEntity<List<TrainersDto>>(trainerDetails, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(getAllActiveTrainers) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves all inactive trainers from the database.
     *
     * @return A ResponseEntity containing a list of trainers.
     */
    @GetMapping("/get-inactive-trainers")
    public ResponseEntity<List<TrainersDto>> getInactiveTrainers() {
        logger.info("TrainerController(getInactiveTrainers) >> Entry");
        try {
            List<Trainers> trainerData = trainerRepo.findInactiveTrainers();
            List<TrainersDto> trainerDetails = new ArrayList<>();
            for (Trainers i : trainerData) {
                TrainersDto customTrainer = new TrainersDto();
                customTrainer.setId(i.getId());
                customTrainer.setFirstname(i.getFirstname());
                customTrainer.setLastname(i.getLastname());
                customTrainer.setUserEmail(i.getUserEmail());
                customTrainer.setContact_number(i.getContact_number());
                customTrainer.setBranch(i.getBranch());
                customTrainer.setBatches(i.getBatches());
                customTrainer.setSkills(i.getSkills());
                trainerDetails.add(customTrainer);
            }
            logger.info("TrainerController(getInactiveTrainers) >> Exit");
            return new ResponseEntity<List<TrainersDto>>(trainerDetails, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(getInactiveTrainers) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves the trainer details for the given ID.
     *
     * @param  id the ID of the trainer
     * @return    the trainer details
     */
    @GetMapping("/get-trainers-by-id/{id}")
    public ResponseEntity<ResponseDto> getTrainersById(@PathVariable Long id) {
        logger.info("TrainerController(getTrainersById) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Trainers> trainerDetails = trainerRepo.findById(id);
            responseDto.setSuccess(true);
            responseDto.setMessage("trainer details got successful !!");
            responseDto.setData(new Gson().toJson(trainerDetails.get()));
            logger.info("TrainerController(getTrainersById) >> Exit");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(getTrainersById) >> Exit");
        }
        return null;
    }

    @GetMapping("/get-given-id-name-by-details")
    public ResponseEntity<ResponseDto> getGivenIdNameByDetails() {
        logger.info("TrainerController(getGivenIdNameByDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<?> trainerByIdNames = trainerRepo.findSpecificIdNameTrainerDetails();
            response.setSuccess(true);
            response.setMessage("Deactivate the trainee successfully");
            response.setData(new Gson().toJson(trainerByIdNames));
            logger.info("TrainerController(getGivenIdNameByDetails) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
        } catch(Exception e){
            logger.info("Exception in TrainerController(getGivenIdNameByDetails) >> Exit");
        }
        logger.info("TrainerController(getActiveTrainee) >> Exit");
        return null;
    }
    @GetMapping("/get-given-id-email-by-details")
    public ResponseEntity<ResponseDto> getGivenIdEmailByDetails() {
        logger.info("TrainerController(getGivenIdEmailByDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<?> trainerByIdNames = trainerRepo.findSpecificUserMailDetails();
            response.setSuccess(true);
            response.setMessage("Deactivate the trainee successfully");
            response.setData(new Gson().toJson(trainerByIdNames));
            logger.info("TrainerController(getGivenIdEmailByDetails) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
        } catch(Exception e){
            logger.info("Exception in TrainerController(getGivenIdEmailByDetails) >> Exit");
        }
        logger.info("TrainerController(getGivenIdEmailByDetails) >> Exit");
        return null;
    }


    /**
     * Update the trainer details by the given ID.
     *
     * @param id     The ID of the trainer to update.
     * @requestbody  trainer new details to update
     * @return       The updated trainer if successful, or null if an exception occurred.
     */
    @PostMapping("/update-trainer")
    public ResponseEntity<ResponseDto> updateTrainer(@RequestParam("id") Long id ,@RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname,@RequestParam("email") String userEmail,
                                                     @RequestParam("address") String address,@RequestParam("contact_number") String contact_number,@RequestParam("alternate_number") String alternate_number,
                                                     @RequestParam("branch") String branch,@RequestParam("branch_id") Long branch_id,
                                                     @RequestParam("skills") List<String> skills,@RequestParam("skill_id") List<Long> skill_id,
                                                     @RequestParam("image") MultipartFile image,@RequestParam("image_name") String image_name,@RequestParam("batches") String batches) {
        logger.info("TrainerController(updateTrainer) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Trainers> oldTrainerData = trainerRepo.findById(id);
            Trainers trainerInfo = new Trainers();
            List<Long> batchIds = new ArrayList<>();
            List<Long> removeIds = new ArrayList<>();
            if(oldTrainerData.isPresent()) {
                oldTrainerData.get().setFirstname(firstname);
                oldTrainerData.get().setLastname(lastname);
                oldTrainerData.get().setBranch(branch);
                oldTrainerData.get().setBranch_id(branch_id);
                oldTrainerData.get().setAddress(address);
                oldTrainerData.get().setContact_number(contact_number);
                oldTrainerData.get().setAlternate_number(alternate_number);
                oldTrainerData.get().setUserEmail(userEmail);
//                trainerData.get().setBatches(trainerDetails.getBatches());
                oldTrainerData.get().setSkills(skills);
                oldTrainerData.get().setSkill_id(skill_id);
                oldTrainerData.get().setLogo_Img(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
                oldTrainerData.get().setModified_time(new Date());
                oldTrainerData.get().setLogo_Img_name(image_name);
                List<TrainerBatch> listOfTrainersBatches = new ArrayList<>();
                List<TrainerBatch> trainerDataId = oldTrainerData.get().getBatches();
                String trainerDataIdStr = trainerDataId.toString();
                if(trainerDataIdStr != "null") {
                    for (int i = 0; i < trainerDataId.size(); i++) {
                        long trainerId = trainerDataId.get(i).getBatch_id();
                        removeIds.add(trainerId);
                    }
                }
                    String batch_data = batches;
                    if (!batch_data.equals("[]")) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(batch_data);
                        for (JsonNode jsonObject : jsonNode) {
                            TrainerBatch trainersbatch = new TrainerBatch();
                            trainersbatch.setBatch_id(jsonObject.get("batch_id").asLong());
                            trainersbatch.setBatch_name(jsonObject.get("batch_name").asText());
                            trainersbatch.setBatch_status(jsonObject.get("batch_status").asText());
                            batchIds.add(trainersbatch.getBatch_id());
                            listOfTrainersBatches.add(trainersbatch);
                        }
                    }
                oldTrainerData.get().setBatches(listOfTrainersBatches);
                Set<Long> A = new HashSet<>(batchIds);
                Set<Long> B = new HashSet<>(removeIds);
                Set<Long> copyA = new HashSet<>(batchIds);
                Set<Long> copyB = new HashSet<>(removeIds);
                copyB.removeAll(A);
                copyA.removeAll(B);
                batchIds = copyA.stream().toList();
                removeIds = copyB.stream().toList();
                trainerInfo = trainerRepo.save(oldTrainerData.get());
                ResponseDto updateTraineeInBatches = batchServiceClient.TrainerBatchUpdate(trainerInfo.getId(),batchIds,removeIds);
                response.setSuccess(true);
                response.setMessage("trainer update successfully");
                response.setData(new Gson().toJson(trainerInfo));
                logger.info("TrainerController(updateTrainer) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                trainerInfo = null;
                response.setSuccess(false);
                response.setMessage("trainer update failed");
                response.setData(new Gson().toJson(trainerInfo));
                logger.info("TrainerController(updateTrainer) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.BAD_REQUEST);
            }
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(updateTrainer) >> Exit");
        }
        return  null;
    }

    /**
     * Retrieves the trainer details  for the given ID its "_active" flag to false.
     *
     * @param  id the ID of the trainer
     * @return    the trainer details in deactivated form
     */
    @GetMapping ("/deactivate-trainer/{id}")
    public ResponseEntity<ResponseDto> deactivateTrainer(@PathVariable Long id) {
        logger.info("TrainerController(deactivateTrainer) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Trainers> trainerData = trainerRepo.findById(id);
            Trainers trainerInfo = new Trainers();
            if (trainerData.isPresent()) {
                trainerData.get().setActive(false);
                logger.info("TrainerController(deactivateTrainer) >> Exit");
                trainerInfo = trainerRepo.save(trainerData.get());
                response.setSuccess(true);
                response.setMessage("Deactivate the trainer successfully");
                response.setData(new Gson().toJson(trainerInfo));
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                trainerInfo = null;
                response.setSuccess(false);
                response.setMessage("Failed to Deactivate the trainer");
                response.setData(null);
                logger.info("TrainerController(deactivateTrainer) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
        }catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(deactivateTrainer) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves the trainer details  for the given ID its "_active" flag to true.
     *
     * @param  id the ID of the trainer
     * @return    the trainer details in activate form
     */
    @GetMapping("/activate-trainer/{id}")
    public ResponseEntity<ResponseDto> activateTrainer(@PathVariable Long id) {
        logger.info("TrainerController(activateTrainer) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Trainers> trainerData = trainerRepo.findById(id);
            Trainers trainerInfo = new Trainers();
            if (trainerData.isPresent()) {
                trainerData.get().setActive(true);
                logger.info("TrainerController(activateTrainer) >> Exit");
                trainerInfo = trainerRepo.save(trainerData.get());
                response.setSuccess(true);
                response.setMessage("Activate the trainer successfully");
                response.setData(new Gson().toJson(trainerInfo));
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }else {
                trainerInfo = null;
                response.setSuccess(true);
                response.setMessage("Failed to activate the trainer");
                response.setData(new Gson().toJson(trainerInfo));
                logger.info("TrainerController(activateTrainer) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
        }catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(activateTrainer) >> Exit");
        }
        return null;
    }

    /**
     * Soft deletes a trainer by setting its "_deleted" flag to true.
     *
     * @param id The ID of the trainer to delete.
     * @return The updated trainer details if found, or a bad request response if not.
     */
    @PutMapping ("/delete-trainer/{id}")
    public ResponseEntity<ResponseDto> deleteTrainer(@PathVariable Long id) {
        logger.info("TrainerController(deleteTrainer) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Trainers> trainerDetails = trainerRepo.findById(id);
            Trainers trainerInfo = new Trainers();
            if(trainerDetails.isPresent()) {
                ResponseDto responseBatch = batchServiceClient.trainerDeleteUpdateBatch(id);
                if (responseBatch.isSuccess()){
                    trainerDetails.get().set_deleted(true);
                    logger.info("TrainerController(deleteTrainer) >> Exit");
                    trainerInfo = trainerRepo.save(trainerDetails.get());
                    response.setSuccess(true);
                    response.setMessage("delete the trainer successfully");
                    response.setData(new Gson().toJson(trainerInfo));
                    return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
                }
                else {
                    logger.info("TrainerController(deleteTrainer) >> Exit");
                    response.setSuccess(false);
                    response.setData(null);
                    response.setMessage("Failed to delete the trainer");
                    response.setData(new Gson().toJson(trainerDetails));
                    return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
                }

            }
            else {
                trainerInfo = null;
                response.setSuccess(false);
                response.setMessage("Failed to delete the trainer");
                response.setData(new Gson().toJson(trainerInfo));
                logger.info("TrainerController(deleteTrainer) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Exception in delete the trainer");
            logger.info("TrainerController(deleteTrainer) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * Deletes a trainer permanently by its ID.
     *
     * @param id The ID of the trainer to be deleted.
     * @return A ResponseEntity with a message indicating the result of the deletion.
     */
    @DeleteMapping("/trainer-hardDelete/{id}")
    public ResponseEntity<String> trainerHardDelete(@PathVariable Long id) {
        logger.info("TrainerController(trainerHardDelete) >> Entry");
        try {
            Optional<Trainers> trainerInfo = trainerRepo.findById(id);
            if(trainerInfo.isPresent()) {
                trainerRepo.deleteById(id);
                logger.info("TrainerController(trainerHardDelete) >> Exit");
                return new ResponseEntity<String>("Deleted Successfully", HttpStatus.OK);
            } else {
                logger.info("TrainerController(trainerHardDelete) >> Exit");
                return new ResponseEntity<String>("Failed to delete trainer", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(trainerHardDelete) >> Exit");
        }
        return null;
    }
    @GetMapping("/get-trainer-details-by-id/{id}")
    public ResponseEntity<ResponseDto> getTrainerDetailsById(@PathVariable Long id) {
        logger.info("TrainerController(getTrainerDetailsById) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Trainers> trainerDetails = trainerRepo.findByTrainerDetailsId(id);
            responseDto.setSuccess(true);
            responseDto.setMessage("Trainer details got successful !!");
            responseDto.setData(new Gson().toJson(trainerDetails.get()));
            logger.info("TrainerController(getTrainersById) >> Exit");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(getTrainerDetailsById) >> Exit");
        }
        return null;
    }
    @PutMapping("/bulk-delete")
    public ResponseEntity<BulkActionDto> bulkTrainerDelete(@RequestBody String request) {
        logger.info("TrainerController(bulkTrainerDelete) >> Entry");
        JSONObject newJsonObject = new JSONObject(request);
        JSONArray newDetails = newJsonObject.getJSONArray("deleteIds");
        BulkActionDto response = new BulkActionDto();
        List<Long> trainerlist = new ArrayList<Long>();
        for (int i = 0; i < newDetails.length(); i++) {
            trainerlist.add(Long.parseLong(String.valueOf(newDetails.get(i))));
        }
        try {
            for (int i = 0; i < trainerlist.size(); i++) {
                Optional<Trainers> trainerDetails = trainerRepo.findById(trainerlist.get(i));
                Trainers trainerInfo = new Trainers();
                if (trainerDetails.isPresent()) {
                    ResponseDto responseBatch = batchServiceClient.trainerDeleteUpdateBatch(trainerDetails.get().getId());
                    if (responseBatch.isSuccess()){
                        trainerDetails.get().set_deleted(true);
                        trainerInfo = trainerRepo.save(trainerDetails.get());
                    }
                } else {
                    trainerInfo = null;
                    response.setSuccess(false);
                    response.setMessage("Failed to delete bulk trainers");
                    return new ResponseEntity<BulkActionDto>(response, HttpStatus.NOT_FOUND);
                }
            }
            response.setSuccess(true);
            response.setMessage("Bulk trainers deleted successfully");
            logger.info("TrainerController(bulkTrainerDelete) >> Exit");
            return new ResponseEntity<BulkActionDto>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(bulkTrainerDelete) >> Exit");
            response.setSuccess(false);
            response.setMessage("Exception in to delete bulk trainers");
            return new ResponseEntity<BulkActionDto>(response, HttpStatus.EXPECTATION_FAILED);
        }
    }
    @PutMapping("/bulk-deactivate")
    public ResponseEntity<BulkActionDto> bulkDeactivateTrainer(@RequestBody String request) {
        logger.info("TrainerController(bulkDeactivateTrainer) >> Entry");
        JSONObject newJsonObject = new JSONObject(request);
        JSONArray newDetails = newJsonObject.getJSONArray("deleteIds");
        List<Long> trainerlist = new ArrayList<Long>();
        BulkActionDto response = new BulkActionDto();
        for (int i = 0; i < newDetails.length(); i++) {
            trainerlist.add(Long.parseLong(String.valueOf(newDetails.get(i))));
        }
        try {
            for (int i = 0; i < trainerlist.size(); i++) {
                Optional<Trainers> trainerDetails = trainerRepo.findById(trainerlist.get(i));
                Trainers trainerInfo = new Trainers();
                if (trainerDetails.isPresent()) {
                    trainerDetails.get().setActive(false);
                    trainerInfo = trainerRepo.save(trainerDetails.get());
                    response.setSuccess(true);
                    response.setMessage("Bulk trainers deactivated successfully");
                } else {
                    trainerInfo = null;
                    response.setSuccess(false);
                    response.setMessage("Failed to deactivate bulk trainers");
                    return new ResponseEntity<BulkActionDto>(response, HttpStatus.BAD_REQUEST);
                }
            }
            logger.info("TrainerController(bulkDeactivateTrainer) >> Exit");
            return new ResponseEntity<BulkActionDto>(response, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();

        }
        return null;
    }
    @PutMapping("/bulk-activate")
    public ResponseEntity<BulkActionDto> bulkActivateTrainer(@RequestBody String request) {
        logger.info("TrainerController(bulkActivateTrainer) >> Entry");
        JSONObject newJsonObject = new JSONObject(request);
        JSONArray newDetails = newJsonObject.getJSONArray("deleteIds");
        List<Long> trainerlist = new ArrayList<Long>();
        BulkActionDto response = new BulkActionDto();
        for (int i = 0; i < newDetails.length(); i++) {
            trainerlist.add(Long.parseLong(String.valueOf(newDetails.get(i))));
        }
        try {
            for (int i = 0; i < trainerlist.size(); i++) {
                Optional<Trainers> trainerDetails = trainerRepo.findById(trainerlist.get(i));
                Trainers trainerInfo = new Trainers();
                if (trainerDetails.isPresent()) {
                    trainerDetails.get().setActive(true);
                    trainerInfo = trainerRepo.save(trainerDetails.get());
                    response.setSuccess(true);
                    response.setMessage("Bulk trainers activated successfully");
                } else {
                    trainerInfo = null;
                    response.setSuccess(false);
                    response.setMessage("Failed to activate bulk trainers");
                    return new ResponseEntity<BulkActionDto>(response, HttpStatus.BAD_REQUEST);
                }
            }
            logger.info("TrainerController(bulkActivateTrainer) >> Exit");
            return new ResponseEntity<BulkActionDto>(response, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();

        }
        return null;
    }
//    /**
//     * Update the batch details by the given trainer ID.
//     *
//     * @param trainerId    The ID of the trainer to update.
//     * @param batchDetails  The batch details recently created with trainer.
//     */
//
//    // Update batch id and name in batches column when trainer is added to that batch
//    @PutMapping("/update-trainer-batch")
//    public ResponseEntity<ResponseDto> updateTrainer(@RequestParam("batchDetails") Object batchDetails, @RequestParam("trainerId") Long trainerId) {
//        logger.info("TraineeController(updateTrainee) >> Entry");
//        ResponseDto response = new ResponseDto();
//        try {
//                Optional<Trainers> trainer = trainerRepo.findById(trainerId);
//                Trainers trainerData = new Trainers();
//                if (trainer.isPresent()) {
//                    trainer.get().setBatches(batchDetails);
//                    trainerData = trainerRepo.save(trainer.get());
//                } else {
//                    trainerData = null;
//                    response.setSuccess(false);
//                    response.setMessage("trainee update failed");
//                    response.setData(new Gson().toJson(trainerData));
//                    logger.info("TraineeController(updateTrainee) >> Exit");
//                    return new ResponseEntity<ResponseDto>(response, HttpStatus.BAD_REQUEST);
//                }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * Updates the trainers in batch.
     *
     * @param  batchDetails    the batch trainee details
     * @param  ids             the list of trainer IDs
     * @param  removedIds      the list of removed trainer IDs
     * @return                 the response entity containing the response DTO
     */
    @PutMapping("/trainers-batch-update")
    public ResponseEntity<ResponseDto> trainersBatchUpdate(@RequestBody BatchTrainerDto batchDetails,
                                                           @RequestParam("ids") List<Long> ids,
                                                           @RequestParam("removedIds") List<Long> removedIds) {
        logger.info("TraineeController(trainersBatchUpdate) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            // Find trainers who have been removed from the batch
            List<Trainers> missedTrainers = trainerRepo.findByIdIn(removedIds);
            if (missedTrainers.size() > 0) {
                for (Trainers iterationData : missedTrainers) {
                    if (iterationData.getBatches() != null) {
                        List<TrainerBatch> tempList = iterationData.getBatches();
                        List<TrainerBatch> Store = new ArrayList<>();
                        for (TrainerBatch batch : tempList ) {
                            if (!(batch.getBatch_id().equals(batchDetails.getBatch_id()))) {
                                Store.add(batch);
                            }
                        }
                        iterationData.setBatches(Store);
                        trainerRepo.save(iterationData);
                    }
                }
            }

            // Find trainers who are still part of the batch or have been newly added
            List<Trainers> trainers = trainerRepo.findByIdIn(ids);
            if (trainers.size() > 0) {
                for (Trainers iterationData : trainers) {
                    if (iterationData.getBatches() != null) {
                        boolean batchPresent = false;
                        for (TrainerBatch batch : iterationData.getBatches()) {
                            if (batch.getBatch_id().equals(batchDetails.getBatch_id())) {
                                batchPresent = true;
                                break;
                            }
                        }
                        if (!batchPresent) {
                            TrainerBatch data = new TrainerBatch();
                            data.setBatch_id(batchDetails.getBatch_id());
                            data.setBatch_name(batchDetails.getBatch_name());
                            data.setBatch_status("Active");
                            List<TrainerBatch> temp = iterationData.getBatches();
                            temp.add(data);
                            iterationData.setBatches(temp);
                            trainerRepo.save(iterationData);
                        }
                    } else {
                        TrainerBatch data = new TrainerBatch();
                        data.setBatch_id(batchDetails.getBatch_id());
                        data.setBatch_name(batchDetails.getBatch_name());
                        data.setBatch_status("Active");
                        List<TrainerBatch> temp = new ArrayList<>();
                        temp.add(data);
                        iterationData.setBatches(temp);
                        trainerRepo.save(iterationData);
                    }
                }
                response.setSuccess(true);
                response.setMessage("Batch details updated in trainers");
                response.setData("Batch details updated in trainers");
            } else {
                response.setSuccess(true);
                response.setMessage("Trainers not found");
                response.setData("Trainers not found");
            }
            logger.info("TraineeController(trainersBatchUpdate) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Trainers batch update failed");
            response.setData("Trainers batch update failed");
            logger.info("TraineeController(trainersBatchUpdate) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.EXPECTATION_FAILED);
        }
    }


    /**
     * Updates the complete status of a trainer.
     *
     * @param id the ID of the trainer
     * @return the response containing the updated trainer details
     */
    @GetMapping("/update-batch-status/{status}/{id}")
    public ResponseEntity<ResponseDto> updateBatchStatus(@PathVariable Long id, @PathVariable String status) {
        // Log the entry point of the function
        logger.info("TrainerController(updateBatchStatus) >> Entry");

        // Create a response object
        ResponseDto response = new ResponseDto();

        try {
            // Find trainer details by batch ID
            List<Trainers> trainerDetails = trainerRepo.findByBatchIdInBatches(id);

            if (trainerDetails.size() != 0) {
                // Iterate through each trainer
                for (Trainers temp : trainerDetails) {
                    for (TrainerBatch batch : temp.getBatches()) {
                        if( batch.getBatch_id() !=null){
                            // Check if the trainer is associated with the given batch ID
                            if (batch.getBatch_id().equals(id)) {
                                // Update the batch status to Status path variable value
                                batch.setBatch_status(status);
                                // Save the trainer details
                                trainerRepo.save(temp);
                                break;
                            }
                        }
                    }
                }

                // Log the exit point of the function
                logger.info("TrainerController(updateBatchStatus) >> Exit");

                // Set the success flag of the response
                response.setSuccess(true);
                // Set the message of the response
                response.setMessage("Update batch status successfully");
                // Set the data of the response
                response.setData(new Gson().toJson(trainerDetails));

                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                // Set the success flag of the response
                response.setSuccess(false);
                // Set the message of the response
                response.setMessage("Trainers not available for this batch");
                // Set the data of the response
                response.setData(null);

                // Log the exit point of the function
                logger.info("TrainerController(updateBatchStatus) >> Exit");

                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            // Print the stack trace
            e.printStackTrace();

            // Set the success flag of the response
            response.setSuccess(false);
            // Set the message of the response
            response.setMessage("Failed to Update batch status");
            // Set the data of the response
            response.setData(null);

            // Log the exit point of the function
            logger.info("TrainerController(updateBatchStatus) >> Exit");

            return new ResponseEntity<ResponseDto>(response, HttpStatus.EXPECTATION_FAILED);
        }
    }
    // To show trainers images in get batch details dialog box onclick batch name in batch table
    /**
     *get the trainer images by id
     *
     * @param id the ID of the trainer
     * @return the response containing the trainer image
     */
    @GetMapping("/get-trainers-image/{id}")
    public ResponseEntity<ResponseDto> getTrainerImages(@PathVariable Long id) {
        logger.info("TrainerController(getTrainerImages) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
                Optional<Trainers> trainerDetails = trainerRepo.findById(id);
                Trainers trainerInfo = new Trainers();
                if (trainerDetails.isPresent()) {
                    Optional<Trainers> trainerData = trainerRepo.getImages(id);
                    if (trainerData.isPresent()) {
                        Binary imageData = trainerData.get().getLogo_Img();
                        responseDto.setSuccess(true);
                        responseDto.setMessage("Trainer details got successful !!");
                        responseDto.setData(new Gson().toJson(imageData));
                        logger.info("TrainerController(getTrainerImages) >> Exit");
                        return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(getTrainerImages) >> Exit");
        }
        return null;
    }
    // To show trainers details in get batch details dialog box onclick batch name in batch table
    /**
     *get the trainers details(name, email, contact number, branch and logo image) by id
     *
     * @param id the ID of the trainer
     * @return the response containing the trainer details
     */
    // To get trainer details by batch id to show in batch dialog box
    @GetMapping("/get-trainers-for-batch/{id}")
    public ResponseEntity<ResponseDto> getTrainerDetailsForBatch(@PathVariable Long id) {
        logger.info("TrainerController(getTrainerDetailsForBatch) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Trainers> trainerDetails = trainerRepo.findById(id);
            Trainers trainerInfo = new Trainers();
            if (trainerDetails.isPresent()) {
                Optional<Trainers> trainerData = trainerRepo.getTrainerDetailsForBatch(id);
                    responseDto.setSuccess(true);
                    responseDto.setMessage("Trainer details got successful !!");
                    responseDto.setData(new Gson().toJson(trainerData.get()));
                    logger.info("TrainerController(getTrainerDetailsForBatch) >> Exit");
                    return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TrainerController(getTrainerDetailsForBatch) >> Exit");
        }
        return null;
    }


    /**
     * Update batch delete endpoint.
     *
     * @param  id  The ID of the batch to be deleted.
     * @return     The response containing the status and message.
     */
    @GetMapping("/update-batch-delete/{id}")
    public ResponseEntity<ResponseDto> updateBatchDelete(@PathVariable Long id) {
        // Log the entry point of the function
        logger.info("TrainerController(updateBatchDelete) >> Entry");

        // Create a response object
        ResponseDto response = new ResponseDto();

        try {
            // Find trainer details by batch ID
            List<Trainers> trainerDetails = trainerRepo.findByBatchIdInBatches(id);

            if (trainerDetails.size() != 0) {
                // Iterate through each trainer

                for (Trainers temp : trainerDetails) {
                    Iterator<TrainerBatch> iterator = temp.getBatches().iterator();
                    while (iterator.hasNext()) {
                        TrainerBatch batch = iterator.next();
                        if (batch.getBatch_id()!= null){
                            if (batch.getBatch_id().equals(id)) {
                                iterator.remove();
                                trainerRepo.save(temp);
                                // If you want to remove only one occurrence, break after removal
                                break;
                            }
                        }
                    }
                }

                // Log the exit point of the function
                logger.info("TrainerController(updateBatchDelete) >> Exit");

                // Set the success flag of the response
                response.setSuccess(true);
                // Set the message of the response
                response.setMessage("Update batch deleted successfully");
                // Set the data of the response
                response.setData(new Gson().toJson(trainerDetails));

                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                // Set the success flag of the response
                response.setSuccess(false);
                // Set the message of the response
                response.setMessage("Trainers not available for this batch");
                // Set the data of the response
                response.setData(null);

                // Log the exit point of the function
                logger.info("TrainerController(updateBatchDelete) >> Exit");

                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            // Print the stack trace
            e.printStackTrace();

            // Set the success flag of the response
            response.setSuccess(false);
            // Set the message of the response
            response.setMessage("Failed to Update batch delete");
            // Set the data of the response
            response.setData(null);

            // Log the exit point of the function
            logger.info("TrainerController(updateBatchDelete) >> Exit");

            return new ResponseEntity<ResponseDto>(response, HttpStatus.EXPECTATION_FAILED);
        }
    }

}
