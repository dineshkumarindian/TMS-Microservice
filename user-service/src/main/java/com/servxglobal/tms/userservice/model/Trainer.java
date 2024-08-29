package com.servxglobal.tms.userservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import java.util.Date;
import java.util.List;

@Document(collection="trainer")
@Getter
@Setter
@ToString
public class Trainer {

	@Id
	private Long id;

	private String firstname;

	private String lastname;

	@Email
	private String userEmail;

	private String password;

	private boolean isActive = true;

	private String address;

	private String contact_number;

	private String alternate_number;

	private Long branch_id;

	private String branch;

	//    private Object batches;
	private List<BatchDetails> batches;

	private List<Long> skill_id;

	private List<String> skills;

	private boolean is_deleted = false;

	private Date created_time;

	private Date modified_time;

	private String logo_Img_name;
//    private Admin createdBy;

	private Binary logo_Img;
}
