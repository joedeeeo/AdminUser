package com.example.exam.entity;

import java.sql.Date;

import com.example.exam.enums.Gender;
import com.example.exam.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class AdminUser {
	
//	1. name
//	 2. dob
//	 3. username
//	 4. password
//	 5. gender (use an enum)
//	 6. address
//	 7. profileImage
//	 8. contactNumber
//	 9. pinCode
//	 10. accessRole
	 
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	 
	 @NotBlank
	 private String name;
	 
	 
	 @Column(unique = true)
	 private String email;
	 
	 
	 private String password;
	 
	 
	 private Date dob;
	 
	 @Enumerated(EnumType.STRING)
	 private Gender gender;
	 
	 @Positive
	 private Long pinCode;
	 
	 private String profileImage;
	 
	 private String contactNumber;
	 
	 
	 private String address;
	 
	 @Enumerated(EnumType.STRING)
	 private Role role;
}
