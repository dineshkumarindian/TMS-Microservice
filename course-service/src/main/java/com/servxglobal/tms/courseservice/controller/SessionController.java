package com.servxglobal.tms.courseservice.controller;


import com.servxglobal.tms.courseservice.model.Session;
import com.servxglobal.tms.courseservice.repository.SessionRepo;
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
@RequestMapping("/api/course/session")
public class SessionController {

    @Autowired
    SessionRepo sessionRepo;
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    /* add the session post API */
    @PostMapping("/add-session")
    public ResponseEntity<Session> addSession(@RequestBody Session sessionDetails) {
        logger.info("SessionController(addSession) >> Entry");
        try {
            Session session_data = new Session();
            session_data.setSession_no(sessionDetails.getSession_no());
            session_data.setCreated_time(new Date());
            session_data.setModified_time(new Date());
            session_data.setCourse_id(sessionDetails.getCourse_id());
            session_data.setMax_level(sessionDetails.getMax_level());
            List<Session> allSession = sessionRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
            if(allSession.isEmpty()){
                session_data.setId(1L);
            } else {
                session_data.setId(allSession.get(0).getId()+1);
            }
            Session save_data= sessionRepo.save(session_data);
            logger.info("SessionController(addSession) >> Exit");
            return new ResponseEntity<Session>(save_data, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("exception in SessionController(addSession) >> Exit");
        }
    return null;
    }

    /* get the active get all session */
    @GetMapping("/active-get-all-session")
    public ResponseEntity<List<Session>> activeGetAllTopic(){
        logger.info("SessionController(activeGetAllTopic) >> Entry");
        try{
            List<Session> sessionList = sessionRepo.findAllSession();
            logger.info("TopicController(activeGetAllTopic)>> Exit");
            return new ResponseEntity<List<Session>>(sessionList, HttpStatus.OK);

        } catch(Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in SessionController(activeGetAllTopic) >> Exit");
        }
        return null;
    }

    /* get all session details */
    @GetMapping("/get-all-session")
    public ResponseEntity<List<Session>> getAllTopic(){
        logger.info("SessionController(getAllTopic) >> Entry");
        try{
            List<Session> sessionList = sessionRepo.findAll();
            logger.info("TopicController(getAllTopic)>> Exit");
            return new ResponseEntity<List<Session>>(sessionList, HttpStatus.OK);

        } catch(Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in SessionController(getAllTopic) >> Exit");
        }
        return null;
    }

   /* get the given id session details */
    @GetMapping("/get-by-session/{id}")
    public ResponseEntity<Optional<Session>> getByIdSession(@PathVariable Long id) {
        logger.info("SessionController(getByIdSession) >> Entry");
        try {
            Optional<Session> SessionDetails = sessionRepo.findById(id);
            logger.info("SessionController(getByIdSession)>> Exit");
            return new ResponseEntity<Optional<Session>>(SessionDetails, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in SessionController(getByIdSession)>>Exit");
        }
        return null;
    }

    /* update the given id session details */
    @PutMapping("/update-session/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id, @RequestBody Session sessionDetails) {
        logger.info("SessionController(updateSession) >> Entry");
        try{
            Optional<Session> session_data = sessionRepo.findById(id);
            if(session_data.isPresent()) {
                session_data.get().setSession_no(sessionDetails.getSession_no());
                session_data.get().setCourse_id(sessionDetails.getCourse_id());
                session_data.get().setMax_level(sessionDetails.getMax_level());
                session_data.get().setModified_time(new Date());
                Session saveUpdateDetails = sessionRepo.save(session_data.get());
                logger.info("SessionController(updateTopic)>> Exit");
                return new ResponseEntity<Session>(saveUpdateDetails, HttpStatus.OK);
            }
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in SessionController(updateSession)>>Exit");
        }
        return null;
    }

    /* session id's soft delete */
    @PutMapping("/active-delete-session/{id}")
    public ResponseEntity<Session>  activeDeleteBySession(@PathVariable Long id){
        logger.info("SessionController(activeDeleteBySession) >> Entry");
        try {
            Optional<Session> sessionDetails = sessionRepo.findById(id);
            Session updateSession = new Session();
            if (sessionDetails.isPresent()) {
                sessionDetails.get().set_deleted(true);
                logger.info("SessionController()>> Exit");
                updateSession = sessionRepo.save(sessionDetails.get());
                return new ResponseEntity<Session>(updateSession, HttpStatus.OK);
            } else {
                updateSession = null;
                logger.info("SessionController(activeDeleteBySession)>> Exit");
                return new ResponseEntity<Session>(updateSession, HttpStatus.BAD_REQUEST);
            }
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in SessionController(activeDeleteBySession)>>Exit");
        }
        return null;
        }

  /*  session id's permanent delete  */
    @DeleteMapping("/delete-session/{id}")
    public ResponseEntity<String>  deleteSessionById(@PathVariable Long id){
        logger.info("SessionController(deleteSessionById) >> Entry");
        try{
            Optional<Session> sessionDetails = sessionRepo.findById(id);
            if(sessionDetails.isPresent()){
                sessionRepo.deleteById(id);
                logger.info("SessionController(deleteSessionById) >> Exit");
                return new ResponseEntity<String>("delete successfully", HttpStatus.OK);
            } else {
                logger.info("SessionController(deleteSessionById) >> Exit");
                return new ResponseEntity<String>("id not found", HttpStatus.BAD_REQUEST);
            }

        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in SessionController(deleteSessionById)");
        }
        logger.info("SessionController(deleteSessionById) >> Exit");
    return null;
    }

// get session by level ids
@GetMapping("/get-all-sessions-by-level-id/{id}")
    public ResponseEntity<List<Session>> getAllSessionsByLevelId(@PathVariable Long id) {
        logger.info("SessionController(getAllSessionsByLevelId) >> Entry");
        try {
            List<Session> sessionList = sessionRepo.findByIDInArray(id);
            logger.info("SessionController(getAllSessionsByLevelId) >> Exit");
            return new ResponseEntity<List<Session>>(sessionList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("SessionController(getAllSessionsByLevelId) >> Exit");
        }
        return null;
    }
    //get session by course id
    @GetMapping("/get-all-session-by-course-id/{id}")
    public ResponseEntity<List<Session>> getAllSessionByCourseId(@PathVariable Long id) {
        logger.info("SessionController(getAllSessionByCourseId) >> Entry");
        Integer sessionSize = 0;
        try {
            List<Session> sessionList = sessionRepo.getSessionByCourseId(id);
//            sessionSize = sessionList.size();
            logger.info("SessionController(getAllSessionByCourseId) >> Exit");
            return new ResponseEntity<List<Session>>(sessionList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("SessionController(getAllSessionByCourseId) >> Exit");
        }
        return null;
    }
}
