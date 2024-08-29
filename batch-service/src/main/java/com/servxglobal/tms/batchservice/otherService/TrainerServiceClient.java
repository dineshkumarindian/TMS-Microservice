package com.servxglobal.tms.batchservice.otherService;

import com.servxglobal.tms.batchservice.dto.BatchTraineeDto;
import com.servxglobal.tms.batchservice.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="trainer-service")
public interface TrainerServiceClient {
    @PutMapping("api/trainer/trainers-batch-update")
    ResponseDto TrainerBatchUpdate(@RequestBody BatchTraineeDto batchDetails, @RequestParam("ids") List<Long> ids, @RequestParam("removedIds") List<Long> RemovedIds);

    @GetMapping("api/trainer/update-batch-status/{status}/{id}")
    ResponseDto updateBatchStatus(@PathVariable("id") Long id, @PathVariable String status);

    @GetMapping("api/trainer/update-batch-delete/{id}")
    ResponseDto updateBatchDelete(@PathVariable("id") Long id);
}
