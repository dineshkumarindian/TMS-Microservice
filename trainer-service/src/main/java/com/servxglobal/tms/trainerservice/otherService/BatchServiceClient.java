package com.servxglobal.tms.trainerservice.otherService;

import com.servxglobal.tms.trainerservice.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "batch-service")
public interface BatchServiceClient {

    @GetMapping("api/batch/update-batch-in-trainer/{trainerId}")
    ResponseDto TrainerBatchUpdate(@PathVariable("trainerId") Long trainerId, @RequestParam("ids") List<Long> ids,@RequestParam("remove_ids") List<Long> remove_ids);


    /**
     * Retrieves a ResponseDto object by deleting or updating a batch of trainees associated with a specified trainer.
     *
     * @param  trainerId   the ID of the trainer
     * @return             a ResponseDto object containing the result of the operation
     */
    @GetMapping("api/batch/trainer-delete-update-batch/{trainerId}")
    ResponseDto trainerDeleteUpdateBatch(@PathVariable("trainerId") Long trainerId);
}
