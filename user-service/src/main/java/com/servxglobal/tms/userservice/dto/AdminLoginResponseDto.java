package com.servxglobal.tms.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class AdminDetails {
	String username;
	String mail;
	Long id;
}
@Getter
@Setter
public class AdminLoginResponseDto {
	private boolean success;
	private String message;
	private String token;
	private AdminDetails admin;
	public void setAdmin(String username, String email, Long id) {
		this.admin = new AdminDetails(username, email, id);
		this.admin.setId(id);
	}

}
