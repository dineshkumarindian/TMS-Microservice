package com.servxglobal.tms.traineeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.Email;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TraineeDto {

    @Id
    private Long id;

    //	private String username;
    private String firstname;

    private String lastname;

    private Long enrollment_id;

    @Email
    private String email;

    private  String contact_number;

    private boolean payment_status = false;

    private String branch;

    private Long batch_id;

    private String batch_name;

    private String batch_status;
}
