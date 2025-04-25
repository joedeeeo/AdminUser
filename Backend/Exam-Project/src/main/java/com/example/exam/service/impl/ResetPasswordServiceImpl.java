package com.example.exam.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.exam.dto.ResetPasswordRequest;
import com.example.exam.entity.AdminUser;
import com.example.exam.entity.ResetPasswordToken;
import com.example.exam.repo.AdminUserRepo;
import com.example.exam.repo.ResetPasswordTokenRepository;
import com.example.exam.service.ResetPasswordService;



@Service
public class ResetPasswordServiceImpl implements ResetPasswordService{
	
	@Autowired
	private ResetPasswordTokenRepository token_db;
	
	@Autowired
	private AdminUserRepo user_db;
	
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@Override
	public String getToken(String email) {
		if(user_db.findByEmail(email).isEmpty()) return "No Such User Exists";
		
		ResetPasswordToken newToken = new ResetPasswordToken();
		newToken.setEmail(email);
		newToken.setExpirationTime(LocalDateTime.now().plusMinutes(10));
		newToken = token_db.save(newToken);
		
		System.err.println(newToken);
		
		return "Password reset link sent to the registered email address.";
	}

	@Override
	public Boolean resetPassword(ResetPasswordRequest request) {
		
		if(!token_db.existsById(request.getToken())) return false;
		
		ResetPasswordToken token = token_db.findById(request.getToken()).get();
		
		if(token_db.findById(request.getToken()).get().getExpirationTime()
		.isBefore(LocalDateTime.now()) || user_db.findByEmail(token.getEmail()).isEmpty())
		{
			token_db.delete(token);
			return false;
		}
		
		AdminUser user = user_db.findByEmail(token.getEmail()).get();
		user.setPassword(encoder.encode(request.getNewPassword()));
		user_db.save(user);
		
		token_db.delete(token); 
		
		return true;
	}

}
