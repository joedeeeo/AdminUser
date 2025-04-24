package com.example.exam.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.exam.dto.LoginRequest;
import com.example.exam.dto.LoginResponse;
import com.example.exam.entity.AdminUser;
import com.example.exam.enums.Gender;
import com.example.exam.enums.Role;
import com.example.exam.exception.ExceptionTypes;
import com.example.exam.exception.GlobalException;
import com.example.exam.proxy.AdminUserProxy;
import com.example.exam.repo.AdminUserRepo;
import com.example.exam.service.AdminUserService;
import com.example.exam.utils.JwtUtils;
import com.example.exam.utils.Mapper;
import com.github.javafaker.Faker;
import org.springframework.data.domain.PageImpl;

@Service
public class AdminUserServiceImpl implements AdminUserService{
		
	@Autowired
	private Mapper mapper;
	
	@Autowired
	private AdminUserRepo db;
	
	@Autowired
	private JwtUtils jwtUtils;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	
	@Override
	public List<AdminUserProxy> getAllUsers() {
		
		List<AdminUser> entities = db.findByRole(Role.USER);
		List<AdminUserProxy> proxies = new ArrayList<>();
		
		for(AdminUser entity : entities)
		{
			AdminUserProxy proxy = mapper.entityToProxy(entity);
			
			File profile_image = new File("src/main/resources/static/profile_images/" + entity.getProfileImage());
			try {
				proxy.setProfileImage(Files.readAllBytes(profile_image.toPath()));
			} catch (IOException e) {}
			
			proxies.add(proxy);
		}
		
		return proxies;
	}

	@Override
	public AdminUserProxy getAdminDetails(String email) {
		
		Optional<AdminUser> optinal = db.findByEmail(email);
		if(optinal.isEmpty()) throw new GlobalException(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(), 
				                                        ExceptionTypes.NO_SUCH_USER_EXISTS.getMessage());
		
		AdminUser entity = optinal.get();
		
		File profile_image = new File("src/main/resources/static/profile_images/" + entity.getProfileImage());
		AdminUserProxy proxy = mapper.entityToProxy(entity);
		try {
			proxy.setProfileImage(Files.readAllBytes(profile_image.toPath()));
		} catch (IOException e) {}
		
		return proxy;
	}

	@Override
	public Boolean updateUserDetails(AdminUserProxy updatedAdminUserProxy, MultipartFile image) {
		
		Optional<AdminUser> optinal = db.findById(updatedAdminUserProxy.getId());
		if(optinal.isEmpty()) throw new GlobalException(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(), 
                ExceptionTypes.NO_SUCH_USER_EXISTS.getMessage());;
		
		AdminUser entity = optinal.get();
		
		entity.setAddress(updatedAdminUserProxy.getAddress());
		entity.setContactNumber(updatedAdminUserProxy.getContactNumber());
		entity.setDob(updatedAdminUserProxy.getDob());
		entity.setEmail(updatedAdminUserProxy.getEmail());
		entity.setGender(updatedAdminUserProxy.getGender());
		entity.setName(updatedAdminUserProxy.getName());
		entity.setPinCode(updatedAdminUserProxy.getPinCode());
		
		String img_name = entity.getId() +"."+ image.getContentType().substring(image.getContentType().lastIndexOf("/") + 1);
		File img = new File("src/main/resources/static/profile_images/" + img_name);
		
		try {
			Files.copy(image.getInputStream(), img.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {return false;}
		
		entity.setProfileImage(img_name);
		
		db.save(entity);
		
		return true;
	}

	@Override
	public LoginResponse login(LoginRequest request) {
		
		Optional<AdminUser> optinal = db.findByEmail(request.getEmail());
		if(optinal.isEmpty()) throw new GlobalException(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(), 
                ExceptionTypes.NO_SUCH_USER_EXISTS.getMessage());
		
		AdminUser entity = optinal.get();
		
		if(!encoder.matches(request.getPassword(), entity.getPassword()))
		{
			throw new GlobalException(ExceptionTypes.INCORRECT_PASSWORD.getStatusCode(), 
					ExceptionTypes.INCORRECT_PASSWORD.getMessage());
		}
		
		String jwt = jwtUtils.generateToken(entity.getEmail());
		
		return new LoginResponse(jwt, entity.getRole());
	}

	@Override
	public List<AdminUserProxy> getAllUsersWithName(String name) {
		
		List<AdminUser> entities = db.findByNameStartingWith(name);
		List<AdminUserProxy> proxies = new ArrayList<>();
		
		for(AdminUser entity : entities)
		{
			AdminUserProxy proxy = mapper.entityToProxy(entity);
			
			File profile_image = new File("src/main/resources/static/profile_images/" + entity.getProfileImage());
			try {
				proxy.setProfileImage(Files.readAllBytes(profile_image.toPath()));
			} catch (IOException e) {}
			
			proxies.add(proxy);
		}
		
		return proxies.stream().filter(Proxy -> Proxy.getRole() == Role.USER).collect(Collectors.toList());
	}

	@Override
	public Boolean addDummyData(Integer count) {
		Faker faker = Faker.instance();
		
		for(int i = 0; i < count; i++)
		{
			AdminUser entity = new AdminUser();
			
			entity.setAddress(faker.address().buildingNumber() +" "+ faker.address().cityName());
			entity.setContactNumber(faker.phoneNumber().cellPhone());
			
			long minDay = LocalDate.of(1970, 1, 1).toEpochDay();
		    long maxDay = LocalDate.of(2015, 12, 31).toEpochDay();
		    long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
		    LocalDate randomDate = LocalDate.ofEpochDay(randomDay);
			
			entity.setDob(Date.valueOf(randomDate));
			
			entity.setEmail(faker.internet().emailAddress().toString());
			
			Random rand = new Random();
	        int rand_int = rand.nextInt(10);
	        
			entity.setGender(rand_int >= 5 ? Gender.MALE : Gender.FEMALE);
			
			entity.setName(faker.superhero().name());
			
			entity.setPassword(encoder.encode(faker.internet().password()));
			
			int pin = rand.nextInt(99999);
			entity.setPinCode((long) pin);
			
			entity.setRole(Role.USER);
			
			
			
			db.save(entity);
		}
		
		return true;
	}

	@Override
	public Boolean deleteUser(Long id) {
		if(!db.existsById(id)) throw new GlobalException(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(), 
                ExceptionTypes.NO_SUCH_USER_EXISTS.getMessage());
		
		AdminUser entity = db.findById(id).get();
		
		if(entity.getRole() == Role.ADMIN) throw new GlobalException(ExceptionTypes.FORBIDDEN_ADMIN_DELETION.getStatusCode(), 
                ExceptionTypes.FORBIDDEN_ADMIN_DELETION.getMessage());
		
		db.deleteById(id);
		// improvement: del image from folder
		return true;
	}

	
	@Override
	  public Page<AdminUserProxy> getAllUsers(Pageable pageable) {
		
	    Page<AdminUser> pageOfEntities = db.findByRole(Role.USER, pageable);

	    
	    List<AdminUserProxy> proxyList = pageOfEntities.stream()
	      .map(entity -> {
	    	  
	        AdminUserProxy proxy = mapper.entityToProxy(entity);
	        File profileImageFile = new File("src/main/resources/static/profile_images/" 
	                                          + entity.getProfileImage());
	        
	        System.err.println(entity.getProfileImage());
	        
	        try {
	          proxy.setProfileImage(Files.readAllBytes(profileImageFile.toPath()));
	        } catch (IOException e) {
	          
	        }
	        return proxy;
	      })
	      .collect(Collectors.toList());

	    
	    return new PageImpl<>(
	      proxyList,
	      pageable,
	      pageOfEntities.getTotalElements()
	    );
	  }
	
	
	
	}
