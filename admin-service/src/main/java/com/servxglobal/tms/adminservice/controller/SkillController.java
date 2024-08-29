package com.servxglobal.tms.adminservice.controller;

import com.google.gson.Gson;
import com.servxglobal.tms.adminservice.dto.ResponseDto;
import com.servxglobal.tms.adminservice.dto.SuccessandMessageDto;
import com.servxglobal.tms.adminservice.model.Branch;
import com.servxglobal.tms.adminservice.model.Skill;
import com.servxglobal.tms.adminservice.repository.AdminRepo;
import com.servxglobal.tms.adminservice.repository.SkillRepo;
import com.servxglobal.tms.adminservice.security.JwtGenerator;
import jakarta.ws.rs.Path;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import javax.swing.text.html.Option;
import javax.xml.crypto.Data;
import java.util.Date;

@RestController
@RequestMapping("/api/skill")
public class SkillController {
    @Autowired
    private SkillRepo skillRepo;

    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private JwtGenerator jwtGenerator;
    private static final Logger logger = LoggerFactory.getLogger(Skill.class);

    /**
     * This function handles the "create skill" API.
     * It reads the data from request body
     * The function returns the newly created skill.
     */
    @PostMapping("/create")
    public ResponseEntity<SuccessandMessageDto> createSkill(@RequestParam("skill") List<String> skill, @RequestHeader(name = "Authorization") String token) {
        logger.info("SkillController(create) >> Entry");
        SuccessandMessageDto response = new SuccessandMessageDto();
        Skill skillData = new Skill();
        Skill skills = new Skill();
        for(String i : skill) {
        skillData.setId(skillRepo.count() + 1);
            skillData.setSkill(i);
            skillData.setCreated_time(new Date());
            skillData.setModified_time(new Date());
            try {
                skillData.setCreatedBy(adminRepo.findByEmail(jwtGenerator.getUsernameFromJWT(token.substring(7))).orElseThrow());
            } catch (Exception e) {
                e.printStackTrace();
                response.setMessage("Unauthorized request");
                response.setSuccess(false);
                logger.info("SkillController(create) >> Exit");
                return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.UNAUTHORIZED);
            }
             skills = skillRepo.save(skillData);
        }
            response.setMessage("Skill Created Successfully !!");
            response.setSuccess(true);
            response.setData(String.valueOf(skills));
            logger.info("SkillController(create) >> Exit");
            return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all the skill details.
     *
     * @return ResponseEntity<List<skill>> - The response entity containing the list of skill.
     */
    @GetMapping("/get-all-skills")
    public ResponseEntity<List<Skill>> getAllSkill() {
        logger.info("SkillController(getAllSkill) >> Entry");
        try {
            List<Skill> skillDetails = skillRepo.findAll();
            logger.info("SkillController(getAllSkill) >> Exit");
            return new ResponseEntity<List<Skill>>(skillDetails, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("BranchController(getAllSkill) >> Exit");
        }
        return null;
    }
    /**
     * Retrieves all active skill from the database.
     *
     * @return A ResponseEntity containing a list of skills.
     */
    @GetMapping("/get-active-skills")
    public ResponseEntity<List<Skill>> getActiveSkill() {
        logger.info("SkillController(getActiveSkill) >> Entry");
        try {
            List<Skill> skillData = skillRepo.getActiveSkills();
            logger.info("SkillController(getActiveSkill) >> Exit");
            return new ResponseEntity<List<Skill>>(skillData, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("BranchController(getActiveSkill) >> Exit");
        }
        return null;
    }
    /**
     * Retrieves all inactive skills  from the database.
     *
     * @return A ResponseEntity containing a list of skills.
     */
    @GetMapping("/get-inactive-skills")
    public ResponseEntity<List<Skill>> getInactiveSkill() {
        logger.info("SkillController(getInactiveSkill) >> Entry");
        try {
            List<Skill> skillData = skillRepo.getInactiveSkills();
            logger.info("SkillController(getInactiveSkill) >> Exit");
            return new ResponseEntity<List<Skill>>(skillData, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("SkillController(getInactiveSkill) >> Exit");
        }
        return null;
    }
    /**
     * Retrieves the skill details for the given ID.
     *
     * @param  id the ID of the skill
     * @return the skill details
     */

    @GetMapping("/get-skill/{id}")
    public ResponseEntity<Optional<Skill>> getSkillById(@PathVariable Long id) {
        logger.info("SkillController(getSkillById) >> Entry");
        try {
            Optional<Skill> skillDetails = skillRepo.findById(id);
            logger.info("SkillController(getSkillById) >> Exit");
            return new ResponseEntity<Optional<Skill>>(skillDetails, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.info("SkillController(getSkillById) >> Exit");
        }
        return null;
    }
    /**
     * Update the skill details by the given ID.
     *
     * @param id     The ID of the skill to update.
     * @requestbody  skill new details to update
     * @return       The updated skill if successful, or null if an exception occurred.
     */
    @PostMapping("/update-skill")
    public ResponseEntity<ResponseDto> updateSkill(@RequestParam("id") Long id,@RequestParam("skill") String skilldata) {
        logger.info("SkillController(updateSkill) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Skill> data = skillRepo.findById(id);
            Skill skillDetails = new Skill();
            if(data.isPresent()) {
                data.get().setSkill(skilldata);
                data.get().setModified_time(new Date());
                skillDetails = skillRepo.save(data.get());
                response.setSuccess(true);
                response.setMessage("Skill update successfully");
                response.setData(new Gson().toJson(skillDetails));
                logger.info("SkillController(updateSkill) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                skillDetails = null;
                response.setSuccess(false);
                response.setMessage("Failed to update the skill");
                response.setData(new Gson().toJson(skillDetails));
                logger.info("SkillController(updateSkill) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.BAD_REQUEST);
            }
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("SkillController(updateSkill) >> Exit");
        }
        return  null;
    }
    /**
     * Soft deletes a skill by setting its "_deleted" flag to true.
     *
     * @param id The ID of the skill to delete.
     * @return The updated skill details if found, or a bad request response if not.
     */
    @PutMapping("/delete-skill/{id}")
    public ResponseEntity<ResponseDto> deleteSkill(@PathVariable Long id) {
        logger.info("SkillController(deleteSkill) >> Entry");
        ResponseDto response = new ResponseDto();
        try {
            Optional<Skill> data = skillRepo.findById(id);
            Skill skillDetails = new Skill();
            if(data.isPresent()) {
                data.get().set_deleted(true);
                logger.info("SkillController(deleteSkill) >> Exit");
                skillDetails = skillRepo.save(data.get());
                response.setSuccess(true);
                response.setMessage("Skill deleted successfully");
                response.setData(new Gson().toJson(skillDetails));
                return new ResponseEntity<ResponseDto>(response, HttpStatus.OK);
            } else {
                skillDetails = null;
                response.setSuccess(false);
                response.setMessage("Failed to delete the skill");
                response.setData(null);
                logger.info("SkillController(deleteSkill) >> Exit");
                return new ResponseEntity<ResponseDto>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("SkillController(deleteSkill) >> Exit");
        }
        return null;
    }
    /**
     * Deletes a skill permanently by its ID.
     *
     * @param id The ID of the skill to be deleted.
     * @return A ResponseEntity with a message indicating the result of the deletion.
     */

    @DeleteMapping("/skill-harddelete/{id}")
    public ResponseEntity<String> skillHardDelete(@PathVariable Long id) {
        logger.info("SkillController(skillHardDelete) >> Entry");
        try {
            Optional<Skill> skillData = skillRepo.findById(id);
            if (skillData.isPresent()) {
                skillRepo.deleteById(id);
                logger.info("SkillController(skillHardDelete) >> Exit");
                return new ResponseEntity<String>("Deleted Successfully", HttpStatus.OK);
            } else {
                logger.info("SkillController(skillHardDelete) >> Exit");
                return new ResponseEntity<String>("Failed to delete skill", HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e) {
                e.printStackTrace();
                logger.info("SkillController(deleteSkill) >> Exit");
            }
            return null;
    }

}
