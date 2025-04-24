package com.example.exam.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.exam.dto.LoginRequest;
import com.example.exam.dto.LoginResponse;
import com.example.exam.dto.ResetPasswordRequest;
import com.example.exam.proxy.AdminUserProxy;
import com.example.exam.service.AdminUserService;
import com.example.exam.service.ResetPasswordService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AdminUserController {
	
	@Autowired
	private AdminUserService userService;
	
	@Autowired
	private ResetPasswordService resetPasswordService;
	
	
	@GetMapping("/get-all-users")
	public List<AdminUserProxy> getAllUsers()
	{
		return userService.getAllUsers();
	}
	
	@GetMapping("/get-admin-data/{email}")
	public AdminUserProxy getAdminDetails(@NotBlank @PathVariable String email)
    {
		return userService.getAdminDetails(email);
	}
	
	@PutMapping("/update-user-data")
	public Boolean updateUserDetails(@RequestPart @Valid AdminUserProxy updatedAdminUserProxy, @RequestPart MultipartFile image)
	{
		return userService.updateUserDetails(updatedAdminUserProxy, image);
	}
	
	@PostMapping("/login")
	public LoginResponse login(@RequestBody @Valid LoginRequest request)
	{
		return userService.login(request);
	}
	
	@GetMapping("/search-users-with-name/{name}")
	public List<AdminUserProxy> getAllUsersWithName(@PathVariable @NotBlank String name)
	{
		return userService.getAllUsersWithName(name);
	}
	
	@GetMapping("/send-reset-password-mail/{email}")
	public String getToken(@PathVariable @NotBlank String email)
	{
		return resetPasswordService.getToken(email);
	}
	
	@PostMapping("/reset-password")
	public Boolean resetPassword(@Valid @RequestBody ResetPasswordRequest request)
	{
		return resetPasswordService.resetPassword(request);
	}
	
	@GetMapping("/add-dummy-data/{count}")
	public Boolean addDummyData(@PathVariable @Positive Integer count) {
		return userService.addDummyData(count);
	}
	
	@DeleteMapping("/delete-user/{id}")
	public Boolean deleteUser(@PathVariable Long id) {
		return userService.deleteUser(id);
	}
	
	@GetMapping("/get-user-table/{page}/{size}")
	  public ResponseEntity<Page<AdminUserProxy>> listUsers(
	      @PathVariable int page,
	      @PathVariable int size) {
		
	    Pageable pageable = PageRequest.of(page, size);

	    Page<AdminUserProxy> result = userService.getAllUsers(pageable);
	    return ResponseEntity.ok(result);
	  }
	
}
