package com.servxglobal.tms.courseservice.otherService;


import com.servxglobal.tms.courseservice.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "batch-service")
public interface BatchServiceClient {

    /**
     * Retrieves a list of course IDs associated with a given trainer ID.
     *
     * @param  trainerId  the ID of the trainer
     * @return            a ResponseDto object containing the list of course IDs
     */
    @GetMapping("api/batch/getCourseIdsByTrainer/{trainerId}")
    ResponseDto getCourseIdsByTrainer(@PathVariable("trainerId") Long trainerId);

    /**
     * Retrieves a list of course IDs associated with a trainee.
     *
     * @param  traineeId  the ID of the trainee
     * @return            a response containing the list of course IDs
     */
    @GetMapping("api/batch/getCourseIdsByTrainee/{traineeId}")
    ResponseDto getCourseIdsByTrainee(@PathVariable("traineeId") Long traineeId);
}
