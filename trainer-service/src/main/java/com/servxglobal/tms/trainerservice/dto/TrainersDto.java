package com.servxglobal.tms.trainerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainersDto {

    private Long id;

    private String firstname;

    private String lastname;

    private String userEmail;

    private String contact_number;

    private String branch;

    private Object batches;

    private List<String> skills;

}
