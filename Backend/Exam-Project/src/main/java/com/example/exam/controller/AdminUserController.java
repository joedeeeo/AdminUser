package com.example.exam.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.exam.dto.LoginRequest;
import com.example.exam.dto.LoginResponse;
import com.example.exam.dto.ResetPasswordRequest;
import com.example.exam.entity.AdminUser;
import com.example.exam.proxy.AdminUserProxy;
import com.example.exam.proxy.ApiResponse;
import com.example.exam.repo.AdminUserRepo;
import com.example.exam.service.AdminUserService;
import com.example.exam.service.ResetPasswordService;

import jakarta.servlet.http.HttpServletResponse;
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
	
	@Autowired
	private AdminUserRepo repo;
	
	@PostMapping("/adduser")
	public ResponseEntity<ApiResponse> addUser(@Valid @RequestBody AdminUserProxy adminUserProxy) {
	    try {
	        String message = userService.addUser(adminUserProxy);
	        return ResponseEntity.ok(new ApiResponse(
	                HttpStatus.OK.value(),
	                "User added successfully",
	                message));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new ApiResponse(
	                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
	                        "Failed to add user",
	                        e.getMessage()));
	    }
	}

	
	
	 @GetMapping("/get-all-users")
	    public ResponseEntity<ApiResponse> getAllUsers() {
	        try {
	            return ResponseEntity.ok(new ApiResponse(
	                    HttpStatus.OK.value(), 
	                    "Users fetched successfully", 
	                    userService.getAllUsers()));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ApiResponse(
	                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	                            "Failed to fetch users", 
	                            null));
	        }
	    }

	    @GetMapping("/get-admin-data/{email}")
	    public ResponseEntity<ApiResponse> getAdminDetails(@NotBlank @PathVariable String email) {
	        try {
	            return ResponseEntity.ok(new ApiResponse(
	                    HttpStatus.OK.value(), 
	                    "Admin data fetched successfully", 
	                    userService.getAdminDetails(email)));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new ApiResponse(
	                            HttpStatus.NOT_FOUND.value(), 
	                            "Admin not found", 
	                            null));
	        }
	    }

	    @PutMapping("/update-user-data")
	    public ResponseEntity<ApiResponse> updateUserDetails(
	            @RequestPart @Valid AdminUserProxy updatedAdminUserProxy,
	            @RequestPart MultipartFile image) {
	        try {
	            Boolean updated = userService.updateUserDetails(updatedAdminUserProxy, image);
	            if (updated) {
	                return ResponseEntity.ok(new ApiResponse(
	                        HttpStatus.OK.value(), 
	                        "User updated successfully", 
	                        updated));
	            } else {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                        .body(new ApiResponse(
	                                HttpStatus.BAD_REQUEST.value(), 
	                                "Failed to update user", 
	                                null));
	            }
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ApiResponse(
	                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	                            "Error updating user", 
	                            null));
	        }
	    }

	    @PostMapping("/login")
	    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest request) {
	        try {
	            LoginResponse response = userService.login(request);
	            return ResponseEntity.ok(new ApiResponse(
	                    HttpStatus.OK.value(), 
	                    "Login successful", 
	                    response));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(new ApiResponse(
	                            HttpStatus.UNAUTHORIZED.value(), 
	                            "Invalid credentials", 
	                            null));
	        }
	    }

	    @GetMapping("/search-users-with-name/{name}")
	    public ResponseEntity<ApiResponse> getAllUsersWithName(@PathVariable @NotBlank String name) {
	        try {
	            return ResponseEntity.ok(new ApiResponse(
	                    HttpStatus.OK.value(), 
	                    "Users fetched successfully", 
	                    userService.getAllUsersWithName(name)));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ApiResponse(
	                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	                            "Failed to fetch users by name", 
	                            null));
	        }
	    }

	    @GetMapping("/send-reset-password-mail/{email}")
	    public ResponseEntity<ApiResponse> getToken(@PathVariable @NotBlank String email) {
	        try {
	            return ResponseEntity.ok(new ApiResponse(
	                    HttpStatus.OK.value(), 
	                    "Reset password token sent", 
	                    resetPasswordService.getToken(email)));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ApiResponse(
	                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	                            "Error sending reset password mail", 
	                            null));
	        }
	    }

	    @PostMapping("/reset-password")
	    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
	        try {
	            Boolean success = resetPasswordService.resetPassword(request);
	            if (success) {
	                return ResponseEntity.ok(new ApiResponse(
	                        HttpStatus.OK.value(), 
	                        "Password reset successfully", 
	                        success));
	            } else {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                        .body(new ApiResponse(
	                                HttpStatus.BAD_REQUEST.value(), 
	                                "Failed to reset password", 
	                                null));
	            }
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ApiResponse(
	                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	                            "Error resetting password", 
	                            null));
	        }
	    }

	    @GetMapping("/add-dummy-data/{count}")
	    public ResponseEntity<ApiResponse> addDummyData(@PathVariable @Positive Integer count) {
	        try {
	            return ResponseEntity.ok(new ApiResponse(
	                    HttpStatus.OK.value(), 
	                    "Dummy data added successfully", 
	                    userService.addDummyData(count)));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ApiResponse(
	                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	                            "Failed to add dummy data", 
	                            null));
	        }
	    }

	    @DeleteMapping("/delete-user/{id}")
	    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
	        try {
	            Boolean success = userService.deleteUser(id);
	            if (success) {
	                return ResponseEntity.ok(new ApiResponse(
	                        HttpStatus.OK.value(), 
	                        "User deleted successfully", 
	                        success));
	            } else {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                        .body(new ApiResponse(
	                                HttpStatus.BAD_REQUEST.value(), 
	                                "Failed to delete user", 
	                                null));
	            }
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ApiResponse(
	                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	                            "Error deleting user", 
	                            null));
	        }
	    }

	    @GetMapping("/get-user-table/{page}/{size}/{role}")
	    public ResponseEntity<ApiResponse> listUsers(
	            @PathVariable int page,
	            @PathVariable int size,
	            @PathVariable String role,
	            @RequestParam(required = false) String search) {

	        try {
	            Pageable pageable = PageRequest.of(page, size);
	            Page<AdminUserProxy> result = userService.getAllUsers(pageable, search, role);
	            return ResponseEntity.ok(new ApiResponse(
	                    HttpStatus.OK.value(), 
	                    "User list fetched successfully", 
	                    result));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ApiResponse(
	                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	                            "Error fetching user list", 
	                            null));
	        }
	    }

	    @GetMapping("/download-users-excel")
	    public ResponseEntity<ApiResponse> downloadUsersExcel(HttpServletResponse response) {
	        try {
	            userService.exportUsersToExcel(response);
	            return ResponseEntity.ok(new ApiResponse(
	                    HttpStatus.OK.value(), 
	                    "Excel file generated", 
	                    null));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ApiResponse(
	                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	                            "Error generating Excel file", 
	                            null));
	        }
	    }
	    
	    
	    @PostMapping("/upload")
	    public ResponseEntity<Map<String, Object>> uploadAdminUsers(@RequestParam("file") MultipartFile file) {
//	        // Validate file format
//	        if (file == null || file.isEmpty()) {
//	            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded."));
//	        }
//
//	        if (!file.getOriginalFilename().endsWith(".xlsx")) {
//	            return ResponseEntity.badRequest().body(Map.of("error", "Only .xlsx files are supported."));
//	        }
//
//	        try {
//	            Map<String, Object> result = userService.uploadUsers(file);
//	            return ResponseEntity.ok(result);
//	        } catch (Exception ex) {
//	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	                    .body(Map.of("error", "Failed to upload users: " + ex.getMessage()));
//	        }
	    	
	    	String filename = file.getOriginalFilename() == null
	                ? "" : file.getOriginalFilename().toLowerCase();

	            if (file.isEmpty() ||
	                !(filename.endsWith(".xlsx")
	               || filename.endsWith(".csv")
	               || filename.endsWith(".tsv"))) {
	                return ResponseEntity
	                    .badRequest()
	                    .body(Map.of("error",
	                        "Please upload a non-empty file of type .xlsx, .csv, or .tsv"));
	            }

	            Map<String, Object> result = userService.uploadUsers(file);
	            return ResponseEntity.ok(result);
	    }
	    
	    
	    @GetMapping("/download-template")
	    public ResponseEntity<byte[]> downloadTemplate() {
//	        // Get the Excel template as a byte array from the service
//			byte[] excelFile = userService.downloadExcelTemplate();
//
//			// Set headers for the response
//			HttpHeaders headers = new HttpHeaders();
//			headers.add("Content-Disposition", "attachment; filename=template.xlsx");
//
//			// Return the response with the Excel file and appropriate headers
//			return ResponseEntity.ok()
//			        .headers(headers)
//			        .body(excelFile);
	    	
	    	byte[] file = userService.downloadExcelTemplate();
	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType(
	                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=template.xlsx")
	                .body(file);
	    }
	    
	    @GetMapping("/check-email")
	    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email){
	    	Optional<AdminUser> found = repo.findByEmail(email);
	        Map<String,Object> resp = new HashMap<>();
	        resp.put("exists", found.isPresent());
	        found.ifPresent(u -> resp.put("id", u.getId()));
	        found.ifPresent(u -> resp.put("email", u.getEmail()));
	        return ResponseEntity.ok(resp);
	    }
	    
	    
}
