package com.servxglobal.tms.userservice.dto;
import java.util.List;
public class TrainerRegisterDto {
	private String username;
	private String email;
	private String password ;
	private String address;
	private String contact_no;
	private String alternate_no;
	private Long branch_id;
	private String branch;
	private List<Long> batches;
	private List<String> skills;

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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getAddress() {return address;}
	public void setAddress(String address) {this.address = address;}
	public String getContact_no() {return contact_no;}
	public void setContact_no(String contact_no) {this.contact_no = contact_no;}
	public String getAlternate_no() {return alternate_no;}
	public void setAlternate_no(String alternate_no) {this.alternate_no = alternate_no;}
	public Long getBranch_id() {return branch_id;}
	public void setBranch_id(Long branch_id) {this.branch_id = branch_id;}
	public void setBranch(String branch) {this.branch = branch;}
	public String getBranch() {	return branch;}
	public List<Long> getBatches() {return batches;}
	public void setBatches(List<Long> batches) {this.batches = batches;}
	public List<String> getSkills() {return skills;}
	public void setSkills(List<String> skills) {this.skills = skills;}
}