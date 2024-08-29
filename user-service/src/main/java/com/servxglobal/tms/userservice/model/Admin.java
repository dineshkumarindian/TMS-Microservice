package com.servxglobal.tms.userservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;

@Document(collection="admin")
@Getter
@Setter
@ToString
public class Admin {
	@Id
	private long id;
	
	private String username;

	@Email
	private String email;
	private String password;
	
}
