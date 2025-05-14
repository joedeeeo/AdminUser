package com.example.exam.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AdminUserServiceImpl implements AdminUserService {

	@Autowired
	private Mapper mapper;

	@Autowired
	private AdminUserRepo db;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private BCryptPasswordEncoder encoder;
	
    public static String[] HEADERS = {
            "Name", "Email", "Password", "Date of Birth", "Gender", "Pin Code", "Contact Number", "Address", "Role"
        };
	
	@Override
	public String addUser(AdminUserProxy adminProxy) {
		AdminUser adminUser = mapper.proxyToEntity(adminProxy);
		adminUser.setPassword(encoder.encode(adminUser.getPassword()));
		adminUser.setIsActive(true);
		db.save(adminUser);
		return "User add succesfully";
		
	}

	@Override
	public List<AdminUserProxy> getAllUsers() {

		List<AdminUser> entities = db.findByRole(Role.USER);
		List<AdminUserProxy> proxies = new ArrayList<>();

		for (AdminUser entity : entities) {
			AdminUserProxy proxy = mapper.entityToProxy(entity);

			File profile_image = new File("src/main/resources/static/profile_images/" + entity.getProfileImage());
			try {
				proxy.setProfileImage(Files.readAllBytes(profile_image.toPath()));
			} catch (IOException e) {
			}

			proxies.add(proxy);
		}

		return proxies;
	}

	@Override
	public AdminUserProxy getAdminDetails(String email) {

		Optional<AdminUser> optinal = db.findByEmail(email);
		if (optinal.isEmpty())
			throw new GlobalException(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(),
					ExceptionTypes.NO_SUCH_USER_EXISTS.getMessage());

		AdminUser entity = optinal.get();

		File profile_image = new File("src/main/resources/static/profile_images/" + entity.getProfileImage());
		AdminUserProxy proxy = mapper.entityToProxy(entity);
		try {
			proxy.setProfileImage(Files.readAllBytes(profile_image.toPath()));
		} catch (IOException e) {
		}

		return proxy;
	}

	@Override
	public Boolean updateUserDetails(AdminUserProxy updatedAdminUserProxy, MultipartFile image) {

		Optional<AdminUser> optinal = db.findById(updatedAdminUserProxy.getId());
		if (optinal.isEmpty())
			throw new GlobalException(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(),
					ExceptionTypes.NO_SUCH_USER_EXISTS.getMessage());
		;

		AdminUser entity = optinal.get();

		entity.setAddress(updatedAdminUserProxy.getAddress());
		entity.setContactNumber(updatedAdminUserProxy.getContactNumber());
		entity.setDob(updatedAdminUserProxy.getDob());
		entity.setEmail(updatedAdminUserProxy.getEmail());
		entity.setGender(updatedAdminUserProxy.getGender());
		entity.setName(updatedAdminUserProxy.getName());
		entity.setPinCode(updatedAdminUserProxy.getPinCode());
//		entity.setModifiedDate(updatedAdminUserProxy.getCreatedDate());

		 if (image != null && !image.isEmpty()) {
		String img_name = entity.getId() + "."
				+ image.getContentType().substring(image.getContentType().lastIndexOf("/") + 1);
		File img = new File("src/main/resources/static/profile_images/" + img_name);

		try {
			Files.copy(image.getInputStream(), img.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			return false;
		}

		entity.setProfileImage(img_name);
		 }

		db.save(entity);

		return true;
	}

	@Override
	public LoginResponse login(LoginRequest request) {

		Optional<AdminUser> optinal = db.findByEmail(request.getEmail());
		if (optinal.isEmpty())
			throw new GlobalException(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(),
					ExceptionTypes.NO_SUCH_USER_EXISTS.getMessage());

		AdminUser entity = optinal.get();

		if (!encoder.matches(request.getPassword(), entity.getPassword())) {
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

		for (AdminUser entity : entities) {
			AdminUserProxy proxy = mapper.entityToProxy(entity);

			File profile_image = new File("src/main/resources/static/profile_images/" + entity.getProfileImage());
			try {
				proxy.setProfileImage(Files.readAllBytes(profile_image.toPath()));
			} catch (IOException e) {
			}

			proxies.add(proxy);
		}

		return proxies.stream().filter(Proxy -> Proxy.getRole() == Role.USER).collect(Collectors.toList());
	}

	@Override
	public Boolean addDummyData(Integer count) {
		Faker faker = Faker.instance();

		for (int i = 0; i < count; i++) {
			AdminUser entity = new AdminUser();

			if (i == 0) {
				entity.setRole(Role.ADMIN);
			} else {
				entity.setRole(Role.USER);
			}

			entity.setAddress(faker.address().buildingNumber() + " " + faker.address().cityName());
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

//			entity.setRole(Role.USER);
			entity.setIsActive(true);

			db.save(entity);
		}

		return true;
	}

	@Override
	public Boolean deleteUser(Long id) {
		if (!db.existsById(id))
			throw new GlobalException(ExceptionTypes.NO_SUCH_USER_EXISTS.getStatusCode(),
					ExceptionTypes.NO_SUCH_USER_EXISTS.getMessage());

		AdminUser entity = db.findById(id).get();

		if (entity.getRole() == Role.ADMIN)
			throw new GlobalException(ExceptionTypes.FORBIDDEN_ADMIN_DELETION.getStatusCode(),
					ExceptionTypes.FORBIDDEN_ADMIN_DELETION.getMessage());

//		db.deleteById(id);
		entity.setIsActive(false);
		db.save(entity);
		// improvement: del image from folder
		return true;
	}

	@Override
	public Page<AdminUserProxy> getAllUsers(Pageable pageable, String search, String role) {
	    Page<AdminUser> pageOfEntities;

	    if (search == null || search.trim().isEmpty()) {
	        if ("ADMIN".equalsIgnoreCase(role)) {
	            pageOfEntities = db.findByRole(Role.ADMIN, pageable);
	        } else {
	            pageOfEntities = db.findByRole(Role.USER, pageable);
	        }
	    } else {
	        if ("ADMIN".equalsIgnoreCase(role)) {
	            pageOfEntities = db.findByRoleAndSearch(Role.ADMIN, search, pageable);
	        } else {
	            pageOfEntities = db.findByRoleAndSearch(Role.USER, search, pageable);
	        }
	    }

	    List<AdminUserProxy> proxyList = pageOfEntities.stream()
	        .filter(entity -> Boolean.TRUE.equals(entity.getIsActive()))
	        .map(entity -> {
	            AdminUserProxy proxy = mapper.entityToProxy(entity);
	            File profileImageFile = new File(
	                "src/main/resources/static/profile_images/" + entity.getProfileImage());
	            try {
	                proxy.setProfileImage(Files.readAllBytes(profileImageFile.toPath()));
	            } catch (IOException e) {
	                // optionally log the error
	            }
	            return proxy;
	        }).collect(Collectors.toList());

	    return new PageImpl<>(proxyList, pageable, pageOfEntities.getTotalElements());
	}


	@Override
	public void exportUsersToExcel(HttpServletResponse response) {
//	    try (Workbook workbook = new XSSFWorkbook()) {
		try (Workbook workbook = new XSSFWorkbook()) {
			List<AdminUser> users = db.findByRole(Role.USER);

			Sheet sheet = workbook.createSheet("Users");

			// Header Row
			Row header = sheet.createRow(0);
			header.createCell(0).setCellValue("Name");
			header.createCell(1).setCellValue("DOB");
			header.createCell(2).setCellValue("Gender");
			header.createCell(3).setCellValue("Contact Number");
			header.createCell(4).setCellValue("Address");
			header.createCell(5).setCellValue("Pin Code");

			// Data Rows
			int rowNum = 1;
			for (AdminUser user : users) {
				Row row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue(user.getName());
				row.createCell(1).setCellValue(user.getDob().toString());
				row.createCell(2).setCellValue(user.getGender().toString());
				row.createCell(3).setCellValue(user.getContactNumber());
				row.createCell(4).setCellValue(user.getAddress());
				row.createCell(5).setCellValue(user.getPinCode());
			}

			// Set response headers
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");

			workbook.write(response.getOutputStream());
			response.getOutputStream().flush();
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	
	private static final DateTimeFormatter DOB_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	
	@Override
    public byte[] downloadExcelTemplate() {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Template");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate template", e);
        }
    }

	
	@Override
    public Map<String, Object> uploadUsers(MultipartFile file) {
        String name = file.getOriginalFilename().toLowerCase();
        try {
            if (name.endsWith(".xlsx")) {
                return uploadExcel(file);
            } else if (name.endsWith(".csv")) {
                return uploadDelimited(file, ",");
            } else { // .tsv
                return uploadDelimited(file, "\t");
            }
        } catch (Exception e) {
            throw new GlobalException(
                500, "Failed to process file: " + e.getMessage());
        }
    }

    private Map<String,Object> uploadExcel(MultipartFile file) throws IOException {
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Iterator<Row> rowIt = sheet.iterator();
            if (rowIt.hasNext()) {
                // skip header
                rowIt.next();
            }
            return processRows(row -> parseXlsRow(row), rowIt);
//            return processRows(
//                row -> parseXlsRow(row),
//                sheet.iterator()
//            );
        }
    }

    private Map<String,Object> uploadDelimited(
            MultipartFile file, String delim) throws IOException {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        // skip header
        String line = br.readLine();
        List<String[]> rows = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            if (line.isBlank()) continue;
            rows.add(line.split(delim, -1));
        }
        return processRows(
            tokens -> parseTokens(tokens),
            rows.iterator()
        );
    }

    private <T> Map<String,Object> processRows(
            RowParser<T> parser, Iterator<T> iter) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        int rowNum = 1;
        while (iter.hasNext()) {
            rowNum++;
            try {
                AdminUser u = parser.parse(iter.next());
                if (db.findByEmail(u.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("Duplicate email");
                }
                db.save(u);
                success++;
            } catch (Exception ex) {
                errors.add("Row " + rowNum + ": " + ex.getMessage());
            }
        }
        return Map.of(
          "savedUsersCount", success,
          "errors", errors
        );
    }

    private AdminUser parseXlsRow(Row r) {
        String[] vals = new String[HEADERS.length];
        for (int i = 0; i < HEADERS.length; i++) {
            Cell c = r.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            
            if (c.getCellType() == CellType.NUMERIC
             && DateUtil.isCellDateFormatted(c)) {
                vals[i] = DOB_FORMAT.format(
                  c.getLocalDateTimeCellValue().toLocalDate()
                );
            } else {
            	
//            	vals[i] = c.toString().trim();
            	
                vals[i] = switch(c.getCellType()) {
                    case STRING  -> c.getStringCellValue().trim();
                    case NUMERIC -> String.valueOf((long)c.getNumericCellValue());
                    case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
                    default      -> "";
                };
            }
        }
        return buildUser(vals);
    }

    private AdminUser parseTokens(String[] t) {
        if (t.length < HEADERS.length) {
            throw new IllegalArgumentException("Missing columns");
        }
        return buildUser(t);
    }

    private AdminUser buildUser(String[] v) {
        // v[0]=name, v[1]=email, v[2]=rawPass, v[3]=dob, v[4]=gender,
        // v[5]=pin, v[6]=contact, v[7]=address, v[8]=role
        if (v[0].isBlank() || v[1].isBlank() || v[2].isBlank()) {
            throw new IllegalArgumentException(
                "Name, Email, and Password are required");
        }

        AdminUser u = new AdminUser();
        u.setName(v[0].trim());
        u.setEmail(v[1].trim());
        u.setPassword(encoder.encode(v[2].trim()));  // assume already encoded elsewhere


        
        String rawDob = v[3].trim();
	     // if it uses slashes instead of dashes, convert:
	     rawDob = rawDob.replace('/', '-');
	     // now try parsing:
	     try {
	         LocalDate ld = LocalDate.parse(rawDob, DOB_FORMAT);
	         u.setDob(Date.valueOf(ld));
	     } catch (DateTimeParseException e1) {
	         // if that still fails, try common alternate:
	         try {
	             DateTimeFormatter alt = DateTimeFormatter.ofPattern("MM-dd-yyyy");
	             LocalDate ld2 = LocalDate.parse(rawDob, alt);
	             u.setDob(Date.valueOf(ld2));
	         } catch (Exception e2) {
//	        	 System.err.println(e2.getMessage());
	             throw new IllegalArgumentException(
	               "Invalid DOB format, expected dd-MM-yyyy"
	             );
	         }
	     }

//        // Gender
//        Gender g = switch (v[4].trim().toUpperCase()) {
//            case "0", "MALE"   -> Gender.MALE;
//            case "1", "FEMALE" -> Gender.FEMALE;
//            default -> throw new IllegalArgumentException("Invalid Gender");
//        };
//        u.setGender(g);
	     
	  // gender: "0" → MALE, "1" → FEMALE, or text
	     String gen = v[4].trim().toUpperCase();
	     Gender g;
	     if ("0".equals(gen)) {
	         g = Gender.MALE;
	     } else if ("1".equals(gen)) {
	         g = Gender.FEMALE;
	     } else {
	         try {
	             g = Gender.valueOf(gen);
	         } catch (Exception ex) {
	             throw new IllegalArgumentException("Invalid Gender");
	         }
	     }
	     u.setGender(g);    

        // Pin
        try {
            u.setPinCode(Long.parseLong(v[5].trim()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Pin Code");
        }

        u.setContactNumber(v[6].trim());
        u.setAddress(v[7].trim());

        // Role
        try {
            u.setRole(Role.valueOf(v[8].trim().toUpperCase()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Role");
        }

        u.setIsActive(true);
        return u;
    }
    
    private interface RowParser<T> {
        AdminUser parse(T source) throws Exception;
    }

	
//	@Override
//	public String addUser(AdminUserProxy adminProxy) {
//	    if (adminProxy == null) {
//	        throw new GlobalException(400, "User data cannot be null.");
//	    }
//
//	    // Check if user already exists
//	    if (db.findByEmail(adminProxy.getEmail()).isPresent()) {
//	        throw new GlobalException(409, "A user with this email already exists.");
//	    }
//
//	    // Map proxy to entity
//	    AdminUser newUser = mapper.proxyToEntity(adminProxy);
//
//	    // Encrypt password
//	    if (adminProxy.getPassword() != null && !adminProxy.getPassword().isBlank()) {
//	        newUser.setPassword(encoder.encode(adminProxy.getPassword()));
//	    } else {
//	        throw new GlobalException(400, "Password must not be empty.");
//	    }
//
//	    // Set default role if not provided
//	    if (newUser.getRole() == null) {
//	        newUser.setRole(Role.USER);
//	    }
//
//	    // Set active and timestamps
//	    newUser.setIsActive(true);
//	    newUser.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
//	    newUser.setModifiedDate(new java.sql.Timestamp(System.currentTimeMillis()));
//
//	    // Set default profile image if missing
//	    if (newUser.getProfileImage() == null || newUser.getProfileImage().isBlank()) {
//	        newUser.setProfileImage("default.png");
//	    }
//
//	    db.save(newUser);
//
//	    return "User added successfully";
//	}

	
	
//	@Override
//	public Map<String, Object> uploadUsers(MultipartFile file) {
//	    List<AdminUser> savedUsers = new ArrayList<>();
//	    List<String> errors = new ArrayList<>();
//
//	    try (InputStream inputStream = file.getInputStream()) {
//	        Workbook workbook = new XSSFWorkbook(inputStream);
//	        Sheet sheet = workbook.getSheetAt(0);
//	        int rowNumber = 0;
//
//	        for (Row row : sheet) {
//	            if (rowNumber++ == 0) continue; // skip header
//
//	            try {
//	                AdminUser user = parseRowToAdminUser(row);
//	                db.save(user);
//	                savedUsers.add(user);
//	            } catch (Exception e) {
//	                errors.add("Row " + rowNumber + ": " + e.getMessage());
//	            }
//	        }
//
//	    } catch (Exception e) {
//	        throw new RuntimeException("Failed to process Excel file: " + e.getMessage(), e);
//	    }
//
//	    Map<String, Object> result = new HashMap<>();
//	    result.put("savedUsersCount", savedUsers.size());
//	    result.put("errors", errors);
//	    return result;
//	}
//
//	
//
//	private AdminUser parseRowToAdminUser(Row row) {
//	    AdminUser user = new AdminUser();
//
//	    user.setName(getCellValue(row, 0));
//	    user.setDob(Date.valueOf(getCellValue(row, 1))); // Format: yyyy-MM-dd
//	    user.setEmail(getCellValue(row, 2));
//	    user.setPassword(encoder.encode(getCellValue(row, 3)));
//	    
//	    // Expecting gender as MALE, FEMALE, or OTHER
//	    user.setGender(Gender.valueOf(getCellValue(row, 4).toUpperCase()));
//
//	    user.setAddress(getCellValue(row, 5));
//	    user.setProfileImage(getCellValue(row, 6));
//	    user.setContactNumber(getCellValue(row, 7));
//
//	    // PinCode as numeric
//	    try {
//	    	user.setPinCode(Long.parseLong(getCellValue(row, 8)));
//
//	    } catch (NumberFormatException e) {
//	        throw new IllegalArgumentException("Invalid pinCode at column 9");
//	    }
//
//	    // Role enum: ADMIN, EMPLOYEE, etc.
//	    user.setRole(Role.valueOf(getCellValue(row, 9).toUpperCase()));
//
//	    user.setIsActive(true); // Default to active on upload
//
//	    return user;
//	}
//
//	private String getCellValue(Row row, int cellIndex) {
//	    Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
//	    return switch (cell.getCellType()) {
//	        case STRING -> cell.getStringCellValue().trim();
//	        case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
//	        case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
//	        case FORMULA -> cell.getCellFormula();
//	        case BLANK  -> "";
//		default -> throw new IllegalArgumentException("Unexpected value: " + cell.getCellType());
//	    };
//	}
//    
//	@Override
//    public byte[] downloadExcelTemplate() {
//        Workbook workbook = new XSSFWorkbook(); // Create a new workbook
//        Sheet sheet = workbook.createSheet("Template"); // Create a new sheet named "Template"
//
//        // Create a row at the top of the sheet for the headers
//        Row headerRow = sheet.createRow(0);
//
//        // Create the header cells
//        for (int i = 0; i < HEADERS.length; i++) {
//            Cell cell = headerRow.createCell(i);
//            cell.setCellValue(HEADERS[i]);
//        }
//
//        // Write the content to a byte array output stream
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        try {
//			workbook.write(byteArrayOutputStream);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        try {
//			workbook.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//        return byteArrayOutputStream.toByteArray(); // Return the byte array of the Excel file
//    }
    

//  @Override
//  public Map<String, Object> uploadUsers(MultipartFile file) {
//      List<String> errors = new ArrayList<>();
//      int successCount = 0;
//
//      try (InputStream in = file.getInputStream();
//           Workbook wb = new XSSFWorkbook(in)) {
//
//          Sheet sheet = wb.getSheetAt(0);
//          int rowNum = 0;
//          for (Row row : sheet) {
//              if (rowNum++ == 0) continue; // skip header
//
//              // skip completely blank rows
//              if (isRowEmpty(row)) continue;
//
//              try {
//                  AdminUser user = parseRowToAdminUser(row);
//                  // duplicate-email check
//                  if (db.findByEmail(user.getEmail()).isPresent()) {
//                      throw new IllegalArgumentException("Duplicate email");
//                  }
//                  db.save(user);
//                  successCount++;
//              } catch (Exception ex) {
//                  errors.add("Row " + rowNum + ": " + ex.getMessage());
//              }
//          }
//
//      } catch (Exception e) {
//          throw new GlobalException(500, "Failed to process Excel file: " + e.getMessage());
//      }
//
//      Map<String,Object> result = new HashMap<>();
//      result.put("savedUsersCount", successCount);
//      result.put("errors", errors);
//      return result;
//  }

//    private AdminUser parseRowToAdminUser(Row row) {
//        String name    = getCellValue(row, 0);
//        String email   = getCellValue(row, 1);
//        String rawPass = getCellValue(row, 2);
//        String dobStr  = getCellValue(row, 3);
//        String genderCode = getCellValue(row, 4);
//        String pinStr  = getCellValue(row, 5);
//        String contact = getCellValue(row, 6);
//        String address = getCellValue(row, 7);
//        String roleStr = getCellValue(row, 8);
//
//        if (name.isBlank() || email.isBlank() || rawPass.isBlank()) {
//            throw new IllegalArgumentException("Name, Email, and Password are required");
//        }
//
//        AdminUser u = new AdminUser();
//        u.setName(name);
//        u.setEmail(email);
//        u.setPassword(encoder.encode(rawPass));
//
//        // parse DOB
//        try {
//            LocalDate ld = LocalDate.parse(dobStr, DOB_FORMAT);
//            u.setDob(Date.valueOf(ld));
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Invalid DOB format, expected dd-MM-yyyy");
//        }
//
//        // map gender: allow "0"/"1" or text
//        Gender g;
//        if (genderCode.matches("\\d+")) {
//            g = "0".equals(genderCode) ? Gender.MALE : Gender.FEMALE;
//        } else {
//            try {
//                g = Gender.valueOf(genderCode.toUpperCase());
//            } catch (Exception ex) {
//                throw new IllegalArgumentException("Invalid Gender");
//            }
//        }
//        u.setGender(g);
//
//        // pin code
//        try {
//            u.setPinCode(Long.parseLong(pinStr));
//        } catch (NumberFormatException ex) {
//            throw new IllegalArgumentException("Invalid Pin Code");
//        }
//
//        u.setContactNumber(contact);
//        u.setAddress(address);
//
//        // role
//        try {
//            u.setRole(Role.valueOf(roleStr.toUpperCase()));
//        } catch (Exception ex) {
//            throw new IllegalArgumentException("Invalid Role");
//        }
//
//        u.setIsActive(true);
//        return u;
//    }

//    private String getCellValue(Row row, int idx) {
//        Cell c = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
//
//        switch (c.getCellType()) {
//            case STRING:
//                return c.getStringCellValue().trim();
//
//            case NUMERIC:
//                // handle Excel date cells first
//                if (DateUtil.isCellDateFormatted(c)) {
//                    // format it as dd-MM-yyyy
//                    return DOB_FORMAT.format(
//                        c.getLocalDateTimeCellValue().toLocalDate()
//                    );
//                }
//                // otherwise it's a plain number (e.g. pin code)
//                return String.valueOf((long) c.getNumericCellValue());
//
//            case BOOLEAN:
//                return String.valueOf(c.getBooleanCellValue());
//
//            default:
//                return "";
//        }
//    }
//
//    private boolean isRowEmpty(Row row) {
//        for (int i = 0; i < HEADERS.length; i++) {
//            if (!getCellValue(row, i).isBlank()) {
//                return false;
//            }
//        }
//        return true;
//    }

//	@Override
//	public String checkEmail(String email) {
//		Optional<AdminUser> byEmail = db.findByEmail(email);
//		if (byEmail.isPresent()) {
//			return "Email Already Exist";
//		}
//		return "Email Not Found";
//	}
	void contextLoads(int a, int b) {
		a=10;
		b=10;
		int c = a+b;
		System.out.println(c);
	}
    
}