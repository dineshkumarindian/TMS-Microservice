package com.servxglobal.tms.trainerservice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchTrainerDto {

    private Long batch_id;

    private String batch_name;
}