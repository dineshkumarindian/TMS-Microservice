package com.servxglobal.tms.userservice.model;

import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import java.util.Date;
import javax.validation.constraints.Email;
@Document(collection="trainee")
@Getter
@Setter
@ToString

public class Trainee {

	@Id
	private Long id;

	//	private String username;
	private String firstname;

	private String lastname;

	private Long enrollment_id;

	@Email
	private String email;

	private String password;

	private String address;

	private  String contact_number;

	private String alternate_number;

	private boolean payment_status = false;

	private boolean is_deleted = false;

	private boolean is_activated = true;

	private Date create_time;

	private Date modified_time;

	private Date date_of_birth;

	private String gender;

	private Long branch_id;

	private String branch;

	private BatchDetails traineeBatch;

	private String branchCode;

	private Long stream_id;

	private String logo_Img_name;

	private Binary logo_Img;

	private boolean batch_complete_status = false;
}
