package com.servxglobal.tms.batchservice.controller;

import com.servxglobal.tms.batchservice.dto.ResponseDto;
import com.servxglobal.tms.batchservice.model.Schedule;
import com.servxglobal.tms.batchservice.repository.ScheduleRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    @Autowired
    private ScheduleRepo scheduleRepo;
    @GetMapping("/get-all-schedule")
    public ResponseEntity<List<Schedule>> getAllSchedule() {
        ResponseDto responseDto = new ResponseDto();
        logger.info("ScheduleController(getAllSchedule) >> Entry");
        try{
            List<Schedule> scheduleList = scheduleRepo.findAll();
            logger.info("ScheduleController(getAllSchedule) >> exit");
            return new ResponseEntity<List<Schedule>>(scheduleList, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace(System.out);
            logger.info("Exception in ScheduleController(getAllSchedule) >> Exit");
        }
        return null;
    }


}
