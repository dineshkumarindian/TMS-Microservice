package com.servxglobal.tms.userservice.dto;

class TrainerDetails {
	private String username;
	private String email;
	private Long id;
	public TrainerDetails(String username, String email, Long id) {
		this.username = username;
		this.email = email;
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	
}

public class TrainerLoginResponseDto {
	private boolean success;
	private String message;
	private String token;
	private TrainerDetails trainerDetails;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public TrainerDetails getTrainer() {
		return trainerDetails;
	}
	public void setTrainer(String username, String email, Long id) {
		this.trainerDetails = new TrainerDetails(username, email, id);
	}
	
	
}
