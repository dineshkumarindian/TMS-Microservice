package com.servxglobal.tms.courseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.servxglobal.tms.courseservice.dto.ResponseDto;
import com.servxglobal.tms.courseservice.dto.StreamCourse;
import com.servxglobal.tms.courseservice.model.Stream;
import com.servxglobal.tms.courseservice.otherService.BatchServiceClient;
import com.servxglobal.tms.courseservice.repository.StreamRepo;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stream")
public class StreamController {
    @Autowired
    private StreamRepo streamRepo;

    @Autowired
    private BatchServiceClient batchServiceClient;

    /**
     * This function handles the "add stream" API.
     * The function returns the newly created stream.
     */
    private static final Logger logger = LoggerFactory.getLogger(StreamController.class);
    @PostMapping("/add-stream")
    public ResponseEntity<ResponseDto> addStream(@RequestParam("title") String Stream,@RequestParam("image") MultipartFile image,@RequestParam("CourseData")  Object CourseData) {
        ResponseEntity<ResponseDto> responseEntity;
        ResponseDto responseDto = new ResponseDto();
        logger.info("StreamController(addStream) >> Entry");
        try{
            Stream streamDetails = new Stream();
            streamDetails.setStream_name(Stream);
            streamDetails.setLogo_Img(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
            streamDetails.setCreated_time(new Date());
            streamDetails.setModified_time(new Date());
            streamDetails.setCourseDetailsWithLevel(CourseData);
            List<Stream> allStream = streamRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
            if(allStream.isEmpty()) {
                streamDetails.setId(1L);
            } else {
                streamDetails.setId(allStream.get(0).getId()+1);
            }
            Stream data = streamRepo.save(streamDetails);
            logger.info("StreamController(addStream)>> Exit");
            responseDto.setSuccess(true);
            responseDto.setMessage("stream added successful !!");
            responseDto.setData(String.valueOf(data));
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);

        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(addStream) >> Entry");
        }
        return null;
    }

    /**
     * Retrieves all active streams from the database.
     *
     * @return A ResponseEntity containing a list of stream objects.
     */
    @GetMapping("/active-get-all-streams")
    public ResponseEntity<List<Stream>> getAllActiveStreams() {
        logger.info("StreamController(getAllActiveStreams) >> Entry");
        try {
            List<Stream> streamList = streamRepo.findAllActiveStreams();
            logger.info("CourseController(getAllActiveCourses)>> Exit");
            return new ResponseEntity<List<Stream>>(streamList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(getAllActiveStreams)>> Exit");
        }
        return null;
    }

    @GetMapping("/get-name-in-streams")
    public ResponseEntity<ResponseDto> getNameInStreams(){
        logger.info("StreamController(getNameInStreams) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try{
            List<?> getTheNameStreams = streamRepo.findSpecificStreamNameDetails();
            responseDto.setSuccess(true);
            responseDto.setMessage("All stream names fetched successfully ");
            responseDto.setData(new Gson().toJson(getTheNameStreams));
            logger.info("CourseController(getNameInStreams)>> Exit");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(getNameInStreams)>> Exit");
        }
        return null;
    }

    /**
     * Retrieves all the stream details.
     *
     * @return ResponseEntity<List<stream>> - The response entity containing the list of streams.
     */
    @GetMapping("/get-all-streams")
    public ResponseEntity<List<Stream>> getAllStreams() {
        logger.info("StreamController(getAllStreams) >> Entry");
        try {
            List<Stream> streamList = streamRepo.findAll();
            logger.info("StreamController(getAllStreams)>> Exit");
            return new ResponseEntity<List<Stream>>(streamList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(getAllStreams)>> Exit");
        }
        return null;
    }
    @GetMapping("/get-by-streams/{id}")
    public ResponseEntity<ResponseDto> getByStreamsId(@PathVariable Long id) {
        logger.info("StreamController(getByStreamsId) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Stream> streamList = streamRepo.findById(id);
            logger.info("StreamController(getByStreamsId)>> Exit");
            responseDto.setSuccess(true);
            responseDto.setMessage("course added successful !!");
            responseDto.setData(new Gson().toJson(streamList.get()));
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(getByStreamsId)>> Exit");
        }
        return null;
    }

    /**
     * Update the stream details by the given ID.
     *
     * @param id     The ID of the stream to update.
     * @return       The updated stream if successful, or null if an exception occurred.
     */
    @PostMapping("/update/{id}")
    public ResponseEntity<ResponseDto> updatestream(@PathVariable Long id, @RequestParam("title") String streamName,@RequestParam("image") MultipartFile image,@RequestParam("CourseData")  Object CourseData) {
        logger.info("StreamController(updateStream) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Stream> Stream = streamRepo.findById(id);
            if (Stream.isPresent()) {
                Stream.get().setStream_name(streamName);
                Stream.get().setCourseDetailsWithLevel(CourseData);
                Stream.get().setLogo_Img(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
                Stream.get().setModified_time(new Date());
                Stream updateStream = streamRepo.save(Stream.get());
                logger.info("StreamController(updateStream)>> Exit");
                responseDto.setSuccess(true);
                responseDto.setMessage("stream updated successful !");
                responseDto.setData(String.valueOf(updateStream));
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                Stream updateStream = null;
                logger.info("StreamController(updateStream)>> Exit");
                responseDto.setSuccess(false);
                responseDto.setMessage("stream update failed!");
                responseDto.setData(String.valueOf(updateStream));
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(updateStream)>> Exit");
            responseDto.setSuccess(false);
            responseDto.setMessage("stream update Exception occurred !");
            responseDto.setData("");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.BAD_REQUEST);

        }
//        return null;
    }

    /**
     * Soft deletes a stream by setting its "_deleted" flag to true.
     *
     * @param id The ID of the stream to delete.
     * @return The updated stream if found, or a bad request response if not.
     */
    @PutMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteStream(@PathVariable Long id) {
        logger.info("StreamController(deleteStream) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Stream> streamDetails = streamRepo.findById(id);
            Stream updateStream = new Stream();
            if (streamDetails.isPresent()) {
                streamDetails.get().set_deleted(true);
                updateStream = streamRepo.save(streamDetails.get());
                logger.info("StreamController(deleteStream)>> Exit");
                responseDto.setSuccess(true);
                responseDto.setMessage("stream deleted successfully !!");
                responseDto.setData(new Gson().toJson(streamDetails.get()));
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                updateStream = null;
                logger.info("StreamController(deleteStream)>> Exit");
//                return new ResponseEntity<Stream>(updateStream, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(deleteStream)>> Exit");
        }
        return null;
    }
    @GetMapping("/delete-stream-level/{stream_id}/{level_id}")
    public ResponseEntity<ResponseDto> deleteStreamWithLevel(@PathVariable Long stream_id,@PathVariable Long level_id) {
        logger.info("StreamController(deleteStreamWithLevel) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Stream> streamDetails = streamRepo.findById(stream_id);
            if (streamDetails.isPresent()) {
                Object streamWithLevel = streamDetails.get().getCourseDetailsWithLevel();
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = streamWithLevel.toString();
                StreamCourse[] courses = objectMapper.readValue(jsonString, StreamCourse[].class);
                List<StreamCourse> jsonList = new ArrayList<>();
                for (StreamCourse course : courses) {
                    jsonList.add(course);
                }
                for(int k=0;k<jsonList.size();k++) {
                    StreamCourse  courseData = jsonList.get(k);
                    if(courseData.getLevel_id() == level_id) {
                        jsonList.remove(courseData);
                        streamDetails.get().setCourseDetailsWithLevel( new Gson().toJson(jsonList));
                        Stream updateData = streamRepo.save(streamDetails.get());
                    }
                }
                responseDto.setSuccess(true);
                responseDto.setMessage("stream level deleted successful !!");
                logger.info("StreamController(deleteStreamWithLevel) >> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            responseDto.setSuccess(false);
            responseDto.setMessage("failed to delete stream levels!!");
            logger.info("Exception in StreamController(deleteStreamWithLevel) >> Exit");
            return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);

        }
        return null;
    }
    /**
     * Deletes a stream permanently by its ID.
     *
     * @param id The ID of the stream to be deleted.
     * @return A ResponseEntity with a message indicating the result of the deletion.
     */
    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<String> hardDeleteStreamById(@PathVariable Long id) {
        logger.info("StreamController(hardDeleteStreamById) >> Entry");
        try {
            Optional<Stream> streamDetails = streamRepo.findById(id);
            if (streamDetails.isPresent()) {
                streamRepo.deleteById(id);
                logger.info("StreamController(hardDeleteStreamById) >> Exit");
                return new ResponseEntity<String>("delete successfully", HttpStatus.OK);
            } else {
                logger.info("StreamController(hardDeleteCourseById) >> Exit");
                return new ResponseEntity<String>("data not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(hardDeleteStreamById)");
        }
        return null;
    }

    //Get course and level details by stream id to update the level
    @PostMapping("/update-stream-with-level/{id}")
    public ResponseEntity<ResponseDto> updateStreamWithLevel(@PathVariable Long id, @RequestParam("course_id") Long courseId, @RequestParam("selectedlevel") Long selectedLevel,
                                                             @RequestParam("level_id") Long level_id) {
        logger.info("StreamController(updateStreamWithLevel) >> Entry");
        ResponseDto responseDto = new ResponseDto();
        try {
            Optional<Stream> streamDetails = streamRepo.findById(id);
            if (streamDetails.isPresent()) {
                Object streamWithLevel = streamDetails.get().getCourseDetailsWithLevel();
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = streamWithLevel.toString();
                StreamCourse[] courses = objectMapper.readValue(jsonString, StreamCourse[].class);
                List<StreamCourse> jsonList = new ArrayList<>();
                for (StreamCourse course : courses) {
                    jsonList.add(course);
                }
                for(int k=0;k<jsonList.size();k++) {
                    StreamCourse  courseData = jsonList.get(k);
                    if(courseId.equals(courseData.getId() )) {
                        jsonList.get(k).setSelectedLevel(Math.toIntExact(selectedLevel));
                        jsonList.get(k).setLevel_id(Math.toIntExact(level_id));
                        streamDetails.get().setCourseDetailsWithLevel(new Gson().toJson(jsonList));
                        Stream updateData = streamRepo.save(streamDetails.get());
                    }
                }
                responseDto.setSuccess(true);
                responseDto.setMessage("Level updated successful !!");
                logger.info("StreamController(updateStreamWithLevel)>> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.OK);
            } else {
                responseDto.setSuccess(false);
                responseDto.setMessage("failed to update the level !!");
                logger.info("StreamController(updateStreamWithLevel)>> Exit");
                return new ResponseEntity<ResponseDto>(responseDto, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(updateStreamWithLevel)>> Exit");
        }
        return null;
    }

    /**
     * Get the list of Stream objects representing the courses associated with a trainer.
     *
     * @param trainerId The ID of the trainer.
     * @return A ResponseEntity containing the list of Stream objects.
     */
    @GetMapping("/getBatchCourseByTrainer/{trainerId}")
    public ResponseEntity<List<Stream>> getBatchCourseByTrainer(@PathVariable Long trainerId) {
        logger.info("StreamController(getBatchCourseByTrainer) >> Entry");

        // Initialize the response object
        ResponseDto response = new ResponseDto();

        // Initialize the list of Stream objects
        List<Stream> streamList = new ArrayList<>();

        try {
            // Call the batch service to get the course IDs associated with the trainer
            ResponseDto responseBatch = batchServiceClient.getCourseIdsByTrainer(trainerId);

            if (responseBatch.isSuccess()) {
                // Extract the course IDs from the response
                List<Long> resultList = new ArrayList<>();
                String[] elements = responseBatch.getData().substring(1, responseBatch.getData().length() - 1).split(",");
                for (String element : elements) {
                    resultList.add(Long.parseLong(element.trim()));
                }

                // Retrieve the Stream objects using the course IDs
                streamList = streamRepo.findByIdIn(resultList);

                // Update the response with the success status, data, and message
                response.setSuccess(true);
                response.setData(new Gson().toJson(streamList));
                response.setMessage("Trainer courses fetched successfully");

                logger.info("CourseController(getBatchCourseByTrainer)>> Exit");

                // Return the list of Stream objects
                return new ResponseEntity<List<Stream>>(streamList, HttpStatus.OK);
            } else {
                logger.info("CourseController(getBatchCourseByTrainer)>> Exit");

                // Update the response with the failure status and message
                response.setSuccess(false);
                response.setMessage(responseBatch.getMessage());

                // Return the empty list of Stream objects with the failure status
                return new ResponseEntity<List<Stream>>(streamList, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(getBatchCourseByTrainer)>> Exit");

            // Update the response with the failure status and message
            response.setSuccess(false);
            response.setMessage("Exception in getting Trainer courses");

            // Return the empty list of Stream objects with the failure status
            return new ResponseEntity<List<Stream>>(streamList, HttpStatus.EXPECTATION_FAILED);
        }
    }
    /**
     * Retrieves the list of courses associated with a trainee.
     *
     * @param traineeId The ID of the trainee.
     * @return ResponseEntity<List<Stream>> The response containing the list of courses.
     */
    @GetMapping("/getBatchCourseByTrainee/{traineeId}")
    public ResponseEntity<List<Stream>> getBatchCourseByTrainee(@PathVariable Long traineeId) {
        logger.info("StreamController(getBatchCourseByTrainee) >> Entry");
        ResponseDto response = new ResponseDto();
        List<Stream> streamList = new ArrayList<>();
        try {
            // Retrieve the course IDs associated with the trainee
            ResponseDto responseBatch = batchServiceClient.getCourseIdsByTrainee(traineeId);

            if (responseBatch.isSuccess()){
                List<Long> resultList = new ArrayList<>();

                // Remove square brackets and split by comma
                String[] elements = responseBatch.getData().substring(1, responseBatch.getData().length() - 1).split(",");

                // Parse string elements to Long and add to the result list
                for (String element : elements) {
                    resultList.add(Long.parseLong(element.trim()));
                }
                // Retrieve the streams with the matching IDs
                streamList = streamRepo.findByIdIn(resultList);
                response.setSuccess(true);
                response.setData(new Gson().toJson(streamList));
                response.setMessage("Trainee courses fetched successfully");
                logger.info("CourseController(getBatchCourseByTrainee)>> Exit");
                return new ResponseEntity<List<Stream>>(streamList, HttpStatus.OK);
            }else{
                logger.info("CourseController(getBatchCourseByTrainee)>> Exit");
                response.setSuccess(false);
                response.setMessage(responseBatch.getMessage());
                return new ResponseEntity<List<Stream>>(streamList, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in StreamController(getBatchCourseByTrainee)>> Exit");
            response.setSuccess(false);
            response.setMessage("Exception in getting Trainee courses");
            return new ResponseEntity<List<Stream>>(streamList, HttpStatus.EXPECTATION_FAILED);
        }
    }
}
