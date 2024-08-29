package com.servxglobal.tms.courseservice.controller;

import com.servxglobal.tms.courseservice.model.Level;
import com.servxglobal.tms.courseservice.repository.LevelRepo;
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
@RequestMapping("/api/course/levels")
public class LevelController {

    @Autowired
    private LevelRepo levelRepo;
    private static final Logger logger = LoggerFactory.getLogger(LevelController.class);

    /* add the levels post API */
    @PostMapping("/add-level")
    public ResponseEntity<Level> addLevel(@RequestBody Level levelDetails) {
            logger.info("LevelController(addLevel) >> Entry");
            try{
                levelDetails.setCreated_time(new Date());
                levelDetails.setModified_time(new Date());
                List<Level> allLevels = levelRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
                if(allLevels.isEmpty()) {
                    levelDetails.setId(1L);
                } else {
                    levelDetails.setId(allLevels.get(0).getId()+1);
                }
//                levelData.setId(levelRepo.count()+1);
                Level save_data = levelRepo.save(levelDetails);
            logger.info("LevelController(addLevel)>> Exit");
            return new ResponseEntity<Level>(save_data, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in LevelController(addLevel) >> Entry");
        }
        return null;
    }

    /*get the levels active details */
    @GetMapping("/active-get-all-levels")
    public ResponseEntity<List<Level>> softGetAllLevels() {
        logger.info("LevelController(getAllSoftLevels) >> Entry");
        try {
            List<Level> levelsList = levelRepo.findAllLevel();
            logger.info("LevelController(getAllSoftLevels)>> Exit");
            return new ResponseEntity<List<Level>>(levelsList, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in LevelController(getAllSoftLevels)>> Exit");
        }
        return null;
    }
    /*get all the levels details*/
    @GetMapping("/get-all-levels")
    public ResponseEntity<List<Level>> getAllLevels() {
        logger.info("LevelController(getAllLevels) >> Entry");
        try {
            List<Level> levelsList = levelRepo.findAll();
            logger.info("LevelController(getAllLevels)>> Exit");
            return new ResponseEntity<List<Level>>(levelsList, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in LevelController(getAllLevels)>> Exit");
        }
        return  null;
    }
    /*get specific levels id details*/
    @GetMapping("/get-by-level/{id}")
    public ResponseEntity<Optional<Level>> getByIdLevels(@PathVariable Long id) {
        logger.info("LevelController(getByIdLevel) >> Entry");
        try {
            Optional<Level> getLevelsDetails = levelRepo.findById(id);
            logger.info("LevelController(getByIdLevel)>> Exit");
            return new ResponseEntity<Optional<Level>>(getLevelsDetails, HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in LevelController(getByIdLevel)>>Exit");
        }
        return null;
    }
  /*  update the levels details by given id */
    @PutMapping("/update-levels/{id}")
    public ResponseEntity<Level> updateLevels(@PathVariable Long id, @RequestBody Level levelsDetails) {
        logger.info("LevelController(updateLevels) >> Entry");
        try {
            Optional<Level> levels = levelRepo.findById(id);
            if (levels.isPresent()) {
                levels.get().setLevel(levelsDetails.getLevel());
                levels.get().setCourse_id(levelsDetails.getCourse_id());
                levels.get().setModified_time(new Date());
                Level updateLevels = levelRepo.save(levels.get());
                logger.info("LevelController(updateLevels)>> Exit");
                return new ResponseEntity<Level>(updateLevels, HttpStatus.OK);
            } else {
                Level updateLevels = null;
                logger.info("LevelController(updateLevels)>> Exit");
                return new ResponseEntity<Level>(updateLevels, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in LevelController(updateLevels)>> Exit");
        }
    return null;
    }

    /*Levels soft delete details  */
    @PutMapping("/active-delete-levels/{id}")
    public ResponseEntity<Level> DeleteByLevels(@PathVariable Long id) {
        logger.info("LevelController(softDeleteByLevels) >> Entry");
        try {
            Optional<Level> levelsDetails = levelRepo.findById(id);
            Level updateLevels = new Level();
            if (levelsDetails.isPresent()) {
                levelsDetails.get().set_deleted(true);
                updateLevels = levelRepo.save(levelsDetails.get());
                logger.info("LevelController(softDeleteByLevels)>> Exit");
                return new ResponseEntity<Level>(updateLevels, HttpStatus.OK);
            } else {
                updateLevels = null;
                logger.info("LevelController(softDeleteByLevels)>> Exit");
                return new ResponseEntity<Level>(updateLevels, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in LevelController(softDeleteByLevels)>> Exit");
        }
    return null;
    }

    /* permanent delete levels details */
    @DeleteMapping("/delete-levels/{id}")
    public ResponseEntity<String> deleteLevelsById(@PathVariable Long id){
        logger.info("LevelController(deleteLevelsById) >> Entry");
        try {
            Optional<Level> levelsDetails = levelRepo.findById(id);
            if (levelsDetails.isPresent()) {
                levelRepo.deleteById(id);
                logger.info("LevelController(deleteLevelsById) >> Exit");
                return new ResponseEntity<String>("delete successfully", HttpStatus.OK);
            } else {
                logger.info("LevelController(deleteLevelsById) >> Exit");
                return new ResponseEntity<String>("id not found", HttpStatus.BAD_REQUEST);
            }
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in LevelController(deleteLevelsById)");
        }
        return null;
    }

    /**
     * Retrieves the level details for a given course ID.
     *
     * @param  id  the ID of the course
     * @return     a ResponseEntity containing a list of Level objects
     */
    @GetMapping("/getLevelDetailsByCourseId/{id}")
    public ResponseEntity<List<Level>> getLevelDetailsByCourseId(@PathVariable Long id) {
        logger.info("LevelController(getLevelDetailsByCourseId) >> Entry");
        try {
            List<Level> levelsList = levelRepo.getLevelDetailsByCourseId(id);
            logger.info("LevelController(getLevelDetailsByCourseId)>> Exit");
            return new ResponseEntity<List<Level>>(levelsList, HttpStatus.OK);
        } catch(Exception e){
            e.printStackTrace(System.out);
            logger.info("Exception in LevelController(getLevelDetailsByCourseId)>> Exit");
        }
        return null;
    }


}







