package com.servxglobal.tms.trainerservice.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TrainerBatch {
    private String batch_name;
    private Long batch_id;
    private String batch_status;
}
