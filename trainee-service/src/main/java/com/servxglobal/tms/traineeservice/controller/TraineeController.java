package com.servxglobal.tms.traineeservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.servxglobal.tms.traineeservice.dto.*;
import com.servxglobal.tms.traineeservice.model.Trainee;
import com.servxglobal.tms.traineeservice.model.TraineeBatch;
import com.servxglobal.tms.traineeservice.repository.TraineeRepo;
import com.servxglobal.tms.traineeservice.utils.Constants;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.servxglobal.tms.traineeservice.model.TraineeBatch;
import com.servxglobal.tms.traineeservice.otherService.BatchServiceClient;

import java.util.*;

@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/trainee")
public class TraineeController {

    @Autowired
    private TraineeRepo traineeRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private BatchServiceClient batchServiceClient;


    private static final Logger logger = LoggerFactory.getLogger(TraineeController.class);
    /**
     * This function handles the "register trainee" API.
     * It reads the data from request body
     * The function returns the newly registered trainee.
     */
    @PostMapping("/register")
    public ResponseEntity<SuccessandMessageDto> traineeRegister(@RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname,@RequestParam("email") String email, @RequestParam("password") String password,
                                                                @RequestParam("address") String address,@RequestParam("contact_number") String contact_number,@RequestParam("alternate_number") String alternate_number,
                                                                @RequestParam("branch") String branch,@RequestParam("branch_id") Long branch_id,@RequestParam("branchCode") String branch_code,
                                                                @RequestParam("dateOfBirth") Date Date_of_birth,@RequestParam("gender") String gender,
                                                                @RequestParam("image") MultipartFile image,@RequestParam("image_name") String image_name,@RequestParam("batches") String batches) {
        logger.info("TraineeController(traineeRegister) >> Entry");
        SuccessandMessageDto response = new SuccessandMessageDto();
//        if(traineeRepo.findByEmail(email).isPresent()) {
//            response.setMessage("Email is already registered !!");
//            response.setSuccess(false);
//            return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.BAD_REQUEST);
//        }
        Optional<Trainee> isEmailPresent = traineeRepo.findByEmail(email);
        if(isEmailPresent.isPresent())  {
            response.setMessage("Email is already registered !!");
            response.setSuccess(false);
//            response.setData(userEmail);
            return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.OK);
        }
        Trainee traineeData = new Trainee();

        try {
            List<Trainee> allTrainee = traineeRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
            if(allTrainee.isEmpty()) {
                traineeData.setId(1L); // creating db id
            } else {
                traineeData.setId(allTrainee.get(0).getId()+1); // creating db id
            }
            traineeData.setFirstname(firstname);
            traineeData.setLastname(lastname);
            traineeData.setPassword(passwordEncoder.encode(password));
            traineeData.setEmail(email);
            traineeData.setAddress(address);
            traineeData.setContact_number(contact_number);
            traineeData.setAlternate_number(alternate_number);
            traineeData.setGender(gender);
            traineeData.setDate_of_birth(Date_of_birth);
            traineeData.setBranch_id(branch_id);
            traineeData.setBranch(branch);
            TraineeBatch trainee = new TraineeBatch();
            Long removeBatchId = 0L;
            String batch_data = batches;
            if (!batch_data.equals("[]")) {

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(batch_data);
                for (JsonNode jsonObject : jsonNode) {
                    trainee.setBatch_id(jsonObject.get("batch_id").asLong());
                trainee.setBatch_name(jsonObject.get("batch_name").asText());
                trainee.setBatch_status(jsonObject.get("batch_status").asText());
                }
            }
            traineeData.setTraineeBatch(trainee);
            traineeData.setLogo_Img(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
            traineeData.setLogo_Img_name(image_name);
            traineeData.setBranchCode(branch_code);
            traineeData.setCreate_time(new Date());
            traineeData.setModified_time(new Date());

//        Enrollment id generation code
            String prefix = "TF"; // prefix for now its hardcoded
            String constantdigit = "0";    // third number is constant always
            String[] alphabetics = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"}; // alphabets to find the index for branch code eg: CBE --> C+B = 3(index of C)+2(index of B) = 5

            String branchCode = null;
            List<Trainee> allTraineeByBranch = traineeRepo.findTraineeByBranch(branch); // to get branch to find its size
            Integer branchCount = allTraineeByBranch.size(); // 6th digit in id will be increment according to the branch size below is code for 6th digit

            // this code to perform 5th digit in id to get branch_code number CBE -> split first number, find its index in above alphabetical order and add them
            String requestedBranchCode = branch_code; // get branch code from request
            String firstBranchCode = requestedBranchCode.substring(0, 1); // split first two letters
            String secondBranchCode = requestedBranchCode.substring(1, 2);
            List<String> list = Arrays.asList(alphabetics); // convert array to list
            int branchIndex = list.indexOf(firstBranchCode); // find index of first  letters in alphabetics array
            int branchSecondIndex = list.indexOf(secondBranchCode);
            Long finalIndexOfBranchCode = Long.valueOf((branchIndex + 1) + (branchSecondIndex + 1));

            // first three digit prefix = TF for above hardcoded value split and find its index and then added
            String firstPrefixCode = prefix.substring(0, 1); // split first two letters
            String secondPrefixCode = prefix.substring(1, 2);// find index of second  letters in alphabetics array
            int prefixFirstIndex = list.indexOf(firstPrefixCode); // find index for T
            int prefixSecondIndex = list.indexOf(secondPrefixCode); // find index of F
            String value1 = String.valueOf(prefixFirstIndex + 1);
            String value2 = String.valueOf(prefixSecondIndex + 1);
            String prefixId = value1.concat(value2);
            String middleDigit = new String();
            String firstIndex = new String();
            int lastChar = 0;
            List<Trainee> totalBranchData = traineeRepo.sortResult(requestedBranchCode); // get all data based on branch which we currently to added
            if(!totalBranchData.isEmpty() ) {
                int SortedArray = totalBranchData.size() - 1; // to get lastly added index
                Trainee idList = totalBranchData.get(SortedArray); // get lastly added details
                firstIndex = String.valueOf(idList.getEnrollment_id()); // get enrollment_id from first index

                // to get middle incremental digit eg: 001 or 011 or 133
                int startIndex = firstIndex.length() - 5; // return remaining value that subtract by 5 if length is 10 then 10-5 = 5 will return
                String result = firstIndex.substring(startIndex); // will get last five digit eg:00186
                middleDigit = result.substring(0, 3); // will return first three digit in result string
                // last digit which is hardcoded for first time and then if one branch reaches 999 this last digit should get increment and proceed with that incremented value
                String trimmedFirstIndex = firstIndex.trim(); // Remove leading and trailing whitespaces
                lastChar = Character.getNumericValue(firstIndex.charAt(trimmedFirstIndex.length() - 1)); // to get present or last added value's last digit
            }
            // generate middle incremental digit
            if(branchCount == 0 || middleDigit.equals("999") || totalBranchData.isEmpty()) { // this is for middle digit that increments
                branchCode = "001";
            } else {
                Integer lastNumber = Integer.parseInt(middleDigit); // output will be single digit ie. without 0
                lastNumber+=1; //add 1 with that digit
                branchCode = String.format("%03d",lastNumber); // add 0 with that digit
            }

//            // last digit which is hardcoded for first time and then if one branch reaches 999 this last digit should get increment and proceed with that incremented value
//            String trimmedFirstIndex = firstIndex.trim(); // Remove leading and trailing whitespaces
//            int lastChar = Character.getNumericValue(firstIndex.charAt(trimmedFirstIndex.length() - 1)); // to get present or last added value's last digit
            int suffixId;
            if(totalBranchData.isEmpty()) {
                suffixId = Constants.idSuffix; // this is hardcoded value for newly created branch adding first person
            } else {
                suffixId = lastChar; // else if already trainees present previous trainee's enroll id last digit will be placed
            }

            // increment the last digit if middle three digit reaches 999
            if((middleDigit.equals("999"))) { // this is for last digit if it reaches 999 trainee in particular branch
//                int lastDigit = firstIndex.length() -1;
                long number = Long.parseLong(firstIndex);
                long lastDigit = number % 10;
                int lastDigitAsInt = (int) lastDigit;// to get last digit '6' from enrollment_id(10 digit)
                suffixId = lastDigitAsInt + 1;
            }

            // Final output of enrollment id - generated by all above value
            Long traineeId = Long.valueOf(prefixId + constantdigit + finalIndexOfBranchCode + branchCode + 8 + suffixId);
            traineeData.setEnrollment_id(traineeId);
            Trainee saveTrainee = traineeRepo.save(traineeData);
            if(!batch_data.equals("[]")) {
                ResponseDto updatetraineeINBatches = batchServiceClient.updateBatchInTraineeDetails(trainee.getBatch_id(), saveTrainee.getId(),removeBatchId);
            }
            response.setMessage("Profile Created Successfully !!");
            response.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.UNAUTHORIZED);
        }

        logger.info("TraineeController(traineeRegister) >> Exit");
        return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all the trainees details.
     *
     * @return ResponseEntity<List<Trainees>> - The response entity containing the list of trainees.
     */
    @GetMapping("/get-all-trainee")
    public ResponseEntity<List<Trainee>> getAllTrainee() {
        logger.info("TraineeController(getAllTrainee) >> Entry");
        try {
            List<Trainee> traineeDetails = traineeRepo.findAll();
            logger.info("TraineeController(getAllTrainee) >> Exit");
            return new ResponseEntity<List<Trainee>>(traineeDetails, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("TraineeController(getAllTrainee) >> Exit");
        }
        return null;
    }
    /**
     * Retrieves all active trainees from the database.
     *
     * @return A ResponseEntity containing a list of trainees.
     */
    @GetMapping("/get-active-trainee")
    public ResponseEntity<List<TraineeDto>> getActiveTrainee() {
        logger.info("TraineeController(getActiveTrainee) >> Entry");
        try{
            List<Trainee> traineeDetails = traineeRepo.findActiveTrainee();
            List<TraineeDto> trainee_data = new ArrayList<>();
            for(Trainee i : traineeDetails)  {
                TraineeDto custom_data = new TraineeDto();
                custom_data.setId(i.getId());
                custom_data.setFirstname(i.getFirstname());
                custom_data.setLastname(i.getLastname());
                custom_data.setEmail((i.getEmail()));
                custom_data.setEnrollment_id(i.getEnrollment_id());
                if (i.getTraineeBatch()!= null){
                    custom_data.setBatch_id(i.getTraineeBatch().getBatch_id());
                    custom_data.setBatch_name(i.getTraineeBatch().getBatch_name());
                    custom_data.setBatch_status(i.getTraineeBatch().getBatch_status());
                }
                custom_data.setContact_number(i.getContact_number());
                custom_data.setPayment_status(i.isPayment_status());
                custom_data.setBranch(i.getBranch());
                trainee_data.add(custom_data);
            }
            logger.info("TraineeController(getActiveTrainee) >> Exit");
            return new ResponseEntity<List<TraineeDto>>(trainee_data, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TraineeController(getActiveTrainee) >> Exit");
        }
        return null;
    }

    @GetMapping("/get-deactive-trainee")
    public ResponseEntity<List<TraineeDto>> getAllDeactiveTrainee() {
        logger.info("TraineeController(getAllDeactiveTrainee) >> Entry");
        try {
            List<Trainee> traineeDetails = traineeRepo.findDeactiveTrainee();
            List<TraineeDto> trainee_data = new ArrayList<>();
            for(Trainee i : traineeDetails)  {
                TraineeDto custom_data = new TraineeDto();
                custom_data.setId(i.getId());
                custom_data.setFirstname(i.getFirstname());
                custom_data.setLastname(i.getLastname());
                custom_data.setEmail((i.getEmail()));
                custom_data.setEnrollment_id(i.getEnrollment_id());
                if (i.getTraineeBatch()!=null){
                    custom_data.setBatch_id(i.getTraineeBatch().getBatch_id());
                    custom_data.setBatch_name(i.getTraineeBatch().getBatch_name());
                }
                custom_data.setContact_number(i.getContact_number());
                custom_data.setPayment_status(i.isPayment_status());
                custom_data.setBranch(i.getBranch());
                trainee_data.add(custom_data);
            }
            logger.info("TraineeController(getAllDeactiveTrainee) >> Exit");
            return new ResponseEntity<List<TraineeDto>>(trainee_data,HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TraineeController(getAllDeactiveTrainee) >> Exit");
        }
        return null;
    }

    @GetMapping("/get-by-deactive-trainee/{id}")
    public ResponseEntity<ResponseDto> getDeactiveTraineeByDetails(@PathVariable Long id){
        logger.info("TraineeController(getDeactiveTraineeByDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try{
            Optional<Trainee> traineeData = traineeRepo.findById(id);
            Trainee traineeInfo = new Trainee();
            if(traineeData.isPresent()){
                traineeData.get().set_activated(false);
                logger.info("TraineeController(getDeactiveTraineeByDetails) >> Exit");
                traineeInfo = traineeRepo.save(traineeData.get());
                response.setSuccess(true);
                response.setMessage("Deactivate the trainee successfully");
                response.setData(new Gson().toJson(traineeInfo));
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                traineeInfo = null;
                response.setSuccess(false);
                response.setMessage("Failed to Deactivate the trainee");
                response.setData(null);
                logger.info("TraineeController(deactivateTrainee) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }

        } catch(Exception e){
            e.printStackTrace();
            logger.info("TraineeController(deactivateTrainee) >> Exit");
        }
        return null;
    }
    @GetMapping("/get-by-trainee-name")
    public ResponseEntity<ResponseDto> getGivenIdNameByDetails() {
        logger.info("TraineeController(getGivenIdNameByDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<?> traineeByIdNames = traineeRepo.findSpecificIdNameTraineeDetails();
            response.setSuccess(true);
            response.setMessage("Deactivate the trainee successfully");
            response.setData(new Gson().toJson(traineeByIdNames));
            logger.info("TraineeController(getGivenIdNameByDetails) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
        } catch(Exception e){
            logger.info("Exception in TraineeController(getGivenIdNameByDetails) >> Exit");
        }
        logger.info("TraineeController(getActiveTrainee) >> Exit");
        return null;
    }

    @GetMapping("/get-given-id-email-by-details")
    public ResponseEntity<ResponseDto> getGivenIdEmailByDetails() {
        logger.info("TraineeController(getGivenIdEmailByDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<?> trainerByIdNames = traineeRepo.findSpecificUserMailDetails();
            response.setSuccess(true);
            response.setMessage("Deactivate the trainee successfully");
            response.setData(new Gson().toJson(trainerByIdNames));
            logger.info("TraineeController(getGivenIdEmailByDetails) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
        } catch(Exception e) {

            logger.info("Exception in TraineeController(getGivenIdEmailByDetails) >> Exit");
        }
        logger.info("TraineeController(getGivenIdEmailByDetails) >> Exit");
        return null;
    }

    @GetMapping("/get-by-active-trainee/{id}")
    public ResponseEntity<ResponseDto> getActiveTraineeByDetails(@PathVariable Long id) {
        logger.info("TraineeController(getActiveTraineeByDetails) >> Entry");
        ResponseDto response = new ResponseDto();
        try{
            Optional<Trainee> traineeData = traineeRepo.findById(id);
            Trainee traineeInfo = new Trainee();
            if(traineeData.isPresent()){
                traineeData.get().set_activated(true);
                logger.info("TraineeController(getActiveTraineeByDetails) >> Exit");
                traineeInfo = traineeRepo.save(traineeData.get());
                response.setSuccess(true);
                response.setMessage("Deactivate the trainee successfully");
                response.setData(new Gson().toJson(traineeInfo));
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                traineeInfo = null;
                response.setSuccess(false);
                response.setMessage("Failed to Deactivate the trainee");
                response.setData(null);
                logger.info("TraineeController(deactivateTrainee) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }

        } catch(Exception e){
            e.printStackTrace();
            logger.info("TraineeController(deactivateTrainee) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves all active trainees by batch_id from the database.
     *
     * @return A ResponseEntity containing a trainees with batch_id .
     */
    @GetMapping("/get-trainee-by-batchid/{id}")
    public ResponseEntity<List<Trainee>> getTraineeByBatchId(@PathVariable Long id) {
        logger.info("TraineeController(getTraineeByBatchId) >> Entry");
        try {
            List<Trainee> traineeData = traineeRepo.findByBatchId(id);
            logger.info("TraineeController(getTraineeByBatchId) >> Exit");
            return new ResponseEntity<List<Trainee>>(traineeData,HttpStatus.OK);
        }catch (Exception e ) {
            e.printStackTrace();
            logger.info("TraineeController(getTraineeByBatchId) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves the trainee details for the given ID.
     *
     * @param  id the ID of the trainee
     * @return    the trainee details
     */
    @GetMapping("/get-by-trainee/{id}")
    public ResponseEntity<ResponseDto> getTraineeById(@PathVariable Long id) {
        logger.info("TraineeController(getTraineeById) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Trainee> traineeDetails = traineeRepo.findById(id);
//            adminEntity.setPassword(passwordEncoder.encode(traineeDetails.));
            logger.info("TraineeController(getTraineeById) >> Exit");
            responseDto.setSuccess(true);
            responseDto.setMessage("trainee details got successful !!");
            responseDto.setData(new Gson().toJson(traineeDetails.get()));
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("TraineeController(getTraineeById) >> Exit");
        }
        return null;
    }

    /**
     * Update the trainee details by the given ID.
     *
     * @param id     The ID of the trainee to update.
     * @requestbody  trainee new details to update
     * @return       The updated trainee if successful, or null if an exception occurred.
     */
    @PostMapping("/update-trainee")
    public ResponseEntity<ResponseDto> updateTrainee(@RequestParam("id") Long id ,@RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname,@RequestParam("email") String email,
                                                     @RequestParam("address") String address,@RequestParam("contact_number") String contact_number,@RequestParam("alternate_number") String alternate_number,
                                                     @RequestParam("branch") String branch,@RequestParam("branch_id") Long branch_id,@RequestParam("branchCode") String branch_code,
                                                     @RequestParam("dateOfBirth") Date Date_of_birth,@RequestParam("gender") String gender,
                                                     @RequestParam("image") MultipartFile image,@RequestParam("image_name") String image_name,@RequestParam("batches") String batches) {
        logger.info("TraineeController(updateTrainee) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Trainee> trainee = traineeRepo.findById(id);
            Long OldTraineeBatchId;
            String oldTraineeBatch = String.valueOf(trainee.get().getTraineeBatch().getBatch_id());
            if(oldTraineeBatch == "null"){
                OldTraineeBatchId = 0L;
            } else {
                OldTraineeBatchId = Long.valueOf(oldTraineeBatch);
            }
            Trainee traineeData = new Trainee();
            if(trainee.isPresent()) {
                trainee.get().setFirstname(firstname);
                trainee.get().setLastname(lastname);
                trainee.get().setEmail(email);
//                trainee.get().setPassword(traineeDetails.getPassword());
                trainee.get().setAddress(address);
                trainee.get().setContact_number(contact_number);
                trainee.get().setAlternate_number(alternate_number);
                trainee.get().setGender(gender);
                trainee.get().setDate_of_birth(Date_of_birth);
                trainee.get().setBranch_id(branch_id);
                trainee.get().setBranch(branch);
                trainee.get().setBranchCode(branch_code);
                String batch_data = batches;
                TraineeBatch traineeBatch = new TraineeBatch();
                if (!batch_data.equals("[]")){
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(batch_data);
                    for (JsonNode jsonObject : jsonNode) {
                        traineeBatch.setBatch_id(jsonObject.get("batch_id").asLong());
                        traineeBatch.setBatch_name(jsonObject.get("batch_name").asText());
                        traineeBatch.setBatch_status(jsonObject.get("batch_status").asText());
                    }
                }
                trainee.get().setTraineeBatch(traineeBatch);
                trainee.get().setLogo_Img(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
                trainee.get().setLogo_Img_name(image_name);
                trainee.get().setModified_time(new Date());
                traineeData = traineeRepo.save(trainee.get());
                response.setSuccess(true);
                response.setMessage("trainee update successfully");
                response.setData(new Gson().toJson(traineeData));
                if(!batch_data.equals("[]")) {
                    if (OldTraineeBatchId != traineeBatch.getBatch_id()){
                        ResponseDto updatetraineeINBatches = batchServiceClient.updateBatchInTraineeDetails(traineeBatch.getBatch_id(), traineeData.getId(), OldTraineeBatchId);
                }
                }
                logger.info("TraineeController(updateTrainee) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                traineeData = null;
                response.setSuccess(false);
                response.setMessage("trainee update failed");
                response.setData(new Gson().toJson(traineeData));
                logger.info("TraineeController(updateTrainee) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TraineeController(updateTrainee) >> Exit");
        }
        return null;
    }
    /**
     * Soft deletes a trainee by setting its "_deleted" flag to true.
     *
     * @param id The ID of the trainee to delete.
     * @return The updated trainee details if found, or a bad request response if not.
     */
    @PutMapping("/delete-trainee/{id}")
    public ResponseEntity<ResponseDto> deleteTrainee(@PathVariable Long id) {
        logger.info("TraineeController(deleteTrainee) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Trainee> traineeDetails = traineeRepo.findById(id);
            Trainee traineeData = new Trainee();
            if(traineeDetails.isPresent()) {
                ResponseDto responseBatch = batchServiceClient.traineeDeleteUpdateBatch(id);
                if (responseBatch.isSuccess()){
                    traineeDetails.get().set_deleted(true);
                    traineeData = traineeRepo.save(traineeDetails.get());
                    logger.info("TraineeController(deleteTrainee) >> Exit");
                    response.setSuccess(true);
                    response.setMessage("Delete the trainee successfully");
                    response.setData(new Gson().toJson(traineeData));
                    return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
                }
                else {
                    logger.info("TraineeController(deleteTrainee) >> Exit");
                    response.setSuccess(false);
                    response.setData(null);
                    response.setMessage("Failed to delete the trainee");
                    response.setData(new Gson().toJson(traineeData));
                    return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
                }

            } else {
                traineeData = null;
                response.setSuccess(false);
                response.setMessage("Failed to delete the trainee");
                response.setData(null);
                logger.info("TraineeController(deleteTrainee) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.BAD_REQUEST);
            }
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("TraineeController(deleteTrainee) >> Exit");
        }
        return null;
    }
    /**
     * Deletes a trainee permanently by its ID.
     *
     * @param id The ID of the trainee to be deleted.
     * @return A ResponseEntity with a message indicating the result of the deletion.
     */
    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<String> hardDeleteTrainee(@PathVariable Long id) {
        logger.info("TraineeController(hardDeleteTrainee) >> Entry");
        try {
            Optional<Trainee> traineeDetails = traineeRepo.findById(id);
            if(traineeDetails.isPresent()) {
                traineeRepo.deleteById(id);
                logger.info("TraineeController(hardDeleteTrainee) >> Exit");
                return  new ResponseEntity<String>("Deleted Successfully", HttpStatus.OK);
            } else  {
                logger.info("TraineeController(hardDeleteTrainee) >> Exit");
                return  new ResponseEntity<String>("Failed to delete trainee", HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TraineeController(hardDeleteTrainee) >> Exit");
        }
        return null;
    }
    @PutMapping("/bulk-delete")
    public ResponseEntity<BulkActionDto> bulkTraineeDelete(@RequestBody String request) {
        logger.info("TraineeController(bulkTraineeDelete) >> Entry");
        JSONObject newJsonObject = new JSONObject(request);
        JSONArray newDetails = newJsonObject.getJSONArray("deleteIds");
        BulkActionDto response = new BulkActionDto();
        List<Long> traineelist = new ArrayList<Long>();
        for (int i = 0; i < newDetails.length(); i++) {
            traineelist.add(Long.parseLong(String.valueOf(newDetails.get(i))));
        }
        try {
            for (int i = 0; i < traineelist.size(); i++) {
                Optional<Trainee> traineeDetails = traineeRepo.findById(traineelist.get(i).longValue());
                Trainee traineeInfo = new Trainee();
                if (traineeDetails.isPresent()) {
                    ResponseDto responseBatch = batchServiceClient.traineeDeleteUpdateBatch(traineeDetails.get().getId());
                    if (responseBatch.isSuccess()){
                        traineeDetails.get().set_deleted(true);
                        traineeInfo = traineeRepo.save(traineeDetails.get());
                    }
                } else {
                    traineeInfo = null;
                    response.setSuccess(false);
                    response.setMessage("Failed to delete bulk trainees");
                    return new ResponseEntity<BulkActionDto>(response, HttpStatus.NOT_FOUND);
                }
            }
            response.setSuccess(true);
            response.setMessage("Bulk trainees deleted successfully");
            logger.info("TraineeController(bulkTraineeDelete) >> Exit");
            return new ResponseEntity<BulkActionDto>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Exception in bulk delete trainer");
            logger.info("TraineeController(bulkTraineeDelete) >> Exit");
            return new ResponseEntity<BulkActionDto>(response, HttpStatus.EXPECTATION_FAILED);
        }
    }
    @PutMapping("/bulk-deactivate")
    public ResponseEntity<BulkActionDto> bulkDeactivateTrainee(@RequestBody String request) {
        logger.info("TraineeController(bulkDeactivateTrainee) >> Entry");
        JSONObject newJsonObject = new JSONObject(request);
        JSONArray newDetails = newJsonObject.getJSONArray("deleteIds");
        List<Long> traineelist = new ArrayList<Long>();
        BulkActionDto response = new BulkActionDto();
        for (int i = 0; i < newDetails.length(); i++) {
            traineelist.add(Long.parseLong(String.valueOf(newDetails.get(i))));
        }
        try {
            for (int i = 0; i < traineelist.size(); i++) {
                Optional<Trainee> traineeDetails = traineeRepo.findById(traineelist.get(i));
                Trainee traineeInfo = new Trainee();
                if (traineeDetails.isPresent()) {
                    traineeDetails.get().set_activated(false);
                    traineeInfo = traineeRepo.save(traineeDetails.get());
                    response.setSuccess(true);
                    response.setMessage("Bulk trainees deactivated successfully");
                } else {
                    traineeInfo = null;
                    response.setSuccess(false);
                    response.setMessage("Failed to deactivated bulk trainees");
                    return new ResponseEntity<BulkActionDto>(response, HttpStatus.BAD_REQUEST);
                }
            }
            logger.info("TraineeController(bulkDeactivateTrainer) >> Exit");
            return new ResponseEntity<BulkActionDto>(response, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();

        }
        return null;
    }
    @PutMapping("/bulk-activate")
    public ResponseEntity<BulkActionDto> bulkActivateTrainee(@RequestBody String request) {
        logger.info("TraineeController(bulkActivateTrainee) >> Entry");
        JSONObject newJsonObject = new JSONObject(request);
        JSONArray newDetails = newJsonObject.getJSONArray("deleteIds");
        List<Long> traineelist = new ArrayList<Long>();
        BulkActionDto response = new BulkActionDto();
        for (int i = 0; i < newDetails.length(); i++) {
            traineelist.add(Long.parseLong(String.valueOf(newDetails.get(i))));
        }
        try {
            for (int i = 0; i < traineelist.size(); i++) {
                Optional<Trainee> traineeDetails = traineeRepo.findById(traineelist.get(i));
                Trainee traineeInfo = new Trainee();
                if (traineeDetails.isPresent()) {
                    traineeDetails.get().set_activated(true);
                    traineeInfo = traineeRepo.save(traineeDetails.get());
                    response.setSuccess(true);
                    response.setMessage("Bulk trainees activated successfully");
                } else {
                    traineeInfo = null;
                    response.setSuccess(false);
                    response.setMessage("Failed to activate bulk trainees");
                    return new ResponseEntity<BulkActionDto>(response, HttpStatus.BAD_REQUEST);
                }
            }
            logger.info("TraineeController(bulkActivateTrainee) >> Exit");
            return new ResponseEntity<BulkActionDto>(response, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();

        }
        return null;
    }
    // Update batch name in trainee
    @PutMapping ("/update-trainee-batch")
    public ResponseEntity<ResponseDto> updateTrainee(@RequestBody BatchTraineeDto batchDetails, @RequestParam("id") Long id) {
        logger.info("TraineeController(updateTrainee) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Trainee> trainee = traineeRepo.findById(id);
            JSONObject newJsonObject = new JSONObject(batchDetails);
            Long batchId = newJsonObject.getLong("batch_id");
            String batchName = newJsonObject.getString("batch_name");
            Trainee traineeData = new Trainee();
            if(trainee.isPresent()) {
                trainee.get().getTraineeBatch().setBatch_id(batchId);
                trainee.get().getTraineeBatch().setBatch_name(batchName);
                traineeData = traineeRepo.save(trainee.get());
                logger.info("TraineeController(updateTrainee) >> Exit");
            }  else {
                traineeData = null;
                response.setSuccess(false);
                response.setMessage("trainee update failed");
                response.setData(new Gson().toJson(traineeData));
                logger.info("TraineeController(updateTrainee) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/update-batch-status/{status}/{id}")
    public ResponseEntity<ResponseDto> updateBatchStatus(@PathVariable Long id, @PathVariable String status) {
        logger.info("TraineeController(updateBatchStatus) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<Trainee> traineeDetails = traineeRepo.getSpecificBatchDetails(id);
            if(traineeDetails.size() != 0) {
                for (Trainee temp: traineeDetails ) {
                    temp.getTraineeBatch().setBatch_status(status);
                    traineeRepo.save(temp);
                }
                logger.info("TraineeController(updateBatchStatus) >> Exit");
                response.setSuccess(true);
                response.setMessage("Update batch status successfully");
                response.setData(new Gson().toJson(traineeDetails));
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                response.setSuccess(false);
                response.setMessage("Trainees not available for this batch");
                response.setData(null);
                logger.info("TraineeController(updateBatchStatus) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
        } catch(Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Failed to update batch status");
            response.setData(null);
            logger.info("TraineeController(updateBatchStatus) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * Updates the batch details for a list of trainees.
     *
     * @param batchDetails the batch details to be updated
     * @param ids          the list of trainee IDs
     * @param removedIds   the list of IDs of removed trainees
     * @return the response entity containing the updated batch details
     */
    @PutMapping("/trainees-batch-update")
    public ResponseEntity<ResponseDto> traineesBatchUpdate(
            @RequestBody BatchTraineeDto batchDetails,
            @RequestParam("ids") List<Long> ids,
            @RequestParam("RemovedIds") List<Long> removedIds) {
        logger.info("TraineeController(traineesBatchUpdate) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            // Update the batch details for the removed trainees
            List<Trainee> missedTrainees = traineeRepo.findByIdIn(removedIds);
            if (missedTrainees.size() > 0) {
                for (Trainee iterationData : missedTrainees) {
                    TraineeBatch temp = new TraineeBatch();
                    iterationData.setTraineeBatch(temp);
                    traineeRepo.save(iterationData);
                }
            }

            // Update the batch details for the trainees
            List<Trainee> trainees = traineeRepo.findByIdIn(ids);
            if (trainees.size() > 0) {
                for (Trainee iterationData : trainees) {
                    if (iterationData.getTraineeBatch() == null || iterationData.getTraineeBatch().getBatch_id() == null ||
                            !(iterationData.getTraineeBatch().getBatch_id().equals(batchDetails.getBatch_id()))) {
                        TraineeBatch temp = new TraineeBatch();
                        temp.setBatch_id(batchDetails.getBatch_id());
                        temp.setBatch_name(batchDetails.getBatch_name());
                        temp.setBatch_status("Active");
                        iterationData.setTraineeBatch(temp);
                        traineeRepo.save(iterationData);
                    }
                }
                response.setSuccess(true);
                response.setMessage("Batch details updated in trainees");
                response.setData("Batch details updated in trainees");
            } else {
                response.setSuccess(true);
                response.setMessage("Trainees not found");
                response.setData("Trainees not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Trainees batch update failed");
            response.setData("Trainees batch update failed");
        }
        return null;
    }
    // To show trainers images in get batch details dialog box onclick batch name in batch table

    /**
     *get trainee image by id.
     *
     * @param id   trainee ID
     * @return the response entity containing the trainee image
     */
    @GetMapping("/get-trainees-image/{id}")
    public ResponseEntity<ResponseDto> getTraineeImages(@PathVariable Long id) {
        logger.info("TraineeController(getTraineeImages) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        List<Optional<Trainee>> trainersImageList = new ArrayList<>();
        try {
            Optional<Trainee> traineeDetails = traineeRepo.findById(id);
            Trainee trainerInfo = new Trainee();
            if (traineeDetails.isPresent()) {
                Optional<Trainee> trainerData = traineeRepo.getImages(id);
                if (trainerData.isPresent()) {
                    Binary imageData = trainerData.get().getLogo_Img();
                    responseDto.setSuccess(true);
                    responseDto.setMessage("Trainee details got successful !!");
                    responseDto.setData(new Gson().toJson(imageData));
                    logger.info("TraineeController(getTraineeImages) >> Exit");
                    return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TraineeController(getTraineeImages) >> Exit");
        }
        return null;
    }
    // To get trainer details by batch id to show in batch dialog box
    /**
     *get trainee required(name, email, contact number, branch and logo image)details by id.
     *
     * @param id   trainee ID
     * @return the response entity containing the trainee details
     */
    @GetMapping("/get-trainee-for-batch/{id}")
    public ResponseEntity<ResponseDto> getTraineeDetailsForBatch(@PathVariable Long id) {
        logger.info("TraineeController(getTraineeDetailsForBatch) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Trainee> trainerDetails = traineeRepo.findById(id);
            Trainee trainerInfo = new Trainee();
            if (trainerDetails.isPresent()) {
                Optional<Trainee> traineeData = traineeRepo.getTraineeDetailsForBatch(id);
                responseDto.setSuccess(true);
                responseDto.setMessage("Trainer details got successful !!");
                responseDto.setData(new Gson().toJson(traineeData.get()));
                logger.info("TraineeController(getTraineeDetailsForBatch) >> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("TraineeController(getTraineeDetailsForBatch) >> Exit");
        }
        return null;
    }

    /**
     * Update batch delete for a specific ID.
     *
     * @param  id   the ID of the batch to update
     * @return      the response entity containing the response dto
     */
    @GetMapping("/update-batch-delete/{id}")
    public ResponseEntity<ResponseDto> updateBatchDelete(@PathVariable Long id) {
        logger.info("TraineeController(updateBatchDelete) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            List<Trainee> traineeDetails = traineeRepo.getSpecificBatchDetails(id);
            if(traineeDetails.size() != 0) {
                for (Trainee temp: traineeDetails ) {
                    temp.setBatch_complete_status(false);
                    temp.setTraineeBatch(null);
                    traineeRepo.save(temp);
                }
                logger.info("TraineeController(updateBatchDelete) >> Exit");
                response.setSuccess(true);
                response.setMessage("Update batch delete successfully");
                response.setData(new Gson().toJson(traineeDetails));
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                response.setSuccess(false);
                response.setMessage("Trainees not available for this batch");
                response.setData(null);
                logger.info("TraineeController(updateBatchDelete) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            }
        } catch(Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("Failed to delete batch status");
            response.setData(null);
            logger.info("TraineeController(updateBatchDelete) >> Exit");
            return new ResponseEntity<ResponseDto>(response, HttpStatus.EXPECTATION_FAILED);
        }
    }
}
