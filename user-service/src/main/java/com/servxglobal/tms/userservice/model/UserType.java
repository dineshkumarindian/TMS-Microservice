package com.servxglobal.tms.userservice.model;


public enum UserType {
	ADMIN("ADMIN"), TRAINEE("TRAINEE"), TRAINER("TRAINER");
	
	private final String type;
	
	UserType(String string) {
		type = string;
	}
	
	@Override
	public String toString() {
		return type;
	}
}
