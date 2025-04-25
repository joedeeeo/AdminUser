package com.example.exam.proxy;

import java.sql.Date;

import com.example.exam.enums.Gender;
import com.example.exam.enums.Role;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminUserProxy {
	
	 private Long id;
	 
	 @NotBlank
	 private String name;
	 
	 @NotBlank
	 private String email;
	 
	 @NotNull
	 private Date dob;
	 
	 @NotNull @Enumerated(EnumType.STRING)
	 private Gender gender;
	 
	 @NotNull @Positive
	 private Long pinCode;
	 
	 private byte[] profileImage;
	 
	 @NotBlank
	 private String contactNumber;
	 
	 @NotBlank 
	 private String address;
	 
	 @NotNull @Enumerated(EnumType.STRING)
	 private Role role;
	 
	 
}
