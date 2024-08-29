package com.servxglobal.tms.batchservice.utils;

import com.servxglobal.tms.batchservice.dto.BatchTraineeDto;
import com.servxglobal.tms.batchservice.otherService.TraineeServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BatchTraineeService {

    @Autowired
    private TraineeServiceClient traineeServiceClient;

    public void updateBatchInTrainee(BatchTraineeDto batchDetails, Long id) {
        traineeServiceClient.updateBatchForTrainee(batchDetails, id);
    }
}
