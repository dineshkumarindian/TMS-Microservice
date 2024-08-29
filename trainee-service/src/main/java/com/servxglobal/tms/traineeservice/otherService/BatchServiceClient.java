package com.servxglobal.tms.traineeservice.otherService;
import com.servxglobal.tms.traineeservice.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "batch-service")
public interface BatchServiceClient {

    @GetMapping("api/batch/update-batch-in-trainee/{batchId}/{traineeId}/{removeId}")
    ResponseDto updateBatchInTraineeDetails(@PathVariable("batchId") Long batchId,@PathVariable("traineeId") Long traineeId,@PathVariable("removeId")Long removeId);


    /**
     * Retrieves a ResponseDto for deleting or updating a trainee batch based on the provided trainee ID.
     *
     * @param  traineeId  the ID of the trainee
     * @return            a ResponseDto indicating the success or failure of the operation
     */
    @GetMapping("api/batch/trainee-delete-update-batch/{traineeId}")
    ResponseDto traineeDeleteUpdateBatch(@PathVariable("traineeId") Long traineeId);

}
