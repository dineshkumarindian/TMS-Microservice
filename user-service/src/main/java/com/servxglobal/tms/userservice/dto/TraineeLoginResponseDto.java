package com.servxglobal.tms.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class TraineeDetails {
	private String username;
	private String email;
	private Long id;
}

public class TraineeLoginResponseDto {
	private boolean success;
	private String message;
	private String token;
	private TraineeDetails trainee;
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
	public TraineeDetails getTrainee() {
		return trainee;
	}
	public void setTrainee(String username, String email, Long id) {
		this.trainee = new TraineeDetails(username, email, id);
	}
	
	
}
