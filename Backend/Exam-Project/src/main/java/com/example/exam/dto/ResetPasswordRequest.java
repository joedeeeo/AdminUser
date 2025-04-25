package com.example.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ResetPasswordRequest {
	String token;
	String newPassword;
}