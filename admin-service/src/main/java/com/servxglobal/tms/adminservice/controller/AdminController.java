package com.servxglobal.tms.adminservice.controller;
import com.servxglobal.tms.adminservice.model.Admin;
import com.servxglobal.tms.adminservice.repository.AdminRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AdminRepo adminRepo;

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@PostMapping("/register")
	public ResponseEntity<String> adminRegister(@RequestBody Admin admin) {
		logger.info("AuthController(adminRegister) >> Entry");
		if(adminRepo.existsByEmail(admin.getEmail())) {
			return new ResponseEntity<String>("Mail id already exist! ",HttpStatus.BAD_REQUEST);
		}
		Admin adminEntity = new Admin();
		adminEntity.setEmail(admin.getEmail());
		adminEntity.setUsername(admin.getUsername());
		adminEntity.setPassword(passwordEncoder.encode(admin.getPassword()));
		adminEntity.setId(adminRepo.count()+1);
		adminRepo.save(adminEntity);
		logger.info("AuthController(adminRegister) >> Exit");
		return new ResponseEntity<String>("Admin Register successfully! ", HttpStatus.CREATED);
	}

}



