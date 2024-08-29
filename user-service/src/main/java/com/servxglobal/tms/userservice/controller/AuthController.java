package com.servxglobal.tms.userservice.controller;

import com.servxglobal.tms.userservice.dto.*;
import com.servxglobal.tms.userservice.model.Admin;
import com.servxglobal.tms.userservice.model.Trainee;
import com.servxglobal.tms.userservice.model.Trainer;
import com.servxglobal.tms.userservice.model.UserType;
import com.servxglobal.tms.userservice.repository.AdminRepo;
import com.servxglobal.tms.userservice.repository.TraineeRepo;
import com.servxglobal.tms.userservice.repository.TrainerRepo;
import com.servxglobal.tms.userservice.security.CustomUserDetailsService;
import com.servxglobal.tms.userservice.security.JwtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/user/login")
public class AuthController {

	@Autowired
	private AdminRepo adminRepo;
	@Autowired
	private TrainerRepo trainerRepo;
	@Autowired
	private TraineeRepo traineeRepo;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtGenerator jwtGenerator;

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	@PostMapping("/admin")
	public ResponseEntity<AdminLoginResponseDto> login(@RequestBody AdminAuthDto adminAuthDto) {
		ResponseEntity<AdminLoginResponseDto> responseEntity;
		AdminLoginResponseDto responseDto = new AdminLoginResponseDto();
		try {
			logger.info("AuthController(admin login) >> Entry");
			customUserDetailsService.setUserType(UserType.ADMIN);
			Optional<Admin> user =  adminRepo.findByEmail(adminAuthDto.getEmail());

//			Email account check
			if(user.isEmpty()){
				responseDto.setMessage("account does not exist !!");
				responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			}
			else{
				Authentication authentication = authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(adminAuthDto.getEmail(), adminAuthDto.getPassword()));
				SecurityContextHolder.getContext().setAuthentication(authentication);
				String token = jwtGenerator.generateToken(authentication, UserType.ADMIN.toString());
				responseDto.setSuccess(true);
				responseDto.setMessage("login successful !!");
				responseDto.setToken(token);
				Admin admin = adminRepo.findByEmail(adminAuthDto.getEmail()).orElseThrow(() -> new RuntimeException("Admin not found"));
				responseDto.setAdmin(admin.getUsername(), admin.getEmail(), admin.getId());

				responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			}
			// Handle authentication-related exceptions
		} catch (Exception e) {
			responseDto.setMessage("invalid credential !!");
			responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
		}
		logger.info("AuthController(admin login) >> Exit");
		return responseEntity;
	}

	@PostMapping("/trainer")
	public ResponseEntity<TrainerLoginResponseDto> trainerLogin(@RequestBody TraineeLoginDto trainerLoginDto) {
		ResponseEntity<TrainerLoginResponseDto> responseEntity;
		TrainerLoginResponseDto responseDto = new TrainerLoginResponseDto();
		try {
			logger.info("AuthController(trainer login) >> Entry");
			Optional<Trainer> user =  trainerRepo.findByUserEmail(trainerLoginDto.getEmail());
			//Email account check
			if(user.isEmpty()){
				responseDto.setMessage("account does not exist !!");
				responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			}
			else{
				customUserDetailsService.setUserType(UserType.TRAINER);
				Authentication authentication = authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(trainerLoginDto.getEmail(), trainerLoginDto.getPassword()));
				SecurityContextHolder.getContext().setAuthentication(authentication);
				String token = jwtGenerator.generateToken(authentication, UserType.TRAINER.toString());

				responseDto.setSuccess(true);
				responseDto.setMessage("login successful !!");
				responseDto.setToken(token);
				Trainer trainerData = trainerRepo.findByUserEmail(trainerLoginDto.getEmail()).orElseThrow();
				String TrainerFullName = trainerData.getFirstname() + " " + trainerData.getLastname();
				responseDto.setTrainer(TrainerFullName, trainerData.getUserEmail(), trainerData.getId());
				responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			}
			// Handle authentication-related exceptions
		} catch (Exception e) {
			responseDto.setMessage("invalid credential !!");
			responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
		}
		logger.info("AuthController(trainer login) >> Exit");
		return responseEntity;
	}

	@PostMapping("/trainee")
	public ResponseEntity<TraineeLoginResponseDto> traineeLogin(@RequestBody TraineeLoginDto traineeLoginDto) {
		ResponseEntity<TraineeLoginResponseDto> responseEntity;
		TraineeLoginResponseDto responseDto = new TraineeLoginResponseDto();
		try {
			logger.info("AuthController(trainee login) >> Entry");
			Optional<Trainee> user =  traineeRepo.findByEmail(traineeLoginDto.getEmail());
			//Email account check
			if(user.isEmpty()){
				responseDto.setMessage("account does not exist !!");
				responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			}
			else{
				customUserDetailsService.setUserType(UserType.TRAINEE);
				Authentication authentication = authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(traineeLoginDto.getEmail(), traineeLoginDto.getPassword()));
				SecurityContextHolder.getContext().setAuthentication(authentication);
				String token = jwtGenerator.generateToken(authentication, UserType.TRAINEE.toString());
				responseDto.setSuccess(true);
				responseDto.setMessage("login successful !!");
				responseDto.setToken(token);
				Trainee traineeData = traineeRepo.findByEmail(traineeLoginDto.getEmail()).orElseThrow();
				String TraineeFullName = traineeData.getFirstname() + " " + traineeData.getLastname();
				responseDto.setTrainee(TraineeFullName, traineeData.getEmail(), traineeData.getId());
				responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			}

			// Handle authentication-related exceptions
		} catch (Exception e) {
			responseDto.setMessage("invalid credential !!");
			responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
		}
		logger.info("AuthController(trainee login) >> Exit");
		return responseEntity;
	}
	@PutMapping("/admin-change-password")
	public ResponseEntity<SuccessandMessageDto> getAdminPassword(@RequestParam("oldpassword") String oldPassword, @RequestParam("confirmpassword") String confirmpassword,@RequestHeader(name = "Authorization") String token) {
		logger.info("AuthController(getAdminPassword) >> Entry");
		SuccessandMessageDto response = new SuccessandMessageDto();
		try{
			Admin adminPassword = adminRepo.findByEmail(jwtGenerator.getUsernameFromJWT(token.substring(7))).orElseThrow();
			String adminRecoveredPassword = adminPassword.getPassword(); // password retrieved from db

			PasswordEncoder passwordEncoder1 = new BCryptPasswordEncoder();
			boolean isPasswordMatch = passwordEncoder1.matches(oldPassword,adminRecoveredPassword); // compare the db password and entered old password

			if(isPasswordMatch) {
				adminPassword.setPassword(passwordEncoder.encode(confirmpassword));
				Admin adminData = adminRepo.save(adminPassword);
				logger.info("AuthController(getAdminPassword)>> Exit");
				response.setMessage("Admin password changed successfully");
				response.setSuccess(true);
				return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.OK);
			} else {
				response.setMessage("Old password is incorrect");
				response.setSuccess(false);
				logger.info("AuthController(getAdminPassword)>> Exit");
				return new ResponseEntity<SuccessandMessageDto>(response, HttpStatus.OK);
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.info("AuthController(getAdminPassword)>> Exit");
		}
		return null;
	}
}
