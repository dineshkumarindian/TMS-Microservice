package com.servxglobal.tms.traineeservice.dto;

import org.bson.types.Binary;

import java.util.Date;

public class TraineeRegisterDto {

    private String firstname;

    private String lastname;

    private String email;

    private String password;

    private String address;

    private String contact_number;

    private String alternate_number;

    private String payment_status;

    private Date date_of_birth;

    private String gender;

    private Long branch_id;

    private String branch;

    private Long batch_id;

    private String branchCode;

    private Long stream_id;

    private Binary logo_Img;
}
