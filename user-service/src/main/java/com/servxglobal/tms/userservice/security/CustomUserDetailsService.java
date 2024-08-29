package com.servxglobal.tms.userservice.security;


import com.servxglobal.tms.userservice.model.Admin;
import com.servxglobal.tms.userservice.model.Trainee;
import com.servxglobal.tms.userservice.model.Trainer;
import com.servxglobal.tms.userservice.model.UserType;
import com.servxglobal.tms.userservice.repository.AdminRepo;
import com.servxglobal.tms.userservice.repository.TraineeRepo;
import com.servxglobal.tms.userservice.repository.TrainerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private AdminRepo adminRepo;
	@Autowired
	private TrainerRepo trainerRepo;
	@Autowired
	private TraineeRepo traineeRepo;

	private UserType userType;

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	@Override
	public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
		if(userType==UserType.ADMIN) {

			Admin adminEntity = adminRepo.findByEmail(mail).orElseThrow(()-> new UsernameNotFoundException("Admin Email "+ mail+ "not found"));

			SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority(UserType.ADMIN.toString());
			Collection<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(adminAuthority);
			return new User(adminEntity.getEmail(), adminEntity.getPassword(), authorities);
		} else if(userType == UserType.TRAINER) {
			Trainer trainerEntity = trainerRepo.findByUserEmail(mail).orElseThrow(()-> new UsernameNotFoundException("Trainer Email "+ mail+ "not found"));

			SimpleGrantedAuthority trainerAuthority = new SimpleGrantedAuthority(UserType.TRAINER.toString());
			Collection<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(trainerAuthority);
			return new User(trainerEntity.getUserEmail(), trainerEntity.getPassword(), authorities);
		} else if(userType == UserType.TRAINEE) {
			Trainee studentEntity = traineeRepo.findByEmail(mail).orElseThrow(()-> new UsernameNotFoundException("Trainee Email "+ mail+ "not found"));

			SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority(UserType.TRAINEE.toString());
			Collection<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(adminAuthority);
			return new User(studentEntity.getEmail(), studentEntity.getPassword(), authorities);
		}
		return null;
	}

}
