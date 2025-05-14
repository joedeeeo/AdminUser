package com.example.exam.proxy;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

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
import com.fasterxml.jackson.annotation.JsonProperty;

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
	 
	 @NotNull
	 private Gender gender;
	 
	 @NotNull @Positive
	 private Long pinCode;
	 
	 private byte[] profileImage;
	 
	 private String password;
	 
	 @NotBlank
	 private String contactNumber;
	 
	 @NotBlank 
	 private String address;
	 
	 @NotNull @Enumerated(EnumType.STRING)
	 private Role role;
	 
	 private Boolean isActive;
	 
	 private Timestamp createdDate;
	 
	 @JsonProperty("formattedCreatedDate")
	 public String getFormattedCreatedDate() {
	     if (createdDate != null) {
	         return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(createdDate);
	     }
	     return null;
	 }
	 
	 private Timestamp modifiedDate;
	 
	 @JsonProperty("formattedModifiedDate")
	 public String getFormattedModifiedDate() {
	     if (createdDate != null) {
	         return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(modifiedDate);
	     }
	     return null;
	 }
}
