package com.servxglobal.tms.courseservice.controller;

import com.servxglobal.tms.courseservice.model.Topic;
import com.servxglobal.tms.courseservice.repository.TopicRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/course/topics")
public class TopicController {

    @Autowired
    private TopicRepo topicsRepo;
    private static final Logger logger = LoggerFactory.getLogger(TopicController.class);

    /* add the topics post API */
    @PostMapping("/add-topics")
    public ResponseEntity<Topic> addTopic(@RequestBody Topic topicsDetails) {
        logger.info("TopicController(addTopic) >> Entry");
        try{
            Topic topicsData = new Topic();
            topicsData.setTopic_name(topicsDetails.getTopic_name());
            topicsData.setCategory(topicsDetails.getCategory());
            topicsData.setCourse_id(topicsDetails.getCourse_id());
            topicsData.setCreated_time(new Date());
            topicsData.setModified_time(new Date());
            List<Topic> allTopic = topicsRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
            if(allTopic.isEmpty()) {
                topicsData.setId(1L);
            } else {
                topicsData.setId(allTopic.get(0).getId()+1);
            }
            Topic save_data = topicsRepo.save(topicsData);
            logger.info("TopicController(addTopic)>> Exit");
            return new ResponseEntity<Topic>(save_data, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in topicsController(addTopic) >> Entry");
        }
        return null;
    }

    /*get the topics active details */
    @GetMapping("/active-get-all-topics")
    public ResponseEntity<List<Topic>> softGetAllTopic() {
        logger.info("TopicController(getAllSoftTopic) >> Entry");
        try {
            List<Topic> topicsList = topicsRepo.findAllTopic();
            logger.info("TopicController(getAllSoftTopic)>> Exit");
            return new ResponseEntity<List<Topic>>(topicsList, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in topicsController(getAllSoftTopic)>> Exit");
        }
        return null;
    }
    /*get all the topics details*/
    @GetMapping("/get-all-topics")
    public ResponseEntity<List<Topic>> getAllTopic() {
        logger.info("TopicController(getAllTopic) >> Entry");
        try {
            List<Topic> topicsList = topicsRepo.findAll();
            logger.info("TopicController(getAllTopic)>> Exit");
            return new ResponseEntity<List<Topic>>(topicsList, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in topicsController(getAllTopic)>> Exit");
        }
        return  null;
    }
    /*get specific topics id details*/
    @GetMapping("/get-by-topics/{id}")
    public ResponseEntity<Optional<Topic>> getByIdTopic(@PathVariable Long id) {
        logger.info("TopicController(getByIdTopic) >> Entry");
        try {
            Optional<Topic> getTopicDetails = topicsRepo.findById(id);
            logger.info("TopicController(getByIdTopic)>> Exit");
            return new ResponseEntity<Optional<Topic>>(getTopicDetails, HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in topicsController(getByIdTopic)>>Exit");
        }
        return null;
    }
    /*  update the topics details by given id */
    @PutMapping("/update-topics/{id}")
    public ResponseEntity<Topic> updateTopic(@PathVariable Long id, @RequestBody Topic topicsDetails) {
        logger.info("TopicController(updateTopic) >> Entry");
        try {
            Optional<Topic> topics = topicsRepo.findById(id);
            if (topics.isPresent()) {
                topics.get().setTopic_name(topicsDetails.getTopic_name());
                topics.get().setCategory(topicsDetails.getCategory());
                topics.get().setCourse_id(topicsDetails.getCourse_id());
                topics.get().setModified_time(new Date());
                Topic updateTopic = topicsRepo.save(topics.get());
                logger.info("TopicController(updateTopic)>> Exit");
                return new ResponseEntity<Topic>(updateTopic, HttpStatus.OK);
            } else {
                Topic updateTopic = null;
                logger.info("TopicController(updateTopic)>> Exit");
                return new ResponseEntity<Topic>(updateTopic, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in topicsController(updateTopic)>> Exit");
        }
        return null;
    }

    /*Topic soft delete details  */
    @PutMapping("/active-delete-topics/{id}")
    public ResponseEntity<Topic> DeleteByTopic(@PathVariable Long id) {
        logger.info("TopicController(softDeleteByTopic) >> Entry");
        try {
            Optional<Topic> topicsDetails = topicsRepo.findById(id);
            Topic updateTopic = new Topic();
            if (topicsDetails.isPresent()) {
                topicsDetails.get().set_deleted(true);
                updateTopic = topicsRepo.save(topicsDetails.get());
                logger.info("TopicController(softDeleteByTopic)>> Exit");
                return new ResponseEntity<Topic>(updateTopic, HttpStatus.OK);
            } else {
                updateTopic = null;
                logger.info("TopicController(softDeleteByTopic)>> Exit");
                return new ResponseEntity<Topic>(updateTopic, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in topicsController(softDeleteByTopic)>> Exit");
        }
        return null;
    }

    /* premanent delete topics details */
    @DeleteMapping("/delete-topics/{id}")
    public ResponseEntity<String> deleteTopicById(@PathVariable Long id){
        logger.info("TopicController(deleteTopicById) >> Entry");
        try {
            Optional<Topic> topicsDetails = topicsRepo.findById(id);
            if (topicsDetails.isPresent()) {
                topicsRepo.deleteById(id);
                logger.info("TopicController(deleteTopicById) >> Exit");
                return new ResponseEntity<String>("delete successfully", HttpStatus.OK);
            } else {
                logger.info("TopicController(deleteTopicById) >> Exit");
                return new ResponseEntity<String>("id not found", HttpStatus.BAD_REQUEST);
            }
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in topicsController(deleteTopicById)");
        }
        return null;
    }



}







