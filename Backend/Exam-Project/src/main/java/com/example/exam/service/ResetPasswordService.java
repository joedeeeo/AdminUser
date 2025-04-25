package com.example.exam.service;

import com.example.exam.dto.ResetPasswordRequest;

public interface ResetPasswordService {
	
	public String getToken(String email);
	public Boolean resetPassword(ResetPasswordRequest request);
	
}
