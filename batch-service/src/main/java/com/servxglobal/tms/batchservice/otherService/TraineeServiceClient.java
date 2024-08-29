package com.servxglobal.tms.batchservice.otherService;

import com.servxglobal.tms.batchservice.dto.BatchTraineeDto;
import com.servxglobal.tms.batchservice.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="trainee-service")
public interface TraineeServiceClient {
    @PutMapping("api/trainee/update-trainee-batch")
        // calling the trainee update service here as trainee is separate service
    ResponseDto updateBatchForTrainee(@RequestBody BatchTraineeDto batchDetails, @RequestParam("id") Long id);

    @GetMapping("api/trainee/update-batch-status/{status}/{id}")
    ResponseDto updateBatchStatus(@PathVariable("id") Long id, @PathVariable String status);

    @PutMapping("api/trainee/trainees-batch-update")
    ResponseDto TraineesBatchUpdate(@RequestBody BatchTraineeDto batchDetails, @RequestParam("ids") List<Long> ids, @RequestParam("RemovedIds") List<Long> RemovedIds);

    @GetMapping("api/trainee/update-batch-delete/{id}")
    ResponseDto updateBatchDelete(@PathVariable("id") Long id);

}

