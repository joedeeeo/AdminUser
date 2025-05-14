package com.example.exam.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import com.example.exam.utils.JwtUtils;
import com.example.exam.utils.Mapper;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

	@Mock
	private Mapper mapper;

	@Mock
	private AdminUserRepo db;

	@Mock
	private BCryptPasswordEncoder encoder;

	@InjectMocks
	private AdminUserServiceImpl service;

	@Mock
	private JwtUtils jwtUtils;

	private AdminUserProxy proxy;
	private AdminUser entity;

	@BeforeEach
	void setUp() {
		proxy = new AdminUserProxy();
		proxy.setPassword("rawpw");
		proxy.setName("Alice");
		proxy.setEmail("alice@example.com");

		entity = new AdminUser();
		entity.setPassword("rawpw");
	}

	@Test
	void addUser_encodesPassword_setsActive_andSaves() {
		when(mapper.proxyToEntity(proxy)).thenReturn(entity);
		when(encoder.encode("rawpw")).thenReturn("ENC(rawpw)");

		String result = service.addUser(proxy);

		assertEquals("User add succesfully", result);

		ArgumentCaptor<AdminUser> captor = ArgumentCaptor.forClass(AdminUser.class);
		verify(db).save(captor.capture());
		AdminUser saved = captor.getValue();

		assertEquals("ENC(rawpw)", saved.getPassword());
		assertEquals(Boolean.TRUE, saved.getIsActive());
	}

	@Test
	void getAllUsers_returnsMappedProxiesWithImage() throws Exception {
		AdminUser entity = new AdminUser();
		entity.setName("Alice");
		entity.setProfileImage("test-image.png");

		List<AdminUser> entities = List.of(entity);
		when(db.findByRole(Role.USER)).thenReturn(entities);

		AdminUserProxy proxy = new AdminUserProxy();
		when(mapper.entityToProxy(entity)).thenReturn(proxy);

		java.nio.file.Path imagePath = Paths.get("src/main/resources/static/profile_images/test-image.png");
		Files.createDirectories(imagePath.getParent());
		byte[] dummyImageBytes = "dummy".getBytes();
		Files.write(imagePath, dummyImageBytes);

		List<AdminUserProxy> result = service.getAllUsers();

		assertEquals(1, result.size());
		assertArrayEquals(dummyImageBytes, result.get(0).getProfileImage());

		Files.deleteIfExists(imagePath);
	}

	@Test
	void getAdminDetails_returnsMappedProxyWithImage() throws Exception {
		String email = "admin@example.com";

		AdminUser entity = new AdminUser();
		entity.setEmail(email);
		entity.setProfileImage("test-image.png");
		Optional<AdminUser> optionalEntity = Optional.of(entity);

		when(db.findByEmail(email)).thenReturn(optionalEntity);

		AdminUserProxy proxy = new AdminUserProxy();
		when(mapper.entityToProxy(entity)).thenReturn(proxy);

		java.nio.file.Path imagePath = Paths.get("src/main/resources/static/profile_images/test-image.png");
		Files.createDirectories(imagePath.getParent());
		byte[] dummyImageBytes = "dummy-image".getBytes();
		Files.write(imagePath, dummyImageBytes);

		AdminUserProxy result = service.getAdminDetails(email);

		assertArrayEquals(dummyImageBytes, result.getProfileImage());

		Files.deleteIfExists(imagePath);
	}

	@Test
	void login_returnsJwtAndRole_whenCredentialsAreCorrect() {
		String email = "admin@example.com";
		String rawPassword = "password";
		String encodedPassword = "encodedPassword";
		String jwt = "mocked.jwt.token";

		AdminUser entity = new AdminUser();
		entity.setEmail(email);
		entity.setPassword(encodedPassword);
		entity.setRole(Role.ADMIN);

		LoginRequest request = new LoginRequest(email, rawPassword);

		when(db.findByEmail(email)).thenReturn(Optional.of(entity));
		when(encoder.matches(rawPassword, encodedPassword)).thenReturn(true);
		when(jwtUtils.generateToken(email)).thenReturn(jwt);

		LoginResponse response = service.login(request);

		assertEquals(jwt, response.getJwt());
		assertEquals(Role.ADMIN, response.getRole());
	}

	@Test
	void login_throwsException_whenPasswordIsIncorrect() {
		String email = "admin@example.com";
		String rawPassword = "wrongpass";
		String encodedPassword = "encodedPassword";

		AdminUser entity = new AdminUser();
		entity.setEmail(email);
		entity.setPassword(encodedPassword);

		LoginRequest request = new LoginRequest(email, rawPassword);

		when(db.findByEmail(email)).thenReturn(Optional.of(entity));
		when(encoder.matches(rawPassword, encodedPassword)).thenReturn(false);

		GlobalException ex = assertThrows(GlobalException.class, () -> service.login(request));

		assertEquals(ExceptionTypes.INCORRECT_PASSWORD.getStatusCode(), ex.getCode());
		assertEquals(ExceptionTypes.INCORRECT_PASSWORD.getMessage(), ex.getMessage());
	}

	@Test
	void login_throwsException_whenUserNotFound() {
		String email = "missing@example.com";
		LoginRequest request = new LoginRequest(email, "any");

		when(db.findByEmail(email)).thenReturn(Optional.empty());

		GlobalException ex = assertThrows(GlobalException.class, () -> service.login(request));

		assertEquals(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(), ex.getCode());
		assertEquals(ExceptionTypes.NO_SUCH_USER_EXISTS.getMessage(), ex.getMessage());
	}

	@Test
	void updateUserDetails_updatesFieldsAndImageSuccessfully() throws Exception {
		AdminUser existing = new AdminUser();
		existing.setId(1L);
		existing.setProfileImage("old.png");

		AdminUserProxy updatedProxy = new AdminUserProxy();
		updatedProxy.setId(1L);
		updatedProxy.setName("Updated Name");
		updatedProxy.setEmail("updated@example.com");
		updatedProxy.setAddress("Updated Address");
		updatedProxy.setContactNumber("1234567890");
		LocalDate localDate = LocalDate.of(1995, 5, 9);
		Date sqlDate = Date.valueOf(localDate);
		updatedProxy.setDob(sqlDate);
		updatedProxy.setGender(Gender.MALE);
		updatedProxy.setPinCode(560001L);

		byte[] fakeImageBytes = "fake image content".getBytes();
		MultipartFile image = mock(MultipartFile.class);
		when(image.isEmpty()).thenReturn(false);
		when(image.getContentType()).thenReturn("image/png");
		when(image.getInputStream()).thenReturn(new ByteArrayInputStream(fakeImageBytes));

		when(db.findById(1L)).thenReturn(Optional.of(existing));

		boolean result = service.updateUserDetails(updatedProxy, image);

		assertTrue(result);
		assertEquals("Updated Name", existing.getName());
		verify(db).save(existing);
	}

	@Test
	public void testDeleteUser_Success() {
		Long userId = 1L;
		AdminUser user = new AdminUser();
		user.setId(userId);
		user.setRole(Role.USER);
		user.setIsActive(true);

		when(db.existsById(userId)).thenReturn(true);
		when(db.findById(userId)).thenReturn(Optional.of(user));

		Boolean result = service.deleteUser(userId);

		assertTrue(result);
		verify(db, times(1)).save(user);
	}

	@Test
	public void testDeleteUser_NoSuchUserExists() {
		Long userId = 2L;

		when(db.existsById(userId)).thenReturn(false);

		GlobalException exception = assertThrows(GlobalException.class, () -> {
			service.deleteUser(userId);
		});

		assertEquals(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(), exception.getCode());
	}

	@Test
	public void testDeleteUser_ForbidenAdminDeletion() {
		Long userId = 3L;
		AdminUser user = new AdminUser();
		user.setId(userId);
		user.setRole(Role.ADMIN);

		when(db.existsById(userId)).thenReturn(true);
		when(db.findById(userId)).thenReturn(Optional.of(user));

		GlobalException exception = assertThrows(GlobalException.class, () -> {
			service.deleteUser(4L);
		});

		assertEquals(ExceptionTypes.FORBIDDEN_ADMIN_DELETION.getStatusCode(), exception.getCode());
	}
}

