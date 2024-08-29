package com.servxglobal.tms.adminservice.controller;
import com.servxglobal.tms.adminservice.dto.SuccessandMessageDto;
import com.servxglobal.tms.adminservice.model.Branch;
import com.servxglobal.tms.adminservice.repository.AdminRepo;
import com.servxglobal.tms.adminservice.repository.BranchRepo;
import com.servxglobal.tms.adminservice.security.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/branch")
public class BranchController {
    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private JwtGenerator jwtGenerator;
    private static final Logger logger = LoggerFactory.getLogger(Branch.class);

    /**
     * This function handles the "create branch" API.
     * It reads the data from request body
     * The function returns the newly created branch.
     */
    @PostMapping("/create")
    public ResponseEntity<SuccessandMessageDto> createbranch(@RequestBody Branch branchDetails, @RequestHeader(name = "Authorization") String token) {
        logger.info("BranchController(create) >> Entry");
        SuccessandMessageDto response = new SuccessandMessageDto();
        branchDetails.setId(branchRepo.count()+1);
        branchDetails.setBranchname(branchDetails.getBranchname());
        branchDetails.setBranch_code(branchDetails.getBranch_code());
        branchDetails.setCreated_time(new Date());
        branchDetails.setModified_time(new Date());
        try {
            branchDetails.setCreatedBy(adminRepo.findByEmail(jwtGenerator.getUsernameFromJWT(token.substring(7))).orElseThrow());
        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("Unauthorized request");
            response.setSuccess(false);
            logger.info("BranchController(create) >> Exit");
            return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.UNAUTHORIZED);
        }
        Branch data = branchRepo.save(branchDetails);
        response.setMessage("Branch Created Successfully !!");
        response.setSuccess(true);
        response.setData(String.valueOf(data));
        logger.info("BranchController(create) >> Exit");
        return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.OK);
    }

    /**
     * Retrieves all the branch details.
     *
     * @return ResponseEntity<List<branch>> - The response entity containing the list of branch.
     */
    @GetMapping("/get-all-branches")
    public ResponseEntity<List<Branch>> getAllBranches() {
        logger.info("BranchController(getAllBranches) >> Entry");
        try {
            List<Branch> branchDetails = branchRepo.findAll();
            logger.info("BranchController(getAllBranches) >> Exit");
            return new ResponseEntity<List<Branch>>(branchDetails,HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("BranchController(getAllBranches) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves all active branches from the database.
     *
     * @return A ResponseEntity containing a list of branches.
     */
    @GetMapping("/get-active-branches")
    public ResponseEntity<List<Branch>> getAllActiveBranches() {
        logger.info("BranchController(getAllActiveBranches) >> Entry");
        try {
            List<Branch> branchDetails = branchRepo.findActiveBranches();
            logger.info("BranchController(getAllActiveBranches) >> Exit");
            return new ResponseEntity<List<Branch>>(branchDetails,HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("BranchController(getAllActiveBranches) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves all inactive branches from the database.
     *
     * @return A ResponseEntity containing a list of branches.
     */
    @GetMapping("/get-inactive-branches")
    public ResponseEntity<List<Branch>> getInactiveBranches() {
        logger.info("BranchController(getInactiveBranches) >> Entry");
        try {
            List<Branch> inactiveBranches = branchRepo.findInactiveBranches();
            logger.info("BranchController(getInactiveBranches) >> Exit");
            return new ResponseEntity<List<Branch>>(inactiveBranches, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("BranchController(getInactiveBranches) >> Exit");
        }
        return null;
    }

    /**
     * Retrieves the branch details for the given ID.
     *
     * @param  id the ID of the branch
     * @return    the branch details
     */
    @GetMapping("/get-branch/{id}")
    public ResponseEntity<Optional<Branch>> getTrainersById(@PathVariable Long id) {
        logger.info("BranchController(getBranchById) >> Entry");
        try {
            Optional<Branch> branchDetails = branchRepo.findById(id);
            logger.info("BranchController(getBranchById) >> Exit");
            return new ResponseEntity<Optional<Branch>>(branchDetails, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.info("BranchController(getBranchById) >> Exit");
        }
        return null;
    }


    /**
     * Update the branch details by the given ID.
     *
     * @param id     The ID of the branch to update.
     * @requestbody  branch new details to update
     * @return       The updated branch if successful, or null if an exception occurred.
     */
    @PutMapping("/update-branch")
    public ResponseEntity<Branch> updateBranch(@RequestBody Branch branchDetails) {
        logger.info("BranchController(updateBranch) >> Entry");
        try {
            Optional<Branch> branchData = branchRepo.findById(branchDetails.getId());
            Branch Branchinfo = new Branch();
            if(branchData.isPresent()) {
                branchData.get().setBranchname(branchDetails.getBranchname());
                branchData.get().setBranch_code(branchDetails.getBranch_code());
                branchData.get().setModified_time(new Date());
                Branchinfo = branchRepo.save(branchData.get());
                logger.info("BranchController(updateBranch) >> Exit");
                return new ResponseEntity<Branch>(Branchinfo, HttpStatus.OK);
            } else {
                Branchinfo = null;
                logger.info("BranchController(updateBranch) >> Exit");
                return new ResponseEntity<Branch>(Branchinfo, HttpStatus.BAD_REQUEST);
            }
        } catch(Exception e) {
            e.printStackTrace();
            logger.info("BranchController(updateBranch) >> Exit");
        }
        return  null;
    }

    /**
     * Soft deletes a branch by setting its "_deleted" flag to true.
     *
     * @param id The ID of the branch to delete.
     * @return The updated branch details if found, or a bad request response if not.
     */
    @PutMapping("/delete-branch/{id}")
    public ResponseEntity<Branch> deleteBranch(@PathVariable Long id) {
        logger.info("BranchController(deleteBranch) >> Entry");
        try {
            Optional<Branch> branchDetails = branchRepo.findById(id);
            Branch branchInfo = new Branch();
            if(branchDetails.isPresent()) {
                branchDetails.get().set_deleted(true);
                logger.info("BranchController(deleteBranch) >> Exit");
                branchInfo = branchRepo.save(branchDetails.get());
                return new ResponseEntity<Branch>(branchInfo, HttpStatus.OK);
            }
            else {
                branchInfo = null;
                logger.info("BranchController(deleteBranch) >> Exit");
                return new ResponseEntity<Branch>(branchInfo, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("BranchController(deleteBranch) >> Exit");
        }
        return null;
    }

    /**
     * Deletes a branch permanently by its ID.
     *
     * @param id The ID of the branch to be deleted.
     * @return A ResponseEntity with a message indicating the result of the deletion.
     */
    @DeleteMapping("/branch-hardDelete/{id}")
    public ResponseEntity<String> branchHardDelete(@PathVariable Long id) {
        logger.info("BranchController(branchHardDelete) >> Entry");
        try {
            Optional<Branch> branchInfo = branchRepo.findById(id);
            if(branchInfo.isPresent()) {
                branchRepo.deleteById(id);
                logger.info("BranchController(branchHardDelete) >> Exit");
                return new ResponseEntity<String>("Deleted Successfully", HttpStatus.OK);
            } else {
                logger.info("BranchController(branchHardDelete) >> Exit");
                return new ResponseEntity<String>("Failed to delete trainer", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("BranchController(branchHardDelete) >> Exit");
        }
        return null;
    }
}

