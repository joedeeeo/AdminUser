package com.example.exam.service;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.exam.dto.LoginRequest;
import com.example.exam.dto.LoginResponse;
import com.example.exam.proxy.AdminUserProxy;


public interface AdminUserService {
	
	public List<AdminUserProxy> getAllUsers();
	public AdminUserProxy getAdminDetails(String email);
	public Boolean updateUserDetails(AdminUserProxy updatedAdminUserProxy, MultipartFile image);
	public LoginResponse login(LoginRequest request);
	public List<AdminUserProxy> getAllUsersWithName(String name);
	public Boolean addDummyData(Integer count);
	public Boolean deleteUser(Long id);
	Page<AdminUserProxy> getAllUsers(Pageable pageable);
	
}
